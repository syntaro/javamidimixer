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
package jp.synthtarou.midimixer.libs.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.MXGlobalTimer;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXButtonUILabel {
    String _title;
    JLabel _toInstall;

    public MXButtonUILabel(JLabel toInstall, Runnable target) {
        _title = toInstall.getText();
        _toInstall = toInstall;
        _toInstall.setText("");
        setSelected(false);
        toInstall.addMouseListener(new MouseListener() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setSelected(false);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setSelected(false);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setSelected(false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setSelected(true);
                if (target != null) {
                    target.run();
                    setSelected(false);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                setSelected(false);
            }
        });
    }
    
    public JLabel getLabel() {
        return _toInstall;
    }
    
    public Dimension getSize() {
        return _toInstall.getSize();
    }

    long timer = 0;
    
    public void setSelected(boolean push) {
        if (push) {
            _toInstall.setIcon(new SelectedButtonIcon(_title));
            timer = System.currentTimeMillis();
            MXGlobalTimer.letsCountdown(100, new Runnable() {
                public void run() {
                    if (timer + 100 >= System.currentTimeMillis()) {
                        _toInstall.setIcon(new ButtonIcon(_title));
                    }
                }
            });
        }else {
            if (timer == 0) {
                _toInstall.setIcon(new ButtonIcon(_title));
            }
            timer = System.currentTimeMillis();
            MXGlobalTimer.letsCountdown(100, new Runnable() {
                public void run() {
                    if (timer + 100 >= System.currentTimeMillis()) {
                        _toInstall.setIcon(new ButtonIcon(_title));
                    }
                }
            });
        }
    }

    class ButtonIcon implements Icon {
        private final JButton _component;
        int preWidth, preHeight;

        public ButtonIcon(String text) {
            _component = new JButton(text);
            preWidth = Math.max(getSize().width, _component.getPreferredSize().width);
            preHeight = Math.max(getSize().height, _component.getPreferredSize().height);
        }

        @Override public int getIconWidth() {
            return preWidth;
        }

        @Override public int getIconHeight() {
            return preHeight;
        }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            _component.setSize(new Dimension(getIconWidth(), getIconHeight()));
            SwingUtilities.paintComponent(g, _component, c.getParent(), x, y, getIconWidth(), getIconHeight());
        }
    }

    class SelectedButtonIcon implements Icon {
        int preWidth, preHeight;
        private final JToggleButton _component;

        public SelectedButtonIcon(String text) {
            _component = new JToggleButton(text);
            _component.setSelected(true);
            preWidth = Math.max(getSize().width, _component.getPreferredSize().width);
            preHeight = Math.max(getSize().height, _component.getPreferredSize().height);
        }

        @Override public int getIconWidth() {
            return preWidth;
        }

        @Override public int getIconHeight() {
            return preHeight;
        }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            _component.setSize(new Dimension(getIconWidth(), getIconHeight()));
            SwingUtilities.paintComponent(g, _component, c.getParent(), x, y, getIconWidth(), getIconHeight());
        }
    }
}
