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
package jp.synthtarou.midimixer.mx30surface;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import jp.synthtarou.midimixer.libs.midi.MXMessage;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX30Packet {
    LinkedList<MXMessage> _listQuque = new LinkedList<>();
    LinkedList<MXMessage> _listResult = new LinkedList<>();
    LinkedList<Runnable> _listResultTask = new LinkedList<>();

    TreeSet<MXMessage> _alreadyQueue = new TreeSet<>();
    TreeSet<MXMessage> _alreadyResult = new TreeSet<>();
    TreeSet<MGStatus> _listInvoked = new TreeSet<>();
    TreeMap<MGStatus, MGSliderMove> _listSliderMove = new TreeMap<>();
    
    public synchronized  void clearQueue() {
        /*
        int x = 0;
        x += _listQuque.size();
        x += _listResult.size();
        x += _listResultTask.size();
        x += _listSliderMove.size();
        if (x >= 1) {
            System.err.println("deleting unprocess " + x);
        }*/

        _listQuque.clear();
        _listResult.clear();
        _listResultTask.clear();
        
        _alreadyQueue.clear();
        _alreadyResult.clear();
        _listInvoked.clear();
        _listSliderMove.clear();
    }

    public MX30Packet() {
    }
    
    public void dumpQueue() {
        System.out.println("dumpqueue "  +_listResult.toString());
    }
    
    public synchronized boolean addQueue(MXMessage message) {
        if (_alreadyQueue.contains(message)) {
            return false;
        }
        _alreadyQueue.add(message);
        _listQuque.add(message);
        return true;
    }

    public synchronized MXMessage popQueue() {
        if (_listQuque.isEmpty()) {
            return null;
        }
        return _listQuque.removeFirst();
    }
    
    public synchronized void addResult(MXMessage message) {
        if (_alreadyResult.contains(message)) {
            return ;
        }
        _alreadyResult.add(message);
        _listResult.add(message);
    }
    
    public boolean isSkipableResult(MXMessage message) {
        return _alreadyResult.contains(message);
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
    
    public synchronized void addInvokedStatus(MGStatus status) {
        _listInvoked.add(status);
    }
    
    public synchronized boolean isInvokedStatus(MGStatus status) {
        return _listInvoked.contains(status);
    }
    
    public void clearTouchedStatus() {
        _listInvoked.clear();
    }
    
    public synchronized void addSliderMove(MGSliderMove move) {
        _listSliderMove.put(move._status, move);
    }
    
    public synchronized MGSliderMove popSliderMove() {
        if (_listSliderMove.isEmpty()) {
            return null;
        }
        MGStatus status = _listSliderMove.firstKey();
        MGSliderMove move = _listSliderMove.remove(status);
        if (move == null) {
            new Throwable().printStackTrace();;
        }
        return move;
    }
    
    public int getResultRollbackTicket() {
        return _listResult.size();
    }
    
    public void rollbackResult(int ticket) {
        while (_listResult.size() > ticket) {
            _listResult.removeLast();
        }
    }
}
