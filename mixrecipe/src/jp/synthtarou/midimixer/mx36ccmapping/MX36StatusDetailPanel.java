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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import jp.synthtarou.midimixer.ccxml.InformationForCCM;
import jp.synthtarou.midimixer.ccxml.ui.NavigatorForCCXMLCC;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.namedobject.MXNamedObject;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.libs.namedobject.MXNamedObjectListFactory;
import jp.synthtarou.libs.navigator.legacy.INavigator;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderSingleClick;
import jp.synthtarou.libs.navigator.MXPopup;
import jp.synthtarou.libs.navigator.MXPopupForList;
import jp.synthtarou.libs.navigator.MXPopupForNumber;
import jp.synthtarou.libs.navigator.MXPopupForText;
import jp.synthtarou.libs.navigator.legacy.NavigatorForNote;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36StatusDetailPanel extends javax.swing.JPanel {

    final MX36Process _process;
    MX36Status _status;
    JTextField[] _listBindMouse = null;

    
    /**
     * Creates new form MX36View
     */
    public MX36StatusDetailPanel(MX36Process process) {
        initComponents();
        _status = new MX36Status();
        _process = process;
        jLabelEmpty1.setText("");
        jLabelEmpty2.setText("");
        jLabelEmpty3.setText("");

        _listBindMouse = new JTextField[]{
            jTextFieldSurfaceTypeAndRow,
            jTextFieldOutGate,
            jTextFieldOutData,};

        new MXPopupForList<Integer>(jTextFieldSurfacePort, _listInputPort) {
            @Override
            public void approvedIndex(int selectedIndex) {
                int port = _status.getSurfacePort();
                int type = _status.getSurfaceUIType();
                int row = _status.getSurfaceRow();
                int column = _status.getSurfaceColumn();
                
                port = _listInputPort.valueOfIndex(selectedIndex);

                _status.setSurface(port, type, row, column);

                showupStatus(_status);
            }
        };
        new MXPopupForList<MX36RowId>(jTextFieldSurfaceTypeAndRow, _listRow) {
            @Override
            public void approvedIndex(int selectedIndex) {
                int port = _status.getSurfacePort();
                int type = _status.getSurfaceUIType();
                int row = _status.getSurfaceRow();
                int column = _status.getSurfaceColumn();
                
                MXNamedObject<MX36RowId> seek = _listRow.get(selectedIndex);
                if (seek != null) {
                    type = seek._value._uiType;
                    row = seek._value._row;
                }
                else {
                    return;
                }

                _status.setSurface(port, type, row, column);

                showupStatus(_status);
            }
        };
        new MXPopupForList<Integer>(jTextFieldSurfaceColumn, _listColumn) {
            @Override
            public void approvedIndex(int selectedIndex) {
                int port = _status.getSurfacePort();
                int type = _status.getSurfaceUIType();
                int row = _status.getSurfaceRow();
                int column = _status.getSurfaceColumn();
                
                column = _listColumn.valueOfIndex(selectedIndex);
                
                _status.setSurface(port, type, row, column);

                showupStatus(_status);
            }
        };

        new MXPopupForText(jTextFieldOutName) {
            @Override
            public void approvedText(String text) {
                _status._outName = text;
                showupStatus(_status);
            }
        };

        new MXPopup(jTextFieldOutData) {
            @Override
            public void simpleAskAsync(JComponent mouseBase) {
                startBrowseXML();
            }
        };

        new MXPopupForList<Integer>(jTextFieldOutPort, _listOutputPort) {
            @Override
            public void approvedIndex(int selectedIndex) {
                _status._outPort = _listOutputPort.valueOfIndex(selectedIndex);
                showupStatus(_status);
            }
        };
        new MXPopup(jTextFieldOutGate) {
            @Override
            public void simpleAskAsync(JComponent mouseBase) {
                if (_status._outGateTypeKey) {
                    NavigatorForNote navi = new NavigatorForNote();
                    navi.setSelectedNoteList(new int[]{_status._outGateRange._value});
                    if (navi.simpleAsk(MX36StatusDetailPanel.this)) {
                        int[] ret = navi.getReturnValue();
                        if (ret != null && ret.length == 1) {
                            _status._outGateRange = _status._outGateRange.changeValue(ret[0]);
                            showupStatus(_status);
                        }
                    }
                } else {
                    MXNamedObjectList<Integer> listForGate = _status.safeGateTable();
                    MXPopup sub = new MXPopupForList<Integer>(null, listForGate) {
                        @Override
                        public void approvedIndex(int selectedIndex) {
                            _status._outGateRange = _status._outGateRange.changeValue(listForGate.valueOfIndex(selectedIndex));
                            showupStatus(_status);
                        }
                    };
                    sub.simpleAskAsync(mouseBase);
                }
            }
        };

        new MXPopupForList<Integer>(jTextFieldOutChannel, _listChannel) {
            @Override
            public void approvedIndex(int selectedIndex) {
                _status._outChannel = _listChannel.valueOfIndex(selectedIndex);
                showupStatus(_status);
            }
        };

        new MXPopupForNumber(jTextFieldBind1RCH, 0, 127) {
            @Override
            public void approvedValue(int selectedValue) {
                _status._bind1RCH = selectedValue;
                showupStatus(_status);
            }
        };
        new MXPopupForNumber(jTextFieldBind2RCH, 0, 127) {
            @Override
            public void approvedValue(int selectedValue) {
                _status._bind2RCH = selectedValue;
                showupStatus(_status);
            }
        };
        new MXPopupForNumber(jTextFieldBind4RCH, 0, 127) {
            @Override
            public void approvedValue(int selectedValue) {
                _status._bind4RCH = selectedValue;
                showupStatus(_status);
            }
        };
        new MXPopupForNumber(jTextFieldBindRSCTPT1, 0, 127) {
            @Override
            public void approvedValue(int selectedValue) {
                _status._bindRSCTPT1 = selectedValue;
                showupStatus(_status);
            }
        };
        new MXPopupForNumber(jTextFieldBindRSCTPT2, 0, 127) {
            @Override
            public void approvedValue(int selectedValue) {
                _status._bindRSCTPT2 = selectedValue;
                showupStatus(_status);
            }
        };
        new MXPopupForNumber(jTextFieldBindRSCTPT3, 0, 127) {
            @Override
            public void approvedValue(int selectedValue) {
                _status._bindRSCTPT3 = selectedValue;
                showupStatus(_status);
            }
        };
        new MXPopupForNumber(jTextFieldBindRSCTRT1, 0, 127) {
            @Override
            public void approvedValue(int selectedValue) {
                _status._bindRSCTRT1 = selectedValue;
                showupStatus(_status);
            }
        };
        new MXPopupForNumber(jTextFieldBindRSCTRT2, 0, 127) {
            @Override
            public void approvedValue(int selectedValue) {
                _status._bindRSCTRT2 = selectedValue;
                showupStatus(_status);
            }
        };
        new MXPopupForNumber(jTextFieldBindRSCTRT3, 0, 127) {
            @Override
            public void approvedValue(int selectedValue) {
                _status._bindRSCTRT3 = selectedValue;
                showupStatus(_status);
            }
        };

        jTextFieldValueValue.setBackground(Color.white);
        jTextFieldValueValue.setForeground(Color.magenta);
        jTextFieldValueValue.setHorizontalAlignment(JTextField.RIGHT);
        jTextFieldValueValue.setPreferredSize(new Dimension(15 * 5, jTextFieldValueValue.getPreferredSize().height));
        jButtonValueInc.setBackground(Color.white);
        jButtonValueDec.setBackground(Color.white);
        jButtonValueInc.setBorder(new EmptyBorder(3, 3, 3, 3));
        jButtonValueDec.setBorder(new EmptyBorder(3, 3, 3, 3));
        new MXAttachSliderLikeEclipse(jSliderValueValue);
        new MXAttachSliderSingleClick(jSliderValueValue);
        setEnabledRecrursible(false);
    }

    public void startBrowseXML() {
        NavigatorForCCXMLCC navi = new NavigatorForCCXMLCC();
        navi.setAllowMultiSelect(true);
        MXUtil.showAsDialog(this, navi, "Which You Choose? (Multi OK)");
        if (navi.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
            List<InformationForCCM> ccmList = navi.getReturnValue();
            if (ccmList == null) {
                return;
            }
            boolean isFirst = true;
            for (InformationForCCM ccm : ccmList) {

                if (ccm == null) {
                    return;
                }

                String data = ccm._data;
                String name = ccm._name;
                String memo = ccm._memo;
                MXRangedValue gate = ccm.getParsedGate();
                MXNamedObjectList<Integer> gateTable = ccm.getParsedGateTable();
                MXRangedValue value = ccm.getParsedValue();
                MXNamedObjectList<Integer> valueTable = ccm.getParsedValueTable();
                MXTemplate template = null;
                try {
                    template = new MXTemplate(data);
                } catch (IllegalFormatException ex) {
                    MXFileLogger.getLogger(MX36StatusDetailPanel.class).log(Level.WARNING, ex.getMessage(), ex);
                    return;
                }

                if (isFirst) {
                    isFirst = false;
                    _status._outName = name;
                    _status._outValueRange = value;
                    _status._outValueTable = valueTable;
                    _status._outGateRange = gate;
                    _status._outGateTable = gateTable;
                    _status.setOutDataText(data);
                    showupStatus(_status);
                } else {
                    MX36Status next = new MX36Status();
                    next._outName = name;
                    next._outValueRange = value;
                    next._outValueTable = valueTable;
                    next._outGateRange = gate;
                    next._outGateTable = gateTable;
                    next.setOutDataText(data);
                    _process.moveFolder(_status._folder, next);
                }
            }
        }
    }

    public void setEnabledRecrursible(boolean ena) {
        ArrayList<Component> list = new ArrayList<>();
        list.add(this);
        while (list.isEmpty() == false) {
            Component c = list.removeLast();
            c.setEnabled(ena);
            if (c instanceof Container) {
                Container cont = (Container) c;
                for (int i = 0; i < cont.getComponentCount(); ++i) {
                    list.add(cont.getComponent(i));
                }
            }
        }
        jButtonNewInFolder.setEnabled(true);
    }

    MXNamedObjectList<Integer> _listInputPort = MXNamedObjectListFactory.listupPort(null);
    MXNamedObjectList<Integer> _listOutputPort = MXNamedObjectListFactory.listupPort("=");
    MXNamedObjectList<Integer> _listColumn = MXNamedObjectListFactory.listupColumn("-");
    MXNamedObjectList<MX36RowId> _listRow = MX36RowId.ListModel;
    MXNamedObjectList<Integer> _listChannel = MXNamedObjectListFactory.listupChannel(null);

    public void showupStatus(MX36Status status) {
        setEnabledRecrursible(true);

        if (_process._folderList._autodetectFolder == status._folder) {
            if (status.isValidForWork()) {
                JOptionPane.showMessageDialog(this, "Moved from AutoDecteted to Primal.");
                _process.moveFolder(_process._folderList._primalFolder, status);
            }
        }

        synchronized (this) {
            _status = null;
            try {

                jTextFieldSurfacePort.setText(_listInputPort.nameOfValue(status.getSurfacePort()));
                jTextFieldSurfaceTypeAndRow.setText(_listRow.nameOfValue(MX36RowId.find(status.getSurfaceUIType(), status.getSurfaceRow())));
                jTextFieldSurfaceColumn.setText(_listColumn.nameOfValue(status.getSurfaceColumn()));
                jLabelSurfaceValueRange.setText(status._surfaceValueRange._min + " ... " + status._surfaceValueRange._max);
                jCheckBoxSurfaceReplace.setSelected(status._surfaceReplace);

                jTextFieldOutPort.setText(_listOutputPort.nameOfValue(status._outPort));
                jTextFieldOutChannel.setText(_listChannel.nameOfValue(status._outChannel));
                jTextFieldOutGate.setText(status.safeGateTable().nameOfValue(status._outGateRange._value));
                jTextFieldOutName.setText(status._outName);
                jTextFieldOutData.setText(status.getOutDataText());
                jLabelOutValueRange.setText(status._outValueRange._min + " ... " + status._outValueRange._max);

                jTextFieldBind1RCH.setText(Integer.toString(status._bind1RCH));
                jTextFieldBind2RCH.setText(Integer.toString(status._bind2RCH));
                jTextFieldBind4RCH.setText(Integer.toString(status._bind4RCH));

                jTextFieldBindRSCTPT1.setText(Integer.toString(status._bindRSCTPT1));
                jTextFieldBindRSCTPT2.setText(Integer.toString(status._bindRSCTPT2));
                jTextFieldBindRSCTPT3.setText(Integer.toString(status._bindRSCTPT3));

                jTextFieldBindRSCTRT1.setText(Integer.toString(status._bindRSCTRT1));
                jTextFieldBindRSCTRT2.setText(Integer.toString(status._bindRSCTRT2));
                jTextFieldBindRSCTRT3.setText(Integer.toString(status._bindRSCTRT3));

                jTextFieldValueValue.setText(status.safeValueTable().nameOfValue(status._outValueRange._value));
                jSliderValueValue.setMinimum(status._outValueRange._min);
                jSliderValueValue.setMaximum(status._outValueRange._max);
                jSliderValueValue.setValue(status._outValueRange._value);

                if (status._folder != null) {
                    //最初のダミーだけnull
                    status._folder.repaintStatus(status);
                }
                status._folder.sortElements();
            } finally {
                _status = status;
            }
        }
    }

    public void repaintDetailSliderStatus() {
        if (_status != null) {
            MX36Status status = _status;
            //再突入を防ぐ
            _status = null;
            jSliderValueValue.setValue(status._outValueRange._value);
            jTextFieldValueValue.setText(Integer.toString(status._outValueRange._value));
            _status = status;
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

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabelSurfaceValueRange = new javax.swing.JLabel();
        jLabelEmpty1 = new javax.swing.JLabel();
        jTextFieldSurfacePort = new javax.swing.JTextField();
        jTextFieldSurfaceTypeAndRow = new javax.swing.JTextField();
        jTextFieldSurfaceColumn = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jCheckBoxSurfaceReplace = new javax.swing.JCheckBox();
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
        jButtonOutTextClear = new javax.swing.JButton();
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
        jButtonValueInc = new javax.swing.JButton();
        jButtonValueDec = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButtonMoveFolder = new javax.swing.JButton();
        jButtonNewInFolder = new javax.swing.JButton();

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
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jLabelEmpty1, gridBagConstraints);

        jTextFieldSurfacePort.setEditable(false);
        jTextFieldSurfacePort.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jTextFieldSurfacePort, gridBagConstraints);

        jTextFieldSurfaceTypeAndRow.setEditable(false);
        jTextFieldSurfaceTypeAndRow.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jTextFieldSurfaceTypeAndRow, gridBagConstraints);

        jTextFieldSurfaceColumn.setEditable(false);
        jTextFieldSurfaceColumn.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jTextFieldSurfaceColumn, gridBagConstraints);

        jLabel8.setText("Replacer");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel2.add(jLabel8, gridBagConstraints);

        jCheckBoxSurfaceReplace.setText("Clear Original");
        jCheckBoxSurfaceReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSurfaceReplaceActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jCheckBoxSurfaceReplace, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
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
        gridBagConstraints.gridwidth = 4;
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

        jButtonOutTextClear.setText("Clear");
        jButtonOutTextClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOutTextClearActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        jPanel3.add(jButtonOutTextClear, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
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
        gridBagConstraints.gridx = 4;
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
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel5.add(jTextFieldValueValue, gridBagConstraints);

        jButtonValueInc.setText("Inc");
        jButtonValueInc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonValueIncActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel5.add(jButtonValueInc, gridBagConstraints);

        jButtonValueDec.setText("Dec");
        jButtonValueDec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonValueDecActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel5.add(jButtonValueDec, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
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
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButton1, gridBagConstraints);

        jButtonMoveFolder.setText("Move Folder");
        jButtonMoveFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMoveFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        add(jButtonMoveFolder, gridBagConstraints);

        jButtonNewInFolder.setText("New In Folder");
        jButtonNewInFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewInFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(jButtonNewInFolder, gridBagConstraints);
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
        if (_status == null) {
            return;
        }
        int value = jSliderValueValue.getValue();
        String name = _status.safeValueTable().nameOfValue(value);
        jTextFieldValueValue.setText(name);
        if (_status != null) {
            MXMessage message = _process.updateOutputValue(null, _status, value);
            if (message != null) {
                _process.sendToNext(message);
            }
        }
    }//GEN-LAST:event_jSliderValueValueStateChanged

    boolean _editing = false;

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        _editing = !_editing;
        if (_editing) {
            jButton1.setText("Customizing...Done?");
            jButton1.setBackground(Color.green);
            for (JTextField textField : _listBindMouse) {
                textField.setBackground(Color.green);
            }
        } else {
            jButton1.setText("Unlock Customize Mode...");
            jButton1.setBackground(null);
            for (JTextField textField : _listBindMouse) {
                textField.setBackground(null);
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextFieldValueValueMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextFieldValueValueMousePressed
        MXNamedObjectList<Integer> list = _status.safeValueTable();
        new MXPopupForList<Integer>(jTextFieldValueValue, list) {
            @Override
            public void approvedIndex(int selectedIndex) {
                int value = list.get(selectedIndex)._value;
                jSliderValueValue.setValue(value);
                jTextFieldValueValue.setText(Integer.toString(value));
            }

        }.simpleAskAsync(jTextFieldValueValue);
    }//GEN-LAST:event_jTextFieldValueValueMousePressed

    private void jButtonOutTextClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOutTextClearActionPerformed
        _status.setOutDataText(null);
        showupStatus(_status);
    }//GEN-LAST:event_jButtonOutTextClearActionPerformed

    private void jButtonValueIncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonValueIncActionPerformed
        int var = _status._outValueRange._value;
        var++;
        if (_status._outValueRange._max >= var) {
            _status._outValueRange = _status._outValueRange.changeValue(var);
            showupStatus(_status);
        }
    }//GEN-LAST:event_jButtonValueIncActionPerformed

    private void jButtonValueDecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonValueDecActionPerformed
        int var = _status._outValueRange._value;
        var--;
        if (_status._outValueRange._min <= var) {
            _status._outValueRange = _status._outValueRange.changeValue(var);
            showupStatus(_status);
        }
    }//GEN-LAST:event_jButtonValueDecActionPerformed

    
    MX36Folder _newFolderResult = null;
    
    MX36Folder createFolder(JButton button) {
        _newFolderResult = null;
        MXPopupForText navi = new MXPopupForText(null) {
            @Override
            public void approvedText(String text) {
                if (_process._folderList.getFolder(text) != null) {
                    JOptionPane.showMessageDialog(jButtonMoveFolder, "Already Exists", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (text.startsWith("*")) {
                    JOptionPane.showMessageDialog(jButtonMoveFolder, "Can' create * starting name", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                MX36Folder newFolder = _process._folderList.newFolder(text);
                _newFolderResult = newFolder;
                if (_process._view != null) {
                    _process._view.tabActivated();
                }
            }
        };
        navi.setDialogTitle("Input Folder Name");
        navi.simpleAskAsync(button);
        navi.waitForPopupClose();;
        return _newFolderResult;
    }
    
    private void jButtonMoveFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMoveFolderActionPerformed
        MXNamedObjectList<MX36Folder> listFolder = new MXNamedObjectList<>();
        for (MX36Folder folder : _process._folderList._listFolder) {
            listFolder.addNameAndValue(folder._folderName, folder);
        }
        listFolder.addNameAndValue("...", null);
        MXPopupForList<MX36Folder> navi = new MXPopupForList<MX36Folder>(null, listFolder) {
            @Override
            public void approvedIndex(int selectedIndex) {
                MX36Folder ret = listFolder.valueOfIndex(selectedIndex);
                if (ret == null) {
                    ret = createFolder(jButtonMoveFolder);
                    if (ret == null) {
                        return;
                    }
                }
                _process.moveFolder(ret, _status);
                _process._view.tabActivated();
                showupStatus(_status);
            }
        };

        navi.simpleAskAsync(jButtonMoveFolder);
    }//GEN-LAST:event_jButtonMoveFolderActionPerformed

    private void jButtonNewInFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewInFolderActionPerformed
        MXNamedObjectList<MX36Folder> listFolder = new MXNamedObjectList<>();
        for (MX36Folder folder : _process._folderList._listFolder) {
            listFolder.addNameAndValue(folder._folderName, folder);
        }
        listFolder.addNameAndValue("...", null);
        MXPopupForList<MX36Folder> navi = new MXPopupForList<MX36Folder>(null, listFolder) {
            @Override
            public void approvedIndex(int selectedIndex) {
                MX36Folder ret = listFolder.valueOfIndex(selectedIndex);
                if (ret == null) {
                    ret = createFolder(jButtonMoveFolder);
                    if (ret == null) {
                        return;
                    }
                }
                _status = new MX36Status();
                ret._accordion.setColorFull(true);
                _process.moveFolder(ret, _status);
                _process._view.tabActivated();
                showupStatus(_status);
            }
        };
        navi.simpleAskAsync(jButtonNewInFolder);
    }//GEN-LAST:event_jButtonNewInFolderActionPerformed

    private void jCheckBoxSurfaceReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSurfaceReplaceActionPerformed
        _status._surfaceReplace = jCheckBoxSurfaceReplace.isSelected();
    }//GEN-LAST:event_jCheckBoxSurfaceReplaceActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonMoveFolder;
    private javax.swing.JButton jButtonNewInFolder;
    private javax.swing.JButton jButtonOutTextClear;
    private javax.swing.JButton jButtonValueDec;
    private javax.swing.JButton jButtonValueInc;
    private javax.swing.JCheckBox jCheckBoxSurfaceReplace;
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
    private javax.swing.JLabel jLabel8;
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
    private javax.swing.JTextField jTextFieldSurfaceTypeAndRow;
    private javax.swing.JTextField jTextFieldValueValue;
    // End of variables declaration//GEN-END:variables
}
