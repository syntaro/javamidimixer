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
package jp.synthtarou.midimixer.mx50resolution;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.ccxml.InformationForCCM;
import jp.synthtarou.midimixer.ccxml.ui.PickerForControlChange;
import jp.synthtarou.midimixer.libs.common.MXLogger2;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.navigator.MXPopup;
import jp.synthtarou.midimixer.libs.navigator.MXPopupForList;
import jp.synthtarou.midimixer.libs.navigator.legacy.INavigator;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapListFactory;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXResolutionView extends javax.swing.JPanel {

    MXResolution _resolution;

    MXWrapList<Integer> _ccGateModel;
    MXWrapList<Integer> _keyGateModel;
    MXWrapList<Integer> _normalGateModel;
    MXWrapList<Integer> _currentGateModel;

    MXWrapList<Integer> _listPort = MXWrapListFactory.listupPort(null);
    MXWrapList<Integer> _listChannel = MXWrapListFactory.listupChannel(null);
    MXWrapList<Integer> _listResolution = new MXWrapList<>();

    public void displayResolutionToPanel() {
        int command = 0;
        try {
            command = _resolution._command.get(0);
        } catch (Exception e) {
        }

        if (command == MXMidi.COMMAND_CH_CHANNELPRESSURE || command == MXMidi.COMMAND_CH_NOTEON || command == MXMidi.COMMAND_CH_NOTEOFF) {
            _currentGateModel = _keyGateModel;
        } else if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
            _currentGateModel = _ccGateModel;
        } else {
            _currentGateModel = _normalGateModel;
        }
        MXWrapList<Integer> gateTable = _resolution._gateTable;
        if (gateTable == null) {
            gateTable = _currentGateModel;
        }
        String gateText = gateTable.nameOfValue(_resolution._gate);
        if (_resolution._command == null) {
            jTextFieldCommand.setText("");
        } else {
            jTextFieldCommand.setText(_resolution._command.toDText());
        }
        jTextFieldGate.setText(gateText);
        jTextFieldPort.setText(_listPort.nameOfValue(_resolution._port));
        jTextFieldChannel.setText(_listChannel.nameOfValue(_resolution._channel));
        jTextFieldResolution.setText(_listResolution.nameOfValue(_resolution._resolution));
    }

    /**
     * Creates new form MX50ResolutionView
     */
    public MXResolutionView(MXResolution resolution) {
        initComponents();
        _resolution = resolution;
        resolution._bindedView = this;

        _ccGateModel = MXWrapListFactory.listupControlChange(true);
        _keyGateModel = MXWrapListFactory.listupNoteNo(true);
        _normalGateModel = MXWrapListFactory.listupGate7Bit();

        new MXPopup(jTextFieldCommand) {
            @Override
            public void showPopup(JComponent mouseBase) {
                startEditCommand();
                displayResolutionToPanel();
            }
        };

        resetBackground();

        new MXPopupForList<Integer>(jTextFieldPort, _listPort) {
            @Override
            public void approvedIndex(int selectedIndex) {
                _resolution._port= _listPort.valueOfIndex(selectedIndex);
                displayResolutionToPanel();
            }
        };
        new MXPopupForList<Integer>(jTextFieldChannel, _listChannel) {
            @Override
            public void approvedIndex(int selectedIndex) {
                _resolution._channel = _listChannel.valueOfIndex(selectedIndex);
                displayResolutionToPanel();
            }
        };
        _listResolution = new MXWrapList<>();
        int[] newReso = new int[]{
            0, 8, 16, 32, 64, 128, 256, 512
        };
        TreeSet<Integer> sort = new TreeSet();
        for (int x : newReso) {
            sort.add(x);
        }
        sort.add(resolution._resolution);
        for (Integer x : sort) {
            _listResolution.addNameAndValue(x == 0 ? "-" : Integer.toString(x), x);
        }
        new MXPopupForList<Integer>(jTextFieldResolution, _listResolution) {
            @Override
            public void approvedIndex(int selectedIndex) {
                _resolution._resolution = _listResolution.valueOfIndex(selectedIndex);
                displayResolutionToPanel();
            }
        };
        new MXPopup(jTextFieldGate) {
            @Override
            public void showPopup(JComponent mouseBase) {
                MXWrapList<Integer> gateTable = _resolution._gateTable == null ? _currentGateModel : _resolution._gateTable;
                if (gateTable != null) {
                    MXPopupForList<Integer> popup = new MXPopupForList(null, gateTable) {
                        @Override
                        public void approvedIndex(int selectedIndex) {
                            int gate = gateTable.valueOfIndex(selectedIndex);
                            _resolution._gate = gate;
                            //refill
                            if (_resolution._command != null) {
                                if (_resolution._command.get(0) == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                                    int[] template = _resolution._command.toIntArray();
                                    template[1] = _resolution._gate;
                                    _resolution._command = new MXTemplate(template);
                                }
                            }
                            displayResolutionToPanel();
                        }
                    };
                    popup.setSelectedIndex(gateTable.indexOfValue(_resolution._gate));
                    popup.showPopup(mouseBase);
                }
            }
        };
        displayResolutionToPanel();
    }

    @Override
    public Color getBackground() {
        if (_resolution == null) {
            return Color.white;
        }
        int x = _resolution._process.indexOfResolution(_resolution) % 3;
        switch (x) {
            case 0:
                return new Color(255, 255, 240);
            case 1:
                return new Color(255, 240, 255);
            default:
                return new Color(240, 255, 255);
        }
    }

    public void resetBackground() {
        LinkedList<Component> list = new LinkedList<>();
        list.add(this);
        while (list.isEmpty() == false) {
            Component seek = list.removeFirst();
            seek.setBackground(null);
            if (seek instanceof Container) {
                Container cont = (Container) seek;
                for (Component child : cont.getComponents()) {
                    list.add(child);
                }
            }
        }
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
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldCommand = new javax.swing.JTextField();
        jTextFieldGate = new javax.swing.JTextField();
        jTextFieldChannel = new javax.swing.JTextField();
        jTextFieldResolution = new javax.swing.JTextField();
        jTextFieldMonitor = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldPort = new javax.swing.JTextField();
        jButtonRemove = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Resolution"));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("New Resolution");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        add(jLabel1, gridBagConstraints);

        jLabel2.setText("Monitor");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel2, gridBagConstraints);

        jLabel3.setText("Command");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel3, gridBagConstraints);

        jLabel6.setText("Gate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel6, gridBagConstraints);

        jLabel7.setText("Channel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel7, gridBagConstraints);

        jTextFieldCommand.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jTextFieldCommand, gridBagConstraints);

        jTextFieldGate.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jTextFieldGate, gridBagConstraints);

        jTextFieldChannel.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jTextFieldChannel, gridBagConstraints);

        jTextFieldResolution.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jTextFieldResolution, gridBagConstraints);

        jTextFieldMonitor.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jTextFieldMonitor, gridBagConstraints);

        jLabel4.setText("Port");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jTextFieldPort, gridBagConstraints);

        jButtonRemove.setText("Remove");
        jButtonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveActionPerformed(evt);
            }
        });
        add(jButtonRemove, new java.awt.GridBagConstraints());
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveActionPerformed
        // TODO add your handling code here:
        _resolution._process.removeResolution(_resolution);
        _resolution._process._view.reloadList();
    }//GEN-LAST:event_jButtonRemoveActionPerformed

    public void startEditCommand() {
        PickerForControlChange picker = new PickerForControlChange();
        MXUtil.showAsDialog(this, picker, "Picker");
        if (picker.getReturnStatus() != INavigator.RETURN_STATUS_APPROVED) {
            return;
        }

        List<InformationForCCM> ccmList = picker.getReturnValue();
        InformationForCCM ccm = null;
        if (ccmList != null && ccmList.isEmpty() == false) {
            ccm = ccmList.getFirst();
        }
        if (ccm != null) {
            try {
                _resolution._command = new MXTemplate(ccm._data);
                _resolution._gate = ccm.getParsedGate()._value;
                _resolution._defaultValue = ccm.getParsedValue()._value;
                _resolution._gateTable = ccm.getParsedGateTable();
                _resolution._valueTable = ccm.getParsedValueTable();
                
                if (_resolution._command.get(0) == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                    int gate = _resolution._command.get(1);
                    if (gate != MXMidi.CCXML_GL) {
                        _resolution._gate = gate;
                    }
                }
                displayResolutionToPanel();
            } catch (RuntimeException ex) {
                MXLogger2.getLogger(MXResolutionView.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField jTextFieldChannel;
    private javax.swing.JTextField jTextFieldCommand;
    private javax.swing.JTextField jTextFieldGate;
    private javax.swing.JTextField jTextFieldMonitor;
    private javax.swing.JTextField jTextFieldPort;
    private javax.swing.JTextField jTextFieldResolution;
    // End of variables declaration//GEN-END:variables

    public void updateMonitor(int original, int translated) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateMonitor(original, translated);
                }
            });
            return;
        }
        jTextFieldMonitor.setText("from " + original + " To " + translated);
    }
}
