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
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SpinnerNumberModel;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.domino.CCXMLNode;
import jp.synthtarou.midimixer.libs.domino.PickerForControlChange;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.capture.MXMessageCapture;
import jp.synthtarou.midimixer.libs.midi.capture.MXMessageCapturePanel;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.capture.GateInfomation;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;
import jp.synthtarou.midimixer.libs.swing.MXSwingFileChooser;
import jp.synthtarou.midimixer.libs.swing.SafeSpinnerNumberModel;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGStatusConfig extends javax.swing.JPanel {
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

    public MGStatusConfig(MX32MixerProcess process, MGStatus status) {
        
        skipDataExchange = true;
        initComponents();

        _process = process;
        _status = status;
        _templateStartWith = _status.getTemplate();

        jLabelStartWith.setText(_templateStartWith.toString());
        jLabelBlank2.setText("");
        jLabelBlank1.setText("");

        setPreferredSize(new Dimension(900, 700));
        ButtonGroup group = new ButtonGroup();
        group.add(jRadioButtonOn);
        group.add(jRadioButtonOnOff);
        group.add(jRadioButtonHarmony);
        group.add(jRadioButtonSequence);

        if (_status.getUiType() == MGStatus.TYPE_DRUMPAD) {
            jTabbedPane1.setEnabledAt(1, true);
        }else {
            jTabbedPane1.setEnabledAt(1, false);
        }

        skipDataExchange = false;
        
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

            if (_status.getName() == null) {
                _status.setName("");
            }
            if (jTextFieldName.getText().equals(_status.getName()) == false) {
                jTextFieldName.setText(_status.getName());
            }
            if (_status.getMemo() == null) {
                _status.setMemo("");
            }
            if (jTextFieldMemo.getText().equals(_status.getMemo()) == false) {
                jTextFieldMemo.setText(_status.getMemo());
            }

            jTextFieldTextCommand.setText(_status.toTemplateText());
            jLabelStartWith.setText(_templateStartWith.toString());

            _channelModel.writeComboBox(jComboBoxChannel, _status.getChannel());
            
            MXMessage message = _status.toMXMessage(null);
            boolean initTurn = true;
            if (jComboBoxGate.getModel() instanceof MXWrapList) {
                initTurn = false;
            }

            jLabelNameDefault.setText("Empty means-> '" + message.toShortString() + "'");
            
            int command = message.getStatus() & 0xf0;
            int gateValue = _status.getGate()._var;

            if (command == MXMidi.COMMAND_CHANNELPRESSURE
              ||command == MXMidi.COMMAND_NOTEON
              ||command == MXMidi.COMMAND_NOTEOFF) {
                jComboBoxGate.setModel(_keyGateModel);
                if (initTurn || ((MXWrap<Integer>)jComboBoxGate.getSelectedItem()).value !=  gateValue) {
                    _keyGateModel.writeComboBox(jComboBoxGate, gateValue);
                }
            }else if (command == MXMidi.COMMAND_CONTROLCHANGE) {
                jComboBoxGate.setModel(_ccGateModel);
                if (initTurn || ((MXWrap<Integer>)jComboBoxGate.getSelectedItem()).value != gateValue) {
                    _ccGateModel.writeComboBox(jComboBoxGate, gateValue);
                }
            }else {
                jComboBoxGate.setModel(_normalGateModel);
                if (initTurn || ((MXWrap<Integer>)jComboBoxGate.getSelectedItem()).value != gateValue) {
                    _normalGateModel.writeComboBox(jComboBoxGate, gateValue);
                }
            }

            jSpinnerOutOnValueFixed.setModel(new SafeSpinnerNumberModel(_status.getSwitchOutOnValueFixed(), 0, 16383, 1));
            jSpinnerOutOffValueFixed.setModel(new SafeSpinnerNumberModel(_status.getSwitchOutOffValueFixed(), 0, 16383, 1));

            jSpinnerMin.setModel(new SafeSpinnerNumberModel(_status.getValue()._min, 0, 128*128 -1, 1));
            jSpinnerMax.setModel(new SafeSpinnerNumberModel(_status.getValue()._max, 0, 128*128 -1 , 1));

            _rpnMSBModel.writeComboBox(jComboBoxMSB, _status.getDataeroomMSB());
            _rpnLSBModel.writeComboBox(jComboBoxLSB, _status.getDataroomLSB());
            jCheckBoxCC14bit.setSelected(_status.isValuePairCC14());
            jCheckBoxCustomRange.setSelected(_status.hasCustomRange());
        }finally {
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
        
        if (data.getName() == null) {
            data.setName("");
        }
        if (data.getName().length() == 0) {
            //NP result.add("Name is empty. Kontrol will use short name of message.");
        }

        if (data.getMemo() == null) {
            data.setMemo("");
        }
        if (data.getMemo().length() == 0) {
            //NP result.add("Memo is empty. Thats No Problem.");
        }
        
        if (data.getTemplate() == null) {
            result.add("TextCommand is empty. Please fill it.");
        }else {
            try {
                MXMessage message = data.toMXMessage(null);
                if (message == null) {
                    result.add("TextCommand [" + data.toTemplateText() + "] is not valid.");
                }
                if (message.isDataentry()) {
                    if (message.getVisitant() == null || message.getVisitant().getDataroomType() == MXVisitant.HAVE_VAL_NOT) {
                        result.add("If you need DATAENTRY. try QuickMenu.");
                    }else  if (message.getVisitant() != null && (message.getVisitant().isHaveDataentryRPN() || message.getVisitant().isHaveDataentryNRPN())) {
                        //OK
                    }
                }else if (message.isMessageTypeChannel() && message.isCommand(MXMidi.COMMAND_CONTROLCHANGE) && !message.isDataentry()) {
                    String newText = "@CC #GL #VL";
                    if (data.toTemplateText().equals(newText) == false) {
                        String errorText = "ControlChange's Text Command can be '" + newText + "'";
                        if (canDialog && JOptionPane.showConfirmDialog(this, errorText, "Smart Replace", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            skipDataExchange = true;
                            data.setTemplateAsText(newText, message.getChannel());
                            jTextFieldTextCommand.setText(newText);
                            data.setGate(message.getGate());
                            _ccGateModel.writeComboBox(jComboBoxGate, data.getGate()._var);
                            skipDataExchange = false;
                        }else {
                            result.add(errorText);
                        }
                    }
                }
                
            }catch(Exception e) {
                result.add("TextCommand [" + data.getTemplate() + "] is not valid.");
            }
        }
        
        if (data.getChannel() >= 0 && data.getChannel() < 16) {
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
        }else {
            str.append("**Validation Result**\n");
            for (String line : textValidate) {
                str.append("\n");
                str.append(line);
            }
        }
        if (str.charAt(str.length() - 1) == '\n') {
            str.setLength(str.length() - 1);
        }
        jTextAreaValidation.setText(str.toString());
    }
    
    public void printTemplateResult() {
        StringBuffer str = new StringBuffer();
        if (textTemplate.size() == 0) {
        }else {
            boolean firstColumn = true;
            for (String line : textTemplate) {
                if (firstColumn) {
                    str.append("**Template [" + line + "] Need Fill*\n");
                    firstColumn = false;
                }else {
                    str.append(line);
                    str.append("\n");
                }
            }
        }
        jTextAreaTemplate.setText(str.toString());
    }

    public void validateBufferSubDrum(ArrayList<String> result) {
        if (_status.getUiType() != MGStatus.TYPE_DRUMPAD) {
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
            boolean sel =  _status.hasCustomRange();
            jSpinnerMin.setEnabled(sel);
            jSpinnerMax.setEnabled(sel);
        }
        
        boolean isdataentry = false;

        MXTemplate temp = _status.getTemplate();
        if (temp.get(0) == MXTemplate.DTEXT_RPN
          ||temp.get(0) == MXTemplate.DTEXT_NRPN) {
            isdataentry = true;
        }
        jComboBoxLSB.setEnabled(isdataentry);
        jComboBoxMSB.setEnabled(isdataentry);

        if (_status.getUiType() != MGStatus.TYPE_DRUMPAD) {
            return;
        }
        int type = getDrumType();

        boolean zoneX = false;
        boolean zoneA = false;
        boolean zoneB = false;
        boolean zoneC = false;
        boolean zoneD = false;
        boolean zoneE = false;
        
        switch(type){
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
        
        jButtonInputEdit.setEnabled(zoneX);
        jComboBoxInputType.setEnabled(zoneX);
        
        jButtonOutputOnEdit.setEnabled(zoneA);
        jComboBoxOutputOnType.setEnabled(zoneA);
        jComboBoxOutputOnValue.setEnabled(zoneA);
        x = _drumOutOnValue.readCombobox(jComboBoxOutputOnValue);
        jSpinnerOutOnValueFixed.setEnabled(zoneA);

        jComboBoxDrumPort.setEnabled(zoneA || zoneB || zoneC || zoneD);
        jComboBoxDrumChannel.setEnabled(zoneA || zoneB || zoneC || zoneD);
        
        jButtonOutputOffEdit.setEnabled(zoneB);
        jComboBoxOutputOffType.setEnabled(zoneB);
        jComboBoxOutputOffValue.setEnabled(zoneB);
        x = _drumOutOffValue.readCombobox(jComboBoxOutputOffValue);
        jSpinnerOutOffValueFixed.setEnabled(zoneB && x == MGStatus.SWITCH_OUT_OFF_VALUE_FIXED);
        
        jCheckBoxToggle.setEnabled(zoneC);
        
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
    }

    MXWrapList<Integer> _drumInputType;
    MXWrapList<Integer> _drumOutOnType;
    MXWrapList<Integer> _drumOutOnValue;
    MXWrapList<Integer> _drumHarmonyVelocityType;
    MXWrapList<Integer> _drumOutOffType;
    MXWrapList<Integer> _drumOutOffValue;

    MXWrapList<Integer> _drumOutChannel;
    MXWrapList<Integer> _drumOutPort;

    public void writeBufferToPanelDrum() {
         if (skipDataExchange) {
            return;
        }
        if (_status.getUiType() != MGStatus.TYPE_DRUMPAD) {
            return;
        }
        if (_drumInputType == null) {

            _drumInputType = new MXWrapList();
            _drumInputType.addNameAndValue("value >= min + 1", _status.SWITCH_ON_IF_PLUS1);
            _drumInputType.addNameAndValue("any", _status.SWITCH_ON_WHEN_ANY);
            //_drumInputType.addNameAndValue("everything", _status.SWITCH_ON_IF_ONESHOT);
            _drumInputType.addNameAndValue("value >= half", _status.SWITCH_ON_IF_OVER_HALF);
            _drumInputType.addNameAndValue("value == max", _status.SWITCH_ON_IF_MAX);
            jComboBoxInputType.setModel(_drumInputType);

            _drumOutOnType = new MXWrapList<>();
            _drumOutOnType.addNameAndValue("same as input", _status.SWITCH_OUT_ON_SAME_AS_INPUT);
            _drumOutOnType.addNameAndValue("custom", _status.SWITCH_OUT_ON_CUSTOM);
            jComboBoxOutputOnType.setModel(_drumOutOnType);

            _drumOutOnValue = new MXWrapList<>();
            _drumOutOnValue.addNameAndValue("on value = as input", _status.SWITCH_OUT_ON_VALUE_AS_INPUT);
            _drumOutOnValue.addNameAndValue("on value = as input+1  (... max)", _status.SWITCH_OUT_ON_VALUE_AS_INPUT_PLUS1);
            _drumOutOnValue.addNameAndValue("fixed", _status.SWITCH_OUT_ON_VALUE_FIXED);
            jComboBoxOutputOnValue.setModel(_drumOutOnValue);

            jSpinnerOutOnValueFixed.setModel(new SpinnerNumberModel(127, 0, 16383, 1));

            _drumOutOffType = new MXWrapList();
            _drumOutOffType.addNameAndValue("none", _status.SWITCH_OUT_OFF_NONE);
            _drumOutOffType.addNameAndValue("same as input", _status.SWITCH_OUT_OFF_SAME_AS_INPUT);
            _drumOutOffType.addNameAndValue("same as output on", _status.SWITCH_OUT_OFF_SAME_AS_OUTPUT_ON);
            _drumOutOffType.addNameAndValue("custom", _status.SWITCH_OUT_OFF_CUSTOM);
            jComboBoxOutputOffType.setModel(_drumOutOffType);

            _drumOutOffValue = new MXWrapList<>();
            _drumOutOffValue.addNameAndValue("0", _status.SWITCH_OUT_OFF_VALUE_0);
            _drumOutOffValue.addNameAndValue("same as min", _status.SWITCH_OUT_OFF_VALUE_SAME_AS_MIN);
            _drumOutOffValue.addNameAndValue("same as input", _status.SWITCH_OUT_OFF_VALUE_SAME_AS_INPUT);
            _drumOutOffValue.addNameAndValue("fixed value", _status.SWITCH_OUT_OFF_VALUE_FIXED);

            jComboBoxOutputOffValue.setModel(_drumOutOffValue);

            jSpinnerOutOffValueFixed.setModel(new SpinnerNumberModel(127, 0, 16383, 1));

            _drumHarmonyVelocityType = new MXWrapList<>();
            _drumHarmonyVelocityType.addNameAndValue("same as input", _status.SWITCH_HARMONY_VELOCITY_SAME_AS_INPUT);
            _drumHarmonyVelocityType.addNameAndValue("fixed value", _status.SWITCH_HARMONY_VELOCITY_FIXED);
            jComboBoxHarmonyVelocityType.setModel(_drumHarmonyVelocityType);

            jSpinnerHarmonyVelocityFixed.setModel(new SpinnerNumberModel(127, 0, 127, 1));

            jTextFieldHarmonyNoteList.setText("");
            jTextFieldSequenceFile.setText("");
            jLabelBlank2.setText("");
            
            _drumOutPort = MXMidi.listupPortAssigned(false);
            _drumOutChannel = MXMidi.listupChannel(false);

            jComboBoxDrumPort.setModel(_drumOutPort);
            jComboBoxDrumChannel.setModel(_drumOutChannel);
        }
        
        int switchType = _status.getSwitchType();
        
        jRadioButtonOn.setSelected(switchType == MGStatus.SWITCH_TYPE_ON);
        jRadioButtonOnOff.setSelected(switchType == MGStatus.SWITCH_TYPE_ONOFF);
        jRadioButtonHarmony.setSelected(switchType == MGStatus.SWITCH_TYPE_HARMONY);
        jRadioButtonSequence.setSelected(switchType == MGStatus.SWITCH_TYPE_SEQUENCE);

        jComboBoxInputType.setSelectedIndex(_drumInputType.indexOfValue(_status.getSwitchInputType()));
        jComboBoxOutputOnType.setSelectedIndex(_drumOutOnType.indexOfValue(_status.getSwitchOutOnType()));
        jComboBoxOutputOnValue.setSelectedIndex(_drumOutOnValue.indexOfValue(_status.getSwitchOutOnTypeOfValue()));
        showsub1();
        jSpinnerOutOnValueFixed.setValue(_status.getSwitchOutOnValueFixed());
        jComboBoxOutputOffType.setSelectedIndex(_drumOutOffType.indexOfValue(_status.getSwitchOutOffType()));
        jComboBoxOutputOffValue.setSelectedIndex(_drumOutOffValue.indexOfValue(_status.getSwitchOutOffTypeOfValue()));
        jSpinnerOutOffValueFixed.setValue(_status.getSwitchOutOffValueFixed());
        jCheckBoxToggle.setSelected(_status.isSwitchWithToggle());

        jComboBoxHarmonyVelocityType.setSelectedIndex(_drumHarmonyVelocityType.indexOfValue(_status.getSwitchHarmonyVelocityType()));
        jSpinnerHarmonyVelocityFixed.setValue(_status.getSwitchHarmonyVelocityFixed());
        jTextFieldHarmonyNoteList.setText(_status.getSwitchHarmonyNotes());
        jTextFieldSequenceFile.setText(_status.getSwitchSequencerFile());

        _drumOutPort.writeComboBox(jComboBoxDrumPort, _status.getSwitchOutPort());
        _drumOutChannel.writeComboBox(jComboBoxDrumChannel, _status.getSwitchOutChannel());

        jCheckBoxSequencerSeekStart.setSelected(_status.isSwitchSequenceSeekStart());
        jCheckBoxSequencerSingleTrack.setSelected(_status.isSwitchSequencerToSingltTrack());
        jCheckBoxSequencerFilterNote.setSelected(_status.isSwitchSequencerFilterNote());
    }
    
    public void readBufferFromPanelSlider() {
        if (skipDataExchange) {
            return;
        }
        _status.setName(jTextFieldName.getText());
        _status.setMemo(jTextFieldMemo.getText());
        _status.setChannel((int) _channelModel.readCombobox(this.jComboBoxChannel));
        _status.setTemplateAsText(jTextFieldTextCommand.getText(), _status.getChannel());
        MXWrap<Integer> x = (MXWrap<Integer>)jComboBoxGate.getSelectedItem();
        _status.setGate(RangedValue.new7bit(x.value));
        
        if (jCheckBoxCustomRange.isSelected()) {
            int min = (Integer)jSpinnerMin.getValue();
            int max = (Integer)jSpinnerMax.getValue();
            _status.setCustomRange(min, max);
        }
        else {
            _status.resetCustomRange();
        }

        _status.setDataroomMSB(_rpnMSBModel.readCombobox(jComboBoxMSB));
        _status.setDataroomLSB(_rpnLSBModel.readCombobox(jComboBoxLSB));

        boolean changed = false;
        try {
            _status.setDataroomType(MXVisitant.ROOMTYPE_NODATA);
            MXTemplate template = _status.getTemplate();
            MXMessage message = template.buildMessage(_status.getPort(),  _status.getChannel(), _status.getGate(), _status.getValue());
            skipDataExchange = true;

            if (message.getGate()._var != _status.getGate()._var) {
                _status.setGate(message.getGate());
                changed = true;
            }
            
            int d = message.getTemplate().get(0);
            
            if (d == MXTemplate.DTEXT_RPN) {
                _status.setDataroomType(MXVisitant.ROOMTYPE_RPN);
                changed = true;
            }else if (d == MXTemplate.DTEXT_NRPN) {
                _status.setDataroomType(MXVisitant.ROOMTYPE_NRPN);
                changed = true;
            }
            skipDataExchange = false;
            
            if (changed) {
                writeBufferToPanelSlider();
            }
        }catch(Exception e) {
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
            _status.setValuePairCC14(jCheckBoxCC14bit.isSelected());
            jCheckBoxCC14bit.setEnabled(true);
        }else {
            _status.setValuePairCC14(false);
            jCheckBoxCC14bit.setEnabled(false);
            jCheckBoxCC14bit.setSelected(false);
        }
    }
    
    public void readBufferFromPanelDrum() {
        if (skipDataExchange) {
            return;
        }
        if (_status.getUiType() != MGStatus.TYPE_DRUMPAD) {
            return;
        }
        MGStatus data = _status;
        data.setSwitchType(getDrumType());
        data.setSwitchInputType(_drumInputType.readCombobox(jComboBoxInputType));
        data.setSwitchOutOnType(_drumOutOnType.readCombobox(jComboBoxOutputOnType));
        data.setSwitchOutOnTypeOfValue(_drumOutOnValue.readCombobox(jComboBoxOutputOnValue));
        data.setSwitchOutOnValueFixed((int)jSpinnerOutOnValueFixed.getValue());
        data.setSwitchOutOffType(_drumOutOffType.readCombobox(jComboBoxOutputOffType));
        data.setSwitchOutOffTypeOfValue(_drumOutOffValue.readCombobox(jComboBoxOutputOffValue));
        data.setSwitchOutOffValueFixed((int)jSpinnerOutOffValueFixed.getValue());
        data.setSwitchWithToggle(jCheckBoxToggle.isSelected());
        data.setSwitchOutPort(_drumOutPort.readCombobox(jComboBoxDrumPort));
        data.setSwitchOutChannel(_drumOutChannel.readCombobox(jComboBoxDrumChannel));
        data.setSwitchHarmonyVelocityType(_drumHarmonyVelocityType.readCombobox(jComboBoxHarmonyVelocityType));
        data.setSwitchHarmonyVelocityFixed((int)jSpinnerHarmonyVelocityFixed.getValue());
        data.setSwitchHarmonyNotes(jTextFieldHarmonyNoteList.getText());
        data.setSwitchSequencerFile(jTextFieldSequenceFile.getText());

        data.setSwitchSequenceSeekStart(jCheckBoxSequencerSeekStart.isSelected());
        data.setSwitchSequencerToSingltTrack(jCheckBoxSequencerSingleTrack.isSelected());
        data.setSwitchSequencerFilterNote(jCheckBoxSequencerFilterNote.isSelected());
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
        jCheckBoxToggle = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        jComboBoxInputType = new javax.swing.JComboBox<>();
        jRadioButtonOnOff = new javax.swing.JRadioButton();
        jRadioButtonHarmony = new javax.swing.JRadioButton();
        jRadioButtonSequence = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jRadioButtonOn = new javax.swing.JRadioButton();
        jLabel18 = new javax.swing.JLabel();
        jComboBoxOutputOnType = new javax.swing.JComboBox<>();
        jComboBoxOutputOffType = new javax.swing.JComboBox<>();
        jSpinnerHarmonyVelocityFixed = new javax.swing.JSpinner();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jComboBoxOutputOffValue = new javax.swing.JComboBox<>();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jTextFieldHarmonyNoteList = new javax.swing.JTextField();
        jButtonSequenceFileBrowse = new javax.swing.JButton();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jTextFieldSequenceFile = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jComboBoxOutputOnValue = new javax.swing.JComboBox<>();
        jSpinnerOutOnValueFixed = new javax.swing.JSpinner();
        jSpinnerOutOffValueFixed = new javax.swing.JSpinner();
        jButtonOutputOffEdit = new javax.swing.JButton();
        jButtonOutputOnEdit = new javax.swing.JButton();
        jLabelOutputOnText = new javax.swing.JLabel();
        jLabelOutputOffText = new javax.swing.JLabel();
        jComboBoxDrumChannel = new javax.swing.JComboBox<>();
        jButtonHarmonyEdit = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jLabelBlank2 = new javax.swing.JLabel();
        jLabelInputText = new javax.swing.JLabel();
        jButtonInputEdit = new javax.swing.JButton();
        jComboBoxHarmonyVelocityType = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jComboBoxDrumPort = new javax.swing.JComboBox<>();
        jCheckBoxSequencerSeekStart = new javax.swing.JCheckBox();
        jCheckBoxSequencerSingleTrack = new javax.swing.JCheckBox();
        jCheckBoxSequencerFilterNote = new javax.swing.JCheckBox();
        jLabel32 = new javax.swing.JLabel();
        jButtonQuickMenuDrum = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaValidation = new javax.swing.JTextArea();
        jButtonCheckValue = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaTemplate = new javax.swing.JTextArea();

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

        jTabbedPane1.addTab("Configuration", jPanel2);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jCheckBoxToggle.setText("Toggle Switch");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jCheckBoxToggle, gridBagConstraints);

        jLabel13.setText("When Turn ON");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel13, gridBagConstraints);

        jComboBoxInputType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxInputTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jComboBoxInputType, gridBagConstraints);

        jRadioButtonOnOff.setText("ON / OFF");
        jRadioButtonOnOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonOnOffActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jRadioButtonOnOff, gridBagConstraints);

        jRadioButtonHarmony.setText("Chord Harmony");
        jRadioButtonHarmony.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonHarmonyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jRadioButtonHarmony, gridBagConstraints);

        jRadioButtonSequence.setText("Sequence");
        jRadioButtonSequence.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonSequenceActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jRadioButtonSequence, gridBagConstraints);

        jLabel6.setText("Output Type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jLabel6, gridBagConstraints);

        jLabel16.setText("Ouput [On]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel16, gridBagConstraints);

        jLabel17.setText("Output [Off]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel17, gridBagConstraints);

        jRadioButtonOn.setText("ON (Single)");
        jRadioButtonOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonOnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jRadioButtonOn, gridBagConstraints);

        jLabel18.setText("jLabel18");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel18, gridBagConstraints);

        jComboBoxOutputOnType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxOutputOnTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jComboBoxOutputOnType, gridBagConstraints);

        jComboBoxOutputOffType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxOutputOffTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jComboBoxOutputOffType, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jSpinnerHarmonyVelocityFixed, gridBagConstraints);

        jLabel20.setText("Type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jLabel20, gridBagConstraints);

        jLabel21.setText("Value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jLabel21, gridBagConstraints);

        jLabel22.setText("Type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jLabel22, gridBagConstraints);

        jComboBoxOutputOffValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxOutputOffValueActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jComboBoxOutputOffValue, gridBagConstraints);

        jLabel24.setText("Harmony/Sequence");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabel24, gridBagConstraints);

        jLabel25.setText("Value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jLabel25, gridBagConstraints);

        jTextFieldHarmonyNoteList.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
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
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jButtonSequenceFileBrowse, gridBagConstraints);

        jLabel26.setText("Port/Channel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jLabel26, gridBagConstraints);

        jLabel27.setText("NoteList");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jLabel27, gridBagConstraints);

        jLabel28.setText("Velocity");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jLabel28, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jTextFieldSequenceFile, gridBagConstraints);

        jLabel30.setText("SMF File");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jLabel30, gridBagConstraints);

        jComboBoxOutputOnValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxOutputOnValueActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jComboBoxOutputOnValue, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jSpinnerOutOnValueFixed, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jSpinnerOutOffValueFixed, gridBagConstraints);

        jButtonOutputOffEdit.setText("Edit");
        jButtonOutputOffEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOutputOffEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jButtonOutputOffEdit, gridBagConstraints);

        jButtonOutputOnEdit.setText("Edit");
        jButtonOutputOnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOutputOnEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jButtonOutputOnEdit, gridBagConstraints);

        jLabelOutputOnText.setText("=");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabelOutputOnText, gridBagConstraints);

        jLabelOutputOffText.setText("=");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabelOutputOffText, gridBagConstraints);

        jComboBoxDrumChannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxDrumChannelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jComboBoxDrumChannel, gridBagConstraints);

        jButtonHarmonyEdit.setText("Edit");
        jButtonHarmonyEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHarmonyEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jButtonHarmonyEdit, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jSeparator1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jSeparator2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jSeparator3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jSeparator4, gridBagConstraints);

        jLabelBlank2.setText("BLANK");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.weighty = 1.0;
        jPanel3.add(jLabelBlank2, gridBagConstraints);

        jLabelInputText.setText("=");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabelInputText, gridBagConstraints);

        jButtonInputEdit.setText("Edit");
        jButtonInputEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInputEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jButtonInputEdit, gridBagConstraints);

        jComboBoxHarmonyVelocityType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxHarmonyVelocityTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jComboBoxHarmonyVelocityType, gridBagConstraints);

        jLabel8.setText("Fix or Mouse");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 9;
        jPanel3.add(jLabel8, gridBagConstraints);

        jLabel14.setText("Fix or Mouse");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 13;
        jPanel3.add(jLabel14, gridBagConstraints);

        jLabel19.setText("Fix or Mouse");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 17;
        jPanel3.add(jLabel19, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jComboBoxDrumPort, gridBagConstraints);

        jCheckBoxSequencerSeekStart.setText("Play Start Timing = 1st Note");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jCheckBoxSequencerSeekStart, gridBagConstraints);

        jCheckBoxSequencerSingleTrack.setText("Play in Single  Track");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jCheckBoxSequencerSingleTrack, gridBagConstraints);

        jCheckBoxSequencerFilterNote.setText("Only Play Note, Pitch, Wheel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 23;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jCheckBoxSequencerFilterNote, gridBagConstraints);

        jLabel32.setText("Play Option");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(jLabel32, gridBagConstraints);

        jButtonQuickMenuDrum.setText("QuickMenu");
        jButtonQuickMenuDrum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonQuickMenuDrumActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jButtonQuickMenuDrum, gridBagConstraints);

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
        gridBagConstraints.gridy = 1;
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
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButtonOK, gridBagConstraints);

        jTextAreaValidation.setEditable(false);
        jTextAreaValidation.setColumns(20);
        jTextAreaValidation.setRows(5);
        jScrollPane1.setViewportView(jTextAreaValidation);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jButtonCheckValue.setText("Validation");
        jButtonCheckValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCheckValueActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        add(jButtonCheckValue, gridBagConstraints);

        jTextAreaTemplate.setColumns(20);
        jTextAreaTemplate.setRows(5);
        jScrollPane2.setViewportView(jTextAreaTemplate);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane2, gridBagConstraints);
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
        MXUtil.closeOwnerWindow(this);
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        _okOption = false;
        MXUtil.closeOwnerWindow(this);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonFromListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFromListActionPerformed
        PickerForControlChange picker = new PickerForControlChange();
        MXUtil.showAsDialog(this, picker, "Picker");
        CCXMLNode x = picker.getTextReturn();

        if (x != null) {
            try {
                String textData = x.firstChildsTextContext("Data");

                MXTemplate template = MXMessageFactory.fromDtext(textData, this._channelModel.readCombobox(jComboBoxChannel).intValue());
                _status.setTemplate(template);
                _status.refillGate();

                CCXMLNode value = x.firstChild("Value");
                if (value != null) {                    
                    int minValue = value._listAttributes.numberOfName("Min", -1);
                    int maxValue = value._listAttributes.numberOfName("Max", -1);
                    int offsetValue = value._listAttributes.numberOfName("Offset", 0);
                    int defaultValue =  value._listAttributes.numberOfName("Default", 0);

                    if (minValue >= 0 && maxValue >= 0) {
                        _status.setCustomRange(minValue + offsetValue, maxValue + offsetValue);
                    }
                }
                /* unsupported
                CCXMLNode gate = x.firstChild("Gate");
                if (gate != null) {                    
                    int minGate = value._listAttributes.numberOfName("Min", -1);
                    int maxGate = value._listAttributes.numberOfName("Max", -1);
                    int offsetGate = value._listAttributes.numberOfName("Offset", 0);
                    int defaultGate =  value._listAttributes.numberOfName("Default", 0);

                    if (minGate >= 0 && maxGate >= 0) {
                        _status.setGate(new RangedValue(defaultGate, minGate + offsetGate, maxGate + offsetGate));
                    }
                }*/

                _status.setMemo(x.firstChildsTextContext("Memo"));
                _status.setName("");
                
                writeBufferToPanelSlider();
            }catch(Throwable e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jButtonFromListActionPerformed

    private void jComboBoxGateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxGateActionPerformed
        if (skipDataExchange) {
            return;
        }
        Object sel = jComboBoxGate.getModel().getSelectedItem();
        MXWrap<Integer> wrap = (MXWrap<Integer>)sel;
        int newGate = wrap.value;
        int oldGate = _status.getGate()._var;
        
        readBufferFromPanelSlider();
        if (oldGate != newGate) {
            if (newGate == MXMidi.DATA1_CC_DATAENTRY) {
                fillDataentry(MXVisitant.ROOMTYPE_RPN);
            }
        }
    }//GEN-LAST:event_jComboBoxGateActionPerformed

    private void jComboBoxChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxChannelActionPerformed
        if (skipDataExchange) {
            return;
        }
        Object sel = jComboBoxChannel.getModel().getSelectedItem();
        MXWrap<Integer> wrap = (MXWrap<Integer>)sel;
        int channel = wrap.value;
        if (_status.getChannel() != channel) {
            _status.setChannel(channel);
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
        MXUtil.showAsDialog(this, panel, "Capture ...");
        GateInfomation retval = panel._selected;
        if (retval != null) {
            _status.setChannel(retval._parent.channel);
            _status.setTemplate(MXMessageFactory.fromDtext(retval._parent.dtext, _status.getChannel()));
            _status.setGate(new RangedValue(retval._gate, retval._hitLoValue, retval._hitHiValue));
            
            String dtext = retval._parent.dtext;

            writeBufferToPanelSlider();
            MXMessage message = _status.toMXMessage(new MXTiming());

            if (message.isCommand(MXMidi.COMMAND_NOTEOFF)) {
                int z = JOptionPane.showConfirmDialog(
                        this
                        ,"Seems you choiced Note Off\n"
                        +"You want to use Note ON?"
                        ,"Offer (adjust value range)"
                        ,JOptionPane.YES_NO_OPTION);
                if (z == JOptionPane.YES_OPTION) {
                    message = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_NOTEON + message.getChannel(), message.getData1(), 127);
                    _status.setChannel(message.getChannel());
                    _status.setTemplate(message.getTemplate());
                    _status.setGate(message.getGate());
                    _status.setValue(message.getValue());
                }
            }else  {
                int max = 128-1;
                if (message.hasValueHiField() || message.isValuePairCC14()) {
                    max = 128*128-1;
                }
                if (retval._hitLoValue != 0 || retval._hitHiValue != max) {
                    int z = JOptionPane.showConfirmDialog(
                            this
                            ,"min-max = " + retval._hitLoValue + "-" + retval._hitHiValue  + "\n"
                          +" I will offer you reset to 0 - " + max
                            ,"Offer (adjust value rnage)"
                            ,JOptionPane.YES_NO_OPTION);
                    if (z == JOptionPane.YES_OPTION) {
                        _status.setCustomRange(0, max);
                    }
                }
            }
            writeBufferToPanelSlider();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    public void actionSetValueRange(int min, int max) {
        if (min < 0) { min = 0; }
        if (max >= 16383) { max = 16383; }
        if (min > max)  max = min;
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
        String text  = jTextFieldTextCommand.getText();
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
                fillNote(12 * 4);
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });
        popup.add(menu);

        menu = new JMenuItem("Quick ModeSet = Control Change");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillControlChange();
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });
        popup.add(menu);

        menu = new JMenuItem("Quick ModeSet = DataEntry RPN");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillDataentry(MXVisitant.ROOMTYPE_RPN);
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });

        popup.add(menu);
        menu = new JMenuItem("Quick ModeSet = DataEntry NRPN");
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillDataentry(MXVisitant.ROOMTYPE_NRPN);
                writeBufferToPanelSlider();
                writeBufferToPanelDrum();
            }
        });
        popup.add(menu);
        
        popup.show(jButtonActionQuickMenu, 0, jButtonActionQuickMenu.getHeight());
    }//GEN-LAST:event_jButtonActionQuickMenuActionPerformed

    private void jRadioButtonOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonOnActionPerformed
        _status.setSwitchType(MGStatus.SWITCH_TYPE_ON);
        disableUnusedOnPanel();
    }//GEN-LAST:event_jRadioButtonOnActionPerformed

    private void jRadioButtonOnOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonOnOffActionPerformed
        _status.setSwitchType(MGStatus.SWITCH_TYPE_ONOFF);
        disableUnusedOnPanel();
    }//GEN-LAST:event_jRadioButtonOnOffActionPerformed

    private void jRadioButtonHarmonyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonHarmonyActionPerformed
        _status.setSwitchType(MGStatus.SWITCH_TYPE_HARMONY);
        disableUnusedOnPanel();
    }//GEN-LAST:event_jRadioButtonHarmonyActionPerformed

    private void jRadioButtonSequenceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonSequenceActionPerformed
        _status.setSwitchType(MGStatus.SWITCH_TYPE_SEQUENCE);
        disableUnusedOnPanel();
    }//GEN-LAST:event_jRadioButtonSequenceActionPerformed

    private void jButtonInputEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInputEditActionPerformed
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jButtonInputEditActionPerformed

    private void jComboBoxInputTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxInputTypeActionPerformed
        _status.setSwitchInputType(_drumInputType.readCombobox(jComboBoxInputType));
        disableUnusedOnPanel();
    }//GEN-LAST:event_jComboBoxInputTypeActionPerformed

    private void jComboBoxOutputOnValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxOutputOnValueActionPerformed
        _status.setSwitchOutOnType(_drumOutOnType.readCombobox(jComboBoxOutputOnType));
        disableUnusedOnPanel();
    }//GEN-LAST:event_jComboBoxOutputOnValueActionPerformed

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

    private void jComboBoxOutputOffValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxOutputOffValueActionPerformed
        _status.setSwitchOutOffTypeOfValue(_drumOutOffValue.readCombobox(jComboBoxOutputOffValue));
        disableUnusedOnPanel();
    }//GEN-LAST:event_jComboBoxOutputOffValueActionPerformed

    private void jComboBoxDrumChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxDrumChannelActionPerformed
        _status.setSwitchOutChannel(_drumOutChannel.readCombobox(jComboBoxDrumChannel));
        disableUnusedOnPanel();
    }//GEN-LAST:event_jComboBoxDrumChannelActionPerformed

    private void jComboBoxHarmonyVelocityTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxHarmonyVelocityTypeActionPerformed
        _status.setSwitchHarmonyVelocityType(_drumHarmonyVelocityType.readCombobox(jComboBoxHarmonyVelocityType));
        disableUnusedOnPanel();
    }//GEN-LAST:event_jComboBoxHarmonyVelocityTypeActionPerformed

    private void jButtonCheckValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCheckValueActionPerformed
        readBufferFromPanelSlider();
        readBufferFromPanelDrum();
        disableUnusedOnPanel();
        validateBuffer(true);
    }//GEN-LAST:event_jButtonCheckValueActionPerformed

    private void jButtonOutputOnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOutputOnEditActionPerformed
        MGStatus status = new MGStatus(_status.getPort(), MGStatus.TYPE_DRUMPAD_OUTSIGNAL, _status.getRow(), _status.getColumn());
        status.setChannel(_status.getSwitchOutChannel());
        status.setGate(RangedValue.new7bit(_status.getSwitchOutOnTextGate()));
        status.setTemplate(MXMessageFactory.fromDtext(_status.getSwitchOutOnText(), status.getChannel()));
        MGStatusConfig config = new MGStatusConfig(_process, status);
        MXUtil.showAsDialog(this, config, "Edit Output-On signal");
        if (config._okOption) {
            _status.setSwitchOutOnType(MGStatus.SWITCH_OUT_ON_CUSTOM);
            _status.setSwitchOutOnText(config._status.toTemplateText());
            _status.setSwitchOutOnTextGate(config._status.getGate()._var);
            _status.setSwitchOutChannel(config._status.getChannel());
            showsub1();
        }
    }//GEN-LAST:event_jButtonOutputOnEditActionPerformed

    private void jButtonOutputOffEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOutputOffEditActionPerformed
        MGStatus status = new MGStatus(_status.getPort(), MGStatus.TYPE_DRUMPAD_OUTSIGNAL, _status.getRow(), _status.getColumn());
        status.setChannel(_status.getSwitchOutChannel());
        status.setGate(RangedValue.new7bit(_status.getSwitchOutOffTextGate()));
        status.setTemplateAsText(_status.getSwitchOutOffText(), status.getChannel());
        MGStatusConfig config = new MGStatusConfig(_process, status);
        MXUtil.showAsDialog(this, config, "Edit Output-Off signal");
        if (config._okOption) {
            _status.setSwitchOutOffType(MGStatus.SWITCH_OUT_OFF_CUSTOM);
            _status.setSwitchOutOffText(config._status.toTemplateText());
            _status.setSwitchOutOffTextGate(config._status.getGate()._var);
            _status.setSwitchOutChannel(config._status.getChannel());
            showsub1();
        }
    }//GEN-LAST:event_jButtonOutputOffEditActionPerformed

    private void jComboBoxOutputOnTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxOutputOnTypeActionPerformed
        int val = _drumOutOnType.readCombobox(jComboBoxOutputOnType);
        _status.setSwitchOutOnType(val);
        showsub1();
    }//GEN-LAST:event_jComboBoxOutputOnTypeActionPerformed

    private void jComboBoxOutputOffTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxOutputOffTypeActionPerformed
        int val = _drumOutOffType.readCombobox(jComboBoxOutputOffType);
        _status.setSwitchOutOffType(val);
        showsub1();
    }//GEN-LAST:event_jComboBoxOutputOffTypeActionPerformed

    private void jButtonSequenceFileBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSequenceFileBrowseActionPerformed
        MXSwingFileChooser chooser = new MXSwingFileChooser();

        chooser.addExtension(".mid", "Standard MIDI File");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogTitle("Chose Standard MIDI File");

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String file = chooser.getSelectedFile().getAbsolutePath();
        jTextFieldSequenceFile.setText(file);
        _status.setSwitchSequencerFile(file);
    }//GEN-LAST:event_jButtonSequenceFileBrowseActionPerformed

    private void jButtonHarmonyEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHarmonyEditActionPerformed
        MXNotePicker picker = new MXNotePicker();
        picker.setSelectedNoteList(MXMidi.textToNoteList(jTextFieldHarmonyNoteList.getText()));
        if (picker.showAsModalDialog(this)) {
            int[] note = picker.getSelectedNoteList();
            String text = MXMidi.noteListToText(note);
            jTextFieldHarmonyNoteList.setText(text);
        }
    }//GEN-LAST:event_jButtonHarmonyEditActionPerformed

    private void jButtonQuickMenuDrumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonQuickMenuDrumActionPerformed
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menu2 = new JMenuItem("Button For Slider's MAX");
        menu2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                skipDataExchange = false;

                _process._data.fillMaxOfSlider(_status, _status.getColumn());
                writeBufferToPanelDrum();
                disableUnusedOnPanel();

                textTemplate.clear();
                textTemplate.add("Slider's MAX");
                textTemplate.add("- Nothing To Do -");
                printTemplateResult();
            }
        });
        popup.add(menu2);

        JMenuItem menu0 = new JMenuItem("Button For Slider'sMIDDLE");
        menu0.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                skipDataExchange = false;

                _process._data.fillMiddleOfSlider(_status, _status.getColumn());
                writeBufferToPanelDrum();
                disableUnusedOnPanel();
                textTemplate.clear();
                textTemplate.add("Slider's Max");
                textTemplate.add("- Nothing To Do -");
                printTemplateResult();
            }
        });
        popup.add(menu0);

        JMenuItem menu1 = new JMenuItem("Button For Slider'sMIN");
        menu1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                skipDataExchange = false;

                _process._data.fillMinOfSlider(_status, _status.getColumn());
                writeBufferToPanelDrum();
                disableUnusedOnPanel();
                textTemplate.clear();
                textTemplate.add("Slider's MIN");
                textTemplate.add("- Nothing To Do -");
                printTemplateResult();
            }
        });
        popup.add(menu1);

        JMenuItem menu3 = new JMenuItem("Note To Pedal ON / OFF");
        menu3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillTogglePedal();
                writeBufferToPanelDrum();
                disableUnusedOnPanel();
                textTemplate.clear();
                textTemplate.add("Note To Pedal ON/OFF");
                textTemplate.add("Page1's Note(Gate)");
                printTemplateResult();
            }
        });
        popup.add(menu3);

        JMenuItem menu4 = new JMenuItem("Song Play / Stop");
        menu4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                skipDataExchange = false;

                _status.setSwitchType(MGStatus.SWITCH_TYPE_SEQUENCE);
                _status.setSwitchWithToggle(true);

                writeBufferToPanelDrum();
                disableUnusedOnPanel();

                textTemplate.clear();
                textTemplate.add("Slider's MAX");
                textTemplate.add("Page1's Trigger");
                textTemplate.add("Page2's Sequencer Section");
                printTemplateResult();
            }
        });
        popup.add(menu4);
        
        popup.show(jButtonQuickMenuDrum, 0, jButtonActionQuickMenu.getHeight());
    }//GEN-LAST:event_jButtonQuickMenuDrumActionPerformed
    
    private void jCheckBoxCC14bitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxCC14bitActionPerformed
        skipDataExchange = true;
        _status.setCustomRange((int)jSpinnerMin.getValue(), (int)jSpinnerMax.getValue());

        if (_status.getDataroomType() == MXVisitant.ROOMTYPE_RPN) {
            fillDataentry(MXVisitant.ROOMTYPE_RPN);
        }
        else if (_status.getDataroomType() == MXVisitant.ROOMTYPE_NRPN) {
            fillDataentry(MXVisitant.ROOMTYPE_NRPN);
        }

        if (jCheckBoxCC14bit.isEnabled() && jCheckBoxCC14bit.isSelected()) {
            _status.setValuePairCC14(true);
        }
        else {
            _status.setValuePairCC14(false);
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
            _status.setDataroomMSB(_rpnMSBModel.readCombobox(jComboBoxMSB));
            if (_status.getDataroomType() == MXVisitant.ROOMTYPE_RPN) {
                fillDataentry(MXVisitant.ROOMTYPE_RPN);
            }else if (_status.getDataroomType() == MXVisitant.ROOMTYPE_NRPN) {
                fillDataentry(MXVisitant.ROOMTYPE_NRPN);
            }
            writeBufferToPanelSlider();
       }
    }//GEN-LAST:event_jComboBoxMSBItemStateChanged

    private void jComboBoxLSBItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxLSBItemStateChanged
       if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            _status.setDataroomLSB(_rpnLSBModel.readCombobox(jComboBoxLSB));
            if (_status.getDataroomType() == MXVisitant.ROOMTYPE_RPN) {
                fillDataentry(MXVisitant.ROOMTYPE_RPN);
            }else if (_status.getDataroomType() == MXVisitant.ROOMTYPE_NRPN) {
                fillDataentry(MXVisitant.ROOMTYPE_NRPN);
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
        MXTemplate temp = MXMessageFactory.fromDtext(jTextAreaTemplate.getText(), 0);
        _status.setTemplate(temp);
    }//GEN-LAST:event_jButtonUpdateCommandActionPerformed
    
    public int getDrumType() {
        if (jRadioButtonOn.isSelected()) {            
            return MGStatus.SWITCH_TYPE_ON;
        }
        if (jRadioButtonOnOff.isSelected()) {
            return MGStatus.SWITCH_TYPE_ONOFF;
        }
        if (jRadioButtonHarmony.isSelected()) {
            return MGStatus.SWITCH_TYPE_HARMONY;
        }
        if (jRadioButtonSequence.isSelected()) {        
            return MGStatus.SWITCH_TYPE_SEQUENCE;
        }
        return 0;
    }
    
    ActionListener selectInternalCommand = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(PROG_INC)) {
                _status.setTemplateAsText(MXTemplate.EXCOMMAND_PROGRAM_INC, 0);
                MGStatusConfig.this.writeBufferToPanelSlider();
                disableUnusedOnPanel();
                return;
            }
            if (e.getActionCommand().equals(PROG_DEC)) {
                _status.setTemplateAsText(MXTemplate.EXCOMMAND_PROGRAM_DEC, 0);
                MGStatusConfig.this.writeBufferToPanelSlider();
                disableUnusedOnPanel();
                return;
            }
        }
    };
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonActionQuickMenu;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonCheckValue;
    private javax.swing.JButton jButtonFromBefore;
    private javax.swing.JButton jButtonFromList;
    private javax.swing.JButton jButtonHarmonyEdit;
    private javax.swing.JButton jButtonInputEdit;
    private javax.swing.JButton jButtonInternalCommand;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JButton jButtonOutputOffEdit;
    private javax.swing.JButton jButtonOutputOnEdit;
    private javax.swing.JButton jButtonQuickMenuDrum;
    private javax.swing.JButton jButtonSequenceFileBrowse;
    private javax.swing.JButton jButtonUpdateCommand;
    private javax.swing.JCheckBox jCheckBoxCC14bit;
    private javax.swing.JCheckBox jCheckBoxCustomRange;
    private javax.swing.JCheckBox jCheckBoxSequencerFilterNote;
    private javax.swing.JCheckBox jCheckBoxSequencerSeekStart;
    private javax.swing.JCheckBox jCheckBoxSequencerSingleTrack;
    private javax.swing.JCheckBox jCheckBoxToggle;
    private javax.swing.JComboBox<String> jComboBoxChannel;
    private javax.swing.JComboBox<String> jComboBoxDrumChannel;
    private javax.swing.JComboBox<String> jComboBoxDrumPort;
    private javax.swing.JComboBox<String> jComboBoxGate;
    private javax.swing.JComboBox<String> jComboBoxHarmonyVelocityType;
    private javax.swing.JComboBox<String> jComboBoxInputType;
    private javax.swing.JComboBox<String> jComboBoxLSB;
    private javax.swing.JComboBox<String> jComboBoxMSB;
    private javax.swing.JComboBox<String> jComboBoxOutputOffType;
    private javax.swing.JComboBox<String> jComboBoxOutputOffValue;
    private javax.swing.JComboBox<String> jComboBoxOutputOnType;
    private javax.swing.JComboBox<String> jComboBoxOutputOnValue;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
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
    private javax.swing.JLabel jLabel23;
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
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelBlank1;
    private javax.swing.JLabel jLabelBlank2;
    private javax.swing.JLabel jLabelInputText;
    private javax.swing.JLabel jLabelNameDefault;
    private javax.swing.JLabel jLabelOutputOffText;
    private javax.swing.JLabel jLabelOutputOnText;
    private javax.swing.JLabel jLabelStartWith;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButtonHarmony;
    private javax.swing.JRadioButton jRadioButtonOn;
    private javax.swing.JRadioButton jRadioButtonOnOff;
    private javax.swing.JRadioButton jRadioButtonSequence;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSpinner jSpinnerHarmonyVelocityFixed;
    private javax.swing.JSpinner jSpinnerMax;
    private javax.swing.JSpinner jSpinnerMin;
    private javax.swing.JSpinner jSpinnerOutOffValueFixed;
    private javax.swing.JSpinner jSpinnerOutOnValueFixed;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextAreaTemplate;
    private javax.swing.JTextArea jTextAreaValidation;
    private javax.swing.JTextField jTextFieldHarmonyNoteList;
    private javax.swing.JTextField jTextFieldMemo;
    private javax.swing.JTextField jTextFieldName;
    private javax.swing.JTextField jTextFieldSequenceFile;
    private javax.swing.JTextField jTextFieldTextCommand;
    // End of variables declaration//GEN-END:variables

    public void showsub1() {
        if (_status.getSwitchOutOnType() == MGStatus.SWITCH_OUT_ON_CUSTOM) {
            jLabelOutputOnText.setText("Custom " + _status.getSwitchOutOnText() + "(Gate:" + _status.getSwitchOutOnTextGate() + ")");
        }else if (_status.getSwitchOutOnType() == MGStatus.SWITCH_OUT_ON_SAME_AS_INPUT) {
            jLabelOutputOnText.setText("Same " + _status.toTemplateText() + "(Gate:" + _status.getGate()+ ")");
        }else {
            jLabelOutputOnText.setText("Unknwon(" + _status.getSwitchOutOnType());
        }
        if (_status.getSwitchOutOffType() == MGStatus.SWITCH_OUT_OFF_CUSTOM) {
            jLabelOutputOffText.setText("Custom " + _status.getSwitchOutOffText() + "(Gate:" + _status.getSwitchOutOffTextGate() + ")");
        }else if (_status.getSwitchOutOffType() == MGStatus.SWITCH_OUT_OFF_NONE) {
            jLabelOutputOffText.setText("None");
        }else if (_status.getSwitchOutOffType() == MGStatus.SWITCH_OUT_OFF_SAME_AS_INPUT) {
            jLabelOutputOffText.setText("Same(Input)" + _status.toTemplateText() + "(Gate:" + _status.getGate()+ ")");
        }else if (_status.getSwitchOutOffType() == MGStatus.SWITCH_OUT_OFF_SAME_AS_OUTPUT_ON) {
            jLabelOutputOffText.setText("Same(Output-ON)" + _status.toTemplateText() + "(Gate:" + _status.getSwitchOutOnTextGate()+ ")");
        }else {
            jLabelOutputOffText.setText("Unknwon(" + _status.getSwitchOutOffType());
        }
    }

    public void fillControlChange() {
        this.skipDataExchange = true;
        try {
            _status.setTemplateAsText("@CC #GL #VL", 0);
            _status.setSwitchOutChannel(0);
            _status.setSwitchInputType(MGStatus.SWITCH_ON_IF_PLUS1);
            _status.setSwitchWithToggle(false);
            _status.setGate(RangedValue.ZERO7);
            _status.setChannel(0);
            _status.setValuePairCC14(false);
            this.writeBufferToPanelDrum();
            textTemplate.clear();
            textTemplate.add("Control Change");
            textTemplate.add("1st Page's ControlChange Number");
            printTemplateResult();
        }finally {
            skipDataExchange = false;
        }
    }

    public void fillDataentry(int dataroomType) {
        skipDataExchange = true;
        try {
            String msb = MXUtil.toHexFF(_status.getDataeroomMSB()) + "h";
            String lsb = MXUtil.toHexFF(_status.getDataroomLSB()) + "h";
            _status.setDataroomType(dataroomType);
            if (dataroomType == MXVisitant.ROOMTYPE_RPN) {
                _status.setTemplateAsText("@RPN " + msb + " "  + lsb + " #VH #VL", 0);
            }else if (dataroomType == MXVisitant.ROOMTYPE_NRPN) {
                _status.setTemplateAsText("@NRPN " + msb + " "  + lsb + " #VH #VL", 0);
            }else {
                new IllegalStateException().printStackTrace();
            }
            writeBufferToPanelSlider();
            readBufferFromPanelSlider();
            disableUnusedOnPanel();
            textTemplate.clear();
            textTemplate.add("DataEntry");
            textTemplate.add("1st Page's DataEntry **");
            printTemplateResult();
        }finally {
            skipDataExchange = false;
        }
    }
    
    public void fillTogglePedal() {
        fillNote(12 * 4);
        skipDataExchange = true;
        try {
            _status.setSwitchType(MGStatus.SWITCH_TYPE_ONOFF);
            _status.setSwitchWithToggle(true);

            _status.setSwitchInputType(MGStatus.SWITCH_ON_IF_PLUS1);

            _status.setSwitchOutOnType(MGStatus.SWITCH_OUT_ON_CUSTOM);
            _status.setSwitchOutOnText("@CC #GL #VL");
            _status.setSwitchOutOnTextGate(MXMidi.DATA1_CC_DAMPERPEDAL);
            _status.setSwitchOutOnTypeOfValue(MGStatus.SWITCH_OUT_ON_VALUE_FIXED);
            _status.setSwitchOutOnValueFixed(127);

            _status.setSwitchOutOffType(MGStatus.SWITCH_OUT_OFF_SAME_AS_OUTPUT_ON);
            _status.setSwitchOutOffTextGate(MXMidi.DATA1_CC_DAMPERPEDAL);
            _status.setSwitchOutOffTypeOfValue(MGStatus.SWITCH_OUT_OFF_VALUE_FIXED);
            _status.setSwitchOutOffValueFixed(0);

            _status.setSwitchWithToggle(true);

            writeBufferToPanelDrum();
            readBufferFromPanelDrum();
            disableUnusedOnPanel();
        }finally {
            skipDataExchange = false;
        }
    }
    
    public void fillNote(int note) {
        fillNormalSlider();
        skipDataExchange = true;
        try {
            _status.setTemplateAsText("90h #GL #VL", 0);
            _status.setGate(RangedValue.new7bit(note));
            _status.setChannel(0);
            _status.setName("");
            _status.setMemo("");
            
            _status.setSwitchWithToggle(true);

            _status.setSwitchInputType(MGStatus.SWITCH_ON_IF_PLUS1);
            _status.setSwitchOutOnTextGate(1);
            _status.setSwitchOutOnValueFixed(100);

            writeBufferToPanelDrum();
            readBufferFromPanelDrum();
            disableUnusedOnPanel();

            textTemplate.clear();
            textTemplate.add("Note");
            textTemplate.add("1st Page's Note(Gate)");
            textTemplate.add("2nd Page's Output ***");
            printTemplateResult();
        }finally {
            skipDataExchange = false;
        }
    }

    public void fillNormalSlider() {
        skipDataExchange = true;
        try {
            _status.setTemplateAsText("@CC #GL #VL", 0);
            _status.setGate(RangedValue.new7bit(7));
            _status.setValue(RangedValue.new7bit(0));
            _status.setValuePairCC14(false);
            _status.setChannel(0);
            _status.setName("");
            _status.setMemo("");
            
            _status.setSwitchType(MGStatus.SWITCH_TYPE_ONOFF);
            _status.setSwitchWithToggle(false);

            _status.setSwitchInputType(MGStatus.SWITCH_ON_IF_MAX);

            _status.setSwitchOutOnType(MGStatus.SWITCH_OUT_ON_SAME_AS_INPUT);
            _status.setSwitchOutOnText("");
            _status.setSwitchOutOnTextGate(127);
            _status.setSwitchOutOnTypeOfValue(MGStatus.SWITCH_OUT_ON_SAME_AS_INPUT);
            _status.setSwitchOutOnValueFixed(127);

            _status.setSwitchOutOffType(MGStatus.SWITCH_OUT_OFF_SAME_AS_OUTPUT_ON);
            _status.setSwitchOutOffTextGate(0);
            _status.setSwitchOutOffTypeOfValue(MGStatus.SWITCH_OUT_OFF_VALUE_0);
            _status.setSwitchOutOffValueFixed(0);

            _status.setSwitchWithToggle(true);

            writeBufferToPanelDrum();
            readBufferFromPanelDrum();
            disableUnusedOnPanel();
            
            textTemplate.clear();
            textTemplate.add("Normal Slider");
            textTemplate.add("As You Like");
            printTemplateResult();
        }finally {
            skipDataExchange = false;
        }
    }
}
