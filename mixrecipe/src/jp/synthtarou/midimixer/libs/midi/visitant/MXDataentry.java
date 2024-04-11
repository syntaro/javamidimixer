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
package jp.synthtarou.midimixer.libs.midi.visitant;

import jp.synthtarou.libs.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXDataentry implements Cloneable, Comparable<MXDataentry>{
    MXVisitant _owner;
    
    public String toString() {
        String x1 = MXUtil.toHexFF(_dataroomMSB & 0xff);
        String x2 = MXUtil.toHexFF(_dataroomLSB & 0xff);
        String x3 = MXUtil.toHexFF(_dataentryMSB & 0xff);
        String x4 = MXUtil.toHexFF(_dataentryLSB & 0xff);
        return x1 + ":" + x2 + "=" + x3 + "," + x4;
    }
    
    public MXDataentry() {
        _owner = null;
        _isRPN = -1;
        _dataroomMSB = -1;
        _dataroomLSB = -1;
        _dataentryMSB = -1;
        _dataentryLSB = -1;
    }
    
    public MXDataentry(MXVisitant owner, int isRPN, int dataroomMSB, int dataroomLSB) {
        _owner = owner;
        _isRPN = isRPN;
        _dataroomMSB = dataroomMSB;
        _dataroomLSB = dataroomLSB;
        _dataentryMSB = -1;
        _dataentryLSB = -1;
    }
    
    public static final int TYPE_RPN = 1;
    public static final int TYPE_NRPN = 2;

    int _dataroomLSB; //98 NL 99 NM 100 RL 101 RN
    int _dataroomMSB;
    int _isRPN; // 1 = RPN / 2 = NRPN / -1 = unknown / another = error
    int _dataentryMSB; //CC6
    int _dataentryLSB; //CC38
    
    public Object clone() {
        MXDataentry e = new MXDataentry(_owner, _isRPN, _dataroomMSB, _dataroomLSB);
        return e;
    }
    
    @Override
    public int compareTo(MXDataentry e) {
        if (this == e) {
            return 0;
        }
        
        int x;
        x = _isRPN - e._isRPN;
        if (x != 0) return x;
        x = _dataroomLSB - e._dataroomLSB;
        if (x != 0) return x;
        x = _dataroomMSB - e._dataroomMSB;
        if (x != 0) return x;
        x = _dataentryLSB - e._dataentryLSB;
        if (x != 0) return x;
        x = _dataentryMSB - e._dataentryMSB;
        if (x != 0) return x;
        
        return 0;
    }

    public int isRPN() {
        return _isRPN;
    }

    public void setIsRPN(int isRPN) {
        _isRPN = isRPN;
    }
    
    public int getDataroomLSB() {
        return _dataroomLSB;
    }

    public int getDataroomMSB() {
        return _dataroomMSB;
    }
    
    public boolean havePartOfDataroom() {
        if (_isRPN == TYPE_RPN || _isRPN == TYPE_NRPN) {            
            if (_dataroomMSB >=0 && _dataroomLSB >= 0) {
                if (_dataroomMSB < 0 && _dataroomLSB < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getDataentryLSB() {
        return _dataentryLSB;
    }

    public int getDataentryMSB() {
        return _dataentryMSB;
    }

    public boolean havePartOfDataentry() {
        if (_dataentryMSB >=0 || _dataentryLSB >= 0) {
            if (_dataentryMSB < 0 || _dataentryLSB < 0) {
                return true;
            }
        }
        return false;
    }

    public int getDataentryValue14() {
        return ((_dataentryMSB & 0x7f) << 7) | (_dataentryLSB & 0x7f);
    }

    public void setDataentry14(int longValue) {
        if (_owner != null && _owner._isLocked) {
            throw new IllegalStateException("Immutable can't change");
        }

        int msb = longValue >> 7;
        int lsb = longValue & 0x7f;
        _dataentryMSB = msb;
        _dataentryLSB = lsb;

        if (_owner != null) {
            _owner._currentAge ++;
        }
    }

    public void setDataentryMSB(int msb) {
        if (_owner != null && _owner._isLocked) {
            throw new IllegalStateException("Immutable can't change");
        }
        _dataentryMSB = msb;
        if (_owner != null) {
            _owner._currentAge ++;
        }
    }

    public void setDataentryLSB(int lsb) {
        if (_owner != null && _owner._isLocked) {
            throw new IllegalStateException("Immutable can't change");
        }
        _dataentryLSB = lsb;
        if (_owner != null) {
            _owner._currentAge ++;
        }
    }

    public void setDataroomMSB(int msb) {
        if (_owner != null && _owner._isLocked) {
            throw new IllegalStateException("Immutable can't change");
        }
        _dataroomMSB = msb;
        if (_owner != null) {
            _owner._currentAge ++;
        }
    }

    public void setDataroomLSB(int lsb) {
        if (_owner != null && _owner._isLocked) {
            throw new IllegalStateException("Immutable can't change");
        }
        _dataroomLSB = lsb;
        if (_owner != null) {
            _owner._currentAge ++;
        }
    }
}
