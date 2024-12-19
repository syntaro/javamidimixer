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
import java.util.HashMap;
import jp.synthtarou.mixtone.synth.soundfont.SFZElement;
import jp.synthtarou.mixtone.synth.soundfont.XTGenOperator;
import jp.synthtarou.mixtone.synth.soundfont.XTGenOperatorMaster;
import jp.synthtarou.mixtone.synth.soundfont.table.XTHeader;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;
import jp.synthtarou.mixtone.synth.soundfont.table.XTTable;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class IBagLineMean {
    final Number getAmountOfOper(XTTable pgen, int oper, Number defValue) {
        XTRow row = pgen.findRow(SFZElement.IGEN_GENOPER, oper);
        if (row != null) {
            Number num = row.intColumn(SFZElement.IGEN_GENAMOUNT);
            if (num == null) {
                return defValue;
            }
            return num.intValue();
        }
        return defValue;
    }
    
    public final ArrayList<String> listAmountAsText(XTTable pgen) {
        ArrayList<String> result = new ArrayList<>();
        XTHeader header = pgen.getHeader();
        for (XTRow row : pgen) {
            String oper = row.textColumn(SFZElement.IGEN_GENOPER_MEAN);
            String amount = row.textColumn(SFZElement.IGEN_GENMAMOUNT_MEAN);
            result.add(oper +": " + amount);
        }
        return result;
    }

    HashMap<Integer, XTRow> _igenMap;
    HashMap<Integer, XTRow> _imodMap;
    
    public int calcLowerOfRange(int x) {
        int a = x & 0xff;
        int b = (x >> 8) & 0xff;
        if (a < b) {
            return a;
        }
        return b;
    }

    public int calcHigherOfRange(int x) {
        int a = x & 0xff;
        int b = (x >> 8) & 0xff;
        if (a > b) {
            return a;
        }
        return b;
    }

    public Number getAmount(int oper) {
        if (_igenMap != null) {
            XTRow row1 = _igenMap.get(oper);
            if (row1 != null) {
                Number num = row1.numberColumn(SFZElement.IGEN_GENAMOUNT, null);
                if (num != null) {
                    return num;
                }
            }
        }
        if (_imodMap != null) {
            XTRow row2 = _imodMap.get(oper);
            if (row2 != null) {
                Number num = row2.numberColumn(SFZElement.IMOD_MODAMOUNT, null);
                if (num != null) {
                    return num;
                }
            }
        }
        if (oper == XTGenOperatorMaster.keyRange
          || oper == XTGenOperatorMaster.velRange) {
            return 0x007f;
        }
        return null;
    }
    
    public Integer keyRange() {
        Number num = getAmount(XTGenOperatorMaster.keyRange);
        if (num == null) {
            return null;
        }
        return num.intValue();
    }

    public Double pan() {
        Number num = getAmount(XTGenOperatorMaster.pan);
        if (num == null) {
            return null;
        }
        return num.doubleValue();
    }
    
    public int keyRangeLo() {
        return calcLowerOfRange(keyRange());
    }

    public int keyRangeHi() {
        return calcHigherOfRange(keyRange());
    }

    public Integer velRange() {
        Number num = getAmount(XTGenOperatorMaster.velRange);
        if (num == null) {
            return null;
        }
        return num.intValue();
    }

    public Integer velRangeLo() {
        return calcLowerOfRange(velRange());
    }

    public Integer velRangeHi() {
        return calcHigherOfRange(velRange());
    }
    
    public Integer sampleID() {
        Number x = getAmount(XTGenOperatorMaster.sampleID);
        if (x != null) {
            return x.intValue();
        }
        return null;
    }
    
    public Integer sampleModes() {
        Number x = getAmount(XTGenOperatorMaster.sampleModes);
        if (x != null) {
            return x.intValue();
        }
        return null;
    }

    public Integer keyNum() {
        Number x = getAmount(XTGenOperatorMaster.keynum);
        if (x != null) {
            return x.intValue();
        }
        return null;
    }
    
    public Integer overridingRootKey() {
        Number x = getAmount(XTGenOperatorMaster.overridingRootKey);
        if (x != null) {
            return x.intValue();
        }
        return null;
    }

    public IBagLineMean(XTRow row) {
        XTTable pgen = row.tableColumn(SFZElement.IBAG_IGENINDEX_TABLE);
        XTTable pmod = row.tableColumn(SFZElement.IBAG_IMODINDEX_TABLE);

        if (pgen != null) {
            _igenMap = new HashMap<>();
            for (int y = 0; y < pgen.size(); ++ y) {
                XTRow genRow = pgen.get(y);
                Number oper = genRow.numberColumn(SFZElement.PGEN_GENOPER, null);
                if (oper != null) {
                    _igenMap.put(oper.intValue(), genRow);
                }
            }
        }
        if (pmod != null) {
            _imodMap = new HashMap<>();
            for (int y = 0; y < pmod.size(); ++ y) {
                XTRow modRow = pmod.get(y);
                Number oper = modRow.numberColumn(SFZElement.PMOD_SRCOPER, null);
                if (oper != null) {
                    _imodMap.put(oper.intValue(), modRow);
                }
            }
        }
    }
    
    XTRow _rawRow;
    
    public String tostring() {
        return _igenMap.toString();
    }
}