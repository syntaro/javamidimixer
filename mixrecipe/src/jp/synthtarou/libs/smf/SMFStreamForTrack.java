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

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFStreamForTrack {
    byte[] _buffer;
    int _pos = 0;
    int _length = -1;
    
    boolean _eof = false;
    boolean _error = false;
    
    public SMFStreamForTrack(SMFInputStream parent, int length) {
        _buffer = new byte[length];
        _length = parent.readBuffer(_buffer, length);
        _pos = 0;
    }
    
    public int peek() {
        if (_pos < _length) {
            return _buffer[_pos] & 0x00ff;
        }else {
            _eof = true;
            return -1;
        }
    }

    public int read8() {
        if (_pos < _length) {
            return _buffer[_pos ++] & 0x00ff;
        }else {
            _eof = true;
            return -1;
        }
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

    public int readBuffer(byte[] data, int offset, int length) {
        for (int i = 0; i < length; ++ i) {
            int x = read8();
            if (x < 0) {
                return i;
            }
            data[i + offset] = (byte)x;
        }
        return length;
    }

    public long readVariable() {
        long value = 0; // the variable-lengh int value
        int currentByte = 0;
        //4ステップ以上のデータはないが
        do {
            currentByte = read8();
            if (currentByte < 0) {
                return -1;
            }
            value = (value << 7) + (currentByte & 0x7F);
        } while ((currentByte & 0x80) != 0);
        return value;
    }
    
    public void skip(int length) {
        _pos += length;
    }
}
