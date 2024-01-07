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
package jp.synthtarou.midimixer.libs.midi.console;

import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMidiConsoleElement implements Comparable<MXMidiConsoleElement>{
    public static final int CONSOLE_DWORD = 1;
    public static final int CONSOLE_DATA = 2;
    public static final int CONSOLE_MESSAGE = 3;

    private MXTiming  _timing;
    private int _port;
    private int _type;
    private int _dword;
    private byte[] _data;
    private MXMessage _message;
    
    public MXMidiConsoleElement(MXTiming timing, int port, int dword) {
        _type = CONSOLE_DWORD;
        _port = port;
        _dword = dword;
        _timing = timing;
    }

    public MXMidiConsoleElement(MXTiming timing, int port, byte[] data) {
        if (data == null) {
            throw new NullPointerException("data is null");
        }
        _type = CONSOLE_DATA;
        _port = port;
        _data = data;
        _timing = timing;
    }

    public MXMidiConsoleElement(MXMessage message) {
        _type = CONSOLE_MESSAGE;
        _message = message;
        _port = message.getPort();
        _timing = message._timing;
    }

    public int getType() {
        return _type;
    }
    
    public MXTiming getTiming() {
        return _timing;
    }

    public int getDword() {
        if (_type != CONSOLE_DWORD) {
            throw new IllegalStateException("getDowrd <> not DWORD type");
        }
        return _dword;
    }

    public byte[] getData() {
        if (_type != CONSOLE_DATA) {
            throw new IllegalStateException("getDowrd <> not DWORD type");
        }
        return _data;
    }

    public MXMessage getMessage() {
        if (_type != CONSOLE_MESSAGE) {
            throw new IllegalStateException("getDowrd <> not DWORD type");
        }
        return _message;
    }
    
    public String formatMessageDump() {
        switch (_type) {
            case CONSOLE_DWORD: {
                int status = (_dword >> 16) & 0xff;
                int data1 = (_dword >> 8) & 0xff;
                int data2 = (_dword) & 0xff;
                return MXUtil.toHexFF(status) +" " + MXUtil.toHexFF(data1) + " " + MXUtil.toHexFF(data2);
            }
            case CONSOLE_DATA: {
                return MXUtil.dumpHex(_data);
            }
            case CONSOLE_MESSAGE: {
                String exString = "";
                if (_message.getDwordCount() >= 1) {
                    StringBuffer str = new StringBuffer();
                    for (int i = 0; i < _message.getDwordCount(); ++ i) {
                        int dword = _message.getAsDword(i);
                        int status = (dword >> 14) & 0x7f;
                        int data1 = (dword >> 7) & 0x7f;
                        int data2 = (dword) & 0x7f;
                        str.append("[");
                        str.append(MXUtil.toHexFF(status) +" " + MXUtil.toHexFF(data1) + " " + MXUtil.toHexFF(data2));
                        str.append("]");
                    }
                    return str.toString();
                }else {
                    byte[] data = _message.getBinary();
                    return MXUtil.dumpHex(data);
                }
            }
        }
        return "Type=" + _type;
    }
    
    public String formatMessageLong() {
        String port = Character.toString((char)('A'+ _port)) + ")";
        String timing = "";//#" + _timing.toString();
        String dump = formatMessageDump() + "  ";
        switch(_type) {
            case CONSOLE_DWORD: {
                return port + dump + toSegmentText(_dword) + timing;
            }
            case CONSOLE_DATA: {
                return port + dump + "[Binary]" + timing;
            }
            case CONSOLE_MESSAGE: {
                String exString = "";
                if(_message.getDwordCount() > 0) {
                    int col = _message.getDwordCount();
                    StringBuffer ret = new StringBuffer();
                    for (int i = 0 ; i < col; ++ i) {
                        int dword = _message.getAsDword(i);
                        String str = toSegmentText(dword);
                        ret.append(str);
                        if (i + 1 <  col) {
                            ret.append("|");
                        }
                    }
                    if (col == 1) {
                        return port + dump  + ret + timing;
                    }else {
                        return port + dump  + "[" + ret + " ]" + timing;
                    }
                }else {
                    return port + dump + "[Binary]" + timing;
                }
            }
        }
        return port + dump + "[???]" + timing;
    }

    public static String toSegmentText(byte[] data) {
        return "[" + MXUtil.dumpHex(data) + "]]";
    }

    public static String toSegmentText(int dword) {
        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = dword & 0xff;
        String channel;
        
        int command;

        if (status >= 0x80 && status <= 0xf0) {
            command = status & 0xf0;
            channel = Integer.toString(status & 0x0f);
        }else {
            command = status;
            channel = "";
        }

        if (command == MXMidi.COMMAND_CH_NOTEON) {
            return  "[On " + channel + ":" + MXMidi.nameOfNote(data1) + "=" + data2 + "]";
        }
        if (command == MXMidi.COMMAND_CH_NOTEOFF) {
            return  "[Off " + channel + ":" + MXMidi.nameOfNote(data1) + "=" + data2 + "]";
        }
        if (command == MXMidi.COMMAND_CH_POLYPRESSURE) {
            return  "[PolyPress " + channel + ":" + MXMidi.nameOfNote(data1) + "=" + data2 + "]";
        }
        if (command == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
            return  "[Program " + channel + ":" + data1 + "]";
        }
        if (command == MXMidi.COMMAND_CH_CHANNELPRESSURE) {
            return  "[ChannelPress " + channel + ":" + data1 + "]";
        }
        if (command == MXMidi.COMMAND_CH_PITCHWHEEL) {
            return  "[Pitch " + channel + ":" + MXUtil.toHexFF(data1) + MXUtil.toHexFF(data2) +"=" + ((data1 << 7) + data2) + "]";
        }
        if (command == MXMidi.COMMAND_SONGPOSITION) {
            return  "[SongPos " + data1 + "]";
        }
        if (command == MXMidi.COMMAND_SONGSELECT) {
            return  "[SongSel " + data1 + "]";
        }
        if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
            String ccname = MXMidi.nameOfControlChange(data1);
            return  "[CC-" + ccname + " " + channel + ":" + MXUtil.toHexFF(data2) + "]";
        }
        
        return "****" + MXUtil.toHexFF(status) + MXUtil.toHexFF(data1) + MXUtil.toHexFF(data2);
        //return  "[" + MXMidi.nameOfMessage(status, data1, data2) + " " + channel + "]";
    }

    @Override
    public int compareTo(MXMidiConsoleElement arg0) {
        MXTiming e1 = this._timing;
        MXTiming e2 = arg0._timing;
        if (e1 == null && e2 == null) return 0;
        if (e1 == null) return -1;
        if (e2 == null) return  1;
        return e1.compareTo(e2);
    }
}
