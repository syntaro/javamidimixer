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

import java.awt.Color;
import java.text.NumberFormat;
import javax.swing.text.NumberFormatter;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class RecordEntry implements Comparable<RecordEntry> {

    public RecordEntry(MXMIDIIn in, int cc) {
        _in = in;
        _cc = cc;
        switch (cc) {
            case MXMidi.DATA1_CC_DATAENTRY:
                _primaryBit = 14;
                break;
            case MXMidi.DATA1_CC_ALLNOTEOFF:
            case MXMidi.DATA1_CC_ALLSOUNDOFF:
                _primaryBit = 7;
                break;
        }
    }

    MXMIDIIn _in;
    int _cc;

    int _count0 = 0;
    int _count32 = 0;
    int _primaryBit = 0;
    int _lockedBit = 0;

    int _pooling0 = -1;
    int _pooling32 = -1;

    int _pastCalculate = 0;

    void recalc() {
        boolean firstTime = _lockedBit == 0 ? true : false;
        if (_pastCalculate == 0) {
            MXPreprocessPanel.getInstance().recalcDone(this, false);
            boolean f = actually14bit();
            _pastCalculate = f ? 14 : 7;
        } else {
            boolean notlock = actually14bitIfNotLock();
            boolean f = actually14bit();
            if (f != notlock) {
                MXPreprocessPanel.getInstance().recalcDone(this, notlock);
                _pastCalculate = notlock ? 14 : 7;
            }

        }
    }

    NumberFormatter percentFomatter = new NumberFormatter(NumberFormat.getPercentInstance());

    public Color getListBoxColor() {
        if (_lockedBit != 0) {
            return Color.red;
        } else if (_primaryBit != 0) {
            return Color.orange;
        } else if (_pooling0 == 0 && _pooling32 == 0) {
            return Color.yellow;
        } else {
            return Color.black;
        }
    }

    public String toString() {
        String ccName = MXMidi.nameOfControlChange(_cc);
        String ccCode = "" + ccName + "@" + _in.getName();
        if (_lockedBit != 0) {
            ccCode = ccName + " = lock " + _lockedBit;
        } else if (_primaryBit != 0) {
            ccCode = ccName + " = primal " + _primaryBit;
        } else if (_pooling0 == 0 && _pooling32 == 0) {
            ccCode = ccName + " have not seen";
        } else {
            int total = _pooling32 + _pooling0;
            double percent = _pooling32 * 1.0 / total;
            try {
                if (percent >= 0.6) {
                    ccCode = ccName + " " + percentFomatter.valueToString(percent) + " maybe 14bit";
                } else {
                    ccCode = ccName + " " + percentFomatter.valueToString(percent) + " maybe 7bit";
                }
            } catch (Throwable e) {
                if (percent >= 0.6) {
                    ccCode = ccName + " 60%+ maybe 14bit";
                } else {
                    ccCode = ccName + " undef 60% maybe 7bit";
                }
            }
        }
        return ccCode;
    }

    public boolean actually14bit() {
        if (_lockedBit != 0) {
            return (_lockedBit == 14);
        }
        if (_primaryBit != 0) {
            return (_primaryBit == 14);
        }
        if (_pooling0 == 0 && _pooling32 == 0) {
            return false;
        }
        if (_pooling32 + (_pooling0 / 3) >= _pooling0) {
            return true;
        }
        return false;
    }

    public boolean actually14bitIfNotLock() {
        if (_primaryBit != 0) {
            return (_primaryBit == 14);
        }
        if (_pooling0 == 0 && _pooling32 == 0) {
            return false;
        }
        if (_pooling32 + (_pooling0 / 3) >= _pooling0) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(RecordEntry o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
