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
package jp.synthtarou.midimixer.libs.midi.port;

import java.util.LinkedList;
import java.util.TreeSet;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import jp.synthtarou.libs.MainThreadTask;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class PreProcessTableModel extends AbstractTableModel {
    public static final String[] _columns = {
        "Name", "00h", "20h", "Pair", "YourChoice"
    };
    public static final int COL_NAME = 0;
    public static final int COL_00H = 1;
    public static final int COL_20H = 2;
    public static final int COL_PAIR = 3;
    public static final int COL_SWITCH = 4;
    

    TreeSet<RecordEntry> _rows = new TreeSet(RecordEntry.COMP_ENTRY);
    LinkedList<TableModelListener> _listeners = new LinkedList<>();

    @Override
    public int getRowCount() {
        return _rows.size();
    }

    @Override
    public int getColumnCount() {
        return _columns.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return _columns[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    public RecordEntry getRecordEntry(int row) {
        int x = 0;
        for (RecordEntry seek : _rows) {
            if (x == row) {
                return seek;
            }
            x ++;
        }
        return null;
    }

    public int indexOfRecordEntry(RecordEntry e) {
        int x = 0;
        for (RecordEntry seek : _rows) {
            if (seek == e) {
                return x;
            }
            x ++;
        }
        return -1;
    }
    
    public void recalc(RecordEntry e) {
        if (e._count0h + e._count20h + e._countPair == 0) {
            return;
        }
        MainThreadTask r = new MainThreadTask(() -> {
            _rows.add(e);
            int index = indexOfRecordEntry(e);
            if (index >= 0) {
                fireTableRowsInserted(index, index);
            }
        });
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        RecordEntry e = getRecordEntry(rowIndex);
        switch(columnIndex){
            case COL_NAME:
                return e.toTableString(COL_NAME);
            case COL_00H:
                return e.toTableString(COL_00H);
            case COL_20H:
                return e.toTableString(COL_20H);
            case COL_PAIR:
                return e.toTableString(COL_PAIR);
            case COL_SWITCH:
                return e.toTableString(COL_SWITCH);
        }
        return "-";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        return;
    }
}
