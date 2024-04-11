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

import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMidiConsoleElement{
    private MXMessage _message;
    
    public MXMidiConsoleElement(MXMessage message) {
        _message = message;
    }

    public MXMessage getMessage() {
        return _message;
    }
    
    public String formatMessageDump() {
        String exString = "";
        if (_message.getDwordCount() >= 1) {
            StringBuffer str = new StringBuffer();
            for (int i = 0; i < _message.getDwordCount(); ++ i) {
                int dword = _message.getAsDword(i);
                int status = (dword >> 16) & 0xff;
                int data1 = (dword >> 8) & 0xff;
                int data2 = (dword) & 0xff;
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
    
    public String formatMessageLong() {
        String port = Character.toString((char)('A'+ _message.getPort())) + ")";
        String dump = formatMessageDump() + "  ";
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
                return "Port: " + port + ", Dump:"+ dump  +" Format:"+ ret;
            }else {
                return "Port: " + port + ", Dump:"+ dump  +" Format:"+ ret;
            }
        }else {
            return "Port: " + port + ", Dump:"+ dump;
        }
    }

    public static String toSegmentText(byte[] data) {
        return "[" + MXUtil.dumpHex(data) + "]]";
    }

    public static String toSegmentText(int dword) {
        int extra = (dword >> 24) & 0xff;
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
            return  "[On ch:" + channel + ":" + MXMidi.nameOfNote(data1) + "=" + data2 + "]";
        }
        if (command == MXMidi.COMMAND_CH_NOTEOFF) {
            return  "[Off ch:" + channel + ":" + MXMidi.nameOfNote(data1) + "=" + data2 + "]";
        }
        if (command == MXMidi.COMMAND_CH_POLYPRESSURE) {
            return  "[PolyPress ch:" + channel + " Note:" + MXMidi.nameOfNote(data1) + " Val:" + data2 + "]";
        }
        if (command == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
            return  "[Program ch:" + channel + ":" + data1 + "]";
        }
        if (command == MXMidi.COMMAND_CH_CHANNELPRESSURE) {
            return  "[ChannelPress ch:" + channel + ":" + data1 + "]";
        }
        if (command == MXMidi.COMMAND_CH_PITCHWHEEL) {
            return  "[Pitch Ch:" + channel + " Hex:" + MXUtil.toHexFF(data1) + MXUtil.toHexFF(data2) +" Deci:" + ((data1 << 7) + data2) + "]";
        }
        if (command == MXMidi.COMMAND_SONGPOSITION) {
            return  "[SongPos " + data1 + "]";
        }
        if (command == MXMidi.COMMAND_SONGSELECT) {
            return  "[SongSel " + data1 + "]";
        }
        if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
            String ccname = MXMidi.nameOfControlChange(data1);
            return  "[CC-" + ccname + " Ch:" + channel + " Val:" + MXUtil.toHexFF(data2) + "]";
        }
        
        if (extra != 0) {
            return "[Extra"+ MXUtil.toHexFF(extra) + "]";
        }
        
        return "****" + MXUtil.toHexFF(status) + MXUtil.toHexFF(data1) + MXUtil.toHexFF(data2);
        //return  "[" + MXMidi.nameOfMessage(status, data1, data2) + " " + channel + "]";
    }
}
