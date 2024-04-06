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
import jp.synthtarou.midimixer.mx30surface.MGStatus;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMessageBag {
    LinkedList<MXMessage> _listQuque = new LinkedList<>();
    LinkedList<MXMessage> _listResult = new LinkedList<>();
    LinkedList<Runnable> _listResultTask = new LinkedList<>();

    TreeSet<MXMessage> _alreadyQueue = new TreeSet<>();
    TreeSet<MXMessage> _alreadyResult = new TreeSet<>();
    
    TreeSet<MGStatus> _touchedStatus = new TreeSet<>();

    public int _resultCode = 0;
    
    public MXMessageBag() {
    }
    
    public void dumpQueue() {
        System.out.println("dumpqueue "  +_listResult.toString());
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
    
    public synchronized void addResult(MXMessage message) {
        //message = (MXMessage)message.clone();
        if (_alreadyResult.contains(message)) {
            return ;
        }
        _alreadyResult.add(message);
        _listResult.add(message);
    }

    public synchronized void addResultTask(Runnable task) {
        _listResultTask.add(task);
    }

    public synchronized MXMessage popResult() {
        if (_listResult.isEmpty()) {
            return null;
        }
        return _listResult.removeFirst();
    }

    public synchronized Runnable popResultTask() {
        if (_listResultTask.isEmpty()) {
            return null;
        }
        return _listResultTask.removeFirst();
    }
    
    public synchronized void addTouchedStatus(MGStatus status) {
        _touchedStatus.add(status);
    }

    public synchronized MGStatus[] listTouchedStatus() {
        MGStatus[] result = new MGStatus[_touchedStatus.size()];
        _touchedStatus.toArray(result);
        return result;
    }
    
    public synchronized boolean isTouchedStatus(MGStatus status) {
        return _touchedStatus.contains(status);
    }
    
    public void clearTouchedStatus() {
        _touchedStatus.clear();
    }
}
