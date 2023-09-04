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
package jp.synthtarou.midimixer.libs.midi.capture;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JOptionPane;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;

/**
 *.
 * @author Syntarou YOSHIDA
 */
public class MXMessageCapturePanel extends javax.swing.JPanel {
    
    public GateInfomation _selected = null;
    MXMessageCapture _capture = null;

    WindowListener _adater = new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            MXMain.setCapture(null);
        }
    };

    MXWrapList<GateInfomation> _templateModel = null;
    Thread tick = null;
    
    public class TickThread extends Thread {
        long _startTick;
        int _age;
        
        public TickThread() {
            _startTick = System.currentTimeMillis();
        }
        
        public void run() {
            _age = -1;
            _capture = new MXMessageCapture();
            try {
                MXMain.setCapture(_capture);
                while(true) {                    
                    synchronized(_capture) {
                        try {
                            _capture.wait(1000);
                        }catch(InterruptedException e) {
                            return;
                        }
                    }
                    long spent = System.currentTimeMillis() - _startTick;
                    spent /= 1000;

                    if (spent >= 30) {
                        jLabel1.setText("Done Scan ... 30 / 30 sec");
                        break;
                    }else {
                        jLabel1.setText("Scanning All Input ... " + spent + " / 30 sec");
                    }
                    if (_age != _capture.getAge()) {
                        _age = _capture.age;
                        _templateModel = _capture.createListModel();
                        jList1.setModel(_templateModel);
                    }
                }
            }finally {
                MXMain.setCapture(null);
            }
        }
    }

    public MXMessageCapturePanel() {
        initComponents();
        startCapture();
        setPreferredSize(new Dimension(600, 400));
        startCapture();
    }
    
    public void stopCapture() {
        jToggleBuittonScan.setSelected(false);
        if (tick != null) {
            synchronized(tick) {
                tick.interrupt();
            }
            tick = null;
        }
    }
    
    public void startCapture() {
        jToggleBuittonScan.setSelected(true);
        if (tick != null) {
            synchronized(tick) {
                tick.interrupt();
            }
            tick = null;
            try {
                Thread.sleep(500);
            }catch(InterruptedException e) {
                
            }
        }
        tick = new TickThread();
        tick.start();
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
        jList1 = new javax.swing.JList<>();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jToggleBuittonScan = new javax.swing.JToggleButton();
        jTextFieldCommandText = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButtonOK, gridBagConstraints);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButtonCancel, gridBagConstraints);

        jLabel1.setText("Lets SCAN");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jLabel1, gridBagConstraints);

        jToggleBuittonScan.setText("Scan");
        jToggleBuittonScan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleBuittonScanActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jToggleBuittonScan, gridBagConstraints);

        jTextFieldCommandText.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jTextFieldCommandText, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        int x = jList1.getSelectedIndex();
    
        if (x >= 0) {
            MXWrap<GateInfomation> wrap = (MXWrap)_templateModel.get(x);
            _selected = wrap.value;

            MXUtil.getOwnerWindow(this).setVisible(false);
            MXMain.setCapture(null);
        }else {
            JOptionPane.showMessageDialog(this, "Choose 1 from List", "Please", JOptionPane.OK_OPTION);
        }
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        _selected = null;
        MXUtil.getOwnerWindow(this).setVisible(false);
        MXMain.setCapture(null);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jToggleBuittonScanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleBuittonScanActionPerformed
        if (jToggleBuittonScan.isSelected()) {
            startCapture();
        }else {
            stopCapture();
        }
    }//GEN-LAST:event_jToggleBuittonScanActionPerformed

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
        int x = jList1.getSelectedIndex();
    
        if (x >= 0) {
            try {
                MXWrap<GateInfomation> wrap = (MXWrap)_templateModel.get(x);
                int channel = wrap.value._parent.channel;
                String dtext = wrap.value._parent.dtext;
                int gate = wrap.value._gate;
                int lowvalue = wrap.value._hitLoValue;
                int hivalue = wrap.value._hitHiValue;

                MXTemplate template = MXMessageFactory.fromDtext(dtext, channel);
                MXMessage msg = template.buildMessage(0, channel, RangedValue.new7bit(gate), RangedValue.new7bit(hivalue));
                String text = msg.toStringHeader(lowvalue, hivalue);
                jTextFieldCommandText.setText(text);
            }catch(Exception e) {
                jTextFieldCommandText.setText("Error");
            }
        }
    }//GEN-LAST:event_jList1ValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldCommandText;
    private javax.swing.JToggleButton jToggleBuittonScan;
    // End of variables declaration//GEN-END:variables
}
