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
package jp.synthtarou.midimixer.libs.midi.programlist.database;

import java.util.Collection;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class PDMap implements DoubleIndexElement{
    int _seq;
    String _name;
    static int _seqReserve = 100;
    
    DoubleIndex<PDProgram> _programSet = new DoubleIndex();

    public PDMap(String name) {
        super();
        _name = name;
        _seq = _seqReserve ++;
    }
    
    public String getName() {
        return _name;
    }

    public Collection<PDProgram> listPrograms() {
        return _programSet.values();
    }

    public PDProgram get(int id) {
        return _programSet.get(id);
    }

    public PDProgram smartReserve(int id, String name) {
        PDProgram x = get(id);
        if (x == null) {
            x = new PDProgram(id, name);
            _programSet.put(x);
        }
        return x;
    }

    @Override
    public int getId() {
        return _seq;
    }
    
    public boolean isEmpty() {
        for (PDProgram x : _programSet.values()) {
            if (x._bankMap.size() >= 1) {
                return false;
            }
        }
        return true;
    }
}
