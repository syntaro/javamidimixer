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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCRuleElement {
    String _name;
    
    boolean _textContents;
    ArrayList<CCRuleAttributes> _listAttributes = new ArrayList<>();
    ArrayList<CCRuleElement> _listChildTags = new ArrayList<>();
        
    public CCRuleElement(String name) {
        _name = name;
    }
    
    public void readyForAttributeMust(String name, CCValueRule rule) {
        readyForAttribute(name, null, rule);
    }

    public CCRuleAttributes readyForAttribute(String name, String defaultValue, CCValueRule rule) {
        for(CCRuleAttributes already : _listAttributes) {
            if (already._name.equalsIgnoreCase(name)) {
                return already;
            }
        }
        CCRuleAttributes attrType = new CCRuleAttributes(name, defaultValue);
        attrType.setValueRule(rule);
        _listAttributes.add(attrType);
        return attrType;
    }

    public void addChild(CCRuleElement  tag) {
        for(CCRuleElement already : _listChildTags) {
            if (already._name.equalsIgnoreCase(tag._name)) {
                return;
            }
        }
        _listChildTags.add(tag);
    }

    public void readyForText(boolean textContents) {
        _textContents = textContents;
    }
    
    public CCRuleAttributes getAttribute(String name) {
        for (CCRuleAttributes attr : _listAttributes) {
            if (attr._name.equalsIgnoreCase(name)) {
                return attr;
            }
        }
        return null;
    }

    public CCRuleElement findChildRule(String name) {
        for (CCRuleElement type : _listChildTags) {
            if (type._name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public String getName() {
        return _name;
    }
    
    public List<CCRuleAttributes> listAttributes() {
        return Collections.unmodifiableList(_listAttributes);
    }

    public List<CCRuleElement> listChildTags() {
        return Collections.unmodifiableList(_listChildTags);
    }

    public boolean hasTextContents() {
        return _textContents;
    }
    

    public String toString() {
        return _name;
    }
            
}

