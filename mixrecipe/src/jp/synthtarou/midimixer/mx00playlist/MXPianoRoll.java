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
package jp.synthtarou.midimixer.mx00playlist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.mx00playlist.MXPianoKeys.KeyRect;
import jp.synthtarou.libs.smf.SMFMessage;
import jp.synthtarou.libs.smf.SMFSequencer;
import jp.synthtarou.libs.smf.SMFTestSynthesizer;
import jp.synthtarou.midimixer.MXMain;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXPianoRoll extends JComponent {

    boolean _doingPaint = true;

    public void setDoingPaint(boolean flag) {
        MXMain.invokeUI(() ->  {
            _doingPaint = flag;
            if (_doingPaint != flag && flag) {
                setPosition(_position, true);
            }
        });
    }

    public static class NoteRect {

        Rectangle _rect = new Rectangle();
        final int _note;

        public NoteRect(int note) {
            this._note = note;
        }

        public boolean isValid() {
            if (_note >= 0 && _note < 128) {
                return true;
            }
            return false;
        }
    }

    ArrayList<NoteRect> _whiteKeysList = new ArrayList<NoteRect>();
    ArrayList<NoteRect> _blackKeysList = new ArrayList<NoteRect>();

    SimpleRGBCanvas _canvas;
    SMFSequencer _sequencer;
    MXPianoKeys _keys;

    boolean _highlightTiming = true;

    public MXPianoRoll(SMFSequencer sequencer, MXPianoKeys keys) {
        _sequencer = sequencer;
        _sequencer._pianoRoll = this;
        _keys = keys;
        _canvas = new SimpleRGBCanvas();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                MXMain.invokeUI(() ->  {
                    setPosition(_position, true);
                });
            }

            @Override
            public void componentShown(ComponentEvent e) {
                MXMain.invokeUI(() ->  {
                    setPosition(_position, true);
                });
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        if (!_doingPaint) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread() == false) {
            throw new IllegalStateException();
        }
        int widthAll = getWidth();
        int heightAll = getHeight();

        if (widthAll <= 0 || heightAll <= 0) {
            return;
        }

        _canvas.prepare(widthAll, heightAll);
        //if (_canvas._prepareReseted) {
            paintOnBuffer1(_position);
        //}

        if (_lastRollingY == 0) {
            g.drawImage(_canvas._image, 0, 0, widthAll, heightAll, 0, 0, widthAll, heightAll, this);
        } else {
            g.drawImage(_canvas._image,
                    0, (int) (heightAll - _lastRollingY) - 1, widthAll, heightAll,
                    0, 0, widthAll, (int) _lastRollingY, this);
            g.drawImage(_canvas._image,
                    0, 0, widthAll, (int) (heightAll - _lastRollingY) + 1,
                    0, (int) _lastRollingY, widthAll, heightAll, this);
        }
    }

    byte[] _colorSeparator = null; //SimpleRGBCanvas.colorToBgr(MXUtil.mixtureColor(Color.red, 60, Color.black, 40), null);
    byte[] _colorBackBlack = SimpleRGBCanvas.colorToBgr(MXUtil.mixtureColor(Color.cyan, 20, Color.black, 80, Color.red, 10), null);
    byte[] _colorBackGray2 = SimpleRGBCanvas.colorToBgr(MXUtil.mixtureColor(MXUtil.mixtureColor(Color.cyan, 70, Color.white, 20), 20, Color.black, 50), null);
    byte[] _colorBackGray4 = SimpleRGBCanvas.colorToBgr(MXUtil.mixtureColor(Color.orange, 20, Color.red, 5, Color.black, 50), null);

    protected void paintOnBuffer1(long currentMilliSec) {
        if (!_doingPaint) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread() == false) {
            throw new IllegalStateException();
        }
        int widthAll = getWidth();
        int heightAll = getHeight();
        if (widthAll <= 0 || heightAll <= 0) {
            return;
        }

        _canvas.prepare(widthAll, heightAll);
        if (_canvas._prepareReseted || _lastMilliSec < 0) {
            _lastRollingY = 0;
            _rollingY = 0;
            _lastMilliSec = -_spanMilliSec;

            _lastCheckedForRolling = 0;
            if (_keys != null) {
                _nextStartForPaintFooterKeys = 0;
                for (int noteNumber = 0; noteNumber < _listNotePressedChannel.length; ++noteNumber) {
                    _listNotePressedChannel[noteNumber] = 0;
                }
                _keys.allNoteOff();
                controlFooterKeys(currentMilliSec);
                _keys.repaint();
            }
        }
        if (_canvas.isReady()) {
            long last = _lastMilliSec;
            if (currentMilliSec - last >= _spanMilliSec) {
                last = currentMilliSec - _spanMilliSec;
            }
            long y0 = (long) (((last) * 1.0 / _spanMilliSec) * heightAll);
            long y1 = (long) (((currentMilliSec) * 1.0 / _spanMilliSec) * heightAll);

            _lastMilliSec = currentMilliSec;

            double startTime = last + _spanMilliSec - _soundMargin;
            byte[] colBuff = new byte[3];
            byte[] peekBuff = new byte[3];
            int lastMeasure = _sequencer._parser.calcMeasureByMilliseconds((long) (startTime - _spanMilliSec / heightAll));

            for (long y = y0; y < y1; ++y) {
                double distanceY = (double) (y - y0);
                double distanceTime = distanceY * _spanMilliSec / heightAll;
                long linetime = (long) (startTime + distanceTime);
                int[] playing = calcPlayingNote(linetime);
                int keysRoot = _keyboardRoot;
                int keysCount = _keyboardOctave * 12;
                int onlyDrum = 1 << 9;
                int realY2 = (int) (_rollingY - y + y0);
                while (realY2 < 0) {
                    realY2 += heightAll;
                }
                while (realY2 >= heightAll) {
                    realY2 -= heightAll;
                }

                _canvas.line(0, realY2, widthAll, realY2, _back);
                int measure = _sequencer._parser.calcMeasureByMilliseconds(linetime);
                byte[] color;
                if (!_highlightTiming) {
                    color = _colorBackBlack;
                } else {
                    if (lastMeasure != measure && _colorSeparator != null) {
                        color = _colorSeparator;
                        lastMeasure = measure;
                    } else {
                        switch (measure % 8) {
                            case 0:
                                color = _colorBackGray4;
                                break;
                            case 2:
                            case 4:
                            case 6:
                                color = _colorBackGray2;
                                break;
                            default:
                                color = _colorBackBlack;
                        }
                    }
                }

                _canvas.line((int) 0, realY2, (int) widthAll - 1, realY2, color);

                for (int i = keysRoot; i < keysRoot + keysCount; ++i) {
                    boolean selected = false;
                    if (playing[i] > 0 && playing[i] != onlyDrum) {
                        selected = true;
                        double from = 1.0 * (i - keysRoot) * widthAll / keysCount;
                        double to = 1.0 * (i - keysRoot + 1) * widthAll / keysCount;
                        double div = (to - from) / 2;
                        if (_keys != null) {
                            KeyRect keysrect = _keys.findRectByNote(i);
                            if (keysrect == null) {
                                continue;
                            }
                            int center = (keysrect._rect.x + keysrect._rect.width / 2);
                            int blackWidth = _keys.getBlackKeysWidth();
                            from = center - blackWidth / 2 + (blackWidth / 4);
                            to = center + blackWidth / 2 + (blackWidth / 4) + 1;
                        }
                        to -= div;
                        Color col = bitColor(playing[i]);
                        if (col == null) {
                            continue;
                        }
                        byte[] col2 = SimpleRGBCanvas.colorToBgr(col, null);
                        _canvas.line((int) from, realY2, (int) to, realY2, col2);
                    }
                }
            }

            _lastRollingY = _rollingY;
            _rollingY -= y1 - y0;
            while (_rollingY < 0) {
                _rollingY += heightAll;
            }
        }
    }

    static Comparator<NoteRect> noteComp = new Comparator<NoteRect>() {
        @Override
        public int compare(NoteRect o1, NoteRect o2) {
            if (o1._note < o2._note) {
                return -1;
            }
            if (o1._note > o2._note) {
                return +1;
            }
            return 0;
        }
    };

    public int[] calcPlayingNote(long milliSeconds) {
        List<SMFMessage> list = _sequencer.listMessage();
        if (_lastCheckedForRolling < list.size()) {
            if (milliSeconds < list.get(_lastCheckedForRolling)._millisecond) {
                _lastCheckedForRolling = 0;
            }
        }
        if (_lastCheckedForRolling == 0) {
            for (int i = 0; i < _currentKeyColor.length; ++i) {
                _currentKeyColor[i] = 0;
            }
        }
        for (int x = _lastCheckedForRolling; x < list.size(); ++x) {
            SMFMessage smf = list.get(x);
            if (smf._millisecond > milliSeconds) {
                break;
            }
            _lastCheckedForRolling = x;

            long time = smf._millisecond;
            int command = smf.getStatus() & 0xf0;
            int channel = smf.getStatus() & 0x0f;
            int channelbit = 1 << channel;
            int note = smf.getData1();
            int velocity = smf.getData2();

            switch (command) {
                case MXMidi.COMMAND_CH_NOTEON:
                    if (velocity >= 1) {
                        _currentKeyColor[note] |= channelbit;
                        break;
                    }
                case MXMidi.COMMAND_CH_NOTEOFF:
                    _currentKeyColor[note] &= ~channelbit;
                    break;
            }
        }
        return _currentKeyColor;
    }

    int _lastCheckedForRolling = 0;
    int[] _currentKeyColor = new int[128];

    int _nextStartForPaintFooterKeys = 0;
    int[] _listNotePressedChannel = new int[128];

    private void controlFooterKeys(long milliSeconds) {
        if (_keys == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread() == false) {
            throw new IllegalStateException();
        }
        boolean lockBuffered = false;
        List<SMFMessage> list = _sequencer.listMessage();
        if (list != null && _nextStartForPaintFooterKeys < list.size()) {
            if (_nextStartForPaintFooterKeys == 0 
              || milliSeconds < list.get(_nextStartForPaintFooterKeys)._millisecond) {
                if (_keys != null) {            
                    lockBuffered = true;
                    _keys.setOnlyBuffer(true);
                   _keys.allNoteOff();
                }
                for (int note = 0; note < _listNotePressedChannel.length; ++ note) {
                    _listNotePressedChannel[note] = 0;
                }
                _nextStartForPaintFooterKeys = 0;
            }
        }

        for (int x = _nextStartForPaintFooterKeys; x < list.size(); ++x) {
            SMFMessage smf = list.get(x);
            if (smf._millisecond > milliSeconds) {
                break;
            }
            _nextStartForPaintFooterKeys = x;

            long time = smf._millisecond;
            int command = smf.getStatus() & 0xf0;
            int channel = smf.getStatus() & 0x0f;
            int channelbit = 1 << channel;
            int note = smf.getData1();
            int velocity = smf.getData2();

            if (channel == 9) {
                continue;
            }

            if (_focusChannel >= 0) {
                if (channel != _focusChannel) {
                    continue;
                }
            }

            switch (command) {
                case MXMidi.COMMAND_CH_NOTEON:
                    if (velocity >= 1) {
                        if (_listNotePressedChannel[note] == 0) {
                            _keys.noteOn(note);
                        }
                        _listNotePressedChannel[note] |= 1 << channel;
                        break;
                    }
                case MXMidi.COMMAND_CH_NOTEOFF:
                    if ((_listNotePressedChannel[note] & (1 << channel)) != 0) {
                        _listNotePressedChannel[note] -= (1 << channel);
                    }
                    if (_listNotePressedChannel[note] == 0) {
                        _keys.noteOff(note);
                    }
                    break;
            }
        }
        if (lockBuffered){            
            _keys.setOnlyBuffer(false);
        }
    }

    public long getPosition() {
        return _position;
    }

    long _position = 0;
    long _spanMilliSec = 4000;
    long _lastMilliSec = -_spanMilliSec;
    long _soundMargin = 200;
    long _rollingY = 0;
    long _lastRollingY = 0;
    byte[] _back = SimpleRGBCanvas.colorToBgr(new Color(0, 50, 50), new byte[3]);

    public void setFocusChannel(int ch) {
        _focusChannel = ch;
        setPosition(_position, true);
    }

    public int getFocuChannel() {
        return _focusChannel;
    }

    static Color[] colors = new Color[]{
            Color.pink, Color.cyan, Color.orange, Color.blue, Color.green, Color.yellow, Color.red, Color.white
        };
    Color[] hibridSource = new Color[16];
    int _focusChannel = -1;
    
    public static Color channelColor(int channel) {
        if (channel == -1) {
            int count = 1;
            return MXUtil.mixtureColor(new Color(50, 50, 50, 255), 4 - count, Color.white, 4 + count);
        }
        Color c = colors[channel % colors.length];
        return c;
    }

    public Color bitColor(int channel) {
        if (_focusChannel >= 0) {
            int mask = 1 << _focusChannel;
            if ((channel & mask) == mask) {
                return Color.orange;
            }
            int count = 0;
            for (int b = 0; b < 16; ++b) {
                int bit = 1 << b;
                if ((channel & bit) == bit) {
                    if (b == 9) {
                        continue;
                    }
                    count++;
                }
            }
            if (count > 4) {
                count = 4;
            }
            if (count == 0) {
                return null;
            }
            return MXUtil.mixtureColor(new Color(50, 50, 50, 255), 4 - count, Color.white, 4 + count);
        } else {
            int count = 0;
            for (int seekCh = 0; seekCh < 16; ++seekCh) {
                hibridSource[seekCh] = null;
                if (seekCh == 9) {
                    continue;
                }
                int seekBit = 1 << seekCh;
                if ((channel & seekBit) == seekBit) {
                    Color c = colors[seekCh % colors.length];
                    hibridSource[seekCh] = c;
                    count++;
                }
            }
            if (count == 0) {// durm only
                return null;
            }
            return MXUtil.mixedColorXYZ(hibridSource);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SMFSequencer sequencer = new SMFSequencer(new File("C:/midi/SynthTAROU000.mid"));
        JFrame win = new JFrame("Piano");
        win.setBounds(0, 0, 600, 400);
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Insets ins = win.getInsets();
        win.setLayout(new GridLayout(1, 1));
        MXPianoRoll comp = new MXPianoRoll(sequencer, null);
        comp._keyboardOctave = 10;
        win.add(comp);
        win.setVisible(true);
        comp._position = 0;
        sequencer._progressSpan = 50;
        MXPianoKeys key = new MXPianoKeys();

        sequencer.startPlayerThread(0, new SMFTestSynthesizer());
    }

    public long getSoundSpan() {
        return _spanMilliSec;
    }

    public long getSoundMargin() {
        return _soundMargin;
    }

    public void setSoundSpan(long span) {
        _spanMilliSec = span;
        setPosition(_position, true);
    }

    public void setSoundMargin(long margin) {
        _soundMargin = margin;
        setPosition(_position, true);
    }

    public void setHighlightTiming(boolean show) {
        _highlightTiming = show;
        setPosition(_position, true);
    }

    public void setPosition(long elapsed, boolean clearCache) {
        if (!SwingUtilities.isEventDispatchThread()) {
            MXMain.invokeUI(() ->  {
                setPosition(elapsed, clearCache);
            });
            return;
        }
        if (elapsed < _position || clearCache) {
            _lastMilliSec = -1;
        }
        _position = elapsed;
        if (_keys != null) {
            controlFooterKeys(_position);
            _keys.repaint();
        }
        //invalidate();
        //paintOnBuffer1(elapsed);
        repaint();
    }

    private int _keyboardRoot = 36;
    private int _keyboardOctave = 4;

    public void setNoteRange(int rootNote, int octave) {
        _keyboardRoot = rootNote;
        _keyboardOctave = octave;
        _keys.setAllowSelect(true, true);
    }
}
