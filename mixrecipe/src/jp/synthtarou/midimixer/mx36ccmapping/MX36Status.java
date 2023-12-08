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
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXWrapListFactory;
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
    RangedValue _surfaceValueRange = new RangedValue(0, 0, 127);
    
    MGStatus _surfaceStatusCache;
    
    String _outName;
    String _outMemo;
    String _outData;
    int _outPort;
    MXMessage _outCachedMessage;
    int _outChannel;

    RangedValue _outValueRange;
    
    int _outValueOffset = 0;
    MXWrapList<Integer> _outValueTable;
    
    RangedValue _outGateRange;
    int _outGateOffset = 0;
    MXWrapList<Integer> _outGateTable;
    boolean _outGateTypeKey;
    
    String _bind1RCH, _bind2RCH, _bind4RCH;
    String _bindRSCTRT1, _bindRSCTRT2, _bindRSCTRT3;
    String _bindRSCTPT1, _bindRSCTPT2, _bindRSCTPT3;
    
    public MX36Status() {
        _outPort = -1;
        _outData = null;
        _outChannel = -1;

        _outGateRange = new RangedValue(64, 0, 127);
        _outGateOffset = 0;
        _outGateTable = MXWrapListFactory.listupRange(_outGateRange._min, _outGateRange._max);
        _outValueRange = new RangedValue(32, 0, 127);
        _outValueOffset = 0;
        _outValueTable = MXWrapListFactory.listupRange(_outValueRange._min, _outValueRange._max);

        _bind1RCH = "1r";
        _bind2RCH = "2r";
        _bind4RCH = "4r";
        _bindRSCTRT1 = "rt1";
        _bindRSCTRT2 = "rt2";
        _bindRSCTRT3 = "rt3";
        _bindRSCTPT1 = "pt1";
        _bindRSCTPT2 = "pt2";
        _bindRSCTPT3 = "pt3";
    }
    
    public String toString() {
        return "#" + Character.toString('A'+_surfacePort) + "-" + MGStatus.getRowAsText(_surfaceUIType, _surfaceRow) + (_surfaceColumn+1) + "=" + _outValueRange._var;
    }
    
    public static MX36Status fromMGStatus(MGStatus status) {
        MX36Status it = new MX36Status();
        
        it._outName = status.getAsName();
        
        it._surfacePort = status._port;
        it._surfaceUIType = status._uiType;
        it._surfaceRow = status._row;
        it._surfaceColumn = status._column;
        it._surfaceValueRange = status.getValue();
        it._surfaceStatusCache = status;
        
        return it;
    }

    public String toTreeString() {
        return "";
    }
    
    public MXWrapList<Integer> createTable(RangedValue range) {
        MXWrapList<Integer> list = new MXWrapList<>();
        for (int i = range._min; i <= range._max; ++ i) {
            list.addNameAndValue(String.valueOf(i), i);
        }
        Collections.reverse(list);
        return list;
    }
    
    public int compareSurfacePosition(MX36Status status) {
        return compareSurfacePosition(status._surfacePort, status._surfaceUIType, status._surfaceRow, status._surfaceColumn);
    }

    public int compareSurfacePosition(MGStatus status) {
        return compareSurfacePosition(status._port, status._uiType, status._row, status._column);
    }

    public int compareSurfacePosition(int port, int uiType, int row, int column) {
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
    
    public MXMessage createOutMessage() {
        if (_outData == null || _outData.length() == 0) {
            return null;
        }
        if (_outCachedMessage == null) {
            try {
                _outCachedMessage = MXMessageFactory.fromCCXMLText(_outValueOffset, _outData, _outChannel);
            }catch(Exception e) {
                e.printStackTrace();;
            }
            if (_outCachedMessage == null) {
                return null;
            }
        }
        if (_outCachedMessage.getStatus() == 0) {
            return null;
        }
        _outCachedMessage.setValue(_outValueRange.changeRange(_outCachedMessage.getValue()._min, _outCachedMessage.getValue()._max));
        _outCachedMessage.setGate(_outGateRange);
        return _outCachedMessage;
    }
}