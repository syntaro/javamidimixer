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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MidiByteReader {
    InputStream _input;
    byte[] _buffer = new byte[4096];
    int _pos = 0;
    int _length = -1;
    
    boolean _eof = false;
    boolean _error = false;
    
    public MidiByteReader(byte[] data) {
        this(new ByteArrayInputStream(data));
    }
    
    public MidiByteReader(byte[] data, int offset, int length) {
        this(new ByteArrayInputStream(data, offset, length));
    }
    
    public MidiByteReader(InputStream input) {
        _input = input;
        _length = -1;
        _pos = 0;
    }
    
    public int read8() {
        if (_pos >= _length) {
            try {
                _pos = 0;
                _length = _input.read(_buffer);
            }catch(IOException e) {
                _eof = true;
                _error = true;
                return -1;
            }
            if (_length == 0) {
                _eof = true;
                return -1;
            }
        }
        if (_pos < _length) {
            return _buffer[_pos ++] & 0x00ff;
        }
        return -1;
    }
    
    public int peek8() {
        if (_pos >= _length) {
            try {
                _pos = 0;
                _length = _input.read(_buffer);
            }catch(IOException e) {
                _eof = true;
                _error = true;
                return -1;
            }
            if (_length == 0) {
                _eof = true;
                return -1;
            }
        }
        if (_pos < _length) {
            return _buffer[_pos] & 0x00ff;
        }
        return -1;
    }
    
    public int read14() {
        int x = read8();
        int y = read8();
        if (x < 0 || y <0) {
            return  -1;
        }
        x = x & 0x7f;
        y = y & 0x7f;
        return x << 7 | y;
    }

    public int read16() {
        int x = read8();
        int y = read8();
        if (x < 0 || y <0) {
            return  -1;
        }
        return x << 8 | y;
    }

    public int read32() {
        int x = read8();
        int y = read8();
        int z = read8();
        int a = read8();
        if (x < 0 || y <0 || z < 0 || a < 0) {
            return  -1;
        }
        x = x << 24;
        y = y << 16;
        z = z << 8;
        return x + y + z + a;
    }
    
    public int readBuffer(byte[] data, int length) {
        for (int i = 0; i < length; ++ i) {
            int x = read8();
            if (x < 0) {
                return i;
            }
            data[i] = (byte)x;
        }
        return length;
    }

    public long readVariable() {
        long value = 0;
        int currentByte = 0;
        do {
            currentByte = read8();
            if (currentByte < 0) {
                return -1;
            }
            int x = currentByte & 0x7f;
            value = (value << 7) + x;
        } while ((currentByte & 0x80) != 0);
        return value;
    }
    
    public static byte[] makeVariable(long value) {
        long x0 = (value >> 21) & 0x7f;
        long x1 = (value >> 14) & 0x7f;
        long x2 = (value >> 7) & 0x7f;
        long x3 = (value) & 0x7f;
        
        if (x0 != 0) {
            byte[] data = new byte[4];
            data[0] = (byte)(x0 | 0x80);
            data[1] = (byte)(x1 | 0x80);
            data[2] = (byte)(x2 | 0x80);
            data[3] = (byte)x3;
            return data;
        }
        if (x1 != 0) {
            byte[] data = new byte[3];
            data[0] = (byte)(x1 | 0x80);
            data[1] = (byte)(x2 | 0x80);
            data[2] = (byte)x3;
            return data;
        }
        if (x2 != 0) {
            byte[] data = new byte[2];
            data[0] = (byte)(x2 | 0x80);
            data[1] = (byte)x3;
            return data;
        }
        byte[] data = new byte[1];
        data[0] = (byte)x3;
        return data;
    }
    
    public void skip(int length) {
        _pos += length;
    }
}
