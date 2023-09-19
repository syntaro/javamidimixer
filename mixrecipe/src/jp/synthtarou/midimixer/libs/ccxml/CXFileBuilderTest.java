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
package jp.synthtarou.midimixer.libs.ccxml;

import java.util.List;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CXFileBuilderTest extends CXFile {

    public CXFileBuilderTest() {
        super("GM Level1");
    }

    public CXNode createOrFindModuleData(String name) {
        for (CXNode tag : listModules()) {
            String nameAttr = tag._listAttributes.valueOfName("name");

            if (name.equalsIgnoreCase(nameAttr)) {
                return tag;
            }
        }

        CCRuleForTag ruleModuleData = CCRuleManager.getInstance().moduleData;
        CXNode moduleData = new CXNode(_document, "ModuleData", ruleModuleData);
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

    public List<CXNode> listInstrumentList(CXNode moduleData) {
        return moduleData.listChildren(ruleManager.instrumentList);
    }

    public List<CXNode> listInstrumentList(CXNode moduleData, int instrument) {
        List<CXNode> listInstrument = moduleData.listChildren(ruleManager.instrumentList);
        return listInstrument;
    }

    public List<CXNode> listInstrumentList_Map(CXNode moduleData, int instrument, int map) {
        List<CXNode> listInstrument = moduleData.listChildren(ruleManager.instrumentList);
        List<CXNode> listMap = listInstrument.get(instrument).listChildren(ruleManager.instrumentList_map);
        return listMap;
    }

    public List<CXNode> listInstrumentList_Map_PC(CXNode moduleData, int instrument, int map, int pc) {
        List<CXNode> listInstrument = moduleData.listChildren(ruleManager.instrumentList);
        List<CXNode> listMap = listInstrument.get(instrument).listChildren(ruleManager.instrumentList_map);
        List<CXNode> listPC = listMap.get(map).listChildren(ruleManager.instrumentList_pc);
        return listPC;
    }

    public List<CXNode> listInstrumentList_Map_PC_Bank(CXNode moduleData, int instrument, int map, int pc, int bank) {
        List<CXNode> listInstrument = moduleData.listChildren(ruleManager.instrumentList);
        List<CXNode> listMap = listInstrument.get(instrument).listChildren(ruleManager.instrumentList_map);
        List<CXNode> listPC = listMap.get(map).listChildren(ruleManager.instrumentList_pc);
        List<CXNode> listBank = listPC.get(bank).listChildren(ruleManager.instrumentList_bank);
        return listBank;
    }

    public List<CXNode> listDrumSetList(CXNode moduleData, int instrument) {
        List<CXNode> listDrumSet = moduleData.listChildren(ruleManager.drumSetList);
        return listDrumSet;
    }

    public List<CXNode> listDrumSetList_Map(CXNode moduleData, int instrument, int map) {
        List<CXNode> listDrumSet = moduleData.listChildren(ruleManager.drumSetList);
        List<CXNode> listMap = listDrumSet.get(instrument).listChildren(ruleManager.drumSetList_map);
        return listMap;
    }

    public List<CXNode> listDrumSetList_Map_PC(CXNode moduleData, int instrument, int map, int pc) {
        List<CXNode> listDrumSet = moduleData.listChildren(ruleManager.drumSetList);
        List<CXNode> listMap = listDrumSet.get(instrument).listChildren(ruleManager.drumSetList_map);
        List<CXNode> listPC = listMap.get(map).listChildren(ruleManager.drumSetList_pc);
        return listPC;
    }

    public List<CXNode> listDrumSetList_Map_PC_Bank(CXNode moduleData, int instrument, int map, int pc, int bank) {
        List<CXNode> listDrumSet = moduleData.listChildren(ruleManager.drumSetList);
        List<CXNode> listMap = listDrumSet.get(instrument).listChildren(ruleManager.drumSetList_map);
        List<CXNode> listPC = listMap.get(map).listChildren(ruleManager.drumSetList_pc);
        List<CXNode> listBank = listPC.get(bank).listChildren(ruleManager.drumSetList_bank);
        return listBank;
    }

    public List<CXNode> listDrumSetList_Map_PC_Bank_Tone(CXNode moduleData, int instrument, int map, int pc, int bank, int tone) {
        List<CXNode> listDrumSet = moduleData.listChildren(ruleManager.drumSetList);
        List<CXNode> listMap = listDrumSet.get(instrument).listChildren(ruleManager.drumSetList_map);
        List<CXNode> listPC = listDrumSet.get(map).listChildren(ruleManager.drumSetList_pc);
        List<CXNode> listBank = listPC.get(bank).listChildren(ruleManager.drumSetList_bank);
        List<CXNode> listTone = listPC.get(bank).listChildren(ruleManager.drumSetList_tone);
        return listTone;
    }
}
