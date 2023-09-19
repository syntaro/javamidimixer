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
import jp.synthtarou.midimixer.libs.ccxml.CXNode;
import jp.synthtarou.midimixer.libs.ccxml.EditorForControlChange;
import jp.synthtarou.midimixer.libs.navigator.NavigatorForText;
import jp.synthtarou.midimixer.libs.navigator.NavigatorUtil;
import jp.synthtarou.midimixer.libs.navigator.ParamsOfNavigator;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.capture.MXMessageCapture;
import jp.synthtarou.midimixer.libs.midi.capture.MXMessageCapturePanel;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXMidiWrapList;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.capture.GateInfomation;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;
import jp.synthtarou.midimixer.libs.swing.MXModalFrame;
import jp.synthtarou.midimixer.libs.swing.MXSwingFileChooser;
import jp.synthtarou.midimixer.libs.swing.SafeSpinnerNumberModel;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileFilterListExt;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileList;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.MXSwingFolderBrowser;
import jp.synthtarou.midimixer.libs.navigator.INavigator2;

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

    String _templateStartWith;

    protected MGStatus _status;

    boolean skipDataExchange = true;
    private ArrayList<String> textValidate = new ArrayList();
    private ArrayList<String> textTemplate = new ArrayList();

    /**
     * Creates new form MGStatusPanel
     */
    public MGStatusPanel(MX32MixerProcess process, MGStatus status) {

        skipDataExchange = true;
        initComponents();

        _process = process;
        _status = status;
        _templateStartWith = _status._base.getTemplateAsText();

        jLabelStartWith.setText(_templateStartWith.toString());

        setPreferredSize(new Dimension(900, 700));
        ButtonGroup group = new ButtonGroup();
        group.add(jRadioButtonDrumTypeSame);
        group.add(jRadioButtonDrumTypeNotes);
        group.add(jRadioButtonDrumTypeSong);
        group.add(jRadioButtonDrumTypeProgram);
        group.add(jRadioButtonDrumTypeDontSend);
        group.add(jRadioButtonDrumTypeCustom);
        group.add(jRadioButtonDrumTypeLink);

        ButtonGroup group2 = new ButtonGroup();
        group2.add(jRadioButtonLinkKnob1);
        group2.add(jRadioButtonLinkKnob2);
        group2.add(jRadioButtonLinkKnob3);
        group2.add(jRadioButtonLinkKnob4);
        group2.add(jRadioButtonSlider);
        
        if (_status._uiType == MGStatus.TYPE_DRUMPAD) {
            jTabbedPane1.setEnabledAt(1, true);
        } else {
            jTabbedPane1.setEnabledAt(1, false);
        }

        skipDataExchange = false;

        jSpinnerDrumInMin.setModel(new SpinnerNumberModel(1, 0, 127, 1));
        jSpinnerDrumInMax.setModel(new SpinnerNumberModel(127, 0, 127, 1));

        _drumOutPort = MXMidiWrapList.listupPortAssigned(false);
        _drumOutChannel = MXMidiWrapList.listupChannel(false);

        jComboBoxDrumOutPort.setModel(_drumOutPort);
        jComboBoxDrumOutChannel.setModel(_drumOutChannel);

        jLabelBlank1.setText("");
        jLabelBlank2.setText("");
        jLabelBlank3.setText("");
        jLabelBlank4.setText("");
        jLabelBlank5.setText("");
        jLabelBlank6.setText("");
        jLabelBlank7.setText("");

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
                _channelModel = MXMidiWrapList.listupChannel(false);
                _ccGateModel = MXMidiWrapList.listupControlChange(true);
                _keyGateModel = MXMidiWrapList.listupNoteNo(true);
                _normalGateModel = MXMidiWrapList.listupGate7Bit();
            }

            if (_status._name == null) {
                _status._name = "";
            }
            if (_status._memo == null) {
                _status._memo = "";
            }
            jTextFieldName.setText(_status._memo);
            jTextFieldTemplate.setText(_status.getBaseMessage().getTemplateAsText());
            jLabelStartWith.setText(_templateStartWith.toString());

            _channelModel.writeComboBox(jComboBoxChannel, _status._base.getChannel());

            MXMessage message = _status.toMXMessage(null);
            boolean initTurn = true;
            if (jComboBoxGate.getModel() instanceof MXWrapList) {
                initTurn = false;
            }

            jLabelDefaultName.setText("'" + message.toStringForUI() + "' if blank");

            int command = message.getStatus() & 0xf0;
            int gateValue = _status._base.getGate()._var;

            if (command == MXMidi.COMMAND_CH_CHANNELPRESSURE
                    || command == MXMidi.COMMAND_CH_NOTEON
                    || command == MXMidi.COMMAND_CH_NOTEOFF) {
                jComboBoxGate.setModel(_keyGateModel);
                if (initTurn || ((MXWrap<Integer>) jComboBoxGate.getSelectedItem())._value != gateValue) {
                    _keyGateModel.writeComboBox(jComboBoxGate, gateValue);
                }
            } else if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                jComboBoxGate.setModel(_ccGateModel);
                if (initTurn || ((MXWrap<Integer>) jComboBoxGate.getSelectedItem())._value != gateValue) {
                    _ccGateModel.writeComboBox(jComboBoxGate, gateValue);
                }
            } else {
                jComboBoxGate.setModel(_normalGateModel);
                if (initTurn || ((MXWrap<Integer>) jComboBoxGate.getSelectedItem())._value != gateValue) {
                    _normalGateModel.writeComboBox(jComboBoxGate, gateValue);
                }
            }
