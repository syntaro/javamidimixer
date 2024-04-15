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
package jp.synthtarou.midimixer.mx10input;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.swing.JTableWithFooter;
import jp.synthtarou.midimixer.libs.swing.JTableWithColumnHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.port.MXPreprocessPanel;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachTableResize;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX10View extends javax.swing.JPanel {
    MX10MidiInListPanel _inPanel;
    JTableWithColumnHeader _jTableSkip;
    MX10ViewData _viewData;
    
    /**
     * Creates new form MX10View
     */
    public MX10View() {
        initComponents();

        _inPanel = new MX10MidiInListPanel();
        jPanelInputSelect.add(_inPanel);

        _jTableSkip = new JTableWithFooter(jPanel1);
        new MXAttachTableResize(_jTableSkip);

        _jTableSkip.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (_jTableSkip.isEnabled()) {
                    jTableSkip_MouseClicked(evt);
                }
            }
        });

        DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer();
        tableCellRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 1; i < _jTableSkip.getColumnCount(); ++ i) {
            TableColumn col = _jTableSkip.getColumnModel().getColumn(i);
            col.setCellRenderer(tableCellRenderer);
        }
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                splitAuto();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                splitAuto();
            }

        });
        
        MXPreprocessPanel manager = MXPreprocessPanel.getInstance();
        jPanelPreprocessor.removeAll();
        jPanelPreprocessor.setLayout(new BoxLayout(jPanelPreprocessor, BoxLayout.Y_AXIS));
        jPanelPreprocessor.add(manager);
    }
    
    public void splitAuto() {
        jSplitPane1.setDividerLocation(Math.max(jSplitPane1.getWidth() * 70 / 100, jSplitPane1.getWidth() - 400));
    }
    
    private void jTableSkip_MouseClicked(java.awt.event.MouseEvent evt) {                                     
        int row = _jTableSkip.rowAtPoint(evt.getPoint());
        int column = _jTableSkip.columnAtPoint(evt.getPoint());
        
        int type = row;
        int port = column - 1;
        DefaultTableModel model = (DefaultTableModel)_jTableSkip.getModel();
        
        if (port >= 0 && port < MXConfiguration.TOTAL_PORT_COUNT) {
            String var = (String)model.getValueAt(row, column);
            if (var == null || var.length() == 0) {
                setSkipDX(row, column, true);
            }
            else {
                setSkipDX(row, column, false);
            }
        }
    }                                    
    
    public void setSkipDX(int row, int column, boolean skip) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                setSkipDX(row, column, skip);
            });
            return;
        }
        if (_viewData != null) {
            int type = row;
            int port = column - 1;
            _viewData.setSkip(port, type, skip);
        }
        DefaultTableModel model = (DefaultTableModel)_jTableSkip.getModel();
        if (skip) {
            model.setValueAt("Skip", row, column);
        }
        else {
            model.setValueAt("", row, column);
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

        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanelSkip = new javax.swing.JPanel();
        jCheckBoxUseSkip = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jPanelInputSelect = new javax.swing.JPanel();
        jPanelPreprocessor = new javax.swing.JPanel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jSplitPane1.setDividerLocation(400);

        jSplitPane2.setDividerLocation(240);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanelSkip.setBorder(javax.swing.BorderFactory.createTitledBorder("2.Input Filter"));
        jPanelSkip.setLayout(new java.awt.GridBagLayout());

        jCheckBoxUseSkip.setText("Use This Recipe");
        jCheckBoxUseSkip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseSkipActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelSkip.add(jCheckBoxUseSkip, gridBagConstraints);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelSkip.add(jPanel1, gridBagConstraints);

        jSplitPane2.setBottomComponent(jPanelSkip);

        jPanelInputSelect.setBorder(javax.swing.BorderFactory.createTitledBorder("1.Input Assign"));
        jPanelInputSelect.setLayout(new javax.swing.BoxLayout(jPanelInputSelect, javax.swing.BoxLayout.LINE_AXIS));
        jSplitPane2.setTopComponent(jPanelInputSelect);

        jSplitPane1.setLeftComponent(jSplitPane2);

        jPanelPreprocessor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanelPreprocessor.setLayout(new javax.swing.BoxLayout(jPanelPreprocessor, javax.swing.BoxLayout.LINE_AXIS));
        jSplitPane1.setRightComponent(jPanelPreprocessor);

        add(jSplitPane1);
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxUseSkipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseSkipActionPerformed
        _viewData._isUsingThieRecipe = jCheckBoxUseSkip.isSelected();
    }//GEN-LAST:event_jCheckBoxUseSkipActionPerformed

    public synchronized TableModel createSkipTableModel(MX10ViewData data) {
        DefaultTableModel model = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        model.addColumn("");
        
        for (int i = 0; i < MXConfiguration.TOTAL_PORT_COUNT; ++ i) {
            model.addColumn(MXMidi.nameOfPortShort(i));
        }
        
        for (int row = 0; row  < MX10ViewData.TYPE_COUNT; ++ row) {
            Vector line = new Vector();
            line.add(MX10ViewData._typeNames[row]);
            
            for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
                int type = row ;
                if (data.isSkip(port, type)) {
                    line.add("Skip");
                }else {
                    line.add("");
                }
            }
            model.addRow(line);
        }
        if (true) {
            Vector line = new Vector();
            line.add("");

            for (int i = 0; i < MXConfiguration.TOTAL_PORT_COUNT; ++ i) {
                line.add(MXMidi.nameOfPortShort(i));
            }
        }
        return model;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxUseSkip;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelInputSelect;
    private javax.swing.JPanel jPanelPreprocessor;
    private javax.swing.JPanel jPanelSkip;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    // End of variables declaration//GEN-END:variables

    public void setViewData(MX10ViewData viewData) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                setViewData(viewData);
            });
            return;
        }
        _viewData = viewData;
        _jTableSkip.setModel(createSkipTableModel(viewData));
        jCheckBoxUseSkip.setSelected(_viewData._isUsingThieRecipe);
        //_jTableSkip.getColumnModel().getColumn(0).setMinWidth(150);
        new MXAttachTableResize(_jTableSkip);
    }
    
    public void tabActivated() {
        _inPanel.refreshList();
    }

    public static final int TYPE_ALL = 0;
    public static final int TYPE_NOTE = 1;
    public static final int TYPE_DAMPER_PEDAL = 2;
    public static final int TYPE_PITCH_BEND = 3; 
    public static final int TYPE_MOD_WHEEL = 4;
    public static final int TYPE_BANK_SELECT = 5;
    public static final int TYPE_PROGRAM_CHANGE  = 6;
    public static final int TYPE_DATA_ENTRY = 7;
    public static final int TYPE_ANOTHER_CC = 8;
    public static final int TYPE_RESET_GENERAL = 9;
    public static final int TYPE_SYSEX = 10;
    public static final int TYPE_ACTIVE_SENSING = 11;

    public static final String[] typeNames = {
        "All", "Note", "DamperPedal", "PitchBend", "ModWheel", "BankChange",
        "ProgramChange", "DataEntry", "AnotherCC", "GM&GS&XGReset", "SysEX", 
        "Active&Clock", 
        
    };
}
