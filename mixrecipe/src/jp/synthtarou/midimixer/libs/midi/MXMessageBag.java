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
package jp.synthtarou.midimixer.libs.midi;

import java.util.LinkedList;
import java.util.TreeSet;
import java.util.stream.Collector;
import jp.synthtarou.midimixer.mx30surface.MGStatus;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMessageBag {
    LinkedList<MXMessage> _listQuque = new LinkedList<>();
    LinkedList<MXMessage> _listTranslated = new LinkedList<>();
    LinkedList<Runnable> _listTranslatedTask = new LinkedList<>();

    TreeSet<MXMessage> _alreadyQueue = new TreeSet<>();
    TreeSet<MXMessage> _alreadyTranslated = new TreeSet<>();
    
    TreeSet<MGStatus> _touchedStatus = new TreeSet<>();

    public int _resultCode = 0;
    
    public MXMessageBag() {
    }
    
    public synchronized void addQueue(MXMessage message) {
        if (_alreadyQueue.contains(message)) {
            return;
        }
        _alreadyQueue.add(message);
        _listQuque.add(message);
    }

    public synchronized MXMessage popQueue() {
        if (_listQuque.isEmpty()) {
            return null;
        }
        return _listQuque.removeFirst();
    }
    
    public synchronized void addTranslated(MXMessage message) {
        message = (MXMessage)message.clone();
        if (_alreadyTranslated.contains(message)) {
            return ;
        }
        _alreadyTranslated.add(message);
        _listTranslated.add(message);
    }

    public synchronized void addTranslatedTask(Runnable task) {
        _listTranslatedTask.add(task);
    }

    public synchronized MXMessage popTranslated() {
        if (_listTranslated.isEmpty()) {
            return null;
        }
        return _listTranslated.removeFirst();
    }

    public synchronized Runnable popTranslatedTask() {
        if (_listTranslatedTask.isEmpty()) {
            return null;
        }
        return _listTranslatedTask.removeFirst();
    }
    
    public synchronized void addTouochedStatus(MGStatus status) {
        _touchedStatus.add(status);
    }

    public synchronized MGStatus[] listTouchedStatus() {
        MGStatus[] result = new MGStatus[_touchedStatus.size()];
        _touchedStatus.toArray(result);
        return result;
    }
}
