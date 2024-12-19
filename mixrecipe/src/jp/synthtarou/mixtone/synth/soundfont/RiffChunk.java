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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class RiffChunk {
    byte[] _chunkData;
    int _chunkDataOffset;
    
    String _chunkId;
    String _chunkType;
    int _chunkSize;
    RiffChunk _parent;
    
    SFZElement _listDataElement;

    public void addChild(RiffChunk child) {
        if (_children == null) {
            _children = new ArrayList<>();
        }
        _children.add(child);
        child._parent = this;
    }
    
    ArrayList<RiffChunk> _children;
    int _readX;
    
    public void skip(int x) {
        x += _readX;
        if (x < 0 || x > _chunkSize) {
            throw new IllegalArgumentException("Cant skip " + x + " from " + _readX);
        }
        _readX = x;
    }
    
    public int getPositiion() {
        return _readX;
    }
    
    public RiffChunk() {
        _chunkData = new byte[0];
        _chunkDataOffset = 0;
        _chunkSize = 0;
    }
    
    public RiffChunk(RiffChunk segment) {
        setData(segment);
    }
    
    public void setData(RiffChunk segment) {
        setData(segment._chunkData, segment._chunkDataOffset, segment._chunkSize);
    }

    public void setData(byte[] data) {
        setData(data, 0, data.length);
    }

    public void setData(byte[] data, int offset, int length) {
        if (_chunkDataOffset + _chunkSize > data.length) {
            throw new IllegalArgumentException("can't seek to "+  _chunkDataOffset + "+" + length + " is over " + data.length);
        }
        _chunkData = data;
        _chunkDataOffset = offset;
        _chunkSize = length;
        _readX = 0;
    }
    
    public void setData(File file) throws IOException {
        long size = Files.size(file.toPath());
        FileInputStream input = new FileInputStream(file);
        try {
            if (size >= 1 && size <= Integer.MAX_VALUE) {
                setData(input, (int)size);
            }else {
                setData(input);
            }
        }finally {
            try{
                input.close();
            }catch(IOException ex) {
                
            }
        }
    }
    
    public void setData(InputStream input) throws IOException {
        BytesAppender data = new BytesAppender(input);
        setData(data.toByteArray());
    }

    public void setData(InputStream input, int size) throws IOException {
        BytesAppender data = new BytesAppender(input, size);
        setData(data.toByteArray());
    }
    
    public void fseek(int pos) {
        if (pos < 0 || pos >= _chunkSize) {
            throw new IllegalArgumentException("bad range, must 0 <= pos < " + _chunkSize);
        }
        _readX = pos;
    }

    public int read() {
        if (_readX < 0 || _readX >= _chunkSize) {
            return -1;
        }
        int ch =_chunkData[_readX + _chunkDataOffset] & 0xff;
        _readX ++;
        return ch;
    }
    
    public int remainByteLength() {
        if (_readX < 0 || _readX >= _chunkSize) {
            return 0;
        }
        return _chunkSize - _readX;
    }
    
    public byte[] readData(int length) {
        if (length < 0) {
            length = remainByteLength();
        }
        byte[] data = new byte[length];
        for (int i = 0; i < length; ++ i) {
            data[i] = _chunkData[(_readX ++) + _chunkDataOffset];
        }
        return data;
    }
    
    public int readDWORD() {
        int c0 = read();
        int c1 = read();
        int c2 = read();
        int c3 = read();
        if (c3 < 0) {
            return -1;
        }
        int dword =  (c0 << 0) + (c1 << 8) + (c2 << 16) + (c3 << 24);
        if ((dword & 0x7fffffff) != dword) {
        }
        return dword;
    }

    public int readWORD() {
        int c0 = read();
        int c1 = read();
        if (c1 < 0) {
            return -1;
        }
        int word = (c0 << 0) + (c1 << 8);
        if ((word & 0x7fff) != word) {
            word |= 0xffff0000;
        }
        return word;
    }
    
    public String readText() {
        StringBuilder text = new StringBuilder();
        while(true) {
            int c = read();
            if (c != 0 && c > 0) {
                text.append((char)c);
            }
            else {
                break;
            }
        }
        return text.toString();
    }

    public String readZSTR(int length) {
        /*
        * ZSTR 文字列形式のフィールド値は終端文字（値 0）を含まなければなりません。
        * また、終端文字はデータサイズが偶数になるように1～2バイトを挿入します。
        */
        StringBuilder text = new StringBuilder();
        boolean stoped = false;
        while(length > 0) {
            int c = read();
            length --;
            if (c != 0 && c > 0) {
                if (!stoped) {
                    text.append((char)c);
                }
            }
            else {
                stoped = true;
                continue;
            }
        }
        return text.toString();
    }
    
    public String read4char() {
        int len = Math.min(remainByteLength(), 4);
        int c0 = read();
        int c1 = read();
        int c2 = read();
        int c3 = read();
        char[] data = new char[] {
            (char)c0, (char)c1, (char)c2, (char)c3
        };
        return new String(data, 0, len);
    }
    
    public RiffChunk split(int pos, int length) {
        if (pos < 0 || _chunkDataOffset + pos >= _chunkSize) {
            throw new IllegalArgumentException();
        }
        RiffChunk basic = new RiffChunk();
        basic._chunkData = _chunkData;
        basic._chunkDataOffset = _chunkDataOffset + pos;
        basic._chunkSize = length;
        return basic;
    }

    public String dumpPath() {
        String text = "";
        RiffChunk seek = this;

        while(seek != null) {
            RiffChunk parent = seek._parent;
            if (parent == null) {
                break;
            }
            String parentName = parent._chunkId;
            if (parentName == null) {
                break;
            }
            if (parent._children.size() == 1) {
                text = parentName + "/" + text;
            }
            else {
                int index = parent._children.indexOf(seek);
                text = parentName + "#" + index + "/" + text;
            }
            seek = parent;
        }
        
        return text;
    }    

}
