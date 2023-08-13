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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import jp.synthtarou.cceditor.view.common.CCPromptUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXAttachClipboarder {

    public static void main(String[] args) {
        CCPromptUtil.showPanelForTest(null, new ClipboardTestPanel());
        System.exit(0);
    }

    ArrayList<JMenuItem> _menusBeforeThis = new ArrayList<>();
    ArrayList<JMenuItem> _menusAfterThis = new ArrayList<>();
    JPopupMenu _popup = null;

    JComponent _component = null;

    public MXAttachClipboarder(JComponent component) {
        _component = component;
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != 1) {
                    createPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    public void insertBeforeThis(JMenuItem item) {
        if (_popup != null) {
        }
        _menusBeforeThis.add(item);
        _popup = null;
    }

    public void insertAfterThis(JMenuItem item) {
        if (_popup != null) {
        }
        _menusAfterThis.add(item);
        _popup = null;
    }

    ActionListener actionCut = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (_component instanceof JTextField) {
                JTextField field = (JTextField) _component;
                field.cut();
            }
            if (_component instanceof JTextArea) {
                JTextArea area = (JTextArea) _component;
                area.cut();
            }
        }
    };

    ActionListener actionCopy = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (_component instanceof JTextField) {
                JTextField field = (JTextField) _component;
                field.copy();
            }
            if (_component instanceof JTextArea) {
                JTextArea area = (JTextArea) _component;
                area.copy();
            }
            if (_component instanceof JLabel) {
                JLabel label = (JLabel)_component;
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection selection = new StringSelection(label.getText());
                clipboard.setContents(selection, null);
            }
        }
    };

    ActionListener actionPaste = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (_component instanceof JTextField) {
                JTextField field = (JTextField) _component;
                field.paste();
            }
            if (_component instanceof JTextArea) {
                JTextArea area = (JTextArea) _component;
                area.paste();
            }
        }
    };

    ActionListener actionSelectAll = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (_component instanceof JTextField) {
                JTextField field = (JTextField) _component;
                field.selectAll();
            }

            if (_component instanceof JTextArea) {
                JTextArea area = (JTextArea) _component;
                area.selectAll();
            }
        }
    };

    public JPopupMenu createPopupMenu() {
        if (_popup != null) {
            return _popup;
        }

        JPopupMenu menuPopup = new JPopupMenu();

        for (JMenuItem item : _menusBeforeThis) {
            if (item != null) {
                menuPopup.add(item);
            } else {
                menuPopup.addSeparator();
            }
        }

        JMenuItem menuCut = new JMenuItem("Cut");
        JMenuItem menuCopy = new JMenuItem("Copy");
        JMenuItem menuPaste = new JMenuItem("Paste");
        JMenuItem menuSelectAll = new JMenuItem("Select All");

        menuCut.addActionListener(actionCut);
        menuCopy.addActionListener(actionCopy);
        menuPaste.addActionListener(actionPaste);
        menuSelectAll.addActionListener(actionSelectAll);

        if (_component instanceof JTextField) {
            JTextField field = (JTextField) _component;
            menuPopup.add(menuCut);
            menuPopup.add(menuCopy);
            if (field.isEditable()) {
                menuPopup.add(menuPaste);
                menuPopup.add(menuSelectAll);
            }
        }
        else if (_component instanceof JTextArea) {
            JTextArea area = (JTextArea) _component;
            menuPopup.add(menuCut);
            menuPopup.add(menuCopy);
            if (area.isEditable()) {
                menuPopup.add(menuPaste);
                menuPopup.add(menuSelectAll);
            }
        }
        else {
            menuPopup.add(menuCopy);
        }

        for (JMenuItem item : _menusAfterThis) {
            if (item != null) {
                menuPopup.add(item);
            } else {
                menuPopup.addSeparator();
            }
        }
        _popup = menuPopup;
        return _popup;
    }
}
