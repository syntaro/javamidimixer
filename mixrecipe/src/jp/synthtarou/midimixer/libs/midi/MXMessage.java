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
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.MXLogger2;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.common.async.Transaction;
import static jp.synthtarou.midimixer.libs.midi.MXTemplate.parseDAlias;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;
import jp.synthtarou.midimixer.libs.midi.console.MXMidiConsoleElement;
import jp.synthtarou.midimixer.mx30surface.MGStatus;
import jp.synthtarou.midimixer.mx70console.MX70View;

/**
 *
 * @author Syntarou YOSHIDA infomation from g200kg Music & Software
 * https://www.g200kg.com/
 */
public final class MXMessage implements Comparable<MXMessage> {

    int _progBankMSB = -1;
    int _progBankLSB = -1;
    
    public MGStatus[] _mx30record = null;

    public void setProgramBank(int msb, int lsb) {
        _progBankMSB = msb;
        _progBankLSB = lsb;
    }

    public MXTiming _timing;
    private int _port;

    private MXRangedValue _value = MXRangedValue.ZERO7;
    private MXRangedValue _gate = MXRangedValue.ZERO7;
    private int _channel = 0;

    protected byte[] _dataBytes = null;
    private boolean _pairedWith14;

    private final MXTemplate _template;

    public boolean isEmpty() {
        return _template.isEmpty();
    }

    public boolean isBinaryMessage() {
        int first = _template.get(0);
        switch (first) {
            case MXMidi.COMMAND_SYSEX:
            case MXMidi.COMMAND_SYSEX_END:
            case MXMidi.COMMAND_META_OR_RESET:
                return true;

            case MXMidi.COMMAND2_META:
            case MXMidi.COMMAND2_SYSTEM:
                return true;
                
            case MXMidi.COMMAND2_CH_RPN:
            case MXMidi.COMMAND2_CH_NRPN:
            case MXMidi.COMMAND2_CH_PROGRAM_INC:
            case MXMidi.COMMAND2_CH_PROGRAM_DEC:
                return false;
        }
        return false;
    }

