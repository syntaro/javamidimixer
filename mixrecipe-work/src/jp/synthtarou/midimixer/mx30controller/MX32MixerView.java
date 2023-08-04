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

import java.awt.Color;
import jp.synthtarou.midimixer.libs.swing.MXFocusGroup;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXStatic;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.midi.MXUtilMidi;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.swing.MXFileOpenChooser;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX32MixerView extends javax.swing.JPanel {
    private static final MXDebugPrint _debug = new MXDebugPrint(MX32MixerView.class);

    MX32MixerProcess _process;
    MXFocusGroup _focusGroup;
    MXWrapList<Integer> chainModel;

    public MX32MixerView(MX32MixerProcess process) {
        int port = process._port;
        _process = process;
        
        initComponents();
        
        // following must here (late bind not work)
        chainModel = new MXWrapList<Integer>();
        chainModel.addNameAndValue(MXUtilMidi.nameOfPortShort(-1), -1);
        for (int p2 = 0; p2 < MXStatic.TOTAL_PORT_COUNT; ++ p2) {
            if (p2 == port) {
                continue;
            }
            chainModel.addNameAndValue(MXUtilMidi.nameOfPortShort(p2), p2);
        }

        jComboBoxChain.setModel(chainModel);

        if (port == 0) {
            MXMain.getMain().addLaunchSequence(new Runnable() {
                public void run() {
                    _process._parent.showTextForFocus(MGStatus.TYPE_SLIDER,  0, 0, 0);
                }
            });
        }
        jLabelKeyTracker.setFocusable(true);
        jSplitPane1.setDividerLocation(getWidth() - 250);
        updateUI();
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
        jPanelControllers = new javax.swing.JPanel();
        jPanelSyncTo = new javax.swing.JPanel();
        jComboBoxChain = new javax.swing.JComboBox<>();
        jCheckBoxSyncTogether = new javax.swing.JCheckBox();
        jPanelEdit = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListABC = new javax.swing.JList<>();
        jLabelKeyTracker = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jSplitPane1.setDividerLocation(500);

        jPanelControllers.setLayout(new javax.swing.BoxLayout(jPanelControllers, javax.swing.BoxLayout.LINE_AXIS));
        jSplitPane1.setLeftComponent(jPanelControllers);

        jPanelSyncTo.setLayout(new java.awt.GridBagLayout());

        jComboBoxChain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxChainActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanelSyncTo.add(jComboBoxChain, gridBagConstraints);

        jCheckBoxSyncTogether.setText("Me Too (Message Chain)");
        jCheckBoxSyncTogether.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSyncTogetherActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelSyncTo.add(jCheckBoxSyncTogether, gridBagConstraints);

        jPanelEdit.setBorder(javax.swing.BorderFactory.createTitledBorder("Target Information"));
        jPanelEdit.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setViewportView(jListABC);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelEdit.add(jScrollPane1, gridBagConstraints);

        jLabelKeyTracker.setForeground(new JLabel().getBackground());
        jLabelKeyTracker.setText("<HTML>PC Key<br>[Cursor]<br>[Space]<br>[Back:]</HTML>");
        jLabelKeyTracker.setFocusCycleRoot(true);
        jLabelKeyTracker.setFocusTraversalPolicyProvider(true);
        jLabelKeyTracker.setVerifyInputWhenFocusTarget(false);
        jLabelKeyTracker.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jLabelKeyTrackerFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jLabelKeyTrackerFocusLost(evt);
            }
        });
        jLabelKeyTracker.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jLabelKeyTrackerKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanelEdit.add(jLabelKeyTracker, gridBagConstraints);
        jLabelKeyTracker.getAccessibleContext().setAccessibleName("Try Cursor<br>&Space+Back<br>");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        jPanelSyncTo.add(jPanelEdit, gridBagConstraints);

        jLabel1.setText("Message Chain To");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanelSyncTo.add(jLabel1, gridBagConstraints);

        jButton3.setText("Customize Controllers");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelSyncTo.add(jButton3, gridBagConstraints);

        jSplitPane1.setRightComponent(jPanelSyncTo);

        add(jSplitPane1);
    }// </editor-fold>//GEN-END:initComponents
        
    private void jComboBoxChainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxChainActionPerformed
        Integer sel = (Integer)((MXWrap)jComboBoxChain.getSelectedItem()).value;
        int x = -1;
        if (sel != null) {
            x = sel;
        }
        _process._patchToMixer = x;
    }//GEN-LAST:event_jComboBoxChainActionPerformed

    private void jCheckBoxSyncTogetherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSyncTogetherActionPerformed
        _process._patchTogether = jCheckBoxSyncTogether.isSelected();
    }//GEN-LAST:event_jCheckBoxSyncTogetherActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        JPopupMenu popup = new JPopupMenu();
        JRadioButtonMenuItem item1 = new JRadioButtonMenuItem("Edit Contoller");
        item1.setSelected(true);
        popup.add(item1);
        item1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doStartEditMixer();
            }
        });

        JMenuItem item2 = new JMenuItem("Initialize With...");
        popup.add(item2);
        item2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doInitializeMixer();
            }
        });

        JMenuItem item3 = new JMenuItem("Import Settings");
        popup.add(item3);
        item3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doImportMixer();
            }
        });

        JMenuItem item4 = new JMenuItem("Export Settings");
        popup.add(item4);
        item4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doExportMixer();
            }
        });

        JMenuItem item5 = new JMenuItem("Resize Mixer");
        popup.add(item5);
        item5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doResizeMixer();
            }
        });
        popup.show(jButton3, 20, jButton3.getHeight());
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jLabelKeyTrackerKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jLabelKeyTrackerKeyPressed
        switch(evt.getKeyCode()) {
            case 38: //UP
            case 40: //DOWN
            case 37: //LEFT
            case 39: //RIGHT
            case '\n':
            case ' ':
            case '\b':
                _process._parent.goNextFocus(_process._port, evt.getKeyCode());
                break;
        }
        
    }//GEN-LAST:event_jLabelKeyTrackerKeyPressed

    private void jLabelKeyTrackerFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelKeyTrackerFocusGained
        if (_focusGroup.getFocus() == null) {
            _process._parent.goNextFocus(_process._port, 0);
        }
        jLabelKeyTracker.setForeground(Color.red);
    }//GEN-LAST:event_jLabelKeyTrackerFocusGained

    private void jLabelKeyTrackerFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelKeyTrackerFocusLost
        jLabelKeyTracker.setForeground(jLabelKeyTracker.getBackground());
    }//GEN-LAST:event_jLabelKeyTrackerFocusLost

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        jLabelKeyTracker.requestFocusInWindow();
    }//GEN-LAST:event_formFocusGained
    
    public void doInitializeMixer() {
        InitializeConfirmPanel panel = new InitializeConfirmPanel(_process);
        MXUtil.showAsDialog(this, panel, "Initialize Mixer");
        updateUI();
        _process._parent.globalContollerHidden();
    }
    
    public void updateUI() {
        super.updateUI();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    MX32MixerData data = _process._data;
                    for (int column = 0; column < MXStatic.SLIDER_COLUMN_COUNT; ++ column) {
                        Color c = MXStatic.sliderColor(column);
                        for (int row = 0; row < MXStatic.CIRCLE_ROW_COUNT; ++ row) {
                            MXUtil.backgroundRecursive(data.getCircle(row, column), c);
                        }
                        for (int row = 0; row < MXStatic.SLIDER_ROW_COUNT; ++ row) {
                            MXUtil.backgroundRecursive(data.getSlider(row, column), c);
                        }
                        for (int row = 0; row < MXStatic.DRUM_ROW_COUNT; ++ row) {
                            MXUtil.backgroundRecursive(data.getDrumPad(row, column), c);
                        }
                    }
                }catch(NullPointerException e) {
                }
            }
        });
    }
    
    public void doImportMixer() {
        MXFileOpenChooser chooser = new MXFileOpenChooser();
        chooser.addExtension(".xml", "XML File");
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            MXSetting setting = new MXSetting(file, false);
            setting.setTarget(_process);
            setting.readSettingFile();   
            updateUI();
            JOptionPane.showMessageDialog(this, "Succeed Import [" + file + "]");
            updateSliderFromStatus();
        }
    }
    
    public void doExportMixer() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            MXSetting setting = new MXSetting(file, false);
            setting.setTarget(_process);
            setting.writeSettingFile();
            JOptionPane.showMessageDialog(this, "Succeed Export [" + file + "]");
        }  
    }
    
    public void doResizeMixer() {
        MX30ResizeMixerSetting config = new MX30ResizeMixerSetting(_process._parent);
        MXUtil.showAsDialog(this, config, "Resize Mixer");
        if (config._okOption) {
            _process._parent.globalContollerHidden();
        }
     }
    
    public void doStartEditMixer() {
        _process._parent.enterEditMode(true);
    }
    
    public JPanel getControllerCase() {
        return jPanelControllers;
    }

    public void initControllers() {
        MX32MixerProcess mixer = this._process;

        try {
            _focusGroup = new MXFocusGroup();

            mixer._data.setEveryComponents(null, null, null);

            JPanel panel001 = getControllerCase();

            java.awt.GridBagConstraints gbc;
            panel001.removeAll();
            panel001.setLayout(new GridBagLayout());

            int grindY = 0;

            ArrayList<MGCircle>[] matrixCircle = new ArrayList[MXStatic.CIRCLE_ROW_COUNT];

            for (int row = 0; row < MXStatic.CIRCLE_ROW_COUNT; ++ row) {
                ArrayList<MGCircle> newCircle = new ArrayList<MGCircle>();
                for (int col = 0; col < MXStatic.SLIDER_COLUMN_COUNT; col ++) {
                    MGCircle cc1 = new MGCircle(_process, row, col);

                    cc1.setSize(50, 50);
                    _focusGroup.attach(cc1);

                    gbc = new java.awt.GridBagConstraints();
                    gbc.gridx = col;
                    gbc.gridy = grindY;
                    gbc.fill = java.awt.GridBagConstraints.BOTH;
                    gbc.anchor = java.awt.GridBagConstraints.CENTER;
                    gbc.weightx = 0;
                    gbc.weighty = 0;

                    panel001.add(cc1, gbc);
                    newCircle.add(cc1);
                }
                matrixCircle[row] = newCircle;
                grindY ++;
            }

            ArrayList<MGSlider>[] matrixSlider = new ArrayList[MXStatic.SLIDER_ROW_COUNT];

            for (int row = 0; row < MXStatic.SLIDER_ROW_COUNT; ++ row) {
                ArrayList<MGSlider> newSlider = new ArrayList<MGSlider>();
                matrixSlider[row] = newSlider;
                
                for (int col = 0; col < MXStatic.SLIDER_COLUMN_COUNT; ++col) {
                    MGSlider sc1 = new MGSlider(_process, row, col);
                    _focusGroup.attach(sc1);

                    sc1.setSize(50, 100);

                    gbc = new java.awt.GridBagConstraints();
                    gbc.gridx = col;
                    gbc.gridy = grindY;
                    gbc.fill = java.awt.GridBagConstraints.BOTH;
                    gbc.anchor = java.awt.GridBagConstraints.CENTER;
                    gbc.weightx = 1.0;
                    gbc.weighty = 1.0;

                    panel001.add(sc1, gbc);
                    newSlider.add(sc1);
                }
            }
            grindY++;

            ArrayList<MGPad>[] matrixPad = new ArrayList[MXStatic.DRUM_ROW_COUNT];

            for (int row = 0; row < MXStatic.DRUM_ROW_COUNT; ++ row) {
                ArrayList<MGPad> newPad = new ArrayList<MGPad>();
                for (int col = 0; col < MXStatic.SLIDER_COLUMN_COUNT; ++col) {
                    MGStatus number = mixer._data.getDrumPadStatus(row, col);
                    MGPad rc1 = new MGPad(_process, row, col);
                    //rc1.initUIWithStatus(number);

                   _focusGroup.attach(rc1);

                    newPad.add(rc1);
                    rc1.setSize(50, 50);

                    gbc = new java.awt.GridBagConstraints();
                    gbc.gridx = col;
                    gbc.gridy = grindY;
                    gbc.fill = java.awt.GridBagConstraints.BOTH;
                    gbc.anchor = java.awt.GridBagConstraints.CENTER;
                    gbc.weightx = 1.0;
                    gbc.weighty = 0;

                    panel001.add(rc1, gbc);
                }
                matrixPad[row] = newPad;
                grindY++;
            }
            _focusGroup.setFocusEnabled(true);

            int x = chainModel.indexOfValue(mixer._patchToMixer);
            jComboBoxChain.setSelectedIndex(x);

            mixer._data.setEveryComponents(matrixSlider, matrixCircle, matrixPad);

            jCheckBoxSyncTogether.setSelected(mixer._patchTogether);
        }finally {
            //data._underInit = false;
            updateUI();
        }
    }

    public void updateSliderFromStatus() {
        initControllers();
    }
    
    public void setFocusString(String str) {
        ArrayList<String> list = new ArrayList();
        MXUtil.split(str, list, '\n');
        DefaultListModel model = new DefaultListModel();
        for (String line : list) {
            model.addElement(line);
        }
        jListABC.setModel(model);
        jLabelKeyTracker.requestFocus();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBoxSyncTogether;
    private javax.swing.JComboBox<String> jComboBoxChain;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelKeyTracker;
    private javax.swing.JList<String> jListABC;
    private javax.swing.JPanel jPanelControllers;
    private javax.swing.JPanel jPanelEdit;
    private javax.swing.JPanel jPanelSyncTo;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    // End of variables declaration//GEN-END:variables

    public void globalControllerHidden() {       
        MX32MixerData data = _process._data;
        int lines = _process._parent.getActiveLines();
        for (int column = 0; column < MXStatic.SLIDER_COLUMN_COUNT; ++ column) {
            for (int row = 0; row < MXStatic.CIRCLE_ROW_COUNT; ++ row) {
                if (_process._parent.isKnobActive(row) && column < lines) {
                    data.getCircle(row, column).setVisible(true);
                }else {
                    data.getCircle(row, column).setVisible(false);
                }                       
            }
            for (int row = 0; row < MXStatic.SLIDER_ROW_COUNT; ++ row) {
                if (column < lines) {
                    data.getSlider(row, column).setVisible(true);
                }else {
                    data.getSlider(row, column).setVisible(false);
                }                       
            }
            for (int row = 0; row < MXStatic.DRUM_ROW_COUNT; ++ row) {
                if (_process._parent.isPadActive(row) && column < lines) {
                    data.getDrumPad(row, column).setVisible(true);
                }else {
                    data.getDrumPad(row, column).setVisible(false);
                }                       
            }
        }
    }
}
