/*
 * Copyright (C) 2024 Syntarou YOSHIDA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jp.synthtarou.mixtone.synth.audio;

import jp.synthtarou.mixtone.synth.XTSynthesizer;
import jp.synthtarou.mixtone.synth.oscilator.XTOscilator;

import java.util.ArrayList;
import java.util.LinkedList;
import jp.synthtarou.libs.MXQueue;
import jp.synthtarou.mixtone.synth.XTSynthesizerSetting;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTAudioStream{
    static final String TAG=  "XTAudioStream";
    XTSynthesizer _synth;
    
    public XTAudioStream(XTSynthesizer synth) {
        _synth = synth;
    }
    
    private JavaSourceDataLine _sourceDL;
    public double _masterVolume = 1.0;
    public boolean _available = true;
    private double[] _frame_left;
    private double[] _frame_right;
    Thread _renderThread = null;
    
    private void startThread() {
        if (_renderThread == null) {
            _renderThread = new Thread() {
                public void run() {
                    while (_renderThread == null) {
                        try {
                            Thread.sleep(10);
                        }catch (Throwable xe) {

                        }
                    }
                    XTSynthesizerSetting setting = XTSynthesizerSetting.getSetting();
                    int pageSize = setting.getSamplePageSize() * setting.getSamplePageCount();
                    double span = 1000.0 * setting.getSampleRate()/ pageSize;
                    while(_renderThread != null) {
                        long start = System.currentTimeMillis();
                        try {
                            boolean proced = updateBuffer();
                            
                            if (proced) {
                                continue;
                            }

                            long reserve = (long)span + start;
                            long current = System.currentTimeMillis();
                            long waiting = reserve - current;
                            if (waiting > 10) {
                                synchronized (this) {
                                    this.wait(5);
                                }
                                start = System.currentTimeMillis();
                            }else if (waiting >= 3) {
                                synchronized (this) {
                                    this.wait(1);
                                }
                                start = System.currentTimeMillis();
                            }
                            else {
                                start = current;
                            }
                        }catch(Throwable ex) {
                            ex.printStackTrace();
                        }
                    }
                    _renderThread = null;
                    _sourceDL.stop();
                    _sourceDL.close();
                    _sourceDL = null;
                }
            };
            _renderThread.start();
        }
    }

    public boolean isReady() {
        return _renderThread != null && _sourceDL != null;
    }
    public void stopThread() {
        _renderThread = null;
    }

    public void startStream() {
        if (_sourceDL == null) {
            _sourceDL = new JavaSourceDataLine();
            _sourceDL.launchThread();
            startThread();
        }
    }
    
    public void stopStream() {
        if (_sourceDL != null) {
            _sourceDL.stop();
            stopThread();
        }
    }

    ArrayList<XTOscilator> _muteCheck = new ArrayList<>();

    public boolean updateBuffer(){
        if (_sourceDL == null) {
            return false;
        }
        int did = 0;
        XTSynthesizerSetting setting = XTSynthesizerSetting.getSetting();
        int pageSize = setting.getSamplePageSize();
        if (_frame_left == null || _frame_left.length != pageSize) {
            _frame_left  = new double[pageSize];
            _frame_right = new double[pageSize];
        }

        while (true) {
            XTOscilator osc = _push.tryPeek();
            if (osc == null) {
                break;
            }
            _push.pop();
            _copy.push(osc);
        }

        _muteCheck.clear();
        for (XTOscilator j : _copy) {
            if (j.isMuted()) {
                _muteCheck.add(j);
            }
        }
        _copy.removeAll(_muteCheck);
        //double popTo = System.currentTimeMillis() - 1.0 * setting.getSamplePageSize() /setting.getSampleRate();
        while( _sourceDL.available() >= pageSize ){
            for(int i = 0; i < pageSize; i++) {
                double valueLeft = 0;
                double valueRight = 0;

                for (XTOscilator j : _copy) {
                    double v = j.nextValueWithAmp() * j._volume * j._velocity;
                    double pan = j._pan;
                    if (j._type == 2) {
                        pan = 1;
                    }else if (j._type == 4) {
                        pan = -1;
                    }
                    pan += 1;
                    pan /= 2;
                    double right = pan, left = 1 - pan;

                    valueRight += v / 2 * right;
                    valueLeft += v / 2 * left;
                }

                _frame_left[i] = valueLeft;
                _frame_right[i] = valueRight;
            }
            /*
            if (pendCount != 0) {
                System.err.println("pend count " +pendCount + " amount " + pendAmount + " (pendNot " + pendNotCount + ")");
            }
            else if (pendNotCount != 0) {
                System.err.println("pend count " +pendCount + " amount " + pendAmount + " (pendNot " + pendNotCount + ")");
            }*/

            _sourceDL.write(_frame_right, _frame_left);
            did ++;
        }
        return did > 0;
    }

    LinkedList<XTOscilator> _copy = new LinkedList<>();
    MXQueue<XTOscilator> _push = new MXQueue<>();

    public void push(XTOscilator osc) {
        osc._timing = System.currentTimeMillis();
        _push.push(osc);
    }
}

