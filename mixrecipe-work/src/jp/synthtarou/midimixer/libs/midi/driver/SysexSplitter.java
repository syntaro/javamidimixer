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
package jp.synthtarou.midimixer.libs.midi.driver;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.smf.MidiByteReader;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SysexSplitter {
    public SysexSplitter() {
        
    }

    ByteArrayOutputStream sysexBody = new ByteArrayOutputStream();
    
    public void clean() {
    }
    
    public void append(byte[] sysexData) {
        MidiByteReader reader = new MidiByteReader(sysexData);

        int status = reader.read8();
        if (status != 0xf0 && status != 0xf7) {
            System.out.println("Status was " + MXUtil.toHexFF(status));
            return;
        }
        
        byte[] data = new byte[512];
        while(true) {
            int x = reader.readBuffer(data, 512);
            if (x <= 0) {
                break;
            }
            sysexBody.write(data, 0, x);
        }
        //最初の文字F0,F7を除いて格納する。最後F7は格納されている
    }
    
    public ArrayList<byte[]> splitOrJoin(int maxLength) {
        // bad usage
        if (maxLength == 0) {
            maxLength = 65535;
        }
        
        if (maxLength < 10) {
            throw new IllegalArgumentException("split length too small");
        }

        ArrayList<byte[]> listResult = new ArrayList<>();
        boolean first = true;
        
        byte[] data = sysexBody.toByteArray();
        MidiByteReader reader = new MidiByteReader(data);
        int length = data.length;
        
        while (length > 0) {
            byte[] readBuffer = new byte[maxLength - 1];
            int len = reader.readBuffer(readBuffer, maxLength - 1);
            
            if (len < 0) {
                break;
            }
            
            boolean eof = false;
            if ((readBuffer[len-1] & 0xff) == 0xf7) {
                eof = true;
            }
            
            byte[] retBuffer = new byte[1 + len];
            retBuffer[0] = (byte)(first ? 0xf0 : 0xf7);

            first = false;

            for(int x = 0; x < len; ++ x) {
                retBuffer[x + 1] = readBuffer[x];
            }
            
            System.out.println(MXUtil.dumpHexFF(retBuffer));
            listResult.add(retBuffer);
            length -= len;
            if (eof) {
                break;
            }
        }
        
        return listResult;
    }
}
