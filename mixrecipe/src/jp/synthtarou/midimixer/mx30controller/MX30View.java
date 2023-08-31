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
package jp.synthtarou.midimixer.mx30controller;

import java.awt.Component;
import javax.swing.JScrollPane;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX30View extends javax.swing.JPanel {
    private static final MXDebugPrint _debug = new MXDebugPrint(MX30View.class);

    MX30Process _process;

    /**
     * Creates new form MX30View
     */
    public MX30View(MX30Process process) {
        _process = process;
        initComponents();
        jCheckBoxUseThisMixRecipe.setSelected(true);
    }
    
    public void addPage(int port, MX32MixerProcess process) {
        MX32MixerView view = (MX32MixerView)process.getReceiverView();
        jTabbedPane1.add(process.getReceiverName(), new JScrollPane(view));
        view.initControllers();
    }
    
    public MX32MixerView getPage(int port) {
        return  (MX32MixerView)_process.getPage(port).getReceiverView();
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jCheckBoxUseThisMixRecipe = new javax.swing.JCheckBox();

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        setLayout(new java.awt.GridBagLayout());

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Controllers"));
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });
        jTabbedPane1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTabbedPane1FocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jTabbedPane1, gridBagConstraints);

        jCheckBoxUseThisMixRecipe.setText("Use This Recipe");
        jCheckBoxUseThisMixRecipe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseThisMixRecipeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        add(jCheckBoxUseThisMixRecipe, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxUseThisMixRecipeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseThisMixRecipeActionPerformed
        if (jCheckBoxUseThisMixRecipe.isSelected()) {
            MXUtil.swingTreeEnable(this, true);
        }else {
            MXUtil.swingTreeEnable(this, false);
        }
        this.jCheckBoxUseThisMixRecipe.setEnabled(true);

    }//GEN-LAST:event_jCheckBoxUseThisMixRecipeActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        int x = jTabbedPane1.getSelectedIndex();
        if (x >= 0) {
            getPage(x).setFocusString("");
        }            
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        int x = jTabbedPane1.getSelectedIndex();
        if (x >= 0) {
            getPage(x).requestFocusInWindow();
        }
    }//GEN-LAST:event_formFocusGained

    private void jTabbedPane1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTabbedPane1FocusGained
        int x = jTabbedPane1.getSelectedIndex();
        if (x >= 0) {
            Component c = jTabbedPane1.getComponentAt(x);
            c.requestFocusInWindow();
        }
    }//GEN-LAST:event_jTabbedPane1FocusGained

    public void lockAnothereTabs(boolean lock) {
        int x = jTabbedPane1.getSelectedIndex();
        if (lock) {
            for (int i = 0; i < jTabbedPane1.getTabCount(); ++ i) {
                jTabbedPane1.setEnabledAt(i, i == x ? true : false);
            }
            //MXUtil.swingTreeEnable(jTabbedPane1.getSelectedComponent(), false);
            MXUtil.swingTreeEditable(getPage(jTabbedPane1.getSelectedIndex()), false);
        }else {
            for (int i = 0; i < jTabbedPane1.getTabCount(); ++ i) {
                jTabbedPane1.setEnabledAt(i, true);
            }
            //MXUtil.swingTreeEnable(jTabbedPane1.getSelectedComponent(), true);
            MXUtil.swingTreeEditable(getPage(jTabbedPane1.getSelectedIndex()), true);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxUseThisMixRecipe;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
}
