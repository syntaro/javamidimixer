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
package jp.synthtarou.midimixer.mx13patch;

import java.awt.Color;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import jp.synthtarou.libs.MXCountdownTimer;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.libs.midi.port.MXMidiFilter;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX13View extends javax.swing.JPanel {

    MX13Process _process;
    int _stopFeedback = 1;

    public static void main(String[] args) {
        MX13Process process = new MX13Process(true);
        MXUtil.showAsDialog(null, process.getReceiverView(), "Test");
    }

    CheckableListCellRenderer selectFrom;
    CheckableListCellRenderer selectTo;
    CheckableListCellRenderer selectFilter;

    /**
     * Creates new form MX13View
     *
     * @param process
     */
    public MX13View(MX13Process process) {
        initComponents();
        _process = process;
        selectTo = new CheckableListCellRenderer<MX13To>(jListTo);
        selectFilter = new CheckableListCellRenderer<MX13ToFilter>(jListToFilter);
        jListTo.setCellRenderer(selectTo);
        jListToFilter.setCellRenderer(selectFilter);
        _stopFeedback--;
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

        jScrollPaneTo = new javax.swing.JScrollPane();
        jListTo = new javax.swing.JList<>();
        jScrollPaneFilter = new javax.swing.JScrollPane();
        jListToFilter = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jCheckBoxOpen = new javax.swing.JCheckBox();
        jLabelDevice = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Select Device ^"));
        setLayout(new java.awt.GridBagLayout());

        jListTo.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListToValueChanged(evt);
            }
        });
        jScrollPaneTo.setViewportView(jListTo);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPaneTo, gridBagConstraints);

        jListToFilter.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListToFilterValueChanged(evt);
            }
        });
        jScrollPaneFilter.setViewportView(jListToFilter);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPaneFilter, gridBagConstraints);

        jLabel2.setText("To");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(jLabel2, gridBagConstraints);

        jLabel1.setText("Filter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(jLabel1, gridBagConstraints);

        jCheckBoxOpen.setText("Open");
        jCheckBoxOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxOpenActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jCheckBoxOpen, gridBagConstraints);

        jLabelDevice.setText("Device :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelDevice, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jListToValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListToValueChanged
        if (_stopFeedback > 0) {
            return;
        }
        _stopFeedback++;
        try {
            int index = jListTo.getSelectedIndex();
            if (index < 0) {
                return;
            }
            MX13To to =_midiTo.get(index);
            _midiFilter = createMidiFilter(to._in, to._port);
            jListToFilter.setModel(_midiFilter);
            jListToFilter.setSelectedIndex(-1);

            Color oColor = jListTo.getBackground();
            jListTo.setBackground(Color.cyan);
            MXCountdownTimer.letsCountdown(100, () -> {
                jListTo.setBackground(oColor);
                jListToFilter.setBackground(Color.cyan);
                MXCountdownTimer.letsCountdown(100, () -> {
                    jListToFilter.setBackground(oColor);
                    jScrollPaneFilter.getVerticalScrollBar().setValue(0);
                });
            });
        } finally {
            _stopFeedback--;
        }
    }//GEN-LAST:event_jListToValueChanged

    private void jListToFilterValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListToFilterValueChanged
        Color oColor = jListTo.getBackground();
        jListToFilter.setBackground(Color.cyan);
        MXCountdownTimer.letsCountdown(100, () -> {
            jListToFilter.setBackground(oColor);
        });
    }//GEN-LAST:event_jListToFilterValueChanged

    private void jCheckBoxOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxOpenActionPerformed
        if (_midiIn != null) {
            if (jCheckBoxOpen.isSelected()) {
                if (_midiIn.isOpen() == false) {
                    _midiIn.openInput(1000);
                    _process.fireChangeListener(new ChangeEvent(_process));
                }
            }
            else {
                if (_midiIn.isOpen()) {
                    _midiIn.close();
                    _process.fireChangeListener(new ChangeEvent(_process));
                }
            }
        }
    }//GEN-LAST:event_jCheckBoxOpenActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxOpen;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelDevice;
    private javax.swing.JList<MX13To> jListTo;
    private javax.swing.JList<MX13ToFilter> jListToFilter;
    private javax.swing.JScrollPane jScrollPaneFilter;
    private javax.swing.JScrollPane jScrollPaneTo;
    // End of variables declaration//GEN-END:variables

    MXMIDIIn _midiIn = null;
    DefaultListModel<MX13To> _midiTo = null;
    DefaultListModel<MX13ToFilter> _midiFilter = null;
    
    public void showMIDIInDetail(MXMIDIIn in) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(() -> {
                showMIDIInDetail(in);
            });
            return;
        }
        if (_midiIn != in) {
            _midiIn = in;
            _midiTo = createMidiTo(in);
            int port = _midiTo.size() > 0 ? _midiTo.get(0)._port : -1;
            jListTo.setModel(_midiTo);
            _midiFilter = createMidiFilter(in, port);
            jListToFilter.setModel(_midiFilter);
            jLabelDevice.setText(in.getName());
            jCheckBoxOpen.setSelected(in.isOpen());
        }
    }

    public DefaultListModel<MX13To> createMidiTo(MXMIDIIn in) {
        DefaultListModel<MX13To> result = new DefaultListModel();
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            MX13To to = new MX13To(_process, in, port);
            result.addElement(to);
        }
        return result;
    }

    public DefaultListModel<MX13ToFilter> createMidiFilter(MXMIDIIn in, int port) {
        DefaultListModel<MX13ToFilter> result = new DefaultListModel();
        if (port >= 0) {
            for (int type = 0; type < MXMidiFilter.COUNT_TYPE; ++ type) {
                MXMidiFilter filter = in.getFilter(port);
                MX13ToFilter filter2 = new MX13ToFilter(_process, in, port, type);
                result.addElement(filter2);
            }
        }
        return result;
    }
}
