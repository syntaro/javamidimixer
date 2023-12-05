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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import javax.swing.JComponent;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXFocusGroup{
    
    public MXFocusGroup(MXFocusHandler handler) {
        _handler = handler;
    }

    public boolean _editMode = false;
    MXFocusHandler _handler;
    MXFocusTargetInfo _focusedControl = null;
    boolean _stopFocusAction = false;
    ArrayList<MXFocusTargetInfo> _groupMember = new ArrayList();

    public void attach(JComponent c) {
        _groupMember.add(new MXFocusTargetInfo(this, c));
    }

    void resetFocus() {
        if (_focusedControl != null) {
            _focusedControl = null;
        }
        resetColor(null);
    }

    public void stopFocusAction(boolean flag) {
        _stopFocusAction = flag;
        if (!flag) {
            for (MXFocusTargetInfo seek : _groupMember) {
                seek.uninstallAll();
                seek.installAllRecursible((Component) seek._root);
            }
        }
    }

    public MXFocusTargetInfo getFocus() {
        return _focusedControl;
    }

    static HashSet<JComponent> _haveColored = new HashSet<>();

    private void resetColor(JComponent exclude) {
        ArrayList<JComponent> clearedSelected = new ArrayList<>();
        for (JComponent able : _haveColored) {
            if (_handler.isFocusWithSelected(able)) {
                continue;
            }
            if (able == exclude) {
                continue;
            }
            clearedSelected.add(able);
        }
        for (JComponent comp : clearedSelected) {
            _haveColored.remove(comp);
            Color color = _handler.getDefaultColor(comp);
            comp.setBackground(color);

            LinkedList<Container> listContainer = new LinkedList();
            listContainer.add(comp);
            while (listContainer.isEmpty() == false) {
                Container cont = listContainer.remove();
                Component[] list = cont.getComponents();
                for (Component child : list) {
                    child.setBackground(color);
                    if (child instanceof Container) {
                        listContainer.add((Container) child);
                    }
                }
            }

            _handler.focusOffAfterUncolored(comp);
        }
    }

    public void setBackgroundAuto(JComponent comp) {
        resetColor(comp);
        Color color = null;
        if (_haveColored.contains(comp) == false) {
            _haveColored.add(comp);
        }

        if (_handler.isFocusWithSelected(comp)) {
            color = Color.yellow;
        } else if (_focusedControl != null) {
            if (comp == _focusedControl._root) {
                if (_editMode) {
                    color = Color.green;
                } else {
                    color = Color.lightGray;
                }
            }
        }

        if (color == null) {
            color = _handler.getDefaultColor(comp);
        }
        comp.setBackground(color);

        LinkedList<Container> listContainer = new LinkedList();
        listContainer.add(comp);
        while (listContainer.isEmpty() == false) {
            Container cont = listContainer.remove();
            Component[] list = cont.getComponents();
            for (Component child : list) {
                child.setBackground(color);
                if (child instanceof Container) {
                    listContainer.add((Container) child);
                }
            }
        }

        _handler.focusOnAfterColored(comp);
    }
}
