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
package jp.synthtarou.midimixer.libs.swing.attachment;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXAttachTableResize {

    JTable _table;

    public MXAttachTableResize(JTable table) {
        _table = table;
        table.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                autoResizeTableColumnWidth(_table);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
                System.out.println(".componentShown()");
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
        autoResizeTableColumnWidth(_table);
    }

    public static void autoResizeTableColumnWidth(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        final TableColumnModel columnModel = table.getColumnModel();
        int totalWidth = table.getWidth();

        Container cont = table.getParent();
        if (cont != null) {
            totalWidth = cont.getWidth();
        }

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
                    } else if (r instanceof String) {
                        width = Math.max(new JLabel((String) r).getPreferredSize().width, width);
                    } else {
                        width = Math.max(50, width);
                    }
                } else {
                    TableCellRenderer renderer = table.getCellRenderer(row, column);
                    Component comp = table.prepareRenderer(renderer, row, column);
                    width = Math.max(comp.getPreferredSize().width + 30, width);
                }
            }
            if (column == table.getColumnCount() - 1) {
                columnModel.getColumn(column).setPreferredWidth(totalWidth);
            } else {
                totalWidth -= width;
                columnModel.getColumn(column).setPreferredWidth(width);
            }
        }
    }
}