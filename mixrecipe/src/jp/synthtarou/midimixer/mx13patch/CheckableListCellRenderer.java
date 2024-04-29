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
package jp.synthtarou.midimixer.mx13patch;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**I
 *
 * @author Syntarou YOSHIDA
 */
public class CheckableListCellRenderer<T extends CheckableElement> implements ListCellRenderer<T> {
    public CheckableListCellRenderer(JList list) {
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                //System.out.println("p.x = " + p.x + " checkheight = " +_check.getPreferredSize().height);
                if (p.x < _check.getPreferredSize().height) {
                    int index = list.locationToIndex(p);
                    ListModel model = list.getModel();

                    T item = (T)model.getElementAt(index);
                    if (item.isItemChecked()) {
                        item.setItemChecked(false);
                    }
                    else {
                        item.setItemChecked(true);
                    }
                    list.repaint();
                }
            }
        });
    }

    private Border _noFocusBorder;
    private Border _focusBorder;

    JCheckBox _check = new JCheckBox() {
        public void updateUI() {
            super.updateUI();
            _focusBorder = null;
            _noFocusBorder = null;
        }
    };

    @Override
    public Component getListCellRendererComponent(
            JList<? extends T> list, T value, int index,
            boolean isSelected, boolean cellHasFocus) {
        JCheckBox component = _check;
        if (isSelected) {
            component.setBackground(list.getSelectionBackground());
        } else {
            component.setBackground(list.getBackground());
        }
        if (_focusBorder == null) {
            _focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
            _noFocusBorder = UIManager.getBorder("List.noFocusBorder");
            if (_noFocusBorder == null && _focusBorder != null) {
                Insets i = _focusBorder.getBorderInsets(component);
                _noFocusBorder = BorderFactory.createEmptyBorder(
                        i.top, i.left, i.bottom, i.right);
            }
        }
        _check.setSelected(value.isItemChecked());
        if (cellHasFocus) {
            component.setBorder(_focusBorder);
        } else {
            component.setBorder(_noFocusBorder);
        }
        component.setText(value.itemToString());
        return component;
    }
}
