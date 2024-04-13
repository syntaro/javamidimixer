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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderSingleClick;
import jp.synthtarou.midimixer.libs.swing.themes.ThemeManager;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGSlider extends javax.swing.JPanel implements MouseWheelListener {
    MX32MixerProcess _mixer;
    int _row, _column;
    boolean _stopFeedback = true;

    public MGStatus getStatus() {
        return _mixer.getStatus(MGStatus.TYPE_SLIDER, _row, _column);
    }
    
    public void setStatus(MGStatus status) {
        _mixer.setStatus(MGStatus.TYPE_SLIDER, _row, _column, status);
    }

    public MGSlider(MX32MixerProcess process, int row, int column) {
        _row = row;
        _column = column;
        _mixer = process;
        _stopFeedback = true;
        initComponents();

        updateUI();

        addMouseWheelListener(this);
        new MXAttachSliderSingleClick(jSliderValue);
        new MXAttachSliderLikeEclipse(jSliderValue);
        _stopFeedback = false;
    }
    
    public void updateUI() {
        if (_mixer== null) {
            return;
        }
        super.updateUI();
        MGStatus status = getStatus();
        if (status != null) {
            Color col = MXUtil.mixtureColor(Color.blue, 30, Color.pink, 70);
            if (ThemeManager.getInstance().isColorfulMetalTheme()) {        
                col = MXUtil.mixtureColor(Color.red, 30, Color.yellow, 70);
            }
            MXRangedValue value = null;
            if (status._base != null) 
            {
                value = status._base.getValue();
            }
            jLabelValue.setForeground(col);
            jLabelMin.setText(String.valueOf(value._min));
            jLabelMax.setText(String.valueOf(value._max));
            jSliderValue.setMinimum(value._min);
            jSliderValue.setMaximum(value._max);
            jSliderValue.setPaintLabels(true);
            jSliderValue.setValue(value._value);
            jLabelValue.setText(String.valueOf(value._value));
            if (status._name == null || status._name.length() == 0) {
                MXMessage message = status._base;
                jLabelName.setText(message.toStringMessageInfo(1));
            }else {
                jLabelName.setText(status._name);
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

        jSliderValue = new javax.swing.JSlider();
        jLabelName = new javax.swing.JLabel();
        jLabelValue = new javax.swing.JLabel();
        jLabelMax = new javax.swing.JLabel();
        jLabelMin = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jSliderValue.setOrientation(javax.swing.JSlider.VERTICAL);
        jSliderValue.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderValueStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSliderValue, gridBagConstraints);

        jLabelName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelName.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jLabelName, gridBagConstraints);

        jLabelValue.setFont(new java.awt.Font("Yu Gothic UI", 1, 12)); // NOI18N
        jLabelValue.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(jLabelValue, gridBagConstraints);

        jLabelMax.setFont(new java.awt.Font("Yu Gothic UI", 1, 12)); // NOI18N
        jLabelMax.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        add(jLabelMax, gridBagConstraints);

        jLabelMin.setFont(new java.awt.Font("Yu Gothic UI", 1, 12)); // NOI18N
        jLabelMin.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        add(jLabelMin, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jSliderValueStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderValueStateChanged
        int newValue = jSliderValue.getValue();
        jLabelValue.setText(String.valueOf(newValue));
        if (_stopFeedback) {
            return;
        }
        _mixer._parent.addSliderMove(null, getStatus(), newValue);
    }//GEN-LAST:event_jSliderValueStateChanged

    public void publishUI(MXRangedValue newValue) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(() -> {
                publishUI(newValue);
            });
            return;
        }
        _stopFeedback = true;
        jSliderValue.setValue(newValue._value);
        _stopFeedback = false;
    }

    public JLabel labelFor(int num, int max) {
        String name = "";
        if (max >= 256) {
            int msb = num / 128;
            int cut = msb * 128;
            int lsb = num - cut;
            name = MXUtil.toHexFF(msb) + ":" + MXUtil.toHexFF(lsb);
        }else {
            name = MXUtil.toHexFF(num);
        }
        JLabel label = new JLabel(name);
        //label.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        return label;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelMax;
    private javax.swing.JLabel jLabelMin;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelValue;
    private javax.swing.JSlider jSliderValue;
    // End of variables declaration//GEN-END:variables

    public void increment(MXMessage owner) {
        MGStatus status = getStatus();
        if (status.getValue().incrementable()) {
            MXRangedValue var = status.getValue().increment();
            _mixer._parent.addSliderMove(owner, status, var._value);
        }
    }

    public void decriment(MXMessage owner) {
        MGStatus status = getStatus();
        if (status.getValue().decrementable()) {
            MXRangedValue var = status.getValue().decrement();
            _mixer._parent.addSliderMove(owner, status, var._value);
        }
    }

    public void editContoller() {
        _mixer._view.stopEditing();
        MGStatus status = (MGStatus)getStatus().clone();
        MGStatusPanel panel = new MGStatusPanel(_mixer, getStatus());
        MXUtil.showAsDialog(this, panel, "Enter Edit Slider {row:" + _row + ", column:" + _column + "}");
        if (panel._okOption) {
            setStatus(panel._status);
            jLabelName.setText(panel._status._name);
            _mixer.notifyCacheBroken();
            updateUI();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int d = e.getUnitsToScroll();
        if (d > 0) {
            decriment(null);
        }else {
            increment(null);
        }
    }

}
