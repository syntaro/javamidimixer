/*
 * Copyright 2023 Syntarou YOSHIDA.
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
package jp.synthtarou.midimixer.mx30controller;

import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGStatus implements Cloneable, Comparable<MGStatus> {
    public static final int TYPE_CIRCLE = 1;
    public static final int TYPE_SLIDER = 2;
    public static final int TYPE_DRUMPAD = 3;
    public static final int TYPE_DRUMPAD_OUTSIGNAL = 4;

    public int _port;
    public int _uiType;
    public final int _row;
    public final int _column;

    String _name = "";
    String _memo = "";
    MXMessage _base = MXMessageFactory.fromTemplate(0, null, 0, RangedValue.ZERO7, RangedValue.ZERO7);
/*
    int _dataroomType = MXVisitant.ROOMTYPE_NODATA;
    int _dataroomMSB = -1;
    int _dataroomLSB = -1;
*/
    boolean _ccPair14 = false;

    MGStatusForDrum _drum = null;

    public MGStatus(int port, int uiType, int row, int column) {
        clearAll();
        _port = port;
        _uiType = uiType;
        _row = row;
        _column = column;

        if (uiType == TYPE_DRUMPAD || uiType == TYPE_DRUMPAD_OUTSIGNAL) {
            _drum = new MGStatusForDrum(this);
        }
    }

    public MXMessage getBaseMessage() {
        return _base;
    }

    public MXMessage toMXMessage(MXTiming timing) {
        if (_base == null) {
            return null;
        }
        MXMessage message = (MXMessage)_base.clone();
        message._timing = timing;
        return message;
    }

    public RangedValue updateValue(int value) {
        _base.setValue(_base.getValue().updateValue(value));
        return _base.getValue();
    }

    public Object clone() {
        MGStatus status = new MGStatus(_port, _uiType, _row, _column);

        status._base = (MXMessage)_base.clone();

        status._name = _name;
        status._memo = _memo;

        status._ccPair14 = _ccPair14;

        return status;
    }

    public void setBaseMessage(MXMessage base) {
        if (base == null) {
            _base = null;
        }
        else {
           _base = (MXMessage)base.clone();
        }
    }

    public String toString() {
        MXMessage message = toMXMessage(null);
        String text;
        switch (_uiType) {
            case TYPE_CIRCLE:
                text = "Circle";
                break;
            case TYPE_SLIDER:
                text = "Slider";
                break;
            case TYPE_DRUMPAD:
                text = "Pad";
                break;
            default:
                message = null;
                text = "Error";
                break;
        }
        String name;
        if (_name == null || _name.length() == 0) {
            if (message == null) {
                name = "null";
            } else {
                name = message.toStringForUI();
            }
        } else {
            name = _name;
        }
        if (message == null) {
            message = MXMessageFactory.createDummy();
        }
        return name + " (" + _memo + ")" + "\n"
                + text + "[row " + (_row + 1) + ", col " + (_column + 1) + "] "
                + "\n" + message.toString() + (message.isValuePairCC14() ? " (=14bit)" : "");
    }

    public boolean controlByMessage(MXMessage message) {
        if (_base.hasSameTemplate(message)) {
            RangedValue value = _base.getValue();
            MXVisitant visit = message.getVisitant();

            if (message.isUnknwonDataentry()) {
                int original = value._var;
                int newVar = visit.getDataentryValue14();
                switch (message.getGate()._var) {
                    case MXMidi.DATA1_CC_DATAENTRY:
                        if (newVar >= value._min && newVar <= value._max) {
                        } else {
                            return false;
                        }
                        break;
                    case MXMidi.DATA1_CC_DATAINC:
                        newVar = original + 1;
                        if (newVar >= value._min && newVar <= value._max) {
                        } else {
                            return false;
                        }
                        break;
                    case MXMidi.DATA1_CC_DATADEC:
                        newVar = original - 1;
                        if (newVar >= value._min && newVar <= value._max) {
                        } else {
                            return false;
                        }
                        break;
                }
                if (newVar >= value._min && newVar <= value._max) {
                    setValue(value.updateValue(newVar));
                    return true;
                }
                return false;
            }

            int newValue = message.getValue()._var;

            if (message.isCommand(MXMidi.COMMAND_CH_NOTEOFF)) {
                newValue = 0;
            }
            if (newValue >= value._min && newValue <= value._max) {
                if (_drum != null) {
                    _drum.messageDetected(message);
                    return true;
                } else {
                    setValue(value.updateValue(newValue));
                    return true;
                }
            }
            return false;
        } else if (isOnlyValueDifferent(message)) { //long message
            RangedValue value = _base.getValue();
            int newValue = message.getValue()._var;
            if (newValue >= value._min && newValue <= value._max) {
                setValue(value.updateValue(newValue));
                return true;
            }
            return false;
        }

        return false;
    }

    public void setValue(RangedValue value) {
        _base.setValue(value);
    }

    public boolean hasCustomRange() {
        int wishMax = _base.hasValueHiField() ? (128 * 128 - 1) : 127;

        if (_ccPair14) {
            wishMax = 128 * 128 - 1;
        }

        if (_base.getValue()._min == 0
        && _base.getValue()._max == wishMax) {
            return false;
        } else {
            return true;
        }
    }

    public void resetCustomRange() {
        if (hasCustomRange()) {
            
        }
        if (getBaseMessage().hasValueHiField()) {
            _base.setValue(_base.getValue().modifyRangeTo(0, 128 * 128 - 1));
        } else if (_ccPair14) {
            _base.setValue(_base.getValue().modifyRangeTo(0, 128 * 128 - 1));
        } else {
            _base.setValue(_base.getValue().modifyRangeTo(0, 128 - 1));
        }
    }

    public void setCustomRange(int min, int max) {
        _base.setValue(_base.getValue().modifyRangeTo(min, max));
    }
    /*

    public boolean haveSameStatusAndGate(MXMessage message) {
        MXMessage from = toMXMessage(null);
        
        if (from.isDataentry() || message.isDataentry()) {
            if (from.isDataentry() == message.isDataentry()) {
                //不完全な状態
                if (from.getVisitant().getBankLSB() == message.getVisitant().getBankLSB()) {
                    if (from.getVisitant().getBankMSB() == message.getVisitant().getBankMSB()) {
                        if (from.getVisitant().getDataroomType() == message.getVisitant().getDataroomType()) {
                            if (_base.getChannel() == message.getChannel()) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        }
        if (from.isCommand(MXMidi.COMMAND_CH_NOTEON) || from.isCommand(MXMidi.COMMAND_CH_NOTEOFF)) {
            if (message.isCommand(MXMidi.COMMAND_CH_NOTEON) || message.isCommand(MXMidi.COMMAND_CH_NOTEOFF)) {
                if (from.getGate() != message.getGate()) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }
        if (message.isMessageTypeChannel()) {
            int command = message.getStatus() & 0xf0;
            int channel = message.getChannel();

            if (_base.getChannel() != channel) {
                return false;
            }
            if (from.isCommand(command)) {
                if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                    int cc1 = from.getGate()._var;
                    int cc2 = message.getGate()._var;
                    if (cc1 != cc2) {
                        if (from.isValuePairCC14() && message.isValuePairCC14()) {
                            if (cc1 >= 0 && cc1 <= 31 && cc1 + 32 == cc2) {
                                return true;
                            }
                            if (cc2 >= 0 && cc2 <= 31 && cc2 + 32 == cc1) {
                                return true;
                            }
                        }
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }*/

    public boolean isOnlyValueDifferent(MXMessage message) {
        if (_base.getChannel() != message.getChannel()) {
            return false;
        }

        if (_base.getGate()._var != message.getGate()._var) {
            return false;
        }
        
        if (_base.hasSameTemplate(message)) {
            return true;
        }
        
        return false;
    }

    public int compareTo(MGStatus another) {
        if (this == another) {
            return 0;
        }
        int x;

        x = _port - another._port;
        if (x == 0) {
            x = _uiType - another._uiType;
        }
        if (x == 0) {
            x = _row - another._row;
        }
        if (x == 0) {
            x = _column - another._column;
        }
        return x;
    }

    public void fillControlChange() {
        clearAll();
        MXMessage message = MXMessageFactory.fromCCXMLText(_port, "@CC #GL #VL", 0);
        setBaseMessage(message);
    }

    public void fillDataentry(boolean isRPN, int msb, int lsb, RangedValue value) {
        clearAll();
        String type = isRPN ? "@RPN" : "@NRPN";
        String msb2 = MXUtil.toHexFF(msb) + "h";
        String lsb2 = MXUtil.toHexFF(lsb) + "h";
 
        RangedValue gate2 = new RangedValue(0, 0, 127);
        
        if (value._max <= 127) {
            setBaseMessage(MXMessageFactory.fromCCXMLText(_port, type + " " + msb2 + " " + lsb2 + " #VL 0", 0, gate2, value));
        }
        else {
            setBaseMessage(MXMessageFactory.fromCCXMLText(_port, type + " " + msb2 + " " + lsb2 + " #VH #VL", 0, gate2, value));
        }
    }

    public void fillTogglePedal() {
        clearAll();
        fillNote(12 * 4);
        _drum._type = MGStatusForDrum.TYPE_CUSTOM_CC;
        _drum._modeToggle = true;
        _drum._currentSwitch = false;
        _drum._strikeZone = new RangedValue(127, 1, 127);
        _drum._customTemplate = MXMessageFactory.fromCCXMLText(_port, "@CC #GL #VL", 0);
        _drum._customOutOnValue = 127;
        _drum._customOutOffValue = 0;

    }

    public void fillNote(int note) {
        clearAll();
        setBaseMessage(MXMessageFactory.fromCCXMLText(_port, "90h #GL #VL", 0, new RangedValue(note, 0, 127), null));
        _name = "note";
    }

    public void clearAll() {
        setBaseMessage(MXMessageFactory.fromCCXMLText(_port,null, 0));
        _name = "";
        _memo = "";

        _ccPair14 = false;

        if (_uiType == TYPE_DRUMPAD || _uiType == TYPE_DRUMPAD_OUTSIGNAL) {
            _drum = new MGStatusForDrum(this);
        }
    }
}
