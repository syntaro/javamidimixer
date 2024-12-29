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
public class PBAGEntry {
    XTFile _sfz;
    int _index;
    PGENEntry _PGENEntry;
    int _genFrom;
    int _genCount;
    
    public PBAGEntry(XTFile sfz, int bagIndex) {
        _sfz = sfz;

        Number pgenFrom = _sfz._pbag.get(bagIndex).numberColumn(SFZElement.PBAG_PGENINDEX, null);
        Number pgenNext = _sfz._pbag.get(bagIndex+1).numberColumn(SFZElement.PBAG_PGENINDEX, null);
        
        if (pgenFrom == null || pgenNext == null) {
            throw new NullPointerException();
        }
        
        _genFrom = pgenFrom.intValue();
        _genCount = pgenNext.intValue() - _genFrom;
        _PGENEntry = null;
    }
    
    public int countPGENEntry() {
        return _genCount;
    }
    
    public PGENEntry getPGENEntry() {
        PGENEntry p = _PGENEntry;
        if (p == null) {
            p = new PGENEntry(_sfz, _genFrom, _genCount);
            _PGENEntry = p;
        }
        return p;
    }
}
