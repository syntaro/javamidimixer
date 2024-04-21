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

import java.util.ArrayList;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.libs.midi.MXMessage;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX13From implements CheckableElement {
    public MX13From(MX13Process process, int port) {
        _port = port;
        _process = process;
        _listTo = new ArrayList<>();
        for (int i = 0; i < MXConfiguration.TOTAL_PORT_COUNT; ++ i) {
            MX13To to = new MX13To(this, i);
            if (i == port) {
                to.setItemChecked(true);
            }
            _listTo.add(to);
        }
        if (port == 0) {
            setItemChecked(_itemChecked);
        }
    }

    int _port;
    ArrayList<MX13To> _listTo;

    boolean _itemChecked;
    MX13Process _process;

    @Override
    public boolean isItemChecked() {
        return _itemChecked;
    }

    @Override
    public void setItemChecked(boolean checked) {
        if (checked != _itemChecked) {
            _itemChecked = checked;
            _process.setInformation();
        }
    }

    @Override
    public String itemToString() {
        return Character.toString('A' + _port);
    }
}
