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
package jp.synthtarou.midimixer.mx35cceditor.ccxml;

import java.util.LinkedList;
import java.util.List;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCDocumentHandler extends DefaultHandler {

    public static final String USERDATA_PATH = "user.path";
    public static final String USERDATA_STARTLINE = "user.startLine";
    public static final String USERDATA_STARTCOLUMN = "user.startColumn";

    LinkedList<CXNode> _cursor = new LinkedList();
    Locator _locator;
    final CXNode _document;

    public CCDocumentHandler(CXNode document) {
        _document = document;
    }

    @Override
    public void startDocument() {
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        _locator = locator;

    }

    UserDataHandler _dataHandler = new UserDataHandler() {
        @Override
        public void handle(short operation, String key, Object data, Node src, Node dst) {

            switch (operation) {
                case NODE_ADOPTED:
                    break;
                case NODE_CLONED:
                    break;
                case NODE_DELETED:
                    break;
                case NODE_IMPORTED:
                    break;
                case NODE_RENAMED:
                    break;
            }
        }
    };

    public static CCRuleForTag findRule(List<CXNode> path, String child) {
        CCRuleForTag root = CCRuleManager.getInstance().getRootTag();
        CCRuleForTag rule = root;
        for (CXNode seek : path) {
            rule = rule.getTag(seek._nodeName);
            if (rule == null) {
                return null;
            }
        }
        if (rule != null) {
            rule = rule.getTag(child);
        }
        return rule;
    }

    public void loopShrink(CXNode target) {
        for (CXNode node : target._listChildTags) {
            String text = node.getTextContent();
            if (text != null) {
                String newtext = MXUtil.shrinkText(text);
                if (newtext.length() == 0) {
                    node.setTextContent(null);
                } else {
                    if (newtext.length() != text.length()) {
                        node.setTextContent(newtext);
                    }
                }
            }
            loopShrink(node);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        CXNode parent = _cursor.isEmpty() ? _document : _cursor.getLast();
        CXNode child = new CXNode(parent, qName, findRule(_cursor, qName));

        parent._listChildTags.add(child);
        _cursor.add(child);

        if (attributes.getLength() > 0) {
            for (int i = 0; i < attributes.getLength(); ++i) {
                String name = attributes.getQName(i);
                String value = attributes.getValue(i);
                child._listAttributes.addNameAndValue(name, value);
            }
        }

        child._lineNumber = _locator.getLineNumber();
        child._columnNumber = _locator.getColumnNumber();
    }

    @Override
    public void characters(char[] ch, int offset, int length) {
        StringBuffer ret = new StringBuffer();
        ret.append(ch, offset, length);

        if (_cursor == null || _cursor.getLast() == null) {
            return;
        }

        if (ret.length() > 0) {
            String prev = _cursor.getLast().getTextContent();
            if (prev != null) {
                _cursor.getLast().setTextContent(prev + ret);
            } else {
                _cursor.getLast().setTextContent(ret.toString());
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (_cursor.isEmpty() == false) {
            CXNode lastLeaf = _cursor.getLast();
            if (lastLeaf._nodeName.equals(qName)) {
                _cursor.removeLast();

                if (_cursor.isEmpty() == false) {
                    lastLeaf = _cursor.getLast();
                }
            }
        }
    }

    @Override
    public void endDocument() {
        loopShrink(_document);
    }
}
