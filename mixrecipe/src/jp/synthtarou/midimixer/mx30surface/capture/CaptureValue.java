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

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CaptureValue {
    public CaptureValue() {
        _minValue = 0x10000;
        _maxValue = -1;
        _count = new TreeMap<>();
    }
    
    public void record(int value) {
        if (_minValue > value) {
            _minValue = value;
        }
        if (_maxValue < value) {
            _maxValue = value;
        }
        Integer x = _count.get(value);
        if (x == null) {
            _count.put(value, 1);
        }else {
            _count.put(value, x + 1);
        }
    }
    
    public String toString() {
        return "Min: " + _minValue + " - Max: "+  _maxValue;
    }

    int _minValue;
    int _maxValue;
    TreeMap<Integer, Integer> _count;
}
