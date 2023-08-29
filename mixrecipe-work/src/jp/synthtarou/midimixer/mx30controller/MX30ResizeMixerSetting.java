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

import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX30ResizeMixerSetting extends javax.swing.JPanel {

    boolean[] activeKnob = new boolean[MXAppConfig.CIRCLE_ROW_COUNT];
    boolean[] activePad = new boolean[MXAppConfig.DRUM_ROW_COUNT];
    int activeLines = MXAppConfig.SLIDER_COLUMN_COUNT;
    MX30Process _process;

    /**
     * Creates new form MX30ResizeMixerSetting
     */
    public MX30ResizeMixerSetting(MX30Process process) {
        initComponents();
        _process = process;
        
        buttonGroupLines.add(jRadioButtonLine4);
        buttonGroupLines.add(jRadioButtonLine8);
        buttonGroupLines.add(jRadioButtonLine9);
        buttonGroupLines.add(jRadioButtonLine16);
        buttonGroupLines.add(jRadioButtonLine17);

        activeLines = process.getActiveLines();
        switch(activeLines) {
            case 4:
                jRadioButtonLine4.setSelected(true);
                break;
            case 8:
                jRadioButtonLine8.setSelected(true);
                break;
            case 9:
                jRadioButtonLine9.setSelected(true);
                break;
            case 16:
                jRadioButtonLine16.setSelected(true);
                break;
            default:
                jRadioButtonLine17.setSelected(true);
                break;
                
        }
        for (int r = 0; r < MXAppConfig.CIRCLE_ROW_COUNT; ++ r) {
            activeKnob[r] = process.isKnobActive(r);
            switch(r) {
                case 0:
                    jCheckBoxKnob1.setSelected(activeKnob[r]);
                    break;
                case 1:
                    jCheckBoxKnob2.setSelected(activeKnob[r]);
                    break;
                case 2:
                    jCheckBoxKnob3.setSelected(activeKnob[r]);
                    break;
                case 3:
                    jCheckBoxKnob4.setSelected(activeKnob[r]);
                    break;
            }
        }
        for (int r = 0; r < MXAppConfig.DRUM_ROW_COUNT; ++ r) {
            activePad[r] = process.isPadActive(r);
            switch(r) {
                case 0:
                    jCheckBoxPad1.setSelected(activePad[r]);
                    break;
                case 1:
                    jCheckBoxPad2.setSelected(activePad[r]);
                    break;
                case 2:
                    jCheckBoxPad3.setSelected(activePad[r]);
                    break;
            }
        }
    }

    boolean _okOption = false;
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroupLines = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jCheckBoxKnob1 = new javax.swing.JCheckBox();
        jCheckBoxKnob2 = new javax.swing.JCheckBox();
        jCheckBoxKnob3 = new javax.swing.JCheckBox();
        jCheckBoxKnob4 = new javax.swing.JCheckBox();
        jRadioButtonLine4 = new javax.swing.JRadioButton();
        jRadioButtonLine8 = new javax.swing.JRadioButton();
        jRadioButtonLine9 = new javax.swing.JRadioButton();
        jRadioButtonLine16 = new javax.swing.JRadioButton();
        jCheckBoxPad1 = new javax.swing.JCheckBox();
        jCheckBoxPad2 = new javax.swing.JCheckBox();
        jCheckBoxPad3 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jRadioButtonLine17 = new javax.swing.JRadioButton();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Knob");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel1, gridBagConstraints);

        jLabel2.setText("Lines");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel2, gridBagConstraints);

        jLabel3.setText("Pad");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel3, gridBagConstraints);

        jCheckBoxKnob1.setText("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jCheckBoxKnob1, gridBagConstraints);

        jCheckBoxKnob2.setText("2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jCheckBoxKnob2, gridBagConstraints);

        jCheckBoxKnob3.setText("3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jCheckBoxKnob3, gridBagConstraints);

        jCheckBoxKnob4.setText("4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jCheckBoxKnob4, gridBagConstraints);

        jRadioButtonLine4.setText("4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jRadioButtonLine4, gridBagConstraints);

        jRadioButtonLine8.setText("8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jRadioButtonLine8, gridBagConstraints);

        jRadioButtonLine9.setText("9");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jRadioButtonLine9, gridBagConstraints);

        jRadioButtonLine16.setText("16");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jRadioButtonLine16, gridBagConstraints);

        jCheckBoxPad1.setText("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jCheckBoxPad1, gridBagConstraints);

        jCheckBoxPad2.setText("2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jCheckBoxPad2, gridBagConstraints);

        jCheckBoxPad3.setText("3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        add(jCheckBoxPad3, gridBagConstraints);

        jButton1.setText("OK");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButton1, gridBagConstraints);

        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButton2, gridBagConstraints);

        jRadioButtonLine17.setText("17");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jRadioButtonLine17, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        _okOption = true;
        if (jRadioButtonLine4.isSelected()) {
            activeLines = 4;
        }
        if (jRadioButtonLine8.isSelected()) {
            activeLines = 8;
        }
        if (jRadioButtonLine9.isSelected()) {
            activeLines = 9;
        }
        if (jRadioButtonLine16.isSelected()) {
            activeLines = 16;
        }
        if (jRadioButtonLine17.isSelected()) {
            activeLines = 17;
        }

        _process.setActiveLines(activeLines);

        activeKnob[0] = jCheckBoxKnob1.isSelected();
        activeKnob[1] = jCheckBoxKnob2.isSelected();
        activeKnob[2] = jCheckBoxKnob3.isSelected();
        activeKnob[3] = jCheckBoxKnob4.isSelected();

        for (int r = 0; r < MXAppConfig.CIRCLE_ROW_COUNT; ++ r) {
            _process.setKnobActive(r, activeKnob[r]);
        }
        
        activePad[0] = jCheckBoxPad1.isSelected();
        activePad[1] = jCheckBoxPad2.isSelected();
        //activePad[2] = jCheckBoxPad3.isSelected();

        for (int r = 0; r < MXAppConfig.DRUM_ROW_COUNT; ++ r) {
            _process.setPadActive(r, activePad[r]);
        }
        
        MXUtil.closeOwnerWindow(this);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        _okOption = false;
        MXUtil.closeOwnerWindow(this);
    }//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupLines;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBoxKnob1;
    private javax.swing.JCheckBox jCheckBoxKnob2;
    private javax.swing.JCheckBox jCheckBoxKnob3;
    private javax.swing.JCheckBox jCheckBoxKnob4;
    private javax.swing.JCheckBox jCheckBoxPad1;
    private javax.swing.JCheckBox jCheckBoxPad2;
    private javax.swing.JCheckBox jCheckBoxPad3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JRadioButton jRadioButtonLine16;
    private javax.swing.JRadioButton jRadioButtonLine17;
    private javax.swing.JRadioButton jRadioButtonLine4;
    private javax.swing.JRadioButton jRadioButtonLine8;
    private javax.swing.JRadioButton jRadioButtonLine9;
    // End of variables declaration//GEN-END:variables
}
