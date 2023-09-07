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

import java.io.File;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;
import jp.synthtarou.midimixer.libs.midi.smf.SMFCallback;
import jp.synthtarou.midimixer.libs.midi.smf.SMFMessage;
import jp.synthtarou.midimixer.libs.midi.smf.SMFPlayer;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGStatus implements Cloneable, Comparable<MGStatus> {

    public int getSwitchOutPort() {
        return _switchSwitchOutPort;
    }

    public void setSwitchOutPort(int switchSwitchOutPort) {
        this._switchSwitchOutPort = switchSwitchOutPort;
    }

    public int getDataroomType() {
        return _dataroomType;
    }

    public void setDataroomType(int dataentryHave) {
        this._dataroomType = dataentryHave;
    }

    /**
     * @return the _dataentryMSB
     */
    public int getDataeroomMSB() {
        return _dataroomMSB;
    }

    /**
     * @param _dataentryMSB the _dataentryMSB to set
     */
    public void setDataroomMSB(int _dataentryMSB) {
        this._dataroomMSB = _dataentryMSB;
    }

    /**
     * @return the _dataentryLSB
     */
    public int getDataroomLSB() {
        return _dataroomLSB;
    }

    /**
     * @param _dataentryLSB the _dataentryLSB to set
     */
    public void setDataroomLSB(int _dataentryLSB) {
        this._dataroomLSB = _dataentryLSB;
    }

    public void setValuePairCC14(boolean flag) {
        _ccPair14 = flag;
    }

    public boolean isValuePairCC14() {
        return _ccPair14;
    }

    //public static final int SWITCH_ON_IF_ONESHOT = 0;
    public static final int SWITCH_ON_WHEN_MATCH = 1;
    public static final int SWITCH_ON_IF_PLUS1 = 2;
    public static final int SWITCH_ON_IF_OVER_HALF = 3;
    public static final int SWITCH_ON_IF_MAX = 4;

    public static final int SWITCH_OUT_ON_SAME_AS_INPUT = 5;
    public static final int SWITCH_OUT_ON_CUSTOM = 6;

    public static final int SWITCH_OUT_ON_VALUE_AS_INPUT_PLUS1 = 8;
    public static final int SWITCH_OUT_ON_VALUE_AS_INPUT = 9;
    public static final int SWITCH_OUT_ON_VALUE_FIXED = 10;

    public static final int SWITCH_OUT_OFF_NONE = 11;
    public static final int SWITCH_OUT_OFF_SAME_AS_INPUT = 12;
    public static final int SWITCH_OUT_OFF_SAME_AS_OUTPUT_ON = 13;
    public static final int SWITCH_OUT_OFF_CUSTOM = 14;

    public static final int SWITCH_OUT_OFF_VALUE_0 = 15;
    public static final int SWITCH_OUT_OFF_VALUE_SAME_AS_MIN = 16;
    public static final int SWITCH_OUT_OFF_VALUE_SAME_AS_INPUT = 17;
    public static final int SWITCH_OUT_OFF_VALUE_FIXED = 18;

    public static final int SWITCH_HARMONY_VELOCITY_SAME_AS_INPUT = 19;
    public static final int SWITCH_HARMONY_VELOCITY_FIXED = 20;

    public static final int SWITCH_TYPE_ON = 1;
    public static final int SWITCH_TYPE_ONOFF = 2;
    public static final int SWITCH_TYPE_HARMONY = 4;
    public static final int SWITCH_TYPE_SEQUENCE = 5;

    public static final int TYPE_CIRCLE = 1;
    public static final int TYPE_SLIDER = 2;
    public static final int TYPE_DRUMPAD = 3;
    public static final int TYPE_DRUMPAD_OUTSIGNAL = 4;

    public static final int SP_NONE = 0xff;
    public static final int SP_NOTE_ON = 0x00;
    public static final int SP_MESSAGE_VALUE = 0x10;
    public static final int SP_SYSTEM = 0x70;
    public static final int SP_PROG_UP = 0x80;
    public static final int SP_PROG_DOWN = 0x90;

    private int _port;
    private int _uiType;
    public final int _row;
    public final int _column;

    private String _name = "";
    private String _memo = "";
    private MXTemplate _template = MXMessageFactory.createDummy().getTemplate();

    private RangedValue _value = RangedValue.ZERO7;
    private int _valueHome = 0;

    private RangedValue _gate = RangedValue.ZERO7;
    private int _channel = 0;

    private int _switchLastDetected;

    private int _switchType = SWITCH_TYPE_ONOFF;
    private int _switchInputType = SWITCH_ON_IF_PLUS1;
    private int _switchOutOnType = SWITCH_OUT_ON_SAME_AS_INPUT;
    private int _switchOutOnTypeOfValue = SWITCH_OUT_ON_VALUE_AS_INPUT;
    private int _switchOutOnValueFixed = 127;
    private String _switchOutOnText = "";
    private int _switchOutOnTextGate = 0;

    MXTemplate _cacheOutOnMessage = null;

    private int _switchOutOffType = SWITCH_OUT_OFF_SAME_AS_INPUT;
    private int _switchOutOffTypeOfValue = SWITCH_OUT_OFF_VALUE_0;
    private int _switchOutOffValueFixed = 0;
    private String _switchOutOffText = "";
    private int _switchOutOffTextGate = 0;
    MXTemplate _cacheOutOffMessage = null;

    private boolean _switchWithToggle = false;
    private int _switchHarmonyVelocityType = SWITCH_HARMONY_VELOCITY_SAME_AS_INPUT;
    private int _switchHarmonyVelocityFixed = 100;
    private String _switchHarmonyNotes = "";

    private String _switchSequencerFile = "";
    private boolean _switchSequenceSeekStart = true;
    private boolean _switchSequencerToSingltTrack = true;
    private boolean _switchSequencerFilterNote = true;
    private SMFPlayer _switchSequencer = null;

    private int _switchSwitchOutPort = 0;
    private int _switchOutChannel = 0;

    private int _dataroomType = MXVisitant.ROOMTYPE_NODATA;
    private int _dataroomMSB = -1;
    private int _dataroomLSB = -1;

    private boolean _ccPair14 = false;
    
    public boolean _switchToSent = false;
    public boolean _switchLastDetect = false;
    public boolean _switchIncomming = false;
    public boolean _switchNeedAction = false;

    public MGStatus(int port, int uiType, int row, int column) {
        _port = port;
        _uiType = uiType;
        _row = row;
        _column = column;
    }

    public MXTemplate getTemplate() {
        return _template;
    }

    public synchronized MXMessage toMXMessage(MXTiming timing) {
        MXTemplate template = getTemplate();
        MXMessage message = template.buildMessage(_port, _channel, _gate, _value);
        message._timing = timing;
        message.setValuePairCC14(isValuePairCC14());

        if (template.get(0) == MXTemplate.DTEXT_RPN
                || template.get(0) == MXTemplate.DTEXT_NRPN) {
            MXVisitant visit = new MXVisitant();
            visit.setDataroomType(getDataroomType());
            visit.setDataroomMSB(getDataeroomMSB());
            visit.setDataroomLSB(getDataroomLSB());
            visit.setDataentry14(_value._var);
            message.setVisitant(visit);
        }
        return message;
    }
    
    public synchronized MXMessage toMXMessageCaseDrumOn(MXTiming traecNumber) {
        if (getSwitchOutOnType() == SWITCH_OUT_ON_SAME_AS_INPUT) {
            int value = getValueForSwitchOn();
            if (value < 0) {
                return null;
            }
            MXMessage message = toMXMessage(traecNumber);
            message.setValue(value);
            return message;
        }
        if (getSwitchOutOnType() == SWITCH_OUT_ON_CUSTOM) {
            if (_cacheOutOnMessage == null) {
                _cacheOutOnMessage = MXMessageFactory.fromDtext(getSwitchOutOnText(), 0);
                if (_cacheOutOnMessage == null) {
                    return null;
                }
            }

            int value = getValueForSwitchOn();
            if (value < 0) {
                return null;
            }
            int ch = getSwitchOutChannel();
            int gate = getSwitchOutOnTextGate();
            MXMessage message = _cacheOutOnMessage.buildMessage(_port, ch, RangedValue.new7bit(gate), _value.updateValue(value));
            return message;
        }
        //TODO
        System.out.println("Error getMXMessageDrumON:getSwitchOutOnType=" + getSwitchOutOnType());
        return null;
    }

    public synchronized MXMessage toMXMessageCaseDrumOff(MXTiming timing) {
        if (getSwitchOutOffType() == SWITCH_OUT_OFF_NONE) {
            return null;
        }
        if (getSwitchOutOffType() == SWITCH_OUT_OFF_SAME_AS_INPUT) {
            int value = getValueForSwitchOff();
            if (value < 0) {
                return null;
            }
            MXMessage message = toMXMessage(timing);
            message.setValue(value);
            return message;
        }
        if (getSwitchOutOffType() == SWITCH_OUT_OFF_SAME_AS_OUTPUT_ON) {
            int value = getValueForSwitchOff();
            if (value < 0) {
                return null;
            }
            MXMessage message = toMXMessageCaseDrumOn(timing);
            message.setValue(value);
            return message;
        }
        if (getSwitchOutOffType() == SWITCH_OUT_OFF_CUSTOM) {
            if (_cacheOutOffMessage == null) {
                _cacheOutOffMessage = MXMessageFactory.fromDtext(getSwitchOutOffText(), 0);
                if (_cacheOutOffMessage == null) {
                    return null;
                }
            }

            int value = getValueForSwitchOff();
            if (value < 0) {
                return null;
            }

            int ch = getSwitchOutChannel();
            int gate = getSwitchOutOffTextGate();
            MXMessage message = _cacheOutOffMessage.buildMessage(_port, ch, RangedValue.new7bit(gate), _value.updateValue(value));
            message._timing = timing;

            return message;
        }
        //TODO
        System.out.println("Error getMXMessageDrumOff:getSwitchOutOffType=" + getSwitchOutOffType());
        return null;
    }

    public Object clone() {
        MGStatus status = new MGStatus(getPort(), getUiType(), getRow(), getColumn());

        status._template = _template;
        status._channel = _channel;
        status._gate = _gate;
        status._value = _value;

        status.setName(getName());
        status.setMemo(getMemo());

        status.setValue(_value);
        status.setValueHome(_valueHome);
        status._switchToSent = _switchToSent;

        status.setSwitchType(getSwitchType());
        status.setSwitchInputType(getSwitchInputType());

        status.setSwitchOutPort(getSwitchOutPort());
        status.setSwitchOutChannel(getSwitchOutChannel());

        status.setSwitchOutOnType(getSwitchOutOnType());
        status.setSwitchOutOnTypeOfValue(getSwitchOutOnTypeOfValue());
        status.setSwitchOutOnValueFixed(getSwitchOutOnValueFixed());
        status.setSwitchOutOnText(getSwitchOutOnText());
        status.setSwitchOutOnTextGate(getSwitchOutOnTextGate());

        status.setSwitchOutOffType(getSwitchOutOffType());
        status.setSwitchOutOffTypeOfValue(getSwitchOutOffTypeOfValue());
        status.setSwitchOutOffValueFixed(getSwitchOutOffValueFixed());
        status.setSwitchOutOffText(getSwitchOutOffText());
        status.setSwitchOutOffTextGate(getSwitchOutOffTextGate());

        status.setSwitchWithToggle(isSwitchWithToggle());

        status.setSwitchHarmonyVelocityType(getSwitchHarmonyVelocityType());
        status.setSwitchHarmonyVelocityFixed(getSwitchHarmonyVelocityFixed());
        status.setSwitchHarmonyNotes(getSwitchHarmonyNotes());

        status.setSwitchSequencerFile(getSwitchSequencerFile());

        status.setDataroomType(getDataroomType());
        status.setDataroomMSB(getDataeroomMSB());
        status.setDataroomLSB(getDataroomLSB());

        status.setValuePairCC14(isValuePairCC14());

        return status;
    }

    public synchronized void setTemplateAsText(String text, int channel) {
        setTemplate(MXMessageFactory.fromDtext(text, _channel));
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

    public synchronized void setTemplate(MXTemplate template) {
        _template = template;
    }

    public synchronized String toString() {
        MXMessage message = toMXMessage(null);
        String text;
        switch (getUiType()) {
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
                + text + "[row " + (getRow() + 1) + ", col " + (getColumn() + 1) + "] "
                + "\n" + message.toString() + (message.isValuePairCC14() ? " (=14bit)" : "");
    }

    public boolean controlByMessage(MXMessage message) {
        MXMessage target = toMXMessage(message._timing);

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
                setValue(_value.updateValue(value));
                return true;
            }
            if (value >= _value._min && value <= _value._max) {
                setValue(_value.updateValue(value));
                return true;
            }
            return false;
        } else if (isOnlyValueDifferent(message)) {
            int value = message.getValue()._var;
            if (value >= _value._min && value <= _value._max) {
                setValue(_value.updateValue(value));
                return true;
            }
            return false;
        }

        return false;
    }

    public int getValueForSwitchOn() {
        switch (getSwitchOutOnType()) {
            case SWITCH_OUT_ON_SAME_AS_INPUT:
            case SWITCH_OUT_ON_CUSTOM:
                switch (getSwitchOutOnTypeOfValue()) {
                    case SWITCH_OUT_ON_VALUE_AS_INPUT:
                        return getSwitchLastDetected();

                    case SWITCH_OUT_ON_VALUE_AS_INPUT_PLUS1:
                        int x = getSwitchLastDetected();
                        if (x == _value._min && x < _value._max) {
                            x++;
                        }
                        return x;
                    case SWITCH_OUT_ON_VALUE_FIXED:
                        return getSwitchOutOnValueFixed();
                }
        }
        return 0;
    }

    public int getValueForSwitchOff() {
        switch (getSwitchOutOffType()) {
            case SWITCH_OUT_OFF_NONE:
                return -1;
            case SWITCH_OUT_OFF_SAME_AS_INPUT:
            case SWITCH_OUT_OFF_SAME_AS_OUTPUT_ON:
            case SWITCH_OUT_OFF_CUSTOM:
                switch (getSwitchOutOnTypeOfValue()) {
                    case SWITCH_OUT_OFF_VALUE_0:
                        return 0;

                    case SWITCH_OUT_OFF_VALUE_FIXED:
                        return getSwitchOutOffValueFixed();

                    case SWITCH_OUT_OFF_VALUE_SAME_AS_INPUT:
                        return getSwitchLastDetected();

                    case SWITCH_OUT_OFF_VALUE_SAME_AS_MIN:
                        return _value._min;

                }

        }
        return 0;
    }

    /**
     * @return the switchOutOffText
     */
    public String getSwitchOutOffText() {
        return _switchOutOffText;
    }

    /**
     * @param switchOutOffText the switchOutOffText to set
     */
    public void setSwitchOutOffText(String switchOutOffText) {
        _switchOutOffText = switchOutOffText;
        _cacheOutOffMessage = null;
    }

    /**
     * @return the switchOutOffTextGate
     */
    public int getSwitchOutOffTextGate() {
        return _switchOutOffTextGate;
    }

    /**
     * @param switchOutOffTextGate the switchOutOffTextGate to set
     */
    public void setSwitchOutOffTextGate(int switchOutOffTextGate) {
        _switchOutOffTextGate = switchOutOffTextGate;
    }

    /**
     * @return the switchOutOnText
     */
    public String getSwitchOutOnText() {
        return _switchOutOnText;
    }

    /**
     * @param switchOutOnText the switchOutOnText to set
     */
    public void setSwitchOutOnText(String switchOutOnText) {
        _switchOutOnText = switchOutOnText;
        _cacheOutOnMessage = null;
    }

    /**
     * @return the switchOutOnTextGate
     */
    public int getSwitchOutOnTextGate() {
        return _switchOutOnTextGate;
    }

    /**
     * @param switchOutOnTextGate the switchOutOnTextGate to set
     */
    public void setSwitchOutOnTextGate(int switchOutOnTextGate) {
        _switchOutOnTextGate = switchOutOnTextGate;
    }

    protected int getSwitchOutOffValueFixed() {
        return _switchOutOffValueFixed;
    }

    protected void setSwitchOutOffValueFixed(int switchOutOffValueFixed) {
        _switchOutOffValueFixed = switchOutOffValueFixed;
    }

    protected int getSwitchType() {
        return _switchType;
    }

    protected void setSwitchType(int switchType) {
        _switchType = switchType;
    }

    protected int getSwitchInputType() {
        return _switchInputType;
    }

    protected void setSwitchInputType(int switchInputType) {
        _switchInputType = switchInputType;
    }

    protected int getSwitchOutOnType() {
        return _switchOutOnType;
    }

    protected void setSwitchOutOnType(int switchOutOnType) {
        _switchOutOnType = switchOutOnType;
    }

    protected int getSwitchOutOnTypeOfValue() {
        return _switchOutOnTypeOfValue;
    }

    protected void setSwitchOutOnTypeOfValue(int switchOutOnValue) {
        _switchOutOnTypeOfValue = switchOutOnValue;
    }

    protected int getSwitchOutOnValueFixed() {
        return _switchOutOnValueFixed;
    }

    protected void setSwitchOutOnValueFixed(int switchOutOnValueFixed) {
        _switchOutOnValueFixed = switchOutOnValueFixed;
    }

    protected int getSwitchOutOffType() {
        return _switchOutOffType;
    }

    protected void setSwitchOutOffType(int switchOutOffType) {
        _switchOutOffType = switchOutOffType;
    }

    protected int getSwitchOutOffTypeOfValue() {
        return _switchOutOffTypeOfValue;
    }

    protected void setSwitchOutOffTypeOfValue(int switchOutOffValue) {
        _switchOutOffTypeOfValue = switchOutOffValue;
    }

    protected int getSwitchOutChannel() {
        return _switchOutChannel;
    }

    protected void setSwitchOutChannel(int switchHarmonyChannel) {
        _switchOutChannel = switchHarmonyChannel;
    }

    protected int getSwitchHarmonyVelocityType() {
        return _switchHarmonyVelocityType;
    }

    protected void setSwitchHarmonyVelocityType(int switchHarmonyVelocityType) {
        _switchHarmonyVelocityType = switchHarmonyVelocityType;
    }

    protected int getSwitchHarmonyVelocityFixed() {
        return _switchHarmonyVelocityFixed;
    }

    protected void setSwitchHarmonyVelocityFixed(int switchHarmonyVelocityFixed) {
        _switchHarmonyVelocityFixed = switchHarmonyVelocityFixed;
    }

    protected String getSwitchHarmonyNotes() {
        return _switchHarmonyNotes;
    }

    protected void setSwitchHarmonyNotes(String switchHarmonyNotes) {
        _switchHarmonyNotes = switchHarmonyNotes;
    }

    protected String getSwitchSequencerFile() {
        return _switchSequencerFile;
    }

    protected void setSwitchSequencerFile(String switchSequencerFile) {
        if (switchSequencerFile != null) {
            if (_switchSequencerFile != null) {
                if (switchSequencerFile.equals(_switchSequencerFile)) {
                    return;
                }
            }
        }
        _switchSequencerFile = switchSequencerFile;
        if (_switchSequencer != null) {
            _switchSequencer.stopPlayer();
        }
        _switchSequencer = null;
        if (switchSequencerFile != null && switchSequencerFile.isEmpty() == false) {
            File f = new File(switchSequencerFile);
            if (f.exists()) {
                try {
                    SMFPlayer player = new SMFPlayer(f);
                    _switchSequencer = player;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @return the switchLastDetected
     */
    public int getSwitchLastDetected() {
        return _switchLastDetected;
    }

    /**
     * @param switchLastDetected the switchLastDetected to set
     */
    public void setSwitchLastDetected(int switchLastDetected) {
        _switchLastDetected = switchLastDetected;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return _port;
    }

    /**
     * @param port the port to set
     */
    public synchronized void setPort(int port) {
        _port = port;
    }

    /**
     * @return the uiType
     */
    public int getUiType() {
        return _uiType;
    }

    /**
     * @param uiType the uiType to set
     */
    public synchronized void setUiType(int uiType) {
        _uiType = uiType;
    }

    /**
     * @return the row
     */
    public int getRow() {
        return _row;
    }


    /*
     * @return the column
     */
    public int getColumn() {
        return _column;
    }

    /**
     * @return the name
     */
    public String getName() {
        return _name;
    }

    /**
     * @param name the name to set
     */
    public synchronized void setName(String name) {
        _name = name;
    }

    /**
     * @return the memo
     */
    public String getMemo() {
        return _memo;
    }

    /**
     * @param memo the memo to set
     */
    public synchronized void setMemo(String memo) {
        _memo = memo;
    }

    public RangedValue getValue() {
        return _value;
    }

    public void setValue(RangedValue value) {
        if (_uiType == TYPE_DRUMPAD) {
            _switchIncomming = isDrumOn(value._var);
        }
        _value = value;
    }
    
    public void updateValue(int value) {
        _value = _value.updateValue(value);
    }

    /**
     * @return the valueHome
     */
    public int getHomePosition() {
        return _valueHome;
    }

    /**
     * @param valueHome the valueHome to set
     */
    public synchronized void setValueHome(int valueHome) {
        _valueHome = valueHome;
    }

    /**
     * @return the valueLastSent
     */
    public boolean isValueLastSent() {
        return _switchToSent;
    }
    /**
     * @return the gate
     */
    public RangedValue getGate() {
        return _gate;
    }

    /**
     * @param gate the gate to set
     */
    public synchronized void setGate(RangedValue gate) {
        _gate = gate;
    }

    /**
     * @return the channel
     */
    public int getChannel() {
        return _channel;
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

    /**
     * @param channel the channel to set
     */
    public synchronized void setChannel(int channel) {
        _channel = channel;
    }

    /**
     * @return the switchWithToggle
     */
    public boolean isSwitchWithToggle() {
        if (getSwitchType() != SWITCH_TYPE_ON) {
            return _switchWithToggle;
        }
        return false;
    }

    /**
     * @param switchWithToggle the switchWithToggle to set
     */
    public void setSwitchWithToggle(boolean switchWithToggle) {
        _switchWithToggle = switchWithToggle;
    }

    /**
     * @return the switchSequenceSeekStart
     */
    public boolean isSwitchSequenceSeekStart() {
        return _switchSequenceSeekStart;
    }

    /**
     * @param switchSequenceSeekStart the switchSequenceSeekStart to set
     */
    public void setSwitchSequenceSeekStart(boolean switchSequenceSeekStart) {
        this._switchSequenceSeekStart = switchSequenceSeekStart;
    }

    /**
     * @return the switchSequencerToSingltTrack
     */
    public boolean isSwitchSequencerToSingltTrack() {
        return _switchSequencerToSingltTrack;
    }

    /**
     * @param switchSequencerToSingltTrack the switchSequencerToSingltTrack to
     * set
     */
    public void setSwitchSequencerToSingltTrack(boolean switchSequencerToSingltTrack) {
        this._switchSequencerToSingltTrack = switchSequencerToSingltTrack;
    }

    /**
     * @return the switchSequencerFilterNote
     */
    public boolean isSwitchSequencerFilterNote() {
        return _switchSequencerFilterNote;
    }

    /**
     * @param switchSequencerFilterNote the switchSequencerFilterNote to set
     */
    public void setSwitchSequencerFilterNote(boolean switchSequencerFilterNote) {
        this._switchSequencerFilterNote = switchSequencerFilterNote;
    }

    public void startSequence(MX32MixerProcess process) {
        if (_switchSequencer != null) {
            if (_switchSequencerToSingltTrack) {
                _switchSequencer.setForceSingleChannel(_switchOutChannel);
            } else {
                _switchSequencer.setForceSingleChannel(-1);
            }
            _switchSequencer.setFilterNoteOnly(_switchSequencerFilterNote);
            if (_switchSequenceSeekStart) {
                _switchSequencer.setStartPosition(_switchSequencer.getPositionOfFirstNote());
            } else {
                _switchSequencer.setStartPosition(0);
            }
            _switchSequencer.startPlayer(new SMFCallback() {
                @Override
                public void smfPlayNote(SMFMessage e) {
                    process.reenterMXMessageByUI(e.fromSMFtoMX(_port));
                }

                @Override
                public void smfStarted() {
                }

                @Override
                public void smfStoped(boolean fineFinish) {
                }

                @Override
                public void smfProgress(int pos, int finish) {
                }
            });
        }
    }

    public void stopSequence() {
        if (_switchSequencer != null) {
            _switchSequencer.stopPlayer();
            _switchSequencer = null;
        }
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

    public boolean isDrumOn(int value) {
        switch (getSwitchInputType()) {
            case SWITCH_ON_WHEN_MATCH:
                return true;
            case SWITCH_ON_IF_PLUS1:
                if (value >= _value._min + 1) {
                    return true;
                }
                return false;
            case SWITCH_ON_IF_OVER_HALF:
                if (value >= (_value._min + _value._max) / 2) {
                    return true;
                }
                return false;
            case SWITCH_ON_IF_MAX:
                if (value == _value._max) {
                    return true;
                }
                return false;
        }
        System.out.println("isDrumOn = " + getSwitchInputType());
        return false;
    }

}
