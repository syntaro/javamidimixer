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
package jp.synthtarou.cceditor.console;

import jp.synthtarou.cceditor.message.CCMIDICode;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ConsoleElement {
    public static final int TYPE_DWORD = 1;
    public static final int TYPE_DATA = 2;
    public static final int TYPE_MESSAGE = 3;

    private int _type;
    private int _dword;
    private byte[] _data;
    
    public ConsoleElement(int dword) {
        _type = TYPE_DWORD;
        _dword = dword;
    }

    public ConsoleElement(byte[] data) {
        if (data == null) {
            throw new NullPointerException("data is null");
        }
        _type = TYPE_DATA;
        _data = data;
    }

    public int getType() {
        return _type;
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
        }
        return "Type=" + _type;
    }
    
    public String formatMessageLong() {
        String timing = "";//#" + _timing.toString();
        String dump = formatMessageDump() + "  ";
        switch(_type) {
            case TYPE_DWORD: {
                return dump + toSegmentText(_dword) + timing;
            }
            case TYPE_DATA: {
                return dump + "[Binary]" + timing;
            }
        }
        return dump + "[???]" + timing;
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

        if (command == CCMIDICode.COMMAND_NOTEON) {
            return  "[On " + channel + ":" + CCMIDICode.nameOfNote(data1) + "=" + data2 + "]";
        }
        if (command == CCMIDICode.COMMAND_NOTEOFF) {
            return  "[Off " + channel + ":" + CCMIDICode.nameOfNote(data1) + "=" + data2 + "]";
        }
        if (command == CCMIDICode.COMMAND_POLYPRESSURE) {
            return  "[PolyPress " + channel + ":" + CCMIDICode.nameOfNote(data1) + "=" + data2 + "]";
        }
        if (command == CCMIDICode.COMMAND_PROGRAMCHANGE) {
            return  "[Program " + channel + ":" + data1 + "]";
        }
        if (command == CCMIDICode.COMMAND_CHANNELPRESSURE) {
            return  "[ChannelPress " + channel + ":" + data1 + "]";
        }
        if (command == CCMIDICode.COMMAND_PITCHWHEEL) {
            return  "[Pitch " + channel + ":" + MXUtil.toHexFF(data1) + MXUtil.toHexFF(data2) +"=" + (data2 << 7 + data1) + "]";
        }
        if (command == CCMIDICode.STATUS_SONGPOSITION) {
            return  "[SongPos " + data1 + "]";
        }
        if (command == CCMIDICode.STATUS_SONGSELECT) {
            return  "[SongSel " + data1 + "]";
        }
        if (command == CCMIDICode.COMMAND_CONTROLCHANGE) {
            String ccname = CCMIDICode.nameOfControlChange(data1);
            return  "[CC-" + ccname + " " + channel + ":" + MXUtil.toHexFF(data2) + "]";
        }

        return "****" + MXUtil.toHexFF(status) + MXUtil.toHexFF(data1) + MXUtil.toHexFF(data2);
        //return  "[" + MXUtilMidi.nameOfMessage(status, data1, data2) + " " + channel + "]";
    }
}
