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
package jp.synthtarou.midimixer.libs.domino;

import jp.synthtarou.midimixer.libs.xml.MXDOMElement;
import java.awt.Color;
import java.util.List;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class DTextMessage {
    public DTextMessage() {
    }

    public DTextMessage(String name, String text, int gate) {
        _name = name;
        _dataText = text;
        _gate = gate;
    }

    int _id = -1; /* from 0 to 1300 */
    String _name = "";
    Color _color = Color.black;
    int _sync; /* 0: None1: Last2: LastEachGate */
    int _gate;
    boolean _muteSync;
    
    public String toString() {
        return _name + ":" + _dataText + ", Gate=" + _gate;
    }
    
    public DTextMessage(int id, String name) {
        _id = id;
        _name = name;
    }
    
    public String getMemoText() {
        return _memoText;
    }

    public String getName() {
        return _name;
    }
    
    public String getDataText() {
        return _dataText;
    }
    
    public int getGate() {
        return _gate;
    }
    
    public String getValueMin() {
        return valueMin;
    }

    public String getValueMax() {
        return valueMax;
    }

    public String getValueOffset() {
        return valueMax;
    }
    
    String _memoText;
    String _dataText;
    
    String valueDefault, valueMin, valueMax, valueOffset, valueName, valueType, valueTableID;
    List<MXDOMElement> valueEntry;
    
    String gateDefault, gateMin, gateMax, gateOffset, gateName, gateType, gateTableID;
    List<MXDOMElement> gateEntry;
}
