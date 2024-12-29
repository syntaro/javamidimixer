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

import java.util.ArrayList;
import javax.swing.AbstractListModel;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.INSTEntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.INSTEntryList;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.PBAGEntry;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ModelForINSTEntryList extends AbstractListModel<String> {
    INSTEntryList _listInst;
    ArrayList<INSTEntry> _listSearched;
    
    public ModelForINSTEntryList(INSTEntryList list) {
        _listInst = list;
    }

    public ModelForINSTEntryList(INSTEntryList list, PBAGEntry parent) {
        _listInst = list;

        if (parent != null) {
            _listSearched = new ArrayList<>();
            INSTEntry hit = list.getINSTEntry(parent.getPGENEntry().getInstrument());
            _listSearched.add(hit);
        }
    }
    
    @Override
    public int getSize() {
        if (_listSearched != null) {
            return _listSearched.size();
        }
        return _listInst.count();
    }
    
    public INSTEntry getINSTEntry(int index) {
        if (_listSearched != null) {
            return _listSearched.get(index);
        }
        return _listInst.getINSTEntry(index);
    }

    @Override
    public String getElementAt(int index) {
        INSTEntry entry = getINSTEntry(index);
        String name = entry.getName();
        return index + " [] " + name;
    }
    
}
