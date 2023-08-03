/*
 * Copyright 2023 Syntarou YOSHIDA.
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
package jp.synthtarou.midimixer.libs.midi.programlist.database;

import java.util.Collection;
import java.util.TreeMap;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class DoubleIndex<T extends DoubleIndexElement> {
    TreeMap<Integer, T> intIndex = new TreeMap();
    TreeMap<String, T> nameIndex = new TreeMap();
    
    public DoubleIndex() {
    }
    
    public T first() {
        if (intIndex.isEmpty()) {
            return null;
        }
        return intIndex.values().iterator().next();
    }

    public void put(T obj) {
        intIndex.put(obj.getId(), obj);
        nameIndex.put(obj.getName(), obj);
    }
    
    public T get(int id) {
        return intIndex.get(id);
    }

    public T get(String name) {
        return nameIndex.get(name);
    }
    
    public Collection<T> values() {
        return intIndex.values();
    }
    
    public int size() {
        return intIndex.size();
    }
}
