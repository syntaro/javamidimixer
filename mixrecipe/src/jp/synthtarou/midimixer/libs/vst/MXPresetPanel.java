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
package jp.synthtarou.midimixer.libs.vst;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.swing.MXModalFrame;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXPresetPanel extends javax.swing.JPanel {
    /**
     * Creates new form MXPresetPanel
     */
    public MXPresetPanel(IndexedFile file, MXPresetAction action) {
        initComponents();
        setPreferredSize(new Dimension(500, 300));
        _file = file;
        _action = action;
        _listModel = createListModel();
        jListEntry.setModel(_listModel);
    }

    public static void main(String[] args) {
        IndexedFile root = new IndexedFile(new File("C:/test"));
        MXPresetAction action = new MXPresetAction() {
            @Override
            public boolean presetActionSave(File file) {
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    out.write("abc".getBytes());
                    out.close();
                    out = null;
                }catch(IOException e) {
                    if (out != null) {
                        try {
                            out.close();
                            out = null;
                            file.delete();
                        }catch(IOException ex) {
                            
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean presetActionLoad(File file) {
                System.out.println("Loading " + file);
                return true;
            }
        };
        root.getLock();
        MXPresetPanel panel = new MXPresetPanel(root, action);
        MXModalFrame.showAsDialog(null, panel, "total recall");
        root.releaseLock();
        System.exit(0);
    }
    
    IndexedFile _file;
    MXWrapList<Integer> _listModel;
    MXPresetAction _action;

    public MXWrapList<Integer> createListModel() {
        _file.readIndexFile();
        MXWrapList<Integer> model = new MXWrapList<>();
        for (int i = 0; i < 128; ++ i) {
            String title = _file.getTitle(i);
            if (title == null) {
                title = " - ";
            }
            String text = createLine(i, title);
            model.addNameAndValue(text, i);
        }
        return model;
    }
    
    public String createLine(int num, String title) {
        return Integer.toString(num) + ": " + title;
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
        jListEntry = new javax.swing.JList<>();
        jButtonLoad = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonClose = new javax.swing.JButton();
        jLabelTitlte = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Save / Load Preset"));
        setLayout(new java.awt.GridBagLayout());

        jListEntry.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jListEntry);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jButtonLoad.setText("Load");
        jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        add(jButtonLoad, gridBagConstraints);

        jButtonSave.setText("Save");
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        add(jButtonSave, gridBagConstraints);

        jButtonClose.setText("Close");
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        add(jButtonClose, gridBagConstraints);

        jLabelTitlte.setText("Title");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabelTitlte, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadActionPerformed
        if (_action == null) {
            return;
        }
        int index = jListEntry.getSelectedIndex();
        if (index >= 0) {
            Integer x = _listModel.valueOfIndex(index);
            if (x != index) {
                System.err.println("Something Wrong , List Index != Data Index");
                return;
            }
            if (_action.presetActionLoad(_file.getPath(x))) {
                MXUtil.getOwnerWindow(this).setVisible(false);
            }
        }
    }//GEN-LAST:event_jButtonLoadActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        int index = jListEntry.getSelectedIndex();
        if (index >= 0) {
            Integer x = _listModel.valueOfIndex(index);
            if (x != index) {
                System.err.println("Something Wrong , List Index != Data Index");
                return;
            }
            String title = _file.getTitle(x);
            PresetNamePanel panel = new PresetNamePanel();
            String ret = panel.showAsDialog(this, title);
            if (ret != null) {
                title = ret;
                _file.setTitle(x, title);
                if (_action.presetActionSave(_file.getPath(x))) {
                    MXUtil.getOwnerWindow(this).setVisible(false);
                }
                
                jListEntry.setModel(createListModel());
            }
        }
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButtonCloseActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JLabel jLabelTitlte;
    private javax.swing.JList<String> jListEntry;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
