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
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.*;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.libs.midi.visitant.MXVisitant;

/**
 *
 * @author Syntarou YOSHIDA infomation from g200kg Music & Software
 * https://www.g200kg.com/
 */
public final class MXMessage implements Comparable<MXMessage>, Cloneable {

    int _progBankMSB = -1;
    int _progBankLSB = -1;

    public void setProgramBank(int msb, int lsb) {
        _progBankMSB = msb;
        _progBankLSB = lsb;
    }

    private int _port;
    private MXRangedValue _value = MXRangedValue.ZERO7;
    private MXRangedValue _gate = MXRangedValue.ZERO7;
    private int _channel = 0;
    public MXMessage _owner;
    public Throwable _trace = (MXConfiguration._DEBUG) ? new Throwable() : null;

    private OneMessage _dataBytes = null;
    private final MXTemplate _template;

    public boolean isEmpty() {
        return _template.isEmpty();
    }

    public static MXMessage getRealOwner(MXMessage msg) {
        if (msg == null) {
            return null;
        }
        MXMessage seek = msg;
        while (seek._owner != null) {
            seek = seek._owner;
            if (seek._owner == seek) {
                return seek;
            }
        }
        return seek;
    }

    public boolean isSysexOrMeta() {
        //don't loop getCompiled
        int first = _template.get(0);
        switch (first) {
            case MXMidi.COMMAND_SYSEX:
            case MXMidi.COMMAND_SYSEX_END:
            case MXMidi.COMMAND_META_OR_RESET:
                return true;

            case MXMidi.COMMAND2_CH_RPN:
            case MXMidi.COMMAND2_CH_NRPN:
            case MXMidi.COMMAND2_CH_PROGRAM_INC:
            case MXMidi.COMMAND2_CH_PROGRAM_DEC:
                return false;
        }
        return false;
    }

    public boolean isPairedWith14() {
        if (isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
            return (indexOfValueHi() >= 0);
        }
        return false;
    }

    private MXVisitant _visitant;

    public MXVisitant getVisitant() {
        return _visitant;
    }

    public void setVisitant(MXVisitant visitant) {
        _visitant = visitant;
        _dataBytes = null;
    }

    public int sizeOfTemplate() {
        return _template.size();
    }

    public int parseTemplate(int pos) {
        int x = _template.get(pos);
        try {
            if ((x & 0xff00) != 0) {
                return MXTemplate.parseDAlias(x, this);
            }
        } catch (IllegalArgumentException e) {
            //throw e;
        }
        return x;
    }

    public int getPort() {
        return _port;
    }

    public synchronized void setPort(int port) {
        if (_port != port) {
            _port = port;
            _dataBytes = null;
        }
    }

    public synchronized int getStatus() {
        return getCompiled(0);
    }

    public boolean isCommand(int command) {
        int status = getTemplate().get(0);
        if ((status & 0xf0) == command) {
            return true;
        }
        return false;
    }

    public synchronized void setChannel(int channel) {
        if (_channel != channel) {
            _dataBytes = null;
            _channel = channel;
        }
    }

