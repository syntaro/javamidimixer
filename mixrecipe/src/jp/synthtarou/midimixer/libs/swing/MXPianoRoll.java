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
package jp.synthtarou.midimixer.libs.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.swing.MXPianoKeys.KeyRect;
import jp.synthtarou.midimixer.libs.midi.smf.SMFMessage;
import jp.synthtarou.midimixer.libs.midi.smf.SMFSequencer;
import jp.synthtarou.midimixer.libs.midi.smf.SMFTestSynthesizer;
import jp.synthtarou.midimixer.libs.navigator.MXPopupForList;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXPianoRoll extends JComponent {

    boolean _doPaint = true;

    public void setSelectedPaint(boolean flag) {
        _doPaint = flag;
        if (flag) {
            clearPickedNote();
        }
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

    boolean[] _sequenceNoteOn;
    boolean _sequenceSustainGlobal;
    boolean[] _sequenceSustain;

    BufferedImage _bufferedImage = null;
    Graphics _bufferedImageGraphics = null;
    SMFSequencer _sequencer;
    MXPianoKeys _keys;

    public MXPianoRoll(SMFSequencer sequencer, MXPianoKeys keys) {
        _sequencer = sequencer;
        _keys = keys;
        _sequencer._pianoRoll = this;
        _sequenceNoteOn = new boolean[256];
        _sequenceSustain = new boolean[256];
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                MXWrapList<Integer> list = new MXWrapList();
                list.addNameAndValue("Colorful", -1);
                for (int i = 0; i < 16; ++i) {
                    list.addNameAndValue("CH." + (i + 1), i);
                }
                MXPopupForList popu = new MXPopupForList(null, list) {
                    @Override
                    public void approvedIndex(int selectedIndex) {
                        setFocusChannel(list.valueOfIndex(selectedIndex));
                    }
                };
                popu.setSelectedIndex(list.indexOfValue(_focusChannel));
                popu.showPopup(MXPianoRoll.this);
            }
        });
    }

    @Override
    public synchronized void paintComponent(Graphics g) {
        if (!_doPaint) {
            return;
        }
        int widthAll = getWidth();
        int heightAll = getHeight();

        if (_bufferedImage != null) {
            if (widthAll != _bufferedImage.getWidth() || heightAll != _bufferedImage.getHeight()) {
                paintOnBuffer();
            }
        }
        if (_bufferedImage != null) {
            g.drawImage(_bufferedImage,
                    0, 0, widthAll - 1, (int) (heightAll - _rollingY - 1),
                     0, (int) _rollingY, widthAll - 1, heightAll - 1, this);

            g.drawImage(_bufferedImage,
                    0, (int) (heightAll - _rollingY - 1), widthAll - 1, heightAll - 1,
                     0, 0, widthAll - 1, (int) (_rollingY), this);
        }
    }

    public synchronized void paintOnBuffer() {
        if (!_doPaint) {
            return;
        }
        int widthAll = getWidth();
        int heightAll = getHeight();

        if (_bufferedImage != null) {
            if (widthAll != _bufferedImage.getWidth() || heightAll != _bufferedImage.getHeight()) {
                _bufferedImage = null;
            }
        }
        if (_bufferedImage == null) {
            if (_bufferedImageGraphics != null) {
                _bufferedImageGraphics.dispose();
            }
            _bufferedImage = null;
            _bufferedImageGraphics = null;
            if (widthAll > 0 && heightAll > 0) {
                _bufferedImage = new BufferedImage(widthAll, heightAll, BufferedImage.TYPE_3BYTE_BGR);
                _bufferedImageGraphics = _bufferedImage.getGraphics();
            }
        }
        if (_bufferedImage != null) {
            int width = getWidth();
            int height = getHeight();

            if (!_doPaint) {
                return;
            }

            long needDraw = _soundSpan;
            if (_lastSongPos >= 0) {
                needDraw = _songPos - _lastSongPos;
                if (needDraw >= _soundSpan) {
                    needDraw = _soundSpan;
                }
            }

            int shiftY = (int) ((1.0 * needDraw * height) / _soundSpan);
            if (shiftY == 0) {
                return;
            }

            _lastSongPos = _songPos;
            _lastRollingY = _rollingY;
            long x = 0;

            for (int y = shiftY - 1; y >= 0; --y) {
                double dig = 1.0 * (shiftY - y) / height * _soundSpan;
                long pos = (long) dig + _songPos + _soundSpan - needDraw;
                int[] playing = listNoteOn(pos - _soundMargin);
                int keysRoot = _keyboardRoot;
                int keysCount = _keyboardOctave * 12;
                int onlyDrum = 1 << 9;
                long realY2 = _rollingY - (x++);
                realY2 = (realY2 + height) % height;
                _bufferedImageGraphics.setColor(back);
                _bufferedImageGraphics.drawLine((int) 0, (int) realY2, (int) width, (int) realY2);
                for (int i = keysRoot; i < keysRoot + keysCount; ++i) {
                    boolean selected = false;
                    if (playing[i] > 0 && playing[i] != onlyDrum) {
                        selected = true;
                        double from = 1.0 * (i - keysRoot) * width / keysCount;
                        double to = 1.0 * (i - keysRoot + 1) * width / keysCount;
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
                        _bufferedImageGraphics.setColor(col);
                        _bufferedImageGraphics.drawLine((int) from, (int) realY2, (int) to, (int) realY2);
                    }
                }
            }
            _rollingY -= shiftY;
            if (_rollingY < 0) {
                _rollingY = height;
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

    public NoteRect findRectByNote(ArrayList<NoteRect> list, int note) {
        int from = 0;
        int to = list.size() - 1;
        while (from <= to) {
            int x = (to - from) / 2 + from;
            NoteRect o = list.get(x);
            if (o._note < note) {
                from = x + 1;
                to = to;
                continue;
            }
            if (note < o._note) {
                from = from;
                to = x - 1;
                continue;
            }
            return o;
        }
        return null;
    }

    int _lastPickupForPaint = 0;
    int[] _pickedNoteForPaint = new int[256];
    int _lastPickupForKick = 0;

    public synchronized void clearPickedNote() {
        _songPos = 0;
        _lastSongPos = -1;
        _lastPickupForKick = 0;
        _lastPickupForPaint = 0;
        for (int i = 0; i < _pickedNoteForPaint.length; ++i) {
            _pickedNoteForPaint[i] = 0;
        }
        _rollingY = 0;
        _bufferedImage = null;
        _keys.allNoteOff();
        repaint();
    }

    public synchronized int[] listNoteOn(long milliSeconds) {
        List<SMFMessage> list = _sequencer.listMessage();
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

    int[] kicked = new int[128];

    public void noteForKick(long milliSeconds) {
        List<SMFMessage> list = _sequencer.listMessage();
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
                        if (kicked[note] == 0) {
                            _keys.noteOn(note);
                        }
                        kicked[note] |= 1 << channel;
                        break;
                    }
                case MXMidi.COMMAND_CH_NOTEOFF:
                    if ((kicked[note] & (1 << channel)) != 0) {
                        kicked[note] -= (1 << channel);
                    }
                    if (kicked[note] == 0) {
                        _keys.noteOff(note);
                    }
                    break;
            }
        }
    }

    long _lastSongPos = -1;
    long _songPos = 0;
    long _soundSpan = 4000;
    long _soundMargin = 10;
    long _rollingY = 0;
    long _lastRollingY = 0;
    Color back = new Color(0, 50, 50);

    public void setFocusChannel(int ch) {
        _focusChannel = ch;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (this) {
                    clearPickedNote();
                }
            }
        });
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
            return MXUtil.mixtureColor(new Color(50, 50, 50), 4 - count, Color.white, 4 + count);
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
        comp._songPos = 0;
        sequencer._progressSpan = 50;
        MXPianoKeys key = new MXPianoKeys();

        sequencer.startPlayer(new SMFTestSynthesizer());
    }

    public long getSoundSpan() {
        return _soundSpan;
    }

    public long getSoundMargin() {
        return _soundMargin;
    }

    public void setTiming(long pos) {
        long needDraw = _soundSpan;
        if (_lastSongPos >= 0) {
            needDraw = pos - _lastSongPos;
            if (needDraw >= _soundSpan) {
                needDraw = _soundSpan;
            }
        }

        int height = getHeight();
        int shiftY = (int) ((1.0 * needDraw * height) / _soundSpan);
        if (shiftY <= 5) {
            return;
        }

        _songPos = pos;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (MXPianoRoll.this) {
                    paintOnBuffer();
                    noteForKick(_songPos);
                    repaint();
                }
            }
        });
    }

    private int _keyboardRoot = 36;
    private int _keyboardOctave = 4;

    public void setNoteRange(int rootNote, int octave) {
        _keyboardRoot = rootNote;
        _keyboardOctave = octave;
        _keys.setAllowSelect(true, true);
    }
}
