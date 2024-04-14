/*
 * Copyright (C) 2024 Syntarou YOSHIDA
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
package jp.synthtarou.midimixer.mx12masterpiano;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.DefaultListModel;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachCopyAndPaste;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX12HelpPanel extends javax.swing.JPanel {

    /**
     * Creates new form MX12HelpPanel
     */
    public MX12HelpPanel() {
        initComponents();
        showTexts();
        Font f = null;
        if (f == null) {
            f = jTextArea1.getFont();
            try {
                Font f2 = new Font("Meiryo", Font.PLAIN, f.getSize() * 4 / 3);
                if (f2 != null) {
                    f = f2;
                }
            }catch(Exception e) {
                f = null;
            }
        }
        if (f == null) {
            f = jTextArea1.getFont();
            try {
                Font f2 = new Font(f.getFontName(), Font.PLAIN, f.getSize() * 4 / 3);
                if (f2 != null) {
                    f = f2;
                }
            }catch(Exception e) {
                f = null;
            }
        }
        jTextArea1.setFont(f);
        jTextArea1.setEditable(false);
        new MXAttachCopyAndPaste(jTextArea1);
        setPreferredSize(new Dimension(450, 370));
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

        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(jButton1, gridBagConstraints);

        jTextArea1.setBackground(new java.awt.Color(255, 247, 234));
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    void showTexts() {
        StringBuffer str = new StringBuffer();
        str.append("You can use following Commands. with Gate and Value.").append("\n");
        str.append(" ").append("\n");
        str.append("Gate = Note, CCNum, DataEntry Room").append("\n");
        str.append(" -> higher 7bit #GH, lower7bit #GL").append("\n");
        str.append("Value = Velocity, CCValue(Dataentry)").append("\n");
        str.append(" -> higher 7bit = #VH, lower7bit = #VL").append("\n");
        str.append(" ").append("\n");
        str.append("PitchBend").append("\n");
        str.append(" @PB #VH #VL").append("\n");
        str.append("Chnanel Pressure").append("\n");
        str.append(" @CP #VL").append("\n");
        str.append("Polyphonic Key Pressure").append("\n");
        str.append(" @PKP #GL #VL").append("\n");
        str.append("Control Change").append("\n");
        str.append(" @CC #GL #VL").append("\n");
        str.append("System Exclusive").append("\n");
        str.append(" @SYSEX F0H ... F7H").append("\n");
        str.append("DataEntry (RPN)").append("\n");
        str.append(" @RPN [MSB] [LSB] #VH #VL").append("\n");
        str.append("DataEntry (NRPN)").append("\n");
        str.append(" @NRPN [MSB] [LSB] #VH #VL").append("\n");
        str.append(" ").append("\n");
        str.append("Note").append("\n");
        str.append(" @SYSEX F0H .[ . ]. F7H　-> insert Checksum between [] at ] position").append("\n");
        str.append(" ").append("\n");
        str.append("Extended Format (MIXRecipe added)").append("\n");
        str.append("Note On").append("\n");
        str.append(" @ON [NOTE] #VL").append("\n");
        str.append("Note Off").append("\n");
        str.append(" @OFF [NOTE] #VL").append("\n");
        str.append("14bit Control Change").append("\n");
        str.append(" @CC $GL #VH #VL").append("\n");
        str.append("7bit DataEntry RPN").append("\n");
        str.append(" @RPN [MSB] [LSB] #VL #NONE").append("\n");
        str.append("7bit DataEntry NRPN").append("\n");
        str.append(" @NRPN [MSB] [LSB] #VL #NONE").append("\n");
        jTextArea1.setText(str.toString());
        jTextArea1.setCaretPosition(0);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}