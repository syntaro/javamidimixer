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
package jp.synthtarou.midimixer.mx30surface;

import javax.swing.JTextField;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapListPopup;
import jp.synthtarou.midimixer.libs.wraplist.PopupHandler;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXAttachPopupForList<T> extends MXAttachPopup {

    MXWrapList<T> _list;

    public MXAttachPopupForList(JTextField target, MXWrapList<T> list) {
        super(target);
        _list = list;
        install();
    }

    public void showPopup() {
        MXWrapList<T> list = getList();

        MXWrapListPopup<T> actions = new MXWrapListPopup(list, new PopupHandler<T>() {
            @Override
            public void popupSelected(MXWrapList<T> list, int selected) {
                approvedIndex(selected);
            }
        });

        String text = _target.getText();
        int x = list.indexOfName(text);
        if (x >= 0) {
            actions.setSelectedIndex(x);
        }
        actions.show(_target);
    }
    
    public MXWrapList<T> getList() {
        return _list;
    }

    public abstract void approvedIndex(int selectedIndex);
}
