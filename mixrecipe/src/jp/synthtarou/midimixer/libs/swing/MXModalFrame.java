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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXModalFrame {

    public static void main(String[] args) {
        JFrame frame = new JFrame("test");
        frame.setPreferredSize(new Dimension(400, 250));
        frame.setBackground(Color.cyan);
        frame.setVisible(true);

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(400, 250));
        panel.setBackground(Color.blue);
        MXModalFrame.showAsDialog(frame, panel, "test");

        panel.setPreferredSize(new Dimension(500, 250));
        panel.setBackground(Color.red);
        MXModalFrame.showAsDialog(frame, panel, "test");

        panel.setPreferredSize(new Dimension(600, 250));
        panel.setBackground(Color.green);
        MXModalFrame.showAsDialog(frame, panel, "test");

        panel.setPreferredSize(new Dimension(700, 250));
        panel.setBackground(Color.yellow);
        MXModalFrame.showAsDialog(frame, panel, "test");

        panel.setPreferredSize(new Dimension(800, 250));
        panel.setBackground(Color.white);
        MXModalFrame.showAsDialog(frame, panel, "test");
        System.exit(0);
    }

    public static void showAsDialog(Container parent, JPanel content, String title) {
        if (false) {
            MXModalFrame dialog = new MXModalFrame(parent, content, title);
            dialog.doOpen();
            dialog.waitForOpen();
            dialog.waitForClose();
        } else {
            MXUtil.showAsDialog(parent, content, title);
        }
    }

    JPanel _content;
    JFrame _jframe;
    Container _parent;
    String _title;

    /**
     *
     * @param parent
     * @param content
     * @param title
     */
    public MXModalFrame(Container parent, JPanel content, String title) {
        _parent = parent;
        _jframe = null;
        _content = content;
        _title = title;
    }

    public MXModalFrame doOpen() {
        if (SwingUtilities.isEventDispatchThread() == false) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        doOpen();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            waitForOpen();
            return this;
        }

        System.out.println("doOpen");
        synchronized (this) {
            if (_jframe != null) {
                return this;
            }
            if (_title == null) {
                _title = MXAppConfig.MX_APPNAME;
            }
            _jframe = new JFrame(_title);
            _jframe.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            _jframe.setContentPane(_content);
            _jframe.addWindowListener(new WindowListener() {

                @Override
                public synchronized void windowClosing(WindowEvent e) {
                    doClosing();
                }

                @Override
                public synchronized void windowClosed(WindowEvent e) {
                    doClosing();
                }

                @Override
                public void windowOpened(WindowEvent e) {
                }

                @Override
                public void windowIconified(WindowEvent e) {
                }

                @Override
                public void windowDeiconified(WindowEvent e) {
                }

                @Override
                public void windowActivated(WindowEvent e) {
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                    if (_jframe != null) {
                        if (_jframe.isVisible() == false) {
                            doClosing();
                        }
                    }
                }
            }
            );
            _parent = MXUtil.getOwnerWindow(_parent);

            _jframe.setContentPane(_content);
            _jframe.pack();
            MXUtil.centerWindow(_jframe);
            _content.requestFocusInWindow();
            _jframe.setVisible(true);
            if (_parent != null) {
                _parent.setEnabled(false);
            }
        }
        return this;
    }

    protected void doClosing() {
        System.out.println("doClosing");
        synchronized (this) {
            _jframe = null;
            notifyAll();
        }
    }

    public void waitForClose() {
        System.out.println("waitForClose");
        while (true) {
            if (_jframe == null) {
                return;
            }
            synchronized (this) {
                try {
                    System.out.println(System.currentTimeMillis());
                    this.wait(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public synchronized MXModalFrame waitForOpen() {
        while (true) {
            if (_jframe == null) {
                return this;
            }
            if (_jframe.isVisible() == false) {
                try {
                    this.wait(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } else {
                return this;
            }
        }
    }
}
