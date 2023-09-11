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
package jp.synthtarou.midimixer.mx30controller;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SpinnerNumberModel;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.mx35cceditor.ccxml.CXNode;
import jp.synthtarou.midimixer.mx35cceditor.ccxml.EditorForControlChange;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.capture.MXMessageCapture;
import jp.synthtarou.midimixer.libs.midi.capture.MXMessageCapturePanel;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.capture.GateInfomation;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;
import jp.synthtarou.midimixer.libs.swing.MXModalFrame;
import jp.synthtarou.midimixer.libs.swing.MXSwingFileChooser;
import jp.synthtarou.midimixer.libs.swing.SafeSpinnerNumberModel;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileFilterListExt;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileList;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.MXSwingFolderBrowser;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGStatusPanel extends javax.swing.JPanel {

    boolean _okOption = false;
    MX32MixerProcess _process;

    MXWrapList<Integer> _channelModel;
    MXWrapList<Integer> _ccGateModel;
    MXWrapList<Integer> _keyGateModel;
    MXWrapList<Integer> _normalGateModel;
    MXWrapList<Integer> _rpnMSBModel;
    MXWrapList<Integer> _rpnLSBModel;

    MXTemplate _templateStartWith;

    protected MGStatus _status;
    /**
     * Creates new form MXUIValueEditPanel
     */
    boolean skipDataExchange = true;
    private ArrayList<String> textValidate = new ArrayList();
    private ArrayList<String> textTemplate = new ArrayList();

    public MGStatusPanel(MX32MixerProcess process, MGStatus status) {

        skipDataExchange = true;
        initComponents();

        _process = process;
        _status = status;
        _templateStartWith = _status.getTemplate();

        jLabelStartWith.setText(_templateStartWith.toString());
        jLabelBlank1.setText("");

        setPreferredSize(new Dimension(900, 700));
        ButtonGroup group = new ButtonGroup();
        group.add(jRadioButtonDrumTypeSame);
        group.add(jRadioButtonDrumTypeNotes);
        group.add(jRadioButtonDrumTypeSong);
        group.add(jRadioButtonDrumTypeJump);
        group.add(jRadioButtonDrumTypeProgram);
        group.add(jRadioButtonDrumTypeJump);

        ButtonGroup group2 = new ButtonGroup();
        group2.add(jRadioButtonDrumProgDec);
        group2.add(jRadioButtonDrumProgInc);
        group2.add(jRadioButtonDrumProgFixed);
        
        if (_status._uiType == MGStatus.TYPE_DRUMPAD) {
            jTabbedPane1.setEnabledAt(1, true);
        } else {
            jTabbedPane1.setEnabledAt(1, false);
        }

        skipDataExchange = false;

        jSpinnerDrumInMin.setModel(new SpinnerNumberModel(1, 0, 127, 1));
        jSpinnerDrumInMax.setModel(new SpinnerNumberModel(127, 0, 127, 1));

        _drumOutPort = MXMidi.listupPortAssigned(false);
        _drumOutChannel = MXMidi.listupChannel(false);

        jComboBoxDrumOutPort.setModel(_drumOutPort);
        jComboBoxDrumOutChannel.setModel(_drumOutChannel);

        writeBufferToPanelSlider();
        writeBufferToPanelDrum();
        readBufferFromPanelSlider();
        readBufferFromPanelDrum();
        disableUnusedOnPanel();
        validateBuffer(false);
    }

    public void writeBufferToPanelSlider() {
        if (skipDataExchange) {
            return;
        }
        skipDataExchange = true;
        try {
            if (_channelModel == null) {
                _channelModel = MXMidi.listupChannel(false);
                _ccGateModel = MXMidi.listupControlChange();
                _keyGateModel = MXMidi.listupNoteNo();
                _normalGateModel = MXMidi.listupGate7Bit();
                _rpnMSBModel = MXMidi.listupXSB();
                _rpnLSBModel = MXMidi.listupXSB();
            }

            if (_status._name == null) {
                _status._name = "";
            }
            if (jTextFieldName.getText().equals(_status._name) == false) {
                jTextFieldName.setText(_status._name);
            }
            if (_status._memo == null) {
                _status._memo = "";
            }
            if (jTextFieldMemo.getText().equals(_status._memo) == false) {
                jTextFieldMemo.setText(_status._memo);
            }

            jTextFieldTextCommand.setText(_status.toTemplateText());
            jLabelStartWith.setText(_templateStartWith.toString());

            _channelModel.writeComboBox(jComboBoxChannel, _status._channel);

            MXMessage message = _status.toMXMessage(null);
            boolean initTurn = true;
            if (jComboBoxGate.getModel() instanceof MXWrapList) {
                initTurn = false;
            }

            jLabelNameDefault.setText("Empty means-> '" + message.toShortString() + "'");

            int command = message.getStatus() & 0xf0;
            int gateValue = _status._gate._var;

            if (command == MXMidi.COMMAND_CHANNELPRESSURE
                    || command == MXMidi.COMMAND_NOTEON
                    || command == MXMidi.COMMAND_NOTEOFF) {
                jComboBoxGate.setModel(_keyGateModel);
                if (initTurn || ((MXWrap<Integer>) jComboBoxGate.getSelectedItem()).value != gateValue) {
                    _keyGateModel.writeComboBox(jComboBoxGate, gateValue);
                }
            } else if (command == MXMidi.COMMAND_CONTROLCHANGE) {
                jComboBoxGate.setModel(_ccGateModel);
                if (initTurn || ((MXWrap<Integer>) jComboBoxGate.getSelectedItem()).value != gateValue) {
                    _ccGateModel.writeComboBox(jComboBoxGate, gateValue);
                }
            } else {
                jComboBoxGate.setModel(_normalGateModel);
                if (initTurn || ((MXWrap<Integer>) jComboBoxGate.getSelectedItem()).value != gateValue) {
                    _normalGateModel.writeComboBox(jComboBoxGate, gateValue);
                }
            }
/*
            jSpinnerOutOnValueFixed.setModel(new SafeSpinnerNumberModel(_status.getSwitchOutOnValueFixed(), 0, 16383, 1));
            jSpinnerOutOffValueFixed.setModel(new SafeSpinnerNumberModel(_status.getSwitchOutOffValueFixed(), 0, 16383, 1));
*/
            jSpinnerMin.setModel(new SafeSpinnerNumberModel(_status._value._min, 0, 128 * 128 - 1, 1));
            jSpinnerMax.setModel(new SafeSpinnerNumberModel(_status._value._max, 0, 128 * 128 - 1, 1));

            _rpnMSBModel.writeComboBox(jComboBoxMSB, _status._dataroomMSB);
            _rpnLSBModel.writeComboBox(jComboBoxLSB, _status._dataroomLSB);
            jCheckBoxCC14bit.setSelected(_status._ccPair14);
            jCheckBoxCustomRange.setSelected(_status.hasCustomRange());
        } finally {
            skipDataExchange = false;
        }

        disableUnusedOnPanel();
        jLabelBlank.setText("");
        updateUI();
    }

    public int validateBuffer(boolean canDialog) {
        if (skipDataExchange) {
            return -1;
        }
        ArrayList<String> result = new ArrayList();
        MGStatus data = _status;

        if (data._name == null) {
            data._name = "";
        }
        if (data._name.length() == 0) {
            //NP result.add("Name is empty. Kontrol will use short name of message.");
        }

        if (data._memo == null) {
            data._memo = "";
        }        
        if (data._memo.length() == 0) {
            //NP result.add("Memo is empty. Thats No Problem.");
        }

        if (data.getTemplate() == null) {
            result.add("TextCommand is empty. Please fill it.");
        } else {
            try {
                MXMessage message = data.toMXMessage(null);
                if (message == null) {
                    result.add("TextCommand [" + data.toTemplateText() + "] is not valid.");
                }
                if (message.isDataentry()) {
                    if (message.getVisitant() == null || message.getVisitant().getDataroomType() == MXVisitant.HAVE_VAL_NOT) {
                        result.add("If you need DATAENTRY. try QuickMenu.");
                    } else if (message.getVisitant() != null && (message.getVisitant().isHaveDataentryRPN() || message.getVisitant().isHaveDataentryNRPN())) {
                        //OK
                    }
                } else if (message.isMessageTypeChannel() && message.isCommand(MXMidi.COMMAND_CONTROLCHANGE) && !message.isDataentry()) {
                    String newText = "@CC #GL #VL";
                    if (data.toTemplateText().equals(newText) == false) {
                        String errorText = "ControlChange's Text Command can be '" + newText + "'";
                        if (canDialog && JOptionPane.showConfirmDialog(this, errorText, "Smart Replace", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            skipDataExchange = true;
                            data.setTemplateAsText(newText, message.getChannel());
                            jTextFieldTextCommand.setText(newText);
                            data._gate = message.getGate();
                            _ccGateModel.writeComboBox(jComboBoxGate, data._gate._var);
                            skipDataExchange = false;
                        } else {
                            result.add(errorText);
                        }
                    }
                }

            } catch (Exception e) {
                result.add("TextCommand [" + data.getTemplate() + "] is not valid.");
            }
        }

        if (data._channel >= 0 && data._channel < 16) {
            //ok
        }

        validateBufferSubDrum(result);

        textValidate = result;
        printValidateResult();

        return result.size();
    }

    public void printValidateResult() {
        StringBuffer str = new StringBuffer();
        if (textValidate.size() == 0) {
            str.append("**Validation All OK**\n");
        } else {
            str.append("**Validation Result**\n");
            for (String line : textValidate) {
                str.append("\n");
                str.append(line);
            }
        }
        if (str.charAt(str.length() - 1) == '\n') {
            str.setLength(str.length() - 1);
        }/*
        jTextAreaValidation.setText(str.toString());*/
    }

    public void printTemplateResult() {
        StringBuffer str = new StringBuffer();
        if (textTemplate.size() == 0) {
        } else {
            boolean firstColumn = true;
            for (String line : textTemplate) {
                if (firstColumn) {
                    str.append("**Template [" + line + "] Need Fill*\n");
                    firstColumn = false;
                } else {
                    str.append(line);
                    str.append("\n");
                }
            }
        }/*
        jTextAreaTemplate.setText(str.toString());*/
    }

    public void validateBufferSubDrum(ArrayList<String> result) {
        if (_status._uiType != MGStatus.TYPE_DRUMPAD) {
            return;
        }
    }

    public void disableUnusedOnPanel() {
        //CheckBoxCC14bit
        if (_status == null) {
            // panel is under construction
            return;
        }
        if (true) {
            boolean sel = _status.hasCustomRange();
            jSpinnerMin.setEnabled(sel);
            jSpinnerMax.setEnabled(sel);
        }

        boolean isdataentry = false;

        MXTemplate temp = _status.getTemplate();
        if (temp.get(0) == MXTemplate.DTEXT_RPN
                || temp.get(0) == MXTemplate.DTEXT_NRPN) {
            isdataentry = true;
        }
        jComboBoxLSB.setEnabled(isdataentry);
        jComboBoxMSB.setEnabled(isdataentry);

        if (_status._uiType != MGStatus.TYPE_DRUMPAD) {
            return;
        }
        int type = getDrumType();

/*
        boolean zoneX = false;
        boolean zoneA = false;
        boolean zoneB = false;
        boolean zoneC = false;
        boolean zoneD = false;
        boolean zoneE = false;

        switch (type) {
            case MGStatus.SWITCH_TYPE_ON:
                zoneX = true;
                zoneA = true;
                break;

            case MGStatus.SWITCH_TYPE_ONOFF:
                zoneX = true;
                zoneA = true;
                zoneB = true;
                zoneC = true;
                break;

            case MGStatus.SWITCH_TYPE_HARMONY:
                zoneX = true;
                zoneA = false;
                zoneB = false;
                zoneC = true;
                zoneD = true;
                zoneE = false;
                break;

            case MGStatus.SWITCH_TYPE_SEQUENCE:
                zoneX = true;
                zoneA = false;
                zoneB = false;
                zoneC = true;
                zoneD = false;
                zoneE = true;
                break;
        }

        int x;

        jSpinnerDrumInMin.setEnabled(zoneX);
        jSpinnerDrumInMax.setEnabled(zoneX);

        jButtonOutputOnEdit.setEnabled(zoneA);
        jComboBoxOutputOnType.setEnabled(zoneA);
        jComboBoxOutputOnValue.setEnabled(zoneA);
        x = _drumOutOnValue.readCombobox(jComboBoxOutputOnValue);
        jSpinnerOutOnValueFixed.setEnabled(zoneA);

        jComboBoxDrumOutPort.setEnabled(zoneA || zoneB || zoneC || zoneD);
        jComboBoxDrumOutChannel.setEnabled(zoneA || zoneB || zoneC || zoneD);

        jButtonOutputOffEdit.setEnabled(zoneB);
        jComboBoxOutputOffType.setEnabled(zoneB);
        jComboBoxOutputOffValue.setEnabled(zoneB);
        x = _drumOutOffValue.readCombobox(jComboBoxOutputOffValue);
        jSpinnerOutOffValueFixed.setEnabled(zoneB && x == MGStatus.SWITCH_OUT_OFF_VALUE_FIXED);

        jCheckBoxDrumInToggle.setEnabled(zoneC);

        jButtonHarmonyEdit.setEnabled(zoneD);
        jTextFieldHarmonyNoteList.setEnabled(zoneD);
        jComboBoxHarmonyVelocityType.setEnabled(zoneD);
        x = _drumHarmonyVelocityType.readCombobox(jComboBoxHarmonyVelocityType);
        jSpinnerHarmonyVelocityFixed.setEnabled(zoneD);

        jTextFieldSequenceFile.setEnabled(zoneE);
        jButtonSequenceFileBrowse.setEnabled(zoneE);
        jCheckBoxSequencerSeekStart.setEnabled(zoneE);
        jCheckBoxSequencerSingleTrack.setEnabled(zoneE);
        jCheckBoxSequencerFilterNote.setEnabled(zoneE);
        */
    }

    MXWrapList<Integer> _drumOutChannel;
    MXWrapList<Integer> _drumOutPort;

    public void writeBufferToPanelDrum() {
        if (skipDataExchange) {
            return;
        }
        if (_status._uiType != MGStatus.TYPE_DRUMPAD) {
            return;
        }

/*
        int switchType = _status._switchType;
        jRadioButtonDrumTypeSame.setSelected(switchType == MGStatus.SWITCH_TYPE_ON);
        jRadioButtonOnOff.setSelected(switchType == MGStatus.SWITCH_TYPE_ONOFF);
        jRadioButtonDrumTypeNotes.setSelected(switchType == MGStatus.SWITCH_TYPE_HARMONY);
        jRadioButtonDrumTypeSong.setSelected(switchType == MGStatus.SWITCH_TYPE_SEQUENCE);

        jSpinnerDrumInMin.setValue(_status.getValueDrumOn()._min);
        jSpinnerDrumInMax.setValue(_status.getValueDrumOn()._max);
        jComboBoxOutputOnType.setSelectedIndex(_drumOutOnType.indexOfValue(_status.getSwitchOutOnType()));
        jComboBoxOutputOnValue.setSelectedIndex(_drumOutOnValue.indexOfValue(_status.getSwitchOutOnTypeOfValue()));
        showsub1();
        jSpinnerOutOnValueFixed.setValue(_status.getSwitchOutOnValueFixed());
        jComboBoxOutputOffType.setSelectedIndex(_drumOutOffType.indexOfValue(_status.getSwitchOutOffType()));
        jComboBoxOutputOffValue.setSelectedIndex(_drumOutOffValue.indexOfValue(_status.getSwitchOutOffTypeOfValue()));
        jSpinnerOutOffValueFixed.setValue(_status.getSwitchOutOffValueFixed());
        jCheckBoxDrumInToggle.setSelected(_status.isSwitchWithToggle());

        jComboBoxHarmonyVelocityType.setSelectedIndex(_drumHarmonyVelocityType.indexOfValue(_status.getSwitchHarmonyVelocityType()));
        jSpinnerHarmonyVelocityFixed.setValue(_status.getSwitchHarmonyVelocityFixed());
        jTextFieldHarmonyNoteList.setText(_status.getSwitchHarmonyNotes());
        jTextFieldSequenceFile.setText(_status.getSwitchSequencerFile());

        _drumOutPort.writeComboBox(jComboBoxDrumOutPort, _status.getSwitchOutPort());
        _drumOutChannel.writeComboBox(jComboBoxDrumOutChannel, _status.getSwitchOutChannel());

        jCheckBoxSequencerSeekStart.setSelected(_status.isSwitchSequenceSeekStart());
        jCheckBoxSequencerSingleTrack.setSelected(_status.isSwitchSequencerToSingltTrack());
        jCheckBoxSequencerFilterNote.setSelected(_status.isSwitchSequencerFilterNote());
        */
    }

    public void readBufferFromPanelSlider() {
        if (skipDataExchange) {
            return;
        }
        _status._name = jTextFieldName.getText();
        _status._memo = jTextFieldMemo.getText();
        _status._channel = (int) _channelModel.readCombobox(this.jComboBoxChannel);
        _status.setTemplateAsText(jTextFieldTextCommand.getText(), _status._channel);
        MXWrap<Integer> x = (MXWrap<Integer>) jComboBoxGate.getSelectedItem();
        _status._gate = RangedValue.new7bit(x.value);

        if (jCheckBoxCustomRange.isSelected()) {
            int min = (Integer) jSpinnerMin.getValue();
            int max = (Integer) jSpinnerMax.getValue();
            _status.setCustomRange(min, max);
        } else {
            _status.resetCustomRange();
        }

        _status._dataroomMSB = _rpnMSBModel.readCombobox(jComboBoxMSB);
        _status._dataroomLSB = _rpnLSBModel.readCombobox(jComboBoxLSB);

        boolean changed = false;
        try {
            _status._dataroomType = MXVisitant.ROOMTYPE_NODATA;
            MXTemplate template = _status.getTemplate();
            MXMessage message = template.buildMessage(_status._port, _status._channel, _status._gate, _status._value);
            skipDataExchange = true;

            if (message.getGate()._var != _status._gate._var) {
                _status._gate = message.getGate();
                changed = true;
            }

            int d = message.getTemplate().get(0);

            if (d == MXTemplate.DTEXT_RPN) {
                _status._dataroomType = MXVisitant.ROOMTYPE_RPN;
                changed = true;
            } else if (d == MXTemplate.DTEXT_NRPN) {
                _status._dataroomType = MXVisitant.ROOMTYPE_NRPN;
                changed = true;
            }
            skipDataExchange = false;

            if (changed) {
                writeBufferToPanelSlider();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        MXMessage msg = _status.toMXMessage(null);
        boolean canHave14bit = false;
        if (msg.isCommand(MXMidi.COMMAND_CONTROLCHANGE)) {
            System.out.println("data1 = " + msg.getData1());
            if (msg.getData1() >= 0 && msg.getData1() < 32) {
                canHave14bit = true;
            }
        }
        if (canHave14bit) {
            _status._ccPair14 = jCheckBoxCC14bit.isSelected();
            jCheckBoxCC14bit.setEnabled(true);
        } else {
            _status._ccPair14 = false;
            jCheckBoxCC14bit.setEnabled(false);
            jCheckBoxCC14bit.setSelected(false);
        }
    }

    public void readBufferFromPanelDrum() {
        if (skipDataExchange) {
            return;
        }
        if (_status._uiType != MGStatus.TYPE_DRUMPAD) {
            return;
        }
        /*
        MGStatus data = _status;
        data.setSwitchType(getDrumType());
        int min = (Integer)jSpinnerDrumInMin.getValue();
        int max = (Integer)jSpinnerDrumInMax.getValue();
        data.setValueDrumOn(new RangedValue((min+max)/2, min, max));
        data.setSwitchOutOnType(_drumOutOnType.readCombobox(jComboBoxOutputOnType));
        data.setSwitchOutOnTypeOfValue(_drumOutOnValue.readCombobox(jComboBoxOutputOnValue));
        data.setSwitchOutOnValueFixed((int) jSpinnerOutOnValueFixed.getValue());
        data.setSwitchOutOffType(_drumOutOffType.readCombobox(jComboBoxOutputOffType));
        data.setSwitchOutOffTypeOfValue(_drumOutOffValue.readCombobox(jComboBoxOutputOffValue));
        data.setSwitchOutOffValueFixed((int) jSpinnerOutOffValueFixed.getValue());
        data.setSwitchWithToggle(jCheckBoxDrumInToggle.isSelected());
        data.setSwitchOutPort(_drumOutPort.readCombobox(jComboBoxDrumOutPort));
        data.setSwitchOutChannel(_drumOutChannel.readCombobox(jComboBoxDrumOutChannel));
        data.setSwitchHarmonyVelocityType(_drumHarmonyVelocityType.readCombobox(jComboBoxHarmonyVelocityType));
        data.setSwitchHarmonyVelocityFixed((int) jSpinnerHarmonyVelocityFixed.getValue());
        data.setSwitchHarmonyNotes(jTextFieldHarmonyNoteList.getText());
        data.setSwitchSequencerFile(jTextFieldSequenceFile.getText());

        data.setSwitchSequenceSeekStart(jCheckBoxSequencerSeekStart.isSelected());
        data.setSwitchSequencerToSingltTrack(jCheckBoxSequencerSingleTrack.isSelected());
        data.setSwitchSequencerFilterNote(jCheckBoxSequencerFilterNote.isSelected());
        */
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldTextCommand = new javax.swing.JTextField();
        jButtonFromBefore = new javax.swing.JButton();
        jButtonFromList = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldMemo = new javax.swing.JTextField();
        jButtonInternalCommand = new javax.swing.JButton();
        jLabelStartWith = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jSpinnerMax = new javax.swing.JSpinner();
        jButtonActionQuickMenu = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSpinnerMin = new javax.swing.JSpinner();
        jComboBoxGate = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jComboBoxChannel = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        jLabelBlank1 = new javax.swing.JLabel();
        jLabelNameDefault = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jComboBoxMSB = new javax.swing.JComboBox<>();
        jComboBoxLSB = new javax.swing.JComboBox<>();
        jLabel23 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel15 = new javax.swing.JLabel();
        jCheckBoxCC14bit = new javax.swing.JCheckBox();
        jLabel29 = new javax.swing.JLabel();
        jCheckBoxCustomRange = new javax.swing.JCheckBox();
        jLabel31 = new javax.swing.JLabel();
        jButtonUpdateCommand = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jCheckBoxDrumInToggle = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        jRadioButtonDrumTypeNotes = new javax.swing.JRadioButton();
        jRadioButtonDrumTypeSong = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        jRadioButtonDrumTypeSame = new javax.swing.JRadioButton();
        jLabel24 = new javax.swing.JLabel();
        jTextFieldHarmonyNoteList = new javax.swing.JTextField();
        jButtonSequenceFileBrowse = new javax.swing.JButton();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jTextFieldSequenceFile = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jComboBoxDrumOutChannel = new javax.swing.JComboBox<>();
        jButtonHarmonyEdit = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jLabelInputText = new javax.swing.JLabel();
        jComboBoxDrumOutPort = new javax.swing.JComboBox<>();
        jCheckBoxSequencerSeekStart = new javax.swing.JCheckBox();
        jCheckBoxSequencerSingleTrack = new javax.swing.JCheckBox();
        jCheckBoxSequencerFilterNote = new javax.swing.JCheckBox();
        jLabel32 = new javax.swing.JLabel();
        jSpinnerDrumInMin = new javax.swing.JSpinner();
        jSpinnerDrumInMax = new javax.swing.JSpinner();
        jSpinnerDrumInMouse = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jRadioButtonDrumTypeCustom = new javax.swing.JRadioButton();
        jRadioButtonDrumTypeJump = new javax.swing.JRadioButton();
        jSeparator7 = new javax.swing.JSeparator();
        jSpinnerDrumOutOnCustom = new javax.swing.JSpinner();
        jSpinnerDrumOutOffCustom = new javax.swing.JSpinner();
        jLabel18 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jCheckBoxDrumOutOnSameAs = new javax.swing.JCheckBox();
        jCheckBoxDrumOutOffSameAs = new javax.swing.JCheckBox();
        jRadioButtonDrumTypeProgram = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        jLabelDrumOutTemplate = new javax.swing.JLabel();
        jButtonDrumOutTemplate = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jSpinnerDrumSliderJump = new javax.swing.JSpinner();
        jRadioButtonDrumProgInc = new javax.swing.JRadioButton();
        jRadioButtonDrumProgDec = new javax.swing.JRadioButton();
        jRadioButtonDrumProgFixed = new javax.swing.JRadioButton();
        jLabel17 = new javax.swing.JLabel();
        jSpinnerDrumProgPC = new javax.swing.JSpinner();
        jSpinnerDurmProgMSB = new javax.swing.JSpinner();
        jSpinnerDrumProgLSB = new javax.swing.JSpinner();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jSeparator8 = new javax.swing.JSeparator();
        jLabel22 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jSliderDrumSliderJump = new javax.swing.JSlider();
        jLabel14 = new javax.swing.JLabel();
        jCheckBoxDontSendOff = new javax.swing.JCheckBox();
        jButtonCancel = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();
        jLabelBlank = new javax.swing.JLabel();

        setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setLayout(new java.awt.GridBagLayout());

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Start was");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jLabel1, gridBagConstraints);

        jLabel3.setText("Command Text");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jLabel3, gridBagConstraints);

        jTextFieldTextCommand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTextCommandActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 3.0;
        jPanel2.add(jTextFieldTextCommand, gridBagConstraints);

        jButtonFromBefore.setText("Reset Command Text To When Start");
        jButtonFromBefore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFromBeforeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jButtonFromBefore, gridBagConstraints);

        jButtonFromList.setText("Domino XML");
        jButtonFromList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFromListActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jButtonFromList, gridBagConstraints);

        jLabel2.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 3.0;
        jPanel2.add(jTextFieldName, gridBagConstraints);

        jLabel7.setText("Memo");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jLabel7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 3.0;
        jPanel2.add(jTextFieldMemo, gridBagConstraints);

        jButtonInternalCommand.setText("From Internal");
        jButtonInternalCommand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInternalCommandActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jButtonInternalCommand, gridBagConstraints);

        jLabelStartWith.setText("F7 00 F0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jLabelStartWith, gridBagConstraints);

        jButton1.setText("From Capture");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jButton1, gridBagConstraints);

        jLabel9.setText("Information");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        jPanel2.add(jLabel9, gridBagConstraints);

        jSpinnerMax.setModel(new javax.swing.SpinnerNumberModel(0, 0, 16383, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jSpinnerMax, gridBagConstraints);

        jButtonActionQuickMenu.setText("Quick Menu");
        jButtonActionQuickMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonActionQuickMenuActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jButtonActionQuickMenu, gridBagConstraints);

        jLabel10.setText("...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jLabel10, gridBagConstraints);

        jLabel4.setText("Channel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jLabel4, gridBagConstraints);

        jSpinnerMin.setModel(new javax.swing.SpinnerNumberModel(0, 0, 16383, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jSpinnerMin, gridBagConstraints);

        jComboBoxGate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxGateActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jComboBoxGate, gridBagConstraints);

        jLabel5.setText("Gate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jLabel5, gridBagConstraints);

        jComboBoxChannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxChannelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jComboBoxChannel, gridBagConstraints);

        jLabel12.setText("I/O Data (Common)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 7;
        jPanel2.add(jLabel12, gridBagConstraints);

        jLabelBlank1.setText("BLANK");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jLabelBlank1, gridBagConstraints);

        jLabelNameDefault.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jLabelNameDefault, gridBagConstraints);
        jPanel2.add(jSeparator5, new java.awt.GridBagConstraints());

        jComboBoxMSB.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxMSBItemStateChanged(evt);
            }
        });
        jComboBoxMSB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxMSBActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jComboBoxMSB, gridBagConstraints);

        jComboBoxLSB.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxLSBItemStateChanged(evt);
            }
        });
        jComboBoxLSB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxLSBActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jComboBoxLSB, gridBagConstraints);

        jLabel23.setText("DataEntry MSB");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jLabel23, gridBagConstraints);

        jLabel33.setText("LSB");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel2.add(jLabel33, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jSeparator6, gridBagConstraints);

        jLabel15.setText("ControlChange");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jLabel15, gridBagConstraints);

        jCheckBoxCC14bit.setText("Enable 14 bit with +32CC (CC: 0 to 31 can pair with #+32)");
        jCheckBoxCC14bit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxCC14bitActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jCheckBoxCC14bit, gridBagConstraints);

        jLabel29.setText("Value Range");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jLabel29, gridBagConstraints);

        jCheckBoxCustomRange.setText("Use Custom");
        jCheckBoxCustomRange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxCustomRangeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        jPanel2.add(jCheckBoxCustomRange, gridBagConstraints);

        jLabel31.setText(" to ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jLabel31, gridBagConstraints);

        jButtonUpdateCommand.setText("UpdateCommand");
        jButtonUpdateCommand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdateCommandActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        jPanel2.add(jButtonUpdateCommand, gridBagConstraints);

        jTabbedPane1.addTab("Input Config", jPanel2);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jCheckBoxDrumInToggle.setText("Toggle Switch");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jCheckBoxDrumInToggle, gridBagConstraints);

        jLabel13.setText("When Input is ON");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel13, gridBagConstraints);

        jRadioButtonDrumTypeNotes.setText("Notes");
        jRadioButtonDrumTypeNotes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDrumTypeNotesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jRadioButtonDrumTypeNotes, gridBagConstraints);

        jRadioButtonDrumTypeSong.setText("Sequence");
        jRadioButtonDrumTypeSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDrumTypeSongActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jRadioButtonDrumTypeSong, gridBagConstraints);

        jLabel6.setText("Output Type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel6, gridBagConstraints);

        jRadioButtonDrumTypeSame.setText("Same CC-Template As Input");
        jRadioButtonDrumTypeSame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDrumTypeSameActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jRadioButtonDrumTypeSame, gridBagConstraints);

        jLabel24.setText("Notes / Sequence");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel24, gridBagConstraints);

        jTextFieldHarmonyNoteList.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 27;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jTextFieldHarmonyNoteList, gridBagConstraints);

        jButtonSequenceFileBrowse.setText("Browse");
        jButtonSequenceFileBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSequenceFileBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jButtonSequenceFileBrowse, gridBagConstraints);

        jLabel26.setText("Out Port / Ch");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel26, gridBagConstraints);

        jLabel27.setText("List Notes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 27;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jLabel27, gridBagConstraints);

        jLabel28.setText("Velocity");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 29;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jLabel28, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jTextFieldSequenceFile, gridBagConstraints);

        jLabel30.setText("SMF File");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jLabel30, gridBagConstraints);

        jComboBoxDrumOutChannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxDrumOutChannelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jComboBoxDrumOutChannel, gridBagConstraints);

        jButtonHarmonyEdit.setText("Edit");
        jButtonHarmonyEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHarmonyEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 27;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jButtonHarmonyEdit, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jSeparator1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 25;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jSeparator2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jSeparator3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jSeparator4, gridBagConstraints);

        jLabelInputText.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabelInputText, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jComboBoxDrumOutPort, gridBagConstraints);

        jCheckBoxSequencerSeekStart.setText("Play Start Timing = 1st Note");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 32;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jCheckBoxSequencerSeekStart, gridBagConstraints);

        jCheckBoxSequencerSingleTrack.setText("Play in Single  Track");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 33;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jCheckBoxSequencerSingleTrack, gridBagConstraints);

        jCheckBoxSequencerFilterNote.setText("Only Play Note+Pitch+Wheel (IgnoreCC)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 34;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jCheckBoxSequencerFilterNote, gridBagConstraints);

        jLabel32.setText("Play Option");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 32;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jLabel32, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jSpinnerDrumInMin, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jSpinnerDrumInMax, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jSpinnerDrumInMouse, gridBagConstraints);

        jLabel11.setText("Mouse Clicked - ON");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel11, gridBagConstraints);

        jRadioButtonDrumTypeCustom.setText("Custom CC-Template");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jRadioButtonDrumTypeCustom, gridBagConstraints);

        jRadioButtonDrumTypeJump.setText("Same Column Slider Jump");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jRadioButtonDrumTypeJump, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jSeparator7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jSpinnerDrumOutOnCustom, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jSpinnerDrumOutOffCustom, gridBagConstraints);

        jLabel18.setText("Out Off Value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel18, gridBagConstraints);

        jLabel34.setText("Out On Value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel34, gridBagConstraints);

        jCheckBoxDrumOutOnSameAs.setText("Same as Input");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jCheckBoxDrumOutOnSameAs, gridBagConstraints);

        jCheckBoxDrumOutOffSameAs.setText("Same as Input");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jCheckBoxDrumOutOffSameAs, gridBagConstraints);

        jRadioButtonDrumTypeProgram.setText("Program");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jRadioButtonDrumTypeProgram, gridBagConstraints);

        jLabel8.setText("CC Template");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel8, gridBagConstraints);

        jLabelDrumOutTemplate.setText("-----------------");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jLabelDrumOutTemplate, gridBagConstraints);

        jButtonDrumOutTemplate.setText("From List");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 14;
        jPanel3.add(jButtonDrumOutTemplate, gridBagConstraints);

        jLabel16.setText("Slider Jump");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel16, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jSpinnerDrumSliderJump, gridBagConstraints);

        jRadioButtonDrumProgInc.setText("INC");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jRadioButtonDrumProgInc, gridBagConstraints);

        jRadioButtonDrumProgDec.setText("DEC");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jRadioButtonDrumProgDec, gridBagConstraints);

        jRadioButtonDrumProgFixed.setText("Fixed");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jRadioButtonDrumProgFixed, gridBagConstraints);

        jLabel17.setText("Program");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel17, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jSpinnerDrumProgPC, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jSpinnerDurmProgMSB, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jSpinnerDrumProgLSB, gridBagConstraints);

        jLabel20.setText("MSB");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel20, gridBagConstraints);

        jLabel21.setText("LSB");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel21, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jSeparator8, gridBagConstraints);

        jLabel22.setText("Program");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel22, gridBagConstraints);

        jButton3.setText("Browse");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jButton3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jSliderDrumSliderJump, gridBagConstraints);

        jLabel14.setText(" = See [Out On Value]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 29;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel14, gridBagConstraints);

        jCheckBoxDontSendOff.setText("Dont send When OFF");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jCheckBoxDontSendOff, gridBagConstraints);

        jTabbedPane1.addTab("Drum Config", jPanel3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jTabbedPane1, gridBagConstraints);

        jButtonCancel.setText("Cancel Edit");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
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
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButtonOK, gridBagConstraints);

        jLabelBlank.setText("BLANK");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 1.0;
        add(jLabelBlank, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonFromBeforeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFromBeforeActionPerformed
        jTextFieldTextCommand.setText(jLabelStartWith.getText());
    }//GEN-LAST:event_jButtonFromBeforeActionPerformed

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        readBufferFromPanelSlider();
        readBufferFromPanelDrum();
        disableUnusedOnPanel();
        if (validateBuffer(true) > 0) {
            return;
        }
        if (_status.toMXMessage(null) == null) { // magic number
            JOptionPane.showMessageDialog(this, "invalid text command", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        _okOption = true;
        MXUtil.getOwnerWindow(this).setVisible(false);

    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        _okOption = false;
        MXUtil.getOwnerWindow(this).setVisible(false);

    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonFromListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFromListActionPerformed
        EditorForControlChange picker = new EditorForControlChange();
        MXModalFrame.showAsDialog(this, picker, "Picker");
        CXNode x = picker.getTextReturn();

        if (x != null) {
            try {
                String textData = x.firstChildsTextContext("Data");

                MXTemplate template = MXMessageFactory.fromDtext(textData, this._channelModel.readCombobox(jComboBoxChannel).intValue());
                _status._template = template;
                _status.refillGate();

                CXNode value = x.firstChild("Value");
                if (value != null) {
                    int minValue = value._listAttributes.numberOfName("Min", -1);
                    int maxValue = value._listAttributes.numberOfName("Max", -1);
                    int offsetValue = value._listAttributes.numberOfName("Offset", 0);
                    int defaultValue = value._listAttributes.numberOfName("Default", 0);

                    if (minValue >= 0 && maxValue >= 0) {
                        _status.setCustomRange(minValue + offsetValue, maxValue + offsetValue);
                    }
                }
                /* unsupported
                CXNode gate = x.firstChild("Gate");
                if (gate != null) {                    
                    int minGate = value._listAttributes.numberOfName("Min", -1);
                    int maxGate = value._listAttributes.numberOfName("Max", -1);
                    int offsetGate = value._listAttributes.numberOfName("Offset", 0);
                    int defaultGate =  value._listAttributes.numberOfName("Default", 0);

                    if (minGate >= 0 && maxGate >= 0) {
                        _status.setGate(new RangedValue(defaultGate, minGate + offsetGate, maxGate + offsetGate));
                    }
                }*/

                _status._memo = x.firstChildsTextContext("Memo");
                _status._name = "";

                writeBufferToPanelSlider();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jButtonFromListActionPerformed

    private void jComboBoxGateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxGateActionPerformed
        if (skipDataExchange) {
            return;
        }
        Object sel = jComboBoxGate.getModel().getSelectedItem();
        MXWrap<Integer> wrap = (MXWrap<Integer>) sel;
        int newGate = wrap.value;
        int oldGate = _status._gate._var;

        readBufferFromPanelSlider();
        if (oldGate != newGate) {
            if (newGate == MXMidi.DATA1_CC_DATAENTRY) {
                _status.fillDataentry(MXVisitant.ROOMTYPE_RPN);
            }
        }
    }//GEN-LAST:event_jComboBoxGateActionPerformed

    private void jComboBoxChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxChannelActionPerformed
        if (skipDataExchange) {
            return;
        }
        Object sel = jComboBoxChannel.getModel().getSelectedItem();
        MXWrap<Integer> wrap = (MXWrap<Integer>) sel;
        int channel = wrap.value;
        if (_status._channel != channel) {
            _status._channel = channel;
            writeBufferToPanelSlider();
        }
    }//GEN-LAST:event_jComboBoxChannelActionPerformed

    private void jTextFieldTextCommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTextCommandActionPerformed
    }//GEN-LAST:event_jTextFieldTextCommandActionPerformed

    static final String PROG_INC = "Program INC";
    static final String PROG_DEC = "Program DEC";

    private void jButtonInternalCommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInternalCommandActionPerformed
        JPopupMenu menu = new JPopupMenu();
        JMenuItem menu1 = new JMenuItem(PROG_INC);
        menu1.addActionListener(selectInternalCommand);
        menu.add(menu1);
        JMenuItem menu2 = new JMenuItem(PROG_DEC);
        menu2.addActionListener(selectInternalCommand);
        menu.add(menu2);
        menu.show(jButtonInternalCommand, 0, jButtonInternalCommand.getHeight());
    }//GEN-LAST:event_jButtonInternalCommandActionPerformed

    MXMessageCapture _capture = null;

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        _capture = new MXMessageCapture();
        MXMain.setCapture(_capture);
        MXMessageCapturePanel panel = new MXMessageCapturePanel();
        MXModalFrame.showAsDialog(this, panel, "Capture ...");
        GateInfomation retval = panel._selected;
        if (retval != null) {
            _status._channel = retval._parent.channel;
            _status._template = MXMessageFactory.fromDtext(retval._parent.dtext, _status._channel);
            _status._gate = new RangedValue(retval._gate, retval._hitLoValue, retval._hitHiValue);

            String dtext = retval._parent.dtext;

            writeBufferToPanelSlider();
            MXMessage message = _status.toMXMessage(new MXTiming());

            if (message.isCommand(MXMidi.COMMAND_NOTEOFF)) {
                int z = JOptionPane.showConfirmDialog(
                        this,
                         "Seems you choiced Note Off\n"
                        + "You want to use Note ON?",
                         "Offer (adjust value range)",
                         JOptionPane.YES_NO_OPTION);
                if (z == JOptionPane.YES_OPTION) {
                    message = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_NOTEON + message.getChannel(), message.getData1(), 127);
                    _status._channel = message.getChannel();
                    _status._template = message.getTemplate();
                    _status._gate = message.getGate();
                    _status._value = message.getValue();
                }
            } else {
                int max = 128 - 1;
                if (message.hasValueHiField() || message.isValuePairCC14()) {
                    max = 128 * 128 - 1;
                }
                if (retval._hitLoValue != 0 || retval._hitHiValue != max) {
                    int z = JOptionPane.showConfirmDialog(
                            this,
                             "min-max = " + retval._hitLoValue + "-" + retval._hitHiValue + "\n"
                            + " I will offer you reset to 0 - " + max,
                             "Offer (adjust value rnage)",
                             JOptionPane.YES_NO_OPTION);
                    if (z == JOptionPane.YES_OPTION) {
                        _status.setCustomRange(0, max);
                    }
                }
            }
            writeBufferToPanelSlider();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    public void actionSetValueRange(int min, int max) {
        if (min < 0) {
            min = 0;
        }
        if (max >= 16383) {
            max = 16383;
        }
        if (min > max) {
            max = min;
        }
        int x = JOptionPane.showConfirmDialog(this, "Reset Value Range from " + min + " ... " + max, "ok?", JOptionPane.YES_NO_OPTION);
        if (x == JOptionPane.YES_OPTION) {
            skipDataExchange = true;
            jSpinnerMin.setValue(min);
            jSpinnerMax.setValue(max);
            skipDataExchange = false;
            _status.setCustomRange(min, max);
        }
    }

    private void jButtonActionQuickMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonActionQuickMenuActionPerformed
        JPopupMenu popup = new JPopupMenu();
        String text = jTextFieldTextCommand.getText();
        JMenuItem menu = new JMenuItem("Rest ValueRange as 7bit / 14bit");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _status.resetCustomRange();;
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });
        popup.add(menu);

        menu = new JMenuItem("Quick ModeSet = Note");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _status.fillNote(12 * 4);
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });
        popup.add(menu);

        menu = new JMenuItem("Quick ModeSet = Control Change");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _status.fillControlChange();
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });
        popup.add(menu);

        menu = new JMenuItem("Quick ModeSet = DataEntry RPN");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _status.fillDataentry(MXVisitant.ROOMTYPE_RPN);
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });

        popup.add(menu);
        menu = new JMenuItem("Quick ModeSet = DataEntry NRPN");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _status.fillDataentry(MXVisitant.ROOMTYPE_NRPN);
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });
        popup.add(menu);

        popup.show(jButtonActionQuickMenu, 0, jButtonActionQuickMenu.getHeight());
    }//GEN-LAST:event_jButtonActionQuickMenuActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        /*        if (jTabbedPane1.getSelectedIndex() == 1) {
            readBufferFromPanelSlider();
            writeBufferToPanelDrum();
            disableUnusedOnPanel();
        }else {
            readBufferFromPanelDrum();
            writeBufferToPanelSlider();
            disableUnusedOnPanel();
        }*/
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void jCheckBoxCC14bitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxCC14bitActionPerformed
        skipDataExchange = true;
        _status.setCustomRange((int) jSpinnerMin.getValue(), (int) jSpinnerMax.getValue());

        if (_status._dataroomType== MXVisitant.ROOMTYPE_RPN) {
            _status.fillDataentry(MXVisitant.ROOMTYPE_RPN);
        } else if (_status._dataroomType == MXVisitant.ROOMTYPE_NRPN) {
            _status.fillDataentry(MXVisitant.ROOMTYPE_NRPN);
        }

        if (jCheckBoxCC14bit.isEnabled() && jCheckBoxCC14bit.isSelected()) {
            _status._ccPair14 = true;
        } else {
            _status._ccPair14 = false;
        }
        skipDataExchange = false;
        writeBufferToPanelSlider();
        disableUnusedOnPanel();
    }//GEN-LAST:event_jCheckBoxCC14bitActionPerformed

    private void jComboBoxMSBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxMSBActionPerformed

    }//GEN-LAST:event_jComboBoxMSBActionPerformed

    private void jComboBoxLSBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxLSBActionPerformed

    }//GEN-LAST:event_jComboBoxLSBActionPerformed

    private void jComboBoxMSBItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxMSBItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            _status._dataroomMSB = _rpnMSBModel.readCombobox(jComboBoxMSB);
            if (_status._dataroomType == MXVisitant.ROOMTYPE_RPN) {
                _status.fillDataentry(MXVisitant.ROOMTYPE_RPN);
            } else if (_status._dataroomType == MXVisitant.ROOMTYPE_NRPN) {
                _status.fillDataentry(MXVisitant.ROOMTYPE_NRPN);
            }
            writeBufferToPanelSlider();
        }
    }//GEN-LAST:event_jComboBoxMSBItemStateChanged

    private void jComboBoxLSBItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxLSBItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            _status._dataroomLSB = _rpnLSBModel.readCombobox(jComboBoxLSB);
            if (_status._dataroomType == MXVisitant.ROOMTYPE_RPN) {
                _status.fillDataentry(MXVisitant.ROOMTYPE_RPN);
            } else if (_status._dataroomType == MXVisitant.ROOMTYPE_NRPN) {
                _status.fillDataentry(MXVisitant.ROOMTYPE_NRPN);
            }
            writeBufferToPanelSlider();
        }
    }//GEN-LAST:event_jComboBoxLSBItemStateChanged

    private void jCheckBoxCustomRangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxCustomRangeActionPerformed
        boolean sel = jCheckBoxCustomRange.isSelected();
        jSpinnerMin.setEnabled(sel);
        jSpinnerMax.setEnabled(sel);
    }//GEN-LAST:event_jCheckBoxCustomRangeActionPerformed

    private void jButtonUpdateCommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdateCommandActionPerformed
        MXTemplate temp = MXMessageFactory.fromDtext(jTextFieldTextCommand.getText(), 0);
        _status._template = temp;
    }//GEN-LAST:event_jButtonUpdateCommandActionPerformed

    private void jButtonHarmonyEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHarmonyEditActionPerformed
        MXNotePicker picker = new MXNotePicker();
        picker.setSelectedNoteList(MXMidi.textToNoteList(jTextFieldHarmonyNoteList.getText()));
        if (picker.showAsModalDialog(this)) {
            int[] note = picker.getSelectedNoteList();
            String text = MXMidi.noteListToText(note);
            jTextFieldHarmonyNoteList.setText(text);
        }
    }//GEN-LAST:event_jButtonHarmonyEditActionPerformed

    private void jComboBoxDrumOutChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxDrumOutChannelActionPerformed
        //TODO _status.setSwitchOutChannel(_drumOutChannel.readCombobox(jComboBoxDrumOutChannel));
        disableUnusedOnPanel();
    }//GEN-LAST:event_jComboBoxDrumOutChannelActionPerformed

    private void jButtonSequenceFileBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSequenceFileBrowseActionPerformed
        MXSwingFileChooser chooser = new MXSwingFileChooser();
        MXSwingFolderBrowser browser = new MXSwingFolderBrowser(null, new FileFilterListExt(".Mid"));
        MXUtil.showAsDialog(this, browser, "Choose Standard MIDI File");
        FileList list = browser.getParamsOfNavigator().getApprovedValue();
        if (list == null || list.isEmpty()) {
            return;
        }
        String file = list.get(0).getAbsolutePath();
        jTextFieldSequenceFile.setText(file);
        //TODO _status.setSwitchSequencerFile(file);
    }//GEN-LAST:event_jButtonSequenceFileBrowseActionPerformed

    private void jRadioButtonDrumTypeSameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDrumTypeSameActionPerformed
        //_status.setSwitchType(MGStatus.SWITCH_TYPE_ON);
        disableUnusedOnPanel();
    }//GEN-LAST:event_jRadioButtonDrumTypeSameActionPerformed

    private void jRadioButtonDrumTypeSongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDrumTypeSongActionPerformed
        //_status.setSwitchType(MGStatus.SWITCH_TYPE_SEQUENCE);
        disableUnusedOnPanel();
    }//GEN-LAST:event_jRadioButtonDrumTypeSongActionPerformed

    private void jRadioButtonDrumTypeNotesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDrumTypeNotesActionPerformed
        //_status.setSwitchType(MGStatus.SWITCH_TYPE_HARMONY);
        disableUnusedOnPanel();
    }//GEN-LAST:event_jRadioButtonDrumTypeNotesActionPerformed

    public int getDrumType() {/*
        if (jRadioButtonDrumTypeSame.isSelected()) {
            return MGStatus.SWITCH_TYPE_ON;
        }
        if (jRadioButtonOnOff.isSelected()) {
            return MGStatus.SWITCH_TYPE_ONOFF;
        }
        if (jRadioButtonDrumTypeNotes.isSelected()) {
            return MGStatus.SWITCH_TYPE_HARMONY;
        }
        if (jRadioButtonDrumTypeSong.isSelected()) {
            return MGStatus.SWITCH_TYPE_SEQUENCE;
        }*/
        return 0;
    }

    ActionListener selectInternalCommand = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(PROG_INC)) {
                _status.setTemplateAsText(MXTemplate.EXCOMMAND_PROGRAM_INC, 0);
                MGStatusPanel.this.writeBufferToPanelSlider();
                disableUnusedOnPanel();
                return;
            }
            if (e.getActionCommand().equals(PROG_DEC)) {
                _status.setTemplateAsText(MXTemplate.EXCOMMAND_PROGRAM_DEC, 0);
                MGStatusPanel.this.writeBufferToPanelSlider();
                disableUnusedOnPanel();
                return;
            }
        }
    };

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButtonActionQuickMenu;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonDrumOutTemplate;
    private javax.swing.JButton jButtonFromBefore;
    private javax.swing.JButton jButtonFromList;
    private javax.swing.JButton jButtonHarmonyEdit;
    private javax.swing.JButton jButtonInternalCommand;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JButton jButtonSequenceFileBrowse;
    private javax.swing.JButton jButtonUpdateCommand;
    private javax.swing.JCheckBox jCheckBoxCC14bit;
    private javax.swing.JCheckBox jCheckBoxCustomRange;
    private javax.swing.JCheckBox jCheckBoxDontSendOff;
    private javax.swing.JCheckBox jCheckBoxDrumInToggle;
    private javax.swing.JCheckBox jCheckBoxDrumOutOffSameAs;
    private javax.swing.JCheckBox jCheckBoxDrumOutOnSameAs;
    private javax.swing.JCheckBox jCheckBoxSequencerFilterNote;
    private javax.swing.JCheckBox jCheckBoxSequencerSeekStart;
    private javax.swing.JCheckBox jCheckBoxSequencerSingleTrack;
    private javax.swing.JComboBox<String> jComboBoxChannel;
    private javax.swing.JComboBox<String> jComboBoxDrumOutChannel;
    private javax.swing.JComboBox<String> jComboBoxDrumOutPort;
    private javax.swing.JComboBox<String> jComboBoxGate;
    private javax.swing.JComboBox<String> jComboBoxLSB;
    private javax.swing.JComboBox<String> jComboBoxMSB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelBlank;
    private javax.swing.JLabel jLabelBlank1;
    private javax.swing.JLabel jLabelDrumOutTemplate;
    private javax.swing.JLabel jLabelInputText;
    private javax.swing.JLabel jLabelNameDefault;
    private javax.swing.JLabel jLabelStartWith;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButtonDrumProgDec;
    private javax.swing.JRadioButton jRadioButtonDrumProgFixed;
    private javax.swing.JRadioButton jRadioButtonDrumProgInc;
    private javax.swing.JRadioButton jRadioButtonDrumTypeCustom;
    private javax.swing.JRadioButton jRadioButtonDrumTypeJump;
    private javax.swing.JRadioButton jRadioButtonDrumTypeNotes;
    private javax.swing.JRadioButton jRadioButtonDrumTypeProgram;
    private javax.swing.JRadioButton jRadioButtonDrumTypeSame;
    private javax.swing.JRadioButton jRadioButtonDrumTypeSong;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSlider jSliderDrumSliderJump;
    private javax.swing.JSpinner jSpinnerDrumInMax;
    private javax.swing.JSpinner jSpinnerDrumInMin;
    private javax.swing.JSpinner jSpinnerDrumInMouse;
    private javax.swing.JSpinner jSpinnerDrumOutOffCustom;
    private javax.swing.JSpinner jSpinnerDrumOutOnCustom;
    private javax.swing.JSpinner jSpinnerDrumProgLSB;
    private javax.swing.JSpinner jSpinnerDrumProgPC;
    private javax.swing.JSpinner jSpinnerDrumSliderJump;
    private javax.swing.JSpinner jSpinnerDurmProgMSB;
    private javax.swing.JSpinner jSpinnerMax;
    private javax.swing.JSpinner jSpinnerMin;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldHarmonyNoteList;
    private javax.swing.JTextField jTextFieldMemo;
    private javax.swing.JTextField jTextFieldName;
    private javax.swing.JTextField jTextFieldSequenceFile;
    private javax.swing.JTextField jTextFieldTextCommand;
    // End of variables declaration//GEN-END:variables

    public void showsub1() {
/*
        if (_status.getSwitchOutOnType() == MGStatus.SWITCH_OUT_ON_CUSTOM) {
            jLabelOutputOnText.setText("Custom " + _status.getSwitchOutOnText() + "(Gate:" + _status.getSwitchOutOnTextGate() + ")");
        } else if (_status.getSwitchOutOnType() == MGStatus.SWITCH_OUT_ON_SAME_AS_INPUT) {
            jLabelOutputOnText.setText("Same " + _status.toTemplateText() + "(Gate:" + _status._gate + ")");
        } else {
            jLabelOutputOnText.setText("Unknwon(" + _status.getSwitchOutOnType());
        }
        if (_status.getSwitchOutOffType() == MGStatus.SWITCH_OUT_OFF_CUSTOM) {
            jLabelOutputOffText.setText("Custom " + _status.getSwitchOutOffText() + "(Gate:" + _status.getSwitchOutOffTextGate() + ")");
        } else if (_status.getSwitchOutOffType() == MGStatus.SWITCH_OUT_OFF_NONE) {
            jLabelOutputOffText.setText("None");
        } else if (_status.getSwitchOutOffType() == MGStatus.SWITCH_OUT_OFF_SAME_AS_INPUT) {
            jLabelOutputOffText.setText("Same(Input)" + _status.toTemplateText() + "(Gate:" + _status._gate + ")");
        } else if (_status.getSwitchOutOffType() == MGStatus.SWITCH_OUT_OFF_SAME_AS_OUTPUT_ON) {
            jLabelOutputOffText.setText("Same(Output-ON)" + _status.toTemplateText() + "(Gate:" + _status.getSwitchOutOnTextGate() + ")");
        } else {
            jLabelOutputOffText.setText("Unknwon(" + _status.getSwitchOutOffType());
        }
*/
    }
}
