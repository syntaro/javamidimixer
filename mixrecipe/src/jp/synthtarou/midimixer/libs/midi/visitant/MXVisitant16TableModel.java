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
package jp.synthtarou.midimixer.libs.midi.visitant;

import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXVisitant16TableModel implements TableModel {
    private ArrayList<MXVisitant16> _element;

    public MXVisitant16TableModel() {
        _element = new ArrayList();
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            _element.add(new MXVisitant16());
        }
    }

    public MXVisitant16 getVisitant16(int port) {
        return _element.get(port);
    }

    public MXVisitant getVisitant(int port, int ch) {
        return _element.get(port).get(ch);
    }
    
    
    public MXMessage preprocess16ForVisitant(MXMessage message) {
        MXVisitant visitant = _element.get(message.getPort()).get(message.getChannel());
        MXMessage ret2 = visitant.catchTheVisitant(message);
        if (ret2 == null) {
            return null;
        }
        return ret2;
    }

    public boolean mergeVisitant16WithVisitant(MXMessage message) {
        MXVisitant visitant = _element.get(message.getPort()).get(message.getChannel());
        if (visitant.mergeNew(message.getVisitant()) > 0) {
            invokeListener("merge [" + visitant._currentAge + "] = " + message.getVisitant());
            return true;
        }
        return false;
    }


    String[] tableColumns =  {
        "Port/Ch", "Bank", "Prog +1", "Vol",  "Exp", "Pan"
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
                return MXMidiStatic.nameOfPortShort(port) + (channel+1);
            case 1:
                if (info.isHavingBank()) {
                    return MXUtil.toHexFF(info.getBankMSB()) + ":" + MXUtil.toHexFF(info.getBankLSB());
                }
                break;

            case 2:
                if (info.isHavingProgram()) {
                    return Integer.toString(info.getProgram() +1);
                }
                break;
            case 3:
                if (info.isCCSet(MXMidiStatic.DATA1_CC_CHANNEL_VOLUME)) {                    
                    return Integer.toString(info.getCCValue(MXMidiStatic.DATA1_CC_CHANNEL_VOLUME));
                }
                break;
            case 4:
                if (info.isCCSet(MXMidiStatic.DATA1_CC_EXPRESSION)) {                    
                    return Integer.toString(info.getCCValue(MXMidiStatic.DATA1_CC_EXPRESSION));
                }
                break;
            case 5:
                if (info.isCCSet(MXMidiStatic.DATA1_CC_PANPOT)) {                    
                    return Integer.toString(info.getCCValue(MXMidiStatic.DATA1_CC_PANPOT));
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

    public void invokeListener(Object message) {
        MXMain.invokeUI(() ->  {
            for (TableModelListener l : _listeners) {
                TableModelEvent e = new TableModelEvent(MXVisitant16TableModel.this, 0, getRowCount());
                l.tableChanged(e);
            }
        });
    }
}
