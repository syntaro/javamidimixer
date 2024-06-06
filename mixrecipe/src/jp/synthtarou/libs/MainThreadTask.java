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
package jp.synthtarou.libs;

import jp.synthtarou.libs.log.MXFileLogger;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MainThreadTask<T> {
    public T _result = null;
    boolean _doing = true;
    public Throwable _errorWhenInvoke = null;
    public Throwable _errorHappens = null;
    
    public static final Object NOTHING = null;

    public MainThreadTask() {
        this(false);
    }
    
    public MainThreadTask(boolean forceInvokeLater) {
        if (forceInvokeLater || SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(this::callerForTask);
        }else {
            callerForTask();
        }
    }

    
    public boolean isDone() {
        return !_doing;
    }
    
    public boolean isResultFine() {
        waitResult();
        return _errorHappens == null;
    }
    
    public Throwable getError() {
        return _errorHappens; //right ?
    }

    public T waitResult() {
        synchronized (this) {
            while (_doing) {
                try {
                    wait(1000);
                }catch(InterruptedException ex) {
                    _errorHappens = ex;
                }
            }
            if (_errorWhenInvoke != null) {
                _errorHappens = new InvocationTargetException(_errorWhenInvoke);
            }
            return _result;
        }
    }

    private void callerForTask() {
        try {
            if (SwingUtilities.isEventDispatchThread() == false) {
                throw new IllegalStateException("Not MainThread");
            }
            _result = runTask();
        }catch(Throwable ex) {
            MXFileLogger.getLogger(MainThreadTask.class).log(Level.SEVERE, ex.getMessage(), ex);
            _errorWhenInvoke = ex;
        }finally {
            _doing = false;
            synchronized (this) {
                notifyAll();
            }
        }
    }
    
    public abstract T runTask();
}
