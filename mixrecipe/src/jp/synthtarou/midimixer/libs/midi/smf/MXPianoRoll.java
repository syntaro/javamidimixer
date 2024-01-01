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
package jp.synthtarou.midimixer.libs.midi.smf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.smf.MXPianoKeys.KeyRect;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXPianoRoll extends JComponent {

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
    BufferedImage _bufferedImage2 = null;
    Graphics _bufferedImageGraphics = null;
    Graphics _bufferedImageGraphics2 = null;
    Rectangle _bufferedRect = null;
    SMFSequencer _sequencer;
    MXPianoKeys _keys;

    public MXPianoRoll(SMFSequencer sequencer, MXPianoKeys keys) {
        _sequencer = sequencer;
        _keys = keys;
        _sequencer._pianoRoll = this;
        _sequenceNoteOn = new boolean[256];
        _sequenceSustain = new boolean[256];
    }

    @Override
    public synchronized void paintComponent(Graphics g) {
        int widthAll = getWidth(), heightAll = getHeight();
        Rectangle bounds = g.getClipBounds();

        if (_bufferedImage != null) {
            if (widthAll != _bufferedImage.getWidth() || heightAll != _bufferedImage.getHeight()) {
                _bufferedImage = null;
                _bufferedImage2 = null;
            }
        }
        if (_bufferedImage == null) {
            paintOnBuffer(null);
            bounds = new Rectangle(_bufferedImage.getWidth(), _bufferedImage.getHeight());
        }

        Rectangle rect = _bufferedRect;
        if (rect == null && bounds == null) {
            rect = new Rectangle(widthAll, heightAll);
        }
        if (rect == null) {
            rect = bounds;
        } else if (bounds != null) {
            rect = rect.union(bounds);
        }
        _bufferedRect = null;

        g.drawImage(_bufferedImage,
                rect.x, rect.y, rect.x + rect.width, rect.y + rect.height,
                rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, this);
        if (_soundMargin >= 150) {
            double pos = getHeight() * 1.0 / _soundSpan * _soundMargin;
            int y = (int) (getHeight() - pos);
            g.setColor(new Color(255, 255, 0, 50));
            g.fillRect(0, y, getWidth(), getHeight() - y);
        }
    }

    public void invalidate() {
        _bufferedImage = null;
        super.invalidate();
    }

    public synchronized void paintOnBuffer(Rectangle rect) {
        int widthAll = getWidth();
        int heightAll = getHeight();

        if (_bufferedImage != null) {
            if (widthAll != _bufferedImage.getWidth() || heightAll != _bufferedImage.getHeight()) {
                _bufferedImage = null;
                _bufferedImage2 = null;
            }
        }
        if (_bufferedImage == null) {
            if (_bufferedImageGraphics != null) {
                _bufferedImageGraphics.dispose();
            }
            if (_bufferedImageGraphics2 != null) {
                _bufferedImageGraphics2.dispose();
            }
            _bufferedImage = new BufferedImage(widthAll, heightAll, BufferedImage.TYPE_3BYTE_BGR);
            _bufferedImage2 = new BufferedImage(widthAll, heightAll, BufferedImage.TYPE_3BYTE_BGR);
            _bufferedImageGraphics = _bufferedImage.getGraphics();
            _bufferedImageGraphics2 = _bufferedImage2.getGraphics();
            paintOnGraphics(null);
            _lastSongPos = -1;
            _bufferedRect = new Rectangle(0, 0, widthAll, heightAll);
        } else {
            paintOnGraphics(rect);
            Rectangle a = _bufferedRect;
            if (rect == null) {
                rect = new Rectangle(widthAll, heightAll);
            }
            if (a == null) {
                a = rect;
            } else {
                a = a.union(rect);
            }
            _bufferedRect = a;
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

    public void clearPickedNote() {
        _lastPickupForPaint = 0;
        for (int i = 0; i < _pickedNoteForPaint.length; ++i) {
            _pickedNoteForPaint[i] = 0;
        }
    }

    public int[] listNoteOn(long milliSeconds) {
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

    int []kicked = new int[128];
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
    long _soundMargin = 500;
    Color back = new Color(0, 50, 50);
    
    private void paintOnGraphics(Rectangle rect) {
        clearPickedNote();
        int width = getWidth();
        int height = getHeight();

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

        _bufferedImageGraphics2.drawImage(_bufferedImage, 0, 0, this);
        _bufferedImageGraphics.drawImage(_bufferedImage2, 0, shiftY, this);
        _bufferedImageGraphics.setColor(back);
        _bufferedImageGraphics.fillRect(0, 0, width, shiftY);

        for (int y = shiftY - 1; y >= 0; --y) {
            double dig = 1.0 * (shiftY - y) / height * _soundSpan;
            long pos = (long) dig + _songPos + _soundSpan - needDraw;
            int[] playing = listNoteOn(pos - _soundMargin);
            int keysRoot = _keyboardRoot;
            int keysCount = _keyboardOctave * 12;
            for (int i = keysRoot; i < keysRoot + keysCount; ++i) {
                boolean selected = false;
                if (playing[i] > 0) {
                    Color col = bitColor(playing[i]);
                    if (col == null) {
                        continue;
                    }
                    _bufferedImageGraphics.setColor(col);
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
                        to = center +  blackWidth / 2 + (blackWidth / 4) + 1;
                    }
                    to -= div;
                    _bufferedImageGraphics.drawLine((int) from, y, (int) to, y);
                }
            }
        }
    }

    Color[] colors = {
        Color.green, Color.cyan, Color.red, Color.yellow, Color.pink, Color.white, Color.gray
    };
    Color[] hibridSource = new Color[16];

    public Color bitColor(int channel) {
        int count = 0;
        for (int b = 0; b < 16; ++b) {
            hibridSource[b] = null;
            if (b == 9) {
                continue;
            }
            int bit = 1 << b;
            if ((channel & bit) == bit) {
                Color c = colors[b % colors.length];
                hibridSource[b] = c;
                count++;
            }
        }
        if (count == 0) {// durm only
            return null;
        }
        int red = 0, green = 0, blue = 0;
        for (int i = 0; i < 16; ++i) {
            Color c = hibridSource[i];
            if (c != null) {
                red += c.getRed();
                green += c.getGreen();
                blue += c.getBlue();
            }
        }
        red /= count * 2;
        green /= count * 2;
        blue /= count * 2;
        return new Color(red + 64, green + 64, blue + 64);
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

    public void resetTiming(long span, long margin) {
        _lastSongPos = -1;
        _songPos = 0;
        _soundSpan = span;
        _soundMargin = margin;
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
        paintOnBuffer(null);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                noteForKick(_songPos);
                repaint();
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
