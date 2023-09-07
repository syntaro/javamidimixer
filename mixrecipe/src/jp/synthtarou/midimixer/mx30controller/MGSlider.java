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
package jp.synthtarou.midimixer.mx30controller;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.swing.MXModalFrame;
import jp.synthtarou.midimixer.libs.swing.focus.MXFocusAble;
import jp.synthtarou.midimixer.libs.swing.focus.MXFocusGroupElement;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderSingleClick;
import jp.synthtarou.midimixer.libs.swing.themes.ThemeManager;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGSlider extends javax.swing.JPanel implements MXFocusAble, MouseWheelListener {
    MX32MixerProcess _process;
    int _row, _column;

    public MGStatus getStatus() {
        if (_process == null) return null;
        return _process._data.getSliderStatus(_row, _column);
    }
    
    public void setStatus(MGStatus status) {
        System.out.println("slider 14bit " + status.isValuePairCC14());
        _process._data.setSliderStatus(_row, _column, status);
    }

    public MGSlider(MX32MixerProcess process, int row, int column) {
        _row = row;
        _column = column;
        _process = process;
        initComponents();

        updateUI();

        addMouseWheelListener(this);
        new MXAttachSliderSingleClick(jSliderValue);
        new MXAttachSliderLikeEclipse(jSliderValue);
    }
    
    public void updateUI() {
        super.updateUI();
        MGStatus status = getStatus();
        if (status != null) {
            Color col = MXUtil.mixedColor(Color.blue, Color.pink, 30);
            if (ThemeManager.getInstance().isColorfulMetalTheme()) {        
                col = MXUtil.mixedColor(Color.red, Color.yellow, 30);
            }
            RangedValue value = status.getValue();
            jLabelValue.setForeground(col);
            jLabelMin.setText(String.valueOf(value._min));
            jLabelMax.setText(String.valueOf(value._max));
            jSliderValue.setMinimum(value._min);
            jSliderValue.setMaximum(value._max);
            jSliderValue.setPaintLabels(true);
            jSliderValue.setValue(value._var);
            jLabelValue.setText(String.valueOf(value._var));
            if (status.getName() == null || status.getName().length() == 0) {
                MXMessage message = status.toMXMessage(null);
                if (message == null) {
                    jLabelName.setText("?");
                }else {
                    jLabelName.setText(message.toShortString());
                }
            }else {
                jLabelName.setText(status.getName());
            }
            focusStatusChanged(false);
        }
    }

    ArrayList<MouseListener> backupedListener = new ArrayList();
    
    public void setValueChangeable(boolean usable) {
        if (usable == false) {            
            for (MouseListener l : jSliderValue.getMouseListeners()) {
                if (l instanceof MXFocusGroupElement.Listen) {
                    continue;
                }else {
                    backupedListener.add(l);
                }
            }
            for (MouseListener l : backupedListener) {
                jSliderValue.removeMouseListener(l);
            }
        }else {
            for (MouseListener l : backupedListener) {
                jSliderValue.addMouseListener(l);
            }
            backupedListener.clear();
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

    boolean _ignoreEvent = false;
    
    private void jSliderValueStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderValueStateChanged
        int newValue = jSliderValue.getValue();
        if (getStatus().getValue()._var == newValue) {
            return;
        }
        jLabelValue.setText(String.valueOf(newValue));
        if (_ignoreEvent) {
            return;
        }
        _process.controlByUI(getStatus(), newValue);        
    }//GEN-LAST:event_jSliderValueStateChanged

    public void updateUIByStatus() {
        MGStatus status = getStatus();
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateUIByStatus();
                }
            });
            return;
        }
        int newValue = status.getValue()._var;
        _ignoreEvent = true;
        jLabelValue.setText(String.valueOf(newValue));
        jSliderValue.setValue(newValue);
        _ignoreEvent = false;
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

    @Override
    public void focusStatusChanged(boolean flag) {
        if (flag) {
            _process._parent.showTextForFocus(MGStatus.TYPE_SLIDER,  _process._port, _row, _column);
        }
    }

    @Override
    public void focusedMousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e) || _process._parent._editingControl) {
            _process._parent.enterEditMode(false);
            focusStatusChanged(false);
            editContoller();
        }
    }

    @Override
    public void focusChangingValue() {
        _process._parent.showTextForFocus(MGStatus.TYPE_SLIDER,  _process._port, _row, _column);
    }

    public void increment() {
        MGStatus status = getStatus();
        RangedValue var = status.getValue().increment();
        if (var != null) {
            _process.controlByUI(status, var._var);
        }
    }
    
    public void decriment() {
        MGStatus status = getStatus();
        RangedValue var = status.getValue().decrement();
        if (var != null) {
            _process.controlByUI(status, var._var);
        }
    }
    
    public void doHomePosition() {
        final MGStatus status = getStatus();
        final int current = status.getValue()._var;
        final int value = status.getHomePosition();
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < 5; ++ i) {
                        int x = current * (5 - i) + value * i;
                        x /= 5;
                        Thread.sleep(70);
                        status.updateValue(x);
                        updateUIByStatus();
                        if (x == value) { 
                            break;
                        }
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }finally {
                    status.updateValue(value);
                    updateUIByStatus();
                }
            }
        });
        t.start();
    }

    public void editContoller() {
        _process._parent.enterEditMode(false);
        MGStatus status = (MGStatus)getStatus().clone();
        MGStatusConfig config = new MGStatusConfig(_process, status);
        MXModalFrame.showAsDialog(this, config, "Enter Edit Slider {row:" + _row + ", column:" + _column + "}");
        if (config._okOption) {
            setStatus(config._status);
            jLabelName.setText(config._status.getName());
            _process.notifyCacheBroken();
            updateUI();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int d = e.getUnitsToScroll();
        if (d > 0) {
            this.decriment();
        }else {
            this.increment();
        }
    }
}
