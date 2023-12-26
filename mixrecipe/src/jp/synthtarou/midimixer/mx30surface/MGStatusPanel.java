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
package jp.synthtarou.midimixer.mx30surface;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.ccxml.CCMParser;
import jp.synthtarou.midimixer.ccxml.PickerForControlChange;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.navigator.NavigatorForText;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapListFactory;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.capture.Counter;
import jp.synthtarou.midimixer.libs.midi.capture.MXCaptureProcess;
import jp.synthtarou.midimixer.libs.midi.capture.MXCaptureView;
import jp.synthtarou.midimixer.libs.navigator.INavigator;
import jp.synthtarou.midimixer.libs.swing.MXModalFrame;
import jp.synthtarou.midimixer.libs.swing.SafeSpinnerNumberModel;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileFilterListExt;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileList;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.MXSwingFolderBrowser;
import jp.synthtarou.midimixer.libs.wraplist.MXWrap;

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
    MXWrapList<Integer> _currentGateModel;

    MXWrapList<Integer> _switchOutTypeOn;
    MXWrapList<Integer> _switchOutTypeOff;
    MXWrapList<Integer> _switchOutProgramType;
    MXWrapList<Integer> _switchLinkColumn;

    String _templateStartWith;

    protected MGStatus _status;

    boolean skipDataExchange = true;
    private ArrayList<String> textValidate = new ArrayList();

    JTextField[] _listBindMouse;

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
        group.add(jRadioButtonDrumTypeSequence);
        group.add(jRadioButtonDrumTypeProgram);
        group.add(jRadioButtonDrumTypeDontSend);
        group.add(jRadioButtonDrumTypeCustom);
        group.add(jRadioButtonDrumTypeLinkSlider);

        ButtonGroup group2 = new ButtonGroup();
        group2.add(jRadioButtonLinkSlider);
        group2.add(jRadioButtonLinkKnob1);
        group2.add(jRadioButtonLinkKnob2);
        group2.add(jRadioButtonLinkKnob3);
        group2.add(jRadioButtonLinkKnob4);

        ButtonGroup group3 = new ButtonGroup();
        group3.add(jRadioButtonJumpInc);
        group3.add(jRadioButtonJumpDec);
        group3.add(jRadioButtonJumpMin);
        group3.add(jRadioButtonJumpMax);
        group3.add(jRadioButtonJumpMiddle);
        group3.add(jRadioButtonJumpValue);

        if (_status._uiType == MGStatus.TYPE_DRUMPAD) {
            jTabbedPane1.setEnabledAt(1, true);
        } else {
            jTabbedPane1.setEnabledAt(1, false);
        }

        jSpinnerDrumOnRangeMin.setModel(new SpinnerNumberModel(1, 0, 127, 1));
        jSpinnerDrumOnRangeMax.setModel(new SpinnerNumberModel(127, 0, 127, 1));
        jSpinnerDrumMouseOnValue.setModel(new SpinnerNumberModel(100, 0, 127, 1));
        jSpinnerDrumMouseOffValue.setModel(new SpinnerNumberModel(0, 0, 127, 1));

        _drumOutPort = MXWrapListFactory.listupPort("as Input");
        _drumOutChannel = MXWrapListFactory.listupChannel("as Input");

        jComboBoxOutPort.setModel(_drumOutPort);
        jComboBoxOutChannel.setModel(_drumOutChannel);

        jLabelBlank1.setText("");
        jLabelBlank2.setText("");
        jLabelBlank3.setText("");
        jLabelBlank4.setText("");
        jLabelBlank5.setText("");
        jLabelBlank6.setText("");
        jLabelBlank7.setText("");

        skipDataExchange = false;
        
        new MXAttachPopupForText(jTextFieldName) {
            @Override
            public void approvedText(String text) {
                _status._name = text;
                displayStatusToPanelSlider();
            }
        };
        
        int index = _listChannel.indexOfName(jTextFieldName.getText());
        new MXAttachPopupForList<Integer>(jTextFieldChannel, _listChannel) {
            @Override
            public void approvedIndex(int selected) {
                int channel  = _listChannel.valueOfIndex(selected);
                _status._base.setChannel(channel);
                displayStatusToPanelSlider();
            }
        };

        new MXAttachPopupForList<Integer>(jTextFieldGate, null) {
            @Override
            public void approvedIndex(int selected) {
                int x = getList().valueOfIndex(selected);
                _status._base.setGate(_status._base.getGate().changeValue(x));
                displayStatusToPanelSlider();
            }
            
            @Override
            public MXWrapList<Integer> getList() {
                if (_status._outGateTable != null) {
                    return _status._outGateTable;
                }
                return _currentGateModel;
            }
        };

        new MXAttachPopupForText(jTextFieldMemo) {
            @Override
            public void approvedText(String text) {
                _status._memo = text;
                displayStatusToPanelSlider();
            }
        };

        new MXAttachPopup(jTextFieldTemplate) {
            @Override
            public void showPopup() {
                JPopupMenu popup = new JPopupMenu();
                JMenuItem item1 = new JMenuItem("Edit");
                popup.add(item1);
                item1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        startEdit();
                    }
                });

                JMenuItem item2 = new JMenuItem("From XML");
                popup.add(item2);
                item2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        startBrowseXML();
                    }
                });
                JMenuItem item3 = new JMenuItem("Capture");
                popup.add(item3);
                item3.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        startCapture();
                    }
                });
                popup.addSeparator();

                JMenuItem item5 = new JMenuItem("Note");
                item5.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        _status.clearAll();
                        _status.setBaseMessage("@ON 64 #VL");
                        _status._name = "note";
                        displayStatusToPanelSlider();
                        displayStatusToPanelDrum();
                    }
                });
                popup.add(item5);

                JMenuItem item6 = new JMenuItem("Control Change");
                item6.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        _status.clearAll();
                        _status.setBaseMessage("@CC #GL #VL");
                        displayStatusToPanelSlider();
                        displayStatusToPanelDrum();
                    }
                });
                popup.add(item6);

                JMenuItem item7 = new JMenuItem("DataEntry RPN");
                item7.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        _status.clearAll();
                        _status.setBaseMessage("@RPN 0 0 #VL 0");
                        displayStatusToPanelSlider();
                        displayStatusToPanelDrum();
                    }
                });

                popup.add(item7);

                JMenuItem item8 = new JMenuItem("DataEntry NRPN");
                item8.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        _status.clearAll();
                        _status.setBaseMessage("@NRPN 0 0 #VL 0");
                        displayStatusToPanelSlider();
                        displayStatusToPanelDrum();
                    }
                });
                popup.add(item8);

                JMenuItem item9 = new JMenuItem("Program +1");
                item9.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        _status.setBaseMessage("@PROG_INC");
                        MGStatusPanel.this.displayStatusToPanelSlider();
                        displayStatusToPanelSlider();
                        displayStatusToPanelDrum();
                    }
                });
                popup.add(item9);

                JMenuItem item10 = new JMenuItem("Program -1");
                item10.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        _status.setBaseMessage("@PROG_DEC");
                        MGStatusPanel.this.displayStatusToPanelSlider();
                        displayStatusToPanelSlider();
                        displayStatusToPanelDrum();
                    }
                });
                popup.add(item10);

                popup.show(_target, 0, _target.getHeight());
            }

        };


        displayStatusToPanelSlider();
        displayStatusToPanelDrum();

        disableUnusedOnPanel();
        validateStatus();
    }

    MXWrapList<Integer> _listPort = MXWrapListFactory.listupPort("-");
    MXWrapList<Integer> _listColumn = MXWrapListFactory.listupColumn("-");
    MXWrapList<Integer> _listChannel = MXWrapListFactory.listupChannel(null);

    public boolean catchPopupResult(JTextField target, int value) {
        if (target == jTextFieldChannel) {
            _status._base.setChannel(value);
        }
        if (target == jTextFieldGate) {
            _status._base.setGate(_status._base.getGate().changeValue(value));
        }
        return true;
    }

    public void displayStatusToPanelSlider() {
        if (skipDataExchange) {
            return;
        }
        skipDataExchange = true;
        try {
            if (_channelModel == null) {
                _channelModel = MXWrapListFactory.listupChannel(null);
                _ccGateModel = MXWrapListFactory.listupControlChange(true);
                _keyGateModel = MXWrapListFactory.listupNoteNo(true);
                _normalGateModel = MXWrapListFactory.listupGate7Bit();
                _switchOutTypeOn = new MXWrapList<>();
                _switchOutTypeOn.addNameAndValue("On value as Input", MGStatusForDrum.VALUETYPE_AS_INPUT);
                _switchOutTypeOn.addNameAndValue("On value as Mouse", MGStatusForDrum.VALUETYPE_AS_MOUSE);
                _switchOutTypeOff = new MXWrapList<>();
                _switchOutTypeOff.addNameAndValue("Off value as Input", MGStatusForDrum.VALUETYPE_AS_INPUT);
                _switchOutTypeOff.addNameAndValue("Off value as Mouse", MGStatusForDrum.VALUETYPE_AS_MOUSE);
                _switchOutTypeOff.addNameAndValue("Off value is Nothing to send", MGStatusForDrum.VALUETYPE_NOTHING);
                _switchOutProgramType = new MXWrapList<>();
                _switchOutProgramType.addNameAndValue("Program Set", MGStatusForDrum.PROGRAM_SET);
                _switchOutProgramType.addNameAndValue("Program +1", MGStatusForDrum.PROGRAM_INC);
                _switchOutProgramType.addNameAndValue("Program -1", MGStatusForDrum.PROGRAM_DEC);
                _switchLinkColumn = new MXWrapList<>();
                _switchLinkColumn.addNameAndValue("Same Column", -1);
                for (int i = 0; i < MXAppConfig.SLIDER_COLUMN_COUNT; ++i) {
                    String text = Integer.toHexString(i);
                    _switchLinkColumn.addNameAndValue(text, i);
                }
            }

            if (_status._name == null) {
                _status._name = "";
            }
            if (_status._memo == null) {
                _status._memo = "";
            }
            jTextFieldName.setText(_status._name);
            jTextFieldMemo.setText(_status._memo);
            jTextFieldTemplate.setText(_status._base.getTemplateAsText());
            jLabelStartWith.setText(_templateStartWith.toString());
            jTextFieldChannel.setText(_channelModel.nameOfValue(_status._base.getChannel()));

            jLabelDefaultName.setText("'" + _status._base.toStringForUI() + "' if blank");

            int command = _status._base.getStatus() & 0xf0;
            int gateValue = _status._base.getGate()._var;

            if (command == MXMidi.COMMAND_CH_CHANNELPRESSURE || command == MXMidi.COMMAND_CH_NOTEON || command == MXMidi.COMMAND_CH_NOTEOFF) {
                _currentGateModel = _keyGateModel;
            } else if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                _currentGateModel = _ccGateModel;
            } else {
                _currentGateModel = _normalGateModel;
            }

            jTextFieldGate.setText(_currentGateModel.nameOfValue(_status._base.getGate()._var));
            /*
            jSpinnerOutOnValueFixed.setModel(new SafeSpinnerNumberModel(_status.getSwitchOutOnValueFixed(), 0, 16383, 1));
            jSpinnerOutOffValueFixed.setModel(new SafeSpinnerNumberModel(_status.getSwitchOutOffValueFixed(), 0, 16383, 1));
             */
            jSpinnerMin.setModel(new SafeSpinnerNumberModel(_status.getValue()._min, 0, 128 * 128 - 1, 1));
            jSpinnerMax.setModel(new SafeSpinnerNumberModel(_status.getValue()._max, 0, 128 * 128 - 1, 1));

            jCheckBoxCC14bit.setSelected(_status._ccPair14);
            jCheckBoxCustomRange.setSelected(_status.hasCustomRange());
        } finally {
            skipDataExchange = false;
        }

        disableUnusedOnPanel();
        //updateUI();
    }

    public int validateStatus() {
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

        if (data._base == null) {
            result.add("TextCommand is empty. Please fill it.");
        } else {
            try {
                MXMessage message = data._base;
                if (message == null) {
                    result.add("TextCommand [" + data._base.getTemplateAsText() + "] is not valid.");
                } else if (message.isDataentryByCC()) {
                    result.add("If you need DATAENTRY. try [@RPN/@NRPN msb lsb value 0].");
                }
                /*
                else if (message.isMessageTypeChannel() && message.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE) && !message.isDataentry()) {
                    String newText = "@CC #GL #VL";
                    if (data.getBaseMessage().getTemplateAsText().equals(newText) == false) {
                        String errorText = "ControlChange's Text Command can be '" + newText + "'";
                        if (JOptionPane.showConfirmDialog(this, errorText, "Smart Replace", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
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
                result.add("TextCommand [" + data._base + "] is not valid.");
            }
        }

        validateStatusSubDrum(result);

        textValidate = result;
        showValidationError();

        return result.size();
    }

    private void showValidationError() {
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

    public void validateStatusSubDrum(ArrayList<String> result) {
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
        boolean sel = _status.hasCustomRange();
        jSpinnerMin.setEnabled(sel);
        jSpinnerMax.setEnabled(sel);
    }

    MXWrapList<Integer> _drumOutChannel;
    MXWrapList<Integer> _drumOutPort;

    public void displayStatusToPanelDrum() {
        if (skipDataExchange) {
            return;
        }
        if (_status._uiType != MGStatus.TYPE_DRUMPAD) {
            return;
        }
        if (_status._drum == null) {
            return;
        }

        skipDataExchange = true;

        try {
            /* Drum */
            jSpinnerDrumOnRangeMin.setValue(_status._drum._strikeZone._min);
            jSpinnerDrumOnRangeMax.setValue(_status._drum._strikeZone._max);

            jLabelOffRange.setText(textForOffRange(_status._drum._strikeZone, _status._base.getValue()));

            jSpinnerDrumMouseOnValue.setValue(_status._drum._mouseOnValue);
            jSpinnerDrumMouseOffValue.setValue(_status._drum._mouseOffValue);
            jCheckBoxDrumModeToggle.setSelected(_status._drum._modeToggle);
            jCheckBoxDrumOnlySwitch.setSelected(_status._drum._onlySwitched);

            /* Drum Output */
            switch (_status._drum._outStyle) {
                case MGStatusForDrum.STYLE_SAME_CC:
                    jRadioButtonDrumTypeSame.setSelected(true);
                    jTabbedPane2.setSelectedIndex(0);
                    break;
                case MGStatusForDrum.STYLE_CUSTOM_CC:
                    jRadioButtonDrumTypeCustom.setSelected(true);
                    jTabbedPane2.setSelectedIndex(0);
                    break;
                case MGStatusForDrum.STYLE_PROGRAM_CHANGE:
                    jRadioButtonDrumTypeProgram.setSelected(true);
                    jTabbedPane2.setSelectedIndex(1);
                    break;
                case MGStatusForDrum.STYLE_NOTES:
                    jRadioButtonDrumTypeNotes.setSelected(true);
                    jTabbedPane2.setSelectedIndex(2);
                    break;
                case MGStatusForDrum.STYLE_SEQUENCE:
                    jRadioButtonDrumTypeSequence.setSelected(true);
                    jTabbedPane2.setSelectedIndex(3);
                    break;
                case MGStatusForDrum.STYLE_LINK_SLIDER:
                    jRadioButtonDrumTypeLinkSlider.setSelected(true);
                    jTabbedPane2.setSelectedIndex(4);
                    break;
                case MGStatusForDrum.STYLE_DONT_SEND:
                    jRadioButtonDrumTypeDontSend.setSelected(true);
                    break;
                default:
                    jRadioButtonDrumTypeSame.setSelected(true);
                    break;
            }
            _switchOutTypeOn.writeComboBox(jComboBoxOutTypeOn, _status._drum._outValueTypeOn);
            _switchOutTypeOff.writeComboBox(jComboBoxOutTypeOff, _status._drum._outValueTypeOff);

            _drumOutPort.writeComboBox(jComboBoxOutPort, _status._drum._outPort);
            _drumOutChannel.writeComboBox(jComboBoxOutChannel, _status._drum._outChannel);

            /* Template*/
            jTextFieldTemplateForOut.setText(_status._drum._customTemplate.toString());
            jLabelTemplateTextGate.setText(Integer.toString(_status._drum._teplateTextGate));

            /* Program */
            _switchOutProgramType.writeComboBox(jComboBoxProgram, _status._drum._programType);
            jSpinnerDrumProgPC.setModel(new SpinnerNumberModel(_status._drum._programType, 0, 127, 1));
            jSpinnerDrumProgMSB.setModel(new SpinnerNumberModel(_status._drum._programMSB, 0, 127, 1));
            jSpinnerDrumProgLSB.setModel(new SpinnerNumberModel(_status._drum._programLSB, 0, 127, 1));

            /* Note */
            jTextFieldHarmonyNoteList.setText(_status._drum._harmonyNotes);

            /* Sequence */
            jTextFieldSequenceFile.setText(_status._drum._sequencerFile);
            jCheckBoxSequencerSeekStart.setSelected(_status._drum._sequencerSeekStart);
            jCheckBoxSequencerSingleTrack.setSelected(_status._drum._sequencerSingleTrack);
            jCheckBoxSequencerFilterNote.setSelected(_status._drum._sequencerFilterNote);

            /* Link */
            _switchLinkColumn.writeComboBox(jComboBoxLinkColumn, _status._drum._linkColumn);
            switch (_status._drum._linkKontrolType) {
                case MGStatus.TYPE_SLIDER:
                    jRadioButtonLinkSlider.setSelected(true);
                    break;
                case MGStatus.TYPE_CIRCLE:
                    switch (_status._drum._linkRow) {
                        case 0:
                            jRadioButtonLinkKnob1.setSelected(true);
                            break;
                        case 1:
                            jRadioButtonLinkKnob2.setSelected(true);
                            break;
                        case 2:
                            jRadioButtonLinkKnob3.setSelected(true);
                            break;
                        case 3:
                            jRadioButtonLinkKnob4.setSelected(true);
                            break;
                    }
                    break;
                case MGStatus.TYPE_DRUMPAD:
                    break;
            }

            switch (_status._drum._linkMode) {
                case MGStatusForDrum.LINKMODE_VALUE:
                    jRadioButtonJumpValue.setSelected(true);
                    break;
                case MGStatusForDrum.LINKMODE_MIN:
                    jRadioButtonJumpMin.setSelected(true);
                    break;
                case MGStatusForDrum.LINKMODE_MAX:
                    jRadioButtonJumpMax.setSelected(true);
                    break;
                case MGStatusForDrum.LINKMODE_MIDDLE:
                    jRadioButtonJumpMiddle.setSelected(true);
                    break;
                case MGStatusForDrum.LINKMODE_INC:
                    jRadioButtonJumpInc.setSelected(true);
                    break;
                case MGStatusForDrum.LINKMODE_DEC:
                    jRadioButtonJumpDec.setSelected(true);
                    break;
            }
        } finally {
            skipDataExchange = false;
        }
        disableUnusedOnPanel();
        //updateUI();
    }

    public void buildStatusFromPanelSlider() {
        if (skipDataExchange) {
            return;
        }
        _status._memo = jTextFieldName.getText();
        String gateName = jTextFieldGate.getText();
        int gate = _currentGateModel.valueOfName(gateName);
        _status._base.setGate(MXRangedValue.new7bit(gate));

        if (jCheckBoxCustomRange.isSelected()) {
            int min = (Integer) jSpinnerMin.getValue();
            int max = (Integer) jSpinnerMax.getValue();
            _status.setCustomRange(min, max);
        } else {
            _status.resetCustomRange();
        }

        MXMessage msg = _status._base;
        if (msg.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE) && msg.getData1() >= 0 && msg.getData1() < 32) {
            _status._ccPair14 = jCheckBoxCC14bit.isSelected();
            jCheckBoxCC14bit.setEnabled(true);
        } else {
            if (msg.isCommand(MXMidi.COMMAND2_CH_RPN)
                    || msg.isCommand(MXMidi.COMMAND2_CH_NRPN)) {
                _status._ccPair14 = true;
                jCheckBoxCC14bit.setEnabled(false);
                jCheckBoxCC14bit.setSelected(true);
            } else {
                _status._ccPair14 = false;
                jCheckBoxCC14bit.setEnabled(false);
                jCheckBoxCC14bit.setSelected(false);
            }
        }
    }

    public void buildStatusFromPanelDrum() {
        if (skipDataExchange) {
            return;
        }
        if (_status._uiType != MGStatus.TYPE_DRUMPAD) {
            return;
        }
        if (_status._drum == null) {
            return;
        }

        int style = 0;
        if (jRadioButtonDrumTypeSame.isSelected()) {
            style = MGStatusForDrum.STYLE_SAME_CC;
        } else if (jRadioButtonDrumTypeCustom.isSelected()) {
            style = MGStatusForDrum.STYLE_CUSTOM_CC;
        } else if (jRadioButtonDrumTypeProgram.isSelected()) {
            style = MGStatusForDrum.STYLE_PROGRAM_CHANGE;
        } else if (jRadioButtonDrumTypeNotes.isSelected()) {
            style = MGStatusForDrum.STYLE_NOTES;
        } else if (jRadioButtonDrumTypeSequence.isSelected()) {
            style = MGStatusForDrum.STYLE_SEQUENCE;
        } else if (jRadioButtonDrumTypeLinkSlider.isSelected()) {
            style = MGStatusForDrum.STYLE_LINK_SLIDER;
        } else if (jRadioButtonDrumTypeDontSend.isSelected()) {
            style = MGStatusForDrum.STYLE_DONT_SEND;
        } else {
            style = MGStatusForDrum.STYLE_SAME_CC;
        }

        /* Drum */
        int onRangeMin = (Integer) jSpinnerDrumOnRangeMin.getValue();
        int onRangeMax = (Integer) jSpinnerDrumOnRangeMax.getValue();
        _status._drum._strikeZone = new MXRangedValue(0, onRangeMin, onRangeMax);

        _status._drum._mouseOnValue = (Integer) jSpinnerDrumMouseOnValue.getValue();
        _status._drum._mouseOffValue = (Integer) jSpinnerDrumMouseOffValue.getValue();
        _status._drum._modeToggle = jCheckBoxDrumModeToggle.isSelected();
        _status._drum._onlySwitched = jCheckBoxDrumOnlySwitch.isSelected();

        /* Drum Output */
        int x = 0;
        if (jRadioButtonDrumTypeSame.isSelected()) {
            x = MGStatusForDrum.STYLE_SAME_CC;
        } else if (jRadioButtonDrumTypeCustom.isSelected()) {
            x = MGStatusForDrum.STYLE_CUSTOM_CC;
        } else if (jRadioButtonDrumTypeProgram.isSelected()) {
            x = MGStatusForDrum.STYLE_PROGRAM_CHANGE;
        } else if (jRadioButtonDrumTypeNotes.isSelected()) {
            x = MGStatusForDrum.STYLE_NOTES;
        } else if (jRadioButtonDrumTypeSequence.isSelected()) {
            x = MGStatusForDrum.STYLE_SEQUENCE;
        } else if (jRadioButtonDrumTypeLinkSlider.isSelected()) {
            x = MGStatusForDrum.STYLE_LINK_SLIDER;
        } else if (jRadioButtonDrumTypeDontSend.isSelected()) {
            x = MGStatusForDrum.STYLE_DONT_SEND;
        } else {
            /* optional */
            x = MGStatusForDrum.STYLE_SAME_CC;
        }
        _status._drum._outStyle = x;
        switch (_status._drum._outStyle) {
            case MGStatusForDrum.STYLE_SAME_CC:
                jRadioButtonDrumTypeSame.setSelected(true);
                jTabbedPane2.setSelectedIndex(0);
                break;
            case MGStatusForDrum.STYLE_CUSTOM_CC:
                jRadioButtonDrumTypeCustom.setSelected(true);
                jTabbedPane2.setSelectedIndex(0);
                break;
            case MGStatusForDrum.STYLE_PROGRAM_CHANGE:
                jRadioButtonDrumTypeProgram.setSelected(true);
                jTabbedPane2.setSelectedIndex(1);
                break;
            case MGStatusForDrum.STYLE_NOTES:
                jRadioButtonDrumTypeNotes.setSelected(true);
                jTabbedPane2.setSelectedIndex(2);
                break;
            case MGStatusForDrum.STYLE_SEQUENCE:
                jRadioButtonDrumTypeSequence.setSelected(true);
                jTabbedPane2.setSelectedIndex(3);
                break;
            case MGStatusForDrum.STYLE_LINK_SLIDER:
                jRadioButtonDrumTypeLinkSlider.setSelected(true);
                jTabbedPane2.setSelectedIndex(4);
                break;
            case MGStatusForDrum.STYLE_DONT_SEND:
                jRadioButtonDrumTypeDontSend.setSelected(true);
                break;
            default:
                jRadioButtonDrumTypeSame.setSelected(true);
                break;
        }

        _status._drum._outValueTypeOn = _switchOutTypeOn.readComboBox(jComboBoxOutTypeOn);
        _status._drum._outValueTypeOff = _switchOutTypeOff.readComboBox(jComboBoxOutTypeOff);

        _status._drum._outPort = _drumOutPort.readComboBox(jComboBoxOutPort);
        _status._drum._outChannel = _drumOutChannel.readComboBox(jComboBoxOutChannel);

        /* Template*/
        try {
            _status._drum._customTemplate = new MXTemplate(jTextFieldTemplateForOut.getText());
        } catch (IllegalFormatException e) {
            e.printStackTrace();
            _status._drum._customTemplate = null;
        }
        _status._drum._teplateTextGate = MXUtil.numberFromText(jLabelTemplateTextGate.getText(), -1);


        /* Program */
        _status._drum._programType = _switchOutProgramType.readComboBox(jComboBoxProgram);
        _status._drum._programNumber = (Integer) jSpinnerDrumProgPC.getValue();
        _status._drum._programMSB = (Integer) jSpinnerDrumProgMSB.getValue();
        _status._drum._programLSB = (Integer) jSpinnerDrumProgLSB.getValue();

        /* Note */
        _status._drum._harmonyNotes = jTextFieldHarmonyNoteList.getText();

        /* Sequence */
        _status._drum._sequencerFile = jTextFieldSequenceFile.getText();
        _status._drum._sequencerSeekStart = jCheckBoxSequencerSeekStart.isSelected();
        _status._drum._sequencerSingleTrack = jCheckBoxSequencerSingleTrack.isSelected();
        _status._drum._sequencerFilterNote = jCheckBoxSequencerFilterNote.isSelected();

        /* Link */
        _status._drum._linkColumn = _switchLinkColumn.readComboBox(jComboBoxLinkColumn);
        if (jRadioButtonLinkSlider.isSelected()) {
            _status._drum._linkKontrolType = MGStatus.TYPE_SLIDER;
            _status._drum._linkRow = 0;
        } else if (jRadioButtonLinkKnob1.isSelected()) {
            _status._drum._linkKontrolType = MGStatus.TYPE_CIRCLE;
            _status._drum._linkRow = 0;
        } else if (jRadioButtonLinkKnob2.isSelected()) {
            _status._drum._linkKontrolType = MGStatus.TYPE_CIRCLE;
            _status._drum._linkRow = 1;
        } else if (jRadioButtonLinkKnob3.isSelected()) {
            _status._drum._linkKontrolType = MGStatus.TYPE_CIRCLE;
            _status._drum._linkRow = 2;
        } else if (jRadioButtonLinkKnob4.isSelected()) {
            _status._drum._linkKontrolType = MGStatus.TYPE_CIRCLE;
            _status._drum._linkRow = 3;
        }

        if (jRadioButtonJumpValue.isSelected()) {
            _status._drum._linkMode = MGStatusForDrum.LINKMODE_VALUE;
        } else if (jRadioButtonJumpMin.isSelected()) {
            _status._drum._linkMode = MGStatusForDrum.LINKMODE_MIN;
        } else if (jRadioButtonJumpMax.isSelected()) {
            _status._drum._linkMode = MGStatusForDrum.LINKMODE_MAX;
        } else if (jRadioButtonJumpMiddle.isSelected()) {
            _status._drum._linkMode = MGStatusForDrum.LINKMODE_MIDDLE;
        } else if (jRadioButtonJumpInc.isSelected()) {
            _status._drum._linkMode = MGStatusForDrum.LINKMODE_INC;
        } else if (jRadioButtonJumpDec.isSelected()) {
            _status._drum._linkMode = MGStatusForDrum.LINKMODE_DEC;
        }

        int z = 0;
        if (jRadioButtonJumpValue.isSelected()) {
            z = MGStatusForDrum.LINKMODE_VALUE;
        } else if (jRadioButtonJumpMin.isSelected()) {
            z = MGStatusForDrum.LINKMODE_MIN;
        } else if (jRadioButtonJumpMin.isSelected()) {
            z = MGStatusForDrum.LINKMODE_MIN;
        } else if (jRadioButtonJumpMax.isSelected()) {
            z = MGStatusForDrum.LINKMODE_MAX;
        } else if (jRadioButtonJumpMiddle.isSelected()) {
            z = MGStatusForDrum.LINKMODE_MIDDLE;
        } else if (jRadioButtonJumpInc.isSelected()) {
            z = MGStatusForDrum.LINKMODE_INC;
        } else if (jRadioButtonJumpDec.isSelected()) {
            z = MGStatusForDrum.LINKMODE_DEC;
        }
        _status._drum._linkMode = z;

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
        jLabelDefaultName = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabelStartWith = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jCheckBoxCustomRange = new javax.swing.JCheckBox();
        jLabel29 = new javax.swing.JLabel();
        jSpinnerMin = new javax.swing.JSpinner();
        jSpinnerMax = new javax.swing.JSpinner();
        jCheckBoxCC14bit = new javax.swing.JCheckBox();
        jLabel15 = new javax.swing.JLabel();
        jButtonResetValueRange = new javax.swing.JButton();
        jTextFieldTemplate = new javax.swing.JTextField();
        jTextFieldChannel = new javax.swing.JTextField();
        jTextFieldGate = new javax.swing.JTextField();
        jPanelPage2 = new javax.swing.JPanel();
        jPanelValue = new javax.swing.JPanel();
        jCheckBoxDrumModeToggle = new javax.swing.JCheckBox();
        jCheckBoxDrumOnlySwitch = new javax.swing.JCheckBox();
        jSpinnerDrumOnRangeMax = new javax.swing.JSpinner();
        jLabelInputText = new javax.swing.JLabel();
        jSpinnerDrumMouseOffValue = new javax.swing.JSpinner();
        jSpinnerDrumMouseOnValue = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jSpinnerDrumOnRangeMin = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabelOffRange = new javax.swing.JLabel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanelTabTemplate = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldTemplateForOut = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabelTemplateTextGate = new javax.swing.JLabel();
        jButtonCCTemplateGate = new javax.swing.JButton();
        jLabelBlank6 = new javax.swing.JLabel();
        jButtonCCTemplate = new javax.swing.JButton();
        jLabel42 = new javax.swing.JLabel();
        jPanelTabProgram = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jSpinnerDrumProgMSB = new javax.swing.JSpinner();
        jSpinnerDrumProgPC = new javax.swing.JSpinner();
        jSpinnerDrumProgLSB = new javax.swing.JSpinner();
        jLabel21 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabelBlank3 = new javax.swing.JLabel();
        jComboBoxProgram = new javax.swing.JComboBox<>();
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
        jToggleButtonPlay = new javax.swing.JToggleButton();
        jLabel23 = new javax.swing.JLabel();
        jPanelTabLink = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jRadioButtonLinkKnob1 = new javax.swing.JRadioButton();
        jRadioButtonLinkKnob2 = new javax.swing.JRadioButton();
        jRadioButtonLinkKnob3 = new javax.swing.JRadioButton();
        jRadioButtonLinkKnob4 = new javax.swing.JRadioButton();
        jRadioButtonLinkSlider = new javax.swing.JRadioButton();
        jComboBoxLinkColumn = new javax.swing.JComboBox<>();
        jLabel34 = new javax.swing.JLabel();
        jLabelBlank5 = new javax.swing.JLabel();
        jRadioButtonJumpValue = new javax.swing.JRadioButton();
        jRadioButtonJumpInc = new javax.swing.JRadioButton();
        jRadioButtonJumpDec = new javax.swing.JRadioButton();
        jRadioButtonJumpMax = new javax.swing.JRadioButton();
        jRadioButtonJumpMin = new javax.swing.JRadioButton();
        jRadioButtonJumpMiddle = new javax.swing.JRadioButton();
        jPanelOutput = new javax.swing.JPanel();
        jRadioButtonDrumTypeNotes = new javax.swing.JRadioButton();
        jRadioButtonDrumTypeProgram = new javax.swing.JRadioButton();
        jRadioButtonDrumTypeSequence = new javax.swing.JRadioButton();
        jRadioButtonDrumTypeCustom = new javax.swing.JRadioButton();
        jRadioButtonDrumTypeSame = new javax.swing.JRadioButton();
        jRadioButtonDrumTypeDontSend = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jComboBoxOutPort = new javax.swing.JComboBox<>();
        jComboBoxOutChannel = new javax.swing.JComboBox<>();
        jRadioButtonDrumTypeLinkSlider = new javax.swing.JRadioButton();
        jLabel35 = new javax.swing.JLabel();
        jLabelBlank7 = new javax.swing.JLabel();
        jComboBoxOutTypeOn = new javax.swing.JComboBox<>();
        jComboBoxOutTypeOff = new javax.swing.JComboBox<>();
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
        gridBagConstraints.gridwidth = 6;
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

        jTextFieldName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jTextFieldName, gridBagConstraints);

        jTextFieldMemo.setEditable(false);
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

        jLabelDefaultName.setText("Default Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jLabelDefaultName, gridBagConstraints);

        jLabel1.setText("Start With");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jLabel1, gridBagConstraints);

        jLabelStartWith.setText("F7 00 F0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelPage1.add(jLabelStartWith, gridBagConstraints);

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

        jCheckBoxCustomRange.setText("Use Custom, If not custom, Then signal's Min ~ Max = [#VL 0-127]  [#VH #VL = 0 - 16383]");
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
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jTextFieldTemplate, gridBagConstraints);

        jTextFieldChannel.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jTextFieldChannel, gridBagConstraints);

        jTextFieldGate.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelPage1.add(jTextFieldGate, gridBagConstraints);

        jTabbedPane1.addTab("Input Config", jPanelPage1);

        jPanelPage2.setLayout(new java.awt.GridBagLayout());

        jPanelValue.setBorder(javax.swing.BorderFactory.createTitledBorder("Value"));
        jPanelValue.setLayout(new java.awt.GridBagLayout());

        jCheckBoxDrumModeToggle.setText("Singal ON -> Toggle ON/OFF");
        jCheckBoxDrumModeToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDrumModeToggleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanelValue.add(jCheckBoxDrumModeToggle, gridBagConstraints);

        jCheckBoxDrumOnlySwitch.setSelected(true);
        jCheckBoxDrumOnlySwitch.setText("Detect Only Turning ON/OFF");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jCheckBoxDrumOnlySwitch, gridBagConstraints);

        jSpinnerDrumOnRangeMax.setModel(new javax.swing.SpinnerNumberModel(127, 0, 127, 1));
        jSpinnerDrumOnRangeMax.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerDrumOnRangeMaxStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jSpinnerDrumOnRangeMax, gridBagConstraints);

        jLabelInputText.setText("<=");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        jPanelValue.add(jLabelInputText, gridBagConstraints);

        jSpinnerDrumMouseOffValue.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jSpinnerDrumMouseOffValue, gridBagConstraints);

        jSpinnerDrumMouseOnValue.setModel(new javax.swing.SpinnerNumberModel(127, 0, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jSpinnerDrumMouseOnValue, gridBagConstraints);

        jLabel11.setText("Mouse");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jLabel11, gridBagConstraints);

        jLabel13.setText("Value Range(ON)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jLabel13, gridBagConstraints);

        jSpinnerDrumOnRangeMin.setModel(new javax.swing.SpinnerNumberModel(1, 0, 127, 1));
        jSpinnerDrumOnRangeMin.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerDrumOnRangeMinStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanelValue.add(jSpinnerDrumOnRangeMin, gridBagConstraints);

        jLabel19.setText("Click(ON)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelValue.add(jLabel19, gridBagConstraints);

        jLabel25.setText("Release(OFF)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelValue.add(jLabel25, gridBagConstraints);

        jLabel39.setText("Toggle");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelValue.add(jLabel39, gridBagConstraints);

        jLabel10.setText("Detect");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jLabel10, gridBagConstraints);

        jLabel12.setText("     (OFF)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jLabel12, gridBagConstraints);

        jLabelOffRange.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelValue.add(jLabelOffRange, gridBagConstraints);

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

        jTextFieldTemplateForOut.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelTabTemplate.add(jTextFieldTemplateForOut, gridBagConstraints);

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

        jLabelTemplateTextGate.setText("jLabel36");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabTemplate.add(jLabelTemplateTextGate, gridBagConstraints);

        jButtonCCTemplateGate.setText("...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        jPanelTabTemplate.add(jButtonCCTemplateGate, gridBagConstraints);

        jLabelBlank6.setText("Blank6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelTabTemplate.add(jLabelBlank6, gridBagConstraints);

        jButtonCCTemplate.setText("...");
        jButtonCCTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCCTemplateActionPerformed(evt);
            }
        });
        jPanelTabTemplate.add(jButtonCCTemplate, new java.awt.GridBagConstraints());

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
        jPanelTabProgram.add(jSpinnerDrumProgMSB, gridBagConstraints);
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

        jComboBoxProgram.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Increment (+1)", "Decremnt I(-1)", "Fixed Program" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabProgram.add(jComboBoxProgram, gridBagConstraints);

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

        jTextFieldSequenceFile.setEditable(false);
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
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelTabSequener.add(jLabelBlank4, gridBagConstraints);

        jToggleButtonPlay.setText("TEST");
        jToggleButtonPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonPlayActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelTabSequener.add(jToggleButtonPlay, gridBagConstraints);

        jLabel23.setText("If Output or Output Value [Dont't Send], music never end.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabSequener.add(jLabel23, gridBagConstraints);

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
        jRadioButtonLinkKnob1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonLinkKnob1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jRadioButtonLinkKnob1, gridBagConstraints);

        jRadioButtonLinkKnob2.setText("Knob2");
        jRadioButtonLinkKnob2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonLinkKnob2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jRadioButtonLinkKnob2, gridBagConstraints);

        jRadioButtonLinkKnob3.setText("Knob3");
        jRadioButtonLinkKnob3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonLinkKnob3ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jRadioButtonLinkKnob3, gridBagConstraints);

        jRadioButtonLinkKnob4.setText("Knob4");
        jRadioButtonLinkKnob4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonLinkKnob4ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jRadioButtonLinkKnob4, gridBagConstraints);

        jRadioButtonLinkSlider.setText("Slider");
        jRadioButtonLinkSlider.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonLinkSliderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jRadioButtonLinkSlider, gridBagConstraints);

        jComboBoxLinkColumn.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Same Column (Pad)", "1", "2", "3", "..." }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelTabLink.add(jComboBoxLinkColumn, gridBagConstraints);

        jLabel34.setText("Value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jLabel34, gridBagConstraints);

        jLabelBlank5.setText("Blank5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelTabLink.add(jLabelBlank5, gridBagConstraints);

        jRadioButtonJumpValue.setText("See> Output/Output Value/On Value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelTabLink.add(jRadioButtonJumpValue, gridBagConstraints);

        jRadioButtonJumpInc.setText("Inc");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabLink.add(jRadioButtonJumpInc, gridBagConstraints);

        jRadioButtonJumpDec.setText("Dec");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabLink.add(jRadioButtonJumpDec, gridBagConstraints);

        jRadioButtonJumpMax.setText("Max");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabLink.add(jRadioButtonJumpMax, gridBagConstraints);

        jRadioButtonJumpMin.setText("Min");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabLink.add(jRadioButtonJumpMin, gridBagConstraints);

        jRadioButtonJumpMiddle.setText("Middle");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelTabLink.add(jRadioButtonJumpMiddle, gridBagConstraints);

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
        jRadioButtonDrumTypeProgram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDrumTypeProgramActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jRadioButtonDrumTypeProgram, gridBagConstraints);

        jRadioButtonDrumTypeSequence.setText("Sequence");
        jRadioButtonDrumTypeSequence.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDrumTypeSequenceActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jRadioButtonDrumTypeSequence, gridBagConstraints);

        jRadioButtonDrumTypeCustom.setText("Custom Template");
        jRadioButtonDrumTypeCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDrumTypeCustomActionPerformed(evt);
            }
        });
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
        jRadioButtonDrumTypeDontSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDrumTypeDontSendActionPerformed(evt);
            }
        });
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

        jComboBoxOutPort.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "As Input", "A", "B", "C", "D", "..." }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jComboBoxOutPort, gridBagConstraints);

        jComboBoxOutChannel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "As Input", "1", "2", "3", "4", "5", "..", "16" }));
        jComboBoxOutChannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxOutChannelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jComboBoxOutChannel, gridBagConstraints);

        jRadioButtonDrumTypeLinkSlider.setText("Link Slider/Knob");
        jRadioButtonDrumTypeLinkSlider.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDrumTypeLinkSliderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jRadioButtonDrumTypeLinkSlider, gridBagConstraints);

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

        jComboBoxOutTypeOn.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "On Value As Input", "On Value As [Mouse Click]" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jComboBoxOutTypeOn, gridBagConstraints);

        jComboBoxOutTypeOff.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Off Value As Input", "Off Value As [Mouse Release]", "Off Value is Notihng to Send" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelOutput.add(jComboBoxOutTypeOff, gridBagConstraints);

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
        buildStatusFromPanelSlider();
        buildStatusFromPanelDrum();
        disableUnusedOnPanel();
        if (validateStatus() > 0) {
            return;
        }
        _okOption = true;
        MXUtil.getOwnerWindow(this).setVisible(false);

    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        _okOption = false;
        MXUtil.getOwnerWindow(this).setVisible(false);

    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jTextFieldMemoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldMemoActionPerformed
    }//GEN-LAST:event_jTextFieldMemoActionPerformed

    public void startEdit() {
        String text = _status._base.getTemplateAsText();
        NavigatorForCommandText textNavi = new NavigatorForCommandText(text);
        MXUtil.showAsDialog(this, textNavi, "Select Template");
        if (textNavi.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
            MXMessage message = MXMessageFactory.fromCCXMLText(0, textNavi.getReturnValue(), 0, null, null);
            if (message != null) {
                _status.setBaseMessage(message);
                displayStatusToPanelSlider();
                disableUnusedOnPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Compile message failed.", "Error", JOptionPane.OK_OPTION);
            }
        }
    }

    public void startBrowseXML() {
        PickerForControlChange picker = new PickerForControlChange(false);
        MXUtil.showAsDialog(jTextFieldTemplate, picker, "Which You Choose?");
        if (picker.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
            CCMParser ccm = picker.getReturnValue();

            if (ccm == null) {
                return;
            }

            String data = ccm._data;
            String name = ccm._name;
            String memo = ccm._memo;
            MXRangedValue gate = ccm._gate;
            MXWrapList<Integer> gateTable = ccm._gateTable;
            MXRangedValue value = ccm._value;
            MXWrapList<Integer> valueTable = ccm._valueTable;
            MXTemplate template = null;
            try {
                template = new MXTemplate(data);
            } catch (IllegalFormatException ex) {
                ex.printStackTrace();
                return;
            }

            MXMessage message = MXMessageFactory.fromTemplate(_status._port, template, _status.getChannel(), gate, value);
            _status.setBaseMessage(message);
            _status._outValueTable = valueTable;
            _status._memo = memo;
            _status._name = name;
            _status._outGateTable = gateTable;
            displayStatusToPanelSlider();
        }

    }

    static final String INTERNAL_PROGINC = "Program INC";
    static final String INTERNAL_PROGDEC = "Program DEC";
    static final String INTERNAL_DATARPN = "Dataentry RPN";
    static final String INTERNAL_DATANRPN = "Dataentry NRPN";

    MXCaptureProcess _capture = null;

    public void startCapture() {
        _capture = new MXCaptureProcess();
        MXMain.setCapture(_capture);
        MXCaptureProcess capture = new MXCaptureProcess();
        MXCaptureView panel = new MXCaptureView(capture);
        MXModalFrame.showAsDialog(this, panel, "Capture ...");

        Counter retval = panel._selected;
        if (retval != null) {
            MXMessage message = retval._message;
            String dtext = retval._message.getTemplateAsText();

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
                int preferedMin = message.getValue()._min;
                int preferedMax = message.getValue()._max;
                int happenedMin = retval._value.getMin();
                int happenedMax = retval._value.getMax();
                _status.setBaseMessage(message);

                if (preferedMin == happenedMin && preferedMax == happenedMax) {
                } else {
                    int z = JOptionPane.showConfirmDialog(
                            this,
                            "min-max = " + happenedMin + "-" + happenedMax + "\n"
                            + " I will offer you reset to " + preferedMin + "-" + preferedMax,
                            "Offer (value range)",
                            JOptionPane.YES_NO_OPTION);
                    if (z == JOptionPane.NO_OPTION) {
                        _status.setCustomRange(happenedMin, happenedMax);
                    }
                }
            }
            displayStatusToPanelSlider();
        }
    }

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
        displayStatusToPanelSlider();
        disableUnusedOnPanel();
    }//GEN-LAST:event_jCheckBoxCC14bitActionPerformed

    private void jCheckBoxCustomRangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxCustomRangeActionPerformed
        boolean sel = jCheckBoxCustomRange.isSelected();
        jSpinnerMin.setEnabled(sel);
        jSpinnerMax.setEnabled(sel);
    }//GEN-LAST:event_jCheckBoxCustomRangeActionPerformed

    class NavigatorForCommandText extends NavigatorForText {

        public NavigatorForCommandText(String text) {
            super(text);
        }

        @Override
        public boolean validateWithNavigator(String text) {
            MXMessage message = MXMessageFactory.fromCCXMLText(0, text, 0, null, null);
            if (message == null) {
                JOptionPane.showMessageDialog(this, "Compile message failed.", "Error", JOptionPane.OK_OPTION);
                return false;
            }
            return true;
        }

    }

    private void jComboBoxOutChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxOutChannelActionPerformed
        disableUnusedOnPanel();
    }//GEN-LAST:event_jComboBoxOutChannelActionPerformed

    private void jRadioButtonDrumTypeSameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDrumTypeSameActionPerformed
        buildStatusFromPanelDrum();
    }//GEN-LAST:event_jRadioButtonDrumTypeSameActionPerformed

    private void jRadioButtonDrumTypeSequenceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDrumTypeSequenceActionPerformed
        buildStatusFromPanelDrum();

    }//GEN-LAST:event_jRadioButtonDrumTypeSequenceActionPerformed

    private void jRadioButtonDrumTypeNotesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDrumTypeNotesActionPerformed
        buildStatusFromPanelDrum();

    }//GEN-LAST:event_jRadioButtonDrumTypeNotesActionPerformed

    private void jCheckBoxDrumModeToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDrumModeToggleActionPerformed
    }//GEN-LAST:event_jCheckBoxDrumModeToggleActionPerformed

    private void jButtonSequenceFileBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSequenceFileBrowseActionPerformed
        MXSwingFolderBrowser browser = new MXSwingFolderBrowser(null, new FileFilterListExt(".Mid"));
        MXUtil.showAsDialog(this, browser, "Choose Standard MIDI File");
        if (browser.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
            FileList list = browser.getReturnValue();
            if (list == null || list.isEmpty()) {
                return;
            }
            String file = list.get(0).getAbsolutePath();
            jTextFieldSequenceFile.setText(file);
            _status._drum.setSwitchSongFile(file);
        }
    }//GEN-LAST:event_jButtonSequenceFileBrowseActionPerformed

    private void jButtonNotesKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNotesKeysActionPerformed
        if (_status._drum != null) {
            MXNotePicker picker = new MXNotePicker(true);
            picker.setSelectedNoteList(_status._drum.getHarmonyNotesAsArray());
            MXUtil.showAsDialog(this, picker, "NoteNumbers");
            if (picker.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
                _status._drum.setHarmoyNotesAsArray(picker.getReturnValue());
                jTextFieldHarmonyNoteList.setText(_status._drum._harmonyNotes);
            }
        }
    }//GEN-LAST:event_jButtonNotesKeysActionPerformed

    private void jButtonResetValueRangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetValueRangeActionPerformed
        _status.resetCustomRange();;
        displayStatusToPanelSlider();
        displayStatusToPanelDrum();
    }//GEN-LAST:event_jButtonResetValueRangeActionPerformed

    private void jButtonCCTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCCTemplateActionPerformed
    }//GEN-LAST:event_jButtonCCTemplateActionPerformed

    private void jSpinnerDrumOnRangeMinStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerDrumOnRangeMinStateChanged
        int onRangeMin = (Integer) jSpinnerDrumOnRangeMin.getValue();
        int onRangeMax = (Integer) jSpinnerDrumOnRangeMax.getValue();
        if (onRangeMin > onRangeMax) {
            onRangeMax = onRangeMin;
            jSpinnerDrumOnRangeMax.setValue(onRangeMax);
            return;
        }
        jLabelOffRange.setText(textForOffRange(new MXRangedValue(0, onRangeMin, onRangeMax), _status._base.getValue()));
    }//GEN-LAST:event_jSpinnerDrumOnRangeMinStateChanged

    private void jSpinnerDrumOnRangeMaxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerDrumOnRangeMaxStateChanged
        int onRangeMin = (Integer) jSpinnerDrumOnRangeMin.getValue();
        int onRangeMax = (Integer) jSpinnerDrumOnRangeMax.getValue();
        if (onRangeMin > onRangeMax) {
            onRangeMin = onRangeMax;
            jSpinnerDrumOnRangeMin.setValue(onRangeMin);
            return;
        }
        jLabelOffRange.setText(textForOffRange(new MXRangedValue(0, onRangeMin, onRangeMax), _status._base.getValue()));
    }//GEN-LAST:event_jSpinnerDrumOnRangeMaxStateChanged

    private void jRadioButtonDrumTypeCustomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDrumTypeCustomActionPerformed
        buildStatusFromPanelDrum();
    }//GEN-LAST:event_jRadioButtonDrumTypeCustomActionPerformed

    private void jRadioButtonDrumTypeProgramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDrumTypeProgramActionPerformed
        buildStatusFromPanelDrum();
    }//GEN-LAST:event_jRadioButtonDrumTypeProgramActionPerformed

    private void jRadioButtonDrumTypeLinkSliderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDrumTypeLinkSliderActionPerformed
        buildStatusFromPanelDrum();
    }//GEN-LAST:event_jRadioButtonDrumTypeLinkSliderActionPerformed

    private void jRadioButtonDrumTypeDontSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDrumTypeDontSendActionPerformed
        buildStatusFromPanelDrum();
    }//GEN-LAST:event_jRadioButtonDrumTypeDontSendActionPerformed

    private void jToggleButtonPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonPlayActionPerformed
        if (jToggleButtonPlay.isSelected()) {
            _status._drum.startSongPlayer();
        } else {
            _status._drum.stopSongPlayer();
        }
    }//GEN-LAST:event_jToggleButtonPlayActionPerformed

    private void jRadioButtonLinkSliderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonLinkSliderActionPerformed
        buildStatusFromPanelDrum();
    }//GEN-LAST:event_jRadioButtonLinkSliderActionPerformed

    private void jRadioButtonLinkKnob1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonLinkKnob1ActionPerformed
        buildStatusFromPanelDrum();
    }//GEN-LAST:event_jRadioButtonLinkKnob1ActionPerformed

    private void jRadioButtonLinkKnob2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonLinkKnob2ActionPerformed
        buildStatusFromPanelDrum();
    }//GEN-LAST:event_jRadioButtonLinkKnob2ActionPerformed

    private void jRadioButtonLinkKnob3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonLinkKnob3ActionPerformed
        buildStatusFromPanelDrum();
    }//GEN-LAST:event_jRadioButtonLinkKnob3ActionPerformed

    private void jRadioButtonLinkKnob4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonLinkKnob4ActionPerformed
        buildStatusFromPanelDrum();
    }//GEN-LAST:event_jRadioButtonLinkKnob4ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButtonCCTemplate;
    private javax.swing.JButton jButtonCCTemplateGate;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonNotesKeys;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JButton jButtonResetValueRange;
    private javax.swing.JButton jButtonSequenceFileBrowse;
    private javax.swing.JCheckBox jCheckBoxCC14bit;
    private javax.swing.JCheckBox jCheckBoxCustomRange;
    private javax.swing.JCheckBox jCheckBoxDrumModeToggle;
    private javax.swing.JCheckBox jCheckBoxDrumOnlySwitch;
    private javax.swing.JCheckBox jCheckBoxSequencerFilterNote;
    private javax.swing.JCheckBox jCheckBoxSequencerSeekStart;
    private javax.swing.JCheckBox jCheckBoxSequencerSingleTrack;
    private javax.swing.JComboBox<String> jComboBoxLinkColumn;
    private javax.swing.JComboBox<String> jComboBoxOutChannel;
    private javax.swing.JComboBox<String> jComboBoxOutPort;
    private javax.swing.JComboBox<String> jComboBoxOutTypeOff;
    private javax.swing.JComboBox<String> jComboBoxOutTypeOn;
    private javax.swing.JComboBox<String> jComboBoxProgram;
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
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
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
    private javax.swing.JLabel jLabelOffRange;
    private javax.swing.JLabel jLabelStartWith;
    private javax.swing.JLabel jLabelTemplateTextGate;
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
    private javax.swing.JRadioButton jRadioButtonDrumTypeLinkSlider;
    private javax.swing.JRadioButton jRadioButtonDrumTypeNotes;
    private javax.swing.JRadioButton jRadioButtonDrumTypeProgram;
    private javax.swing.JRadioButton jRadioButtonDrumTypeSame;
    private javax.swing.JRadioButton jRadioButtonDrumTypeSequence;
    private javax.swing.JRadioButton jRadioButtonJumpDec;
    private javax.swing.JRadioButton jRadioButtonJumpInc;
    private javax.swing.JRadioButton jRadioButtonJumpMax;
    private javax.swing.JRadioButton jRadioButtonJumpMiddle;
    private javax.swing.JRadioButton jRadioButtonJumpMin;
    private javax.swing.JRadioButton jRadioButtonJumpValue;
    private javax.swing.JRadioButton jRadioButtonLinkKnob1;
    private javax.swing.JRadioButton jRadioButtonLinkKnob2;
    private javax.swing.JRadioButton jRadioButtonLinkKnob3;
    private javax.swing.JRadioButton jRadioButtonLinkKnob4;
    private javax.swing.JRadioButton jRadioButtonLinkSlider;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSpinner jSpinnerDrumMouseOffValue;
    private javax.swing.JSpinner jSpinnerDrumMouseOnValue;
    private javax.swing.JSpinner jSpinnerDrumOnRangeMax;
    private javax.swing.JSpinner jSpinnerDrumOnRangeMin;
    private javax.swing.JSpinner jSpinnerDrumProgLSB;
    private javax.swing.JSpinner jSpinnerDrumProgMSB;
    private javax.swing.JSpinner jSpinnerDrumProgPC;
    private javax.swing.JSpinner jSpinnerMax;
    private javax.swing.JSpinner jSpinnerMin;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTextField jTextFieldChannel;
    private javax.swing.JTextField jTextFieldGate;
    private javax.swing.JTextField jTextFieldHarmonyNoteList;
    private javax.swing.JTextField jTextFieldMemo;
    private javax.swing.JTextField jTextFieldName;
    private javax.swing.JTextField jTextFieldSequenceFile;
    private javax.swing.JTextField jTextFieldTemplate;
    private javax.swing.JTextField jTextFieldTemplateForOut;
    private javax.swing.JToggleButton jToggleButtonPlay;
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

    public String textForOffRange(MXRangedValue on, MXRangedValue all) {
        String min = "";
        if (on._min > all._min) {
            if (all._min == on._min - 1) {
                min = "" + all._min;
            } else {
                min = all._min + " ~ " + (on._min - 1);
            }
        }
        String max = "";
        if (on._max < all._max) {
            if (on._max + 1 == all._max) {
                max = "" + all._max;
            } else {
                max = (on._max + 1) + " ~ " + all._max;
            }
        }
        if (min.isEmpty() || max.isEmpty()) {
            return min + max;
        }
        return min + ", " + max;

    }
}
