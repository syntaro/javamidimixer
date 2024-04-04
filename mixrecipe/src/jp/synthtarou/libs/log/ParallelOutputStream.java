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
package jp.synthtarou.libs.log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ParallelOutputStream extends OutputStream {
    public ParallelOutputStream() {
        
    }

    @Override
    public void write(int b) throws IOException {
        synchronized (this) {
            for (OutputStream out : _listOutput) {
                out.write(b);
            }
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        synchronized (this) {
            for (OutputStream out : _listOutput) {
                out.write(b, off, len);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        synchronized (this) {
            for (OutputStream out : _listOutput) {
                out.flush();
            }
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (this) {
            for (OutputStream out : _listOutput) {
                out.close();
            }
        }
    }
    
    ArrayList<OutputStream> _listOutput = new ArrayList();
    
    public void connect(OutputStream out) {
        synchronized (this) {
            disconnect(out);
            _listOutput.add(out);
        }
    }
    
    public void disconnect(OutputStream out) {
        synchronized (this) {
            for (int i = 0; i < _listOutput.size(); ++ i) {
                if (_listOutput.get(i) == out) {
                    _listOutput.remove(i);
                    return;
                }
            }
        }
    }
}
