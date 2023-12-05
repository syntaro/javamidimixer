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
package jp.synthtarou.midimixer.ccxml;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachCopyAndPaste;
import jp.synthtarou.midimixer.libs.common.MXGlobalTimer;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CXTreeModel implements TreeModel {
    final CXNode _root;
    CXTreeRenderer _render;
    JTree _tree;
    JCheckBoxMenuItem _checkAttribute;
    JCheckBoxMenuItem _checkTextContent;
    Runnable _onSelect;

    public CXTreeModel(CXNode ddfile) {
        _root = ddfile;
        _tree = null;
    }

    public void attachTreeView(JTree tree, Runnable onSelect) {
        _tree = tree;
        _render = new CXTreeRenderer();
        _onSelect = onSelect;
        tree.setCellRenderer(_render);

        _checkAttribute = new JCheckBoxMenuItem("Show Attributes");
        _checkTextContent = new JCheckBoxMenuItem("Show TextContent");
        
        _checkAttribute.setSelected(CXTreeRenderer._displayAttribute);
        _checkTextContent.setSelected(CXTreeRenderer._displayTextContent);

        _checkAttribute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CXTreeRenderer._displayAttribute = _checkAttribute.isSelected();
                _tree.updateUI();
            }
        });
        _checkTextContent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CXTreeRenderer._displayTextContent = _checkAttribute.isSelected();
                _tree.updateUI();
            }
        });

        MXAttachCopyAndPaste clip = new MXAttachCopyAndPaste(tree);
        clip.insertBeforeThis(_checkAttribute);
        clip.insertBeforeThis(_checkTextContent);
        clip.insertBeforeThis(null);
        tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                _lastMoveSequence++;
                MXGlobalTimer.letsCountdown(300, new ViewUpdateTimer(_lastMoveSequence));
            }
        });
    }

    public void selectNodeOnTree(CXNode node) {
        List<CXNode> path = _root.pathTo(node);
        
        for (int i = 0; i< path.size(); ++ i) {
            List<CXNode> sub = path.subList(0, i);
            if (sub.size() > 0) {
                _tree.expandPath(new TreePath(sub.toArray()));
            }
        }
        _tree.setSelectionPath(new TreePath(path.toArray()));
        _tree.scrollPathToVisible(new TreePath(path.toArray()));
    }

    public CXNode getSelection() {
        TreePath selected = _tree.getSelectionPath();
        if (selected != null) {
            Object[] path = selected.getPath();
            if (path != null && path.length > 0) {
                CXNode node = (CXNode) path[path.length - 1];
                return node;
            }
        }
        return null;
    }

    long _lastMoveSequence;

    class ViewUpdateTimer implements Runnable {

        long _sequence;

        public ViewUpdateTimer(long seq) {
            _sequence = seq;
        }

        public void run() {
            if (_lastMoveSequence != _sequence) {
                return;
            }
            if (_onSelect != null) {
                _onSelect.run();
            }
        }
    }

    @Override
    public Object getRoot() {
        return _root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        CXNode it =  (CXNode)parent;
        return it._listChildTags.get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        CXNode it =  (CXNode)parent;
        return it._listChildTags.size();
    }

    @Override
    public boolean isLeaf(Object node) {
        if (getChildCount(node) == 0) {
            return true;
        }
        return false;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        return;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        CXNode it =  (CXNode)parent;
        return it._listChildTags.indexOf(child);
    }
    
    @Override
    public void addTreeModelListener(TreeModelListener l) {
        return;
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        return;
    }
}
