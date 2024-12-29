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
package jp.synthtarou.mixtone.synth.view.listmodel;

import javax.swing.AbstractListModel;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.SHDREntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.SHDREntryList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ModelForISHDREntryList extends AbstractListModel<String>{
    SHDREntryList _shdrEntryList;
    
    public ModelForISHDREntryList(SHDREntryList shdrEntryList) {
        _shdrEntryList = shdrEntryList;
    }

    @Override
    public int getSize() {
        return _shdrEntryList.count();
    }
    
    public SHDREntry getSHDREntry(int index) {
        return _shdrEntryList.getSHDREntry(index);
    }

    @Override
    public String getElementAt(int index) {
        SHDREntry e = _shdrEntryList.getSHDREntry(index);

        StringBuilder str = new StringBuilder();
        str.append("{SHDR:");
        str.append("sampleId=" + index + ", ");
        str.append("name=" + e.getName());
        str.append("start=" + e.getStart());
        str.append("end=" + e.getEnd());
        str.append("}");
        return str.toString();
    }
}
