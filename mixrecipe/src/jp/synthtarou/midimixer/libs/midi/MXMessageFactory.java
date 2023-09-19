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
import java.util.LinkedList;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.RangedValue;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMessageFactory {
    private static MXMessageFactory _instance = new MXMessageFactory();
    
    public MXMessageFactory() {
    }
    
    
    public MXTemplate[] _cacheCommand = new MXTemplate[256];
    public MXTemplate[] _cacheControlChange = new MXTemplate[256];
    
    synchronized MXMessage fromDword2(int port, int dword) {
        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = dword & 0xff;
        int channel = -1;
        MXTemplate found = null;

        if (status >= 0x80 && status <= 0xef) {
            int command = status & 0xf0;
            channel = status & 0x0f;
            
            if (command == 0xB0) { //ControlChange
                if (_cacheControlChange[data1] == null) {
                    _cacheControlChange[data1] = MXTemplate.fromDword1(dword);
                }
                found = _cacheControlChange[data1];
            }
            else {
                if (_cacheCommand[command] == null) {
                    _cacheCommand[command] = MXTemplate.fromDword1(dword);
                    System.out.println("cache commamdn " + _cacheCommand[command]);
                }
                found = _cacheCommand[command];
            }
        }
        
        if (status >= 0xf1 && status <= 0xfe) {
            int command = status;
            
            if (_cacheCommand[command] == null) {
                _cacheCommand[command] = MXTemplate.fromDword1(dword);
            }
            found = _cacheCommand[command];
        }
        
        if (found == null) {
            int[] template = new int[3];
            template[0] = status;
            template[1] = data1;
            template[2] = data2;
            found = new MXTemplate(template);
        }
        
        MXMessage message = found.readDwordForChGateValue(port, dword);
        if (message == null) {
            message = new MXMessage(port, found);
        }
        return message;
    }
    
    static final MXTemplate ZERO = new MXTemplate(new int[]{ MXMidi.COMMAND2_NONE, 0, 0 });
    
    LinkedList<MXTemplate> _cachedTemplate = new LinkedList();
    
    public static synchronized MXMessage fromTemplate(int port, int[] template, int channel, RangedValue gate, RangedValue value) {
        if (template == null || template.length == 0 || template[0] == 0) {
            template = new int[] { 0, 0, 0 };
        }

        MXTemplate t = new MXTemplate(template);
        MXMessage message = new MXMessage(port, t);
        message.setChannel(channel);
        if (gate == null) {
            if (message.hasGateHiField()) {
                gate = RangedValue.new14bit(0);
            }else if (message.hasGateLowField()) {
                gate = RangedValue.new7bit(0);
            }else {
                gate = RangedValue.new7bit(0);
            }
        }
        message.setGate(gate);
        if (value == null) {
            if (message.hasValueHiField()) {
                value = RangedValue.new14bit(0);
            }else if (message.hasValueLowField()) {
                value = RangedValue.new7bit(0);
            }else {
                value = RangedValue.new7bit(0);
            }
        }
        message.setValue(value);
        return message;
    }
 
    public static MXMessage fromCCXMLText(int port, String text, int channel) {
        return fromCCXMLText(port, text, channel, null, null);
    }

    public static MXMessage fromCCXMLText(int port, String text, int channel, RangedValue gate, RangedValue value) {
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
            int[] compiled = new int[separated.size()];
            int cx = 0;
            int px = 0;

            for (int sx = 0; sx < separated.size(); ++sx) {
                String str = separated.get(sx);
                int code;
                if (str.startsWith("@")) {
                    code = MXTemplate.readCommandText(str);
                    if (code < 0) {
                        System.out.println("can't parse " + str);
                        return null;
                    }
                }else if (str.startsWith("#")) {
                    code = MXTemplate.readAliasText(str);
                    if (code < 0) {
                        System.out.println("can't parse " + str);
                        return null;
                    }
                }else if (str.equals("[") || str.equals("]")) {
                    code = MXTemplate.readAliasText(str);
                }else {
                    code = MXUtil.numberFromText(str);
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

    public static synchronized MXMessage fromBinary(int port, byte[] data) {
        if (data == null || data.length == 0 || data[0] == 0) {
            return null;
        }

        if (data.length == 3) {
            int status = data[0] & 0xff;
            int data1 = data[1] & 0xff;
            int data2 = data[2] & 0xff;
            
            if (status >= 0x80 && status <= 0xef) {
                return fromShortMessage(0, status, data1, data2);
            }
            if (status >= 0xf1 && status <= 0xfe) {
                return fromShortMessage(0, status, data1, data2);
            }
        }

        int c = data[0] & 0xff;
        
        boolean seekCache = true;
        
        if (c == 0xff && data.length >= 100) {
            seekCache = false;
        }
        if (c == 0xf0 && data.length >= 100) {
            seekCache = false;
        }

        int[] template= new int[data.length];
        for (int i = 0; i < data.length; ++ i) {
            template[i] = data[i] & 0xff;
        }
        return fromTemplate(port, template, 0, RangedValue.ZERO7, RangedValue.ZERO7);
    }

    public static MXMessage createDummy() {
        return _instance.fromDWordMessage(0, 0);
    }

    public static MXMessage fromClone(MXMessage old) {
        MXMessage msg = (MXMessage) old.clone();
        return msg;
    }

    public MXMessage fromDWordMessage(int port, int dword) {
        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = dword & 0xff;
        return fromShortMessage(port, status, data1, data2);
    }

    public static MXMessage fromShortMessage(int port, int status, int data1, int data2) {
        int dword = (status << 16) | (data1 << 8) | data2;
        return _instance.fromDword2(port, dword);
    }
}
