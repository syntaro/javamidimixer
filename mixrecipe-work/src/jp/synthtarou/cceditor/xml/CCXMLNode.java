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

package jp.synthtarou.cceditor.xml;

import java.util.ArrayList;
import java.util.Collection;
import jp.synthtarou.cceditor.xml.rules.CCXMLTagRule;
import jp.synthtarou.cceditor.xml.rules.CCXMLRule;
import jp.synthtarou.midimixer.libs.common.MXWrapList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCXMLNode  {
    public CCXMLNode(CCXMLNode parent, String name, CCXMLTagRule rule) {
        _name = name;
        _tagRule = rule;
        _parent = parent;
        _listAttributes = new MXWrapList<>();
        _listAttributes.setIgnoreCase(true);
    }
    
    final CCXMLTagRule _tagRule;
    final String _name;
    final CCXMLNode _parent;

    String _textContext;
    String _warningText;
    int _lineNumber;
    int _columnNumber;

    public final MXWrapList<String> _listAttributes;
    ArrayList<CCXMLNode> _listChildTags = new ArrayList<>();
    
    public String getWarningText() {
        return _warningText;
    }
    
    public int getLineNumber() {
        return _lineNumber;
    }

    public int getColumnNumber() {
        return _columnNumber;
    }
    
    public String getName() {
        return _name;
    }

    public String getTextContent() {
        return _textContext;
    }

    public void setTextContent(String text) {
        _textContext = text;
    }
    
    public CCXMLTagRule getTagRule() {
        return _tagRule;
    }
    
    public CCXMLNode getParent() {
        return _parent;
    }
    
    public ArrayList<CCXMLNode> pathTo(CCXMLNode child) {
        ArrayList<CCXMLNode> list = new ArrayList<>();
        CCXMLNode node = child;

        while(node != null) {
            if (node.getParent() == null) {
                //ignore root
                break;
            }
            list.add(0, node);
            if (node == this) {
                break;
            }
            node = node.getParent();
        }

        return list;
    }
    
    public static String pathToString(Collection<CCXMLNode> path) {
        StringBuffer str = new StringBuffer();
        for (CCXMLNode node : path) {
            if (str.length() != 0) {
                str.append("/");
            }
            str.append(node._name);
        }
        return str.toString();
    }


    public int countChildTags() {
        return _listChildTags.size();
    }
    
    public CCXMLNode getChild(int index) {
        return _listChildTags.get(index);
    }
    
    public MXWrapList<CCXMLNode> getChildren(String name) {
        MXWrapList<CCXMLNode> list = new MXWrapList<>();

        for (CCXMLNode tag : _listChildTags) {
            if (tag._name.equalsIgnoreCase(name)) {
                String nameAttr = tag._listAttributes.valueOfName("name");
                String idAttr = tag._listAttributes.valueOfName("id");
                
                String dispName = tag._name;
               
                if (idAttr != null) {
                    dispName = idAttr;
                }
                if (nameAttr != null) {
                    dispName = nameAttr;
                }
                list.addNameAndValue(dispName, tag);
            }
        }

        return list;
    }

    public MXWrapList<CCXMLNode> getChildInstruemntsList() {
        CCXMLRule def = CCXMLRule.getInstance();
        return getChildren(def.getInstrumentListTag().getName());
    }

    public MXWrapList<CCXMLNode> getChildDrumSetList() {
        CCXMLRule def = CCXMLRule.getInstance();
        return getChildren(def.getDrumSetListTag().getName());
    }

    public MXWrapList<CCXMLNode> getChildControlChangeMacroList() {
        CCXMLRule def = CCXMLRule.getInstance();
        return getChildren(def.getControlChangeMacroListTag().getName());
    }

    public MXWrapList<CCXMLNode> getChildTemplateList() {
        CCXMLRule def = CCXMLRule.getInstance();
        return getChildren(def.getTemplateListTag().getName());
    }

    public MXWrapList<CCXMLNode> getChildDefaultData() {
        CCXMLRule def = CCXMLRule.getInstance();
        return getChildren(def.getDefaultDataTag().getName());
    }

    public String toString() {
        String tag = _name;

        String name = _listAttributes.valueOfName("name");
        if (name != null) {
            return tag + "(name="  + name + ")";
        }

        String id = _listAttributes.valueOfName("id");
        if (id != null) {
            return tag + "(id="  + id + ")";
        }
        
        return tag;
    }
}
