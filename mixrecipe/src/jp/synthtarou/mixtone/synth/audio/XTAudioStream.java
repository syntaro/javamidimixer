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

    public boolean updateBuffer(){
        if (_sourceDL == null) {
            return false;
        }
        int did = 0;
        XTSynthesizerSetting setting = XTSynthesizerSetting.getSetting();
        int pageSize = setting.getSamplePageSize();
        if (_frame_left == null) {
            _frame_left  = new double[pageSize];
            _frame_right = new double[pageSize];
        }

        double popTo = System.currentTimeMillis() - 1.0 * setting.getSamplePageSize() /setting.getSampleRate();
        while( _sourceDL.available() >= setting.getSamplePageSize() ){
            //double pendAmount = 0;
            //int pendCount = 0;
            //int pendNotCount = 0; 
            for(int i = 0; i < pageSize; i++) {
                double valueLeft = 0;
                double valueRight = 0;
                double lastTime = 1.0 * i / setting.getSampleRate() + popTo;

                while (true) {
                    XTOscilator osc = _push.tryPeek();
                    if (osc == null) {
                        break;
                    }
                    if (osc._timing <= lastTime) {
                        _push.pop();
                        _copy.push(osc);
                        //pendNotCount ++;
                    }
                    else {
                        //pendCount ++;
                        //pendAmount += lastTime - osc._timing;
                        break;
                    }
                }

                ArrayList<XTOscilator> check = new ArrayList<>();
                for (XTOscilator j : _copy) {
                    if (j.isMuted()) {
                        check.add(j);
                    }
                }
                _copy.removeAll(check);

                for (XTOscilator j : _copy) {
                    double v = j.nextValueWithAmp() * j._volume * j._velocity;
                    double pan = j._pan;

                    double right, left;

                    right = (pan + 1) / 2;
                    left = (1 - pan) / 2;

                    switch (j._type) {
                        case 2:
                            valueRight += v * right;
                            break;
                        case 4:
                            valueLeft += v * left;
                            break;
                        default:
                            valueRight += v / 2 * right;
                            valueLeft += v / 2 * left;
                            break;
                    }
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

