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
package jp.synthtarou.midimixer.mx80vst;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.TreeSet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import javax.swing.tree.TreePath;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class FolderBrowser extends javax.swing.JPanel {

    FileSystemView _view  = FileSystemView.getFileSystemView();
    DefaultTreeModel _model;
    TreeSet<File> _already;
    boolean _onlyDirectory = false;
    
    public static void main(String[] args) {
        FolderBrowser chooser = new FolderBrowser();
        chooser._onlyDirectory = true;
        MXUtil.showAsDialog(null, chooser, "FolderPicker");
        
        System.out.println("Return " + chooser._result.getPath());
    }

    /**
     * Creates new form MXFolderChooser
     */
    public FolderBrowser() {
        initComponents();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(null);
        fillRoot(node);

        jLabel2.setText("");

        _model =  new DefaultTreeModel(node);
        _model.setAsksAllowsChildren(true);
        jTree1.setCellRenderer(new MyRenderer());
        jTree1.setRootVisible(false);
        jTree1.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                TreePath path = event.getPath();
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                fillChildren(node);
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            }
        });
        jTree1.setModel(_model);
        jTree1.expandRow(0);
        jTree1.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getNewLeadSelectionPath();
                if (path != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                    File f = (File)node.getUserObject();
                    _selection = f;
                    jLabelSelection.setText(f.toString());
                }
            }
        });

        jTree1.setRequestFocusEnabled(true);
        jTree1.requestFocusInWindow();
        jTree1.setSelectionRow(0);
        
        setPreferredSize(new Dimension(500, 300));
    }
    
    protected void fillRoot(DefaultMutableTreeNode node) {
        if (_already != null) {
            return;
        }
        
        _already = new TreeSet();

        File[] listChild = _view.getRoots();
        if (listChild != null) {
            for (File c : listChild) {
                DefaultMutableTreeNode cnode = new DefaultMutableTreeNode(c);
                node.add(cnode);
                cnode.setAllowsChildren(true);
            }
            node.setAllowsChildren(true);
        }
    }
    
    public boolean filterIfNetwork(File f) {
        if (f.getPath().startsWith("::")) {
            if (f.toString().equals("ネットワーク") 
             || f.toString().equalsIgnoreCase("network")) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isUsableFile(File target) {
        if (_onlyDirectory) {
            return target.isDirectory();
        }
        return true;
    }
    
    protected void fillChildren(DefaultMutableTreeNode node) {
        File file = (File)node.getUserObject();
        if (_already.contains(file) == false) {
            _already.add(file);

            if (filterIfNetwork(file)) {
                return;
            }
            File[] listChild = _view.getFiles(file, true); //2nd : useFileHiding
            if (listChild != null) {
                for (File c : listChild) {
                    if (isUsableFile(c) == false) {
                        continue;
                    }
                    DefaultMutableTreeNode cnode = new DefaultMutableTreeNode(c);

                    if (c.isDirectory()) {
                        boolean deepThought = true;
                        if (deepThought) {
                            if (filterIfNetwork(c)) {
                                cnode.setAllowsChildren(false);
                            }else {
                                File[] seek = c.listFiles();
                                boolean found = false;
                                if (seek != null) {
                                    for (File f : seek) {
                                        if (isUsableFile(f)) {
                                            found = true;
                                        }
                                    }
                                }
                                cnode.setAllowsChildren(found);
                            }
                        }else {
                            cnode.setAllowsChildren(true);
                        }
                    }else {
                        cnode.setAllowsChildren(false);
                    }
                    
                    node.add(cnode);
                }
            }
        }
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
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
                Object user = node.getUserObject();
                if (user != null && user instanceof File) {
                    File file = (File)node.getUserObject();
                    JLabel label = (JLabel)component;
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
        jLabel2 = new javax.swing.JLabel();

        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });
        setLayout(new java.awt.GridBagLayout());

        jButtonOK.setText("Enter");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(jButtonOK, gridBagConstraints);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        add(jButtonCancel, gridBagConstraints);

        jLabelSelection.setText("Please Select 1 From Tree");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jLabelSelection, gridBagConstraints);

        jTree1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTree1MouseClicked(evt);
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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jLabel2.setText("-hide");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jLabel2, gridBagConstraints);
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
        switch(evt.getExtendedKeyCode()) {
            case KeyEvent.VK_ENTER:
                setResultAndClose(_selection);
                return;
            case KeyEvent.VK_ESCAPE:
                setResultAndClose(null);
                return;
        }
    }//GEN-LAST:event_jTree1KeyPressed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        switch(evt.getExtendedKeyCode()) {
            case KeyEvent.VK_ENTER:
                setResultAndClose(_selection);
                return;
            case KeyEvent.VK_ESCAPE:
                setResultAndClose(null);
                return;
        }
    }//GEN-LAST:event_formKeyPressed

    File _selection;
    File _result = null;

    public void setResultAndClose(File result) {
        if (result == null) {
            _result = null;
        }else {
            if (result.getPath().startsWith("::")) {
                JOptionPane.showMessageDialog(this, "Can't select System Folder", "notice", JOptionPane.OK_OPTION);
                return;
            }
            _result = result.getAbsoluteFile();
        }
        Container c = MXUtil.getOwnerWindow(this);
        c.setVisible(false);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelSelection;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
}
