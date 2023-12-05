/*
 * Copyright (C) 2023 yaman
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
package example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import javax.swing.SpinnerNumberModel;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderSingleClick;

/**
 *
 * @author yaman
 */
public class RGBTrans extends javax.swing.JPanel {

    int _red, _green, _blue;

    public static void main(String[] args) {
        RGBTrans panel = new RGBTrans();
        MXUtil.showAsDialog(null, panel, "RGBTrans");
        System.exit(0);
    }

    /**
     * Creates new form RGBTrans
     */
    public RGBTrans() {
        initComponents();
        _red = 255;
        _green = 255;
        _blue = 255;
        flushColorToAll();
        new MXAttachSliderSingleClick(jSliderRed);
        new MXAttachSliderSingleClick(jSliderGreen);
        new MXAttachSliderSingleClick(jSliderBlue);
        new MXAttachSliderLikeEclipse(jSliderRed);
        new MXAttachSliderLikeEclipse(jSliderGreen);
        new MXAttachSliderLikeEclipse(jSliderBlue);
        setPreferredSize(new Dimension(500, 500));
    }

    public boolean _flushing = false;

    public void flushColorToAll() {
        _flushing = true;
        try {
            if (_red < 0) {
                _red = 0;
            }
            if (_green < 0) {
                _green = 0;
            }
            if (_blue < 0) {
                _blue = 0;
            }
            if (_red > 255) {
                _red = 255;
            }
            if (_green > 255) {
                _green = 255;
            }
            if (_blue > 255) {
                _blue = 255;
            }
            jSpinnerRed.setModel(new SpinnerNumberModel(_red, 0, 255, 1));
            jSpinnerGreen.setModel(new SpinnerNumberModel(_green, 0, 255, 1));
            jSpinnerBlue.setModel(new SpinnerNumberModel(_blue, 0, 255, 1));

            if (_red != jSliderRed.getValue()) {
                jSliderRed.setMinimum(0);
                jSliderRed.setMaximum(255);
                jSliderRed.setValue(_red);
            }

            if (_green != jSliderGreen.getValue()) {
                jSliderGreen.setMinimum(0);
                jSliderGreen.setMaximum(255);
                jSliderGreen.setValue(_green);
            }

            if (_blue != jSliderBlue.getValue()) {
                jSliderBlue.setMinimum(0);
                jSliderBlue.setMaximum(255);
                jSliderBlue.setValue(_blue);
            }

            String text10 = "" + _red + "," + _green + "," + _blue;

            String red16 = Integer.toHexString(_red);
            String green16 = Integer.toHexString(_green);
            String blue16 = Integer.toHexString(_blue);

            if (red16.length() == 1) {
                red16 = "0" + red16;
            }
            if (green16.length() == 1) {
                green16 = "0" + green16;
            }
            if (blue16.length() == 1) {
                blue16 = "0" + blue16;
            }

            String text16 = red16 + green16 + blue16;

            if (jTextField10.equals(text10) == false) {
                jTextField10.setText(text10);
            }
            if (jTextField16.equals(text16) == false) {
                jTextField16.setText(red16 + green16 + blue16);
            }

            Color col = new Color(_red, _green, _blue);
            jTextPane1.setBackground(col);
        } finally {
            _flushing = false;
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

        jSliderRed = new javax.swing.JSlider();
        jSliderGreen = new javax.swing.JSlider();
        jSliderBlue = new javax.swing.JSlider();
        jSpinnerRed = new javax.swing.JSpinner();
        jSpinnerGreen = new javax.swing.JSpinner();
        jSpinnerBlue = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jTextField16 = new javax.swing.JTextField();
        jTextField10 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jButtonColor1 = new javax.swing.JButton();
        jButtonColor2 = new javax.swing.JButton();
        jButtonColor3 = new javax.swing.JButton();
        jButtonColor4 = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jSliderRed.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderRedStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jSliderRed, gridBagConstraints);

        jSliderGreen.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderGreenStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jSliderGreen, gridBagConstraints);

