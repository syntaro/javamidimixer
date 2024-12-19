package jp.synthtarou.mixtone.synth.audio;

import java.util.LinkedList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import jp.synthtarou.libs.MXQueue;
import jp.synthtarou.mixtone.synth.XTSynthesizerSetting;

import jp.synthtarou.mixtone.synth.oscilator.XTFilter;

public class JavaSourceDataLine {
    public void stop() {
        pauseThread();
    }
    
    public void close() {
        pauseThread();
    }

    public synchronized void launchThread() {
        if (_audioTrack == null) {
            XTSynthesizerSetting setting = XTSynthesizerSetting.getSetting();
            try {
                AudioFormat frmt= new AudioFormat(
                        setting.getSampleRate(), 
                        setting.getSampleBits(),
                        setting.getSampleChannel(),
                        true,
                        false);
                DataLine.Info info= new DataLine.Info(SourceDataLine.class,frmt);
                SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);
                dataLine.flush();
                dataLine.open(frmt, setting.getSamplePageSize() * setting.getSamplePageCount());
                dataLine.addLineListener(new LineListener() {
                    @Override
                    public void update(LineEvent event) {
                    }
                });
                _audioTrack = dataLine;
            } catch (LineUnavailableException e) {
                System.out.println("cant get line///");
                throw new RuntimeException(e);
            }
            _audioTrack.flush();
            _audioTrack.start();
        }

        if (_audioThread == null) {
            _audioThread = new Thread() {
                @Override
                public void run() {
                    boolean needReboot = false;
                    XTSynthesizerSetting setting = XTSynthesizerSetting.getSetting();
                    setting.clearUpdated();
                    int frame_size = setting.getSamplePageSize();
                    byte[] _stereo = new byte[frame_size /** setting.getSamplePageCount() */* 4];
                    if (_audioThread == null) {
                        try {
                            Thread.sleep(10);
                        }catch(Throwable ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (_audioTrack == null) {
                        System.err.println("Launch fatal error");
                    }
                    try {
                        while (_audioThread != null) {
                            if (setting.isUpdated()) {
                                needReboot = true;
                                break;
                            }
                            //_pool.waitForCount(setting.getSamplePageCount());
                            
                            int pos = 0;
                            //for (int i = 0; i < setting.getSamplePageCount(); ++ i) {
                                AudioSegment p = _pool.pop();
                                for (int x = 0; x < frame_size; x ++) {
                                    long sampleleft = (long)(p._left[x] * 30000);
                                    long sampleright = (long)(p._right[x] * 30000);
                                    _stereo[pos ++] = (byte)(sampleleft  & 0xff);
                                    _stereo[pos ++] = (byte)((sampleleft >> 8)  & 0xff);
                                    _stereo[pos ++] = (byte)(sampleright  & 0xff);
                                    _stereo[pos ++] = (byte)((sampleright >> 8)  & 0xff);
                                }
                                synchronized (_trashCan) {
                                    _trashCan.push(p);
                                }
                            //}
                            //if (pos >= _stereo.length) {
                                _audioTrack.write(_stereo,0,pos);
                                pos = 0;
                            //}
                        }
                    }finally {
                        if (_audioTrack != null) {
                            _audioTrack.stop();
                            _audioTrack = null;
                        }
                        _audioThread = null;
                    }
                    if (needReboot) {
                        JavaSourceDataLine.this.launchThread();
                    }
                }
            };
            _audioThread.setDaemon(true);
            _audioThread.start();
        }

    }

    public int available() {
        XTSynthesizerSetting setting = XTSynthesizerSetting.getSetting();
        int x= (setting.getSamplePageCount()- _pool.size()) * setting.getSamplePageSize();
        if (x < 0) {
            return 0;
        }
        return x;
    }

    MXQueue<AudioSegment> _pool = new MXQueue<>();
    LinkedList<AudioSegment> _trashCan = new LinkedList<>();
    class AudioSegment {
        public AudioSegment(int size) {
            _pos = 0;
            _right = new double[size];
            _left = new double[size];
        }

        int _pos;
        double[] _right;
        double[] _left;

        public void write(double r, double l) {
            _right[_pos] = r;
            _left[_pos] = l;
            _pos ++;
        }
    }

    public void write(double[] right, double[] left) {
        if (right.length == left.length) {
            write(right, left, 0, right.length);
        }else{
            throw  new IllegalArgumentException();
        }
    }

    double _mum = 32768;
    
    public void write(double[] right, double[] left, int pos, int length) {
        double x = 0;
        for (int i = pos; i < pos + length ;++ i) {
            double r = right[i] * _mum;
            double l = left[i] * _mum;
            if (r >= 0.8 || r <= -0.8 || l >= 0.8 || l <= -0.8) {
                _mum *= 0.8;
                i --;
                continue;
            }
            if (r < 0) {
                x -= r;
            }
            else {
                x += r;
            }
        }

        AudioSegment seg = null;
        while (true) {
            synchronized (_trashCan) {
                if (_trashCan.isEmpty()) {
                    break;
                }
                seg = _trashCan.removeFirst();
                if (seg._left.length == length) {
                    break;
                }   
                seg = null;
            }
        }
        if (seg == null) {
            seg = new AudioSegment(length);
        }

        int pen = 0;
        for (int i = pos; i < pos + length ;++ i) {
            double r = right[i] * _mum;
            double l = left[i] * _mum;
            r = _filterR.update(r);
            l = _filterL.update(l);
            seg._left[pen] = l;
            seg._right[pen] = r;
            pen ++;
        }
        _pool.push(seg);
    }
    XTFilter _filterL = new XTFilter();
    XTFilter _filterR = new XTFilter();

    private Thread _audioThread;
    private SourceDataLine _audioTrack;

    public boolean isRunning() {
        return (_audioThread != null);
    }

    public void pauseThread() {
        _audioThread = null;
        while(_audioThread != null) {
            try {
                Thread.sleep(10);
            }
            catch (Throwable ex) {

            }
        }
    }

    static String TAG = "AndroidSourceDataLine";
}
