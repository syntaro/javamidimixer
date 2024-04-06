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
 * You should havereceived a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jp.synthtarou.midimixer.libs.midi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import jp.synthtarou.midimixer.libs.midi.visitant.MXDataentry;
import jp.synthtarou.midimixer.libs.midi.visitant.MXVisitant;

/**
 * エントランス番号ごとに、あるメッセージは、1度づつしか通れないという制御を行う
 * @author Syntarou YOSHIDA
 */
@Deprecated
public class MXEntrance {
    static class MessageComparator implements Comparator<MXMessage> {

        @Override
        public int compare(MXMessage o1, MXMessage o2) {
            if (o1.isDataentryBy2()) {
                if (o2.isDataentryBy2() == false) {
                    return -1;
                }

                return o1.compareTo(o2);
            }else if (o2.isDataentryBy2()) {
                return 1;
            }
            
            int d1 = o1.getDwordCount();
            int d2 = o2.getDwordCount();
            if (d1 > 0 && d1 == d2) {
                for (int i = 0; i < d1; ++ i) {
                    int r = o1.getAsDword(i) - o2.getAsDword(i);
                    if (r < 0) return -1;
                    if (r > 0) return 1;
                }
            }
                
            byte[] t1 = o1.getBinary();
            byte[] t2 = o2.getBinary();
            
            if (t1 == null) {
                if (t2 == null) {
                    return 0;
                }
                return  -1;
            }
            if (t2 == null) {
                return 1;
            }            

            int x = t1.length - t2.length;
            
            if (x < 0) return -1;
            if (x > 0) return 1;

            for (int i = 0; i < t1.length; ++ i) {
                x = t1[i] - t2[i];
                if (x < 0) return -1;
                if (x > 0) return 1;
            }
            x = o1.getPort() - o2.getPort();
            if (x < 0) return -1;
            if (x > 0) return 1;

            return 0;
        }
    }

    public static class Entrance extends TreeSet<MXMessage> {
        Entrance() {
            super(new MessageComparator());
        }
        
        public boolean rideOn(MXMessage message) {
            if (contains(message)) {
                return false;
            }
            add(message);
            return true;
        }
    }
    
    ArrayList<Entrance> _listEntrance = new ArrayList();
    
    public synchronized Entrance getEntrance(int entrance) {
        while (_listEntrance.size() <= entrance) {
            _listEntrance.add(new Entrance());
        }
        return _listEntrance.get(entrance);
    }
    
    public synchronized boolean rideOn(int entranceNo, MXMessage message) {
        Entrance entrance = getEntrance(entranceNo);
        return entrance.rideOn(message);
    }
}
