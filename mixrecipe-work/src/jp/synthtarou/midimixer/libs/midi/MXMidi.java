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
import jp.synthtarou.midimixer.libs.common.MXUtil;
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
}
