/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFOutputStream extends OutputStream {

    OutputStream _base;

    public SMFOutputStream(OutputStream out) {
        _base = out;
    }

    @Override
    public void write(int b) throws IOException {
        write8(b);
    }

    @Override
    public void close() throws IOException {
        _base.close();
    }

    @Override
    public void flush() throws IOException {
        _base.flush();
    }

    public void write8(int dword) throws IOException {
        _base.write(dword);
    }

    public void write14(int dword) throws IOException {
        int b1 = (dword >> 7) & 0x7f;
        int b2 = (dword) & 0x7f;
        _base.write(b1);
        _base.write(b2);
    }

    public void write16(int dword) throws IOException {
        int b1 = (dword >> 8) & 0xff;
        int b2 = (dword) & 0xff;
        _base.write(b1);
        _base.write(b2);
    }

    public void write32(int dword) throws IOException {
        int b1 = (dword >> 24) & 0xff;
        int b2 = (dword >> 16) & 0xff;
        int b3 = (dword >> 8) & 0xff;
        int b4 = (dword) & 0xff;
        _base.write(b1);
        _base.write(b2);
        _base.write(b3);
        _base.write(b4);
    }

    public void writeVariable(long value) throws IOException {
        int x0 = (int)((value >> 21) & 0x7f);
        int x1 = (int)((value >> 14) & 0x7f);
        int x2 = (int)((value >> 7) & 0x7f);
        int x3 = (int)((value) & 0x7f);

        if (x0 != 0) {
            write8(x0 | 0x80);
            write8(x1 | 0x80);
            write8(x2 | 0x80);
            write8(x3);
            return;
        }
        if (x1 != 0) {
            write8(x1 | 0x80);
            write8(x2 | 0x80);
            write8(x3);
            return;
        }
        if (x2 != 0) {
            write8(x2 | 0x80);
            write8(x3);
            return;
        }
        write8(x3);
    }
}
