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
package jp.synthtarou.cceditor.view.common;

import java.awt.Component;
import java.awt.Container;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class PromptTableModel extends AbstractTableModel {
    private final String[] _columns;
    
    public PromptTableModel(String[] columns) {
        _columns = columns;
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public String getColumnName(int column) {
        return _columns[column];
    }
    
    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount() {
        return _columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return getValueAsText(rowIndex, columnIndex);
    }
    
    public void startEdit(Container parent, int row) {
        IPrompt editor = getRowEditor(parent, row);
        CCPromptUtil.showPrompt(parent, editor);
    }
    
    public boolean startCellEdit(Container parent, int row, int column) {
        IPromptForInput editor = getCellEditor(parent, row, column);
        CCPromptUtil.showPrompt(parent, editor);
        Object result = editor.getPromptResult();
        if (result != null) {
            setValueAt(result, row, column);
            return true;
        }
        return false;
    }
    
    public abstract String getValueAsText(int rowIndex, int columnIndex);
    
    public abstract IPrompt getRowEditor(Component parent, int rowIndex);

    public abstract IPromptForInput getCellEditor(Component parent, int rowIndex, int cellIndex);
}
