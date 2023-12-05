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
package jp.synthtarou.midimixer.libs.midi;

import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.ccxml.CXGeneralMidiFile;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import static jp.synthtarou.midimixer.libs.midi.MXMidi.nameOfChannelMessage;
import static jp.synthtarou.midimixer.libs.midi.MXMidi.nameOfControlChange;
import static jp.synthtarou.midimixer.libs.midi.MXMidi.nameOfNote;
import static jp.synthtarou.midimixer.libs.midi.MXMidi.nameOfSystemCommonMessage;
import static jp.synthtarou.midimixer.libs.midi.MXMidi.nameOfSystemRealtimeMessage;
import jp.synthtarou.midimixer.mx30surface.MGStatus;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXWrapListFactory {

    public static MXWrapList<Integer> listupGate7Bit() {
        MXWrapList<Integer> newList = new MXWrapList<>();
        for (int i = 0; i < 128; ++i) {
            newList.addNameAndValue(String.valueOf(i), i);
        }
        return newList;
    }

    public static MXWrapList<Integer> listupProgramNumber(boolean showNumber) {
        CXGeneralMidiFile gmfile = CXGeneralMidiFile.getInstance();
        MXWrapList<Integer> newList = new MXWrapList<>();
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

    public static MXWrapList<Integer> listupDrumnote(boolean showNumber) {
        CXGeneralMidiFile gmfile = CXGeneralMidiFile.getInstance();
        MXWrapList<Integer> newList = new MXWrapList<>();
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

    public static MXWrapList<Integer> listupVelocity() {
        return listupRange(0, 127);
    }

    public static MXWrapList<Integer> listupRange(int from, int to) {
        MXWrapList<Integer> list = new MXWrapList<Integer>();
        for (int i = from; i <= to; ++i) {
            list.addNameAndValue(String.valueOf(i), i);
        }
        return list;
    }

    public static MXWrapList<Integer> listupCommand(boolean showNumber) {
        MXWrapList<Integer> list = new MXWrapList<>();
        for (int i = 128; i <= 240; i += 16) {
            String name = nameOfChannelMessage(i);
            if (showNumber) {
                name = "" + i + ". " + name;
            }
            list.addNameAndValue(name, i);
        }
        return list;
    }

    public static MXWrapList<Integer> listupSystemModeOneShot(boolean showNumber) {
        MXWrapList<Integer> list = new MXWrapList<>();
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

    public static MXWrapList<Integer> listupChannel(String addMinusOne) {
        MXWrapList<Integer> list = new MXWrapList();
        if (addMinusOne != null) {
            list.addNameAndValue(addMinusOne, -1);
        }
        for (int i = 0; i < 16; ++i) {
            list.addNameAndValue(String.valueOf(i + 1), i);
        }
        return list;
    }

    public static MXWrapList<Integer> listupColumn(String addMinusOne) {
        MXWrapList<Integer> list = new MXWrapList();
        if (addMinusOne != null) {
            list.addNameAndValue(addMinusOne, -1);
        }
        for (int i = 0; i < MXAppConfig.SLIDER_COLUMN_COUNT; ++i) {
            list.addNameAndValue(String.valueOf(i + 1), i);
        }
        return list;
    }

    public static MXWrapList<Integer> listChannelModeSlider() {
        MXWrapList<Integer> list = new MXWrapList<>();
        for (int i = 120; i <= 127; ++i) {
            if (i == 122) {
                String name = nameOfControlChange(i);
                list.addNameAndValue(name + "(" + i + ")", i);
            }
        }
        return list;
    }

    public static MXWrapList<Integer> listupPercent() {
        MXWrapList<Integer> list = new MXWrapList();
        for (int i = 100; i >= 0; i -= 10) {
            list.addNameAndValue(String.valueOf(i) + "%", i);
        }
        return list;
    }

    public static MXWrapList<Integer> listupPort(String addMinusOne) {
        MXWrapList<Integer> list = new MXWrapList();
        if (addMinusOne != null) {
            list.addNameAndValue(addMinusOne, -1);
        }
        for (int i = 0; i < MXAppConfig.TOTAL_PORT_COUNT; ++i) {
            list.addNameAndValue(Character.toString('A' + i), i);
        }
        return list;
    }

    public static MXWrapList<Integer> listupChannelModeOnShot(boolean showNumber) {
        MXWrapList<Integer> list = new MXWrapList<>();
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

    public static MXWrapList<Integer> listupControlChange(boolean showNumber) {
        MXWrapList<Integer> list = new MXWrapList<>();
        for (int i = 0; i <= 119; ++i) {
            String name = nameOfControlChange(i);
            if (showNumber) {
                name = "" + i + ". " + name;
            }
            list.addNameAndValue(name, i);
        }
        return list;
    }

    public static MXWrapList<Integer> listupNoteNo(boolean showNumber) {
        MXWrapList<Integer> list = new MXWrapList();
        for (int i = 0; i <= 127; ++i) {
            String name = nameOfNote(i);
            if (showNumber) {
                name = "" + i + ". " + name;
            }
            list.addNameAndValue(name, i);
        }
        return list;
    }

    public static MXWrapList<Integer> listupZero() {
        MXWrapList<Integer> list = new MXWrapList<Integer>();
        list.addNameAndValue("---", 0);
        return list;
    }
}
