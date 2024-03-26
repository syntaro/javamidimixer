/*
 * Copyright (C) 2024 yaman
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
package jp.synthtarou.midimixer.mx00playlist;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.namedvalue.MNamedValueList;
import jp.synthtarou.midimixer.libs.navigator.MXPopupForList;

/**
 *
 * @author yaman
 */
public class MXPianoRollSettings extends javax.swing.JPanel {
    MNamedValueList<Integer> listColor = new MNamedValueList();
    MNamedValueList<Integer> listMagin = new MNamedValueList();
    MNamedValueList<Integer> listSpan = new MNamedValueList();
    MNamedValueList<Integer> listTiming = new MNamedValueList();
    MX00Process _process;

    /**
     * Creates new form PianoRollSettings
     */
    public MXPianoRollSettings(MX00Process process) {
        initComponents();
        MXUtil.centerWindow(this);
        _process = process;

        jTextFieldColor.setEditable(false);
        jTextFieldMargin.setEditable(false);
        jTextFieldSpan.setEditable(false);
        jTextFieldShowTiming.setEditable(false);

        listColor.addNameAndValue("Color", -1);
        for (int ch = 0; ch < 16; ++ ch) {
            listColor.addNameAndValue("Ch " +(ch+1), ch);
        }
        for (int margin = 0; margin <= 1000; margin += 100) {
            listMagin.addNameAndValue(margin + " msec", margin);
        }
        for (int span = 2000; span <= 10000; span += 1000) {
            listSpan.addNameAndValue((span / 1000) + " sec", span);
        }
        listTiming.addNameAndValue("Off", 0);
        listTiming.addNameAndValue("On", 1);
        
        MXPopupForList<Integer> popupForColor = new MXPopupForList<Integer>(jTextFieldColor, listColor) {
            @Override
            public void approvedIndex(int selectedIndex) {
                int value = listColor.valueOfIndex(selectedIndex);
                _process._structure._focusChannel = value;
                jTextFieldColor.setText(listColor.nameOfValue(_process._structure._focusChannel));
                _process._view._pianoRollRoll.setFocusChannel(value);
            }
        };
        MXPopupForList<Integer> popupForMargin = new MXPopupForList<Integer>(jTextFieldMargin, listMagin) {
            @Override
            public void approvedIndex(int selectedIndex) {
                int value = listMagin.valueOfIndex(selectedIndex);
                _process._structure._soundMargin = value;
                jTextFieldMargin.setText(listMagin.nameOfValue((int)_process._structure._soundMargin));
                _process._view._pianoRollRoll.setSoundMargin(value);
            }
        };
        MXPopupForList<Integer> popupForSpan = new MXPopupForList<Integer>(jTextFieldSpan, listSpan) {
            @Override
            public void approvedIndex(int selectedIndex) {
                int value = listSpan.valueOfIndex(selectedIndex);
                _process._structure._soundSpan = value;
                jTextFieldSpan.setText(listSpan.nameOfValue((int)_process._structure._soundSpan));
                _process._view._pianoRollRoll.setSoundSpan(value);
            }
        };

        MXPopupForList<Integer> popupForTiming = new MXPopupForList<Integer>(jTextFieldShowTiming, listTiming) {
            @Override
            public void approvedIndex(int selectedIndex) {
                int value = listTiming.valueOfIndex(selectedIndex);
                _process._structure._showMeasure = value > 0;
                jTextFieldShowTiming.setText(listTiming.nameOfValue(Integer.valueOf(_process._structure._showMeasure ? 1 : 0)));
                _process._view._pianoRollRoll.setShowMeasure(value > 0);
            }
        };
        this.setSize(new Dimension(350, 150));
        showStructureFirst();
    }

    public void showStructureFirst() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jTextFieldColor.setText(listColor.nameOfValue(_process._structure._focusChannel));
                jTextFieldMargin.setText(listMagin.nameOfValue((int)_process._structure._soundMargin));
                jTextFieldSpan.setText(listSpan.nameOfValue((int)_process._structure._soundSpan));
                jTextFieldShowTiming.setText(listTiming.nameOfValue(Integer.valueOf(_process._structure._showMeasure ? 1 : 0)));
                _process._view._pianoRollRoll._showMeasure = _process._structure._showMeasure;
                _process._view._pianoRollRoll._soundMargin = _process._structure._soundMargin;
                _process._view._pianoRollRoll.setSoundSpan(_process._structure._soundSpan);
            }
        });
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

        jLabel1 = new javax.swing.JLabel();
        jTextFieldColor = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldSpan = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldMargin = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldShowTiming = new javax.swing.JTextField();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jLabel1.setText("Color");
        add(jLabel1);

        jTextFieldColor.setEditable(false);
        add(jTextFieldColor);

        jLabel3.setText("Range");
        add(jLabel3);

        jTextFieldSpan.setEditable(false);
        add(jTextFieldSpan);

        jLabel2.setText("Margin");
        add(jLabel2);

        jTextFieldMargin.setEditable(false);
        add(jTextFieldMargin);

        jLabel4.setText("Timing");
        add(jLabel4);

        jTextFieldShowTiming.setEditable(false);
        add(jTextFieldShowTiming);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextFieldColor;
    private javax.swing.JTextField jTextFieldMargin;
    private javax.swing.JTextField jTextFieldShowTiming;
    private javax.swing.JTextField jTextFieldSpan;
    // End of variables declaration//GEN-END:variables
}
