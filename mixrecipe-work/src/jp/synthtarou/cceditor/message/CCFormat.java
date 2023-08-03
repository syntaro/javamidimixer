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
package jp.synthtarou.cceditor.message;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCFormat {

    private final int[] _template;
    private final int _templateLength;
    private final int _dataLength;
    
    
    public CCFormat(int[] template) {
        _template = template;

        _templateLength = _template.length;
        _dataLength = _templateLength - countChecksumStart(template);
    }
    
    private int countChecksumStart(int[] template) {
        int count = 0;
        for (int i = 0; i < template.length; ++ i) {
            if (template[i] == CCVariable.VAR_CHECKSUM_START) {
                count ++;
            }
        }
        return count;
    }

    public byte[] bindParameters(CCParameters params) {
        byte[] data = new byte[_dataLength];
        int pos = 0;
        boolean checksumStarted = false;
        int checksumValue = 0;

        for (int i = 0; i < _template.length; ++ i) {
            int x = _template[i];
            if ((x & 0xff00) != 0) {
                if (x == CCVariable.VAR_CHECKSUM_START) {
                    checksumStarted = true;
                    checksumValue = 0;
                    continue;
                }
                if (x == CCVariable.VAR_CHECKSUM_SET) {
                    checksumValue = checksumValue & 0x7f;
                    int r = 128 - checksumValue;
                    x = r & 0x7f;
                    checksumStarted = false;
                }
                else {
                    x = CCVariable.getValiable(x, params);
                }
            }
            data[pos++] = (byte)x;
            if (checksumStarted) {
                checksumValue += x;
            }
        }
        return data;
    }
}