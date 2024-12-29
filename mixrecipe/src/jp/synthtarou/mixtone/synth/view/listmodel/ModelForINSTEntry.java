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
import jp.synthtarou.mixtone.synth.soundfont.XTGenOperatorMaster;
import jp.synthtarou.mixtone.synth.soundfont.XTGenOperatorMasterEntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.IBAGEntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.IGENEntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.INSTEntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.SHDREntry;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ModelForINSTEntry extends AbstractListModel<String>{
    INSTEntry _instEntry;
    
    public ModelForINSTEntry(INSTEntry instEntry) {
        _instEntry = instEntry;
    }

    @Override
    public int getSize() {
        return _instEntry.countIBAGEntry();
    }
    
    public IBAGEntry getIBAGEntry(int index) {
        return _instEntry.getIBAGEntry(index);
    }

    public SHDREntry getSHDREntry(int index) {
        IBAGEntry e = getIBAGEntry(index);
        IGENEntry igen = e.getIGENEntry();
        Integer sampleId = igen.getSampleId();
        if (sampleId == null) {
            return null;
        }
        SHDREntry shdr = new SHDREntry(e.getXTFile(), sampleId);
        return shdr;
    }
    
    
    @Override
    public String getElementAt(int index) {
        IBAGEntry e = _instEntry.getIBAGEntry(index);
        IGENEntry igen = e.getIGENEntry();

        StringBuilder str = new StringBuilder();
        str.append("{IGEN:");
        boolean first = true;
        XTGenOperatorMaster master = XTGenOperatorMaster.getMaster();

        for (int x = 0;x <= XTGenOperatorMaster.endOper; ++ x) {
            XTGenOperatorMasterEntry ope = master.getEntry(x);
            Integer generator = igen.getGenerator(x);
            if (generator == null) {
                continue;
            }
            Double param = ope.asParameter(generator);
            if (!first) {
                str.append(", ");
            }
            str.append(ope.getName() +"=" + generator + " mean " + param);
        }
        str.append("}");
        return str.toString();
    }
}
