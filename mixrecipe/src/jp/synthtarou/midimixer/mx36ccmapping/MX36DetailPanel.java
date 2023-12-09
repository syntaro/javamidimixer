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
package jp.synthtarou.midimixer.mx36ccmapping;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTextField;
import jp.synthtarou.midimixer.ccxml.CCXParserForCCM;
import jp.synthtarou.midimixer.ccxml.EditorForControlChange;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.MXWrapListPopup;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessageWrapListFactory;
import jp.synthtarou.midimixer.libs.navigator.INavigator;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderSingleClick;
import jp.synthtarou.midimixer.mx30surface.MGStatus;
import jp.synthtarou.midimixer.mx30surface.MXNotePicker;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36DetailPanel extends javax.swing.JPanel {

    MX36Process _process;
    MX36Status _status = null;
    JTextField[] listBindMouse = null;

    /**
     * Creates new form MX36View
     */
    public MX36DetailPanel(MX36Process process) {
        initComponents();
        _process = process;
        jLabelEmpty1.setText("");
        jLabelEmpty2.setText("");
        jLabelEmpty3.setText("");

        MouseListener mouseHandle = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getSource() instanceof JTextField) {
                    JTextField target = (JTextField) e.getSource();
                    textFieldDoPopup(target);
                }
            }
        };
        KeyListener keyHandle = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (e.getSource() instanceof JTextField) {
                        JTextField target = (JTextField) e.getSource();
                        textFieldDoPopup(target);
                    }
                }
            }
        };
        listBindMouse = new JTextField[]{
            jTextFieldSurfacePort,
            jTextFieldSurfaceRow,
            jTextFieldSurfaceColumn,
            jTextFieldOutChannel,
            jTextFieldOutGate,
            jTextFieldOutPort,
            jTextFieldOutData,
            jTextFieldBind1RCH,
            jTextFieldBind2RCH,
            jTextFieldBind4RCH,
            jTextFieldBindRSCTPT1,
            jTextFieldBindRSCTPT2,
            jTextFieldBindRSCTPT3,
            jTextFieldBindRSCTRT1,
            jTextFieldBindRSCTRT2,
            jTextFieldBindRSCTRT3
        };
        for (JTextField textField : listBindMouse) {
            textField.addMouseListener(mouseHandle);
            textField.addKeyListener(keyHandle);
        }

        jTextFieldValueValue.setBackground(Color.white);
        jTextFieldValueValue.setForeground(Color.magenta);
        jTextFieldValueValue.setMinimumSize(new Dimension(80, 30));
        jTextFieldValueValue.setHorizontalAlignment(JTextField.RIGHT);
        new MXAttachSliderLikeEclipse(jSliderValueValue);
        new MXAttachSliderSingleClick(jSliderValueValue);
        //updateViewByStatus(new MX36Status(null));
    }

    MXWrapList<Integer> _listPort = MXMessageWrapListFactory.listupPort("-");
    MXWrapList<Integer> _listColumn = MXMessageWrapListFactory.listupColumn("-");
    MXWrapList<Integer> _listChannel = MXMessageWrapListFactory.listupChannel("-");

    public synchronized void updateViewByStatus(MX36Status status) {
        _status = null;

        jTextFieldSurfacePort.setText(_listPort.nameOfValue(status._surfacePort));
        String surfaceRowText;

        switch (status._surfaceUIType) {
            case MGStatus.TYPE_SLIDER:
                surfaceRowText = Character.toString('S' + status._surfaceRow);
                break;
            case MGStatus.TYPE_DRUMPAD:
                surfaceRowText = Character.toString('X' + status._surfaceRow);
                break;
            case MGStatus.TYPE_CIRCLE:
                surfaceRowText = Character.toString('A' + status._surfaceRow);
                break;
            default:
                surfaceRowText = "-";
                break;
        }

        jTextFieldSurfacePort.setText(_listPort.nameOfValue(status._surfacePort));
        jTextFieldSurfaceRow.setText(surfaceRowText);
        jTextFieldSurfaceColumn.setText(_listColumn.nameOfValue(status._surfaceColumn));
        jLabelSurfaceValueRange.setText(status._surfaceValueRange._min + " ... " + status._surfaceValueRange._max);

        jTextFieldOutPort.setText(_listPort.nameOfValue(status._outPort));
        jTextFieldOutChannel.setText(_listChannel.nameOfValue(status._outChannel));
        jTextFieldOutGate.setText(status._outGateTable.nameOfValue(status._outGateRange._var));
        jTextFieldOutName.setText(status._outName);
        jTextFieldOutData.setText(status._outDataText);
        jLabelOutValueRange.setText(status._outValueRange._min + " ... " + status._outValueRange._max);

        jTextFieldBind1RCH.setText(status._bind1RCH);
        jTextFieldBind2RCH.setText(status._bind2RCH);
        jTextFieldBind4RCH.setText(status._bind4RCH);

        jTextFieldBindRSCTPT1.setText(status._bindRSCTPT1);
        jTextFieldBindRSCTPT2.setText(status._bindRSCTPT2);
        jTextFieldBindRSCTPT3.setText(status._bindRSCTPT3);

        jTextFieldBindRSCTRT1.setText(status._bindRSCTRT1);
        jTextFieldBindRSCTRT2.setText(status._bindRSCTRT2);
        jTextFieldBindRSCTRT3.setText(status._bindRSCTRT3);

        jSliderValueValue.setMinimum(status._outValueRange._min);
        jSliderValueValue.setMaximum(status._outValueRange._max);
        jSliderValueValue.setValue(status._outValueRange._var);

        _status = status;
    }
    
    public void updateSliderByStatus() {
        MX36Status status = _status;
        _status = null;
        jSliderValueValue.setValue(status._outValueRange._var);
        _status = status;
    }

    public void textFieldDoPopup(final JTextField target) {
        if (_editing == false) {
            return;
        }
        boolean dopopup = false;

        if (target == jTextFieldSurfacePort) {
            MXWrapListPopup<Integer> actions = new MXWrapListPopup<Integer>(target, _listPort);
            actions.show();
            return;
        }
        if (target == jTextFieldSurfaceRow) {
            MXWrapList<String> list = new MXWrapList<>();
            list.addNameAndValue("A", "knob1");
            list.addNameAndValue("B", "knob2");
            list.addNameAndValue("C", "knob3");
            list.addNameAndValue("D", "knob4");
            list.addNameAndValue("S", "slider");
            MXWrapListPopup<String> actions = new MXWrapListPopup<>(target, list);
            actions.show();
            return;
        }
        if (target == jTextFieldOutChannel) {
            MXWrapListPopup<Integer> actions = new MXWrapListPopup<>(target, _listChannel);
            actions.show();
            return;
        };
        if (target == jTextFieldOutGate) {
            if (_status._outGateTypeKey) {
                MXNotePicker picker = new MXNotePicker(false);
                picker.setSelectedNoteList(new int[]{ _status._outGateRange._var });
                MXUtil.showAsDialog(null, picker, "Note Number");
                if (picker.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
                    int[] ret = picker.getReturnValue();
                    if (ret != null && ret.length == 1) {
                        _status._outGateRange = _status._outGateRange.changeValue(ret[0]);
                        String noteName = _status._outGateTable.nameOfValue(ret[0]);
                        jTextFieldOutGate.setText(noteName);
                    }
                }
            }
            else {
                MXWrapListPopup<Integer> actions = new MXWrapListPopup<>(target, _status._outGateTable);
                actions.show();
            }
            return;
        }
        if (target == jTextFieldOutPort) {
            MXWrapListPopup<Integer> actions = new MXWrapListPopup<>(target, _listPort);
            actions.show();
            return;
        }

        if (target == jTextFieldOutData) {
            EditorForControlChange picker = new EditorForControlChange(false);
            MXUtil.showAsDialog(this, picker,  "Which You Choose?");
            if (picker.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
                CCXParserForCCM ccm = picker.getReturnValue();
                _status._outName = ccm._name;
                _status._outMemo = ccm._memo;
                _status._outDataText = ccm._data;

                _status._outGateRange = new RangedValue(ccm._defaultGate, ccm._minGate, ccm._maxGate);
                _status._outGateOffset = ccm._offsetGate;
                _status._outGateTable = ccm._gateTable;
                _status._outGateTypeKey = ccm._gateTypeKey;

                _status._outValueRange = new RangedValue(ccm._defaultValue, ccm._minValue, ccm._maxValue);
                _status._outValueOffset = ccm._offsetValue;
                _status._outValueTable = ccm._valueTable;
                
                _status._outCachedMessage = null;

                updateViewByStatus(_status);
            }
            return;
        }
        if (target == jTextFieldBind1RCH) {
            dopopup = true;
        }
        if (target == jTextFieldBind2RCH) {
            dopopup = true;
        }
        if (target == jTextFieldBind4RCH) {
            dopopup = true;
        }

        if (target == jTextFieldBindRSCTPT1) {
            dopopup = true;
        }
        if (target == jTextFieldBindRSCTPT2) {
            dopopup = true;
        }
        if (target == jTextFieldBindRSCTPT3) {
            dopopup = true;
        }

        if (target == jTextFieldBindRSCTRT1) {
            dopopup = true;
        }
        if (target == jTextFieldBindRSCTRT2) {
            dopopup = true;
        }
        if (target == jTextFieldBindRSCTRT3) {
            dopopup = true;
        }

        if (dopopup) {
            //final Color backBackground = target.getBackground();
            //target.setBackground(Color.red);
            JPopupMenu menu;
            menu = new JPopupMenu();
            menu.add(new AbstractAction("abc") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println(getValue(Action.NAME) + " pushed ");
                }
            });
            menu.add(new AbstractAction("def") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println(getValue(Action.NAME) + " pushed ");
                }
            });
            menu.add(new AbstractAction("ghi") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println(getValue(Action.NAME) + " pushed ");
                }
            });
            menu.show(target, 0, target.getHeight());
        }
    }

    public void refreshTree() {
        //jSplitPane1.setDividerLocation(jSplitPane1.getWidth() / 2);
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

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabelSurfaceValueRange = new javax.swing.JLabel();
        jLabelEmpty1 = new javax.swing.JLabel();
        jTextFieldSurfacePort = new javax.swing.JTextField();
        jTextFieldSurfaceRow = new javax.swing.JTextField();
        jTextFieldSurfaceColumn = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldOutName = new javax.swing.JTextField();
        jTextFieldOutData = new javax.swing.JTextField();
        jLabelOutValueRange = new javax.swing.JLabel();
        jLabelEmpty2 = new javax.swing.JLabel();
        jTextFieldOutPort = new javax.swing.JTextField();
        jTextFieldOutChannel = new javax.swing.JTextField();
        jTextFieldOutGate = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabelEmpty3 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jTextFieldBind1RCH = new javax.swing.JTextField();
        jTextFieldBind2RCH = new javax.swing.JTextField();
        jTextFieldBind4RCH = new javax.swing.JTextField();
        jTextFieldBindRSCTRT1 = new javax.swing.JTextField();
        jTextFieldBindRSCTRT2 = new javax.swing.JTextField();
        jTextFieldBindRSCTRT3 = new javax.swing.JTextField();
        jTextFieldBindRSCTPT1 = new javax.swing.JTextField();
        jTextFieldBindRSCTPT2 = new javax.swing.JTextField();
        jTextFieldBindRSCTPT3 = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jSliderValueValue = new javax.swing.JSlider();
        jTextFieldValueValue = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Surface Input"));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Port");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel2.add(jLabel1, gridBagConstraints);

        jLabel2.setText("Row");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel2.add(jLabel2, gridBagConstraints);

        jLabel3.setText("Column");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel2.add(jLabel3, gridBagConstraints);

        jLabel4.setText("ValueRange");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel2.add(jLabel4, gridBagConstraints);

        jLabelSurfaceValueRange.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jLabelSurfaceValueRange, gridBagConstraints);

        jLabelEmpty1.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jLabelEmpty1, gridBagConstraints);

        jTextFieldSurfacePort.setEditable(false);
        jTextFieldSurfacePort.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jTextFieldSurfacePort, gridBagConstraints);

        jTextFieldSurfaceRow.setEditable(false);
        jTextFieldSurfaceRow.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jTextFieldSurfaceRow, gridBagConstraints);

        jTextFieldSurfaceColumn.setEditable(false);
        jTextFieldSurfaceColumn.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jTextFieldSurfaceColumn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Output CC"));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel5.setText("Text");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel3.add(jLabel5, gridBagConstraints);

        jLabel6.setText("Value Range");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel3.add(jLabel6, gridBagConstraints);

        jTextFieldOutName.setEditable(false);
        jTextFieldOutName.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jTextFieldOutName, gridBagConstraints);

        jTextFieldOutData.setEditable(false);
        jTextFieldOutData.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jTextFieldOutData, gridBagConstraints);

        jLabelOutValueRange.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jLabelOutValueRange, gridBagConstraints);

        jLabelEmpty2.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        jPanel3.add(jLabelEmpty2, gridBagConstraints);

        jTextFieldOutPort.setEditable(false);
        jTextFieldOutPort.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jTextFieldOutPort, gridBagConstraints);

        jTextFieldOutChannel.setEditable(false);
        jTextFieldOutChannel.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jTextFieldOutChannel, gridBagConstraints);

        jTextFieldOutGate.setEditable(false);
        jTextFieldOutGate.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jTextFieldOutGate, gridBagConstraints);

        jLabel12.setText("Gate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel3.add(jLabel12, gridBagConstraints);

        jLabel11.setText("Channel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel3.add(jLabel11, gridBagConstraints);

        jLabel10.setText("Port");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel3.add(jLabel10, gridBagConstraints);

        jLabel7.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel3.add(jLabel7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel3, gridBagConstraints);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Bind Params"));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabelEmpty3.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        jPanel4.add(jLabelEmpty3, gridBagConstraints);

        jLabel15.setText("1RCH");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(jLabel15, gridBagConstraints);

        jLabel24.setText("RSCTPT3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jLabel24, gridBagConstraints);

        jLabel23.setText("RSCTPT2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jLabel23, gridBagConstraints);

        jLabel22.setText("RSCTPT1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(jLabel22, gridBagConstraints);

        jLabel21.setText("RSCTRT3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jLabel21, gridBagConstraints);

        jLabel20.setText("RSCTRT2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jLabel20, gridBagConstraints);

        jLabel19.setText("RSCTRT1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(jLabel19, gridBagConstraints);

        jLabel17.setText("4RCH");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jLabel17, gridBagConstraints);

        jLabel16.setText("2RCH");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jLabel16, gridBagConstraints);

        jTextFieldBind1RCH.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(jTextFieldBind1RCH, gridBagConstraints);

        jTextFieldBind2RCH.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jTextFieldBind2RCH, gridBagConstraints);

        jTextFieldBind4RCH.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jTextFieldBind4RCH, gridBagConstraints);

        jTextFieldBindRSCTRT1.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(jTextFieldBindRSCTRT1, gridBagConstraints);

        jTextFieldBindRSCTRT2.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jTextFieldBindRSCTRT2, gridBagConstraints);

        jTextFieldBindRSCTRT3.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jTextFieldBindRSCTRT3, gridBagConstraints);

        jTextFieldBindRSCTPT1.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(jTextFieldBindRSCTPT1, gridBagConstraints);

        jTextFieldBindRSCTPT2.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jTextFieldBindRSCTPT2, gridBagConstraints);

        jTextFieldBindRSCTPT3.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jTextFieldBindRSCTPT3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jPanel4, gridBagConstraints);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Value"));
        jPanel5.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jPanel5MouseWheelMoved(evt);
            }
        });
        jPanel5.setLayout(new java.awt.GridBagLayout());

        jSliderValueValue.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderValueValueStateChanged(evt);
            }
        });
        jSliderValueValue.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jSliderValueValueMouseWheelMoved(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel5.add(jSliderValueValue, gridBagConstraints);

        jTextFieldValueValue.setEditable(false);
        jTextFieldValueValue.setFont(new java.awt.Font("メイリオ", 0, 24)); // NOI18N
        jTextFieldValueValue.setText("127");
        jTextFieldValueValue.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTextFieldValueValueMousePressed(evt);
            }
        });
        jTextFieldValueValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldValueValueActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel5.add(jTextFieldValueValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jPanel5, gridBagConstraints);

        jButton1.setText("Unlock Customize Mode");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButton1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jSliderValueValueMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jSliderValueValueMouseWheelMoved
        int min = jSliderValueValue.getMinimum();
        int max = jSliderValueValue.getMaximum();
        int var = jSliderValueValue.getValue();

        int d = evt.getUnitsToScroll();

        if (d > 0) {
            var--;
        } else {
            var++;
        }
        if (min <= var && var <= max) {
            jSliderValueValue.setValue(var);
        }
    }//GEN-LAST:event_jSliderValueValueMouseWheelMoved

    private void jPanel5MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jPanel5MouseWheelMoved
        int min = jSliderValueValue.getMinimum();
        int max = jSliderValueValue.getMaximum();
        int var = jSliderValueValue.getValue();

        int d = evt.getUnitsToScroll();

        if (d > 0) {
            var--;
        } else {
            var++;
        }
        if (min <= var && var <= max) {
            jSliderValueValue.setValue(var);
        }
    }//GEN-LAST:event_jPanel5MouseWheelMoved

    private void jSliderValueValueStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderValueValueStateChanged
        int value = jSliderValueValue.getValue();
        jTextFieldValueValue.setText(Integer.toString(value));
        if (_status != null) {
            _process.updateOutputValue(_status, value);
        }
    }//GEN-LAST:event_jSliderValueValueStateChanged

    boolean _editing = false;

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        _editing = !_editing;
        if (_editing) {
            jButton1.setBackground(Color.orange);
            for (JTextField textField : listBindMouse) {
                textField.setBackground(Color.orange);
            }
        } else {
            jButton1.setBackground(null);
            for (JTextField textField : listBindMouse) {
                textField.setBackground(null);
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextFieldValueValueMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextFieldValueValueMousePressed
        MXWrapList<Integer> list = _status._outValueTable;
        MXWrapListPopup<Integer> actions = new MXWrapListPopup<Integer>(jTextFieldValueValue, list) {
            @Override
            public void doAction() {
                super.doAction();

                String value = jTextFieldValueValue.getText();
                JSlider slider = jSliderValueValue;
                try {
                    int x = Integer.valueOf(value);
                    slider.setValue(x);
                } catch (NumberFormatException e) {

                }
            }
        };
        actions.show();
    }//GEN-LAST:event_jTextFieldValueValueMousePressed

    private void jTextFieldValueValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldValueValueActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_jTextFieldValueValueActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelEmpty1;
    private javax.swing.JLabel jLabelEmpty2;
    private javax.swing.JLabel jLabelEmpty3;
    private javax.swing.JLabel jLabelOutValueRange;
    private javax.swing.JLabel jLabelSurfaceValueRange;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSlider jSliderValueValue;
    private javax.swing.JTextField jTextFieldBind1RCH;
    private javax.swing.JTextField jTextFieldBind2RCH;
    private javax.swing.JTextField jTextFieldBind4RCH;
    private javax.swing.JTextField jTextFieldBindRSCTPT1;
    private javax.swing.JTextField jTextFieldBindRSCTPT2;
    private javax.swing.JTextField jTextFieldBindRSCTPT3;
    private javax.swing.JTextField jTextFieldBindRSCTRT1;
    private javax.swing.JTextField jTextFieldBindRSCTRT2;
    private javax.swing.JTextField jTextFieldBindRSCTRT3;
    private javax.swing.JTextField jTextFieldOutChannel;
    private javax.swing.JTextField jTextFieldOutData;
    private javax.swing.JTextField jTextFieldOutGate;
    private javax.swing.JTextField jTextFieldOutName;
    private javax.swing.JTextField jTextFieldOutPort;
    private javax.swing.JTextField jTextFieldSurfaceColumn;
    private javax.swing.JTextField jTextFieldSurfacePort;
    private javax.swing.JTextField jTextFieldSurfaceRow;
    private javax.swing.JTextField jTextFieldValueValue;
    // End of variables declaration//GEN-END:variables
}
