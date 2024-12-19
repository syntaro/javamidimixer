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

import jp.synthtarou.mixtone.synth.oscilator.XTFilter;

public class JavaSourceDataLine {
    public static final int _frame_size = XTAudioStream._frame_size;
    public static final int _buffer_rooms = XTAudioStream._buffer_rooms;
    public static final float _sampleRate = XTAudioStream._sampleRate;
    public static final int _sampleBits = XTAudioStream._sampleBits;
    public static final int _sampleChannel = XTAudioStream._sampleChannel;

    public void stop() {

    }
    public void close() {

    }
    
    private SourceDataLine _audioTrack;

    public void start() {
        if (true) {
            AudioFormat frmt= new AudioFormat(_sampleRate, _sampleBits,_sampleChannel,true,false);
            DataLine.Info info= new DataLine.Info(SourceDataLine.class,frmt);
            //if (_sourceDL == null) {
                try {
                    _audioTrack = (SourceDataLine) AudioSystem.getLine(info);
                } catch (LineUnavailableException e) {
                    System.out.println("cant get line///");
                    throw new RuntimeException(e);
                }
                _audioTrack.addLineListener(new LineListener() {
                    @Override
                    public void update(LineEvent event) {
                    }
                });
            //}
            _audioTrack.flush();
            try {
                _audioTrack.open(frmt,_frame_size * 4);
            } catch (LineUnavailableException e) {
                System.out.println("cant open line....");
                throw new RuntimeException(e);
            }
            _audioTrack.start();
        }

        if (_audioThread == null) {
            _audioThread = new Thread() {
                @Override
                public void run() {
                    byte[] _stereo = new byte[_frame_size * 4];
                    try {
                        while (_audioThread != null) {
                            AudioSegment p = _pool.pop();
                            short left = 0, right = 0;
                            
                            int pos = 0;

                            for (int x = 0; x < _frame_size; x ++) {
                                long sampleleft = (long)(p._left[x] * 30000);
                                long sampleright = (long)(p._right[x] * 30000);
                                _stereo[pos ++] = (byte)(sampleleft  & 0xff);
                                _stereo[pos ++] = (byte)((sampleleft >> 8)  & 0xff);
                                _stereo[pos ++] = (byte)(sampleright  & 0xff);
                                _stereo[pos ++] = (byte)((sampleright >> 8)  & 0xff);
                            }
                            _audioTrack.write(_stereo,0,pos);
                           synchronized (_trashCan) {
                                _trashCan.push(p);
                            }
                        }
                    }finally {
                        if (_audioTrack != null) {
                            close();
                        }
                        _audioThread = null;
                    }
                }
            };
            _audioThread.start();
        }
    }

    public int available() {
        int cnt = _pool.size();
        if (cnt >= _buffer_rooms) {
            return 0;
        }
        return (_buffer_rooms - _pool.size()) * _frame_size;
    }

    MXQueue<AudioSegment> _pool = new MXQueue<>();
    LinkedList<AudioSegment> _trashCan = new LinkedList<>();
    class AudioSegment {
        public AudioSegment() {
            int size = _frame_size;
            _pos = 0;
            _right = new double[size];
            _left = new double[size];
        }

        int _pos;
        double[] _right;
        double[] _left;

        public boolean canWrite() {
            return _pos < _right.length;
        }

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

    AudioSegment _currentWrite = new AudioSegment();
    double _mum = 32768;
    
    public void write(double[] right, double[] left, int pos, int length) {
        for (int i = pos; i < pos + length ;++ i) {
            double r = right[i] * _mum;
            double l = left[i] * _mum;
            if (r >= 0.8 || r <= -0.8 || l >= 0.8 || l <= -0.8) {
                _mum *= 0.8;
                i --;
            }
        }
        for (int i = pos; i < pos + length ;++ i) {
            double r = right[i] * _mum;
            double l = left[i] * _mum;
            if (!_currentWrite.canWrite()) {
                _pool.push(_currentWrite);
                synchronized (_trashCan) {
                    if (_trashCan.isEmpty()) {
                        _currentWrite = new AudioSegment();
                    }else {
                        _currentWrite = _trashCan.removeFirst();
                    }
                    _currentWrite._pos = 0;
                }
            }
            r = _filterR.update(r);
            l = _filterL.update(l);
            _currentWrite.write(r, l);
        }
    }
    XTFilter _filterL = new XTFilter();
    XTFilter _filterR = new XTFilter();


    static String TAG = "AndroidSourceDataLine";

    //private AudioTrack _audioTrack;
    private Thread _audioThread;

    public boolean isRunning() {
        return (_audioThread != null);
    }

    boolean _paused = false;
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
}
