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
 */package jp.synthtarou.midimixer.libs.swing;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CheckBoxListCellRenderer extends JCheckBox implements ListCellRenderer, MouseListener {
    JList _list;
    
    public CheckBoxListCellRenderer() {
    }

    public void bind(JList list) {
        if (_list != null) {
            throw new Error("aho");
        }
        _list = list;
        list.setCellRenderer(this);
        list.addMouseListener(this);
    }
    @Override
    public Component getListCellRendererComponent(
      JList list,
      Object value,
      int index,
      boolean isSelected,
      boolean cellHasFocus){

      JCheckBox checkBox = (JCheckBox)value;
      setText(checkBox.getText());
      setSelected(checkBox.isSelected());

      return this;
    }

    @Override
    public void mouseClicked(MouseEvent e){
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();

        int index = _list.locationToIndex(p);

        JCheckBox checkBox = (JCheckBox)_list.getModel().getElementAt(index);
        if (checkBox.isSelected()){
            checkBox.setSelected(false);
        }else{
            checkBox.setSelected(true);
        }
        //_list.setSelectedIndex(index);
        _list.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
