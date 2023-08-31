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
package jp.synthtarou.midimixer.libs.midi;

import java.io.PrintStream;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import static jp.synthtarou.midimixer.libs.midi.MXTemplate.DTEXT_PROGDEC;
import static jp.synthtarou.midimixer.libs.midi.MXTemplate.DTEXT_PROGINC;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;
import jp.synthtarou.midimixer.libs.console.ConsoleElement;

/**
 *
 * @author Syntarou YOSHIDA
 * infomation from g200kg Music & Software https://www.g200kg.com/
 */
public final class MXMessage {

    public boolean isBinMessage() {
        if (createBytes() == null) {
            return false;
        }
        if (_template.size() == 3) {
            int b = _dataBytes[0] & 0xff;
            if (b >= 0x80 && b <= 0xff) {
                switch(b) {
                    case 0xf0:
                    case 0xf7:
                    case 0xff:
                        return true;
                }
                return false;
            }
            int c = _template.get(0);
            if (c == DTEXT_PROGINC || c== DTEXT_PROGDEC) {//TODO check all about this gimic
                return false;
            }
        }
        return true;
    }

    //Utilitie
    public boolean isPairAbleCC14() {
        if (isCommand(MXMidi.COMMAND_CONTROLCHANGE)) {
            int cc = getGate()._var;
            if (cc >= 0 && cc <= 31) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the it14bitCC
     */
    public boolean isValuePairCC14() {
        return _valuePair14bit;
    }
    
    /**
     * @param value14bit the it14bitCC to set
     */
    public void setValuePairCC14(boolean value14bit) {
        if (_valuePair14bit != value14bit) {
            int value = getValue()._var;
            if (value14bit == false && value >= 128) {
                setValue(127);
            }
        }
        this._valuePair14bit = value14bit;
    }

    private MXVisitant _visitant;

    public MXVisitant getVisitant() {
        return _visitant;
    }

    public void setVisitant(MXVisitant visitant) {
        _visitant = visitant;
        _dataBytes = null;
    }
    
    private static final MXDebugPrint _debug = new MXDebugPrint(MXMessage.class);

    public MXTiming _timing;
    int _port;

    private RangedValue _value = RangedValue.ZERO7;
    private RangedValue _gate = RangedValue.ZERO7;
    private int _channel = 0;
    
    protected byte[] _dataBytes = null;
    private boolean _valuePair14bit;

    private final MXTemplate _template;

    private int _metaType;
    public String _metaText;
    
    public byte[] createBytes() {
        if (_dataBytes != null) {
            return _dataBytes;
        }
        if (_template == null) {
            return null;
        }
        _dataBytes = _template.makeBytes(_dataBytes, this);
        return _dataBytes;
    }

    public int getPort() {
        return _port;
    }
    
    public void setPort(int port) {
        if (_port != port) {
            _port = port;
        }
    }
 
    public int getStatus() {
        if (createBytes() == null) {
            return 0;
        }
        int status = _dataBytes[0] & 0xff;
        return status;
    }
    
    public boolean isCommand(int command) {
        int status = getStatus();
        if (status >= 0x80 && status <= 0xef) {
            if ((status & 0xf0) == command) {
                return true;
            }
        }
        return false;
    }

    public void setChannel(int channel) {
        if (_channel != channel) {
             _dataBytes = null;
            _channel = channel;
        }
    }
    
    public int getData1() {
        if (createBytes() == null) {
            return 0;
        }
        if (_dataBytes.length > 1) {
            return _dataBytes[1] & 0xff;    
        }
        return 0;
    }

    public int getData2() {
        if (createBytes() == null) {
            return 0;
        }
        if (_dataBytes.length > 2) {
            return _dataBytes[2] & 0xff;
        }
        return 0;
    }

    public int getChannel() {
        if (_channel < 0 || _channel > 15) {
            _debug.println("getAsChannel " + _channel);
            return 0;
        }
        return _channel;
    }
    
    public RangedValue getValue() {
        return _value;
    }

    public void setValue(int value) {
        if (value != _value._var) {
            _value = _value.updateValue(value);
            _dataBytes = null;
        }
    }

    public RangedValue getGate() {
        return _gate;
    }

    public void setGate(int gate) {
        if (gate != _gate._var) {
            _gate = _gate.updateValue(gate);
            _dataBytes = null;
        }
    }

    public boolean isMetaMessage() {
        byte[] data = getDataBytes();
        if (data.length > 0) {
            if ((data[0] & 0xff) == 0xff) {
                return true;
            }
        }
        return false;
    }

    public int getMetaType() {
        return _metaType;
    }

    public void setMetaType(int metaCode) {
        if (metaCode != _metaType) {
            _metaType = metaCode;
            _dataBytes = null;
        }
    }

    protected MXMessage(int port, MXTemplate template) {
        this(port, template, 0, RangedValue.ZERO7, RangedValue.ZERO7);
    }

    protected MXMessage(int port, MXTemplate template, int channel, RangedValue gate, RangedValue value) {
        if (template == null) {
            throw new NullPointerException();
        }
        _port = port;
        _template = template;
        _channel = channel;
        _gate = gate;
        _value = value;
        
        if (_template != null && _template.size() > 0) {
            if (_template.get(0) == 0xff) {
                _metaType = _template.get(1);
            }
        }
    }


    public byte[] getDataBytes() {
        createBytes();
        return _dataBytes;        //SYSEXの場合１バイト目は、STATUSに入る
    }

    public boolean isMessageTypeChannel() {
        createBytes();
        if (isBinMessage() == false) {
            int status = getStatus();
            if (status >= 0x80 && status <= 0xef) {
                return true;
            }
        }
        return false;
    }

    public boolean isDataentry() {
        createBytes();
        if (_template.isDataentry()) {
            return true;
        }
        if (isCommand(MXMidi.COMMAND_CONTROLCHANGE)) {
            switch (getData1()) {
                case MXMidi.DATA1_CC_DATAENTRY:
                case MXMidi.DATA1_CC_DATAINC:
                case MXMidi.DATA1_CC_DATADEC:
                    return true;
            }
        }
        return false;
    }

    public String toString() {
        String str = toStringHeader();
        if (isCommand(MXMidi.COMMAND_PROGRAMCHANGE)) {
            return str + "("+ getGate() + ")";
        }else {
            return str + "("+ getValue() + ")";
        }
    }
        
    public String toStringHeader(int min, int max) {
        String str = toStringHeader();
        if (min != max) {
            return str + "= Range" + min + " to " + max;
        }else {
            return str + "= " + max;
        }
    }
    
    public String toStringHeader() {
        if (createBytes() == null) {
            return "";
        }
        
        int port = getPort();
        
        switch ((int)_dataBytes[0]) {
            case 0xff:
                return "Meta [" + this.getMetaType() + ":" + this._metaText + "]";
            case 0xf0:
                return "Sysex [" + MXUtil.dumpHexFF(getDataBytes()) + "]";
            case 0xf7:
                return "SysexSpecial [" + MXUtil.dumpHexFF(getDataBytes()) + "]";
        }
        if (isDataentry()) {
            int r1 = toDataroomMSB1();
            int r2 = toDataroomLSB2();
            int d1 = toDatavalueMSB1();
            int d2 = toDatavalueLSB2();
            
            return ConsoleElement.toSegmentText(r1)
                 + ConsoleElement.toSegmentText(r2)
                 + ConsoleElement.toSegmentText(d1)
                 + ConsoleElement.toSegmentText(d2);
        }
        if (isBinMessage()) {
            return "Binary[" + MXUtil.dumpHexFF(getDataBytes()) + "]";
        }

        int dword = (((getStatus() << 8) | getData1()) << 8) | getData2();
        return ConsoleElement.toSegmentText(dword);
    }
    
    
    public String toShortString() {
        if (createBytes() == null) {
            return "";
        }
        
        String chname;
        if (isMessageTypeChannel()) {
            int channel = getChannel();
            chname = "" + (channel+1);
        }else {
            chname = "";
        }

        switch((int)_dataBytes[0]) {
            case 0xf0:
                return "Sys";
            case 0xf7:
                return "Sys2";
            case 0xff:
                return "Meta";
        }
        if (isBinMessage()) {
            return "???";
        }
 
        int status = getStatus();
        int command = status;
        if (command >= 0x80 && command <= 0xef) {
            command &= 0xf0;
        }
        
        if (command == MXMidi.COMMAND_CONTROLCHANGE) {
            int data1 = getData1();
            if (data1 == MXMidi.DATA1_CC_DATAENTRY && _visitant != null) {
                if (_visitant.isHaveDataentryRPN()) {
                    return chname + "RPN[" + MXUtil.toHexFF(_visitant.getDataroomMSB()) + ":" + MXUtil.toHexFF(_visitant.getDataroomLSB()) + "]";
                }else if (_visitant.isHaveDataentryNRPN()) {
                    return chname + "NRPN[" + MXUtil.toHexFF(_visitant.getDataroomMSB()) + ":" + MXUtil.toHexFF(_visitant.getDataroomLSB()) + "]";
                }
            }else 
            if (data1 == MXMidi.DATA1_CC_DATAINC && _visitant != null) {
                if (_visitant.isHaveDataentryRPN()) {
                    return chname + "RPN[" + MXUtil.toHexFF(_visitant.getDataroomMSB()) + ":" + MXUtil.toHexFF(_visitant.getDataroomLSB()) + "]+";
                }else if (_visitant.isHaveDataentryNRPN()) {
                    return chname + "NRPN[" + MXUtil.toHexFF(_visitant.getDataroomMSB()) + ":" + MXUtil.toHexFF(_visitant.getDataroomLSB()) + "]+";
                }            
            }else 
            if (data1 == MXMidi.DATA1_CC_DATADEC && _visitant != null) {
                if (_visitant.isHaveDataentryRPN()) {
                    return chname + "RPN[" + MXUtil.toHexFF(_visitant.getDataroomMSB()) + ":" + MXUtil.toHexFF(_visitant.getDataroomLSB()) + "]-";
                }else if (_visitant.isHaveDataentryNRPN()) {
                    return chname + "NRPN[" + MXUtil.toHexFF(_visitant.getDataroomMSB()) + ":" + MXUtil.toHexFF(_visitant.getDataroomLSB()) + "]-";
                }            
            }
            return chname + MXMidi.nameOfControlChange(data1);
        }else {
            if (command == MXMidi.COMMAND_NOTEOFF) {
                int note = getGate()._var;
                int velocity = getValue()._var;
                return  chname + MXMidi.nameOfNote(note) + "-";
            }
            if (command == MXMidi.COMMAND_NOTEON) {
                int note = getGate()._var;
                int velocity = getValue()._var;
                return  chname + MXMidi.nameOfNote(note);
            }
            if (command == MXMidi.COMMAND_POLYPRESSURE) {
                int note = getGate()._var;
                int velocity = getValue()._var;
                return  chname + "PPrs";
            }
            if (command == MXMidi.COMMAND_PROGRAMCHANGE) {
                int program = getGate()._var;
                return  chname + "PG" + program;
            }
            if (command == MXMidi.COMMAND_CHANNELPRESSURE) {
                return  chname + "ChPrs";
            }
            if (command == MXMidi.COMMAND_PITCHWHEEL) {
                return  chname + "Pitch";
            }
            if (command == MXMidi.STATUS_SONGPOSITION) {
                return  chname + "Pos";
            }
            if (command == MXMidi.STATUS_SONGSELECT) {
                return  chname + "Song";
            }
        }

        if (isMessageTypeChannel()) {
            return  chname + MXMidi.nameOfMessage(getStatus(), getData1(), getData2());
        }else {
            return  MXMidi.nameOfMessage(getStatus(), getData1(), getData2());
        }
    }
    
    public static void main(String[] args) {
        MXMessage message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_NOTEON + 0, 64, 127);
        String dtext = message._template.toDText(message);

        MXTemplate template = MXMessageFactory.fromDtext(dtext, message.getChannel());
        MXMessage msg = template.buildMessage(message.getPort(), message.getChannel(), message.getGate(), message.getValue());

        PrintStream output = System.out;
        
        output.println(message);
        output.println(dtext);
        output.println(msg);
        
        msg.setPort(6);
        msg.setChannel(2);
        msg.setValue(100);

        output.println(msg);
        output.println("----------------");

        MXMessage message2 = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CONTROLCHANGE + 1, MXMidi.DATA1_CC_CHANNEL_VOLUME, 127);
        output.println(message2);
        String dtext2 = message2._template.toDText(message2);
        MXTemplate template2 = MXMessageFactory.fromDtext(dtext2, message2.getChannel());
        MXMessage msg2 = template.buildMessage(message2.getPort(), message2.getChannel(), message2.getGate(), message2.getValue());

        output.println(dtext2);
        output.println(msg2);
        
        msg2.setPort(6);
        msg2.setChannel(2);
        msg2.setValue(100);

        output.println(msg2);
        output.println("----------------");
    }
    
