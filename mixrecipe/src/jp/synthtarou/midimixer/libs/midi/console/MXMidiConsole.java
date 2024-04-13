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
import java.util.logging.Level;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import jp.synthtarou.libs.MXCountdownTimer;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.libs.MainThreadTask;
import static jp.synthtarou.libs.MainThreadTask.NOTHING;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMidiConsole implements ListModel<String> {

    static final int _capacity = 500;
    static final int _timer = 500;

    public final ShiftableList<MXMidiConsoleElement> _list = new ShiftableList(_capacity);
    ArrayList<ListDataListener> _listener = new ArrayList();
    LinkedList<MXMidiConsoleElement> _queue = new LinkedList();

    JList _refList;
    long _selectedTiming = 0;
    public boolean _showAllLine = false;
    public boolean _globalSelection = true;

    ListCellRenderer<Object> _renderer = new ListCellRenderer<Object>() {
        DefaultListCellRenderer _def = new DefaultListCellRenderer();

        public Component getListCellRendererComponent(JList list, Object var, int index, boolean isSelected, boolean cellHasFocus) {

            MXMidiConsoleElement value = _list.get(index);

            if (_globalSelection) {
                boolean prevSele = isSelected;
                cellHasFocus = false;
                isSelected = false;
                Color back = Color.white;
                boolean gray = false;

                if (value == null) {
                    if (_globalSelection) {
                        if (_selectedTiming == 0) {
                            isSelected = prevSele;
                            //back = null;
                            gray = true;
                        }
                    }
                    var = "-";
                } else {
                    /*
                    if (_selectedTiming == value.getMessage()._timing) {
                        //back = Color.red;
                        isSelected = true;
                        if (_selectedTiming != 0) {
                            //back = Color.cyan;
                        } else {
                            //back = Color.gray;
                        }
                        if (_refList.hasFocus()) {
                            cellHasFocus = true;
                        }
                    }*/
                }
                Component c = null;
                c = _def.getListCellRendererComponent(list, var, index, isSelected, cellHasFocus);
                /*
                if (gray) {
                    back = c.getBackground();
                    if (back != null) {
                        back = MXUtil.mixtureColor(back, 20, Color.white, 80);
                    }
                }
                */
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
        return _list.size();
    }

    public MXMidiConsoleElement getConsoleElement(int index) {
        return _list.get(index);
    }

    @Override
    public String getElementAt(int index) {
        MXMidiConsoleElement e = _list.get(index);
        if (e == null) {
            return "";
        }
        return e.formatMessageLong();
    }

    public synchronized void add(MXMidiConsoleElement e) {
        if (false) {
            addImpl(e);
        } else if (true) {
            synchronized (_queue) {
                _queue.add(e);
            }
            countDown();
        }
    }

    boolean _switchPause = false;

    public void switchPause(boolean pause) {
        if (pause) {
            invokeFire();
            _switchPause = pause;
        } else {
            _switchPause = pause;
            invokeFire();
        }
    }

    boolean _recordClock = false;

    public void setRecordClock(boolean record) {
        _recordClock = record;
    }

    private synchronized void addImpl(MXMidiConsoleElement e) {
        /* Ignore */
        if (_recordClock == false) {
            int status = e.getMessage().getStatus();
            if (status == MXMidi.COMMAND_ACTIVESENSING
                    || status == MXMidi.COMMAND_MIDICLOCK
                    || status == MXMidi.COMMAND_MIDITIMECODE) {
                return;
            }
        }
        _list.add(e);
        /*
        if (_list.size() >= 2) {
            MXMidiConsoleElement prevE = _list.get(_list.size() - 1);
            if (prevE != null) {
                MXTiming prevNumber = prevE.getTiming();
                int comp = prevNumber.compareTo(e.getTiming());
                if (comp > 0) {
                    MXFileLogger.getLogger(MXMessage.class).warning("This " + e.formatMessageLong() + "\n" + "Before " + prevE.formatMessageLong());
                }
            }
        }*/
        
        countDown();
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
            invokeFire();
        } else {
            _repainReserved = true;
            MXCountdownTimer.letsCountdown(_timer - (tickNow - _repaintLastTick), this::invokeFire);
        }
    }

    private void invokeFire() {
        new MainThreadTask() {
            @Override
            public Object runTask() {
                implFire();
                return NOTHING;
            }
        };
    }

    private void implFire() {
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
            for (ListDataListener listener : _listener) {
                try {
                    listener.contentsChanged(e);
                } catch (RuntimeException ex) {
                    MXFileLogger.getLogger(MXMidiConsole.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
            if (_refList != null) {
                _refList.ensureIndexIsVisible(_list.size() - 1);
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
/*
    public void setSelectedTiming(MXTiming selection) {
        new MainThreadTask() {
            @Override
            public Object runTask() {
                synchronized (_list) {
                    _selectedTiming = selection;
                    for (int i = 0; i < getSize(); ++i) {
                        MXMidiConsoleElement elem = _list.get(i);
                        if (elem == null) {
                            continue;
                        }
                        if (elem.getMessage()._timing == selection) {
                            int start = _refList.getFirstVisibleIndex();
                            int fin = start + _refList.getVisibleRowCount() - 1;
                            if (start <= i && i <= fin) {
                                return NOTHING;
                            } else {
                                _refList.ensureIndexIsVisible(i);
                            }
                            return NOTHING;
                        }
                    }
                    _refList.invalidate();
                }
                return NOTHING;
            }
        };
    }
*/
    public synchronized void clear() {
        _list.clear();
        _queue = new LinkedList();
        final ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, _list.size());
        for (ListDataListener listener : _listener) {
            listener.contentsChanged(e);
        }
    }
}
