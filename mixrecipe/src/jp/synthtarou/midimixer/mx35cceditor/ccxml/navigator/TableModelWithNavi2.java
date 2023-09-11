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
package jp.synthtarou.midimixer.mx35cceditor.ccxml.navigator;

import java.awt.Component;
import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class TableModelWithNavi2 extends TableModelWithNavi {
    static String[] header = {
        "Module",
        "Ch",
        "Gate",
        "Value",
        "Kontroler Module,Ch,Gate"
    };
    
    ArrayList<String[]> _listRow = new ArrayList<>();
    
    public TableModelWithNavi2() {
        super(header);
        _listRow.add(new String[5]);
        _listRow.add(new String[5]);
        _listRow.add(new String[5]);
    }

    @Override
    public INavigator<String> getRowEditor(Component parent, int rowIndex) {
        INavigator<String> editor = new AnotherTableEditPanel(this, rowIndex);
        return editor;
    }

    @Override
    public INavigator<String> getCellEditor(Component parent, int rowIndex, int columnIndex) {
        String text = getValueAsText(rowIndex, columnIndex);
        INavigator<String> editor = new NavigatorForText(text, "Text");
        return editor;
    }

    @Override
    public String getValueAsText(int rowIndex, int columnIndex) {
        String[] data = _listRow.get(rowIndex);
        String text = data[columnIndex];
        return text == null ? "" : text;
    }
    
    @Override
    public void setValueAt(Object data, int rowIndex, int columnIndex) {
        _listRow.get(rowIndex)[columnIndex] = (String)data;
    }
}
