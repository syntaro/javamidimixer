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
package jp.synthtarou.midimixer.libs.swing.folderbrowser;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MyRenderer extends  DefaultTreeCellRenderer{

        private TreeCellRenderer _defRenderer;
        private FileSystemView _view;

        MyRenderer() {
            _defRenderer = new JTree().getCellRenderer();
            _view = FileSystemView.getFileSystemView();
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component component = _defRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object user = node.getUserObject();
                if (user != null) {
                    if (user instanceof File) {
                        File file = (File) node.getUserObject();
                        JLabel label = (JLabel) component;
                        label.setIcon(_view.getSystemIcon(file));
                        label.setText(_view.getSystemDisplayName(file));
                        //label.setToolTipText(file.getPath());
                    }
                    else if (user instanceof FileSystemCache.Element) {
                        FileSystemCache.Element element = (FileSystemCache.Element) node.getUserObject();
                        Icon icon = element.getIcon();
                        JLabel label = (JLabel) component;
                        label.setIcon(icon);
                        label.setText(element.getDisplayName());
                        //label.setToolTipText(file.getPath());
                        if (icon != null) {                    
                            int width = 200;
                            /*
                            try {
                                JViewport port = (JViewport)tree.getParent();
                                JScrollPane scroll = (JScrollPane)port.getParent();
                                width = scroll.getWidth();
                            }catch(Exception e) {
                                e.printStackTrace();
                            }*/
                            component.setPreferredSize(new Dimension(width, icon.getIconHeight() + 4));
                        }
                    }
                }
            }
            
            return component;
        }
}