    //Utilitie
    public boolean isPairAbleCC14() {
        if (isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
            int cc = getGate()._value;
            if (cc >= 0 && cc <= 31) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the it14bitCC
     */
    public boolean isPairedWith14() {
        return _pairedWith14;
    }

    /**
     * @param value14bit the it14bitCC to set
     */
    public void setPairedWith14(boolean value14bit) {
        this._pairedWith14 = value14bit;
    }

    private MXVisitant _visitant;

    public MXVisitant getVisitant() {
        return _visitant;
    }

    public void setVisitant(MXVisitant visitant) {
        _visitant = visitant;
        _dataBytes = null;
    }

    private byte[] createBytes() {
        _dataBytes = _template.makeBytes(_dataBytes, this);
        return _dataBytes;
    }
    
    public int sizeOfTemplate() {
        return _template.size();
    }
    
    public int parseTemplate(int pos) {
        int x = _template.get(pos);
        try {
            return parseDAlias(x, this);
        } catch (IllegalArgumentException e) {
            //throw e;
        }
        return x;
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
            return 0;
        }
        return _channel;
    }

    public MXRangedValue getValue() {
        return _value;
    }

    public void setValue(MXRangedValue value) {
        if (_value != null) {
            if (_value.equals(value)) {
                return;
            }
        }
        _value = value;
        _dataBytes = null;
    }

    public void setValue(int value) {
        if (_value._value == value) {
            return;
        }
        _value = _value.changeValue(value);
        _dataBytes = null;
    }

    public MXRangedValue getGate() {
        return _gate;
    }

    public void setGate(MXRangedValue gate) {
        if (_gate.equals(gate)) {
            return;
        }
        _gate = gate;
        _dataBytes = null;
    }

    public void setGate(int gate) {
        if (_gate._value == gate) {
            return;
        }
        _gate = _gate.changeValue(gate);
        _dataBytes = null;
    }

    public boolean isMetaMessage() {
        byte[] data = getBinary();
        if (data.length > 0) {
            if ((data[0] & 0xff) == 0xff) {
                return true;
            }
        }
        return false;
    }

    public int getMetaType() {
        if (_template.get(0) == 0xff) {
            return _template.get(1);
        }
        return -1;
    }

    public String getMetaText() {
        if (_template.get(0) == 0xff) {
            String text = "";
            byte[] data = getBinary();
            try {
                text = new String(data, 2, data.length - 2, "ASCII");
                text = new String(data, 2, data.length - 2, "utf8");
                text = new String(data, 2, data.length - 2, "SJIS");
            } catch (UnsupportedEncodingException ex) {
                MXLogger2.getLogger(MXMessage.class).log(Level.WARNING, ex.getMessage(), ex);
            } catch (RuntimeException ex) {
                MXLogger2.getLogger(MXMessage.class).log(Level.WARNING, ex.getMessage(), ex);
            }
            return text;
        }
        return null;
    }

    MXMessage(int port, MXTemplate template) {
        this(port, template, 0, MXRangedValue.ZERO7, MXRangedValue.ZERO7);
    }

    MXMessage(int port, MXTemplate template, int channel, MXRangedValue gate, MXRangedValue value) {
        if (template == null) {
            throw new NullPointerException();
        }
        if (gate == null) {
            if (template.indexOfGateHi() >= 0) {
                gate = MXRangedValue.new14bit(0);
            } else if (template.indexOfGateLow() >= 0) {
                gate = MXRangedValue.new7bit(0);
            } else {
                gate = MXRangedValue.new7bit(0);
            }
        }
        if (value == null) {
            if (template.indexOfValueHi() >= 0) {
                value = MXRangedValue.new14bit(0);
            } else if (template.indexOfValueLow() >= 0) {
                value = MXRangedValue.new7bit(0);
            } else {
                value = MXRangedValue.new7bit(0);
            }
        }
        _port = port;
        _template = template;
        _channel = channel;
        _gate = gate;
        _value = value;
    }

    public byte[] getBinary() {
        if (getDwordCount() >= 2) {
            Exception ex = new IllegalStateException("dword count == "+  getDwordCount());
            MXLogger2.getLogger(MXMessage.class).log(Level.WARNING, ex.getMessage(), ex);
        }
        createBytes();
        return _dataBytes;        //SYSEXの場合１バイト目は、STATUSに入る
    }

    public boolean isMessageTypeChannel() {
        if (_template.size() > 0) {
            int code = _template.get(0);
            if (code >= 0x80 && code <= 0xef) {
                return true;
            }
        }
        return false;
    }

    public boolean isDataentryBy2() {
        if (_template.get(0) == MXMidi.COMMAND2_CH_RPN
                || _template.get(0) == MXMidi.COMMAND2_CH_NRPN) {
            return true;
        }
        return false;
    }

    public boolean isDataentryByCC() {
        if (_template.get(0) == MXMidi.COMMAND2_CH_RPN
                || _template.get(0) == MXMidi.COMMAND2_CH_NRPN) {
            return false;
        }
        createBytes();
        if (isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
            switch (getData1()) {
                case MXMidi.DATA1_CC_DATAENTRY:
                case MXMidi.DATA1_CC_DATAENTRY + 0x20:
                case MXMidi.DATA1_CC_DATAINC:
                case MXMidi.DATA1_CC_DATADEC:
                    return true;
            }
        }
        return false;
    }

    public String toString() {
        String str = toStringHeader();
        if (isCommand(MXMidi.COMMAND_CH_PROGRAMCHANGE)) {
            return str + "(" + getGate() + ")";
        } else {
            return str + "(" + getValue() + ")";
        }
    }

    public String toStringHeader(int min, int max) {
        String str = toStringHeader();
        if (min != max) {
            return str + "= Range" + min + " to " + max;
        } else {
            return str + "= " + max;
        }
    }

    public String toStringHeader() {
        if (createBytes() == null) {
            return "";
        }

        int port = getPort();

        switch ((int) _dataBytes[0]) {
            case 0xff:
                return "Meta [" + getMetaType() + ":" + getMetaText() + "]";
            case 0xf0:
                return "Sysex [" + MXUtil.dumpHex(getBinary()) + "]";
            case 0xf7:
                return "SysexSpecial [" + MXUtil.dumpHex(getBinary()) + "]";
        }
        if (getDwordCount() >= 1) {
            StringBuffer str = new StringBuffer();
            for (int i = 0; i < getDwordCount(); ++ i) {
                int dword = getAsDword(i);
                if (i != 0) {
                    str.append(", ");
                }
                str.append(MXMidiConsoleElement.toSegmentText(dword));
            }
            return str.toString();
        }
        else if (isBinaryMessage()) {
            return "Binary[" + MXUtil.dumpHex(getBinary()) + "]";
        }
        else {
            return "*Nan*";
        }
    }

    public String toStringForUI() {
        if (createBytes() == null) {
            return "";
        }

        String chname;
        if (isMessageTypeChannel()) {
            int channel = getChannel();
            chname = "" + (channel + 1);
        } else {
            chname = "";
        }

        switch ((int) _dataBytes[0]) {
            case 0xf0:
                return "Sys";
            case 0xf7:
                return "Sys2";
            case 0xff:
                return "Meta";
        }
        if (isBinaryMessage()) {
            return "???";
        }

        int status = getStatus();
        int command = status;
        if (command >= 0x80 && command <= 0xef) {
            command &= 0xf0;
        }

        if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
            int data1 = getData1();
            return chname + MXMidi.nameOfControlChange(data1);
        } else if (command == MXMidi.COMMAND_CH_NOTEOFF) {
            int note = getGate()._value;
            int velocity = getValue()._value;
            return chname + MXMidi.nameOfNote(note) + "-";
        }
        if (command == MXMidi.COMMAND_CH_NOTEON) {
            int note = getGate()._value;
            int velocity = getValue()._value;
            return chname + MXMidi.nameOfNote(note);
        }
        if (command == MXMidi.COMMAND_CH_POLYPRESSURE) {
            int note = getGate()._value;
            int velocity = getValue()._value;
            return chname + "Prs";
        }
        if (command == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
            int program = getGate()._value;
            return chname + "Pg" + program;
        }
        if (command == MXMidi.COMMAND_CH_CHANNELPRESSURE) {
            return chname + "ChPrs";
        }
        if (command == MXMidi.COMMAND_CH_PITCHWHEEL) {
            return chname + "Pitch";
        }
        if (command == MXMidi.COMMAND_SONGPOSITION) {
            return chname + "Pos";
        }
        if (command == MXMidi.COMMAND_SONGSELECT) {
            return chname + "Song";
        }

        if (isMessageTypeChannel()) {
            return chname + MXMidi.nameOfMessage(getStatus(), getData1(), getData2());
        } else {
            return MXMidi.nameOfMessage(getStatus(), getData1(), getData2());
        }
    }

