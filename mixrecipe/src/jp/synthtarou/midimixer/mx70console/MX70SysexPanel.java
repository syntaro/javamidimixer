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
package jp.synthtarou.midimixer.mx70console;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.libs.midi.console.MXMidiConsoleElement;
import jp.synthtarou.midimixer.libs.midi.console.MXMidiConsole;
import jp.synthtarou.libs.namedobject.MXNamedObjectListFactory;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.swing.MXFileChooser;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX70SysexPanel extends javax.swing.JPanel {
    MXMidiConsole _list;
    SysEXFile _file;
    MXNamedObjectList<Integer> _listPort = MXNamedObjectListFactory.listupPort(null);

    /**
     * Creates new form MX70SysexPanel
     */
    public MX70SysexPanel(MXMidiConsole sysex) {
        initComponents();
        _list = sysex;
        _list.bind(jListScan); // 更新も自動
        
        _file = new SysEXFile();
        _file.bind(jTextArea1);
        setPreferredSize(new Dimension(800, 600));
        
        jComboBoxPort.setModel(_listPort);
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
        jListScan = new javax.swing.JList<>();
        jButtonLoadSysex = new javax.swing.JButton();
        jButtonSaveSysex = new javax.swing.JButton();
        jButtonDumpSysex = new javax.swing.JButton();
        jComboBoxPort = new javax.swing.JComboBox<>();
        jButtonToFile = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jProgressBar1 = new javax.swing.JProgressBar();
        jButtonClearLog = new javax.swing.JButton();
        jButtonClearFile = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jListScan.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListScanValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListScan);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jButtonLoadSysex.setText("Load .sysex");
        jButtonLoadSysex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadSysexActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jButtonLoadSysex, gridBagConstraints);

        jButtonSaveSysex.setText("Save .sysex");
        jButtonSaveSysex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveSysexActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButtonSaveSysex, gridBagConstraints);

        jButtonDumpSysex.setText("Dump To");
        jButtonDumpSysex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDumpSysexActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButtonDumpSysex, gridBagConstraints);

        jComboBoxPort.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jComboBoxPort, gridBagConstraints);

        jButtonToFile.setText("This Line To File");
        jButtonToFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonToFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        add(jButtonToFile, gridBagConstraints);

        jToggleButton1.setText("Pause");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        add(jToggleButton1, gridBagConstraints);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane3.setViewportView(jTextArea1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jProgressBar1, gridBagConstraints);

        jButtonClearLog.setText("ClearLog");
        jButtonClearLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearLogActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButtonClearLog, gridBagConstraints);

        jButtonClearFile.setText("ClearFile");
        jButtonClearFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButtonClearFile, gridBagConstraints);

        jLabel1.setText("MultiSelect = with Ctrl / Shift");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        add(jLabel1, gridBagConstraints);

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(1024, 0, 9999, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jSpinner1, gridBagConstraints);

        jLabel2.setText("Split Bytes Per (0 -> nosplit)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        add(jLabel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonLoadSysexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadSysexActionPerformed
        _file.clear(jTextArea1);
        MXFileChooser chooser = new MXFileChooser();
        chooser.addExtension(".txt", "TEXT");
        chooser.addExtension(".sysex", "SysEX");
        chooser.addExtension(".syx", "SysEX");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            boolean success = false;
            if (file.getName().toLowerCase().endsWith(".txt")) { 
                success = _file.readText(file);
            }else {
                success = _file.readBinary(file);
            }
            _file.bind(jTextArea1);
            if (success) {
                JOptionPane.showMessageDialog(this, "Done", "Done Read" + file , JOptionPane.OK_OPTION);
            }else {
                JOptionPane.showMessageDialog(this, "Fail", "Fail Read" + file , JOptionPane.OK_OPTION);
            }
        }
    }//GEN-LAST:event_jButtonLoadSysexActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        _list.switchPause(jToggleButton1.isSelected());
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jButtonToFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonToFileActionPerformed
        int[] index = jListScan.getSelectedIndices();
        if (index != null & index.length >= 1) {
            for (int i = 0; i < index.length; ++ i) {
                MXMidiConsoleElement e = _list.elementAt(index[i]);
                MXMessage message = e.getMessage();
                if (message.isSysexOrMeta()) {
                    byte[] data = message.toOneMessage(0).getBinary();
                    _file.add(data, jTextArea1);
                    break;
                }
            }
            _file.bind(jTextArea1);
        }
    }//GEN-LAST:event_jButtonToFileActionPerformed

    private void jButtonSaveSysexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveSysexActionPerformed
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String extCheck = file.getName().toLowerCase();
            boolean isText = false;
            if (extCheck.endsWith(".txt")) {
                isText = true;
            }
            else  if (extCheck.endsWith(".sysex") || extCheck.endsWith("syx")) {
                isText = false;
            }
            else {
                int opt = JOptionPane.showConfirmDialog(this, "Save as Text?" , "SYSEX EXPORT", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    isText = true;
                }else {
                    isText = false;
                }
            }
            boolean success = false;
            try {
                if (isText) {
                    success = _file.writeText(file);
                }
                else {
                    success = _file.writeBinary(file);
                }
            }catch(IOException e) {
            }
            if (success) {
                JOptionPane.showMessageDialog(this, "Done", "Done Write " + file , JOptionPane.OK_OPTION);
            }else {
                JOptionPane.showMessageDialog(this, "Error", "Error", JOptionPane.OK_OPTION);
            }
        }  
    }//GEN-LAST:event_jButtonSaveSysexActionPerformed

    private void jButtonDumpSysexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDumpSysexActionPerformed
        Integer port  = _listPort.readComboBox(jComboBoxPort);
        if (port != null) {
            _file.sendSysexTo(port, (int pos, int total) -> {
                jProgressBar1.setMaximum(total);
                jProgressBar1.setValue(pos);
            }, (int)jSpinner1.getValue());
        }
    }//GEN-LAST:event_jButtonDumpSysexActionPerformed

    private void jButtonClearLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearLogActionPerformed
        _list.clear();
        jListScan.repaint();
    }//GEN-LAST:event_jButtonClearLogActionPerformed

    private void jButtonClearFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearFileActionPerformed
        _file.clear(jTextArea1);
    }//GEN-LAST:event_jButtonClearFileActionPerformed

    private void jListScanValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListScanValueChanged
        int index = jListScan.getSelectedIndex();
        if (index >= 0) {
            MXMidiConsoleElement seek =_list.elementAt(index);
            if (seek != null) {
                _list.setHIghlightOwner(seek.getMessage());
            }
        }
    }//GEN-LAST:event_jListScanValueChanged

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClearFile;
    private javax.swing.JButton jButtonClearLog;
    private javax.swing.JButton jButtonDumpSysex;
    private javax.swing.JButton jButtonLoadSysex;
    private javax.swing.JButton jButtonSaveSysex;
    private javax.swing.JButton jButtonToFile;
    private javax.swing.JComboBox<String> jComboBoxPort;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList<MXMidiConsoleElement> jListScan;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JToggleButton jToggleButton1;
    // End of variables declaration//GEN-END:variables
}
