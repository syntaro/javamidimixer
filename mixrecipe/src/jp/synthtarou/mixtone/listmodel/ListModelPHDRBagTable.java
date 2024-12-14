/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
