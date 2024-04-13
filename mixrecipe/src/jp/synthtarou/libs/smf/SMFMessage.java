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
package jp.synthtarou.libs.smf;

import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFMessage implements Comparable<SMFMessage> {

    public SMFMessage(long tick, int status, int data1, int data2) {
        this(tick, new byte[]{(byte) status, (byte) (data1 & 0x7f), (byte) (data2 & 0x7f)});
    }

    public SMFMessage(long tick, byte[] binary) {
        if (binary.length == 0) {
            throw new IllegalArgumentException("NULLPO");
        }
        _tick = tick;
        _binary = binary;
    }

    private final byte[] _binary;

    public int _port = 0;
    public long _tick;
    public int _seqTrack;
    public int _fileOrder;
    public long _millisecond;

    public int getStatus() {
        return _binary.length >= 1 ? _binary[0] & 0xff : 0;
    }

    public int getData1() {
        return _binary.length >= 2 ? _binary[1] & 0xff : 0;
    }

    public int getData2() {
        return _binary.length >= 3 ? _binary[2] & 0xff : 0;
    }

    public String getMetaTitle() {
        if (getStatus() == 0xff) {
            switch (getData1()) {
                case 0x00:
                    return "Sequence Number";
                case 0x01:
                    return "Text Event";
                case 0x02:
                    return "Copyright Notice";
                case 0x03:
                    return "Sequence Name / Track Name";
                case 0x04:
                    return "Instrument Name";
                case 0x05:
                    return "Lyric";
                case 0x06:
                    return "Marker";
                case 0x07:
                    return "Cue Point";
                case 0x20:
                    return "Channel Prefix";
                case 0x2f:
                    return "End Of Track";
                case 0x54:
                    return "SMPTE Offset";
                case 0x58:
                    return "Time Signature";
                case 0x59:
                    return "Key Signature";
                case 0x51:
                    return "Set Tempo";
                case 0x75:
                    return "Sequence-Specific Meta-Event";
                default:
                    return "Unknown(" + Integer.toHexString(getData1()) + "h)";
            }
        }
        return null;
    }

    public String getMetaText() {
        if (getStatus() == 0xff) {
            String meta = getMetaTitle();
            if (meta == null) {
                return meta;
            }
            switch (getData1()) {
                case 0x01:
                case 0x02:
                case 0x03:
                case 0x04:
                case 0x05:
                case 0x06:
                case 0x07:
                    byte[] metaData = new byte[_binary.length - 2];
                    for (int i = 0; i < metaData.length; ++i) {
                        metaData[i] = _binary[i + 2];
                    }
                    String meta2 = dumpHexFF(_binary);
                    try {
                        meta2 = new String(metaData, "ISO-8859-1");
                        meta2 = new String(metaData, "Shift_JIS");
                    }
                    catch(Exception e) {
                    }
                    meta2 = MXUtil.shrinkText(meta2);
                    return meta + " = \"" + meta2 + "\"";
                default:
                    StringBuffer meta3 = new StringBuffer();
                    String meta4 = "";
                    for (int i = 2; i < _binary.length; ++ i) {
                        int x = _binary[i] & 0xff;
                        if (meta3.length() > 0) {
                            meta3.append(", ");
                        }
                        meta3.append(x);
                    }
                    switch(getData1()) {
                        case 0x54: //SMPTE offset
                            meta4 = " as hour, min, sec, frame, FF";
                            break;
                        case 0x58: //Time Signature	
                            meta4 = " as numerator, denominator, cc, bb";
                            break;
                        case 0x59: //Key Signature
                            meta4 = " as sf/mi";
                            break;
                    }
                    
                    return meta + " = [" + meta3 + "]" + meta4;
            }
        }
        return null;
    }
    
    public String toString() {
        if (getStatus() == 0xff) {
            return getMetaText();
        } else {
            return dumpHexFF(_binary);
        }
    }

    public static String toHexFF(int i) {
        String str = Integer.toHexString(i).toUpperCase();
        if (str.length() == 1) {
            return "0" + str;
        }
        if (str.length() >= 3) {
            return str.substring(str.length() - 2, str.length());
        }
        return str;
    }

    public static String dumpHexFF(byte[] data) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < data.length; ++i) {
            if (i != 0) {
                str.append(" ");
            }
            str.append(toHexFF((int) data[i]));
        }
        return str.toString();
    }

    public MXMessage toMXMessage() {
        MXMessage message = null;
        int status = getStatus();

        if (status >= 0x80 && status <= 0xef) {
            message = MXMessageFactory.fromShortMessage(_port, getStatus(), getData1(), getData2());
        } else {
            message = MXMessageFactory.fromBinary(_port, getBinary());
        }
        return message;
    }

    @Override
    public int compareTo(SMFMessage o) {
        SMFMessage o1 = this;
        SMFMessage o2 = o;
        long x, y, z;
        
        if (o1._tick == 0 && o2._tick == 0) {
            x = o1._millisecond - o2._millisecond;
        }
        else {
            x = o1._tick - o2._tick;
        }
        if (x < 0) {
            return -1;
        }
        if (x > 0) {
            return 1;
        }

        z = o1._seqTrack - o2._seqTrack;
        if (z < 0) {
            return -1;
        }
        if (z > 0) {
            return 1;
        }

        y = o1._fileOrder - o2._fileOrder;
        if (y < 0) {
            return -1;
        }
        if (y > 0) {
            return 1;
        }
        
        boolean isReset1 = MXMidi.isReset(o1.getBinary());
        boolean isReset2 = MXMidi.isReset(o2.getBinary());
        boolean isProg1 = (o1.getStatus() & 0xf0) == MXMidi.COMMAND_CH_PROGRAMCHANGE;
        boolean isProg2 = (o2.getStatus() & 0xf0) == MXMidi.COMMAND_CH_PROGRAMCHANGE;
        boolean isBank1 = (o1.getStatus() & 0xf0) == MXMidi.COMMAND_CH_CONTROLCHANGE && (o1.getData1() == 0 || o1.getData1() == 32);
        boolean isBank2 = (o2.getStatus() & 0xf0) == MXMidi.COMMAND_CH_CONTROLCHANGE && (o2.getData1() == 0 || o2.getData1() == 32);
        
        /* リセットとバンクとプログラムは早めに送信する */
        if (isReset1 && !isReset2) {
            return -1;
        }
        if (!isReset1 && isReset2) {
            return 1;
        }

        if (isProg1 && !isProg2) {
            return -1;
        }
        if (!isProg1 && isProg2) {
            return 1;
        }

        if (isBank1 && !isBank2) {
            return -1;
        }
        if (!isBank1 && isBank2) {
            return 1;
        }
        
        int len = o1.getBinary().length - o2.getBinary().length;
        if (len != 0) {
            return len;
        }

        for (int i = 0; i < o1.getBinary().length; ++i) {
            x = o1.getBinary()[i] - o2.getBinary()[i];
            if (x < 0) {
                return -1;
            }
            if (x > 0) {
                return 1;
            }
        }

        y = o1._port - o2._port;
        if (y < 0) {
            return -1;
        }
        if (y > 0) {
            return 1;
        }

        return 0;
    }

    public int getMetaTempo() {
        int b1 = (0xff & _binary[2]) << 16;
        int b2 = (0xff & _binary[3]) << 8;
        int b3 = (0xff & _binary[4]);
        return b1 + b2 + b3;
    }
    
    public boolean isMetaMessage() {
        return getStatus() == 0xff;
    }

    public boolean isBinaryMessage() {
        if (_binary != null) {
            if (_binary.length <= 4) {
                return false;
            }
            return true;
        }
        return false;
    }

    public byte[] getBinary() {
        return _binary;
    }

    public int toDwordMessage() {
        if (isBinaryMessage()) {
            return 0;
        }
        int st = getStatus() & 0xff;
        int dt1 = getData1() & 0x7f;
        int dt2 = getData2() & 0x7f;
        return (((st << 8) | dt1) << 8) | dt2;
    }
    
    public boolean isTempoMessage() {
        if (getStatus() == 0xff) {
            if (getData1() == 0x51) {
                return true;
            }
        }
        return false;
    }
}
