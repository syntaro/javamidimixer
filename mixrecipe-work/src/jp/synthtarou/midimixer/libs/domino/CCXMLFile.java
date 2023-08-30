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
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.text.MXLineReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCXMLFile {

    final ArrayList<CCXMLNode> _arrayModuleData = new ArrayList<>();
    public final File _file;
    final ArrayList<CCXMLNode> _listWarning = new ArrayList<>();
    Exception _loadError;

    public String toString() {
        return _file.toString();
    }

    public boolean isLoaded() {
        return (_loadError == null) && countModule() > 0;
    }

    public int countModule() {
        return _arrayModuleData.size();
    }

    public CCXMLNode getModule(int module) {
        return _arrayModuleData.get(module);
    }

    public MXWrapList<CCXMLNode> listModules() {
        MXWrapList<CCXMLNode> list = new MXWrapList<>();

        for (CCXMLNode tag : _arrayModuleData) {
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

        return list;
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
                    CCXMLFile f2 = new CCXMLFile(file);
                    f2.dumpWarning();

                    /*
                    try {
                        f2.writeDocument(System.out);
                    } catch (TransformerException ex) {
                        Logger.getLogger(CCXMLFile.class.getName()).log(Level.SEVERE, null, ex);
                    }*/
                }
            }
        }
    }

    static File _editingDirecotory = new File(MXUtil.getAppBaseDirectory(), "Editing");

    public CCXMLFile(String name) {
        this(new File(_editingDirecotory, name));
    }

    public final String _fileEncoding;
    
    public CCXMLFile(File file) {
        _file = file;

        Element docElement;
        
        String encoding = null;

        try {
            InputStream check = new FileInputStream(file);
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
                Logger.getLogger(CCXMLFile.class.getName()).log(Level.SEVERE, null, ex);
            }

        }catch(IOException ex) {
            _loadError = ex;
            return;
        }
        finally {
            _fileEncoding = encoding;
        }
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            InputStream stream = new FileInputStream(file);
            CCDocumentHandler handler = new CCDocumentHandler();
            

            try {
                InputSource source = new InputSource(stream);
                saxParser.parse(source, handler);
                System.out.println("source encoding  = "+ source.getEncoding());

            } finally {
                stream.close();
            }
            
            _arrayModuleData.clear();

            for (CCXMLNode moduleNode : handler._document._listChildTags) {
                CCRuleManager rule = CCRuleManager.getInstance();
                if (moduleNode._name.equalsIgnoreCase(rule.getModuleDataTag().getName())) {
                    fillRules(moduleNode, rule.getModuleDataTag());
                    _arrayModuleData.add(moduleNode);
                }
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

    public void fillRules(CCXMLNode target, CCRuleElement targetRule) {
        target._warningText = null;

        if (targetRule == null) {
            target._warningText = " this is undocumented ";
            _listWarning.add(target);
            return;
        }

        StringBuffer warning = new StringBuffer();
        ArrayList<String> missingAttr = new ArrayList<>();
        ArrayList<String> undocumentedAttr = new ArrayList<>();

        for (CCRuleAttributes ruleAttr : targetRule.listAttributes()) {
            if (ruleAttr.isMust()) {
                if (target._listAttributes.indexOfName(ruleAttr.getName()) < 0) {
                    missingAttr.add(ruleAttr.getName());
                }
            }
        }
        if (missingAttr.size() > 0) {
            warning.append(target._name + " hasn's attributes " + missingAttr + "");
        }
        for (MXWrap<String> keyValue : target._listAttributes) {
            if (targetRule.getAttribute(keyValue.name) == null) {
                undocumentedAttr.add(keyValue.name + "=" + keyValue.value);
            }
        }

        if (undocumentedAttr.size() > 0) {
            warning.append(target._name + " have undocumented attributes " + undocumentedAttr + "");
        }

        ArrayList<String> undocumentedTag = new ArrayList<>();

        for (CCXMLNode child : target._listChildTags) {
            CCRuleElement childRule = targetRule.findChildRule(child._name);
            if (childRule == null) {
                undocumentedTag.add(child._name);
            }
            fillRules(child, childRule);
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

    public List<CCXMLNode> listWarning() {
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
        if (countModule() == 0) {
            return "Error, This XML Don't have '<Module>'";
        }
        StringBuffer str = new StringBuffer();
        for (CCXMLNode node : _listWarning) {
            if (str.length() > 0) {
                str.append("\n");
            }
            str.append("" + node._lineNumber + ", " + node._columnNumber + ": " + node._warningText);
        }
        return str.toString();
    }

    public static void prepareNode(Document doc, Element parent, CCXMLNode ccnode) {
        Element newnode = doc.createElement(ccnode._name);

        if (ccnode._textContext != null && ccnode._textContext.length() > 0) {
            newnode.setTextContent(MXUtil.shrinkText(ccnode._textContext));
        }
        for (MXWrap<String> attr : ccnode._listAttributes) {
            String name = attr.name;
            String value = attr.value;
            newnode.setAttribute(name, value);
        }

        if (parent == null) {
            doc.appendChild(newnode);
        } else {
            parent.appendChild(newnode);
        }

        for (CCXMLNode child : ccnode._listChildTags) {
            prepareNode(doc, newnode, child);
        }
    }

    public Document prepareDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            for (int i = 0; i < countModule(); ++i) {
                CCXMLNode ccnode = getModule(i);

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
        File temporary = MXUtil.createTemporaryFile(_file);
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

    public void writeDocument(OutputStream file) throws TransformerException {
        writeDocumentSub(file, prepareDocument());
    }

    public void writeDocumentSub(OutputStream file, Document doc) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer tf = factory.newTransformer();

        tf.setOutputProperty("indent", "yes");
        if (_fileEncoding == null) {
            tf.setOutputProperty("encoding", "UTF-8");
        }
        else {
            tf.setOutputProperty("encoding", _fileEncoding);
        }

        tf.transform(new DOMSource(doc), new StreamResult(file));
    }

    public void writeDocumentSub(File file, Document doc) throws IOException, TransformerException {
        boolean result = false;
        System.out.println("writing " + file);
        OutputStream out = new FileOutputStream(file);
        BufferedOutputStream bout = new BufferedOutputStream(out);
        writeDocumentSub(bout, doc);
        out.close();
    }
}
