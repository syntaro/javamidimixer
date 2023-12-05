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
package jp.synthtarou.midimixer.ccxml;

import java.util.IllegalFormatException;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.MXWrapListFactory;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCXParserForCCM {

    public final CXFile _file;
    public final CXNode _node;

    public final String _name;
    public final String _memo;
    public final String _data;

    public final int _minValue;
    public final int _maxValue;
    public final int _offsetValue;
    public final int _defaultValue;
    public final int _minGate;
    public final int _maxGate;
    public final int _offsetGate;
    public final int _defaultGate;
    public final MXWrapList<Integer> _valueTable;
    public final MXWrapList<Integer> _gateTable;
    public final boolean _gateTypeKey;
    
    public CCXParserForCCM(CXFile file, CXNode node) {
        _node = node;
        _file = file;

        _name = _node._listAttributes.valueOfName("Name");
        _memo = _node.firstChildsTextContext("Memo");
        _data = _node.firstChildsTextContext("Data");
        
        MXTemplate template = null;
        try {
            template = new MXTemplate(_data);
        }catch(IllegalFormatException ex) {
            ex.printStackTrace();
        }
        
        boolean hasValueHi = false;
        boolean hasGateHi = false;
        if (template != null) {
            hasValueHi = (template.getBytePosHiValue() >= 0) ? true : false;
            hasGateHi = (template.getBytePosHiGate() >= 0) ? true:  false;
        }
        
        MXWrapList<Integer> valueTable = null;
        MXWrapList<Integer> gateTable = null;

        CXNode value = _node.firstChild("Value");
        if (value != null) {
            _minValue = value._listAttributes.numberOfName("Min", 0);
            _maxValue = value._listAttributes.numberOfName("Max", hasValueHi ? 16383 : 127);
            _offsetValue = value._listAttributes.numberOfName("Offset", 0);
            _defaultValue = value._listAttributes.numberOfName("Default", _maxValue / 2 + 1);
            String tableId = value._listAttributes.valueOfName("TableID");
            if (tableId != null) {
                Integer tableID2 = CXFile.parseNumber(tableId);
                if (tableID2 != null) {
                    valueTable = file.getTable(tableID2);
                }
            }
        } else {
            _minValue = 0;
            _maxValue = hasValueHi ? 16383 : 127;
            _offsetValue = 0;
            _defaultValue = _maxValue / 2 + 1;
        }
        if (valueTable == null) {
            valueTable = MXWrapListFactory.listupRange(_minValue, _maxValue);
        }
        _valueTable = valueTable;

        CXNode gate = _node.firstChild("Gate");
        String gateType = null;
        if (gate != null) {
            _minGate = gate._listAttributes.numberOfName("Min", 0);
            _maxGate = gate._listAttributes.numberOfName("Max", hasGateHi ? 16383 : 127);
            _offsetGate = gate._listAttributes.numberOfName("Offset", 0);
            _defaultGate = gate._listAttributes.numberOfName("Default", _maxGate / 2 + 1);
            String tableId = gate._listAttributes.valueOfName("TableID");
            if (tableId != null) {
                int tableID2 = CXFile.parseNumber(tableId);
                if (tableID2 >= 0) {
                    gateTable = file.getTable(tableID2);
                }
            }
            else {
                gateType = gate._listAttributes.valueOfName("Type");
            }
        } else {
            _minGate = 0;
            _maxGate = hasGateHi ? 16383 : 127;
            _offsetGate = 0;
            _defaultGate = _maxGate / 2 + 1;
        }
        if (gateType != null && gateType.equalsIgnoreCase("Key")) {
            if (gateTable == null) {
                gateTable = MXWrapListFactory.listupNoteNo(false);
            }
            _gateTypeKey = true;
        }
        else {
            if (gateTable == null) {
                gateTable = MXWrapListFactory.listupRange(_minGate, _maxGate);
            }
            _gateTypeKey = false;
        }
        _gateTable = gateTable;
    }
}
