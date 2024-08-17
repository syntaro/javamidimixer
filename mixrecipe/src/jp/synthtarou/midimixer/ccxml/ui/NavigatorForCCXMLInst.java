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
package jp.synthtarou.midimixer.ccxml.ui;

import java.awt.Container;
import jp.synthtarou.midimixer.ccxml.xml.CXXMLManager;
import jp.synthtarou.midimixer.ccxml.xml.CXNode;
import jp.synthtarou.midimixer.ccxml.xml.CXFile;
import jp.synthtarou.midimixer.ccxml.rules.CCRuleManager;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.namedobject.MXNamedObject;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.libs.MXCountdownTimer;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.libs.midi.port.FinalMIDIOut;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.libs.namedobject.MXNamedObjectListFactory;
import jp.synthtarou.libs.navigator.legacy.INavigator;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIInManager;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOutManager;
import jp.synthtarou.midimixer.mx00playlist.MXPianoKeys;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class NavigatorForCCXMLInst extends javax.swing.JPanel implements INavigator<CCXMLInst> {

    public static void main(String[] args) {
        NavigatorForCCXMLInst editor = new NavigatorForCCXMLInst();
        MXUtil.showAsDialog(null, editor, "Test");
        MXMIDIInManager.getManager().readJSonfile(null);
        MXMIDIOutManager.getManager().readJSonfile(null);
        System.exit(0);
    }

    public boolean simpleAsk(Container parent) {
        MXUtil.showAsDialog(parent, this, INavigator.DEFAULT_TITLE);
        if (getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
            return true;
        }
        return false;
    }

    /**
     * Creates new form NavigatorForCCXMLInst
     */
    public NavigatorForCCXMLInst() {
        this(null);
    }

    public NavigatorForCCXMLInst(CXFile file) {
        initComponents();

        jComboBoxTestPort.setModel(MXNamedObjectListFactory.listupPort(null));
        jComboBoxTestChannel.setModel(MXNamedObjectListFactory.listupChannel(null));
        _listXMLFile = CXXMLManager.getInstance().listLoaded();

        updateXMLComboModel();

        _piano = new MXPianoKeys();
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
        _piano.updateNoteGraphics(0, 11);
        _piano.setAutoScanSize(true);

        _group1 = new ButtonGroup();
        _group1.add(jRadioButtonVel1);
        _group1.add(jRadioButtonVel2);
        _group1.add(jRadioButtonVel3);
        _group1.add(jRadioButtonVel4);

        jRadioButtonVel3.setSelected(true);
        _stopFeedback--;
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

    int _returnStatus;
    CCXMLInst _returnValue;

    ButtonGroup _group1;

    int _testPort;
    int _testChannel;

    ArrayList<CXFile> _listXMLFile;
    MXNamedObjectList<CXFile> _modelListXML;
    MXNamedObjectList<CXNode> _modelListModule;
    MXNamedObjectList<CXNode> _modelListMap;
    MXNamedObjectList<CXNode> _modelListProgram;
    MXNamedObjectList<CXNode> _modelListBank;

    MXPianoKeys _piano;

    MXNamedObjectList<MXReceiver> _listReceiver;

    CXFile _selectedXMLFile = null;
    CXNode _selectedModule = null;
    CXNode _selectedMap = null;
    CXNode _selectedProgram = null;
    CXNode _selectedBank = null;
    String _resultText = "";

    @Override
    public int getNavigatorType() {
        return INavigator.TYPE_EDITOR;
    }

    @Override
    public int getReturnStatus() {
        return _returnStatus;
    }

    @Override
    public CCXMLInst getReturnValue() {
        return _returnValue;
    }

    @Override
    public boolean isNavigatorRemovable() {
        return false;
    }

    @Override
    public boolean validateWithNavigator(CCXMLInst result) {
        return true;
    }

    @Override
    public JPanel getNavigatorPanel() {
        return this;
    }

    class MyHandler implements MXPianoKeys.MXMouseHandler {

        @Override
        public void noteOn(int note) {
            int data1 = note;
            int data2 = getVelocity();
            MXMessage message = MXMessageFactory.fromNoteon(getPort(), getChannel(), data1, data2);
            MXMIDIIn.messageToReceiverThreaded(message, getReceiver());
        }

        @Override
        public void noteOff(int note) {
            int data1 = note;
            MXMessage message = MXMessageFactory.fromNoteoff(getPort(), getChannel(), data1);
            MXMIDIIn.messageToReceiverThreaded(message, getReceiver());
        }

        @Override
        public void selectionChanged() {
        }
    }

    long _timeToSearch = 0;

    public void textChanged() {
        _timeToSearch = System.currentTimeMillis() + 300;
        MXCountdownTimer.letsCountdown(300, () -> {
            if (System.currentTimeMillis() >= _timeToSearch) {
                applyFilter(jTextFieldSearch.getText());
            }
        });
    }

    public void applyFilter(String text) {
        //TODO
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
        jButtonClose = new javax.swing.JButton();

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

        jComboBoxXML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxXMLActionPerformed(evt);
            }
        });
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

        jComboBoxModule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxModuleActionPerformed(evt);
            }
        });
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

        jButtonClose.setText("Close");
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButtonClose, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxTestChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTestChannelActionPerformed

    }//GEN-LAST:event_jComboBoxTestChannelActionPerformed

    private void jListProgramValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListProgramValueChanged
        if (_stopFeedback > 0) {
            return;
        }
        int index = jListProgram.getSelectedIndex();
        if (index >= 0) {
            CXNode program = _modelListProgram.valueOfIndex(index);
            _selectedProgram = program;
            updateBankListModel(program);
        }
    }//GEN-LAST:event_jListProgramValueChanged

    private void jListBankValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListBankValueChanged
        int index = jListBank.getSelectedIndex();
        if (index >= 0) {
            CXNode bank = _modelListBank.valueOfIndex(index);
            _selectedBank = bank;
            _returnValue = new CCXMLInst();
            _returnValue._bankMSB = MXUtil.numberFromText(_selectedBank._listAttributes.valueOfName("MSB"), -1);
            _returnValue._bankLSB = MXUtil.numberFromText(_selectedBank._listAttributes.valueOfName("LSB"), -1);
            _returnValue._bankName = _selectedBank._listAttributes.valueOfName("NAME");
            _returnValue._programName = _selectedProgram._listAttributes.valueOfName("Name");
            _returnValue._progranNumber = MXUtil.numberFromText(_selectedProgram._listAttributes.valueOfName("PC"), -1);
            sendProgramChange();
        }
    }//GEN-LAST:event_jListBankValueChanged

    private void jComboBoxTestPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTestPortActionPerformed
    }//GEN-LAST:event_jComboBoxTestPortActionPerformed

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed
        _returnStatus = INavigator.RETURN_STATUS_NOTSET;
        if (_returnValue != null) {
            _returnStatus = INavigator.RETURN_STATUS_APPROVED;
        }
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButtonCloseActionPerformed

    private void jListMapValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListMapValueChanged
        if (_stopFeedback > 0) {
            return;
        }
        int sel = jListMap.getSelectedIndex();
        if (sel >= 0) {
            CXNode scanMap = _modelListMap.valueOfIndex(sel);
            updateProgramListModel(scanMap);
        }
    }//GEN-LAST:event_jListMapValueChanged

    private void jComboBoxTestReceiverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTestReceiverActionPerformed

    }//GEN-LAST:event_jComboBoxTestReceiverActionPerformed

    private void jComboBoxXMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxXMLActionPerformed
        int sel = jComboBoxXML.getSelectedIndex();
        if (sel >= 0) {
            String name = _modelListXML.nameOfIndex(sel);
            CXFile file = _modelListXML.valueOfIndex(sel);
            updateModuleComboModel(file);
        }
    }//GEN-LAST:event_jComboBoxXMLActionPerformed

    private void jComboBoxModuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxModuleActionPerformed
        int sel = jComboBoxModule.getSelectedIndex();
        if (sel >= 0) {
            String name = _modelListModule.nameOfIndex(sel);
            CXNode scanModule = _modelListModule.valueOfIndex(sel);
            JOptionPane.showMessageDialog(this, name, "Module", JOptionPane.OK_OPTION);
            updateMapListModel(scanModule);
        }
    }//GEN-LAST:event_jComboBoxModuleActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButtonClose;
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

    int _stopFeedback = 1;

    public void updateXMLComboModel() {
        /* XML一覧を更新し、_scanXMLFileを選択する */
        _modelListXML = new MXNamedObjectList();
        try {
            _stopFeedback++;
            _modelListXML = new MXNamedObjectList<>();
            _modelListModule = null;

            // 一覧を更新する
            for (int i = 0; i < _listXMLFile.size(); ++i) {
                CXFile xmlSeek = _listXMLFile.get(i);
                _modelListXML.addNameAndValue(xmlSeek._file.getName(), xmlSeek);
            }
            jComboBoxXML.setModel(_modelListXML);
            _selectedXMLFile = _modelListXML.valueOfIndex(0);
            updateModuleComboModel(_selectedXMLFile);
        } finally {
            _stopFeedback--;
        }
    }

    public void updateModuleComboModel(CXFile scanFile) {
        try {
            _stopFeedback++;
            _modelListModule = new MXNamedObjectList<>();
            CCRuleManager rule = CCRuleManager.getInstance();
            List<CXNode> moduleData = scanFile._document.listChildren(rule.moduleData);
            int scanModule = -1;
            if (moduleData != null) {
                for (int seek = 0; seek < moduleData.size(); ++seek) {
                    CXNode moduleSeek = moduleData.get(seek);
                    _modelListModule.addNameAndValue(moduleSeek._listAttributes.valueOfName("Name"), moduleSeek);
                    if (_selectedModule == moduleSeek) {
                        scanModule = seek;
                    }
                }
            }
            if (scanModule < 0 && _modelListModule.size() > 0) {
                scanModule = 0;
            }
            jComboBoxModule.setModel(_modelListModule);
            if (scanModule >= 0) {
                _selectedModule = _modelListModule.valueOfIndex(scanModule);
                jComboBoxModule.setSelectedIndex(scanModule);
                updateMapListModel(_selectedModule);
            }
        } finally {
            _stopFeedback--;
        }
    }

    public void updateMapListModel(CXNode scanModule) {
        try {
            _stopFeedback++;
            _modelListMap = new MXNamedObjectList<>();
            int selection = -1;

            // 一覧を更新する
            CCRuleManager rule = CCRuleManager.getInstance();
            List<CXNode> instrumentList = scanModule.listChildren(rule.instrumentList);
            int scanMap = -1;
            if (instrumentList != null && instrumentList.size() > 0) {
                List<CXNode> mapList = instrumentList.get(0).listChildren(rule.instrumentList_map);
                for (int seek = 0; seek < mapList.size(); ++seek) {
                    CXNode seekMap = mapList.get(seek);
                    _modelListMap.addNameAndValue(seekMap._listAttributes.valueOfName("Name"), seekMap);
                    if (_selectedMap == seekMap) {
                        scanMap = seek;
                    }
                }
            }
            if (scanMap < 0 && _modelListMap.size() > 0) {
                scanMap = 0;
            }
            jListMap.setModel(_modelListMap);
            if (scanMap >= 0) {
                _selectedProgram = _modelListMap.valueOfIndex(scanMap);
                jListMap.setSelectedIndex(scanMap);
                updateProgramListModel(_selectedProgram);
            }
        } finally {
            _stopFeedback--;
        }
    }

    public void updateProgramListModel(CXNode scanMap) {
        try {
            _stopFeedback++;
            _modelListProgram = new MXNamedObjectList<>();
            
            int scanProgram = -1;
            String selPC = "";
            if (_selectedProgram != null) {
                _selectedProgram._listAttributes.valueOfName("PC");
            }

            List<CXNode> PCList = scanMap.listChildren("PC");
            for (int i = 0; i < PCList.size(); ++i) {
                CXNode programSeek = PCList.get(i);
                String pc = programSeek._listAttributes.valueOfName("PC");
                String name = programSeek._listAttributes.valueOfName("Name");
                
                if (_selectedProgram != null) {
                    if (pc.equals(selPC)) {
                        scanProgram = i;
                    }
                }

                _modelListProgram.addNameAndValue(pc + ". " + name, programSeek);
            }
            if (scanProgram < 0 && _modelListProgram.size() > 0) {
                scanProgram = 0;
            }
            jListProgram.setModel(_modelListProgram);
            if (scanProgram >= 0) {
                _selectedBank = _modelListProgram.valueOfIndex(scanProgram);
                jListProgram.setSelectedIndex(scanProgram);
                updateBankListModel(_selectedBank);
            }
        } finally {
            _stopFeedback--;
        }
    }

    public void updateBankListModel(CXNode scanProgram) {
        try {
            _stopFeedback++;
            _modelListBank = new MXNamedObjectList<>();
            int selection = -1;

            List<CXNode> listBank = scanProgram.listChildren("Bank");
            int scanBank = -1;
            String selMSB = "", selLSB =  "";
            if (_selectedBank != null) {
                selMSB = _selectedBank._listAttributes.valueOfName("MSB");
                selLSB = _selectedBank._listAttributes.valueOfName("LSB");
            }
            for (int i = 0; i < listBank.size(); ++i) {
                CXNode bankSeek = listBank.get(i);
                String msb = bankSeek._listAttributes.valueOfName("MSB");
                String lsb = bankSeek._listAttributes.valueOfName("LSB");
                String name = bankSeek._listAttributes.valueOfName("Name");

                if (_selectedBank != null) {
                    if (msb.equals(selMSB) && lsb.equals(selLSB)) {
                        scanBank = i;
                    }
                }

                _modelListBank.addNameAndValue(msb + ":" + lsb + ", " + name, bankSeek);
            }

            if (scanBank < 0 && _modelListBank.size() > 0) {
                scanBank = 0;
            }
            jListBank.setModel(_modelListBank);
            if (scanBank >= 0) {
                _selectedBank = _modelListBank.valueOfIndex(scanBank);
                jListBank.setSelectedIndex(scanBank);
            }
        } finally {
            _stopFeedback--;
        }
    }

    public void sendProgramChange() {
        MXNamedObject<MXReceiver> receiverObj = (MXNamedObject) jComboBoxTestReceiver.getSelectedItem();
        MXNamedObject<Integer> portObj = (MXNamedObject) jComboBoxTestPort.getSelectedItem();
        MXNamedObject<Integer> channelObj = (MXNamedObject) jComboBoxTestChannel.getSelectedItem();

        if (_listReceiver == null) {
            return;
        }
        MXReceiver receiver = _listReceiver.readComboBox(jComboBoxTestReceiver);
        if (receiver == null) {
            return;
        }
        int port = portObj._value;
        int channel = channelObj._value;

        int index = jListBank.getSelectedIndex();
        if (index < 0) {
            return;
        }

        if (_returnValue == null) {
            return;
        }

        int pc = _returnValue._progranNumber;

        if (pc >= 1 && pc <= 128) {
            jLabelBankProgram.setText(pc + ": " + _returnValue._programName);

            int data1 = pc - 1;
            MXMessage message = MXMessageFactory.fromProgramChange(getPort(), getChannel(), data1);
            System.out.println("sending " + message + " to " + getReceiver());
            MXMIDIIn.messageToReceiverThreaded(message, getReceiver());
        }
        int msb = _returnValue._bankMSB;
        int lsb = _returnValue._bankLSB;

        if (msb >= 0 && lsb >= 0) {
            int data1 = MXMidi.DATA1_CC_BANKSELECT;
            int data2 = 0;
            MXMessage message = MXMessageFactory.fromControlChange14(getPort(), getChannel(), data1, msb, lsb);
            MXMIDIIn.messageToReceiverThreaded(message, getReceiver());
            System.out.println("sending " + message + " to " + getReceiver());
        }

    }

    int getPort() {
        MXNamedObject<Integer> portObj = (MXNamedObject) jComboBoxTestPort.getSelectedItem();
        int port = portObj._value;
        return port;
    }

    int getChannel() {
        MXNamedObject<Integer> channelObj = (MXNamedObject) jComboBoxTestChannel.getSelectedItem();
        int channel = channelObj._value;
        return channel;
    }

    MXReceiver getReceiver() {
        MXNamedObject<MXReceiver> receiverObj = (MXNamedObject) jComboBoxTestReceiver.getSelectedItem();
        MXReceiver receiver = _listReceiver.readComboBox(jComboBoxTestReceiver);
        return receiver;
    }
    
    public void scan(int map, int program, int bankMSB, int bankLSB) {
        
    }
}
