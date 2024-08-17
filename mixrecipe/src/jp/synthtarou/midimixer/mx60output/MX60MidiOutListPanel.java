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
package jp.synthtarou.midimixer.mx60output;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_NotFound;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIInManager;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOut;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOutManager;
import jp.synthtarou.midimixer.mx63patch.MX63Process;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX60MidiOutListPanel extends javax.swing.JPanel {

    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTableDevice;
    private MX60Process _process;

    public MX60MidiOutListPanel(MX60Process process) {
        initComponents();
        _process = process;

        MXMIDIInManager manager = MXMIDIInManager.getManager();

        jTableDevice = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane(jTableDevice);

        jTableDevice.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableDevice.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = jTableDevice.getSelectedRow();
                if (row >= 0) {
                    MXMIDIOutManager manager = MXMIDIOutManager.getManager();
                    MXMIDIOut output = manager.listAllOutput().valueOfIndex(row);
                    _process.showMIDIOutDetail(output);
                }
            }
        });
        this.add(jScrollPane4);

        refreshList();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));
    }// </editor-fold>//GEN-END:initComponents

    public void refreshList() {
        MXMain.invokeUI(() ->  {
            MXMIDIOutManager manager = MXMIDIOutManager.getManager();
            manager.reloadDeviceList();

            jTableDevice.setModel(createDeviceModel());
            jTableDevice.getColumnModel().getColumn(3).setWidth(500);
	});
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    public TableModel createDeviceModel() {
        MXNamedObjectList<MXMIDIOut> allOutput = MXMIDIOutManager.getManager().listAllOutput();
        MXMIDIOutManager manager = MXMIDIOutManager.getManager();
        DefaultTableModel tableModel = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableModel.addColumn("Port");
        tableModel.addColumn("Assign");
        tableModel.addColumn("Open");
        tableModel.addColumn("Filter");

        for (MXMIDIOut output : allOutput.valueList()) {
            String prefix = "";
            if (output.getDriver().getDriverUID() == 0) { //not found
                prefix = "*";
            }
            tableModel.addRow(new Object[]{
                prefix + output.getName(),
                output.getPortAssignedAsText(),
                output.isOpen() ? "o" : "-",
                MX63Process.getFilterInfo(output)
            });
        }

        return tableModel;
    }

    public void updateDeviceTable() {
        DefaultTableModel model = (DefaultTableModel) jTableDevice.getModel();
        TableModel newModel = createDeviceModel();
        for (int i = 0; i < model.getRowCount(); ++i) {
            String name = (String) model.getValueAt(i, 0);
            String asssign = (String) model.getValueAt(i, 1);
            String open = (String) model.getValueAt(i, 2);

            String newName = (String) newModel.getValueAt(i, 0);
            String newAssign = (String) newModel.getValueAt(i, 1);
            String newOpen = (String) newModel.getValueAt(i, 2);
            String newSkip = (String) newModel.getValueAt(i, 3);

            if (name.equals(newName) == false) {
                MXFileLogger.getLogger(MX60MidiOutListPanel.class).warning("any trouble?");
                break;
            }

            model.setValueAt(newAssign, i, 1);
            model.setValueAt(newOpen, i, 2);
            model.setValueAt(newSkip, i, 3);
        }
    }
}
