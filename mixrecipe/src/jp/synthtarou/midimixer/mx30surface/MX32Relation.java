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

import java.util.Comparator;
import java.util.TreeSet;
import jp.synthtarou.midimixer.libs.midi.MXMessage;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX32Relation {

    static Comparator<MGStatus> _comp = new Comparator<>() {
        @Override
        public int compare(MGStatus o1, MGStatus o2) {
            if (o1 == o2) {
                return 0;
            }
            int x;

            x = o1._port - o2._port;
            if (x == 0) {
                x = o1._uiType - o2._uiType;
            }
            if (x == 0) {
                x = o1._row - o2._row;
            }
            if (x == 0) {
                x = o1._column - o2._column;
            }
            return x;
        }
    };
    
    MX32Mixer _mixer;
    MGStatus _creator;
    public MXMessage _base;
    public MX32Relation _chanMessage;
    public TreeSet<MGStatus> _listStatus = new TreeSet(_comp);

    public MX32Relation(MX32Mixer mixer) {
        _mixer = mixer;
        _creator = null;
        _base = null;
    }

    public MX32Relation(MX32Mixer mixer, MXMessage message) {
        _mixer = mixer;
        _creator = null;
        _base = message;
    }

    public void push(MXMessage message, MGStatus status) {
        MX32Relation relation = this;
        while (relation != null) {
            if (relation._base == message) {
                relation._listStatus.add(status);
                return;
            }
            relation = relation._chanMessage;
        }
        MX32Relation last = this;
        while (last._chanMessage != null){
            last = last._chanMessage;
        }
        last._chanMessage = new MX32Relation(_mixer, message);
        last._chanMessage.push(message, status);
    }
    
    public boolean contains(MGStatus status) {
        MX32Relation relation = this;
        while (relation != null) {
            if (relation._listStatus.contains(status)) {
                return true;
            }
            relation = relation._chanMessage;
        }
        return false;
    }
}
