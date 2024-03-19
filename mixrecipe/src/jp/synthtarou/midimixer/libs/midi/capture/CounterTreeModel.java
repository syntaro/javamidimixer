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
package jp.synthtarou.midimixer.libs.midi.capture;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import jp.synthtarou.midimixer.libs.swing.UITask;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CounterTreeModel extends DefaultTreeModel {

    public static class CounterRender implements  TreeCellRenderer {
        TreeCellRenderer _defRender;
        public CounterRender(JTree tree) {
            _defRender = tree.getCellRenderer();
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
                value = node.getUserObject();
            }
            if (value instanceof String) {
                
            }
            else if (value instanceof Counter) {
                Counter c = (Counter)value;
                value = c.formatCounter();
            }
            else if (value instanceof CounterFolder) {
                CounterFolder f = (CounterFolder)value;
                value = f._name;
            }
            else {
                value = "#" + value.getClass().getName();
            }
            return _defRender.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
        
    } 
    private List<CounterFolder> _listFolder = new ArrayList<>();
    
    public CounterTreeModel() {
        super(new DefaultMutableTreeNode("Root"));
    }
    
    @Override
    public boolean isLeaf(Object o) {
        if (o instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)o;
            o = node.getUserObject();
        }
        if (o instanceof Counter) {
            return true;
        }
        if (o instanceof CounterFolder) {
            return false;
        }
        return false;
    }
    
    public void reload0(TreeNode node) {
        new UITask() {
            @Override
            public Object run() {
                if (node == null) {
                    reload();
                }
                else {
                    reload(node);
                }
                return null;
            }
        };
    }

    public void addFolder(CounterFolder folder) {
        _listFolder.add(folder);
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)getRoot();
        root.add(new DefaultMutableTreeNode(folder));
        reload0(null);
    }
    
    public void reloadCounter(Counter counter) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)getRoot();
        for (int x = 0; x < root.getChildCount(); ++ x) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)root.getChildAt(x);
            if (child.getUserObject() == counter._bindedFolder) {
                for (int y = 0;  y < child.getChildCount(); ++ y) {
                    DefaultMutableTreeNode folder = (DefaultMutableTreeNode)child.getChildAt(y);
                    if (folder.getUserObject() == counter) {
                        reload0(folder);
                    }
                }
            }
        }
    }
    
    public void addCounter(CounterFolder folder, Counter counter) {
        folder.add(counter);
        
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)getRoot();
        for (int x = 0; x < root.getChildCount(); ++ x) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)root.getChildAt(x);
            if (child.getUserObject() == folder) {
                child.add(new DefaultMutableTreeNode(counter));
            }
        }
    }

    public void reloadFolder(CounterFolder folder) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)getRoot();
        for (int x = 0; x < root.getChildCount(); ++ x) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)root.getChildAt(x);
            if (child.getUserObject() == folder) {
                reload0(child);
            }
        }
    }
}
