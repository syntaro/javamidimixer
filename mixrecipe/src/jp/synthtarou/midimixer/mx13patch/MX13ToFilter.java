/*
 * Copyright (C) 2024 yaman
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
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.libs.midi.port.MXMidiFilter;

/**
 *
 * @author yaman
 */
public class MX13ToFilter implements CheckableElement {
    MX13Process _process;
    MXMIDIIn _in;
    int _port;
    int _type;
    
    public MX13ToFilter(MX13Process process, MXMIDIIn in, int port, int type) {
        _process = process;
        _in = in;
        _port = port;
        _type = type;
    }

    @Override
    public boolean isItemChecked() {
        return _in.getFilter(_port).isChecked(_type);
    }

    @Override
    public void setItemChecked(boolean checked) {
        if (isItemChecked() != checked) {
            _in.getFilter(_port).setChecked(_type, checked);
            _process.fireChangeListener(new ChangeEvent(_process));
        }
    }

    @Override
    public String itemToString() {
        return MXMidiFilter.getName(_type);
    }    
}
