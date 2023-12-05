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
package jp.synthtarou.midimixer;

import java.awt.Frame;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXVersionDialog extends javax.swing.JDialog {
    public static MXVersionDialog showAsModal(Frame parent) {
        MXVersionDialog dialog = new MXVersionDialog(parent, true);
        dialog.setTitle("Thank YOU for Trial");
        dialog.setAlwaysOnTop(true);
        dialog.pack();
        dialog.setSize(500, 400);
        MXUtil.centerWindow(dialog);
        dialog.setVisible(true);
        return dialog;
    }

    public MXVersionDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        jProgressBar1.setMinimum(0);
        StringBuffer text = new StringBuffer();
        text.append(MXAppConfig.MX_APPNAME + "\n");
        text.append("\n");
        text.append("java.vendor=" + System.getProperty("java.vendor") + "\n");
        text.append("java.version=" + System.getProperty("java.version") + "\n");
        text.append("\n");
        text.append("V0.50 Alpha renew UI 'Mixer' is now 'Surface17' and 'CCMapping'\n");
        text.append("\n");
        text.append("V0.47 sysex bugfix\n");
        text.append("V0.43 Pair CC (+32) support again\n");
        text.append("V0.42b Java Synth Will Ingore GMReset Signal\n");
        text.append("V0.42 SysEX Bugfix\n");
        text.append("V0.4 Phraase Recorder, UWPx3 Bug Fix\n");
        text.append("V0.3+a8 Tuned RangeConverter\n");
        text.append("V0.3 Apache2.0 + GNU GPL3 Dual License\n");
        text.append("\n");
        text.append("V0.16 beta fixed mixer component\n");
        text.append("V0.15 Visitant Architecture, Dataentry Input\n");
        text.append("V0.14 MouseWheel, ControllerSize\n");
        text.append("V0.13 Sequencer RealTime Tuning\n");
        text.append("V0.12 Template for CC Picker\n");
        text.append("V0.11 (Pre) Support DATAENTRY / 14bit\n");
        text.append("V0.10 Support 14 bit CC\n");
        text.append("V0.09 More Humanic Interface\n");
        text.append("V0.08 Drum Pad + Sequence + Chord\n");
        text.append("V0.07 Drum Pad + 0.1\n");
        text.append("V0.06 CC Mixer Chain\n");
        text.append("V0.05 supported Drum Pad\n");
        text.append("V0.04B supported SysEX Checksum\n");
        jTextArea1.setText(text.toString());
        jTextArea1.setEditable(false);
        jTextArea1.setCaretPosition(0);
        jProgressBar1.setIndeterminate(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jProgressBar1 = new javax.swing.JProgressBar();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("I wish you have fun.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        getContentPane().add(jLabel1, gridBagConstraints);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jProgressBar1, gridBagConstraints);

        jButton1.setText("OK");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        getContentPane().add(jButton1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

}
