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
package jp.synthtarou.mixtone.synth.soundfont;

import java.io.IOException;
import java.util.ArrayList;
import java.io.InputStream;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class BytesAppender {
    ArrayList<byte[]> _listAll;
    
    public BytesAppender(InputStream input, int size) throws IOException {
        _listAll = new ArrayList<>();
        byte[] data = new byte[size];
        int pos = 0;
        while(size > 0) {
            int x = input.read(data, pos, size);
            if (x < 0) { 
                return;
            }
            pos += x;
            size -= x;
        }
        _listAll.add(data);
    }

    public BytesAppender(InputStream input) throws IOException {
        _listAll = new ArrayList<>();
        while(true) {
            byte[] data = new byte[32768];
            int x = input.read(data, 0, data.length);
            if (x < 0) { 
                return;
            }
            appendReused(data, x);
        }
    }
    
    public void append(byte[] data, int length) {
        byte[] copy = new byte[length];
        for (int i = 0; i < length; ++ i) {
            copy[i] = data[i];
        }
        appendReused(copy, length);
    }

    protected void appendReused(byte[] data, int length) {
        if (length > 0) {
            if (length != data.length) {
                byte[] copy = new byte[length];
                for (int i = 0; i < length; ++ i) {
                    copy[i] = data[i];
                }
                data = copy;
            }
            _listAll.add(data);
        }
    }
    
    public int size() {
        int x = 0;
        for (byte[] data : _listAll) {
            x += data.length;
        }
        return x;
    }

    public byte[] toByteArray() {
        if (_listAll.size() == 1) {
            return _listAll.get(0);
        }
        byte[] result = new byte[size()];
        int x = 0;
        for (byte[] data : _listAll) {
            for (int i = 0; i < data.length; ++ i) {
                result[x++] = data[i];
            }
        }
        return result;
    }
}
