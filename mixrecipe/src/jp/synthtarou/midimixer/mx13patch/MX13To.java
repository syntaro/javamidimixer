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
package jp.synthtarou.midimixer.mx13patch;

import javax.swing.event.ChangeEvent;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX13To implements CheckableElement {
    MX13Process _process;
    MXMIDIIn _in;
    int _port;
    
    public MX13To(MX13Process process, MXMIDIIn in, int port) {
        _process = process;
        _in = in;
        _port = port;
    }

    @Override
    public boolean isItemChecked() {
        return _in.isPortAssigned(_port);
    }

    @Override
    public void setItemChecked(boolean checked) {
        if (isItemChecked() != checked) {
            _in.setPortAssigned(_port, checked);
            _process.fireChangeListener(new ChangeEvent(_process));
        }
    }

    @Override
    public String itemToString() {
        return MXMidiStatic.nameOfPortShort(_port);
    }
}
