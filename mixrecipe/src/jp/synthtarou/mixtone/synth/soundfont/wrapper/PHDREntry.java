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
public class PHDREntry {
    XTFile _sfz;
    int _presetIndex;
    PBAGEntry[] _listPBAGEntry;
    
    int _bagFrom;
    int _bagCount;

    public PHDREntry(XTFile sfz, int preset) {
        _sfz = sfz;
        _presetIndex = preset;
        
        Number bagFrom = _sfz._phdr.get(preset).numberColumn(SFZElement.PHDR_BAGINDEX, null);
        if (bagFrom == null) {
            throw new NullPointerException(preset + " was null");
        }
        _bagFrom = bagFrom.intValue();

        if (preset + 1 < _sfz._phdr.size()) {
            Number bagNext = _sfz._phdr.get(preset + 1).numberColumn(SFZElement.PHDR_BAGINDEX, null);
            if (bagNext == null) {
                throw new NullPointerException((preset+1) + " was null");
            }
            _bagCount = bagNext.intValue() - _bagFrom;
        }
        else {
            _bagCount = 0;
        }
        
        _listPBAGEntry = new PBAGEntry[_bagCount];
    }
    
    public int countPBAGEntry() {
        return _bagCount;
    }

    public PBAGEntry getPBAGEntry(int index) {
        PBAGEntry b = _listPBAGEntry[index];
        if (b == null) {
            b = new PBAGEntry(_sfz, _bagFrom + index);
            _listPBAGEntry[index] = b;
        }
        return b;
    }

    public String getName() {
        return _sfz._phdr.get(_presetIndex).textColumn(SFZElement.PHDR_NAME);
    }
}
