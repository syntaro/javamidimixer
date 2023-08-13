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
package jp.synthtarou.midimixer.libs.swing.attachment;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedList;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import jp.synthtarou.midimixer.MXStatic;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXAttachSpinnerWheelAble {
    JSpinner _spinner;
    JComponent _parent;
    Point _basePoint;

    public MXAttachSpinnerWheelAble(JSpinner spinner) {
         JComponent parent = (JComponent)spinner.getParent();
        _spinner = spinner;
        _parent = parent;

        LinkedList<Component> list = new LinkedList<Component>();
        list.add(parent);
        
        while(list.isEmpty() == false) {
            Component c = list.removeFirst();
            if (c instanceof Container) {
                Container p = (Container)c;
                Component[] children = p.getComponents();
                for (int i = 0; i < children.length ; ++ i ){
                    list.add(children[i]);
                }
            }

            for (MouseListener listener : c.getMouseListeners()) {
                if (listener instanceof MouseManager) {
                    c.removeMouseListener(listener);
                    break;
                }
            }
            for (MouseMotionListener listener : c.getMouseMotionListeners() ){
                if (listener instanceof MouseManager) {
                    c.removeMouseMotionListener(listener);
                    break;
                }
            }
            for (MouseWheelListener listener : c.getMouseWheelListeners() ){
                if (listener instanceof MouseManager) {
                    c.removeMouseWheelListener(listener);
                    break;
                }
            }

            MouseManager l = new MouseManager();
            c.addMouseListener(l);
            c.addMouseMotionListener(l);
            c.addMouseWheelListener(l);
        }
    }

    public class MouseManager implements MouseListener, MouseMotionListener, MouseWheelListener  {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (MXStatic._trapMouseForOnlySelect) {
                return;
            }
            mouseMoved(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (MXStatic._trapMouseForOnlySelect) {
                return;
            }
            if (_spinner.isEnabled()) {
                if (_basePoint != null) {
                    Point p2 = e.getLocationOnScreen();
                    int div = (p2.y - _basePoint.y) - (p2.x - _basePoint.x);
                    div *= 5;
                    div /= 200;

                    SpinnerModel model = _spinner.getModel();
                    if (model instanceof SpinnerNumberModel) {
                        SpinnerNumberModel number = (SpinnerNumberModel)model;
                        int x = (Integer)number.getNumber();
                        int v = div;
                        x -= v;
                        if (x < (Integer)number.getMinimum()) x = (Integer)number.getMinimum();
                        if (x > (Integer)number.getMaximum()) x = (Integer)number.getMaximum();

                        _spinner.setValue(x);
                    }
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (MXStatic._trapMouseForOnlySelect) {
                return;
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (MXStatic._trapMouseForOnlySelect) {
                return;
            }
            if (_spinner.isEnabled()) {
                _basePoint = e.getLocationOnScreen();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (MXStatic._trapMouseForOnlySelect) {
                return;
            }
            _basePoint = null;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (MXStatic._trapMouseForOnlySelect) {
                return;
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (MXStatic._trapMouseForOnlySelect) {
                return;
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (MXStatic._trapMouseForOnlySelect) {
                return;
            }
            if (_spinner.isEnabled()) {
                SpinnerModel model = _spinner.getModel();
                if (model instanceof SpinnerNumberModel) {
                    SpinnerNumberModel number = (SpinnerNumberModel)model;
                    int x = (Integer)number.getNumber();
                    int v = e.getWheelRotation();
                    x -= v;
                    if (x < (Integer)number.getMinimum()) x = (Integer)number.getMinimum();
                    if (x > (Integer)number.getMaximum()) x = (Integer)number.getMaximum();

                    _spinner.setValue(x);
                }
            }
        }
    }
}  