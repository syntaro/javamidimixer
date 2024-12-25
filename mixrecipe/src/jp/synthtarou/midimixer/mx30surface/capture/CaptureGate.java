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
package jp.synthtarou.midimixer.mx30surface.capture;

import java.util.TreeMap;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CaptureGate {
    public CaptureGate(CaptureCommand command, int gate, String text) {
        _command = command;
        _gate = gate;
        _text = text;
    }
    
    public String toString() {
        int command = -1;
        if (_command._template != null && _command._template.size() >= 1) {
            command = _command._template.safeGet(0) & 0xfff0;
        }
        String add = "";
        if (command == MXMidiStatic.COMMAND_CH_NOTEON || command == MXMidiStatic.COMMAND_CH_POLYPRESSURE || command == MXMidiStatic.COMMAND_CH_NOTEOFF) {
            add = " = " + MXMidiStatic.nameOfNote(_gate);
        }
        if (command == MXMidiStatic.COMMAND_CH_CONTROLCHANGE) {
            add = " = CC " + MXMidiStatic.nameOfControlChange(_gate);
        }
        return _gate + add;
    }
    
    CaptureCommand _command;
    int _gate;
    String _text;
    
    CaptureValue _value = new CaptureValue();
}
