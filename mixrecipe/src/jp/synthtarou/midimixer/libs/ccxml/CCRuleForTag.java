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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCRuleForTag {
    String _tagName;
    
    boolean _textContents;
    ArrayList<CCRuleForAttribute> _listAttributes = new ArrayList<>();
    ArrayList<CCRuleForTag> _listChildTags = new ArrayList<>();
        
    public CCRuleForTag(String name) {
        _tagName = name;
    }
    
    public CCRuleForAttribute setupAttributeMust(String name, CCValueRule rule) {
        return setupAttribute(name, null, rule);
    }

    public CCRuleForAttribute setupAttribute(String name, String defaultValue, CCValueRule rule) {
        for(CCRuleForAttribute already : _listAttributes) {
            if (already._name.equalsIgnoreCase(name)) {
                return already;
            }
        }
        CCRuleForAttribute attrType = new CCRuleForAttribute(name, defaultValue);
        attrType.setValueRule(rule);
        _listAttributes.add(attrType);
        return attrType;
    }

    public void addChild(CCRuleForTag  tag) {
        for(CCRuleForTag already : _listChildTags) {
            if (already._tagName.equalsIgnoreCase(tag._tagName)) {
                return;
            }
        }
        _listChildTags.add(tag);
    }

    public void readyForText(boolean textContents) {
        _textContents = textContents;
    }
    
    public CCRuleForAttribute getAttribute(String name) {
        for (CCRuleForAttribute attr : _listAttributes) {
            if (attr._name.equalsIgnoreCase(name)) {
                return attr;
            }
        }
        return null;
    }

    public CCRuleForTag getTag(String name) {
        for (CCRuleForTag type : _listChildTags) {
            if (type._tagName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public String getName() {
        return _tagName;
    }
    
    public List<CCRuleForAttribute> listAttributes() {
        return Collections.unmodifiableList(_listAttributes);
    }

    public List<CCRuleForTag> listChildTags() {
        return Collections.unmodifiableList(_listChildTags);
    }

    public boolean hasTextContents() {
        return _textContents;
    }
    
    public String toString() {
        return _tagName;
    }
}

