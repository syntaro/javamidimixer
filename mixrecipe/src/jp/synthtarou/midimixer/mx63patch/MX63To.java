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
package jp.synthtarou.midimixer.mx63patch;

import javax.swing.event.ChangeEvent;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOut;
import jp.synthtarou.midimixer.mx13patch.CheckableElement;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX63To implements CheckableElement {
    MX63Process _process;
    MXMIDIOut _out;
    int _port;
    
    public MX63To(MX63Process process, MXMIDIOut out, int port) {
        _process = process;
        _out = out;
        _port = port;
    }

    @Override
    public boolean isItemChecked() {
        return _out.isPortAssigned(_port);
    }

    @Override
    public void setItemChecked(boolean checked) {
        if (isItemChecked() != checked) {
            _out.setPortAssigned(_port, checked);
            _process.fireChangeListener(new ChangeEvent(_process));
        }
    }

    @Override
    public String itemToString() {
        return MXMidi.nameOfPortShort(_port);
    }
}
