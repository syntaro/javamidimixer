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
package jp.synthtarou.midimixer.libs;

import jp.synthtarou.midimixer.MXThreadList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXLongJourney implements Runnable {
    private static MXLongJourney _single = new MXLongJourney();
    
    public MXLongJourney getSingle() {
        return _single;
    }
    
    MXQueue1<Runnable> _queue = new MXQueue1();
    Thread _thisThread;

    public MXLongJourney() {
        _thisThread = MXThreadList.newDaemon("MXLongJourney", this);
        _thisThread.setPriority((Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2);
        _thisThread.start();
    }
    
    public void push(Runnable run) {
        _queue.push(run);
    }

    @Override
    public void run() {
        while(true) {
            Runnable runner = _queue.pop();
            if (runner == null) {
                break;
            }
            try {
                runner.run();
            }catch(Throwable e) {
                e.printStackTrace();;
            }
        }
    }
}
