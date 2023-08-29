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
package jp.synthtarou.midimixer.mx40layer;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.MXGlobalTimer;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.programlist.database.ProgramPicker;
import jp.synthtarou.midimixer.libs.swing.MXSwingFileChooser;
import jp.synthtarou.midimixer.libs.swing.SafeSpinnerNumberModel;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachTableResize;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX40View extends javax.swing.JPanel implements TableModelListener {
    private static final MXDebugPrint _debug = new MXDebugPrint(MX40View.class);

    public MX40Process _process;
    
    MX40Group _editingGroup;
    MX40Layer _editingLayer;

    //MXWrapList<Integer> _watchPort = MXMidi.createPortShort();
    MXWrapList<Integer> _watchChannel = MXMidi.listupChannel(false);
    MXWrapList<Integer> _watchProgram = MXMidi.listupProgramNumber();

    MXWrapList<Integer> _modPort = MX40Layer.createSendOption(false);
    MXWrapList<Integer> _modChannel = MX40Layer.createSendOption(false);
    MXWrapList<Integer> _modBank = MX40Layer.createSendOption(true);
    MXWrapList<Integer> _modProgram = MX40Layer.createSendOption(true);

    //MXWrapList<Integer> _changePort = MXMidi.createPortShort();
    MXWrapList<Integer> _changeChannel = MXMidi.listupChannel(false);
    MXWrapList<Integer> _changeProgram = MXMidi.listupProgramNumber();
    MXWrapList<Integer> _changeVolume = MXMidi.listupPercent();
    MXWrapList<Integer> _changeExpression = MXMidi.listupPercent();

    MXWrapList<Integer> _changeLowNote = MXMidi.listupNoteNo();
    MXWrapList<Integer> _changeHighNote = MXMidi.listupNoteNo();

    MXWrapList<Integer> _changeLowVelocity = MXMidi.listupVelocity();
    MXWrapList<Integer> _changeHighVelocity = MXMidi.listupVelocity();

    MXWrapList<MX40Group> _groupsModel = null;
    MXWrapList<MX40Layer> _layersModel = null;
    
    boolean underConstruction = true;

    public MX40View(MX40Process process) {
        initComponents();
        _process = process;

        jTextFieldGroupName.setText("New Group");

        _editingGroup = null;
        _editingLayer = null;
        //jComboBoxWatchPort.setModel(_watchPort);
        jComboBoxWatchChannel.setModel(_watchChannel);
        
        jTextFieldLayerName.setText("New Layer");

        jComboBoxModPort.setModel(_modPort);
        //jComboBoxSendPort.setModel(_changePort);
        jComboBoxModChannel.setModel(_modChannel);
        jComboBoxSendChannel.setModel(_changeChannel);
        jComboBoxModBank.setModel(_modBank);
        jComboBoxModProgram.setModel(_modProgram);
        
        jSpinnerSendProgram.setModel(new SafeSpinnerNumberModel(0, 0, 127, 1));
        jSpinnerSendBankMSB.setModel(new SafeSpinnerNumberModel(0, 0, 127, 1));
        jSpinnerSendBankLSB.setModel(new SafeSpinnerNumberModel(0, 0, 127, 1));
        
        jSpinnerSendFixedPan.setModel(new SafeSpinnerNumberModel(0, 0, 127, 1));
        jSpinnerSendAdjustExpression.setModel(new SafeSpinnerNumberModel(100, 0, 100, 1));
        jSpinnerSendAdjustTranspose.setModel(new SafeSpinnerNumberModel(0, -128, 128, 1));
        jSpinnerSendAdjustVelocity.setModel(new SafeSpinnerNumberModel(0, -128, 128, 1));

        jComboBoxSendNoteLow.setModel(_changeLowNote);
        jComboBoxSendNoteHigh.setModel(_changeHighNote);

        jComboBoxSendVelocityLow.setModel(_changeLowVelocity);
        jComboBoxSendVelocityHi.setModel(_changeHighVelocity);

        jCheckBoxUseLayer.setSelected(process.isUsingThisRecipe());

        _editingGroup = null;
        _editingLayer = null;

        jTable1.setModel(_process._inputInfo);
        jTable2.setModel(_process._outputInfo);
        
        _process._inputInfo.addTableModelListener(this);
        _process._outputInfo.addTableModelListener(this);
        
        new MXAttachTableResize(jTable1);
        new MXAttachTableResize(jTable2);

        startEditingPack(0, 0);
        disableUnusedOnPanel();
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jCheckBoxUseLayer = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jPanelGroup = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldGroupName = new javax.swing.JTextField();
        jButtonWatchPort = new javax.swing.JButton();
        jComboBoxWatchChannel = new javax.swing.JComboBox<>();
        jCheckBoxPort = new javax.swing.JCheckBox();
        jCheckBoxChannel = new javax.swing.JCheckBox();
        jCheckBoxBank = new javax.swing.JCheckBox();
        jCheckBoxProgram = new javax.swing.JCheckBox();
        jCheckBoxRotateLayer = new javax.swing.JCheckBox();
        jButtonNewGroup = new javax.swing.JButton();
        jButtonApplyGroup = new javax.swing.JButton();
        jButtonRemoveGroup = new javax.swing.JButton();
        jButtonGroupProgram = new javax.swing.JButton();
        jButtonGroupUp = new javax.swing.JButton();
        jButtonGroupDown = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListGroupList = new javax.swing.JList<>();
        jSpinnerRotatePoly = new javax.swing.JSpinner();
        jSpinnerWatchProgram = new javax.swing.JSpinner();
        jSpinnerWatchBankMSB = new javax.swing.JSpinner();
        jSpinnerWatchBankLSB = new javax.swing.JSpinner();
        jPanelLayer = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldLayerName = new javax.swing.JTextField();
        jButtonNewLayer = new javax.swing.JButton();
        jButtonApplyLayer = new javax.swing.JButton();
        jButtonRemoveLayer = new javax.swing.JButton();
        jButtonLayerUp = new javax.swing.JButton();
        jButtonLayerDown = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListLayerList = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jComboBoxModPort = new javax.swing.JComboBox<>();
        jButtonSendPort = new javax.swing.JButton();
        jComboBoxModChannel = new javax.swing.JComboBox<>();
        jComboBoxSendChannel = new javax.swing.JComboBox<>();
        jComboBoxModProgram = new javax.swing.JComboBox<>();
        jSpinnerSendProgram = new javax.swing.JSpinner();
        jComboBoxModBank = new javax.swing.JComboBox<>();
        jSpinnerSendBankMSB = new javax.swing.JSpinner();
        jSpinnerSendBankLSB = new javax.swing.JSpinner();
        jSpinnerSendFixedPan = new javax.swing.JSpinner();
        jSpinnerSendAdjustExpression = new javax.swing.JSpinner();
        jComboBoxSendNoteLow = new javax.swing.JComboBox<>();
        jComboBoxSendNoteHigh = new javax.swing.JComboBox<>();
        jSpinnerSendAdjustTranspose = new javax.swing.JSpinner();
        jSpinnerSendAdjustVelocity = new javax.swing.JSpinner();
        jLabel14 = new javax.swing.JLabel();
        jLabelTargetGroupName = new javax.swing.JLabel();
        jButtonLayerProgram = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jCheckBoxFixPan = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxSendVelocityLow = new javax.swing.JComboBox<>();
        jComboBoxSendVelocityHi = new javax.swing.JComboBox<>();
        jLabel18 = new javax.swing.JLabel();
        jPanel0 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButtonImport = new javax.swing.JButton();
        jButtonExport = new javax.swing.JButton();

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable3);

        setLayout(new java.awt.GridBagLayout());

        jCheckBoxUseLayer.setText("Use This Recipe");
        jCheckBoxUseLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseLayerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jCheckBoxUseLayer, gridBagConstraints);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        jSplitPane1.setDividerLocation(700);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanelGroup.setBorder(javax.swing.BorderFactory.createTitledBorder("1.Edit Group"));
        jPanelGroup.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Group Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelGroup.add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        jPanelGroup.add(jTextFieldGroupName, gridBagConstraints);

        jButtonWatchPort.setText("-");
        jButtonWatchPort.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonWatchPortMousePressed(evt);
            }
        });
        jButtonWatchPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWatchPortActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelGroup.add(jButtonWatchPort, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        jPanelGroup.add(jComboBoxWatchChannel, gridBagConstraints);

        jCheckBoxPort.setText("Watch Port");
        jCheckBoxPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxPortActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanelGroup.add(jCheckBoxPort, gridBagConstraints);

        jCheckBoxChannel.setText("Watch Channel");
        jCheckBoxChannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxChannelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanelGroup.add(jCheckBoxChannel, gridBagConstraints);

        jCheckBoxBank.setText("Watch Bank (MSB/LSB)");
        jCheckBoxBank.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxBankActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanelGroup.add(jCheckBoxBank, gridBagConstraints);

        jCheckBoxProgram.setText("Watch Program");
        jCheckBoxProgram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxProgramActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanelGroup.add(jCheckBoxProgram, gridBagConstraints);

        jCheckBoxRotateLayer.setText("Rotate Layer (Poly)");
        jCheckBoxRotateLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxRotateLayerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelGroup.add(jCheckBoxRotateLayer, gridBagConstraints);

        jButtonNewGroup.setText("Start New Group");
        jButtonNewGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewGroupActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelGroup.add(jButtonNewGroup, gridBagConstraints);

        jButtonApplyGroup.setText("Save Group");
        jButtonApplyGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonApplyGroupActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 2.0;
        jPanelGroup.add(jButtonApplyGroup, gridBagConstraints);

        jButtonRemoveGroup.setText("Remove");
        jButtonRemoveGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveGroupActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelGroup.add(jButtonRemoveGroup, gridBagConstraints);

        jButtonGroupProgram.setText("Program Picker");
        jButtonGroupProgram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGroupProgramActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 2.0;
        jPanelGroup.add(jButtonGroupProgram, gridBagConstraints);

        jButtonGroupUp.setText("Up");
        jButtonGroupUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGroupUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelGroup.add(jButtonGroupUp, gridBagConstraints);

        jButtonGroupDown.setText("Down");
        jButtonGroupDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGroupDownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelGroup.add(jButtonGroupDown, gridBagConstraints);

        jScrollPane3.setViewportBorder(javax.swing.BorderFactory.createTitledBorder("Group List"));

        jListGroupList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListGroupListValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jListGroupList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 3.0;
        gridBagConstraints.weighty = 1.0;
        jPanelGroup.add(jScrollPane3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        jPanelGroup.add(jSpinnerRotatePoly, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        jPanelGroup.add(jSpinnerWatchProgram, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        jPanelGroup.add(jSpinnerWatchBankMSB, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        jPanelGroup.add(jSpinnerWatchBankLSB, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jPanelGroup, gridBagConstraints);

        jPanelLayer.setBorder(javax.swing.BorderFactory.createTitledBorder("2.Edit Layer"));
        jPanelLayer.setLayout(new java.awt.GridBagLayout());

        jLabel8.setText("Layer Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelLayer.add(jLabel8, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jTextFieldLayerName, gridBagConstraints);

        jButtonNewLayer.setText("Start New Layer");
        jButtonNewLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewLayerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelLayer.add(jButtonNewLayer, gridBagConstraints);

        jButtonApplyLayer.setText("Save Layer");
        jButtonApplyLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonApplyLayerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelLayer.add(jButtonApplyLayer, gridBagConstraints);

        jButtonRemoveLayer.setText("Remove");
        jButtonRemoveLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveLayerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelLayer.add(jButtonRemoveLayer, gridBagConstraints);

        jButtonLayerUp.setText("Up");
        jButtonLayerUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLayerUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelLayer.add(jButtonLayerUp, gridBagConstraints);

        jButtonLayerDown.setText("Down");
        jButtonLayerDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLayerDownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelLayer.add(jButtonLayerDown, gridBagConstraints);

        jScrollPane2.setViewportBorder(javax.swing.BorderFactory.createTitledBorder("Layer List"));

        jListLayerList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListLayerListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jListLayerList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelLayer.add(jScrollPane2, gridBagConstraints);

        jLabel2.setText("Port");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jLabel2, gridBagConstraints);

        jLabel3.setText("Channel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jLabel3, gridBagConstraints);

        jLabel4.setText("Bank");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jLabel4, gridBagConstraints);

        jLabel5.setText("Program");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jLabel5, gridBagConstraints);

        jLabel7.setText("Pan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jLabel7, gridBagConstraints);

        jLabel9.setText("Expression");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jLabel9, gridBagConstraints);

        jLabel10.setText("Note Range");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jLabel10, gridBagConstraints);

        jLabel11.setText("Velocity Range");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jLabel11, gridBagConstraints);

        jLabel12.setText("Transpose");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jLabel12, gridBagConstraints);

        jLabel13.setText("Velocity");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jLabel13, gridBagConstraints);

        jComboBoxModPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxModPortActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        jPanelLayer.add(jComboBoxModPort, gridBagConstraints);

        jButtonSendPort.setText("-");
        jButtonSendPort.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonSendPortMousePressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelLayer.add(jButtonSendPort, gridBagConstraints);

        jComboBoxModChannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxModChannelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        jPanelLayer.add(jComboBoxModChannel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jComboBoxSendChannel, gridBagConstraints);

        jComboBoxModProgram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxModProgramActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        jPanelLayer.add(jComboBoxModProgram, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        jPanelLayer.add(jSpinnerSendProgram, gridBagConstraints);

        jComboBoxModBank.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxModBankActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelLayer.add(jComboBoxModBank, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelLayer.add(jSpinnerSendBankMSB, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelLayer.add(jSpinnerSendBankLSB, gridBagConstraints);

        jSpinnerSendFixedPan.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerSendFixedPanStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        jPanelLayer.add(jSpinnerSendFixedPan, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jSpinnerSendAdjustExpression, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        jPanelLayer.add(jComboBoxSendNoteLow, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        jPanelLayer.add(jComboBoxSendNoteHigh, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        jPanelLayer.add(jSpinnerSendAdjustTranspose, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        jPanelLayer.add(jSpinnerSendAdjustVelocity, gridBagConstraints);

        jLabel14.setText("In Group");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelLayer.add(jLabel14, gridBagConstraints);

        jLabelTargetGroupName.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelLayer.add(jLabelTargetGroupName, gridBagConstraints);

        jButtonLayerProgram.setText("Program Picker");
        jButtonLayerProgram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLayerProgramActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelLayer.add(jButtonLayerProgram, gridBagConstraints);

        jLabel15.setText("%   0%-100%");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 9;
        jPanelLayer.add(jLabel15, gridBagConstraints);

        jLabel16.setText("-128....+128");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 12;
        jPanelLayer.add(jLabel16, gridBagConstraints);

        jLabel17.setText("-128....+128");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 13;
        jPanelLayer.add(jLabel17, gridBagConstraints);

        jCheckBoxFixPan.setText("Fix Pan");
        jCheckBoxFixPan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxFixPanActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jCheckBoxFixPan, gridBagConstraints);

        jLabel6.setText(" to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jLabel6, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jComboBoxSendVelocityLow, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jComboBoxSendVelocityHi, gridBagConstraints);

        jLabel18.setText(" to ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelLayer.add(jLabel18, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jPanelLayer, gridBagConstraints);

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel0.setLayout(new java.awt.GridBagLayout());

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Input Info"));
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane5.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane5.setViewportView(jTable1);

        jPanel4.add(jScrollPane5);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.weighty = 1.0;
        jPanel0.add(jPanel4, gridBagConstraints);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Output Info"));
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane6.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane6.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane6.setViewportView(jTable2);

        jPanel3.add(jScrollPane6);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.weighty = 1.0;
        jPanel0.add(jPanel3, gridBagConstraints);

        jSplitPane1.setRightComponent(jPanel0);

        jPanel2.add(jSplitPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);

        jButtonImport.setText("Import");
        jButtonImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonImportActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButtonImport, gridBagConstraints);

        jButtonExport.setText("Export");
        jButtonExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(jButtonExport, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    public void disableUnusedOnPanel() {
        MX40Group group = new MX40Group(_process);
        readGroupFromPanel(group);

        //jComboBoxWatchPort.setEnabled(group._isWatchPort);
        jButtonWatchPort.setEnabled(group._isWatchPort);
        jComboBoxWatchChannel.setEnabled(group._isWatchChannel);
        jSpinnerWatchProgram.setEnabled(group._isWatchProgram);
        jSpinnerWatchBankMSB.setEnabled(group._isWatchBank);
        jSpinnerWatchBankLSB.setEnabled(group._isWatchBank);
        jButtonGroupProgram.setEnabled(group._isWatchBank || group._isWatchProgram);
        jSpinnerRotatePoly.setEnabled(group._isRotate);

        boolean flag = (_editingGroup != null);
        if (flag) {
            MX40Layer layer = new MX40Layer(_process, group);
            readLayerFromPanel(layer);
            jButtonSendPort.setEnabled(layer._modPort == MX40Layer.MOD_FIXED);
            jComboBoxSendChannel.setEnabled(layer._modChannel == MX40Layer.MOD_FIXED);
            jSpinnerSendBankMSB.setEnabled(layer._modBank == MX40Layer.MOD_FIXED);
            jSpinnerSendBankLSB.setEnabled(layer._modBank == MX40Layer.MOD_FIXED);
            jSpinnerSendProgram.setEnabled(layer._modProgram == MX40Layer.MOD_FIXED);
            jButtonLayerProgram.setEnabled(layer._modBank == MX40Layer.MOD_FIXED|| layer._modProgram == MX40Layer.MOD_FIXED);
            jLabelTargetGroupName.setText(_editingGroup._title);
            jSpinnerSendFixedPan.setEnabled(layer._modPan == MX40Layer.MOD_FIXED ? true : false);
        }else {
            jButtonSendPort.setEnabled(false);
            jComboBoxSendChannel.setEnabled(false);
            jSpinnerSendBankMSB.setEnabled(false);
            jSpinnerSendBankLSB.setEnabled(false);
            jSpinnerSendProgram.setEnabled(false);
            jButtonLayerProgram.setEnabled(false);
            jLabelTargetGroupName.setText("*** none **");
            jSpinnerSendFixedPan.setEnabled(false);
        }
        jTextFieldLayerName.setEnabled(flag);

        jButtonNewLayer.setEnabled(flag);
        jButtonApplyLayer.setEnabled(flag);
        jButtonApplyGroup.setEnabled(flag);

        jComboBoxModProgram.setEnabled(flag);
        jComboBoxModBank.setEnabled(flag);
        jComboBoxModPort.setEnabled(flag);
        jComboBoxModChannel.setEnabled(flag);
        jCheckBoxFixPan.setEnabled(flag);
        
        jSpinnerSendAdjustExpression.setEnabled(flag);
        jSpinnerSendAdjustTranspose.setEnabled(flag);
        jSpinnerSendAdjustVelocity.setEnabled(flag);

        jComboBoxSendNoteLow.setEnabled(flag);
        jComboBoxSendNoteHigh.setEnabled(flag);
    }

    private void jButtonNewGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewGroupActionPerformed
        if (underConstruction) {
            return;
        }
        startNewGroupAndLayer();
        
        int lastGroupEntry = _process._groupList.size() - 1;
        startEditingPack(lastGroupEntry, 0);
        disableUnusedOnPanel();
    }//GEN-LAST:event_jButtonNewGroupActionPerformed

    private void jCheckBoxUseLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseLayerActionPerformed
        if (underConstruction) {
            return;
        }
        _process.setUsingThisRecipe(jCheckBoxUseLayer.isSelected());
        
        recursibleEnable(this, jCheckBoxUseLayer.isSelected());
        jCheckBoxUseLayer.setEnabled(true); // This controller must be enabled always
    }//GEN-LAST:event_jCheckBoxUseLayerActionPerformed

    private void jButtonApplyGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonApplyGroupActionPerformed
        if (underConstruction) {
            return;
        }
        if (_editingGroup != null) {
            MX40Group group2 = new MX40Group(_process);
            readGroupFromPanel(group2);
            
            if (group2._title.equals(_editingGroup._title) == false) {
                if (_groupsModel.indexOfName(group2._title) >= 0) {
                    JOptionPane.showMessageDialog(this, "name [" + group2._title + "] already used", "can't save", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            readGroupFromPanel(_editingGroup);
            listUpGroups();
        }
    }//GEN-LAST:event_jButtonApplyGroupActionPerformed

    private void jListGroupListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListGroupListValueChanged
        if (underConstruction) {
            return;
        }
        int index = jListGroupList.getSelectedIndex();
        if (index < 0) {
            return;
        }
        startEditingPack(index, 0);
    }//GEN-LAST:event_jListGroupListValueChanged

    private void jButtonNewLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewLayerActionPerformed
        if (underConstruction) {
            return;
        }
        if (_editingGroup == null) {
            JOptionPane.showMessageDialog(this, "グループが選択されていなのでレイヤーを操作できない", "条件を満たしていません", JOptionPane.ERROR_MESSAGE);
            return;
        }
        MX40Layer layer = new MX40Layer(_process, _editingGroup);
        readLayerFromPanel(layer);
        startNewLayer(_editingGroup, layer);
        int selGroup = _process._groupList.indexOf(_editingGroup);
        int lastLayerEntry = _editingGroup._listLayer.size() - 1;
        startEditingPack(selGroup, lastLayerEntry);
    }//GEN-LAST:event_jButtonNewLayerActionPerformed

    private void jButtonApplyLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonApplyLayerActionPerformed
        if (underConstruction) {
            return;
        }
        if (_editingLayer != null) {
            MX40Layer layer2 = new MX40Layer(_process, _editingGroup);
            readLayerFromPanel(layer2);

            if (layer2._title.equals(_editingLayer._title) == false) {
                if (_layersModel.indexOfName(layer2._title) >= 0) {
                    JOptionPane.showMessageDialog(this, "name [" + layer2._title + "] already used", "can't save", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            readLayerFromPanel(_editingLayer);
            listupLayers();
            _process.resendProgramChange();
        }
    }//GEN-LAST:event_jButtonApplyLayerActionPerformed

    private void jListLayerListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListLayerListValueChanged
        if (underConstruction) {
            return;
        }
        int index = jListLayerList.getSelectedIndex();
        if (index < 0) {
            return;
        }
        int groupIndex = 0;
        try {
            groupIndex = _process._groupList.indexOf(_editingGroup);
        }catch(Exception e) {
        }
        startEditingPack(groupIndex, index);
    }//GEN-LAST:event_jListLayerListValueChanged

    private void jButtonGroupProgramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGroupProgramActionPerformed
        if (underConstruction) {
            return;
        }
        MX40Group group = new MX40Group(null);
        readGroupFromPanel(group);
        ProgramPicker picker = new ProgramPicker();
        picker.setDefault(group._watchingProgram, group._watchingBankMSB, group._watchingBankLSB);
        MXUtil.showAsDialog(this, picker, "Select Program");
        if (picker._returnProgram >= 0) {
            jSpinnerWatchProgram.setValue(picker._returnProgram);
            jSpinnerWatchBankMSB.setValue(picker._returnBankMSB);
            jSpinnerWatchBankLSB.setValue(picker._returnBankLSB);
            JOptionPane.showMessageDialog(this, "Push [Save Group] Please");
        }
    }//GEN-LAST:event_jButtonGroupProgramActionPerformed

    private void jButtonLayerProgramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLayerProgramActionPerformed
        if (underConstruction) {
            return;
        }
        MX40Layer layer = new MX40Layer(null, null);
        readLayerFromPanel(layer);
        ProgramPicker picker = new ProgramPicker();
        picker.setDefault(layer._fixedProgram, layer._fixedBankMSB, layer._fixedBankLSB);
        MXUtil.showAsDialog(this, picker, "Select Program");
        if (picker._returnProgram >= 0) {
            jSpinnerSendProgram.setValue(picker._returnProgram);
            jSpinnerSendBankMSB.setValue(picker._returnBankMSB);
            jSpinnerSendBankLSB.setValue(picker._returnBankLSB);
            JOptionPane.showMessageDialog(this, "Push [Save Layer] Please");
        }
    }//GEN-LAST:event_jButtonLayerProgramActionPerformed

    private void jCheckBoxPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxPortActionPerformed
        if (underConstruction) {
            return;
        }
        disableUnusedOnPanel();
    }//GEN-LAST:event_jCheckBoxPortActionPerformed

    private void jCheckBoxChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxChannelActionPerformed
        if (underConstruction) {
            return;
        }
        disableUnusedOnPanel();
    }//GEN-LAST:event_jCheckBoxChannelActionPerformed

    private void jCheckBoxBankActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxBankActionPerformed
        if (underConstruction) {
            return;
        }
        disableUnusedOnPanel();
    }//GEN-LAST:event_jCheckBoxBankActionPerformed

    private void jCheckBoxProgramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxProgramActionPerformed
        if (underConstruction) {
            return;
        }
        disableUnusedOnPanel();
    }//GEN-LAST:event_jCheckBoxProgramActionPerformed

    private void jComboBoxModPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxModPortActionPerformed
        if (underConstruction) {
            return;
        }
        disableUnusedOnPanel();
    }//GEN-LAST:event_jComboBoxModPortActionPerformed

    private void jComboBoxModChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxModChannelActionPerformed
        if (underConstruction) {
            return;
        }
        disableUnusedOnPanel();
    }//GEN-LAST:event_jComboBoxModChannelActionPerformed

    private void jComboBoxModBankActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxModBankActionPerformed
        if (underConstruction) {
            return;
        }
        disableUnusedOnPanel();
    }//GEN-LAST:event_jComboBoxModBankActionPerformed

    private void jComboBoxModProgramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxModProgramActionPerformed
        if (underConstruction) {
            return;
        }
        disableUnusedOnPanel();
    }//GEN-LAST:event_jComboBoxModProgramActionPerformed

    private void jButtonRemoveGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveGroupActionPerformed
        if (underConstruction) {
            return;
        }
        int x = jListGroupList.getSelectedIndex();
        if (x >= 0) {
            _process._groupList.remove(x);
            try {
                _editingGroup = null;
                listUpGroups();
            }catch(Exception e) {
                e.printStackTrace();;
            }
            try {
                _editingLayer = null;
                listupLayers();
            }catch(Exception e) {
                e.printStackTrace();;
            }
            _process.resendProgramChange();
            disableUnusedOnPanel();
        }  

    }//GEN-LAST:event_jButtonRemoveGroupActionPerformed

    private void jButtonGroupUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGroupUpActionPerformed
        if (underConstruction) {
            return;
        }
        int x = jListGroupList.getSelectedIndex();
        if (x >= 0) {
            if (x >= 1) {
                Object o =_process._groupList.remove(x);
                _process._groupList.add(x -1, (MX40Group)o);
                listUpGroups();
                _process.resendProgramChange();
            }
        }  
    }//GEN-LAST:event_jButtonGroupUpActionPerformed

    private void jButtonGroupDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGroupDownActionPerformed
        if (underConstruction) {
            return;
        }
        int x = jListGroupList.getSelectedIndex();
        if (x >= 0) {
            if (x < _process._groupList.size() - 1) {
                Object o =_process._groupList.remove(x);
                _process._groupList.add(x+1, (MX40Group)o);
                listUpGroups();
                _process.resendProgramChange();
            }
        }  
    }//GEN-LAST:event_jButtonGroupDownActionPerformed

    private void jButtonRemoveLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveLayerActionPerformed
        if (underConstruction) {
            return;
        }
        MX40Group parent = _editingGroup;
        if (parent == null) {
            return;
        }
        int x = jListLayerList.getSelectedIndex();
        if (x >= 0) {
            parent._listLayer.remove(x);
            listupLayers();
            _process.resendProgramChange();
            disableUnusedOnPanel();
        }  
    }//GEN-LAST:event_jButtonRemoveLayerActionPerformed

    private void jButtonLayerUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLayerUpActionPerformed
        if (underConstruction) {
            return;
        }
        MX40Group parent = _editingGroup;
        if (parent == null) {
            return;
        }
        int x = jListLayerList.getSelectedIndex();
        if (x >= 0) {
            if (x >= 1) {
                Object o = parent._listLayer.remove(x);
                parent._listLayer.add(x -1, (MX40Layer)o);
                listupLayers();
                _process.resendProgramChange();
            }
        }  
    
    }//GEN-LAST:event_jButtonLayerUpActionPerformed

    private void jButtonLayerDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLayerDownActionPerformed
        if (underConstruction) {
            return;
        }
        MX40Group parent = _editingGroup;
        if (parent == null) {
            return;
        }
        int x = jListLayerList.getSelectedIndex();
        if (x >= 0) {
            if (x <= _process._groupList.size() - 1) {
                Object o = parent._listLayer.remove(x);
                parent._listLayer.add(x+1, (MX40Layer)o);
                listupLayers();
                _process.resendProgramChange();
            }
        }  
    }//GEN-LAST:event_jButtonLayerDownActionPerformed

    private void jCheckBoxFixPanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxFixPanActionPerformed
        if (underConstruction) {
            return;
        }
        disableUnusedOnPanel();
    }//GEN-LAST:event_jCheckBoxFixPanActionPerformed

    private void jButtonImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImportActionPerformed
        if (underConstruction) {
            return;
        }
        doImportLayer();
    }//GEN-LAST:event_jButtonImportActionPerformed

    private void jButtonExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportActionPerformed
        if (underConstruction) {
            return;
        }
        doExportLayer();
    }//GEN-LAST:event_jButtonExportActionPerformed

    private void jSpinnerSendFixedPanStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerSendFixedPanStateChanged
    }//GEN-LAST:event_jSpinnerSendFixedPanStateChanged

    private void jCheckBoxRotateLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxRotateLayerActionPerformed
        if (underConstruction) {
            return;
        }
        disableUnusedOnPanel();
    }//GEN-LAST:event_jCheckBoxRotateLayerActionPerformed

    private void jButtonWatchPortMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonWatchPortMousePressed
        JPopupMenu menu = new JPopupMenu();
        if (jButtonWatchPort.isEnabled() == false) {
            return;
        }
        
        for (int i = 0; i < MXAppConfig.TOTAL_PORT_COUNT; ++ i) {
            String name = MXMidi.nameOfPortOutput(i);
            JMenuItem item = new JMenuItem(name);  
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    jButtonWatchPort.setText(e.getActionCommand());
                }
            });
            menu.add(item);
        }

        menu.show(jButtonWatchPort, 0, jButtonWatchPort.getHeight());
    }//GEN-LAST:event_jButtonWatchPortMousePressed

    private void jButtonWatchPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWatchPortActionPerformed

    }//GEN-LAST:event_jButtonWatchPortActionPerformed

    private void jButtonSendPortMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonSendPortMousePressed
        JPopupMenu menu = new JPopupMenu();
        if (jButtonSendPort.isEnabled() == false) {
            return;
        }
        
        for (int i = 0; i < MXAppConfig.TOTAL_PORT_COUNT; ++ i) {
            String name = MXMidi.nameOfPortOutput(i);
            JMenuItem item = new JMenuItem(name);  
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    jButtonSendPort.setText(e.getActionCommand());
                }
            });
            menu.add(item);
        }

        menu.show(jButtonSendPort, 0, jButtonSendPort.getHeight());
    }//GEN-LAST:event_jButtonSendPortMousePressed

    public void doImportLayer() {
        MXSwingFileChooser chooser = new MXSwingFileChooser();
        chooser.addExtension(".xml", "XML File");
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            _process.importSetting(file);
            _process.resendProgramChange();
            justRefreshViewListAndPanel();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(MX40View.this, "Succeed Import [" + file + "]");
                }
            });
        }
    }
    
    public void doExportLayer() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            _process.exportSetting(file);
            JOptionPane.showMessageDialog(this, "Succeed Export [" + file + "]");
        }  
    }

    
    public void recursibleEnable(Component c, boolean enable) { 
        if (c instanceof Container) {
            Container parent = (Container)c;
            int count = parent.getComponentCount();
            for (int x = 0; x < count; ++ x) {
                recursibleEnable(parent.getComponent(x), enable);
            }
        }
        if (c instanceof JComponent) {
            ((JComponent)c).setEnabled(enable);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApplyGroup;
    private javax.swing.JButton jButtonApplyLayer;
    private javax.swing.JButton jButtonExport;
    private javax.swing.JButton jButtonGroupDown;
    private javax.swing.JButton jButtonGroupProgram;
    private javax.swing.JButton jButtonGroupUp;
    private javax.swing.JButton jButtonImport;
    private javax.swing.JButton jButtonLayerDown;
    private javax.swing.JButton jButtonLayerProgram;
    private javax.swing.JButton jButtonLayerUp;
    private javax.swing.JButton jButtonNewGroup;
    private javax.swing.JButton jButtonNewLayer;
    private javax.swing.JButton jButtonRemoveGroup;
    private javax.swing.JButton jButtonRemoveLayer;
    private javax.swing.JButton jButtonSendPort;
    private javax.swing.JButton jButtonWatchPort;
    private javax.swing.JCheckBox jCheckBoxBank;
    private javax.swing.JCheckBox jCheckBoxChannel;
    private javax.swing.JCheckBox jCheckBoxFixPan;
    private javax.swing.JCheckBox jCheckBoxPort;
    private javax.swing.JCheckBox jCheckBoxProgram;
    private javax.swing.JCheckBox jCheckBoxRotateLayer;
    private javax.swing.JCheckBox jCheckBoxUseLayer;
    private javax.swing.JComboBox<String> jComboBoxModBank;
    private javax.swing.JComboBox<String> jComboBoxModChannel;
    private javax.swing.JComboBox<String> jComboBoxModPort;
    private javax.swing.JComboBox<String> jComboBoxModProgram;
    private javax.swing.JComboBox<String> jComboBoxSendChannel;
    private javax.swing.JComboBox<String> jComboBoxSendNoteHigh;
    private javax.swing.JComboBox<String> jComboBoxSendNoteLow;
    private javax.swing.JComboBox<String> jComboBoxSendVelocityHi;
    private javax.swing.JComboBox<String> jComboBoxSendVelocityLow;
    private javax.swing.JComboBox<String> jComboBoxWatchChannel;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelTargetGroupName;
    private javax.swing.JList<String> jListGroupList;
    private javax.swing.JList<String> jListLayerList;
    private javax.swing.JPanel jPanel0;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelGroup;
    private javax.swing.JPanel jPanelLayer;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSpinner jSpinnerRotatePoly;
    private javax.swing.JSpinner jSpinnerSendAdjustExpression;
    private javax.swing.JSpinner jSpinnerSendAdjustTranspose;
    private javax.swing.JSpinner jSpinnerSendAdjustVelocity;
    private javax.swing.JSpinner jSpinnerSendBankLSB;
    private javax.swing.JSpinner jSpinnerSendBankMSB;
    private javax.swing.JSpinner jSpinnerSendFixedPan;
    private javax.swing.JSpinner jSpinnerSendProgram;
    private javax.swing.JSpinner jSpinnerWatchBankLSB;
    private javax.swing.JSpinner jSpinnerWatchBankMSB;
    private javax.swing.JSpinner jSpinnerWatchProgram;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTextField jTextFieldGroupName;
    private javax.swing.JTextField jTextFieldLayerName;
    // End of variables declaration//GEN-END:variables

    public void readGroupFromPanel(MX40Group group) {
        group._title = jTextFieldGroupName.getText();

        group._isWatchPort = jCheckBoxPort.isSelected();
        group._isWatchChannel = jCheckBoxChannel.isSelected();
        group._isWatchBank = jCheckBoxBank.isSelected();
        group._isWatchProgram = jCheckBoxProgram.isSelected();
        group._isRotate = jCheckBoxRotateLayer.isSelected();
        group._rotatePoly = (int)jSpinnerRotatePoly.getValue();
        
        String text=  jButtonWatchPort.getText();
        int watchPort = text.length() >= 1 ? text.charAt(0) - 'A' : -1;
        group._watchingPort = watchPort;
        group._watchingChannel = (int)((MXWrap)jComboBoxWatchChannel.getSelectedItem()).value;
        group._watchingProgram = (int)jSpinnerWatchProgram.getValue();
        group._watchingBankMSB = (int)jSpinnerWatchBankMSB.getValue();
        group._watchingBankLSB = (int)jSpinnerWatchBankLSB.getValue();
    }

    public void writeGroupToPanel(MX40Group group) {
        _editingGroup = group;
        if (group == null) {
            jPanelGroup.setBorder(javax.swing.BorderFactory.createTitledBorder("New Group"));
            group = new MX40Group(_process);
        }else {
            jListGroupList.setSelectedIndex(_groupsModel.indexOfValue(group));
            jPanelGroup.setBorder(javax.swing.BorderFactory.createTitledBorder("Editing Group [" + group._title + "]"));
        }
        jTextFieldGroupName.setText(group._title);
        jCheckBoxPort.setSelected(group._isWatchPort);
        jCheckBoxChannel.setSelected(group._isWatchChannel);
        jCheckBoxBank.setSelected(group._isWatchBank);
        jCheckBoxProgram.setSelected(group._isWatchProgram);
        jCheckBoxRotateLayer.setSelected(group._isRotate);
        jSpinnerRotatePoly.setValue(group._rotatePoly);
        jButtonWatchPort.setText(MXMidi.nameOfPortShort(group._watchingPort));
        //jComboBoxWatchPort.setSelectedIndex(_watchPort.indexOfValue(group._watchingPort));
        jComboBoxWatchChannel.setSelectedIndex(_watchChannel.indexOfValue(group._watchingChannel));
        jSpinnerWatchProgram.setValue(group._watchingProgram);
        jSpinnerWatchBankMSB.setValue(group._watchingBankMSB);
        jSpinnerWatchBankLSB.setValue(group._watchingBankLSB);
    }

    public void readLayerFromPanel(MX40Layer layer) {
        layer._title = jTextFieldLayerName.getText();

        layer._modPort = _modPort.readCombobox(jComboBoxModPort);
        layer._modChannel = _modChannel.readCombobox(jComboBoxModChannel);
        layer._modBank = _modBank.readCombobox(jComboBoxModBank);
        layer._modProgram = _modProgram.readCombobox(jComboBoxModProgram);
        layer._modPan = jCheckBoxFixPan.isSelected() ? MX40Layer.MOD_FIXED : MX40Layer.MOD_ASFROM;
        
        String text= jButtonSendPort.getText();
        int sendPort = text.length() >= 1 ? text.charAt(0) - 'A' : -1;
        layer._fixedPort = sendPort;
        //layer._fixedPort = _changePort.readCombobox(jComboBoxSendPort);
        layer._fixedChannel = _changeChannel.readCombobox(jComboBoxSendChannel);
        layer._fixedBankMSB = (int)jSpinnerSendBankMSB.getValue();
        layer._fixedBankLSB = (int)jSpinnerSendBankLSB.getValue();
        layer._fixedProgram = (int)jSpinnerSendProgram.getValue();
        layer._fixedPan = (Integer)jSpinnerSendFixedPan.getValue();
        layer._adjustTranspose = (Integer)jSpinnerSendAdjustTranspose.getValue();
        layer._adjustVelocity = (Integer)jSpinnerSendAdjustVelocity.getValue();
        layer._adjustExpression = (Integer)jSpinnerSendAdjustExpression.getValue();

        layer.setAcceptKeyLowest((int) _changeLowNote.readCombobox(jComboBoxSendNoteLow));
        layer.setAcceptKeyHighest((int) _changeHighNote.readCombobox(jComboBoxSendNoteHigh));

        layer.setAcceptVelocityLowest((int) _changeLowVelocity.readCombobox(jComboBoxSendVelocityLow));
        layer.setAcceptVelocityHighest((int) _changeHighVelocity.readCombobox(jComboBoxSendVelocityHi));
    }
    
    public void writeLayerToPanel(MX40Layer layer) {
        _editingLayer = layer;

        if (layer == null) {
            if (_editingGroup != null) {
                layer = new MX40Layer(_process, _editingGroup); // for reset panel
                jPanelLayer.setBorder(javax.swing.BorderFactory.createTitledBorder(layer._title));
            }else {
                layer = new MX40Layer(_process, _editingGroup); //for reset panel
                jPanelLayer.setBorder(BorderFactory.createTitledBorder("Select Group 1st"));
            }
        }else {
            jListLayerList.setSelectedIndex(_layersModel.indexOfValue(_editingLayer));
            jPanelLayer.setBorder(javax.swing.BorderFactory.createTitledBorder("Editing Layer [" + layer._title + "]"));
        }

        jTextFieldLayerName.setText(layer._title);

        _modPort.writeComboBox(jComboBoxModPort, layer._modPort);
        _modChannel.writeComboBox(jComboBoxModChannel, layer._modChannel);
        _modBank.writeComboBox(jComboBoxModBank, layer._modBank);
        _modProgram.writeComboBox(jComboBoxModProgram, layer._modProgram);

        jButtonSendPort.setText(MXMidi.nameOfPortShort(layer._fixedPort));
        //_changePort.writeComboBox(jComboBoxSendPort, layer._fixedPort);
        _changeChannel.writeComboBox(jComboBoxSendChannel, layer._fixedChannel);
        jSpinnerSendProgram.setValue(layer._fixedProgram);
        jSpinnerSendBankMSB.setValue(layer._fixedBankMSB);
        jSpinnerSendBankLSB.setValue(layer._fixedBankLSB);

        jCheckBoxFixPan.setSelected(layer._modPan == MX40Layer.MOD_FIXED ? true : false);

        jSpinnerSendFixedPan.setModel(new SafeSpinnerNumberModel(layer._fixedPan, 0, 127, 1));
        jSpinnerSendAdjustExpression.setModel(new SafeSpinnerNumberModel(layer._adjustExpression, 0, 100, 1));
        jSpinnerSendAdjustTranspose.setModel(new SafeSpinnerNumberModel(layer._adjustTranspose, -128, 128, 1));
        jSpinnerSendAdjustVelocity.setModel(new SafeSpinnerNumberModel(layer._adjustVelocity, -128, 128, 1));

        _changeLowNote.writeComboBox(jComboBoxSendNoteLow, layer.getAcceptKeyLowest());
        _changeHighNote.writeComboBox(jComboBoxSendNoteHigh, layer.getAcceptKeyHighest());

        _changeLowVelocity.writeComboBox(jComboBoxSendVelocityLow, layer.getAcceptVelocityLowest());
        _changeHighVelocity.writeComboBox(jComboBoxSendVelocityHi, layer.getAcceptVelocityHighest());
    }
    
    public void startNewGroupAndLayer() {
        MX40Group group = new MX40Group(_process);
        readGroupFromPanel(group);
        
        String newPrefix = group._title;

        if (newPrefix == null || newPrefix.isEmpty() || newPrefix.isBlank()) {
            newPrefix = "Group";
        }
        String newTitle = newPrefix;
        while(true) {
            boolean already = false;
            for (int i = 0; i < _process._groupList.size(); ++ i) {
                String title = _process._groupList.get(i)._title;
                if (title.equals(newTitle)) {
                    already = true;
                }
            }
            if (!already) {
                break;
            }
            newTitle = randomName(newPrefix);
        }

        group._title = newTitle;
        _process._groupList.add(group);
        MX40Layer layer = new MX40Layer(_process, group);
        layer._title = jTextFieldLayerName.getText();
        startNewLayer(group, layer);
    }
    
    public void startNewLayer(MX40Group group, MX40Layer layer) {
        String newPrefix = layer._title;
        
        if (newPrefix == null || newPrefix.isEmpty() || newPrefix.isBlank()) {
            newPrefix = "Layer";
        }
        String newTitle = newPrefix;
        while(true) {
            boolean already = false;
            for (int i = 0; i < group._listLayer.size(); ++ i) {
                String title = group._listLayer.get(i)._title;
                if (title.equals(newTitle)) {
                    already = true;
                }
            }
            if (!already) {
                break;
            }
            newTitle = randomName(newPrefix);
        }


        layer._title = newTitle;
        group._listLayer.add(layer);
    }
    
    public void startEditingPack(int group, int layer) {
        underConstruction = true;
        try {
            MX40Group newStartGroup = null;
            MX40Layer newStartLayer = null;

            try {
                newStartGroup = _process._groupList.get(group);
            }catch(Exception e) {
            }

            try {
                newStartLayer = newStartGroup._listLayer.get(layer);
            }catch(Exception e) {

            }

            if (newStartGroup == _editingGroup && newStartLayer == _editingLayer) {
                return;
            }

            MX40Group panelGroup = new MX40Group(_process);
            readGroupFromPanel(panelGroup);
            boolean askGroupChanged = false;
            if (_editingGroup == null || panelGroup.equals(_editingGroup)) {
            }else {
                if (_process._groupList.contains(_editingGroup)) {
                    askGroupChanged = true;

                    if (newStartGroup == _editingGroup) {
                        askGroupChanged = false;
                    }
                }
            }

            MX40Layer panelLayer = new MX40Layer(_process, null);
            readLayerFromPanel(panelLayer);
            boolean askProgramChanged = false;
            if (_editingLayer == null || panelLayer.equals(_editingLayer)) {
            }else {
                if (_editingGroup != null && _editingGroup._listLayer.contains(_editingLayer) == false) {
                    
                }else {
                    askProgramChanged = true;

                    if (newStartLayer == _editingLayer) {
                        askProgramChanged = false;
                    }
                }
            }

            if (askGroupChanged) {
                _editingGroup.caneChageTo(panelGroup);
                int ret = JOptionPane.showConfirmDialog(this, "Will discard Changes", "Group Changed", JOptionPane.OK_CANCEL_OPTION);
                if (ret == JOptionPane.CANCEL_OPTION) {
                    jListGroupList.setSelectedIndex(_groupsModel.indexOfValue(_editingGroup));
                    jListLayerList.setSelectedIndex(_layersModel.indexOfValue(_editingLayer));
                    return;
                }
            }

            if (askProgramChanged) {
                //if (_editingLayer._title.equals(panelLayer._title)) {
                //    _editingLayer.canChangeTo(panelGroup);
                //}
                int ret = JOptionPane.showConfirmDialog(this, "Will discard Changes", "Layer Changed", JOptionPane.OK_CANCEL_OPTION);
                if (ret == JOptionPane.CANCEL_OPTION) {
                    jListGroupList.setSelectedIndex(_groupsModel.indexOfValue(_editingGroup));
                    jListLayerList.setSelectedIndex(_layersModel.indexOfValue(_editingLayer));
                    return;
                }
            }

            _editingGroup = newStartGroup;
            _editingLayer = newStartLayer;

            listUpGroups();
            listupLayers();

            writeGroupToPanel(_editingGroup);
            writeLayerToPanel(_editingLayer);
        }finally {
            try {
                disableUnusedOnPanel();
            }catch(Exception e) {
                
            }
            underConstruction = false;
        }
    }

    public void listUpGroups() {
        MXWrapList<MX40Group> listModel = new MXWrapList();
        for (int i = 0; i < _process._groupList.size(); ++ i) {
            MX40Group g = _process._groupList.get(i);
            listModel.addNameAndValue(g._title, g);
            if (_editingGroup == g) {
                listModel.setSelectedItem(g);
            }
        }
        _groupsModel = listModel;
        jListGroupList.setModel(_groupsModel);
        repaint();
    }
    
    public void listupLayers() {
        MX40Group parent = _editingGroup;
        MXWrapList<MX40Layer> listModel = new MXWrapList();
        if (parent != null) {
            for (int i = 0; i < parent._listLayer.size(); ++ i) {
                MX40Layer l = parent._listLayer.get(i);
                listModel.addNameAndValue(l._title, l);
                if (_editingLayer == l) {
                    listModel.setSelectedItem(l);
                }
            }
        }
        _layersModel = listModel;
        jListLayerList.setModel(listModel);
        repaint();
    }
    
    public String randomName(String prefix) {
        // cut suffix numbers
        while (prefix.length() > 0) {
            int ch = prefix.charAt(prefix.length() - 1);
            if (ch >= '0' && ch <= '9') {
                prefix = prefix.substring(0, prefix.length() - 1);
                continue;
            }else {
                break;
            }
        }
        long x = 0;
        do {
            x = (long)(Math.random() * 8999 + 1000);
        }while(x < 1000 || x >= 10000);
        return prefix + Integer.toString((int)x);
    }

    public void justRefreshViewListAndPanel() {
        int indexGroup = -1;
        int indexLayer = -1;
        try {
            indexGroup = _process._groupList.indexOf(_editingGroup);
            indexLayer = _editingGroup._listLayer.indexOf(_editingLayer);
        }catch(Exception e) {
        }
        justRefreshViewListAndPanel(indexGroup, indexLayer);
    }

    public void justRefreshViewListAndPanel(int indexGroup, int indexLayer) {
        //Refresh List
        
        MXWrapList<MX40Group> groupModel = new MXWrapList();
        for (int i = 0; i < _process._groupList.size(); ++ i ){
            MX40Group group = _process._groupList.get(i);
            groupModel.addNameAndValue(group._title, group);
        }

        MXWrapList<MX40Layer> layerModel = new MXWrapList();
        if (_editingGroup != null) {
            for (int i = 0; i < _editingGroup._listLayer.size(); ++ i ){
                MX40Layer layer = _editingGroup._listLayer.get(i);
                layerModel.addNameAndValue(layer._title, layer);
            }
        }

        jListGroupList.setModel(groupModel);
        if (indexGroup >= 0) {            
            jListGroupList.setSelectedIndex(indexGroup);
        }
        jListLayerList.setModel(layerModel);
        if (indexLayer >= 0) {
            jListLayerList.setSelectedIndex(indexLayer);
        }

        //Refresh Panel + Controls
        startEditingPack(indexGroup, indexLayer);
    }

    boolean updateRequest = false;
    
    @Override
    public void tableChanged(TableModelEvent e) {
        updateRequest = true;
        MXGlobalTimer.letsCountdown(500, new Runnable() { 
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (updateRequest) {
                            updateRequest = false;
                        }
                        /*
                        jTable1.setModel(_process._inputInfo);
                        jTable2.setModel(_process._outputInfo);
                        */
                        jTable1.invalidate();
                        jTable2.invalidate();
                    }
                });
            }
        });
    }
}
