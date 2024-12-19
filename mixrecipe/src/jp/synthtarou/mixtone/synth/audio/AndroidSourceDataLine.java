package jp.synthtarou.mixtone.synth.audio;

import java.util.LinkedList;
import javax.sound.sampled.AudioFormat;
import jp.synthtarou.libs.MXQueue;

import jp.synthtarou.mixtone.synth.oscilator.XTFilter;

public class AndroidSourceDataLine {
    /*
    public static int _frameRate = 48000;
    public static final int _bitRate = 16;
    public static final int _bufferSize = 512;
    public static final int _bufferCount = 5;
    public static final int _polyPhony = 48;


    public void stop() {

    }
    public void close() {

    }

    public void start() {
        if (_audioTrack == null) {
            int CONTENT_TYPE = AudioAttributes.CONTENT_TYPE_MUSIC;
            int CONTENT_USAGE = AudioAttributes.USAGE_MEDIA;
            int STREAM_TYPE = AudioManager.STREAM_MUSIC;

            AudioManager am = MainActivity._audioManager;

            int MODE = AudioTrack.MODE_STREAM;

            AudioFormat.Builder format = new AudioFormat.Builder();
            format.setEncoding(AudioFormat.ENCODING_PCM_16BIT);
            format.setSampleRate(_frameRate);
            format.setChannelMask(AudioFormat.CHANNEL_OUT_STEREO);

            AudioAttributes.Builder attributes = new AudioAttributes.Builder();
            attributes.setContentType(CONTENT_TYPE);
            attributes.setUsage(CONTENT_USAGE);

            AudioTrack.Builder builder = new AudioTrack.Builder();

            builder.setAudioFormat(format.build());
            builder.setTransferMode(MODE);
            builder.setAudioAttributes(attributes.build());
            builder.setBufferSizeInBytes(_bufferSize);

            _audioTrack = builder.build();
            _audioTrack.setPositionNotificationPeriod(_bufferSize);
            _audioTrack.play();
        }else {
            _audioTrack.play();
        }

        if (_audioThread == null) {
            _audioThread = new Thread() {
                @Override
                public void run() {
                    short[] sBuffer = new short[_bufferSize*2];
                    try {
                        while (_audioThread != null) {
                            AudioSegment p = _pool.pop();
                            short left = 0, right = 0;

                            for (int i = 0; i < _bufferSize; i++) {
                                left = (short)(p._left[i] * Short.MAX_VALUE / 2);
                                right = (short)(p._right[i] * Short.MAX_VALUE / 2);
                                sBuffer[i * 2] = left;
                                sBuffer[i * 2 + 1] = right;
                            }
                            _audioTrack.write(sBuffer, 0, sBuffer.length);
                            synchronized (_trashCan) {
                                _trashCan.push(p);
                            }
                        }
                    }finally {
                        if (_audioTrack != null) {
                            _audioTrack.stop();
                            _audioTrack.flush();
                            _audioTrack.release();
                            _audioTrack = null;
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
        if (cnt >= _bufferCount) {
            return 0;
        }
        return (_bufferCount - _pool.size()) * _bufferSize;
    }

    double _div = 10000.0;
    MXQueue<AudioSegment> _pool = new MXQueue<>();
    LinkedList<AudioSegment> _trashCan = new LinkedList<>();
    class AudioSegment {
        public AudioSegment() {
            int size = _bufferSize;
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

    public void write(double[] right, double[] left, int pos, int length) {
        for (int i = pos; i < pos + length ;++ i) {
            double r = right[i] * _div;
            double l = left[i] * _div;
            if (r <= -1 || r >= 1) {
                _div *= 0.8;
                i--;
                continue;
            }
            if (l <= -1 || l >= 1) {
                _div *= 0.8;
                i--;
                continue;
            }
        }
        for (int i = pos; i < pos + length ;++ i) {
            double r = right[i] * _div * 0.8;
            double l = left[i] * _div * 0.8;
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

    private AudioTrack _audioTrack;
    private Thread _audioThread;

    public boolean isRunning() {
        return (_audioThread != null);
    }

    boolean _paused = false;
    public void pauseThread() {
        _audioThread = null;
        while(_audioTrack != null) {
            try {
                Thread.sleep(10);
            }
            catch (Throwable ex) {

            }
        }
    }
    */
}
