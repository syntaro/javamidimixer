/*
 * Copyright (C) 2024 Syntarou YOSHIDA
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
package jp.synthtarou.mixtone.synth.view;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import javax.swing.JOptionPane;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.navigator.legacy.INavigator;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileFilterListExt;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileList;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.MXFolderBrowser;
import jp.synthtarou.mixtone.synth.XTSynthesizer;
import jp.synthtarou.mixtone.synth.XTSynthesizerSetting;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTSynthesizerSettingPanel extends javax.swing.JPanel {

    /**
     * Creates new form XTSynthesizerPanel
     */
    public XTSynthesizerSettingPanel() {
        initComponents();
        XTSynthesizerSetting setting = XTSynthesizerSetting.getSetting();

        float rate = setting.getSampleRate();
        buttonGroupHz.add(jRadioButton22khz);
        buttonGroupHz.add(jRadioButton44khz);
        buttonGroupHz.add(jRadioButton48khz);
        if (rate == 22050) {
            jRadioButton22khz.setSelected(true);
        }
        else if (rate == 44100) {
            jRadioButton44khz.setSelected(true);
        }
        else if (rate == 48000) {
            jRadioButton48khz.setSelected(true);
        }
        buttonGroupBit.add(jRadioButton16bit);
        jRadioButton16bit.setSelected(true);

        buttonGroupSmpl.add(jRadioButton128smpl);
        buttonGroupSmpl.add(jRadioButton256smpl);
        buttonGroupSmpl.add(jRadioButton512smpl);
        buttonGroupSmpl.add(jRadioButton1024smpl);
        buttonGroupSmpl.add(jRadioButton2048smpl);
        buttonGroupSmpl.add(jRadioButton360smpl);
        int smpl = setting.getSamplePageSize();
        if (smpl == 128) {
            jRadioButton128smpl.setSelected(true);
        }
        else if (smpl == 256) {
            jRadioButton256smpl.setSelected(true);
        }
        else if (smpl == 360) {
            jRadioButton360smpl.setSelected(true);
        }
        else if (smpl == 512) {
            jRadioButton512smpl.setSelected(true);
        }
        else if (smpl == 1024) {
            jRadioButton1024smpl.setSelected(true);
        }
        else if (smpl == 2048) {
            jRadioButton2048smpl.setSelected(true);
        }
        
        jSliderSwitch.setMinimum(0);
        jSliderSwitch.setMaximum(1);
        
        jSliderSwitch.setValue(setting.getSynthInstance().isReady() ? 1 : 0);
        jSliderSwitch.addChangeListener((evt) -> {
            if (jSliderSwitch.getValue() > 0) {
                loadSynth();
            }
            else {
                unloadSynth();
            }
        });

        jRadioButton22khz.addActionListener(this::readPanelKhz);
        jRadioButton44khz.addActionListener(this::readPanelKhz);
        jRadioButton48khz.addActionListener(this::readPanelKhz);
        //jRadioButton16bit.addActionListener(this::readPanel);
        jRadioButton128smpl.addActionListener(this::readPanelSmpl);
        jRadioButton256smpl.addActionListener(this::readPanelSmpl);
        jRadioButton360smpl.addActionListener(this::readPanelSmpl);
        jRadioButton512smpl.addActionListener(this::readPanelSmpl);
        jRadioButton1024smpl.addActionListener(this::readPanelSmpl);
        jRadioButton2048smpl.addActionListener(this::readPanelSmpl);
    }
    

    public void readPanelKhz(ActionEvent evt) {
        XTSynthesizerSetting setting = XTSynthesizerSetting.getSetting();
        XTSynthesizer synth = setting.getSynthInstance();
        if (jRadioButton22khz.isSelected()) {
            setting.setSampleRate(22050);
        }
        if (jRadioButton44khz.isSelected()) {
            setting.setSampleRate(44100);
        }
        if (jRadioButton48khz.isSelected()) {
            setting.setSampleRate(48000);
        }
        unloadSynth();
        loadSynth();
    }

    public void readPanelSmpl(ActionEvent evt) {
        XTSynthesizerSetting setting = XTSynthesizerSetting.getSetting();
        if (jRadioButton128smpl.isSelected()) {
            setting.setSamplePageSize(128);
        }
        if (jRadioButton256smpl.isSelected()) {
            setting.setSamplePageSize(256);
        }
        if (jRadioButton360smpl.isSelected()) {
            setting.setSamplePageSize(360);
        }
        if (jRadioButton512smpl.isSelected()) {
            setting.setSamplePageSize(512);
        }
        if (jRadioButton1024smpl.isSelected()) {
            setting.setSamplePageSize(1024);
        }
        if (jRadioButton2048smpl.isSelected()) {
            setting.setSamplePageSize(2048);
        }
        unloadSynth();
        loadSynth();
    }
    
    public void unloadSynth() {
        XTSynthesizerSetting setting = XTSynthesizerSetting.getSetting();
        XTSynthesizer synth = setting.getSynthInstance();
        synth.stopAudioStream();
    }
    
    public void loadSynth() {
        XTSynthesizerSetting setting = XTSynthesizerSetting.getSetting();
        XTSynthesizer synth = setting.getSynthInstance();
        synth.startAudioStream();
        if (synth.isReady()) {
            testNote();
        }
        else {
            if (jSliderSwitch.getValue() > 0) {
                jSliderSwitch.setValue(0);
            }            
        }
    }

    public void testNote() {
        XTSynthesizerSetting setting = XTSynthesizerSetting.getSetting();
        XTSynthesizer synth = setting.getSynthInstance();
        synth.processMessage(OneMessage.thisCodes(0, MXMidiStatic.COMMAND_CH_NOTEON, 64, 127));
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                synth.processMessage(OneMessage.thisCodes(0, MXMidiStatic.COMMAND_CH_NOTEOFF, 64, 0));
            }catch(Throwable ex) {
                ex.printStackTrace();
            }
        }).start();
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

        buttonGroupHz = new javax.swing.ButtonGroup();
        buttonGroupBit = new javax.swing.ButtonGroup();
        buttonGroupSmpl = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jRadioButton22khz = new javax.swing.JRadioButton();
        jRadioButton44khz = new javax.swing.JRadioButton();
        jRadioButton48khz = new javax.swing.JRadioButton();
        jRadioButton16bit = new javax.swing.JRadioButton();
        jRadioButton128smpl = new javax.swing.JRadioButton();
        jRadioButton256smpl = new javax.swing.JRadioButton();
        jRadioButton512smpl = new javax.swing.JRadioButton();
        jRadioButton1024smpl = new javax.swing.JRadioButton();
        jRadioButton2048smpl = new javax.swing.JRadioButton();
        jRadioButton360smpl = new javax.swing.JRadioButton();
        jButtonTestTone = new javax.swing.JButton();
        jSliderSwitch = new javax.swing.JSlider();
        jTextFieldFile = new javax.swing.JTextField();
        jButtonFile = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Sample Rate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

        jLabel2.setText("Sample Bits");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel2, gridBagConstraints);

        jLabel3.setText("Switch");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel3, gridBagConstraints);

        jLabel4.setText("Latency");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel4, gridBagConstraints);

        jRadioButton22khz.setText("22050");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jRadioButton22khz, gridBagConstraints);

        jRadioButton44khz.setText("44100");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jRadioButton44khz, gridBagConstraints);

        jRadioButton48khz.setText("48000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jRadioButton48khz, gridBagConstraints);

        jRadioButton16bit.setText("16bit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jRadioButton16bit, gridBagConstraints);

        jRadioButton128smpl.setText("128");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jRadioButton128smpl, gridBagConstraints);

        jRadioButton256smpl.setText("256");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jRadioButton256smpl, gridBagConstraints);

        jRadioButton512smpl.setText("512");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jRadioButton512smpl, gridBagConstraints);

        jRadioButton1024smpl.setText("1024");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jRadioButton1024smpl, gridBagConstraints);

        jRadioButton2048smpl.setText("2048");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jRadioButton2048smpl, gridBagConstraints);

        jRadioButton360smpl.setText("360");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jRadioButton360smpl, gridBagConstraints);

        jButtonTestTone.setText("Test Tone");
        jButtonTestTone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTestToneActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButtonTestTone, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jSliderSwitch, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jTextFieldFile, gridBagConstraints);

        jButtonFile.setText("Select");
        jButtonFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        add(jButtonFile, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonTestToneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTestToneActionPerformed
        testNote();
    }//GEN-LAST:event_jButtonTestToneActionPerformed

    private void jButtonFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFileActionPerformed
        FileFilter filtersf2 = new FileFilterListExt(new String[]{".sf2"});
        MXFolderBrowser chooser = new MXFolderBrowser(new File("c:"), filtersf2);
        MXUtil.showAsDialog(this, chooser, "Choise sf2");
        if (chooser.getReturnStatus() != INavigator.RETURN_STATUS_APPROVED) {
            return;
        }
        FileList selected  = chooser.getReturnValue();
        if (selected == null || selected.size() <= 0) {
            return;
        }
        File one = selected.get(0);
        try {
            XTSynthesizerSetting.getSetting().getSynthInstance().openSoundfont(one);
        }catch(Throwable ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.toString(), "Errror", JOptionPane.OK_OPTION);
        }
    }//GEN-LAST:event_jButtonFileActionPerformed
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupBit;
    private javax.swing.ButtonGroup buttonGroupHz;
    private javax.swing.ButtonGroup buttonGroupSmpl;
    private javax.swing.JButton jButtonFile;
    private javax.swing.JButton jButtonTestTone;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JRadioButton jRadioButton1024smpl;
    private javax.swing.JRadioButton jRadioButton128smpl;
    private javax.swing.JRadioButton jRadioButton16bit;
    private javax.swing.JRadioButton jRadioButton2048smpl;
    private javax.swing.JRadioButton jRadioButton22khz;
    private javax.swing.JRadioButton jRadioButton256smpl;
    private javax.swing.JRadioButton jRadioButton360smpl;
    private javax.swing.JRadioButton jRadioButton44khz;
    private javax.swing.JRadioButton jRadioButton48khz;
    private javax.swing.JRadioButton jRadioButton512smpl;
    private javax.swing.JSlider jSliderSwitch;
    private javax.swing.JTextField jTextFieldFile;
    // End of variables declaration//GEN-END:variables
}