        jSliderBlue.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderBlueStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jSliderBlue, gridBagConstraints);

        jSpinnerRed.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerRedStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        add(jSpinnerRed, gridBagConstraints);

        jSpinnerGreen.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerGreenStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        add(jSpinnerGreen, gridBagConstraints);

        jSpinnerBlue.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerBlueStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        add(jSpinnerBlue, gridBagConstraints);

        jLabel1.setText("Red");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        add(jLabel1, gridBagConstraints);

        jLabel2.setText("Green");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        add(jLabel2, gridBagConstraints);

        jLabel3.setText("Blue");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        add(jLabel3, gridBagConstraints);

        jTextPane1.setBackground(new java.awt.Color(204, 255, 51));
        jScrollPane1.setViewportView(jTextPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jTextField16.setText("jTextField1");
        jTextField16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField16ActionPerformed(evt);
            }
        });
        jTextField16.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField16KeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField16KeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jTextField16, gridBagConstraints);

        jTextField10.setText("jTextField2");
        jTextField10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField10ActionPerformed(evt);
            }
        });
        jTextField10.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField10KeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField10KeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jTextField10, gridBagConstraints);

        jLabel4.setText("#FFFFFF");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        add(jLabel4, gridBagConstraints);

        jLabel5.setText("255,255,255");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        add(jLabel5, gridBagConstraints);

        jButtonColor1.setBackground(new java.awt.Color(143, 210, 230));
        jButtonColor1.setText("背景");
        jButtonColor1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(jButtonColor1, gridBagConstraints);

        jButtonColor2.setBackground(new java.awt.Color(124, 112, 224));
        jButtonColor2.setText("文字色");
        jButtonColor2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(jButtonColor2, gridBagConstraints);

        jButtonColor3.setBackground(new java.awt.Color(25, 102, 194));
        jButtonColor3.setText("ご案内");
        jButtonColor3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor3ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(jButtonColor3, gridBagConstraints);

        jButtonColor4.setBackground(new java.awt.Color(219, 73, 156));
        jButtonColor4.setText("TakeOut");
        jButtonColor4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonColor4ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(jButtonColor4, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    public int parse10(String number) {
        try {
            return Integer.parseInt(number);
        } catch (Exception e) {
        }
        return 0;
    }

    public int parse16(String number) {
        try {
            return Integer.parseInt(number, 16);
        } catch (Exception e) {
        }
        return 0;
    }

    private void jTextField16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField16ActionPerformed

    }//GEN-LAST:event_jTextField16ActionPerformed

    private void jTextField10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField10ActionPerformed

    }//GEN-LAST:event_jTextField10ActionPerformed

    private void jSliderRedStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderRedStateChanged
        if (_flushing) {
            return;
        }
        _red = jSliderRed.getValue();
        flushColorToAll();
    }//GEN-LAST:event_jSliderRedStateChanged

    private void jSliderGreenStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderGreenStateChanged
        if (_flushing) {
            return;
        }
        _green = jSliderGreen.getValue();
        flushColorToAll();
    }//GEN-LAST:event_jSliderGreenStateChanged

    private void jSliderBlueStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderBlueStateChanged
        if (_flushing) {
            return;
        }
        _blue = jSliderBlue.getValue();
        flushColorToAll();
    }//GEN-LAST:event_jSliderBlueStateChanged

    private void jTextField16KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField16KeyPressed

    }//GEN-LAST:event_jTextField16KeyPressed

    private void jTextField10KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField10KeyPressed

    }//GEN-LAST:event_jTextField10KeyPressed

    private void jTextField16KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField16KeyTyped
        // TODO add your handling code here:
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            String text = jTextField10.getText();
            if (text.length() >= 6) {
                String red = text.substring(0, 1);
                String green = text.substring(2, 3);
                String blue = text.substring(4, 5);
                _red = parse16(red);
                _green = parse16(green);
                _blue = parse16(blue);
                flushColorToAll();
            }
        }

    }//GEN-LAST:event_jTextField16KeyTyped

    private void jTextField10KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField10KeyTyped
        // TODO add your handling code here:
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            String text = jTextField10.getText();
            String[] list = text.split(",");
            if (list != null && list.length >= 3) {
                _red = parse10(list[0]);
                _green = parse10(list[1]);
                _blue = parse10(list[2]);
                flushColorToAll();
            }
        }

    }//GEN-LAST:event_jTextField10KeyTyped

    private void jSpinnerRedStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerRedStateChanged
        if (_flushing) {
            return;
        }
        _red = (Integer) jSpinnerRed.getValue();
        flushColorToAll();
    }//GEN-LAST:event_jSpinnerRedStateChanged

    private void jSpinnerGreenStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerGreenStateChanged
        if (_flushing) {
            return;
        }
        _green = (Integer) jSpinnerGreen.getValue();
        flushColorToAll();
    }//GEN-LAST:event_jSpinnerGreenStateChanged

    private void jSpinnerBlueStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerBlueStateChanged
        if (_flushing) {
            return;
        }
        _blue = (Integer) jSpinnerBlue.getValue();
        flushColorToAll();
    }//GEN-LAST:event_jSpinnerBlueStateChanged

    private void jButtonColor1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor1ActionPerformed
        Color col = jButtonColor1.getBackground();
        _red = col.getRed();
        _green = col.getGreen();
        _blue = col.getBlue();
        flushColorToAll();
    }//GEN-LAST:event_jButtonColor1ActionPerformed

    private void jButtonColor2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor2ActionPerformed
        Color col = jButtonColor2.getBackground();
        _red = col.getRed();
        _green = col.getGreen();
        _blue = col.getBlue();
        flushColorToAll();
    }//GEN-LAST:event_jButtonColor2ActionPerformed

    private void jButtonColor3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor3ActionPerformed
        Color col = jButtonColor3.getBackground();
        _red = col.getRed();
        _green = col.getGreen();
        _blue = col.getBlue();
        flushColorToAll();
    }//GEN-LAST:event_jButtonColor3ActionPerformed

    private void jButtonColor4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor4ActionPerformed
        Color col = jButtonColor4.getBackground();
        _red = col.getRed();
        _green = col.getGreen();
        _blue = col.getBlue();
        flushColorToAll();
    }//GEN-LAST:event_jButtonColor4ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonColor1;
    private javax.swing.JButton jButtonColor2;
    private javax.swing.JButton jButtonColor3;
    private javax.swing.JButton jButtonColor4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSliderBlue;
    private javax.swing.JSlider jSliderGreen;
    private javax.swing.JSlider jSliderRed;
    private javax.swing.JSpinner jSpinnerBlue;
    private javax.swing.JSpinner jSpinnerGreen;
    private javax.swing.JSpinner jSpinnerRed;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField16;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
}
