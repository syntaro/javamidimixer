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
package jp.synthtarou.mixtone.main.view;

import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.swing.CurvedSlider;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderSingleClick;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTMixerLine extends javax.swing.JPanel {
    private static final boolean _enableControl = false;

    /**
     * Creates new form XTMixerLine
     */
    public XTMixerLine() {
        initComponents();
        java.awt.GridBagConstraints gridBagConstraints;

        _panSlider = new CurvedSlider(30, _enableControl);
        _reverveSlider = new CurvedSlider(30, _enableControl);
        _chorusSlider = new CurvedSlider(30, _enableControl);
        _panSlider.setValue(new MXRangedValue(0, -64, 53));
        _reverveSlider.setValue(new MXRangedValue(0, 0, 127));
        _chorusSlider.setValue(new MXRangedValue(0, 0, 127));

        jLabelChannel = new javax.swing.JLabel();
        jLabelProgram = new javax.swing.JLabel();
        jSliderNoteCount = new javax.swing.JSlider();
        jSliderNoteCount.setMinimum(0);
        jSliderNoteCount.setMaximum(16);
        jSliderNoteCount.setValue(100);
        new MXAttachSliderLikeEclipse(jSliderNoteCount);
        if (_enableControl) {
           new MXAttachSliderSingleClick(jSliderNoteCount);
        }
        else {
            JComponent[] listDisable = {
                jSliderNoteCount,
            };
            for (JComponent seek : listDisable) {
                for (MouseListener l : seek.getMouseListeners()) {
                    seek.removeMouseListener(l);
                }
                for (MouseMotionListener l2 : seek.getMouseMotionListeners()) {
                    seek.removeMouseMotionListener(l2);
                }
            }
        }

        setLayout(new java.awt.GridBagLayout());

        jLabelChannel.setText("Ch 1");
        jLabelChannel.setPreferredSize(new Dimension(50, 20));
        jLabelChannel.setHorizontalAlignment(JLabel.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(jLabelChannel, gridBagConstraints);

        jLabelProgram.setText("Pg 45[00:ff]");
        jLabelProgram.setPreferredSize(new Dimension(100, 20));
        jLabelProgram.setHorizontalAlignment(JLabel.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(jLabelProgram, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        add(_panSlider, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jSliderNoteCount, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        add(_reverveSlider, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        add(_chorusSlider, gridBagConstraints);

        jLabelMute = new JLabel();
        jLabelMute.setText("[Play]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        add(jLabelMute, gridBagConstraints);
    }
    
    public void setChannel(int channel) {
        jLabelChannel.setText(String.valueOf(channel + 1) + " ch");
        if (channel == 9) {
            updateBank0(127);
        }
    }

    public void setProgram(int program, int bank0, int bank32) {
        jLabelProgram.setText("Pg " + String.valueOf(program) + "[" + Integer.toHexString(bank0) + ":" + Integer.toHexString(bank32) + "]");
    }
    
    int _currentProgram;
    int _currentBank0;
    int _currentBank32;
    
    public void updateProgram(int program) {
        _currentProgram = program;
        setProgram(_currentProgram, _currentBank0, _currentBank32);
    }

    public void updateBank0(int program) {
        _currentBank0 = program;
        setProgram(_currentProgram, _currentBank0, _currentBank32);
    }

    public void updateBank32(int program) {
        _currentBank32 = program;
        setProgram(_currentProgram, _currentBank0, _currentBank32);
    }
    
    CurvedSlider _panSlider;
    CurvedSlider _reverveSlider;
    CurvedSlider _chorusSlider;
    JLabel jLabelChannel;
    JLabel jLabelProgram;
    JLabel jLabelMute;
    JSlider jSliderNoteCount;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents

    class Note {
        public Note(int number) {
            _number = number;
        }
        int _number;
    }
    
    LinkedList<Note> _noteList = new LinkedList<>();
    
    public void updateCount(int count) {
        jSliderNoteCount.setValue(count < 16 ? count : 16);
    }
    
    public synchronized void push(OneMessage smf) {
        int status = smf.getStatus();
        int data1 = smf.getData1();
        int data2 = smf.getData2();
        
        int statusHi = status & 0xf0;
        if (statusHi == MXMidi.COMMAND_CH_NOTEON) {
            if (data2 == 0) {
                statusHi = MXMidi.COMMAND_CH_NOTEOFF;
            }
            else {
                _noteList.add(new Note(data1));
                updateCount(_noteList.size());
                return;
            }
        }
        if (statusHi == MXMidi.COMMAND_CH_NOTEOFF) {
            for (Iterator<Note> it = _noteList.iterator(); it.hasNext(); ) {
                Note t = it.next();
                if (t._number == data1) {
                    it.remove();
                    updateCount(_noteList.size());
                    return;
                }
            }
        }
        if (statusHi == MXMidi.COMMAND_CH_CONTROLCHANGE) {
            switch(data1) {
                case MXMidi.DATA1_CC_PANPOT:
                    _panSlider.setValue(MXRangedValue.new7bit(data2));
                    break;
                case MXMidi.DATA1_CC_EFFECT1_REVERVE:
                    _reverveSlider.setValue(MXRangedValue.new7bit(data2));
                    break;
                case MXMidi.DATA1_CC_EFFECT3_CHORUS:
                    _chorusSlider.setValue(MXRangedValue.new7bit(data2));
                    break;
                case MXMidi.DATA1_CC_BANKSELECT:
                    updateBank0(data2);
                    break;
                case MXMidi.DATA1_CC_BANKSELECT + 32:
                    updateBank32(data2);
                    break;
            }
        }
        if (statusHi == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
            updateProgram(data1);
        }
    }
    
    public void clear() {
        _noteList.clear();
        updateCount(0);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
