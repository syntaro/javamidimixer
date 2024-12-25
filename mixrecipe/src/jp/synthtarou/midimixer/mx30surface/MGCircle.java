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
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFormatter;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGCircle extends javax.swing.JPanel implements MouseWheelListener {

    MX32MixerProcess _mixer;
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

    public MGCircle(MX32MixerProcess process, int row, int column) {
        _row = row;
        _column = column;
        _mixer = process;

        initComponents();

        updateUI();
        addMouseWheelListener(this);
        _stopFeedback = 0;
    }

    public void updateUI() {
        MGStatus status = getStatus();
        if (status != null) {
            jCircleValue.setValue(status.getValue());
            MXMessageFormatter format = MXMessageFormatter._short;
            String formatText = status._name;
            if (formatText != null && formatText.length() > 0) {
                format = new MXMessageFormatter(formatText);
            }
            jLabel1.setText(format.format(status._base));
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

    int _stopFeedback = 0;

    private void jCircleValueStateChanged(javax.swing.event.ChangeEvent evt) {
        if (_stopFeedback > 0) {
            return;
        }
        int newValue = jCircleValue.getValue();
        //jLabel1.setText(String.valueOf(newValue));
        _mixer._parent.addSliderMove(null, getStatus(), newValue);
    }

    public void publishUI(MXRangedValue newValue) {
        MXMain.invokeUI(() ->  {
            MGStatus status = getStatus();
            _stopFeedback ++;
            jCircleValue.setValue(newValue);
            _stopFeedback --;
	});
    }

    // Variables declaration - do not modify                     
    private javax.swing.JLabel jLabel1;
    private CurvedSlider jCircleValue;
    // End of variables declaration                   

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
        MGStatus status = (MGStatus) getStatus().clone();
        MGStatusPanel panel = new MGStatusPanel(_mixer, status);
        MXUtil.showAsDialog(this, panel, "Enter Edit Circle {row:" + _row + ", column:" + _column + "}");
        if (panel._okOption) {
            setStatus(panel._status);
            MXMessageFormatter format = MXMessageFormatter._long; 
            String formatText = status._name;
            if (formatText != null && formatText.length() > 0) {
                format = new MXMessageFormatter(formatText);
            }
            jLabel1.setText(format.format(status._base));
            _mixer.notifyCacheBroken();
            updateUI();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int d = e.getUnitsToScroll();
        if (d > 0) {
            this.decriment(null);
        } else {
            this.increment(null);
        }
    }
}
