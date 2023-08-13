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
import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class PromptTableModelEx extends PromptTableModel {
    static String[] header = {
        "Module",
        "Ch",
        "Gate",
        "Value",
        "Kontroler Module,Ch,Gate"
    };
    
    ArrayList<String[]> _listRow = new ArrayList<>();
    
    public PromptTableModelEx() {
        super(header);
        _listRow.add(new String[5]);
        _listRow.add(new String[5]);
        _listRow.add(new String[5]);
    }

    @Override
    public IPrompt getRowEditor(Component parent, int rowIndex) {
        IPrompt editor = new AnotherTableEditPanel(this, rowIndex);
        return editor;
    }

    @Override
    public IPromptForInput getCellEditor(Component parent, int rowIndex, int columnIndex) {
        String text = getValueAsText(rowIndex, columnIndex);
        IPromptForInput<String> editor = new CCTextPrompt(text, "Text");
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
