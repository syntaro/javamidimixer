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
package jp.synthtarou.midimixer.mx80vst;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.async.Transaction;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOut;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOutManager;
import jp.synthtarou.midimixer.libs.swing.MXModalFrame;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderSingleClick;
import jp.synthtarou.midimixer.libs.vst.IndexedFile;
import jp.synthtarou.midimixer.libs.vst.MXPresetAction;
import jp.synthtarou.midimixer.libs.vst.MXPresetPanel;
import jp.synthtarou.midimixer.libs.vst.VSTInstance;
import jp.synthtarou.midimixer.libs.vst.VSTStream;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class VSTInstancePanel extends javax.swing.JPanel {

    VSTInstance _instance;
    MX80Panel _parent;
    boolean _initDone = false;

    public void setParent(MX80Panel parent) {
        _parent = parent;
    }
    
    public void noticeBlackListed() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int opt = JOptionPane.showConfirmDialog(
                        VSTInstancePanel.this, 
                        "Do you want to try Reload?", 
                        "BlackListed by Exception", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    enterCloseVST();
                    enterOpenVST();
                }
                else {
                    enterCloseVST();
                }
            }
        });
    }

    Transaction _loadHandler = new Transaction("loadHandler") {
        @Override
        public void run() {
            if (_instance.getPath() == null || _instance.getPath().isEmpty()) {
                jLabelName.setText("-");
                jLabelName.setToolTipText("-");
                jButtonLoad.setText("Open By Navi");
                jButtonLoad.setEnabled(true);
                _editorHandler.run();
                jButtonLaunch.setEnabled(false);
                jButtonEdit.setEnabled(false);
                jButtonPreset.setEnabled(false);
            }else {
                jLabelName.setText(_instance.getName());
                jLabelName.setToolTipText(_instance.getPath());
                jButtonLoad.setText("Bye For Now");
                jButtonLoad.setEnabled(true);

                if (_instance.isOpen()) {
                    jButtonLaunch.setEnabled(true);
                    jButtonLaunch.setText("Close");
                    jButtonEdit.setEnabled(true);
                    jButtonPreset.setEnabled(true);
                }else {
                    jButtonLaunch.setEnabled(true);
                    jButtonLaunch.setText("Load");
                    jButtonEdit.setEnabled(false);
                    jButtonPreset.setEnabled(false);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            jPanelVolume.removeAll();
                            jPanelVolume.updateUI();
                        }
                    });
                }
                _editorHandler.run();
            }
            createVolumePanel();
            if (_parent != null) {
                _parent.onResizeSynth();
            }
        }
    };

    Transaction _editorHandler = new Transaction("editorHandler") {
        @Override
        public void run() {
            if (_instance.isOpen() == false) {
                jButtonEdit.setEnabled(false);
                jButtonEdit.setText("Editor");
                jButtonPreset.setEnabled(false);
            }else {
                jButtonEdit.setEnabled(true);
                jButtonPreset.setEnabled(true);
                if (_instance.isEditorOpen()) {
                    jButtonEdit.setText("Close Editor");
                }else {
                    jButtonEdit.setText("Editor");
                }
            }
        }
    };
    /**
     * Creates new form VSTInstancePanel
     */
    public VSTInstancePanel(VSTInstance vst) {
        initComponents();
        _instance = vst;
        jLabelName.setText(_instance.getName());
        jLabelName.setToolTipText(_instance.getPath());
        _loadHandler.run();

        if (vst.isEffect()) {
            char ch = (char)('A' + vst.getSlot());
            String slot = "Effect " + Character.toString(ch);
            if (vst.getSlot() == 0) {
                slot += ":Insert";
            }else {
                slot += ":Aux Send";
            }
            setBorder(BorderFactory.createTitledBorder(slot));
        }else {
            char ch = (char)('A' + vst.getSlot());
            String slot = "Slot " + Character.toString(ch);            
            setBorder(BorderFactory.createTitledBorder(slot));
        }
        
        new MXAttachSliderLikeEclipse(jSliderInsert);
        new MXAttachSliderLikeEclipse(jSliderSend);
        new MXAttachSliderSingleClick(jSliderInsert);
        new MXAttachSliderSingleClick(jSliderSend);
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

        jLabelName = new javax.swing.JLabel();
        jButtonLoad = new javax.swing.JButton();
        jButtonEdit = new javax.swing.JButton();
        jButtonLaunch = new javax.swing.JButton();
        jButtonPreset = new javax.swing.JButton();
        jPanelVolume = new javax.swing.JPanel();
        jLabelSend = new javax.swing.JLabel();
        jSliderSend = new javax.swing.JSlider();
        jSliderInsert = new javax.swing.JSlider();
        jLabelInsert = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Slot 1"));
        setLayout(new java.awt.GridBagLayout());

        jLabelName.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        add(jLabelName, gridBagConstraints);

        jButtonLoad.setText("from Navi");
        jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jButtonLoad, gridBagConstraints);

        jButtonEdit.setText("Editor");
        jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButtonEdit, gridBagConstraints);

        jButtonLaunch.setText("Launch");
        jButtonLaunch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLaunchActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jButtonLaunch, gridBagConstraints);

        jButtonPreset.setText("Preset");
        jButtonPreset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPresetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButtonPreset, gridBagConstraints);

        jPanelVolume.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelVolume, gridBagConstraints);

        jLabelSend.setText("Insert Bal");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelSend, gridBagConstraints);

        jSliderSend.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderSendStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jSliderSend, gridBagConstraints);

        jSliderInsert.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderInsertStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jSliderInsert, gridBagConstraints);

        jLabelInsert.setText("Aux Send");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelInsert, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    public void enterGoodbyeVST() {
        int opt = JOptionPane.showConfirmDialog(this, "Do you want to bye VST[" + _instance.getName() + "] for Now?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            jButtonLoad.setEnabled(false);
            _instance.setPath(null);
            _instance.postCloseVST(_loadHandler.copyWithNewTicket("postCloseVST"));
            File file = VSTInstance.getTotalRecallSetting(_instance.isEffect(), _instance.getSlot());
            file.delete();
        }
    }

    public void enterSetVST(File file) {
        jButtonLaunch.setEnabled(false);
        jButtonLoad.setEnabled(false);
        _instance.setPath(file.getPath());
        enterOpenVST();
    }

    public void enterOpenVST() {
        jButtonLaunch.setEnabled(false);
        MX80Process process = MX80Process.getInstance();
        _instance.postLaunchVST(_loadHandler.copyWithNewTicket("enterOpenVST"));
    }
    
    public void enterCloseVST() {
        jButtonLaunch.setEnabled(false);
        _instance.postCloseEditor(_editorHandler.copyWithNewTicket("postCloseEditor"));
        _instance.postCloseVST(_loadHandler.copyWithNewTicket("postCloseVSTi"));
    }
 
    public void enterOpenEditor() {
        jButtonEdit.setEnabled(false);
        _instance.postOpenEditor(_editorHandler.copyWithNewTicket("postOpenEditor"), _editorHandler.copyWithNewTicket("postOpenEditor#close"));
    }

    public void enterCloseEditor() {
        jButtonEdit.setEnabled(false);
        _instance.postCloseEditor(_editorHandler.copyWithNewTicket("postCloseEditor"));
    }    
   
    private void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadActionPerformed
        // TODO add your handling code here:
        if (_instance.getPath() != null && _instance.getPath().length() > 0) {
            enterGoodbyeVST();
            return;
        }

        File file = _parent.getSelectedFileAsVST();
        if (file != null && file.exists()) {
            enterSetVST(file);
            if (VSTStream.getInstance().isOpen() == false) {
                JOptionPane.showMessageDialog(this, "Please Open Steram too", "Notice", JOptionPane.OK_OPTION);
            }else {
            }
        }else {
            JOptionPane.showMessageDialog(this, "Select VST from Left Navigateion", "Message", JOptionPane.OK_OPTION);
        }
    }//GEN-LAST:event_jButtonLoadActionPerformed
    
    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
        // TODO add your handling code here:
        if (_instance.isEditorOpen()) {
            enterCloseEditor();
        }else {
            enterOpenEditor();
        }
    }//GEN-LAST:event_jButtonEditActionPerformed

    private void jButtonLaunchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLaunchActionPerformed
        if (_instance.isOpen()) {
            Transaction tr = new Transaction("closeVST - preset");
            _instance.postSavePreset(VSTInstance.getTotalRecallSetting(_instance.isEffect(), _instance.getSlot()).getPath(), tr);
            enterCloseVST();
        }else {
            enterOpenVST();
            
            Transaction tr = new Transaction("openVST - preset");
            File file = VSTInstance.getTotalRecallSetting(_instance.isEffect(), _instance.getSlot());
            if (file.exists() && file.isFile()) {
                _instance.postLoadPreset(file.getPath(), tr);
            }
            
            if (!_instance.isEffect()) {
                MXWrapList<MXMIDIOut> list = MXMIDIOutManager.getManager().listSelectedOutput();
                MXMIDIOut found = null;
                for (int i = 0; i < list.getSize(); ++ i) {
                    MXMIDIOut out = list.valueOfIndex(i);
                    if (out.isDriverTypeVSTi()) {
                        int x = out.getVStiDriverNumber();
                        if (x == this._instance.getSlot()) {
                            found = out;
                            break;
                        }
                    }
                }
                if (found == null) {
                    JOptionPane.showMessageDialog(this, "Not Assigned @ Output Tab", "Please Assign", JOptionPane.OK_OPTION);
                }
            }
        }
    }//GEN-LAST:event_jButtonLaunchActionPerformed

    private void jButtonPresetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPresetActionPerformed
        // TODO add your handling code here:
        IndexedFile root = VSTInstance.getGlobalSetting(_instance.getPath());
        if (root.getLock() == false) {
            JOptionPane.showMessageDialog(this, "Can't occupation", "Error", JOptionPane.OK_OPTION);
            return;
        }
        
        try {
            MXPresetPanel panel = new MXPresetPanel(root, new MXPresetAction() {
                @Override
                public boolean presetActionSave(File file) {
                    if (file != null) {
                        Transaction tr = new Transaction("presetActinoSave");
                        _instance.postSavePreset(file.getPath(), tr);
                        return tr.awaitResult() == 0 ? true : false;
                    }
                    return false;
                }

                @Override
                public boolean presetActionLoad(File file) {
                    if (file != null && file.exists()) {
                        Transaction tr = new Transaction("presetActionLoad");
                        _instance.postLoadPreset(file.getPath(), tr);
                        return tr.awaitResult() == 0 ? true : false;
                    }
                    return false;
                }
            });
            MXModalFrame.showAsDialog(this, panel, "Preset for Slot " + _instance.getSlot());
        }finally {
            root.releaseLock();
        }
    }//GEN-LAST:event_jButtonPresetActionPerformed

    private void jSliderInsertStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderInsertStateChanged
        if (_initDone) {
            _instance.setInsertBalance(jSliderInsert.getValue());
        }
    }//GEN-LAST:event_jSliderInsertStateChanged

    private void jSliderSendStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderSendStateChanged
        if (_initDone) {
            _instance.setAuxSend(jSliderSend.getValue());
        }
    }//GEN-LAST:event_jSliderSendStateChanged

    public boolean isOpen() {
        return _instance.isOpen();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonLaunch;
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JButton jButtonPreset;
    private javax.swing.JLabel jLabelInsert;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelSend;
    private javax.swing.JPanel jPanelVolume;
    private javax.swing.JSlider jSliderInsert;
    private javax.swing.JSlider jSliderSend;
    // End of variables declaration//GEN-END:variables
    
    public void createVolumePanel() {
        if (_instance.isEffect()) {
            jLabelSend.setVisible(false);
            jLabelInsert.setVisible(false);
            jSliderSend.setVisible(false);
            jSliderInsert.setVisible(false);
            return;
        }
        _initDone = false;
        jPanelVolume.removeAll();
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        int count = _instance.getBusCount();
        int contents = 0;
        if (_instance.isOpen() == false) {
            count = 0;
        }
        for (int bus = 0; bus < count; ++ bus) {
            if ((bus % 8) == 0) {
                jPanelVolume.add(panel);
                panel.updateUI();
                panel.setBorder(null); //BorderFactory.createEmptyBorder());
                panel = new JPanel();
                contents = 0;
            }
            GridBagConstraints gridBagConstraints;
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = bus;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1;
            gridBagConstraints.weighty = 1;

            VSTVolume slider = new VSTVolume(_instance, bus);
            panel.add(slider, gridBagConstraints);        
            contents ++;
        }
        if (contents > 0) {
            jPanelVolume.add(panel);
            panel.updateUI();;
        }
        if (_instance.getPath() == null) {
            jSliderInsert.setEnabled(false);
            jSliderSend.setEnabled(false);
        }else {
            jSliderInsert.setEnabled(true);
            jSliderInsert.setMinimum(0);
            jSliderInsert.setMaximum(127);
            jSliderInsert.setValue(_instance.getInsertBalanace());
            jSliderSend.setEnabled(true);
            jSliderSend.setMinimum(0);
            jSliderSend.setMaximum(127);
            jSliderSend.setValue(_instance.getAuxSend());
        }
        _initDone = true;
    }   

    public void onResize(int width) {
        /*
        boolean debug = false;

        setMinimumSize(new Dimension(width - 5, 100));
        setMaximumSize(null);
        Dimension temp = new Dimension(width - 5, 800);
        setPreferredSize(temp);
        
        if (debug) {
            System.out.println("width = " + width + " debug size " + jPanelVolume.getSize());
        }

        int maxy = 10;
        for (int i = 0; i < jPanelVolume.getComponentCount(); ++ i) {
            Component c = jPanelVolume.getComponent(i);
            int y = c.getHeight() + c.getY();
            if (maxy < y) {
                maxy = y;
            }
        }

        int thisy = maxy + jButtonEdit.getY() + jButtonEdit.getHeight() + 3;
        
        Dimension min = new Dimension(100, maxy);
        Dimension thismin = new Dimension(100, thisy);
        jPanelVolume.setMinimumSize(min);
        this.setMinimumSize(thismin);

        Dimension max = new Dimension(width, maxy);
        Dimension thismax = new Dimension(width, thisy);
        jPanelVolume.setPreferredSize(max);
        this.setPreferredSize(thismax);
        if (debug) {
            System.out.println("onResize min " + min);
            System.out.println("onResize min " + max);
        }
        
        updateUI();
        */
    }
}
