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
import jp.synthtarou.mixtone.synth.soundfont.wrapper.PBAGEntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.PGENEntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.PHDREntry;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ModelForPHDREntry extends AbstractListModel<String>{
    PHDREntry _phdr;

    public ModelForPHDREntry(PHDREntry phdr) {
        _phdr = phdr;
    }

    @Override
    public int getSize() {
        return  _phdr.countPBAGEntry();
    }
    
    public PBAGEntry getPBAGEntry(int index) {
        return _phdr.getPBAGEntry(index);
    }
    
    @Override
    public String getElementAt(int index) {
        PBAGEntry e = getPBAGEntry(index);
        PGENEntry pgen = e.getPGENEntry();

        StringBuilder str = new StringBuilder();
        str.append("{PGEN:");
        boolean first = true;
        XTGenOperatorMaster master = XTGenOperatorMaster.getMaster();

        for (int x = 0;x <= XTGenOperatorMaster.endOper; ++ x) {
            XTGenOperatorMasterEntry ope = master.getEntry(x);
            Integer generator = pgen.getGenerator(x);
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
