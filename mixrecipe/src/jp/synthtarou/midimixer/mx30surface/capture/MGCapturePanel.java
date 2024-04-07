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
package jp.synthtarou.midimixer.mx30surface.capture;

import java.util.ArrayList;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.libs.midi.MXMessage;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGCapturePanel extends javax.swing.JPanel {

    CapturePool _pool = new CapturePool();
    MGCapture _capture = new MGCapture();

    JList jListCommand = new JList();
    JList jListGate = new JList();
    JList jListValue = new JList();

    MXNamedObjectList<CaptureCommand> listCommandModel;
    MXNamedObjectList<CaptureGate> listGateModel;
    MXNamedObjectList<String> listValueModel;

    /**
     * Creates new form MGCapturePanel
     */
    public MGCapturePanel() {
        initComponents();

        jPanelCommand.add(new JScrollPane(jListCommand));
        jPanelGate.add(new JScrollPane(jListGate));
        jPanelValue.add(new JScrollPane(jListValue));

        updateListModel();
    }

    public void updateListModel() {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateListModel();
                }
            });
            return;
        }
        listCommandModel = _capture.createCommandListModel();
        listGateModel = _capture.createGateListModel(null);
        listValueModel = _capture.createValueListModel(null);

        jListCommand.setModel(listCommandModel);
        jListGate.setModel(listGateModel);
        jListValue.setModel(listValueModel);

        jListCommand.revalidate();
        jListCommand.repaint();

        jListCommand.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int x = jListCommand.getSelectedIndex();
                if (x >= 0) {
                    CaptureCommand command = listCommandModel.valueOfIndex(x);
                    listGateModel = _capture.createGateListModel(command);
                    jListGate.setModel(listGateModel);
                }
            }
        });
        jListGate.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int x = jListGate.getSelectedIndex();
                if (x >= 0) {
                    CaptureGate gate = listGateModel.valueOfIndex(x);
                    listValueModel = _capture.createValueListModel(gate);
                    jListValue.setModel(listValueModel);
                }
            }
        });
        jListCommand.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int x = jListValue.getSelectedIndex();
                if (x >= 0) {
                }
            }
        });

    }

    public MXNamedObjectList<CaptureCommand> getCommandModel() {
        MXNamedObjectList<CaptureCommand> model = new MXNamedObjectList<>();

        model.addNameAndValue("a", null);
        model.addNameAndValue("b", null);
        model.addNameAndValue("c", null);
        return model;
    }

    public MXNamedObjectList<CaptureGate> getGateModel() {
        MXNamedObjectList<CaptureGate> model = new MXNamedObjectList<>();
        model.addNameAndValue("d", null);
        model.addNameAndValue("e", null);
        model.addNameAndValue("f", null);
        return model;
    }

    public MXNamedObjectList<CaptureValue> getValueModel() {
        MXNamedObjectList<CaptureValue> model = new MXNamedObjectList<>();
        model.addNameAndValue("g", null);
        model.addNameAndValue("h", null);
        model.addNameAndValue("i", null);
        return model;
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

        jPanelCommand = new javax.swing.JPanel();
        jPanelGate = new javax.swing.JPanel();
        jPanelValue = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jCheckBoxCapture = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        jPanelCommand.setBorder(javax.swing.BorderFactory.createTitledBorder("Command"));
        jPanelCommand.setLayout(new javax.swing.BoxLayout(jPanelCommand, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelCommand, gridBagConstraints);

        jPanelGate.setBorder(javax.swing.BorderFactory.createTitledBorder("Gate"));
        jPanelGate.setLayout(new javax.swing.BoxLayout(jPanelGate, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelGate, gridBagConstraints);

        jPanelValue.setBorder(javax.swing.BorderFactory.createTitledBorder("Value"));
        jPanelValue.setLayout(new javax.swing.BoxLayout(jPanelValue, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelValue, gridBagConstraints);

        jButton3.setText("Use This Value");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        add(jButton3, gridBagConstraints);

        jCheckBoxCapture.setText("Auto Update");
        jCheckBoxCapture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxCaptureActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(jCheckBoxCapture, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        int x1 = jListCommand.getSelectedIndex();
        int x2 = jListGate.getSelectedIndex();
        int x3 = jListValue.getSelectedIndex();

        if (x1 < 0 || x2 < 0 || x3 < 0) {
            JOptionPane.showMessageDialog(this, "Select from 3xList", "Please", JOptionPane.OK_OPTION);
            return;
        }

        CaptureCommand command = this.listCommandModel.valueOfIndex(x1);
        CaptureGate gate = this.listGateModel.valueOfIndex(x2);
        String value = this.listValueModel.valueOfIndex(x3);

        String str = command + "(gate=" + gate + ")" + value;

        JOptionPane.showMessageDialog(this, str, "selected", JOptionPane.OK_OPTION);

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jCheckBoxCaptureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxCaptureActionPerformed
        if (jCheckBoxCapture.isSelected()) {
            _pool.startCapture();
            setTimer(1000);
        } else {
            _pool.stopCapture();
        }
    }//GEN-LAST:event_jCheckBoxCaptureActionPerformed

    public void showCapture() {
        ArrayList<MXMessage> pooled = _pool.getLastTime(5000);
        MGCapture capture = new MGCapture();
        for (MXMessage seek : pooled) {
            capture.record(seek);
            System.out.println("Copying " + seek);
        }
        _capture = capture;
        updateListModel();
    }

    public void setTimer(long next) {
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(next);
                    showCapture();
                    if (jCheckBoxCapture.isSelected()) {
                        setTimer(1000);
                    }
                } catch (Exception e) {
                }
            }
        }.start();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBoxCapture;
    private javax.swing.JPanel jPanelCommand;
    private javax.swing.JPanel jPanelGate;
    private javax.swing.JPanel jPanelValue;
    // End of variables declaration//GEN-END:variables
}