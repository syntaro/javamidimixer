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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.text.MXLineReader;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class GMFile {

    static GMFile _instance = new GMFile();

    public static GMFile getInstance() {
        return _instance;
    }

    CCXMLFile _file;

    GMFile() {
        _file = new CCXMLFile("GMLevel1.xml");

        if (_file.listModules() != null) {
            if (_file.listModules().size() > 0) {
                return;
            }
        }

        _file._document._listChildTags.clear();
        _file._document._listAttributes.clear();

        CCXMLNode moduleData = _file._document.newTag("ModuleData", true);

        moduleData._listAttributes.addNameAndValue("Name", "GMLevel1");
        moduleData._listAttributes.addNameAndValue("Folder", "Preinstall");
        moduleData._listAttributes.addNameAndValue("Priority", "1");
        moduleData._listAttributes.addNameAndValue("FileCreator", "MIXRecipe");
        moduleData._listAttributes.addNameAndValue("FileVersion", "1.01");
        moduleData._listAttributes.addNameAndValue("WebSite", "https://github.com/syntaro/javamidimixer");

        CCXMLNode instrumentList = moduleData.newTag(CCRuleManager.getInstance().getInstrumentListTag().getName(), true);

        try {
            InputStream stream = GMFile.class.getResourceAsStream("GMLevel1.csv");
            readCSVProgram(instrumentList, stream, "Shift_JIS");
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        CCXMLNode drumSetList = moduleData.newTag(CCRuleManager.getInstance().getDrumSetListTag().getName(), true);

        try {
            InputStream stream = GMFile.class.getResourceAsStream("GMLevel1Drum.csv");
            readCSVDrum(drumSetList, stream, "Shift_JIS");
            stream.close();
        } catch (Exception e) {
        }

        CCXMLNode controlChangeMacroList = moduleData.newTag(CCRuleManager.getInstance().getControlChangeMacroListTag().getName(), true);
        createControlChange(controlChangeMacroList);

        try {
            _file.writeToTeporary();
            _file.moveTemporaryToThis();
        } catch (Exception e) {
            e.printStackTrace();;
        }

        CCXMLManager.getInstance()._listLoaded.add(_file);
    }

    public void readCSVProgram(CCXMLNode instrumentList, InputStream stream, String charset) {
        MXLineReader reader = new MXLineReader(stream, charset);
        String line;
        String folder = "";
        int folderId = 0;
        ArrayList<String> cells = new ArrayList();

        CCXMLNode mapTag = instrumentList.newTag("Map", true);
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

                        CCXMLNode pcTag = mapTag.newTag("PC", true);
                        pcTag._listAttributes.addNameAndValue("Name", nameEnglish);
                        pcTag._listAttributes.addNameAndValue("PC", dispCode);

                        CCXMLNode bankTag = pcTag.newTag("Bank", true);
                        bankTag._listAttributes.addNameAndValue("Name", nameEnglish);
                        bankTag._listAttributes.addNameAndValue("MSB", "0");
                        bankTag._listAttributes.addNameAndValue("LSB", "0");
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();;
        }
    }

    public void readCSVDrum(CCXMLNode drumSetList, InputStream stream, String charset) {
        MXLineReader reader = new MXLineReader(stream, charset);
        String line;
        String folder = "";
        ArrayList<String> cells = new ArrayList();

        CCXMLNode mapTag = drumSetList.newTag("Map", true);
        mapTag._listAttributes.addNameAndValue("Name", "GM Drum");

        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    folder = line.substring(1);
                    continue;
                }

                MXUtil.split(line, cells, ',');

                CCXMLNode pcTag = mapTag.newTag("PC", true);
                pcTag._listAttributes.addNameAndValue("Name", "Standard");
                pcTag._listAttributes.addNameAndValue("PC", "1");

                CCXMLNode bankTag = pcTag.newTag("Bank", true);
                bankTag._listAttributes.addNameAndValue("Name", "Standard");
                bankTag._listAttributes.addNameAndValue("MSB", "0");
                bankTag._listAttributes.addNameAndValue("LSB", "0");

                if (cells.size() >= 4) {
                    String noteNumber = cells.get(0);
                    String noteKey1 = cells.get(1);
                    String nameEnglish = cells.get(2);
                    String noteKey2 = cells.get(3);

                    CCXMLNode toneTag = bankTag.newTag("Tone", true);
                    toneTag._listAttributes.addNameAndValue("Name", nameEnglish);
                    toneTag._listAttributes.addNameAndValue("Key", noteNumber);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();;
        }
    }

    public void createControlChange(CCXMLNode controlChangeMacroList) {

        CCXMLNode folderTag1 = controlChangeMacroList.newTag("Folder", true);
        folderTag1._listAttributes.addNameAndValue("Name", "Command");

        for (MXWrap<Integer> wrap : MXMidi.listupCommand()) {
            int command = wrap.value;
            if (command == MXMidi.COMMAND_CONTROLCHANGE) {
                continue;
            }
            String name = MXMidi.nameOfChannelMessage(command);
            MXMessage message = MXMessageFactory.fromShortMessage(0, command, 0, 0);

            CCXMLNode ccmTag = folderTag1.newTag("CCM", true);
            ccmTag._listAttributes.addNameAndValue("ID", String.valueOf(1000 + command));
            ccmTag._listAttributes.addNameAndValue("Name", name);
            ccmTag._listAttributes.addNameAndValue("Color", "");
            ccmTag._listAttributes.addNameAndValue("Sync", "Last");

            CCXMLNode valueTag = ccmTag.newTag("Value", true);
            valueTag.setTextContent("");
            CCXMLNode gateTag = ccmTag.newTag("Gate", true);
            gateTag.setTextContent("" + message.getGate()._var);
            CCXMLNode dataTag = ccmTag.newTag("Data", true);
            dataTag.setTextContent(message.toTemplateText());
            CCXMLNode memoTag = ccmTag.newTag("Memo", true);
            memoTag.setTextContent("");
        }

        CCXMLNode folderTag2 = controlChangeMacroList.newTag("Folder", true);
        folderTag2._listAttributes.addNameAndValue("Name", "ControlChange");

        for (int i = 0; i < 128; ++i) {
            String name = MXMidi.nameOfControlChange(i);
            CCXMLNode ccmTag = folderTag2.newTag("CCM", true);
            ccmTag._listAttributes.addNameAndValue("ID", String.valueOf(2000 + i));
            ccmTag._listAttributes.addNameAndValue("Name", name);
            ccmTag._listAttributes.addNameAndValue("Color", "");
            ccmTag._listAttributes.addNameAndValue("Sync", "Last");

            CCXMLNode valueTag = ccmTag.newTag("Value", true);
            valueTag.setTextContent("");
            CCXMLNode dataTag = ccmTag.newTag("Data", true);
            dataTag.setTextContent("@CC " + MXUtil.toHexFF(i) + "h #VL");
            CCXMLNode memoTag = ccmTag.newTag("Memo", true);
            memoTag.setTextContent("");
        }
    }

    public String simpleFindProgram(int seekPC) {
        String seekPCText = Integer.toString(seekPC);

        try {
            List<CCXMLNode> listModuileData = _file._document.listChildren(CCRuleManager._instance.moduleData);
            for (CCXMLNode module : listModuileData) {
                List<CCXMLNode> listInstrument = module.listChildren(CCRuleManager._instance.instrumentList);
                for (CCXMLNode instrument : listInstrument) {
                    List<CCXMLNode> listMap = instrument.listChildren(CCRuleManager._instance.instrumentList_map);
                    for (CCXMLNode map : listMap) {
                        List<CCXMLNode> listPC = map.listChildren(CCRuleManager._instance.instrumentList_pc);
                        for (CCXMLNode pc : listPC) {
                            String attrName = pc._listAttributes.valueOfName("Name");
                            String attrPC = pc._listAttributes.valueOfName("PC");
                            if (attrPC.equals(seekPCText)) {
                                return attrName;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-";
    }

    public String simpleFindDrum(int seekKey) {
        try {
            List<CCXMLNode> listModuileData = _file._document.listChildren(CCRuleManager._instance.moduleData);
            for (CCXMLNode module : listModuileData) {
                List<CCXMLNode> listDrumSet = module.listChildren("DrumSetList");
                for (CCXMLNode drum : listDrumSet) {
                    List<CCXMLNode> listMap = drum.listChildren("Map");
                    String seekKeyText = Integer.toString(seekKey);

                    for (CCXMLNode map : listMap) {
                        List<CCXMLNode> listPC = map.listChildren("PC");
                        for (CCXMLNode pc : listPC) {
                            List<CCXMLNode> listBank = pc.listChildren("Bank");
                            for (CCXMLNode bank : listBank) {
                                List<CCXMLNode> listTone = bank.listChildren("Tone");
                                for (CCXMLNode tone : listTone) {
                                    String attrName = tone._listAttributes.valueOfName("Name");
                                    String attrKey = tone._listAttributes.valueOfName("Key");
                                    if (attrKey.equals(seekKeyText)) {
                                        return attrName;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-";
    }
}
