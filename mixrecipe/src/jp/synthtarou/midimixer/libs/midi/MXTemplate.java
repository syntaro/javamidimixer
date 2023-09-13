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

import java.util.ArrayList;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXTemplate {

    private final int[] _commands;
    private int _bytePosGate;
    private int _bytePosHiGate;
    private int _bytePosValue;
    private int _bytePosHiValue;
    private int _checksumCount;

    public static final int DTEXT_NONE = 0x100;
    public static final int DTEXT_VL = 0x200;
    public static final int DTEXT_VH = 0x300;
    public static final int DTEXT_GL = 0x400;
    public static final int DTEXT_GH = 0x500;
    public static final int DTEXT_CH = 0x600;
    public static final int DTEXT_1CH = 0x700;
    public static final int DTEXT_2CH = 0x800;
    public static final int DTEXT_3CH = 0x900;
    public static final int DTEXT_PCH = 0xA00;
    public static final int DTEXT_1RCH = 0xB00;
    public static final int DTEXT_2RCH = 0xC00;
    public static final int DTEXT_3RCH = 0xD00;
    public static final int DTEXT_4RCH = 0xE00;
    public static final int DTEXT_VF1 = 0xF00;
    public static final int DTEXT_VF2 = 0x1000;
    public static final int DTEXT_VF3 = 0x1100;
    public static final int DTEXT_VF4 = 0x1200;
    public static final int DTEXT_VPGL = 0x1300;
    public static final int DTEXT_VPGH = 0x1400;
    public static final int DTEXT_CCNUM = 0x1500;
    public static final int DTEXT_RSCTRT1 = 0x1A00;
    public static final int DTEXT_RSCTRT2 = 0x1B00;
    public static final int DTEXT_RSCTRT3 = 0x1C00;
    public static final int DTEXT_RSCTRT1P = 0x1D00;
    public static final int DTEXT_RSCTRT2P = 0x1E00;
    public static final int DTEXT_RSCTRT3P = 0x1F00;
    public static final int DTEXT_RSCTPT1 = 0x2000;
    public static final int DTEXT_RSCTPT2 = 0x2100;
    public static final int DTEXT_RSCTPT3 = 0x2200;
    public static final int DTEXT_RSCTPT1P = 0x2300;
    public static final int DTEXT_RSCTPT2P = 0x2400;
    public static final int DTEXT_RSCTPT3P = 0x2500;
    public static final int DTEXT_CHECKSUM_SET = 0x2600;
    public static final int DTEXT_CHECKSUM_START = 0x2700;

    static MXWrapList<Integer> textCommand = new MXWrapList();
    static MXWrapList<Integer> textAlias = new MXWrapList();

    public MXTemplate(int[] template) {
        _commands = template;
        initFields();
    }
    
    public int get(int pos) {
        if (_commands == null) {
            return 0;
        }
        if (pos < 0 || pos >= _commands.length) {
            return 0;
        }
        return _commands[pos];
    }

    public int size() {
        if (_commands == null) {
            return 0;
        }
        return _commands.length;
    }

    private void initFields() {
        _bytePosHiValue = -1;
        _bytePosValue = -1;
        _bytePosGate = -1;
        _bytePosHiGate = -1;
        _checksumCount = 0;

        for (int i = 0; i < _commands.length; ++i) {
            switch (_commands[i]) {
                case DTEXT_VL:
                    _bytePosValue = i;
                    break;
                case DTEXT_VH:
                    _bytePosHiValue = i;
                    break;
                case DTEXT_GL:
                    _bytePosGate = i;
                    break;
                case DTEXT_GH:
                    _bytePosHiGate = i;
                    break;
                case DTEXT_CHECKSUM_START:
                    _checksumCount++;
                    break;
            }
        }
    }

    public byte[] makeBytes(byte[] data, MXMessage message) {
        int dataLength = _commands.length - _checksumCount;

        if (data == null || data.length != dataLength) {
            data = new byte[dataLength];
        }

        if (isDataentry()) {//TODO VISITANT
            data[0] = (byte) (MXMidi.COMMAND_CH_CONTROLCHANGE + message.getChannel());
            data[1] = MXMidi.DATA1_CC_DATAENTRY;
            data[2] = 0;
        } else {
            boolean inChecksum = false;
            int sumChecksum = 0;
            int dpos = 0;

            for (int i = 0; i < _commands.length; ++i) {
                int x = _commands[i];
                if ((x & 0xff00) != 0) {
                    if (x == DTEXT_CHECKSUM_START) {
                        sumChecksum = 0;
                        inChecksum = true;
                        continue;
                    } else if (x == DTEXT_CHECKSUM_SET) {
                        sumChecksum &= 0x7f;
                        sumChecksum = 128 - sumChecksum;
                        sumChecksum &= 0x7f;
                        x = sumChecksum;
                        inChecksum = false;
                    } else {
                        try {
                            x = getDValue(x, message);
                        } catch (IllegalArgumentException e) {
                            throw e;
                        }
                    }
                }
                data[dpos++] = (byte) (x & 0xff);
                if (inChecksum) {
                    sumChecksum += x;
                }
            }

            int command = data[0] & 0xff;
            if (command >= 0x80 && command <= 0xef) {
                data[0] = (byte) ((command & 0xf0) + message.getChannel());
            }
        }
        return data;
    }

    public static int getDValue(int alias, MXMessage message) {
        int gate = message.getGate()._var;
        int value = message.getValue()._var;
        int channel = message.getChannel();

        String str = fromAlias(alias);

        switch (alias & 0xff00) {
            case DTEXT_NONE:
                return 0;
            case DTEXT_VL:
                alias = value & 0x7f;
                break;
            case DTEXT_VH:
                alias = (value >> 7) & 0x7f;
                break;
            case DTEXT_GL:
                alias = gate & 0x7f;
                break;
            case DTEXT_GH:
                alias = (gate >> 7) & 0x7f;
                break;
            case DTEXT_CH:
                alias = channel;
                break;
            case DTEXT_1CH:
                alias = 0x10 + channel;
                break;
            case DTEXT_2CH:
                alias = 0x20 + channel;
                break;
            case DTEXT_3CH:
                alias = 0x30 + channel;
                break;
            case DTEXT_PCH:
                if (message.getPort() >= 0 && message.getPort() <= 3) {
                    alias = message.getPort() * 0x10 + channel;
                } else {
                    alias = 0x30 + channel;
                }
                break;
            case DTEXT_1RCH:
            case DTEXT_2RCH:
            case DTEXT_3RCH:
            case DTEXT_4RCH:
                throw new IllegalArgumentException("1RCH, 2RCH, 3RCH, 4RCH not supported.");
            //break;
            case DTEXT_VF1:
                alias = (value) & 0x0f;
                break;
            case DTEXT_VF2:
                alias = (value >> 4) & 0x0f;
                break;
            case DTEXT_VF3:
                alias = (value >> 8) & 0x0f;
                break;
            case DTEXT_VF4:
                alias = (value >> 12) & 0x0f;
                break;
            case DTEXT_VPGL:
                alias = (value + gate) & 0x7f;
                break;
            case DTEXT_VPGH:
                alias = ((value + gate) >> 7) & 0x7f;
                break;
            case DTEXT_RSCTRT1:
            case DTEXT_RSCTRT2:
            case DTEXT_RSCTRT3:
                throw new IllegalArgumentException("RSCTRT1, RSCTRT2, RSCTRT3 not supported.");
            //break;
            case DTEXT_RSCTRT1P:
            case DTEXT_RSCTRT2P:
            case DTEXT_RSCTRT3P:
                throw new IllegalArgumentException("RSCTRT1P, RSCTRT2P, RSCTRT3P not supported.");
            //break;
            case DTEXT_RSCTPT1:
            case DTEXT_RSCTPT2:
            case DTEXT_RSCTPT3:
                throw new IllegalArgumentException("RSCTPT1, RSCTPT2, RSCTPT3 not supported.");
            //break;
            case DTEXT_RSCTPT1P:
            case DTEXT_RSCTPT2P:
            case DTEXT_RSCTPT3P:
                throw new IllegalArgumentException("RSCTPT1P, RSCTPT2P, RSCTPT3P not supported.");
            //break;
/*
static final int DTEXT_CCNUM = 0x1500;
             */
            case DTEXT_CHECKSUM_SET:
                return 0;

            case 0:
                return (byte) alias;

            default:
                boolean haveEx = false;
                throw new IllegalArgumentException("something wrong " + Integer.toHexString(alias) + " , " + fromAlias(alias));
        }
        return (byte) alias;
    }

    public static String fromType(int command) {
        int index = textCommand.indexOfValue(command);
        if (index < 0 && command == 0) {
            index = textCommand.indexOfValue(MXMidi.COMMAND2_NONE);
        }
        return textCommand.get(index).name;
    }

    public static String fromAlias(int dtext) {
        int index = textAlias.indexOfValue(dtext);
        if (index >= 0) {
            return textAlias.get(index).name;
        }
        //return MXUtil.toHexFF(dtext) + "h";
        return Integer.toHexString(dtext) + "h";
    }

    public String toDText(MXMessage message) {
        ArrayList<String> array = toDArray(message);

        StringBuffer text = new StringBuffer();
        String last = "]";
        for (String seg : array) {
            if (seg.length() == 0) {
                continue;
            }
            if (text.length() >= 0) {
                if (seg.equals("[") || seg.equals("]") || last.equals("[") || last.equals("]")) {
                    // nothing
                } else {
                    text.append(" ");
                }
            }
            last = seg;
            text.append(seg);
        }
        return text.toString();
    }

    public ArrayList<String> toDArray(MXMessage message) {
        ArrayList<String> texts = new ArrayList();

        if (_commands == null) {
            return texts;
        }

        texts.add(fromType(_commands[0]));

        if (isDataentry()) {
            MXVisitant visitant = message.getVisitant();
            if (visitant != null) {
                texts.add(fromAlias(visitant.getDataentryMSB()));
                texts.add(fromAlias(visitant.getDataentryLSB()));
                texts.add(fromAlias(message.getValue()._var & 0x7f));
                texts.add(fromAlias((message.getValue()._var >> 7) & 0x7f));
                return texts;
            } else {
                new Exception("RPN have no DATA").printStackTrace();
                return null;
            }
        }

        if (message.isMessageTypeChannel()) {
            int status = _commands[0];
            int command = status;
            int channel = message.getChannel();

            int data1 = message.getData1();
            int data2 = message.getData2();
            
            switch(command) {
                case MXMidi.COMMAND_CH_NOTEOFF:
                case MXMidi.COMMAND_CH_NOTEON:
                case MXMidi.COMMAND_CH_POLYPRESSURE:
                case MXMidi.COMMAND_CH_CONTROLCHANGE:
                case MXMidi.COMMAND_SONGPOSITION:
                    texts.add(fromAlias(_commands[1]));
                    texts.add(fromAlias(_commands[2]));
                    return texts;
                case MXMidi.COMMAND_CH_PROGRAMCHANGE:
                case MXMidi.COMMAND_CH_CHANNELPRESSURE:
                case MXMidi.COMMAND_SONGSELECT:
                case MXMidi.COMMAND_MIDITIMECODE:
                    texts.add(fromAlias(_commands[1]));
                    return texts;
                case MXMidi.COMMAND_F4:
                case MXMidi.COMMAND_F5:
                case MXMidi.COMMAND_TUNEREQUEST:
                case MXMidi.COMMAND_MIDICLOCK:
                case MXMidi.COMMAND_F9:
                case MXMidi.COMMAND_SEQSTART:
                case MXMidi.COMMAND_SEQCONTINUE:
                case MXMidi.COMMAND_SEQSTOP:
                case MXMidi.COMMAND_FD:
                case MXMidi.COMMAND_ACTIVESENSING:
                case MXMidi.COMMAND_META_OR_RESET:
                    return texts;
                case MXMidi.COMMAND_SYSEX:
                case MXMidi.COMMAND_SYSEX_END:
                    break;

                case MXMidi.COMMAND2_CH_RPN:
                case MXMidi.COMMAND2_CH_NRPN:
                    texts.add(fromAlias(_commands[1]));
                    texts.add(fromAlias(_commands[2]));
                    texts.add(fromAlias(_commands[3]));
                    texts.add(fromAlias(_commands[4]));
                    return texts;
                case MXMidi.COMMAND2_NONE:
                case MXMidi.COMMAND2_CH_PROGRAM_INC:
                case MXMidi.COMMAND2_CH_PROGRAM_DEC:
                    return texts;
                case MXMidi.COMMAND2_SYSTEM:
                case MXMidi.COMMAND2_META:
                    break;
            }
        }

        for (int i = 0; i < _commands.length; ++i) {
            int code = _commands[i];
            if (i == 0 && message.isMessageTypeChannel()) {
                code &= 0xf0;
            }
            if (code == DTEXT_CHECKSUM_START) {
                texts.add("[");
                continue;
            }
            if (code == DTEXT_CHECKSUM_SET) {
                texts.add("]");
                continue;
            }
            texts.add(fromAlias(code));
        }
        return texts;
    }

    static {
        textAlias.addNameAndValue("#NONE", DTEXT_NONE);
        textAlias.addNameAndValue("#VL", DTEXT_VL);
        textAlias.addNameAndValue("#VH", DTEXT_VH);
        textAlias.addNameAndValue("#GL", DTEXT_GL);
        textAlias.addNameAndValue("#GH", DTEXT_GH);
        textAlias.addNameAndValue("#CH", DTEXT_CH);
        textAlias.addNameAndValue("#1CH", DTEXT_1CH);
        textAlias.addNameAndValue("#2CH", DTEXT_2CH);
        textAlias.addNameAndValue("#3CH", DTEXT_3CH);
        textAlias.addNameAndValue("#PCH", DTEXT_PCH);
        textAlias.addNameAndValue("#1RCH", DTEXT_1RCH);
        textAlias.addNameAndValue("#2RCH", DTEXT_2RCH);
        textAlias.addNameAndValue("#3RCH", DTEXT_3RCH);
        textAlias.addNameAndValue("#4RCH", DTEXT_4RCH);
        textAlias.addNameAndValue("#VF1", DTEXT_VF1);
        textAlias.addNameAndValue("#VF2", DTEXT_VF2);
        textAlias.addNameAndValue("#VF3", DTEXT_VF3);
        textAlias.addNameAndValue("#VF4", DTEXT_VF4);
        textAlias.addNameAndValue("#VPGL", DTEXT_VPGL);
        textAlias.addNameAndValue("#VPGH", DTEXT_VPGH);
        textAlias.addNameAndValue("#RSCTRT1", DTEXT_RSCTRT1);
        textAlias.addNameAndValue("#RSCTRT2", DTEXT_RSCTRT2);
        textAlias.addNameAndValue("#RSCTRT3", DTEXT_RSCTRT3);
        textAlias.addNameAndValue("#RSCTRT1P", DTEXT_RSCTRT1P);
        textAlias.addNameAndValue("#RSCTRT2P", DTEXT_RSCTRT2P);
        textAlias.addNameAndValue("#RSCTRT3P", DTEXT_RSCTRT3P);
        textAlias.addNameAndValue("#RSCTPT1", DTEXT_RSCTPT1);
        textAlias.addNameAndValue("#RSCTPT2", DTEXT_RSCTPT2);
        textAlias.addNameAndValue("#RSCTPT3", DTEXT_RSCTPT3);
        textAlias.addNameAndValue("#RSCTPT1P", DTEXT_RSCTPT1P);
        textAlias.addNameAndValue("#RSCTPT2P", DTEXT_RSCTPT2P);
        textAlias.addNameAndValue("#RSCTPT3P", DTEXT_RSCTPT3P);
        textAlias.addNameAndValue("[", DTEXT_CHECKSUM_START);
        textAlias.addNameAndValue("]", DTEXT_CHECKSUM_SET);

        textCommand.addNameAndValue("@NONE", MXMidi.COMMAND2_NONE);
        textCommand.addNameAndValue("@PB", MXMidi.COMMAND_CH_PITCHWHEEL);
        textCommand.addNameAndValue("@CC", MXMidi.COMMAND_CH_CONTROLCHANGE);
        textCommand.addNameAndValue("@CP", MXMidi.COMMAND_CH_CHANNELPRESSURE);
        textCommand.addNameAndValue("@PKP", MXMidi.COMMAND_CH_POLYPRESSURE);
        textCommand.addNameAndValue("@SYSEX",MXMidi.COMMAND_SYSEX);
        textCommand.addNameAndValue("@RPN", MXMidi.COMMAND2_CH_RPN); //@RPN [RPN MSB] [RPN LSB] [Data MSB] [Data LSB]
        textCommand.addNameAndValue("@NRPN", MXMidi.COMMAND2_CH_NRPN); //@NRPN [NRPN MSB] [NRPN LSB] [Data MSB] [Data LSB]

        textCommand.addNameAndValue("@PROG_INC", MXMidi.COMMAND2_CH_PROGRAM_INC);
        textCommand.addNameAndValue("@PROG_DEC", MXMidi.COMMAND2_CH_PROGRAM_DEC);
        textCommand.addNameAndValue("@ON", MXMidi.COMMAND_CH_NOTEON);
        textCommand.addNameAndValue("@OFF", MXMidi.COMMAND_CH_NOTEOFF);
        textCommand.addNameAndValue("@PROGRAM", MXMidi.COMMAND_CH_PROGRAMCHANGE);
        textCommand.addNameAndValue("@META",  MXMidi.COMMAND_META_OR_RESET);
    }

    public static int readCommandText(String str) {
        int code = -1;
        if (str.startsWith("@")) {
            int find = textCommand.indexOfName(str);
            if (find >= 0) {
                code = textCommand.get(find).value.intValue();
                return code & 0xff00;
            }
        }
        return code;
    }

    public static int readAliasText(String str) {
        int code = -1;
        if (str.startsWith("#") || str.equals("[") || str.equals("]")) {
            int find = textAlias.indexOfName(str);
            if (find >= 0) {
                code = textAlias.get(find).value.intValue();
                return code & 0xff00;
            }
        }
        int x = MXUtil.numberFromText(str);
        if (x >= 0) {
            return x & 0xff;
        }
        return 0;
    }

    /**
     * @return the _bytePosHiValue
     */
    public int getBytePosHiValue() {
        return _bytePosHiValue;
    }

    /**
     * @return the _bytePosValue
     */
    public int getBytePosValue() {
        return _bytePosValue;
    }

    /**
     * @return the _bytePosGate
     */
    public int getBytePosGate() {
        return _bytePosGate;
    }

    /**
     * @return the _bytePosHiGate
     */
    public int getBytePosHiGate() {
        return _bytePosHiGate;
    }

    public String toString() {
        if (_commands == null) {
            return "null";
        } else {
            StringBuffer str = new StringBuffer();
            for (int i = 0; i < _commands.length; ++i) {
                int x = _commands[i];
                String seg = fromAlias(x);
                if (seg == null) {
                    seg = Integer.toHexString(x);
                }
                if (str.length() != 0) {
                    str.append(", ");
                }
                str.append(seg);
            }
            return str.toString();
        }
    }

    public boolean isDataentry() {
        if (_commands.length > 0) {
            if (_commands[0] == MXMidi.COMMAND2_CH_RPN 
             || _commands[0] == MXMidi.COMMAND2_CH_NRPN) {
                return true;
            }
        }
        return false;
    }

    public boolean canReuseBinary(byte[] data) {
        if (_commands.length - _checksumCount == data.length) {
            int dpos = 0;
            for (int tpos = 0; tpos < _commands.length; ++tpos) {
                int seek = _commands[tpos];
                if (seek == DTEXT_CHECKSUM_START) {
                    continue;
                }
                int d1 = data[dpos++] & 0xff;
                if (seek != d1) {
                    if (seek != DTEXT_VH && seek != DTEXT_VL) {
                        return false;
                    }
                    if (seek != DTEXT_CHECKSUM_SET) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean canReuseTemplate(int[] template) {
        if (_commands.length == template.length) {
            for (int i = 0; i < template.length; ++i) {
                int seek = _commands[i];
                int d1 = template[i];
                if (seek != d1) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static MXTemplate fromDword1(int dword) {
        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = dword & 0xff;
        int[] template = null;

        if (status >= 0x80 && status <= 0xef) {
            status = status & 0xf0;
            switch (status) {
                case MXMidi.COMMAND_CH_PITCHWHEEL:
                    template = new int[]{MXMidi.COMMAND_CH_PITCHWHEEL, MXTemplate.DTEXT_VL, MXTemplate.DTEXT_VH};
                    break;
                case MXMidi.COMMAND_CH_CHANNELPRESSURE:
                    template = new int[]{MXMidi.COMMAND_CH_CHANNELPRESSURE, MXTemplate.DTEXT_VL};
                    break;
                case MXMidi.COMMAND_CH_POLYPRESSURE:
                    template = new int[]{MXMidi.COMMAND_CH_POLYPRESSURE, MXTemplate.DTEXT_GL, MXTemplate.DTEXT_VL};
                    break;
                case MXMidi.COMMAND_CH_CONTROLCHANGE:
                    template = new int[]{MXMidi.COMMAND_CH_CONTROLCHANGE, MXTemplate.DTEXT_GL, MXTemplate.DTEXT_VL};
                    break;
                case MXMidi.COMMAND_CH_NOTEON:
                    template = new int[]{MXMidi.COMMAND_CH_NOTEON, MXTemplate.DTEXT_GL, MXTemplate.DTEXT_VL};
                    break;
                case MXMidi.COMMAND_CH_NOTEOFF:
                    template = new int[]{MXMidi.COMMAND_CH_NOTEOFF, MXTemplate.DTEXT_GL, MXTemplate.DTEXT_VL};
                    break;
                case MXMidi.COMMAND_CH_PROGRAMCHANGE:
                    template = new int[]{MXMidi.COMMAND_CH_PROGRAMCHANGE, MXTemplate.DTEXT_GL};
                    break;
            }
        }else {
            switch(status) {
                case MXMidi.COMMAND_SYSEX: //sysex
            }
        }

        if (template == null) {
            return null;
        }
        return new MXTemplate(template);
    }

    public static MXMessage fromDtext(int port, String text, int channel, RangedValue gate, RangedValue value) {
        if (text == null || text.length() == 0) {
            return null;
        }

        while (text.startsWith(" ")) {
            text = text.substring(1);
        }
        while (text.endsWith(" ")) {
            text = text.substring(0, text.length() - 1);
        }

        try {
            int rpn_msb;
            int rpn_lsb;
            int nrpn_msb;
            int nrpn_lsb;

            char[] line = text.toCharArray();

            char[] word = new char[line.length];
            int wx = 0;

            int readX = 0;
            ArrayList<String> separated = new ArrayList();
            boolean inChecksum = false;

            while (readX < line.length) {
                char ch = line[readX++];
                if (ch == '[') {
                    separated.add("[");
                    inChecksum = true;
                    continue;
                }
                if (ch == ']') {
                    if (inChecksum) {
                        inChecksum = false;
                        if (wx != 0) {
                            separated.add(new String(word, 0, wx));
                        }
                        separated.add("]");
                        wx = 0;
                    } else {
                        new Exception("Checksum have not opened").printStackTrace();
                    }
                    continue;
                }
                if (ch == ' ' || ch == '\t' || ch == ',') {
                    if (wx != 0) {
                        separated.add(new String(word, 0, wx));
                    }
                    wx = 0;
                    continue;
                }
                word[wx++] = ch;
            }

            if (wx != 0) {
                separated.add(new String(word, 0, wx));
                wx = 0;
            }

            if (separated.size() <= 0) {
                return null;
            }
            
            // cleanup
            int type = MXTemplate.readCommandText(separated.get(0));
            int[] compiled = new int[separated.size()];
            int cx = 0;
            int px = 0;

            for (int sx = 0; sx < separated.size(); ++sx) {
                String str = separated.get(sx);
                int code = MXTemplate.readAliasText(str);
                if (code < 0) {
                    return null;
                }
                compiled[px++] = code;
            }

            MXMessage temp = MXMessageFactory.fromTemplate(port, compiled, channel, gate, value);
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public MXMessage readDataForChGateValue(int port, byte[] data) {
        MXTemplate me = this;
        
        boolean checkMatch = false;
        MXTemplate found = null;
        
        int channel  = 0;
        RangedValue gate = RangedValue.ZERO7;
        RangedValue value = RangedValue.ZERO7;

        switch(me.get(0)) {
            case MXMidi.COMMAND2_NONE:
                if (data.length == 0) {
                    //TODO cache
                    return new MXMessage(port, me);
                }
                return null;
            case MXMidi.COMMAND2_CH_RPN:
                //TODO
                return null;
            case MXMidi.COMMAND2_CH_NRPN:
                //TODO
                return null;
            case MXMidi.COMMAND2_CH_PROGRAM_INC:
                return null;
            case MXMidi.COMMAND2_CH_PROGRAM_DEC:
                return null;
            case MXMidi.COMMAND2_META:
                checkMatch = true;
                break;
            case MXMidi.COMMAND2_SYSTEM:
                checkMatch = true;
                break;
            default: //Command1 
                checkMatch = true;
                break;
        }
        
        if (checkMatch) {
            int gateLo = -1, gateHi = -1;
            int valueLo = -1, valueHi = -1;

            int dataSeek = 0;
            int templateSeek = 0;

            while (templateSeek < me.size() && dataSeek < data.length) {
                int c1 = me.get(templateSeek);
                templateSeek ++;

                if (c1 == MXTemplate.DTEXT_CHECKSUM_START) {
                    continue;
                }
                if (c1 == MXTemplate.DTEXT_CHECKSUM_SET) {
                    //any ok
                    dataSeek ++;
                    continue;
                }
                int c2 = data[dataSeek ++] & 0xff;
                if (templateSeek == 1 && c2 >= 0x80 && c2 <= 0xef) {
                    channel = c2 & 0x0f;
                    c1 = c1 & 0xf0;
                    c2 = c2 & 0xf0;
                }
                if (c1 != c2) {
                    switch(c1) {                        
                        case MXTemplate.DTEXT_GL:
                            gateLo = c2;
                            break;
                        case MXTemplate.DTEXT_GH:
                            gateHi = c2;
                            break;
                        case MXTemplate.DTEXT_VL:
                            valueLo = c2;
                            break;
                        case MXTemplate.DTEXT_VH:
                            valueHi = c2;
                            break;
                        default:
                            if ((c1 & 0xff00) == 0) {
                                return null;
                            }
                            break;
                    }
                }
            }

            if (gateLo >= 0 || gateHi >= 0) {
                if (gateLo < 0) {
                    gateLo = 0;
                }
                if (gateHi < 0) {
                    gateHi = 0;
                    gate = RangedValue.new7bit(gateLo);
                }
                else {
                    gate = RangedValue.new14bit((gateHi << 7) | gateLo);
                }
            }
            if (valueLo >= 0 || valueHi >= 0) {
                if (valueLo < 0) {
                    valueLo = 0;
                }
                if (valueHi < 0) {
                    valueHi = 0;
                    value = RangedValue.new7bit(valueLo);
                }
                else {
                    value = RangedValue.new14bit((valueHi << 7) | valueLo);
                }
            }
            return new MXMessage(port, me, channel, gate, value);
        }
        
        return null;
    }

    byte[] dwordBuffer = new byte[3];    
    
    public synchronized MXMessage readDwordForChGateValue(int port, int dword) {
        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = dword & 0xff;
        
        dwordBuffer[0] = (byte)status;
        dwordBuffer[1] = (byte)data1;
        dwordBuffer[2] = (byte)data2;
        
        return readDataForChGateValue(port, dwordBuffer);
    }
}
