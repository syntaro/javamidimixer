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

import java.util.Comparator;
import jp.synthtarou.midimixer.mx36ccmapping.accordion.MXAccordion;
import jp.synthtarou.midimixer.mx36ccmapping.accordion.MXAccordionFocus;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36Folder implements Comparable<MX36Folder> {
    SortedArray<MX36Status> _list;
    MXAccordionFocus _focus;
    MX36Process _process;
    
    public static final Comparator<MX36Status> SORT_BY_COLUMN = new Comparator<MX36Status>() {
        @Override
        public int compare(MX36Status o1, MX36Status o2) {
            return o1.compareSurfacePositionColumn(o2);
        }
    };
    
    public static final Comparator<MX36Status> SORT_BY_ROW = new Comparator<MX36Status>() {
        @Override
        public int compare(MX36Status o1, MX36Status o2) {
            return o1.compareSurfacePositionRow(o2);
        }
    };
    
    final String _folderName;
    int _order;
    MXAccordion _accordion;

    public MX36Folder(MX36Process process, MXAccordionFocus focus, int order, String name) {
        _list = new SortedArray<>(SORT_BY_ROW);
        _order = order;
        _process = process;
        _focus = focus;
        _folderName = name;
        _accordion = new MXAccordion(focus, name);
    }

    @Override
    public int compareTo(MX36Folder o) {
        if (_order < o._order) {
            return -1;
        }
        if (_order > o._order) {
            return 1;
        }
        return 0;
    }

    public void sort(Comparator<MX36Status> comp) {
        _list._insertComparator = comp;
        _list.sort(comp);
    }

    public void insertSorted(MX36Status status) {
        int pos = _list.insertSorted(status);
        _accordion.insertAt(pos, new MX36StatusPanel(_process, _accordion, status));
        setupMouse();
    }

    public void refill(MX36Status status) {
        int count = 0;
        for (MX36Status seek : _list) {
            if (seek == status) {
                _accordion.refresh(count);
            }
            count ++;
        }
    }
    
    public void setupMouse() {
       _focus.setupMouse(0, _accordion);
     }
    
    public MX36Status createStatus() {
        return new MX36Status(this);
    }
}
