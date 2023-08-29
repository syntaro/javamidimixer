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
package jp.synthtarou.cceditor.view.common;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCPromptUtil {

    public static void showPanelForTest(Container parent, JPanel panel) {
        Container cont = MXUtil.getOwnerWindow(parent);
        String title = MXAppConfig.MX_APPNAME;

        JDialog modal = null;
        if (cont instanceof Frame) {
            Frame F = (Frame) cont;
            modal = new JDialog(F);
        } else if (cont instanceof Dialog) {
            Dialog D = (Dialog) cont;
            modal = new JDialog(D);
        } else if (cont instanceof Window) {
            Window W = (Window) cont;
            modal = new JDialog(W);
        } else {
            modal = new JDialog();
        }
        //modal.setLayout(new BoxLayout(modal, BoxLayout.LINE_AXIS));
        modal.add(panel, null);
        modal.pack();
        modal.setModal(true);
        if (panel instanceof IPrompt) {
            IPrompt prompt = (IPrompt) panel;
            modal.setTitle(prompt.getPromptTitle());
            modal.setSize(prompt.getPromptSize());
        }
        MXUtil.centerWindow(modal);
        panel.requestFocusInWindow();
        modal.setVisible(true);
    }

    public static void closeAnyway(IPrompt prompt) {
        Component c = prompt.getAsPanel();
        while (c != null) {
            c = c.getParent();
            if (c == null) {
                break;
            }
            if (c instanceof Dialog || c instanceof Window) {
                c.setVisible(false);
                return;
            }
        }
    }

    public static void showFrame(JPanel panel) {
        String title = MXAppConfig.MX_APPNAME;

        JFrame child = null;
        child = new JFrame(title);
        child.getContentPane().add(panel, "Center");
        child.pack();

        MXUtil.centerWindow(child);
        panel.requestFocusInWindow();
        child.setVisible(true);
    }

    static class CloseFook extends WindowAdapter {

        JDialog _owner;
        IPromptForInput<Object> _input;

        CloseFook(JDialog owner, IPromptForInput input) {
            _owner = owner;
            _input = input;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            if (_input.validatePromptResult()) {
                _owner.dispose();
            }
        }
    }

    public static Object showPrompt(Container parent, IPrompt prompt) {
        Container cont = MXUtil.getOwnerWindow(parent);
        String title = prompt.getPromptTitle();

        IPromptForInput _asInput = null;

        if (prompt instanceof IPromptForInput) {
            _asInput = (IPromptForInput) prompt;
        }

        JDialog child = null;
        if (title == null) {
            title = MXAppConfig.MX_APPNAME;
        }
        if (cont instanceof Window) {
            Window W = (Window) cont;
            child = new JDialog(W, title);
        } else if (cont instanceof Dialog) {
            Dialog D = (Dialog) cont;
            child = new JDialog(D, title);
        } else {
            child = new JDialog((Window) parent, title);
        }
        child.setModal(true);
        child.getContentPane().add(prompt.getAsPanel(), "Center");
        child.pack();

        Dimension size = prompt.getPromptSize();
        if (size != null) {
            child.setSize(size);
        }
        MXUtil.centerWindow(child);
        prompt.getAsPanel().requestFocusInWindow();

        if (_asInput != null) {
            child.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            child.addWindowListener(new CloseFook(child, _asInput));
        }
        child.setVisible(true);

        if (_asInput != null) {
            return _asInput.getPromptResult();
        }
        return null;
    }
}
