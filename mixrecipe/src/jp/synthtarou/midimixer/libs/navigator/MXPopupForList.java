/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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
package jp.synthtarou.midimixer.libs.navigator;

import jp.synthtarou.midimixer.libs.navigator.legacy.INavigator;
import jp.synthtarou.midimixer.libs.navigator.legacy.NavigatorFor1ColumnList;
import jp.synthtarou.midimixer.libs.navigator.legacy.NavigatorFor2ColumnList;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.namedvalue.MNamedValue;
import jp.synthtarou.midimixer.libs.namedvalue.MNamedValueList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXPopupForList<T> extends MXPopup {

    MNamedValueList<T> _list;

    public MXPopupForList(JTextField target, MNamedValueList<T> list) {
        super(target);
        _list = list;
        install();
    }

    public void showPopup(JComponent mouseBase) {
        MNamedValueList<T> list = getList();

        if (_target != null) {
            String text = _target.getText();
            int x = list.indexOfName(text);
            if (x >= 0) {
                _selectedIndex = x;
            }
        }
        if (list.size() >= 20) {
            boolean found = false;
            for (MNamedValue<T> seek : list) {
                String str = String.valueOf(seek._value);
                if (str.equals(seek._name) == false) {
                    found = true;
                    break;
                }
            }
            if (found) {
                NavigatorFor2ColumnList navi = new NavigatorFor2ColumnList(list, _selectedIndex);
                MXUtil.showAsDialog(_target, navi, _dialogTitle);
                if (navi.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
                    _selectedIndex = navi.getReturnIndex();
                    approvedIndex(_selectedIndex);
                }
            }else {
                NavigatorFor1ColumnList navi = new NavigatorFor1ColumnList(list, _selectedIndex);
                MXUtil.showAsDialog(_target, navi, _dialogTitle);
                if (navi.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
                    _selectedIndex = navi.getReturnIndex();
                    approvedIndex(_selectedIndex);
                }
            }
            popupHiddenAndGoNext();
        } else {
            _menu = new JPopupMenu();
            for (int i = 0; i < list.size(); ++i) {
                MNamedValue<T> seek = list.get(i);
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(new WrapAction(i));
                if (i == _selectedIndex) {
                    item.setSelected(true);
                }
                _menu.add(item);
            }

            int width = mouseBase.getWidth();
            int height = mouseBase.getHeight();
            if (height >= 70) {
                height = height /2 - 10;
                width = width / 2 - 30;
            }
            else {
                width = 0;
            }
            _menu.show(mouseBase, width, height);
            _menu.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    popupHiddenAndGoNext();
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    popupHiddenAndGoNext();
                }
            });
        }
    }
    
    public MNamedValueList<T> getList() {
        return _list;
    }

    public abstract void approvedIndex(int selectedIndex);

    class WrapAction extends AbstractAction {
        int _choice;

        WrapAction(int choice) {
            super(getList().nameOfIndex(choice));
            _choice = choice;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            _selectedIndex = _choice;
            approvedIndex(_selectedIndex);
        }
    }

    protected int _selectedIndex = -1;
    protected JPopupMenu _menu;

    public void setDialogTitle(String title) {
        _dialogTitle = title;
    }

    public void setSelectedIndex(int index) {
        _selectedIndex = index;
    }
    
    String _dialogTitle = INavigator.DEFAULT_TITLE;
    
}
