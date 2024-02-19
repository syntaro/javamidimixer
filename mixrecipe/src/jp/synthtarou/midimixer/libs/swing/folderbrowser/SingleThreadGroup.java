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
package jp.synthtarou.midimixer.libs.swing.folderbrowser;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SingleThreadGroup {

    LinkedList<Thread> _registedList = new LinkedList();
    boolean _priorityLast = true;

    /**
     *
     */
    public SingleThreadGroup() {

    }

    public synchronized int countThread() {
        return _registedList.size();
    }

    public synchronized boolean addToTop(Thread t) {
        if (_registedList.contains(t)) {
            return false;
        }
        _registedList.add(t);
        return true;
    }

    public synchronized void removeFromList(Thread t) {
        _registedList.remove(t);
        this.notifyAll();
    }

    public synchronized void clearnUpUnused() {
        Iterator<Thread> it = _registedList.iterator();
        while (it.hasNext()) {
            Thread seek = it.next();
            if (seek.isAlive() == false) {
                it.remove();;
            }
        }
    }

    public synchronized boolean isMyTurn() {
        clearnUpUnused();
        if (_registedList.isEmpty()) {
            return false;
        }
        if (_priorityLast) {
            Thread t1 = Thread.currentThread();
            Thread t2 = _registedList.getLast();
            return t1 == t2;
        } else {
            Thread t1 = Thread.currentThread();
            Thread t2 = _registedList.getFirst();
            return t1 == t2;
        }
    }

    public boolean waitTillMyTurn() {
        while (true) {
            if (isMyTurn()) {
                return true;
            }
            synchronized (this) {
                try {
                    this.wait(1000);
                } catch (InterruptedException e) {
                    return false;
                }
            }
        }
    }
}