    public synchronized int getCompiled(int x) {
        if (x == 0) {
            return _template.get(0);
        }
        int c = _template.get(x);
        if ((c & 0xff00) != 0) {
            return _template.parseDAlias(c, this);
        }
        return x;
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

    public synchronized boolean setValue(MXRangedValue value) {
        if (_value != null) {
            if (_value.equals(value)) {
                return false;
            }
        }
        _value = value;
        _dataBytes = null;
        return true;
    }

    public synchronized void setValue(int value) {
        if (_value._value == value) {
            return;
        }
        _value = _value.changeValue(value);
        _dataBytes = null;
    }

    public MXRangedValue getGate() {
        return _gate;
    }

    public synchronized void setGate(MXRangedValue gate) {
        if (_gate.equals(gate)) {
            return;
        }
        _gate = gate;
        _dataBytes = null;
    }

    public synchronized void setGate(int gate) {
        if (_gate._value == gate) {
            return;
        }
        _gate = _gate.changeValue(gate);
        _dataBytes = null;
    }

    public boolean isMetaMessage() {
        if (_template.size() == 0) {
            return false;
        }
        return _template.get(0) == MXMidi.COMMAND_META_OR_RESET;
    }

    public int getMetaType() {
        if (_template.size() >= 2) {
            int x = _template.get(1);
            return _template.parseDAlias(x, this);
        }
        return -1;
    }

    public String getMetaText() {
        if (isMetaMessage()) {
            String text = "";
            byte[] data = _dataBytes.getBinary();
            try {
                text = new String(data, 2, data.length - 2, "ASCII");
                text = new String(data, 2, data.length - 2, "utf8");
                text = new String(data, 2, data.length - 2, "SJIS");
            } catch (UnsupportedEncodingException ex) {
                MXFileLogger.getLogger(MXMessage.class).log(Level.WARNING, ex.getMessage(), ex);
            } catch (RuntimeException ex) {
                MXFileLogger.getLogger(MXMessage.class).log(Level.WARNING, ex.getMessage(), ex);
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
                gate = MXRangedValue.ZERO14;
            } else if (template.indexOfGateLow() >= 0) {
                gate = MXRangedValue.ZERO7;
            } else {
                gate = MXRangedValue.ZERO7;
            }
        }
        if (value == null) {
            if (template.indexOfValueHi() >= 0) {
                value = MXRangedValue.new14bit(0);
            } else if (template.indexOfValueLow() >= 0) {
                value = MXRangedValue.ZERO7;
            } else {
                value = MXRangedValue.ZERO7;
            }
        }
        _port = port;
        _template = template;
        _channel = channel;
        _gate = gate;
        _value = value;
    }

    public boolean isChannelMessage1() {
        int code = getStatus();
        if (code >= 0x80 && code <= 0xef) {
            return true;
        }
        return false;
    }

    public boolean isChannelMessage2() {
        int code = getStatus();
        if (code >= 0x80 && code <= 0xef) {
            return true;
        }
        if (code >= 0x100 && code != MXMidi.COMMAND2_NONE) {
            return true;
        }
        return false;
    }

    public ArrayList<OneMessage> listOneMessage() {
        int count = countOneMessage();
        ArrayList<OneMessage> result = new ArrayList<>();
        for (int i = 0; i < count; ++ i) {
            result.add(toOneMessage(i));
        }
        return result;
    }
    
    public String toString() {
        String str0 = toStringMessageInfo(1);
        String str1 = Character.toString((char) ('A' + _port)) + ":";
        String str2 = toStringGateValue();
        if (str2.length() > 0) {
            return str1 + "(" + str0 + "," + str2 + ")";
        }
        return str1 + "{" + str0 + "}";
    }

    public String toStringGateValue() {
        if (getTemplate().size() == 0) {
            return "";
        }
        int command = getTemplate().get(0);
        if (command == 0) {
            return "";
        }
        //
        if (isChannelMessage2()) {
            command = command & 0xfff0;
        }
        String gate = null, value = null;
        if (indexOfGateLow() >= 0 || indexOfGateHi() >= 0) {
            int num = getGate()._value;
            gate = String.valueOf(num);
            if (indexOfGateHi() >= 0) {
                gate = "(" + MXUtil.toHexFF(num >> 7) + MXUtil.toHexFF(num & 0x7f) + ")";
            } else {
                gate = "(" + MXUtil.toHexFF(num & 0x7f) + ")";
            }
        }
        if (indexOfValueLow() >= 0 || indexOfValueHi() >= 0) {
            value = String.valueOf(getValue()._value);
            if (indexOfValueHi() >= 0) {
                int vh = (getValue()._value >> 7) & 0x7f;
                int vl = getValue()._value & 0x7f;
                value += MXUtil.toHexFF(vh)  +":" + MXUtil.toHexFF(vl);
            }
        }
        if (gate == null && value == null) {
            return "";
        }
        if (gate != null && value == null) {
            return gate + "=" + value;
        }
        if (value != null) {
            return value;
        }
        return "Gate=" + gate;
    }

    public String toStringMessageInfo(int showChannel) {
        String chname = "";
        if (isChannelMessage1()) {
            int channel = getChannel();
            if (showChannel == 1) {
                chname = "" + (channel + 1) + ":";
            } else if (showChannel == 0) {
                chname = "";
            } else {
                chname = "Ch" + (channel + 1) + ":";
            }
        }
        if (_template.size() == 0) {
            return "Empty";
        }

        int status = getStatus();
        int command = status;
        if (command >= 0x80 && command <= 0xef) {
            command &= 0xf0;
        }
        if (command >= 0x100) {
            command &= 0xfff0;
        }
        if (_dataBytes != null && _dataBytes.getBinary() != null) {
            switch (status) {
                case 0xf0:
                    return "Syx." + _dataBytes.getBinary().length;
                case 0xf7:
                    return "Syx2." + _dataBytes.getBinary().length;
                case 0xff:
                    return "Meta." + _dataBytes.getBinary().length;
            }
        }
        else {
            switch (status) {
                case 0xf0:
                    return "Syx.0";
                case 0xf7:
                    return "Syx2.0";
                case 0xff:
                    return "Meta.0";
            }
        }
        if (isSysexOrMeta()) {
            return "Bin";
        }

        if (command == MXMidi.COMMAND2_CH_PITCH_MSBLSB) {
            return chname + "PITCH = " + getValue()._value;
        }
        if (command == MXMidi.COMMAND2_CH_RPN) {
            int temp1 = MXTemplate.parseDAlias(_template.get(1), this);
            int temp2 = MXTemplate.parseDAlias(_template.get(2), this);
            int gate = (temp1 << 7) | temp2;
            int val1 = MXTemplate.parseDAlias(_template.get(3), this);
            int val2 = MXTemplate.parseDAlias(_template.get(4), this);
            int value = (val1 << 7) | val2;
            return chname + "RPN MSB:LSB = " + gate + " value = " + value;
        }
        if (command == MXMidi.COMMAND2_CH_NRPN) {
            int temp1 = MXTemplate.parseDAlias(_template.get(1), this);
            int temp2 = MXTemplate.parseDAlias(_template.get(2), this);
            int gate = (temp1 << 7) | temp2;
            int val1 = MXTemplate.parseDAlias(_template.get(3), this);
            int val2 = MXTemplate.parseDAlias(_template.get(4), this);
            int value = (val1 << 7) | val2;
            return chname + "NRPN MSB:LSB = " + gate + " value = " + value;
        }
        if (command == MXMidi.COMMAND2_CH_PROGRAM_INC) {
            return chname + "PROGRAM INC";
        }
        if (command == MXMidi.COMMAND2_CH_PROGRAM_DEC) {
            return chname + "PROGRAM DEC";

        }

        if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
            int data1 = getGate()._value;
            return chname + MXMidi.nameOfControlChange(data1);
        } else if (command == MXMidi.COMMAND_CH_NOTEOFF) {
            int note = getGate()._value;
            int velocity = getValue()._value;
            return chname + MXMidi.nameOfNote(note) + "#OFF";
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
            int program = getCompiled(1);
            return chname + "Pg" + program;
        }
        if (command == MXMidi.COMMAND_CH_CHANNELPRESSURE) {
            return chname + "ChPrs";
        }
        if (command == MXMidi.COMMAND_CH_PITCHWHEEL || command == MXMidi.COMMAND2_CH_PITCH_MSBLSB) {
            return chname + "Pitch";
        }
        if (command == MXMidi.COMMAND_SONGPOSITION) {
            return chname + "Pos";
        }
        if (command == MXMidi.COMMAND_SONGSELECT) {
            return chname + "Song";
        }

        if (isChannelMessage1()) {
            return chname + MXMidi.nameOfMessage(getStatus(), getCompiled(1), getCompiled(2));
        } else {
            return MXMidi.nameOfMessage(getStatus(), getCompiled(1), getCompiled(2));
        }
    }