    public void debugDump(String func) {
        PrintStream output = System.out;
        StringBuffer buf = new StringBuffer();
        buf.append(func + " debugDump [template = ");
        buf.append(_template.toDArray(this));
        buf.append("] bytes = [ ");
        byte[] b = getDataBytes();
        for (int i = 0; i < b.length; ++ i) {
            buf.append(MXUtil.toHexFF(b[i]) + " ");
        }
        buf.append("]");
        buf.append(" gate = " + getGate());
        buf.append(" value = " + getValue());
        output.println(buf);
    }
    
    public String toDumpString() {
        createBytes();
        
        String chname;
        if (isMessageTypeChannel()) {
            int channel = getChannel();
            chname = Integer.toString(channel+1);
        }else {
            chname = "";
        }

        if (isBinMessage()) {
            if (_dataBytes[0] == 0xff) {
                return _metaText;
            }
        }
        return MXUtil.dumpHexFF(_dataBytes);
    }
    
    public int getDwordCount() {
        if (isDataentry()) {
            MXVisitant visit = getVisitant();
            if (visit != null) {
                if (visit.isHaveDataentryRPN()) {
                    return 4;
                }else if (visit.isHaveDataentryNRPN()) {
                    return 4;
                }
            }
            return -1;
        }
        if (isBinMessage()) {
            return 0;
        }
        if (_template.size() == 0) {
            return 0;
        }
        if (_template.get(0) == DTEXT_PROGINC || _template.get(0) == DTEXT_PROGDEC) {
            return 3;
        }
        if (isCommand(MXMidi.COMMAND_CONTROLCHANGE) && isValuePairCC14()) {
            return 2;
        }
        return 1;
    }

