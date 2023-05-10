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
package jp.synthtarou.midimixer.libs.text;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXLineReader {
    InputStream _in;
    String _charset;
    byte[] _buffer;
    int _pos;
    int _bufferSize;
    
    public MXLineReader(InputStream in) {
        this(in, "utf-8");
    }
    public MXLineReader(InputStream in, String charset) {
        _in = in;
        _charset = charset;
        _buffer = new byte[4096];
        _pos = 0;
        _bufferSize = 0;
    }
    
    protected boolean fetchIfNeed() throws IOException {
        if (_pos >= _bufferSize) {
            int newSize = _in.read(_buffer, 0, _buffer.length);
            if (newSize <= 0) {
                return false;
            }
            _bufferSize = newSize;
            _pos = 0;
        }
        return true;
    }
    
    public String readLine() throws IOException {
        byte[] line = new byte[256];
        int x = 0;
        
        do {
            if (_pos >= _bufferSize && fetchIfNeed() == false) {
                break;
            }
            byte ch = _buffer[_pos ++ ];
            if (ch == '\r') continue;
            if (ch == '\n') break;
            line[x ++] = ch;
            if (x >= line.length) {
                byte[] newLine = new byte[line.length * 2];
                System.arraycopy(line, 0, newLine, 0, line.length);
                line = newLine;
            }
        }while(true);
        
        if (x == 0 && _pos >= _bufferSize) {
            return null;
        }
        
        String text = new String(line, 0, x, "ASCII");
        try {
            text = new String(line, 0, x, _charset);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return text;
    }
}
