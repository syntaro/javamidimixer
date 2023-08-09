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
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXOpening extends javax.swing.JDialog {
    private static final MXDebugPrint _debug = new MXDebugPrint(MXOpening.class);

    public static MXOpening showAsStartup(Frame parent) {
        MXOpening dialog = new MXOpening(parent, false);
        dialog.setTitle("Please wait.");
        dialog.setAlwaysOnTop(true);
        dialog.pack();
        dialog.setSize(400, 300);
        MXUtil.centerWindow(dialog);
        return dialog;
    }

    public MXOpening(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        jProgressBar1.setMinimum(0);
        StringBuffer text = new StringBuffer();
        text.append(MXStatic.MX_APPNAME_WITH_VERSION + "\n");
        text.append("Compiled with OpenJDK 20 + NetBeans IDE18\n");
        text.append("\n");
        text.append(("Copyright(C) SynthTAROU.\n"));
        text.append(("Thank you for Trial.\n"));        
        text.append("\n");
        text.append(("I wish  You have Fun.\n"));        
        jTextArea1.setText(text.toString());
        jTextArea1.setEditable(false);
        jProgressBar1.setIndeterminate(true);
    }

    public void showProgress(int current, int max) {
        if (jProgressBar1.getMaximum() != max) {
            jProgressBar1.setMaximum(max);
            jProgressBar1.setIndeterminate(false);
        }
        jProgressBar1.setValue(current);
        
        invalidate();
        repaint();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(300, 200));
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

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

}
