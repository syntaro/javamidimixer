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
package jp.synthtarou.mixtone.synth.soundfont.wrapper;

import jp.synthtarou.mixtone.synth.soundfont.SFZElement;
import jp.synthtarou.mixtone.synth.soundfont.XTFile;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class INSTEntry {
    XTFile _sfz;
    int _instIndex;
    IBAGEntry[] _listIBAGEntry;
    
    int _bagFrom;
    int _bagCount;

    public INSTEntry(XTFile sfz, int instrument) {
        _sfz = sfz;
        _instIndex = instrument;

        Number bagFrom = _sfz._inst.get(instrument).numberColumn(SFZElement.INST_BAGINDEX, null);
        if (bagFrom == null) {
            throw new NullPointerException();
        }
        _bagFrom = bagFrom.intValue();
        
        if (instrument + 1 < _sfz._inst.size()) {
            Number bagNext = _sfz._inst.get(instrument + 1).numberColumn(SFZElement.INST_BAGINDEX, null);
            if (bagNext == null) {
                throw new NullPointerException();
            }
            _bagCount = bagNext.intValue() - _bagFrom;
        }
        else {
            _bagCount = 0;
        }
        _listIBAGEntry = new IBAGEntry[_bagCount];
    }
    
    public String getName() {
        return _sfz._inst.get(_instIndex).textColumn(SFZElement.INST_NAME);
    }
    
    public int countIBAGEntry() {
        return _bagCount;
    }

    public IBAGEntry getIBAGEntry(int index) {
        IBAGEntry b = _listIBAGEntry[index];
        if (b == null) {
            b = new IBAGEntry(_sfz, _bagFrom + index);
            _listIBAGEntry[index] = b;
        }
        return b;
    }
}
