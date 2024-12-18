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

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTAudioStream implements LineListener {
    private final int _frame_size = 512;
    private final int _frame_bytes = _frame_size * 4;
    private final int _buffer_rooms = 3;

    static XTAudioStream _instance;
    
    public static XTAudioStream getInstance() {
        if (_instance == null) {
            _instance = new XTAudioStream();
        }
        return _instance;
    }

    private XTAudioStream() {
    }
    
    private SourceDataLine _sourceDL;
    public double _masterVolume = 1.0;

    public static int _sampleChannel = 2;
    public static float _sampleRate = 44100;
    public static int _sampleBits = 16;
    public static boolean _available = true;

    private double[] _frame_left = new double[_frame_size];
    private double[] _frame_right = new double[_frame_size];
    private byte[] _stereo = new byte[_frame_bytes];

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
    
    Thread _renderThread = null;
    
    public void startThread() {
        if (_renderThread == null) {
            _renderThread = new Thread() {
                public void run() {
                    while(true) {
                        if (_renderThread == null) {
                            break;
                        }
                        double span = 1000.0 * _frame_size /  _sampleRate;
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
            //_renderThread.setPriority(Thread.MAX_PRIORITY);
            _renderThread.start();
        }
    }
    
    public void stopThread() {
        _renderThread = null;
    }

    public void startStream() {
        if (_renderThread == null) {
            startThread();
            AudioFormat frmt= new AudioFormat(_sampleRate, _sampleBits,_sampleChannel,true,false);
            DataLine.Info info= new DataLine.Info(SourceDataLine.class,frmt);
            //if (_sourceDL == null) {
                try {
                    _sourceDL = (SourceDataLine) AudioSystem.getLine(info);
                } catch (LineUnavailableException e) {
                    System.out.println("cant get line///");
                    throw new RuntimeException(e);
                }
                _sourceDL.addLineListener(this);
            //}
            _sourceDL.flush();
            try {
                _sourceDL.open(frmt,_frame_bytes * _buffer_rooms);
            } catch (LineUnavailableException e) {
                System.out.println("cant open line....");
                throw new RuntimeException(e);
            }
            _sourceDL.start();
        }
    }
    
    public void stopStream() {
        if (_renderThread != null) {
            stopThread();
        }
    }
    
    @Override
    public void update(LineEvent event) {
        System.err.println(event);
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
        /*
        Thread t = Thread.currentThread();
        if (t.getPriority() != Thread.MAX_PRIORITY) {
            t.setPriority(Thread.MAX_PRIORITY);
        }*/
        int did = 0;
        int avail = _sourceDL.available();
        //System.err.println("avail " + avail + " bytes " + _frame_bytes + " x 3");
        while( /*(_frame_bytes * _buffer_rooms) - */_sourceDL.available() >= _frame_bytes ){
            for(int i = 0;i < _frame_size; i++) {
                synchronized (_queue) {
                    while(_queue.isEmpty() == false) {
                        _oscilator.add(_queue.remove());
                    }
                }
                if (_oscilator.size() > 64) {
                    System.err.println("Osc Cut " + _oscilator.size());
                    while (_oscilator.size() > 64) {
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
                    _stereo[pos ++] = (byte)((sampleleft >> 8)  & 0xff);
                    _stereo[pos ++] = (byte)(sampleleft  & 0xff);
                    _stereo[pos ++] = (byte)((sampleright >> 8)  & 0xff);
                    _stereo[pos ++] = (byte)(sampleright  & 0xff);
                }
                else if (_sampleBits == 8) {
                    _stereo[pos ++] = (byte)(sampleleft  & 0xff);
                    _stereo[pos ++] = (byte)(sampleright  & 0xff);
                }
            }
            did ++;
            _sourceDL.write(_stereo,0,pos);
        }
        if (did >= 2) {
            System.err.println("Did " + did + " Frames" );
        }
        return did > 0;
    }
    
    double _streamMin = 100;
    double _streamMax = -100;
}

