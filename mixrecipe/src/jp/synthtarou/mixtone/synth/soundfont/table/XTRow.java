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
public class XTRow extends ArrayList<XTColumn> {
    XTTable _table;

    public XTRow(XTTable table) {
        _table = table;
    }

    void ensurePosition(int column) {
        while(column >= size()) {
            add(null);
        }
    }
    
    public void setColumn(int column, String value) {
        ensurePosition(column);
        set(column, new XTColumn(value));
    }

    public void setColumn(int column, int value) {
        ensurePosition(column);
        set(column, new XTColumn(value));
    }

    public void setColumn(int column, long value) {
        ensurePosition(column);
        set(column, new XTColumn(value));
    }

    public void setColumn(int column, double value) {
        ensurePosition(column);
        set(column, new XTColumn(value));
    }

    public void setColumn(int column, float value) {
        ensurePosition(column);
        set(column, new XTColumn(value));
    }

    public void setColumn(int column, XTTable value) {
        ensurePosition(column);
        set(column, new XTColumn(value));
    }
    
    public void set(String column, XTColumn value) {
        int x = _table._header.indexOf(column);
        if (x < 0) {
            throw new IllegalArgumentException("Column named [" + column + "] is not member.");
        }
        ensurePosition(x);
        set(x, value);
    }

    public String textColumn(int column) {
        return textColumn(column, "");
    }

    public String textColumn(int column, String def) {
        if (column >= size()) {
            return def;
        }
        return get(column).textValue();
    }

    public int intColumn(int column) {
        return numberColumn(column, -1).intValue();
    }

    public Number numberColumn(int column, Number def) {
        if (column >= size()) {
            return def;
        }
        return get(column).numberValue();
    }

    public XTTable tableColumn(int column) {
        if (column >= size()) {
            return null;
        }
        return get(column).extraValue();
    }
    
    public String getDump() {
        XTHeader header = _table.getHeader();
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < header.size(); ++ i) {
            if (i >= size()) {
                break;
            }
            XTColumn col = get(i);
            if (i > 0) {
                str.append(", ");
            }
            str.append(header.get(i) +"=" + col);
        }
        return str.toString();
    }
    
    public String toString() {
        return getDump();
    }
}
