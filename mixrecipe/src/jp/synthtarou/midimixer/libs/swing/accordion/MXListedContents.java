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

import java.awt.Component;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXListedContents {
    MXScrollablePanel _contentPane;
    MXListedContentsGroup _group;
    
    public MXListedContents() {
        _contentPane = new MXScrollablePanel();
        _contentPane.setLayout(new BoxLayout(_contentPane, BoxLayout.Y_AXIS));
        _group = new MXListedContentsGroup();
    }
    
    public void overWriteGroup(MXListedContentsGroup group) {
        _group = group;
    }
    
    public JComponent getContentPane() {
        return _contentPane;
    }
    
    boolean _doingAnimation = false;
    
    public void add(JComponent child) {
        _contentPane.add(child);
    }

    public int count() {
        return _contentPane.getComponentCount();
    }
    
    public JComponent get(int x) {
        return (JComponent)_contentPane.getComponent(x);
    }
    
    public void openWithAnimation(boolean open) {
        _doingAnimation = true;
        if (open) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        _contentPane.setVisible(true);
                        _contentPane.setBackground(null);
                        revalidateASAP();
                    }catch(Throwable e) {
                        e.printStackTrace();;
                    }
                    for (int p = 0; p < 100; p += 5) {
                        _contentPane.setScrollPercent(p);
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();;
                        }
                    }
                    _contentPane.setScrollPercent(100);
                    _doingAnimation = false;
                    revalidateASAP();
                }
            }).start();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int p = 100; p >= 0; p -= 5) {
                        _contentPane.setScrollPercent(p);
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
                            _contentPane.setVisible(false);
                            _doingAnimation = false;
                            revalidateASAP();
                        }
                    });
                }
            }).start();
        }
    }

    public void revalidateASAP() {
        Component c = _contentPane;
        while(c != null) {
            if (c instanceof MXAccordionPanel) {
                MXAccordionPanel panel = (MXAccordionPanel)c;
                panel.revalidate();
                return;
            }
            c = c.getParent();
        }
    }
}
