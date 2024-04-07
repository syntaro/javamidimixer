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
package jp.synthtarou.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.SysexMessage;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.libs.midi.sysex.SplittableSysexMessage;
import jp.synthtarou.midimixer.libs.midi.sysex.SysexSplitter;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SysexSplitTest {

    public static int AMERICA_FROM = 0x01;
    public static int AMERICA_TO = 0x1F;

    public static int AMERICA_SEQUENCIAL = 0x01;
    public static int AMERICA_CLARITY = 0x1F;

    public static int EUROPE_FROM = 0x20;
    public static int EUROPE_TO = 0x3F;

    public static int EUROPE_PASSAC = 0x20;
    public static int EUROPE_WERCI = 0x3B;

    public static int JAPAN_FROM = 0x40;
    public static int JAPAN_TO = 0x5F;

    public static int JAPAN_KAWAI = 0x40;
    public static int JAPAN_ROLAND = 0x41;
    public static int JAPAN_KORG = 0x42;
    public static int JAPAN_YAMAHA = 0x43;
    public static int JAPAN_CASIO = 0x44;
    public static int JAPAN_KAMIYA_STUDIO = 0x46;
    public static int JAPAN_AKAI = 0x47;
    public static int JAPAN_JAPAN_VICTOR = 0x48;
    public static int JAPAN_MEISOSHA = 0x49;
    public static int JAPAN_HOSHINO_GAKKI = 0x4A;
    public static int JAPAN_FUJITSU = 0x4B;
    public static int JAPAN_SONY = 0x4C;
    public static int JAPAN_NISSIN_ONPA = 0x4D;
    public static int JAPAN_TEAC = 0x4E;
    public static int JAPAN_SYSTEM_PRODUCT = 0x4F;
    public static int JAPAN_MATSUSHITA_ELECTRIC = 0x50;
    public static int JAPAN_FOSTEX = 0x51;

    public static int OTHER_FROM = 0x60;
    public static int OTHER_TO = 0x7C;

    public static int SPECIAL_FROM = 0x7D;
    public static int SPECIAL_TO = 0x7F;

    public static int SPECIAL_NONE_REALTIME = 0x7E;
    public static int SPECIAL_REALTME = 0x7F;

    public static int DEVICE_ALL_CALL = 0x7F;

    public static byte[] makeUniversalSysex(int id, int device, byte[] data) {
        byte[] sysex = new byte[data.length + 4];
        sysex[0] = (byte) 0xf0;
        sysex[1] = (byte) id;
        sysex[2] = (byte) device;
        for (int i = 0; i < data.length; ++i) {
            sysex[i + 3] = data[i];
        }
        sysex[sysex.length - 1] = (byte) 0xf7;
        return sysex;
    }

    public static final int HANDSHAKE_OK = 0x7f;
    public static final int HANDSHAKE_NOTOK = 0x7e;
    public static final int HANDSHAKE_CANCEL = 0x7d;
    public static final int HANDSHAKE_WAIT = 0x7c;
    public static final int HANDSHAKE_EOF = 0x7b;

    public static byte[] makeHandshake(int id, int device, int handshake, int packet) {
        byte[] sysex = {
            (byte) 0xf0,
            (byte) 0x7e,
            (byte) device,
            (byte) handshake,
            (byte) packet,
            (byte) 0xf7
        };
        return sysex;
    }

    public static String dumpHex(byte data) {
        String x = Integer.toHexString((int) data & 0xff);
        if (x.length() < 2) {
            x = "0" + x;
        }
        return x;
    }

    public static String dumpHexArray(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; ++i) {
            if (i != 0) {
                buf.append(", ");
            }
            buf.append(dumpHex(data[i]));
            if ((i % 16) == 15) {
                buf.append("\n  ");
            }
        }
        return buf.toString();
    }

    public static void main(String[] args) {
        byte[] pool = new byte[100];
        for (int i = 0; i < 100; ++i) {
            pool[i] = (byte) i;
        }
        byte[] sysex = makeUniversalSysex(JAPAN_FROM, 15, pool);
        System.out.println("from " + dumpHexArray(sysex));
        SysexSplitter splitter = new SysexSplitter();
        splitter.append(sysex);
        List<byte[]> splitted = splitter.splitOrJoin(20);
        for (byte[] seg : splitted) {
            System.out.println("split : " + dumpHexArray(seg));
        }

        compareSame(sysex, splitted, 0);
        compareSame(sysex, splitted, 1);
        compareSame(sysex, splitted, 2);
    }

    public static byte[] rebuildByPath(byte[] data, int rebuildType) {
        try {
            if (rebuildType == 1) {
                SysexMessage message = new SysexMessage(data, data.length);
                return message.getMessage();
            }
            if (rebuildType == 2) {
                SplittableSysexMessage message = new SplittableSysexMessage(data);
                byte[] data1 = message.getMessage();
                if (data1[0] == (byte)0xf0 || data1[0] == (byte)0xf7) {
                    return data1;
                }
                else {
                    //F7始まりのメッセージは先頭のF7を前の終端と間違えられるので、
                    //F7をスキップして送るようになっている
                    byte[] data2 = new byte[data1.length + 1];
                    for (int i = 0; i < data1.length; ++ i) {
                        data2[i + 1] = data1[i];
                    }
                    data2[0] = (byte)0xf7;
                    return data2;
                }
            }
        } catch (InvalidMidiDataException ex) {
            MXFileLogger.getLogger(SysexSplitTest.class).log(Level.SEVERE, ex.getMessage(), ex);
            return new byte[] { (byte)0xff, (byte)0xff };
        }
        return data;
    }

    public static void compareSame(byte[] sysex, List<byte[]> splitted, int rebuildType) {
        SysexSplitter splitter = new SysexSplitter();
        for (byte[] seg : splitted) {
            seg = rebuildByPath(seg, rebuildType);
            splitter.append(seg);
        }
        List<byte[]> result = splitter.splitOrJoin(0);

        byte[] from = sysex;
        byte[] to = result.get(0);
        ArrayList<String> fail = new ArrayList<>();

        if (from.length != to.length) {
            fail.add("from.length = " + from.length + ", to.length = " + to.length);
        } else {
            for (int i = 0; i < from.length; ++i) {
                if (from[i] != to[i]) {
                    String fail1 = "from[" + i + "] = " + dumpHex(from[i]);
                    String fail2 = "to[" + i + "] = " + dumpHex(to[i]);
                    fail.add(fail1 + ", " + fail2);
                }
            }
        }

        if (fail.isEmpty()) {
            System.out.println("Match");
        } else {
            System.out.println("Fail");
            System.out.println(fail);
            System.out.println("reconnnect " + dumpHexArray(to));
        }
    }
}
