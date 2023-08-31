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
package jp.synthtarou.midimixer.libs.xml;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.domino.CCParameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXDOMElement {
    private static final MXDebugPrint _debug = new MXDebugPrint(MXDOMElement.class);

    MXDOMElement _parent;
    String _nodeName;
    String _text;
    List<String> _nodePath;
    MXWrapList<MXDOMElement> _childElements;
    CCParameters _attributes;

    public int _userDataType;
    public Object _userData;
    
    public MXDOMElement(MXDOMElement parent, Element element) {
        _parent = parent;
        _childElements = new MXWrapList();
        _attributes = new CCParameters();
        _nodeName = element.getNodeName();
        ArrayList nodePath = new ArrayList();

        nodePath.add(element.getNodeName());
        Node seek = element.getParentNode();
        while (seek != null) {
            if (!seek.getNodeName().startsWith("#")) {
                nodePath.add(0, seek.getNodeName());
            }
            seek = seek.getParentNode();
        }
        _nodePath = Collections.unmodifiableList(nodePath);
        NodeList list = element.getChildNodes();
        for (int x = 0; x < list.getLength(); ++ x) {
            Node node = list.item(x);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                _childElements.addNameAndValue(node.getNodeName(), new MXDOMElement(this, (Element)node));
            }
            if (node.getNodeType() == Node.TEXT_NODE) {
                String text1 = shrinkSpace(node.getNodeValue());
                if (text1 != null) {
                    _text = text1;
                }
            }
        }
        NamedNodeMap fusion = element.getAttributes();
        for (int x = 0; x < fusion.getLength(); ++ x) {
            Node n = fusion.item(x);
            String name = n.getNodeName();
            String value = n.getNodeValue();
            value = MXDOMElement.shrinkSpace(value);
            if (value != null && value.length() == 0) {
                value = null;
            }
            _attributes.addNameAndValue(name, value);
        }
    }
    
    public String getNodeName() {
        return _nodeName;
    }

    public List<String> getNodePath() {
        return _nodePath;
    }

    public static MXDOMElement fromDocument(Document doc) {
        return new MXDOMElement(null, doc.getDocumentElement());
    }
    
    public static MXDOMElement fromFile(File file) throws SAXException {
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document document;
        factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
            return null;
        }
        try {
            document = builder.parse(file);
        } catch (SAXException ex) {
            throw ex;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        MXDOMElement docElement = MXDOMElement.fromDocument(document);
        return docElement;
    }
    
    public MXDOMElement getParentNode() {
        return _parent;
    }
    
    public List<MXDOMElement> getChildElements() {
        return _childElements.valueList();
    }

    public List<MXDOMElement> getChildElements(String name) {
        ArrayList<MXDOMElement> list = new ArrayList();
        for (MXWrap<MXDOMElement> child : _childElements) {
            if (name.equals(child.value.getNodeName())) {
                list.add(child.value);
            }
        }
        return list;
    }
    
    public String getText() {
        return _text;
    }

    public CCParameters getAttributesMap() {
        return _attributes;
    }

    public static String shrinkSpace(String original) {
        if (original == null) {
            return null;
        }
        StringBuffer text = new StringBuffer(original);
        
        while(text.length() > 0) {
            char c = text.charAt(0);
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                text.deleteCharAt(0);
                continue;
            }
            c = text.charAt(text.length() - 1);
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                text.deleteCharAt(text.length() - 1);
                continue;
            }
            break;
        }
        
        return text.toString();
    }
    
    public static void main(String[] args) throws Exception {
        PrintStream output = System.out;

        MXDebugPrint.globalSwitchOn();

        MXDOMElement rootElement = MXDOMElement.fromFile(new File("C:/Domino144/Module/GMLevel1.xml"));
        MXDOMElement docElement = rootElement;

        ArrayList<MXDOMElement> process = new ArrayList();
        process.add(docElement);
        

        CCParameters params = docElement.getAttributesMap();
        if (params.size() > 0) {
            output.print("{");
            for (int i = 0; i < params.size(); ++ i) {
                if (i > 0) {
                    output.print(",");
                }
                output.print(params.get(i).name + "=" + params.get(i).value);
            }
            output.print("}");
        }

        while (process.size() > 0) {
            MXDOMElement element = process.remove(process.size() - 1);
            
            String nodeName = element.getNodeName();
            String text = element.getText();
            List<MXDOMElement> child = element.getChildElements();
            List<String> path = element.getNodePath();

            output.print("<" + path + ">");
            if (text != null && text.length() > 0) {
                output.print("text = " +  text);
            }
            CCParameters eparams = element.getAttributesMap();
            if (eparams.size() > 0) {
                output.print("{");
                for (int i = 0; i < eparams.size(); ++ i) {
                    if (i > 0) {
                        output.print(",");
                    }
                    output.print(eparams.get(i).name + "=" + eparams.get(i).value);
                }
                output.print("}");
            }
            _debug.println();
            
            if (child.size() > 0) {
                for (int x = child.size() - 1; x >= 0; x --) {                    
                    process.add(child.get(x));
                }
            }
        }

        process.clear();
        process.add(docElement);

        while(process.size() > 0) {
            MXDOMElement e = process.remove(0);
            _debug.println("-" + e.getNodeName());
            List<MXDOMElement> instList = e.getChildElements("InstrumentList");

            while(instList.size() > 0) {
                MXDOMElement inst = instList.remove(0);
                List<MXDOMElement> mapList = inst.getChildElements("Map");
                
                while(mapList.size() > 0) {
                    MXDOMElement map = mapList.remove(0);

                    List<MXDOMElement> listPC = map.getChildElements("PC");
                    while(listPC.size() > 0) {
                        MXDOMElement pc = listPC.remove(0);
                        _debug.println(pc.getNodeName() + "=" + pc.getText() + pc.getAttributesMap());

                        List<MXDOMElement> listBank = pc.getChildElements("Bank");
                        while(listBank.size() > 0) {
                            MXDOMElement bank = listBank.remove(0);
                            _debug.println(bank.getNodeName() + "=" + bank.getText() + bank.getAttributesMap());
                        }
                    }
                }
            }
        }
    }
    
    public void dump(PrintStream output) {
        ArrayList<MXDOMElement> process = new ArrayList();
        MXDOMElement e = this;
        process.add(e);
        
        CCParameters params =  e.getAttributesMap();

        if (params.size() > 0) {
            output.print("{");
            for (int i = 0; i < params.size(); ++ i) {
                if (i > 0) {
                    output.print(",");
                }
                output.print(params.get(i).name + "=" + params.get(i).value);
            }
            output.print("}");
        }

        while (process.size() > 0) {
            MXDOMElement element = process.remove(process.size() - 1);
            
            String nodeName = element.getNodeName();
            String text = element.getText();
            List<MXDOMElement> child = element.getChildElements();
            List<String> path = element.getNodePath();

            output.print("<" + path + ">");
            if (text != null && text.length() > 0) {
                output.print("text = " +  text);
            }
            CCParameters eparams = element.getAttributesMap();
            if (eparams.size() > 0) {
                output.print("{");
                for (int i = 0; i < eparams.size(); ++ i) {
                    if (i > 0) {
                        output.print(",");
                    }
                    output.print(eparams.get(i).name + "=" + eparams.get(i).value);
                }
                output.print("}");
            }
            _debug.println();
            
            if (child.size() > 0) {
                for (int x = child.size() - 1; x >= 0; x --) {                    
                    process.add(child.get(x));
                }
            }
        }
        _debug.println("-------------------------");

        process.clear();
        process.add(e);

        while(process.size() > 0) {
            e = process.remove(0);
            _debug.println("-" + e.getNodeName());
            List<MXDOMElement> instList = e.getChildElements("InstrumentList");

            while(instList.size() > 0) {
                MXDOMElement inst = instList.remove(0);
                List<MXDOMElement> mapList = inst.getChildElements("Map");
                
                while(mapList.size() > 0) {
                    MXDOMElement map = mapList.remove(0);

                    List<MXDOMElement> listPC = map.getChildElements("PC");
                    while(listPC.size() > 0) {
                        MXDOMElement pc = listPC.remove(0);
                        _debug.println(pc.getNodeName() + "=[" + pc.getText() + "]" + pc.getAttributesMap());

                        List<MXDOMElement> listBank = pc.getChildElements("Bank");
                        while(listBank.size() > 0) {
                            MXDOMElement bank = listBank.remove(0);
                            _debug.println("    "  + bank.getNodeName() + "=[" + bank.getText() + "]" + bank.getAttributesMap());
                        }
                    }
                }
            }
        }
    }

    public String toString() {
        return _nodePath.toString();
    }
}
