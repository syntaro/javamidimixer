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
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;
import jp.synthtarou.libs.MXSafeThread;
import jp.synthtarou.midimixer.libs.midi.MXMessage;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMidiConsole extends DefaultListModel<MXMidiConsoleElement> {

    boolean done = false;

    public MXMidiConsole() {
        MXSafeThread t = new MXSafeThread("ListUpdate", new Runnable() {
            @Override
            public void run() {
                try {
                    while (!done) {
                        Thread.sleep(1000);
                        commitQueue();
                    }
                } catch (InterruptedException e) {
                    done = true;
                    return;
                } catch(Throwable e) {
                    e.printStackTrace();;
                    done = true;
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }

    static final int _timer = 1000;

    ArrayList<ListDataListener> _listener = new ArrayList();
    LinkedList<MXMidiConsoleElement> _queue = new LinkedList();

    JList _refList;
    MXMessage _selectedTiming = null;

    ListCellRenderer<Object> _renderer = new ListCellRenderer<Object>() {
        DefaultListCellRenderer _def = new DefaultListCellRenderer();

        public Component getListCellRendererComponent(JList list, Object var, int index, boolean isSelected, boolean cellHasFocus) {
            if (var == null) {
                var = "-";
                return _def.getListCellRendererComponent(list, var, index, isSelected, cellHasFocus);
            }
            try {
                MXMidiConsoleElement value = (MXMidiConsoleElement) var;
                MXMessage message = value.getMessage();

                var = message.toStringMessageInfo(2) + message.toStringGateValue();
                boolean prevSele = isSelected;
                cellHasFocus = false;
                isSelected = false;
                Color back = Color.white;
                boolean gray = false;
                
                MXMessage owner = MXMessage.getRealOwner(message);

                if (owner == _selectedTiming) {
                    isSelected = true;
                    if (_refList.hasFocus()) {
                        cellHasFocus = true;
                    }
                    back = Color.orange;
                }
                Component c = null;
                c = _def.getListCellRendererComponent(list, var, index, isSelected, cellHasFocus);
                if (back != null) {
                    c.setBackground(back);
                }
                return c;
            }
            catch (Throwable e) {
                return _def.getListCellRendererComponent(list, var, index, isSelected, cellHasFocus);
            }
        }
    };

    public void bind(JList list) {
       list.setModel(this);
        list.setCellRenderer(_renderer);
        _refList = list;
    }

    public void addElement2(MXMidiConsoleElement e) {
        if (true) {
            synchronized (this) {
                _queue.add(e);
            }
        } else {
            super.addElement(e);
        }
    }

    public void commitQueue() {
        SwingUtilities.invokeLater(() -> {
            if (_refList == null) {
                return;
            }
            if (_switchPause) {
                return;
            }
            LinkedList<MXMidiConsoleElement> list;
            synchronized (MXMidiConsole.this) {
                list = _queue;
                _queue = new LinkedList<>();
            }
            _refList.setValueIsAdjusting(true);
            for (MXMidiConsoleElement seek : list) {
                addElement(seek);
                if (size() >= 500) {
                    removeElementAt(0);
                }
            }
            _refList.setValueIsAdjusting(false);
            _refList.repaint();;
        });
    }

    boolean _switchPause = false;

    public void switchPause(boolean pause) {
        if (pause) {
            _switchPause = pause;
        } else {
            _switchPause = pause;
        }
    }

    boolean _recordClock = false;

    public void setRecordClock(boolean record) {
        _recordClock = record;
    }

    public void setMarked(MXMessage selection) {
        if (selection != null) {
            _selectedTiming = MXMessage.getRealOwner(selection);
            _refList.repaint();
        }
    }

    public void clear() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                clear();
            });
            return;
        }

        clear();;
    }
}
