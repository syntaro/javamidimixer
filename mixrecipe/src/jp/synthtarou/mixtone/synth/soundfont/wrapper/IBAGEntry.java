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
public class IBAGEntry {
    XTFile _sfz;
    int _instIndex;
    int _index;
    IGENEntry _IGENEntry;

    int _genFrom;
    int _genCount;
    
    public IBAGEntry(XTFile sfz, int bagIndex) {
        _sfz = sfz;

        Number igenFrom = _sfz._ibag.get(bagIndex).numberColumn(SFZElement.IBAG_IGENINDEX, null);
        Number igenNext = _sfz._ibag.get(bagIndex+1).numberColumn(SFZElement.IBAG_IGENINDEX, null);
        
        if (igenFrom == null || igenNext == null) {
            throw new NullPointerException();
        }
        
        _genFrom = igenFrom.intValue();
        _genCount = igenNext.intValue() - _genFrom;
        _IGENEntry = null;
    }
    
    public IGENEntry getIGENEntry() {
        IGENEntry i = _IGENEntry;
        if (i == null) {
            i = new IGENEntry(_sfz, _genFrom, _genCount);
            _IGENEntry = i;
        }
        return i;
    }
    
    public XTFile getXTFile() {
        return _sfz;
    }
}
