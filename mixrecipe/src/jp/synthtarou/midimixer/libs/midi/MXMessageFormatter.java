/*
 * Copyright (C) 2024 Syntarou YOSHIDA
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

import jp.synthtarou.libs.MXRangedValue;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMessageFormatter {
    
    public static final String _port = "/port";
    public static final String _channel = "/channel";
    public static final String _command = "/command";
    public static final String _mean = "/mean";
    public static final String _escape = "//";

    public static final String _port_hint = "Port Code [A][B] etc";
    public static final String _channel_hint = "Channel Number [1:] etc";
    public static final String _command_hint = "Command [@CC 40] ect";
    public static final String _mean_hint = "Mean [Pan] etc";
    public static final String _escape_hint = "Escape for '/'";

    public String _format;
 
    public static final MXMessageFormatter _short = new MXMessageFormatter(_channel + _mean);
    public static final MXMessageFormatter _long = new MXMessageFormatter("Ch" + _channel + ":" + _mean);
    
    public MXMessageFormatter(String format) {
        _format = format;
    }
    
    public static void main(String[] args) {
        MXMessageFormatter format = MXMessageFormatter._short;
        System.out.println(format._format);
        String text = MXMessageFormatter._short.format(MXMessageFactory.fromNoteon(0, 9, 63, 50));
        System.out.println(text);
    }
    
    public String format(MXMessage message) {
        StringBuilder str = new StringBuilder();
        int pos = 0;
        while(pos < _format.length()) {
            int ch = _format.charAt(pos);
            if (ch == '/') {
                String split = _format.substring(pos);
                String part = "/";
                String label = "/";
                if (split.startsWith(_port)) {
                    label = _port;
                    part = MXMidiStatic.nameOfPortShort(message.getPort());
                }
                else if (split.startsWith(_channel)) {
                    label = _channel;
                    if (message.isChannelMessage1()) {
                        part = Integer.toString(message.getChannel() + 1) + ":";
                    }
                    else {
                        part = "";
                    }
                }
                else if (split.startsWith(_command)) {
                    label = _command;
                    part = message.getTemplateAsText();
                }
                else if (split.startsWith(_mean)) {
                    label = _mean;
                    part = getMessageMean(message);
                }
                str.append(part);
                pos += label.length();
            }
            else {
                str.append((char)ch);
                pos ++;
            }
        }
        return str.toString();
    }

    private String getMessageMean(MXMessage message) {
        MXTemplate template = message.getTemplate();
        if (template.size() == 0) {
            return "Empty";
        }

        int status = message.getStatus();
        int command = status;
        
        MXRangedValue value = message.getValue();
        MXRangedValue gate = message.getGate();
        
        if (command >= 0x80 && command <= 0xef) {
            command &= 0xf0;
        }
        if (command >= 0x100) {
            command &= 0xfff0;
        }
        
        if (message.isSysexOrMeta()) {
            int len = template.getLengthWithChecksum();
            switch (status) {
                case 0xf0:
                    return "Syx." + len;
                case 0xf7:
                    return "SyxEnd." + len;
                case 0xff:
                    return "Meta." + len;
            }
            return "Bin." + len;
        }

        if (command == MXMidiStatic.COMMAND2_CH_PITCH_MSBLSB) {
            return "PITCH = " + value._value;
        }
        if (command == MXMidiStatic.COMMAND2_CH_RPN) {
            int temp1 = MXTemplate.parseDAlias(template.safeGet(1), message);
            int temp2 = MXTemplate.parseDAlias(template.safeGet(2), message);
            int gate14 = (temp1 << 7) | temp2;
            int val1 = MXTemplate.parseDAlias(template.safeGet(3), message);
            int val2 = MXTemplate.parseDAlias(template.safeGet(4), message);
            int value14 = (val1 << 7) | val2;
            return "RPN MSB:LSB = " + gate14 + " value = " + value14;
        }
        if (command == MXMidiStatic.COMMAND2_CH_NRPN) {
            int temp1 = MXTemplate.parseDAlias(template.safeGet(1), message);
            int temp2 = MXTemplate.parseDAlias(template.safeGet(2), message);
            int gate14 = (temp1 << 7) | temp2;
            int val1 = MXTemplate.parseDAlias(template.safeGet(3), message);
            int val2 = MXTemplate.parseDAlias(template.safeGet(4), message);
            int value14 = (val1 << 7) | val2;
            return "NRPN MSB:LSB = " + gate14 + " value = " + value14;
        }
        if (command == MXMidiStatic.COMMAND2_CH_PROGRAM_INC) {
            return "PROGRAM INC";
        }
        if (command == MXMidiStatic.COMMAND2_CH_PROGRAM_DEC) {
            return "PROGRAM DEC";

        }

        if (command == MXMidiStatic.COMMAND_CH_CONTROLCHANGE) {
            int data1 = gate._value;
            return MXMidiStatic.nameOfControlChange(data1);
        } else if (command == MXMidiStatic.COMMAND_CH_NOTEOFF) {
            int note = gate._value;
            int velocity = value._value;
            return MXMidiStatic.nameOfNote(note) + "#OFF";
        }
        if (command == MXMidiStatic.COMMAND_CH_NOTEON) {
            int note = gate._value;
            int velocity = value._value;
            return MXMidiStatic.nameOfNote(note);
        }
        if (command == MXMidiStatic.COMMAND_CH_POLYPRESSURE) {
            int note = gate._value;
            int velocity = value._value;
            return "Prs";
        }
        if (command == MXMidiStatic.COMMAND_CH_PROGRAMCHANGE) {
            int program = message.getCompiled(1);
            return "Pg" + program;
        }
        if (command == MXMidiStatic.COMMAND_CH_CHANNELPRESSURE) {
            return "ChPrs";
        }
        if (command == MXMidiStatic.COMMAND_CH_PITCHWHEEL || command == MXMidiStatic.COMMAND2_CH_PITCH_MSBLSB) {
            return "Pitch";
        }
        if (command == MXMidiStatic.COMMAND_SONGPOSITION) {
            return "Pos";
        }
        if (command == MXMidiStatic.COMMAND_SONGSELECT) {
            return "Song";
        }

        return MXMidiStatic.nameOfMessage(message.getStatus(), message.getCompiled(1), message.getCompiled(2));
    }
}
