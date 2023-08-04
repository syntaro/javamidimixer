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
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMessageTemplate;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.smf.SMFCallback;
import jp.synthtarou.midimixer.libs.midi.smf.SMFMessage;
import jp.synthtarou.midimixer.libs.midi.smf.SMFPlayer;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGStatus implements Cloneable {

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
    public static final int SWITCH_ON_WHEN_ANY = 1;
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

    private static final MXDebugPrint _debug = new MXDebugPrint(MGStatus.class);

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
    private int _row;
    private int _column;

    private String _name = "";
    private String _memo = "";
    private String _textCommand = "";
    
    private int _value = 0;
    private int _rangeMin = 0;
    private int _rangeMax = 127;
    private boolean _uiValueInvert = false;
    private int _valueHome = 0;    
    private boolean _valueLastSent = false;
    private boolean _valueLastDetect = false;
    
    private int _gate = 0;
    private int _channel = 0;
    
    private int _switchLastDetected;
    
    private MXMessageTemplate _cachedMessage = null;

    private int _switchType = SWITCH_TYPE_ONOFF;
    private int _switchInputType = SWITCH_ON_IF_PLUS1;
    private int _switchOutOnType = SWITCH_OUT_ON_SAME_AS_INPUT;
    private int _switchOutOnTypeOfValue = SWITCH_OUT_ON_VALUE_AS_INPUT;
    private int _switchOutOnValueFixed = 127;
    private String _switchOutOnText = "";
    private int _switchOutOnTextGate = 0;
    
    MXMessageTemplate _cacheOutOnMessage = null;

    private int _switchOutOffType = SWITCH_OUT_OFF_SAME_AS_INPUT;
    private int _switchOutOffTypeOfValue = SWITCH_OUT_OFF_VALUE_0;
    private int _switchOutOffValueFixed = 0;
    private String _switchOutOffText = "";
    private int _switchOutOffTextGate = 0;
    MXMessageTemplate _cacheOutOffMessage = null;

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

    public MGStatus(int port, int uiType, int row, int column) {
        _port = port;
        _uiType = uiType;
        _row = row;
        _column = column;
    }

    public MXMessageTemplate getTemplate() {
        if (_cachedMessage == null) {
            _cachedMessage = MXMessageFactory.fromDtext(getTextCommand(), getChannel());
            if (_cachedMessage == null) {
                _cachedMessage = new MXMessageTemplate(new int[3]);
            }
        }
        return _cachedMessage;
    }
    
    public synchronized MXMessage toMXMessage(MXTiming timing) {
        getTemplate();

        MXMessageTemplate template = getTemplate();
        if (template == null) {
            return null;
        }
        MXMessage message = template.buildMessage(_port, _gate, _value);
        message.setValuePairCC14(isValuePairCC14());
        message.setChannel(_channel);;
        
        if (template.get(0) == MXMessageTemplate.DTEXT_RPN
          ||template.get(0) == MXMessageTemplate.DTEXT_NRPN) {
            MXVisitant visit = new MXVisitant();
            visit.setDataroomType(getDataroomType());
            visit.setDataroomMSB(getDataeroomMSB());
            visit.setDataroomLSB(getDataroomLSB());
            visit.setDataentry14(_value);
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
            MXMessage message = _cacheOutOnMessage.buildMessage(_port, getSwitchOutOnTextGate(), value);
            message.setChannel(getSwitchOutChannel());
            return message;
        }
        //TODO
        _debug.println("Error getMXMessageDrumON:getSwitchOutOnType=" + getSwitchOutOnType());
        return null;
    }

    public synchronized MXMessage toMXMessageCaseDrumOff(MXTiming timing) { 
        if (getSwitchOutOffType() == SWITCH_OUT_OFF_NONE) {
            return  null;
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

            MXMessage message = _cacheOutOffMessage.buildMessage(_port, getSwitchOutOffTextGate(), value);
            message._timing = timing;
            message.setChannel(getSwitchOutChannel());
            
            return message;
        }
        //TODO
        _debug.println("Error getMXMessageDrumOff:getSwitchOutOffType=" + getSwitchOutOffType());
        return null;
    }

    public Object clone() {
        MGStatus status = new MGStatus(getPort(), getUiType(), getRow(), getColumn());

        status.setMonitoringTarget(getTextCommand(), getChannel(), getGate(), getValue());
        status.setName(getName());
        status.setMemo(getMemo());
   
        status.setRangeMin(_rangeMin);
        status.setRangeMax(_rangeMax);
        status.setUiValueInvert(_uiValueInvert);
        status.setValueHome(_valueHome); 
        status.setValueLastSent(_valueLastSent);

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

    public synchronized void setMonitoringTarget(String textCommand, int channel, int gate, int value) {
        setTextCommand(textCommand);
        _cachedMessage = null;
        _channel = channel;
        _gate = gate;
        _value = value;

        _rangeMin = 0;
        _rangeMax = 128 -1;
        if (getTemplate().getBytePosHiValue() >= 0) {
            _rangeMax = 128 * 128 -1;
        }
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
            }
            else {
               name = message.toShortString();
            }
        }else {
            name = _name;
        }
        return name + " (" +_memo + ")" + "\n"
           + text + "[row " + (getRow()+1) + ", col " + (getColumn()+1) +"] "
           + "\n" + message.toString() + (message.isValuePairCC14() ? " (=14bit)" : "");
    }

    public boolean statusTryCatch(MXMessage message) {
        MXMessage target = toMXMessage(message._timing);

        if (haveSameStatusAndGate(message)) {
            MXVisitant visit = message.getVisitant();
            if (message.isDataentry()) {
                int original = _value;
                int value = visit.getDataentryValue14();
                switch(message.getGate()) {
                    case MXMidi.DATA1_CC_DATAENTRY:
                        if (value >= _rangeMin && value <= _rangeMax) {
                        }else {
                            return false;
                        }
                        break;
                    case MXMidi.DATA1_CC_DATAINC:
                        value = original + 1;
                        if (value >= _rangeMin && value <= _rangeMax) {
                        }else {
                            return false;
                        }
                        break;
                    case MXMidi.DATA1_CC_DATADEC:
                        value = original - 1;
                        if (value >= _rangeMin && value <= _rangeMax) {
                        }else {
                            return false;
                        }
                        break;
                }
                if (value >= _rangeMin && value <= _rangeMax) {
                    setValue(value);
                    return true;
                }
                return false;
            }

            int value = message.getValue();
            if (message.getCommand() == MXMidi.COMMAND_NOTEOFF) {
                setValue(0);
                return true;
            }
            if (value >= _rangeMin && value <= _rangeMax) {
                setValue(value);
                return true;
            }
            return false;
        }
        else if (canFixWithTemplate(message)) {
            int value = target.importBytesUseTemplate(message);
            if (value >= _rangeMin && value <= _rangeMax) {
                setValue(value);
                return true;
            }
            return false;
        }

        return false;
    }

    public void fixRangedValue() {
        if (getValue() < getRangeMin()) {
            setValue(getRangeMin());
        }
        if (getValue() > getRangeMax()) {
            setValue(getRangeMax());
        }
        if (getRangeMin() > getRangeMax()) {
            setRangeMax(getRangeMin());
        }
    }

    public boolean isDrumOn(int value) {
        switch(getSwitchInputType()) {
            case SWITCH_ON_WHEN_ANY:
                return true;
            case SWITCH_ON_IF_PLUS1:
                if (value >= getRangeMin() + 1) {
                    return true;
                }
                return false;
            case SWITCH_ON_IF_OVER_HALF:
                if (value >= (getRangeMin() + getRangeMax()) / 2) {
                    return true;
                }
                return false;
            case SWITCH_ON_IF_MAX:
                if (value == getRangeMax()) {
                    return true;
                }
                return false;
        }
        _debug.println("isDrumOn = " + getSwitchInputType());
        return false;
    }

    public int getValueForSwitchOn() {
        switch(getSwitchOutOnType()) {
            case SWITCH_OUT_ON_SAME_AS_INPUT:
            case SWITCH_OUT_ON_CUSTOM:
                switch(getSwitchOutOnTypeOfValue()) {
                    case SWITCH_OUT_ON_VALUE_AS_INPUT:
                        return getSwitchLastDetected();
                        
                    case SWITCH_OUT_ON_VALUE_AS_INPUT_PLUS1:
                        int x = getSwitchLastDetected();
                        if (x == getRangeMin() && x < getRangeMax()) {
                            x ++;
                        }
                        return x;
                    case SWITCH_OUT_ON_VALUE_FIXED:
                        return getSwitchOutOnValueFixed();
                }
        }
        return 0;
    }

    public int getValueForSwitchOff() {
        switch(getSwitchOutOffType()) {
            case SWITCH_OUT_OFF_NONE:
                return -1;
            case SWITCH_OUT_OFF_SAME_AS_INPUT:
            case SWITCH_OUT_OFF_SAME_AS_OUTPUT_ON:
            case SWITCH_OUT_OFF_CUSTOM:
                switch(getSwitchOutOnTypeOfValue()) {
                    case SWITCH_OUT_OFF_VALUE_0:
                        return 0;

                    case SWITCH_OUT_OFF_VALUE_FIXED:
                        return getSwitchOutOffValueFixed();

                    case SWITCH_OUT_OFF_VALUE_SAME_AS_INPUT:
                        return getSwitchLastDetected();

                    case SWITCH_OUT_OFF_VALUE_SAME_AS_MIN:
                        return getRangeMin();
                        
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
                }catch(Exception e) {
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
        _cachedMessage = null;
        _uiType = uiType;
    }

    /**
     * @return the row
     */
    public int getRow() {
        return _row;
    }

    /**
     * @param row the row to set
     */
    public synchronized void setRow(int row) {
        _row = row;
    }

    /**
     * @return the column
     */
    public int getColumn() {
        return _column;
    }

    /**
     * @param column the column to set
     */
    public synchronized void setColumn(int column) {
        _column = column;
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

    /**
     * @return the textCommand
     */
    public String getTextCommand() {
        return _textCommand;
    }

    /**
     * @param textCommand the textCommand to set
     */
    public synchronized void setTextCommand(String textCommand) {
        _cachedMessage = null;
        _textCommand = textCommand;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return _value;
    }

    /**
     * @param value the value to set
     */
    public synchronized void setValue(int value) {
        if (value > 128 * 128 -1) {
            value = 128 * 128 -1;
        }
        _value = value;
    }

    /**
     * @return the rangeMin
     */
    public int getRangeMin() {
        return _rangeMin;
    }

    /**
     * @param rangeMin the ioRangeMin to set
     */
    public synchronized void setRangeMin(int rangeMin) {
        if (rangeMin > 128 * 128 -1) {
            rangeMin = 128 * 128 -1;
        }
        _rangeMin = rangeMin;
    }

    /**
     * @return the ioRangeMax
     */
    public int getRangeMax() {
        return _rangeMax;
    }

    /**
     * @param rangeMax the ioRangeMax to set
     */
    public synchronized void setRangeMax(int rangeMax) {
        if (rangeMax > 128 * 128 -1) {
            rangeMax = 128 * 128 -1;
        }
        _rangeMax = rangeMax;
    }

    /**
     * @return the uiValueInvert
     */
    public boolean isUiValueInvert() {
        return _uiValueInvert;
    }

    /**
     * @param uiValueInvert the uiValueInvert to set
     */
    public synchronized void setUiValueInvert(boolean uiValueInvert) {
        _uiValueInvert = uiValueInvert;
    }

    /**
     * @return the valueHome
     */
    public int getValueHome() {
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
        return _valueLastSent;
    }

    /**
     * @param valueLastSent the valueLastSent to set
     */
    public synchronized void setValueLastSent(boolean valueLastSent) {
        _valueLastSent = valueLastSent;
    }

    /**
     * @param valueLastSent the valueLastSent to set
     */
    public boolean getValueLastSent() {
        return _valueLastSent;
    }

    /**
     * @return the gate
     */
    public int getGate() {
        return _gate;
    }

    /**
     * @param gate the gate to set
     */
    public synchronized void setGate(int gate) {
        _gate = gate;
    }

    /**
     * @return the channel
     */
    public int getChannel() {
        return _channel;
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
     * @param switchSequencerToSingltTrack the switchSequencerToSingltTrack to set
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
    
    public void startSequence() {
        if (_switchSequencer != null) {
            if (_switchSequencerToSingltTrack) {
                _switchSequencer.setForceSingleChannel(_switchOutChannel);
            }else {
                _switchSequencer.setForceSingleChannel(-1);
            }
            _switchSequencer.setFilterNoteOnly(_switchSequencerFilterNote);
            if (_switchSequenceSeekStart) {
                _switchSequencer.setStartPosition(_switchSequencer.getPositionOfFirstNote());
            }else {
                _switchSequencer.setStartPosition(0);
            }
            _switchSequencer.startPlayer(new SMFCallback() {
                @Override
                public void smfPlayNote(SMFMessage e) {
                    //tODO 
                    MXReceiver process = MXMain.getMain().getKontrolProcess().getNextReceiver();
                    process.processMXMessage(e.fromSMFtoMX(_port));
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

    /**
     * @return the _valueLastDetect
     */
    public boolean isValueLastDetect() {
        return _valueLastDetect;
    }

    /**
     * @param _valueLastDetect the _valueLastDetect to set
     */
    public void setValueLastDetect(boolean _valueLastDetect) {
        this._valueLastDetect = _valueLastDetect;
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
        if (message.isMessageTypeChannel()) {
            if (_channel != message.getChannel()) {
                return false;
            }
            if (from.getCommand() == MXMidi.COMMAND_NOTEON || from.getCommand() == MXMidi.COMMAND_NOTEOFF) {
                if (message.getCommand() == MXMidi.COMMAND_NOTEON || message.getCommand() == MXMidi.COMMAND_NOTEOFF) {
                    if (from.getGate() != message.getGate()) {
                        return false;
                    }
                    return true;
                }else {
                    return false;
                }
            }
            if (from.getCommand() == message.getCommand()) {
                if (from.getCommand() == MXMidi.COMMAND_CONTROLCHANGE) {
                    int cc1 = from.getGate();
                    int cc2 = message.getGate();
                    if (cc1 != message.getGate()) {
                        if (cc1 >= 0 && cc1 <= 31 && cc1 + 32 == cc2) {
                            return true;
                        }
                        if (cc2 >= 0 && cc2 <= 31 && cc2 + 32 == cc1) {
                            return true;
                        }
                        return false;
                    }
                    return true;
                }
                return true;
            }
            return false;
        }
        return false;
    }
    
    public boolean canFixWithTemplate(MXMessage message) {
        MXMessage from = toMXMessage(null);
        byte[] byteFrom = from.getDataBytes();
        byte[] byteTo = message.getDataBytes();
        if (byteFrom == null) {
            if (byteTo == null) {
                return true;
            }
            return false;
        }
        if (byteTo == null) {
            return false;
        }

        if (byteFrom.length != byteTo.length) {
            return false;
        }

        int hitcount = 0;
        int faultcont = 0;
        int ignorecount = 0;
        for (int i = 0; i < byteFrom.length; ++ i) {
            if (byteFrom[i] == byteTo[i]) {
                hitcount ++;
                continue;
            }
            if (byteFrom[i] != byteTo[i]) {
                int fromX = from.getTemplate(i);

                if (fromX == MXMessageTemplate.DTEXT_VH || fromX == MXMessageTemplate.DTEXT_VL || fromX == MXMessageTemplate.DTEXT_CHECKSUM) {
                    ignorecount ++;
                    continue;
                }

                int messageX = message.getTemplate(i);

                if (messageX == MXMessageTemplate.DTEXT_VH || messageX == MXMessageTemplate.DTEXT_VL || messageX == MXMessageTemplate.DTEXT_CHECKSUM) {
                    ignorecount ++;
                    continue;
                }
                faultcont ++;
            }
        }
        
        if (faultcont == 0) {
            return true;
        }
        return false;
    }
}
