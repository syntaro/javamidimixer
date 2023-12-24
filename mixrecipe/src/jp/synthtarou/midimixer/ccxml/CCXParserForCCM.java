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
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapListFactory;

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

    public final MXRangedValue _value;
    public final int _offsetValue;
    public final MXRangedValue _gate;
    public final int _offsetGate;

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
            hasValueHi = (template.indexOfValueHi() >= 0) ? true : false;
            hasGateHi = (template.indexOfGateHi() >= 0) ? true:  false;
        }
        
        MXWrapList<Integer> valueTable = null;
        MXWrapList<Integer> gateTable = null;

        CXNode value = _node.firstChild("Value");
        int minValue, maxValue, defaultValue, offsetValue;
        if (value != null) {
            minValue = value._listAttributes.numberOfName("Min", 0);
            maxValue = value._listAttributes.numberOfName("Max", hasValueHi ? 16383 : 127);
            offsetValue = value._listAttributes.numberOfName("Offset", 0);
            defaultValue = value._listAttributes.numberOfName("Default", (minValue + maxValue) / 2 + 1);
            String tableId = value._listAttributes.valueOfName("TableID");
            if (tableId != null) {
                Integer tableID2 = CXFile.parseNumber(tableId);
                if (tableID2 != null) {
                    valueTable = file.getTable(tableID2);
                }
            }
        } else {
            minValue = 0;
            maxValue = hasValueHi ? 16383 : 127;
            offsetValue = 0;
            defaultValue = (minValue + maxValue) / 2 + 1;
        }
        _value = new MXRangedValue(defaultValue, minValue, maxValue);
        _offsetValue = offsetValue;
        _valueTable = valueTable;

        CXNode gate = _node.firstChild("Gate");
        String gateType = null;
        int minGate, maxGate, defaultGate, offsetGate;
        if (gate != null) {
            minGate = gate._listAttributes.numberOfName("Min", 0);
            maxGate = gate._listAttributes.numberOfName("Max", hasGateHi ? 16383 : 127);
            offsetGate = gate._listAttributes.numberOfName("Offset", 0);
            defaultGate = gate._listAttributes.numberOfName("Default", (minGate + maxGate) / 2 + 1);
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
            minGate = 0;
            maxGate = hasGateHi ? 16383 : 127;
            offsetGate = 0;
            defaultGate = (minGate + maxGate) / 2 + 1;
        }
        if (gateType != null && gateType.equalsIgnoreCase("Key")) {
            if (gateTable == null) {
                gateTable = MXWrapListFactory.listupNoteNo(false);
            }
            _gateTypeKey = true;
        }
        else {
            _gateTypeKey = false;
        }
        _gate = new MXRangedValue(defaultGate, minGate, maxGate);
        _offsetGate = offsetGate;
        _gateTable = gateTable;
    }
}
