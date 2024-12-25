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
package jp.synthtarou.midimixer.libs.midi.port;

import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;

/**
 *
 * @author Syntarou YOSHIDA
 */

public class MXMidiFilter {
    public static final int TYPE_ISSKIPPER = 0;
    public static final int TYPE_NOTE = 1;
    public static final int TYPE_DAMPER_PEDAL = 2;
    public static final int TYPE_PITCH_BEND = 3;
    public static final int TYPE_MOD_WHEEL = 4;
    public static final int TYPE_BANK_SELECT = 5;
    public static final int TYPE_PROGRAM_CHANGE = 6;
    public static final int TYPE_DATA_ENTRY = 7;
    public static final int TYPE_ANOTHER_CC = 8;
    public static final int TYPE_RESET_GENERAL = 9;
    public static final int TYPE_SYSEX = 10;
    public static final int TYPE_CLOCK = 11;
    public static final int TYPE_ACTIVE_SENSING = 12;
    public static final int COUNT_TYPE = 13;

    private static final String[] typeNames = {
        "*Not", "Note", "CC:DamperPedal", "PitchBend", "CC:ModWheel", "CC:BankChange",
        "ProgramChange", "CC:DataEntry", "CC:Another", "GM&GS&XGReset", "SysEX",
        "Clock", "Active"
    };

    public static String getName(int id) {
        return typeNames[id];
    }
    
    public static int fromName(String name) {
        for (int i = 0; i < typeNames.length; ++ i) {
            if (typeNames[i].equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    boolean[] _itemChecked;

    public MXMidiFilter() {
        clearChecked();
        _itemChecked[TYPE_ISSKIPPER] = true;
        _itemChecked[TYPE_ACTIVE_SENSING] = true;
        _itemChecked[TYPE_CLOCK] = true;
    }
    
    public void clearChecked() {
        _itemChecked = new boolean[COUNT_TYPE];
    }
    
    public boolean isChecked(int type) {
        return _itemChecked[type];
    }
    
    public void setChecked(int type, boolean flag) {
        _itemChecked[type] = flag;
    }

    public boolean isOK(MXMessage message) {
        boolean flag = false;
        for (int i = 1; i < COUNT_TYPE; ++ i) {
            if (isChecked(i)) {
                if (isMessageHit(i, message)) {
                    flag = true;
                    break;
                }
            }
        }
        if (isChecked(TYPE_ISSKIPPER)) {
            return !flag;
        }else {
            return flag;
        }
    }

    public boolean isMessageHit(int type, MXMessage message) {
        int status = message.getStatus();
        int command = status;
        if (message.isChannelMessage1() || command >= 0x100) {
            command &= 0xfff0;
        }
        int data1 = message.getCompiled(1);
        int data2 = message.getCompiled(2);

        switch (type) {
            case TYPE_ISSKIPPER:
                return true;
            case TYPE_NOTE:
                if (command == MXMidiStatic.COMMAND_CH_NOTEON
                        || command == MXMidiStatic.COMMAND_CH_NOTEOFF) {
                    return true;
                }
                break;
            case TYPE_DAMPER_PEDAL:
                if (command == MXMidiStatic.COMMAND_CH_CONTROLCHANGE
                        && data1 == MXMidiStatic.DATA1_CC_DAMPERPEDAL) {
                    return true;
                }
                break;
            case TYPE_PITCH_BEND:
                if (command == MXMidiStatic.COMMAND_CH_PITCHWHEEL || command == MXMidiStatic.COMMAND2_CH_PITCH_MSBLSB) {
                    return true;
                }
                break;
            case TYPE_MOD_WHEEL:
                if (command == MXMidiStatic.COMMAND_CH_CONTROLCHANGE
                        && data1 == MXMidiStatic.DATA1_CC_MODULATION) {
                    return true;
                }
                break;
            case TYPE_BANK_SELECT:
                if (command == MXMidiStatic.COMMAND_CH_CONTROLCHANGE) {
                    switch (data1) {
                        case MXMidiStatic.DATA1_CC_BANKSELECT:
                        case MXMidiStatic.DATA1_CC_BANKSELECT + 0x20:
                            return true;
                    }
                }
                break;
            case TYPE_PROGRAM_CHANGE:
                if (command == MXMidiStatic.COMMAND_CH_PROGRAMCHANGE) {
                    return true;
                }
                break;
            case TYPE_DATA_ENTRY:
                if (command == MXMidiStatic.COMMAND_CH_CONTROLCHANGE) {
                    switch (data1) {
                        case MXMidiStatic.DATA1_CC_DATAENTRY:
                        case MXMidiStatic.DATA1_CC_DATAENTRY2:
                        case MXMidiStatic.DATA1_CC_DATAINC:
                        case MXMidiStatic.DATA1_CC_DATADEC:
                        case MXMidiStatic.DATA1_CC_RPN_LSB:
                        case MXMidiStatic.DATA1_CC_RPN_MSB:
                        case MXMidiStatic.DATA1_CC_NRPN_LSB:
                        case MXMidiStatic.DATA1_CC_NRPN_MSB:
                            return true;
                    }
                }
                break;
            case TYPE_ANOTHER_CC:
                if (command == MXMidiStatic.COMMAND_CH_CONTROLCHANGE) {
                    switch (data1) {
                        case MXMidiStatic.DATA1_CC_DAMPERPEDAL:
                        case MXMidiStatic.DATA1_CC_MODULATION:
                        case MXMidiStatic.DATA1_CC_BANKSELECT:
                        case MXMidiStatic.DATA1_CC_BANKSELECT + 0x20:
                        case MXMidiStatic.DATA1_CC_DATAENTRY:
                        case MXMidiStatic.DATA1_CC_DATAENTRY2:
                        case MXMidiStatic.DATA1_CC_DATAINC:
                        case MXMidiStatic.DATA1_CC_DATADEC:
                        case MXMidiStatic.DATA1_CC_RPN_LSB:
                        case MXMidiStatic.DATA1_CC_RPN_MSB:
                        case MXMidiStatic.DATA1_CC_NRPN_LSB:
                        case MXMidiStatic.DATA1_CC_NRPN_MSB:
                            return false;
                    }
                    return true;
                }
                break;
            case TYPE_RESET_GENERAL:
                if (message.isSysexOrMeta()) {
                    if (MXMidiStatic.isReset(message.toOneMessage(0).getBinary())) {
                        return true;
                    }
                }
                break;
            case TYPE_SYSEX:
                if (command == MXMidiStatic.COMMAND_SYSEX || command == MXMidiStatic.COMMAND_SYSEX_END){
                    return true;
                }
                break;
            case TYPE_CLOCK:
                if (command == MXMidiStatic.COMMAND_TRANSPORT_MIDICLOCK) {
                    return true;
                }
                break;
            case TYPE_ACTIVE_SENSING:
                if (command == MXMidiStatic.COMMAND_ACTIVESENSING) {
                    return true;
                }
                break;
        }
        return false;
    }
    
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < COUNT_TYPE; ++ i) {
            if (isChecked(i)) {
                if (str.length() != 0) {
                    str.append(", ");
                }
                str.append(getName(i));
            }
        }
        return str.toString();
    }
}
