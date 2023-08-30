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

/**
 *
 * @author Syntarou YOSHIDA
 */


public class MXFocusGroupElement {
    MXFocusGroup _group;
    Component _root;
    LinkedList<Component> _element;
    Listen _listenerObject;

    MXFocusGroupElement(MXFocusGroup group, Component root) {
        _group = group;
        _root = root;
        _element = new LinkedList<Component>();
        _listenerObject = new Listen();
 
        installAllRecursible(_root);
    }
    
    boolean checkMouseInTarget() {
        Point p1 = MouseInfo.getPointerInfo().getLocation();
        Component c = (Component)_root;
        try {
            Point p2 = c.getLocationOnScreen();
            Point p3 = new Point(p2.x + c.getWidth(), p2.y + c.getHeight());

            if (p1.x >= p2.x && p1.y >= p2.y) {
                if (p1.x < p3.x && p1.x < p3.x) {
                    return true;
                }
            }
        }catch(Exception e) {
        }
        return false;
    }

    public synchronized  void uninstallAll() {
        for (Component c : _element) {
            c.removeMouseListener(_listenerObject);
            c.removeMouseMotionListener(_listenerObject);
        }
        _element.clear();
    }

    public synchronized void installAllRecursible(Component c) {
        _element.add(c);
        c.addMouseListener(_listenerObject);
        c.addMouseMotionListener(_listenerObject);

        if (c instanceof Container) {
            Container ct = (Container)c;
            Component[] child = ct.getComponents();
            for (int i = 0; i < child.length; ++ i) {
                installAllRecursible(child[i]);
            }
        }
    }

    static MXFocusGroup _lock = null;
    static MXFocusGroup _lockNew = null;

    public class Listen implements MouseListener, MouseMotionListener {
        
        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            _lock = MXFocusGroupElement.this._group;
            if (_group._checkEnable == false) {
                return;
            }
            if (_root.isEnabled()) {
                _group.comingFocus(_root);

                ((MXFocusAble)_root).focusedMousePressed(e);
                ((MXFocusAble)_root).focusChangingValue();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            _lock = null;
            if (_root.isEnabled()) {
                //_newUnderLock.doDoubleCheck();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            _lockNew = MXFocusGroupElement.this._group;
             if (_lock != null) {
                return;
            }
            if (_group._checkEnable == false) {
                return;
            }
            if (_root.isEnabled()) {
                _group.comingFocus(_root);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (_lock != null) {
                return;
            }
            if (_group._checkEnable == false) {
                return;
            }
            if (_root.isEnabled()) {
                _lockNew.doDoubleCheck();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (_lock != null) {
                if (_root.isEnabled()) {
                    ((MXFocusAble)_root).focusChangingValue();
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (_lock != null) {
                if (_root.isEnabled()) {
                    ((MXFocusAble)_root).focusChangingValue();
                }
            }
        }
    }
}
