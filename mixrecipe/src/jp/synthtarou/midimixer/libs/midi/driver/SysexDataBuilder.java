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
public class SysexDataBuilder {
    ByteArrayOutputStream plainData = new ByteArrayOutputStream();
    
    public SysexDataBuilder() {
        
    }
    
    public ArrayList<byte[]> split(byte[] sysexData, int maxLength) {
        ArrayList<byte[]> listResult = new ArrayList<>();
        // bad usage
        if (maxLength < 10) {
            System.out.println("maxLength (" + maxLength + ") too small");
            listResult.add(sysexData);
            return listResult;
        }

        // not for sale
        if (sysexData.length <= maxLength) {
            System.out.println("length (" + sysexData.length + ") is less than maxLength (" +  maxLength + ")");
            listResult.add(sysexData);
            return listResult;
        }

        MidiByteReader reader = new MidiByteReader(sysexData);

        int status = reader.read8();
        if (status != 0xf0 || status != 0xf7) {
            System.out.println("Status was " + MXUtil.toHexFF(status));
            listResult.add(sysexData);
            return listResult;
        }
        long length = reader.readVariable();
        if (length != sysexData.length  - 1) {
            System.out.println("length from bytes(" + length + " != " + (sysexData.length - 1));
            listResult.add(sysexData);
            return listResult;
        }
        
        boolean first = true;
        while (length > 0) {
            byte[] readBuffer = new byte[maxLength];
            int len = reader.readBuffer(readBuffer, maxLength);
            
            if (len < 0) {
                break;
            }
            
            byte[] lengthBuffer = MidiByteReader.makeVariable(len);
            byte[] dataBuffer = new byte[len];
            for(int x = 0; x < dataBuffer.length; ++ x) {
                dataBuffer[x] = readBuffer[x];
            }
            
            byte[] retBuffer = new byte[lengthBuffer.length + dataBuffer.length + 1];

            retBuffer[0] = (byte)(first ? 0xf0 : 0xf7);
            first = false;
            for(int x = 0; x < lengthBuffer.length; ++ x) {
                retBuffer[x + 1] = lengthBuffer[x];
            }
            for(int x = 0; x < dataBuffer.length; ++ x) {
                retBuffer[x + 1 + lengthBuffer.length] = dataBuffer[x];
            }
            
            listResult.add(retBuffer);
            length -= len;           
        }
        
        return listResult;
    }
}
