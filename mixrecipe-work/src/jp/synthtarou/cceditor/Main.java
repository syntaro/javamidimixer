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

package jp.synthtarou.cceditor;

import jp.synthtarou.cceditor.view.MainFrame;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class Main {
    static MainFrame _mainFrame;

    public static MainFrame getMainFrame() {
        return _mainFrame;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        _mainFrame = new MainFrame();
        _mainFrame.setVisible(true);
    }
    
    public static void centerWindow(Component c) {
        Component owner = (c instanceof Window) ? ((Window) c).getOwner() : null;
        if (owner != null && !owner.isVisible()) {
            owner = null;
        }
        Component parent = (owner != null) ? owner : c.getParent();
        if (parent != null && !parent.isVisible()) {
            parent = null;
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension mySize = c.getSize();
        Point loc = new Point(0, 0);
        if (parent != null) {
            screenSize = parent.getSize();
            if (parent == owner) {
                loc = owner.getLocation();
            }
        }
        loc.x += (screenSize.width - mySize.width) / 2;
        loc.y += (screenSize.height - mySize.height) / 2;
        c.setLocation(loc);
    }

    public static final String APPLICATION_NAME = "XMLCC Editor";
    public static final String APPLICATION_VERSION = "Mock-Up 2023-05-28";
    public static final String TITLE = APPLICATION_NAME + "(" + APPLICATION_VERSION + ")";
}
        

