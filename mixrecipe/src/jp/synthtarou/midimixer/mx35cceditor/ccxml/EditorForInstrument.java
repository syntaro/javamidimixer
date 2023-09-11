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
package jp.synthtarou.midimixer.mx35cceditor.ccxml;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.MXGlobalTimer;
import jp.synthtarou.midimixer.libs.midi.port.FinalMIDIOut;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIInManager;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOutManager;
import jp.synthtarou.midimixer.libs.swing.MXModalFrame;
import jp.synthtarou.midimixer.libs.swing.MXSwingPiano;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class EditorForInstrument extends javax.swing.JPanel {

    public static void main(String[] args) {
        EditorForInstrument picker = new EditorForInstrument();
        MXModalFrame.showAsDialog(null, picker, "Test");
        MXMIDIInManager.getManager().initWithSetting();
        MXMIDIOutManager.getManager().initWithSetting();
        System.exit(0);
    }

    /**
     * Creates new form MXProgramPicker
     */
    public EditorForInstrument() {
        initComponents();

        jComboBoxTestPort.setModel(MXMidi.listupPortAssigned(false));
        jComboBoxTestChannel.setModel(MXMidi.listupChannel(false));
        _listXMLFile = CXFileList.getInstance()._listLoaded;

        _scanXMLFile = null;
        if (_listXMLFile.size() > 0) {
            _scanXMLFile = _listXMLFile.get(0);
        }
        updateXMLFileView();

        _piano = new MXSwingPiano();
        jPanelPiano.add(_piano);
        _piano.setHandler(new MyHandler());
        jTextFieldSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                textChanged();
            }

            public void removeUpdate(DocumentEvent e) {
                textChanged();
            }

            public void insertUpdate(DocumentEvent e) {
                textChanged();
            }
        });

        _listReceiver = MXMain.getMain().getReceiverList();
        _listReceiver.writeComboBox(jComboBoxTestReceiver, FinalMIDIOut.getInstance());

        setPreferredSize(new Dimension(800, 600));


        _group1 = new ButtonGroup();
        _group1.add(jRadioButtonVel1);
        _group1.add(jRadioButtonVel2);
        _group1.add(jRadioButtonVel3);
        _group1.add(jRadioButtonVel4);

        jRadioButtonVel3.setSelected(true);
    }

    int[] _tblVelocity = {20, 60, 100, 127};

    public int getVelocity() {
        if (jRadioButtonVel1.isSelected()) {
            return _tblVelocity[0];
        }
        if (jRadioButtonVel2.isSelected()) {
            return _tblVelocity[1];
        }
        if (jRadioButtonVel3.isSelected()) {
            return _tblVelocity[2];
        }
        if (jRadioButtonVel4.isSelected()) {
            return _tblVelocity[3];
        }
        return 127;
    }

    ButtonGroup _group1;

    int _testPort;
    int _testChannel;

    ArrayList<CXFile> _listXMLFile;
    MXWrapList<CXFile> _modelListXML;
    MXWrapList<CXNode> _modelListModule;
    MXWrapList<CXNode> _modelListMap;
    MXWrapList<CXNode> _modelListProgram;
    MXWrapList<CXNode> _modelListBank;

    MXSwingPiano _piano;

    MXWrapList<MXReceiver> _listReceiver;

    CXFile _resultXMLFile = null;
    CXNode _resultModule = null;
    CXNode _resultMap = null;
    public CXNode _resultProgram = null;
    public CXNode _resultBank = null;
    String _resultText = "";

    CXFile _scanXMLFile = null;
    CXNode _scanModule = null;
    CXNode _scanMap = null;
    int _scanProgram = 1;
    /* 1~128 */
    int _scanBankMSB = -1;
    /* 0-FF */
    int _scanBankLSB = -1;
    /* 0-FF */
    String _scanText = "";

    class MyHandler implements MXSwingPiano.Handler {

        @Override
        public void noteOn(int note) {
            sendMessageToReceiver(MXMidi.COMMAND_NOTEON, note, getVelocity());
        }

        @Override
        public void noteOff(int note) {
            sendMessageToReceiver(MXMidi.COMMAND_NOTEOFF, note, 0);
        }

        @Override
        public void selectionChanged() {
        }
    }

    long _timeToSearch = 0;

    public void textChanged() {
        _timeToSearch = System.currentTimeMillis() + 300;
        MXGlobalTimer.letsCountdown(300, new Runnable() {
            public void run() {
                if (System.currentTimeMillis() >= _timeToSearch) {
                    _scanText = jTextFieldSearch.getText();
                    _scanMap = null;
                    updateXMLFileView();
                }
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

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jPanelPiano = new javax.swing.JPanel();
        jComboBoxTestChannel = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jComboBoxTestPort = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jLabelBankProgram = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jComboBoxTestReceiver = new javax.swing.JComboBox<>();
        jButtonSelect = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jRadioButtonVel1 = new javax.swing.JRadioButton();
        jRadioButtonVel2 = new javax.swing.JRadioButton();
        jRadioButtonVel3 = new javax.swing.JRadioButton();
        jRadioButtonVel4 = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jTextFieldSearch = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListProgram = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListBank = new javax.swing.JList<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jComboBoxXML = new javax.swing.JComboBox<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListMap = new javax.swing.JList<>();
        jLabel9 = new javax.swing.JLabel();
        jComboBoxModule = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Sound Tests"));
        setLayout(new java.awt.GridBagLayout());

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Test Output"));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanelPiano.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanelPiano.setLayout(new javax.swing.BoxLayout(jPanelPiano, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jPanelPiano, gridBagConstraints);

        jComboBoxTestChannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTestChannelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jComboBoxTestChannel, gridBagConstraints);

        jLabel5.setText("Port");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel2.add(jLabel5, gridBagConstraints);

        jComboBoxTestPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTestPortActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel2.add(jComboBoxTestPort, gridBagConstraints);

        jLabel6.setText("CH");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jLabel6, gridBagConstraints);

        jLabelBankProgram.setFont(jLabelBankProgram.getFont().deriveFont(jLabelBankProgram.getFont().getSize()+7f));
        jLabelBankProgram.setForeground(new java.awt.Color(0, 153, 51));
        jLabelBankProgram.setText("Piano -");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jLabelBankProgram, gridBagConstraints);

        jLabel7.setText("On");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jLabel7, gridBagConstraints);

        jComboBoxTestReceiver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTestReceiverActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jComboBoxTestReceiver, gridBagConstraints);

        jButtonSelect.setText("OK");
        jButtonSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel2.add(jButtonSelect, gridBagConstraints);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel2.add(jButtonCancel, gridBagConstraints);

        jRadioButtonVel1.setText("p");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        jPanel2.add(jRadioButtonVel1, gridBagConstraints);

        jRadioButtonVel2.setText("pf");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jRadioButtonVel2, gridBagConstraints);

        jRadioButtonVel3.setText("f");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jRadioButtonVel3, gridBagConstraints);

        jRadioButtonVel4.setText("ff");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jRadioButtonVel4, gridBagConstraints);

        jSplitPane1.setLeftComponent(jPanel2);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Tone"));
        jPanel1.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jTextFieldSearch, gridBagConstraints);

        jLabel1.setText("Map");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel1, gridBagConstraints);

        jLabel2.setText("Program");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel2, gridBagConstraints);

        jListProgram.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListProgramValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListProgram);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jLabel3.setText("Bank");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel3, gridBagConstraints);

        jListBank.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListBankValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jListBank);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane2, gridBagConstraints);

        jLabel4.setText("Search");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel4, gridBagConstraints);

        jLabel8.setText("XML");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jLabel8, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jComboBoxXML, gridBagConstraints);

        jListMap.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListMapValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jListMap);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jScrollPane3, gridBagConstraints);

        jLabel9.setText("Module");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jLabel9, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jComboBoxModule, gridBagConstraints);

        jButton1.setText("+");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jButton1, gridBagConstraints);

        jButton3.setText("Edit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(jButton3, gridBagConstraints);

        jButton4.setText("+");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jButton4, gridBagConstraints);

        jButton6.setText("Edit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(jButton6, gridBagConstraints);

        jButton7.setText("+");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jButton7, gridBagConstraints);

        jButton9.setText("Edit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(jButton9, gridBagConstraints);

        jSplitPane1.setRightComponent(jPanel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSplitPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxTestChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTestChannelActionPerformed

    }//GEN-LAST:event_jComboBoxTestChannelActionPerformed

    private void jListProgramValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListProgramValueChanged
        if (_internalChange) {
            return;
        }
        int index = jListProgram.getSelectedIndex();
        if (index >= 0) {
            _scanProgram = _modelListProgram.valueOfIndex(index)._listAttributes.numberOfName("PC", -1);
            updateXMLFileView();
        }
    }//GEN-LAST:event_jListProgramValueChanged

    private void jListBankValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListBankValueChanged
        sendProgramChange();
    }//GEN-LAST:event_jListBankValueChanged

    private void jComboBoxTestPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTestPortActionPerformed
    }//GEN-LAST:event_jComboBoxTestPortActionPerformed

    private void jButtonSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectActionPerformed
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButtonSelectActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        _resultProgram = null;
        _resultBank = null;
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jListMapValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListMapValueChanged
        if (_internalChange) {
            return;
        }
        _scanMap = null;
        int sel = jListMap.getSelectedIndex();
        if (sel >= 0) {
            _scanMap = _modelListMap.valueOfIndex(sel);
        }
        updateXMLFileView();
    }//GEN-LAST:event_jListMapValueChanged

    private void jComboBoxTestReceiverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTestReceiverActionPerformed

    }//GEN-LAST:event_jComboBoxTestReceiverActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonSelect;
    private javax.swing.JComboBox<String> jComboBoxModule;
    private javax.swing.JComboBox<String> jComboBoxTestChannel;
    private javax.swing.JComboBox<String> jComboBoxTestPort;
    private javax.swing.JComboBox<String> jComboBoxTestReceiver;
    private javax.swing.JComboBox<String> jComboBoxXML;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelBankProgram;
    private javax.swing.JList<String> jListBank;
    private javax.swing.JList<String> jListMap;
    private javax.swing.JList<String> jListProgram;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelPiano;
    private javax.swing.JRadioButton jRadioButtonVel1;
    private javax.swing.JRadioButton jRadioButtonVel2;
    private javax.swing.JRadioButton jRadioButtonVel3;
    private javax.swing.JRadioButton jRadioButtonVel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jTextFieldSearch;
    // End of variables declaration//GEN-END:variables

    boolean _internalChange = false;

    public void updateXMLFileView() {
        System.out.println("scanXMLFile = " + _scanXMLFile);
        /* XML一覧を更新し、_scanXMLFileを選択する */
        _modelListXML = new MXWrapList();
        if (_resultXMLFile != _scanXMLFile || _modelListXML == null) {
            _resultXMLFile = _scanXMLFile;
            _modelListXML = new MXWrapList<>();
            _modelListModule = null;

            int selection = -1;
            // 一覧を更新する
            for (int i = 0; i < _listXMLFile.size(); ++i) {
                CXFile xmlSeek = _listXMLFile.get(i);
                _modelListXML.addNameAndValue(xmlSeek._file.getName(), xmlSeek);
            }
            // 選択を取得する
            for (int i = 0; i < _modelListXML.size(); ++i) {
                CXFile seek = _modelListXML.valueOfIndex(i);
                if (seek == _scanXMLFile) {
                    selection = i;
                }
            }
            //UIを更新する
            _internalChange = true;
            jComboBoxXML.setModel(_modelListXML);
            jComboBoxXML.setSelectedIndex(selection);
            _internalChange = false;
        }
        updateModuleView();
    }

    public void updateModuleView() {
        System.out.println("scanModule = " + _scanModule);
        /* MAP一覧を更新し、_scanMapを選択する */
        if (_resultModule != _scanModule || _scanText.equals(_resultText) == false || _modelListModule == null) {
            _resultModule = _scanModule;
            _modelListMap = null;

            _modelListModule = new MXWrapList<>();
            int selection = -1;

            if (_resultXMLFile != null && _resultXMLFile._document != null) {
                // 一覧を更新する
                List<CXNode> moduleData = _resultXMLFile._document.listChildren(CCRuleManager._instance.moduleData);
                if (moduleData != null) {
                    if (_scanModule == null && moduleData.size() > 0) {
                        _scanModule = moduleData.get(0);
                        _resultModule = _scanModule;
                    }
                    for (int seek = 0; seek < moduleData.size(); ++seek) {
                        CXNode moduleSeek = moduleData.get(seek);
                        _modelListModule.addNameAndValue(moduleSeek._listAttributes.valueOfName("Name"), moduleSeek);
                    }
                }
            }
            // 選択を取得する
            for (int i = 0; i < _modelListModule.size(); ++i) {
                CXNode seek = _modelListModule.valueOfIndex(i);
                if (seek == _scanModule) {
                    _resultModule = seek;
                    selection = i;
                }
            }
            // UIを更新する
            _internalChange = true;
            jComboBoxModule.setModel(_modelListModule);
            jComboBoxModule.setSelectedIndex(selection);
            _internalChange = false;
        }
        updateMapView();
    }

    public void updateMapView() {
        System.out.println("scanMap= " + _scanMap);
        /* MAP一覧を更新し、_scanMapを選択する */
        if (_resultMap != _scanMap || _scanText.equals(_resultText) == false || _modelListMap == null) {
            _resultMap = _scanMap;
            _modelListProgram = null;

            _modelListMap = new MXWrapList<>();
            int selection = -1;

            // 一覧を更新する
            if (_scanModule != null) {
                List<CXNode> instrumentList = _scanModule.listChildren(CCRuleManager._instance.instrumentList);
                System.out.println("instrumentList  " + instrumentList.size());
                if (instrumentList != null && instrumentList.size() > 0) {
                    List<CXNode> mapList = instrumentList.get(0).listChildren(CCRuleManager._instance.instrumentList_map);
                    System.out.println("mapList " + mapList.size());
                    for (int seek = 0; seek < mapList.size(); ++seek) {
                        CXNode mapSeek = mapList.get(seek);
                        _modelListMap.addNameAndValue(mapSeek._listAttributes.valueOfName("Name"), mapSeek);
                    }
                }
            }
            // 選択を取得する
            for (int i = 0; i < _modelListMap.size(); ++i) {
                CXNode seek = _modelListMap.valueOfIndex(i);
                if (seek == _scanMap) {
                    _resultMap = seek;
                    selection = i;
                }
            }
            // UIを更新する
            _internalChange = true;
            jListMap.setModel(_modelListMap);
            jListMap.setSelectedIndex(selection);
            _internalChange = false;
        }
        updateProgramView();
    }

    public void updateProgramView() {
        System.out.println("scanProgram = " + _scanProgram);
        /* プログラム一覧を更新し、_scanProgramを選択する */
        int resultPC = -1;
        if (_resultProgram != null) {
            resultPC = _resultProgram._listAttributes.numberOfName("PC", -1);
        }
        if (resultPC != _scanProgram || _scanText.equals(_resultText) == false || _modelListProgram == null) {
            _modelListBank = null;
            _modelListProgram = new MXWrapList<>();

            if (_resultMap != null) {
                //一覧を更新する
                List<CXNode> PCList = _resultMap.listChildren("PC");
                System.out.println("PCList " + PCList.size());
                for (int i = 0; i < PCList.size(); ++i) {
                    CXNode programSeek = PCList.get(i);
                    String pc = programSeek._listAttributes.valueOfName("PC");
                    String name = programSeek._listAttributes.valueOfName("Name");

                    _modelListProgram.addNameAndValue(pc + ". " + name, programSeek);
                }
            }
            //選択を取得する
            int selection = -1;
            for (int i = 0; i < _modelListProgram.size(); ++i) {
                CXNode seek = _modelListProgram.valueOfIndex(i);
                String pc = seek._listAttributes.valueOfName("PC");

                if (MXUtil.numberFromText(pc) == _scanProgram || _modelListProgram.size() == 1) {
                    _resultProgram = seek;
                    selection = i;
                    break;
                }
            }
            //UIを更新する
            _internalChange = true;
            jListProgram.setModel(_modelListProgram);
            jListProgram.setSelectedIndex(selection);
            _internalChange = false;
        }
        updateBankView();
    }

    public void updateBankView() {
        System.out.println("scanBank = " + _scanBankMSB + ":" + _scanBankLSB);
        //BANK一覧を更新し、_scanBankMSB::_scanBankSBを選択する
        int resultMSB = -1;
        int resultLSB = -1;
        if (_resultBank != null) {
            resultMSB = _resultBank._listAttributes.numberOfName("MSB", -1);
            resultLSB = _resultBank._listAttributes.numberOfName("LSB", -1);
        }
        if (_scanBankMSB != resultMSB || _scanBankLSB != resultLSB || _scanText.equals(_resultText) == false || _modelListBank == null) {

            _modelListBank = new MXWrapList<>();

            int selection = -1;

            if (_resultProgram != null) {
                List<CXNode> listBank = _resultProgram.listChildren("Bank");

                for (int i = 0; i < listBank.size(); ++i) {
                    CXNode bankSeek = listBank.get(i);
                    String lsb = bankSeek._listAttributes.valueOfName("LSB");
                    String msb = bankSeek._listAttributes.valueOfName("MSB");
                    String name = bankSeek._listAttributes.valueOfName("Name");

                    _modelListBank.addNameAndValue(msb + ":" + lsb + ", " + name, bankSeek);
                    int msbNum = MXUtil.numberFromText(msb, -1);
                    int lsbNum = MXUtil.numberFromText(lsb, -1);
                    if (listBank.size() == 1 || (msbNum == _scanBankMSB && lsbNum == _scanBankLSB)) {
                        _resultBank = bankSeek;
                        selection = i;
                    }
                }
            }

            _internalChange = true;
            jListBank.setModel(_modelListBank);
            jListBank.setSelectedIndex(selection);
            _internalChange = false;
        }
        sendProgramChange();
    }

    public void scan(int xml, int program, int msb, int lsb) {
        if (_listXMLFile.isEmpty()) {
            return;
        }
        _scanText = "";
        CXFile file = _listXMLFile.get(xml);
        _scanXMLFile = file;
        //TODO
    }

    public void sendProgramChange() {
        MXWrap<MXReceiver> receiverObj = (MXWrap) jComboBoxTestReceiver.getSelectedItem();
        MXWrap<Integer> portObj = (MXWrap) jComboBoxTestPort.getSelectedItem();
        MXWrap<Integer> channelObj = (MXWrap) jComboBoxTestChannel.getSelectedItem();

        if (_listReceiver == null) {
            return;
        }
        MXReceiver receiver = _listReceiver.readCombobox(jComboBoxTestReceiver);
        if (receiver == null) {
            return;
        }
        int port = portObj.value;
        int channel = channelObj.value;

        int index = jListBank.getSelectedIndex();
        if (index < 0) {
            return;
        }

        if (_resultBank == null) {
            return;
        }

        String bankMSB = _resultBank._listAttributes.valueOfName("MSB");
        String bankLSB = _resultBank._listAttributes.valueOfName("LSB");
        String bankName = _resultBank._listAttributes.valueOfName("NAME");

        int msb = MXUtil.numberFromText(bankMSB, -1);
        int lsb = MXUtil.numberFromText(bankLSB, -1);

        if (msb > 0 && lsb > 0) {
            sendMessageToReceiver(MXMidi.COMMAND_CONTROLCHANGE, MXMidi.DATA1_CC_BANKSELECT, msb);
            sendMessageToReceiver(MXMidi.COMMAND_CONTROLCHANGE, MXMidi.DATA1_CC_BANKSELECT + 32, lsb);
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String pcPC = _resultProgram._listAttributes.valueOfName("PC");
        String pcName = _resultProgram._listAttributes.valueOfName("Name");

        int pc = MXUtil.numberFromText(pcPC, -1);

        if (pc >= 1 && pc <= 128) {
            jLabelBankProgram.setText(pcPC + ": " + pcName);
            sendMessageToReceiver(MXMidi.COMMAND_PROGRAMCHANGE, pc - 1, 0);
        }
    }

    public void sendMessageToReceiver(int command, int data1, int data2) {
        MXWrap<MXReceiver> receiverObj = (MXWrap) jComboBoxTestReceiver.getSelectedItem();
        MXWrap<Integer> portObj = (MXWrap) jComboBoxTestPort.getSelectedItem();
        MXWrap<Integer> channelObj = (MXWrap) jComboBoxTestChannel.getSelectedItem();

        MXReceiver receiver = _listReceiver.readCombobox(jComboBoxTestReceiver);
        int port = portObj.value;
        int channel = channelObj.value;

        int state = command + channel;

        MXMessage message = MXMessageFactory.fromShortMessage(port, state, data1, data2);
        MXMain.getMain().messageDispatch(message, receiver);
    }
}
