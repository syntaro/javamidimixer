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
package jp.synthtarou.midimixer.libs.accordion;

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

    public void remove(MXAccordionElement element) {
        for (int i = 0; i < _listElement.size(); ++ i) {
            if (element == _listElement.get(i)) {
                _listElement.remove(i);
                _animationPanel.remove(element.getAccordionView());
            }
        }
    }
    
    boolean _doingAnimation = false;
    
    public void add(MXAccordionElement child,int index) {
        _listElement.add(index, child);
        _animationPanel.add(child.getAccordionView(), index);
    }

    public int count() {
        return _listElement.size();
    }
    
    public MXAccordionElement get(int x) {
        return _listElement.get(x);
    }
    
    public void openWithAnimation(boolean open, boolean invert) {
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
                    if (invert) {
                        for (int p = 200; p > 100; p -= 5) {
                            _animationPanel.setScrollPercent(p);
                            try {
                                Thread.sleep(10);
                            } catch (Exception e) {
                                e.printStackTrace();;
                            }
                        }
                    }
                    else {
                        for (int p = 0; p < 100; p += 5) {
                            _animationPanel.setScrollPercent(p);
                            try {
                                Thread.sleep(10);
                            } catch (Exception e) {
                                e.printStackTrace();;
                            }
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
                    if (invert) {
                        for (int p = 100; p <= 200; p += 5) {
                            _animationPanel.setScrollPercent(p);
                            try {
                                if (p != 0) {
                                    Thread.sleep(10);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();;
                            }
                        }
                    }
                    else {
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
