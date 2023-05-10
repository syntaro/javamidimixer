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
package jp.synthtarou.midimixer.libs.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jp.synthtarou.midimixer.MXStatic;
import jp.synthtarou.midimixer.mx30controller.MGCircle;
import jp.synthtarou.midimixer.mx30controller.MGPad;
import jp.synthtarou.midimixer.mx30controller.MGSlider;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXUtil {
    public static String toHexFF(int i) {
        String str = Integer.toHexString(i).toUpperCase();
        if (str.length() == 1) {
            return "0" + str;
        }
        if (str.length() >= 3) {
            return str.substring(str.length() - 2, str.length());
        }
        return str;
    }

    public static String dumpHexFF(byte[] data) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < data.length; ++i) {
            if (i != 0) {
                str.append(" ");
            }
            str.append(toHexFF((int) data[i]));
        }
        return str.toString();
    }
 
    public static final int parseTextForNumber(String text) {
        int x = 0;
        int start = 0;
        int mum = 10;
        if (text == null) {
            return -1;
        }
        if (text.startsWith("0x")) {
            text = text.substring(2);
            mum = 16;
        }
        if (text.endsWith("h") || text.endsWith("H")) {
            text = text.substring(0, text.length() - 1);
            mum = 16;
        }
        for (start = 0; start < text.length(); ++start) {
            char ch = text.charAt(start);
            if (ch == ' ' || ch == '\t' || ch == '\n') {
                continue;
            }
            break;
        }
        if (start >= text.length()) {
            return -1;
        }
        for (; start < text.length(); ++start) {
            int ch = text.charAt(start) & 255;
            if (ch >= '0' && ch <= '9') {
                x *= mum;
                x += ch - (char) '0';
            } else if (ch >= 'A' && ch <= 'F' && mum == 16) {
                x *= mum;
                x += ch - (char) 'A' + 10;
            } else if (ch >= 'a' && ch <= 'f' && mum == 16) {
                x *= mum;
                x += ch - (char) 'a' + 10;
            } else {
                break;
            }
        }
        return x;
    }
    
    
    public static boolean searchTextIgnoreCase(String text, String words) {
        text = text.toLowerCase();
        words = words.toLowerCase();
        if (words.indexOf(' ') < 0) {
            return text.indexOf(words) >= 0;
        }
        ArrayList<String> cells = new ArrayList();
        split(words, cells, ' ');
        for (String parts : cells) {
            if (text.indexOf(parts) < 0) {
                return false;
            }
        }
        return true;
    }

    public static void swingTreeEnable(Component c, boolean enable) {
        if (c instanceof Container) {
            Container parent = (Container) c;
            int count = parent.getComponentCount();
            for (int x = 0; x < count; ++x) {
                swingTreeEnable(parent.getComponent(x), enable);
            }
        }
        if (c instanceof JComponent) {
            ((JComponent) c).setEnabled(enable);
        }
    }

    public static void swingTreeEditable(Component c, boolean enable) {
        if (c instanceof Container) {
            Container parent = (Container) c;
            int count = parent.getComponentCount();
            for (int x = 0; x < count; ++x) {
                swingTreeEditable(parent.getComponent(x), enable);
            }
        }
        if (c instanceof MGCircle) {
            MGCircle circle = (MGCircle)c;
            circle.setValueChangeable(enable);
        }
        if (c instanceof MGSlider) {
            MGSlider slider = (MGSlider)c;  
            slider.setValueChangeable(enable);
        }
        if (c instanceof MGPad) {
            MGPad pad = (MGPad)c;
            pad.setValueChangeable(enable);
        }
    }

    public static void showAsDialog(Container parent, JPanel panel, String title) {
        Container cont = getOwnerWindow(parent);
        if (title == null) {
            title = MXStatic.MX_APPNAME_WITH_VERSION;
        }
        JDialog dialog = null;
        if (cont instanceof Window) {
            Window W = (Window) cont;
            dialog = new JDialog(W, title);
        } else if (cont instanceof Dialog) {
            Dialog D = (Dialog) cont;
            dialog = new JDialog(D, title);
        } else {
            dialog = new JDialog((Window) parent, title);
        }
        dialog.setModal(true);
        dialog.getContentPane().add(panel, "Center");
        dialog.pack();
        centerWindow(dialog);
        panel.requestFocusInWindow();
        dialog.setVisible(true);
    }
    
    public static void closeOwnerWindow(Component c) {
        while(c != null) {
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

    public static void autoResizeTableColumnWidth(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 10; // Min width
            for (int row = -1; row < table.getRowCount(); row++) {
                if (row < 0) {
                    TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
                    TableColumnModel model = table.getColumnModel();
                    TableColumn col = model.getColumn(column);
                    col.getHeaderValue();
                    Object r = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, column);
                    if (r instanceof Component) {
                        Component comp = (Component) r;
                        width = Math.max(comp.getPreferredSize().width + 30, width);
                    } else {
                        width = 50;
                    }
                } else {
                    TableCellRenderer renderer = table.getCellRenderer(row, column);
                    Component comp = table.prepareRenderer(renderer, row, column);
                    width = Math.max(comp.getPreferredSize().width + 30, width);
                }
            }
            if (width > 300) {
                width = 300;
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    public static Container getOwnerWindow(Component panel) {
        while (panel != null) {
            if (panel instanceof Window) {
                return (Window) panel;
            }
            if (panel instanceof Dialog) {
                return (Dialog) panel;
            }
            panel = panel.getParent();
        }
        return null;
    }

    public static void split(String str, ArrayList<String> list, char splitter) {
        list.clear();
        int len = str.length();
        int from = 0;
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (ch == splitter) {
                list.add(str.substring(from, i));
                from = i + 1;
                continue;
            }
        }
        if (from < len) {
            list.add(str.substring(from, len));
        }
    }

    public static Color mixedColor(Color left, Color right, int percent) {
        int lr = (int) (left.getRed() * (100 - percent) / 100);
        int lg = (int) (left.getGreen() * (100 - percent) / 100);
        int lb = (int) (left.getBlue() * (100 - percent) / 100);
        int rr = (int) (right.getRed() * percent / 100);
        int rg = (int) (right.getGreen() * percent / 100);
        int rb = (int) (right.getBlue() * percent / 100);
        return new Color(lr + rr, lg + rg, lb + rb);
    }
    
    public static void backgroundRecursive(Container container, Color color) {
        LinkedList<Container> listContainer = new LinkedList();
        listContainer.add(container);
        
        while(listContainer.isEmpty() == false) {
            Container cont = listContainer.remove();
            if (cont == null) {
                continue;
            }
            cont.setBackground(color);
            
            Component[] list = cont.getComponents();
            for (Component child : list) {
                if (child instanceof Container) {
                    listContainer.add((Container)child);
                }else {
                    child.setBackground(color);
                }
            }
        }
    }
}
