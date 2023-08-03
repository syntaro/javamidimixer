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
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMessageFactory {
    private static final MXDebugPrint _debug = new MXDebugPrint(MXMessageFactory.class);
    
    static {
        _debug.switchOn();
    }
    
    public static MXMessage createDummy() {
        return new MXMessage(0, new MXMessageTemplate(null), 0, 0);
    }

    public static MXMessage fromClone(MXMessage old) {
        MXMessage msg = (MXMessage)old.clone();
        return msg; 
    }
    
    public static MXMessage fromDWordMessage(int port, int dword) {
        int status = ((dword >> 8) >> 8) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = dword & 0xff;
        return MXMessageFactory.fromShortMessage(port, status, data1, data2);
    }

    public static MXMessage fromMeta(int port, byte[] data) {
        int[] template = new int[data.length];
        for (int i = 0; i < data.length; ++ i) {
            template[i] = data[i] & 0xff;
        }
        MXMessage message = new MXMessage(port, template, 0, 0);
        message.setMetaType(data[1] & 0xff);

        String text = null;
        try {
            text = new String(data, 2, data.length -2, "ASCII");
            text = new String(data, 2, data.length -2);
            text = new String(data, 2, data.length -2, "SJIS");
        }catch(Exception e) {
            e.printStackTrace();
        }

        message._metaText = text;
        message._dataBytes = data;
        return message;
    }

    public static MXMessage fromSysexMessage(int port, byte[] data) {
        int status = data[0] & 0xff;
        int[] template = new int[data.length];
        if (status != 240 && status != 247) {
            new MXException("SysEx(240,247) was " + status).printStackTrace();
        }
        for (int i = 0; i < data.length; ++i) {
            template[i] = data[i] & 0xff;
        }
        MXMessage m = new MXMessage(port, template, 0, 0);
        return m;
    }
    
    public static MXMessage fromShortMessage(int port, int status, int data1, int data2) {
        int command = status;
        int channel = 0;
        if (status >= 0x80 && status <= 0xe0) {
            command = status & 0xf0;
            channel = status & 0x0f;
        }

        if (command < 0 || command > 255) {
            _debug.println("command = " + command);
            return null;
        }
        if (channel < 0 || channel > 15) {
            _debug.println("channel= " + channel);
            return null;
        }
        if (data1 < 0 || data1 > 127) {
            _debug.println("data1 = " + data1);
            return null;
        }
        if (data2 < 0 || data2 > 127) {
            _debug.println("data2 = " + data2);
            return null;
        }

        int gate = 0;
        int value = 0;
        
        switch (command) {
            case MXMidi.COMMAND_PROGRAMCHANGE:
                gate = data1;
                value = 0;
                
                data1 = MXMessageTemplate.DTEXT_GL;
                data2 = 0;
                break;
            case MXMidi.COMMAND_CONTROLCHANGE:
                gate = data1;
                value = data2;

                data1 = MXMessageTemplate.DTEXT_GL;
                data2 = MXMessageTemplate.DTEXT_VL;
                break;
            case MXMidi.COMMAND_NOTEON:
            case MXMidi.COMMAND_NOTEOFF:
            case MXMidi.COMMAND_POLYPRESSURE:
                gate = data1;
                value = data2;

                data1 = MXMessageTemplate.DTEXT_GL;
                data2 = MXMessageTemplate.DTEXT_VL;
                break;
            case MXMidi.COMMAND_PITCHWHEEL:
                value = (data1 & 127) | (data2 << 7);
                data1 = MXMessageTemplate.DTEXT_VL;
                data2 = MXMessageTemplate.DTEXT_VH;
                break;
            case MXMidi.COMMAND_CHANNELPRESSURE:
                value = data1;
                data1 = MXMessageTemplate.DTEXT_VL;
                break;
            default:
                if (command >= 240 && command <= 247) {
                    if (command == MXMidi.STATUS_SONGPOSITION) {
                        value = (data1 & 127) | (data2 << 7);
                        data1= MXMessageTemplate.DTEXT_VL;
                        data2 = MXMessageTemplate.DTEXT_VH;
                    }
                    if (command == MXMidi.STATUS_SONGSELECT) {
                        value = data1;
                        data1 = MXMessageTemplate.DTEXT_VL;
                    }
                }
                break;
        }

        int[] template = new int[3];
        template[0] = status;
        template[1] = data1;
        template[2] = data2;
        MXMessage message = new MXMessage(port, template, gate, value);
        message.setChannel(channel);

        return message;
    }

    public static MXMessage fromBinary(int port, byte[] data)  {
        if (data == null || data.length == 0 || data[0] == 0) {
            return null;
        }
        int[] template = new int[data.length];
        for (int i = 0; i < data.length; ++ i) {
            template[i] = data[i] & 0xff;
        }
        try {
            MXMessage ret = new MXMessage(port, template, 0, 0);
            return ret;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static MXMessageTemplate fromDtext(String text, int channel)  {
        if (text == null || text.length() == 0) {
            return null;
        }
        
        while (text.startsWith(" ")) {
            text = text.substring(1);
        }
        while (text.endsWith(" ")) {
            text = text.substring(0, text.length() - 1);
        }
        
        
        if (text.equals(MXMessageTemplate.EXCOMMAND_PROGRAM_INC)) {
            int[] template = { MXMessageTemplate.DTEXT_PROGINC, channel };
            return new MXMessageTemplate(template);
        }
        if (text.equals(MXMessageTemplate.EXCOMMAND_PROGRAM_DEC)) {
            int[] template = { MXMessageTemplate.DTEXT_PROGDEC, channel };
            return new MXMessageTemplate(template);
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
            int checksumKeep = -1;

            while(readX < line.length) {
                char ch = line[readX ++];
                if (ch == '[') {
                    inChecksum = true;
                    checksumKeep = 0;
                    continue;
                }
                if (ch == ']') {
                    if (inChecksum) {
                        inChecksum = false;
                        if (wx != 0) {
                            separated.add(new String(word, 0, wx));
                        }
                        separated.add("#CHECKSUM");
                        wx = 0;
                    }else {
                        _debug.println("Checksum have not opened");
                        _debug.printStackTrace();
                    }
                    continue;
                }
                if (ch == ' '|| ch == '\t' || ch == ',') {
                    if (wx != 0) {
                        separated.add(new String(word, 0, wx));
                        if (inChecksum) {
                            checksumKeep ++;
                        }
                    }
                    wx = 0;
                    continue;
                }
                word[wx ++] = ch;
            }

            if (wx != 0) {
                separated.add(new String(word, 0, wx));
                if (inChecksum) {
                    checksumKeep ++;
                }
                wx = 0;
            }

            int gatetemp = -1;
            if (text.contains("@")) {
                ArrayList<String> sepa2 = new ArrayList();
                for (int sx = 0; sx < separated.size(); ++ sx) {
                    String str = separated.get(sx);
                    if (str.startsWith("@")) {
                        if (str.equalsIgnoreCase("@PB")) {
                            sepa2.add("#ECH");
                            sepa2.add(separated.get(++ sx));
                            sepa2.add(separated.get(++ sx));
                        }
                        else if (str.equalsIgnoreCase("@CP")) {
                            sepa2.add("#DCH");
                            sepa2.add(separated.get(++ sx));
                            sepa2.add("#NONE");
                        }
                        else if (str.equalsIgnoreCase("@PKP")) {
                            sepa2.add("#ACH");
                            String t = separated.get(++sx);
                            if(t.startsWith("#")) {
                                sepa2.add(t);
                            }else {
                                gatetemp = MXUtil.numberFromText(t);
                                sepa2.add("#GL");
                            }
                            sepa2.add(separated.get(++ sx));
                        }
                        else if (str.equalsIgnoreCase("@CC")) {
                            sepa2.add("#BCH");
                            String t = separated.get(++sx);
                            if(t.startsWith("#")) {
                                sepa2.add(t);
                            }else {
                                gatetemp = MXUtil.numberFromText(t);
                                sepa2.add("#GL");
                            }
                            sx ++;
                            if (separated.size() <= sx) {
                                return null;
                            }
                            sepa2.add(separated.get(sx));
                        }
                        else if (str.equalsIgnoreCase("@SYSEX")) {
                            //THRU (no need recompile)
                        }
                        else if (str.equalsIgnoreCase("@RPN")) {
                            int  msb = MXMessageTemplate.readAliasText(separated.get(++sx));
                            int  lsb = MXMessageTemplate.readAliasText(separated.get(++sx));
                            int data = MXMessageTemplate.readAliasText(separated.get(++sx));
                            if (separated.size() >= sx +2) {
                                data = data << 7;
                                data |= MXMessageTemplate.readAliasText(separated.get(++sx));
                            }

                            int[] template = { MXMessageTemplate.DTEXT_RPN, msb, lsb, data };
                            return new MXMessageTemplate(template);
                        }
                        else if (str.equalsIgnoreCase("@NRPN")) {
                            int  msb = MXMessageTemplate.readAliasText(separated.get(++sx));
                            int  lsb = MXMessageTemplate.readAliasText(separated.get(++sx));
                            int data = MXMessageTemplate.readAliasText(separated.get(++sx));
                            if (separated.size() >= sx +2) {
                                data = data << 7;
                                data |= MXMessageTemplate.readAliasText(separated.get(++sx));
                            }

                            int[] template = { MXMessageTemplate.DTEXT_NRPN, msb, lsb, data };
                            return new MXMessageTemplate(template);
                        }else {
                            _debug.println("Not Support [" + text + "]");
                            return null;
                        }
                    }else {
                        sepa2.add(str);
                    }
                }
                separated = sepa2;
            }

            // cleanup
            int[] compiled = new int[line.length];
            int cx = 0;
            int px = 0;
            
            for (int sx = 0; sx < separated.size(); ++ sx) {
                String str = separated.get(sx);
                int code = MXMessageTemplate.readAliasText(str);
                if (code < 0) {
                    return null;
                }
                compiled[px++] = code;
                continue;
            }
            int[] template = new int[Math.max(px, 3)];
            for (int i = 0; i < px; ++ i) {
                template[i] = compiled[i];
            }
            
            MXMessageTemplate temp = new MXMessageTemplate(template);
            temp._checksumTo = checksumKeep;
            return temp;
        }catch(Exception e) {
            _debug.printStackTrace(e);
            return null;
        }
    }
}
