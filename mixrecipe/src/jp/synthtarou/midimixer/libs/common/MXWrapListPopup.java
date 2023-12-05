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
package jp.synthtarou.midimixer.libs.common;

import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import jp.synthtarou.midimixer.libs.navigator.INavigator;
import jp.synthtarou.midimixer.libs.navigator.NavigatorForWrapList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXWrapListPopup<T> {

    class WrapAction extends AbstractAction {

        int _choice;

        WrapAction(int choice) {
            super(_list.nameOfIndex(choice));
            _choice = choice;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            _selectedIndex = _choice;
            doAction();
        }
    }

    JTextField _textField;
    MXWrapList<T> _list;
    int _selectedIndex = -1;

    public MXWrapListPopup(JTextField target, MXWrapList<T> list) {
        _textField = target;
        _list = list;
        
        String targetText = target.getText();
        
        for (int x = 0; x < list.size(); ++ x) {
            if (list.nameOfIndex(x).equals(targetText)) {
                _selectedIndex = x;
                break;
            }
        }
    }

    public void show() {
        if (_list.size() >= 20) {
            NavigatorForWrapList navi = new NavigatorForWrapList(_list, _selectedIndex);
            MXUtil.showAsDialog(_textField, navi, "Select");
            if (navi.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
                _selectedIndex = navi.getReturnValue();
                doAction();
            }
        }
        else {
            JPopupMenu menu = new JPopupMenu();
            for (int i = 0; i < _list.size(); ++i) {
                MXWrap<T> wrap = _list.get(i);
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(new WrapAction(i));
                if (i == _selectedIndex) {
                    item.setSelected(true);
                }
                menu.add(item);
            }
            
            final Color prevColor = _textField.getBackground();
            _textField.setBackground(Color.pink);
            menu.show(_textField, 0, _textField.getHeight());
            menu.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    _textField.setBackground(prevColor);
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }
    }

    public void doAction() {
        _textField.setText(_list.nameOfIndex(_selectedIndex));
    }
}
