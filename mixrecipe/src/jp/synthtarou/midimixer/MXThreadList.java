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
package jp.synthtarou.midimixer;

import java.util.ArrayList;
import java.util.logging.Level;
import jp.synthtarou.midimixer.libs.common.MXLogger2;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXThreadList {
    ArrayList<ManagedThread> _listThread = new ArrayList();
    
    class ManagedThread  {
        String _name;

        class Capsule implements Runnable {
            Runnable _r;

            public Capsule(ManagedThread t, Runnable r) {
                _r = r;
            }

            @Override
            public void run() {
                try {
                    _r.run();
                }catch(RuntimeException ex) {
                    MXLogger2.getLogger(MXThreadList.class).log(Level.WARNING, ex.getMessage(), ex);
                }
                _listThread.remove(ManagedThread.this);
            }
        }

        Thread _thread;
        Throwable _launch;

        ManagedThread(String name, Runnable run) {
            _name = name;
            _thread = new Thread(new Capsule(this, run));
            _launch = new Throwable();
            _listThread.add(this);
        }

        ManagedThread(String name, Thread thread) {
            _name = name;
            _thread = thread;
            _launch = new Throwable();
            _listThread.add(this);
        }
    }

    protected ManagedThread launch(String name, Runnable run) {
        return new ManagedThread(name, run);
    }

    protected void exitRoutine() {
        System.out.println("Checking another Thread ...");
        for (ManagedThread t : _listThread) {
            if (t._thread == Thread.currentThread()) {
                continue;
            }
            /*
            if (t._thread.isDaemon()) {
                continue;
            }
            */
            if (t._thread.isAlive()) {
                if (t._thread.isDaemon()) {
                    System.out.println("Still running daemon ... " + t._name);
                }else {
                    System.out.println("Still running thread ... " + t._name);
                }
            }
        }
        System.out.println("Done check another Thread ...");
    }

    static MXThreadList _instance = new MXThreadList();
    
    public static Thread newThread(String name, Runnable run) {
        return _instance.launch(name, run)._thread;
    }

    public static Thread newDaemon(String name, Runnable run) {
        Thread t = _instance.launch(name, run)._thread;
        t.setDaemon(true);
        return t;
    }
    
    public static void attachIfNeed(String name, Thread thread) {
        _instance.attachImpl(name, thread);
    }
    
    void attachImpl(String name, Thread thread) {
        for (int i = 0; i < _listThread.size(); ++ i) {
            ManagedThread te = _listThread.get(i);
            if (te._thread.isAlive() == false) {
                System.err.println("Thread Ended");
                _listThread.remove(i);
                -- i;
            }
        }
        for (ManagedThread te : _listThread) {
            if (te._thread == thread) {
                return;
            }
        }
        _listThread.add(new ManagedThread(name, thread));
    }
    
    public static void onExit() {
        _instance.exitRoutine();
    }
}
