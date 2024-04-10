/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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
package jp.synthtarou.tools;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.SpinnerNumberModel;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderSingleClick;
import jp.synthtarou.libs.uiproperty.MXUIProperty;
import jp.synthtarou.libs.uiproperty.MXUIPropertyEvent;
import jp.synthtarou.libs.uiproperty.MXUIPropertyListener;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class RGBTransVUI extends javax.swing.JPanel {

    public class ViewModel implements MXUIPropertyListener {

        public final MXUIProperty _spinnerRed, _spinnerGreen, _spinnerBlue;
        public final MXUIProperty _sliderRed, _sliderGreen, _sliderBlue;
        public final MXUIProperty _textHex, _textDec;

        boolean _construction = true;
        ViewModel() {
            _spinnerRed = new MXUIProperty(jSpinnerRed, this);
            _spinnerGreen = new MXUIProperty(jSpinnerGreen, this);
            _spinnerBlue = new MXUIProperty(jSpinnerBlue, this);

            _sliderRed = new MXUIProperty(jSliderRed, this);
            _sliderGreen = new MXUIProperty(jSliderGreen, this);
            _sliderBlue = new MXUIProperty(jSliderBlue, this);

            _textDec = new MXUIProperty(jTextField10);
            _textHex = new MXUIProperty(jTextField16);
            
            _textDec.addChangeListener(this);
            _textHex.addChangeListener(this);

            _construction = false;
        }

        @Override
        public void uiProperityValueChanged(MXUIPropertyEvent evt) {
            if (_underFlushing) {
                return;
            }
            if (_construction) {
                return;
            }
            MXUIProperty control = evt.getMXContoller();
            if (control == _spinnerRed
                    || control == _spinnerGreen
                    || control == _spinnerBlue) {
                _red = _spinnerRed.getAsInt();
                _green = _spinnerGreen.getAsInt();
                _blue = _spinnerBlue.getAsInt();
                flushColorToAll();
            } else if (control == _sliderRed
                    || control == _sliderGreen
                    || control == _sliderBlue) {
                _red = _sliderRed.getAsInt();
                _green = _sliderGreen.getAsInt();
                _blue = _sliderBlue.getAsInt();
                flushColorToAll();
            } else if (control == _textHex) {
                setupWithTextHex();
            } else if (control == _textDec) {
                setupWithTextDec();
            }
        }
    }

    int _red, _green, _blue;
    ViewModel _vm;

    public static void main(String[] args) {
        RGBTransVUI panel = new RGBTransVUI();
        MXUtil.showAsDialog(null, panel, "RGBTrans");
        System.exit(0);
    }

    /**
     * Creates new form RGBTrans
     */
    public RGBTransVUI() {
        initComponents();
        _red = 100;
        _green = 200;
        _blue = 0;

        jSpinnerRed.setModel(new SpinnerNumberModel(_red, 0, 255, 1));
        jSpinnerGreen.setModel(new SpinnerNumberModel(_green, 0, 255, 1));
        jSpinnerBlue.setModel(new SpinnerNumberModel(_blue, 0, 255, 1));

        jSliderRed.setMinimum(0);
        jSliderRed.setMaximum(255);
        jSliderGreen.setMinimum(0);
        jSliderGreen.setMaximum(255);
        jSliderBlue.setMinimum(0);
        jSliderBlue.setMaximum(255);

        new MXAttachSliderSingleClick(jSliderRed);
        new MXAttachSliderSingleClick(jSliderGreen);
        new MXAttachSliderSingleClick(jSliderBlue);
        new MXAttachSliderLikeEclipse(jSliderRed);
        new MXAttachSliderLikeEclipse(jSliderGreen);
        new MXAttachSliderLikeEclipse(jSliderBlue);
        setPreferredSize(new Dimension(500, 500));
        
        _vm = new ViewModel();

        flushColorToAll();
    }

    boolean _underFlushing = false;
    
    public void flushColorToAll() {
        _underFlushing = true;
        try {
            _red = new MXRangedValue(_red, 0, 255)._value;
            _green = new MXRangedValue(_green, 0, 255)._value;
            _blue = new MXRangedValue(_blue, 0, 255)._value;
            
            _vm._spinnerRed.set(_red);
            _vm._spinnerGreen.set(_green);
            _vm._spinnerBlue.set(_blue);

            _vm._sliderRed.set(_red);
            _vm._sliderGreen.set(_green);
            _vm._sliderBlue.set(_blue);

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

            String text10 = _red + ", " + _green + "," + _blue;
            String text16 = "#" + red16 + green16 + blue16;

            _vm._textDec.set(text10);
            _vm._textHex.set(text16);

            Color col = new Color(_red, _green, _blue);
            jTextPane1.setBackground(col);
        } finally {
            _underFlushing = false;
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jSliderRed, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jSliderGreen, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jSliderBlue, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        add(jSpinnerRed, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        add(jSpinnerGreen, gridBagConstraints);
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jTextField16, gridBagConstraints);

        jTextField10.setText("jTextField2");
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
            number = number.stripLeading();
            number = number.stripTrailing();
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

    public void setupWithTextHex() {
        String text = _vm._textHex.getAsText();
        while (text.startsWith("#")) {
            text = text.substring(1);
        }
        if (text.length() >= 6) {
            String red = text.substring(0, 2);
            String green = text.substring(2, 4);
            String blue = text.substring(4, 6);
            _red = parse16(red);
            _green = parse16(green);
            _blue = parse16(blue);
            flushColorToAll();
        }
    }

    public void setupWithTextDec() {
        String text = _vm._textDec.getAsText();
        String[] list = text.split(",");
        if (list != null && list.length >= 3) {
            _red = parse10(list[0]);
            _green = parse10(list[1]);
            _blue = parse10(list[2]);
            flushColorToAll();
        }
    }
    
    public void setupWithColor(Color col) {
        _red = col.getRed();
        _green = col.getGreen();
        _blue = col.getBlue();
        flushColorToAll();    
    }

    private void jButtonColor1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor1ActionPerformed
        setupWithColor(jButtonColor1.getBackground());
    }//GEN-LAST:event_jButtonColor1ActionPerformed

    private void jButtonColor2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor2ActionPerformed
        setupWithColor(jButtonColor2.getBackground());
    }//GEN-LAST:event_jButtonColor2ActionPerformed

    private void jButtonColor3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor3ActionPerformed
        setupWithColor(jButtonColor3.getBackground());
    }//GEN-LAST:event_jButtonColor3ActionPerformed

    private void jButtonColor4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonColor4ActionPerformed
        setupWithColor(jButtonColor4.getBackground());
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
