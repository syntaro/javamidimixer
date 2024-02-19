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
package jp.synthtarou.midimixer.libs.swing;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class JTableWithColumnHeader extends JTable {
    boolean _isColumn1AsHeader = true;
    
    /**
     *
     */
    public JTableWithColumnHeader() {
        super();
    }
    
    /**
     *
     * @param model
     * @param column
     */
    public JTableWithColumnHeader(TableModel model, TableColumnModel column) {
        super(model, column);
    }

    public TableCellRenderer getCellRenderer(int row, int column){
        TableCellRenderer root = super.getCellRenderer(row, column);
        Component render = (Component)root.getTableCellRendererComponent(this, this.getModel().getValueAt(row, column), false, false, row, column);
        if (column == 0 && _isColumn1AsHeader) {
            TableCellRenderer headerRenderer = getTableHeader().getDefaultRenderer();
            JLabel newRender = (JLabel)headerRenderer;
            newRender.setText(String.valueOf(getValueAt(row, column)));
            render = newRender;
        }
        render.setEnabled(isEnabled());
        return (TableCellRenderer)render;
    }
}

