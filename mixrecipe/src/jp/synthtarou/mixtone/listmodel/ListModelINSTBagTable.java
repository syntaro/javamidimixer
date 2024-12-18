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
import java.util.TreeMap;
import javax.swing.AbstractListModel;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;
import jp.synthtarou.mixtone.synth.soundfont.table.XTTable;
import jp.synthtarou.mixtone.synth.soundfont.SFZElement;
import jp.synthtarou.mixtone.synth.soundfont.XTGenOperatorMaster;
import jp.synthtarou.mixtone.synth.soundfont.XTGenOperator;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ListModelINSTBagTable extends AbstractListModel<String>{
    XTTable _instBag;
    
    class Element {
        TreeMap<Integer, XTRow> _igenParams = null;
        TreeMap<Integer, XTRow> _imodParams = null;

        public Element(XTTable igen, XTTable imod) {
            _igenParams = new TreeMap<>();
            for (int i = 0; i < igen.size(); ++ i) {
                XTRow row = igen.get(i);
                Number title = row.get(SFZElement.IGEN_GENOPER).numberValue();
                Number value = row.get(SFZElement.IGEN_GENAMOUNT).numberValue();
                if (title != null && value != null) {
                    _igenParams.put(title.intValue(), row);
                }
            }
            _imodParams = new TreeMap<>();
            for (int i = 0; i < imod.size(); ++ i) {
                XTRow row = imod.get(i);
                Number title = row.get(SFZElement.IMOD_AMTSRCOPER).numberValue();
                Number value = row.get(SFZElement.IMOD_MODAMOUNT).numberValue();
                if (title != null && value != null) {
                    _imodParams.put(title.intValue(), row);
                }
            }
        }

        public Number valueOfIGEN(int oper, Number defValue) {
            XTRow row = _igenParams.get(oper);
            if (row != null) {
                Number num = row.intColumn(SFZElement.IGEN_GENAMOUNT);
                if (num == null) {
                    return defValue;
                }
                return num.intValue();
            }
            return defValue;
        }

        public Number valueOfIMOD(int oper, Number defValue) {
            XTRow row = _igenParams.get(oper);
            if (row != null) {
                Number num = row.intColumn(SFZElement.IMOD_MODAMOUNT);
                if (num == null) {
                    return defValue;
                }
                return num.intValue();
            }
            return defValue;
        }
    }
    ArrayList<Element> _data;
    
    public ListModelINSTBagTable(XTTable instBag) {
        _instBag = instBag;
        _data = new ArrayList<>();
        for (int i = 0; i < instBag.size(); ++ i) {
            XTRow row = instBag.get(i);
            XTTable igen = row.tableColumn(SFZElement.IBAG_IGENINDEX_TABLE);
            XTTable imod = row.tableColumn(SFZElement.IBAG_IMODINDEX_TABLE);

            _data.add(new Element(igen, imod));
        }
    }

    public boolean getLoopOf(int y) {
        Number loop = _data.get(y).valueOfIGEN(XTGenOperatorMaster.sampleModes, null);
        if (loop == null) {
            return false;
        }
        return loop.intValue() > 0;
    }
                    
    public int getSampleIdOf(int y) {
        Number sampleId = _data.get(y).valueOfIGEN(XTGenOperatorMaster.sampleID, null);
        if (sampleId == null) {
            return -1;
        }
        return sampleId.intValue();
    }
    
    public int getKeynumberOf(int y) {
        Number keynum = _data.get(y).valueOfIGEN(XTGenOperatorMaster.keynum, null);
        if (keynum == null) {
            return -1;
        }
        return keynum.intValue();
    }

    public int getOverrindingRootkey(int y) {
        Number overrideKey = _data.get(y).valueOfIGEN(XTGenOperatorMaster.overridingRootKey,null);
        if (overrideKey == null) {
            return -1;
        }
        return overrideKey.intValue();
    }
    
    public int getKeyRangeOf(int y) {
        Number keyRange = _data.get(y).valueOfIGEN(XTGenOperatorMaster.keyRange,null);
        if (keyRange == null) {
            return -1;
        }
        return keyRange.intValue();
        // lo = v & 0xff
        // hi = (v >> 7) & 0xff
    }

    public int getVelocityRangeOf(int y) {
        Number velRange = _data.get(y).valueOfIGEN(XTGenOperatorMaster.velRange,null);
        if (velRange == null) {
            return -1;
        }
        return velRange.intValue();
        // lo = v & 0xff
        // hi = (v >> 7) & 0xff
    }

    @Override
    public int getSize() {
        return _data.size();
    }
    
    int lowerValue(int x) {
        int n1 = (x >> 8) & 0xff;
        int n2 = (x >> 8) & 0xff;
        if (n1 < n2) return n1;
        return n2;
    }

    int higherValue(int x) {
        int n1 = (x >> 8) & 0xff;
        int n2 = (x >> 8) & 0xff;
        if (n1 > n2) return n1;
        return n2;
    }

    @Override
    public String getElementAt(int index) {
        Element e = _data.get(index);

        TreeMap<Integer, XTRow> igen = e._igenParams;
        TreeMap<Integer, XTRow> imod = e._imodParams;
        
        ArrayList<XTRow> genLines = new ArrayList<>();
        
        for (XTRow genRow : igen.values()) {
            int x = -1;
            switch(genRow.intColumn(SFZElement.IGEN_GENAMOUNT)) {
                case XTGenOperatorMaster.sampleID:
                    x = 0;
                    break;
                case XTGenOperatorMaster.keynum:
                    x = 1;
                    break;
                case XTGenOperatorMaster.keyRange:
                    x = 2;
                    break;
                case XTGenOperatorMaster.velRange:
                    x = 3;
                    break;
                default:
                    x = -1;
                    break;
            }
            if (x >= 0) {
                while (genLines.size() <= x) {
                    genLines.add(null);
                }
                genLines.set(x, genRow);
            }
            else  {
                genLines.add(genRow);
            }
        }

        StringBuilder str = new StringBuilder();
        str.append("{GEN:");
        boolean first = true;
        XTGenOperatorMaster master = new XTGenOperatorMaster();
        for (XTRow row : genLines) {
            if (first) {
                first = false;
            }
            else {
                str.append(",");
            }
            int oper = row.intColumn(SFZElement.IGEN_GENOPER);
            XTGenOperator operObj = master.get(oper);
            if (operObj != null) {
                String name = operObj.getName();
                Number amount = row.numberColumn(SFZElement.IGEN_GENAMOUNT, null);
                Number amountMean = row.numberColumn(SFZElement.IGEN_GENMAMOUNT_MEAN, null);
                if (oper == XTGenOperatorMaster.keyRange) {
                    str.append(name + "=" + String.valueOf(amount) + "[" + lowerValue(amount.intValue()) + "-" + higherValue(amount.intValue()));
                }
                else if (oper == XTGenOperatorMaster.velRange) {
                    str.append(name + "=" + String.valueOf(amount) + "[" + lowerValue(amount.intValue()) + "-" + higherValue(amount.intValue()));
                }
                else {
                    str.append(name + "=" + String.valueOf(amount) + "[" + String.valueOf(amountMean) +"]");
                }
            }
        }
        str.append("}");
        return str.toString();
    }
}
