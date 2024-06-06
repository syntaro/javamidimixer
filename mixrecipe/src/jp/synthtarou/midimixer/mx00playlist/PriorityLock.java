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

import java.util.logging.Level;
import jp.synthtarou.libs.log.MXFileLogger;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class PriorityLock {
    static String TAG = "PriorityLock";

    public PriorityLock() {

    }

    int _invalidateCount = 0;
    boolean _strongQueue = false;
    boolean _taken = false;

    long _takenTime = 0;
    public synchronized boolean getStrongForReader(long limit) {
        _strongQueue = true;
        if (_taken) {
            try {
                wait(limit);
            } catch (InterruptedException ex) {
                debugGraphics("Interrupted");
                _strongQueue = false;
                return false;
            }
        }
        if (_taken) {
            //_strongQueue = false; 予約する
            debugGraphics("NOT MY TURN");
            return false;
        }
        _taken = true;
        _takenTime = System.currentTimeMillis();
        _invalidateCount = 0;
        debugGraphics("STRONG TURN");
        return true;
    }

    public synchronized boolean getWeakForWriter(int timeout) {
        if (_taken || _strongQueue) {
            try {
                wait(timeout);
            } catch (InterruptedException ex) {
                debugGraphics("Interrupted");
                return false;
            }
        }
        if (_taken) {
            debugGraphics("ERR TAKEN STOLEN STILL");
            return false;
        }
        if (_strongQueue) {
            debugGraphics("ERR TAKEN STOLEN BY STRONGER");
            return false;
        }
        debugGraphics("GET WEAK");
        _taken = true;
        _takenTime = System.currentTimeMillis();
        _invalidateCount ++;
        return true;
    }

    public void release(boolean wasStrong) {
        _taken = false;
        if (wasStrong) {
            _strongQueue = false;
            debugGraphics("RELEASE STRONG");
        }
        else {
            debugGraphics("RELEASE WEAK");
        }
        synchronized (this) {
            notifyAll();
        }
        long span = System.currentTimeMillis() - _takenTime;

        if (span >= 500) {
            debugGraphics("Took " + wasStrong + " -" +  span, new Throwable());
        }else if (span >= 100)  {
            debugGraphics("Took " + wasStrong + " -" + span);
        }
    }

    static boolean _DOLOG = false;
    public void debugGraphics(String text) {
        if (_DOLOG) {
            MXFileLogger.getLogger(PriorityLock.class).log(Level.INFO, text);
        }
    }

    public void debugGraphics(String text, Throwable e) {
        if (_DOLOG) {
            MXFileLogger.getLogger(PriorityLock.class).log(Level.INFO, text);
        }
    }
}
