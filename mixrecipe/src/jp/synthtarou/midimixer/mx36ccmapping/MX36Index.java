/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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

import java.util.ArrayList;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.mx30surface.MGStatus;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36Index {
    static class Column extends ArrayList<MX36StatusPanel> {
    
    }

    static class PortData {
        ArrayList<Column>[] _listPocket;

        public PortData() {
            int totalRow = typeToIndex(-1, 0);
            _listPocket = new ArrayList[totalRow + 1];
            
            for (int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++ row) {
                _listPocket[typeToIndex(MGStatus.TYPE_CIRCLE, row)] = new ArrayList<Column>();
            }
            for (int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++ row) {
                _listPocket[typeToIndex(MGStatus.TYPE_SLIDER, row)] = new ArrayList<Column>();
            }
            for (int row = 0; row < MXAppConfig.DRUM_ROW_COUNT; ++ row) {
                _listPocket[typeToIndex(MGStatus.TYPE_DRUMPAD, row)] = new ArrayList<Column>();
            }
        }
    }

    PortData[] _listPort;

    public void safeAdd(MX36StatusPanel panel) {
        MX36Status status = panel._status;
        int port = status._surfacePort;
        int type = status._surfaceUIType;
        int row = status._surfaceRow;
        int column = status._surfaceColumn;
        
        if (_listPort == null) {
            _listPort = new PortData[MXAppConfig.TOTAL_PORT_COUNT];
        }
        PortData portData = _listPort[port];
        if (portData == null) {
            portData = new PortData();
            _listPort[port] = portData;
        }
        int x = typeToIndex(type, row);
        ArrayList<Column>columnList =  portData._listPocket[x];
        if (columnList == null) {
            columnList = new ArrayList();
            portData._listPocket[x] = columnList;
        }
        while(column >= columnList.size()) {
            columnList.add(null);
        }
        Column col = columnList.get(column);
        if (col == null) {
            col = new Column();
            columnList.set(column, col);
        }
        if (col.indexOf(status) >= 0) {
            return;
        }
        col.add(panel);
    }
    
    public ArrayList<MX36StatusPanel> safeGet(int port, int type, int row, int column) {
        if (_listPort == null) {
            return null;
        }
        if (_listPort[port] == null) {
            return null;
        }
        PortData portData = _listPort[port];
        if (portData == null) {
            return null;
        }
        ArrayList<Column>columnList =  portData._listPocket[typeToIndex(type, row)];
        if (columnList == null) {
            return null;
        }
        if(column >= columnList.size()) {
            return null;
        }
        return columnList.get(column);
    }

    public void safeRemove(MX36StatusPanel panel) {
        MX36Status status = panel._status;
        int port = status._surfacePort;
        int type = status._surfaceUIType;
        int row = status._surfaceRow;
        int column = status._surfaceColumn;
        ArrayList<MX36StatusPanel> hit = safeGet(port, type, row, column);
        if (hit !=null) {
            hit.remove(panel);
        }
    }
    
    static int typeToIndex(int type, int row) {
        switch(type) {
            case MGStatus.TYPE_CIRCLE:
                return row;
            case MGStatus.TYPE_SLIDER:
                return row + MXAppConfig.CIRCLE_ROW_COUNT;
            case MGStatus.TYPE_DRUMPAD:
                return row + MXAppConfig.CIRCLE_ROW_COUNT + MXAppConfig.SLIDER_ROW_COUNT;
        }
        return row + MXAppConfig.CIRCLE_ROW_COUNT + MXAppConfig.SLIDER_ROW_COUNT + MXAppConfig.DRUM_ROW_COUNT;
    }
}
