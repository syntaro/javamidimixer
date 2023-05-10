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
package jp.synthtarou.midimixer.libs.domino.unreach;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import org.xml.sax.helpers.DefaultHandler;
 
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
 
/**
 *
 * @author Syntarou YOSHIDA
 */
public class DOMTest extends DefaultHandler{
    private static final MXDebugPrint _debug = new MXDebugPrint(DOMTest.class);
    
    public DOMTest() {
    }
    
    public static String getChildText(Node node) {
        NodeList childList= node.getChildNodes();
        for (int i = 0; i < childList.getLength(); ++ i) {
            Node n = childList.item(i);
            short childType = n.getNodeType();
            if (childType == Node.TEXT_NODE) {
                return cutspace(n.getNodeValue());
            }
        }
        return null;
    }
    
    public static void main(String[] args) throws Exception {
        MXDebugPrint.globalSwitchOn();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        //Document document = builder.parse(new File("c:/java/recepi2.xml"));
        Document document = builder.parse(new File("c:/java/GMLevel1.xml"));
        Element bookList = document.getDocumentElement();
        
        ArrayList<Node> process = new ArrayList();
        process.add(document.getDocumentElement());

        while (process.size() > 0) {
            Node node = process.get(process.size() - 1);
            process.remove(process.size() - 1);
            String nodeName = node.getNodeName();
            String nodeValue  = cutspace(node.getNodeValue());
            short nodeType = node.getNodeType();
            
            ArrayList<String> path = new ArrayList();
            if (!nodeName.startsWith("#")) {
                path.add(nodeName);
            }
            Node parent = node.getParentNode();
            while (parent != null) {
                if (!parent.getNodeName().startsWith("#")) {
                    path.add(0, parent.getNodeName());
                }
                parent = parent.getParentNode();
            }
            
            StringBuffer str = new StringBuffer();
            
            switch(nodeType) {
                case Node.DOCUMENT_TYPE_NODE:
                    //str.append("Document " + path + " = " + nodeValue + "\n");
                    break;
                case Node.ELEMENT_NODE:
                    if (nodeValue == null) {
                        str.append(path.toString());
                    }else {
                        str.append(path + " = " + nodeValue);
                    }
                    NamedNodeMap attrList = node.getAttributes();
                    if (attrList != null && attrList.getLength() > 0) {
                        str.append(" Attr{");
                        for(int i = 0; i < attrList.getLength(); ++ i) {
                            Node attr = attrList.item(i);
                            str.append("[" + attr.getNodeName() + "]=[" + attr.getNodeValue() + "]");
                        }
                        str.append("}");
                    }
                    String childText = getChildText(node);
                    if (childText != null && childText.length() > 0) {
                        str.append("<TEXT>"  + childText + "</TEXT>");
                    }
                    _debug.println(str.toString());

                    NodeList childList = node.getChildNodes();
                    for (int x = 0; x < childList.getLength(); ++ x) {
                        Node child = childList.item(childList.getLength() - 1 - x);
                        if (child.getNodeType() != Node.TEXT_NODE) {
                            process.add(child);
                        }
                    }
                    break;
                case Node.ENTITY_NODE:
                    _debug.println("#Entity " + path + " = " + nodeValue);
                    break;
                case Node.ENTITY_REFERENCE_NODE:
                    _debug.println("#EntityReference " + path + " = " + nodeValue);
                    break;
                case Node.NOTATION_NODE:
                    _debug.println("#Notation" + path + " = " + nodeValue);
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    _debug.println("#ProcessingInstruction " + path + " = " + nodeValue);
                    break;
                case Node.TEXT_NODE:
                    if (nodeValue != null && nodeValue.length() > 0) {
                        _debug.println("<TEXT>" + nodeValue + "</TEXT>");
                    }
                    break;
            }
        }
    }
    
    public static String cutspace(String original) {
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

    public static boolean writeDocument(File file, Document doc) {
        Transformer tf = null;
 
        try {
            TransformerFactory factory = TransformerFactory
                  .newInstance();
            tf = factory.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            return false;
        }
 
        tf.setOutputProperty("indent", "yes");
        tf.setOutputProperty("encoding", "UTF-8");
        FileWriter writer = null;

        try {
            StringWriter string = new StringWriter();
            tf.transform(new DOMSource(doc), new StreamResult(string));
            
            writer = new FileWriter(file);
            writer.write(string.toString());
            writer.close();
            writer = null;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        } finally {
            if (writer != null) {
                try { writer.close(); } catch(IOException e) { }
                file.delete();
            } 
        }
 
        return true;
    }
}
