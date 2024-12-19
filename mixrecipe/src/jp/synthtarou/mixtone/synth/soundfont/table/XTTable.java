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
package jp.synthtarou.mixtone.synth.soundfont.table;

import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTTable extends ArrayList<XTRow> {
    XTHeader _header;
    
    public XTTable() {
        _header = new XTHeader();
    }

    public XTTable(XTTable parent, int from, int to) {
        _header = parent._header;
        for (int i = from; i <= to; ++ i) {
            XTRow row = parent.get(i);
            if (row != null) {
                add(row);
            }
        }
    }

    public XTTable(XTHeader header) {
        _header = header;
    }

    public XTHeader getHeader() {
        return _header;
    }
    
    public XTRow newRow() {
        XTRow row = new XTRow(this);
        add(row);
        return row;
    }
    public XTRow getOr(int x) {
        if (x < 0 || x >= size()) {
            return null;
        }
        return get(x);
    }
    
    public XTRow findRow(int column, Number target) {
        for (int y = 0; y < size(); ++ y) {
            XTRow row = get(y);
            Number n = row.get(column)._numberValue;
            if (n != null && n.equals(target)) {
                return row;
            }
        }
        return null;
    }
}

