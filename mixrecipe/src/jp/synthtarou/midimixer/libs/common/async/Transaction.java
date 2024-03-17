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

import java.util.logging.Level;
import jp.synthtarou.midimixer.libs.common.MXLogger2;
import jp.synthtarou.midimixer.libs.common.MXUtil;


/**
 *
 * @author Syntarou YOSHIDA
 */
public class Transaction implements Runnable {
    protected int _transactionTicket;
    protected boolean _running;
    protected int _result;
    protected String _taskName;
    protected Runnable _then;
    protected boolean _doneThen;
    protected boolean _canceled;
    
    protected Transaction(int ticket) {
        _transactionTicket = ticket;
    }
    
    public Transaction copyWithNewTicket(String newName) {
        Transaction vst = new Transaction(this);
        vst._taskName = newName;
        return vst;
    }
    
    public Transaction(String taskName) {
        _running = true;
        _taskName = taskName;
        
        TransactionBox.getInstance().entryWithTicket(this);
    }
    
    private Transaction(Transaction parent) {
        _running = true;
        _taskName = parent._taskName;
        _then = parent;
        
        TransactionBox.getInstance().entryWithTicket(this);
    }

    protected final void notifyFinished(int result) {
        _result = result;
        _running = false;
        synchronized(this) {
            this.notifyAll();
        }

        try {
            run();
        }catch(RuntimeException ex) {
            MXLogger2.getLogger(Transaction.class).log(Level.WARNING, ex.getMessage(), ex);
        }
        
        boolean needThen = false;
        
        synchronized (this) {
            if (_then != null && _doneThen == false) {
                needThen = true;
            }
        }
        
        if (needThen) {
            try {
                _doneThen = true;
                _then.run();
            }catch(RuntimeException ex) {
                MXLogger2.getLogger(Transaction.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    public final void letsCancel() {
        TransactionBox.getInstance().callmeWhenCanceled(_transactionTicket);
    }
    
    public final int awaitResult() {
        synchronized (this) {
            while(_running) {
                try {
                    this.wait(1000);
                }catch(InterruptedException ex) {
                    MXLogger2.getLogger(Transaction.class).log(Level.WARNING, ex.getMessage(), ex);
                    _result = -1;
                    break;
                }
            }
        }
        return _result;
    }
    
    public final void setThen(Runnable run) {
        _then = run;
        
        boolean needThen = false;
        synchronized (this) {
            if (!_running) {
                if (_then != null && _doneThen == false) {
                    _doneThen = true;
                    needThen = true;
                }
            }
        }
        if (needThen) {
            try {
               _then.run();
            }catch(RuntimeException ex) {
                MXLogger2.getLogger(Transaction.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    
    public final int getTransactionTicket() {
        return _transactionTicket;
    }

    @Override
    public void run() {
        //override
    }
}
