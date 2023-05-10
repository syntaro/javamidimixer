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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
 
/**
 *
 * @author Syntarou YOSHIDA
 */
public class SAXTest extends DefaultHandler{
    boolean startItem = false;
    ArrayList<String> path = new ArrayList();
    DocumentBuilder builder;
    Document document;
    ArrayList<Element> pathOut = new ArrayList();
    
    public SAXTest() {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
            System.out.println("保存できません");
            return;
        }
        document = builder.newDocument();
    }
    
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {//[1]
        MXDebugPrint.globalSwitchOn();
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();//[2]
        SAXParser saxParser = saxParserFactory.newSAXParser();//[3]
        
        InputStream stream = new FileInputStream("c:/java/receipt_complex.xml");
        try {
            InputSource source = new InputSource(stream);
            
            SAXTest test = new SAXTest();
            saxParser.parse(source, test);
            writeDocument(new File("c:/java/recepi2.xml"), test.document);
        }finally {
            stream.close();
        }
    }

    public void startDocument() {//[10]
        //System.out.println("[11] ドキュメント開始");
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {//[20]
        path.add(qName);
        System.out.println(path);//[21]
        
        Element element = document.createElement(qName);
        if (pathOut.size() == 0) {
            document.appendChild(element);
        }else {
            pathOut.get(pathOut.size() - 1).appendChild(element);
        }
        pathOut.add(element);
        
        if (attributes.getLength() > 0) {
            System.out.print("attr = ");
            for (int i = 0; i < attributes.getLength(); ++ i) {
                String aName = attributes.getQName(i);
                String aValue = attributes.getValue(i);
                System.out.print("[" + aName + "]=["+ aValue + "]");
                element.setAttribute(aName, aValue);
            }
            System.out.println("");
        }
        if (qName.equals("shop")) {//[22]
            startItem = true;//[23]
            return;//[24]
        }
        if (qName.equals("item")) {//[25]
            startItem = true;//[26]
            return;//[27]
        }
    }
    public void characters(char[] ch, int offset, int length) {//[30]
        StringBuffer text = new StringBuffer();
        text.append(ch, offset, length);
        
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
        
        if (text.length() > 0) {
            System.out.println(path + "'s TEXT=[" + text + "]");
            pathOut.get(pathOut.size() - 1).setTextContent(text.toString());
        }
    }
    public void endElement(String uri, String localName, String qName) {//[40]
        //System.out.println("[41] 要素終了 = " + path);
        if (path.size() > 0) {
            if (path.get(path.size() -1).equals(qName)) {
                path.remove(path.size() - 1);
                pathOut.remove(pathOut.size() - 1);
            }else {
                System.out.println("XML ERROR " + qName);
            }
        }else {
            System.out.println("XML ERROR " + qName);
        }
        startItem = false;//[42]
    }

    public void endDocument(){//[50]
        //System.out.println("[51] ドキュメント終了");
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
 
          try {
               tf.transform(new DOMSource(doc), new StreamResult(
                         file));
          } catch (TransformerException e) {
               e.printStackTrace();
               return false;
          }
 
          return true;
     }
}
