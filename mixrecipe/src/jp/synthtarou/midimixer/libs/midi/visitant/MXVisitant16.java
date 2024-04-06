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
package jp.synthtarou.midimixer.libs.midi.visitant;

import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXVisitant16 {
    
    MXVisitant[] _array;

    public MXVisitant16() {
        _array = new MXVisitant[16];
        for(int i = 0; i < _array.length; ++ i) {
            _array[i] = new MXVisitant();
        }
    }

    public MXVisitant get(int channel) {
        return _array[channel];
    }
    
    public String toString() {
        ArrayList<MXVisitant> list = new ArrayList();
        for (MXVisitant v : _array) {
            list.add(v);
        }
        return list.toString();
    }
}
