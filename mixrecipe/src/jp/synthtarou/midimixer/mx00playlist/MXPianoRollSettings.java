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
package jp.synthtarou.midimixer.mx00playlist;

import java.awt.Dimension;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.namedobject.MXNamedObject;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.libs.navigator.MXPopupForList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXPianoRollSettings extends javax.swing.JPanel {
    MXNamedObjectList<Integer> listColor = new MXNamedObjectList();
    MXNamedObjectList<Integer> listMagin = new MXNamedObjectList();
    MXNamedObjectList<Integer> listSpan = new MXNamedObjectList();
    MXNamedObjectList<Integer> listTiming = new MXNamedObjectList();
    MX00Process _playListProcess;

    /**
     * Creates new form PianoRollSettings
     */
    public MXPianoRollSettings(MX00Process process) {
        initComponents();
        MXUtil.centerWindow(this);
        _playListProcess = process;

        jTextFieldColor.setEditable(false);
        jTextFieldMargin.setEditable(false);
        jTextFieldSpan.setEditable(false);
        jTextFieldHighlightTiming.setEditable(false);

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
                if (_playListProcess._viewData._focusChannel != value) {
                    _playListProcess._viewData._focusChannel = value;
                    jTextFieldColor.setText(listColor.nameOfValue(_playListProcess._viewData._focusChannel));
                    _playListProcess._view._pianoRollRoll.setFocusChannel(value);
                }
            }

            @Override
            public void customizeMenu(JRadioButtonMenuItem item, MXNamedObject<Integer> entry) {
                int channel = entry._value;
                item.setForeground(MXPianoRoll.channelColor(channel));
            }
        };
        MXPopupForList<Integer> popupForMargin = new MXPopupForList<Integer>(jTextFieldMargin, listMagin) {
            @Override
            public void approvedIndex(int selectedIndex) {
                int value = listMagin.valueOfIndex(selectedIndex);
                if (_playListProcess._viewData._soundMargin != value) {
                    _playListProcess._viewData._soundMargin = value;
                    jTextFieldMargin.setText(listMagin.nameOfValue((int)_playListProcess._viewData._soundMargin));
                    _playListProcess._view._pianoRollRoll.setSoundMargin(value);
                }
            }
        };
        MXPopupForList<Integer> popupForSpan = new MXPopupForList<Integer>(jTextFieldSpan, listSpan) {
            @Override
            public void approvedIndex(int selectedIndex) {
                int value = listSpan.valueOfIndex(selectedIndex);
                if (_playListProcess._viewData._soundSpan != value) {
                    _playListProcess._viewData._soundSpan = value;
                    jTextFieldSpan.setText(listSpan.nameOfValue((int)_playListProcess._viewData._soundSpan));
                    _playListProcess._view._pianoRollRoll.setSoundSpan(value);
                }
            }
        };

        MXPopupForList<Integer> popupForTiming = new MXPopupForList<Integer>(jTextFieldHighlightTiming, listTiming) {
            @Override
            public void approvedIndex(int selectedIndex) {
                int valueNumber = listTiming.valueOfIndex(selectedIndex);
                boolean value = valueNumber > 0;
                if (_playListProcess._viewData._highlightTiming != value) {
                    _playListProcess._viewData._highlightTiming = value;
                    jTextFieldHighlightTiming.setText(listTiming.nameOfValue(Integer.valueOf(value ? 1 : 0)));
                    _playListProcess._view._pianoRollRoll.setHighlightTiming(value);
                }
            }
        };
        this.setSize(new Dimension(350, 150));
        showDataFirst();
    }

    public void showDataFirst() {
        SwingUtilities.invokeLater(() -> {
            jTextFieldColor.setText(listColor.nameOfValue(_playListProcess._viewData._focusChannel));
            jTextFieldMargin.setText(listMagin.nameOfValue((int)_playListProcess._viewData._soundMargin));
            jTextFieldSpan.setText(listSpan.nameOfValue((int)_playListProcess._viewData._soundSpan));
            jTextFieldHighlightTiming.setText(listTiming.nameOfValue(Integer.valueOf(_playListProcess._viewData._highlightTiming ? 1 : 0)));
            _playListProcess._view._pianoRollRoll._highlightTiming = _playListProcess._viewData._highlightTiming;
            _playListProcess._view._pianoRollRoll._soundMargin = _playListProcess._viewData._soundMargin;
            _playListProcess._view._pianoRollRoll.setSoundSpan(_playListProcess._viewData._soundSpan);
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

        jLabel1 = new javax.swing.JLabel();
        jTextFieldColor = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldSpan = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldMargin = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldHighlightTiming = new javax.swing.JTextField();

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

        jLabel4.setText("Highlight");
        add(jLabel4);

        jTextFieldHighlightTiming.setEditable(false);
        add(jTextFieldHighlightTiming);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextFieldColor;
    private javax.swing.JTextField jTextFieldHighlightTiming;
    private javax.swing.JTextField jTextFieldMargin;
    private javax.swing.JTextField jTextFieldSpan;
    // End of variables declaration//GEN-END:variables
}
