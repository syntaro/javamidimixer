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
package jp.synthtarou.midimixer.mx30surface.capture;

import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JPanel;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CapturePool {
    static class Entry {
        public Entry(MXMessage message) {
            _time = System.currentTimeMillis();
            _message = message;
        }

        long _time;
        MXMessage _message;
    }

    long longestMilliSeconds = 60 * 1000;
    LinkedList<Entry> _pool = new LinkedList<>();
    
    public CapturePool() {
        
    }
    
    public synchronized  void record(MXMessage message) {
        long timelimit = System.currentTimeMillis() - longestMilliSeconds;

        Entry e = new Entry(message);
        _pool.addLast(e);
        
        while (_pool.isEmpty() == false) {
            e = _pool.peekFirst();
            if (e._time < timelimit) {
                _pool.removeFirst();
                continue;
            }
            else {
                break;
            }
        }
    }

    public synchronized ArrayList<MXMessage> getLastTime(long milliSeconds) {
        long timelimit = System.currentTimeMillis() - milliSeconds;
        ArrayList<MXMessage> result = new ArrayList<>();
        for (Entry e : _pool) {
            if (e._time < timelimit) {
                continue;
            }
            result.add(e._message);
        }
        return result;
    }
    
    public synchronized void clear() {
        _pool.clear();
    }
    
    public void startCapture() {
        MXMain.getMain().setCapture(new MXReceiver() {
            @Override
            public String getReceiverName() {
                return "Capture";
            }

            @Override
            public JPanel getReceiverView() {
                return null;
            }

            @Override
            public void processMXMessage(MXMessage message) {
                record(message);
            }
        });
    }
    
    public void stopCapture() {
        MXMain.getMain().setCapture(null);
    }
    
}
