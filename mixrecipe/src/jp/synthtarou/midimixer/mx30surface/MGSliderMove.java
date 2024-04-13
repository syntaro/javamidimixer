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
package jp.synthtarou.midimixer.mx30surface;

import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXTiming;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGSliderMove {
    MXMessage _owner;
    MGStatus _status;
    int _newValue;
    MXTiming _timing;
    
    public MGSliderMove(MXMessage owner, MGStatus slider, int newValue, MXTiming timing) {
        _owner = owner;
        _status = slider;
        _newValue = newValue;
        _timing = timing;
    }

    public MGSliderMove(MXMessage owner, MGStatus slider, int newValue) {
        _owner = owner;
        _status = slider;
        _newValue = newValue;
        _timing = null;
    }
}