    public int getAsDword(int column) {
        if (isDataentry()) {
            switch(column) {
                case 0:
                    return toDataroomMSB1();
                case 1:
                    return toDataroomLSB2();
                case 2:
                    return toDatavalueMSB1();
                case 3:
                    return toDatavalueLSB2();
            }
            return 0;
        }
        if (isBinMessage() == false) {
            int status = getStatus();
            int data1 = getData1();
            int data2 = getData2();
            if ((getStatus() & 0xf0)== MXMidi.COMMAND_CONTROLCHANGE && isValuePairCC14()) {
                if (column == 0)  {
                    data2 = (getValue()._var >> 7) & 0x7f;
                }else {
                    data1 += 0x20;
                    data2 = getValue()._var & 0x7f;
                }
            }
            return (status << 16) | (data1 << 8) | data2;
        }
        return 0;
    }

    public int toDataroomMSB1() {
        if (getVisitant() == null) {
            return  0;
        }
        int status, data1, data2;
        if (getVisitant().isHaveDataentryRPN()){
            status =MXMidi.COMMAND_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_RPN_MSB;
            data2 = getVisitant().getDataroomMSB();
            return (((status << 8) | data1) << 8) | data2;
        }else if (getVisitant().isHaveDataentryNRPN()) {
            status =MXMidi.COMMAND_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_NRPN_MSB;
            data2 = getVisitant().getDataroomMSB();
            return (((status << 8) | data1) << 8) | data2;
        }
        return 0;
    }

