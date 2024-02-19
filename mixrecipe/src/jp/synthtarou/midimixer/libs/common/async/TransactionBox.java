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
package jp.synthtarou.midimixer.libs.common.async;

import java.util.Comparator;
import java.util.TreeMap;
import jp.synthtarou.midimixer.MXMain;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class TransactionBox {
    private static TransactionBox _instance = new TransactionBox();

    public static TransactionBox getInstance() {
        return _instance;
    }
    
    protected TransactionBox() {
        _lastSentTicket = 1;
    }
    
    public synchronized void entryWithTicket(Transaction tr) {
        do {
            tr._transactionTicket = _lastSentTicket ++;
            if (_lastSentTicket >= 65000) {
                _lastSentTicket = 1;
            }
        }while(_keepTable.containsKey(tr));
        _keepTable.put(tr, tr);
    }
    
    public final void callmeWhenFinished(int ticket, int result) {
        Transaction tr = getTransaction(ticket);
        if (tr != null) {
            tr.notifyFinished(result);
            closeTicket(tr);
            if (result < 0) {
                MXMain.printDebug("Error in " + tr._taskName + " result = " + result);
            }
        }
    }
    
    public final void callmeWhenCanceled(int ticket) {
        Transaction tr = getTransaction(ticket);
        tr._canceled = true;
        tr._result = -1;
        tr._running = false;
        synchronized (this) {
            notifyAll();
        }
        closeTicket(tr);
        MXMain.printDebug("Task Canceled " + tr.getTransactionTicket() + " : " + tr._taskName);
    }

    protected synchronized void closeTicket(Transaction tr) {
        _keepTable.remove(tr);
    }

    private synchronized Transaction getTransaction(int ticket) {
        Transaction dummy = new Transaction(ticket);
        return _keepTable.get(dummy);
    }
    
    Comparator<Transaction> tr = new Comparator<Transaction>() {
        @Override
        public int compare(Transaction o1, Transaction o2) {
            if (o1._transactionTicket < o2._transactionTicket) return -1;
            if (o1._transactionTicket > o2._transactionTicket) return  1;
            return 0;
        }
    };
    
    TreeMap<Transaction, Transaction> _keepTable = new TreeMap(tr);
    int _lastSentTicket;
}
