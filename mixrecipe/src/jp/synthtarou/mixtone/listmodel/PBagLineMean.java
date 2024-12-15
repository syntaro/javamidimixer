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
import jp.synthtarou.mixtone.synth.soundfont.XTGenOperatorMaster;
import jp.synthtarou.mixtone.synth.soundfont.table.XTHeader;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;
import jp.synthtarou.mixtone.synth.soundfont.table.XTTable;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class PBagLineMean {
    final Number getAmountOfOper(XTTable pgen, int oper, Number defValue) {
        XTRow row = pgen.findRow(SFZElement.PGEN_GENOPER, oper);
        if (row != null) {
            Number num = row.intColumn(SFZElement.PGEN_GENAMOUNT);
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
            String oper = row.textColumn(SFZElement.PGEN_GENOPER_MEAN);
            String amount = row.textColumn(SFZElement.PGEN_GENAMOUNT_MEAN);
            result.add(oper +": " + amount);
        }
        return result;
    }
    HashMap<Integer, XTRow> _pgenMap;
    HashMap<Integer, XTRow> _pmodMap;
    
    public Integer calcLowerOfRange(Integer x) {
        if (x == null) {
            return null;
        }
        int a = x & 0xff;
        int b = (x >> 8) & 0xff;
        if (a < b) {
            return a;
        }
        return b;
    }

    public Integer calcHigherOfRange(Integer x) {
        if (x == null) {
            return null;
        }
        int a = x & 0xff;
        int b = (x >> 8) & 0xff;
        if (a > b) {
            return a;
        }
        return b;
    }
    
    public Number getAmount(int oper) {
        if (_pgenMap != null) {
            XTRow row1 = _pgenMap.get(oper);
            if (row1 != null) {
                Number num = row1.numberColumn(SFZElement.PGEN_GENAMOUNT, null);
                if (num != null) {
                    return num;
                }
            }
        }
        if (_pmodMap != null) {
            XTRow row2 = _pmodMap.get(oper);
            if (row2 != null) {
                Number num = row2.numberColumn(SFZElement.PMOD_MODAMOUNT, null);
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

    public Integer keyRangeLo() {
        return calcLowerOfRange(keyRange());
    }

    public Integer keyRangeHi() {
        return calcHigherOfRange(keyRange());
    }

    public Integer velRange() {
        Number num = getAmount(XTGenOperatorMaster.velRange);
        if (num == null) {
            return null;
        }
        return num.intValue();
    }

    public int velRangeLo() {
        return calcLowerOfRange(velRange());
    }

    public int velRangeHi() {
        return calcHigherOfRange(velRange());
    }
    
    public Integer instrument() {
        Number num = getAmount(XTGenOperatorMaster.instrument);
        if (num == null) {
            return null;
        }
        return num.intValue();
    }

    public PBagLineMean(XTRow row) {
        XTTable pgen = row.tableColumn(SFZElement.PBAG_PGENINDEX_TABLE);
        XTTable pmod = row.tableColumn(SFZElement.PBAG_PMODINDEX_TABLE);

        if (pgen != null) {
            _pgenMap = new HashMap<>();
            for (int y = 0; y < pgen.size(); ++ y) {
                XTRow genRow = pgen.get(y);
                Number oper = genRow.numberColumn(SFZElement.PGEN_GENOPER, null);
                if (oper != null) {
                    _pgenMap.put(oper.intValue(), genRow);
                }
            }
        }
        if (pmod != null) {
            _pmodMap = new HashMap<>();
            for (int y = 0; y < pmod.size(); ++ y) {
                XTRow modRow = pmod.get(y);
                Number oper = modRow.numberColumn(SFZElement.PMOD_AMTSRCOPER, null);
                if (oper != null) {
                    _pmodMap.put(oper.intValue(), modRow);
                }
            }
        }
    }
}
