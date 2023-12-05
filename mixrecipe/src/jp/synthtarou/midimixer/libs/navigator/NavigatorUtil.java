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
package jp.synthtarou.midimixer.libs.navigator;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class NavigatorUtil {
    static class CloseFook extends WindowAdapter {

        JDialog _owner;
        INavigator _input;

        CloseFook(JDialog owner, INavigator input) {
            _owner = owner;
            _input = input;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            if (_input.validateWithNavigator(null)) {
                _owner.dispose();
            }
        }
    }

    public static void showNavigator(Container parent, INavigator navi, String title) {
        Container cont = MXUtil.getOwnerWindow(parent);

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
        child.getContentPane().add(navi.getNavigatorPanel(), "Center");
        child.pack();

        MXUtil.centerWindow(child);
        navi.getNavigatorPanel().requestFocusInWindow();

        child.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        child.addWindowListener(new CloseFook(child, navi));
        child.setVisible(true);
    }
}
