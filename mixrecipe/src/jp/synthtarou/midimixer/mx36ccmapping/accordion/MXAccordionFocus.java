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
package jp.synthtarou.midimixer.mx36ccmapping.accordion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXAccordionFocus {

    /**
     *
     */
    public MXAccordionFocus() {
        
    }

    public class MyMouseListener extends MouseAdapter {
        int _group;
        MXAccordion _accordion;
        MXAccordionElement _element;

        public MyMouseListener(int group, MXAccordion focus, MXAccordionElement element) {
            _group = group;
            _element = element;
            _accordion = focus;
            if (element == null || focus == null) {
                throw new NullPointerException();
            }
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            focusableMousePressed(_group, _accordion, _element);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            JPanel panel = _element.getRenderer();
            Point ownerPos = panel.getLocationOnScreen();
            Rectangle ownerDim = new Rectangle(ownerPos.x, ownerPos.y, panel.getWidth(), panel.getHeight());
            if (ownerDim.contains(e.getXOnScreen(), e.getYOnScreen())) {
                focusableMouseReleased(_group, _accordion, _element, true);
            }
            else {
                focusableMouseReleased(_group, _accordion, _element, false);
            }
        }
    }

    TreeMap<Integer, MXAccordion> _selectedAccordion = new TreeMap<>();
    TreeMap<Integer, MXAccordionElement> _selectedAccordionChild = new TreeMap<>();

    public void setSelected(int group, MXAccordion accordion, MXAccordionElement child) {
        MXAccordion accordionPast = _selectedAccordion.get(group);
        if (accordionPast != accordion) {
            _selectedAccordion.put(group, accordion); 
            accordion.setColorFull(true);
        }

        MXAccordionElement focusPast = _selectedAccordionChild.get(group);
        if (focusPast != child) {
            if (focusPast != null) {
                JPanel panel = focusPast.getRenderer();
                panel.setBorder(BorderFactory.createEtchedBorder());
                focusPast.accordionFocus(false);
            }
        }

        JPanel childPanel = child.getRenderer();
        childPanel.setBorder(BorderFactory.createEtchedBorder(Color.green, Color.green));
        if (focusPast != child) {
            _selectedAccordionChild.put(group, child); 
            child.accordionFocus(true);
        }
    }
    
    void focusableMousePressed(int group, MXAccordion accordion, MXAccordionElement element) {
        JPanel panel = element.getRenderer();
        panel.setBorder(BorderFactory.createEtchedBorder(Color.yellow,Color.yellow));
    }
    
    void focusableMouseReleased(int group, MXAccordion accordion, MXAccordionElement element, boolean isComing) {
        if (isComing) {
            setSelected(group, accordion, element);
        }
        else {
            JPanel panel = element.getRenderer();
            panel.setBorder(BorderFactory.createEtchedBorder());
        }
    }
    
    public void setupMouse(int group, MXAccordion accordion) {
        uninstallMouse(accordion);
 
        if (true) {
            //accordion.addMouseListener(new MyMouseListener(group, accordion, null));
            accordion.setBorder(BorderFactory.createEtchedBorder());
        }

        int childCount = accordion.elementCount();
        for (int i = 0; i < childCount; ++ i) {
            MXAccordionElement child = accordion.elementAt(i);
            JPanel childPanel = child.getRenderer();

            LinkedList<JComponent> list = new LinkedList<>();
            list.add(childPanel);
            childPanel.setBorder(BorderFactory.createEtchedBorder());

            while(list.isEmpty() == false) {
                JComponent pop = list.remove();
                
                pop.addMouseListener(new MyMouseListener(group, accordion, child));

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

    public void uninstallMouse(MXAccordion accordion) {
        if (true) {
            MouseListener[]installed = accordion.getMouseListeners();
            for (MouseListener  m : installed) {
                if (m instanceof MyMouseListener) {
                    accordion.removeMouseListener(m);
                }
            }
        }
        
        int childCount = accordion.elementCount();
        for (int i = 0; i < childCount; ++ i) {
            MXAccordionElement child = accordion.elementAt(i);
            JPanel childPanel = child.getRenderer();

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

