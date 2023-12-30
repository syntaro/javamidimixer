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
package jp.synthtarou.midimixer.ccxml.xml;

import jp.synthtarou.midimixer.ccxml.ui.EditorForXMLTag;
import java.awt.Dimension;
import javax.swing.JOptionPane;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIInManager;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOutManager;
import jp.synthtarou.midimixer.libs.settings.MXSetting;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CXXMLManagerPanel extends javax.swing.JPanel {

    public static void main(String[] args) {
        MXMIDIInManager.getManager().initWithSetting();
        MXMIDIOutManager.getManager().initWithSetting();

        CXXMLManagerPanel panel = new CXXMLManagerPanel();
        MXUtil.showAsDialog(null, panel, "Test");

        MXSetting.saveEverySettingToFile();

        System.exit(0);
    }

    public MXWrapList<CXFile> _listModel;

    /**
     * Creates new form CXFileListPanel
     */
    public CXXMLManagerPanel() {
        initComponents();

        reloadListModel();
        setPreferredSize(new Dimension(600, 500));
    }

    public void reloadListModel() {
        CXXMLManager manager = CXXMLManager.getInstance();

        _listModel = new MXWrapList<>();

        for (CXFile file : manager._listLoaded) {
            _listModel.addNameAndValue(file._file.getName(), file);
        }
        jListXMLFiles.setModel(_listModel);
        updateEnables();
    }

    public void updateEnables() {
        int index = jListXMLFiles.getSelectedIndex();
        CXFile sel = null;
        if (index >= 0) {
            jButtonMinus.setEnabled(true);
            sel = _listModel.valueOfIndex(index);
            jButtonEditXML.setEnabled(sel != null && !sel.listModules().isEmpty());
        } else {
            jButtonMinus.setEnabled(false);
            jButtonEditXML.setEnabled(false);
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

        jButtonPlus = new javax.swing.JButton();
        jButtonMinus = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListXMLFiles = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaWarning = new javax.swing.JTextArea();
        jButtonEditXML = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Managed XML Files"));
        setLayout(new java.awt.GridBagLayout());

        jButtonPlus.setText("+ Plus");
        jButtonPlus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPlusActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(jButtonPlus, gridBagConstraints);

        jButtonMinus.setText("- Minus");
        jButtonMinus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMinusActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(jButtonMinus, gridBagConstraints);

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jListXMLFiles.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListXMLFilesValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListXMLFiles);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jTextAreaWarning.setColumns(20);
        jTextAreaWarning.setRows(5);
        jScrollPane2.setViewportView(jTextAreaWarning);

        jSplitPane1.setRightComponent(jScrollPane2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSplitPane1, gridBagConstraints);

        jButtonEditXML.setText("Edit XML");
        jButtonEditXML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditXMLActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        add(jButtonEditXML, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jListXMLFilesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListXMLFilesValueChanged
        int sel = jListXMLFiles.getSelectedIndex();
        if (sel >= 0) {
            CXFile file = _listModel.valueOfIndex(sel);
            String text = file.getAdviceForXML();
            jTextAreaWarning.setText(text);
            jTextAreaWarning.setCaretPosition(0);
        }
        updateEnables();
    }//GEN-LAST:event_jListXMLFilesValueChanged

    private void jButtonPlusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPlusActionPerformed
        CXXMLManager manager = CXXMLManager.getInstance();
        manager.browseAndImport(this);
        reloadListModel();
    }//GEN-LAST:event_jButtonPlusActionPerformed

    private void jButtonMinusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMinusActionPerformed
        int sel = jListXMLFiles.getSelectedIndex();
        if (sel >= 0) {
            CXFile file = _listModel.valueOfIndex(sel);
            CXXMLManager manager = CXXMLManager.getInstance();
            manager._listLoaded.remove(file);
            reloadListModel();
        }
    }//GEN-LAST:event_jButtonMinusActionPerformed

    private void jButtonEditXMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditXMLActionPerformed
        int sel = jListXMLFiles.getSelectedIndex();
        if (sel >= 0) {
            CXFile file = _listModel.valueOfIndex(sel);
            EditorForXMLTag panel = new EditorForXMLTag(file);
            MXUtil.showAsDialog(this, panel, file.toString());
            
            try {
                if (file.writeToTeporary() == false) {
                    JOptionPane.showMessageDialog(this, "File Not Changed");
                }
                else {
                    file.moveTemporaryToThis();
                    JOptionPane.showMessageDialog(this, "Wrote: " + file._file);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Sorry, Error happened while saving XML: " + ex.toString(), "Close without save?", JOptionPane.YES_NO_OPTION);
            } finally {
                try {
                    file.rebuildCache();
                }catch(Exception ex) {
                }
            }
        }
    }//GEN-LAST:event_jButtonEditXMLActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonEditXML;
    private javax.swing.JButton jButtonMinus;
    private javax.swing.JButton jButtonPlus;
    private javax.swing.JList<String> jListXMLFiles;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextAreaWarning;
    // End of variables declaration//GEN-END:variables

}
