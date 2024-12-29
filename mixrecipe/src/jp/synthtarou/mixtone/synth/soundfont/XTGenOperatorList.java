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
package jp.synthtarou.mixtone.synth.soundfont;

import jp.synthtarou.mixtone.synth.soundfont.wrapper.IBAGEntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.PBAGEntry;
import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTGenOperatorList {
    static XTGenOperatorMaster _genMaster = null;

    PBAGEntry _pgenTable;
    ArrayList<IBAGEntry> _igenTable;
    
    public XTGenOperatorList(XTFile sfz, int presetIndex, int key, int velocity) {
        if (_genMaster == null) {
            _genMaster = XTGenOperatorMaster.getMaster();
        }
        _sfz = sfz;
        _key = key;
        _presetIndex = presetIndex;
        _velocity = velocity;
        
        _pgenTable = new PBAGEntry(sfz, presetIndex);
    }

    XTFile _sfz;
    int _presetIndex;
    int _key;
    int _velocity;
}
