/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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
package jp.synthtarou.midimixer.mx36ccmapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SortedArray<T> extends ArrayList<T> {
    Comparator<T> _insertComparator;
    boolean _duplicateOk = true;

    public SortedArray() {
        _insertComparator = new BasicComparator();
    }
    
    public SortedArray(Comparator<T> comp) {
        _insertComparator = comp;
    }
    
    public void sortWith(Comparator<T> comp) {
        _insertComparator = comp;
        Collections.sort(this, comp);
    }

    public int insertSorted(T value) {
        int low = 0;
        int high = size() - 1;
        while (low <= high) {
            int middle = low + (high - low) / 2;
            T o = get(middle);
            int c = _insertComparator.compare(value, o);
            if (c == 0) {
                low = middle;
                high = middle;
                break;
            }
            if (c < 0) {
                high = middle - 1;
            } else {
                low = middle + 1;
            }
        }
        if (low == high) {
            if (_duplicateOk == false) {
                set(low, value);
                return -1;
            }
        }
        add(low, value);
        return low;
    }
   
    public class BasicComparator implements Comparator<T> {
        @Override
        public int compare(T o1, T o2) {
            Comparable a1 = (Comparable)o1;
            Comparable a2 = (Comparable)o2;
            return a1.compareTo(a2);
        }
    }
    
    public static void main(String[] args) {
        SortedArray<Integer> sorted = new SortedArray<>();
        for (int i = 0; i < 10; ++ i) {
            sorted.insertSorted(i * 10);
            System.out.println(sorted);
        }
        for (int i = 0; i < 10; ++ i) {
            sorted.insertSorted(i * 3);
            System.out.println(sorted);
        }
        for (int i = 0; i < 10; ++ i) {
            sorted.insertSorted(i * 7);
            System.out.println(sorted);
        }
        for (int i = 0; i < 10; ++ i) {
            sorted.insertSorted(i * 9);
            System.out.println(sorted);
        }
        System.out.println(sorted);
    }
}
