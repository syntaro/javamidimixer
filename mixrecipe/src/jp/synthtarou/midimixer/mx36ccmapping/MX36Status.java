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

import java.util.Collections;
import java.util.logging.Level;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.libs.namedobject.MXNamedObjectListFactory;
import jp.synthtarou.midimixer.mx30surface.MGStatus;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36Status {
    int _surfacePort;
    int _surfaceUIType;
    int _surfaceRow;
    int _surfaceColumn;
    MXRangedValue _surfaceValueRange = new MXRangedValue(0, 0, 127);
   
    MGStatus _surface;
    
    String _outName;
    String _outMemo;
    private String _outDataText;
    int _outPort;
    MXMessage _outCachedMessage;
    int _outChannel;

    MXRangedValue _outValueRange;
    int _outValueOffset = 0;
    MXNamedObjectList<Integer> _outValueTable;
    
    MXRangedValue _outGateRange;
    int _outGateOffset = 0;
    MXNamedObjectList<Integer> _outGateTable;
    
    boolean _outGateTypeKey;
    
    static int findRowTypeFromValue(int value) {
        return (value >> 8) & 0xff;
    }
    
    static int findRowNumberFromValue(int value) {
        return value & 0xff;
    }
    
    static int makeRowValue(int rowType, int rowNumber) {
        return (rowType << 8) | rowNumber;
    }
    
    static MXNamedObjectList<Integer> _listRowType;

    static {
        _listRowType = new MXNamedObjectList<>();
        _listRowType.addNameAndValue("-", -1);
        for (int i = 0; i < MXConfiguration.CIRCLE_ROW_COUNT; ++ i) {
            String caption = "Knob";
            if (i != 0) {
                caption += Integer.toString(i + 1);
            }
            _listRowType.addNameAndValue(caption, makeRowValue(MGStatus.TYPE_CIRCLE, i));
        }
        for (int i = 0; i < MXConfiguration.SLIDER_ROW_COUNT; ++ i) {
            String caption = "Slider";
            if (i != 0) {
                caption += Integer.toString(i + 1);
            }
            _listRowType.addNameAndValue(caption, makeRowValue(MGStatus.TYPE_SLIDER, i));
        }
        for (int i = 0; i < MXConfiguration.DRUM_ROW_COUNT; ++ i) {
            String caption = "Drum";
            if (i != 0) {
                caption += Integer.toString(i + 1);
            }
            _listRowType.addNameAndValue(caption, makeRowValue(MGStatus.TYPE_DRUMPAD, i));
        }
    }
    
    public String getOutValueLabel() {
        String text = null;
        if (_outValueTable != null) {
            text = _outValueTable.nameOfValue(_outValueRange._value);
        }
        if (text == null) {
            text =  Integer.toString(_outValueRange._value);
        }
        return text;
    }

    public String getOutGateLabel() {
        String text = null;
        if (_outGateTable != null) {
            text = _outGateTable.nameOfValue(_outGateRange._value);
        }
        if (text == null) {
            text =  Integer.toString(_outGateRange._value);
        }
        return text;
    }
    
    int _bind1RCH, _bind2RCH, _bind4RCH;
    int _bindRSCTRT1, _bindRSCTRT2, _bindRSCTRT3;
    int _bindRSCTPT1, _bindRSCTPT2, _bindRSCTPT3;
    
    MX36Folder _folder;
    
    MX36StatusPanel _panel;
    
    public String getOutDataText() {
        if (_outDataText == null) {
            return null;
        }
        if (_outDataText.isEmpty()) {
            return null;
        }
        return _outDataText;
    }

    public void setOutDataText(String text) {
        _outDataText = text;
        _outCachedMessage = null;
        createOutMessage();
    }
    
    public MX36Status() {
        _outPort = -1;
        _folder = null;
        _outDataText = null;
        _outChannel = -1;

        _outGateRange = new MXRangedValue(64, 0, 127);
        _outGateOffset = 0;
        _outGateTable = null;
        _outValueRange = new MXRangedValue(32, 0, 127);
        _outValueOffset = 0;
        _outValueTable = null;
    }
    
    public String toSurfaceText() {
        int x = MX36Status.makeRowValue(_surfaceUIType, _surfaceRow);
        String row = _listRowType.nameOfValue(x);
        return "#" + Character.toString('A'+_surfacePort) + "-" + row + "-" + (_surfaceColumn+1);
    }
    
    public String toOutputPortText() {
        String portText = "OutPort = as input";
        if (_outPort >= 0) {
            portText = "OutPort =" + Character.toString('A'+_outPort);
        }
        String channelText = "Ch = as input";
        if (_outChannel >= 0) {
            return "Ch= " + (_outChannel + 1);
        }
        return portText +", " + channelText;
    }

    public String toString() {
        return toSurfaceText() + "=" + _outValueRange._value;
    }
    
    public static MX36Status fromMGStatus(MGStatus status) {
        MX36Status it = new MX36Status();
                
        it._outName = status.getAsName();
        
        it._surfacePort = status._port;
        it._surfaceUIType = status._uiType;
        it._surfaceRow = status._row;
        it._surfaceColumn = status._column;
        it._surfaceValueRange = status.getValue();
        it._surface = status;

        it._outChannel = status.getChannel();
        it._outValueRange = status.getValue();
        it._outValueTable = null;
        
        return it;
    }

    public String toTreeString() {
        return "";
    }
    
    public MXNamedObjectList<Integer> createTable(MXRangedValue range) {
        MXNamedObjectList<Integer> list = new MXNamedObjectList<>();
        for (int i = range._min; i <= range._max; ++ i) {
            list.addNameAndValue(String.valueOf(i), i);
        }
        Collections.reverse(list);
        return list;
    }
    
    int compareSurfacePositionColumn(MX36Status status) {
        return MX36Status.this.compareSurfacePositionColumn(status._surfacePort, status._surfaceUIType, status._surfaceRow, status._surfaceColumn);
    }

    int compareSurfacePositionColumn(int port, int uiType, int row, int column) {
        if (_surfacePort < port) return -1;
        if (_surfacePort > port) return 1;
        if (_surfaceColumn < column) return -1;
        if (_surfaceColumn > column) return 1;
        if (_surfaceUIType < uiType) return -1;
        if (_surfaceUIType > uiType) return 1;
        if (_surfaceRow < row) return -1;
        if (_surfaceRow > row) return 1;
        return 0;
    }
    
    int compareSurfacePositionRow(MX36Status status) {
        return MX36Status.this.compareSurfacePositionColumn(status._surfacePort, status._surfaceUIType, status._surfaceRow, status._surfaceColumn);
    }

    int compareSurfacePositionRow(int port, int uiType, int row, int column) {
        if (_surfacePort < port) return -1;
        if (_surfacePort > port) return 1;
        if (_surfaceUIType < uiType) return -1;
        if (_surfaceUIType > uiType) return 1;
        if (_surfaceRow < row) return -1;
        if (_surfaceRow > row) return 1;
        if (_surfaceColumn < column) return -1;
        if (_surfaceColumn > column) return 1;
        return 0;
    }

    public MXMessage createOutMessage() {
        if (_outDataText == null || _outDataText.length() == 0) {
            return null;
        }
        if (_outCachedMessage == null) {
            try {
                _outCachedMessage = MXMessageFactory.fromCCXMLText(_outValueOffset, _outDataText, _outChannel);
            }catch(Exception ex) {
                MXFileLogger.getLogger(MX36Status.class).log(Level.WARNING, ex.getMessage(), ex);
            }
            if (_outCachedMessage == null) {
                _outDataText = null;
                return null;
            }
        }
        if (_outCachedMessage.getStatus() == 0) {
            _outDataText = null;
            return null;
        }
        int port = _surfacePort;
        int channel = _outChannel;
        if (_outPort >= 0) {
            port = _outPort;
        }

        _outCachedMessage.setPort(port);
        _outCachedMessage.setChannel(channel);
        _outCachedMessage.setValue(_outValueRange.changeRange(_outCachedMessage.getValue()._min, _outCachedMessage.getValue()._max));
        _outCachedMessage.setGate(_outGateRange);
        _outCachedMessage._timing = null;

        return (MXMessage)_outCachedMessage.clone();
    }
    
    public boolean isValidForWork() {
        if (_surfacePort < 0) return false;
        if (_surfaceUIType < 0) return false;
        if (_surfaceRow < 0) return false;
        if (_surfaceColumn < 0) return false;
        if (_outDataText == null) return false;
        if (_outDataText.isBlank()) return false;
        return true;
    }
    
    static MXNamedObjectList<Integer> listForDefault = MXNamedObjectListFactory.listupRange(0, 127);

    public MXNamedObjectList<Integer> safeGateTable() {
        if (_outGateTable != null) {
            return _outGateTable;
        }
        MXRangedValue range = _outGateRange;
        if (range._min == 0 && range._max == 127) {
            return listForDefault;
        }
        MXNamedObjectList<Integer> listForGate = MXNamedObjectListFactory.listupRange(range._min, range._max);
        return listForGate;
    }

    public MXNamedObjectList<Integer> safeValueTable() {
        if (_outValueTable != null) {
            return _outValueTable;
        }
        MXRangedValue range = _outValueRange;
        if (range._min == 0 && range._max == 127) {
            return listForDefault;
        }
        MXNamedObjectList<Integer> listForValue = MXNamedObjectListFactory.listupRange(range._min, range._max);
        return listForValue;
    }
    
}
