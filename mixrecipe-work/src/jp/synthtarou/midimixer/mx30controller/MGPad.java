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
import jp.synthtarou.midimixer.libs.UniqueChecker;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.swing.MXFocusAble;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGPad extends javax.swing.JPanel implements MXFocusAble {
    private static final MXDebugPrint _debug = new MXDebugPrint(MGPad.class);
    final MX32MixerProcess _process;
    int _row, _column;
    boolean _dispFlag;
    
    Color highlight = MXUtil.mixedColor(Color.white, Color.blue, 50);
    
    class MyButtonUI extends BasicButtonUI {
        public MyButtonUI() {
        }
        
        public void paint(Graphics g, JComponent c) {
            MGStatus status = getStatus();
            boolean sel = _dispFlag;

            if (sel) {
                Dimension d = c.getSize();
                g.setColor(highlight);
                g.fillRect(0, 0, c.getWidth(), c.getHeight());
            }else {
                super.paint(g, c);
            }
        }
    }
    
    public MGStatus getStatus() {
        return _process._data.getDrumPadStatus(_row, _column);
    }

    public void setStatus(MGStatus status) {
        _process._data.setDrumPadStatus(_row, _column, status);
    }

    public MGPad(MX32MixerProcess process, int row, int column) {
        _process = process;
        _row = row;
        _column = column;
        initComponents();
        jButton1.setText("");
        jButton1.setMargin(new Insets(0, 0, 0, 0));
        for (MouseListener l : jButton1.getMouseListeners()) {
            jButton1.removeMouseListener(l);
        }
        jButton1.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)  {
                if(SwingUtilities.isRightMouseButton(e)){
                   return;
                }
                _process.catchedValueDrum(getStatus(), null,  true, getStatus().getSwitchOutOnValueFixed(), null);
            }
            
            public void mouseReleased(MouseEvent e)  {
                if(SwingUtilities.isRightMouseButton(e)){
                    return;
                }
                _process.catchedValueDrum(getStatus(), null, false, 0, null);
            }
        });
        
        jButton1.setUI(new MyButtonUI());

        //jSpinnerValue.setVisible(false);

        updateUI();
    }
    
    public void updateUIOnly(boolean newValue) {
        _dispFlag = newValue;
        jButton1.repaint();
    }

    public void updateUI() {
        if (_process != null && _process != null) {
            MGStatus status = getStatus();
            
            if (status.getName() == null || status.getName().length() == 0) {
                MXMessage message = status.toMXMessage(null);
                if (message == null) {
                    jButton1.setText("?");
                }else {
                    jButton1.setText(message.toShortString());
                }
            }else {
                jButton1.setText(status.getName());
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jButton1.setText("-");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jButton1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void focusStatusChanged(boolean flag) {
        if (flag) {
            if (_process != null) {
                MGStatus status = getStatus();
                _process._parent.showTextForFocus(MGStatus.TYPE_DRUMPAD, _process._port, _row, _column);
            }
        }
    }
    
    public void setValueChangeable(boolean using) {
        jButton1.setEnabled(using);
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
        _process._parent.showTextForFocus(MGStatus.TYPE_DRUMPAD,  _process._port, _row, _column);
    }

    public void increment(UniqueChecker already) {
        _process.catchedValueDrum(getStatus(), null, true, getStatus().getSwitchOutOnValueFixed(), null);
    }
    
    public void decriment(UniqueChecker already) { 
        _process.catchedValueDrum(getStatus(), null, false, 0, null);
    }

   public void editContoller() {
        _process._parent.enterEditMode(false);
        MGStatus status = (MGStatus)getStatus().clone();
        MGStatusConfig config = new MGStatusConfig(_process, status);
        MXUtil.showAsDialog(this, config, "Enter Edit Pad {row:" + _row + ", column:" + _column + "}");
        _process.notifyCacheBroken();

        if (config._okOption) {
            setStatus(config._status);
            _process.notifyCacheBroken();
            updateUI();
        }
    }
}
