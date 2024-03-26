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
        _doingPaint = flag;
        if (_doingPaint != flag && flag) {
            clearCache(_songPos);
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

    BufferedImage _bufferForRoll = null;
    Graphics _bufferForRollGraphics = null;

    SMFSequencer _sequencer;
    MXPianoKeys _keys;

    public MXPianoRoll(SMFSequencer sequencer, MXPianoKeys keys) {
        _sequencer = sequencer;
        _sequencer._pianoRoll = this;
        _keys = keys;
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        clearCache(_songPos);
                    }
                });
            }

            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        clearCache(_songPos);
                    }
                });
            }
        });
    }

    BufferedImage _buff2 = null;
    Graphics _buff2Graph = null;
    int _measureIndex;
    long _measureNextTiming;
    int _measureOnLine = 0;
    boolean _showTempo = true;

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
        double startTime = _lastSongPos - _soundMargin;
        
        if (widthAll <= 0 || heightAll <= 0) {
            return;
        }

        if (_bufferForRoll != null) {
            if (widthAll != _bufferForRoll.getWidth() || heightAll != _bufferForRoll.getHeight()) {
                paintOnBuffer1();
            }
        }
        if (_bufferForRoll == null) {
            return;
        }

        if (_showTempo == false) {
            if (_lastRollingY == 0) {
                g.drawImage(_bufferForRoll, 0, 0, widthAll, heightAll, 0, 0, widthAll, heightAll, this);
            } else {
                g.drawImage(_bufferForRoll,
                        0, (int) (heightAll - _lastRollingY) - 1, widthAll, heightAll,
                        0, 0, widthAll, (int) _lastRollingY, this);
                g.drawImage(_bufferForRoll,
                        0, 0, widthAll, (int) (heightAll - _lastRollingY) + 1,
                        0, (int) _lastRollingY, widthAll, heightAll, this);
            }
        }else {
            if (_buff2 != null) {
                if (_buff2.getWidth() != widthAll || _buff2.getHeight() != heightAll) {
                    _buff2Graph.dispose();
                    _buff2 = null;
                }
            }
            if (_buff2 == null) {
                _buff2 = new BufferedImage(widthAll, heightAll, BufferedImage.TYPE_3BYTE_BGR);
                _buff2Graph = _buff2.getGraphics();
            }
            if (_lastRollingY == 0) {
                _buff2Graph.drawImage(_bufferForRoll, 0, 0, widthAll, heightAll, 0, 0, widthAll, heightAll, this);
            } else {
                _buff2Graph.drawImage(_bufferForRoll,
                        0, (int) (heightAll - _lastRollingY) - 1, widthAll, heightAll,
                        0, 0, widthAll, (int) _lastRollingY, this);
                _buff2Graph.drawImage(_bufferForRoll,
                        0, 0, widthAll, (int) (heightAll - _lastRollingY) + 1,
                        0, (int) _lastRollingY, widthAll, heightAll, this);
            }
            long[] measure = _sequencer._parser.listMeasure();

            _measureIndex = 0;
            _measureOnLine = 0;
            _measureNextTiming = 0;
            for (_measureIndex = 0; _measureIndex < measure.length; ++ _measureIndex) {
                _measureNextTiming = measure[_measureIndex];
                if (_measureNextTiming > startTime) {
                    break;
                }
            }
            Color separator = Color.white;
            if (_measureIndex < measure.length) {
                for (long y = heightAll- 1; y >= 0; --y) {
                    double distanceY = (double) (heightAll - y);
                    double distanceTime = distanceY * _soundSpan / heightAll;
                    long lineTime = (long) (startTime + distanceTime);
                    if (lineTime < _measureNextTiming) {
                        continue;
                    }
                    if (_measureOnLine <= 0) {
                            if ((_measureIndex & 3) == 0) {
                                _measureOnLine += 5;
                                separator = Color.red;
                            } else if ((_measureIndex & 1) == 0) {
                                _measureOnLine += 2;
                                separator = Color.yellow;
                            }else {
                                _measureOnLine ++;
                                separator = Color.yellow;
                            }

                        _measureIndex++;
                        if (_measureIndex -1 >= measure.length) {
                            break;
                        }
                        _measureNextTiming = measure[_measureIndex-1];
                    }
                    else {
                        for (int x = _measureOnLine % 1; x < widthAll; x += 2) {
                            if (_buff2.getRGB(x, (int)y) == back.getRGB()) {
                                _buff2.setRGB(x, (int) y, separator.getRGB());
                            }
                        }
                        _measureOnLine--;
                    }
                }
            }
            g.drawImage(_buff2, 0, 0, widthAll, heightAll, this);
        }
    }
    
    protected void paintOnBuffer1() {
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

        if (_bufferForRoll != null) {
            if (widthAll != _bufferForRoll.getWidth() || heightAll != _bufferForRoll.getHeight()) {
                _bufferForRoll = null;
            }
        }
        if (_bufferForRoll == null) {
            if (_bufferForRollGraphics != null) {
                _bufferForRollGraphics.dispose();
                _bufferForRollGraphics = null;
            }
            _bufferForRoll = new BufferedImage(widthAll, heightAll, BufferedImage.TYPE_3BYTE_BGR);
            _bufferForRollGraphics = _bufferForRoll.getGraphics();
        }
        if (_bufferForRoll != null) {
            //描画するべき時間の長さ
            long needDraw;
            needDraw = _songPos - _lastSongPos;
            if (needDraw >= _soundSpan) {
                needDraw = _soundSpan;
            }

            //lsatSongPosからsongPosまで描写する、Y座標としていくつ違うか
            long y0 = (long) (((_lastSongPos) * 1.0 / _soundSpan) * heightAll);
            long y1 = (long) (((_songPos) * 1.0 / _soundSpan) * heightAll);

            long startDrawY = _rollingY;
            double startTime = _lastSongPos + _soundSpan - _soundMargin;

            for (long y = y0; y <= y1; ++y) {
                double distanceY = (double) (y - y0);
                double distanceTime = distanceY * _soundSpan / heightAll;
                long linetime = (long) (startTime + distanceTime);
                int[] playing = listPaintNote(linetime);
                int keysRoot = _keyboardRoot;
                int keysCount = _keyboardOctave * 12;
                int onlyDrum = 1 << 9;
                long realY2 = startDrawY - y + y0;
                while (realY2 <= 0) {
                    realY2 += heightAll;
                }
                while (realY2 > heightAll) {
                    realY2 -= heightAll;
                }
                _bufferForRollGraphics.setColor(back);
                _bufferForRollGraphics.drawLine((int) 0, (int) realY2, (int) widthAll, (int) realY2);
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
                        _bufferForRollGraphics.setColor(col);
                        _bufferForRollGraphics.drawLine((int) from, (int) realY2, (int) to, (int) realY2);
                    }
                }
            }

            _rollingY -= y1 - y0;
            while (_rollingY < 0) {
                _rollingY += heightAll;
            }
            _lastSongPos = _songPos;
            _lastRollingY = _rollingY;
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
        _measureIndex = 0;
        _measureNextTiming = 0;
        _measureOnLine = 0;

        _rollingY = 0;
        _lastRollingY = 0;
        _songPos = position;
        _lastSongPos = _songPos - _soundSpan;
        _lastPickupForKick = 0;
        _keys.allNoteOff();
        for (int i = 0; i < _kickedChannelList.length; ++i) {
            _kickedChannelList[i] = 0;
        }
        _lastPickupForPaint = 0;
        for (int i = 0; i < _pickedNoteForPaint.length; ++i) {
            _pickedNoteForPaint[i] = 0;
        }
        _bufferForRoll = null;
        if (_bufferForRollGraphics != null) {
            _bufferForRollGraphics.dispose();
            _bufferForRollGraphics = null;
        }
        _keys.repaint();
        paintOnBuffer1();
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

    public long getSongPos() {
        return _songPos;
    }

    long _songPos = 0;
    long _soundSpan = 4000;
    long _lastSongPos = -_soundSpan;
    long _soundMargin = 200;
    long _rollingY = 0;
    long _lastRollingY = 0;
    Color back = new Color(0, 50, 50);

    public void setFocusChannel(int ch) {
        _focusChannel = ch;
        clearCache(_songPos);
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
        comp._songPos = 0;
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
        clearCache(_songPos);
    }

    public void setSoundMargin(long margin) {
        _soundMargin = margin;
        clearCache(_songPos);
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
        _songPos = elapsed;

        noteForKick(_songPos);
        _keys.repaint();
        paintOnBuffer1();
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
