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
package jp.synthtarou.midimixer.mx10input;

import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX10Data {
    private static final MXDebugPrint _debug = new MXDebugPrint(MX10Data.class);

    public static final int TYPE_NOTE = 1;
    public static final int TYPE_DAMPER_PEDAL = 2;
    public static final int TYPE_PITCH_BEND = 3; 
    public static final int TYPE_MOD_WHEEL = 4;
    public static final int TYPE_BANK_SELECT = 5;
    public static final int TYPE_PROGRAM_CHANGE  = 6;
    public static final int TYPE_DATA_ENTRY = 7;
    public static final int TYPE_ANOTHER_CC = 8;
    public static final int TYPE_SYSEX = 9;
    public static final int TYPE_ACTIVE_SENSING = 10;

    public static final int TYPE_COUNT = 10;
    
    public static final int TYPE_ZERO = 0;

    public static final String[] typeNames = {
        "Note", "DamperPedal", "PitchBend", "ModWheel", "BankChange",
        "ProgramChange", "DataEntry", "AnotherCC", "SysEX",
        "Active&Clock"
    };
    
    long[] _whichToSkip;
    int _portCount;

    public MX10Data() {
        _portCount = MXAppConfig.TOTAL_PORT_COUNT;
        _whichToSkip = new long[MXAppConfig.TOTAL_PORT_COUNT];
        for (int i = 0; i < _whichToSkip.length; ++ i) {
            _whichToSkip[i] = 1L << TYPE_ACTIVE_SENSING;
        }
    }
    
    public boolean isMarkedToSkip(MXMessage message) {
        if (message == null) {
            return true;
        }
        int port = message.getPort();
        int command = message.getStatus();
        if (command >= 0x80 && command <= 0xef) {
            command &= 0xf0;
        }

        int type = TYPE_ZERO;

        int data1 = message.getGate()._var;
    
        if (command == MXMidi.COMMAND_NOTEON || command == MXMidi.COMMAND_NOTEOFF) {
            type = TYPE_NOTE;
        }else if (command == MXMidi.COMMAND_CONTROLCHANGE && data1 == MXMidi.DATA1_CC_DAMPERPEDAL) {
                type = TYPE_DAMPER_PEDAL;
        }else if (command == MXMidi.COMMAND_PITCHWHEEL) {
            type = TYPE_PITCH_BEND;
        }else if (command == MXMidi.COMMAND_CONTROLCHANGE && data1 == MXMidi.DATA1_CC_MODULATION) {
            type = TYPE_MOD_WHEEL;
        }else if (command == MXMidi.COMMAND_CONTROLCHANGE && (data1 == MXMidi.DATA1_CC_BANKSELECT || data1 == MXMidi.DATA1_CC_BANKSELECT + 0x20)) {
            type = TYPE_BANK_SELECT;
        }else if (command == MXMidi.COMMAND_PROGRAMCHANGE) {
            type = TYPE_PROGRAM_CHANGE;
        }else if (command == MXMidi.COMMAND_CONTROLCHANGE && (data1 == MXMidi.DATA1_CC_DATAENTRY || data1 == MXMidi.DATA1_CC_DATAINC || data1 == MXMidi.DATA1_CC_DATADEC)) {
            type = TYPE_DATA_ENTRY;
        }else if (command == MXMidi.COMMAND_CONTROLCHANGE && data1 >= MXMidi.DATA1_CC_NRPN_LSB && data1 <= MXMidi.DATA1_CC_RPN_MSB) {
            type = TYPE_DATA_ENTRY;
        }else if (command == MXMidi.COMMAND_CONTROLCHANGE) {
            type = TYPE_ANOTHER_CC;
        } else if (command == 0xf0 || command == 0xf7) {
            type = TYPE_SYSEX;
        } else  if (command == MXMidi.STATUS_ACTIVESENSING || command == MXMidi.STATUS_MIDICLOCK) {
            type = TYPE_ACTIVE_SENSING;
        }
        return isSkip(port, type);
    }
    
    public void setSkip(int port, int type, boolean skipFlag) {
        long bit = 1L << type;
        if ((_whichToSkip[port] & bit) != 0) {
            if (skipFlag) {
                return;
            }else {
                _whichToSkip[port] -= bit;
            }
        }else {
            if (skipFlag) {
                _whichToSkip[port] |= bit;
            }else {
                return;
            }
        }
    }
    
    public boolean isSkip(int port, int type) {
        long bit = 1L << type;
        if ((_whichToSkip[port] & bit) != 0) {
            return true;
        }
        return false;
    }
    
    public static int countOfTypes() {
        return TYPE_COUNT;
    }

    public static String nameOfType(int x) {
        return typeNames[x];
    }
}
