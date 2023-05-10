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
package jp.synthtarou.midimixer.libs.midi.smf;

import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXException;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFMessage implements Comparable<SMFMessage> {
    /**
     * @return the _binary
     */
    public byte[] getBinary() {
        if (_binary != null) {
            if (_status == 0xff) {
                if (_dataType == 0x51) {
                    byte[] binary = new byte[] {
                        (byte)0xff,
                        (byte)0x51,
                        _binary[0],
                        _binary[1],
                        _binary[2]
                    };
                    return binary;
                }
            }
            byte[] binary = _binary;
            return _binary.clone();
        }else {
            return null;
        }
    }

    public SMFMessage(long tick, int status, int data1, int data2) {
        _tick = tick;
        _status = status;
        _data1 = data1;
        _data2 = data2;
        _binary = null;
    }

    public SMFMessage(long tick, int status, int type, byte[] binary) {
        if (binary.length == 0) {
            throw new IllegalArgumentException("NULLPO");
        }
        _tick = tick;
        _status = status;
        _dataType = type;
        _data1 = 0;        
        _data2 = 0;
        _binary = binary;
    }

    private byte[] _binary;

    public int _port = -1;
    public long _tick;
    public int _status;
    public int _data1;
    public int _data2;
    public int _seqTrack;
    public int _order;
    public int _dataType;
        
    public String toString() {
        byte[] data = getBinary();
        if (data != null)
        {
            if (_status == 0xff) {
                if (_dataType== 0x51) {
                    return new String("Tempo");
                }else {
                    String meta = "";
                    try {
                        meta = dumpHexFF(data);
                        meta = new String(data, "ISO-8859-1");
                        meta = new String(data, "Shift_JIS");
                    }catch(Exception e) {

                    }
                    return meta;
                }
            }else {
                
            }
            return toHexFF(_status) + " -> " + dumpHexFF(data);
        }else {
            return toHexFF(_status) + ", " + toHexFF(_data1) + ", " + toHexFF(_data2);
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

    public MXMessage fromSMFtoMX(int port) {
        MXMessage message = null;

        if (_status>= 0x80 && _status <= 0xef) {
            message = MXMessageFactory.fromShortMessage(port, _status, _data1, _data2);
        }
        else if (_status == 0xf0 || _status == 0xf7) {
            message = MXMessageFactory.fromSysexMessage(port, _status, getBinary());
        }
        else if (_status == 0xff) {
            message = MXMessageFactory.fromMeta(port, getBinary());
        }
        else {
            //Bug
        }
        return message;
    }

    @Override
    public int compareTo(SMFMessage o) {
        SMFMessage o1 = this;
        SMFMessage o2 = o;
        long x, y, z;
        x = o1._tick - o2._tick;
        if (x < 0) return -1; if (x > 0) return 1;

        z = o1._seqTrack - o2._seqTrack;
        if (z < 0) return -1; if (z > 0) return 1;

        y = o1._order - o2._order;
        if (y < 0) return -1; if (y > 0) return 1;

        //System.out.println("Unreach " + x + ", " + y + ", " + z);
        
        boolean isProg1 = (o1._status & 0xf0) == MXMidi.COMMAND_PROGRAMCHANGE;
        boolean isProg2 = (o2._status & 0xf0) == MXMidi.COMMAND_PROGRAMCHANGE;
        boolean isBank1 = (o1._status & 0xf0) == MXMidi.COMMAND_CONTROLCHANGE && (o1._data1 == 0 || o1._data1 == 32);
        boolean isBank2 = (o2._status & 0xf0) == MXMidi.COMMAND_CONTROLCHANGE && (o2._data1 == 0 || o2._data1 == 32);

        if (isBank1 && !isBank2) return -1;
        if (!isBank1 && isBank2) return 1;

        if (isProg1 && !isProg2) return -1;
        if (!isProg1 && isProg2) return 1;

        x = o1._status - o2._status;
        if (x < 0) return -1; if (x > 0) return 1;
        x = o1._data1- o2._data1;
        if (x < 0) return -1; if (x > 0) return 1;
        x = o1._data2 - o2._data2;
        if (x < 0) return -1; if (x > 0) return 1;
        if (o1.getBinary() != null || o2.getBinary() != null) {
            if (o2.getBinary() == null) return -1;
            if (o1.getBinary() == null) return  1;

            int len = o1.getBinary().length - o2.getBinary().length;
            if (len != 0) return len;

            for (int i = 0; i < o1.getBinary().length; ++ i) {
                x = o1.getBinary()[i] - o2.getBinary()[i];
                if (x < 0) return -1; if (x > 0) return 1;
            }
        }
        return 0;
    }
    
    public int getMetaTempo() {
        int b1 = (0xff & _binary[0]) << 16;
        int b2 = (0xff & _binary[1]) << 8;
        int b3 = (0xff & _binary[2]);
        return b1 + b2 + b3;
    }
    
    public boolean isMetaMessage() {
        return (_status == 0xff) ? true : false;
    }
   
    public boolean isBinaryMessage() {
        /*
        switch(_status) {
            case 0xf0:
            case 0xf7:
            case 0xff:
                return true;
        }
        return false;
        */
        return _binary != null;
    }
    
    public int toDwordMessage() throws MXException {
        if (isBinaryMessage()) {
            throw new MXException("Its binary");
        }
        int st = _status & 0xff;
        int dt1 = _data1 & 0xff;
        int dt2 = _data2 & 0xff;
        return (((st << 8) | dt1) << 8) | dt2;
    }
}
