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
import java.util.LinkedList;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import jp.synthtarou.libs.MXCountdownTimer;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ListModelOutputStream extends OutputStream {

    public static interface ByteLinesListener {

        public void listAdded(ListModelOutputStream byteLines);
    }

    byte[] _data = new byte[1024];
    int pos = 0;
    DefaultListModel<String> _lines = new DefaultListModel<>();
    LinkedList<String> _lagPool = new LinkedList<>();
    long _lastAdded = 0;
    JList _installed;
    boolean _pause = false;
    ListDataListener _installedListener = new MyDataListener();

    class MyDataListener implements ListDataListener {

        public MyDataListener() {

        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            scrollToLast();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            scrollToLast();
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            scrollToLast();
        }

        public void scrollToLast() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    synchronized (ListModelOutputStream.this) {
                        if (_installed != null) {
                            int size = _installed.getModel().getSize();
                            if (size >= 1) {
                                _installed.ensureIndexIsVisible(size - 1);
                            }
                        }
                    }
                }
            }
            );
        }
    }

    public void attach(JList install) {
        if (_installed != null) {
            if (_installed == install) {
                return;
            }

            _installed.getModel().removeListDataListener(_installedListener);
        }
        _installed = install;
        _installed.setModel(_lines);
        _installed.getModel().addListDataListener(_installedListener);
    }


    public void checkLag() {
        synchronized (this) {
            if (_lagPool.isEmpty() || _pause) {
                return;
            }
            long spent = System.currentTimeMillis() - _lastAdded;
            if (spent >= 500) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (_installed != null) {
                            _installed.setValueIsAdjusting(true);
                        }
                        synchronized(ListModelOutputStream.this) {
                            while(_lagPool.isEmpty() == false) {
                                String str = _lagPool.removeFirst();
                                _lines.addElement(str);
                                while (_lines.size() >= 500) {
                                    _lines.removeElementAt(0);
                                }
                            }
                        }
                        _lagPool.clear();
                        if (_installed != null) {
                            _installed.setValueIsAdjusting(false);
                        }
                    }
                });
            } else {
                MXCountdownTimer.letsCountdown(500 - spent, new Runnable() {
                    @Override
                    public void run() {
                        checkLag();
                    }
                });
            }
        }
    }

    public void clearLogLine() {
        synchronized (this) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    _lines.removeAllElements();
                }
            });
        }
    }
    
    public void addLine(String text) {
        synchronized(this) {            
            _lagPool.add(text);
            checkLag();
        }
    }
    
    protected void commitBuffer() {
        synchronized (this) {
            String text = new String(_data, 0, pos, System.out.charset());
            pos = 0;
            addLine(text);
        }
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
