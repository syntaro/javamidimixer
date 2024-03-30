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
package jp.synthtarou.midimixer.ccxml.xml;

import jp.synthtarou.midimixer.ccxml.rules.CCRuleForAttribute;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.namedobject.MXNamedObject;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CXAttributes extends MXNamedObjectList<String> {
    final CXNode _tag;
    
    public CXAttributes(CXNode tag) {
        _tag = tag;
        super.setIgnoreCase(true);
    }
    
    public String getTagName() {
        if (_tag != null) {
            return _tag._nodeName;
        }
        return "";
    }
    
    public int numberOfName(String name, int errorNumber) {
        String value = valueOfName(name);
        return MXUtil.numberFromText(value, errorNumber);
    }

    public CCRuleForAttribute getAttributeRule(String attrName) {
        return _tag._tagRule.getAttribute(attrName);
    }
    
    public void setAttribute(String attrName, String value) {
        int x = indexOfName(attrName);
        if (x < 0) {
            super.addNameAndValue(attrName, value);
        }
        else {
            MXNamedObject<String> data = super.get(x);
            MXNamedObject<String> newData = new MXNamedObject(attrName, value);
            super.set(x, newData);
        }
    }
}
