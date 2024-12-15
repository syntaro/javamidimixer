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

import jp.synthtarou.mixtone.synth.oscilator.XTOscilator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTAudioStream implements LineListener {
    private final int _frame_size = 1000;
    private final int _buffer_size = 4000;

    private double[] _frame_left = new double[_frame_size];
    private double[] _frame_right = new double[_frame_size];
    private byte[] _frame_16_stere = new byte[_frame_size * 2 * 2];
    
    static XTAudioStream _instance;
    
    public static XTAudioStream getInstance() {
        if (_instance == null) {
            _instance = new XTAudioStream();
        }
        return _instance;
    }

    private XTAudioStream() {
    }
    
    private Timer _timer;
    private TimerTask _task;
    private SourceDataLine _sourceDL;
    public double _masterVolume = 1.0;

    public void startStream() {
        if (_task == null) {
            _timer = new Timer();
            _task = new TimerTask2(_timer);
            _timer.scheduleAtFixedRate(_task , 0, 1);
            AudioFormat frmt= new AudioFormat(44100,16,2,true,false);
            DataLine.Info info= new DataLine.Info(SourceDataLine.class,frmt);
            if (_sourceDL == null) {
                try {
                    _sourceDL = (SourceDataLine) AudioSystem.getLine(info);
                } catch (LineUnavailableException e) {
                    System.out.println("cant get line///");
                    throw new RuntimeException(e);
                }
                _sourceDL.addLineListener(this);
            }
            _sourceDL.flush();
            try {
                _sourceDL.open(frmt,_buffer_size);
            } catch (LineUnavailableException e) {
                System.out.println("cant open line....");
                throw new RuntimeException(e);
            }
            _sourceDL.start();
        }
    }
    
    public void stopStream() {
        if (_task != null) {
            _sourceDL.stop();
            _sourceDL.close();
            _sourceDL = null;
            _task.cancel();
            _task = null;
            _timer.cancel();
            _timer = null;
        }
    }
    
    @Override
    public void update(LineEvent event) {
    }
    
    class TimerTask2 extends TimerTask {
        Timer _timer; 

        public TimerTask2(Timer timer) {
            _timer = timer;
        }

        @Override
        public void run() {
            updateBuffer();
        }
    }
    
    LinkedList<XTOscilator> _oscilator = new LinkedList<>();
    LinkedList<XTOscilator> _queue = new LinkedList<>();

    public void addToQueue(XTOscilator osc) {
        synchronized (_queue) {
            _queue.add(osc);
            //System.err.println(osc._name + " : "+  osc._playKey);
        }
    }
    
    ArrayList<XTOscilator> listRemove = null;
    int mumBit = 256;

    public boolean updateBuffer(){
        if (_sourceDL == null) {
            return false;
        }
        int now_available = _sourceDL.available();
        if( _buffer_size - now_available < _frame_size * 2){
            for(int i = 0;i < _frame_size; i++) {
                synchronized (_queue) {
                    while(_queue.isEmpty() == false) {
                        _oscilator.add(_queue.remove());
                    }
                }
                double valueLeft = 0;
                double valueRight = 0;
                for (XTOscilator j : _oscilator) {
                    double v = j.nextValueWithAmp();
                    double pan = j._pan;
                    double vol = j._volume;
                    
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

            for (XTOscilator j : _oscilator) {
                if (j.isClose()) {
                    if (listRemove == null) {
                        listRemove = new ArrayList<>();
                    }
                    listRemove.add(j);
                }
            }
            if (listRemove != null && listRemove.isEmpty() == false) {
                _oscilator.removeAll(listRemove);
                listRemove.clear();
            }
            
            int pos = 0;
            int min = -10000;
            int max = 10000;
            
            for (int x = 0; x < _frame_size; x ++) {
                double sampleleft = (_frame_left[x] * mumBit);
                double sampleright = (_frame_right[x] * mumBit);
                
                if (sampleleft > max) {
                    mumBit -= 20;
                    x --;
                    continue;
                }
                if (sampleleft < min) {
                    sampleleft = min;
                    mumBit -= 20;
                    x --;
                    continue;
                }
                if (sampleright > max) {
                    sampleright = max;
                    mumBit -= 20;
                    x --;
                    continue;
                }
                if (sampleright < min) {
                    sampleright = min;
                    mumBit -= 20;
                    x --;
                    continue;
                }
            }
            for (int x = 0; x < _frame_size; x ++) {
                int sampleleft = (int)(_frame_left[x] * _masterVolume * mumBit);
                int sampleright = (int)(_frame_right[x] * _masterVolume * mumBit);
                _frame_16_stere[pos ++] = (byte)((sampleleft >> 8)  & 0xff);
                _frame_16_stere[pos ++] = (byte)(sampleleft  & 0xff);
                _frame_16_stere[pos ++] = (byte)((sampleright >> 8)  & 0xff);
                _frame_16_stere[pos ++] = (byte)(sampleright  & 0xff);
            }
            _sourceDL.write(_frame_16_stere,0,pos);

            return true;
        }
        return false;
    }
    
    double _streamMin = 100;
    double _streamMax = -100;
}

