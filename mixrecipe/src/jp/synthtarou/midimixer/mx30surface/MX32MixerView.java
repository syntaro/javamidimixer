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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import jp.synthtarou.midimixer.libs.swing.focus.MXFocusGroup;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.wraplist.MXWrap;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.swing.MXFileChooser;
import jp.synthtarou.midimixer.libs.swing.focus.MXFocusHandler;
import jp.synthtarou.midimixer.libs.swing.focus.MXFocusTargetInfo;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX32MixerView extends javax.swing.JPanel implements MXFocusHandler {

    MX32MixerProcess _mixer;
    MXFocusGroup _focusGroup;
    MXWrapList<Integer> chainModel;

    public MX32MixerView(MX32MixerProcess process) {
        int port = process._port;
        _mixer = process;

        initComponents();

        // following must here (late bind not work)
        chainModel = new MXWrapList<Integer>();
        chainModel.addNameAndValue(MXMidi.nameOfPortShort(-1), -1);
        for (int p2 = 0; p2 < MXAppConfig.TOTAL_PORT_COUNT; ++p2) {
            if (p2 == port) {
                continue;
            }
            chainModel.addNameAndValue(MXMidi.nameOfPortShort(p2), p2);
        }

        jComboBoxChain.setModel(chainModel);

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
        Integer sel = (Integer) ((MXWrap) jComboBoxChain.getSelectedItem())._value;
        int x = -1;
        if (sel != null) {
            x = sel;
        }
        _mixer._patchToMixer = x;
    }//GEN-LAST:event_jComboBoxChainActionPerformed

    private void jCheckBoxSyncTogetherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSyncTogetherActionPerformed
        _mixer._patchTogether = jCheckBoxSyncTogether.isSelected();
    }//GEN-LAST:event_jCheckBoxSyncTogetherActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item1 = new JMenuItem("Edit Contoller");
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
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
            case '\n':
            case ' ':
            case '\b':
                goNextFocus(evt.getKeyCode());
                break;
        }

    }//GEN-LAST:event_jLabelKeyTrackerKeyPressed

    private void jLabelKeyTrackerFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelKeyTrackerFocusGained
        if (_focusGroup.getFocus() == null) {
            showTextForFocus(_mixer.getSlider(0, 0));
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
        InitializeConfirmPanel panel = new InitializeConfirmPanel(_mixer);
        MXUtil.showAsDialog(this, panel, "Initialize Mixer");
        updateUI();
        _mixer._parent.globalContollerHidden();
    }

    public void updateUI() {
        super.updateUI();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
                        for (int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++row) {
                            initializeColor(_mixer.getCircle(row, column));
                        }
                        for (int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++row) {
                            initializeColor(_mixer.getSlider(row, column));
                        }
                        for (int row = 0; row < MXAppConfig.DRUM_ROW_COUNT; ++row) {
                            initializeColor(_mixer.getDrumPad(row, column));
                        }
                    }
                } catch (NullPointerException e) {
                }
            }
        });
    }

    public void doImportMixer() {
        MXFileChooser chooser = new MXFileChooser();
        chooser.addExtension(".xml", "XML File");
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            MXSetting setting = new MXSetting(file, false);
            setting.setTarget(_mixer);
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
            setting.setTarget(_mixer);
            setting.writeSettingFile();
            JOptionPane.showMessageDialog(this, "Succeed Export [" + file + "]");
        }
    }

    public void doResizeMixer() {
        MX30ResizeMixerSetting config = new MX30ResizeMixerSetting(_mixer._parent);
        MXUtil.showAsDialog(this, config, "Resize Mixer");
        if (config._okOption) {
            _mixer._parent.globalContollerHidden();
        }
    }

    public void doStartEditMixer() {
        _mixer._view.toggleEditing();
    }

    public JPanel getControllerCase() {
        return jPanelControllers;
    }

    public void initControllers() {
        MX32MixerProcess mixer = this._mixer;

        try {
            _focusGroup = new MXFocusGroup(this);

            mixer.setEveryComponents(null, null, null);

            JPanel panel001 = getControllerCase();

            java.awt.GridBagConstraints gbc;
            panel001.removeAll();
            panel001.setLayout(new GridBagLayout());

            columnLabel = new JLabel[MXAppConfig.SLIDER_COLUMN_COUNT];
            circleLabel = new JLabel[MXAppConfig.CIRCLE_ROW_COUNT];
            sliderLabel = new JLabel[MXAppConfig.SLIDER_ROW_COUNT];
            drumLabel = new JLabel[MXAppConfig.DRUM_ROW_COUNT];

            for (int col = 0; col < MXAppConfig.SLIDER_COLUMN_COUNT; col++) {
                String text = Integer.toString(col + 1);
                JLabel label = new JLabel(text);
                columnLabel[col] = label;

                gbc = new java.awt.GridBagConstraints();
                gbc.gridx = col + 1;
                gbc.gridy = MXAppConfig.CIRCLE_ROW_COUNT + MXAppConfig.SLIDER_ROW_COUNT + MXAppConfig.DRUM_ROW_COUNT;
                gbc.fill = java.awt.GridBagConstraints.NONE;
                gbc.anchor = java.awt.GridBagConstraints.CENTER;
                gbc.weightx = 1.0;
                gbc.weighty = 0;

                panel001.add(label, gbc);
            }

            ArrayList<MGCircle>[] matrixCircle = new ArrayList[MXAppConfig.CIRCLE_ROW_COUNT];

            int positionY = 0;

            for (int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++row) {
                ArrayList<MGCircle> newCircle = new ArrayList<MGCircle>();

                JLabel label = new JLabel(Character.toString('A' + row));
                circleLabel[row] = label;
                gbc = new java.awt.GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.fill = java.awt.GridBagConstraints.NONE;
                gbc.anchor = java.awt.GridBagConstraints.CENTER;
                gbc.weightx = 1.0;
                gbc.weighty = 0;
                panel001.add(label, gbc);

                for (int col = 0; col < MXAppConfig.SLIDER_COLUMN_COUNT; col++) {
                    MGCircle cc1 = new MGCircle(_mixer, row, col);

                    cc1.setSize(50, 50);
                    _focusGroup.attach(cc1);

                    gbc = new java.awt.GridBagConstraints();
                    gbc.gridx = col + 1;
                    gbc.gridy = positionY + row;
                    gbc.fill = java.awt.GridBagConstraints.BOTH;
                    gbc.anchor = java.awt.GridBagConstraints.CENTER;
                    gbc.weightx = 0;
                    gbc.weighty = 0;

                    panel001.add(cc1, gbc);
                    newCircle.add(cc1);
                }
                matrixCircle[row] = newCircle;
            }
            positionY = MXAppConfig.CIRCLE_ROW_COUNT;

            ArrayList<MGSlider>[] matrixSlider = new ArrayList[MXAppConfig.SLIDER_ROW_COUNT];

            for (int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++row) {
                ArrayList<MGSlider> newSlider = new ArrayList<MGSlider>();
                matrixSlider[row] = newSlider;

                JLabel label = new JLabel(Character.toString('S' + row));
                sliderLabel[row] = label;
                gbc = new java.awt.GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = row + positionY;
                gbc.fill = java.awt.GridBagConstraints.NONE;
                gbc.anchor = java.awt.GridBagConstraints.CENTER;
                gbc.weightx = 1.0;
                gbc.weighty = 0;
                panel001.add(label, gbc);

                for (int col = 0; col < MXAppConfig.SLIDER_COLUMN_COUNT; ++col) {
                    MGSlider sc1 = new MGSlider(_mixer, row, col);
                    _focusGroup.attach(sc1);

                    sc1.setSize(50, 100);

                    gbc = new java.awt.GridBagConstraints();
                    gbc.gridx = col + 1;
                    gbc.gridy = positionY + row;
                    gbc.fill = java.awt.GridBagConstraints.BOTH;
                    gbc.anchor = java.awt.GridBagConstraints.CENTER;
                    gbc.weightx = 1.0;
                    gbc.weighty = 1.0;

                    panel001.add(sc1, gbc);
                    newSlider.add(sc1);
                }
            }
            positionY = MXAppConfig.CIRCLE_ROW_COUNT + MXAppConfig.SLIDER_ROW_COUNT;

            ArrayList<MGDrumPad>[] matrixPad = new ArrayList[MXAppConfig.DRUM_ROW_COUNT];

            for (int row = 0; row < MXAppConfig.DRUM_ROW_COUNT; ++row) {
                JLabel label = new JLabel(Character.toString('X' + row));
                drumLabel[row] = label;

                gbc = new java.awt.GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = row + positionY;
                gbc.fill = java.awt.GridBagConstraints.NONE;
                gbc.anchor = java.awt.GridBagConstraints.CENTER;
                gbc.weightx = 1.0;
                gbc.weighty = 0;
                panel001.add(label, gbc);

                ArrayList<MGDrumPad> newPad = new ArrayList<MGDrumPad>();
                for (int col = 0; col < MXAppConfig.SLIDER_COLUMN_COUNT; ++col) {
                    MGStatus number = mixer.getStatus(MGStatus.TYPE_DRUMPAD, row, col);
                    MGDrumPad rc1 = new MGDrumPad(_mixer, row, col);
                    //rc1.initUIWithStatus(number);

                    _focusGroup.attach(rc1);

                    newPad.add(rc1);
                    rc1.setSize(50, 50);

                    gbc = new java.awt.GridBagConstraints();
                    gbc.gridx = col + 1;
                    gbc.gridy = positionY + row;
                    gbc.fill = java.awt.GridBagConstraints.BOTH;
                    gbc.anchor = java.awt.GridBagConstraints.CENTER;
                    gbc.weightx = 1.0;
                    gbc.weighty = 0;

                    panel001.add(rc1, gbc);
                }
                matrixPad[row] = newPad;
            }

            positionY = MXAppConfig.CIRCLE_ROW_COUNT + MXAppConfig.SLIDER_ROW_COUNT + MXAppConfig.DRUM_ROW_COUNT;

            int x = chainModel.indexOfValue(mixer._patchToMixer);
            jComboBoxChain.setSelectedIndex(x);

            mixer.setEveryComponents(matrixSlider, matrixCircle, matrixPad);

            jCheckBoxSyncTogether.setSelected(mixer._patchTogether);
        } finally {
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

    JLabel[] columnLabel;
    JLabel[] circleLabel;
    JLabel[] sliderLabel;
    JLabel[] drumLabel;

    public void globalControllerHidden() {
        int lines = _mixer._parent.getVisibleLineCount();
        boolean ranged;
        for (int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++row) {
            ranged = _mixer._parent.isKnobVisible(row);
            circleLabel[row].setVisible(ranged);
        }
        for (int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++row) {
            ranged = true;
            sliderLabel[row].setVisible(ranged);
        }
        for (int row = 0; row < MXAppConfig.DRUM_ROW_COUNT; ++row) {
            ranged = _mixer._parent.isPadVisible(row);
            drumLabel[row].setVisible(ranged);
        }

        for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
            ranged = column < lines;
            columnLabel[column].setVisible(ranged);

            for (int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++row) {
                ranged = _mixer._parent.isKnobVisible(row) && column < lines;
                _mixer.getCircle(row, column).setVisible(ranged);
            }
            for (int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++row) {
                ranged = column < lines;
                _mixer.getSlider(row, column).setVisible(ranged);
            }
            for (int row = 0; row < MXAppConfig.DRUM_ROW_COUNT; ++row) {
                ranged = _mixer._parent.isPadVisible(row) && column < lines;
                _mixer.getDrumPad(row, column).setVisible(ranged);
            }
        }
        updateUI();
    }

    public void showTextForFocus(JComponent focus) {
        String value = "(NONE)";

        try {
            if (focus != null) {                
                if (focus instanceof MGSlider) {
                    MGSlider ctrl = (MGSlider)focus;
                    value = ctrl.getStatus().toString();
                }
                else if (focus instanceof MGCircle) {
                    MGCircle ctrl = (MGCircle)focus;
                    value = ctrl.getStatus().toString();
                }
                else if (focus instanceof MGDrumPad) {
                    MGDrumPad ctrl = (MGDrumPad)focus;
                    value = ctrl.getStatus().toString();
                }
            }
        } catch (Throwable e) {
            value = "ERR";
        }

        setFocusString(value);
    }

    public void goNextFocus(final int keyCode) {
        if (_focusGroup.getFocus() == null) {
            return;
        }
        MXFocusTargetInfo info = _focusGroup.getFocus();
        if (info== null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    goNextFocus(keyCode);
                }
            });
            return;
        }
        
        JComponent focus = info._root;
        MGStatus status = null;
        if (focus instanceof MGSlider) {
            MGSlider ctrl = (MGSlider)focus;
            status = ctrl.getStatus();
        }
        else if (focus instanceof MGCircle) {
            MGCircle ctrl = (MGCircle)focus;
            status = ctrl.getStatus();
        }
        else if (focus instanceof MGDrumPad) {
            MGDrumPad ctrl = (MGDrumPad)focus;
            status = ctrl.getStatus();
        }
        else {
            return;
        }

        int type = status._uiType;
        int row = status._row;
        int column = status._column;

        if (type < 0) {
            type = 0;
            row = 0;
            column = 0;
        } else {
            switch (keyCode) {
                case KeyEvent.VK_UP: //UP
                    if (row > 0) {
                        row--;
                        break;
                    }
                    switch (--type) {
                        case 0:
                            type = 1;
                            break;
                        case MGStatus.TYPE_CIRCLE:
                            row = 3;
                            break;
                        case MGStatus.TYPE_SLIDER:
                            row = 0;
                            break;
                        case MGStatus.TYPE_DRUMPAD:
                            row = 1;
                            break;
                    }
                    break;

                case KeyEvent.VK_DOWN: //DOWN
                    row++;
                    switch (type) {
                        case MGStatus.TYPE_CIRCLE:
                            if (row >= 4) {
                                row = 0;
                                type = MGStatus.TYPE_SLIDER;
                            }
                            break;
                        case MGStatus.TYPE_SLIDER:
                            if (row >= 1) {
                                row = 0;
                                type = MGStatus.TYPE_DRUMPAD;
                            }
                            break;
                        case MGStatus.TYPE_DRUMPAD:
                            if (row >= 2) {
                                row--;
                            }
                            break;
                    }
                    break;

                case KeyEvent.VK_LEFT: //LEFT
                    column--;
                    if (column < 0) {
                        column = 0;
                    }
                    break;

                case KeyEvent.VK_RIGHT: //RIGHT
                    column++;
                    if (column >= MXAppConfig.SLIDER_COLUMN_COUNT) {
                        column--;
                    }
                    break;

                case ' ':
                case '\b':
                case '\n':
                    switch (type) {
                        case MGStatus.TYPE_SLIDER:
                            MGSlider slider = _mixer.getSlider(row, column);
                            switch (keyCode) {
                                case ' ':
                                    slider.increment(null);
                                    break;
                                case '\b':
                                    slider.decriment(null);
                                    break;
                                case '\n':
                                    slider.editContoller();
                                    break;
                            }
                            break;
                        case MGStatus.TYPE_CIRCLE:
                            MGCircle circle = _mixer.getCircle(row, column);
                            switch (keyCode) {
                                case ' ':
                                    circle.increment(null);
                                    break;
                                case '\b':
                                    circle.decriment(null);
                                    break;
                                case '\n':
                                    circle.editContoller();
                                    break;
                            }
                            break;
                        case MGStatus.TYPE_DRUMPAD:
                            MGDrumPad drum = _mixer.getDrumPad(row, column);
                            switch (keyCode) {
                                case ' ':
                                    drum.increment(null);
                                    break;
                                case '\b':
                                    drum.decriment(null);
                                    break;
                                case '\n':
                                    drum.editContoller();
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    break;
            }
        }
        showTextForFocus(focus);
    }

    public void toggleEditing() {
        _focusGroup._editMode = !_focusGroup._editMode;
        if (_focusGroup._editMode) {
            MXMain.getMain().getMainWindow().setTitle(MXAppConfig.MX_EDITING);
        }
        else {
            MXMain.getMain().getMainWindow().setTitle(MXAppConfig.MX_APPNAME);
        }
    }

    public void stopEditing() {
        _focusGroup._editMode = false;
        MXMain.getMain().getMainWindow().setTitle(MXAppConfig.MX_APPNAME);
    }

    @Override
    public void focusMouseClicked(JComponent comp, MouseEvent e) {
       if (SwingUtilities.isRightMouseButton(e) || _mixer._view._focusGroup._editMode) {
            _mixer._view.stopEditing();
            if (comp instanceof MGCircle) {
                MGCircle c2 = (MGCircle)comp;
                c2.editContoller();
            }
            if (comp instanceof MGSlider) {
                MGSlider c2 = (MGSlider)comp;
                c2.editContoller();
            }
            if (comp instanceof MGDrumPad) {
                MGDrumPad c2 = (MGDrumPad)comp;
                c2.editContoller();
            }
        }
    }

    @Override
    public void focusOnAfterColored(JComponent comp) {
        showTextForFocus(comp);
    }

    @Override
    public void focusOffAfterUncolored(JComponent comp) {
    }

    @Override
    public boolean isFocusWithSelected(JComponent comp) {
        if (comp instanceof MGDrumPad) {
            MGDrumPad c2 = (MGDrumPad)comp;
            return c2._focusSelected;
        }
        return false;
    }

    @Override
    public Color getDefaultColor(JComponent c) {
        int col = -1;
        if (c instanceof MGSlider) {
            MGSlider slider = (MGSlider) c;
            col = slider._column;
        } else if (c instanceof MGCircle) {
            MGCircle circle = (MGCircle) c;
            col = circle._column;
        } else if (c instanceof MGDrumPad) {
            MGDrumPad pad = (MGDrumPad) c;
            col = pad._column;
        }
        if (col >= 0) {
            return MXAppConfig.sliderColor(col);
        }
        return null;
    }
    
    public void initializeColor(JComponent comp) {
        Color color = getDefaultColor(comp);

        comp.setBackground(color);

        LinkedList<Container> listContainer = new LinkedList();
        listContainer.add(comp);
        while (listContainer.isEmpty() == false) {
            Container cont = listContainer.remove();
            Component[] list = cont.getComponents();
            for (Component child : list) {
                child.setBackground(color);
                if (child instanceof Container) {
                    listContainer.add((Container) child);
                }
            }
        }
    }
}
