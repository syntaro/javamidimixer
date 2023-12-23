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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.wraplist.MXWrap;
import jp.synthtarou.midimixer.libs.common.MXLineReader;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CXFile {

    public String toString() {
        return _file.toString();
    }

    public boolean isLoaded() {
        return (_loadError == null) && listModules() != null;
    }

    public List<CXNode> listModules() {
        return _document.listChildren(CCRuleManager._instance.moduleData);
    }

    public static void main(String[] args) {
        CCRuleManager.dumpRules();

        File moduleDirectory = new File("C:/Domino144/Module");
        for (File file : moduleDirectory.listFiles()) {
            if (file.isFile() == false) {
                continue;
            }
            String name = file.getName();
            name = name.toLowerCase();
            if (name.contains("_back")) {
                continue;
            }
            if (name.endsWith(".xml")) {
                if (file.canRead()) {
                    System.out.println("XMLFile [" + name + "]");
                    CXFile f2 = new CXFile(file);
                    f2.dumpWarning();
                }
            }
        }
    }

    static File _editingDirecotory = new File(MXUtil.getAppBaseDirectory(), "Editing");

    public final File _file;
    final String _fileEncoding;
    final CCRuleManager ruleManager = CCRuleManager.getInstance();

    final CXNode _document = new CXNode(null, "", ruleManager.getRootTag());
    final ArrayList<CXNode> _listWarning = new ArrayList<>();
    Exception _loadError;

    public CXFile(String fileName) {
        this(new File(_editingDirecotory, fileName));
    }

    public CXFile(File file) {
        _file = file;
        System.out.println("file = " + file);
        Element docElement;

        String encoding = null;

        try {
            InputStream check = new FileInputStream(_file);
            MXLineReader reader = new MXLineReader(check);
            String line = reader.readLine();

            //only support 2
            if (line.contains("encoding=\"Shift_JIS\"")) {
                encoding = "Shift_JIS";
            } else if (line.contains("encoding=\"Windows-31J\"")) {
                encoding = "Windows-31J";
            } else {
                encoding = null;

            }
            try {
                check.close();
            } catch (IOException ex) {
                Logger.getLogger(CXFile.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (IOException ex) {
            _loadError = ex;
            return;
        } finally {
            _fileEncoding = encoding;
        }
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            InputStream stream = new FileInputStream(file);
            CCDocumentHandler handler = new CCDocumentHandler(_document);

            try {
                InputSource source = new InputSource(stream);
                saxParser.parse(source, handler);
                System.out.println("source encoding  = " + source.getEncoding());

            } finally {
                stream.close();
            }

            CCRuleManager rule = CCRuleManager.getInstance();
            for (CXNode moduleNode : _document._listChildTags) {
                checkWaring(moduleNode, rule.getModuleDataTag());
            }

        } catch (ParserConfigurationException ex) {
            _loadError = ex;
            return;
        } catch (SAXException ex) {
            _loadError = ex;
            return;
        } catch (IOException ex) {
            _loadError = ex;
            return;
        }
        _loadError = null;
    }

    public void checkWaring(CXNode target, CCRuleForTag targetRule) {
        target._warningText = null;

        if (targetRule == null) {
            target._warningText = " this is undocumented ";
            _listWarning.add(target);
            return;
        }

        StringBuffer warning = new StringBuffer();
        ArrayList<String> missingAttr = new ArrayList<>();
        ArrayList<String> undocumentedAttr = new ArrayList<>();

        for (CCRuleForAttribute ruleAttr : targetRule.listAttributes()) {
            if (ruleAttr.isMust()) {
                if (target._listAttributes.indexOfName(ruleAttr.getName()) < 0) {
                    missingAttr.add(ruleAttr.getName());
                }
            }
        }
        if (missingAttr.size() > 0) {
            warning.append(target._nodeName + " hasn's attributes " + missingAttr + "");
        }
        for (MXWrap<String> keyValue : target._listAttributes) {
            if (targetRule.getAttribute(keyValue._name) == null) {
                undocumentedAttr.add(keyValue._name + "=" + keyValue._value);
            }
        }

        if (undocumentedAttr.size() > 0) {
            warning.append(target._nodeName + " have undocumented attributes " + undocumentedAttr + "");
        }

        ArrayList<String> undocumentedTag = new ArrayList<>();

        for (CXNode child : target._listChildTags) {
            CCRuleForTag childRule = targetRule.getTag(child._nodeName);
            if (childRule == null) {
                undocumentedTag.add(child._nodeName);
            }
            checkWaring(child, childRule);
        }

        if (undocumentedTag.size() > 0) {
            warning.append(" undocumented tags " + undocumentedTag + "");
        }

        if (warning.length() > 0) {
            target._warningText = warning.toString();
            _listWarning.add(target);
        }
    }

    public void dumpWarning() {
        System.out.println(getAdviceForXML());
    }

    public List<CXNode> listWarning() {
        return Collections.unmodifiableList(_listWarning);
    }

    public String getAdviceForXML() {
        if (_loadError != null) {
            if (_loadError instanceof SAXParseException) {
                SAXParseException saxe = (SAXParseException) _loadError;
                return "Error [" + saxe.getLineNumber() + ", " + saxe.getColumnNumber() + "] -> " + saxe.getMessage();
            }
            return "Error: " + _loadError.getMessage();
        }
        if (listModules() == null) {
            return "Error, This XML Don't have '<Module>'";
        }
        StringBuffer str = new StringBuffer();
        for (CXNode node : _listWarning) {
            if (str.length() > 0) {
                str.append("\n");
            }
            str.append("" + node._lineNumber + ", " + node._columnNumber + ": " + node._warningText);
        }
        return str.toString();
    }

    public static void prepareNode(Document doc, Element parent, CXNode ccnode) {
        Element newnode = doc.createElement(ccnode._nodeName);

        if (ccnode._textContext != null && ccnode._textContext.length() > 0) {
            newnode.setTextContent(MXUtil.shrinkText(ccnode._textContext));
        }
        for (MXWrap<String> attr : ccnode._listAttributes) {
            String name = attr._name;
            String value = attr._value;
            newnode.setAttribute(name, value);
        }

        if (parent == null) {
            doc.appendChild(newnode);
        } else {
            parent.appendChild(newnode);
        }

        for (CXNode child : ccnode._listChildTags) {
            prepareNode(doc, newnode, child);
        }
    }

    public Document prepareDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            for (CXNode ccnode : listModules()) {
                doc.getDocumentElement();
                prepareNode(doc, null, ccnode);
            }
            return doc;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    File _temporary = null;

    public boolean writeToTeporary() throws IOException, TransformerException {
        _temporary = null;
        System.out.println("writing " + _file);
        File temporary = MXUtil.createTemporaryFile(_file);
        System.out.println("temp " + temporary);
        writeDocumentSub(temporary, prepareDocument());
        if (MXUtil.compareFileText(_file, temporary) == 0) {
            temporary.delete();
            return false;
        }
        _temporary = temporary;
        return true;
    }

    public void moveTemporaryToThis() {
        if (_temporary != null) {
            File temp = _temporary;
            MXUtil.safeRenameToBackup(_file);
            if (_file.exists()) {
                _file.delete();
            }
            _temporary.renameTo(_file);
        }
    }

    protected void writeDocument(OutputStream file) throws TransformerException {
        writeDocumentSub(file, prepareDocument());
    }

    protected void writeDocumentSub(OutputStream file, Document doc) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer tf = factory.newTransformer();

        tf.setOutputProperty("indent", "yes");
        if (_fileEncoding == null) {
            tf.setOutputProperty("encoding", "UTF-8");
        } else {
            tf.setOutputProperty("encoding", _fileEncoding);
        }

        tf.transform(new DOMSource(doc), new StreamResult(file));
    }

    protected void writeDocumentSub(File file, Document doc) throws IOException, TransformerException {
        boolean result = false;
        OutputStream out = new FileOutputStream(file);
        BufferedOutputStream bout = new BufferedOutputStream(out);
        writeDocumentSub(bout, doc);
        out.close();
    }

    public static class TableInfo {

        String _id;
        MXWrapList<Integer> _table;
    }

    MXWrapList<MXWrapList<Integer>> _listTable = null;

    public MXWrapList<Integer> getTable(int id) {
        prepareTable();
        if (_listTable == null) {
            return null;
        }
        return _listTable.valueOfName(String.valueOf(id));
    }

    public static Integer parseNumber(String value) {
        try {
            int x;
            if (value.length() >= 3 && value.startsWith("0x")) {
                x = Integer.parseInt(value.substring(2), 16);
            } else if (value.length() >= 2 && value.endsWith("H") || value.endsWith("h")) {
                x = Integer.parseInt(value.substring(0, value.length() - 2), 16);
            } else {
                x = Integer.parseInt(value);
            }
            return x;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public void prepareTable() {
        if (_listTable != null) {
            return;
        }
        CCRuleManager rules = CCRuleManager.getInstance();
        CXNode ccm = _document.firstChild(rules.controlChangeMacroList);
        if (ccm == null) {
            return;
        }
        _listTable = new MXWrapList<>();

        List<CXNode> listTable = ccm.listChildren(rules.controlChangeMacroList_table);
        if (listTable == null) {
            return;
        }
        for (CXNode seekTable : listTable) {
            String id = seekTable._listAttributes.valueOfName("ID");
            int id2 = parseNumber(id);
            if (id2 < 0) {
                continue;
            }
            List<CXNode> listEntry = seekTable.listChildren("Entry");
            if (listEntry == null) {
                continue;
            }
            MXWrapList<Integer> contents = new MXWrapList<>();

            for (CXNode seekEntry : listEntry) {
                String label = seekEntry._listAttributes.valueOfName("Label");
                String value = seekEntry._listAttributes.valueOfName("Value");
                if (value == null || value.isBlank()) {
                    continue;
                }
                if (label == null || label.isBlank()) {
                    //is it right?
                    label = value;
                }
                int x = parseNumber(value);
                
                contents.addNameAndValue(label, x);
            }

            if (!contents.isEmpty()) {
                _listTable.addNameAndValue(String.valueOf(id2), contents);
            }
        }
    }
}
