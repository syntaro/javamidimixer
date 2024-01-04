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
import java.util.IllegalFormatException;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXTemplate implements Comparable<MXTemplate> {

    public static class CheckSumInfo {

        int _from;
        int _to;
    }

    private int[] _commands;
    private int _indexOfGateLow;
    private int _indexOfGateHi;
    private int _indexOfValueLow;
    private int _indexOfValueHi;
    private ArrayList<CheckSumInfo> _listChecksum;

    static MXWrapList<Integer> textCommand = new MXWrapList();
    static MXWrapList<Integer> textAlias = new MXWrapList();

    public MXTemplate(int[] template) {
        if (template == null || template.length == 0 || template[0] == 0) {
            initFields(null);
        } else {
            initFields(template);
        }
    }
    
    public int getLengthWithChecksum() {
        int len = 0;
        if (_commands != null) {
            len = _commands.length;
        }
        if (_listChecksum != null) {
            len += _listChecksum.size();
        }
        return len;
    }

    public boolean isEmpty() {
        if (_commands[0] == 0) {
            return true;
        }
        return false;
    }

    public MXTemplate(String text) throws IllegalFormatException {
        while (text.startsWith(" ")) {
            text = text.substring(1);
        }
        while (text.endsWith(" ")) {
            text = text.substring(0, text.length() - 1);
        }

        if (text == null || text.length() == 0) {
            initFields(null);
            return;
        }

        while (text.startsWith(" ")) {
            text = text.substring(1);
        }
        while (text.endsWith(" ")) {
            text = text.substring(0, text.length() - 1);
        }

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
                    throw new IllegalArgumentException("Checksum not opened" + text);
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
            initFields(null);
            return;
        }

        // cleanup
        int[] compiled = new int[separated.size()];
        int cx = 0;
        int px = 0;

        for (int sx = 0; sx < separated.size(); ++sx) {
            String str = separated.get(sx);
            int code;
            if (str.startsWith("@")) {
                code = MXTemplate.readCommandText(str);
                if (code < 0) {
                    throw new IllegalArgumentException("can't parse " + str);
                }
            } else if (str.startsWith("#")) {
                code = MXTemplate.readAliasText(str);
                if (code < 0) {
                    throw new IllegalArgumentException("can't parse " + str);
                }
            } else if (str.equals("[") || str.equals("]")) {
                code = MXTemplate.readAliasText(str);
            } else {
                code = MXUtil.numberFromText(str);
            }
            compiled[px++] = code;
        }

        initFields(compiled);
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

    private void initFields(int[] template) {
        _indexOfValueHi = -1;
        _indexOfValueLow = -1;
        _indexOfGateLow = -1;
        _indexOfGateHi = -1;
        if (template == null) {
            template = new int[]{0, 0, 0};
        }
        _commands = template;
        _listChecksum = null;

        int countChecksumStart = 0;
        int countChecksumEnd = 0;
        for (int i = 0; i < _commands.length; ++i) {
            switch (_commands[i]) {
                case MXMidi.CCXML_CHECKSUM_START:
                    countChecksumStart++;
                    break;
                case MXMidi.CCXML_CHECKSUM_END:
                    countChecksumEnd++;
                    break;
            }
        }

        if (countChecksumStart != 0) {
            if (countChecksumStart != countChecksumEnd) {
                //will process on next chance
            }
            int[] writeBuffer = new int[template.length - countChecksumStart];
            int writePos = 0;
            CheckSumInfo checksum = null;

            for (int i = 0; i < _commands.length; ++i) {
                int x = _commands[i];
                if (x == MXMidi.CCXML_CHECKSUM_START) {
                    if (checksum != null) {
                        throw new IllegalArgumentException("Checksum stasrt without past end");
                    }
                    checksum = new CheckSumInfo();
                    checksum._from = writePos;
                     if (_listChecksum == null) {
                        _listChecksum = new ArrayList<>();
                    }
                    _listChecksum.add(checksum);
                } else if (x == MXMidi.CCXML_CHECKSUM_END) {
                    if (checksum == null) {
                        throw new IllegalArgumentException("Checksum stasrt without past end");
                    }
                    checksum._to = writePos;
                    if (checksum._from == checksum._to) {
                        throw new IllegalArgumentException("Checksum start and end without span");
                    }
                    writeBuffer[writePos++] = x;
                    checksum = null;
                } else {
                    writeBuffer[writePos++] = x;
                }
            }
            if (checksum != null) {
                throw new IllegalArgumentException("Checksum not closed");
            }
            _commands = writeBuffer;
        }

        for (int i = 0; i < _commands.length; ++i) {
            int x = _commands[i];
            switch (_commands[i]) {
                case MXMidi.CCXML_VL:
                    _indexOfValueLow = i;
                    break;
                case MXMidi.CCXML_VH:
                    _indexOfValueHi = i;
                    break;
                case MXMidi.CCXML_GL:
                    _indexOfGateLow = i;
                    break;
                case MXMidi.CCXML_GH:
                    _indexOfGateHi = i;
                    break;
            }
        }
    }

    byte[] makeBytes(byte[] data, MXMessage message) {
        int dataLength = _commands.length;

        if (data == null || data.length != dataLength) {
            data = new byte[dataLength];
        }

        if ((_commands[0] & 0xff00) != 0) {
            return null;
        }
        int dpos = 0;

        for (int i = 0; i < _commands.length; ++i) {
            int x = _commands[i];
            try {
                x = parseDAlias(x, message);
            } catch (IllegalArgumentException e) {
                throw e;
            }
            data[dpos++] = (byte) (x & 0xff);
        }
        if (data.length >= 3
         && (data[0] & 0xff) == MXMidi.COMMAND_SYSEX
         && ((data[1] & 0xff) == MXMidi.COMMAND_SYSEX || (data[1] & 0xff) == MXMidi.COMMAND_SYSEX_END)) {
            dataLength --;
            byte []copyData = new byte[dataLength];
            for (int i = 0; i < copyData.length; ++ i) {
                copyData[i] = data[i + 1];
            }            
            data = copyData;
        }
        if (_listChecksum != null) {
            for (CheckSumInfo seek : _listChecksum) {
                int sumChecksum = 0;
                for (int x = seek._from; x <= seek._to - 1; ++x) {
                    sumChecksum += data[x] & 0x7f;
                }
                sumChecksum &= 0x7f;
                sumChecksum = 128 - sumChecksum;
                sumChecksum &= 0x7f;

                data[seek._to] = (byte) sumChecksum;
            }
        }
        int command = data[0] & 0xff;
        if (command >= 0x80 && command <= 0xef) {
            data[0] = (byte) ((command & 0xf0) + message.getChannel());
        }
        return data;
    }

    static int parseDAlias(int alias, MXMessage message) {
        int gate = message.getGate()._value;
        int value = message.getValue()._value;
        int channel = message.getChannel();

        String str = fromAliasText(alias);

        switch (alias & 0xff00) {
            case MXMidi.CCXML_NONE:
                return 0;
            case MXMidi.CCXML_VL:
                alias = value & 0x7f;
                break;
            case MXMidi.CCXML_VH:
                alias = (value >> 7) & 0x7f;
                break;
            case MXMidi.CCXML_GL:
                alias = gate & 0x7f;
                break;
            case MXMidi.CCXML_GH:
                alias = (gate >> 7) & 0x7f;
                break;
            case MXMidi.CCXML_CH:
                alias = channel;
                break;
            case MXMidi.CCXML_1CH:
                alias = 0x10 + channel;
                break;
            case MXMidi.CCXML_2CH:
                alias = 0x20 + channel;
                break;
            case MXMidi.CCXML_3CH:
                alias = 0x30 + channel;
                break;
            case MXMidi.CCXML_PCH:
                if (message.getPort() >= 0 && message.getPort() <= 3) {
                    alias = message.getPort() * 0x10 + channel;
                } else {
                    alias = 0x30 + channel;
                }
                break;
            case MXMidi.CCXML_1RCH:
            case MXMidi.CCXML_2RCH:
            case MXMidi.CCXML_3RCH:
            case MXMidi.CCXML_4RCH:
                throw new IllegalArgumentException("1RCH, 2RCH, 3RCH, 4RCH not supported.");
            //break;
            case MXMidi.CCXML_VF1:
                alias = (value) & 0x0f;
                break;
            case MXMidi.CCXML_VF2:
                alias = (value >> 4) & 0x0f;
                break;
            case MXMidi.CCXML_VF3:
                alias = (value >> 8) & 0x0f;
                break;
            case MXMidi.CCXML_VF4:
                alias = (value >> 12) & 0x0f;
                break;
            case MXMidi.CCXML_VPGL:
                alias = (value + gate) & 0x7f;
                break;
            case MXMidi.CCXML_VPGH:
                alias = ((value + gate) >> 7) & 0x7f;
                break;
            case MXMidi.CCXML_RSCTRT1:
            case MXMidi.CCXML_RSCTRT2:
            case MXMidi.CCXML_RSCTRT3:
                throw new IllegalArgumentException("RSCTRT1, RSCTRT2, RSCTRT3 not supported.");
            //break;
            case MXMidi.CCXML_RSCTRT1P:
            case MXMidi.CCXML_RSCTRT2P:
            case MXMidi.CCXML_RSCTRT3P:
                throw new IllegalArgumentException("RSCTRT1P, RSCTRT2P, RSCTRT3P not supported.");
            //break;
            case MXMidi.CCXML_RSCTPT1:
            case MXMidi.CCXML_RSCTPT2:
            case MXMidi.CCXML_RSCTPT3:
                throw new IllegalArgumentException("RSCTPT1, RSCTPT2, RSCTPT3 not supported.");
            //break;
            case MXMidi.CCXML_RSCTPT1P:
            case MXMidi.CCXML_RSCTPT2P:
            case MXMidi.CCXML_RSCTPT3P:
                throw new IllegalArgumentException("RSCTPT1P, RSCTPT2P, RSCTPT3P not supported.");

            case MXMidi.CCXML_CHECKSUM_START:
                return 0;
            case MXMidi.CCXML_CHECKSUM_END:
                return 0;

            case 0:
                return (byte) alias;

            default:
                boolean haveEx = false;
                throw new IllegalArgumentException("something wrong " + Integer.toHexString(alias) + " , " + fromAliasText(alias));
        }
        return (byte) alias;
    }

    public static String fromTypeText(int code) {
        int index = textCommand.indexOfValue(code);
        if (index >= 0) {
            return textCommand.get(index)._name;
        }
        return Integer.toHexString(code) + "h";
    }
    
    public static String fromAliasText(int code) {
        int index = textAlias.indexOfValue(code);
        if (index >= 0) {
            return textAlias.get(index)._name;
        }
        return Integer.toHexString(code) + "h";
    }


    public String toDText() {
        ArrayList<String> array = toDTextArray();

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
    
    public int[] toIntArray() {
        if (_commands == null) {
            return null;
        }
        
        int code0 = _commands[0];
        int index = textCommand.indexOfValue(code0);
        int[] data = new int[getLengthWithChecksum()];
        int wrote = 0;
        
        int status = 0;
        String name;
        if (index >= 0) {
            status = textCommand.get(index)._value;
            name = textCommand.get(index)._name;
            data[wrote++] = status;

            switch(status) {
                case MXMidi.COMMAND_CH_NOTEON:
                case MXMidi.COMMAND_CH_NOTEOFF:
                case MXMidi.COMMAND_CH_POLYPRESSURE:
                case MXMidi.COMMAND_CH_CONTROLCHANGE:
                case MXMidi.COMMAND_SONGPOSITION:
                    data[wrote++] = _commands[1];
                    data[wrote++] = _commands[2];
                    return data;

                case MXMidi.COMMAND_CH_PROGRAMCHANGE:
                case MXMidi.COMMAND_CH_CHANNELPRESSURE:
                case MXMidi.COMMAND_SONGSELECT:
                case MXMidi.COMMAND_MIDITIMECODE:
                    data[wrote++] = _commands[1];
                    return data;

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
                    return data;

                case MXMidi.COMMAND_SYSEX:
                case MXMidi.COMMAND_SYSEX_END:
                    break;

                case MXMidi.COMMAND2_CH_RPN:
                case MXMidi.COMMAND2_CH_NRPN:
                    data[wrote++] = _commands[1];
                    data[wrote++] = _commands[2];
                    data[wrote++] = _commands[3];
                    data[wrote++] = _commands[4];
                    return data;

                case MXMidi.COMMAND2_NONE:
                case MXMidi.COMMAND2_CH_PROGRAM_INC:
                case MXMidi.COMMAND2_CH_PROGRAM_DEC:
                    return data;
                    
                case MXMidi.COMMAND2_SYSTEM:
                case MXMidi.COMMAND2_META:
                    break;

                case MXMidi.COMMAND_CH_PITCHWHEEL:
                    data[wrote ++] = MXMidi.CCXML_VH;
                    data[wrote ++] = MXMidi.CCXML_VL;
                    return data;
            }
        }

        int[] sumHead = null;
        if (_listChecksum != null) {
            sumHead = new int[_listChecksum.size()];
            for (int x = 0; x < _listChecksum.size(); ++ x) {
                sumHead[x] = _listChecksum.get(x)._from;
            }
        }
        int seekHead = 0;
        wrote = 0;
        
        for (int i = 0; i < _commands.length; ++i) {
            int code = _commands[i];
            if (sumHead != null && seekHead < sumHead.length && sumHead[seekHead] == wrote) {
                data[wrote++] = MXMidi.CCXML_CHECKSUM_START;
                seekHead ++;
            }

            if (i == 0) {
                int int1 = _commands[0];
                data[wrote++]  = int1;
                continue;
            }

            if (i == 0 && code >= 0x80 && code <= 0xef) {
                code &= 0xf0;
            }
            if (code == MXMidi.CCXML_CHECKSUM_END) {
                data[wrote ++] = MXMidi.CCXML_CHECKSUM_END;
                continue;
            }
            data[wrote ++] = code;
        }
        return data;
    }
    public ArrayList<String> toDTextArray() {
        int[] data = toIntArray();
        ArrayList<String> texts = new ArrayList();
        boolean first = true;
        
        for (int seek : data) {
            if (first) {
                texts.add(fromTypeText(seek));
                first = false;
            }else {
                texts.add(fromAliasText(seek));
            }
        }
        
        return texts;
    }

    static {
        textAlias.addNameAndValue("#NONE", MXMidi.CCXML_NONE);
        textAlias.addNameAndValue("#VL", MXMidi.CCXML_VL);
        textAlias.addNameAndValue("#VH", MXMidi.CCXML_VH);
        textAlias.addNameAndValue("#GL", MXMidi.CCXML_GL);
        textAlias.addNameAndValue("#GH", MXMidi.CCXML_GH);
        textAlias.addNameAndValue("#CH", MXMidi.CCXML_CH);
        textAlias.addNameAndValue("#1CH", MXMidi.CCXML_1CH);
        textAlias.addNameAndValue("#2CH", MXMidi.CCXML_2CH);
        textAlias.addNameAndValue("#3CH", MXMidi.CCXML_3CH);
        textAlias.addNameAndValue("#PCH", MXMidi.CCXML_PCH);
        textAlias.addNameAndValue("#1RCH", MXMidi.CCXML_1RCH);
        textAlias.addNameAndValue("#2RCH", MXMidi.CCXML_2RCH);
        textAlias.addNameAndValue("#3RCH", MXMidi.CCXML_3RCH);
        textAlias.addNameAndValue("#4RCH", MXMidi.CCXML_4RCH);
        textAlias.addNameAndValue("#VF1", MXMidi.CCXML_VF1);
        textAlias.addNameAndValue("#VF2", MXMidi.CCXML_VF2);
        textAlias.addNameAndValue("#VF3", MXMidi.CCXML_VF3);
        textAlias.addNameAndValue("#VF4", MXMidi.CCXML_VF4);
        textAlias.addNameAndValue("#VPGL", MXMidi.CCXML_VPGL);
        textAlias.addNameAndValue("#VPGH", MXMidi.CCXML_VPGH);
        textAlias.addNameAndValue("#RSCTRT1", MXMidi.CCXML_RSCTRT1);
        textAlias.addNameAndValue("#RSCTRT2", MXMidi.CCXML_RSCTRT2);
        textAlias.addNameAndValue("#RSCTRT3", MXMidi.CCXML_RSCTRT3);
        textAlias.addNameAndValue("#RSCTRT1P", MXMidi.CCXML_RSCTRT1P);
        textAlias.addNameAndValue("#RSCTRT2P", MXMidi.CCXML_RSCTRT2P);
        textAlias.addNameAndValue("#RSCTRT3P", MXMidi.CCXML_RSCTRT3P);
        textAlias.addNameAndValue("#RSCTPT1", MXMidi.CCXML_RSCTPT1);
        textAlias.addNameAndValue("#RSCTPT2", MXMidi.CCXML_RSCTPT2);
        textAlias.addNameAndValue("#RSCTPT3", MXMidi.CCXML_RSCTPT3);
        textAlias.addNameAndValue("#RSCTPT1P", MXMidi.CCXML_RSCTPT1P);
        textAlias.addNameAndValue("#RSCTPT2P", MXMidi.CCXML_RSCTPT2P);
        textAlias.addNameAndValue("#RSCTPT3P", MXMidi.CCXML_RSCTPT3P);
        textAlias.addNameAndValue("[", MXMidi.CCXML_CHECKSUM_START);
        textAlias.addNameAndValue("]", MXMidi.CCXML_CHECKSUM_END);

        textCommand.addNameAndValue("@NONE", MXMidi.COMMAND2_NONE);
        textCommand.addNameAndValue("@PB", MXMidi.COMMAND_CH_PITCHWHEEL);
        textCommand.addNameAndValue("@CC", MXMidi.COMMAND_CH_CONTROLCHANGE);
        textCommand.addNameAndValue("@CP", MXMidi.COMMAND_CH_CHANNELPRESSURE);
        textCommand.addNameAndValue("@PKP", MXMidi.COMMAND_CH_POLYPRESSURE);
        textCommand.addNameAndValue("@SYSEX", MXMidi.COMMAND_SYSEX);
        textCommand.addNameAndValue("@RPN", MXMidi.COMMAND2_CH_RPN); //@RPN [RPN MSB] [RPN LSB] [Data MSB] [Data LSB]
        textCommand.addNameAndValue("@NRPN", MXMidi.COMMAND2_CH_NRPN); //@NRPN [NRPN MSB] [NRPN LSB] [Data MSB] [Data LSB]

        textCommand.addNameAndValue("@PROG_INC", MXMidi.COMMAND2_CH_PROGRAM_INC);
        textCommand.addNameAndValue("@PROG_DEC", MXMidi.COMMAND2_CH_PROGRAM_DEC);
        textCommand.addNameAndValue("@ON", MXMidi.COMMAND_CH_NOTEON);
        textCommand.addNameAndValue("@OFF", MXMidi.COMMAND_CH_NOTEOFF);
        textCommand.addNameAndValue("@PROGRAM", MXMidi.COMMAND_CH_PROGRAMCHANGE);
        textCommand.addNameAndValue("@META", MXMidi.COMMAND_META_OR_RESET);
    }

    public static int readCommandText(String str) {
        if (str.startsWith("@")) {
            int find = textCommand.indexOfName(str);
            if (find < 0) {
                return -1;
            }

            int code = textCommand.get(find)._value.intValue();
            return code;
        }
        return -1;
    }

    public static int readAliasText(String str) {
        if (str.startsWith("#") || str.equals("[") || str.equals("]")) {
            int find = textAlias.indexOfName(str);
            if (find < 0) {
                return -1;
            }
            int code = textAlias.get(find)._value.intValue();
            return code;
        }
        int x = MXUtil.numberFromText(str);
        if (x >= 0) {
            return x & 0xff;
        }
        return -1;
    }

    public int indexOfValueLow() {
        return _indexOfValueLow;
    }

    public int indexOfValueHi() {
        return _indexOfValueHi;
    }

    public int indexOfGateLow() {
        return _indexOfGateLow;
    }

    public int indexOfGateHi() {
        return _indexOfGateHi;
    }

    public String toString() {
        if (_commands == null) {
            return "null";
        } else {
            StringBuffer str = new StringBuffer();
            for (int i = 0; i < _commands.length; ++i) {
                int x = _commands[i];
                String seg = fromAliasText(x);
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

    public static MXTemplate fromDword1(int dword) {
        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = dword & 0xff;
        int[] template = null;

        if (status >= 0x80 && status <= 0xef) {
            status = status & 0xf0;
            switch (status) {
                case MXMidi.COMMAND_CH_PITCHWHEEL:
                    template = new int[]{MXMidi.COMMAND_CH_PITCHWHEEL, MXMidi.CCXML_VL, MXMidi.CCXML_VH};
                    break;
                case MXMidi.COMMAND_CH_CHANNELPRESSURE:
                    template = new int[]{MXMidi.COMMAND_CH_CHANNELPRESSURE, MXMidi.CCXML_VL};
                    break;
                case MXMidi.COMMAND_CH_POLYPRESSURE:
                    template = new int[]{MXMidi.COMMAND_CH_POLYPRESSURE, MXMidi.CCXML_GL, MXMidi.CCXML_VL};
                    break;
                case MXMidi.COMMAND_CH_CONTROLCHANGE:
                    template = new int[]{MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.CCXML_GL, MXMidi.CCXML_VL};
                    break;
                case MXMidi.COMMAND_CH_NOTEON:
                    template = new int[]{MXMidi.COMMAND_CH_NOTEON, MXMidi.CCXML_GL, MXMidi.CCXML_VL};
                    break;
                case MXMidi.COMMAND_CH_NOTEOFF:
                    template = new int[]{MXMidi.COMMAND_CH_NOTEOFF, MXMidi.CCXML_GL, MXMidi.CCXML_VL};
                    break;
                case MXMidi.COMMAND_CH_PROGRAMCHANGE:
                    template = new int[]{MXMidi.COMMAND_CH_PROGRAMCHANGE, MXMidi.CCXML_GL};
                    break;
            }
        } else {
            switch (status) {
                case MXMidi.COMMAND_SYSEX: //sysex
            }
        }

        if (template == null) {
            return null;
        }
        return new MXTemplate(template);
    }

    /**
     * dataを読み込んでメッセージ型に、ch, gate, valueをセットして返す
     * @param port
     * @param data
     * @return 
     */
    public MXMessage readDataForChGateValue(int port, byte[] data) {
        boolean checkMatch = false;
        MXTemplate found = null;

        int channel = 0;
        MXRangedValue gate = MXRangedValue.ZERO7;
        MXRangedValue value = MXRangedValue.ZERO7;

        switch (get(0)) {
            case MXMidi.COMMAND2_NONE:
                if (data.length == 0) {
                    return new MXMessage(port, this);
                }
                return null;
            case MXMidi.COMMAND2_CH_RPN:
                return null;
            case MXMidi.COMMAND2_CH_NRPN:
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

            while (templateSeek < size() && dataSeek < data.length) {
                int c1 = get(templateSeek);
                templateSeek++;

                if (c1 == MXMidi.CCXML_CHECKSUM_END) {
                    dataSeek++;
                    continue;
                }
                int c2 = data[dataSeek++] & 0xff;
                // チャンネルメッセージはチャンネル番号を取り込む
                if (templateSeek == 1 && c2 >= 0x80 && c2 <= 0xef) {
                    channel = c2 & 0x0f;
                    c1 = c1 & 0xf0;
                    c2 = c2 & 0xf0;
                }
                if (c1 != c2) {
                    switch (c1) {
                        case MXMidi.CCXML_GL:
                            gateLo = c2;
                            break;
                        case MXMidi.CCXML_GH:
                            gateHi = c2;
                            break;
                        case MXMidi.CCXML_VL:
                            valueLo = c2;
                            break;
                        case MXMidi.CCXML_VH:
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
                    gate = MXRangedValue.new7bit(gateLo);
                } else {
                    gate = MXRangedValue.new14bit((gateHi << 7) | gateLo);
                }
            }
            if (valueLo >= 0 || valueHi >= 0) {
                if (valueLo < 0) {
                    valueLo = 0;
                }
                if (valueHi < 0) {
                    valueHi = 0;
                    value = MXRangedValue.new7bit(valueLo);
                } else {
                    value = MXRangedValue.new14bit((valueHi << 7) | valueLo);
                }
            }
            return new MXMessage(port, this, channel, gate, value);
        }

        return null;
    }

    byte[] dwordBuffer = new byte[3];

    public synchronized MXMessage readDwordForChGateValue(int port, int dword) {
        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = dword & 0xff;

        dwordBuffer[0] = (byte) status;
        dwordBuffer[1] = (byte) data1;
        dwordBuffer[2] = (byte) data2;

        return readDataForChGateValue(port, dwordBuffer);
    }

    @Override
    public int compareTo(MXTemplate o) {
        if (_commands == o._commands) {
            return 0;
        }
        int x = _commands.length - o._commands.length;
        if (x < 0) {
            return -1;
        }
        if (x > 0) {
            return 1;
        }

        for (int n = 0; n < _commands.length; ++n) {
            x = _commands[n] - o._commands[n];
            if (x < 0) {
                return -1;
            }
            if (x > 0) {
                return 1;
            }
        }
        return 0;
    }
    
    public static void main(String[] args) {
        String[] test = { "0xf0 [1 2 #VL 3] 4 5 6 0xf7" };
        for (String seek : test) {
            MXTemplate template = new MXTemplate(seek);
            for (int x = 0;  x < 127; x += 26) {
                MXMessage message = MXMessageFactory.fromTemplate(1, template, 5, MXRangedValue.ZERO7, MXRangedValue.new7bit(x));
                System.out.println(" x = " + x + " / " + MXUtil.dumpHex(message.getBinary()));
                System.out.println(" template = " + message.getTemplateAsText());
            }
        }
    }
}
