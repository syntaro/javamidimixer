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

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import jp.synthtarou.midimixer.libs.xml.MXDOMElement;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.midi.programlist.database.PDBank;
import jp.synthtarou.midimixer.libs.midi.programlist.database.PDDrumSet;
import jp.synthtarou.midimixer.libs.midi.programlist.database.PDFile;
import jp.synthtarou.midimixer.libs.midi.programlist.database.PDMap;
import jp.synthtarou.midimixer.libs.midi.programlist.database.PDModule;
import jp.synthtarou.midimixer.libs.midi.programlist.database.PDProgram;
import jp.synthtarou.midimixer.libs.midi.programlist.database.ProgramPicker;
import org.xml.sax.SAXException;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class DTextXML implements PDFile {
    private static final MXDebugPrint _debug = new MXDebugPrint(DTextXML.class);

    static boolean _DEBUG = false;

    File _file;
    
    public String getName() {
        return getFile().getName();
    }
    
    public File getFile() {
        return _file;
    }
    
    String _fileCreator;
    String _fileVersion;
    String _folder;
    String _name;
    String _priority;
    String _webSite;
 
    PDModule _module;
    PDModule _drumSet;
    DTextMessageList _ccList;
    
    String RhythmTrackDefault_Gate;
    String ExclusiveEventDefault_Data;
    String ProgramChangeEventPropertyDlg_AutoPreviewDelay;
    String ControlChangeEventDefault_ID;
    
    public PDModule getModule() {
        return _module;
    }
    
    public PDModule getDrumSet() {
        return _drumSet;
    }
    
    public DTextMessageList getCCList() {
        return _ccList;
    }
    
    public DTextXML(MXDOMElement e) {
        MXDOMElement _templateElement;
        MXDOMElement _defaultDataElement;
        TreeSet<String> folderAfter = new TreeSet();
        TreeMap<String, MXDOMElement> _idedFolders = new TreeMap();


        ArrayList<MXDOMElement> seeking = new ArrayList();
        seeking.add(e);

        ArrayList<MXDOMElement> instElement = new ArrayList();
        ArrayList<MXDOMElement> drumElement = new ArrayList();
        MXDOMElement ccElement = null;
        
        _fileCreator = e.getAttributeValue("FileCreator");
        _fileVersion = e.getAttributeValue("FileVersion");
        _folder = e.getAttributeValue("Folder");
        _name = e.getAttributeValue("Name");
        _priority = e.getAttributeValue("Priority");
        _webSite = e.getAttributeValue("WebSite");

        while (seeking.size() > 0) {
            MXDOMElement element = seeking.remove(seeking.size() - 1);
            
            String nodeName = element.getNodeName();
            String text = element.getText();
            List<String> path = element.getNodePath();

            List<MXDOMElement> child = element.getChildElements();
            if (child.size() > 0) {
                for (int x = child.size() - 1; x >= 0; x --) {                    
                    seeking.add(child.get(x));
                }
            }
            
            if (nodeName.equalsIgnoreCase("Folder")) {
                String id = element.getAttributeValue("ID");
                if (id != null) {
                    if (id.length() > 0) {
                        _idedFolders.put(id, element);
                    }
                }
            }
            
            if (nodeName.equalsIgnoreCase("Folder") || nodeName.equalsIgnoreCase("FolderLink")) {
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < path.size() - 1; ++ i) {
                    if (i != 0) {
                        buffer.append(",");
                    }
                    buffer.append(path.get(i));
                }
                folderAfter.add(buffer.toString());
            }
            
            //Folder以外のパスを算出
            ArrayList<String> pathRecalc = new ArrayList();
            for (String step : path) {
                if (step.equalsIgnoreCase("Folder")) {
                    continue;
                }
                if (step.equalsIgnoreCase("FolderLink")) {
                    continue;
                }
                pathRecalc.add(step);
            }
            path = pathRecalc;

            if (nodeName.equalsIgnoreCase("Folder")) {
                //ignore
                continue;
            }

            if (pathEndWith(path, "InstrumentList,Map")) {
                instElement.add(element);
                continue;
            }
            if (pathEndWith(path, "DrumSetList,Map")) {
                drumElement.add(element);
                continue;
            }
            else if (nodeName.equalsIgnoreCase("TemplateList")) {
                _templateElement = element;
                continue;
            }
            else if (nodeName.equalsIgnoreCase("DefaultData")) {
                _defaultDataElement = element;
                continue;
            }
            else if (pathEndWith(path, "ModuleData")) {
                continue;
            }
            else if (pathEndWith(path, "ModuleData,InstrumentList")
                  || pathEndWith(path, "ModuleData,InstrumentList,Map")
                  || pathEndWith(path, "ModuleData,InstrumentList,Map,PC")
                  || pathEndWith(path, "ModuleData,InstrumentList,Map,PC,Bank")
                    ) {
                continue;
            }
            else if (pathEndWith(path, "ModuleData,DrumSetList")
                  || pathEndWith(path, "ModuleData,DrumSetList,Map")
                  || pathEndWith(path, "ModuleData,DrumSetList,Map,PC")
                  || pathEndWith(path, "ModuleData,DrumSetList,Map,PC,Bank")
                  || pathEndWith(path, "ModuleData,DrumSetList,Map,PC,Bank,Tone")
                    ) {
                continue;
            }
            else if (pathEndWith(path, "ModuleData,ControlChangeMacroList")) {
                ccElement = element;
                continue;
            }
            else if (pathEndWith(path, "ModuleData,ControlChangeMacroList,Memo")
                  || pathEndWith(path, "ModuleData,ControlChangeMacroList,Table")
                  || pathEndWith(path, "ModuleData,ControlChangeMacroList,Table,Entry")
                  || pathEndWith(path, "ModuleData,ControlChangeMacroList,CCMLink")
                  || pathEndWith(path, "ModuleData,ControlChangeMacroList,CCM")
                  || pathEndWith(path, "ModuleData,ControlChangeMacroList,CCM,Memo")
                  || pathEndWith(path, "ModuleData,ControlChangeMacroList,CCM,Gate")
                  || pathEndWith(path, "ModuleData,ControlChangeMacroList,CCM,Gate,Entry")
                  || pathEndWith(path, "ModuleData,ControlChangeMacroList,CCM,Data")
                  || pathEndWith(path, "ModuleData,ControlChangeMacroList,CCM,Value")
                  || pathEndWith(path, "ModuleData,ControlChangeMacroList,CCM,Value,Entry")
                    ) {
                continue;
            }
            else if (pathEndWith(path, "ModuleData,TemplateList,Template")
                  || pathEndWith(path, "ModuleData,TemplateList,Template,PC")
                  || pathEndWith(path, "ModuleData,TemplateList,Template,CC")
                  || pathEndWith(path, "ModuleData,TemplateList,Template,Comment")
                  || pathEndWith(path, "ModuleData,TemplateList,Template,Memo")
                    ) {
                continue;
            }
            else if (pathEndWith(path, "ModuleData,DefaultData,Track")
                  || pathEndWith(path, "ModuleData,DefaultData,Mark")
                  || pathEndWith(path, "ModuleData,DefaultData,Track,CC")
                  || pathEndWith(path, "ModuleData,DefaultData,Track,TimeSignature")
                  || pathEndWith(path, "ModuleData,DefaultData,Track,Comment")
                  || pathEndWith(path, "ModuleData,DefaultData,Track,Mark")
                  || pathEndWith(path, "ModuleData,DefaultData,Track,EOT")
                  || pathEndWith(path, "ModuleData,DefaultData,Track,PC")
                  || pathEndWith(path, "ModuleData,DefaultData,Track,Tempo")
                  || pathEndWith(path, "ModuleData,DefaultData,Track,Template")
                    ) {
                continue;
            }
            
            if (nodeName.equalsIgnoreCase("RhythmTrackDefault")) {
                if (RhythmTrackDefault_Gate == null) {
                    RhythmTrackDefault_Gate = element.getAttributeValue("Gate");
                    continue;
                }
            }
            if (nodeName.equalsIgnoreCase("ExclusiveEventDefault")) {
                if (ExclusiveEventDefault_Data == null) {
                    ExclusiveEventDefault_Data = element.getAttributeValue("Data");
                    continue;
                }
            }
            if (nodeName.equalsIgnoreCase("ProgramChangeEventPropertyDlg")) {
                if (ProgramChangeEventPropertyDlg_AutoPreviewDelay == null) {
                    ProgramChangeEventPropertyDlg_AutoPreviewDelay = element.getAttributeValue("AutoPreviewDelay");
                    continue;
                }
            }
            if (nodeName.equalsIgnoreCase("ControlChangeEventDefault")) {
                if (ControlChangeEventDefault_ID == null) {
                    ControlChangeEventDefault_ID = element.getAttributeValue("ID");
                    continue;
                }
            }

            _debug.println("unknown <" + path + "/" + nodeName + ">");                
            if (text != null && text.length() > 0) {
                _debug.print("text = " +  text);
            }
            if (element.countAttributes() > 0) {
                _debug.print("{");
                for (int i = 0; i < element.countAttributes(); ++ i) {
                    String name = element.getAttributeName(i);
                    String value = element.getAttributeValue(i);
                    if (i > 0) {
                        _debug.print(",");
                    }
                    _debug.print(name + "=" + value);
                }
                _debug.print("}");
            }
            _debug.println();
        }

        if (_DEBUG) {            
            for(String pos : folderAfter) {
                _debug.println("Folders@ " + pos);
            }
        }

        _module = parseInstrument(instElement, false);
        _drumSet = parseInstrument(drumElement, true);
        _ccList = praseControlChangeList(ccElement);
    }

    MXDOMElement _part2;
    
    public static void main(String[] args) throws Exception {
        MXDebugPrint.globalSwitchOn();
        DTextXML trial1 = DTextXML.fromFile(new File("C:/Domino144/Module/GMLevel1.xml"));
        DTextXML trial2 = DTextXML.fromFile(new File("C:/Domino144/Module/ok_GSm.xml"));
        DTextXML trial3 = DTextXML.fromFile(new File("C:/Domino144/Module/SC-88Pro.xml"));
        DTextXML trial4 = DTextXML.fromFile(new File("C:/Domino144/Module/SC-8850.xml"));
        DTextXML trial5 = DTextXML.fromFile(new File("C:/Domino144/Module/ok_XGb.xml"));
        //DTextXML trial6 = DTextXML.fromFile(new File("C:/Domino144/Module/ok_XG2k.xml"));
        DTextXML trial7 = DTextXML.fromFile(new File("C:/Domino144/Module/X50_v010.xml"));
        
        DTextPicker picker = new DTextPicker();
        
        picker.add(trial1);
        picker.add(trial2);
        picker.add(trial3);
        picker.add(trial4);
        picker.add(trial5);
        //picker.add(trial6);
        picker.add(trial7);
        
        MXUtil.showAsDialog(null, picker, "CC Picker");

        ProgramPicker browser = new ProgramPicker();
        browser.add(trial1);
        browser.add(trial2);
        browser.add(trial3);
        browser.add(trial4);
        browser.add(trial5);
        //browser.add(trial6);
        browser.add(trial7);

        MXUtil.showAsDialog(null, browser, "Program Browser");
        
        System.exit(0);
    }
    
    public static DTextMessageList praseControlChangeList(MXDOMElement elem) {
        DTextMessageList list = new DTextMessageList();
        if (elem.getNodeName().equalsIgnoreCase("ControlChangeMacroList")) {
            parseChildren(list, elem.getChildElements());
        }
        return list;
    }
    
    public static PDModule parseInstrument(ArrayList<MXDOMElement> listMaps, boolean isDrumKit) {
        PDModule database = new PDModule();
        
        for (MXDOMElement e1 : listMaps) {
            String mapName = e1.getAttributeValue("name");
            if (mapName == null) {
                //Sometimes Unnamed Happes
                continue;
            }
            PDMap map = database.smartReserve(mapName);
            for (MXDOMElement e2 : e1.getChildElements("PC")) {
                String name = e2.getAttributeValue("name");
                String pc = e2.getAttributeValue("PC");
                int number = MXUtil.numberFromText(pc);
                
                PDProgram prog = map.smartReserve(number, name);

                for (MXDOMElement e3 : e2.getChildElements("Bank")) {
                    String bankName = e3.getAttributeValue("Name");
                    String msb = e3.getAttributeValue("MSB");
                    String lsb = e3.getAttributeValue("LSB");

                    int msbNumber = MXUtil.numberFromText(msb);
                    int lsbNumber = MXUtil.numberFromText(lsb);
                    PDBank  bank = prog.smartReserve(msbNumber, lsbNumber, bankName);

                    if (isDrumKit) {
                        for (MXDOMElement e4 : e3.getChildElements("Tone")) {
                            String toneName = e4.getAttributeValue("Name");
                            String toneKey = e4.getAttributeValue("Key");
                            int key = MXUtil.numberFromText(toneKey);
                            if (key < 0) {
                                continue;
                            }

                            PDDrumSet dtone = bank.smartReserve(new PDDrumSet());
                            if (dtone.getNote(key) == null) {
                                dtone.addNote(key, toneName);
                            }else {
                                _debug.println("drumを多重セットできません");
                            }
                        }
                    }
                }
            }
        }
        return database;
    }

    public static DTextXML fromFile(File file) throws SAXException {
        MXDOMElement e = MXDOMElement.fromFile(file);
        if (e == null) {
            return null;
        }
        DTextXML t = new DTextXML(e);
        t._file = file;
        return t;
    }

    public boolean pathEndWith(List<String> happen, String hopeText) {
        String[] hope = hopeText.split(",");
        
        int happenSize = happen.size();
        int hopeSize = hope.length;
        
        if (happenSize < hopeSize) {
            return false;
        }
        
        int offset = happenSize - hopeSize;
        
        for (int i = 0; i < happen.size() && i < hope.length; ++ i) {
            if (happen.get(i + offset).equalsIgnoreCase(hope[i])) {
                //ok
            }else {
                return false;
            }
        }
        return true;
    }

    public boolean pathStartWith(List<String> happen, String hopeText) {
        String[] hope = hopeText.split(",");
        
        int happenSize = happen.size();
        int hopeSize = hope.length;
        
        if (happenSize < hopeSize) {
            return false;
        }
        
        for (int i = 0; i < happen.size() && i < hope.length; ++ i) {
            if (happen.get(i).equalsIgnoreCase(hope[i])) {
                //ok
            }else {
                return false;
            }
        }
        return true;
    }

    public static  void parseChildren(DTextMessageList list, List<MXDOMElement> children) {
        for (MXDOMElement e1 : children) {
            String nodeName = e1.getNodeName();
            if (nodeName.equalsIgnoreCase("Folder")) {
                String name = e1.getAttributeValue("Name");
                String id = e1.getAttributeValue("ID");

                DTextFolder folder = new DTextFolder(name);
                list.addFolder(folder);
                folder._id = id;
                parseChildren(folder._list, e1.getChildElements());
                continue;
            }
            if (nodeName.equalsIgnoreCase("CCM")) {
                String txtId = e1.getAttributeValue("ID");
                int id = MXUtil.numberFromText(txtId);
               
                String name = e1.getAttributeValue("Name");
                
                String txtColor = e1.getAttributeValue("Color"); //Format = #000000
                Color color = Color.black;
                
                String txtSync  = e1.getAttributeValue("Sync"); // 0"", 1"Last" 2"LastEachGate"
                int sync = 0;
                if (txtSync != null) {
                    if (txtSync.equalsIgnoreCase("Last")) {
                        sync = 1;
                    }else if (txtSync.equalsIgnoreCase("LastEachGate")) {
                        sync = 2;
                    }
                }

                String txtMuteSync  = e1.getAttributeValue("MuteSync"); // 0 or 1
                boolean muteSync = false;
                if (txtMuteSync != null && MXUtil.numberFromText(txtMuteSync) > 0) {
                    muteSync = true;
                }
                
                if (txtColor != null) {                    
                    if (txtColor.startsWith("#") && txtColor.length() == 7) {
                        String txt = txtColor.substring(1);
                        String r = "0x" + txt.substring(0, 2);
                        String g = "0x" + txt.substring(2, 4);
                        String b = "0x" + txt.substring(4, 6);
                        color = new Color(MXUtil.numberFromText(r), MXUtil.numberFromText(g), MXUtil.numberFromText(b));
                    }
                }
                if (MXUtil.numberFromText(txtMuteSync) > 0) {
                    muteSync = true;
                }
                
                
                DTextMessage cc = new DTextMessage(id, name);
                cc._color = color;
                cc._sync = sync;
                cc._muteSync = muteSync;
                               
                List<MXDOMElement> listValue = e1.getChildElements("Value");
                
                if (listValue != null) {
                    for (MXDOMElement e2 : listValue) {
                        cc.valueDefault = e2.getAttributeValue("Default");
                        cc.valueMin = e2.getAttributeValue("Min");
                        cc.valueMax = e2.getAttributeValue("Max");
                        cc.valueOffset = e2.getAttributeValue("Offset");
                        cc.valueName = e2.getAttributeValue("Name");
                        cc.valueType = e2.getAttributeValue("Type");
                        cc.valueTableID = e2.getAttributeValue("TableID");
                        cc.valueEntry = e2.getChildElements("Entry");
                    }
                }
                
                List<MXDOMElement> listGate = e1.getChildElements("Gate");
                if (listGate != null) {
                    for (MXDOMElement e2 : listGate) {
                        cc.gateDefault = e2.getAttributeValue("Default");
                        cc.gateMin = e2.getAttributeValue("Min");
                        cc.gateMax = e2.getAttributeValue("Max");
                        cc.gateOffset = e2.getAttributeValue("Offset");
                        cc.gateName = e2.getAttributeValue("Name");
                        cc.gateType = e2.getAttributeValue("Type");
                        cc.gateTableID = e2.getAttributeValue("TableID");
                        cc.gateEntry = e2.getChildElements("Entry");
                    }
                }

                List<MXDOMElement> listMemo = e1.getChildElements("Memo");
                if (listMemo != null) {
                    cc._memoText = "";
                    for (MXDOMElement e2 : listMemo) {
                        String text = e2.getText();
                        if (cc._memoText.length() > 0) {
                            cc._memoText += "\n";
                        }
                        cc._memoText += text;
                    }
                }

                List<MXDOMElement> listData = e1.getChildElements("Data");
                if (listData != null) {
                    for (MXDOMElement e2 : listData) {
                        cc._dataText = e2.getText();
                    }
                }
                
                list.addMessage(cc);
            }
        }        
    }
}
