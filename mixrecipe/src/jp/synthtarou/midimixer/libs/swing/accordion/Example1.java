/*
 * Copyright (C) 2023 Syntarou YOSHIDA.
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
package jp.synthtarou.midimixer.libs.swing.accordion;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class Example1 extends javax.swing.JPanel {
  public static class BindValue {

        String key;
        int value;
    }

    public static class ValueTableEntry {

        int value;
        String label;

        public ValueTableEntry(int value, String label) {
            this.value = value;
            this.label = label;
        }
    }

    public static class ValueTable {

        String id = null;
        ArrayList<ValueTableEntry> listEntry;

        public ValueTable() {
            clearEntry();
        }

        public ValueTable(int min, int max) {
            this();
            addRange(min, max, min < max ? 1 : -1);
        }

        public ValueTable(int min, int max, int step) {
            this();
            addRange(min, max, step);
        }

        public void clearEntry() {
            listEntry = new ArrayList<>();
        }

        public void addEntry(int value, String label) {
            listEntry.add(new ValueTableEntry(value, label));
        }

        public void addRange(int min, int max, int step) {
            if (step > 0) {
                for (int x = min; x <= max; x += step) {
                    listEntry.add(new ValueTableEntry(x, String.valueOf(x)));
                }
            } else if (step < 0) {
                for (int x = min; x >= max; x += step) {
                    listEntry.add(new ValueTableEntry(x, String.valueOf(x)));
                }
            } else {
                throw new IllegalArgumentException("Step can't 0");
            }
        }
    }

    public static class ValueFormat {

        int min;
        int max;
        int offset;
        /* 実際にデータを送信する際にプラスする値を指定します。　*/
 /* 例えばパンポットは-64から+63の範囲で指定しますが、midi機器に送信する際にはこの値に+64してから送信しないといけません。こうした場合にはOffsetを64に指定します。 */
        String name;
        boolean type;
        /* true=「Key」を指定するとプロパティ画面でキーボードを参照するボタンが出ます。 */
        int defaultValue;
        ValueTable valueTable;

        public ValueFormat() {
            min = 0;
            max = 127;
            offset = 0;
            name = "-";
            type = false;
            defaultValue = max;
            valueTable = new ValueTable(min, max, 1);
        }

        public String findLabel(int value) {
            for (ValueTableEntry e : valueTable.listEntry) {
                if (e.value == value) {
                    return e.label;
                }
            }
            return "?";
        }
    }

    public static class ValueFormatForNote extends ValueFormat {

        public ValueFormatForNote() {
            valueTable = new ValueTable();
            for (int i = 0; i < 128; ++i) {
                String note = MXMidi.nameOfNote(i);
                valueTable.addEntry(i, note);
            }
        }
    }

    public static class ValueFormatForCC extends ValueFormat {

        public ValueFormatForCC() {
            valueTable = new ValueTable();
            for (int i = 0; i < 128; ++i) {
                String cc = MXMidi.nameOfControlChange(i);
                valueTable.addEntry(i, cc);
            }
        }
    }

    public static class Value {

        String name;
        String port;
        int channel;
        /* 0-15 */
        String ccMessageA_Domino;
        String ccMessageB_Template;
        int value = 0;
        ValueFormat valueFormat;
        int gate = 1;
        ValueFormat gateFormat;

        public Value() {
            port = "MIDI-IN";
            channel = (int) (Math.random() * 256) % 16;
            valueFormat = new ValueFormat();
            valueFormat = new ValueFormat();
        }

        public String getGateLabel() {
            return gateFormat.findLabel(gate);
        }

        public String getValueLabel() {
            return valueFormat.findLabel(value);
        }
    }

    public static class ValuePolyPressure extends Value {

        public ValuePolyPressure() {
            super();
            name = "ModWheel";
            ccMessageA_Domino = "@PKP 64 #VL";
            ccMessageB_Template = "#9CH #GL #VL";
            gate = 64;
            gateFormat = new ValueFormatForNote();
            value = 127;
            valueFormat = new ValueFormat();
        }
    }

    public static class ValueModWheel extends Value {

        public ValueModWheel() {
            super();
            name = "Volume";
            ccMessageA_Domino = "@CC 1 #VL";
            ccMessageB_Template = "#BCH #GL #VL";
            gate = 1;
            gateFormat = new ValueFormatForCC();
            value = 0;
            valueFormat = new ValueFormat();
        }

    }

    /**
     * Creates new form Example1
     */
    public Example1() {
        initComponents();
        setValue(Math.random() >= 0.5 ? new ValuePolyPressure() : new ValueModWheel());
    }

    Value _value;

    public void setValue(Value value) {
        _value = value;
        jLabelName.setText("Name: " + value.name);
        jLabel1.setText("" + (value.channel + 1) + "@" + value.port);
        String gateLabel = value.getGateLabel();
        String gateDecimal = Integer.toString(value.gate);
        if (gateLabel.equals(gateDecimal)) {
            gateDecimal = "";
        }
        else {
            gateDecimal = " = "  + gateDecimal  +" =" + MXUtil.toHexFF(value.gate) + "h";
        }
        jLabelGate.setText("Gate:" + gateLabel + gateDecimal);
        String valueLabel = value.getValueLabel();
        String valueDecimal = Integer.toString(value.value);
        if (valueLabel.equals(valueDecimal)) {
            valueDecimal = "";
        }
        jLabelValue.setText(valueLabel);
        jLabelValueDecimal.setText(valueDecimal);
        jLabelValueHex.setText(MXUtil.toHexFF(value.value) + "h");
        jLabelText.setText("Format:" + value.ccMessageA_Domino);
        jLabelValue.setPreferredSize(new Dimension(60, 24));
        jLabelValue.setBackground(Color.orange);
    }

    public String getName() {
        if (_value == null) {
            return "";
        }
        return _value.name + " CH: " + _value.channel;
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

        jLabelText = new javax.swing.JLabel();
        jLabelGate = new javax.swing.JLabel();
        jLabelName = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabelValue = new javax.swing.JLabel();
        jLabelValueDecimal = new javax.swing.JLabel();
        jLabelValueHex = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabelText.setFont(new java.awt.Font("メイリオ", 0, 12)); // NOI18N
        jLabelText.setForeground(new java.awt.Color(102, 153, 255));
        jLabelText.setText("text");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(jLabelText, gridBagConstraints);

        jLabelGate.setFont(new java.awt.Font("メイリオ", 0, 12)); // NOI18N
        jLabelGate.setForeground(new java.awt.Color(102, 153, 255));
        jLabelGate.setText("gate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelGate, gridBagConstraints);

        jLabelName.setFont(new java.awt.Font("メイリオ", 0, 14)); // NOI18N
        jLabelName.setForeground(new java.awt.Color(0, 204, 204));
        jLabelName.setText("name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jLabelName, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("メイリオ", 0, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(153, 51, 0));
        jLabel1.setText("port+ch");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel2, gridBagConstraints);

        jLabelValue.setFont(new java.awt.Font("メイリオ", 0, 24)); // NOI18N
        jLabelValue.setForeground(new java.awt.Color(0, 204, 102));
        jLabelValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelValue.setText("value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jLabelValue, gridBagConstraints);

        jLabelValueDecimal.setFont(new java.awt.Font("メイリオ", 0, 14)); // NOI18N
        jLabelValueDecimal.setForeground(new java.awt.Color(0, 204, 102));
        jLabelValueDecimal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelValueDecimal.setText("00");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jLabelValueDecimal, gridBagConstraints);

        jLabelValueHex.setFont(new java.awt.Font("メイリオ", 0, 14)); // NOI18N
        jLabelValueHex.setForeground(new java.awt.Color(0, 204, 102));
        jLabelValueHex.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelValueHex.setText("ff");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jLabelValueHex, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelGate;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelText;
    private javax.swing.JLabel jLabelValue;
    private javax.swing.JLabel jLabelValueDecimal;
    private javax.swing.JLabel jLabelValueHex;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
