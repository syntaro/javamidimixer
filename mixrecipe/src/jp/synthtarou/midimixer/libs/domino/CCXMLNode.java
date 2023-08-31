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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCXMLNode  {
    public CCXMLNode(CCXMLNode parent, String name, CCRuleForTag rule) {
        _nodeName = name;
        _tagRule = rule;
        _parent = parent;
        _listAttributes = new CCParameters(_tagRule);
        _listAttributes.setIgnoreCase(true);
        _listChildTags = new ArrayList<>();
    }
    
    final CCRuleForTag _tagRule;
    final String _nodeName;
    final CCXMLNode _parent;

    String _textContext;
    String _warningText;
    int _lineNumber;
    int _columnNumber;

    public final CCParameters _listAttributes;
    ArrayList<CCXMLNode> _listChildTags;
    
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
        return _nodeName;
    }

    public String getTextContent() {
        return _textContext;
    }

    public void setTextContent(String text) {
        _textContext = text;
    }
    
    public CCRuleForTag getTagRule() {
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
            str.append(node._nodeName);
        }
        return str.toString();
    }


    public int countChildTags() {
        return _listChildTags.size();
    }
    
    public CCXMLNode getChild(int index) {
        return _listChildTags.get(index);
    }

    public CCXMLNode firstChild(String child) {
        try {
            return listChildren(child).get(0);
        }catch(Exception e) {
            return null;
        }
    }

    public String firstChildsTextContext(String child) {
        try {
            String result = firstChild(child)._textContext;
            result = MXUtil.shrinkText(result);
            if (result.isBlank() == false) {
                return result;
            }
            return "";
        }catch(Exception e) {
            return null;
        }
    }

    public List<CCXMLNode> listChildren(CCRuleForTag child) {
        ArrayList<CCXMLNode> list = new ArrayList<>();

        for (CCXMLNode tag : _listChildTags) {
            if (tag.getTagRule() == child) {
                list.add(tag);
            }
        }

        return list.size() > 0 ? list : null;
    }

    public List<CCXMLNode> listChildren(String name) {
        ArrayList<CCXMLNode> list = new ArrayList<>();

        for (CCXMLNode tag : _listChildTags) {
            if (tag._nodeName.equalsIgnoreCase(name)) {
                list.add(tag);
            }
        }

        return list.size() > 0 ? list : null;
    }

    public List<CCXMLNode> listModule() {
        CCRuleManager def = CCRuleManager.getInstance();
        return listChildren(def.getModuleDataTag());
    }

    public List<CCXMLNode> listInstruemntList() {
        CCRuleManager def = CCRuleManager.getInstance();
        return listChildren(def.getInstrumentListTag());
    }

    public List<CCXMLNode> listDrumSet() {
        CCRuleManager def = CCRuleManager.getInstance();
        return listChildren(def.getDrumSetListTag());
    }

    public List<CCXMLNode> listControlChangeMacro() {
        CCRuleManager def = CCRuleManager.getInstance();
        return listChildren(def.getControlChangeMacroListTag());
    }

    public List<CCXMLNode> listTemplate() {
        CCRuleManager def = CCRuleManager.getInstance();
        return listChildren(def.getTemplateListTag());
    }

    public List<CCXMLNode> listDefaultData() {
        CCRuleManager def = CCRuleManager.getInstance();
        return listChildren(def.getDefaultDataTag());
    }

    public String toString() {
        String tag = _nodeName;

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

    public CCXMLNode newTag(String name, boolean strict) {
        // 既存ルールを探索する
        CCRuleForTag newRule = _tagRule.getTag(name);

        // ルール上つくれる場合
        if (newRule != null) {
            CCXMLNode newChild = new CCXMLNode(this, name, newRule);
            this._listChildTags.add(newChild);
            return newChild;
        }

        // 強制的につくる場合
        if (strict == false) {
            CCXMLNode newChild = new CCXMLNode(this, name, null);
            this._listChildTags.add(newChild);
            return newChild;
        }
        
        return null;
    }
}
