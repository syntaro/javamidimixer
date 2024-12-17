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
import jp.synthtarou.mixtone.synth.oscilator.XTFilter;
import jp.synthtarou.mixtone.synth.soundfont.XTFile;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTAudioStream implements LineListener {
    private final int _frame_size = 1024;
    private final int _buffer_size = 2048;

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

    public static int _sampleChannel = 2;
    public static float _sampleRate = 44100;
    public static int _sampleBits = 16;
    public static boolean _available = true;

    private double[] _frame_left = new double[_frame_size];
    private double[] _frame_right = new double[_frame_size];
    private byte[] _stereo = new byte[_frame_size * _sampleChannel * _sampleBits / 8];

    static {
        try {
            AudioFormat frmt0 = AudioSystem.getClip().getFormat();
            _sampleChannel = frmt0.getChannels();
            _sampleRate = frmt0.getSampleRate();
            _sampleBits = frmt0.getSampleSizeInBits();
            System.err.print("Audio " +  _sampleRate +"hz ch:" + _sampleChannel + " (" + _sampleBits +" bit)");
        }catch(Throwable ex) {
            _available = false;
            System.err.print("Audio not avail " + ex.toString());
        }
    }

    public void startStream() {
        if (_task == null) {
            _timer = new Timer();
            _task = new TimerTask2(_timer);
            _timer.scheduleAtFixedRate(_task , 0, 1);
            AudioFormat frmt= new AudioFormat(_sampleRate, _sampleBits,_sampleChannel,true,false);
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
            try {
                updateBuffer();
            }catch(Throwable ex) {
                ex.printStackTrace();
            }
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
    double mumBit = 0.5;
    
    XTFilter _filterL = new XTFilter();
    XTFilter _filterR = new XTFilter();

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
                if (_oscilator.size() >= 100) {
                    System.err.println("Osc Cut " + _oscilator.size());
                    while (_oscilator.size() >= 100) {
                        _oscilator.removeFirst();
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
   
                valueLeft = _filterL.update(valueLeft);
                valueRight = _filterR.update(valueRight);
                
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
            double min = -120;
            double max = 120;
            
            for (int x = 0; x < _frame_size; x ++) {
                double sampleleft = (_frame_left[x] * mumBit);
                double sampleright = (_frame_right[x] * mumBit);

                if (sampleleft > max || sampleleft < min
                 || sampleright > max || sampleright < min) {
                    mumBit *= 0.8;
                    System.err.println("mumBit =" + mumBit);
                    x --;
                    continue;
                }
            }
            for (int x = 0; x < _frame_size; x ++) {
                long sampleleft = (long)(_frame_left[x] * _masterVolume * mumBit);
                long sampleright = (long)(_frame_right[x] * _masterVolume * mumBit);
                if (_sampleBits == 24) {
                    _stereo[pos ++] = (byte)((sampleleft >> 16)  & 0xff);
                    _stereo[pos ++] = (byte)((sampleleft >> 8)  & 0xff);
                    _stereo[pos ++] = (byte)(sampleleft  & 0xff);
                    _stereo[pos ++] = (byte)((sampleright >> 16)  & 0xff);
                    _stereo[pos ++] = (byte)((sampleright >> 8)  & 0xff);
                    _stereo[pos ++] = (byte)(sampleright  & 0xff);
                }
                else if (_sampleBits == 16) {
                    _stereo[pos ++] = 0;//(byte)((sampleleft >> 8)  & 0xff);
                    _stereo[pos ++] = (byte)(sampleleft  & 0xff);
                    _stereo[pos ++] = 0;//(byte)((sampleright >> 8)  & 0xff);
                    _stereo[pos ++] = (byte)(sampleright  & 0xff);
                }
                else if (_sampleBits == 8) {
                    _stereo[pos ++] = (byte)(sampleleft  & 0xff);
                    _stereo[pos ++] = (byte)(sampleright  & 0xff);
                }
            }
            _sourceDL.write(_stereo,0,pos);

            return true;
        }
        return false;
    }
    
    double _streamMin = 100;
    double _streamMax = -100;
}

