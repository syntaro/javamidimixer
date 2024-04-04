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

import jp.synthtarou.midimixer.ccxml.xml.CXNode;
import jp.synthtarou.midimixer.ccxml.xml.CXFile;
import java.util.IllegalFormatException;
import java.util.logging.Level;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.libs.namedobject.MXNamedObject;
import jp.synthtarou.libs.namedobject.MXNamedObjectListFactory;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class InformationForCCM {

    public final CXNode _node;
    public final InformationForModule _module;

    public final String _name;
    public final String _memo;
    public final String _data;

    public final MXRangedValue _value;
    public final int _offsetValue;
    public final MXRangedValue _gate;
    public final int _offsetGate;

    public final MXNamedObjectList<Integer> _valueTable;
    public final MXNamedObjectList<Integer> _gateTable;
    public final boolean _gateTypeKey;

    public final int _id;
    public final int _ccmLinkValue;
    public final int _ccmLinkGate;
    public final boolean _isLink;

    public InformationForCCM(InformationForModule module, CXNode node) {
        _module = module;
        _node = node;

        int intId = Integer.MIN_VALUE;

        String textId = _node._listAttributes.valueOfName("ID");
        try {
            if (textId != null) {
                intId = Integer.parseInt(textId);
            }
        } catch (Exception E) {

        }
        _id = intId;

        int intValue = Integer.MIN_VALUE;
        int intGate = Integer.MIN_VALUE;

        if (node.getName().equalsIgnoreCase("CCMLink")) {
            String textValue = node._listAttributes.valueOfName("Value");
            String textGate = node._listAttributes.valueOfName("Gate");
            try {
                if (textValue != null) {
                    intValue = Integer.parseInt(textValue);
                }
            } catch (Exception E) {

            }
            try {
                if (textGate != null) {
                    intGate = Integer.parseInt(textGate);
                }
            } catch (Exception E) {

            }
            _isLink = true;
        } else {
            _isLink = false;
        }
        _ccmLinkGate = intGate;
        _ccmLinkValue = intValue;

        _name = _node._listAttributes.valueOfName("Name");
        _memo = _node.firstChildsTextContext("Memo");
        _data = _node.firstChildsTextContext("Data");

        MXTemplate template = null;
        try {
            if (_data != null) {
                template = new MXTemplate(_data);
            }
        } catch (IllegalFormatException ex) {
            MXFileLogger.getLogger(InformationForCCM.class).log(Level.WARNING, ex.getMessage(), ex);
        }

        boolean hasValueHi = false;
        boolean hasGateHi = false;
        if (template != null) {
            hasValueHi = (template.indexOfValueHi() >= 0) ? true : false;
            hasGateHi = (template.indexOfGateHi() >= 0) ? true : false;
        }

        MXNamedObjectList<Integer> valueTable = null;
        MXNamedObjectList<Integer> gateTable = null;

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
                    valueTable = _module.getTable(tableID2);
                }
            }
        } else {
            minValue = 0;
            maxValue = hasValueHi ? 16383 : 127;
            offsetValue = 0;
            defaultValue = (minValue + maxValue) / 2 + 1;
        }
        _value = new MXRangedValue(defaultValue, minValue, maxValue);
        _valueTable = valueTable;
        _offsetValue = offsetValue;

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
                    gateTable = _module.getTable(tableID2);
                }
            } else {
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
                gateTable = MXNamedObjectListFactory.listupNoteNo(false);
            }
            _gateTypeKey = true;
        } else {
            _gateTypeKey = false;
        }
        _gate = new MXRangedValue(defaultGate, minGate, maxGate);
        _offsetGate = offsetGate;
        _gateTable = gateTable;
    }    
    
    public MXNamedObjectList<Integer> canonicalTable(MXNamedObjectList<Integer> original, int offset) {
        if (offset == 0) {
            return original;
        }
        MXNamedObjectList<Integer> result = new MXNamedObjectList<>();
        for (MXNamedObject<Integer> seek : original) {
            result.addNameAndValue(seek._name, seek._value + offset);
        }
        return result;
    }

    public InformationForCCM(InformationForCCM linkFrom, int newGate, int newValue) {
        _isLink = true;

        _module = linkFrom._module;
        _node = linkFrom._node;

        _name = linkFrom._name;
        _memo = linkFrom._memo;
        _data = linkFrom._data;

        if (newValue == Integer.MIN_VALUE) {
            _value = linkFrom._value;
        } else {
            _value = linkFrom._value.changeValue(newValue);
        }
        _offsetValue = linkFrom._offsetValue;

        if (newGate == Integer.MIN_VALUE) {
            _gate = linkFrom._gate;
        } else {
            _gate = linkFrom._gate.changeValue(newGate);
        }
        _offsetGate = linkFrom._offsetGate;

        _valueTable = linkFrom._valueTable;
        _gateTable = linkFrom._gateTable;

        _gateTypeKey = linkFrom._gateTypeKey;

        _id = linkFrom._id;
        _ccmLinkValue = linkFrom._ccmLinkValue;
        _ccmLinkGate = linkFrom._ccmLinkGate;
    }

    public MXRangedValue getParsedGate() {
        if (_offsetGate == 0) {
            return _gate;
        }
        int value = _gate._value + _offsetGate;
        int min = _gate._min + _offsetGate;
        int max = _gate._max+ _offsetGate;
        return new MXRangedValue(value, min, max);
    }

    public MXNamedObjectList<Integer> getParsedGateTable() {
        if (_offsetGate == 0) {
            return _gateTable;
        }
        if (_gateTable == null) {
            return null;
        }
        MXNamedObjectList<Integer> result = new MXNamedObjectList<>();
        for (MXNamedObject<Integer> seek : _gateTable) {
            int value = seek._value + _offsetGate;
            result.addNameAndValue(seek._name, value);
        }
        return result;
    }

    public MXRangedValue getParsedValue() {
        if (_offsetValue == 0) {
            return _value;
        }
        int value = _value._value + _offsetValue;
        int min = _value._min + _offsetValue;
        int max = _value._max+ _offsetValue;
        return new MXRangedValue(value, min, max);
    }

    public MXNamedObjectList<Integer> getParsedValueTable() {
        if (_offsetValue == 0) {
            return _valueTable;
        }
        if (_valueTable == null) {
            return null;
        }
        MXNamedObjectList<Integer> result = new MXNamedObjectList<>();
        for (MXNamedObject<Integer> seek : _valueTable) {
            int value = seek._value + _offsetValue;
            result.addNameAndValue(seek._name, value);
        }
        return result;
    }
}
