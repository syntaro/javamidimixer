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

import jp.synthtarou.midimixer.libs.swing.CurvedSlider;
import java.awt.Color;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.swing.MXModalFrame;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGCircle extends javax.swing.JPanel implements MouseWheelListener {

    MX32Mixer _mixer;
    int _row, _column;
    Color foreground = null;
    boolean _disconnectMoment = false;

    public MGStatus getStatus() {
        if (_mixer == null) {
            return null;
        }
        return _mixer.getStatus(MGStatus.TYPE_CIRCLE, _row, _column);
    }

    public void setStatus(MGStatus status) {
        _mixer.setStatus(MGStatus.TYPE_CIRCLE, _row, _column, status);
    }

    public MGCircle(MX32Mixer process, int row, int column) {
        _row = row;
        _column = column;
        _mixer = process;

        initComponents();

        updateUI();
        addMouseWheelListener(this);
    }

    public void updateUI() {
        MGStatus status = getStatus();
        if (status != null) {
            jCircleValue.setValue(status.getValue());

            if (status._name == null || status._name.length() == 0) {
                MXMessage message = status._base;
                if (message == null) {
                    jLabel1.setText("?");
                } else {
                    jLabel1.setText(message.toStringForUI());
                }
            } else {
                jLabel1.setText(status._name);
            }
        }
        super.updateUI();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jCircleValue = new CurvedSlider(45);
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        //jCircleValue.setOrientation(javax.swing.JSlider.VERTICAL);
        jCircleValue.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCircleValueStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jCircleValue, gridBagConstraints);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
    }// </editor-fold>                        

    boolean _ignoreEvent = false;

    private void jCircleValueStateChanged(javax.swing.event.ChangeEvent evt) {
        int newValue = jCircleValue.getValue();
        if (getStatus().getValue()._var == newValue) {
            return;
        }
        if (_ignoreEvent) {
            return;
        }
        _mixer.updateStatusAndSend(getStatus(), newValue, null);
    }

    MXTiming _trackNumer;

    public void publishUI() {
        MGStatus status = getStatus();
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    publishUI();
                }
            });
            return;
        }
        MXRangedValue newValue = status._base.getValue();
        if (jCircleValue.getValue() == newValue._var) {
            return;
        }
        _ignoreEvent = true;
        jCircleValue.setValue(newValue);
        _ignoreEvent = false;
    }

    // Variables declaration - do not modify                     
    private javax.swing.JLabel jLabel1;
    private CurvedSlider jCircleValue;
    // End of variables declaration                   

    public void increment() {
        MGStatus status = getStatus();
        MXRangedValue var = status.getValue().increment();
        if (var != null) {
            _mixer.updateStatusAndSend(status, var._var, null);
        }
    }

    public void decriment() {
        MGStatus status = getStatus();
        MXRangedValue var = status.getValue().decrement();
        if (var != null) {
            _mixer.updateStatusAndSend(status, var._var, null);
        }
    }

    public void editContoller() {
        _mixer._view.stopEditing();
        MGStatus status = (MGStatus) getStatus().clone();
        MGStatusPanel panel = new MGStatusPanel(_mixer, status);
        MXModalFrame.showAsDialog(this, panel, "Enter Edit Circle {row:" + _row + ", column:" + _column + "}");
        if (panel._okOption) {
            setStatus(panel._status);
            jLabel1.setText(panel._status._name);
            _mixer.notifyCacheBroken();
            updateUI();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int d = e.getUnitsToScroll();
        if (d > 0) {
            this.decriment();
        } else {
            this.increment();
        }
    }
}
