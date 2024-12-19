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

import jp.synthtarou.mixtone.main.XTSynthesizer;
import jp.synthtarou.mixtone.synth.oscilator.XTOscilator;

import java.util.ArrayList;
import java.util.LinkedList;
import jp.synthtarou.libs.MXQueue;
import static jp.synthtarou.mixtone.synth.audio.XTAudioStream._sampleRate;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTAudioStream{
    static final String TAG=  "XTAudioStream";
    //private final int _frame_size = AndroidSourceDataLine._bufferSize;
    public static final int _frame_size = 512;
    public static final int _buffer_rooms = 1;

    static XTAudioStream _instance;
    XTSynthesizer _synth;
    
    public static XTAudioStream getInstance(XTSynthesizer synth) {
        if (_instance == null) {
            _instance = new XTAudioStream(synth);
        }
        return _instance;
    }

    private XTAudioStream(XTSynthesizer synth) {
        _synth = synth;
    }
    
    private JavaSourceDataLine _sourceDL;
    public double _masterVolume = 1.0;

    public static final int _sampleChannel = 2;
    public static final float _sampleRate = 48000;
    public static final int _sampleBits = 16;
    public static final boolean _available = true;

    private double[] _frame_left = new double[_frame_size];
    private double[] _frame_right = new double[_frame_size];
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
                    double span = 1000.0 * _frame_size /  _sampleRate;
                    while(true) {
                        long start = System.currentTimeMillis();
                        if (_renderThread == null) {
                            break;
                        }
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
            //_renderThread.setPriority(Thread.MAX_PRIORITY);
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
            _sourceDL.start();
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
        while( _sourceDL.available() >= _frame_size ){
            for(int i = 0; i < _frame_size; i++) {

                double valueLeft = 0;
                double valueRight = 0;

                while (_push.isEmpty() == false) {
                    _copy.push(_push.pop());
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

            _sourceDL.write(_frame_right, _frame_left);
            did ++;
        }
        return did > 0;
    }

    LinkedList<XTOscilator> _copy = new LinkedList<>();
    MXQueue<XTOscilator> _push = new MXQueue<>();

    public void push(XTOscilator osc) {
        _push.push(osc);
    }
}

