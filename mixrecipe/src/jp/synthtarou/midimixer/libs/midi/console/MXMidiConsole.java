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
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMidiConsole extends DefaultListModel<MXMidiConsoleElement> {

    boolean done = false;

    public MXMidiConsole() {
        MXSafeThread t = new MXSafeThread("MidiConsole", new Runnable() {
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
                } catch (Throwable e) {
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

                var = message.toStringMessageInfo(2);
                if (message.isBinaryMessage()) {
                    var += "\"" + MXUtil.dumpHex(message.getBinary()) + "\"";
                }
                String gateValue = message.toStringGateValue();
                if (gateValue.isEmpty() == false) {
                    if (message.isCommand(MXMidi.COMMAND_CH_NOTEOFF)) {
                        int data2 = message.parseTemplate(2);
                        if (data2 != 0) {
                            var += "("+ gateValue + ")";
                        }
                    }else {
                        var += "("+ gateValue + ")";
                    }
                }
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
            } catch (Throwable e) {
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
            boolean selLast = false;
            if (_refList.getSelectedIndex() == size() - 1) {
                selLast = true;
            }
            LinkedList<MXMidiConsoleElement> list;
            synchronized (MXMidiConsole.this) {
                list = _queue;
                _queue = new LinkedList<>();
            }
            _refList.setValueIsAdjusting(true);
            addAll(list);
            int newTotal = size();
            if (newTotal >= 500) {
                int minus = newTotal - 500;
                if (minus >= 0) {
                    removeRange(0, minus);
                }
            }
            if (list.isEmpty() == false) {
                _refList.ensureIndexIsVisible(size()-1);
            }
            _refList.setValueIsAdjusting(false);
            _refList.repaint();
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
    
    MXMessage _lastSel = null;

    public void setHIghlightOwner(MXMessage selection) {
        MXMessage owner = MXMessage.getRealOwner(selection);
        if (_selectedTiming == owner) {
            //for removeRange feedback
            return;
        }
        _selectedTiming = owner;
        if (owner == null) {
            _refList.repaint();
        }else {
            for (int i = 0; i < getSize(); ++i) {
                MXMidiConsoleElement e = elementAt(i);
                if (e == null) {
                    continue;
                }
                MXMessage seek = e.getMessage();
                seek = MXMessage.getRealOwner(seek);
                if (seek == owner) {
                    _refList.ensureIndexIsVisible(i);
                }
            }
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
