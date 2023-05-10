/*
 * Copyright 2023 Syntarou YOSHIDA.
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXPianoComponent extends JComponent {

    /**
     * @return the allowMultiSelect
     */
    public boolean isAllowMultiSelect() {
        return _allowMultiSelect;
    }

    /**
     * @param allowMultiSelect the allowMultiSelect to set
     */
    public void setAllowMultiSelect(boolean allowMultiSelect) {
        this._allowMultiSelect = allowMultiSelect;
    }

    public static interface Handler {
        public void noteOn(int note);
        public void noteOff(int note);
        public void selectionChanged();
    }
    
    Handler _handler;
    
    public void setHandler(Handler handler) {
        _handler = handler;
    }
    
    ArrayList<KeyRect> _whiteKeysList = new ArrayList<KeyRect>();
    ArrayList<KeyRect> _blackKeysList = new ArrayList<KeyRect>();

    int _mouseNoteOn = -1;
    boolean[] _sequenceNoteOn;
    boolean _sequenceSustainGlobal;
    boolean[] _sequenceSustain;
    
    BufferedImage _bufferedImage = null;
    Graphics _bufferedImageGraphics = null;
    Rectangle _bufferedRect = null;
 
    private int _rootNote = 36;
    private int _keyboardOctave = 4;
    
    public MXPianoComponent() {
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                releaseNoteByMouse();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                MXPianoComponent.this.pushNoteByMouse(e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                releaseNoteByMouse();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                MXPianoComponent.this.pushNoteByMouse(e.getPoint());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });
        setSize(new Dimension(100, 30));
        _sequenceNoteOn = new boolean[256];
        _sequenceSustain  = new boolean[256];
    }
    
    static int[] whiteNote = { 0, 2, 4, 5, 7, 9, 11 };
    static int[] blackNoteLow = { 1, 3 };
    static int[] blackNoteHi = { 6, 8, 10 };
    
    public void setNoteRange(int rootNote, int octave) {
        _rootNote = rootNote;
        _keyboardOctave = octave;
    }
    
    private void makeWhiteAndBlack1Oct(int rootNote, double offset, double width, double height, Collection<KeyRect> noteListWhite, Collection<KeyRect> noteListBlack, double whiteWidth) {
        double whiteHeight = height;
        int count;
        count = 0;

        for(int i = 0; i < 7; ++ i) {
            double fromX = width * i / 7.0 + offset;
            double fromY = 0;

            KeyRect key = new KeyRect(whiteNote[count ++] + rootNote);
            if (key.isValid()) {
                key._rect.setBounds((int)fromX, (int)fromY, (int)whiteWidth, (int)whiteHeight);
                noteListWhite.add(key);
            }
        }

        double  blackLeftWidth = width * 3.0 / 7.0;
        double  blackHeight = height * 5.0 / 7.0;
        double blackKeyWidth =blackLeftWidth / 6.0;
        count = 0;

        for (int i = 0; i < 6; ++i) {
            if (i == 2 || i == 4) {

                double fromX = (int)(blackLeftWidth * i / 6.0 - (blackKeyWidth / 2.0) + offset);
                double fromY = 0;
                KeyRect key = new KeyRect(blackNoteLow[count ++] + rootNote);
                if (key.isValid()) {
                    key._rect.setBounds((int)fromX, (int)fromY,  (int)blackKeyWidth, (int)blackHeight);
                    noteListBlack.add(key);
                }
            }
        }

        double blackRightWidth = width * 4 / 7;
        double rightOffset = blackLeftWidth;
        count = 0;

        for (int i = 0; i < 8; ++i) {
            if (i == 2 || i == 4 || i == 6) {
                double fromX = blackRightWidth * i / 8 + rightOffset - (blackKeyWidth / 2) + offset;
                double fromY = 0;

                KeyRect key = new KeyRect(blackNoteHi[count ++] + rootNote);
                if (key.isValid()) {
                    key._rect.setBounds((int)fromX, (int)fromY, (int)blackKeyWidth, (int)blackHeight);
                    noteListBlack.add(key);
                }
            }
        }
    }

    private void makeWhiteAndBlack(int rootNote, Rectangle target, Collection<KeyRect> noteListWhite, Collection<KeyRect> noteListBlack) {
        int whiteKeyCount = _keyboardOctave * 7 + 1;
        if (whiteKeyCount >= 75) { // 128 / 12 = 10.XXX, 128-120 = 8, 12 - 8 ... 7 - 3
            whiteKeyCount = 75;
        }
        double whiteKeyWidth =  target.width * 1.0 / (double)whiteKeyCount;
        
        for (int i = 0; i < _keyboardOctave; ++ i) {
            makeWhiteAndBlack1Oct(rootNote + i * 12, target.x +  (double)i * whiteKeyWidth * 7.0, (double)whiteKeyWidth * 7.0, target.height, noteListWhite, noteListBlack, whiteKeyWidth);
        }
        
        int whiteLastX = 0;
        for (KeyRect key : noteListWhite) {
            int whiteLastNext = key._rect.x + key._rect.width;
            if (whiteLastX < whiteLastNext) {
                whiteLastX = whiteLastNext;
            }
        }
        KeyRect key = new KeyRect(rootNote + 12 * _keyboardOctave);
        key._rect.setBounds(whiteLastX, target.y, (int)whiteKeyWidth, target.height);
        noteListWhite.add(key);
    }

    static Color pushedColor = MXUtil.mixedColor(MXUtil.mixedColor(Color.WHITE, Color.yellow, 80), Color.blue, 10);;
    static Color centerColor = MXUtil.mixedColor(MXUtil.mixedColor(Color.WHITE, Color.yellow, 20), Color.green, 20);
    static Color selectedColor = MXUtil.mixedColor(MXUtil.mixedColor(Color.WHITE, Color.yellow, 20), Color.red, 40);
    static Color sustainColor = new Color(0x87, 0xce, 0xeb);
    
    public int getAdjustedHeight(int width) {
        int heightAll = getHeight();
        int widthOne = width / _whiteKeysList.size();
        return widthOne * 5;
    }

    public synchronized void paintOnGraphics(Graphics g, Rectangle rect) {
        Rectangle whiteRect = rect;
        
        for (KeyRect key : _whiteKeysList) {
            Rectangle fill = key._rect;
            if (rect == null || fill.intersects(rect)){
                Color color;
                if (key._note == _mouseNoteOn) {
                    color = Color.PINK;
                }else if (isSelectedNote(key._note)) {
                    color = selectedColor;
                }else if (_sequenceNoteOn[key._note]) {
                    color = pushedColor;
                }else if (_sequenceSustain[key._note]) {
                    color = sustainColor;
                }else if (key._note == 60) {
                    color = centerColor;
                }else {
                    color = Color.WHITE;
                }
                g.setColor(color);
                g.fillRect(fill.x + 1, fill.y + 1, fill.width - 2, fill.height - 2);

                g.setColor(Color.BLACK);
                g.drawRect(fill.x, fill.y, fill.width, fill.height);
                if (whiteRect != null) {
                    whiteRect = whiteRect.union(fill);
                }
            }
        }
        
        rect =  whiteRect; 
        for (KeyRect key : _blackKeysList) {
            Rectangle fill = key._rect;
            if (rect == null || fill.intersects(rect)){
                Color color;
                if (key._note == _mouseNoteOn) {
                    color = Color.PINK;
                }else if (isSelectedNote(key._note)) {
                    color = selectedColor;
                }else if (_sequenceNoteOn[key._note]) {
                    color = pushedColor;
                }else if (_sequenceSustain[key._note]) {
                    color = sustainColor;
                }else {
                    color = Color.BLACK;
                }
                g.setColor(color);
                g.fillRoundRect(fill.x + 1, fill.y + 1, fill.width - 2, fill.height - 2, 10, 10);

                g.setColor(Color.GRAY);
                g.drawRoundRect(fill.x,  fill.y , fill.width, fill.height, 10, 10);
            }
        }
    }
    
    @Override
    public synchronized void paintComponent(Graphics g) {
        int widthAll = getWidth(), heightAll = getHeight();
        Rectangle bounds = g.getClipBounds();

        if (_bufferedImage != null) {
            if (widthAll != _bufferedImage.getWidth() || heightAll != _bufferedImage.getHeight()) {
                _bufferedImage = null;
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
        }else if (bounds != null) {
            rect = rect.union(bounds);
        }
        _bufferedRect = null;

        g.drawImage(_bufferedImage, 
                rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, 
                rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, this);
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
            }
        }
        if (_bufferedImage == null) {
            TreeSet<KeyRect> whiteKeys = new TreeSet<KeyRect>(noteComp);
            TreeSet<KeyRect> blackKeys = new TreeSet<KeyRect>(noteComp);
            makeWhiteAndBlack(_rootNote, new Rectangle(0, 0, widthAll, heightAll), whiteKeys, blackKeys);
            _whiteKeysList = new ArrayList(whiteKeys);
            _blackKeysList = new ArrayList(blackKeys);
            if (_bufferedImageGraphics != null) {
                _bufferedImageGraphics.dispose();
            }
            _bufferedImage = new BufferedImage(widthAll, heightAll, BufferedImage.TYPE_3BYTE_BGR);
            _bufferedImageGraphics = _bufferedImage.getGraphics();
            paintOnGraphics(_bufferedImageGraphics, null);
            _bufferedRect = new Rectangle(0, 0, widthAll, heightAll);
        }else {
            paintOnGraphics(_bufferedImageGraphics, rect);
            Rectangle a = _bufferedRect;
            if (rect == null) {
                rect = new Rectangle(widthAll, heightAll);
            }
            if (a == null) {
                a = rect;
            }else {
                a = a.union(rect);
            }
            _bufferedRect = a;
        }
    }

    public void noteOn(int note) {
        _sequenceNoteOn[note] = true;
        if (_sequenceSustainGlobal) {
            _sequenceSustain[note] = true;
        }
        orderRedrawNote(note);
    }
    
    public void noteOff(int note) {
        _sequenceNoteOn[note] = false;
        orderRedrawNote(note);
    }
    
    public void allNoteOff() {
        _sequenceSustainGlobal = false;
        for (int note = 0; note < _sequenceNoteOn.length; ++ note) {
            _sequenceNoteOn[note] = false;
        }
        orderRedrawNote(-1);
    }

    public void sustain(int pedal) {
        if (pedal >= 20) {
            _sequenceSustainGlobal = true;
            for (int i = 0; i < _sequenceSustain.length; ++ i) {
                _sequenceSustain[i] = _sequenceNoteOn[i];
            }
        }else {
            _sequenceSustainGlobal = false;
            int count= 0;
            for (int i = 0; i < _sequenceSustain.length; ++ i) {
                if (_sequenceSustain[i] || _sequenceNoteOn[i]) {
                    count ++;
                }
            }
            if (count == 1) {
                for (int i = 0; i < _sequenceSustain.length; ++ i) {
                    if (_sequenceSustain[i] || _sequenceNoteOn[i]) {
                        _sequenceSustain[i] = false;
                        orderRedrawNote(i);
                    }
                }
            }else if(count == 0) {
                return;
            }
            for (int i = 0; i < _sequenceSustain.length; ++ i) {
                if (_sequenceSustain[i]) {
                    _sequenceSustain[i] = false;
                }
            }
            orderRedrawNote(-1);
        }
    }

    public static class KeyRect {
        Rectangle _rect = new Rectangle();
        final int _note;
        
        public KeyRect(int note) {
            this._note = note;
        }
        
        public boolean isValid() {
            if (_note >= 0 && _note < 128) {
                return true;
            }
            return false;
        }
    }
    
    static Comparator<KeyRect> noteComp = new Comparator<KeyRect> () {
        @Override
        public int compare(KeyRect o1, KeyRect o2) {
            if (o1._note < o2._note) return -1;
            if (o1._note > o2._note) return +1;
            return 0;
        }
    };

    public void pushNoteByMouse(Point p) {
        for (KeyRect key : _blackKeysList) {
            if (key.isValid() == false) {
                continue;
            }
            if (key._rect.contains(p)) {
                int prevNote = _mouseNoteOn;
                if (prevNote != key._note) {                    
                    releaseNoteByMouse();
                    _mouseNoteOn = key._note;
                    toggleNoteSelected(key._note);
                    if (_handler != null) {
                        _handler.noteOn(key._note);
                    }
                    orderRedrawNote(prevNote);
                    orderRedrawNote(key._note);
                }
                return;
            }
        }
        for (KeyRect key : _whiteKeysList) {
            if (key.isValid() == false) {
                continue;
            }
            if (key._rect.contains(p)) {
                int prevNote = _mouseNoteOn;
                if (prevNote != key._note) {                    
                    releaseNoteByMouse();
                    _mouseNoteOn = key._note;
                    toggleNoteSelected(key._note);
                    if (_handler != null) {
                        _handler.noteOn(key._note);
                    }
                    orderRedrawNote(prevNote);
                    orderRedrawNote(key._note);
                }
                return;
            }
        }
    }

    public void releaseNoteByMouse() {
        if (_mouseNoteOn >= 0) {
            int note = _mouseNoteOn;
            _mouseNoteOn = -1;
            orderRedrawNote(note);

            if (_handler != null) {
                _handler.noteOff(note);
            }
        }
    }
    
    public KeyRect findRectByNote(ArrayList<KeyRect> list, int note) {
        int from = 0;
        int to  = list.size() - 1;
        while(from <= to) {
            int x = (to - from) / 2 + from;
            KeyRect o = list.get(x);
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

    public void orderRedrawNote(final int note) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    orderRedrawNote(note);
                }
            });
            return;
        }
        if (note >= 0) {
            MXPianoComponent.KeyRect key = findRectByNote(_whiteKeysList, note);
            if (key == null) {
                key = findRectByNote(_blackKeysList, note);
            }
            if (key != null && key.isValid()) {
                paintOnBuffer(key._rect);
                repaint(key._rect);
            }
        }else {
            paintOnBuffer(null);
            repaint();
        }
    }
    
    public static void main(String[] args) {
        MXDebugPrint.globalSwitchOn();
        JFrame win = new JFrame("Piano");
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Insets ins = win.getInsets();
        win.setSize(600 + ins.left + ins.right, 500 + ins.top + ins.bottom);
        win.setLayout(new GridLayout(7, 1));

        for (int i = 1 ; i <= 7; ++ i) {
            MXPianoComponent comp = new MXPianoComponent();
            if (i == 0) {
                comp.setAllowMultiSelect(true);
            }
            comp._keyboardOctave = i;
            win.add(comp);
        }
        win.setVisible(true);
    }
    
    private boolean _allowMultiSelect = false;
    boolean[] _selectedNote =  new boolean[256];

    public boolean isSelectedNote(int note) {
        if (_allowMultiSelect) {
            return _selectedNote[note];
        }
        return false;
    }
    
    public void selectNote(int note, boolean flag) {
        if (_allowMultiSelect) {
            _selectedNote[note] = flag;
            _handler.selectionChanged();
        }
    }
    
    public void toggleNoteSelected(int note) {
        if (_allowMultiSelect) {
            _selectedNote[note] = !_selectedNote[note];
            if (_handler != null) {                
                _handler.selectionChanged();
            }
        }
    }
    
    public int[] listMultiSelected() {
        ArrayList<Integer> list = new ArrayList();
        for (int i = 0; i < _selectedNote.length; ++ i) {
            if (_selectedNote[i]) {
                list.add(i);
            }
        }
        int[] ret = new int[list.size()];
        int x = 0;
        for (int n : list) {
            ret[x++] = n;
        }
        return ret;
    }
}
