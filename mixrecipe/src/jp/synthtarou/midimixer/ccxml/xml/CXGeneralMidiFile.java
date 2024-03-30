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
import jp.synthtarou.midimixer.ccxml.rules.CCRuleForTag;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.xml.transform.TransformerException;
import jp.synthtarou.midimixer.ccxml.InformationForModule;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.namedobject.MXNamedObject;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.libs.MXLineReader;
import jp.synthtarou.libs.MXFileLogger;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.libs.namedobject.MXNamedObjectListFactory;

/**
 *
 * @author Syntarou YOSHIDA
 */

public class CXGeneralMidiFile extends CXFile {

    static CXGeneralMidiFile _instance = new CXGeneralMidiFile();

    public static CXGeneralMidiFile getInstance() {
        return _instance;
    }

    CXGeneralMidiFile() {
        super("GMLevel1.xml");

        if (listModules() != null) {
            if (listModules().size() > 0) {
                return;
            }
        }

        _loadError = null;
        _document._listChildTags.clear();
        _document._listAttributes.clear();

        CXNode moduleData = _document.newTag("ModuleData", true);

        moduleData._listAttributes.addNameAndValue("Name", "GMLevel1");
        moduleData._listAttributes.addNameAndValue("Folder", "Preinstall");
        moduleData._listAttributes.addNameAndValue("Priority", "1");
        moduleData._listAttributes.addNameAndValue("FileCreator", "MIXRecipe");
        moduleData._listAttributes.addNameAndValue("FileVersion", "1.01");
        moduleData._listAttributes.addNameAndValue("WebSite", "https://github.com/syntaro/javamidimixer");

        CXNode instrumentList = moduleData.newTag(CCRuleManager.getInstance().getInstrumentListTag().getName(), true);

        try {
            InputStream stream = CXGeneralMidiFile.class.getResourceAsStream("GMLevel1.csv");
            readCSVProgram(instrumentList, stream, "Shift_JIS");
            stream.close();
        } catch (IOException ex) {
            MXFileLogger.getLogger(CXGeneralMidiFile.class).log(Level.WARNING, ex.getMessage(), ex);
        }

        CXNode drumSetList = moduleData.newTag(CCRuleManager.getInstance().getDrumSetListTag().getName(), true);

        try {
            InputStream stream = CXGeneralMidiFile.class.getResourceAsStream("GMLevel1Drum.csv");
            readCSVDrum(drumSetList, stream, "Shift_JIS");
            stream.close();
        } catch (IOException ex) {
            MXFileLogger.getLogger(CXGeneralMidiFile.class).log(Level.WARNING, ex.getMessage(), ex);
        }

        CXNode controlChangeMacroList = moduleData.newTag(CCRuleManager.getInstance().getControlChangeMacroListTag().getName(), true);
        createControlChange(controlChangeMacroList);

        CCRuleManager rule = CCRuleManager.getInstance();
        for (CXNode seek : _document._listChildTags) {
            if (seek._nodeName.equals("ModuleData")) {
                InformationForModule module = new InformationForModule(this,seek);
                module.fillCCMLink();
                _listModules.add(module);
                checkWaring(module, seek, rule.getModuleDataTag());
            }
        }

        try {
            writeToTeporary();
            moveTemporaryToThis();
        } catch (TransformerException ex) {
            MXFileLogger.getLogger(CXGeneralMidiFile.class).log(Level.WARNING, ex.getMessage(), ex);
        } catch (IOException ex) {
            MXFileLogger.getLogger(CXGeneralMidiFile.class).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    public void readCSVProgram(CXNode instrumentList, InputStream stream, String charset) {
        MXLineReader reader = new MXLineReader(stream, charset);
        String line;
        String folder = "";
        int folderId = 0;
        ArrayList<String> cells = new ArrayList();

        CXNode mapTag = instrumentList.newTag("Map", true);
        mapTag._listAttributes.addNameAndValue("Name", "GM Level1");

        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    folder = line.substring(1);
                    folderId++;
                    continue;
                }

                MXUtil.split(line, cells, ',');

                if (cells.size() >= 4) {
                    try {
                        String dispCode = cells.get(0);
                        String numberHex = cells.get(1);
                        String nameEnglish = cells.get(2);
                        String nameLocalized = cells.get(3);

                        CXNode pcTag = mapTag.newTag("PC", true);
                        pcTag._listAttributes.addNameAndValue("Name", nameEnglish);
                        pcTag._listAttributes.addNameAndValue("PC", dispCode);

                        CXNode bankTag = pcTag.newTag("Bank", true);
                        bankTag._listAttributes.addNameAndValue("Name", nameEnglish);
                        bankTag._listAttributes.addNameAndValue("MSB", "0");
                        bankTag._listAttributes.addNameAndValue("LSB", "0");
                    } catch (RuntimeException ex) {
                        MXFileLogger.getLogger(CXGeneralMidiFile.class).log(Level.WARNING, ex.getMessage(), ex);
                    }
                }
            }
        } catch (IOException ex) {
            MXFileLogger.getLogger(CXGeneralMidiFile.class).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    public void readCSVDrum(CXNode drumSetList, InputStream stream, String charset) {
        MXLineReader reader = new MXLineReader(stream, charset);
        String line;
        String folder = "";
        ArrayList<String> cells = new ArrayList();

        CXNode mapTag = drumSetList.newTag("Map", true);
        mapTag._listAttributes.addNameAndValue("Name", "GM Drum");

        CXNode pcTag = mapTag.newTag("PC", true);
        pcTag._listAttributes.addNameAndValue("Name", "Standard");
        pcTag._listAttributes.addNameAndValue("PC", "1");

        CXNode bankTag = pcTag.newTag("Bank", true);
        bankTag._listAttributes.addNameAndValue("Name", "Standard");
        bankTag._listAttributes.addNameAndValue("MSB", "0");
        bankTag._listAttributes.addNameAndValue("LSB", "0");

        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    folder = line.substring(1);
                    continue;
                }

                MXUtil.split(line, cells, ',');

                if (cells.size() >= 4) {
                    String noteNumber = cells.get(0);
                    String noteKey1 = cells.get(1);
                    String nameEnglish = cells.get(2);
                    String noteKey2 = cells.get(3);

                    CXNode toneTag = bankTag.newTag("Tone", true);
                    toneTag._listAttributes.addNameAndValue("Name", nameEnglish);
                    toneTag._listAttributes.addNameAndValue("Key", noteNumber);
                }
            }
        } catch (IOException ex) {
            MXFileLogger.getLogger(CXGeneralMidiFile.class).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    public void createControlChange(CXNode controlChangeMacroList) {
        CXNode folderTag0 = controlChangeMacroList.newTag("Folder", true);
        folderTag0._listAttributes.addNameAndValue("Name", "Note");
        for (MXNamedObject<Integer> seek : MXNamedObjectListFactory.listupCommand(false)) {
            int command = seek._value;
            if (command != MXMidi.COMMAND_CH_NOTEON && command != MXMidi.COMMAND_CH_NOTEOFF) {
                continue;
            }
            if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                continue;
            }
            String name = seek._name;
            MXMessage message = MXMessageFactory.fromShortMessage(0, command, 0, 0);

            CXNode ccmTag = folderTag0.newTag("CCM", true);
            ccmTag._listAttributes.addNameAndValue("ID", String.valueOf(1000 + command));
            ccmTag._listAttributes.addNameAndValue("Name", name);
            ccmTag._listAttributes.addNameAndValue("Color", "");
            ccmTag._listAttributes.addNameAndValue("Sync", "Last");

            CXNode valueTag = ccmTag.newTag("Value", true);
            valueTag.setTextContent("");
            CXNode gateTag = ccmTag.newTag("Gate", true);
            gateTag.setTextContent("" + message.getGate()._value);
            switch (message.getStatus() & 0xf0) {
                case MXMidi.COMMAND_CH_POLYPRESSURE:
                case MXMidi.COMMAND_CH_NOTEON:
                case MXMidi.COMMAND_CH_NOTEOFF:
                    gateTag._listAttributes.addNameAndValue("Type", "Key");
                    break;
            }
            CXNode dataTag = ccmTag.newTag("Data", true);
            dataTag.setTextContent(message.getTemplateAsText());
            CXNode memoTag = ccmTag.newTag("Memo", true);
            memoTag.setTextContent("");
        }

        CXNode folderTag1 = controlChangeMacroList.newTag("Folder", true);
        folderTag1._listAttributes.addNameAndValue("Name", "Command");

        for (MXNamedObject<Integer> seek : MXNamedObjectListFactory.listupCommand(false)) {
            int command = seek._value;
            if (command == MXMidi.COMMAND_CH_NOTEON || command == MXMidi.COMMAND_CH_NOTEOFF) {
                continue;
            }
            if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                continue;
            }
            String name = seek._name;
            MXMessage message = MXMessageFactory.fromShortMessage(0, command, 0, 0);

            CXNode ccmTag = folderTag1.newTag("CCM", true);
            ccmTag._listAttributes.addNameAndValue("ID", String.valueOf(1000 + command));
            ccmTag._listAttributes.addNameAndValue("Name", name);
            ccmTag._listAttributes.addNameAndValue("Color", "");
            ccmTag._listAttributes.addNameAndValue("Sync", "Last");

            CXNode valueTag = ccmTag.newTag("Value", true);
            valueTag.setTextContent("");
            CXNode gateTag = ccmTag.newTag("Gate", true);
            gateTag.setTextContent("" + message.getGate()._value);
            switch(message.getStatus() & 0xf0) {
                case MXMidi.COMMAND_CH_POLYPRESSURE:
                case MXMidi.COMMAND_CH_NOTEON:
                case MXMidi.COMMAND_CH_NOTEOFF:
                    gateTag._listAttributes.addNameAndValue("Type", "Key");
                    break;
            }
            CXNode dataTag = ccmTag.newTag("Data", true);
            dataTag.setTextContent(message.getTemplateAsText());
            CXNode memoTag = ccmTag.newTag("Memo", true);
            memoTag.setTextContent("");
        }

        CXNode folderTag2 = controlChangeMacroList.newTag("Folder", true);
        folderTag2._listAttributes.addNameAndValue("Name", "ControlChange");

        for (int i = 0; i < 128; ++i) {
            String name = MXMidi.nameOfControlChange(i);
            CXNode ccmTag = folderTag2.newTag("CCM", true);
            ccmTag._listAttributes.addNameAndValue("ID", String.valueOf(2000 + i));
            ccmTag._listAttributes.addNameAndValue("Name", name);
            ccmTag._listAttributes.addNameAndValue("Color", "");
            ccmTag._listAttributes.addNameAndValue("Sync", "Last");

            CXNode valueTag = ccmTag.newTag("Value", true);
            valueTag.setTextContent("");
            CXNode dataTag = ccmTag.newTag("Data", true);
            dataTag.setTextContent("@CC " + MXUtil.toHexFF(i) + "h #VL");
            CXNode memoTag = ccmTag.newTag("Memo", true);
            memoTag.setTextContent("");
        }

        /* https://www.dtmstation.com/wp-content/uploads/2022/05/AMS_MIDI_Impre.pdf ? */
        MXNamedObjectList<String> listDataentry = new MXNamedObjectList<>();
        listDataentry.addNameAndValue("Vibrate Rate", "@NRPN 1 1 #VL 0");
        listDataentry.addNameAndValue("Vibrate Depth", "@NRPN 1 9 #VL 0");
        listDataentry.addNameAndValue("Vibrate Delay", "@NRPN 1 10 #VL 0");
        listDataentry.addNameAndValue("Filter Cutoff", "@NRPN 1 32 #VL 0");
        listDataentry.addNameAndValue("Filter Resonance", "@NRPN 1 33 #VL 0");
        listDataentry.addNameAndValue("Attack Time", "@NRPN 1 99 #VL 0");
        listDataentry.addNameAndValue("Decay Time", "@NRPN 1 100 #VL 0");
        listDataentry.addNameAndValue("Release Time", "@NRPN 1 102 #VL 0");

        listDataentry.addNameAndValue("DrumInst. Pitch", "@NRPN 24 #GL #VL 0");
        listDataentry.addNameAndValue("DrumInst. Level", "@NRPN 26 #GL #VL 0");
        listDataentry.addNameAndValue("DrumInst. Panpot", "@NRPN 28 #GL #VL 0");
        listDataentry.addNameAndValue("DrumInst. Reverve", "@NRPN 29 #GL #VL 0");
        listDataentry.addNameAndValue("DrumInst. Chorus", "@NRPN 30 #GL #VL 0");
        listDataentry.addNameAndValue("DrumInst. Delay", "@NRPN 31 #GL #VL 0");

        int offsetComment = listDataentry.getSize();
        listDataentry.addNameAndValue("PitchBend Range", "@RPN 0 0 #VL 0"); /* 0~24 */
        listDataentry.addNameAndValue("Fine Tuning", "@RPN 0 #VH #VL"); /* 0000 - 7f7f = -100 ~ 99.99 cent */
        listDataentry.addNameAndValue("Course Tuning", "@RPN 1 2 #VL 0");   /* 40~88 */
        listDataentry.addNameAndValue("Moduleration Depth Range", "@RPN 1 5 #VH #VL"); /* 0~4, 0~127 */
        
        CXNode folderTag3 = controlChangeMacroList.newTag("Folder", true);
        folderTag3._listAttributes.addNameAndValue("Name", "Dataentry");
        for (int i = 0; i < listDataentry.size(); ++ i) {
            String name = listDataentry.nameOfIndex(i);
            String text = listDataentry.valueOfIndex(i);

            CXNode ccmTag = folderTag3.newTag("CCM", true);
            ccmTag._listAttributes.addNameAndValue("ID", String.valueOf(2000 + i));
            ccmTag._listAttributes.addNameAndValue("Name",  name);
            ccmTag._listAttributes.addNameAndValue("Color", "");
            ccmTag._listAttributes.addNameAndValue("Sync", "Last");

            CXNode valueTag = ccmTag.newTag("Value", true);
            valueTag.setTextContent("");
            CXNode dataTag = ccmTag.newTag("Data", true);
            dataTag.setTextContent(text);
            CXNode memoTag = ccmTag.newTag("Memo", true);
            memoTag.setTextContent("");
            
            switch(i - offsetComment) {
                case 0:
                    valueTag._listAttributes.addNameAndValue("Min", "0");
                    valueTag._listAttributes.addNameAndValue("Max", "24");
                    break;
                case 1:
                    //memoTag.setTextContent("-100 ~ 99.99 cent");
                    break;
                case 2:
                    valueTag._listAttributes.addNameAndValue("Min", "48");
                    valueTag._listAttributes.addNameAndValue("Max", "88");
                    break;
                case 3:
                    CXNode gateTag = ccmTag.newTag("Gate", true);
                    gateTag._listAttributes.addNameAndValue("Min", "0");
                    gateTag._listAttributes.addNameAndValue("Max", "4");
                    break;
                default:
                    break;
            }
        }
    }
    
    public List<CXNode> seekEveryonesChildren(List<CXNode> parents, CCRuleForTag tag) {
        if (parents == null) {
            return null;
        }
        ArrayList<CXNode> result = new ArrayList<>();
        for (CXNode node : parents) {
            List<CXNode> list = node.listChildren(tag);
            if (list != null) {
                result.addAll(list);
            }
        }
        
        if (result.isEmpty()) {
            return null;
        }
        return result;
    }

    public List<CXNode> seekEveryonesChildren(List<CXNode> parents, String tag) {
        if (parents == null) {
            return null;
        }
        ArrayList<CXNode> result = new ArrayList<>();
        for (CXNode node : parents) {
            List<CXNode> list = node.listChildren(tag);
            if (list != null) {
                result.addAll(list);
            }
        }
        
        if (result.isEmpty()) {
            return null;
        }
        return result;
    }

    public String simpleFindProgramName(int seekPC) {
        CXNode node = simpleFindProgram(seekPC);
        String name = null;
        if (node != null) {
            name = node._listAttributes.valueOfName("Name");
        }
        return (name == null) ? "-" : name;
    }

    public CXNode simpleFindProgram(int seekPC) {
        seekPC ++; // 0~>1
        
        CCRuleManager rule = CCRuleManager.getInstance();
        List<CXNode> listModuleData = _document.listChildren(rule.moduleData);
        List<CXNode> listInstrument = seekEveryonesChildren(listModuleData, rule.instrumentList);
        List<CXNode> listMap = seekEveryonesChildren(listInstrument, rule.instrumentList_map);
        List<CXNode> listPC = seekEveryonesChildren(listMap, rule.instrumentList_pc);

        if (listPC == null) {
            return null;
        }

        for (CXNode program : listPC) {
            String attrName = program._listAttributes.valueOfName("Name");
            String attrPC = program._listAttributes.valueOfName("PC");
            int x = MXUtil.numberFromText(attrPC, -1);
            if (x == seekPC) {
                return program;
            }
        }
        return null;
    }

    public String simpleFindDrumName(int seekPC) {
        CXNode node = simpleFindDrum(seekPC);
        String name = null;
        if (node != null) {
            name = node._listAttributes.valueOfName("Name");
        }
        return (name == null) ? "-" : name;
    }
    
    public CXNode simpleFindDrum(int key) {
        _document.getChangeStamp(); //TODO
        CCRuleManager rule = CCRuleManager.getInstance();
        List<CXNode> listModuileData = _document.listChildren(rule.moduleData);
        List<CXNode> listDrumSet = CXGeneralMidiFile.this.seekEveryonesChildren(listModuileData, "DrumSetList");
        List<CXNode> listMap = CXGeneralMidiFile.this.seekEveryonesChildren(listDrumSet, "Map");
        List<CXNode> listPC = CXGeneralMidiFile.this.seekEveryonesChildren(listMap, "PC");
        List<CXNode> listBank = CXGeneralMidiFile.this.seekEveryonesChildren(listPC, "Bank");
        List<CXNode> listTone = CXGeneralMidiFile.this.seekEveryonesChildren(listBank, "Tone");

        if (listTone == null) {
            return null;
        }

        for (CXNode tone : listTone) {
            String attrName = tone._listAttributes.valueOfName("Name");
            String attrKey = tone._listAttributes.valueOfName("Key");
            int x = MXUtil.numberFromText(attrKey, -1);
            if (x == key) {
                return tone;
            }
        }
        return null;
    }
}
