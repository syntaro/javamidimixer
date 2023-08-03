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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXLineWriter {
    OutputStream _fout;
    PrintWriter _writer;
    
    String _charset;
   
    public MXLineWriter(File file, String charset) throws IOException {
        _fout = new FileOutputStream(file);
        try {
            _writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(_fout, charset)));
        }catch(IOException ioe) {
            try {
                _fout.close();
            }catch(IOException e) {
            }
            _fout = null;
            throw ioe;
        }
        _writer.println("#charset=" + charset);
    }
    
    public void close() throws IOException {
        if (_writer != null) {
            _writer.flush();
            _writer.close();
            _writer = null;
        }
    }
    
    public void println(String text) {
        _writer.println(text);
    }

    public PrintWriter getWriter() {
        return _writer;
    }
    
}
