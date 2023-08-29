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
package jp.synthtarou.midimixer.libs.domino.rules;

import jp.synthtarou.cceditor.view.common.CCValueRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCXMLTagRule {
    String _name;
    
    boolean _textContents;
    ArrayList<CCXMLAttributeRule> _listAttributes = new ArrayList<>();
    ArrayList<CCXMLTagRule> _listChildTags = new ArrayList<>();
        
    public CCXMLTagRule(String name) {
        _name = name;
    }
    
    public void readyForAttributeMust(String name, CCValueRule rule) {
        readyForAttribute(name, null, rule);
    }

    public CCXMLAttributeRule readyForAttribute(String name, String defaultValue, CCValueRule rule) {
        for(CCXMLAttributeRule already : _listAttributes) {
            if (already._name.equalsIgnoreCase(name)) {
                return already;
            }
        }
        CCXMLAttributeRule attrType = new CCXMLAttributeRule(name, defaultValue);
        attrType.setValueRule(rule);
        _listAttributes.add(attrType);
        return attrType;
    }

    public void addChild(CCXMLTagRule  tag) {
        for(CCXMLTagRule already : _listChildTags) {
            if (already._name.equalsIgnoreCase(tag._name)) {
                return;
            }
        }
        _listChildTags.add(tag);
    }

    public void readyForText(boolean textContents) {
        _textContents = textContents;
    }
    
    public CCXMLAttributeRule getAttribute(String name) {
        for (CCXMLAttributeRule attr : _listAttributes) {
            if (attr._name.equalsIgnoreCase(name)) {
                return attr;
            }
        }
        return null;
    }

    public CCXMLTagRule findChildRule(String name) {
        for (CCXMLTagRule type : _listChildTags) {
            if (type._name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public String getName() {
        return _name;
    }
    
    public List<CCXMLAttributeRule> listAttributes() {
        return Collections.unmodifiableList(_listAttributes);
    }

    public List<CCXMLTagRule> listChildTags() {
        return Collections.unmodifiableList(_listChildTags);
    }

    public boolean hasTextContents() {
        return _textContents;
    }
    

    public String toString() {
        return _name;
    }
            
}

