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
package jp.synthtarou.midimixer.mx12masterpiano;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpinnerNumberModel;
import jp.synthtarou.libs.accordionui.MXAccordionElement;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.libs.namedobject.MXNamedObjectListFactory;
import jp.synthtarou.libs.navigator.legacy.INavigator;
import jp.synthtarou.libs.navigator.legacy.NavigatorForGate;
import jp.synthtarou.libs.navigator.legacy.NavigatorForText;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.ccxml.InformationForCCM;
import jp.synthtarou.midimixer.ccxml.ui.NavigatorForCCXMLCC;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.console.MXMidiConsole;
import jp.synthtarou.midimixer.libs.midi.console.MXMidiConsoleElement;
import jp.synthtarou.midimixer.mx00playlist.MXPianoKeys;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderSingleClick;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX12MasterkeysPanel extends javax.swing.JPanel implements MXAccordionElement {

    public static void main(String[] args) {
        JFrame win = new JFrame("Piano");
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Insets ins = win.getInsets();
        win.setSize(500 + ins.left + ins.right, 500 + ins.top + ins.bottom);
        win.setLayout(new GridLayout(1, 1));
        MX12Process proc = new MX12Process();
        MX12MasterkeysPanel keys = new MX12MasterkeysPanel(proc);
        win.add(keys);
        win.setVisible(true);
    }

    final MX12Process _process;
    MXPianoKeys _piano;
    int _sentPitch = 8192;
    int _sentModulation = 0;
    MXNamedObjectList<Integer> _listCC = MXNamedObjectListFactory.listupControlChange(true);
    MXChordSignalChecker _notePicker = new MXChordSignalChecker();

    /**
     * Creates new form MX12MasterkeysPanel
     */
    public MX12MasterkeysPanel(MX12Process process) {
        initComponents();
        _process = process;

        jSliderPitch.setMinimum(0);
        jSliderPitch.setMaximum(16384 - 1);
        jSliderPitch.setValue(8192);

        new MXAttachSliderLikeEclipse(jSliderPitch);
        new MXAttachSliderLikeEclipse(jSliderModwheel);
        new MXAttachSliderSingleClick(jSliderPitch);

        jSliderPitch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                setPitchBendDX(8192);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                setPitchBendDX(8192);
            }
        });

        jSliderPitch.setMajorTickSpacing(2048);
        jSliderPitch.setMinorTickSpacing(512);
        jSliderPitch.setPaintTicks(true);

        Hashtable table = new Hashtable();
        table.put(0, new JLabel("-1.0"));
        table.put(8192, new JLabel("0"));
        table.put(16383, new JLabel("+1.0"));
        jSliderPitch.setLabelTable(table);
        jSliderPitch.setPaintLabels(false);

        jSliderModwheel.setMinimum(0);
        jSliderModwheel.setMaximum(127);
        jSliderModwheel.setValue(0);

        jSliderModwheel.setMajorTickSpacing(32);
        jSliderModwheel.setPaintTicks(true);

        Hashtable table2 = new Hashtable();
        table2.put(0, new JLabel("0"));
        table2.put(127, new JLabel("+1.0"));
        jSliderModwheel.setLabelTable(table2);
        jSliderModwheel.setPaintLabels(false);

        _piano = new MXPianoKeys();

        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setViewportView(_piano);
        
        _piano.setMinimumSize(new Dimension(9 * 200, 100));
        _piano.setPreferredSize(new Dimension(9 * 200, 150));
        _piano.updateNoteGraphics(0, 11);
        _piano.setAutoScanSize(true);

        _piano.setHandler(new MXPianoKeys.MXMouseHandler() {
            public void noteOn(int note) {
                MXMessage message = MXMessageFactory.fromNoteon(_process._mousePort, _process._mouseChannel, note, _process._mouseVelocity);
                _process.sendCCAndGetResult(message, MXMain.getMain().getInputProcess());
                if (_notePicker != null && _piano.isAllowMultiSelect()) {
                    _notePicker.noteOn(note);
                    _notePicker.refresh();
                    ArrayList<String> listChord = _notePicker.getChordName();
                    _labelAfterName.setChordNames(listChord.toString() + ", " + _notePicker.getNotes());
                }
            }

            public void noteOff(int note) {
                MXMessage message = MXMessageFactory.fromNoteoff(_process._mousePort, _process._mouseChannel, note);
                _process.sendCCAndGetResult(message, MXMain.getMain().getInputProcess());
                if (_notePicker != null && _piano.isAllowMultiSelect()) {
                    _notePicker.noteOff(note);
                    _notePicker.refresh();
                    ArrayList<String> listChord = _notePicker.getChordName();
                    _labelAfterName.setChordNames(listChord.toString() + ", " + _notePicker.getNotes());
                }
            }

            @Override
            public void selectionChanged() {
            }
        });

        final String title = "Select Output to Connect.";
        new MXAttachSliderSingleClick(jSliderModwheel);

        setMinimumSize(new Dimension(150, 200));
        _labelAfterName = new MX12LabelAfterName(_process);

        jButtonHelp.setBackground(Color.white);
        jButtonHelp.setBorder(BorderFactory.createLineBorder(Color.black));
        jButtonHelp.setMinimumSize(new Dimension(20, jButtonHelp.getMinimumSize().height));
        jButtonBrowseCC.setBackground(Color.white);
        jButtonBrowseCC.setBorder(BorderFactory.createLineBorder(Color.black));
        jButtonBrowseCC.setMinimumSize(new Dimension(20, jButtonHelp.getMinimumSize().height));

        jSpinnerCCValue.setModel(new SpinnerNumberModel(0, 0, 127, 1));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (jTabbedPane1.getSelectedIndex() > 0) {
                    jSplitPane1.setDividerLocation(jSplitPane1.getWidth() / 2);
                }
                int height = jTextFieldGate.getHeight();
                jTextFieldGate.setMinimumSize(new Dimension(100, height));
            }

            @Override
            public void componentShown(ComponentEvent e) {
                if (jTabbedPane1.getSelectedIndex() > 0) {
                    jSplitPane1.setDividerLocation(jSplitPane1.getWidth() / 2);
                }
                int height = jTextFieldGate.getHeight();
                jTextFieldGate.setMinimumSize(new Dimension(100, height));
            }
        });

        _baseMessage = MXMessageFactory.fromShortMessage(0, MXMidiStatic.COMMAND_CH_NOTEON, 64, 0);

        scrollToCenter();
        _input.bind(jListInput);
        _output.bind(jListOutput);

        showAll();
        _stopFeedback--;
    }

    MXMidiConsole _input = new MXMidiConsole();
    MXMidiConsole _output = new MXMidiConsole();

    MX12LabelAfterName _labelAfterName;
    int _stopFeedback = 1;

    public void scrollToCenter() {
        Dimension scrollSize = jScrollPane1.getSize();
        Dimension pianoSize = _piano.getSize();
        Rectangle rect = new Rectangle((int) (pianoSize.getWidth() - scrollSize.getWidth()) / 2, 0, (int) scrollSize.getWidth(), 50);
        jScrollPane1.getViewport().scrollRectToVisible(rect);
    }

    public void setPitchBendDX(int value) {
        MXMain.invokeUI(() ->  {
            if (_sentPitch != value) {
                _sentPitch = value;
                jSliderPitch.setValue(value);

                MXMessage msg  = MXMessageFactory.fromTemplate(_process._mousePort
                        , MXMidiStatic.TEMPLATE_CCXMLPB, _process._mouseChannel
                        , MXRangedValue.ZERO7, MXRangedValue.new14bit(value));

                //MXMessage msg = MXMessageFactory.fromShortMessage(_process._mousePort, MXMidi.COMMAND_CH_PITCHWHEEL + _process._mouseChannel, 0, 0);
                if (msg.indexOfValueHi() >= 0) {
                    msg.setValue(MXRangedValue.new14bit(value));
                } else {
                    msg.setValue(MXRangedValue.new7bit(value));
                }
                _process.sendCCAndGetResult(msg, MXMain.getMain().getInputProcess());
            }
	});
    }

    public void setModulatoinWheelDX(int value) {
        MXMain.invokeUI(() ->  {
            if (_sentModulation != value) {
                _sentModulation = value;
                jSliderModwheel.setValue(value);

                _sentModulation = value;
                MXMessage msg = MXMessageFactory.fromControlChange(_process._mousePort, _process._mouseChannel, MXMidiStatic.DATA1_CC_MODULATION, 0);
                msg.setValue(MXRangedValue.new7bit(value));
                _process.sendCCAndGetResult(msg, MXMain.getMain().getInputProcess());
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jSliderPitch = new javax.swing.JSlider();
        jSliderModwheel = new javax.swing.JSlider();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jSpinnerCCValue = new javax.swing.JSpinner();
        jButtonSendCCNow = new javax.swing.JButton();
        jCheckBox14bit = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldCCCommand = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldGate = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListOutput = new javax.swing.JList<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListInput = new javax.swing.JList<>();
        jButtonHelp = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButtonBrowseCC = new javax.swing.JButton();
        jLabelMean = new javax.swing.JLabel();

        jLabel1.setText("jLabel1");

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jSliderPitch.setOrientation(javax.swing.JSlider.VERTICAL);
        jSliderPitch.setToolTipText("Pitch");
        jSliderPitch.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderPitchStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jPanel1.add(jSliderPitch, gridBagConstraints);

        jSliderModwheel.setOrientation(javax.swing.JSlider.VERTICAL);
        jSliderModwheel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jSliderModwheel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderModwheelStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jPanel1.add(jSliderModwheel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jTabbedPane1.addTab("Touch Piano", jPanel1);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jSpinnerCCValue.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerCCValueStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 6;
        jPanel2.add(jSpinnerCCValue, gridBagConstraints);

        jButtonSendCCNow.setText("SendNow");
        jButtonSendCCNow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSendCCNowActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jButtonSendCCNow, gridBagConstraints);

        jCheckBox14bit.setText("14bit");
        jCheckBox14bit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox14bitActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 6;
        jPanel2.add(jCheckBox14bit, gridBagConstraints);

        jLabel3.setText("CC Command");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        jPanel2.add(jLabel3, gridBagConstraints);

        jTextFieldCCCommand.setEditable(false);
        jTextFieldCCCommand.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTextFieldCCCommandMousePressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jTextFieldCCCommand, gridBagConstraints);

        jLabel4.setText("Gate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        jPanel2.add(jLabel4, gridBagConstraints);

        jTextFieldGate.setEditable(false);
        jTextFieldGate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTextFieldGateMousePressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jTextFieldGate, gridBagConstraints);

        jLabel5.setText("Value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        jPanel2.add(jLabel5, gridBagConstraints);

        jSplitPane1.setDividerLocation(200);

        jListOutput.setBorder(javax.swing.BorderFactory.createTitledBorder("Result"));
        jListOutput.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListOutputValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jListOutput);

        jSplitPane1.setRightComponent(jScrollPane2);

        jListInput.setBorder(javax.swing.BorderFactory.createTitledBorder("History"));
        jListInput.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListInputValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jListInput);

        jSplitPane1.setLeftComponent(jScrollPane3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jSplitPane1, gridBagConstraints);

        jButtonHelp.setText("?");
        jButtonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHelpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        jPanel2.add(jButtonHelp, gridBagConstraints);

        jButton1.setText("fromHistory");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel2.add(jButton1, gridBagConstraints);

        jButtonBrowseCC.setText("...");
        jButtonBrowseCC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseCCActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        jPanel2.add(jButtonBrowseCC, gridBagConstraints);

        jLabelMean.setText(" =");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jLabelMean, gridBagConstraints);

        jTabbedPane1.addTab("Messanger", jPanel2);

        add(jTabbedPane1);
    }// </editor-fold>//GEN-END:initComponents

    private void jSliderModwheelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderModwheelStateChanged
        if (_stopFeedback > 0) {
            return;
        }
        int value = jSliderModwheel.getValue();
        setModulatoinWheelDX(value);
    }//GEN-LAST:event_jSliderModwheelStateChanged

    private void jSliderPitchStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderPitchStateChanged
        if (_stopFeedback > 0) {
            return;
        }
        int value = jSliderPitch.getValue();
        setPitchBendDX(value);
    }//GEN-LAST:event_jSliderPitchStateChanged

    public void changed14Bit(boolean new14bit) {
        if (_stopFeedback > 0) {
            return;
        }
        _stopFeedback++;
        try {
            int ccValue = (int) jSpinnerCCValue.getValue();
            if (new14bit) {
                int adjustValue = MXRangedValue.new7bit(ccValue).changeRange(0, 128 * 128 - 1)._value;
                jSpinnerCCValue.setModel(new SpinnerNumberModel(adjustValue, 0, 128 * 128 - 1, 1));
            } else {
                int adjustValue = MXRangedValue.new14bit(ccValue).changeRange(0, 127)._value;
                jSpinnerCCValue.setModel(new SpinnerNumberModel(adjustValue, 0, 128 - 1, 1));
            }
        } finally {
            _stopFeedback--;
        }
    }

    public void showAll() {
        _stopFeedback++;
        try {
            String text;
            
            boolean new14Bit = false;
            if (_baseMessage.getTemplate().get(0) == MXMidiStatic.COMMAND_CH_CONTROLCHANGE) {
                if (_baseMessage.getCompiled(1)>=0 && _baseMessage.getCompiled(1) <= 31) {
                    new14Bit = true;
                }
            }

            if (new14Bit) {
                jCheckBox14bit.setEnabled(true);
            } else {
                jCheckBox14bit.setEnabled(false);
                if (jCheckBox14bit.isSelected()) {
                    changed14Bit(false);
                }
                jCheckBox14bit.setSelected(false);
            }
            
            jLabelMean.setText(" mean = " + _baseMessage.toString());

            jTextFieldCCCommand.setText(_baseMessage.getTemplateAsText());
            jTextFieldGate.setText(String.valueOf(_baseMessage.getGate()._value));
            jSpinnerCCValue.setValue(_baseMessage.getValue()._value);
        } finally {
            _stopFeedback--;
        }
    }
    
    private void jSpinnerCCValueStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerCCValueStateChanged
        _baseMessage.setValue((int)jSpinnerCCValue.getValue());
        showAll();
    }//GEN-LAST:event_jSpinnerCCValueStateChanged

    private void jButtonSendCCNowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSendCCNowActionPerformed
        MXMessage message = (MXMessage)_baseMessage.clone();
        _process.sendCCAndGetResult(message, MXMain.getMain().getInputProcess());
    }//GEN-LAST:event_jButtonSendCCNowActionPerformed

    private void jCheckBox14bitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox14bitActionPerformed
        // TODO addThreaded your handling code here:
        changed14Bit(jCheckBox14bit.isSelected());
        showAll();
    }//GEN-LAST:event_jCheckBox14bitActionPerformed

    private void jButtonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHelpActionPerformed
        // TODO addThreaded your handling code here:
        MX12HelpPanel panel = new MX12HelpPanel();
        MXUtil.showAsDialog(null, panel, "Text Command Memo");
    }//GEN-LAST:event_jButtonHelpActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        if (jTabbedPane1.getSelectedIndex() >= 0) {
            jSplitPane1.setDividerLocation(jSplitPane1.getWidth() / 2);
        }
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void jTextFieldGateMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextFieldGateMousePressed
        startEditGate();
        showAll();
    }//GEN-LAST:event_jTextFieldGateMousePressed

    MXMessage _baseMessage;

    public void startBrowseXML() {
        NavigatorForCCXMLCC navi = new NavigatorForCCXMLCC();
        MXUtil.showAsDialog(this, navi, "Which You Choose?");
        if (navi.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
            List<InformationForCCM> ccmList = navi.getReturnValue();
            InformationForCCM ccm = null;
            if (ccmList != null && ccmList.isEmpty() == false) {
                ccm = ccmList.getFirst();
            }

            if (ccm == null) {
                return;
            }

            String data = ccm._data;
            String name = ccm._name;
            String memo = ccm._memo;
            MXRangedValue gate = ccm.getParsedGate();
            MXNamedObjectList<Integer> gateTable = ccm.getParsedGateTable();
            MXRangedValue value = ccm.getParsedValue();
            MXNamedObjectList<Integer> valueTable = ccm.getParsedValueTable();
            MXTemplate template = null;
            try {
                template = new MXTemplate(data);
            } catch (IllegalFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Text Command", "Error", JOptionPane.OK_OPTION);
                return;
            }
            
            try {
                if (template.size() >= 3) {
                    if (template.get(0) == MXMidiStatic.COMMAND_CH_CONTROLCHANGE && template.get(1) != MXMidiStatic.CCXML_GL) {
                        gate = MXRangedValue.new7bit(template.get(1));
                        template = new MXTemplate(new int[]{MXMidiStatic.COMMAND_CH_CONTROLCHANGE, MXMidiStatic.CCXML_GL, MXMidiStatic.CCXML_VL});
                    }
                }
            }catch(Throwable ex) {
                MXFileLogger.getLogger(MX12MasterkeysPanel.class).log(Level.SEVERE, ex.getMessage(), ex);
            }

            MXMessage message = MXMessageFactory.fromTemplate(0, template, 0, gate, value);
            message.setGate(gate);
            message.setValue(_baseMessage.getValue());
            _baseMessage = message;
            jTextFieldCCCommand.setText(message.getTemplateAsText());
            jTextFieldGate.setText(String.valueOf(message.getGate()._value));
            jSpinnerCCValue.setValue(_baseMessage.getValue()._value);
        }
    }

    private void jButtonBrowseCCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseCCActionPerformed
        startBrowseXML();
        showAll();
    }//GEN-LAST:event_jButtonBrowseCCActionPerformed

    private void jTextFieldCCCommandMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextFieldCCCommandMousePressed
        startEditCommand();
        showAll();
    }//GEN-LAST:event_jTextFieldCCCommandMousePressed

    private void jListInputValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListInputValueChanged
        int index = jListInput.getSelectedIndex();
        if (index >= 0) {
            MXMidiConsoleElement e = _input.elementAt(index);
            if (e != null) {
                MXMessage message = e.getMessage();
                _input.setHIghlightOwner(message);
                _output.setHIghlightOwner(message);
                jListInput.ensureIndexIsVisible(index);
                jListOutput.ensureIndexIsVisible(index);
            }
        }
    }//GEN-LAST:event_jListInputValueChanged

    private void jListOutputValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListOutputValueChanged
        int index = jListOutput.getSelectedIndex();
        if (index >= 0) {
            MXMidiConsoleElement e = _output.elementAt(index);
            if (e != null) {
                MXMessage message = e.getMessage();
                _input.setHIghlightOwner(message);
                _output.setHIghlightOwner(message);
                jListInput.ensureIndexIsVisible(index);
                jListOutput.ensureIndexIsVisible(index);
            }
        }
    }//GEN-LAST:event_jListOutputValueChanged

    public void startEditCommand() {
        NavigatorForText navi = new NavigatorForText(jTextFieldCCCommand.getText()) {
            @Override
            public boolean validateWithNavigator(String result) {
                try {
                    MXTemplate temp = new MXTemplate(result);
                    return true;
                }catch(Throwable ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
                return false;
            }
        }; 
        if (navi.simpleAsk(this)) {
            try {
                String text = navi.getReturnValue();
                MXTemplate template = new MXTemplate(text);
                if (template.size() >= 2) {
                    int command = template.get(0) & 0xfff0;
                    int gate = template.get(1);
                    if (command == MXMidiStatic.COMMAND_CH_CONTROLCHANGE && gate != MXMidiStatic.CCXML_GL) {
                        int[] newTemplate = new int[template.size()];
                        for (int i = 0; i < newTemplate.length; ++ i) {
                            newTemplate[i] = template.get(i);
                        }
                        newTemplate[1] = gate;
                        template = new MXTemplate(newTemplate);
                        _baseMessage = MXMessageFactory.fromTemplate(0, template, 0, new MXRangedValue(gate, 0, 128), MXRangedValue.ZERO7);
                        _baseMessage.setGate(MXRangedValue.new7bit(gate));
                        jTextFieldCCCommand.setText(_baseMessage.getTemplateAsText());
                    }
                    else {
                        _baseMessage = MXMessageFactory.fromTemplate(0, template, 0, MXRangedValue.ZERO7, MXRangedValue.ZERO7);
                        jTextFieldCCCommand.setText(_baseMessage.getTemplateAsText());
                    }
                    showAll();
                }
            }catch(Throwable ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    public void startEditGate() {
        int command = 0;
        int gate = 0;
        if (_baseMessage != null) {
            command = _baseMessage.getStatus() & 0xf0;
            gate = _baseMessage.getGate()._value;
        }
        NavigatorForGate navi = new NavigatorForGate(command, gate);
        if (navi.simpleAsk(this)) {
            int x = navi.getReturnValue();
            _baseMessage.setGate(x);
            jTextFieldGate.setText(String.valueOf(x));
            showAll();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonBrowseCC;
    private javax.swing.JButton jButtonHelp;
    private javax.swing.JButton jButtonSendCCNow;
    private javax.swing.JCheckBox jCheckBox14bit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelMean;
    private javax.swing.JList<String> jListInput;
    private javax.swing.JList<String> jListOutput;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSlider jSliderModwheel;
    private javax.swing.JSlider jSliderPitch;
    private javax.swing.JSpinner jSpinnerCCValue;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldCCCommand;
    private javax.swing.JTextField jTextFieldGate;
    // End of variables declaration//GEN-END:variables

    @Override
    public JPanel getAccordionView() {
        return this;
    }

    @Override
    public void repaintAccordion() {
        invalidate();
        repaint();
    }

    @Override
    public void accordionFocused(boolean flag) {
        //mothing
    }

    public JComponent getComponentAfterName() {
        return _labelAfterName;
    }

    public void updateViewForSettingChange() {
        if (_labelAfterName != null) {
            _labelAfterName.reloadSetting();
        }
    }
    
    public void setDebugMode(boolean mode) {
        jTabbedPane1.setSelectedIndex(mode ? 1 : 0);
    }
    
    public void clearSelectedNote() {
        //_notePicker.clearSelectedNote();
        _piano.clearSelectedNote();
    }
}
