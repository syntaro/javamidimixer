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

import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ShiftableList<T> {
    public ShiftableList(int capacity) {
        _capacity = capacity;
        _base = new ArrayList<>(capacity);
    }
    
    int _capacity;
    ArrayList<T> _base;

    public int size() {
        return _base.size();
    }
    
    public void removeFirst() {
        _base.remove(0);
    }

    public void add(T obj) {
        while (size() >= _capacity) {
            removeFirst();
        }
        _base.add(obj);
    }
    
    public T get(int index) {
        if (index < size()) {
            return _base.get(index);
        }
        else {
            return null;
        }
    }
   
    public void clear() {
        _base.clear();
    }
}
