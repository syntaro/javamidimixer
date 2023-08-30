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

/**
 *
 * @author Syntarou YOSHIDA
 */
public class PDBank implements DoubleIndexElement {
    int _bankMSB;
    int _bankLSB;
    String _bankName;
    PDProgram _parent;
    PDDrumSet _drumSet;

    public PDBank(PDProgram parent, int msb, int lsb, String name) {
        _parent = parent;
        _bankMSB = msb;
        _bankLSB = lsb;
        _bankName = name;
    }
    
    public int getBankMSB() {
        return _bankMSB;
    }

    public int getBankLSB() {
        return _bankLSB;
    }
    
    public String getName() {
        return _bankName;
    }

    public int getId() {
        return _bankMSB << 8 | _bankLSB;
    }
    
    public PDDrumSet smartReserve(PDDrumSet set) {
        if (_drumSet == null) {
            if (set == null) {
                set = new PDDrumSet();
            } 
            _drumSet = set; 
        }
        return _drumSet;
    }
    
    public PDDrumSet getDrum() {
        return _drumSet;
    }
}
