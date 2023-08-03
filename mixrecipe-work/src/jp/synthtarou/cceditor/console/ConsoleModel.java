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
package jp.synthtarou.cceditor.console;

import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import jp.synthtarou.midimixer.libs.MXGlobalTimer;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ConsoleModel implements ListModel<String>{
    ArrayList<ConsoleElement> _list = new ArrayList();
    ArrayList<ListDataListener> _listener = new ArrayList();
    LinkedList<ConsoleElement> _queue = new LinkedList();
    
    static final int _capacity = 5000;
    static final int _timer = 500;
    int _startPos = 0;
    int _writePos = 0;
    JList _refList;
    
    public void bind(JList list) {
        list.setModel(this);
        _refList = list;
    }
    
    public  void unbind(JList list) {
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
        ConsoleElement e = getConsoleElement(index);
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
            index  -= _capacity;
        }
        if (index  >= _list.size()) {
            return -1;
        }
        return index;
    }

    public int indexView(int index) {
        int  viewpos = index - _list.size() + _capacity - _startPos;
        while (viewpos < 0) {
            viewpos += _capacity;
        }
        while (viewpos >= _capacity) {
            viewpos  -= _capacity;
        }
        return viewpos;
    }
    
    public ConsoleElement getConsoleElement(int viewpos) {
        int index = viewIndex(viewpos);
        if (index >= 0) {
            return _list.get(index);
        }
        return null;
    }

    public void add(ConsoleElement e) {
        if (false) {
            addImpl(e);
        }else if (true) {
            synchronized(_queue) {
                _queue.add(e);
            }
            countDown();
        }
    }
    
    boolean _switchPause = false;
    
    public void switchPause(boolean pause) {
        if (_switchPause) {
            _switchPause = false;
            fireImpl();
        }else {
            fireImpl();
            _switchPause = true;
        }
    }

    private synchronized void addImpl(ConsoleElement e) {
        if (_writePos >= _capacity) {
            _writePos = 0;
        }
        if (_list.size() >= _capacity) {
            _list.set(_writePos, e);
            _writePos ++;
            _startPos = _writePos;
        }else {
            _list.add(e);
            _writePos ++;
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
        }else {
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
        _repainReserved = false;
        _repaintLastTick = System.currentTimeMillis();
        LinkedList<ConsoleElement> pop;
        synchronized (_queue) {
            pop = _queue;
            _queue = new LinkedList<>();
        }
        synchronized(this) {
            for (ConsoleElement e : pop) {
                addImpl(e);
            }
            final ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, _list.size());
            try {
                for (ListDataListener listener : _listener) {
                    listener.contentsChanged(e);
                }
                _refList.ensureIndexIsVisible(_capacity -1);
                _refList.repaint();
            }catch(Throwable ex) {

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
}
