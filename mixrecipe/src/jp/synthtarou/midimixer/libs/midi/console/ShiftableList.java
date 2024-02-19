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
package jp.synthtarou.midimixer.libs.midi.console;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ShiftableList<T> {
    public ShiftableList(int capacity) {
        _capacity = capacity;
        _base = new Object[capacity];
    }
    
    Object[] _base;
    int _lastWrite = -1;
    int _capacity;

    public synchronized  int size() {
        return _base.length;
    }
    
    public synchronized  void add(T obj) {
        _lastWrite ++;
        if (_lastWrite >= _capacity) {
            _lastWrite -= _capacity;
        }
        _base[_lastWrite] = obj;
    }
    
    public synchronized  T get(int index) {
        int top = _lastWrite + 1;
        if (top >= _capacity) {
            top -= _capacity;
        }
        index += top;
        if (index >= _capacity) {
            index -= _capacity;
        }
        return (T)_base[index];
    }
   
    public synchronized  void clear() {
        for (int i = 0; i < _base.length; ++i ){
            _base[i] = null;
        }
    }
}
