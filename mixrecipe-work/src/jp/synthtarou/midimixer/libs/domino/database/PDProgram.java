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
package jp.synthtarou.midimixer.libs.domino.database;

import java.util.Collection;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class PDProgram  implements DoubleIndexElement{
    int _programNumber = -1;
    String _programName = "?";
    DoubleIndex<PDBank> _bankMap = new DoubleIndex();

    public PDProgram(int number, String name) {
        _programNumber = number;
        _programName = name;
    }
    
    public int getProgramNumber() {
        return _programNumber;
    }

    public String getName() {
        return _programName;
    }

    public Collection<PDBank> listBanks() {
        return _bankMap.values();
    }
    
    public PDBank get(int msb, int lsb) {
        PDBank seek = new PDBank(this, msb, lsb, getName());
        return _bankMap.get(seek.getId());
    }

    public PDBank smartReserve(int msb, int lsb, String name) {
        PDBank x = get(msb, lsb);
        if (x == null) {
            x = new PDBank(this, msb, lsb, name);
            _bankMap.put(x);
        }
        return x;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        //str.append("[Folder " + p._mapSeq + ":" + p._mapName + "]");
        //str.append("[Program " + p._programNumber + ":" + p._programName + "]");
        //str.append("[Bank " + p._bankSeq + ":" + p._bankName + "]");
        return str.toString();
    }

    @Override
    public int getId() {
        return _programNumber;
    }
}
