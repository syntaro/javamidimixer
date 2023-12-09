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
package jp.synthtarou.midimixer.mx12masterkeys;

import javax.swing.JDialog;
import javax.swing.JFrame;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.midi.MXMessageWrapListFactory;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.swing.SafeSpinnerNumberModel;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX12MasterPanelEditor extends javax.swing.JPanel {
    public static JDialog showAsDialog(JFrame parent, MX12Process process, boolean modal) {
        MX12MasterPanelEditor editor = new MX12MasterPanelEditor(process);
        JDialog dialog = new JDialog(parent, modal);
        dialog.setTitle(editor.getTitle());
        dialog.setAlwaysOnTop(true);
        dialog.pack();
        dialog.setSize(550, 330);
        dialog.getContentPane().add(editor);
        MXUtil.centerWindow(dialog);
        dialog.setVisible(true);
        return dialog;
    }

    public static JDialog showAsDialog(JDialog parent, MX12Process process, boolean modal) {
        MX12MasterPanelEditor editor = new MX12MasterPanelEditor(process);
        JDialog dialog = new JDialog(parent, modal);
        dialog.setTitle(editor.getTitle());
        dialog.setAlwaysOnTop(true);
        dialog.pack();
        dialog.setSize(550, 330);
        dialog.getContentPane().add(editor);
        MXUtil.centerWindow(dialog);
        dialog.setVisible(true);
        return dialog;
    }

    MXWrapList<Integer> _portModel;
    MXWrapList<Integer> _channelModel;
    MXWrapList<MXReceiver> _receiverModel;

    /**
     * Creates new form MX12MasterPanelEditor
     */
    public MX12MasterPanelEditor(MX12Process process) {
        initComponents();
        _process = process;
        jLabel6.setText("");
        
        showParameters();
    }
   
    public void showParameters() {
        _receiverModel = MXMain.getMain().getReceiverList();
        _receiverModel.writeComboBox(jComboBoxReciever, _process.getNextReceiver());

        _portModel = MXMessageWrapListFactory.listupPort(null);
        _portModel.writeComboBox(jComboBoxPort, _process.getMousePort());
        
        _channelModel = MXMessageWrapListFactory.listupChannel(null);
        _channelModel.writeComboBox(jComboBoxChannel, _process.getMouseChannel());
        
        jSpinnerMouseVelocity.setModel(new SafeSpinnerNumberModel(_process.getMouseVelocity(), 1, 127, 1));
        
        jCheckBoxAdjustPort.setSelected(_process.isOverwriteInputChannel());

        jCheckBoxInputPagePort.setSelected(_process.isAcceptInputPanelSignal());
        jCheckBoxThisPagePort.setSelected(_process.isAcceptThisPageSignal());
    }
    
    public void catchParameters() {
        _process.setNextReceiver(_receiverModel.get(jComboBoxReciever.getSelectedIndex())._value);
        _process.setMousePort((int) _portModel.get(jComboBoxPort.getSelectedIndex())._value);
        _process.setMouseChannel((int) _channelModel.get(jComboBoxChannel.getSelectedIndex())._value);
        _process.setMouseVelocity((int)jSpinnerMouseVelocity.getValue());
        _process.setOverwriteInputChannel(this.jCheckBoxAdjustPort.isSelected());
        _process.setAcceptInputPanelSignal(jCheckBoxInputPagePort.isSelected());
        _process.setAcceptThisPageSignal(jCheckBoxThisPagePort.isSelected());
        _process._view._piano.allNoteOff();
    }

    MX12Process _process;
    
    public String getTitle() {
        return MXAppConfig.MX_APPNAME;
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

        jLabel1 = new javax.swing.JLabel();
        jComboBoxReciever = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxPort = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jComboBoxChannel = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jSpinnerMouseVelocity = new javax.swing.JSpinner();
        jCheckBoxAdjustPort = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jCheckBoxInputPagePort = new javax.swing.JCheckBox();
        jCheckBoxThisPagePort = new javax.swing.JCheckBox();
        jButtonReset = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Master Key Controller"));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Receiver");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

        jComboBoxReciever.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxRecieverActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(jComboBoxReciever, gridBagConstraints);

        jLabel2.setText("Port");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(jComboBoxPort, gridBagConstraints);

        jLabel3.setText("Channel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(jComboBoxChannel, gridBagConstraints);

        jLabel4.setText("Velocity(Mouse)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(jSpinnerMouseVelocity, gridBagConstraints);

        jCheckBoxAdjustPort.setText("With Re-Adjust Ch/Port ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        add(jCheckBoxAdjustPort, gridBagConstraints);

        jButton1.setText("OK");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(jButton1, gridBagConstraints);

        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(jButton2, gridBagConstraints);

        jLabel6.setText("jLabel6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        add(jLabel6, gridBagConstraints);

        jLabel7.setText("Process");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel7, gridBagConstraints);

        jCheckBoxInputPagePort.setText("Input Panel Page's Signal <- Top Left");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jCheckBoxInputPagePort, gridBagConstraints);

        jCheckBoxThisPagePort.setText("This Page's Signal");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jCheckBoxThisPagePort, gridBagConstraints);

        jButtonReset.setText("Reset");
        jButtonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(jButtonReset, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        catchParameters();
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButtonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetActionPerformed
        _receiverModel.writeComboBox(jComboBoxReciever, MXMain.getMain().getInputProcess());
        _portModel.writeComboBox(jComboBoxPort, 0);
        _channelModel.writeComboBox(jComboBoxChannel, 0);
        jCheckBoxAdjustPort.setSelected(false);
        jCheckBoxInputPagePort.setSelected(true);
        jCheckBoxThisPagePort.setSelected(true);
    }//GEN-LAST:event_jButtonResetActionPerformed

    private void jComboBoxRecieverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxRecieverActionPerformed
        
    }//GEN-LAST:event_jComboBoxRecieverActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButtonReset;
    private javax.swing.JCheckBox jCheckBoxAdjustPort;
    private javax.swing.JCheckBox jCheckBoxInputPagePort;
    private javax.swing.JCheckBox jCheckBoxThisPagePort;
    private javax.swing.JComboBox<String> jComboBoxChannel;
    private javax.swing.JComboBox<String> jComboBoxPort;
    private javax.swing.JComboBox<String> jComboBoxReciever;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JSpinner jSpinnerMouseVelocity;
    // End of variables declaration//GEN-END:variables
}
