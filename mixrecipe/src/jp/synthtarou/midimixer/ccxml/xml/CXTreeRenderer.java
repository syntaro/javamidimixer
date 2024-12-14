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
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import jp.synthtarou.libs.namedobject.MXNamedObject;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CXTreeRenderer implements TreeCellRenderer {
    TreeCellRenderer _base = new DefaultTreeCellRenderer();
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        String text = "";
        if (value != null && (value instanceof CXNode)) {
            CXNode node = (CXNode)value;
            text = format(node);
        }
        else {
            text = String.valueOf(value);
        }
        return _base.getTreeCellRendererComponent(tree, text, selected, expanded, leaf, row, hasFocus);
    }


    public static boolean _displayAttribute = true;
    public static boolean _displayTextContent = true;
    
    public String escapeDQuote(String text) {
        StringBuilder ret = new StringBuilder();

        final char DQUOTE = '\"';
        final char BSLASH = '\\';

        ret.append(DQUOTE);
        
        for (char ch : text.toCharArray()) {
            switch(ch) {
                case DQUOTE:
                    ret.append(BSLASH);
                    ret.append(DQUOTE);
                    break;
                case BSLASH:
                    ret.append(BSLASH);
                    ret.append(BSLASH);
                    break;
                case '\n':
                    ret.append(BSLASH);
                    ret.append('n');
                    break;
                default:
                    ret.append(ch);
            }
        }
        
        ret.append(DQUOTE);
        
        return ret.toString();
    }
    
    public String format(CXNode node) {
        StringBuilder text = new StringBuilder();
        text.append(node._nodeName);

        if (_displayAttribute) {
            ArrayList<String> listAttrDefined = new ArrayList();
            ArrayList<String> listAttrNonedefined = new ArrayList();

            if (node._tagRule != null) {
                HashSet<String> already = new HashSet();

                for (CCRuleForAttribute dump :  node._tagRule.listAttributes()) {
                    String name = dump.getName();
                    String value = node._listAttributes.valueOfName(name);
                    if (value != null) {
                        listAttrDefined.add(name + "=" + escapeDQuote(value));
                    }
                    already.add(name.toLowerCase());
                }

                for (MXNamedObject<String> attr : node._listAttributes) {
                    if (already.contains(attr._name.toLowerCase())) {
                        continue;
                    }
                    listAttrNonedefined.add(attr._name + "="  + escapeDQuote(attr._value));
                }
            }
            else {
                for (MXNamedObject<String> attr : node._listAttributes) {
                    listAttrNonedefined.add(attr._name + "="  + escapeDQuote(attr._value));
                }
            }
            
            boolean first = true;
            if (listAttrDefined.size() > 0) {
                for (String seg : listAttrDefined) {
                    if (!first)  {
                        text.append(", ");
                    }
                    else {                        
                        text.append(":");
                    }
                    first = false;
                    text.append(seg);
                }
            }
            if (listAttrNonedefined.size() > 0) {
                for (String seg : listAttrNonedefined) {
                    if (!first)  {
                        text.append(", ");
                    }else {                        
                        text.append(":");
                    }
                    first = false;
                    text.append(seg);
                }
            }
        }

        if (_displayTextContent) {
            if (node._textContext != null && node._textContext.length() > 0) {
                text.append("(");
                text.append(node._textContext);
                text.append(")");
            }
        }
        return text.toString();
    }
    
}
