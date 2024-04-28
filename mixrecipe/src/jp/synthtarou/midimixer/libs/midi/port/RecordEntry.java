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
package jp.synthtarou.midimixer.libs.midi.port;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Comparator;
import javax.swing.text.NumberFormatter;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class RecordEntry {

    static final Comparator<MXMIDIIn> COMP_MIDI = new Comparator<MXMIDIIn>() {
        @Override
        public int compare(MXMIDIIn o1, MXMIDIIn o2) {
            if (o1 == o2) {
                return 0;
            }
            int x1 = o1._name.compareTo(o2._name);
            if (x1 != 0) {
                return x1;
            }
            int a1 = o1._driver.hashCode();
            int a2 = o2._driver.hashCode();
            if (a1 != a2) {
                if (a1 < a2) {
                    return -1;
                } else {
                    return 1;
                }
            }
            a1 = o1._orderInDriver;
            a2 = o2._orderInDriver;
            if (a1 != a2) {
                if (a1 < a2) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return 0;
        }
    ;
    };

    static final Comparator<RecordEntry> COMP_ENTRY = new Comparator<RecordEntry>() {
        @Override
        public int compare(RecordEntry o1, RecordEntry o2) {
            int x = 0;//COMP_MIDI.compare(o1._in, o2._in);
            if (x == 0) {
                if (o1._cc != o2._cc) {
                    return (o1._cc < o2._cc) ? -1 : 1;
                }
            }
            return x;
        }
    };

    public RecordEntry(MXMIDIIn in, int cc) {
        _in = in;
        _cc = cc;
        switch (cc) {
            case MXMidi.DATA1_CC_BANKSELECT:
                _choiceBit = 7;
                break;
            case MXMidi.DATA1_CC_DATAENTRY:
                _choiceBit = 14;
                break;
        }
    }

    MXMIDIIn _in;
    int _cc;

    public int _count0h = 0;
    public int _count20h = 0;
    public int _countPair = 0;
    public int _choiceBit = 0;
    
    public int _pooling0 = -1;
    public int _pooling32 = -1;

    void recalc() {
        MXPreprocessPanel.getInstance().recalcDone(this);
    }

    NumberFormatter percentFomatter = new NumberFormatter(NumberFormat.getPercentInstance());
    
    public String toTableString(int column) {
        if (column == PreProcessTableModel.COL_NAME) {
            return toString();
        }
        if (column == PreProcessTableModel.COL_SWITCH) {
            if (_choiceBit != 0) {
                return _choiceBit == 14 ? "14" : "7";
            }
            return "-";
        }
        double total = _count0h + _count20h + _countPair;
        try {
            if (column == PreProcessTableModel.COL_00H) {
                double percent = _count0h / total;
                return percentFomatter.valueToString(percent);
            }
            if (column == PreProcessTableModel.COL_20H) {
                double percent = _count20h / total;
                return percentFomatter.valueToString(percent);
            }
            if (column == PreProcessTableModel.COL_PAIR) {
                double percent = _countPair / total;
                return percentFomatter.valueToString(percent);
            }
            if (column == PreProcessTableModel.COL_SWITCH) {
                return _choiceBit >0 ? Integer.toString(_choiceBit) : "-";
            }
        }catch(ParseException ex) {

        }
        return "-";
    }

    public String toString() {
        String ccName = MXMidi.nameOfControlChange(_cc);
        String ccCode = _cc + ":" + ccName;
        //String device = ccCode + "@" + _in.getName();
        return ccCode;
    }

    public boolean is14bitChoiced() {
        if (_choiceBit != 0) {
            return (_choiceBit == 14);
        }
        return false;
    }
}
