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
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CaptureCommand {
    public CaptureCommand(int channel, String command) {
        _channel = channel;
        _command = command;
        _template = null;
        try {
            _template = new MXTemplate(command);
        }catch(Exception ex) {
            
        }
    }
    
    public String toString() {
        int command = -1;
        if (_template != null && _template.size() >= 1) {
            command = _template.get(0) & 0xfff0;
        }
        String add = "";
        if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
            add = " = CC";
        }
        else {
            add = MXMidi.nameOfSystemCommonMessage(command);
            if (add == null) {
                add = "";
            }
            else {
                add = " = "+ add;
            }
        }
        return _channel + ":" + _command + add;
    }

    MXTemplate _template;
    int _channel;
    String _command;
    TreeMap<Integer, CaptureGate> _listGate = new TreeMap();
}
