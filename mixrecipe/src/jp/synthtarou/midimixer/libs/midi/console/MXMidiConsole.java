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
package jp.synthtarou.midimixer.libs.midi.console;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import jp.synthtarou.midimixer.libs.common.MXGlobalTimer;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMidiConsole implements ListModel<String> {

    ArrayList<MXMidiConsoleElement> _list = new ArrayList();
    ArrayList<ListDataListener> _listener = new ArrayList();
    LinkedList<MXMidiConsoleElement> _queue = new LinkedList();

    static final int _capacity = 5000;
    static final int _timer = 500;
    int _startPos = 0;
    int _writePos = 0;
    JList _refList;
    MXTiming _selectedTiming = null;
    public boolean _showAllLine = false;
    public boolean _globalSelection = true;

    ListCellRenderer<Object> _renderer = new ListCellRenderer<Object>() {
        DefaultListCellRenderer _def = new DefaultListCellRenderer();

        public Component getListCellRendererComponent(JList list, Object var, int index, boolean isSelected, boolean cellHasFocus) {

            MXMidiConsoleElement value = getConsoleElement(index);

            if (_globalSelection) {
                boolean prevSele = isSelected;
                cellHasFocus = false;
                isSelected = false;
                Color back = null;
                boolean gray = false;

                if (value == null) {
                    if (_globalSelection) {
                        if (_selectedTiming == null) {
                            isSelected = prevSele;
                            back = null;
                            gray = true;
                        }
                    }
                    var = "-";
                } else {
                    if (_selectedTiming == value.getTiming()) {
                        back = Color.red;
                        isSelected = true;
                        if (_selectedTiming != null) {
                            back = Color.cyan;
                        } else {
                            back = Color.gray;
                        }
                        if (_refList.hasFocus()) {
                            cellHasFocus = true;
                        }
                    }
                }
                Component c = null;
                c = _def.getListCellRendererComponent(list, var, index, isSelected, cellHasFocus);
                if (gray) {
                    back = c.getBackground();
                    if (back != null) {
                        back = MXUtil.mixedColor(back, Color.white, 20);
                    }
                }
                if (back != null) {
                    c.setBackground(back);
                }
                return c;
            } else {
                Component c = null;
                c = _def.getListCellRendererComponent(list, var, index, isSelected, cellHasFocus);
                return c;
            }
        }
    };

    public void bind(JList list) {
        list.setModel(this);
        list.setCellRenderer(_renderer);
        _refList = list;
    }

    public void unbind(JList list) {
        //nothing
        _refList = null;
    }

    public static int getGlobalCapacity() {
        return _capacity;
    }

    @Override
    public int getSize() {
        return _capacity;
        //return _list.size();
    }

    @Override
    public String getElementAt(int index) {
        MXMidiConsoleElement e = getConsoleElement(index);
        if (e == null) {
            return "";
        }
        return e.formatMessageLong();
    }

    public int viewIndex(int viewpos) {
        int index = viewpos + _list.size() - _capacity + _startPos;
        if (index < 0) {
            return -1;
        }
        while (index >= _capacity) {
            index -= _capacity;
        }
        if (index >= _list.size()) {
            return -1;
        }
        return index;
    }

    public int indexView(int index) {
        int viewpos = index - _list.size() + _capacity - _startPos;
        while (viewpos < 0) {
            viewpos += _capacity;
        }
        while (viewpos >= _capacity) {
            viewpos -= _capacity;
        }
        return viewpos;
    }

    public MXMidiConsoleElement getConsoleElement(int viewpos) {
        int index = viewIndex(viewpos);
        if (index >= 0) {
            return _list.get(index);
        }
        return null;
    }

    public void add(MXMidiConsoleElement e) {
        if (false) {
            addImpl(e);
        } else if (true) {
            if (e.getTiming() == null) {
                new Exception("null timing" + e.formatMessageLong()).printStackTrace();
            }
            synchronized (_queue) {
                _queue.add(e);
            }
            countDown();
        }
    }

    boolean _switchPause = false;

    public void switchPause(boolean pause) {
        if (pause) {
            fireImpl();
            _switchPause = pause;
        } else {
            _switchPause = pause;
            fireImpl();
        }
    }

    boolean _recordClock = false;

    public void setRecordClock(boolean record) {
        _recordClock = record;
    }

    private synchronized void addImpl(MXMidiConsoleElement e) {
        /* Ignore */
        if (_recordClock == false) {
            switch (e.getType()) {
                case MXMidiConsoleElement.CONSOLE_DWORD: {
                    int dword = e.getDword();
                    int status = (dword >> 16) & 0xff;
                    int data1 = (dword >> 8) & 0xff;
                    int data2 = (dword) & 0xff;
                    if (status == MXMidi.COMMAND_ACTIVESENSING
                            || status == MXMidi.COMMAND_MIDICLOCK
                            || status == MXMidi.COMMAND_MIDITIMECODE) {
                        return;
                    }
                    break;
                }
                case MXMidiConsoleElement.CONSOLE_DATA: {
                    byte[] data = e.getData();
                    if (data.length > 0) {
                        int status = data[0] & 0xff;
                        if (status == MXMidi.COMMAND_ACTIVESENSING
                                || status == MXMidi.COMMAND_MIDICLOCK
                                || status == MXMidi.COMMAND_MIDITIMECODE) {
                            return;
                        }
                    }
                    break;
                }
                case MXMidiConsoleElement.CONSOLE_MESSAGE: {
                    byte[] data = e.getMessage().createBytes();
                    if (data == null) {
                        break;
                    }else if (data.length > 0) {
                        int status2 = data[0] & 0xff;
                        if (status2 == MXMidi.COMMAND_ACTIVESENSING
                                || status2 == MXMidi.COMMAND_MIDICLOCK
                                || status2 == MXMidi.COMMAND_MIDITIMECODE) {
                            return;
                        }
                    }
                    break;
                }
            }
        }
        //e.getTiming()._arrivePath = new Throwable();
        if (_writePos >= _capacity) {
            _writePos = 0;
        }
        if (_list.size() >= _capacity) {
            int prev = _writePos - 1;
            if (prev < 0) {
                prev = _capacity - 1;
            }
            MXMidiConsoleElement prevE = _list.get(prev);
            if (prevE != null) {
                MXTiming prevNumber = prevE.getTiming();
                int comp = prevNumber.compareTo(e.getTiming());
                if (comp > 0) {
                    System.err.println("***********************************************************");
                    System.err.println("This " + e.formatMessageLong());
                    //e.getTiming()._arrivePath.printStackTrace();
                    System.err.println("Before" + prevE.formatMessageLong());
                    //prevE.getTiming()._arrivePath.printStackTrace();
                }
            }

            _list.set(_writePos, e);
            _writePos++;
            _startPos = _writePos;
        } else {
            _list.add(e);
            _writePos++;
        }
    }

    long _repaintLastTick = 0;
    boolean _repainReserved = false;

    private void countDown() {
        long tickNow = System.currentTimeMillis();
        if (_switchPause) {
            return;
        }
        if (_repainReserved) {
            return;
        }
        if (tickNow - _repaintLastTick >= _timer) {
            fireImpl();
        } else {
            _repainReserved = true;
            MXGlobalTimer.letsCountdown(_timer - (tickNow - _repaintLastTick), new Runnable() {
                @Override
                public void run() {
                    fireImpl();
                }
            });
        }
    }

    private void fireImpl() {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fireImpl();
                }
            });
            return;
        }
        if (_switchPause) {
            return;
        }
        _repainReserved = false;
        _repaintLastTick = System.currentTimeMillis();
        LinkedList<MXMidiConsoleElement> pop;
        synchronized (_queue) {
            pop = _queue;
            _queue = new LinkedList<>();
        }
        synchronized (this) {
            for (MXMidiConsoleElement e : pop) {
                addImpl(e);
            }
            final ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, _list.size());
            try {
                for (ListDataListener listener : _listener) {
                    listener.contentsChanged(e);
                }
                _refList.ensureIndexIsVisible(_capacity - 1);
                _refList.repaint();
            } catch (Throwable ex) {

            }
        }
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        _listener.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        _listener.remove(l);
    }

    boolean reserved2 = false;
    long lastTick2 = 0;

    public void setSelectedTiming(MXTiming selection) {
        _selectedTiming = selection;

        long tickNow = System.currentTimeMillis();
        if (tickNow - lastTick2 >= 100) {
            reserved2 = true;
            fireSelectByTiming(selection);
        } else {
            if (reserved2) {
                return;
            }
            reserved2 = true;
            MXGlobalTimer.letsCountdown(100 - (tickNow - lastTick2), new Runnable() {
                @Override
                public void run() {
                    fireSelectByTiming(selection);
                }
            });
        }
    }

    public void fireSelectByTiming(MXTiming selection) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fireImpl();
                }
            });
            return;
        }
        if (selection == null) {
            _refList.repaint();
            return;
        }
        if (reserved2) {
            lastTick2 = System.currentTimeMillis();
            reserved2 = false;
            synchronized (this) {
                int low = 0;
                int high = getSize();
                while (low < high) {
                    int middle = (high + low) / 2;
                    MXMidiConsoleElement elem = getConsoleElement(middle);
                    if (elem == null) {
                        low = middle + 1;
                        continue;
                    }
                    MXTiming middlesNumber = elem.getTiming();
                    if (middlesNumber == null) {
                        low = middle + 1;
                        continue;
                    }

                    int comp = middlesNumber.compareTo(selection);
                    if (comp == 0) {
                        low = high = middle;
                        break;
                    }
                    if (comp < 0) {
                        low = middle + 1;
                    } else {
                        high = middle - 1;
                    }
                }
                if (low != high) {
                    //該当しなかった
                    _refList.ensureIndexIsVisible(low);
                    _refList.repaint();
                    return;
                }
                while (low >= 0) {
                    MXMidiConsoleElement e1 = getConsoleElement(low);
                    MXMidiConsoleElement e2 = getConsoleElement(low - 1);
                    if (e1 == null || e2 == null) {
                        break;
                    }
                    MXTiming t1 = e1.getTiming();
                    MXTiming t2 = e2.getTiming();
                    if (t1 == null || t2 == null) {
                        break;
                    }
                    if (t1.compareTo(t2) == 0) {
                        low--;
                        continue;
                    } else {
                        break;
                    }
                }
                while (high + 1 < getSize()) {
                    MXMidiConsoleElement e1 = getConsoleElement(high);
                    MXMidiConsoleElement e2 = getConsoleElement(high + 1);
                    if (e1 == null || e2 == null) {
                        break;
                    }
                    MXTiming t1 = e1.getTiming();
                    MXTiming t2 = e2.getTiming();
                    if (t1 == null || t2 == null) {
                        break;
                    }
                    if (t1.compareTo(t2) == 0) {
                        high++;
                        continue;
                    } else {
                        break;
                    }
                }
                _refList.ensureIndexIsVisible(low);
                _refList.repaint();
            };
        }
    }

    public void clear() {
        _list = new ArrayList();
        _queue = new LinkedList();
        _startPos = 0;
        _writePos = 0;
        final ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, _list.size());
        for (ListDataListener listener : _listener) {
            listener.contentsChanged(e);
        }
    }


    /*
                String text ="-";
                if (value != null) {
                    byte[] data = value.getData();
                    System.out.println("data = " + data.length + MXUtil.dumpHex(data));
                    text = MXUtil.dumpHex(data);
                }
                
                JViewport port = (JViewport)_refList.getParent();
                int maxWidth = ((int)port.getWidth() - 10);
                
                if (maxWidth >= 10) {                
                    StringBuffer all = new StringBuffer();
                    int x = 0;
                    all.append("<html>");

                    System.out.println("maxWidth " + maxWidth);
                    boolean first = true;
                    FontMetrics font = list.getGraphics().getFontMetrics(new JLabel().getFont());

                    while (x < text.length()) {
                        String parts1 = text.substring(x, x + 1);
                        int lastSpaceX = -1;
                        boolean isOver = false;
                        int charWidth = 0;

                        for (int x1 = x; x1 < text.length(); ++ x1) {
                            charWidth += font.charWidth(text.charAt(x1));
                            if (charWidth >= maxWidth) {
                                isOver = true;
                                break;
                            }else if (text.charAt(x1) == ' ') {
                                lastSpaceX = x1;
                            }
                        }
                        if (isOver && lastSpaceX > 0) {
                            String parts3 = text.substring(x, lastSpaceX);
                            first = false;
                            if (!first) {
                                all.append("<br>");
                            }
                            all.append(parts3);
                            x = lastSpaceX + 1;
                        }else {
                            String parts3 = text.substring(x);
                            first = false;
                            if (!first) {
                                all.append("<br>");
                            }
                            all.append(parts3);
                            x = lastSpaceX + 1;
                            break;
                        }
                    }

                    all.append("</html>");
                    System.out.println(all);
                    JLabel l = new JLabel(all.toString());
                    c = l;
                    l.setBackground(back);
                }else {
                    c = new JLabel(text);
                }
     */
}
