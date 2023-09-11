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
import jp.synthtarou.midimixer.libs.midi.MXTemplateCache;
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
    MXTemplate _template = MXMessageFactory.createDummy().getTemplate();

    RangedValue _value = RangedValue.ZERO7;
    RangedValue _gate = RangedValue.ZERO7;
    int _channel = 0;

    int _dataroomType = MXVisitant.ROOMTYPE_NODATA;
    int _dataroomMSB = -1;
    int _dataroomLSB = -1;

    boolean _ccPair14 = false;

    MGStatusForDrum _drum = null;

    public MGStatus(int port, int uiType, int row, int column) {
        initOpts();
        _port = port;
        _uiType = uiType;
        _row = row;
        _column = column;

        if (uiType == TYPE_DRUMPAD || uiType == TYPE_DRUMPAD_OUTSIGNAL) {
            _drum = new MGStatusForDrum(this);
        }
    }

    public MXTemplate getTemplate() {
        return _template;
    }

    public MXMessage toMXMessage(MXTemplate template, MXTiming timing) {
        if (template == null) {
            return null;
        }
        MXMessage message = template.buildMessage(_port, _channel, _gate, _value);
        message._timing = timing;
        message.setValuePairCC14(_ccPair14);

        if (template.get(0) == MXTemplate.DTEXT_RPN
                || template.get(0) == MXTemplate.DTEXT_NRPN) {
            MXVisitant visit = new MXVisitant();
            visit.setDataroomType(_dataroomType);
            visit.setDataroomMSB(_dataroomMSB);
            visit.setDataroomLSB(_dataroomLSB);
            visit.setDataentry14(_value._var);
            message.setVisitant(visit);
        }
        return message;
    }

    public MXMessage toMXMessage(MXTiming timing) {
        return toMXMessage(getTemplate(), timing);
    }

    public RangedValue updateValue(int value) {
        _value = _value.updateValue(value);
        return _value;
    }

    public Object clone() {
        MGStatus status = new MGStatus(_port, _uiType, _row, _column);

        status._template = _template;
        status._channel = _channel;
        status._gate = _gate;
        status._value = _value;

        status._name = _name;
        status._memo = _memo;

        status._value = _value;

        status._dataroomType = _dataroomType;
        status._dataroomMSB = _dataroomMSB;
        status._dataroomLSB = _dataroomLSB;

        status._ccPair14 = _ccPair14;

        return status;
    }

    public void setTemplateAsText(String text, int channel) {
        _channel = channel;
        _template = MXMessageFactory.fromDtext(text, _channel);
    }

    public void refillGate() {
        System.out.println("refillGate()- 1");
        switch (_template.get(0)) {
            case MXTemplate.DTEXT_9CH: // noteon
            case MXTemplate.DTEXT_8CH: // noteoff
            case MXTemplate.DTEXT_BCH: // controlchange
            case MXTemplate.DTEXT_ACH: // polyPressure
            case MXTemplate.DTEXT_CCH: // progrramChange
            case MXMidi.COMMAND_NOTEON:
            case MXMidi.COMMAND_NOTEOFF:
            case MXMidi.COMMAND_CONTROLCHANGE:
            case MXMidi.COMMAND_POLYPRESSURE:
            case MXMidi.COMMAND_PROGRAMCHANGE:
                System.out.println("refillGate()- 2");
                if ((_template.get(1) & 0xff00) == 0) {
                    int[] newTemplate = new int[]{
                        _template.get(0),
                        MXTemplate.DTEXT_GL,
                        _template.get(2)
                    };
                    _gate = RangedValue.new7bit(_template.get(1));
                    _template = new MXTemplate(newTemplate);
                    System.out.println("refillGate()- 4");
                    return;
                }
                break;
        }
        System.out.println("refillGate()- 3");
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
                name = message.toShortString();
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
        if (haveSameStatusAndGate(message)) {
            MXVisitant visit = message.getVisitant();
            if (message.isDataentry()) {
                int original = _value._var;
                int newVar = visit.getDataentryValue14();
                switch (message.getGate()._var) {
                    case MXMidi.DATA1_CC_DATAENTRY:
                        if (newVar >= _value._min && newVar <= _value._max) {
                        } else {
                            return false;
                        }
                        break;
                    case MXMidi.DATA1_CC_DATAINC:
                        newVar = original + 1;
                        if (newVar >= _value._min && newVar <= _value._max) {
                        } else {
                            return false;
                        }
                        break;
                    case MXMidi.DATA1_CC_DATADEC:
                        newVar = original - 1;
                        if (newVar >= _value._min && newVar <= _value._max) {
                        } else {
                            return false;
                        }
                        break;
                }
                if (newVar >= _value._min && newVar <= _value._max) {
                    setValue(_value.updateValue(newVar));
                    return true;
                }
                return false;
            }

            int value = message.getValue()._var;

            if (message.isCommand(MXMidi.COMMAND_NOTEOFF)) {
                value = 0;
            }
            if (value >= _value._min && value <= _value._max) {
                if (_drum != null) {
                    _drum.messageDetected(message);
                    return true;
                } else {
                    setValue(_value.updateValue(value));
                    return true;
                }
            }
            return false;
        } else if (isOnlyValueDifferent(message)) { //long message
            int value = message.getValue()._var;
            if (value >= _value._min && value <= _value._max) {
                setValue(_value.updateValue(value));
                return true;
            }
            return false;
        }

        return false;
    }

    public void setValue(RangedValue value) {
        _value = value;
    }

    public boolean hasCustomRange() {
        int wishMax = getTemplate().getBytePosHiValue() >= 0 ? (128 * 128 - 1) : 127;

        if (_ccPair14) {
            wishMax = 128 * 128 - 1;
        }

        if (_value._min == 0 && _value._max == wishMax) {
            return false;
        } else {
            return true;
        }
    }

    public void resetCustomRange() {
        if (getTemplate().getBytePosHiValue() >= 0) {
            _value = _value.modifyRangeTo(0, 128 * 128 - 1);
        } else if (_ccPair14) {
            _value = _value.modifyRangeTo(0, 128 * 128 - 1);
        } else {
            _value = _value.modifyRangeTo(0, 128 - 1);
        }
    }

    public void setCustomRange(int min, int max) {
        _value = _value.modifyRangeTo(min, max);
    }

    public boolean haveSameStatusAndGate(MXMessage message) {
        MXMessage from = toMXMessage(null);

        if (from.isDataentry() || message.isDataentry()) {
            if (from.isDataentry() == message.isDataentry()) {
                //不完全な状態
                if (from.getVisitant().getBankLSB() == message.getVisitant().getBankLSB()) {
                    if (from.getVisitant().getBankMSB() == message.getVisitant().getBankMSB()) {
                        if (from.getVisitant().getDataroomType() == message.getVisitant().getDataroomType()) {
                            if (_channel == message.getChannel()) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        }
        if (from.isCommand(MXMidi.COMMAND_NOTEON) || from.isCommand(MXMidi.COMMAND_NOTEOFF)) {
            if (message.isCommand(MXMidi.COMMAND_NOTEON) || message.isCommand(MXMidi.COMMAND_NOTEOFF)) {
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

            if (_channel != channel) {
                return false;
            }
            if (from.isCommand(command)) {
                if (command == MXMidi.COMMAND_CONTROLCHANGE) {
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
    }

    public boolean isOnlyValueDifferent(MXMessage message) {
        if (_channel != message.getChannel()) {
            return false;
        }

        if (_gate._var != message.getGate()._var) {
            return false;
        }

        MXTemplate temp1 = getTemplate();
        MXTemplate temp2 = message.getTemplate();

        if (temp1 != temp2) {
            if (temp1.size() != temp2.size()) {
                return false;
            }
            for (int i = 0; i < temp1.size(); ++i) {
                int t1 = temp1.get(i);
                int t2 = temp2.get(i);

                if (t1 == t2) {
                    continue;
                }

                if (t1 == MXTemplate.DTEXT_VH || t1 == MXTemplate.DTEXT_VL
                        || t2 == MXTemplate.DTEXT_VH || t2 == MXTemplate.DTEXT_VL) {
                    continue;
                }

                return false;
            }
        }

        return true;
    }

    public String toTemplateText() {
        return _template.buildMessage(_port, _channel, _gate, _value).toTemplateText();
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
        initOpts();
        setTemplateAsText("@CC #GL #VL", 0);
        _channel = 0;
        _value = RangedValue.new7bit(127);
        _gate = RangedValue.new7bit(0);
    }

    public void fillDataentry(int dataroomType) {
        initOpts();
        String msb = MXUtil.toHexFF(_dataroomMSB) + "h";
        String lsb = MXUtil.toHexFF(_dataroomLSB) + "h";
        _dataroomType = dataroomType;
        if (dataroomType == MXVisitant.ROOMTYPE_RPN) {
            setTemplateAsText("@RPN " + msb + " " + lsb + " #VH #VL", 0);
        } else if (dataroomType == MXVisitant.ROOMTYPE_NRPN) {
            setTemplateAsText("@NRPN " + msb + " " + lsb + " #VH #VL", 0);
        } else {
            new IllegalStateException().printStackTrace();
        }
    }

    public void fillTogglePedal() {
        initOpts();
        fillNote(12 * 4);
        _drum._type = MGStatusForDrum.TYPE_CUSTOM_CC;
        _drum._modeToggle = true;
        _drum._currentSwitch = false;
        _drum._strikeZone = new RangedValue(127, 1, 127);
        _drum._customTemplate = MXMessageFactory.fromDtext("@CC #GL #VL", 0);
        _drum._customTemplateGate = MXMidi.DATA1_CC_DAMPERPEDAL;
        _drum._customOutOnValue = 127;
        _drum._customOutOffValue = 0;

    }

    public void fillNote(int note) {
        initOpts();
        setTemplateAsText("90h #GL #VL", 0);
        _gate = RangedValue.new7bit(note);
        _channel = 0;
        _name = "note";
    }

    public void initOpts() {
        setTemplateAsText("", 0);
        _name = "";
        _memo = "";

        _value = RangedValue.ZERO7;
        _gate = RangedValue.ZERO7;

        _dataroomType = MXVisitant.ROOMTYPE_NODATA;
        _dataroomMSB = -1;
        _dataroomLSB = -1;
        _ccPair14 = false;
        _channel = 0;

        if (_uiType == TYPE_DRUMPAD || _uiType == TYPE_DRUMPAD_OUTSIGNAL) {
            _drum = new MGStatusForDrum(this);
        }
    }
}