    public int toDataroomLSB2()  {
        if (getVisitant() == null) {
            //System.out.println("DATAENTRY ERROR");
            return  0;
        }
        int status, data1, data2;
        if (getVisitant().isHaveDataentryRPN()){
            status =MXMidi.COMMAND_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_RPN_LSB;
            data2 = getVisitant().getDataroomLSB();
            return (((status << 8) | data1) << 8) | data2;
        }else if (getVisitant().isHaveDataentryNRPN()) {
            status =MXMidi.COMMAND_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_NRPN_LSB;
            data2 = getVisitant().getDataroomLSB();
            return (((status << 8) | data1) << 8) | data2;
        }
        return 0;
    }

    public int toDatavalueMSB1() {
        if (getVisitant() == null) {
            //System.out.println("DATAENTRY ERROR");
            return  0;
        }
        int status, data1, data2;
        if (getVisitant().isHaveDataentryRPN() || getVisitant().isHaveDataentryNRPN()) {
            status =MXMidi.COMMAND_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_DATAENTRY;
            data2 = getVisitant().getDataentryMSB();
            return (((status << 8) | data1) << 8) | data2;
        }
        return 0;
    }

    public int toDatavalueLSB2()  {
        if (getVisitant() == null) {
            //System.out.println("DATAENTRY ERROR");
            return  0;
        }
        int status, data1, data2;
        if (getVisitant().isHaveDataentryRPN() || getVisitant().isHaveDataentryNRPN()) {
            status = MXMidi.COMMAND_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_DATAENTRY + 0x20;
            data2 = getVisitant().getDataentryLSB();
            return (((status << 8) | data1) << 8) | data2;
        }
        return 0;
    }
    
    public boolean hasValueHiField() {
        return _template.getBytePosHiValue() >= 0;
    }

    public boolean hasValueLowField() {
        return _template.getBytePosValue() >= 0;
    }

    public boolean hasGateLowField() {
        return _template.getBytePosGate() >= 0;
    }

    public boolean hasGateHiField() {
        if (_template == null) {
            return false;
        }
        return _template.getBytePosHiGate() >= 0;
    }
    
    public String toTemplateText() {
        return _template.toDText(this);
    }
    
    public MXTemplate getTemplate() {
        return _template;
    }
    
    public Object clone() {
        MXMessage message;
        message = new MXMessage(_port, _template, _channel, _gate, _value);
        
        message._timing = this._timing;

        message.setVisitant(getVisitant());
        message.setValuePairCC14(isValuePairCC14());

        return message;
    }
}
