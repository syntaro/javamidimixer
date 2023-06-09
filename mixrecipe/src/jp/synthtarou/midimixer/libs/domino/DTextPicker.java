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
package jp.synthtarou.midimixer.libs.domino;

import jp.synthtarou.midimixer.libs.midi.programlist.database.PDFileManager;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.programlist.database.PDFile;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class DTextPicker extends javax.swing.JPanel {
    ArrayList<PDFile> _listRoot = new ArrayList();
    DTextMessage _textReturn;
   
    public DTextMessage getTextReturn() {
        return _textReturn;
    }
    
    public DTextPicker() {
        initComponents();
        PDFileManager manager = PDFileManager.getManager();
        for (int i = 0; i < manager.size(); ++ i) {
            add(manager.get(i));
        }
        setPreferredSize(new Dimension(600, 600));
    }
    
    public void add(PDFile root) {
        _listRoot.add(root);
        jTree1.setModel(createTreeModel());
    }
    
    public TreeModel createTreeModel() {
        return new DefaultTreeModel(createTreeNode());
    }
    
    public DefaultMutableTreeNode createTreeNode() {
        DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("treeRoot", true);
        for (PDFile xml : _listRoot) {
            if (xml.getCCList() != null) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(xml.getName(), true);
                buildTree(child, xml.getCCList());
                treeRoot.add(child);
            }
        }
        return treeRoot;
    }
    
    public void buildTree(DefaultMutableTreeNode node, DTextMessageList ccList) {
        for (DTextFolder folder : ccList._listFolder) {
            DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folder, true);
            node.add(folderNode);
            buildTree(folderNode, folder._list);
        }
        for (DTextMessage message : ccList._listMessage) {
            DefaultMutableTreeNode messageNode = new DefaultMutableTreeNode(message, false);
            node.add(messageNode);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jTextFieldData = new javax.swing.JTextField();
        jButtonAddXML = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabelName = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });
        jTree1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTree1PropertyChange(evt);
            }
        });
        jScrollPane1.setViewportView(jTree1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jTextFieldData.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jTextFieldData, gridBagConstraints);

        jButtonAddXML.setText("Add DominoXML");
        jButtonAddXML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddXMLActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        add(jButtonAddXML, gridBagConstraints);

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButtonOK, gridBagConstraints);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButtonCancel, gridBagConstraints);

        jLabel1.setText("Name");
        add(jLabel1, new java.awt.GridBagConstraints());

        jLabel2.setText("Data");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(jLabel2, gridBagConstraints);

        jLabelName.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabelName, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jTree1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTree1PropertyChange
    }//GEN-LAST:event_jTree1PropertyChange

    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }
        if (node.isLeaf()) {
            Object sel = node.getUserObject();
            if (sel instanceof DTextFolder) {
                DTextFolder folder = (DTextFolder)sel;
                jLabelName.setText(folder._name);
                jTextFieldData.setText("");
                _textReturn = null;
            }else if (sel instanceof DTextMessage) {
                DTextMessage message = (DTextMessage)sel;
                jLabelName.setText(message._name);
                jTextFieldData.setText(message._dataText + ", Gate:" + message.getGate());
                _textReturn = message;
            }
        }
    }//GEN-LAST:event_jTree1ValueChanged

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        MXUtil.closeOwnerWindow(this);
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        _textReturn = null;
        MXUtil.closeOwnerWindow(this);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonAddXMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddXMLActionPerformed
        JOptionPane.showMessageDialog(this, "Try go [OutputPort] Setting");
    }//GEN-LAST:event_jButtonAddXMLActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddXML;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldData;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
}
