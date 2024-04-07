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
public class CaptureGate {
    public CaptureGate(int gate, String text) {
        _gate = gate;
        _text = text;
    }
    
    int _gate;
    String _text;

    TreeMap<Integer, CaptureValue> _listValue = new TreeMap();
}
