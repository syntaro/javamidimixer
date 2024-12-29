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

import jp.synthtarou.mixtone.synth.soundfont.XTFile;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class INSTEntryList {
    XTFile _sfz;
    INSTEntry[] _listINSTEntry;

    public INSTEntryList(XTFile sfz) {
        _sfz = sfz;
        
        int count = _sfz._inst.size();
        _listINSTEntry = new INSTEntry[count];
    }
    
    public int count() {
        return _listINSTEntry.length;
    }
    
    public INSTEntry getINSTEntry(int instrument) {
        INSTEntry e = _listINSTEntry[instrument];
        if (e == null) {
            e = new INSTEntry(_sfz, instrument);
            _listINSTEntry[instrument] = e;
        }
        return e;
    }
}
