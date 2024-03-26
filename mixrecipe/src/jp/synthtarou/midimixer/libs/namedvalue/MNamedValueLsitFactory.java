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
package jp.synthtarou.midimixer.libs.namedvalue;

import jp.synthtarou.midimixer.MXAppConfig;
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
public class MNamedValueLsitFactory {

    public static MNamedValueList<Integer> listupGate7Bit() {
        MNamedValueList<Integer> newList = new MNamedValueList<>();
        for (int i = 0; i < 128; ++i) {
            newList.addNameAndValue(String.valueOf(i), i);
        }
        return newList;
    }

    public static MNamedValueList<Integer> listupGeneralMidi(boolean showNumber) {
        CXGeneralMidiFile gmfile = CXGeneralMidiFile.getInstance();
        MNamedValueList<Integer> newList = new MNamedValueList<>();
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

    public static MNamedValueList<Integer> listupDrumnote(boolean showNumber) {
        CXGeneralMidiFile gmfile = CXGeneralMidiFile.getInstance();
        MNamedValueList<Integer> newList = new MNamedValueList<>();
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

    public static MNamedValueList<Integer> listupVelocity() {
        return listupRange(0, 127);
    }

    public static MNamedValueList<Integer> listupRange(int from, int to) {
        MNamedValueList<Integer> list = new MNamedValueList<Integer>();
        for (int i = from; i <= to; ++i) {
            list.addNameAndValue(String.valueOf(i), i);
        }
        return list;
    }

    public static MNamedValueList<Integer> listupCommand(boolean showNumber) {
        MNamedValueList<Integer> list = new MNamedValueList<>();
        for (int i = 128; i <= 240; i += 16) {
            String name = nameOfChannelMessage(i);
            if (showNumber) {
                name = "" + i + ". " + name;
            }
            list.addNameAndValue(name, i);
        }
        return list;
    }

    public static MNamedValueList<Integer> listupSystemModeOneShot(boolean showNumber) {
        MNamedValueList<Integer> list = new MNamedValueList<>();
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

    public static MNamedValueList<Integer> listupChannel(String addMinusOne) {
        MNamedValueList<Integer> list = new MNamedValueList();
        if (addMinusOne != null) {
            list.addNameAndValue(addMinusOne, -1);
        }
        for (int i = 0; i < 16; ++i) {
            list.addNameAndValue(String.valueOf(i + 1), i);
        }
        return list;
    }

    public static MNamedValueList<Integer> listupColumn(String addMinusOne) {
        MNamedValueList<Integer> list = new MNamedValueList();
        if (addMinusOne != null) {
            list.addNameAndValue(addMinusOne, -1);
        }
        for (int i = 0; i < MXAppConfig.SLIDER_COLUMN_COUNT; ++i) {
            list.addNameAndValue(String.valueOf(i + 1), i);
        }
        return list;
    }

    public static MNamedValueList<Integer> listChannelModeSlider() {
        MNamedValueList<Integer> list = new MNamedValueList<>();
        for (int i = 120; i <= 127; ++i) {
            if (i == 122) {
                String name = nameOfControlChange(i);
                list.addNameAndValue(name + "(" + i + ")", i);
            }
        }
        return list;
    }

    public static MNamedValueList<Integer> listupPercent() {
        MNamedValueList<Integer> list = new MNamedValueList();
        for (int i = 100; i >= 0; i -= 10) {
            list.addNameAndValue(String.valueOf(i) + "%", i);
        }
        return list;
    }

    public static MNamedValueList<Integer> listupPort(String addMinusOne) {
        MNamedValueList<Integer> list = new MNamedValueList();
        if (addMinusOne != null) {
            list.addNameAndValue(addMinusOne, -1);
        }
        for (int i = 0; i < MXAppConfig.TOTAL_PORT_COUNT; ++i) {
            list.addNameAndValue(Character.toString('A' + i), i);
        }
        return list;
    }

    public static MNamedValueList<Integer> listupChannelModeOnShot(boolean showNumber) {
        MNamedValueList<Integer> list = new MNamedValueList<>();
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

    public static MNamedValueList<Integer> listupControlChange(boolean showNumber) {
        MNamedValueList<Integer> list = new MNamedValueList<>();
        for (int i = 0; i <= 119; ++i) {
            String name = nameOfControlChange(i);
            if (showNumber) {
                name = "" + i + ". " + name;
            }
            list.addNameAndValue(name, i);
        }
        return list;
    }

    public static MNamedValueList<Integer> listupNoteNo(boolean showNumber) {
        MNamedValueList<Integer> list = new MNamedValueList();
        for (int i = 0; i <= 127; ++i) {
            String name = nameOfNote(i);
            if (showNumber) {
                name = "" + i + ". " + name;
            }
            list.addNameAndValue(name, i);
        }
        return list;
    }

    public static MNamedValueList<Integer> listupZero() {
        MNamedValueList<Integer> list = new MNamedValueList<Integer>();
        list.addNameAndValue("---", 0);
        return list;
    }
}
