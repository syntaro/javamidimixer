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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIInManager;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachTableResize;
import jp.synthtarou.midimixer.libs.vst.VSTFolder;
import jp.synthtarou.midimixer.mx00playlist.PlayListDX;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX10MidiInListPanel extends javax.swing.JPanel {
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTableDevice;

    static MX10MidiInListPanel _lastInstance; 

    public static MX10MidiInListPanel getLastInstance() {
        return _lastInstance;
    }
    
    public MX10MidiInListPanel() {
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

        add(jScrollPane4);
        new MXAttachTableResize(jTableDevice);
        
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

        setBorder(javax.swing.BorderFactory.createTitledBorder(""));
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
        MXMIDIInManager manager = MXMIDIInManager.getManager();
        manager.reloadDeviceList();

        jTableDevice.setModel(createDeviceModel());
        jTableDevice.getColumnModel().getColumn(0).setWidth(400);
        jTableDevice.getColumnModel().getColumn(1).setWidth(400);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    public TableModel createDeviceModel() {
        MXNamedObjectList<MXMIDIIn> allInput = MXMIDIInManager.getManager().listAllInput();
        MXMIDIInManager manager = MXMIDIInManager.getManager();
        DefaultTableModel tableModel = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableModel.addColumn("Port");
        tableModel.addColumn("Assign");
        tableModel.addColumn("Open");

        for (MXMIDIIn input : allInput.valueList()) {
            String prefix = "";
            /*
            if (input.getDriver() instanceof MXDriver_Empty) {
                prefix = "*";
            }*/
            tableModel.addRow(new Object[] { 
                prefix + input.getName(),
                input.getPortAssignedAsText(),
                input.isOpen() ? "o" : "-"
                /*,input.textForMasterChannel() */
            });
        }
        
        return tableModel;
    }
    
    public void updateDeviceTable() {
        DefaultTableModel model = (DefaultTableModel)jTableDevice.getModel();
        TableModel newModel = createDeviceModel();
        for (int i = 0; i < model.getRowCount(); ++ i) {
            String name = (String)model.getValueAt(i, 0);
            String value = (String)model.getValueAt(i, 1);
            String opened = (String)model.getValueAt(i, 2);
            //String master = (String)model.getValueAt(i, 3);
            
            String newName = (String)newModel.getValueAt(i, 0);
            String newValue = (String)newModel.getValueAt(i, 1);
            String newOpen = (String)newModel.getValueAt(i, 2);
            //String newMaster = (String)newModel.getValueAt(i, 3);
            
            if (name.equals(newName) == false) {
                MXFileLogger.getLogger(MX10MidiInListPanel.class).warning("any troubole?");
                break;
            }
            
            model.setValueAt(newValue, i, 1);
            model.setValueAt(newOpen, i, 2);
            //model.setValueAt(newMaster, i, 3);
        }
    }
    
    public void popupInputPortSelect(int row) {
        JPopupMenu menu = createPopupMenuForPort(row);
        menu.show(jTableDevice, jTableDevice.getColumnModel().getColumns().nextElement().getWidth(), jTableDevice.getRowHeight(0) * (row + 1));
    }

    public JPopupMenu createPopupMenuForPort(final int row) {
        JPopupMenu popup = new JPopupMenu();
        
        for (int i = -1; i < MXConfiguration.TOTAL_PORT_COUNT; ++ i) {
            JMenuItem item;
            if (i < 0) {
                item = popup.add("(none)");
            }else {
                item = popup.add(MXMidi.nameOfPortShort(i));
            }
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    JMenuItem item = (JMenuItem)arg0.getSource();
                    String itemText = item.getText();
                    int newAssign;
                    if (itemText.startsWith("(")) {
                        newAssign = -1;
                    }else {
                        newAssign = MXMidi.valueOfPortName(itemText);
                    }
                    MXMIDIInManager manager = MXMIDIInManager.getManager();
                    
                    DefaultTableModel model = null;
                    model = (DefaultTableModel)jTableDevice.getModel();
                    String text = (String)model.getValueAt(row, 0);
                    MXMIDIIn input = manager.listAllInput().valueOfName(text);
                    
                    if (newAssign >= 0) {
                        if (input == null) {
                            MXFileLogger.getLogger(MX10MidiInListPanel.class).severe("Can't create / Wrong way");
                            return;
                        }
                        if (input.isPortAssigned(newAssign)) {
                            input.allNoteOffToPort(null, newAssign);
                        }
                        input.setPortAssigned(newAssign, !input.isPortAssigned(newAssign));
                    }else {
                        input.resetPortAssigned();
                    }
                    if (newAssign >= 0 && input.openInput(5) == false) {
                        JOptionPane.showMessageDialog(MX10MidiInListPanel.this, "Error when opening " + text);
                    }
                    updateDeviceTable();
                }
            });
        }
        return popup;
    }

    private void jTableDeviceKeyPressed(java.awt.event.KeyEvent evt) {                                         
        if (evt.getKeyChar() == ' ' || evt.getKeyChar() == '\n') {            
            popupInputPortSelect(jTableDevice.getSelectedRow());
        }
    }                                        

    private void jTableDeviceMousePressed(java.awt.event.MouseEvent evt) {                                           
        int row = jTableDevice.rowAtPoint(evt.getPoint());
        int col = jTableDevice.columnAtPoint(evt.getPoint());
        if (col == 1) {
            popupInputPortSelect(row);
        }
        if (col == 2) {
            toggleOpen(row);
        }
        if (col == 3) {
            //popupSetMaster(row);
        }
    }                                          

    /*
    public class ListenerForSetMaster implements ActionListener {
        MXMIDIIn _input;
        int _channel;

        public ListenerForSetMaster(MXMIDIIn from, int channel) {
            _input = from;
            _channel = channel;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (_input.isToMaster(_channel)) {
                _input.setToMaster(_channel, false);
            }else {
                _input.setToMaster(_channel, true);
            }
            updateDeviceTable();
        }
    }
*/
    
    public void toggleOpen(int row) {
        DefaultTableModel model = null;
        model = (DefaultTableModel)jTableDevice.getModel();
        String text = (String)model.getValueAt(row, 0);
        MXMIDIIn input = MXMIDIInManager.getManager().listAllInput().valueOfName(text);

        if (input.isOpen()) {
            input.allNoteOff(null);
            input.close();
        }else {
            input.openInput(5);
        }
        updateDeviceTable();
    }
/*
    public void popupSetMaster(int row) {
        try {
            DefaultTableModel model = null;
            model = (DefaultTableModel)jTableDevice.getModel();
            String text = (String)model.getValueAt(row, 0);
            MXMIDIIn input = MXMIDIInManager.getManager().listAllInput().valueOfName(text);

            JPopupMenu jpopup = new JPopupMenu();

            for (int ch = 0; ch < 16; ++ ch) {
                boolean sel = input.isToMaster(ch);
                String portName = input.getPortAssignedAsText();
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(portName + Integer.toString(ch + 1));
                if (sel) {
                    item.setSelected(true);
                }else {
                    item.setSelected(false);
                }
                item.addActionListener(new ListenerForSetMaster(input, ch));
                jpopup.add(item);
            }
            
            int target = 3;
            int width = 0;
           
            for (int x = 0; x < target; ++ x) {
                TableColumn col = jTableDevice.getColumnModel().getColumn(x);
                width += col.getWidth();
            }

            jpopup.show(jTableDevice, width, jTableDevice.getRowHeight(0) * (row + 1));
        }catch(runtimeException e) {
            MXLogger2.getLogger(MX10MidiInListPanel.class).log(Level.WARNING, ex.getMessage(), ex);
        }   
    }
  */  
}
