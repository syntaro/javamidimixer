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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXStatic;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.MXUtilMidi;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_Empty;
import jp.synthtarou.midimixer.libs.swing.CheckBoxListCellRenderer;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIInManager;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOut;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOutManager;
import jp.synthtarou.midimixer.libs.swing.MXFileOpenChooser;
import org.xml.sax.SAXException;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX60MidiOutListPanel extends javax.swing.JPanel {
    private static final MXDebugPrint _debug = new MXDebugPrint(MX60MidiOutListPanel.class);
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTableDevice;

    static MX60MidiOutListPanel _lastInstance; 

    public static MX60MidiOutListPanel getLastInstance() {
        return _lastInstance;
    }

    public MX60MidiOutListPanel() {
        initComponents();
        
        _lastInstance = this;

        /*
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                //None
            }
        };
        jTableInputs.getActionMap().put("MY_CUSTOM_ACTION", action);
        jTableInputs.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "MY_CUSTOM_ACTION");
        jTableInputs.getColumnModel().getColumn(0).setMinWidth(150);*/

        MXMIDIInManager manager = MXMIDIInManager.getManager();

        jTableDevice = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane(jTableDevice);

        jTableDevice.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTableDeviceMousePressed(evt);
            }
        });
        jTableDevice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTableDeviceKeyPressed(evt);
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
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    refreshList();
                }
            });
            return;
        }
        MXMIDIOutManager manager = MXMIDIOutManager.getManager();
        manager.reloadDeviceList();

        jTableDevice.setModel(createDeviceModel());
        jTableDevice.getColumnModel().getColumn(0).setWidth(400);
        jTableDevice.getColumnModel().getColumn(1).setWidth(400);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    public TableModel createDeviceModel() {
        MXWrapList<MXMIDIOut> allOutput = MXMIDIOutManager.getManager().listAllOutput();
        MXMIDIOutManager manager = MXMIDIOutManager.getManager();
        DefaultTableModel tableModel = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableModel.addColumn("Port");
        tableModel.addColumn("Assign");
        tableModel.addColumn("Open");
        tableModel.addColumn("DominoXML");

        for (MXMIDIOut output : allOutput.valueList()) {
            String prefix = "";
            if (output.getDriver() instanceof MXDriver_Empty) {
                prefix = "*";
            }
            File dfile = output.getDXMLFile();
            String dfileName = "";
            if (dfile != null) {
                dfileName = dfile.getName();
            }
            tableModel.addRow(new Object[] { 
                prefix + output.getName(),
                output.getPortAssignedAsText(),
                output.isOpen() ? "o" : "-",
                dfileName
            });
        }

        return tableModel;
    }

    public void updateDeviceTable() {
        DefaultTableModel model = (DefaultTableModel)jTableDevice.getModel();
        TableModel newModel = createDeviceModel();
        for (int i = 0; i < model.getRowCount(); ++ i) {
            String name = (String)model.getValueAt(i, 0);
            String asssign = (String)model.getValueAt(i, 1);
            String open = (String)model.getValueAt(i, 2);
            String withD = (String)model.getValueAt(i, 3);
            
            String newName = (String)newModel.getValueAt(i, 0);
            String newAssign = (String)newModel.getValueAt(i, 1);
            String newOpen = (String)newModel.getValueAt(i, 2);
            String newWithD = (String)newModel.getValueAt(i, 3);
            
            if (name.equals(newName) == false) {
                _debug.println("any troubole?");
                break;
            }
            
            model.setValueAt(newAssign, i, 1);
            model.setValueAt(newOpen, i, 2);
            model.setValueAt(newWithD, i, 3);
        }
    }

    public void popupOutputPortSelect(int row) {
        JPopupMenu menu = createPopupMenuForPort(row);
        menu.show(jTableDevice, jTableDevice.getColumnModel().getColumns().nextElement().getWidth(), jTableDevice.getRowHeight(0) * (row + 1));
    }

    public JPopupMenu createPopupMenuForPort(final int row) {
        JPopupMenu popup = new JPopupMenu();
        
        for (int i = -1; i < MXStatic.TOTAL_PORT_COUNT; ++ i) {
            JMenuItem item;
            if (i < 0) {
                item = popup.add("(none)");
            }else {
                item = popup.add(MXUtilMidi.nameOfPortShort(i));
            }
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    JMenuItem item = (JMenuItem)arg0.getSource();
                    String itemText = item.getText();
                    int newAssign;
                    if (itemText.startsWith("(none")) {
                        newAssign = -1;
                    }else {
                        newAssign = MXUtilMidi.valueOfPortName(itemText);
                    }
                    MXMIDIOutManager manager = MXMIDIOutManager.getManager();
                    
                    DefaultTableModel model = null;
                    model = (DefaultTableModel)jTableDevice.getModel();
                    String text = (String)model.getValueAt(row, 0);
                    MXMIDIOut output = manager.listAllOutput().valueOfName(text);

                    if (newAssign >= 0) {
                        if (output.isPortAssigned(newAssign)) {
                            synchronized(MXTiming.mutex) {
                                output.allNoteOffFromPort(new MXTiming(), newAssign);
                            }
                        }
                        output.setPortAssigned(newAssign, !output.isPortAssigned(newAssign));
                    }else {
                        output.resetPortAssigned();
                    }
                    if (newAssign >= 0 && output.openOutput(0) == false) {
                        JOptionPane.showMessageDialog(MX60MidiOutListPanel.this, "Couldn't open " + text);
                    }
                    MX60MidiOutListPanel.this.updateDeviceTable();
                }
            });
        }
        return popup;
    }

    private void jTableDeviceKeyPressed(java.awt.event.KeyEvent evt) {                                         
        if (evt.getKeyChar() == ' ' || evt.getKeyChar() == '\n') {            
            popupOutputPortSelect(jTableDevice.getSelectedRow());
        }
    }                                        

    private void jTableDeviceMousePressed(java.awt.event.MouseEvent evt) {                                           
        int row = jTableDevice.rowAtPoint(evt.getPoint());
        int col = jTableDevice.columnAtPoint(evt.getPoint());
        if (col == 1) {
            popupOutputPortSelect(row);
        }
        if (col == 2) {
            DefaultTableModel tableModel = (DefaultTableModel)jTableDevice.getModel();
            String name = (String)tableModel.getValueAt(row, 0);
            MXWrapList<MXMIDIOut> allOutput = MXMIDIOutManager.getManager().listAllOutput();
            for (MXMIDIOut output : allOutput.valueList()) {
                if (output.getName().equals(name)) {
                    if (output.isOpen()) {
                        output.close();
                    }else {
                        if (output.openOutput(5) == false) {
                            JOptionPane.showMessageDialog(this, "Can't open " + output.getName());
                        }
                    }
                    break;
                }
            }
            updateDeviceTable();
        }       
        if (col == 3) {
            DefaultTableModel tableModel = (DefaultTableModel)jTableDevice.getModel();
            String portname = (String)tableModel.getValueAt(row, 0);
            MXMIDIOut out = MXMIDIOutManager.getManager().findMIDIOutput(portname);
            if (out == null) {
                return;
            }
            
            File dir = null;
            if (out.getDXMLFile() != null) {
                dir = MXFileOpenChooser.getExistDirectoryRecursive(out.getDXMLFile());
            }
            if (dir == null) {
                dir = MXFileOpenChooser.getStartDirectory();
            }
            MXFileOpenChooser chooser = new MXFileOpenChooser(dir);
            chooser.addExtension(".xml", "Domino XML File");
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String name = (String)tableModel.getValueAt(row, 0);
                MXWrapList<MXMIDIOut> allOutput = MXMIDIOutManager.getManager().listAllOutput();
                for (MXMIDIOut output : allOutput.valueList()) {
                    if (output.getName().equals(name)) {
                        try {
                            output.setDXMLFile(chooser.getSelectedFile());
                            updateDeviceTable();
                        }catch(SAXException e) {
                            JOptionPane.showMessageDialog(this, e.toString());
                        }
                    }
                }
            }
        }
    }    
}
