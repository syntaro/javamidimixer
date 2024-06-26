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
package jp.synthtarou.libs.navigator.legacy;

import jp.synthtarou.midimixer.ccxml.xml.CXGeneralMidiFile;
import jp.synthtarou.midimixer.ccxml.xml.CXNode;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.namedobject.MXNamedObjectListFactory;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class Demo extends javax.swing.JPanel {

    public static void main(String[] args) {
        Demo demo = new Demo();
        MXUtil.showAsDialog(null, demo, "demo");
        System.exit(0);
    }

    /**
     * Creates new form NavigatorTester
     */
    public Demo() {
        initComponents();
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
        jTextArea1 = new javax.swing.JTextArea();
        jButtonList = new javax.swing.JButton();
        jButtonText = new javax.swing.JButton();
        jButtonNumber = new javax.swing.JButton();
        jButtonAttrib = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 3.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jButtonList.setText("1 ColumnList");
        jButtonList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonListActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jButtonList, gridBagConstraints);

        jButtonText.setText("Text");
        jButtonText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jButtonText, gridBagConstraints);

        jButtonNumber.setText("Number");
        jButtonNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNumberActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jButtonNumber, gridBagConstraints);

        jButtonAttrib.setText("NodeAttr");
        jButtonAttrib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAttribActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jButtonAttrib, gridBagConstraints);

        jButton1.setText("2 ColumnList");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jButton1, gridBagConstraints);

        jButton2.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jButton2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonListActionPerformed
        MXNamedObjectList<Integer> list = MXNamedObjectListFactory.listupDrumnote(true);
        int x = 0;
        try {
            int y = Integer.valueOf(jTextArea1.getText());
            x = list.indexOfValue(y);
        }catch(Exception e) {
            x = -1;
        }
        NavigatorFor1ColumnList<Integer> navi = new NavigatorFor1ColumnList(list, x);
        if (navi.simpleAsk(this)) {
            jTextArea1.setText(String.valueOf(navi._returnValue));
        }
    }//GEN-LAST:event_jButtonListActionPerformed

    private void jButtonTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTextActionPerformed
        NavigatorForText navi = new NavigatorForText("Default");
        if (navi.simpleAsk(this)) {
            jTextArea1.setText(String.valueOf(navi._returnValue));
        }
    }//GEN-LAST:event_jButtonTextActionPerformed

    private void jButtonNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNumberActionPerformed
        NavigatorForNumber navi = new NavigatorForNumber(MXRangedValue.ZERO7);
        if (navi.simpleAsk(this)) {
            jTextArea1.setText(String.valueOf(navi._returnValue));
        }
    }//GEN-LAST:event_jButtonNumberActionPerformed

    private void jButtonAttribActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAttribActionPerformed
        CXGeneralMidiFile file = CXGeneralMidiFile.getInstance();
        CXNode node = file.simpleFindProgram(80);
        NavigatorForNodeAttribute navi = new NavigatorForNodeAttribute(node, "Name");
        if (navi.simpleAsk(this)) {
            jTextArea1.setText(String.valueOf(navi._returnValue._name + " = " + navi._returnValue._value));
        }
    }//GEN-LAST:event_jButtonAttribActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        MXNamedObjectList<Integer> list = MXNamedObjectListFactory.listupDrumnote(true);
        int x;
        try {
            int y = Integer.valueOf(jTextArea1.getText());
            x = list.indexOfValue(y);
        }catch(Exception e) {
            x = -1;
        }
        NavigatorFor2ColumnList navi = new NavigatorFor2ColumnList(list, x);
        if (navi.simpleAsk(this)) {
            jTextArea1.setText(String.valueOf(navi._returnValue));
        }
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButtonAttrib;
    private javax.swing.JButton jButtonList;
    private javax.swing.JButton jButtonNumber;
    private javax.swing.JButton jButtonText;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
