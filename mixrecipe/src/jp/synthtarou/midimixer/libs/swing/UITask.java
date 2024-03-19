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
package jp.synthtarou.midimixer.libs.swing;

import java.util.logging.Level;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.common.MXLogger2;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class UITask<T> {
    public T _result = null;
    boolean _doing = true;
    public Throwable _targetException = null;
    public Throwable _interruped = null;
    
    public static final Object NOTHING = null;

    public UITask() {
        this(false);
    }
    
    public UITask(boolean forceLater) {
        if (forceLater || SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    callerForTask();
                }
            });
        }else {
            callerForTask();
        }
    }

    
    public boolean isDone() {
        return !_doing;
    }
    
    public boolean isSucseed() {
        waitResult();
        return _targetException == null;
    }
    
    public Throwable getTargetException() {
        return _targetException != null ? _targetException : _interruped; //right ?
    }

    public synchronized T waitResult() {
        while (_doing) {
            try {
                wait(1000);
            }catch(InterruptedException ex) {
                _interruped = ex;
                break;
            }
        }
        return _result;
    }

    private void callerForTask() {
        try {
            _result = run();
        }catch(Throwable ex) {
            MXLogger2.getLogger(UITask.class).log(Level.SEVERE, ex.getMessage(), ex);
            _targetException = ex;
        }finally {
            _doing = false;
            synchronized (this) {
                notifyAll();
            }
        }
    }
    
    public abstract T run();
}
