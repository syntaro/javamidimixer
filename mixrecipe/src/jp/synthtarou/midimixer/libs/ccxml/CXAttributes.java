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
package jp.synthtarou.midimixer.libs.ccxml;

import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.common.MXWrapList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CXAttributes extends MXWrapList<String> {
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
        for (CCRuleForAttribute attr : _tag._tagRule.listAttributes()) {
            if (attr._name.equals(attrName)) {
                return attr;
            }
        }
        return null;
    }
    
    public void setAttribute(String attrName, String value) {
        int x = indexOfName(attrName);
        if (x < 0) {
            super.addNameAndValue(attrName, value);
        }
        else {
            MXWrap<String> data = super.get(x);
            MXWrap<String> newData = new MXWrap(attrName, value);
            super.set(x, newData);
        }
    }
}
