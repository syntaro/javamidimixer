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
import jp.synthtarou.midimixer.libs.midi.MXMessage;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX13To implements CheckableElement {
    MX13From _from;
    int _port;
    ArrayList<MX13SignalType> _list;

    public MX13To(MX13From from, int port) {
        _from = from;
        _port = port;
        resetSkip();
    }

    public void setSkip(int type, boolean skipFlag) {
        _list.get(type)._itemChecked = skipFlag;
    }

    public void resetSkip() {
        ArrayList<MX13SignalType> newList = new ArrayList<>();
        for(int i = 0; i < MX13SignalType.COUNT_TYPE; ++ i) {
            MX13SignalType signal = new MX13SignalType(this, i);
            if (i == MX13SignalType.TYPE_CLOCK || i == MX13SignalType.TYPE_ACTIVE_SENSING) {
                signal.setItemChecked(true);
            }
            else {
                signal.setItemChecked(false);
            }
            newList.add(signal);
        }
        _list = newList;
    }

    public boolean accept(MXMessage message) {
        for (MX13SignalType seek : _list) {
            if (seek.isItemChecked() && seek.isSkip(message)) {
                return false;
            }
        }
        return true;
    }

    boolean _itemChecked;

    @Override
    public boolean isItemChecked() {
        return _itemChecked;
    }

    @Override
    public void setItemChecked(boolean checked) {
        if (checked != _itemChecked) {
            _itemChecked = checked;
            _from._process.setInformation();
        }
    }

    @Override
    public String itemToString() {
        return Character.toString('A' + _port);
    }
}
