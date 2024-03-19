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

import jp.synthtarou.midimixer.libs.midi.console.MXMidiConsoleElement;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.MXGlobalTimer;
import jp.synthtarou.midimixer.libs.common.MXLogger2;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachCopyAndPaste;
import jp.synthtarou.midimixer.mx50resolution.MX50Process;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX70View extends javax.swing.JPanel {

    final MX70Process _process;

    /**
     * Creates new form MX70Panel
     */
    public MX70View(MX70Process process) {
        _process = process;
        initComponents();
        Dimension size = getSize();
        size.width /= 4;
        size.width -= 4;
        size.height -= 4;

        jScrollPane2.setPreferredSize(size);
        jScrollPane3.setPreferredSize(size);
        jScrollPane4.setPreferredSize(size);
        jScrollPane5.setPreferredSize(size);
        
        new MXAttachCopyAndPaste(jTextFieldDump);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                _process._outsideInput.bind(jListOutsideInput);
                _process._insideInput.bind(jListInsideInput);
                _process._insideOutput.bind(jListInsideOutput);
                _process._outsideOutput.bind(jListOutsideOutput);
                showTimeSpend();
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

        jPanelOutsideInput = new javax.swing.JPanel();
        jLabelOutsideInput = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListOutsideInput = new javax.swing.JList<>();
        jPanelInsideInput = new javax.swing.JPanel();
        jLabelInsideInput = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListInsideInput = new javax.swing.JList<>();
        jPanelInsideOutput = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jListInsideOutput = new javax.swing.JList<>();
        jLabelInsideOutput = new javax.swing.JLabel();
        jPanelOutsideOutput = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jListOutsideOutput = new javax.swing.JList<>();
        jLabelOutsideOutput = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jButtonSysEXMenu = new javax.swing.JButton();
        jCheckBoxLogging = new javax.swing.JCheckBox();
        jCheckBoxRecordClock = new javax.swing.JCheckBox();
        jLabelMemory = new javax.swing.JLabel();
        jTextFieldDump = new javax.swing.JTextField();
        jButtonClearLog = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jPanelOutsideInput.setBorder(javax.swing.BorderFactory.createTitledBorder("Outside Input"));
        jPanelOutsideInput.setLayout(new java.awt.GridBagLayout());

        jLabelOutsideInput.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanelOutsideInput.add(jLabelOutsideInput, gridBagConstraints);

        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jListOutsideInput.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jListOutsideInputMousePressed(evt);
            }
        });
        jListOutsideInput.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListOutsideInputValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jListOutsideInput);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.weighty = 2.0;
        jPanelOutsideInput.add(jScrollPane2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelOutsideInput, gridBagConstraints);

        jPanelInsideInput.setBorder(javax.swing.BorderFactory.createTitledBorder("Inide Input"));
        jPanelInsideInput.setLayout(new java.awt.GridBagLayout());

        jLabelInsideInput.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanelInsideInput.add(jLabelInsideInput, gridBagConstraints);

        jScrollPane3.setToolTipText("");
        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jListInsideInput.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jListInsideInputMousePressed(evt);
            }
        });
        jListInsideInput.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListInsideInputValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jListInsideInput);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.weighty = 2.0;
        jPanelInsideInput.add(jScrollPane3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelInsideInput, gridBagConstraints);

        jPanelInsideOutput.setBorder(javax.swing.BorderFactory.createTitledBorder("Inside Output"));
        jPanelInsideOutput.setLayout(new java.awt.GridBagLayout());

        jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jListInsideOutput.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jListInsideOutputMousePressed(evt);
            }
        });
        jListInsideOutput.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListInsideOutputValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(jListInsideOutput);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.weighty = 2.0;
        jPanelInsideOutput.add(jScrollPane4, gridBagConstraints);

        jLabelInsideOutput.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        jPanelInsideOutput.add(jLabelInsideOutput, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelInsideOutput, gridBagConstraints);

        jPanelOutsideOutput.setBorder(javax.swing.BorderFactory.createTitledBorder("Outsde Output"));
        jPanelOutsideOutput.setLayout(new java.awt.GridBagLayout());

        jScrollPane5.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jListOutsideOutput.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jListOutsideOutputMousePressed(evt);
            }
        });
        jListOutsideOutput.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListOutsideOutputValueChanged(evt);
            }
        });
        jScrollPane5.setViewportView(jListOutsideOutput);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.weighty = 2.0;
        jPanelOutsideOutput.add(jScrollPane5, gridBagConstraints);

        jLabelOutsideOutput.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        jPanelOutsideOutput.add(jLabelOutsideOutput, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelOutsideOutput, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jButtonSysEXMenu.setText("*Launch SysEx Tool*");
        jButtonSysEXMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSysEXMenuActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel4.add(jButtonSysEXMenu, gridBagConstraints);

        jCheckBoxLogging.setSelected(true);
        jCheckBoxLogging.setText("Logging / Pause");
        jCheckBoxLogging.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxLoggingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel4.add(jCheckBoxLogging, gridBagConstraints);

        jCheckBoxRecordClock.setText("Clock Record/Skip");
        jCheckBoxRecordClock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxRecordClockActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        jPanel4.add(jCheckBoxRecordClock, gridBagConstraints);

        jLabelMemory.setText("Memory");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel4.add(jLabelMemory, gridBagConstraints);

        jTextFieldDump.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(jTextFieldDump, gridBagConstraints);

        jButtonClearLog.setText("Clear");
        jButtonClearLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearLogActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        jPanel4.add(jButtonClearLog, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel4, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    public void selectByTimingCall(JList list, MXTiming trace) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                selectByTiming(list, trace);
            }
        });
    }

    public void selectByTiming(JList list, MXTiming trace) {
        _process._outsideInput.setSelectedTiming(trace);
        _process._insideInput.setSelectedTiming(trace);
        _process._insideOutput.setSelectedTiming(trace);
        _process._outsideOutput.setSelectedTiming(trace);
    }

    private void jListOutsideInputValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListOutsideInputValueChanged
        int index = jListOutsideInput.getSelectedIndex();
        if (index >= 0) {
            MXMidiConsoleElement e = _process._outsideInput.getConsoleElement(index);
            if (e != null) {
                selectByTimingCall(jListOutsideInput, e.getTiming());
                jTextFieldDump.setText("OutsideInput " + e.getTiming().thisWrap(0) + ": " + e.formatMessageLong());
            } else {
                selectByTimingCall(jListOutsideInput, null);
            }
        }
    }//GEN-LAST:event_jListOutsideInputValueChanged

    private void jListInsideInputValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListInsideInputValueChanged
        int index = jListInsideInput.getSelectedIndex();
        if (index >= 0) {
            MXMidiConsoleElement e = _process._insideInput.getConsoleElement(index);
            if (e != null) {
                selectByTimingCall(jListInsideInput, e.getTiming());
                jTextFieldDump.setText("InsideInput " + e.getTiming().thisWrap(1) + ": " + e.formatMessageLong());
            } else {
                selectByTimingCall(jListInsideInput, null);
            }
        }
    }//GEN-LAST:event_jListInsideInputValueChanged

    private void jListInsideOutputValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListInsideOutputValueChanged
        int index = jListInsideOutput.getSelectedIndex();
        if (index >= 0) {
            MXMidiConsoleElement e = _process._insideOutput.getConsoleElement(index);
            if (e != null) {
                selectByTimingCall(jListInsideOutput, e.getTiming());
                jTextFieldDump.setText("InsideOutput " + e.getTiming().thisWrap(2) + ": " + e.formatMessageLong());
            } else {
                selectByTimingCall(jListInsideOutput, null);
            }
        }
    }//GEN-LAST:event_jListInsideOutputValueChanged

    private void jListOutsideOutputValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListOutsideOutputValueChanged
        int index = jListOutsideOutput.getSelectedIndex();
        if (index >= 0) {
            MXMidiConsoleElement e = _process._outsideOutput.getConsoleElement(index);
            if (e != null) {
                selectByTimingCall(jListOutsideOutput, e.getTiming());
                jTextFieldDump.setText("OutsideOutput " + e.getTiming().thisWrap(3) + ": " + e.formatMessageLong());
            } else {
                selectByTimingCall(jListOutsideOutput, null);
            }
        }
    }//GEN-LAST:event_jListOutsideOutputValueChanged

    private void jListOutsideInputMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListOutsideInputMousePressed
        int index = jListOutsideInput.getSelectedIndex();
        if (index >= 0) {
            MXMidiConsoleElement e = _process._outsideInput.getConsoleElement(index);
            if (e != null) {
                selectByTimingCall(jListOutsideInput, e.getTiming());
            } else {
                selectByTimingCall(jListOutsideInput, null);
            }
        }
    }//GEN-LAST:event_jListOutsideInputMousePressed

    private void jListInsideInputMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListInsideInputMousePressed
        int index = jListInsideInput.getSelectedIndex();
        if (index >= 0) {
            MXMidiConsoleElement e = _process._insideInput.getConsoleElement(index);
            if (e != null) {
                selectByTimingCall(jListInsideInput, e.getTiming());
            } else {
                selectByTimingCall(jListInsideInput, null);
            }
        }
    }//GEN-LAST:event_jListInsideInputMousePressed

    private void jListInsideOutputMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListInsideOutputMousePressed
        int index = jListInsideOutput.getSelectedIndex();
        if (index >= 0) {
            MXMidiConsoleElement e = _process._insideOutput.getConsoleElement(index);
            if (e != null) {
                selectByTimingCall(jListInsideOutput, e.getTiming());
            } else {
                selectByTimingCall(jListInsideOutput, null);
            }
        }
    }//GEN-LAST:event_jListInsideOutputMousePressed

    private void jListOutsideOutputMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListOutsideOutputMousePressed
        int index = jListOutsideOutput.getSelectedIndex();
        if (index >= 0) {
            MXMidiConsoleElement e = _process._outsideOutput.getConsoleElement(index);
            if (e != null) {
                selectByTimingCall(jListOutsideOutput, e.getTiming());
            } else {
                selectByTimingCall(jListOutsideOutput, null);
            }
        }
    }//GEN-LAST:event_jListOutsideOutputMousePressed

    private void jButtonSysEXMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSysEXMenuActionPerformed
        MX70SysexPanel panel = _process.createSysexPanel();
        MXUtil.showAsDialog(this, panel, "System Exclusive");
    }//GEN-LAST:event_jButtonSysEXMenuActionPerformed

    private void jCheckBoxLoggingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxLoggingActionPerformed
        boolean pause = jCheckBoxLogging.isSelected() == false;
        _process._outsideInput.switchPause(pause);
        _process._insideInput.switchPause(pause);
        _process._insideOutput.switchPause(pause);
        _process._outsideOutput.switchPause(pause);
    }//GEN-LAST:event_jCheckBoxLoggingActionPerformed

    private void jCheckBoxRecordClockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxRecordClockActionPerformed
        boolean showclock = jCheckBoxRecordClock.isSelected();
        _process._outsideInput.setRecordClock(showclock);
        _process._insideInput.setRecordClock(showclock);
        _process._insideOutput.setRecordClock(showclock);
        _process._outsideOutput.setRecordClock(showclock);
    }//GEN-LAST:event_jCheckBoxRecordClockActionPerformed

    private void jButtonClearLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearLogActionPerformed
        _process._outsideInput.clear();
        _process._insideInput.clear();
        _process._insideOutput.clear();
        _process._outsideOutput.clear();
    }//GEN-LAST:event_jButtonClearLogActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClearLog;
    private javax.swing.JButton jButtonSysEXMenu;
    private javax.swing.JCheckBox jCheckBoxLogging;
    private javax.swing.JCheckBox jCheckBoxRecordClock;
    private javax.swing.JLabel jLabelInsideInput;
    private javax.swing.JLabel jLabelInsideOutput;
    private javax.swing.JLabel jLabelMemory;
    private javax.swing.JLabel jLabelOutsideInput;
    private javax.swing.JLabel jLabelOutsideOutput;
    private javax.swing.JList<String> jListInsideInput;
    private javax.swing.JList<String> jListInsideOutput;
    private javax.swing.JList<String> jListOutsideInput;
    private javax.swing.JList<String> jListOutsideOutput;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelInsideInput;
    private javax.swing.JPanel jPanelInsideOutput;
    private javax.swing.JPanel jPanelOutsideInput;
    private javax.swing.JPanel jPanelOutsideOutput;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTextField jTextFieldDump;
    // End of variables declaration//GEN-END:variables

    DecimalFormat format_mem = new DecimalFormat("#,###MB");
    DecimalFormat format_ratio = new DecimalFormat("##.#");

    public String getMemoryInfo() {
        long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        long used = total - free;
        double ratio = (used * 100 / (double) total);

        return " Memory: " + format_mem.format(used) + "/" + format_mem.format(total) + " (" + format_ratio.format(ratio) + "%)";
    }
    
    public boolean isOwnerWindowVisible() {
        Container cont = MXUtil.getOwnerWindow(this);
        if (cont == null) {
            return false;
        }
        return cont.isVisible();
    }

    NumberFormat formatter3 = new DecimalFormat("0.000");

    public void showTimeSpend() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 4; ++i) {
                        long count = MXTiming.totalCount(i);
                        long spend = MXTiming.totalWrap(i);
                        long bottom = MXTiming.totalBottom(i);

                        String div = formatter3.format(1.0 * spend / count);

                        String text = "<html>" + spend + "ms/" + count + "=" + div + "<br> bottom" + bottom + "ms</html>";
                        switch (i) {
                            case 0:
                                jLabelOutsideInput.setText(text);
                                break;
                            case 1:
                                jLabelInsideInput.setText(text);
                                break;
                            case 2:
                                jLabelInsideOutput.setText(text);
                                break;
                            case 3:
                                jLabelOutsideOutput.setText(text);
                                break;
                        }
                    }
                    jLabelMemory.setText(getMemoryInfo());
                } catch (RuntimeException ex) {
                    MXLogger2.getLogger(MX70View.class).log(Level.WARNING, ex.getMessage(), ex);
                }
                Container parent = getParent();
                while (parent != null) {
                    if (parent instanceof Window) {
                        Window w = (Window) parent;
                        if (w.isVisible() == false) {
                            return;
                        }
                    }
                    if (parent instanceof Dialog) {
                        Dialog d = (Dialog) parent;
                        if (d.isVisible() == false) {
                            return;
                        }
                    }
                    parent = parent.getParent();
                }
                MXGlobalTimer.letsCountdown(1000, new Runnable() {
                    public void run() {
                        showTimeSpend();
                    }
                });
            }
        });
    }

    public void showAsWindow() {
        JFrame newFrame = new JFrame();
        newFrame.setTitle("Free Consone / SysEX(" + MXAppConfig.MX_APPNAME + ")");
        //dialog.setAlwaysOnTop(modal ? true : false);
        newFrame.pack();
        newFrame.getContentPane().add(this);
        setPreferredSize(new Dimension(800, 600));
        newFrame.pack();
        MXUtil.centerWindow(newFrame);
        newFrame.setVisible(true);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (_process._outsideInput.getSize() >= 1) {
                    jListOutsideInput.ensureIndexIsVisible(_process._outsideInput.getSize() - 1);
                }
                if (_process._insideInput.getSize() >= 1) {
                    jListInsideInput.ensureIndexIsVisible(_process._insideInput.getSize()- 1);
                }
                if (_process._insideOutput.getSize() >= 1) {
                    jListInsideOutput.ensureIndexIsVisible(_process._insideOutput.getSize() - 1);
                }
                if (_process._outsideOutput.getSize() >= 1) {
                    jListOutsideOutput.ensureIndexIsVisible(_process._outsideOutput.getSize()-1);
                }
            }
        });
    }
}