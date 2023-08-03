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
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.TreeSet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCFolderBrowser extends javax.swing.JPanel implements IPromptForInput<File> {

    FileSystemView _view = FileSystemView.getFileSystemView();
    DefaultTreeModel _model;
    TreeSet<File> _already;
    boolean _ignoreSystemFile = true;
    boolean _onlyDirectory = false;
    FileFilter _onlyFilter = null;
    File _curerntDirectory = null;

    public static void main(String[] args) {
        FileFilter filter = new CCFileExtensionFilter(new String[]{".xml"});
        CCFolderBrowser chooser = new CCFolderBrowser(new File("C:/Domino144/Module"), filter);
        CCPromptUtil.showPanelForTest(null, chooser);
        System.out.println("Return " + chooser.getSelectedFile());
        System.exit(0);
    }

    public static final boolean APPROVE_OPTION = true;
    public static final boolean CANCEL_OPTION = false;

    public File getSelectedFile() {
        return _result;
    }

    /**
     * Creates new form MXFolderChooser
     */
    public CCFolderBrowser(File initialDir, FileFilter filter) {
        initComponents();

        _already = new TreeSet();
        _curerntDirectory = initialDir;
        _onlyFilter = filter;

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(null);
        fillRoot(node);

        _model = new DefaultTreeModel(node);
        _model.setAsksAllowsChildren(true);

        jTree1.setCellRenderer(new MyRenderer());
        jTree1.setRootVisible(false);
        jTree1.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                TreePath path = event.getPath();
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                fillChildren(node);
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            }
        });
        jTree1.setModel(_model);
        jTree1.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getNewLeadSelectionPath();
                if (path != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    File f = (File) node.getUserObject();
                    _selection = f;
                    if (f == null) {
                        jLabelSelection.setText("-");
                    }
                    else {
                        jLabelSelection.setText(f.toString());
                    }
                }
            }
        });

        jTree1.setRequestFocusEnabled(true);
        jTree1.requestFocusInWindow();

        setPreferredSize(new Dimension(500, 300));
        selectFile(separateDirectory(_curerntDirectory));
    }
    
    public ArrayList<File> separateDirectory(File file) {
        ArrayList<File> path = new ArrayList<>();
        while (file.exists() == false) {
            file = file.getParentFile();
            if (file == null) {
                return path;
            }
        }
        path.add(0, file);
        while (true) {
            File parent = file.getParentFile();
            if (parent == null) {
                break;
            }
            path.add(0, parent);
            file = parent;
        }
        return path;
    }

    public void expand(ArrayList<TreeNode> selection) {
        TreeNode[] arrayPath = new TreeNode[selection.size()];
        selection.toArray(arrayPath);
        
        TreePath path = new TreePath(arrayPath);
        jTree1.expandPath(path);
        jTree1.setSelectionPath(path);
        jTree1.scrollPathToVisible(path);
    }

    public void selectFile(ArrayList<File> path) {
        ArrayList<TreeNode> selection = new ArrayList<>();

        DefaultMutableTreeNode maybeRoot = (DefaultMutableTreeNode)_model.getRoot();
        selection.add(maybeRoot);
        expand(selection);

        DefaultMutableTreeNode maybeDesktop = (DefaultMutableTreeNode) maybeRoot.getChildAt(0);
        selection.add(maybeDesktop);
        fillChildren(maybeDesktop);
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)maybeDesktop.getChildAt(0);

        DefaultMutableTreeNode lastHit = null;
        selection.add(node);
        expand(selection);

        for (int p = 0; p < path.size(); ++ p) {
            File target = path.get(p);
            fillChildren(node);
            DefaultMutableTreeNode hit = null;
            
            for (int i = 0; i < node.getChildCount(); ++ i) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                File file = (File)childNode.getUserObject();
                if (file.canRead()) {
                    String path1 = file.getPath();
                    String path2 = target.getPath();
                    if (path1.equalsIgnoreCase(path2)) {
                        hit = childNode;
                        break;
                    }
                }
            }
            
            if (hit != null) {
                node = hit;
                lastHit = hit;
                selection.add(node);
                expand(selection);

                continue;
            }
            else {
                break;
            }
        }
    }

    protected void fillRoot(DefaultMutableTreeNode node) {
        File[] listChild = _view.getRoots();
        if (listChild != null) {
            boolean first = true;
            for (File file : listChild) {
                DefaultMutableTreeNode cnode = new DefaultMutableTreeNode(file);
                node.add(cnode);
                cnode.setAllowsChildren(true);
            }
            node.setAllowsChildren(true);
        }
    }

    public boolean filterIfLongTimeToRead(File f) {
        if (f.getPath().startsWith("::")) {
            if (f.toString().equals("ネットワーク")
                    || f.toString().equalsIgnoreCase("network")) {
                return true;
            }
            if (f.canRead() == false) {
                System.out.println("cant read " + f);
                return true;
            }
        }
        return false;
    }

    public boolean isTargetVisible(File target) {
        if (target.isDirectory() == false) {
            if (_onlyFilter.accept(target)) {
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean isTargetOpenable(File target) {
        if (isTargetVisible(target)) {
            if (_onlyDirectory) {
                return target.isDirectory();
            }
            return true;
        }
        return false;
    }

    protected void fillChildren(DefaultMutableTreeNode node) {
        File file = (File) node.getUserObject();

        if (_already.contains(file) == false) {
            _already.add(file);

            if (filterIfLongTimeToRead(file)) {
                return;
            }
            File[] listChild = _view.getFiles(file, true); //2nd : useFileHiding
            if (listChild != null) {
                for (File c : listChild) {
                    if (isTargetVisible(c) == false) {
                        continue;
                    }
                    DefaultMutableTreeNode cnode = new DefaultMutableTreeNode(c);

                    if (c.isDirectory()) {
                        boolean deepThought = true;
                        if (deepThought) {
                            if (filterIfLongTimeToRead(c)) {
                                cnode.setAllowsChildren(false);
                            } else {
                                File[] seek = c.listFiles();
                                boolean found = false;
                                if (seek != null) {
                                    for (File f : seek) {
                                        if (isTargetOpenable(f)) {
                                            found = true;
                                        }
                                    }
                                }
                                cnode.setAllowsChildren(found);
                            }
                        } else {
                            cnode.setAllowsChildren(true);
                        }
                    } else {
                        cnode.setAllowsChildren(false);
                    }

                    node.add(cnode);
                }
            }
        }
    }

    @Override
    public JPanel getAsPanel() {
        return this;
    }

    @Override
    public String getPromptTitle() {
        return null;
    }

    @Override
    public Dimension getPromptSize() {
        return new Dimension(700, 600);
    }

    @Override
    public File getPromptResult() {
        return _result;
    }

    static class MyRenderer extends DefaultTreeCellRenderer {

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
                if (user != null && user instanceof File) {
                    File file = (File) node.getUserObject();
                    JLabel label = (JLabel) component;
                    label.setIcon(_view.getSystemIcon(file));
                    label.setText(_view.getSystemDisplayName(file));
                    label.setToolTipText(file.getPath());
                }
            }

            return component;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jLabelSelection = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();

        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });
        setLayout(new java.awt.GridBagLayout());

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButtonOK, gridBagConstraints);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jButtonCancel, gridBagConstraints);

        jLabelSelection.setText("Please Select 1 From Tree");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jLabelSelection, gridBagConstraints);

        jTree1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTree1MouseClicked(evt);
            }
        });
        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });
        jTree1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTree1KeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTree1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        setResultAndClose(_selection);
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        setResultAndClose(null);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jTree1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTree1MouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() >= 2) {
            //setResultAndClose(_selection);
        }
    }//GEN-LAST:event_jTree1MouseClicked

    private void jTree1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTree1KeyPressed
        switch (evt.getExtendedKeyCode()) {
            case KeyEvent.VK_ENTER:
                setResultAndClose(_selection);
                return;
            case KeyEvent.VK_ESCAPE:
                setResultAndClose(null);
                return;
        }
    }//GEN-LAST:event_jTree1KeyPressed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        switch (evt.getExtendedKeyCode()) {
            case KeyEvent.VK_ENTER:
                setResultAndClose(_selection);
                return;
            case KeyEvent.VK_ESCAPE:
                setResultAndClose(null);
                return;
        }
    }//GEN-LAST:event_formKeyPressed

    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged

    }//GEN-LAST:event_jTree1ValueChanged

    File _selection;
    File _result = null;

    @Override
    public boolean validatePromptResult() {
        if (_result != null && _result.getPath().startsWith("::")) {
            JOptionPane.showMessageDialog(this, "Can't select System Folder", "notice", JOptionPane.OK_OPTION);
            return false;
        }
        return true;
    }

    public void setResultAndClose(File result) {
        if (result != null)  {
            _result = result;
            if (!validatePromptResult()) {
                return;
            }
        }
        else  {
            _result = null;
        }
        CCPromptUtil.closeAnyway(this);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabelSelection;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
}
