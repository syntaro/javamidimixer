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
package jp.synthtarou.midimixer.libs.console;

import jdk.nashorn.api.tree.ThrowTree;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.MXUtilMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ConsoleElement implements Comparable<ConsoleElement>{
    public static final int TYPE_DWORD = 1;
    public static final int TYPE_DATA = 2;
    public static final int TYPE_MESSAGE = 3;

    private MXTiming  _timing;
    private int _port;
    private int _type;
    private int _dword;
    private byte[] _data;
    private MXMessage _message;
    
    public ConsoleElement(MXTiming timing, int port, int dword) {
        _type = TYPE_DWORD;
        _port = port;
        _dword = dword;
        _timing = timing;
    }

    public ConsoleElement(MXTiming timing, int port, byte[] data) {
        if (data == null) {
            throw new NullPointerException("data is null");
        }
        _type = TYPE_DATA;
        _port = port;
        _data = data;
        _timing = timing;
    }

    public ConsoleElement(MXMessage message) {
        _type = TYPE_MESSAGE;
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
        if (_type != TYPE_DWORD) {
            throw new IllegalStateException("getDowrd <> not DWORD type");
        }
        return _dword;
    }

    public byte[] getData() {
        if (_type != TYPE_DATA) {
            throw new IllegalStateException("getDowrd <> not DWORD type");
        }
        return _data;
    }

    public MXMessage getMessage() {
        if (_type != TYPE_MESSAGE) {
            throw new IllegalStateException("getDowrd <> not DWORD type");
        }
        return _message;
    }
    
    public String formatMessageDump() {
        switch (_type) {
            case TYPE_DWORD: {
                int status = (_dword >> 16) & 0xff;
                int data1 = (_dword >> 8) & 0xff;
                int data2 = (_dword) & 0xff;
                return MXUtil.toHexFF(status) +" " + MXUtil.toHexFF(data1) + " " + MXUtil.toHexFF(data2);
            }
            case TYPE_DATA: {
                return MXUtil.dumpHexFF(_data);
            }
            case TYPE_MESSAGE: {
                String exString = "";
                if (_message.isBinMessage()) {
                    byte[] data = _message.getDataBytes();
                    return MXUtil.dumpHexFF(data);
                }else {
                    int status = _message.getStatus();
                    int data1 = _message.getData1();
                    int data2 = _message.getData2();
                    return "" + MXUtil.toHexFF(status) +" " + MXUtil.toHexFF(data1) + " " + MXUtil.toHexFF(data2);
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
            case TYPE_DWORD: {
                return port + dump + toSegmentText(_dword) + timing;
            }
            case TYPE_DATA: {
                return port + dump + "[Binary]" + timing;
            }
            case TYPE_MESSAGE: {
                String exString = "";
                if(_message.isBinMessage() == false || _message.isDataentry()) {
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
        return "[" + MXUtil.dumpHexFF(data) + "]]";
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

        if (command == MXMidi.COMMAND_NOTEON) {
            return  "[On " + channel + ":" + MXUtilMidi.nameOfNote(data1) + "=" + data2 + "]";
        }
        if (command == MXMidi.COMMAND_NOTEOFF) {
            return  "[Off " + channel + ":" + MXUtilMidi.nameOfNote(data1) + "=" + data2 + "]";
        }
        if (command == MXMidi.COMMAND_POLYPRESSURE) {
            return  "[PolyPress " + channel + ":" + MXUtilMidi.nameOfNote(data1) + "=" + data2 + "]";
        }
        if (command == MXMidi.COMMAND_PROGRAMCHANGE) {
            return  "[Program " + channel + ":" + data1 + "]";
        }
        if (command == MXMidi.COMMAND_CHANNELPRESSURE) {
            return  "[ChannelPress " + channel + ":" + data1 + "]";
        }
        if (command == MXMidi.COMMAND_PITCHWHEEL) {
            return  "[Pitch " + channel + ":" + MXUtil.toHexFF(data1) + MXUtil.toHexFF(data2) +"=" + (data2 << 7 + data1) + "]";
        }
        if (command == MXMidi.STATUS_SONGPOSITION) {
            return  "[SongPos " + data1 + "]";
        }
        if (command == MXMidi.STATUS_SONGSELECT) {
            return  "[SongSel " + data1 + "]";
        }
        if (command == MXMidi.COMMAND_CONTROLCHANGE) {
            String ccname = MXUtilMidi.nameOfControlChange(data1);
            return  "[CC-" + ccname + " " + channel + ":" + MXUtil.toHexFF(data2) + "]";
        }

        return "****" + MXUtil.toHexFF(status) + MXUtil.toHexFF(data1) + MXUtil.toHexFF(data2);
        //return  "[" + MXUtilMidi.nameOfMessage(status, data1, data2) + " " + channel + "]";
    }

    @Override
    public int compareTo(ConsoleElement arg0) {
        MXTiming e1 = this._timing;
        MXTiming e2 = arg0._timing;
        if (e1 == null && e2 == null) return 0;
        if (e1 == null) return -1;
        if (e2 == null) return  1;
        return e1.compareTo(e2);
    }
}
