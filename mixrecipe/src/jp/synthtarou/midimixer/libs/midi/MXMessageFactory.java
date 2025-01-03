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

import java.util.logging.Level;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXRangedValue;

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
    
    synchronized MXMessage fromCached(int port, int dword) {
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
                    _cacheControlChange[data1] = fromDword1(dword);
                }
                found = _cacheControlChange[data1];
            }
            else {
                if (_cacheCommand[command] == null) {
                    _cacheCommand[command] = fromDword1(dword);
                }
                found = _cacheCommand[command];
            }
        }
        
        if (status >= 0xf1 && status <= 0xfe) {
            int command = status;
            
            if (_cacheCommand[command] == null) {
                _cacheCommand[command] = fromDword1(dword);
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
        
        MXMessage message = found.readMessageWithThisTemplate(port, dword);
        if (message == null) {
            message = new MXMessage(port, found);
        }
        return message;
    }
    

    public static MXTemplate fromDword1(int dword) {
        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = dword & 0xff;
        int[] template = null;

        if (status >= 0x80 && status <= 0xef) {
            status = status & 0xf0;
            switch (status) {
                case MXMidiStatic.COMMAND_CH_PITCHWHEEL:
                    template = new int[]{MXMidiStatic.COMMAND_CH_PITCHWHEEL, MXMidiStatic.CCXML_VL, MXMidiStatic.CCXML_VH};
                    break;
                case MXMidiStatic.COMMAND_CH_CHANNELPRESSURE:
                    template = new int[]{MXMidiStatic.COMMAND_CH_CHANNELPRESSURE, MXMidiStatic.CCXML_VL};
                    break;
                case MXMidiStatic.COMMAND_CH_POLYPRESSURE:
                    template = new int[]{MXMidiStatic.COMMAND_CH_POLYPRESSURE, MXMidiStatic.CCXML_GL, MXMidiStatic.CCXML_VL};
                    break;
                case MXMidiStatic.COMMAND_CH_CONTROLCHANGE:
                    template = new int[]{MXMidiStatic.COMMAND_CH_CONTROLCHANGE, MXMidiStatic.CCXML_GL, MXMidiStatic.CCXML_VL};
                    break;
                case MXMidiStatic.COMMAND_CH_NOTEON:
                    template = new int[]{MXMidiStatic.COMMAND_CH_NOTEON, MXMidiStatic.CCXML_GL, MXMidiStatic.CCXML_VL};
                    break;
                case MXMidiStatic.COMMAND_CH_NOTEOFF:
                    template = new int[]{MXMidiStatic.COMMAND_CH_NOTEOFF, MXMidiStatic.CCXML_GL, MXMidiStatic.CCXML_VL};
                    break;
                case MXMidiStatic.COMMAND_CH_PROGRAMCHANGE:
                    template = new int[]{MXMidiStatic.COMMAND_CH_PROGRAMCHANGE, MXMidiStatic.CCXML_GL};
                    break;
            }
        } else {
            switch (status) {
                case MXMidiStatic.COMMAND_SYSEX: //sysex
            }
        }

        if (template == null) {
            return null;
        }
        return new MXTemplate(template);
    }
    
    static final MXTemplate ZERO = new MXTemplate(new int[]{ MXMidiStatic.COMMAND2_NONE, 0, 0 });
    
    public static MXMessage fromTemplate(int port, MXTemplate template, int channel, MXRangedValue gate, MXRangedValue value) {
        MXMessage message = new MXMessage(port, template, channel, gate, value);
        return message;
    }
 
    public static MXMessage fromCCXMLText(int port, String text, int channel) {
        return fromCCXMLText(port, text, channel, null, null);
    }

    public static MXMessage fromCCXMLText(int port, String text, int channel, MXRangedValue gate, MXRangedValue value) {
        try {
            MXTemplate template = new MXTemplate(text);
            MXMessage msg = MXMessageFactory.fromTemplate(port, template, channel, gate, value);
            return msg;
        } catch (RuntimeException ex) {
            MXFileLogger.getLogger(MXMessageFactory.class).log(Level.WARNING, ex.getMessage(), ex);
            return null;
        }
    }

    public static MXMessage fromBinary(int port, byte[] data) {
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
        
        int[] template= new int[data.length];
        for (int i = 0; i < data.length; ++ i) {
            template[i] = data[i] & 0xff;
        }
        return fromTemplate(port, new MXTemplate(template), 0, null, null);
    }
    
    static MXTemplate _empty = new MXTemplate((int[])null);

    public static MXMessage newEmpty(int port) {
        return new MXMessage(port, _empty);
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
        return _instance.fromCached(port, dword);
    }

    static final int [] _cc7bit_int = new int[]{MXMidiStatic.COMMAND_CH_CONTROLCHANGE, MXMidiStatic.CCXML_GL, MXMidiStatic.CCXML_VL};
    static final MXTemplate _cc7bit = new MXTemplate(_cc7bit_int);

    public static MXMessage fromControlChange(int port, int channel, int cc, int value) {
        MXMessage message = new MXMessage(port, _cc7bit, channel, MXRangedValue.new7bit(cc), MXRangedValue.new7bit(value));
        return message;
    }

    static final int [] _cc14bit_int = new int[]{MXMidiStatic.COMMAND_CH_CONTROLCHANGE, MXMidiStatic.CCXML_GL, MXMidiStatic.CCXML_VH, MXMidiStatic.CCXML_VL};
    static final MXTemplate _cc14bit = new MXTemplate(_cc14bit_int);
    
    public static MXMessage fromControlChange14(int port, int channel, int data1, int msb, int lsb) {
        int data2 = (msb << 7) | lsb;
        MXMessage message = new MXMessage(port, _cc14bit, channel, MXRangedValue.new7bit(data1), MXRangedValue.new14bit(data2));
        return message;
    }

    static final int [] _noteon_int = new int[]{MXMidiStatic.COMMAND_CH_NOTEON, MXMidiStatic.CCXML_GL, MXMidiStatic.CCXML_VL};
    static final MXTemplate _noteon = new MXTemplate(_noteon_int);

    public static MXMessage fromNoteon(int port, int channel, int note, int velocity) {
        MXMessage message = new MXMessage(port, _noteon, channel, MXRangedValue.new7bit(note), MXRangedValue.new7bit(velocity));
        return message;
    }

    static final int [] _noteoff_int = new int[]{MXMidiStatic.COMMAND_CH_NOTEOFF, MXMidiStatic.CCXML_GL, MXMidiStatic.CCXML_VL};
    static final MXTemplate _noteoff = new MXTemplate(_noteoff_int);

    public static MXMessage fromNoteoff(int port, int channel, int note) {
        MXMessage message = new MXMessage(port, _noteoff, channel, MXRangedValue.new7bit(note), MXRangedValue.ZERO7);
        return message;
    }

    static final int [] _programchange_int = new int[]{MXMidiStatic.COMMAND_CH_PROGRAMCHANGE, MXMidiStatic.CCXML_GL, 0 };
    static final MXTemplate _programchange = new MXTemplate(_programchange_int);

    public static MXMessage fromProgramChange(int port, int channel, int program) {
        MXMessage message = new MXMessage(port, _programchange, channel, MXRangedValue.new7bit(program), MXRangedValue.ZERO7);
        return message;
    }
}
