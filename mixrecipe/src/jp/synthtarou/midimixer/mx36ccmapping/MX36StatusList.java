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

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36StatusList extends SortedArray<MX36Status> {

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
    
    public MX36StatusList() {
        this(SORT_BY_ROW);
    }

    public MX36StatusList(Comparator<MX36Status> comparator) {
        super(comparator);
    }
    
}
