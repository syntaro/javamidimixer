/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachTableResize;
import jp.synthtarou.midimixer.libs.wraplist.MXWrap;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class NavigatorFor2ColumnList<T> extends javax.swing.JPanel implements INavigator<T> {

    MXWrapList<T> _list;

    /**
     * Creates new form NavigatorForWrapList
     */
    public NavigatorFor2ColumnList(MXWrapList<T> list, int selectedIndex) {
        initComponents();
        _list = list;

        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = jTable1.getSelectedRow();
                if (row >= 0) {
                    String name = _list.nameOfIndex(row);
                    Object value = _list.valueOfIndex(row);
                    boolean match = false;
                    if (value != null) {
                        if (value instanceof Number) {
                            value = String.valueOf(value);
                        }
                        if (value instanceof String) {
                            if (value.equals(name)) {
                                match = true;
                            }
                        }
                    }
                    if (match) {
                        jLabel1.setText(name);
                    } else {
                        jLabel1.setText(value + " = \"" + name + "\"");
                    }
                }
            }
        });

        boolean haveUnmatch = false;
        for (Object row : list) {
            MXWrap wrap = (MXWrap<Object>) row;
            if (wrap._value == null) {
                haveUnmatch = true;
                break;
            }
            if (wrap._value instanceof Integer) {
                String vs = String.valueOf((Integer) wrap._value);
                if (vs.equals(wrap._name)) {
                    continue;
                } else {
                    haveUnmatch = true;
                    break;
                }
            }
            if (wrap._value instanceof String == false) {
                haveUnmatch = true;
                break;
            }
            if (wrap._name.equals((String) wrap._value) == false) {
                haveUnmatch = true;
                break;
            }
        }

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        if (haveUnmatch) {
            model.addColumn("Value");
            model.addColumn("Name");

            for (Object row : list) {
                MXWrap wrap = (MXWrap<Object>) row;
                model.addRow(new Object[]{String.valueOf(wrap._value), wrap._name});
            }
            jTable1.setModel(model);
        } else {
            model.addColumn("Value");

            for (Object row : list) {
                MXWrap wrap = (MXWrap<Object>) row;
                model.addRow(new Object[]{wrap._name});
            }
            jTable1.setModel(model);
        }

        for (int i = 0; i < jTable1.getColumnCount(); ++ i) {
            TableColumn target = jTable1.getColumnModel().getColumn(i);
            target.setCellRenderer(new DefaultRowCellRenderer(selectedIndex));
            target.setCellEditor(null);
        }
        jTable1.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    _returnStatus = INavigator.RETURN_STATUS_APPROVED;
                    int row = jTable1.getSelectedRow();
                    _returnIndex = row;
                    _returnValue = _list.valueOfIndex(row);
                    MXUtil.getOwnerWindow(NavigatorFor2ColumnList.this).setVisible(false);
               }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        if (selectedIndex >= 0) {
            jTable1.setRowSelectionInterval(selectedIndex, selectedIndex);
            jTable1.scrollRectToVisible(jTable1.getCellRect(selectedIndex, 0, true));
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jTable1.requestFocus();
                new MXAttachTableResize(jTable1);
            }
        });
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        add(jLabel1, gridBagConstraints);

        jScrollPane1.setViewportView(jTable1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        add(jButtonOK, gridBagConstraints);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButtonCancel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        _returnStatus = INavigator.RETURN_STATUS_CANCELED;
        _returnIndex = -1;
        _returnValue = null;
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        _returnStatus = INavigator.RETURN_STATUS_APPROVED;
        int row = jTable1.getSelectedRow();
        _returnIndex = row;
        _returnValue = _list.valueOfIndex(row);
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButtonOKActionPerformed

    @Override
    public int getNavigatorType() {
        return INavigator.TYPE_SELECTOR;
    }

    int _returnStatus = INavigator.RETURN_STATUS_NOTSET;
    T _returnValue = null;
    int _returnIndex = -1;

    @Override
    public int getReturnStatus() {
        return _returnStatus;
    }

    public int getReturnIndex() {
        return _returnIndex;
    }

    @Override
    public T getReturnValue() {
        return _returnValue;
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
    public JPanel getNavigatorPanel() {
        return this;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    static class CheckBoxCellRenderer implements TableCellRenderer {
        public CheckBoxCellRenderer() {
            _base = new DefaultTableCellRenderer();
        }

        TableCellRenderer _base;
        JCheckBox _checkBox = new JCheckBox();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component base1 = _base.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            _checkBox.setText(String.valueOf(value));
            _checkBox.setSelected(isSelected);
            _checkBox.setBackground(base1.getBackground());
            return _checkBox;
        }
    }

    static class DefaultRowCellRenderer implements TableCellRenderer {
        public DefaultRowCellRenderer(int defaultRow) {
            _base = new DefaultTableCellRenderer();
            _defaultRow = defaultRow;
            _baseTextColor = new JLabel().getForeground();
        }

        int _defaultRow;
        Color _baseTextColor;
        TableCellRenderer _base;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component order = _base.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (_defaultRow == row) {
                order.setForeground(Color.green);
            }else {
                order.setForeground(_baseTextColor);
            }
            return order;
        }
    }
}
