/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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

    private double[] _frame_double = new double[_frame_size];
    private byte[] _frame_16 = new byte[_frame_size * 2];

    public XTAudioStream() {
        start();
    }
    
    private Timer _timer;
    private TimerTask _task;
    private SourceDataLine _sourceDL;
    
    public void start() {
        if (_task == null) {
            _timer = new Timer();
            _task = new TimerTask2(_timer);
            _timer.scheduleAtFixedRate(_task ,0,1);
            AudioFormat frmt= new AudioFormat(44100,16,1,true,false);
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
    
    public void stop() {
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
        }
    }
    
    ArrayList<XTOscilator> listRemove = null;

    public boolean updateBuffer(){
        if (_sourceDL == null) {
            return false;
        }
        int now_available = _sourceDL.available();
        if( _buffer_size - now_available < _frame_size){
            synchronized (_queue) {
                while(_queue.isEmpty() == false) {
                    _oscilator.add(_queue.remove());
                }
            }
            for(int i = 0;i < _frame_double.length;i++) {
                double value = 0;
                for (XTOscilator j : _oscilator) {
                    value += j.nextValueOfAmp();
                }
                _frame_double[i] = value;
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
            
            for (int x = 0; x < _frame_double.length; x ++) {
                double signal = _frame_double[x];
                if (signal < _streamMin) {
                    _streamMin = signal;
                }
                if (signal > _streamMax) {
                    _streamMax = signal;
                }
                
                double dif  = _streamMax - _streamMin;
                double newAudio = signal / (dif * 5);
                
                if (dif >= 10000) {
                    _streamMax = -100;
                    _streamMin = 100;
                }
                
                int sampleInt = (int)(newAudio * (1<<7));
                if (newAudio > 1) {
                    sampleInt = 1 << 8;
                }else if (newAudio < -1) {
                    sampleInt = -(1 << 8);
                }

                _frame_16[pos ++] = (byte)((sampleInt >> 8)  & 0xff);
                _frame_16[pos ++] = (byte)(sampleInt  & 0xff);
            }
            //source.write(wave_frame,buffer_size - now_available,wave_frame.length);
            _sourceDL.write(_frame_16,0,_frame_16.length);

            return true;
        }
        return false;
    }
    
    double _streamMin = 100;
    double _streamMax = -100;
}

