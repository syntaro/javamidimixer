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
package jp.synthtarou.midimixer.mx30surface;

import javax.swing.JOptionPane;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class InitializeConfirmPanel extends javax.swing.JPanel {

    MX32Mixer _mixer;

    /**
     * Creates new form InitializeConfirmPanel
     */
    public InitializeConfirmPanel(MX32Mixer process) {
        _mixer = process;
        initComponents();
        buttonGroup1.add(jRadioButtonMixer);
        buttonGroup1.add(jRadioButtonDAW);
        buttonGroup1.add(jRadioButtonGMTone);
        jRadioButtonGMTone.setVisible(false);
        buttonGroup1.add(jRadioButtonSound);
        buttonGroup1.add(jRadioButtonInit);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jRadioButtonMixer = new javax.swing.JRadioButton();
        jRadioButtonDAW = new javax.swing.JRadioButton();
        jRadioButtonSound = new javax.swing.JRadioButton();
        jButton1 = new javax.swing.JButton();
        jRadioButtonInit = new javax.swing.JRadioButton();
        jRadioButtonGMTone = new javax.swing.JRadioButton();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Which way ?");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

        jRadioButtonMixer.setSelected(true);
        jRadioButtonMixer.setText("Mixer");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        add(jRadioButtonMixer, gridBagConstraints);

        jRadioButtonDAW.setText("DAW");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        add(jRadioButtonDAW, gridBagConstraints);

        jRadioButtonSound.setText("Sound");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        add(jRadioButtonSound, gridBagConstraints);

        jButton1.setText("Initialize");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButton1, gridBagConstraints);

        jRadioButtonInit.setText("Init");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        add(jRadioButtonInit, gridBagConstraints);

        jRadioButtonGMTone.setText("GMTone");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        add(jRadioButtonGMTone, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        boolean ret = false;
        if (jRadioButtonMixer.isSelected()) {
            ret = new MX32MixerInitializer(_mixer).initializeData(MX32MixerInitializer.INIT_TYPE_MIXER);
        }
        if (jRadioButtonDAW.isSelected()) {
            ret = new MX32MixerInitializer(_mixer).initializeData(MX32MixerInitializer.INIT_TYPE_DAW);
        }
        if (jRadioButtonGMTone.isSelected()) {
            ret = new MX32MixerInitializer(_mixer).initializeData(MX32MixerInitializer.INIT_TYPE_GMTOME);
        }
        if (jRadioButtonSound.isSelected()) {
            ret = new MX32MixerInitializer(_mixer).initializeData(MX32MixerInitializer.INIT_TYPE_SOUDMODULE);
        }
        if (jRadioButtonInit.isSelected()) {
            ret = new MX32MixerInitializer(_mixer).initializeData(MX32MixerInitializer.INIT_TYPE_ZERO);
        }
        if (ret) {
            _mixer._view.initControllers();

            MXUtil.getOwnerWindow(this).setVisible(false);
        } else {
            JOptionPane.showMessageDialog(_mixer._view, "Not ready", "Sorry", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton jRadioButtonDAW;
    private javax.swing.JRadioButton jRadioButtonGMTone;
    private javax.swing.JRadioButton jRadioButtonInit;
    private javax.swing.JRadioButton jRadioButtonMixer;
    private javax.swing.JRadioButton jRadioButtonSound;
    // End of variables declaration//GEN-END:variables
}