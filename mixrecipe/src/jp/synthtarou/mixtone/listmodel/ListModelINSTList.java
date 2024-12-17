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
package jp.synthtarou.mixtone.listmodel;

import javax.swing.AbstractListModel;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;
import jp.synthtarou.mixtone.synth.soundfont.table.XTTable;
import jp.synthtarou.mixtone.synth.soundfont.SFZElement;
import jp.synthtarou.mixtone.synth.soundfont.XTFile;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ListModelINSTList extends AbstractListModel<String> {
    XTFile _sfz;
    SFZElement.SFZElement_inst _inst;
    
    public ListModelINSTList(XTFile sfz) {
        _sfz = sfz;
        _inst = sfz.getElement_inst();
    }
    
    public ListModelINSTBagTable getAsINSTBagTable(int x) {
        if (x >= 0 && x < _inst.size()) {
            XTTable bag = _inst.get(x).tableColumn(SFZElement.INST_BAGINDEX_TABLE);
            if (bag != null) {
                return new ListModelINSTBagTable(bag);
            }
        }
        return null;
    }

    @Override
    public int getSize() {
        return _inst.size();
    }

    @Override
    public String getElementAt(int index) {
        XTRow row = _inst.get(index);
        String name = row.textColumn(SFZElement.INST_NAME);
        return index + " [] " + name;
    }
}
