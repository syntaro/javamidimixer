/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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
package jp.synthtarou.libs.namedobject;

import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.ccxml.xml.CXGeneralMidiFile;
import static jp.synthtarou.midimixer.libs.midi.MXMidi.nameOfChannelMessage;
import static jp.synthtarou.midimixer.libs.midi.MXMidi.nameOfControlChange;
import static jp.synthtarou.midimixer.libs.midi.MXMidi.nameOfNote;
import static jp.synthtarou.midimixer.libs.midi.MXMidi.nameOfSystemCommonMessage;
import static jp.synthtarou.midimixer.libs.midi.MXMidi.nameOfSystemRealtimeMessage;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXNamedObjectListFactory {

    public static MXNamedObjectList<Integer> listupGate7Bit() {
        MXNamedObjectList<Integer> newList = new MXNamedObjectList<>();
        for (int i = 0; i < 128; ++i) {
            newList.addNameAndValue(String.valueOf(i), i);
        }
        return newList;
    }
    
    public static MXNamedObjectList<Integer> listupGeneralMidi(boolean showNumber) {
        CXGeneralMidiFile gmfile = CXGeneralMidiFile.getInstance();
        MXNamedObjectList<Integer> newList = new MXNamedObjectList<>();
        for (int i = 0; i < 128; ++i) {
            String name = CXGeneralMidiFile.getInstance().simpleFindProgramName(i);
            if (name == null) {
                name = "";
            }
            if (showNumber) {
                name = "" + i + ". " + name;
            }
            newList.addNameAndValue(name, i);
        }
        return newList;
    }

    public static MXNamedObjectList<Integer> listupDrumnote(boolean showNumber) {
        CXGeneralMidiFile gmfile = CXGeneralMidiFile.getInstance();
        MXNamedObjectList<Integer> newList = new MXNamedObjectList<>();
        for (int i = 0; i < 128; ++i) {
            String name = CXGeneralMidiFile.getInstance().simpleFindDrumName(i);
            if (name == null) {
                name = "";
            }
            if (showNumber) {
                name = "" + i + ". " + name;
            }
            newList.addNameAndValue(name, i);
        }
        return newList;
    }

    public static MXNamedObjectList<Integer> listupVelocity() {
        return listupRange(0, 127);
    }

    public static MXNamedObjectList<Integer> listupRange(int from, int to) {
        MXNamedObjectList<Integer> list = new MXNamedObjectList<Integer>();
        for (int i = from; i <= to; ++i) {
            list.addNameAndValue(String.valueOf(i), i);
        }
        return list;
    }

    public static MXNamedObjectList<Integer> listupCommand(boolean showNumber) {
        MXNamedObjectList<Integer> list = new MXNamedObjectList<>();
        for (int i = 128; i <= 240; i += 16) {
            String name = nameOfChannelMessage(i);
            if (showNumber) {
                name = "" + i + ". " + name;
            }
            list.addNameAndValue(name, i);
        }
        return list;
    }

    public static MXNamedObjectList<Integer> listupSystemModeOneShot(boolean showNumber) {
        MXNamedObjectList<Integer> list = new MXNamedObjectList<>();
        for (int i = 240; i <= 247; ++i) {
            String name = nameOfSystemCommonMessage(i);
            if (showNumber) {
                name = "" + i + ". " + name;
            }
            list.addNameAndValue(name, i);
        }
        for (int i = 248; i <= 255; ++i) {
            String name = nameOfSystemRealtimeMessage(i);
            if (showNumber) {
                name = "" + i + ". " + name;
            }
            list.addNameAndValue(name, i);
        }
        return list;
    }

    public static MXNamedObjectList<Integer> listupChannel(String addMinusOne) {
        MXNamedObjectList<Integer> list = new MXNamedObjectList();
        if (addMinusOne != null) {
            list.addNameAndValue(addMinusOne, -1);
        }
        for (int i = 0; i < 16; ++i) {
            list.addNameAndValue(String.valueOf(i + 1), i);
        }
        return list;
    }

    public static MXNamedObjectList<Integer> listupColumn(String addMinusOne) {
        MXNamedObjectList<Integer> list = new MXNamedObjectList();
        if (addMinusOne != null) {
            list.addNameAndValue(addMinusOne, -1);
        }
        for (int i = 0; i < MXConfiguration.SLIDER_COLUMN_COUNT; ++i) {
            list.addNameAndValue(String.valueOf(i + 1), i);
        }
        return list;
    }

    public static MXNamedObjectList<Integer> listChannelModeSlider() {
        MXNamedObjectList<Integer> list = new MXNamedObjectList<>();
        for (int i = 120; i <= 127; ++i) {
            if (i == 122) {
                String name = nameOfControlChange(i);
                list.addNameAndValue(name + "(" + i + ")", i);
            }
        }
        return list;
    }

    public static MXNamedObjectList<Integer> listupPercent() {
        MXNamedObjectList<Integer> list = new MXNamedObjectList();
        for (int i = 100; i >= 0; i -= 10) {
            list.addNameAndValue(String.valueOf(i) + "%", i);
        }
        return list;
    }

    public static MXNamedObjectList<Integer> listupPort(String addMinusOne) {
        MXNamedObjectList<Integer> list = new MXNamedObjectList();
        if (addMinusOne != null) {
            list.addNameAndValue(addMinusOne, -1);
        }
        for (int i = 0; i < MXConfiguration.TOTAL_PORT_COUNT; ++i) {
            list.addNameAndValue(Character.toString('A' + i), i);
        }
        return list;
    }

    public static MXNamedObjectList<Integer> listupChannelModeOnShot(boolean showNumber) {
        MXNamedObjectList<Integer> list = new MXNamedObjectList<>();
        for (int i = 120; i <= 127; ++i) {
            if (i != 122) {
                String name = nameOfControlChange(i);
                if (showNumber) {
                    name = "" + i + ". " + name;
                }
                list.addNameAndValue(name, i);
            }
        }
        return list;
    }

    public static MXNamedObjectList<Integer> listupControlChange(boolean showNumber) {
        MXNamedObjectList<Integer> list = new MXNamedObjectList<>();
        for (int i = 0; i <= 119; ++i) {
            String name = nameOfControlChange(i);
            if (showNumber) {
                name = "" + i + ". " + name;
            }
            list.addNameAndValue(name, i);
        }
        return list;
    }

    public static MXNamedObjectList<Integer> listupNoteNo(boolean showNumber) {
        MXNamedObjectList<Integer> list = new MXNamedObjectList();
        for (int i = 0; i <= 127; ++i) {
            String name = nameOfNote(i);
            if (showNumber) {
                name = "" + i + ". " + name;
            }
            list.addNameAndValue(name, i);
        }
        return list;
    }

    public static MXNamedObjectList<Integer> listupZero() {
        MXNamedObjectList<Integer> list = new MXNamedObjectList<Integer>();
        list.addNameAndValue("---", 0);
        return list;
    }
}
