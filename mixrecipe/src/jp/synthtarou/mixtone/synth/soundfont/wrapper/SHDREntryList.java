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
public class SHDREntryList {
    XTFile _sfz;
    SHDREntry[] _listSHDREntry;

    public SHDREntryList(XTFile sfz) {
        _sfz = sfz;
        
        int count = _sfz._shdr.size();
        _listSHDREntry = new SHDREntry[count];
    }
    
    public int count() {
        return _listSHDREntry.length;
    }
    
    public SHDREntry getSHDREntry(int sampleId) {
        SHDREntry e = _listSHDREntry[sampleId];
        if (e == null) {
            e = new SHDREntry(_sfz, sampleId);
            _listSHDREntry[sampleId] = e;
        }
        return e;
    }
}
