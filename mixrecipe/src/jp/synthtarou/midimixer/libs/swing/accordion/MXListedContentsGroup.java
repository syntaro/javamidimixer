/*
 * Copyright (C) 2023 Syntarou YOSHIDA.
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
package jp.synthtarou.midimixer.libs.swing.accordion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXListedContentsGroup {
    public MXListedContentsGroup() {
        
    }

    MXAccordionPanel _selectedParent = null;
    JPanel _selectedChildren = null;

    public class MyMouseListener extends MouseAdapter {
        JPanel _owner;
        MXAccordionPanel _owner2;
        JComponent _target;

        public MyMouseListener(JPanel owner, JComponent target) {
            _owner = owner;
            _target = target;
            Container seek = _owner;
            while (seek != null){ 
                if (seek instanceof MXAccordionPanel) {
                    _owner2 = (MXAccordionPanel)seek;
                     break;
                }
                seek = seek.getParent();
            }
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            _owner.setBorder(BorderFactory.createEtchedBorder(Color.yellow,Color.yellow));
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            Point ownerPos = _owner.getLocationOnScreen();
            Rectangle ownerDim = new Rectangle(ownerPos.x, ownerPos.y, _owner.getWidth(), _owner.getHeight());
            if (ownerDim.contains(e.getXOnScreen(), e.getYOnScreen())) {
                if (_selectedChildren != null) {
                    _selectedChildren.setBorder(BorderFactory.createEtchedBorder());
                }
                _selectedChildren = _owner;
                _selectedChildren.setBorder(BorderFactory.createEtchedBorder(Color.green, Color.green));

                if (_selectedParent != null) {
                    _selectedParent.setSelected(false);
                }
                _selectedParent = _owner2;
                _selectedParent.setSelected(true);
            }
            else {
                //cancel
                _owner.setBorder(BorderFactory.createEtchedBorder());
            }
        }
    }
    
    public void installMouseListener(MXAccordionPanel accordion) {
        uninstallMouseListener(accordion);
        
        MXListedContents listedContents = accordion.getListedContents();
        accordion.setSelected(false);
        
        if (true) {
            accordion.addMouseListener(new MyMouseListener(accordion, accordion));
            accordion.setBorder(BorderFactory.createEtchedBorder());
        }

        int childCount = listedContents.count();
        for (int i = 0; i < childCount; ++ i) {
            Component child = listedContents.get(i);
            if (!(child instanceof JPanel)) {
                continue;
            }
            JPanel childPanel = (JPanel)child;

            LinkedList<JComponent> list = new LinkedList<>();
            list.add(childPanel);
            childPanel.setBorder(BorderFactory.createEtchedBorder());

            while(list.isEmpty() == false) {
                JComponent pop = list.remove();
                
                pop.addMouseListener(new MyMouseListener(childPanel, pop));

                if (pop instanceof Container) {
                    int seekCount = pop.getComponentCount();
                    for (int j = 0; j < seekCount; ++ j) {
                        Component c = pop.getComponent(j);
                        list.add((JComponent)c);
                    }
                }
            }
        }
    }

    public void uninstallMouseListener(MXAccordionPanel accordion) {
        MXListedContents listedContents = accordion.getListedContents();

        if (true) {
            MouseListener[]installed = accordion.getMouseListeners();
            for (MouseListener  m : installed) {
                if (m instanceof MyMouseListener) {
                    accordion.removeMouseListener(m);
                }
            }
        }
        int childCount = listedContents.count();
        for (int i = 0; i < childCount; ++ i) {
            Component child = listedContents.get(i);
            if (!(child instanceof JPanel)) {
                continue;
            }
            JPanel childPanel = (JPanel)child;

            LinkedList<JComponent> list = new LinkedList<>();
            list.add(childPanel);
            childPanel.setBorder(BorderFactory.createEtchedBorder());

            while(list.isEmpty() == false) {
                JComponent pop = list.remove();
                
                MouseListener[]installed = pop.getMouseListeners();
                for (MouseListener  m : installed) {
                    if (m instanceof MyMouseListener) {
                        pop.removeMouseListener(m);
                    }
                }

                if (pop instanceof Container) {
                    int seekCount = pop.getComponentCount();
                    for (int j = 0; j < seekCount; ++ j) {
                        Component c = pop.getComponent(j);
                        list.add((JComponent)c);
                    }
                }
            }
        }
    }
}
