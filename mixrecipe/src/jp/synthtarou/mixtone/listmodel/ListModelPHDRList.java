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
public class ListModelPHDRList extends AbstractListModel<String>{
    XTFile _sfz;
    SFZElement.SFZElement_phdr _phdr;

    public ListModelPHDRList(XTFile sfz) {
        _sfz = sfz;
        _phdr = (SFZElement.SFZElement_phdr)sfz.getElement("phdr");
    }

    public ListModelPHDRBagTable getAsPHDRBagTable(int x) {
        XTTable bag = _phdr.get(x).tableColumn(SFZElement.PHDR_BAGINDEX_TABLE);
        if (bag == null) {
            return null;
        }
        return new ListModelPHDRBagTable(bag);
    }

    @Override
    public int getSize() {
        return  _phdr.size();
    }

    @Override
    public String getElementAt(int index) {
        XTRow row  = _phdr.get(index);
        int number = row.intColumn(SFZElement.PHDR_PRESETNO);
        String name = row.textColumn(SFZElement.PHDR_NAME);
        return index +" [" + number +"] = "+ name + "*" + row.getDump();
    }
}
