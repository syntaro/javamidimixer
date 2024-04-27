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
package jp.synthtarou.midimixer.mx13patch;

import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX13SignalType implements CheckableElement {

    public static final int TYPE_ALL = 0;
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

    public static final String[] typeNames = {
        "All", "Note", "DamperPedal", "PitchBend", "ModWheel", "BankChange",
        "ProgramChange", "DataEntry", "AnotherCC", "GM&GS&XGReset", "SysEX",
        "Clock", "Active"
    };
    
    public static int fromName(String name) {
        for (int i = 0; i < typeNames.length; ++ i) {
            if (typeNames[i].equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    public MX13SignalType(MX13To to, int type) {
        _type = type;
        _to = to;
        _name = MX13SignalType.typeNames[type];
    }

    MX13To _to;
    int _type;
    String _name;
    boolean _itemChecked;

    @Override
    public boolean isItemChecked() {
        return _itemChecked;
    }

    @Override
    public void setItemChecked(boolean checked) {
        if (checked != _itemChecked) {
            _itemChecked = checked;
            _to._from._process.setInformation();
        }
    }

    @Override
    public String itemToString() {
        return _name;
    }

    public boolean isSkip(MXMessage message) {
        int status = message.getStatus();
        int command = status;
        if (message.isChannelMessage1() || command >= 0x100) {
            command &= 0xfff0;
        }
        int data1 = message.getCompiled(1);
        int data2 = message.getCompiled(2);

        switch (_type) {
            case TYPE_ALL:
                return true;
            case TYPE_NOTE:
                if (command == MXMidi.COMMAND_CH_NOTEON
                        || command == MXMidi.COMMAND_CH_NOTEOFF) {
                    return true;
                }
                break;
            case TYPE_DAMPER_PEDAL:
                if (command == MXMidi.COMMAND_CH_CONTROLCHANGE
                        && data1 == MXMidi.DATA1_CC_DAMPERPEDAL) {
                    return true;
                }
                break;
            case TYPE_PITCH_BEND:
                if (command == MXMidi.COMMAND_CH_PITCHWHEEL || command == MXMidi.COMMAND2_CH_PITCH_MSBLSB) {
                    return true;
                }
                break;
            case TYPE_MOD_WHEEL:
                if (command == MXMidi.COMMAND_CH_CONTROLCHANGE
                        && data1 == MXMidi.DATA1_CC_MODULATION) {
                    return true;
                }
                break;
            case TYPE_BANK_SELECT:
                if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                    switch (data1) {
                        case MXMidi.DATA1_CC_BANKSELECT:
                        case MXMidi.DATA1_CC_BANKSELECT + 0x20:
                            return true;
                    }
                }
                break;
            case TYPE_PROGRAM_CHANGE:
                if (command == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
                    return true;
                }
                break;
            case TYPE_DATA_ENTRY:
                if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                    switch (data1) {
                        case MXMidi.DATA1_CC_DATAENTRY:
                        case MXMidi.DATA1_CC_DATAENTRY2:
                        case MXMidi.DATA1_CC_DATAINC:
                        case MXMidi.DATA1_CC_DATADEC:
                        case MXMidi.DATA1_CC_RPN_LSB:
                        case MXMidi.DATA1_CC_RPN_MSB:
                        case MXMidi.DATA1_CC_NRPN_LSB:
                        case MXMidi.DATA1_CC_NRPN_MSB:
                            return true;
                    }
                }
                break;
            case TYPE_ANOTHER_CC:
                if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                    switch (data1) {
                        case MXMidi.DATA1_CC_DAMPERPEDAL:
                        case MXMidi.DATA1_CC_MODULATION:
                        case MXMidi.DATA1_CC_BANKSELECT:
                        case MXMidi.DATA1_CC_BANKSELECT + 0x20:
                        case MXMidi.DATA1_CC_DATAENTRY:
                        case MXMidi.DATA1_CC_DATAENTRY2:
                        case MXMidi.DATA1_CC_DATAINC:
                        case MXMidi.DATA1_CC_DATADEC:
                        case MXMidi.DATA1_CC_RPN_LSB:
                        case MXMidi.DATA1_CC_RPN_MSB:
                        case MXMidi.DATA1_CC_NRPN_LSB:
                        case MXMidi.DATA1_CC_NRPN_MSB:
                            return false;
                    }
                    return true;
                }
                break;
            case TYPE_RESET_GENERAL:
                if (message.isBinaryMessage()) {
                    if (MXMidi.isReset(message.getBinary())) {
                        return true;
                    }
                }
                break;
            case TYPE_SYSEX:
                if (command == MXMidi.COMMAND_SYSEX || command == MXMidi.COMMAND_SYSEX_END){
                    return true;
                }
                break;
            case TYPE_CLOCK:
                if (command == MXMidi.COMMAND_MIDICLOCK) {
                    return true;
                }
                break;
            case TYPE_ACTIVE_SENSING:
                if (command == MXMidi.COMMAND_ACTIVESENSING) {
                    return true;
                }
                break;
        }
        return false;
    }
}
