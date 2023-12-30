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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.accordion.MXAccordionElement;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapListFactory;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.swing.MXSwingPiano;
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
    
    JButton _jButtonEdit = new JButton("Edit");
    MX12Process _process;
    MXSwingPiano _piano;
    MXWrapList<Integer> _watchPort = MXWrapListFactory.listupPort(null);
    MXWrapList<Integer> _watchChannel = MXWrapListFactory.listupChannel(null);
    
    int _sentPitch = 8192;
    int _sentModulation = 0;
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

        new MXAttachSliderLikeEclipse(jSliderPitch);
        new MXAttachSliderLikeEclipse(jSliderModwheel);
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

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateViewForSettingChange();
            }
        });
        _beforeBuild = false;
        _jButtonEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = MXMain.getMain().getMainWindow();
                MX12MasterPanelEditor.showAsDialog(frame, _process, true);
                updateViewForSettingChange();
            }
        });
        
        setMinimumSize(new Dimension(150, 150));
    }
    
    public void scrollToCenter() {
        Dimension scrollSize = jScrollPane1.getSize();
        Dimension pianoSize = _piano.getSize();
        Rectangle rect = new Rectangle((int)(pianoSize.getWidth()-scrollSize.getWidth()) / 2, 0, (int)scrollSize.getWidth(), 50);
        jScrollPane1.getViewport().scrollRectToVisible(rect);
    }
    
    public void updateViewForSettingChange() {
        StringBuffer info = new StringBuffer();
        MXReceiver rec = MXMain.getMain().getActiveSendableReceiver();
        if (rec != null) {
            info.append("To " + rec.getReceiverName() +", ");
            info.append("Port " + MXMidi.nameOfPortInput(_process.getMousePort()) + ", ");
            info.append("Channel " + (_process.getMouseChannel() + 1));
            /*
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
            }*/
            _jButtonEdit.setText(info.toString());
        }
    }
    
    public void setPitchBend(int value) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setPitchBend(value);
                }
            });
            return;
        }
        if (_sentPitch != value) {
            _sentPitch = value;
            jSliderPitch.setValue(value);
            synchronized(MXTiming.mutex) {            
                MXMessage msg = MXMessageFactory.fromShortMessage(_process.getMousePort(), MXMidi.COMMAND_CH_PITCHWHEEL + _process.getMouseChannel(), 0, 0);
                if (msg.indexOfValueHi() >= 0) {
                    msg.setValue(MXRangedValue.new14bit(value));
                }
                else {
                    msg.setValue(MXRangedValue.new7bit(value));
                }
                _process.mouseMessage(msg);
            }         
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
        if (_sentModulation != value) {
            _sentModulation = value;
            jSliderModwheel.setValue(value);

            _sentModulation = value;
            synchronized(MXTiming.mutex) {            
                MXMessage msg = MXMessageFactory.fromShortMessage(_process.getMousePort(), MXMidi.COMMAND_CH_CONTROLCHANGE + _process.getMouseChannel(), MXMidi.DATA1_CC_MODULATION, 0);
                msg.setValue(MXRangedValue.new7bit(value));
                _process.mouseMessage(msg);
            }
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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 18, 0);
        add(jSliderPitch, gridBagConstraints);

        jSliderModwheel.setOrientation(javax.swing.JSlider.VERTICAL);
        jSliderModwheel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jSliderModwheel.setInverted(true);
        jSliderModwheel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderModwheelStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 18, 0);
        add(jSliderModwheel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jSliderModwheelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderModwheelStateChanged
        if (_beforeBuild) {
            return;
        }
        int value = jSliderModwheel.getValue();
        setModulatoinWheel(value);
    }//GEN-LAST:event_jSliderModwheelStateChanged

    private void jSliderPitchStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderPitchStateChanged
        if (_beforeBuild) {
            return;
        }
        int value = jSliderPitch.getValue();
        setPitchBend(value);
    }//GEN-LAST:event_jSliderPitchStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSliderModwheel;
    private javax.swing.JSlider jSliderPitch;
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
        return _jButtonEdit;
    }
}
