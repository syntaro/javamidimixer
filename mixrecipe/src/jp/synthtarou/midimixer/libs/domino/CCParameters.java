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

import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.mx35cceditor.prompt.CCValueRule;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCParameters extends MXWrapList<String> {
    public final CCRuleForTag _rule;

    public CCParameters() {
        this(new CCRuleForTag(""));
    }
    
    public CCParameters(CCRuleForTag rule) {
        _rule = rule;
        super.setIgnoreCase(true);
    }
    
    public String getTagName() {
        if (_rule != null) {
            return _rule._tagName;
        }
        return "";
    }
    
    public int numberOfName(String name, int errorNumber) {
        String value = valueOfName(name);
        return MXUtil.numberFromText(value, errorNumber);
    }

    public CCRuleForAttribute getAttributeRule(String attrName) {
        for (CCRuleForAttribute attr : _rule.listAttributes()) {
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
            data.value = value;
        }
    }

    public CCRuleForAttribute setupAttributeMust(String name, CCValueRule rule) {
        return _rule.setupAttributeMust(name, rule);
    }

    public CCRuleForAttribute setupAttribute(String name, String defaultValue, CCValueRule rule) {
        return _rule.setupAttribute(name, defaultValue, rule);
    }
}
