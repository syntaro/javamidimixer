/*
 * Copyright (C) 2023 Syntarou YOSHIDA.
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
package jp.synthtarou.midimixer.mx36ccmapping;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36StatusPanel extends javax.swing.JPanel {
    /**
     * Creates new form Example1
     */
    public MX36StatusPanel(MX36Status status) {
        initComponents();
        _status = status;
        refill();
    }
    
    MX36Status _status;
    
    public MX36Status getStatus() {
        return _status;
    }
    
    public void refill() {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    refill();
                }
            });
            return;
        }
        jLabelSurface.setText(_status.toSurfaceString());
        jLabelName.setText("Name: " + _status._outName);
        jLabelPortAndCh.setText("" + (_status._outChannel + 1) + "@" + _status._outPort);
        int gateDecimal = _status._outGateRange._var;
        String gateLabel = _status._outGateTable.nameOfValue(gateDecimal);
        String gateDecimalText = String.valueOf(gateDecimal);

        if (gateLabel.equals(gateDecimal)) {
            gateDecimalText = "";
        } else {
            gateDecimalText = " = (" + gateDecimal + " =" + MXUtil.toHexFF(gateDecimal) + "h)";
        }

        jLabelGate.setText("Gate:" + gateLabel + gateDecimalText);

        String valueLabel = _status._outValueTable.nameOfIndex(_status._outValueRange._var);
        int valueDecimal =_status._outValueRange._var;
        String valueDecimalText = String.valueOf(valueDecimal);

        if (valueLabel.equals(valueDecimalText)) {
            valueDecimalText = "";
        }

        jLabelValue.setText(valueLabel);
        jLabelValueDecimal.setText(valueDecimalText);
        jLabelValueHex.setText("=" + Integer.toHexString(valueDecimal) + "h");
        jLabelText.setText("Format:" + _status._outDataText);
        invalidate();
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

        jLabelText = new javax.swing.JLabel();
        jLabelGate = new javax.swing.JLabel();
        jLabelName = new javax.swing.JLabel();
        jLabelPortAndCh = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabelValue = new javax.swing.JLabel();
        jLabelValueDecimal = new javax.swing.JLabel();
        jLabelValueHex = new javax.swing.JLabel();
        jLabelSurface = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabelText.setFont(new java.awt.Font("メイリオ", 0, 12)); // NOI18N
        jLabelText.setText("text");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabelText, gridBagConstraints);

        jLabelGate.setFont(new java.awt.Font("メイリオ", 0, 12)); // NOI18N
        jLabelGate.setText("gate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabelGate, gridBagConstraints);

        jLabelName.setBackground(new java.awt.Color(255, 255, 255));
        jLabelName.setFont(new java.awt.Font("メイリオ", 0, 14)); // NOI18N
        jLabelName.setText("name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabelName, gridBagConstraints);

        jLabelPortAndCh.setFont(new java.awt.Font("メイリオ", 0, 12)); // NOI18N
        jLabelPortAndCh.setText("port+ch");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabelPortAndCh, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        jLabelValue.setFont(new java.awt.Font("Comic Sans MS", 0, 24)); // NOI18N
        jLabelValue.setForeground(new java.awt.Color(255, 68, 190));
        jLabelValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelValue.setText("127");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(jLabelValue, gridBagConstraints);

        jLabelValueDecimal.setFont(new java.awt.Font("Comic Sans MS", 0, 18)); // NOI18N
        jLabelValueDecimal.setForeground(new java.awt.Color(255, 61, 177));
        jLabelValueDecimal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelValueDecimal.setText("127");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(jLabelValueDecimal, gridBagConstraints);

        jLabelValueHex.setFont(new java.awt.Font("Comic Sans MS", 0, 18)); // NOI18N
        jLabelValueHex.setForeground(new java.awt.Color(255, 102, 0));
        jLabelValueHex.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelValueHex.setText("ff");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(jLabelValueHex, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(jPanel1, gridBagConstraints);

        jLabelSurface.setFont(new java.awt.Font("HGSｺﾞｼｯｸM", 0, 14)); // NOI18N
        jLabelSurface.setForeground(new java.awt.Color(0, 153, 51));
        jLabelSurface.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(jLabelSurface, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelGate;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelPortAndCh;
    private javax.swing.JLabel jLabelSurface;
    private javax.swing.JLabel jLabelText;
    private javax.swing.JLabel jLabelValue;
    private javax.swing.JLabel jLabelValueDecimal;
    private javax.swing.JLabel jLabelValueHex;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
