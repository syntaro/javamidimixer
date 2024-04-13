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
package jp.synthtarou.midimixer.libs.midi.console;

import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMidiConsoleElement {

    private MXMessage _message;

    public MXMidiConsoleElement(MXMessage message) {
        _message = message;
    }

    public MXMessage getMessage() {
        return _message;
    }

    public String formatMessageDump() {
        String exString = "";
        if (_message.getDwordCount() >= 1) {
            StringBuffer str = new StringBuffer();
            for (int i = 0; i < _message.getDwordCount(); ++i) {
                int dword = _message.getAsDword(i);
                int status = (dword >> 16) & 0xff;
                int data1 = (dword >> 8) & 0xff;
                int data2 = (dword) & 0xff;
                str.append("[");
                str.append(MXUtil.toHexFF(status) + " " + MXUtil.toHexFF(data1) + " " + MXUtil.toHexFF(data2));
                str.append("]");
            }
            return str.toString();
        } else {
            byte[] data = _message.getBinary();
            return MXUtil.dumpHex(data);
        }
    }

    public String formatMessageLong() {
        return _message.toString() + formatMessageDump();
    }
}
