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

import java.util.List;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCXMLFileBuilderTest extends CCXMLFile {

    public CCXMLFileBuilderTest() {
        super("GM Level1");
    }

    public CCXMLNode createOrFindModuleData(String name) {
        for (CCXMLNode tag : listModules()) {
            String nameAttr = tag._listAttributes.valueOfName("name");

            if (name.equalsIgnoreCase(nameAttr)) {
                return tag;
            }
        }

        CCRuleForTag ruleModuleData = CCRuleManager.getInstance().moduleData;
        CCXMLNode moduleData = new CCXMLNode(_document, "ModuleData", ruleModuleData);
        moduleData._listAttributes.setAttribute("Name", name);
        moduleData._listAttributes.setAttribute("Folder", "");
        moduleData._listAttributes.setAttribute("Priority", "1");
        moduleData._listAttributes.setAttribute("FileCreator", "SynthTAROU");
        moduleData._listAttributes.setAttribute("FileVersion", "0.1");
        moduleData._listAttributes.setAttribute("WebSite", "https://github.com/syntaro/javamidimixer");
        _document._listChildTags.add(moduleData);

        /*
         moduleData.getReadyForTag(CCRuleManager._instance.instrumentList.getName(), true);
         moduleData.getReadyForTag(CCRuleManager._instance.drumSetList.getName(), true);
         moduleData.getReadyForTag(CCRuleManager._instance.controlChangeMacroList.getName(), true);
         moduleData.getReadyForTag(CCRuleManager._instance.templateList.getName(), true);
         moduleData.getReadyForTag(CCRuleManager._instance.defaultData.getName(), true);
         */
        return moduleData;
    }

    public List<CCXMLNode> listInstrumentList(CCXMLNode moduleData) {
        return moduleData.listChildren(ruleManager.instrumentList);
    }

    public List<CCXMLNode> listInstrumentList(CCXMLNode moduleData, int instrument) {
        List<CCXMLNode> listInstrument = moduleData.listChildren(ruleManager.instrumentList);
        return listInstrument;
    }

    public List<CCXMLNode> listInstrumentList_Map(CCXMLNode moduleData, int instrument, int map) {
        List<CCXMLNode> listInstrument = moduleData.listChildren(ruleManager.instrumentList);
        List<CCXMLNode> listMap = listInstrument.get(instrument).listChildren(ruleManager.instrumentList_map);
        return listMap;
    }

    public List<CCXMLNode> listInstrumentList_Map_PC(CCXMLNode moduleData, int instrument, int map, int pc) {
        List<CCXMLNode> listInstrument = moduleData.listChildren(ruleManager.instrumentList);
        List<CCXMLNode> listMap = listInstrument.get(instrument).listChildren(ruleManager.instrumentList_map);
        List<CCXMLNode> listPC = listMap.get(map).listChildren(ruleManager.instrumentList_pc);
        return listPC;
    }

    public List<CCXMLNode> listInstrumentList_Map_PC_Bank(CCXMLNode moduleData, int instrument, int map, int pc, int bank) {
        List<CCXMLNode> listInstrument = moduleData.listChildren(ruleManager.instrumentList);
        List<CCXMLNode> listMap = listInstrument.get(instrument).listChildren(ruleManager.instrumentList_map);
        List<CCXMLNode> listPC = listMap.get(map).listChildren(ruleManager.instrumentList_pc);
        List<CCXMLNode> listBank = listPC.get(bank).listChildren(ruleManager.instrumentList_bank);
        return listBank;
    }

    public List<CCXMLNode> listDrumSetList(CCXMLNode moduleData, int instrument) {
        List<CCXMLNode> listDrumSet = moduleData.listChildren(ruleManager.drumSetList);
        return listDrumSet;
    }

    public List<CCXMLNode> listDrumSetList_Map(CCXMLNode moduleData, int instrument, int map) {
        List<CCXMLNode> listDrumSet = moduleData.listChildren(ruleManager.drumSetList);
        List<CCXMLNode> listMap = listDrumSet.get(instrument).listChildren(ruleManager.drumSetList_map);
        return listMap;
    }

    public List<CCXMLNode> listDrumSetList_Map_PC(CCXMLNode moduleData, int instrument, int map, int pc) {
        List<CCXMLNode> listDrumSet = moduleData.listChildren(ruleManager.drumSetList);
        List<CCXMLNode> listMap = listDrumSet.get(instrument).listChildren(ruleManager.drumSetList_map);
        List<CCXMLNode> listPC = listMap.get(map).listChildren(ruleManager.drumSetList_pc);
        return listPC;
    }

    public List<CCXMLNode> listDrumSetList_Map_PC_Bank(CCXMLNode moduleData, int instrument, int map, int pc, int bank) {
        List<CCXMLNode> listDrumSet = moduleData.listChildren(ruleManager.drumSetList);
        List<CCXMLNode> listMap = listDrumSet.get(instrument).listChildren(ruleManager.drumSetList_map);
        List<CCXMLNode> listPC = listMap.get(map).listChildren(ruleManager.drumSetList_pc);
        List<CCXMLNode> listBank = listPC.get(bank).listChildren(ruleManager.drumSetList_bank);
        return listBank;
    }

    public List<CCXMLNode> listDrumSetList_Map_PC_Bank_Tone(CCXMLNode moduleData, int instrument, int map, int pc, int bank, int tone) {
        List<CCXMLNode> listDrumSet = moduleData.listChildren(ruleManager.drumSetList);
        List<CCXMLNode> listMap = listDrumSet.get(instrument).listChildren(ruleManager.drumSetList_map);
        List<CCXMLNode> listPC = listDrumSet.get(map).listChildren(ruleManager.drumSetList_pc);
        List<CCXMLNode> listBank = listPC.get(bank).listChildren(ruleManager.drumSetList_bank);
        List<CCXMLNode> listTone = listPC.get(bank).listChildren(ruleManager.drumSetList_tone);
        return listTone;
    }
}
