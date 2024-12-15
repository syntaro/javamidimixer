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

import java.util.ArrayList;
import javax.swing.AbstractListModel;
import jp.synthtarou.mixtone.synth.soundfont.table.XTHeader;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;
import jp.synthtarou.mixtone.synth.soundfont.table.XTTable;
import jp.synthtarou.mixtone.synth.soundfont.SFZElement;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ListModelPHDRBagTable extends AbstractListModel<String>{
    ArrayList<PBagLineMean> _data;
    XTTable _phdrBag;

    public final ArrayList<String> listAmountAsText(XTTable pgen) {
        ArrayList<String> result = new ArrayList<>();
        XTHeader header = pgen.getHeader();
        for (XTRow row : pgen) {
            String oper = row.textColumn(SFZElement.PGEN_GENOPER_MEAN);
            String amount = row.textColumn(SFZElement.PGEN_GENAMOUNT_MEAN);
            result.add(oper +": " + amount);
        }
        return result;
    }

    public ListModelPHDRBagTable(XTTable phdrBag) {
        _phdrBag = phdrBag;
        _data = new ArrayList<>();
        for (int i = 0; i < phdrBag.size(); ++ i) {
            XTRow row = phdrBag.get(i);
            PBagLineMean mean = new PBagLineMean(row);
            _data.add(mean);
        }
    }

    public int getInstrumentOf(int y) {
        return _data.get(y).instrument();
    }

    @Override
    public int getSize() {
        return _data.size();
    }

    @Override
    public String getElementAt(int index) {
        PBagLineMean mean = _data.get(index);
        String inst = "inst:" +  mean.instrument();
        String keyRange = "";
        String velRange = "";
        if (mean.keyRange() != null) {
            keyRange = " key from " + mean.keyRangeLo() + " to " + mean.keyRangeHi() + ", ";
        }
        if (mean.velRange() != null) {
            velRange = " vel from " + mean.velRangeLo() + " to " + mean.velRangeHi() + ", ";
        }
        return inst + keyRange + velRange;
    }
}
