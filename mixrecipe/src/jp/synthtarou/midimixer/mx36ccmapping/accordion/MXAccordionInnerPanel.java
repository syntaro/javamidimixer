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

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXAccordionInnerPanel {
    private MXAnimationPanel _animationPanel;
    ArrayList<MXAccordionElement> _listElement;
    
    public MXAccordionInnerPanel() {
        _animationPanel = new MXAnimationPanel();
        _listElement = new ArrayList<>();
    }

    public MXAnimationPanel getAnimationPanel() {
        return _animationPanel;
    }
    
    boolean _doingAnimation = false;
    
    public void add(MXAccordionElement child,int index) {
        _listElement.add(index, child);
        _animationPanel.add(child.getRenderer(), index);
    }

    public int count() {
        return _listElement.size();
    }
    
    public MXAccordionElement get(int x) {
        return _listElement.get(x);
    }
    
    public void openWithAnimation(boolean open) {
        _doingAnimation = true;
        if (open) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        _animationPanel.setVisible(true);
                        _animationPanel.setBackground(null);
                        revalidateASAP();
                    }catch(Throwable e) {
                        e.printStackTrace();;
                    }
                    for (int p = 0; p < 100; p += 5) {
                        _animationPanel.setScrollPercent(p);
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();;
                        }
                    }
                    _animationPanel.setScrollPercent(100);
                    _doingAnimation = false;
                    revalidateASAP();
                }
            }).start();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int p = 100; p >= 0; p -= 5) {
                        _animationPanel.setScrollPercent(p);
                        try {
                            if (p != 0) {
                                Thread.sleep(10);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();;
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            _animationPanel.setVisible(false);
                            _doingAnimation = false;
                            revalidateASAP();
                        }
                    });
                }
            }).start();
        }
    }

    public void revalidateASAP() {
        Component c = _animationPanel;
        while(c != null) {
            if (c instanceof MXAccordion) {
                MXAccordion panel = (MXAccordion)c;
                panel.revalidate();
                return;
            }
            c = c.getParent();
        }
    }
}
