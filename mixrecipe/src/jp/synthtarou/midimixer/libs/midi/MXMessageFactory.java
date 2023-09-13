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

import java.util.LinkedList;
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
        message.setGate(gate);
        message.setValue(value);
        return message;
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
