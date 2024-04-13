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
package jp.synthtarou.midimixer.ccxml.ui;

import jp.synthtarou.midimixer.ccxml.xml.CXXMLManager;
import jp.synthtarou.midimixer.ccxml.xml.CXNode;
import jp.synthtarou.midimixer.ccxml.xml.CXFile;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import jp.synthtarou.midimixer.ccxml.InformationForCCM;
import jp.synthtarou.midimixer.ccxml.InformationForModule;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.navigator.legacy.INavigator;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class NavigatorForCCXMLCC extends javax.swing.JPanel implements INavigator<List<InformationForCCM>> {
    ArrayList<CXFile> _listXMLFile = CXXMLManager.getInstance().listLoaded();
    boolean _wideScan;
    CXFile _selectedFile;

    TreeCellRenderer _rendererBase;

    public boolean simpleAsk(Container parent) {
        MXUtil.showAsDialog(parent, this, INavigator.DEFAULT_TITLE);
        if (getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
            return true;
        }
        return false;
    }

    public NavigatorForCCXMLCC() {
        this(null);
        setAllowMultiSelect(false);
    }

    public NavigatorForCCXMLCC(CXFile file) {
        initComponents();

        _selectedFile = file;
        _wideScan = (file == null) ? true : false;

        _rendererBase = jTree1.getCellRenderer();
        jTree1.setCellRenderer(new DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                String text = "";
                if (value instanceof DefaultMutableTreeNode) {
                    Object user = ((DefaultMutableTreeNode) value).getUserObject();
                    CXNode node = null;

                    if (user instanceof CXNode) {
                        node = (CXNode) user;
                        if (node._nodeName.equalsIgnoreCase("CCMLink")) {
                            //TODO
                        }
                        if (node._nodeName.equalsIgnoreCase("CCM")) {
                            String name = node._listAttributes.valueOfName("Name");
                            CXNode theMemo = node.firstChild("Memo");
                            CXNode theData = node.firstChild("Data");

                            String txtMemo = null, txtData = null;
                            if (theMemo != null) {
                                txtMemo = theMemo.getTextContent();
                            }
                            if (theData != null) {
                                txtData = theData.getTextContent();
                            }

                            if (txtMemo == null || txtMemo.length() == 0 || txtMemo.equals(txtData)) {
                                text = txtData;
                            } else {
                                text = txtMemo + "=" + txtData;
                            }
                            if (name != null && name.isBlank() == false) {
                                text = name + " (" + text + ")";
                            }
                        }
                        if (node._nodeName.equalsIgnoreCase("Folder")) {
                            String name = node._listAttributes.valueOfName("Name");
                            text = name;
                        }
                        value = text;
                    }
                }
                text = String.valueOf(value);
                return _rendererBase.getTreeCellRendererComponent(tree, text, selected, expanded, leaf, row, hasFocus);
            }

        });

        jTree1.setModel(createTreeModel());
        jTree1.setRootVisible(false);
        setPreferredSize(new Dimension(600, 500));
        setAllowMultiSelect(false);
    }
    
    public void setAllowMultiSelect(boolean multi) {
        if (multi) {
            jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        }
        else {
            jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        }
              
    }

    public TreeModel createTreeModel() {
        return new DefaultTreeModel(createTreeRoot());
    }

    public InformationForModule findParentModule(CXNode node) {
        while (node != null) {
            if (node._nodeName.equals("ModuleData")) {
                break;
            }
            node = node.getParent();
        }
        if (_wideScan) {
            for (CXFile xml : _listXMLFile) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(xml, true);
                for (InformationForModule module : xml.listModules()) {
                    if (module._node == node) {
                        return module;
                    }
                }
            }
        } else {
            CXFile xml = _selectedFile;
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(xml, true);
            for (InformationForModule module : xml.listModules()) {
                if (module._node == node) {
                    return module;
                }
            }
        }
        return null;
    }

    public DefaultMutableTreeNode createTreeRoot() {
        DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("treeRoot", true);
        if (_wideScan) {
            for (CXFile xml : _listXMLFile) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(xml, true);
                for (InformationForModule module : xml.listModules()) {
                    for (CXNode cclist : module._node.listChildren("ControlChangeMacroList")) {
                        buildTree(child, cclist);
                    }
                }
                treeRoot.add(child);
            }
        } else {
            CXFile xml = _selectedFile;
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(xml, true);
            for (InformationForModule module : xml.listModules()) {
                for (CXNode cclist : module._node.listChildren("ControlChangeMacroList")) {
                    buildTree(child, cclist);
                }
                treeRoot.add(child);
            }
        }
        return treeRoot;
    }

    public void buildTree(DefaultMutableTreeNode node, CXNode ccmParent) {
        List<CXNode> listFolder = ccmParent.listChildren("Folder");
        if (listFolder != null) {
            for (CXNode folder : listFolder) {
                DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folder, true);
                node.add(folderNode);
                buildTree(folderNode, folder);
            }
        }
        List<CXNode> listCCM = ccmParent.listChildren("CCM");
        if (listCCM != null) {
            for (CXNode message : listCCM) {
                DefaultMutableTreeNode messageNode = new DefaultMutableTreeNode(message, false);
                node.add(messageNode);
            }
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
        jButtonOK = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabelName = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

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
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jTextFieldData, gridBagConstraints);

        jButtonOK.setText("Close");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButtonOK, gridBagConstraints);

        jLabel1.setText("Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel1, gridBagConstraints);

        jLabel2.setText("Data:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel2, gridBagConstraints);

        jLabelName.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabelName, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    int _returnStatus = INavigator.RETURN_STATUS_NOTSET;
    List<InformationForCCM> _returnValue;

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        TreePath[] pathList = jTree1.getSelectionPaths();
        if (pathList == null || pathList.length == 0) {
            return;
        }
        ArrayList<InformationForCCM> result = new ArrayList<>();

        _returnValue = null;
        for (TreePath path : pathList) {
            DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode) path.getPathComponent(1);

            if (lastNode == null) {
                continue;
            }
            if (lastNode.isLeaf() == false) {
                continue;
            }
            Object last = lastNode.getUserObject();
            Object first = firstNode.getUserObject();

            if (last instanceof CXNode) {
                CXNode cc = (CXNode) last;
                String name = cc._listAttributes.valueOfName("Name");

                if (cc.getName().equalsIgnoreCase("Folder")) {
                    jLabelName.setText(name);
                    jTextFieldData.setText("");
                    _returnValue = null;
                } else if (cc.getName().equalsIgnoreCase("CCM")) {
                    if (first instanceof CXFile) {
                        _selectedFile = (CXFile) first;
                    }

                    jLabelName.setText(name);

                    String textData = cc.firstChildsTextContext("Data");
                    String textGate = cc.firstChildsTextContext("Gate");

                    if (textGate != null && textGate.isBlank() == false) {
                        jTextFieldData.setText(textData + ", Gate:" + textGate);
                    } else {
                        jTextFieldData.setText(textData);
                    }

                    result.add(new InformationForCCM(findParentModule(cc), cc));
                }
            }
        }
        _returnValue = result;
        _returnStatus = INavigator.RETURN_STATUS_APPROVED;
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButtonOKActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldData;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

    @Override
    public int getNavigatorType() {
        return INavigator.TYPE_SELECTOR;
    }

    @Override
    public int getReturnStatus() {
        return _returnStatus;
    }

    @Override
    public List<InformationForCCM> getReturnValue() {
        return _returnValue;
    }

    @Override
    public boolean isNavigatorRemovable() {
        return false;
    }

    @Override
    public JPanel getNavigatorPanel() {
        return this;
    }

    @Override
    public boolean validateWithNavigator(List<InformationForCCM> result) {
        return true;
    }
}
