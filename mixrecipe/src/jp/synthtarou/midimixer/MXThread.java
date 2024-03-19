/*
 * Copyright (C) 2024 yaman
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

import java.util.logging.Level;
import jp.synthtarou.midimixer.libs.common.MXLogger2;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXThread extends Thread {
    static ThreadGroup _group = new ThreadGroup("MIDIMixer");
    Runnable _run;
    
    public MXThread(String name, Runnable run) {
        super(_group, run, name);
    }

    public static void listThread() {
        _group.list();
    }

    public static void exitAll() {
        int count = _group.activeCount();
        Thread[] list = new Thread[count + 5];
        _group.enumerate(list);
        for (int i = 0; i < list.length; ++ i) {
            Thread t = list[i];
            if (t == null) {
                continue;
            }
            if (t.isAlive()) {
                MXLogger2.getLogger(MXThread.class).info("Checked as Alive [" + i + "] " + t.getName());
            }
        }
        for (int i = 0; i < list.length; ++ i) {
            Thread t = list[i];
            if (t == null) {
                continue;
            }
            if(t.isAlive()) {
                MXLogger2.getLogger(MXThread.class).info("Exit thread [" + i + "] " + t.getName());
                t.interrupt();
                try {
                    t.join();
                } catch (InterruptedException ex) {
                    MXLogger2.getLogger(MXThread.class).log(Level.SEVERE, null, ex);
                }
            }
        }
        MXLogger2.getLogger(MXThread.class).info("Finally ... ");
        _group.list();
    }
}
