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
package jp.synthtarou.midimixer.libs.wraplist;

import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.navigator.INavigator;
import jp.synthtarou.midimixer.libs.navigator.NavigatorFor2ColumnList;

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
            _handler.popupSelected(_list, _selectedIndex);
        }
    }

    protected MXWrapList<T> _list;
    protected int _selectedIndex = -1;
    protected PopupHandler<T> _handler = null;
    protected JPopupMenu _menu;

    public MXWrapListPopup(MXWrapList<T> list, PopupHandler<T> handler) {
        if (list == null) {
            throw new NullPointerException("list can't null");
        }
        _list = list;
        _handler = handler;
        _selectedIndex = -1;
    }
    
    public void setSelectedIndex(int x) {
        _selectedIndex = x;
    }

    public void show(JTextField textField) {
        if (_list.size() >= 20) {
            NavigatorFor2ColumnList navi = new NavigatorFor2ColumnList(_list, _selectedIndex);
            MXUtil.showAsDialog(textField, navi, "Select");
            if (navi.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
                _selectedIndex = navi.getReturnIndex();
                _handler.popupSelected(_list, _selectedIndex);
            }
        } else {
            _menu = new JPopupMenu();
            for (int i = 0; i < _list.size(); ++i) {
                MXWrap<T> wrap = _list.get(i);
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(new WrapAction(i));
                if (i == _selectedIndex) {
                    item.setSelected(true);
                }
                _menu.add(item);
            }

            final Color prevColor = textField.getBackground();
            textField.setBackground(Color.pink);
            _menu.show(textField, 0, textField.getHeight());
            _menu.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    textField.setBackground(prevColor);
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }
    }
}
