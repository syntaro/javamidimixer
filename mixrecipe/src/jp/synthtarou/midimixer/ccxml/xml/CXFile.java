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

import jp.synthtarou.midimixer.ccxml.rules.CCRuleManager;
import jp.synthtarou.midimixer.ccxml.rules.CCRuleForAttribute;
import jp.synthtarou.midimixer.ccxml.rules.CCRuleForTag;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
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
import jp.synthtarou.midimixer.ccxml.InformationForCCM;
import jp.synthtarou.midimixer.ccxml.InformationForModule;
import jp.synthtarou.midimixer.ccxml.rules.CCValueRule;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.namedobject.MXNamedObject;
import jp.synthtarou.libs.MXLineReader;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
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
        return (_loadError == null) && _listModules.isEmpty() == false;
    }
    
    public ArrayList<InformationForModule> listModules() {
        return _listModules;
    }
 
    static int countRunSame = 0;
    static int countRunDiffectent = 0;

    public static void main(String[] args) {
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
                    for (String text : f2._listDebug.keySet()) {
                        ArrayList<CXNode> nodeList = f2._listDebug.get(text);
                        System.out.println(text);
                        for (CXNode seek : nodeList) {
                            System.out.println("  " + seek._listAttributes.valueOfName("Name") + "@" + seek._lineNumber);
                        }
                        System.out.println();
                    }
                    //System.out.println(f2.getAdviceForXML());
                }
            }
        }
        System.out.println("testSame = " + countRunSame);
        System.out.println("testDifferent = " + countRunDiffectent);
    }

    public static boolean nearly(String text1, String text2) {
        try {
            text1 = text1.replace("[", " [ ");
            text2 = text2.replace("[", " [ ");
            text1 = text1.replace("]", " ] ");
            text2 = text2.replace("]", " ] ");

            text1 = text1.replace("  ", " ");
            text2 = text2.replace("  ", " ");

            text1 = text1.replace("  ", " ");
            text2 = text2.replace("  ", " ");

            String[] array1 = text1.split(" ");
            String[] array2 = text2.split(" ");

            if (array1.length != array2.length) {
                return false;
            }

            for (int x = 0; x < array1.length; ++x) {
                String seg1 = array1[x].toUpperCase();
                String seg2 = array2[x].toUpperCase();

                if (seg1.equals(seg2)) {
                    continue;
                }

                if (seg1.endsWith("H")) {
                    String hex = seg1.substring(0, seg1.length() - 1);
                    int var = Integer.parseInt(hex, 16);
                    seg1 = Integer.toString(var);
                }
                if (seg2.endsWith("H")) {
                    String hex = seg2.substring(0, seg2.length() - 1);
                    int var = Integer.parseInt(hex, 16);
                    seg2 = Integer.toString(var);
                }

                if (seg1.equals(seg2) == false) {
                    return false;
                }
            }

            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    static File _editingDirecotory = new File(MXUtil.getAppBaseDirectory(), "Editing");

    public final File _file;
    public final String _fileEncoding;

    public final CXNode _document = new CXNode(null, "", CCRuleManager.getInstance().getRootTag());
    final ArrayList<CXNode> _listWarning = new ArrayList<>();
    final TreeMap<String, ArrayList<CXNode>> _listDebug = new TreeMap();
    final ArrayList<InformationForModule> _listModules = new ArrayList<>();
    Throwable _loadError;

    public CXFile(String fileName) {
        this(new File(_editingDirecotory, fileName));
    }

    public CXFile(File file) {
        _file = file;
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
                MXFileLogger.getLogger(CXFile.class.getName()).log(Level.SEVERE, null, ex);
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
            } finally {
                stream.close();
            }

            rebuildCache();
        } catch (ParserConfigurationException ex) {
            _loadError = ex;
            MXFileLogger.getLogger(CXFile.class).log(Level.WARNING, ex.getMessage(), ex);
            return;
        } catch (SAXException ex) {
            _loadError = ex;
            MXFileLogger.getLogger(CXFile.class).log(Level.WARNING, ex.getMessage(), ex);
            return;
        } catch (IOException ex) {
            _loadError = ex;
            MXFileLogger.getLogger(CXFile.class).log(Level.WARNING, ex.getMessage(), ex);
            return;
        } catch (RuntimeException ex) {
            _loadError = ex;
            MXFileLogger.getLogger(CXFile.class).log(Level.WARNING, ex.getMessage(), ex);
            return;
        }
        _loadError = null;
    }
    
    public void rebuildCache() {
        _listModules.clear();
        _listWarning.clear();
        _listDebug.clear();
        CCRuleManager rule = CCRuleManager.getInstance();
         for (CXNode seek : _document._listChildTags) {
             if (seek._nodeName.equals("ModuleData")) {
                 InformationForModule module = new InformationForModule(this,seek);
                 module.fillCCMLink();
                 _listModules.add(module);
                 checkWaring(module, seek, rule.getModuleDataTag());
             }
         }
    }
    
    public void recordCCMDebug(String key, CXNode node) {
        ArrayList<CXNode> list  = _listDebug.get(key);
        if (list == null) {
            list = new ArrayList<>();
            _listDebug.put(key, list);
        }
        list.add(node);
    }

    public void checkWaring(InformationForModule module, CXNode target, CCRuleForTag targetRule) {
        target._warningText = null;

        if (target._nodeName.equals("CCM")) {
            InformationForCCM ccm = new InformationForCCM(module, target);
            String err;
            
            if (ccm._data == null) {
                err = module._file +" have null SysEx";
                recordCCMDebug(err, target);
            }else  if (ccm._data.startsWith("@SYSEX")) {
                boolean templateOK = false;
                try {
                    MXTemplate template = null;
                    try {
                        template = new MXTemplate(ccm._data);
                        if (template.get(1) == MXMidi.COMMAND_SYSEX || template.get(1) == MXMidi.COMMAND_SYSEX_END) 
                            if (template.get(template.size() - 1) == 0xf7) {
                                templateOK = true;
                        }
                        if (template.haveChecksum()) {
                            err = module._file +" have checksum";
                            recordCCMDebug(err, target);
                        }
                    } catch (IllegalFormatException ex) {
                        MXFileLogger.getLogger(CXFile.class).log(Level.WARNING, ex.getMessage(), ex);
                    }
                }catch(Exception ex) {
                    MXFileLogger.getLogger(CXFile.class).log(Level.WARNING, ex.getMessage(), ex);
                }
                if (!templateOK) {
                    err = module._file +" have Wrong SysEx [" + ccm._data+ "]";
                    recordCCMDebug(err, target);
                }
            }
            if (ccm._offsetGate != 0) {
                err = module._file +" have offset gate";
                recordCCMDebug(err, target);
                if (ccm._gateTable != null) {
                    err = module._file +" have offset gateTable";
                    recordCCMDebug(err, target);
                }
            }
            if (ccm._offsetValue != 0) {
                err = module._file +" have offset value";
                recordCCMDebug(err, target);
                if (ccm._valueTable != null) {
                    err = module._file +" have offset valueTable";
                    recordCCMDebug(err, target);
                }
            }
        }

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
        for (MXNamedObject<String> keyValue : target._listAttributes) {
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
            checkWaring(module, child, childRule);
        }

        if (undocumentedTag.size() > 0) {
            warning.append(" undocumented tags " + undocumentedTag + "");
        }

        if (warning.length() > 0) {
            target._warningText = warning.toString();
            _listWarning.add(target);
        }
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
        if (isLoaded() == false) {
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
        for (MXNamedObject<String> attr : ccnode._listAttributes) {
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
            CCRuleManager rule = CCRuleManager.getInstance();
            for (CXNode ccmodule : _document.listChildren(rule.moduleData)) {
                doc.getDocumentElement();
                prepareNode(doc, null, ccmodule);
            }
            return doc;

        } catch (Exception ex) {
            MXFileLogger.getLogger(CXFile.class).log(Level.WARNING, ex.getMessage(), ex);
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

    public void doChecksum(InformationForCCM ccm) {

        try {
            MXTemplate temp = new MXTemplate(ccm._data);
            String data2 = temp.toDText();
            String data3 = new MXTemplate(temp.toIntArray()).toDText();
            if (nearly(ccm._data, data3)) {
                countRunSame++;
            } else {
                countRunDiffectent++;
                MXFileLogger.getLogger(CXFile.class).info(ccm._data + " != " + data2 + "  != " + data3);
            }
        } catch (Exception ex) {
            MXFileLogger.getLogger(CXFile.class).log(Level.WARNING, ex.getMessage(), ex);
        }
    }
}
