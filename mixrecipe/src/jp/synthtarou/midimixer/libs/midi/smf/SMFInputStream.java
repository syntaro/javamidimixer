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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * SMFファイルの数値を読み取るヘルパー
 * @author Syntarou YOSHIDA
 */
public class SMFInputStream extends InputStream {
    InputStream _base;
    byte[] _buffer = new byte[4096];
    int _pos = 0;
    int _length = -1;
    
    boolean _eof = false;
    boolean _error = false;
    
    /**
     * バイト配列からコンストラクトする
     * @param data バイト配列
     */
    public SMFInputStream(byte[] data) {
        this(new ByteArrayInputStream(data));
    }
    
    /**
     * バイト配列からコンストラクトする
     * @param data　バイト配列
     * @param offset　開始オフセット位置
     * @param length 処理するデータ長
     */
    public SMFInputStream(byte[] data, int offset, int length) {
        this(new ByteArrayInputStream(data, offset, length));
    }
    
    /**
     * InputStreamからコンストラクトする
     * @param input
     */
    public SMFInputStream(InputStream input) {
        _base = input;
        _length = -1;
        _pos = 0;
    }
    
    /**
     * 8ビット表現の数を読む
     * @return 数 終端を超えていたら-1
     */
    public int read8() {
        if (_pos >= _length) {
            try {
                _pos = 0;
                _length = _base.read(_buffer);
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
    
    /**
     * 8ビット表現の数値が、まだ読める場合、現在位置を更新せずに読み取る
     * @return 数 終端を超えていたら-1
     */
    public int peek8() {
        if (_pos >= _length) {
            try {
                _pos = 0;
                _length = _base.read(_buffer);
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

    /**
     * 16ビット表現の数を読む
     * @return 数 終端を超えていたら-1
     */
    public int read16() {
        int x = read8();
        int y = read8();
        if (x < 0 || y <0) {
            return  -1;
        }
        return x << 8 | y;
    }

    /**
     * 32ビット表現の数を読む
     * @return 数 終端を超えていたら-1
     */
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
    
    /**
     * 連続するバイト配列を読む
     * @param data 受け取る配列
     * @param length 最大長
     * @return 読み取った長さ
     */
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

    /**
     * Variableとして数値を読みとる
     * @return 数 終端を超えていたら-1
     */
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
    
    /**
     * 指定されたバイト数スキップする
     * @param length スキップする長さ
     */
    public void skip(int length) {
        _pos += length;
    }

    /**
     * read8に同じ
     * 8ビット表現の数値が、まだ読める場合、現在位置を更新せずに読み取る
     * @return 数
     * @throws IOException 終端を超えていたらEOFExceptionをthrowする
     */
    @Override
    public int read() throws IOException {
        int x = read8();
        if (x < 0) {
            throw new EOFException();
        }
        return x;
    }
    
    /**
     * ストリームを閉じる（内包するInputStreamも閉じる）
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        _base.close();
    }
}