    public static void main(String[] args) {
        MXMessage message = MXMessageFactory.fromNoteon(0, 0, 64, 127);
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

        MXMessage message2 = MXMessageFactory.fromControlChange(0, 1, MXMidi.DATA1_CC_CHANNEL_VOLUME, 127);
        output.println(message2);
        String dtext2 = message2._template.toDText();
        MXMessage message3 = MXMessageFactory.fromTemplate(message2.getPort(), new MXTemplate(dtext2), message2.getChannel(), message2.getGate(), message2.getValue());

        output.println(dtext2);
        output.println(message3);

        message3.setPort(6);
        message3.setChannel(2);
        message3.setValue(MXRangedValue.new7bit(100));

        output.println(message3);
        output.println("----------------");
    }

    public String toStringDumped() {
        int dwcount = countOneMessage();
        StringBuilder str = new StringBuilder();
        str.append("[");
        for (int i = 0; i < dwcount; ++i) {
            if (i != 0) {
                str.append(", ");
            }
            str.append(toOneMessage(i).toString());
        }
        str.append("]");
        return str.toString();
    }

    public int countOneMessage() {
        if (_template.size() == 0) {
            return 0;
        }
        int command = getStatus() & 0xfff0;
        if (command == MXMidi.COMMAND2_CH_RPN || command == MXMidi.COMMAND2_CH_NRPN) {
            if (indexOfValueHi() >= 0) {
                return 4;
            }
            return 3;
        }
        if (command == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
            if (_progBankLSB >= 0 && _progBankMSB >= 0) {
                return 3;
            }
        }
        if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
            if (indexOfValueHi() >= 0) {
                return 2;
            }
            return 1;
        }
        if (isSysexOrMeta()) {
            return 1;
        }
        if (isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE) && isPairedWith14()) {
            return 2;
        }
        return 1;
    }

    public OneMessage toOneMessage(int column) {
        int command = getStatus();
        if ((command >= 0x80 && command <= 0xef) || command >= 0x100) {
            command = command & 0xfff0;
        }
        int status = (command | getChannel()) & 0xff;
        int data1 = getCompiled(1) & 0x7f;
        int data2 = getCompiled(2) & 0x7f;
        if (command == MXMidi.COMMAND2_CH_RPN || command == MXMidi.COMMAND2_CH_NRPN) {
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
            return null;
        }
        if (command == MXMidi.COMMAND2_CH_PITCH_MSBLSB) {
            MXMessage newMessage = MXMessageFactory.fromTemplate(_port, MXMidi.TEMPLATE_PITCH, _channel, _gate, _value);
            return newMessage.toOneMessage(column);
        }
        if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
            if (indexOfValueHi() >= 0) {
                if (column == 0) {
                    data2 = MXTemplate.parseDAlias(getTemplate().get(2), this);
                    return OneMessage.thisCodes(0, status, data1, data2);
                } else {
                    data1 = data1 + 0x20;
                    data2 = MXTemplate.parseDAlias(getTemplate().get(3), this);
                    return OneMessage.thisCodes(0, status, data1, data2);
                }
            }
            data2 = getValue()._value & 0x7f;
            return OneMessage.thisCodes(0, status, data1, data2);
        }
        if (command == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
            if (_progBankLSB >= 0 & _progBankMSB >= 0) {
                if (column == 0) {
                    return OneMessage.thisCodes(0, status, data1, data2);
                } else if (column == 1) {
                    int status2 = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
                    data1 = MXMidi.DATA1_CC_BANKSELECT;
                    data2 = _progBankMSB;
                    return OneMessage.thisCodes(0, status2, data1, data2);
                } else if (column == 2) {
                    int status2 = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
                    data1 = MXMidi.DATA1_CC_BANKSELECT + 0x20;
                    data2 = _progBankLSB;
                    return OneMessage.thisCodes(0, status2, data1, data2);
                }
            }
        }
        if (command == MXMidi.COMMAND_SYSEX) {
            return _template.makeBytes(this);
        }
        return OneMessage.thisCodes(0, status, data1, data2);
    }

    public OneMessage toDataroomMSB1() {
        int status, data1, data2;
        status = getCompiled(0);

        if (status == MXMidi.COMMAND2_CH_RPN) {
            status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_RPN_MSB;
            data2 = MXTemplate.parseDAlias(getTemplate().get(1), this);
            return OneMessage.thisCodes(0, status, data1, data2);
        } else if (status == MXMidi.COMMAND2_CH_NRPN) {
            status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_NRPN_MSB;
            data2 = MXTemplate.parseDAlias(getTemplate().get(1), this);
            return OneMessage.thisCodes(0, status, data1, data2);
        }
        MXFileLogger.getLogger(MXMessage.class).severe("Invalid Entrane?");
        return null;
    }

    public OneMessage toDataroomLSB2() {
        int status, data1, data2;
        status = getCompiled(0);

        if (status == MXMidi.COMMAND2_CH_RPN) {
            status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_RPN_LSB;
            data2 = MXTemplate.parseDAlias(getTemplate().get(2), this);
            return OneMessage.thisCodes(0, status, data1, data2);
        } else if (status == MXMidi.COMMAND2_CH_NRPN) {
            status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_NRPN_LSB;
            data2 = MXTemplate.parseDAlias(getTemplate().get(2), this);
            return OneMessage.thisCodes(0, status, data1, data2);
        }
        MXFileLogger.getLogger(MXMessage.class).severe("Invalid Entrane?");
        return null;
    }

    public OneMessage toDatavalueMSB1() {
        int status, data1, data2;
        status = getCompiled(0);
        if (status == MXMidi.COMMAND2_CH_RPN || status == MXMidi.COMMAND2_CH_NRPN) {
            status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_DATAENTRY;
            if (getTemplate().get(3) < 0) {
                return null;
            }
            data2 = MXTemplate.parseDAlias(getTemplate().get(3), this);
            return OneMessage.thisCodes(0, status, data1, data2);
        }
        MXFileLogger.getLogger(MXMessage.class).severe("Invalid Entrane?");
        return null;
    }

    public OneMessage toDatavalueLSB2() {
        int status, data1, data2;
        status = getCompiled(0);
        if (status == MXMidi.COMMAND2_CH_RPN || status == MXMidi.COMMAND2_CH_NRPN) {
            status = MXMidi.COMMAND_CH_CONTROLCHANGE + getChannel();
            data1 = MXMidi.DATA1_CC_DATAENTRY + 0x20;
            if (getTemplate().get(4) < 0) {
                return null;
            }
            data2 = MXTemplate.parseDAlias(getTemplate().get(4), this);
            return OneMessage.thisCodes(0, status, data1, data2);
        }
        MXFileLogger.getLogger(MXMessage.class).severe("Invalid Entrane?");
        return null;
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

    public MXTemplate getTemplate() {
        return _template;
    }

    @Override
    public Object clone() {
        MXMessage message;
        message = new MXMessage(_port, _template, _channel, _gate, _value);

        message._visitant = _visitant;

        return message;
    }

    public MXMessage refillGate() {
        MXTemplate temp = _template;
        int command = temp.get(0) & 0xff80;
        int channel = temp.get(0) & 0x0f;

        switch (temp.get(0)) {
            case MXMidi.COMMAND_CH_NOTEON: // noteon
            case MXMidi.COMMAND_CH_NOTEOFF: // noteoff
            case MXMidi.COMMAND_CH_CONTROLCHANGE: // controlchange
            case MXMidi.COMMAND_CH_POLYPRESSURE: // polyPressure
            case MXMidi.COMMAND_CH_PROGRAMCHANGE: // progrramChange
                if ((temp.get(1) & 0xff00) == 0) {
                    int[] newTemplate = new int[]{
                        command,
                        MXMidi.CCXML_GL,
                        temp.get(2)
                    };
                    int newGate = temp.get(1);
                    MXTemplate newTemp = new MXTemplate(newTemplate);
                    MXMessage message = new MXMessage(_port, newTemp, _channel, MXRangedValue.new7bit(newGate), _value);
                    message.setVisitant(getVisitant());
                    message.setChannel(getChannel());

                    return message;
                }
                break;
        }
        return null;
    }

    public String getTemplateAsText() {
        return _template.toDText();
    }

    public MXRangedValue catchValue(MXMessage catchTarget) {
        MXTemplate temp1 = getTemplate();
        MXTemplate temp2 = catchTarget.getTemplate();

        if (temp1.isEmpty() || temp2.isEmpty()) {
            return null;
        }

        if (getChannel() != catchTarget.getChannel()) {
            return null;
        }

        if (temp1 == temp2) {
            if (getGate() == catchTarget.getGate()) {
                return catchTarget.getValue();
            }
            return null;
        }

        int t1 = temp1.get(0);
        int t2 = temp2.get(0);
        if (t1 == MXMidi.COMMAND_CH_CONTROLCHANGE
                && t2 == MXMidi.COMMAND_CH_CONTROLCHANGE) {
            int gate1 = MXTemplate.parseDAlias(temp1.get(1), this);
            int gate2 = MXTemplate.parseDAlias(temp2.get(1), catchTarget);
            if (gate1 == gate2) {
                return catchTarget.getValue();
            }
        }
        if (t1 == MXMidi.COMMAND2_CH_PITCH_MSBLSB || t1 == MXMidi.COMMAND_CH_PITCHWHEEL) {
            if (t2 == MXMidi.COMMAND2_CH_PITCH_MSBLSB || t2 == MXMidi.COMMAND_CH_PITCHWHEEL) {
                return catchTarget.getValue();
            }
        }

        if (temp1.size() != temp2.size()) {
            return null;
        }
        for (int i = 0; i < temp1.size(); ++i) {
            t1 = temp1.get(i);
            t2 = temp2.get(i);

            if (t1 == t2) {
                continue;
            }

            if (t2 <= 0) {
                t2 = 0;
            }

            //value can different
            if (t1 == MXMidi.CCXML_VH || t1 == MXMidi.CCXML_VL
                    || t2 == MXMidi.CCXML_VH || t2 == MXMidi.CCXML_VL) {
                continue;
            }
            if (t1 == MXMidi.CCXML_NONE || t2 == MXMidi.CCXML_NONE) {
                continue;
            }

            if (t1 == MXMidi.CCXML_GL || t2 == MXMidi.CCXML_GL) {
                int gate1 = getGate()._value & 0x7f;
                int gate2 = catchTarget.getGate()._value & 0x7f;

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
                int gateHi2 = (catchTarget.getGate()._value >> 7) % 0x7f;

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

        int vh = 0, vl = 0;

        int indexVL = indexOfValueLow();
        if (indexVL >= 0) {
            vl = catchTarget.parseTemplate(indexVL);
            if (vl >= 0x80) {
                vl = 0;
            }
        }

        int indexVH = indexOfValueHi();
        if (indexVH >= 0) {
            vh = catchTarget.parseTemplate(indexVH);
            if (vh >= 0x80) {
                vh = 0;
            }
        }
        MXRangedValue range = vh >= 0 ? MXRangedValue.new14bit((vh << 7) | vl) : MXRangedValue.new7bit(vl);

        boolean baseHave14 = this.indexOfValueHi() >= 0;
        boolean visitHave14 = catchTarget.indexOfValueHi() >= 0;

        if (baseHave14 != visitHave14) {
            if (baseHave14) {
                range = range.changeRange(0, 128 * 128 - 1);
            } else {
                range = range.changeRange(0, 128 - 1);
            }
        }
        if (getValue().contains(range._value)) {
            return range;
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
            x = _channel - o._channel;
            if (x == 0) {
                x = _gate.compareTo(o._gate);
                if (x == 0) {
                    x = _value.compareTo(o._value);
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
