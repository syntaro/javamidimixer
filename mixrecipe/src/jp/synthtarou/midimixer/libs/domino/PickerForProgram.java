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
package jp.synthtarou.midimixer.libs.domino;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
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
import jp.synthtarou.midimixer.libs.swing.MXModalFrame;
import jp.synthtarou.midimixer.libs.swing.MXSwingPiano;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class PickerForProgram extends javax.swing.JPanel {

    public static void main(String[] args) {
        PickerForProgram picker = new PickerForProgram();
        MXModalFrame.showAsDialog(null, picker, "Test");
        System.exit(0);
    }

    ArrayList<CCXMLFile> _listRoot = CCXMLManager.getInstance()._listLoaded;
    MXWrapList<CCXMLFile> _xmlList;

    /**
     * Creates new form MXProgramPicker
     */
    public PickerForProgram() {
        initComponents();

        jComboBoxTestPort.setModel(MXMidi.listupPortAssigned(false));
        jComboBoxTestChannel.setModel(MXMidi.listupChannel(false));
        _xmlList = new MXWrapList();
        for (CCXMLFile xml : _listRoot) {
            _xmlList.addNameAndValue(xml._file.getName(), xml);
        }
        jComboBoxMap.setModel(_xmlList);
        if (_xmlList.size() > 0) {
            jComboBoxMap.setSelectedIndex(0);
            xmlSelected(_xmlList.readCombobox(jComboBoxMap));
        }

        setPreferredSize(new Dimension(600, 600));
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

        _receiver = FinalMIDIOut.getInstance();
        _listReceiver = MXMain.getMain().getReceiverList();
        _listReceiver.writeComboBox(jComboBoxTestReceiver, _receiver);
    }

    String _searchText;
    int _testPort;
    int _testChannel;

    MXWrapList<CCXMLNode> _modelMapList;
    CCXMLNode _selectedMap;

    MXWrapList<CCXMLNode> _modelProgramList;
    CCXMLNode _selectedProgram;

    MXWrapList<CCXMLNode> _modelBankList;
    CCXMLNode _lastChoice;
    CCXMLNode _programBank;

    MXSwingPiano _piano;

    MXReceiver _receiver;
    MXWrapList<MXReceiver> _listReceiver;

    public int _returnProgram = -1;
    public int _returnBankMSB = -1;
    public int _returnBankLSB = -1;

    public void setDefault(int prog, int msb, int lsb) {
        jComboBoxCategory.setSelectedIndex(0);
        fillProgramList();
        int hit = -1;
        for (int i = 0; i < _modelProgramList.size(); i++) {
            CCXMLNode dbProg = _modelProgramList.get(i).value;
            int pc = dbProg._listAttributes.numberOfName("PC", -1);
            if (pc == prog) {
                hit = i;
                break;
            }
        }
        if (hit >= 0) {
            jListProgram.setSelectedIndex(hit);
            fillBankList();
            hit = -1;
            for (int i = 0; i < _modelBankList.size(); ++i) {
                CCXMLNode dbBank = _modelBankList.get(i).value;
                int msb2 = dbBank._listAttributes.numberOfName("MSB", -1);
                int lsb2 = dbBank._listAttributes.numberOfName("LSB", -1);
                if (msb == msb2 && lsb == lsb2) {
                    hit = i;
                    break;
                }
            }
            if (hit >= 0) {
                jListBank.setSelectedIndex(hit);
            }
        }
    }

    public void xmlSelected(CCXMLFile root) {
        _modelMapList = new MXWrapList();
        _modelProgramList = new MXWrapList();
        _modelBankList = new MXWrapList();
        if (root == null) {
            jComboBoxCategory.setModel(_modelMapList);
            fillProgramList();
            updateUISelection(true);
            return;
        }

        for (CCXMLNode module : root._document.listModule()) {
            for (CCXMLNode instrumentList : module.listChildren("InstrumentList")) {
                for (CCXMLNode map : instrumentList.listChildren("Map")) {
                    if (map._listChildTags.size() > 0) {
                        String name = map._listAttributes.valueOfName("Name");
                        _modelMapList.addNameAndValue(name, map);
                        if (_selectedMap == null) {
                            _selectedMap = map;
                        }
                    }
                }
            }
        }

        jComboBoxCategory.setModel(_modelMapList);
        fillProgramList();
        updateUISelection(true);
    }

    class MyHandler implements MXSwingPiano.Handler {

        @Override
        public void noteOn(int note) {
            MXWrap<Integer> portObj = (MXWrap) jComboBoxTestPort.getSelectedItem();
            MXWrap<Integer> channelObj = (MXWrap) jComboBoxTestChannel.getSelectedItem();

            int port = portObj.value;
            int channel = channelObj.value;

            MXMessage message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_NOTEON + channel, note, 100);
            MXMain.getMain().messageDispatch(message, FinalMIDIOut.getInstance());
        }

        @Override
        public void noteOff(int note) {
            MXWrap<Integer> portObj = (MXWrap) jComboBoxTestPort.getSelectedItem();
            MXWrap<Integer> channelObj = (MXWrap) jComboBoxTestChannel.getSelectedItem();

            int port = portObj.value;
            int channel = channelObj.value;

            MXMessage message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_NOTEOFF + channel, note, 0);
            MXMain.getMain().messageDispatch(message, FinalMIDIOut.getInstance());
        }

        @Override
        public void selectionChanged() {
        }
    }

    long reserveSearch = 0;

    public void textChanged() {
        if (jComboBoxCategory.getSelectedIndex() != 0) {
            jComboBoxCategory.setSelectedIndex(0);
        }

        reserveSearch = System.currentTimeMillis() + 500;
        MXGlobalTimer.letsCountdown(500, new Runnable() {
            public void run() {
                if (System.currentTimeMillis() >= reserveSearch) {
                    invokeSearch();
                }
            }
        });
    }

    public void invokeSearch() {
        _searchText = jTextFieldSearch.getText();
        fillProgramList();
        fillBankList();
    }

    public void updateUISelection(boolean defaultSelection) {
        int progSel = _modelProgramList.indexOfValue(_selectedProgram);
        int bankSel = _modelBankList.indexOfValue(_lastChoice);

        if (defaultSelection) {
            if (progSel < 0) {
                progSel = 0;
            }
            if (bankSel < 0) {
                bankSel = 0;
            }
        }

        jListProgram.setSelectedIndex(progSel);
        jListBank.setSelectedIndex(bankSel);
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
        jPanel1 = new javax.swing.JPanel();
        jTextFieldSearch = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxCategory = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListProgram = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListBank = new javax.swing.JList<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jComboBoxMap = new javax.swing.JComboBox<>();

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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 7;
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
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jComboBoxTestChannel, gridBagConstraints);

        jLabel5.setText("Port");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jLabel5, gridBagConstraints);

        jComboBoxTestPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTestPortActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jComboBoxTestPort, gridBagConstraints);

        jLabel6.setText("Channel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jLabel6, gridBagConstraints);

        jLabelBankProgram.setText("Piano -");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jLabelBankProgram, gridBagConstraints);

        jLabel7.setText("Test ON");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jLabel7, gridBagConstraints);

        jComboBoxTestReceiver.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxTestReceiverItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jComboBoxTestReceiver, gridBagConstraints);

        jButtonSelect.setText("OK");
        jButtonSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel2.add(jButtonSelect, gridBagConstraints);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jButtonCancel, gridBagConstraints);

        jSplitPane1.setLeftComponent(jPanel2);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Tone"));
        jPanel1.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jTextFieldSearch, gridBagConstraints);

        jLabel1.setText("Map");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel1, gridBagConstraints);

        jComboBoxCategory.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxCategoryItemStateChanged(evt);
            }
        });
        jComboBoxCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCategoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jComboBoxCategory, gridBagConstraints);

        jLabel2.setText("Program");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel2, gridBagConstraints);

        jListProgram.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListProgramValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListProgram);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jLabel3.setText("Bank");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabel3, gridBagConstraints);

        jListBank.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListBankValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jListBank);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
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

        jComboBoxMap.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxMapItemStateChanged(evt);
            }
        });
        jComboBoxMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxMapActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jComboBoxMap, gridBagConstraints);

        jSplitPane1.setRightComponent(jPanel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSplitPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxTestChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTestChannelActionPerformed
        _programBank = _lastChoice;
    }//GEN-LAST:event_jComboBoxTestChannelActionPerformed

    private void jComboBoxCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCategoryActionPerformed
        _selectedMap = _modelMapList.readCombobox(jComboBoxCategory);

        fillProgramList();
        updateUISelection(true);
    }//GEN-LAST:event_jComboBoxCategoryActionPerformed

    public void fillProgramList() {
        _modelProgramList = new MXWrapList();
        if (_searchText != null && _searchText.isBlank() == false) {
            List<CCXMLNode> searchTemp = new ArrayList();
            for (CCXMLNode map : _modelMapList.valueList()) {
                for (CCXMLNode prog : map.listChildren("PC")) {
                    boolean hit = false;
                    if (MXUtil.searchTextIgnoreCase(prog.getName(), _searchText)) {
                        hit = true;
                    } else {
                        for (CCXMLNode bank : prog.listChildren("Bank")) {
                            String name = bank._listAttributes.valueOfName("Name");
                            if (MXUtil.searchTextIgnoreCase(name, _searchText)) {
                                hit = true;
                                break;
                            }
                        }
                    }
                    if (hit) {
                        searchTemp.add(prog);
                    }
                }
            }

            for (CCXMLNode prog : searchTemp) {
                int pc = prog._listAttributes.numberOfName("PC", -1);
                String name = prog._listAttributes.valueOfName("Name");
                _modelProgramList.addNameAndValue(pc + "." + name, prog);
            }
        } else {
            _selectedMap = _modelMapList.readCombobox(jComboBoxCategory);
            if (_selectedMap != null) {
                List<CCXMLNode> listPC = _selectedMap.listChildren("PC");
                if (listPC != null) {
                    for (CCXMLNode prog : listPC) {
                        String pc = prog._listAttributes.valueOfName("PC");
                        String name = prog._listAttributes.valueOfName("Name");
                        _modelProgramList.addNameAndValue(pc + "." + name, prog);
                    }
                }
            }
        }
        jListProgram.setModel(_modelProgramList);
    }

    private void jListProgramValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListProgramValueChanged
        int index = jListProgram.getSelectedIndex();
        if (index < 0) {
            _selectedProgram = null;
        } else {
            _selectedProgram = _modelProgramList.get(index).value;
        }
        fillBankList();
        updateUISelection(true);
    }//GEN-LAST:event_jListProgramValueChanged

    private void jListBankValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListBankValueChanged
        int index = this.jListBank.getSelectedIndex();
        if (index < 0) {
            if (_modelBankList.size() > 0) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jListBank.setSelectedIndex(0);
                    }
                });
            }
            return;
        } else {
            _lastChoice = _modelBankList.get(index).value;
        }
        _programBank = _lastChoice;
        if (_lastChoice != null) {
            StringBuffer text = new StringBuffer();

            String msbText = _lastChoice._listAttributes.valueOfName("MSB");
            String lsbText = _lastChoice._listAttributes.valueOfName("LSB");
            String idText = _lastChoice._listAttributes.valueOfName("ID");

            int msb = MXUtil.numberFromText(msbText, -1);
            int lsb = MXUtil.numberFromText(lsbText, -1);

            text.append(idText);
            if (msb >= 0 && lsb >= 0) {
                text.append("(BANK:" + MXUtil.toHexFF(msb) + ":" + MXUtil.toHexFF(lsb) + ")");
            }
            text.append(_lastChoice.getName());
            jLabelBankProgram.setText(text.toString());

            MXWrap<Integer> portObj = (MXWrap) jComboBoxTestPort.getSelectedItem();
            MXWrap<Integer> channelObj = (MXWrap) jComboBoxTestChannel.getSelectedItem();

            int port = portObj.value;
            int channel = channelObj.value;

            String msbChanged = _programBank._listAttributes.valueOfName("MSB");
            String lsbChanged = _programBank._listAttributes.valueOfName("LSB");
            String idChanged = _programBank._listAttributes.valueOfName("ID");

            int intMSBChanged = MXUtil.numberFromText(msbChanged, -1);
            int intLSBChanged = MXUtil.numberFromText(lsbChanged, -1);

            if (intMSBChanged >= 0 && intLSBChanged >= 0) {
                MXMessage bank1 = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CONTROLCHANGE + channel, MXMidi.DATA1_CC_BANKSELECT, intMSBChanged);
                MXMain.getMain().messageDispatch(bank1, FinalMIDIOut.getInstance());
                MXMessage bank2 = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CONTROLCHANGE + channel, MXMidi.DATA1_CC_BANKSELECT + 32, intLSBChanged);
                MXMain.getMain().messageDispatch(bank2, FinalMIDIOut.getInstance());
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            MXMessage prog = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_PROGRAMCHANGE + channel, 0, 0);
            prog.setGate(_programBank._parent._listAttributes.numberOfName("PC", 0));

            MXMain.getMain().messageDispatch(prog, FinalMIDIOut.getInstance());
        }
    }//GEN-LAST:event_jListBankValueChanged

    private void jComboBoxTestPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTestPortActionPerformed
        _programBank = _lastChoice;
    }//GEN-LAST:event_jComboBoxTestPortActionPerformed

    private void jButtonSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectActionPerformed
        _returnProgram = _selectedProgram._listAttributes.numberOfName("PC", -1);

        String msbLast = _lastChoice._listAttributes.valueOfName("MSB");
        String lsbLast = _lastChoice._listAttributes.valueOfName("LSB");
        int intMSBLast = MXUtil.numberFromText(msbLast, -1);
        int intLSBLast = MXUtil.numberFromText(lsbLast, -1);

        _returnBankMSB = intMSBLast;
        _returnBankLSB = intLSBLast;
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButtonSelectActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        _returnProgram = -1;
        _returnBankMSB = -1;
        _returnBankLSB = -1;
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jComboBoxMapItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxMapItemStateChanged
        xmlSelected(_xmlList.readCombobox(jComboBoxMap));
    }//GEN-LAST:event_jComboBoxMapItemStateChanged

    private void jComboBoxMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxMapActionPerformed
    }//GEN-LAST:event_jComboBoxMapActionPerformed

    private void jComboBoxCategoryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxCategoryItemStateChanged
    }//GEN-LAST:event_jComboBoxCategoryItemStateChanged

    private void jComboBoxTestReceiverItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxTestReceiverItemStateChanged
        _receiver = this._listReceiver.readCombobox(jComboBoxTestReceiver);
    }//GEN-LAST:event_jComboBoxTestReceiverItemStateChanged

    public void fillBankList() {
        int sel = jListProgram.getSelectedIndex();
        if (sel >= 0) {
            _selectedProgram = _modelProgramList.get(sel).value;
        } else {
            _selectedProgram = null;
        }
        _modelBankList = new MXWrapList();
        if (_selectedProgram != null) {
            for (CCXMLNode bank : _selectedProgram.listChildren("Bank")) {
                String name = bank._listAttributes.valueOfName("Name");
                int msb = bank._listAttributes.numberOfName("MSB", -1);
                int lsb = bank._listAttributes.numberOfName("LSB", -1);

                String bankCode = MXUtil.toHexFF(msb) + ":" + MXUtil.toHexFF(lsb);
                if (bankCode.equals("ff:ff")) {
                    bankCode = "-";
                }
                _modelBankList.addNameAndValue(bankCode + "." + name, bank);
            }
        }
        jListBank.setModel(_modelBankList);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonSelect;
    private javax.swing.JComboBox<String> jComboBoxCategory;
    private javax.swing.JComboBox<String> jComboBoxMap;
    private javax.swing.JComboBox<String> jComboBoxTestChannel;
    private javax.swing.JComboBox<String> jComboBoxTestPort;
    private javax.swing.JComboBox<String> jComboBoxTestReceiver;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelBankProgram;
    private javax.swing.JList<String> jListBank;
    private javax.swing.JList<String> jListProgram;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelPiano;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jTextFieldSearch;
    // End of variables declaration//GEN-END:variables
}
