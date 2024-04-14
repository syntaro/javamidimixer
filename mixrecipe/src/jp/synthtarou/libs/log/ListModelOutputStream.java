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
package jp.synthtarou.libs.log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import jp.synthtarou.libs.MXCountdownTimer;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ListModelOutputStream extends OutputStream {

    static class MyModel extends DefaultListModel<String> {

        JList _list;

        MyModel(JList list) {
            _list = list;
        }
    }

    byte[] _data = new byte[1024];
    int pos = 0;
    ArrayList<MyModel> _lines = new ArrayList();
    LinkedList<String> _lagPool = new LinkedList<>();
    long _lastAdded = 0;
    JList _installed;
    boolean _pause = false;

    public void setPause(boolean pause) {
        _pause = pause;
    }

    public synchronized void attachListForLogging(JList install) {
        _installed = install;
        MyModel model = new MyModel(install);
        _installed.setModel(model);
        _lines.add(model);
    }

    public synchronized void detachList(JList instlal) {
        for (int i = _lines.size() - 1; i >= 0; i--) {
            MyModel m = _lines.get(i);
            if (m._list == instlal) {
                _lines.remove(i);
            }
        }
    }

    public synchronized void checkLag() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                checkLag();
            });
            return;
        }
        if (_lagPool.isEmpty() || _pause) {
            return;
        }
        long spent = System.currentTimeMillis() - _lastAdded;
        if (spent >= 500) {
            if (_installed != null) {
                _installed.setValueIsAdjusting(true);
            }
            synchronized (ListModelOutputStream.this) {
                while (_lagPool.isEmpty() == false) {
                    String str = _lagPool.removeFirst();
                    for (int i = _lines.size() - 1; i >= 0; i--) {
                        MyModel m = _lines.get(i);
                        m.addElement(str);
                        while (m.size() >= 500) {
                            m.removeElementAt(0);
                        }
                    }
                }
                _lagPool.clear();
            }
            if (_installed != null) {
                _installed.setValueIsAdjusting(false);
            }
            for (int i = _lines.size() - 1; i >= 0; i--) {
                synchronized (ListModelOutputStream.this) {
                    MyModel m = _lines.get(i);
                    m._list.ensureIndexIsVisible(m.getSize() - 1);
                }
            }
        } else {
            MXCountdownTimer.letsCountdown(500 - spent, this::checkLag);
        }
    }

    public synchronized void clearLogLine() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                clearLogLine();
            });
            return;
        }
        for (int i = _lines.size() - 1; i >= 0; i--) {
            MyModel m = _lines.get(i);
            m.removeAllElements();
        }
    }

    public synchronized void addLine(String text) {
        _lagPool.add(text);
        checkLag();
    }

    protected synchronized void commitBuffer() {
        String text = new String(_data, 0, pos, System.out.charset());
        pos = 0;
        addLine(text);
    }

    public void ensureCapacity(int newSize) {
        if (newSize < _data.length) {
            return;
        }

        byte[] newData = new byte[newSize];
        for (int i = 0; i < _data.length; ++i) {
            newData[i] = _data[i];
        }
        _data = newData;
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\r') {
            return;
        }
        if (pos >= _data.length) {
            ensureCapacity(_data.length + 1024);
        }
        if (b == '\n') {
            commitBuffer();
        } else {
            _data[pos++] = (byte) b;
        }
    }
}
