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
package jp.synthtarou.midimixer.libs.swing.focus;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Syntarou YOSHIDA
 */

public class MXFocusGroup {
    Component _focusedControl = null;
    boolean _checkEnable = false;
    ArrayList<MXFocusGroupElement> _groupMember = new ArrayList();

    public void attach(Component c) {
        if (c instanceof MXFocusAble) {
            _groupMember.add(new MXFocusGroupElement(this, c));
        }else {
            throw new IllegalArgumentException("Component is not MXFocusAble");
        }
    }

    public void detach(MXFocusAble c) {
        Iterator<MXFocusGroupElement> it = _groupMember.iterator();
        while(it.hasNext()) {
            MXFocusGroupElement e = it.next();
            if (e._root == c) {
                it.remove();
                return;
            }
        }
    }

    void comingFocus(Component c) {
        if (c != _focusedControl) {
            if (_focusedControl != null) {
                if (_focusedControl instanceof MXFocusAble) {
                    MXFocusAble f = (MXFocusAble)_focusedControl;
                    f.focusStatusChanged(false);
                }
                _focusedControl = null;
            }
            _focusedControl = c;
            if (c != null) {
                if (c instanceof MXFocusAble) {
                    MXFocusAble f = (MXFocusAble)c;
                    f.focusStatusChanged(true);
                }
            }
        }
    }
    
    void doDoubleCheck() {
        for (MXFocusGroupElement t : _groupMember) {
            if (t.checkMouseInTarget()) {
                comingFocus(t._root);
                return;
            }
        }
        comingFocus(null);
    }

    public void setFocusEnabled(boolean flag) {
        _checkEnable = flag;
        for (MXFocusGroupElement member : _groupMember) {
            for (Component c : member._element) {
                MouseListener[] listM = c.getMouseListeners();
                int mouseListenterCount = 0, required = 0;               
                for (int i = 0; i < listM.length; ++ i) {
                    MouseListener m = listM[i];
                    if (m == member) {
                        mouseListenterCount ++;
                        break;
                    }
                }
                required ++;
                if (mouseListenterCount != required) {
                    member.uninstallAll();
                    member.installAllRecursible(c);
                    break;
                }
            }
        }
        comingFocus(null);
    }
    
    public boolean isFocusEnabled() {
        return _checkEnable;
    }
    
    public Component getFocus() {
        return _focusedControl;
    }
}
