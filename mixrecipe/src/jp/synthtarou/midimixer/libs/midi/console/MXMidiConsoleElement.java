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
import jp.synthtarou.libs.smf.OneMessage;
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
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < _message.countOneMessage(); ++i) {
            OneMessage smf = _message.toOneMessage(i);
            str.append("[" + smf.toString() + "]");
        }
        return str.toString();
    }

    public String formatMessageLong() {
        return _message.toString() + formatMessageDump();
    }
}