    public static void main(String[] args) {
        MXMessage message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_NOTEON + 0, 64, 127);
        String dtext = message._template.toDText();
        MXMessage msg = MXMessageFactory.fromTemplate(message.getPort(), new MXTemplate(dtext), message.getChannel(), message.getGate(), message.getValue());

        PrintStream output = System.out;

        output.println(message);
        output.println(dtext);
        output.println(msg);

        msg.setPort(6);
        msg.setChannel(2);
        msg.setValue(MXRangedValue.new7bit(100));

        output.println(msg);
        output.println("----------------");

        MXMessage message2 = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + 1, MXMidi.DATA1_CC_CHANNEL_VOLUME, 127);
        output.println(message2);
        String dtext2 = message2._template.toDText();
        MXMessage msg2 = MXMessageFactory.fromTemplate(message2.getPort(), new MXTemplate(dtext2), message2.getChannel(), message2.getGate(), message2.getValue());

        output.println(dtext2);
        output.println(msg2);

        msg2.setPort(6);
        msg2.setChannel(2);
        msg2.setValue(MXRangedValue.new7bit(100));

        output.println(msg2);
        output.println("----------------");
    }

    public void debugDump(String func) {
        PrintStream output = System.out;
        StringBuffer buf = new StringBuffer();
        buf.append(func + " debugDump [template = ");
        buf.append(_template.toDTextArray());
        buf.append("] bytes = [ ");
        byte[] b = getBinary();
        for (int i = 0; i < b.length; ++i) {
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
            chname = Integer.toString(channel + 1);
        } else {
            chname = "";
        }

        if (isBinaryMessage()) {
            if (_dataBytes[0] == 0xff) {
                return getMetaText();
            }
        }
        return MXUtil.dumpHex(_dataBytes);
    }

    public int getDwordCount() {
        if (_template.size() == 0) {
            return 0;
        }
        if (_template.get(0) == MXMidi.COMMAND2_CH_RPN
                || _template.get(0) == MXMidi.COMMAND2_CH_NRPN) {
            return 4;
        }
        if ((getStatus() & 0xf0) == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
            if (_progBankLSB >= 0 & _progBankMSB >= 0) {
                return 3;
            }
        }
        if (isDataentryByCC()) {
            MXVisitant visit = getVisitant();
            if (visit != null) {
                if (visit.isHavingDataentryRPN()) {
                    return 4;
                } else if (visit.isHavingDataentryNRPN()) {
                    return 4;
                }
            }
            return -1;
        }
        if (isBinaryMessage()) {
            return 0;
        }
        if (isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE) && isPairedWith14()) {
            return 2;
        }
        return 1;
    }

    public int getAsDword(int column) {
        if (isDataentryBy2()) {
            switch (column) {
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
        if ((getStatus() & 0xf0) == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
            if (_progBankLSB >= 0 & _progBankMSB >= 0) {
                if (column == 0) {
                    int status = getStatus();
                    int data1 = getData1();
                    int data2 = getData2();
                    return (status << 16) | (data1 << 8) | data2;
                } else if (column == 1) {
                    int status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
                    int data1 = MXMidi.DATA1_CC_BANKSELECT;
                    int data2 = _progBankMSB;
                    return (status << 16) | (data1 << 8) | data2;
                } else if (column == 2) {
                    int status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
                    int data1 = MXMidi.DATA1_CC_BANKSELECT + 0x20;
                    int data2 = _progBankLSB;
                    return (status << 16) | (data1 << 8) | data2;
                }
            }
        }
        if (isBinaryMessage() == false) {
            int status = getStatus();
            int data1 = getData1();
            int data2 = getData2();
            if ((getStatus() & 0xf0) == MXMidi.COMMAND_CH_CONTROLCHANGE && isPairedWith14()) {
                if (column == 0) {
                    data2 = (getValue()._value >> 7) & 0x7f;
                } else {
                    data1 += 0x20;
                    data2 = getValue()._value & 0x7f;
                }
            }
            return (status << 16) | (data1 << 8) | data2;
        }
        return 0;
    }

    public int toDataroomMSB1() {
        int status, data1, data2;
        byte[] data = createBytes();
        /* reformat */
        status = data[0] & 0xff;
        
        if (status == MXMidi.COMMAND2_CH_RPN) {
            status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_RPN_MSB;
            data2 = data[1] & 0xff;
            return (((status << 8) | data1) << 8) | data2;
        } else if (status == MXMidi.COMMAND2_CH_NRPN) {
            status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_NRPN_MSB;
            data2 = data[11] & 0xff;
            return (((status << 8) | data1) << 8) | data2;
        }
        if (getVisitant() == null) {
            MXLogger2.getLogger(MXMessage.class).log(Level.WARNING, "Visitant was NULL");
            return 0;
        }
        else {
            if (getVisitant().isHavingDataentryRPN()) {
                status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
                data1 = MXMidi.DATA1_CC_RPN_MSB;
                data2 = getVisitant().getDataroomMSB();
                return (((status << 8) | data1) << 8) | data2;
            } else if (getVisitant().isHavingDataentryNRPN()) {
                status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
                data1 = MXMidi.DATA1_CC_NRPN_MSB;
                data2 = getVisitant().getDataroomMSB();
                return (((status << 8) | data1) << 8) | data2;
            }
            MXLogger2.getLogger(MXMessage.class).warning("Incomplete  ?" + getVisitant().isIncomplemteDataentry());
            MXLogger2.getLogger(MXMessage.class).warning("Visitant has nodata  "+ getVisitant());
        }
        return 0;
    }

    public int toDataroomLSB2() {
        int status, data1, data2;
        /* reformat */
        byte[] data = createBytes();
        status = data[0] & 0xff;

        if (status == MXMidi.COMMAND2_CH_RPN) {
            status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_RPN_LSB;
            data2 = data[1] & 0xff;
            return (((status << 8) | data1) << 8) | data2;
        } else if (status == MXMidi.COMMAND2_CH_NRPN) {
            status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_NRPN_LSB;
            data2 = data[1] & 0xff;
            return (((status << 8) | data1) << 8) | data2;
        }
        if (getVisitant() == null) {
            MXLogger2.getLogger(MXMessage.class).log(Level.WARNING, "Visitant was NULL");
            return 0;
        }
        else {
            if (getVisitant().isHavingDataentryRPN()) {
                status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
                data1 = MXMidi.DATA1_CC_RPN_LSB;
                data2 = getVisitant().getDataroomLSB();
                return (((status << 8) | data1) << 8) | data2;
            } else if (getVisitant().isHavingDataentryNRPN()) {
                status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
                data1 = MXMidi.DATA1_CC_NRPN_LSB;
                data2 = getVisitant().getDataroomLSB();
                return (((status << 8) | data1) << 8) | data2;
            }
        }
        return 0;
    }

    public int toDatavalueMSB1() {
        int status, data1, data2;
        /* reformat */
        byte[] data = createBytes();
        status = data[0] & 0xff;
        if (status == MXMidi.COMMAND2_CH_RPN || status == MXMidi.COMMAND2_CH_NRPN) {
            status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_DATAENTRY;
            data2 = data[3] & 0xff;
            return (((status << 8) | data1) << 8) | data2;
        }
        if (getVisitant() == null) {
            MXLogger2.getLogger(MXMessage.class).log(Level.WARNING, "Visitant was NULL");
            return 0;
        }
        else {
            if (getVisitant().isHavingDataentryRPN() || getVisitant().isHavingDataentryNRPN()) {
                status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
                data1 = MXMidi.DATA1_CC_DATAENTRY;
                data2 = getVisitant().getDataentryMSB();
                return (((status << 8) | data1) << 8) | data2;
            }
        }
        return 0;
    }

    public int toDatavalueLSB2() {
        int status, data1, data2;
        /* reformat */
        byte[] data = createBytes();
        status = data[0] & 0xff;
        if (status == MXMidi.COMMAND2_CH_RPN || status == MXMidi.COMMAND2_CH_NRPN) {
            status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_DATAENTRY + 0x20;
            data2 = data[4];
            return (((status << 8) | data1) << 8) | data2;
        }
        if (getVisitant() == null) {
            MXLogger2.getLogger(MXMessage.class).log(Level.WARNING, "Visitant was NULL");
            return 0;
        }
        else {
            if (getVisitant().isHavingDataentryRPN() || getVisitant().isHavingDataentryNRPN()) {
                status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
                data1 = MXMidi.DATA1_CC_DATAENTRY + 0x20;
                data2 = getVisitant().getDataentryLSB();
                return (((status << 8) | data1) << 8) | data2;
            }
        }
        return 0;
    }

    public int indexOfValueHi() {
        return _template.indexOfValueHi();
    }

    public int indexOfValueLow() {
        return _template.indexOfValueLow();
    }

    public int indexOfGateLow() {
        return _template.indexOfGateLow();
    }

    public int indexOfGateHi() {
        return _template.indexOfGateHi();
    }

    public int valueOfIndex(int x) {
        byte[] data = getBinary();
        return data[x] & 0xff;
    }
    
    public MXTemplate getTemplate() {
        return _template;
    }

    public Object clone() {
        MXMessage message;
        message = new MXMessage(_port, _template, _channel, _gate, _value);

        message._timing = _timing;

        message._visitant = _visitant;
        message._pairedWith14 = _pairedWith14;
        message._mx30record = _mx30record;

        return message;
    }

    public MXMessage refillGate() {
        MXTemplate temp = _template;
        switch (temp.get(0)) {
            case MXMidi.COMMAND_CH_NOTEON: // noteon
            case MXMidi.COMMAND_CH_NOTEOFF: // noteoff
            case MXMidi.COMMAND_CH_CONTROLCHANGE: // controlchange
            case MXMidi.COMMAND_CH_POLYPRESSURE: // polyPressure
            case MXMidi.COMMAND_CH_PROGRAMCHANGE: // progrramChange
                if ((temp.get(1) & 0xff00) == 0) {
                    int[] newTemplate = new int[]{
                        temp.get(0),
                        MXMidi.CCXML_GL,
                        temp.get(2)
                    };
                    int newGate = temp.get(1);
                    MXTemplate newTemp = new MXTemplate(newTemplate);
                    MXMessage message = new MXMessage(_port, newTemp, _channel, MXRangedValue.new7bit(newGate), _value);

                    message._timing = this._timing;

                    message.setVisitant(getVisitant());
                    message.setPairedWith14(isPairedWith14());

                    return message;
                }
                break;
        }
        return null;
    }

    public String getTemplateAsText() {
        return _template.toDText();
    }

    public MXRangedValue catchValue(MXMessage message) {
        MXTemplate temp1 = getTemplate();
        MXTemplate temp2 = message.getTemplate();

        if (temp1.isEmpty() || temp2.isEmpty()) {
            return null;
        }

        if (getChannel() != message.getChannel()) {
            return null;
        }

        if (temp1 == temp2) {
            if (getGate() == message.getGate()) {
                return message.getValue();
            }
            return null;
        }

        if (temp1.size() != temp2.size()) {
            return null;
        }
        for (int i = 0; i < temp1.size(); ++i) {
            int t1 = temp1.get(i);
            int t2 = temp2.get(i);

            if (t1 == t2) {
                continue;
            }

            //value can different
            if (t1 == MXMidi.CCXML_VH || t1 == MXMidi.CCXML_VL
                    || t2 == MXMidi.CCXML_VH || t2 == MXMidi.CCXML_VL) {
                continue;
            }

            if (t1 == MXMidi.CCXML_GL || t2 == MXMidi.CCXML_GL) {
                int gate1 = getGate()._value & 0x7f;
                int gate2 = message.getGate()._value & 0x7f;

                if (t1 != MXMidi.CCXML_GL) {
                    gate1 = t1 & 0x7f;
                }
                if (t2 != MXMidi.CCXML_GL) {
                    gate2 = t2 & 0x7f;
                }
                if (gate1 != gate2) {
                    return null;
                }
            }

            if (t1 == MXMidi.CCXML_GH || t2 == MXMidi.CCXML_GH) {
                int gateHi1 = (getGate()._value >> 7) & 0x7f;
                int gateHi2 = (message.getGate()._value >> 7) % 0x7f;

                if (t1 != MXMidi.CCXML_GH) {
                    gateHi1 = t1 & 0x7f;
                }
                if (t2 != MXMidi.CCXML_GH) {
                    gateHi2 = t2 & 0x7f;
                }
                if (gateHi1 != gateHi2) {
                    return null;
                }
            }
            
            if (t1 != t2) {
                return null;
            }
        }
        
        int vh = 0, vl = 0, gh = 0, gl = 0;

        int indexVH = indexOfValueHi();
        if (indexVH >= 0) vh = message.valueOfIndex(indexVH);
        int indexVL = indexOfValueLow();
        if (indexVL >= 0) vl = message.valueOfIndex(indexVL);
        int indexGH = indexOfGateHi();
        if (indexGH >= 0) gh = message.valueOfIndex(indexGH);
        int indexGL = indexOfGateLow();
        if (indexGL >= 0) gl = message.valueOfIndex(indexGL);
        
        int newValue = (vh << 7) | vl;
        if (getValue().contains(newValue)) {
            //Rangeは考慮しない O　フォーマットに依存する
            MXRangedValue value = getValue().changeValue(newValue);
            return value;
        }
        return null;
    }

    @Override
    public int compareTo(MXMessage o) {
        if (this == o) {
            return 0;
        }

        int x = _template.compareTo(o._template);
        if (x < 0) {
            return -1;
        }
        if (x > 0) {
            return 1;
        }

        x = _port - o._port;

        if (x == 0) {
            x = _port - o._port;
            if (x == 0) {
                x = _channel - o._channel;
                if (x == 0) {
                    x = _gate.compareTo(o._gate);
                    if (x == 0) {
                        x = _value.compareTo(o._value);
                    }
                }
            }
        }
        if (x < 0) {
            return -1;
        }
        if (x > 0) {
            return 1;
        }
        return 0;
    }
}
