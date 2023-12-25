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

package jp.synthtarou.midimixer.ccxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CXNode  {
    public CXNode(CXNode parent, String name, CCRuleForTag rule) {
        _nodeName = name;
        _tagRule = rule;
        _parent = parent;
        _listAttributes = new CXAttributes(this);
        _listAttributes.setIgnoreCase(true);
        _listChildTags = new ArrayList<>();
    }
    
    final CCRuleForTag _tagRule;
    final String _nodeName;
    final CXNode _parent;

    String _textContext;
    String _warningText;
    int _lineNumber;
    int _columnNumber;
    private int _changeStamp;

    public int getChangeStamp() {
        return _changeStamp;
    }
    
    public void incrementChangeStamp() {
        _changeStamp ++;
        if (_parent  != null) {
            _parent.incrementChangeStamp();
        }
    }
   
    public final CXAttributes _listAttributes;
    ArrayList<CXNode> _listChildTags;
    
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
        incrementChangeStamp();
    }
    
    public CCRuleForTag getTagRule() {
        return _tagRule;
    }
    
    public CXNode getParent() {
        return _parent;
    }
    
    public ArrayList<CXNode> pathTo(CXNode child) {
        ArrayList<CXNode> list = new ArrayList<>();
        CXNode node = child;

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
    
    public static String pathToString(Collection<CXNode> path) {
        StringBuffer str = new StringBuffer();
        for (CXNode node : path) {
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
    
    public CXNode getChild(int index) {
        return _listChildTags.get(index);
    }

    public CXNode firstChild(String child) {
        try {
            return listChildren(child).get(0);
        }catch(Exception e) {
            return null;
        }
    }

    public CXNode firstChild(CCRuleForTag child) {
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

    public List<CXNode> listChildren(CCRuleForTag child) {
        ArrayList<CXNode> list = new ArrayList<>();

        for (CXNode tag : _listChildTags) {
            if (tag.getTagRule() == child) {
                list.add(tag);
            }
        }

        return list.size() > 0 ? list : null;
    }

    public List<CXNode> listChildren(String name) {
        ArrayList<CXNode> list = new ArrayList<>();

        for (CXNode tag : _listChildTags) {
            if (tag._nodeName.equalsIgnoreCase(name)) {
                list.add(tag);
            }
        }

        return list.size() > 0 ? list : null;
    }

    public List<CXNode> listChildren() {
        ArrayList<CXNode> list = new ArrayList<>();

        for (CXNode tag : _listChildTags) {
            list.add(tag);
        }

        return list.size() > 0 ? list : null;
    }

    public List<CXNode> listModule() {
        CCRuleManager def = CCRuleManager.getInstance();
        return listChildren(def.getModuleDataTag());
    }

    public List<CXNode> listInstruemntList() {
        CCRuleManager def = CCRuleManager.getInstance();
        return listChildren(def.getInstrumentListTag());
    }

    public List<CXNode> listDrumSet() {
        CCRuleManager def = CCRuleManager.getInstance();
        return listChildren(def.getDrumSetListTag());
    }

    public List<CXNode> listControlChangeMacro() {
        CCRuleManager def = CCRuleManager.getInstance();
        return listChildren(def.getControlChangeMacroListTag());
    }

    public List<CXNode> listTemplate() {
        CCRuleManager def = CCRuleManager.getInstance();
        return listChildren(def.getTemplateListTag());
    }

    public List<CXNode> listDefaultData() {
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

    public CXNode newTag(String name, boolean strict) {
        incrementChangeStamp();
 
        // 既存ルールを探索する
        CCRuleForTag newRule = _tagRule.getTag(name);

        // ルール上つくれる場合
        if (newRule != null) {
            CXNode newChild = new CXNode(this, name, newRule);
            this._listChildTags.add(newChild);
            return newChild;
        }

        // 強制的につくる場合
        if (strict == false) {
            CXNode newChild = new CXNode(this, name, null);
            this._listChildTags.add(newChild);
            return newChild;
        }
        
        return null;
    }
}
