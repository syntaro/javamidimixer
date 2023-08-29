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
package jp.synthtarou.midimixer.libs.midi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.text.MXLineReader;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMidi {
    private static final MXDebugPrint _debug = new MXDebugPrint(MXMidi.class);

    public static void main(String[] args) {
        MXDebugPrint.globalSwitchOn();
        //出力したあと手動で補正する
        File dir = new File("C:/java/release/2022-02-work/src");
        File file = new File(dir, "jp/synthtarou/midimixer/libs/midi/MXMidi.java");
        InputStream in = null;
        
        try {
            in = new FileInputStream(file);
            MXLineReader reader = new MXLineReader(in, "utf-8");
            ArrayList<String> sort = new ArrayList();
            while(true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                String text = "public static final int";

                int x = line.indexOf(text);
                if (x >= 0) {
                    x += text.length() + 1;

                    String sub = line.substring(x);
                    
                    int y = sub.indexOf("=");
                    if (y >= 0) {
                        sort.add(line);
                    }
                }
            }
            sort.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    int x1 = o1.indexOf("=");
                    int x2 = o2.indexOf("=");
                    if (x1 < 0) {
                        if (x2 < 0) {
                            return o1.compareTo(o2);
                        }
                        return -1;
                    }
                    if (x2 < 0) {
                        return 1;
                    }
                    o1 = o1.substring(x1 + 1);
                    o2 = o2.substring(x2 + 1);

                    int i1 = MXUtil.numberFromText(o1);
                    int i2 = MXUtil.numberFromText(o2);

                    if (i1 < i2) return -1;
                    if (i1 > i2) return +1;
                    return o1.compareTo(o2);
                }
            });
            Iterator<String> it = sort.iterator();
            while(it.hasNext()) {
                String s = it.next();
                _debug.println(s);
            }
        }catch(IOException e) {
            _debug.printStackTrace(e);
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                _debug.printStackTrace(e);
            }
        }
    }
    
    
    public static final int COMMAND_NOTEOFF = 0x80;
    public static final int COMMAND_NOTEON = 0x90;
    public static final int COMMAND_POLYPRESSURE = 0xa0;
    public static final int COMMAND_CONTROLCHANGE = 0xb0;
    public static final int COMMAND_PROGRAMCHANGE = 0xc0;
    public static final int COMMAND_CHANNELPRESSURE = 0xd0;
    public static final int COMMAND_PITCHWHEEL = 0xe0;
    public static final int COMMAND_SYSEX = 0xf0;
    
    public static final int DATA1_CC_BANKSELECT = 0;
    public static final int DATA1_CC_MODULATION = 1;
    public static final int DATA1_CC_BREATH = 2;
    public static final int DATA1_CC_3 = 3;
    public static final int DATA1_CC_FOOTCONTROL = 4;
    public static final int DATA1_CC_PORTAMENTTIME = 5;
    public static final int DATA1_CC_DATAENTRY = 6;
    public static final int DATA1_CC_CHANNEL_VOLUME = 7;
    public static final int DATA1_CC_BALANCE = 8;
    public static final int DATA1_CC_9 = 9;
    public static final int DATA1_CC_PANPOT = 10;
    public static final int DATA1_CC_EXPRESSION = 11;
    public static final int DATA1_CC_EFFECTCONTROL1 = 12;
    public static final int DATA1_CC_EFFECTCONTROL2 = 13;
    public static final int DATA1_CC_14 = 14;
    public static final int DATA1_CC_15 = 15;
    public static final int DATA1_CC_COMMON1 = 16;
    public static final int DATA1_CC_COMMON2 = 17;
    public static final int DATA1_CC_COMMON3 = 18;
    public static final int DATA1_CC_COMMON4 = 19;
    public static final int DATA1_CC_DAMPERPEDAL = 64;
    public static final int DATA1_CC_PORTAMENT = 65;
    public static final int DATA1_CC_FOOT_SOFTENUT = 66;
    public static final int DATA1_CC_FOOT_SOFT = 67;
    public static final int DATA1_CC_FOOT_LEGATO = 68; 
    public static final int DATA1_CC_HOLD2_FREEZE = 69;
    public static final int DATA1_CC_SOUND_VALIATION = 70;
    public static final int DATA1_CC_SOUND_TIMBER = 71;
    public static final int DATA1_CC_SOUND_RELEASETIME = 72;
    public static final int DATA1_CC_SOUND_ATTACKTIME = 73;
    public static final int DATA1_CC_SOUND_BLIGHTNESS = 74;
    public static final int DATA1_CC_SOUND_DECAYTIME = 75;
    public static final int DATA1_CC_SOUND_VIBRATE_RATE = 76;
    public static final int DATA1_CC_SOUND_VIBRATE_DEPTH = 77;
    public static final int DATA1_CC_SOUND_VIBRATE_DELAY = 78;
    public static final int DATA1_CC_79 = 79;
    public static final int DATA1_CC_COMMON5 = 80;
    public static final int DATA1_CC_COMMON6 = 81;
    public static final int DATA1_CC_COMMON7 = 82;
    public static final int DATA1_CC_COMMON8 = 83;
    public static final int DATA1_CC_CONTROL_SOURCENOTE = 84;
    public static final int DATA1_CC_85 = 85;
    public static final int DATA1_CC_86 = 86;
    public static final int DATA1_CC_87 = 87;
    public static final int DATA1_CC_VELOCITYHQ = 88;
    public static final int DATA1_CC_89 = 89;
    public static final int DATA1_CC_90 = 90;
    public static final int DATA1_CC_EFFECT1_REVERVE = 91;
    public static final int DATA1_CC_EFFECT2_TREMOLO = 92;
    public static final int DATA1_CC_EFFECT3_CHORUS = 93;
    public static final int DATA1_CC_EFFECT4_DETUNE = 94;
    public static final int DATA1_CC_EFFECT5_PHASER = 95;
    public static final int DATA1_CC_DATAINC = 96;
    public static final int DATA1_CC_DATADEC = 97;
    public static final int DATA1_CC_NRPN_LSB = 98;
    public static final int DATA1_CC_NRPN_MSB = 99;
    public static final int DATA1_CC_RPN_LSB = 100;
    public static final int DATA1_CC_RPN_MSB = 101;
    public static final int DATA1_CC_ALLSOUNDOFF = 120;
    public static final int DATA1_CC_RESET_ALLCTRLS = 121;
    public static final int DATA1_CC_LOCALCTRL = 122;
    public static final int DATA1_CC_ALLNOTEOFF = 123;
    public static final int DATA1_CC_OMNI_OFF = 124;
    public static final int DATA1_CC_OMNI_ON = 125;
    public static final int DATA1_CC_MONOMODE = 126;
    public static final int DATA1_CC_POLYMODE = 127;
    public static final int STATUS_SYSEXSTART = 240; //0xf0
    public static final int STATUS_MIDITIMECODE = 241;
    public static final int STATUS_SONGPOSITION = 242;
    public static final int STATUS_SONGSELECT = 243;
    public static final int STATUS_F4 = 244;
    public static final int STATUS_F5 = 245;
    public static final int STATUS_TUNEREQUEST = 246;
    public static final int STATUS_SYSEXFIN = 247;
    public static final int STATUS_MIDICLOCK = 248;
    public static final int STATUS_F9 = 249;
    public static final int STATUS_SEQSTART = 250;
    public static final int STATUS_SEQCONTINUE = 251;
    public static final int STATUS_SEQSTOP = 252;
    public static final int STATUS_FD = 253;
    public static final int STATUS_ACTIVESENSING = 254;
    public static final int STATUS_RESET = 255;

    public static final int MSB3D_AZIMUTH_ANGLE = 0;
    public static final int MSB3D_ELEVATION_ANGLE = 1;
    public static final int MSB3D_GAIN = 2;
    public static final int MSB3D_DISTANCE_RATIO = 3;
    public static final int MSB3D_MAXIMUM_DISTANCE = 4;
    public static final int MSB3_REFERENCE_DISTANCE_RATIO = 6;
    public static final int MSB3D_PAN_SPREAD_ANGLE = 7;
    public static final int MSB3D_ROLL_ANGLE = 8;

    public static final int MSB0_PITCHBEND_QUALITY = 0;
    public static final int MSB0_PITCHBEND_CHANNELFINETUNING = 1;
    public static final int MSB0_PITCHBEND_CHANNELCOURSETUNING = 2;
    public static final int MSB0_PITCHBEND_TUNINPROGRAMCHANGE = 3;
    public static final int MSB0_PITCHBEND_TUNINGBANKSELECT = 4;
    public static final int MSB0_PITCHBEND_MODULATIONDEPTHRANGE = 5;

    protected static final String[] noteSymbols = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public static MXWrapList<Integer> listupGate7Bit() {
        MXWrapList<Integer> newList = new MXWrapList<>();
        for(int i = 0; i < 128; ++ i) {
            newList.addNameAndValue(String.valueOf(i) , i);
        }
        return newList;
    }
    
    public static MXWrapList<Integer> listupProgramNumber() {
        //GMProgramList list = GMProgramList.getInstance();
        MXWrapList<Integer> newList = new MXWrapList<>();
        for (int i = 0; i < 128; ++i) {
            Integer number = i;
            //String name = list.nameOfIndex(i);
            newList.addNameAndValue("" + number, number);
        }
        return newList;
    }

    public static MXWrapList<Integer> listupVelocity() {
        MXWrapList<Integer> list = new MXWrapList<Integer>();
        for (int i = 0; i <= 127; ++i) {
            list.addNameAndValue(String.valueOf(i), i);
        }
        return list;
    }

    public static MXWrapList<Boolean> listupSendFilter() {
        MXWrapList<Boolean> list = new MXWrapList<Boolean>();
        list.addNameAndValue("Send", Boolean.TRUE);
        list.addNameAndValue("Don't send", Boolean.FALSE);
        return list;
    }

    public static MXWrapList<Integer> listupXSB() {
        MXWrapList<Integer> newList = new MXWrapList<>();
        for (int i = 0; i < 256; ++i) {
            String name = MXUtil.toHexFF(i);
            Integer number = i;
            newList.addNameAndValue(name, number);
        }
        return newList;
    }

    public static MXWrapList<Integer> listupCommand() {
        MXWrapList<Integer> list = new MXWrapList<>();
        for (int i = 128; i <= 240; i += 16) {
            String name = nameOfChannelMessage(i);
            String title = Integer.valueOf(i) + " " + name;
            list.addNameAndValue(title, i);
        }
        return list;
    }

    public static MXWrapList<Integer> listupSystemModeOneShot() {
        MXWrapList<Integer> list = new MXWrapList<>();
        for (int i = 240; i <= 247; ++i) {
            String name = nameOfSystemCommonMessage(i);
            list.addNameAndValue(name + "(" + i + ")", i);
        }
        for (int i = 248; i <= 255; ++i) {
            String name = nameOfSystemRealtimeMessage(i);
            list.addNameAndValue(name + "(" + i + ")", i);
        }
        return list;
    }

    public static MXWrapList<Integer> listupChannel(boolean addOmni) {
        MXWrapList<Integer> list = new MXWrapList();
        if (addOmni) {
            list.addNameAndValue("any", -1);
        }
        for (int i = 0; i < 16; ++i) {
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
    
    public static MXWrapList<Integer> listupPortShort() {
        MXWrapList<Integer> list = new MXWrapList();
        for (int i = 0; i < MXAppConfig.TOTAL_PORT_COUNT; ++i) {
            list.addNameAndValue(Character.toString('A' + i), i);
        }
        return list;
    }

    public static MXWrapList<Integer> listupPortAssigned(boolean addOmni) {
        MXWrapList<Integer> list = new MXWrapList();
        if (addOmni) {
            list.addNameAndValue("Any ", -1);
        }
        for (int i = 0; i < MXAppConfig.TOTAL_PORT_COUNT; ++i) {
            list.addNameAndValue(Character.toString('A' + i), i);
        }
        return list;
    }

    public static MXWrapList<Integer> listupChannelModeOnShot() {
        MXWrapList<Integer> list = new MXWrapList<>();
        for (int i = 120; i <= 127; ++i) {
            if (i != 122) {
                String name = nameOfControlChange(i);
                list.addNameAndValue(name + "(" + i + ")", i);
            }
        }
        return list;
    }

    public static MXWrapList<Integer> listupControlChange() {
        MXWrapList<Integer> list = new MXWrapList<>();
        for (int i = 0; i <= 119; ++i) {
            String name = nameOfControlChange(i);
            list.addNameAndValue(name + "(" + i + ")", i);
        }
        return list;
    }

    public static MXWrapList<Integer> listupNoteNo() {
        MXWrapList<Integer> list = new MXWrapList();
        for (int i = 0; i <= 127; ++i) {
            list.addNameAndValue(nameOfNote(i), i);
        }
        return list;
    }

    public static MXWrapList<Integer> listupZero() {
        MXWrapList<Integer> list = new MXWrapList<Integer>();
        list.addNameAndValue("---", 0);
        return list;
    }
    
    public static final String nameOfChannelMessage(int command) {
        switch (command) {
            case MXMidi.COMMAND_NOTEON:
                return "ON   ";
            case MXMidi.COMMAND_NOTEOFF:
                return "OFF  ";
            case MXMidi.COMMAND_POLYPRESSURE:
                return "PresP";
            case MXMidi.COMMAND_CONTROLCHANGE:
                return "CC   ";
            case MXMidi.COMMAND_PROGRAMCHANGE:
                return "PROG ";
            case MXMidi.COMMAND_CHANNELPRESSURE:
                return "Press";
            case MXMidi.COMMAND_PITCHWHEEL:
                return "PITCH";
            case MXMidi.STATUS_SYSEXSTART:
                return "SYSEX";
        }
        return null;
    }

    public static String nameOfNote(int noteNo) {
        int oct = (noteNo / 12) - 1;
        noteNo = noteNo % 12;
        return "" + noteSymbols[noteNo] + Integer.toString(oct);
    }

    static int[] alphaToNote = { 9, 11, 0, 2, 4, 5, 7 };

    public static int noteOfName(String note) {
        char ch = note.charAt(0);
        int base = -1;
        int index = 0;
        if (ch >= 'a' && ch <= 'z') {
            ch += 'A' - 'a';
        }
        if (ch >= 'A' && ch <= 'G') {
            int key =  alphaToNote[ch - 'A'];
            index ++;
            if (note.charAt(index) == '#') {
                key = key + 1;
                index ++;
            }
            int oct = Integer.parseInt(note.substring(index));
            key = key + oct * 12;
            return key;
        }
        return -1;
    }

    public static final String nameOfControlChange(int data1cc) {
        String namePrefix = "";
        switch (data1cc) {
            case MXMidi.DATA1_CC_BANKSELECT:
                return namePrefix + "BANK";
            case MXMidi.DATA1_CC_MODULATION:
                return namePrefix + "MODW";
            case MXMidi.DATA1_CC_BREATH:
                return namePrefix + "BRTH";
            case MXMidi.DATA1_CC_3:
                return namePrefix + "CC03";
            case MXMidi.DATA1_CC_FOOTCONTROL:
                return namePrefix + "FOOT";
            case MXMidi.DATA1_CC_PORTAMENTTIME:
                return namePrefix + "PRTA";
            case MXMidi.DATA1_CC_DATAENTRY:
                return namePrefix + "DATA ";
            case MXMidi.DATA1_CC_CHANNEL_VOLUME:
                return namePrefix + "VOL ";
            case MXMidi.DATA1_CC_BALANCE:
                return namePrefix + "BAL ";
            case MXMidi.DATA1_CC_9:
                return namePrefix + "CC09";
            case MXMidi.DATA1_CC_PANPOT:
                return namePrefix + "PAN ";
            case MXMidi.DATA1_CC_EXPRESSION:
                return namePrefix + "EXP ";
            case MXMidi.DATA1_CC_EFFECTCONTROL1:
                return namePrefix + "EFC1";
            case MXMidi.DATA1_CC_EFFECTCONTROL2:
                return namePrefix + "EFC2";
            case MXMidi.DATA1_CC_14:
                return namePrefix + "CC14";
            case MXMidi.DATA1_CC_15:
                return namePrefix + "CC15";
            case MXMidi.DATA1_CC_COMMON1:
                return namePrefix + "CMN1";
            case MXMidi.DATA1_CC_COMMON2:
                return namePrefix + "CMN2";
            case MXMidi.DATA1_CC_COMMON3:
                return namePrefix + "CMN3";
            case MXMidi.DATA1_CC_COMMON4:
                return namePrefix + "CMN4";
            case MXMidi.DATA1_CC_DAMPERPEDAL:
                return namePrefix + "DUMP";
            case MXMidi.DATA1_CC_PORTAMENT:
                return namePrefix + "PORT";
            case MXMidi.DATA1_CC_FOOT_SOFTENUT:
                return namePrefix + "SFTE";
            case MXMidi.DATA1_CC_FOOT_SOFT:
                return namePrefix + "SOFT";
            case MXMidi.DATA1_CC_FOOT_LEGATO:
                return namePrefix + "REGD";
            case MXMidi.DATA1_CC_HOLD2_FREEZE:
                return namePrefix + "FREZ";
            case MXMidi.DATA1_CC_SOUND_VALIATION:
                return namePrefix + "VALI";
            case MXMidi.DATA1_CC_SOUND_TIMBER:
                return namePrefix + "TMBR";
            case MXMidi.DATA1_CC_SOUND_RELEASETIME:
                return namePrefix + "RELS";
            case MXMidi.DATA1_CC_SOUND_ATTACKTIME:
                return namePrefix + "ATCK";
            case MXMidi.DATA1_CC_SOUND_BLIGHTNESS:
                return namePrefix + "BLIG";
            case MXMidi.DATA1_CC_SOUND_DECAYTIME:
                return namePrefix + "DCAY";
            case MXMidi.DATA1_CC_SOUND_VIBRATE_RATE:
                return namePrefix + "VRate";
            case MXMidi.DATA1_CC_SOUND_VIBRATE_DEPTH:
                return namePrefix + "VDpth";
            case MXMidi.DATA1_CC_SOUND_VIBRATE_DELAY:
                return namePrefix + "VDlay";
            case MXMidi.DATA1_CC_79:
                return namePrefix + "CC79";
            case MXMidi.DATA1_CC_COMMON5:
                return namePrefix + "CMN5";
            case MXMidi.DATA1_CC_COMMON6:
                return namePrefix + "CMN6";
            case MXMidi.DATA1_CC_COMMON7:
                return namePrefix + "CMN7";
            case MXMidi.DATA1_CC_COMMON8:
                return namePrefix + "CMN8";
            case MXMidi.DATA1_CC_CONTROL_SOURCENOTE:
                return namePrefix + "NOTE#";
            case MXMidi.DATA1_CC_85:
                return namePrefix + "CC85";
            case MXMidi.DATA1_CC_86:
                return namePrefix + "CC86";
            case MXMidi.DATA1_CC_87:
                return namePrefix + "CC87";
            case MXMidi.DATA1_CC_VELOCITYHQ:
                return namePrefix + "VEL2";
            case MXMidi.DATA1_CC_89:
                return namePrefix + "CC89";
            case MXMidi.DATA1_CC_90:
                return namePrefix + "CC90";
            case MXMidi.DATA1_CC_EFFECT1_REVERVE:
                return namePrefix + "REVR";
            case MXMidi.DATA1_CC_EFFECT2_TREMOLO:
                return namePrefix + "TRML";
            case MXMidi.DATA1_CC_EFFECT3_CHORUS:
                return namePrefix + "CHOR";
            case MXMidi.DATA1_CC_EFFECT4_DETUNE:
                return namePrefix + "DETU";
            case MXMidi.DATA1_CC_EFFECT5_PHASER:
                return namePrefix + "PHAS";
            case MXMidi.DATA1_CC_DATAINC:
                return namePrefix + "INC ";
            case MXMidi.DATA1_CC_DATADEC:
                return namePrefix + "DEC ";
            case MXMidi.DATA1_CC_NRPN_LSB:
                return namePrefix + "NRPN L";
            case MXMidi.DATA1_CC_NRPN_MSB:
                return namePrefix + "NRPN M";
            case MXMidi.DATA1_CC_RPN_LSB:
                return namePrefix + "RPN L";
            case MXMidi.DATA1_CC_RPN_MSB:
                return namePrefix + "RPN M";
            case MXMidi.DATA1_CC_ALLSOUNDOFF:
                return "AllOff";
            case MXMidi.DATA1_CC_RESET_ALLCTRLS:
                return "ResetCC";
            case MXMidi.DATA1_CC_LOCALCTRL:
                return "Local";
            case MXMidi.DATA1_CC_ALLNOTEOFF:
                return "AllNoteOff";
            case MXMidi.DATA1_CC_OMNI_OFF:
                return "OmniOff";
            case MXMidi.DATA1_CC_OMNI_ON:
                return "OmniOn";
            case MXMidi.DATA1_CC_MONOMODE:
                return "Mono";
            case MXMidi.DATA1_CC_POLYMODE:
                return "Poly";
        }
        return "#CC(" + data1cc + ")";
    }

    public static String nameOfSystemRealtimeMessage(int command) {
        switch (command) {
            case MXMidi.STATUS_MIDICLOCK:
                return "Clock";
            case MXMidi.STATUS_F9:
                return "#F9";
            case MXMidi.STATUS_SEQSTART:
                return "Seq Start";
            case MXMidi.STATUS_SEQCONTINUE:
                return "Seq Cont";
            case MXMidi.STATUS_SEQSTOP:
                return "Seq Stop";
            case MXMidi.STATUS_FD:
                return "#FD";
            case MXMidi.STATUS_ACTIVESENSING:
                return "Active";
            case MXMidi.STATUS_RESET:
                return "Reset";
        }
        return null;
    }

    public static String nameOfPortShort(int port) {
        if (port < 0) {
            return "-";
        }
        char portchar = (char) ('A' + port);
        return String.valueOf(portchar);
    }

    public static String nameOfPortInput(int port) {
        if (port < 0) {
            return "-";
        }
        char portchar = (char) ('A' + port);
        return String.valueOf(portchar);
    }

    public static String nameOfPortOutput(int port) {
        if (port < 0) {
            return "-";
        }
        char portchar = (char) ('A' + port);
        return String.valueOf(portchar);
    }

    public static String nameOfSystemCommonMessage(int status) {
        switch (status) {
            case MXMidi.STATUS_SYSEXSTART:
                return "SysEx[";
            case MXMidi.STATUS_MIDITIMECODE:
                return "Time  ";
            case MXMidi.STATUS_SONGPOSITION:
                return "SngPos";
            case MXMidi.STATUS_SONGSELECT:
                return "SngNum";
            case MXMidi.STATUS_F4:
                return "Sys F4";
            case MXMidi.STATUS_F5:
                return "Sys F5";
            case MXMidi.STATUS_TUNEREQUEST:
                return "Tuner ";
            case MXMidi.STATUS_SYSEXFIN:
                return "]SysEx";
        }
        return null;
    }

    public static String nameOfMessage(int status, int data1, int data2) {
        int command = status & 240;
        if (command == MXMidi.COMMAND_CONTROLCHANGE) {
            if (data1 == MXMidi.DATA1_CC_DATAINC) {
                return "INC";
            }
            if (data1 == MXMidi.DATA1_CC_DATADEC) {
                return "DEC";
            }
            if (data1 == MXMidi.DATA1_CC_DATAENTRY) {
                return "DATA";
            }
            return nameOfControlChange(data1);
        }
        if (command >= 128 && command <= 224) {
            String name = nameOfChannelMessage(command);
            if (command == MXMidi.COMMAND_NOTEON || command == MXMidi.COMMAND_NOTEOFF || command == MXMidi.COMMAND_POLYPRESSURE) {
                return name;
            } else {
                return name;
            }
        }
        if (status >= 240 && status <= 247) {
            return nameOfSystemCommonMessage(status);
        }
        if (status >= 248 && status <= 255) {
            return nameOfSystemRealtimeMessage(status);
        }
        return "?";
    }

    public static int valueOfPortName(String capital) {
        char portchar = capital.charAt(0);
        if (portchar == '(') {
            return -1;
        }
        return (int) portchar - 'A';
    }
    
    
    public static int[] textToNoteList(String text) {
        ArrayList<String> list = new ArrayList();
        MXUtil.split(text, list, ' ');
        ArrayList<Integer> retList = new ArrayList();

        for (String noteText : list) {
            int note = noteOfName(noteText);
            if (note >= 0) {
                retList.add(note);
            }else {
                System.out.println("parse error [note:" + noteText + "]");
            }
        }
        
        int[] ret = new int[retList.size()];
        for (int x = 0; x < ret.length; ++ x) {
            ret[x] = retList.get(x);
        }
        return ret;
    }
    
    public static String noteListToText(int[] note) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < note.length; ++ i) {
            str.append(MXMidi.nameOfNote(note[i]));
            str.append(" ");
        }
        return str.toString();
    }
}
