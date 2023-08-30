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

import jp.synthtarou.midimixer.mx35cceditor.prompt.CCValueRule;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCRuleAttributes {
    final String _name;
    final String _defaultValue;
    final boolean _must;
    CCValueRule _rule;

    public CCRuleAttributes(String name) { 
        this(name, null);
    }

    public CCRuleAttributes(String name, String defaultValue) { 
        _name = name;
        if (defaultValue == null) {
            _must = true;
            _defaultValue = null;
        }
        else {
            _must = false;
            _defaultValue = defaultValue;
        }
    }
    
    public String toString() {
        if (_must) {
            return _name +"(must)";
        }
        if (_defaultValue != null) {
            return _name + "=" + _defaultValue;
        }
        return _name;
    }

    public String getName() {
        return _name;
    }
    
    public String getDefaultValue() {
        return _defaultValue;
    }

    public boolean isMust() {
        return _must;
    }
    
    public CCValueRule getValueRule() {
        return _rule;
    }

    public void setValueRule(CCValueRule rule) {
        _rule = rule;
    }
}
