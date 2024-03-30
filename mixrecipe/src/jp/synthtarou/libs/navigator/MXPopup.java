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
package jp.synthtarou.libs.navigator;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JTextField;
import jp.synthtarou.libs.MXFileLogger;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXPopup {
    JTextField _target;
    MouseListener _listener1;
    KeyListener _listener2;

    public MXPopup(JTextField target) {
        _target = target;
        install();
    }

    public synchronized void install() {
        if (_target == null) {
            return;
        }
        if (_listener1 != null) {
            return;
        }
        _listener1 = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    if (_target == null || _target.isEnabled() == false) {
                        return;
                    }
                    setupColor(true);
                    showPopup(_target);
                }catch(RuntimeException ex) {
                    MXFileLogger.getLogger(MXPopup.class).log(Level.WARNING, ex.getMessage(), ex);
                }finally {
                    setupColor(false);
                }
            }
        };
        _listener2 = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (_target == null || _target.isEnabled() == false) {
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        setupColor(true);
                        showPopup(_target);
                    }catch(RuntimeException ex) {
                        MXFileLogger.getLogger(MXPopup.class).log(Level.WARNING, ex.getMessage(), ex);
                    }finally {
                        setupColor(false);
                    }
                }
            }
        };
        _target.addMouseListener(_listener1);
        _target.addKeyListener(_listener2);
    }

    public synchronized void uninstall() {
        if (_target == null) {
            return;
        }
        if (_listener1 != null) {
            _target.removeMouseListener(_listener1);
            _listener1 = null;
        }
        if (_listener2 != null) {
            _target.removeKeyListener(_listener2);
            _listener2 = null;
        }
    }
    
    Color _colorBackup = null;
    
    public void setupColor(boolean startEdit) {
        if (_target == null) {
            return;
        }
        if (startEdit) {
            _colorBackup = _target.getBackground();
            _target.setBackground(Color.green);
        }
        else {
            _target.setBackground(_colorBackup);
            _colorBackup = null;
        }
    }

    public abstract void showPopup(JComponent mouseBase);
    
    boolean _hidden = false;
    
    public synchronized  void popupHiddenAndGoNext() {
        _hidden = true;
        notifyAll();
    }
    
    public void waitForPopupClose() {
        while (_hidden == false) {
            try {
                wait(1000);
            }catch(InterruptedException ex) {
                return;
            }
        }
    }
}
