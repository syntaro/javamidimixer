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
import jp.synthtarou.midimixer.libs.common.MXGlobalTimer;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.mx00playlist.MXPianoKeys.KeyRect;
import jp.synthtarou.midimixer.libs.midi.smf.SMFMessage;
import jp.synthtarou.midimixer.libs.midi.smf.SMFSequencer;
import jp.synthtarou.midimixer.libs.midi.smf.SMFTestSynthesizer;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXPianoRoll extends JComponent {

    boolean _doingPaint = true;

    public void setDoingPaint(boolean flag) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                _doingPaint = flag;
                if (_doingPaint != flag && flag) {
                    clearCache(_soundTiming);
                }
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

    boolean _showMeasure = true;

    public MXPianoRoll(SMFSequencer sequencer, MXPianoKeys keys) {
        _sequencer = sequencer;
        _sequencer._pianoRoll = this;
        _keys = keys;
        _canvas = new SimpleRGBCanvas();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        clearCache(_soundTiming);
                    }
                });
            }

            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        clearCache(_soundTiming);
                    }
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
        if (_canvas._prepareReseted) {
            paintOnBuffer1(_soundTiming);
        }

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
    
    static long _static_x = 0;
    static long _static_y = 0;

    protected Color measureColor(long timing) {
        long[] measure = _sequencer._parser.listMeasure();
        int measureIndex;
        long measureNextTiming;
        int measureOnLine = 0;

        measureIndex = 0;
        measureOnLine = 0;
        measureNextTiming = 0;
        Color col = null;
        
        for (measureIndex = 0; measureIndex < measure.length; ++measureIndex) {
            measureNextTiming = measure[measureIndex];
            
            if ((measureIndex & 3) == 0) {
                measureOnLine = 40;
                col = Color.red;
            } else if ((measureIndex & 1) == 0) {
                measureOnLine = 30;
                col = Color.yellow;
            } else {
                measureOnLine = 20;
                col = Color.yellow;
            } 
            
            long distance = measureNextTiming - timing;
            if (distance >= 0 && distance <= measureOnLine) {
                return col;
            }
        }
        return null;
    }
    
    protected void paintOnBuffer1(long timing) {
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
        if (_canvas._prepareReseted || timing < _doneTiming) {
            _lastRollingY = Integer.MAX_VALUE;
            _doneTiming = -_soundSpan;
            _lastPickupForKick = 0;
            _keys.allNoteOff();
            for (int i = 0; i < _kickedChannelList.length; ++i) {
                _kickedChannelList[i] = 0;
            }
            _lastPickupForPaint = 0;
            for (int i = 0; i < _pickedNoteForPaint.length; ++i) {
                _pickedNoteForPaint[i] = 0;
            }
            _keys.repaint();
        }
        if (_canvas.isReady()) {
            //描画するべき時間の長さ
            long needDraw;
            needDraw = _soundTiming - _doneTiming;
            if (needDraw >= _soundSpan) {
                needDraw = _soundSpan;
            }

            //lsatSongPosからsongPosまで描写する、Y座標としていくつ違うか
            long y0 = (long) (((_doneTiming) * 1.0 / _soundSpan) * heightAll);
            long y1 = (long) (((timing) * 1.0 / _soundSpan) * heightAll);

            double startTime = _doneTiming + _soundSpan - _soundMargin;
            byte[] colBuff = new byte[3];
            byte[] peekBuff = new byte[3];

            for (long y = y0; y < y1; ++y) {
                double distanceY = (double) (y - y0);
                double distanceTime = distanceY * _soundSpan / heightAll;
                long linetime = (long) (startTime + distanceTime);
                int[] playing = listPaintNote(linetime);
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
                Color c = measureColor(linetime);
                if (c != null) {
                    SimpleRGBCanvas.colorToBgr(c, colBuff);
                    for (int x = 0; x < widthAll; x += 2) {
                        _canvas.setPixel(x, realY2, colBuff);
                    }
                }
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
            _doneTiming = _soundTiming;
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

    int _lastPickupForPaint = 0;
    int[] _pickedNoteForPaint = new int[256];
    int _lastPickupForKick = 0;

    public void clearCache(long position) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    clearCache(position);
                }
            });
            return;
        }
        _canvas.dispose(); //reserve reset
        _keys.repaint();
        _doneTiming = 0;
        _soundTiming = position;
        paintOnBuffer1(position);
        repaint();
    }

    public int[] listPaintNote(long milliSeconds) {
        List<SMFMessage> list = _sequencer.listMessage();
        if (list != null && list.size() > _lastPickupForPaint) {
            if (milliSeconds < list.get(_lastPickupForPaint)._millisecond) {
                _lastPickupForPaint = 0;
                for (int i = 0; i < _pickedNoteForPaint.length; ++i) {
                    _pickedNoteForPaint[i] = 0;
                }
            }
        }
        for (int x = _lastPickupForPaint; x < list.size(); ++x) {
            SMFMessage smf = list.get(x);
            if (smf._millisecond > milliSeconds) {
                break;
            }
            _lastPickupForPaint = x;

            long time = smf._millisecond;
            int command = smf.getStatus() & 0xf0;
            int channel = smf.getStatus() & 0x0f;
            int channelbit = 1 << channel;
            int note = smf.getData1();
            int velocity = smf.getData2();

            switch (command) {
                case MXMidi.COMMAND_CH_NOTEON:
                    if (velocity >= 1) {
                        _pickedNoteForPaint[note] |= channelbit;
                        break;
                    }
                case MXMidi.COMMAND_CH_NOTEOFF:
                    _pickedNoteForPaint[note] &= ~channelbit;
                    break;
            }
        }
        return _pickedNoteForPaint;
    }

    int[] _kickedChannelList = new int[128];

    private void noteForKick(long milliSeconds) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            throw new IllegalStateException();
        }
        List<SMFMessage> list = _sequencer.listMessage();
        if (list != null && list.size() > _lastPickupForKick) {
            if (milliSeconds < list.get(_lastPickupForKick)._millisecond) {
                _keys.allNoteOff();
                _lastPickupForKick = 0;
            }
        }

        for (int x = _lastPickupForKick; x < list.size(); ++x) {
            SMFMessage smf = list.get(x);
            if (smf._millisecond > milliSeconds) {
                break;
            }
            _lastPickupForKick = x;

            long time = smf._millisecond;
            int command = smf.getStatus() & 0xf0;
            int channel = smf.getStatus() & 0x0f;
            int channelbit = 1 << channel;
            int note = smf.getData1();
            int velocity = smf.getData2();

            if (channel == 9) {
                continue;
            }

            if (channel != _focusChannel && _focusChannel >= 0) {
                continue;
            }

            switch (command) {
                case MXMidi.COMMAND_CH_NOTEON:
                    if (velocity >= 1) {
                        if (_kickedChannelList[note] == 0) {
                            _keys.noteOn(note);
                        }
                        _kickedChannelList[note] |= 1 << channel;
                        break;
                    }
                case MXMidi.COMMAND_CH_NOTEOFF:
                    if ((_kickedChannelList[note] & (1 << channel)) != 0) {
                        _kickedChannelList[note] -= (1 << channel);
                    }
                    if (_kickedChannelList[note] == 0) {
                        _keys.noteOff(note);
                    }
                    break;
            }
        }
    }

    public long getSoundTiming() {
        return _soundTiming;
    }

    long _soundTiming = 0;
    long _soundSpan = 4000;
    long _doneTiming = -_soundSpan;
    long _soundMargin = 200;
    long _rollingY = 0;
    long _lastRollingY = 0;
    byte []_back = SimpleRGBCanvas.colorToBgr(new Color(0, 50, 50), new byte[3]);

    public void setFocusChannel(int ch) {
        _focusChannel = ch;
        clearCache(_soundTiming);
    }

    public int getFocuChannel() {
        return _focusChannel;
    }

    Color[] colors = null;
    Color[] hibridSource = new Color[16];
    int _focusChannel = -1;

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
            for (int b = 0; b < 16; ++b) {
                hibridSource[b] = null;
                if (b == 9) {
                    continue;
                }
                int bit = 1 << b;
                if ((channel & bit) == bit) {
                    if (colors == null) {
                        colors = new Color[]{
                            Color.pink, Color.cyan, Color.orange, Color.blue, Color.green, Color.yellow, Color.red, Color.white
                        };
                    }
                    Color c = colors[b % colors.length];
                    hibridSource[b] = c;
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
        comp._soundTiming = 0;
        sequencer._progressSpan = 50;
        MXPianoKeys key = new MXPianoKeys();

        sequencer.startPlayerThread(0, new SMFTestSynthesizer());
    }

    public long getSoundSpan() {
        return _soundSpan;
    }

    public long getSoundMargin() {
        return _soundMargin;
    }

    public void setSoundSpan(long span) {
        _soundSpan = span;
        clearCache(_soundTiming);
    }

    public void setSoundMargin(long margin) {
        _soundMargin = margin;
        clearCache(_soundTiming);
    }

    public void setShowMeasure(boolean show) {
        _showMeasure = show;
        clearCache(_soundTiming);
    }

    public void setSoundTiming(long elapsed) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setSoundTiming(elapsed);
                }
            });
            return;
        }
        if (elapsed < _soundTiming) {
            clearCache(elapsed);
        }else {            
            _soundTiming = elapsed;
            noteForKick(_soundTiming);
            _keys.repaint();
            paintOnBuffer1(elapsed);
            repaint();
        }
    }

    private int _keyboardRoot = 36;
    private int _keyboardOctave = 4;

    public void setNoteRange(int rootNote, int octave) {
        _keyboardRoot = rootNote;
        _keyboardOctave = octave;
        _keys.setAllowSelect(true, true);
    }
}
