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
package jp.synthtarou.midimixer.libs.midi.port;

import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXVisitantRecorder implements TableModel {
    private ArrayList<MXVisitant16> _element;

    public MXVisitantRecorder() {
        _element = new ArrayList();
        for (int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++ port) {
            _element.add(new MXVisitant16());
        }
    }

    public MXVisitant16 getVisitant16(int port) {
        return _element.get(port);
    }

    public MXVisitant getVisitant(int port, int ch) {
        return _element.get(port).get(ch);
    }

    public boolean updateVisitant16WithMessage(MXMessage message) {
        MXVisitant visitant = _element.get(message.getPort()).get(message.getChannel());
        if (visitant.updateVisitantChannel(message)) {
            //System.out.println("updated " + visitant);
            invokeListener();
            return true;
        }
        return false;
    }

    public boolean mergeVisitant16WithVisitant(MXMessage message) {
        MXVisitant visitant = _element.get(message.getPort()).get(message.getChannel());
        if (visitant.mergeNew(message.getVisitant())) {
            //System.out.println("merged " + visitant);
            invokeListener();
            return true;
        }
        return false;
    }

    public MXMessage incProg(int port, MXTiming timing, int channel) {
        MXVisitant e = getVisitant(port, channel);
        if (e.getProgram() < 127) {
            e.setProgram(e.getProgram() + 1);
            invokeListener();
        }else {
            return null;
        }
        MXMessage message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_PROGRAMCHANGE + channel, e.getProgram(), 0);
        message._timing = timing;
        return message;
    }

    public MXMessage decProg(int port, MXTiming timing, int channel) {
        MXVisitant e = getVisitant(port, channel);
        if (e.getProgram() > 0) {
            e.setProgram(e.getProgram() - 1);            
            invokeListener();
        }else {
            return null;
        }
        MXMessage message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_PROGRAMCHANGE + channel, e.getProgram(), 0);
        message._timing = timing;
        return message;
    }


    String[] tableColumns =  {
        "Port/Ch", "Bank", "Prog-1", "Vol",  "Exp", "Pan", "Data"
    };
    @Override
    public int getRowCount() {
        return _element.size() * 16;
    }

    @Override
    public int getColumnCount() {
        return tableColumns.length;
    }

    @Override
    public String getColumnName(int i) {
        return tableColumns[i];
    }

    @Override
    public Class<?> getColumnClass(int i) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int port = rowIndex / 16;
        int channel = rowIndex-  (port * 16);
        MXVisitant info = getVisitant(port, channel);
        
        switch(columnIndex) {
            case 0:
                return MXMidi.nameOfPortShort(port) + (channel+1);
            case 1:
                if (info._havingBank != MXVisitant.HAVE_VAL_NOT) {
                    return MXUtil.toHexFF(info.getBankMSB()) + ":" + MXUtil.toHexFF(info.getBankLSB());
                }
                break;
            case 2:
                if (info.isHavingProgram()) {
                    return Integer.toString(info.getProgram());
                }
                break;
            case 3:
                if (info.isHavingVolume()) {                    
                    return Integer.toString(info.getInfoVolume());
                }
                break;
            case 4:
                if (info.isHavingExpression()) {
                    return Integer.toString(info.getInfoExpression());
                }
                break;
            case 5:
                if (info.isHavingPan()) {
                    return Integer.toString(info.getInfoPan());
                }
                break;
            case 6:
                if (info.isHaveDataentryRPN()) {
                    return "R(" + MXUtil.toHexFF(info.getDataentryMSB()) + ":" + MXUtil.toHexFF(info.getDataentryLSB()) + ")=" + info.getDataentryValue14();
                }else if (info.isHaveDataentryNRPN()) {
                    return "N(" + MXUtil.toHexFF(info.getDataentryMSB()) + ":" + MXUtil.toHexFF(info.getDataentryLSB()) + ")=" + info.getDataentryValue14();
                }
                break;
        }
        return "-";
    }

    @Override
    public void setValueAt(Object o, int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    LinkedList<TableModelListener> _listeners = new LinkedList();
    
    @Override
    public void addTableModelListener(TableModelListener l) {
        synchronized(_listeners) {
            _listeners.add(l);
        }
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        synchronized(_listeners) {
            _listeners.remove(l);
        }
    }

    public void invokeListener() {
        for (TableModelListener l : _listeners) {
            TableModelEvent e = new TableModelEvent(MXVisitantRecorder.this, 0, getRowCount());
            l.tableChanged(e);
        }
    }
}
