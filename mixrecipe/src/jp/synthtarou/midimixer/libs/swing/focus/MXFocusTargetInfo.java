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
import java.awt.Container;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;
import javax.swing.JComponent;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXFocusTargetInfo {

    public final MXFocusGroup _group;
    public final JComponent _root;
    LinkedList<Component> _listComponent;
    MyListener _myListener;

    MXFocusTargetInfo(MXFocusGroup group, JComponent root) {
        _group = group;
        if (root instanceof JComponent) {
            _root = root;
            _listComponent = new LinkedList<Component>();
            _myListener = new MyListener();

            installAllRecursible(_root);
        } else {
            throw new IllegalArgumentException("MXFocuGroupElement must usered to JComponent");
        }
    }

    public boolean isMouseOnIT() {
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        Component c = (Component) _root;
        try {
            Point componentStart = c.getLocationOnScreen();
            Point componentEnd = new Point(componentStart.x + c.getWidth(), componentStart.y + c.getHeight());

            if (mousePosition.x >= componentStart.x && mousePosition.y >= componentStart.y) {
                if (mousePosition.x < componentEnd.x && mousePosition.x < componentEnd.x) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public synchronized void uninstallAll() {
        for (Component c : _listComponent) {
            c.removeMouseListener(_myListener);
            c.removeMouseMotionListener(_myListener);
        }
        _listComponent.clear();
    }

    public synchronized void installAllRecursible(Component c) {
        _listComponent.add(c);
        c.addMouseListener(_myListener);
        c.addMouseMotionListener(_myListener);

        if (c instanceof Container) {
            Container ct = (Container) c;
            Component[] child = ct.getComponents();
            for (int i = 0; i < child.length; ++i) {
                installAllRecursible(child[i]);
            }
        }
    }

    static boolean _lockWhilePressingSome = false;
    static MXFocusGroup _lastGroup = null;

    public class MyListener implements MouseListener, MouseMotionListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            _lockWhilePressingSome = false;
            if (isRootEnable()) {
                _group._handler.focusMouseClicked(_root, e);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            _lockWhilePressingSome = true;
            if (_group._stopFocusAction) {
                return;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            _lockWhilePressingSome = false;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (_lockWhilePressingSome) {
                return;
            }
            if (_lastGroup != null && _lastGroup != _group) {
                _lastGroup.resetFocus();
            }
            _lastGroup = _group;
            if (_group._stopFocusAction) {
                return;
            }
            if (isRootEnable()) {
                if (MXFocusTargetInfo.this != _group._focusedControl) {
                    _group.resetFocus();
                }
                _group._focusedControl = MXFocusTargetInfo.this;
                _group.setBackgroundAuto(_root);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (_lockWhilePressingSome) {
                return;
            }
            if (_group._stopFocusAction) {
                return;
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }
    }

    public boolean isRootEnable() {
        JComponent c = _root;
        return c.isEnabled();
    }
}
