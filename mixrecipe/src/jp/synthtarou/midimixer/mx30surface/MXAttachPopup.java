/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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
package jp.synthtarou.midimixer.mx30surface;

import com.sun.source.tree.ThrowTree;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTextField;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXAttachPopup {
    JTextField _target;
    MouseListener _listener1;
    KeyListener _listener2;
    Throwable _owner;

    public MXAttachPopup(JTextField target) {
        _target = target;
        _owner = new Throwable("Just for Debug");
        install();
    }

    public synchronized void install() {
        if (_listener1 != null) {
            return;
        }
        _listener1 = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    showPopup();
                }catch(Throwable ex) {
                    ex.printStackTrace();
                    _owner.printStackTrace();;
                }
            }
        };
        _listener2 = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    showPopup();
                }
            }
        };
        _target.addMouseListener(_listener1);
        _target.addKeyListener(_listener2);
    }

    public synchronized void uninstall() {
        if (_listener1 != null) {
            _target.removeMouseListener(_listener1);
            _listener1 = null;
        }
        if (_listener2 != null) {
            _target.removeKeyListener(_listener2);
            _listener2 = null;
        }
    }

    public abstract void showPopup();
}
