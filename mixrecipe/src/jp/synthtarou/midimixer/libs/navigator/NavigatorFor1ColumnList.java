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
package jp.synthtarou.midimixer.libs.navigator;

import java.awt.Dimension;
import javax.swing.JPanel;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class NavigatorFor1ColumnList<T> extends javax.swing.JPanel implements INavigator<T>{
    
    public static void main(String[] args) {
        MXWrapList<String> model = new MXWrapList<>();
        model.addNameAndValue("ABC", "abcdefg");
        model.addNameAndValue("HIJ", "hijklmn");
        model.addNameAndValue("OPQ", "opqrstu");
        model.addNameAndValue("VWX", "vwxyz");
        
        NavigatorFor1ColumnList<String> picker = new NavigatorFor1ColumnList<>(model, 2);
        MXUtil.showAsDialog(null, picker, "Navigator for List");
        System.out.println(picker.getReturnStatus() + " = " + picker.getReturnValue());
    }

    
    MXWrapList<T> _listChoise;
    boolean _initDone = false;

    /**
     * Creates new form NavigatorForList
     */
    public NavigatorFor1ColumnList(MXWrapList<T> choise, int selectedIndex) {
        initComponents();
        _listChoise = choise;
        int sel = selectedIndex;
        jListChoise.setModel(_listChoise);
        if (sel >= 0) {
           jListChoise.setSelectedIndex(selectedIndex);
           jListChoise.scrollRectToVisible(jListChoise.getCellBounds(sel, sel));
        }
        _initDone = true;

        setPreferredSize(new Dimension(400, 600));
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
        jListChoise = new javax.swing.JList<>();
        jButtonCancel = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();
        jLabelSelection = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jListChoise.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListChoiseValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListChoise);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jButtonCancel, gridBagConstraints);

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        add(jButtonOK, gridBagConstraints);

        jLabelSelection.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jLabelSelection, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        if (!_initDone) {
            return;
        }
        _returnStatus = INavigator.RETURN_STATUS_CANCELED;
        _returnValue = null;
        _returnIndex = -1;
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        if (!_initDone) {
            return;
        }
        int sel = jListChoise.getSelectedIndex();
        if (sel >= 0) {
            _returnIndex = sel;
            _returnValue = _listChoise.valueOfIndex(sel);
            _returnStatus = INavigator.RETURN_STATUS_APPROVED;
            MXUtil.getOwnerWindow(this).setVisible(false);
        }
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jListChoiseValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListChoiseValueChanged
        if (!_initDone) {
            return;
        }
        int debugSel = jListChoise.getSelectedIndex();
        String name = _listChoise.nameOfIndex(debugSel);
        T value = _listChoise.valueOfIndex(debugSel);
        
        jLabelSelection.setText(value + " = \"" + name + "\"");
    }//GEN-LAST:event_jListChoiseValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabelSelection;
    private javax.swing.JList<String> jListChoise;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public JPanel getNavigatorPanel() {
        return this;
    }

    @Override
    public int getNavigatorType() {
        return INavigator.TYPE_SELECTOR;
    }

    @Override
    public boolean isNavigatorRemovable() {
        return false;
    }

    @Override
    public boolean validateWithNavigator(T result) {
        return true;
    }

    @Override
    public int getReturnStatus() {
        return _returnStatus;
    }

    @Override
    public T getReturnValue() {
        return _returnValue;
    }

    public int getReturnIndex() {
        return _returnIndex;
    }

    int _returnStatus = INavigator.RETURN_STATUS_NOTSET;
    T _returnValue = null;
    int _returnIndex = -1;
}
