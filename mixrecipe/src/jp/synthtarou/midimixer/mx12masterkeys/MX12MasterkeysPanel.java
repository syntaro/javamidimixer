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
package jp.synthtarou.midimixer.mx12masterkeys;

import java.awt.Container;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachLabelSeemsButton;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Hashtable;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapListFactory;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.swing.MXSwingPiano;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderSingleClick;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX12MasterkeysPanel extends javax.swing.JPanel {
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

    MX12Process _process;
    MXSwingPiano _piano;
    MXWrapList<Integer> _watchPort = MXWrapListFactory.listupPort(null);
    MXWrapList<Integer> _watchChannel = MXWrapListFactory.listupChannel(null);
    
    int _valuePitch = -1;
    int _valueModulation = -1;
    boolean _beforeBuild = true;
    
    /**
     * Creates new form MX12MasterkeysPanel
     */
    public MX12MasterkeysPanel(MX12Process process) {
        initComponents();
        _process = process;
       
        jSliderPitch.setMinimum(0);
        jSliderPitch.setMaximum(16384 - 1);
        jSliderPitch.setValue(8192);
        new MXAttachSliderSingleClick(jSliderPitch);
        jSliderPitch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                setPitchBend(8192);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                setPitchBend(8192);
            }
        });
        
        jSliderPitch.setMajorTickSpacing(2048);
        jSliderPitch.setMinorTickSpacing(512);
        jSliderPitch.setPaintTicks(true);
        
        Hashtable table = new Hashtable();
        table.put(0, new JLabel("0"));
        table.put(8192, new JLabel("8192"));
        table.put(16383, new JLabel("16383"));
        jSliderPitch.setLabelTable(table);
        jSliderPitch.setPaintLabels(true);
        
        jSliderModwheel.setMinimum(0);
        jSliderModwheel.setMaximum(127);
        jSliderModwheel.setValue(0);
        
        jSliderModwheel.setMajorTickSpacing(32);
        jSliderModwheel.setPaintTicks(true);
        
        Hashtable table2 = new Hashtable();
        table2.put(0, new JLabel("0"));
        table2.put(127, new JLabel("127"));
        jSliderModwheel.setLabelTable(table2);
        jSliderModwheel.setPaintLabels(true);

        _piano = new MXSwingPiano();
        
        _piano.setNoteRange(0, 11);
        _piano.setMinimumSize(new Dimension(9 * 200, 1));
        _piano.setPreferredSize(new Dimension(9 * 200, 120));

        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setViewportView(_piano);

        _piano.setHandler(new MXSwingPiano.Handler() {
            public void noteOn(int note) {
                MXMessage message = MXMessageFactory.fromShortMessage(_process.getMousePort(), MXMidi.COMMAND_CH_NOTEON + _process.getMouseChannel(), note, _process.getMouseVelocity());
                _process.mouseMessage(message);
            }

            public void noteOff(int note) {
                MXMessage message = MXMessageFactory.fromShortMessage(_process.getMousePort(), MXMidi.COMMAND_CH_NOTEOFF + _process.getMouseChannel(), note, 0);
                _process.mouseMessage(message);
            }

            @Override
            public void selectionChanged() {
            }
        });

        final String title = "Select Output to Connect.";
        new MXAttachSliderSingleClick(jSliderModwheel);
        new MXAttachLabelSeemsButton(jLabelEdit, new Runnable() {
            public void run() {
                Window w = SwingUtilities.getWindowAncestor(MX12MasterkeysPanel.this);
                JFrame frame = MXMain.getMain().getMainWindow();
                if (w instanceof JFrame) {
                    frame = (JFrame)w;
                }
                MX12MasterPanelEditor.showAsDialog(frame, _process, true);
                updateViewForSettingChange();
            }
        });

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateViewForSettingChange();
            }
        });
        _beforeBuild = false;
    }
    
    public void scrollToCenter() {
        Dimension scrollSize = jScrollPane1.getSize();
        Dimension pianoSize = _piano.getSize();
        Rectangle rect = new Rectangle((int)(pianoSize.getWidth()-scrollSize.getWidth()) / 2, 0, (int)scrollSize.getWidth(), 50);
        jScrollPane1.getViewport().scrollRectToVisible(rect);
    }
    
    public void updateViewForSettingChange() {
        StringBuffer info = new StringBuffer();
        info.append(_process.getReceiverName() +", ");
        info.append("Port " + MXMidi.nameOfPortInput(_process.getMousePort()) + ", ");
        info.append("Channel " + (_process.getMouseChannel() + 1));
        jLabelInfo1.setText(info.toString());
        info = new StringBuffer();
        if (_process.isAcceptThisPageSignal()) {
            info.append("Process This page's Signal ");
            if (_process.isAcceptInputPanelSignal()) {
                info.append(", And ");
            }
        }else {
            if (_process.isAcceptInputPanelSignal()) {
                info.append("And Process ");
            }
        }
        if (_process.isAcceptInputPanelSignal()) {
            info.append("Input Panel's Signal ");
            if (_process.isOverwriteInputChannel()) {
                info.append("( With Re-Adjust Ch/Port ) ");
            }
        }
        jLabelInfo2.setText(info.toString());
    }
    
    boolean _updateLock = false;
    
    public void setPitchBend(int value) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setPitchBend(value);
                }
            });
            return;
        }
        if (_valuePitch != value) {
            _valuePitch = value;
            _updateLock = true;
            jSliderPitch.setValue(value);
            _updateLock = false;
         }
    }
    
    public void setModulatoinWheel(int value) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setModulatoinWheel(value);
                }
            });
            return;
        }
        if (_valueModulation != value) {
            _valueModulation = value;
            _updateLock = true;
            jSliderModwheel.setValue(value);
            _updateLock = false;
        }
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
        jSliderPitch = new javax.swing.JSlider();
        jSliderModwheel = new javax.swing.JSlider();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanelOutput = new javax.swing.JPanel();
        jLabelEdit = new javax.swing.JLabel();
        jLabelInfo1 = new javax.swing.JLabel();
        jLabelInfo2 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jSliderPitch.setOrientation(javax.swing.JSlider.VERTICAL);
        jSliderPitch.setToolTipText("Pitch");
        jSliderPitch.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jSliderPitch.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderPitchStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jSliderPitch, gridBagConstraints);

        jSliderModwheel.setOrientation(javax.swing.JSlider.VERTICAL);
        jSliderModwheel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jSliderModwheel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderModwheelStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jSliderModwheel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jPanelOutput.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));
        jPanelOutput.setLayout(new java.awt.GridBagLayout());

        jLabelEdit.setText("Config");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelOutput.add(jLabelEdit, gridBagConstraints);

        jLabelInfo1.setText("Info1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelOutput.add(jLabelInfo1, gridBagConstraints);

        jLabelInfo2.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelOutput.add(jLabelInfo2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jPanelOutput, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jSliderModwheelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderModwheelStateChanged
        if (_beforeBuild) {
            return;
        }
        if (_updateLock) {
            return;
        }
        int value = jSliderModwheel.getValue();
        _valueModulation = value;
        synchronized(MXTiming.mutex) {            
            MXMessage msg = MXMessageFactory.fromShortMessage(_process.getMousePort(), MXMidi.COMMAND_CH_CONTROLCHANGE + _process.getMouseChannel(), MXMidi.DATA1_CC_MODULATION, 0);
            msg.setValue(MXRangedValue.new7bit(value));
            _process.mouseMessage(msg);
        }

    }//GEN-LAST:event_jSliderModwheelStateChanged

    private void jSliderPitchStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderPitchStateChanged
        if (_beforeBuild) {
            return;
        }
        if (_updateLock) {
            return;
        }
        int value = jSliderPitch.getValue();
        _valuePitch = value;
        synchronized(MXTiming.mutex) {            
            MXMessage msg = MXMessageFactory.fromShortMessage(_process.getMousePort(), MXMidi.COMMAND_CH_PITCHWHEEL + _process.getMouseChannel(), 0, 0);
            msg.setValue(MXRangedValue.new7bit(value));
            _process.mouseMessage(msg);
        }
    }//GEN-LAST:event_jSliderPitchStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel jLabelEdit;
    private javax.swing.JLabel jLabelInfo1;
    private javax.swing.JLabel jLabelInfo2;
    private javax.swing.JPanel jPanelOutput;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSliderModwheel;
    private javax.swing.JSlider jSliderPitch;
    // End of variables declaration//GEN-END:variables

    public boolean isOwnerwindowVisible() {
        try {
            Container cont = MXUtil.getOwnerWindow(this);
            if (cont == null) {
                //initializing
                return true;
            }
            if (cont.isVisible()) {
                return true;
            }
            return false;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public void showAsWindow() {
        JFrame newFrame = new JFrame();
        newFrame.setTitle("Master Keys (" + MXAppConfig.MX_APPNAME + ")");
        //dialog.setAlwaysOnTop(modal ? true : false);
        newFrame.pack();
        newFrame.getContentPane().add(this);
        setPreferredSize(new Dimension(1000, 250));
        newFrame.pack();
        MXUtil.centerWindow(newFrame);
        newFrame.setVisible(true);
        scrollToCenter();
    }
}