/*
            jSpinnerOutOnValueFixed.setModel(new SafeSpinnerNumberModel(_status.getSwitchOutOnValueFixed(), 0, 16383, 1));
            jSpinnerOutOffValueFixed.setModel(new SafeSpinnerNumberModel(_status.getSwitchOutOffValueFixed(), 0, 16383, 1));
*/
            jSpinnerMin.setModel(new SafeSpinnerNumberModel(_status._base.getValue()._min, 0, 128 * 128 - 1, 1));
            jSpinnerMax.setModel(new SafeSpinnerNumberModel(_status._base.getValue()._max, 0, 128 * 128 - 1, 1));

            jCheckBoxCC14bit.setSelected(_status._ccPair14);
            jCheckBoxCustomRange.setSelected(_status.hasCustomRange());
        } finally {
            skipDataExchange = false;
        }

        disableUnusedOnPanel();
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
            //NP result.add("Name is empty. Kontrol will use short _name of message.");
        }

        if (data._memo == null) {
            data._memo = "";
        }        
        if (data._memo.length() == 0) {
            //NP result.add("Memo is empty. Thats No Problem.");
        }

        if (data.getBaseMessage() == null) {
            result.add("TextCommand is empty. Please fill it.");
        } else {
            try {
                MXMessage message = data.toMXMessage(null);
                if (message == null) {
                    result.add("TextCommand [" + data.getBaseMessage().getTemplateAsText() + "] is not valid.");
                }
                else if (message.isUnknwonDataentry()) {
                    result.add("If you need DATAENTRY. try [@RPN/@NRPN msb lsb value 0].");
                } 
                /*
                else if (message.isMessageTypeChannel() && message.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE) && !message.isDataentry()) {
                    String newText = "@CC #GL #VL";
                    if (data.getBaseMessage().getTemplateAsText().equals(newText) == false) {
                        String errorText = "ControlChange's Text Command can be '" + newText + "'";
                        if (canDialog && JOptionPane.showConfirmDialog(this, errorText, "Smart Replace", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            skipDataExchange = true;
                            message = message.refillGate();
                            data.setBaseMessage(message);
                            jTextFieldMemo.setText(newText);
                            _ccGateModel.writeComboBox(jComboBoxGate, data._base.getGate()._var);
                            skipDataExchange = false;
                        } else {
                            result.add(errorText);
                        }
                    }
                }
                */
            } catch (Exception e) {
                result.add("TextCommand [" + data.getBaseMessage() + "] is not valid.");
            }
        }

        validateBufferSubDrum(result);

        textValidate = result;
        showValidationError();

        return result.size();
    }

    public void showValidationError() {
        StringBuffer str = new StringBuffer();
        if (textValidate.size() == 0) {
            return;
        }
        str.append("<html>**Validation Result**");
        for (String line : textValidate) {
            str.append("<br>");
            str.append(line);
        }
        JOptionPane.showMessageDialog(this, str, "Validate Error", JOptionPane.OK_OPTION);
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

        int temp = _status.getBaseMessage().getTemplateAsPlain(0);
        if (temp == MXMidi.COMMAND2_CH_RPN || temp == MXMidi.COMMAND2_CH_NRPN) {
            isdataentry = true;
        }

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
        _status._memo = jTextFieldName.getText();
        MXWrap<Integer> x = (MXWrap<Integer>) jComboBoxGate.getSelectedItem();
        _status._base.setGate(RangedValue.new7bit(x._value));

        if (jCheckBoxCustomRange.isSelected()) {
            int min = (Integer) jSpinnerMin.getValue();
            int max = (Integer) jSpinnerMax.getValue();
            _status.setCustomRange(min, max);
        } else {
            _status.resetCustomRange();
        }

        MXMessage msg = _status.toMXMessage(null);
        if (msg.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE) && msg.getData1() >= 0 && msg.getData1() < 32) {
            _status._ccPair14 = jCheckBoxCC14bit.isSelected();
            jCheckBoxCC14bit.setEnabled(true);
        }
        else {
            if (msg.isCommand(MXMidi.COMMAND2_CH_RPN)
             || msg.isCommand(MXMidi.COMMAND2_CH_NRPN)) {
                _status._ccPair14 = true;
                jCheckBoxCC14bit.setEnabled(false);
                jCheckBoxCC14bit.setSelected(true);
            }
            else {
                _status._ccPair14 = false;
                jCheckBoxCC14bit.setEnabled(false);
                jCheckBoxCC14bit.setSelected(false);
            }
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
        jPanelPage1 = new javax.swing.JPanel();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel31 = new javax.swing.JLabel();
        jLabelBlank1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jTextFieldMemo = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButtonFromList = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButtonMenu = new javax.swing.JButton();
        jButtonTemplateInput = new javax.swing.JButton();
        jLabelDefaultName = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabelStartWith = new javax.swing.JLabel();
        jComboBoxChannel = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jComboBoxGate = new javax.swing.JComboBox<>();
        jCheckBoxCustomRange = new javax.swing.JCheckBox();
        jLabel29 = new javax.swing.JLabel();
        jSpinnerMin = new javax.swing.JSpinner();
        jSpinnerMax = new javax.swing.JSpinner();
        jCheckBoxCC14bit = new javax.swing.JCheckBox();
        jLabel15 = new javax.swing.JLabel();
        jButtonResetValueRange = new javax.swing.JButton();
        jTextFieldTemplate = new javax.swing.JTextField();
        jPanelPage2 = new javax.swing.JPanel();
        jPanelValue = new javax.swing.JPanel();
        jCheckBoxDrumInToggle = new javax.swing.JCheckBox();
        jSpinnerDrumInMax = new javax.swing.JSpinner();
        jLabelInputText = new javax.swing.JLabel();
        jSpinnerDrumInMin = new javax.swing.JSpinner();
        jSpinnerDrumInMouse = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanelTabTemplate = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabelBlank6 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jLabel42 = new javax.swing.JLabel();
        jPanelTabProgram = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jSpinnerDurmProgMSB = new javax.swing.JSpinner();
        jSpinnerDrumProgPC = new javax.swing.JSpinner();
        jSpinnerDrumProgLSB = new javax.swing.JSpinner();
        jLabel21 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabelBlank3 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox<>();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jPanelTabNotes = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jTextFieldHarmonyNoteList = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jButtonNotesKeys = new javax.swing.JButton();
        jLabelBlank2 = new javax.swing.JLabel();
        jPanelTabSequener = new javax.swing.JPanel();
        jTextFieldSequenceFile = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jButtonSequenceFileBrowse = new javax.swing.JButton();
        jCheckBoxSequencerSeekStart = new javax.swing.JCheckBox();
        jCheckBoxSequencerSingleTrack = new javax.swing.JCheckBox();
        jCheckBoxSequencerFilterNote = new javax.swing.JCheckBox();
        jLabel32 = new javax.swing.JLabel();
        jLabelBlank4 = new javax.swing.JLabel();
        jPanelTabLink = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jRadioButtonLinkKnob1 = new javax.swing.JRadioButton();
        jRadioButtonLinkKnob2 = new javax.swing.JRadioButton();
        jRadioButtonLinkKnob3 = new javax.swing.JRadioButton();
        jRadioButtonLinkKnob4 = new javax.swing.JRadioButton();
        jRadioButtonSlider = new javax.swing.JRadioButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel34 = new javax.swing.JLabel();
        jLabelBlank5 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jPanelOutput = new javax.swing.JPanel();
        jRadioButtonDrumTypeNotes = new javax.swing.JRadioButton();
        jRadioButtonDrumTypeProgram = new javax.swing.JRadioButton();
        jRadioButtonDrumTypeSong = new javax.swing.JRadioButton();
        jRadioButtonDrumTypeCustom = new javax.swing.JRadioButton();
        jRadioButtonDrumTypeSame = new javax.swing.JRadioButton();
        jRadioButtonDrumTypeDontSend = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jComboBoxDrumOutPort = new javax.swing.JComboBox<>();
        jComboBoxDrumOutChannel = new javax.swing.JComboBox<>();
        jRadioButtonDrumTypeLink = new javax.swing.JRadioButton();
        jLabel35 = new javax.swing.JLabel();
        jLabelBlank7 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox<>();
        jComboBox4 = new javax.swing.JComboBox<>();
        jButtonCancel = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();

        setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setLayout(new java.awt.GridBagLayout());

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jPanelPage1.setLayout(new java.awt.GridBagLayout());
        jPanelPage1.add(jSeparator5, new java.awt.GridBagConstraints());

        jLabel31.setText(" to ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelPage1.add(jLabel31, gridBagConstraints);

        jLabelBlank1.setText("jLabelBlank1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelPage1.add(jLabelBlank1, gridBagConstraints);

        jLabel7.setText("Memo");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jLabel7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jTextFieldName, gridBagConstraints);

        jTextFieldMemo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldMemoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jTextFieldMemo, gridBagConstraints);

        jLabel3.setText("Template");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jLabel3, gridBagConstraints);

        jLabel9.setText(" Default Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jLabel9, gridBagConstraints);

        jLabel2.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jLabel2, gridBagConstraints);

        jButtonFromList.setText("From XML");
        jButtonFromList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFromListActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelPage1.add(jButtonFromList, gridBagConstraints);

        jButton1.setText("From Capture");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        jPanelPage1.add(jButton1, gridBagConstraints);

        jButtonMenu.setText("Stationary");
        jButtonMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMenuActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelPage1.add(jButtonMenu, gridBagConstraints);

        jButtonTemplateInput.setText("Edit >");
        jButtonTemplateInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTemplateInputActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelPage1.add(jButtonTemplateInput, gridBagConstraints);

        jLabelDefaultName.setText("Default Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jLabelDefaultName, gridBagConstraints);

        jLabel1.setText(" when Start Editing");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jLabel1, gridBagConstraints);

        jLabelStartWith.setText("F7 00 F0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelPage1.add(jLabelStartWith, gridBagConstraints);

        jComboBoxChannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxChannelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jComboBoxChannel, gridBagConstraints);

        jLabel4.setText("Channel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jLabel4, gridBagConstraints);

        jLabel5.setText("Gate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jLabel5, gridBagConstraints);

        jComboBoxGate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxGateActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jComboBoxGate, gridBagConstraints);

        jCheckBoxCustomRange.setText("Use Custom, If not custom signal's Min ~ Max = [#VL 0-127]  [#VH #VL = 0 - 16383]");
        jCheckBoxCustomRange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxCustomRangeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelPage1.add(jCheckBoxCustomRange, gridBagConstraints);

        jLabel29.setText("Value Range");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jLabel29, gridBagConstraints);

        jSpinnerMin.setModel(new javax.swing.SpinnerNumberModel(0, 0, 16383, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelPage1.add(jSpinnerMin, gridBagConstraints);

        jSpinnerMax.setModel(new javax.swing.SpinnerNumberModel(0, 0, 16383, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelPage1.add(jSpinnerMax, gridBagConstraints);

        jCheckBoxCC14bit.setText("Enable 14bit with +32CC (CC: 0 to 31 can pair with #+32)");
        jCheckBoxCC14bit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxCC14bitActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelPage1.add(jCheckBoxCC14bit, gridBagConstraints);

        jLabel15.setText("CC 14bit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelPage1.add(jLabel15, gridBagConstraints);

        jButtonResetValueRange.setText("Reset");
        jButtonResetValueRange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetValueRangeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 11;
        jPanelPage1.add(jButtonResetValueRange, gridBagConstraints);

        jTextFieldTemplate.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jTextFieldTemplate, gridBagConstraints);

        jTabbedPane1.addTab("Input Config", jPanelPage1);

        jPanelPage2.setLayout(new java.awt.GridBagLayout());

        jPanelValue.setBorder(javax.swing.BorderFactory.createTitledBorder("Value"));
        jPanelValue.setLayout(new java.awt.GridBagLayout());

        jCheckBoxDrumInToggle.setText("Switch When On");
        jCheckBoxDrumInToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDrumInToggleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanelValue.add(jCheckBoxDrumInToggle, gridBagConstraints);

        jSpinnerDrumInMax.setModel(new javax.swing.SpinnerNumberModel(127, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jSpinnerDrumInMax, gridBagConstraints);

        jLabelInputText.setText("<=");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        jPanelValue.add(jLabelInputText, gridBagConstraints);

        jSpinnerDrumInMin.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jSpinnerDrumInMin, gridBagConstraints);

        jSpinnerDrumInMouse.setModel(new javax.swing.SpinnerNumberModel(127, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jSpinnerDrumInMouse, gridBagConstraints);

        jLabel11.setText("Mouse");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jLabel11, gridBagConstraints);

        jLabel13.setText("Value When On");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jLabel13, gridBagConstraints);

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanelValue.add(jSpinner1, gridBagConstraints);

        jLabel19.setText("Click(ON)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelValue.add(jLabel19, gridBagConstraints);

        jLabel25.setText("Release(OFF)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelValue.add(jLabel25, gridBagConstraints);

        jLabel39.setText("Toggle");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelValue.add(jLabel39, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelPage2.add(jPanelValue, gridBagConstraints);

        jPanelTabTemplate.setBorder(javax.swing.BorderFactory.createTitledBorder("Output Template"));
        jPanelTabTemplate.setLayout(new java.awt.GridBagLayout());

        jLabel8.setText("CC Template");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabTemplate.add(jLabel8, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelTabTemplate.add(jTextField1, gridBagConstraints);

        jLabel16.setText("Gate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabTemplate.add(jLabel16, gridBagConstraints);

        jLabel18.setText("Value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabTemplate.add(jLabel18, gridBagConstraints);

        jLabel36.setText("jLabel36");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabTemplate.add(jLabel36, gridBagConstraints);

        jButton2.setText("...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        jPanelTabTemplate.add(jButton2, gridBagConstraints);

        jLabelBlank6.setText("Blank6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelTabTemplate.add(jLabelBlank6, gridBagConstraints);

        jButton5.setText("...");
        jPanelTabTemplate.add(jButton5, new java.awt.GridBagConstraints());

        jLabel42.setText(" = See [Output/Output Value/On Value]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabTemplate.add(jLabel42, gridBagConstraints);

        jTabbedPane2.addTab("Template", jPanelTabTemplate);

        jPanelTabProgram.setBorder(javax.swing.BorderFactory.createTitledBorder("Output - Program"));
        jPanelTabProgram.setLayout(new java.awt.GridBagLayout());

        jLabel17.setText("PC");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabProgram.add(jLabel17, gridBagConstraints);

        jLabel20.setText("MSB");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabProgram.add(jLabel20, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabProgram.add(jSpinnerDurmProgMSB, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabProgram.add(jSpinnerDrumProgPC, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabProgram.add(jSpinnerDrumProgLSB, gridBagConstraints);

        jLabel21.setText("LSB");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanelTabProgram.add(jLabel21, gridBagConstraints);

        jButton3.setText("...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabProgram.add(jButton3, gridBagConstraints);

        jLabelBlank3.setText("Blank3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelTabProgram.add(jLabelBlank3, gridBagConstraints);

        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Increment (+1)", "Decremnt I(-1)", "Fixed Program" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabProgram.add(jComboBox5, gridBagConstraints);

        jLabel37.setText("jLabel37");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabProgram.add(jLabel37, gridBagConstraints);

        jLabel38.setText("Program Numbrer");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabProgram.add(jLabel38, gridBagConstraints);

        jLabel40.setText("Program Change");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabProgram.add(jLabel40, gridBagConstraints);

        jLabel41.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabProgram.add(jLabel41, gridBagConstraints);

        jTabbedPane2.addTab("Program", jPanelTabProgram);

        jPanelTabNotes.setBorder(javax.swing.BorderFactory.createTitledBorder("Output Notess"));
        jPanelTabNotes.setLayout(new java.awt.GridBagLayout());

        jLabel27.setText("List Notes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelTabNotes.add(jLabel27, gridBagConstraints);

        jTextFieldHarmonyNoteList.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelTabNotes.add(jTextFieldHarmonyNoteList, gridBagConstraints);

        jLabel14.setText(" = See [Output/Output Value/On Value]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabNotes.add(jLabel14, gridBagConstraints);

        jLabel28.setText("Velocity");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelTabNotes.add(jLabel28, gridBagConstraints);

        jButtonNotesKeys.setText("...");
        jButtonNotesKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNotesKeysActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelTabNotes.add(jButtonNotesKeys, gridBagConstraints);

        jLabelBlank2.setText("Blank6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelTabNotes.add(jLabelBlank2, gridBagConstraints);

        jTabbedPane2.addTab("Notes", jPanelTabNotes);

        jPanelTabSequener.setBorder(javax.swing.BorderFactory.createTitledBorder("Output Sequencer"));
        jPanelTabSequener.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelTabSequener.add(jTextFieldSequenceFile, gridBagConstraints);

        jLabel30.setText("SMF File");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelTabSequener.add(jLabel30, gridBagConstraints);

        jButtonSequenceFileBrowse.setText("...");
        jButtonSequenceFileBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSequenceFileBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelTabSequener.add(jButtonSequenceFileBrowse, gridBagConstraints);

        jCheckBoxSequencerSeekStart.setText("Play Start Timing = 1st Note");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabSequener.add(jCheckBoxSequencerSeekStart, gridBagConstraints);

        jCheckBoxSequencerSingleTrack.setText("Play in Single Channel (Port / Ch = Output Section)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabSequener.add(jCheckBoxSequencerSingleTrack, gridBagConstraints);

        jCheckBoxSequencerFilterNote.setText("Only Play Note+Pitch+Wheel (IgnoreCC)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabSequener.add(jCheckBoxSequencerFilterNote, gridBagConstraints);

        jLabel32.setText("Play Option");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelTabSequener.add(jLabel32, gridBagConstraints);

        jLabelBlank4.setText("Blank4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelTabSequener.add(jLabelBlank4, gridBagConstraints);

        jTabbedPane2.addTab("Sequencer", jPanelTabSequener);

        jPanelTabLink.setBorder(javax.swing.BorderFactory.createTitledBorder("Output - Link Slider / Knob"));
        jPanelTabLink.setLayout(new java.awt.GridBagLayout());

        jLabel22.setText("Row");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jLabel22, gridBagConstraints);

        jLabel24.setText("Column");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jLabel24, gridBagConstraints);

        jRadioButtonLinkKnob1.setText("Knob1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jRadioButtonLinkKnob1, gridBagConstraints);

        jRadioButtonLinkKnob2.setText("Knob2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jRadioButtonLinkKnob2, gridBagConstraints);

        jRadioButtonLinkKnob3.setText("Knob3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jRadioButtonLinkKnob3, gridBagConstraints);

        jRadioButtonLinkKnob4.setText("Knob4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jRadioButtonLinkKnob4, gridBagConstraints);

        jRadioButtonSlider.setText("Slider");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jRadioButtonSlider, gridBagConstraints);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Same Column (Pad)", "1", "2", "3", "..." }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelTabLink.add(jComboBox1, gridBagConstraints);

        jLabel34.setText("Value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jLabel34, gridBagConstraints);

        jLabelBlank5.setText("Blank5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelTabLink.add(jLabelBlank5, gridBagConstraints);

        jLabel43.setText(" = See [Output/Output Value/On Value]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabLink.add(jLabel43, gridBagConstraints);

        jTabbedPane2.addTab("Link Slider / Knob", jPanelTabLink);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelPage2.add(jTabbedPane2, gridBagConstraints);

        jPanelOutput.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));
        jPanelOutput.setLayout(new java.awt.GridBagLayout());

        jRadioButtonDrumTypeNotes.setText("Notes");
        jRadioButtonDrumTypeNotes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDrumTypeNotesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jRadioButtonDrumTypeNotes, gridBagConstraints);

        jRadioButtonDrumTypeProgram.setText("Program");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jRadioButtonDrumTypeProgram, gridBagConstraints);

        jRadioButtonDrumTypeSong.setText("Sequence");
        jRadioButtonDrumTypeSong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDrumTypeSongActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jRadioButtonDrumTypeSong, gridBagConstraints);

        jRadioButtonDrumTypeCustom.setText("Custom Template");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jRadioButtonDrumTypeCustom, gridBagConstraints);

        jRadioButtonDrumTypeSame.setText("Same Template As Input");
        jRadioButtonDrumTypeSame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDrumTypeSameActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jRadioButtonDrumTypeSame, gridBagConstraints);

        jRadioButtonDrumTypeDontSend.setText("Don't Send");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jRadioButtonDrumTypeDontSend, gridBagConstraints);

        jLabel6.setText("Type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jLabel6, gridBagConstraints);

        jLabel26.setText("Port / Channel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jLabel26, gridBagConstraints);

        jComboBoxDrumOutPort.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "As Input", "A", "B", "C", "D", "..." }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jComboBoxDrumOutPort, gridBagConstraints);

        jComboBoxDrumOutChannel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "As Input", "1", "2", "3", "4", "5", "..", "16" }));
        jComboBoxDrumOutChannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxDrumOutChannelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jComboBoxDrumOutChannel, gridBagConstraints);

        jRadioButtonDrumTypeLink.setText("Link Slider/Knob");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jRadioButtonDrumTypeLink, gridBagConstraints);

        jLabel35.setText("Output Value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jLabel35, gridBagConstraints);

        jLabelBlank7.setText("Blank7");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelOutput.add(jLabelBlank7, gridBagConstraints);

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "On Value As Input", "On Value As [Mouse Click]" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jComboBox3, gridBagConstraints);

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Off Value As Input", "Off Value As [Mouse Release]", "Off Value is Notihng to Send" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jComboBox4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelPage2.add(jPanelOutput, gridBagConstraints);

        jTabbedPane1.addTab("Drum Transform", jPanelPage2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
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
    }// </editor-fold>//GEN-END:initComponents

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
        CXNode x = picker.getParamsOfNavigator().getApprovedValue();
        
        if (x != null) {
            try {
                String textData = x.firstChildsTextContext("Data");

                MXMessage template = MXMessageFactory.fromCCXMLText(0, textData, 0);
                _status._base = template;
                //_status.refillGate();
                CXNode value = x.firstChild("Value");
                if (value != null) {
                    int minValue = value._listAttributes.numberOfName("Min", -1);
                    int maxValue = value._listAttributes.numberOfName("Max", -1);
                    int offsetValue = value._listAttributes.numberOfName("Offset", 0);
                    int defaultValue = value._listAttributes.numberOfName("Default", 0);

                    if (minValue >= 0 && maxValue >= 0) {
                        template.setValue(new RangedValue(defaultValue, minValue + offsetValue, maxValue + offsetValue));
                    }
                }
                CXNode gate = x.firstChild("Gate");
                if (gate != null) {                    
                    int minGate = gate._listAttributes.numberOfName("Min", -1);
                    int maxGate = gate._listAttributes.numberOfName("Max", -1);
                    int offsetGate = gate._listAttributes.numberOfName("Offset", 0);
                    int defaultGate =  gate._listAttributes.numberOfName("Default", 0);

                    if (minGate >= 0 && maxGate >= 0) {
                        template.setGate(new RangedValue(defaultGate, minGate + offsetGate, maxGate + offsetGate));
                    }
                }

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
        int newGate = wrap._value;
        int oldGate = _status._base.getGate()._var;

        readBufferFromPanelSlider();
        if (oldGate != newGate) {
            writeBufferToPanelSlider();
        }
    }//GEN-LAST:event_jComboBoxGateActionPerformed

    private void jComboBoxChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxChannelActionPerformed
        if (skipDataExchange) {
            return;
        }
        Object sel = jComboBoxChannel.getModel().getSelectedItem();
        MXWrap<Integer> wrap = (MXWrap<Integer>) sel;
        int channel = wrap._value;
        if (_status._base.getChannel() != channel) {
            _status._base.setChannel(channel);
            writeBufferToPanelSlider();
        }
    }//GEN-LAST:event_jComboBoxChannelActionPerformed

    private void jTextFieldMemoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldMemoActionPerformed
    }//GEN-LAST:event_jTextFieldMemoActionPerformed

    static final String INTERNAL_PROGINC = "Program INC";
    static final String INTERNAL_PROGDEC = "Program DEC";
    static final String INTERNAL_DATARPN = "Dataentry RPN";
    static final String INTERNAL_DATANRPN = "Dataentry NRPN";

    MXMessageCapture _capture = null;

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        _capture = new MXMessageCapture();
        MXMain.setCapture(_capture);
        MXMessageCapturePanel panel = new MXMessageCapturePanel();
        MXModalFrame.showAsDialog(this, panel, "Capture ...");
        GateInfomation retval = panel._selected;
        if (retval != null) {
            /* TODO
            _status._base = retval._parent.channel;
            _status._base = MXTemplate.fromCCXMLText(_status._port, retval._parent.dtext, _status._channel);
            _status._gate = new RangedValue(retval._gate, retval._hitLoValue, retval._hitHiValue);
            */
            String dtext = retval._parent.dtext;

            writeBufferToPanelSlider();
            MXMessage message = _status.toMXMessage(new MXTiming());

            if (message.isCommand(MXMidi.COMMAND_CH_NOTEOFF)) {
                int z = JOptionPane.showConfirmDialog(
                        this,
                         "Seems you choiced Note Off\n"
                        + "You want to use Note ON?",
                         "Offer (adjust value range)",
                         JOptionPane.YES_NO_OPTION);
                if (z == JOptionPane.YES_OPTION) {
                    message = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_CH_NOTEON + message.getChannel(), message.getData1(), 127);
                    _status.setBaseMessage(message);
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

    private void jButtonMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMenuActionPerformed
        JPopupMenu popup = new JPopupMenu();
        String text = jTextFieldMemo.getText();
        JMenuItem menu;
        
        menu = new JMenuItem("Note");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _status.fillNote(12 * 5);
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });
        popup.add(menu);

        menu = new JMenuItem("Control Change");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _status.fillControlChange();
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });
        popup.add(menu);

        menu = new JMenuItem("DataEntry RPN");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _status.fillDataentry(true, 0, 0, RangedValue.ZERO7);
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });

        popup.add(menu);

        menu = new JMenuItem("DataEntry NRPN");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _status.fillDataentry(false, 0, 0, RangedValue.ZERO7);
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });
        popup.add(menu);

        menu = new JMenuItem("Program +1");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MXMessage message = MXMessageFactory.fromCCXMLText(_status._port, "@PROG_INC", _status._base.getChannel());
                _status.setBaseMessage(message);
                MGStatusPanel.this.writeBufferToPanelSlider();
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });
        popup.add(menu);

        menu = new JMenuItem("Program -1");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MXMessage message = MXMessageFactory.fromCCXMLText(_status._port, "@PROG_DEC", _status._base.getChannel());
                _status.setBaseMessage(message);
                MGStatusPanel.this.writeBufferToPanelSlider();
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });
        popup.add(menu);

        popup.show(jButtonMenu, 0, jButtonMenu.getHeight());
    }//GEN-LAST:event_jButtonMenuActionPerformed

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

        if (jCheckBoxCC14bit.isEnabled() && jCheckBoxCC14bit.isSelected()) {
            _status._ccPair14 = true;
        } else {
            _status._ccPair14 = false;
        }
        skipDataExchange = false;
        writeBufferToPanelSlider();
        disableUnusedOnPanel();
    }//GEN-LAST:event_jCheckBoxCC14bitActionPerformed

    private void jCheckBoxCustomRangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxCustomRangeActionPerformed
        boolean sel = jCheckBoxCustomRange.isSelected();
        jSpinnerMin.setEnabled(sel);
        jSpinnerMax.setEnabled(sel);
    }//GEN-LAST:event_jCheckBoxCustomRangeActionPerformed

    class NavigatorForCommand extends NavigatorForText implements INavigator2<String> {
        public NavigatorForCommand(String text) {
            super(text, "Input Command");
        }
        
        @Override
        public boolean validatePromptResult(String text) {
            ParamsOfNavigator<String> param = getParamsOfNavigator();
            MXMessage message = MXMessageFactory.fromCCXMLText(0, text, 0, null, null);
            if (message == null) {
                JOptionPane.showMessageDialog(this, "Compile message failed.", "Error", JOptionPane.OK_OPTION);
                return false;
            }
            return true;
        }
        
    }
    
    private void jButtonTemplateInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTemplateInputActionPerformed
        String text = _status.getBaseMessage().getTemplateAsText();
        NavigatorForCommand textNavi = new NavigatorForCommand(text); 
        NavigatorUtil.showPrompt(this, textNavi);
        ParamsOfNavigator<String> result = textNavi.getParamsOfNavigator();
        if (result.isApproved()) {
            MXMessage message = MXMessageFactory.fromCCXMLText(0, result.getApprovedValue(), 0, null, null);
            if (message != null) {
                _status.setBaseMessage(message);
                writeBufferToPanelSlider();
                disableUnusedOnPanel();
            }
            else {
                JOptionPane.showMessageDialog(this, "Compile message failed.", "Error", JOptionPane.OK_OPTION);
            }
        }
    }//GEN-LAST:event_jButtonTemplateInputActionPerformed

    private void jComboBoxDrumOutChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxDrumOutChannelActionPerformed
        //TODO _status.setSwitchOutChannel(_drumOutChannel.readCombobox(jComboBoxDrumOutChannel));
        disableUnusedOnPanel();
    }//GEN-LAST:event_jComboBoxDrumOutChannelActionPerformed

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

    private void jCheckBoxDrumInToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDrumInToggleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxDrumInToggleActionPerformed

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

    private void jButtonNotesKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNotesKeysActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonNotesKeysActionPerformed

    private void jButtonResetValueRangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetValueRangeActionPerformed
        _status.resetCustomRange();;
        writeBufferToPanelSlider();
        writeBufferToPanelDrum();
    }//GEN-LAST:event_jButtonResetValueRangeActionPerformed

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
            String command  = e.getActionCommand();
            if (command.equals(INTERNAL_PROGINC)) {
                MXMessage message = MXMessageFactory.fromCCXMLText(_status._port, "@PROG_INC", _status._base.getChannel());
                _status.setBaseMessage(message);
                MGStatusPanel.this.writeBufferToPanelSlider();
                disableUnusedOnPanel();
                return;
            }
            if (command.equals(INTERNAL_PROGDEC)) {
                MXMessage message = MXMessageFactory.fromCCXMLText(_status._port, "@PROG_DEC", _status._base.getChannel());
                _status.setBaseMessage(message);
                MGStatusPanel.this.writeBufferToPanelSlider();
                disableUnusedOnPanel();
                return;
            }
            if (command.equals(INTERNAL_DATARPN)) {
                _status.fillDataentry(true, 0, 0, RangedValue.ZERO7);
                MGStatusPanel.this.writeBufferToPanelSlider();
                disableUnusedOnPanel();
            }
            if (command.equals(INTERNAL_DATANRPN)) {
                _status.fillDataentry(false, 0, 0, RangedValue.ZERO7);
                MGStatusPanel.this.writeBufferToPanelSlider();
                disableUnusedOnPanel();
            }
        }
    };

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonFromList;
    private javax.swing.JButton jButtonMenu;
    private javax.swing.JButton jButtonNotesKeys;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JButton jButtonResetValueRange;
    private javax.swing.JButton jButtonSequenceFileBrowse;
    private javax.swing.JButton jButtonTemplateInput;
    private javax.swing.JCheckBox jCheckBoxCC14bit;
    private javax.swing.JCheckBox jCheckBoxCustomRange;
    private javax.swing.JCheckBox jCheckBoxDrumInToggle;
    private javax.swing.JCheckBox jCheckBoxSequencerFilterNote;
    private javax.swing.JCheckBox jCheckBoxSequencerSeekStart;
    private javax.swing.JCheckBox jCheckBoxSequencerSingleTrack;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JComboBox<String> jComboBoxChannel;
    private javax.swing.JComboBox<String> jComboBoxDrumOutChannel;
    private javax.swing.JComboBox<String> jComboBoxDrumOutPort;
    private javax.swing.JComboBox<String> jComboBoxGate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelBlank1;
    private javax.swing.JLabel jLabelBlank2;
    private javax.swing.JLabel jLabelBlank3;
    private javax.swing.JLabel jLabelBlank4;
    private javax.swing.JLabel jLabelBlank5;
    private javax.swing.JLabel jLabelBlank6;
    private javax.swing.JLabel jLabelBlank7;
    private javax.swing.JLabel jLabelDefaultName;
    private javax.swing.JLabel jLabelInputText;
    private javax.swing.JLabel jLabelStartWith;
    private javax.swing.JPanel jPanelOutput;
    private javax.swing.JPanel jPanelPage1;
    private javax.swing.JPanel jPanelPage2;
    private javax.swing.JPanel jPanelTabLink;
    private javax.swing.JPanel jPanelTabNotes;
    private javax.swing.JPanel jPanelTabProgram;
    private javax.swing.JPanel jPanelTabSequener;
    private javax.swing.JPanel jPanelTabTemplate;
    private javax.swing.JPanel jPanelValue;
    private javax.swing.JRadioButton jRadioButtonDrumTypeCustom;
    private javax.swing.JRadioButton jRadioButtonDrumTypeDontSend;
    private javax.swing.JRadioButton jRadioButtonDrumTypeLink;
    private javax.swing.JRadioButton jRadioButtonDrumTypeNotes;
    private javax.swing.JRadioButton jRadioButtonDrumTypeProgram;
    private javax.swing.JRadioButton jRadioButtonDrumTypeSame;
    private javax.swing.JRadioButton jRadioButtonDrumTypeSong;
    private javax.swing.JRadioButton jRadioButtonLinkKnob1;
    private javax.swing.JRadioButton jRadioButtonLinkKnob2;
    private javax.swing.JRadioButton jRadioButtonLinkKnob3;
    private javax.swing.JRadioButton jRadioButtonLinkKnob4;
    private javax.swing.JRadioButton jRadioButtonSlider;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinnerDrumInMax;
    private javax.swing.JSpinner jSpinnerDrumInMin;
    private javax.swing.JSpinner jSpinnerDrumInMouse;
    private javax.swing.JSpinner jSpinnerDrumProgLSB;
    private javax.swing.JSpinner jSpinnerDrumProgPC;
    private javax.swing.JSpinner jSpinnerDurmProgMSB;
    private javax.swing.JSpinner jSpinnerMax;
    private javax.swing.JSpinner jSpinnerMin;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextFieldHarmonyNoteList;
    private javax.swing.JTextField jTextFieldMemo;
    private javax.swing.JTextField jTextFieldName;
    private javax.swing.JTextField jTextFieldSequenceFile;
    private javax.swing.JTextField jTextFieldTemplate;
    // End of variables declaration//GEN-END:variables

    public void showsub1() {
/*
        if (_status.getSwitchOutOnType() == MGStatus.SWITCH_OUT_ON_CUSTOM) {
            jLabelOutputOnText.setText("Custom " + _status.getSwitchOutOnText() + "(Gate:" + _status.getSwitchOutOnTextGate() + ")");
        } else if (_status.getSwitchOutOnType() == MGStatus.SWITCH_OUT_ON_SAME_AS_INPUT) {
            jLabelOutputOnText.setText("Same " + _status.getTemplateAsText() + "(Gate:" + _status._gate + ")");
        } else {
            jLabelOutputOnText.setText("Unknwon(" + _status.getSwitchOutOnType());
        }
        if (_status.getSwitchOutOffType() == MGStatus.SWITCH_OUT_OFF_CUSTOM) {
            jLabelOutputOffText.setText("Custom " + _status.getSwitchOutOffText() + "(Gate:" + _status.getSwitchOutOffTextGate() + ")");
        } else if (_status.getSwitchOutOffType() == MGStatus.SWITCH_OUT_OFF_NONE) {
            jLabelOutputOffText.setText("None");
        } else if (_status.getSwitchOutOffType() == MGStatus.SWITCH_OUT_OFF_SAME_AS_INPUT) {
            jLabelOutputOffText.setText("Same(Input)" + _status.getTemplateAsText() + "(Gate:" + _status._gate + ")");
        } else if (_status.getSwitchOutOffType() == MGStatus.SWITCH_OUT_OFF_SAME_AS_OUTPUT_ON) {
            jLabelOutputOffText.setText("Same(Output-ON)" + _status.getTemplateAsText() + "(Gate:" + _status.getSwitchOutOnTextGate() + ")");
        } else {
            jLabelOutputOffText.setText("Unknwon(" + _status.getSwitchOutOffType());
        }
*/
    }
}
