/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.listmodel;

import java.util.ArrayList;
import javax.swing.AbstractListModel;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;
import jp.synthtarou.mixtone.synth.soundfont.table.XTTable;
import jp.synthtarou.mixtone.synth.soundfont.SFZElement;
import jp.synthtarou.mixtone.synth.soundfont.XTGenOperatorMaster;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ListModelINSTBagTable extends AbstractListModel<String>{
    XTTable _instBag;
    ArrayList<Element> _data;
    
    class Element {
        int sampleId = -1;
        int keyRange1 = -1;
        int keyRange2 = -1;
        int velRange1 = -1;
        int velRange2 = -1;
        int overridingRootKey = -1;
        int keyNumber = -1;
        boolean _loop = false;
    }
    
    public final Number getAmountOfOper(XTTable igen, int oper, Number defValue) {
        XTRow row = igen.findRow(SFZElement.IGEN_GENOPER, oper);
        if (row != null) {
            Number num = row.intColumn(SFZElement.IGEN_GENAMOUNT);
            if (num == null) {
                return defValue;
            }
            return num.intValue();
        }
        return defValue;
    }

    public ListModelINSTBagTable(XTTable instBag) {
        _instBag = instBag;
        _data = new ArrayList<>();
        for (int i = 0; i < instBag.size(); ++ i) {
            XTRow row = instBag.get(i);
            XTTable igen = row.tableColumn(SFZElement.IBAG_IGENINDEX_TABLE);
            XTTable imod = row.tableColumn(SFZElement.IBAG_IMODINDEX_TABLE);
            
            Element e = new Element();
            Number loop = getAmountOfOper(igen, XTGenOperatorMaster.sampleModes, null);
            e._loop = (loop != null && loop.intValue() > 0);

            Number sampleId = getAmountOfOper(igen, XTGenOperatorMaster.sampleID, null);
            if (sampleId != null) {
                e.sampleId =  sampleId.intValue();
            }

            Number keyNumber = getAmountOfOper(igen, XTGenOperatorMaster.keynum, null);
            if (keyNumber != null) {
                e.keyNumber =  keyNumber.intValue();
            }

            Number overridingRootKey = getAmountOfOper(igen, XTGenOperatorMaster.overridingRootKey, null);
            if (overridingRootKey != null) {
                e.overridingRootKey =  overridingRootKey.intValue();
            }

            Number keyRange = getAmountOfOper(igen, XTGenOperatorMaster.keyRange, null);
            if (keyRange != null) {
                int v = keyRange.intValue();
                e.keyRange1 = v & 0xff;
                e.keyRange2 = (v >> 8) & 0xff;
            }

            Number velRange = getAmountOfOper(igen, XTGenOperatorMaster.velRange, null);
            if (velRange != null) {
                int v = velRange.intValue();
                
                e.velRange1 = v & 0xff;
                e.velRange2 = (v >> 8) & 0xff;
            }

            if (e.sampleId >= 0) {
                _data.add(e);
            }
        }
    }

    public boolean getLoopOf(int y) {
        return _data.get(y)._loop;
    }
    
    public int getSampleIdOf(int y) {
        return _data.get(y).sampleId;
    }
    
    public int getOverridingRootKeyOf(int y) {
        return _data.get(y).overridingRootKey;
    }

    @Override
    public int getSize() {
        return _data.size();
    }

    @Override
    public String getElementAt(int index) {
        Element e = _data.get(index);
        String sample = "smpl:" +  e.sampleId  +", ";
        String keyNumber = "";
        String keyRange = "";
        String velRange = "";
        if (e.keyNumber >= 0) {
            keyRange = " key number " + e.keyNumber + ", ";
        }
        if (e.overridingRootKey >= 0) {
            keyRange = " overridingRootKey " + e.overridingRootKey + ", ";
        }
        if (e.keyRange1 >= 0) {
            keyRange = " key from " + e.keyRange1 + " to " + e.keyRange2 + ", ";
        }
        if (e.velRange1 >= 0) {
            velRange = " vel from " + e.velRange1 + " to " + e.velRange2 + ", ";
        }
        return sample + keyRange + velRange;
    }
}
