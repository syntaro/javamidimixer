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
package jp.synthtarou.midimixer.ccxml;

import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCRuleManager {

    static CCRuleManager _instance = new CCRuleManager();
    
    public static CCRuleManager getInstance() {
        return _instance;
    }

    public CCRuleForTag getRootTag() { 
        return root;
    }
    
    public CCRuleForTag getModuleDataTag() { 
        return moduleData;
    }

    public CCRuleForTag getInstrumentListTag() {
        return instrumentList;
    }

    public CCRuleForTag getDrumSetListTag() { 
        return drumSetList;
    }

    public CCRuleForTag getControlChangeMacroListTag() { 
        return controlChangeMacroList;
    }

    public CCRuleForTag getTemplateListTag() { 
        return templateList;
    }

    public CCRuleForTag getDefaultDataTag() { 
        return defaultData;
    }

    public final CCRuleForTag root;

    public final CCRuleForTag moduleData;
    public final CCRuleForTag moduleData_controlCangeDefault;
    public final CCRuleForTag moduleData_exclusiveDefault;
    public final CCRuleForTag moduleData_progDefault;
    public final CCRuleForTag moduleData_rhythmDefault;

    public final CCRuleForTag instrumentList;
    public final CCRuleForTag instrumentList_map;
    public final CCRuleForTag instrumentList_pc;
    public final CCRuleForTag instrumentList_bank;

    public final CCRuleForTag drumSetList;
    public final CCRuleForTag drumSetList_map;
    public final CCRuleForTag drumSetList_pc;
    public final CCRuleForTag drumSetList_bank;
    public final CCRuleForTag drumSetList_tone;

    public final CCRuleForTag controlChangeMacroList;
    public final CCRuleForTag controlChangeMacroList_folder;
    public final CCRuleForTag controlChangeMacroList_folderlink;
    public final CCRuleForTag controlChangeMacroList_ccm;
    public final CCRuleForTag controlChangeMacroList_ccm_value;
    public final CCRuleForTag controlChangeMacroList_ccm_value_entry;
    public final CCRuleForTag controlChangeMacroList_ccm_gate;
    public final CCRuleForTag controlChangeMacroList_ccm_gate_entry;
    public final CCRuleForTag controlChangeMacroList_ccm_memo;
    public final CCRuleForTag controlChangeMacroList_ccm_data;
    public final CCRuleForTag controlChangeMacroList_ccmLink;
    public final CCRuleForTag controlChangeMacroList_table;
    public final CCRuleForTag controlChangeMacroList_table_entry;

    public final CCRuleForTag templateList;
    public final CCRuleForTag templateList_folder;
    public final CCRuleForTag templateList_template;
    public final CCRuleForTag templateList_template_memo;
    public final CCRuleForTag templateList_template_cc;
    public final CCRuleForTag templateList_template_pc;
    public final CCRuleForTag templateList_template_comment;

    public final CCRuleForTag defaultData;
    public final CCRuleForTag defaultData_mark;
    public final CCRuleForTag defaultData_track;
    public final CCRuleForTag defaultData_track_mark;
    public final CCRuleForTag defaultData_track_tempo;
    public final CCRuleForTag defaultData_track_timeSignature;
    public final CCRuleForTag defaultData_track_keySignature;
    public final CCRuleForTag defaultData_track_CC;
    public final CCRuleForTag defaultData_track_PC;
    public final CCRuleForTag defaultData_track_Comment;
    public final CCRuleForTag defaultData_track_Template;
    public final CCRuleForTag defaultData_track_EOT;
    
    final boolean _enableUndocumented = false;

    protected CCRuleManager() {
        root = new CCRuleForTag(null);

        moduleData = new CCRuleForTag("ModuleData");
        root.addChild(moduleData);

        /* ModuleData */
        moduleData.setupAttributeMust("Name", CCValueRule.valueRuleText); 
        moduleData.setupAttribute("Folder", "", CCValueRule.valueRuleText);
        moduleData.setupAttribute("Priority", "100", CCValueRule.valueRulePlusMinus);
        moduleData.setupAttribute("FileCreator", "", CCValueRule.valueRuleText);
        moduleData.setupAttribute("FileVersion", "", CCValueRule.valueRuleText);
        moduleData.setupAttribute("WebSite", "", CCValueRule.valueRuleText);

        /* ModuleData/RhythmTrackDefault */
        moduleData_rhythmDefault = new CCRuleForTag("RhythmTrackDefault");
        moduleData.addChild(moduleData_rhythmDefault);
        moduleData_rhythmDefault.setupAttributeMust("Gate", CCValueRule.valueRulePlusMinus);

        /* ModuleData/ExclusiveEventDefault */
        moduleData_exclusiveDefault = new CCRuleForTag("ExclusiveEventDefault");
        moduleData.addChild(moduleData_exclusiveDefault);
        moduleData_exclusiveDefault.setupAttributeMust("Data", CCValueRule.valueRuleText);

        /* ModuleData/ProgramChangeEventPropertyDlg */
        moduleData_progDefault = new CCRuleForTag("ProgramChangeEventPropertyDlg");
        moduleData.addChild(moduleData_progDefault);
        moduleData_progDefault.setupAttribute("AutoPreviewDelay", "0", CCValueRule.valueRule14bit);

        /* ModuleData/ControlChangeEventDefault */
        moduleData_controlCangeDefault = new CCRuleForTag("ControlChangeEventDefault");
        moduleData.addChild(moduleData_controlCangeDefault);
        moduleData_controlCangeDefault.setupAttribute("ID", "", CCValueRule.valueRulePlusMinus);

        /* ModuleData/InstrumentList */

        instrumentList = new CCRuleForTag("InstrumentList");
        moduleData.addChild(instrumentList);

        /* ModuleData/InstrumentList/Map */
        instrumentList_map = new CCRuleForTag("Map");
        instrumentList.addChild(instrumentList_map);
        instrumentList_map.setupAttributeMust("Name", CCValueRule.valueRuleText);

        /* ModuleData/InstrumentList/Map/PC */
        instrumentList_pc = new CCRuleForTag("PC");
        instrumentList_map.addChild(instrumentList_pc);

        instrumentList_pc.setupAttributeMust("Name", CCValueRule.valueRuleText);
        instrumentList_pc.setupAttributeMust("PC", CCValueRule.valueRule7bit);

        /* ModuleData/InstrumentList/Map/PC/Bank */
        instrumentList_bank = new CCRuleForTag("Bank");
        instrumentList_pc.addChild(instrumentList_bank);

        instrumentList_bank.setupAttributeMust("Name", CCValueRule.valueRuleText);
        instrumentList_bank.setupAttribute("LSB", "", CCValueRule.valueRule7bit);
        instrumentList_bank.setupAttribute("MSB", "", CCValueRule.valueRule7bit);

        /* ModuleData/DrumSetList */
        drumSetList = new CCRuleForTag("DrumSetList");
        moduleData.addChild(drumSetList);

        /* ModuleData/DrumSetList/Map */
        drumSetList_map = new CCRuleForTag("Map");
        drumSetList.addChild(drumSetList_map);
        drumSetList_map.setupAttributeMust("Name", CCValueRule.valueRuleText);

        /* ModuleData/DrumSetList/Map/PC */
        drumSetList_pc = new CCRuleForTag("PC");
        drumSetList_map.addChild(drumSetList_pc);
        drumSetList_pc.setupAttributeMust("Name", CCValueRule.valueRuleText);
        drumSetList_pc.setupAttributeMust("PC", CCValueRule.valueRule7bit);

        /* ModuleData/DrumSetList/Map/PC/Bank */
        drumSetList_bank = new CCRuleForTag("Bank");
        drumSetList_pc.addChild(drumSetList_bank);
        drumSetList_bank.setupAttributeMust("Name", CCValueRule.valueRuleText);
        drumSetList_bank.setupAttribute("LSB", "", CCValueRule.valueRule7bit);
        drumSetList_bank.setupAttribute("MSB", "", CCValueRule.valueRule7bit);

        /* ModuleData/DrumSetList/Map/PC/Bank/Tone */
        drumSetList_tone = new CCRuleForTag("Tone");
        drumSetList_bank.addChild(drumSetList_tone);
        drumSetList_tone.setupAttributeMust("Name", CCValueRule.valueRuleText);
        drumSetList_tone.setupAttributeMust("Key", CCValueRule.valueRule7bit);

        /* ModuleData/ControlChangeMacroList */
        controlChangeMacroList = new CCRuleForTag("ControlChangeMacroList");
        moduleData.addChild(controlChangeMacroList);

        /* ModuleData/ControlChangeMacroList/Folder */
        controlChangeMacroList_folder = new CCRuleForTag("Folder");
        controlChangeMacroList.addChild(controlChangeMacroList_folder);
        controlChangeMacroList_folder.addChild(controlChangeMacroList_folder);

        controlChangeMacroList_folder.setupAttributeMust("Name", CCValueRule.valueRuleText);
        controlChangeMacroList_folder.setupAttribute("ID", "-1", CCValueRule.valueRulePlusMinus);

        /* ModuleData/ControlChangeMacroList/CCM */
        controlChangeMacroList_ccm = new CCRuleForTag("CCM");
        controlChangeMacroList.addChild(controlChangeMacroList_ccm);

        controlChangeMacroList_ccm.setupAttributeMust("ID", CCValueRule.valueRulePlusMinus);
        controlChangeMacroList_ccm.setupAttributeMust("Name", CCValueRule.valueRuleText);
        controlChangeMacroList_ccm.setupAttribute("Color", "#000000", CCValueRule.valueRuleColorFormat);
        controlChangeMacroList_ccm.setupAttribute("Sync", "", CCValueRule.valueRuleSyncOR);
        controlChangeMacroList_ccm.setupAttribute("MuteSync", "", CCValueRule.valueRule1bit);

        /* ModuleData/ControlChangeMacroList/CCM/Value */
        controlChangeMacroList_ccm_value = new CCRuleForTag("Value");
        controlChangeMacroList_ccm.addChild(controlChangeMacroList_ccm_value);

        controlChangeMacroList_ccm_value.setupAttribute("Default", "0", CCValueRule.valueRulePlusMinus);
        controlChangeMacroList_ccm_value.setupAttribute("Min", "0", CCValueRule.valueRulePlusMinus);
        controlChangeMacroList_ccm_value.setupAttribute("Max", "127", CCValueRule.valueRulePlusMinus);
        controlChangeMacroList_ccm_value.setupAttribute("Offset", "0", CCValueRule.valueRulePlusMinus);
        controlChangeMacroList_ccm_value.setupAttribute("Name", "", CCValueRule.valueRuleText);
        controlChangeMacroList_ccm_value.setupAttribute("Type", "", CCValueRule.valueRuleKeyOR);
        controlChangeMacroList_ccm_value.setupAttribute("TableID", "", CCValueRule.valueRulePlusMinus);

        /* ModuleData/ControlChangeMacroList/CCM/Value/Entry */
        controlChangeMacroList_ccm_value_entry = new CCRuleForTag("Entry");
        controlChangeMacroList_ccm_value.addChild(controlChangeMacroList_ccm_value_entry);

        controlChangeMacroList_ccm_value_entry.setupAttributeMust("Label", CCValueRule.valueRuleText);
        controlChangeMacroList_ccm_value_entry.setupAttributeMust("Value", CCValueRule.valueRuleText);

        /* ModuleData/ControlChangeMacroList/CCM/Gate */
        controlChangeMacroList_ccm_gate = new CCRuleForTag("Gate");
        controlChangeMacroList_ccm.addChild(controlChangeMacroList_ccm_gate);

        controlChangeMacroList_ccm_gate.setupAttribute("Default", "0", CCValueRule.valueRulePlusMinus);
        controlChangeMacroList_ccm_gate.setupAttribute("Min", "0", CCValueRule.valueRulePlusMinus);
        controlChangeMacroList_ccm_gate.setupAttribute("Max", "127", CCValueRule.valueRulePlusMinus);
        controlChangeMacroList_ccm_gate.setupAttribute("Offset", "0", CCValueRule.valueRulePlusMinus);
        controlChangeMacroList_ccm_gate.setupAttribute("Name", "", CCValueRule.valueRuleText);
        controlChangeMacroList_ccm_gate.setupAttribute("Type", "", CCValueRule.valueRuleKeyOR);
        controlChangeMacroList_ccm_gate.setupAttribute("TableID", "", CCValueRule.valueRulePlusMinus);
        
        /* ModuleData/ControlChangeMacroList/CCM/Gate/Entry */
        controlChangeMacroList_ccm_gate_entry = new CCRuleForTag("Entry");
        controlChangeMacroList_ccm_gate.addChild(controlChangeMacroList_ccm_gate_entry);

        controlChangeMacroList_ccm_gate_entry.setupAttributeMust("Label", CCValueRule.valueRuleText);
        controlChangeMacroList_ccm_gate_entry.setupAttributeMust("Value", CCValueRule.valueRule14bit);
        
        /* ModuleData/ControlChangeMacroList/CCM/Memo */
        controlChangeMacroList_ccm_memo = new CCRuleForTag("Memo");
        controlChangeMacroList_ccm_memo.readyForText(true);
        controlChangeMacroList_ccm.addChild(controlChangeMacroList_ccm_memo);
        controlChangeMacroList_folder.addChild(controlChangeMacroList_ccm_memo);
        
        /* ModuleData/ControlChangeMacroList/CCM/Data */
        controlChangeMacroList_ccm_data = new CCRuleForTag("Data");
        //controlChangeMacroList_ccm_data.readyForAttribute("Value", ""); //Undocumented
        controlChangeMacroList_ccm_data.readyForText(true);
        controlChangeMacroList_ccm.addChild(controlChangeMacroList_ccm_data);

        /* ModuleData/ControlChangeMacroList/CCMLink */
        controlChangeMacroList_ccmLink = new CCRuleForTag("CCMLink");
        controlChangeMacroList.addChild(controlChangeMacroList_ccmLink);

        controlChangeMacroList_ccmLink.setupAttributeMust("ID", CCValueRule.valueRulePlusMinus);
        controlChangeMacroList_ccmLink.setupAttribute("Value", "", CCValueRule.valueRule14bit);
        controlChangeMacroList_ccmLink.setupAttribute("Gate", "", CCValueRule.valueRule14bit);

        /* ModuleData/ControlChangeMacroList/FolderList */
        controlChangeMacroList_folderlink = new CCRuleForTag("FolderLink");
        controlChangeMacroList.addChild(controlChangeMacroList_folderlink);
        controlChangeMacroList_folder.addChild(controlChangeMacroList_folderlink);

        controlChangeMacroList_folderlink.setupAttributeMust("Name", CCValueRule.valueRuleText);
        controlChangeMacroList_folderlink.setupAttributeMust("ID", CCValueRule.valueRulePlusMinus);
        controlChangeMacroList_folderlink.setupAttribute("Value", "", CCValueRule.valueRule14bit);
        controlChangeMacroList_folderlink.setupAttribute("Gate", "", CCValueRule.valueRule14bit);
        
        /* ModuleData/ControlChangeMacroList/CCM/Data */
        controlChangeMacroList_ccm_data.readyForText(true);
        controlChangeMacroList_ccm.addChild(controlChangeMacroList_ccm_data);
        
        /* ModuleData/ControlChangeMacroList/Table */
        controlChangeMacroList_table = new CCRuleForTag("Table");
        controlChangeMacroList.addChild(controlChangeMacroList_table);
        controlChangeMacroList_table.setupAttributeMust("ID", CCValueRule.valueRulePlusMinus);

        /* ModuleData/ControlChangeMacroList/Table/Entry */
        controlChangeMacroList_table_entry = new CCRuleForTag("Entry");
        controlChangeMacroList_table.addChild(controlChangeMacroList_table_entry);
        controlChangeMacroList_table_entry.setupAttributeMust("Label", CCValueRule.valueRuleText);
        controlChangeMacroList_table_entry.setupAttributeMust("Value", CCValueRule.valueRulePlusMinus);

        /* ModuleData/ControlChangeMacroList/Folder */
        controlChangeMacroList_folder.setupAttributeMust("Name", CCValueRule.valueRuleText);
        controlChangeMacroList_folder.setupAttribute("ID", "-1", CCValueRule.valueRulePlusMinus);
        controlChangeMacroList_folder.addChild(controlChangeMacroList_ccm);
        controlChangeMacroList_folder.addChild(controlChangeMacroList_folder);
        controlChangeMacroList_folder.addChild(controlChangeMacroList_ccmLink);
        controlChangeMacroList_folder.addChild(controlChangeMacroList_table);

        /* ModuleData/TemplateList */

        templateList = new CCRuleForTag("TemplateList");
        moduleData.addChild(templateList);
        
        /* ModuleData/TemplateList/Template */
        templateList_template = new CCRuleForTag("Template");

        /* ModuleData/TemplateList/Folder */
        templateList_folder = new CCRuleForTag("Folder");
        templateList.addChild(templateList_template);
        templateList.addChild(templateList_folder);

        templateList_folder.setupAttributeMust("Name", CCValueRule.valueRuleText);
        templateList_folder.addChild(templateList_template);
        templateList_folder.addChild(templateList_folder);

        templateList_template.setupAttribute("ID", "", CCValueRule.valueRulePlusMinus);
        templateList_template.setupAttributeMust("Name", CCValueRule.valueRuleText);

        /* ModuleData/TemplateList/CC */
        templateList_template_cc = new CCRuleForTag("CC");
        templateList_template.addChild(templateList_template_cc);
        templateList_template_cc.setupAttributeMust("ID", CCValueRule.valueRulePlusMinus);
        templateList_template_cc.setupAttribute("Value", "", CCValueRule.valueRulePlusMinus);
        templateList_template_cc.setupAttribute("Gate", "", CCValueRule.valueRulePlusMinus);

        /* ModuleData/TemplateList/PC */
        templateList_template_pc = new CCRuleForTag("PC");
        templateList_template.addChild(templateList_template_pc);
        templateList_template_pc.setupAttribute("PC", "1", CCValueRule.valueRule7bit);
        templateList_template_pc.setupAttribute("MSB", "", CCValueRule.valueRule7bit);
        templateList_template_pc.setupAttribute("LSB", "", CCValueRule.valueRule7bit);
        templateList_template_pc.setupAttribute("Mode", "", CCValueRule.valueRuleTypeDrumOR);

        /* ModuleData/TemplateList/Memo */
        templateList_template_memo = new CCRuleForTag("Memo");
        templateList_template.addChild(templateList_template_memo);
        templateList_template_memo.readyForText(true);

        /* ModuleData/TemplateList/Comment */
        templateList_template_comment = new CCRuleForTag("Comment");
        templateList_template.addChild(templateList_template_comment);
        templateList_template_comment.setupAttribute("Text", "", CCValueRule.valueRuleText);

        /* ModuleData/DefaultData */
        defaultData = new CCRuleForTag("DefaultData");
        moduleData.addChild(defaultData);

        /* ModuleData/DefaultData/Mark */
        defaultData_mark = new CCRuleForTag("Mark");
        defaultData.addChild(defaultData_mark);
        defaultData_mark.setupAttribute("Name", "", CCValueRule.valueRuleText); //Undocumented
        defaultData_mark.setupAttribute("Tick", "", CCValueRule.valueRulePlusMinus);
        defaultData_mark.setupAttribute("Step", "", CCValueRule.valueRulePlusMinus);
        //defaultData_mark.readyForAttribute("Meas", ""); //Undocumented

        /* ModuleData/DefaultData/Track */
        defaultData_track = new CCRuleForTag("Track");
        defaultData.addChild(defaultData_track);
        defaultData_track.setupAttribute("Name", "", CCValueRule.valueRuleText);
        defaultData_track.setupAttribute("Ch", "1", CCValueRule.valueRule4bit);
        defaultData_track.setupAttribute("Mode", "", CCValueRule.valueRuleTypeDrumOR);
        //defaultData_track.readyForAttribute("Current", "1"); //Undocumented

        /* ModuleData/DefaultData/Track/Mark */
        defaultData_track_mark = new CCRuleForTag("Mark");
        defaultData_track.addChild(defaultData_track_mark);
        defaultData_track_mark.setupAttribute("Name", "", CCValueRule.valueRuleText);
        defaultData_track_mark.setupAttribute("Tick", "", CCValueRule.valueRulePlusMinus);
        defaultData_track_mark.setupAttribute("Step", "", CCValueRule.valueRulePlusMinus);
        //defaultData_track_mark.readyForAttribute("Meas", ""); //Undocumented

        /* ModuleData/DefaultData/Track/Tempo */
        defaultData_track_tempo = new CCRuleForTag("Tempo");
        defaultData_track.addChild(defaultData_track_tempo);
//        defaultData_track_tempo.readyForAttribute("Tick", "", CCValueRule.valueRulePlusMinus);
        defaultData_track_tempo.setupAttribute("Tempo", "", CCValueRule.valueRule14bit);
        /* follow 3 lines undocumented but, some XML file do like it */
        defaultData_track_tempo.setupAttribute("Tick", "", CCValueRule.valueRule14bit);
        defaultData_track_tempo.setupAttribute("Current", "", CCValueRule.valueRule14bit);
        defaultData_track_tempo.setupAttribute("Skip", "", CCValueRule.valueRule14bit);

        /* ModuleData/DefaultData/Track/TimeSignature */
        defaultData_track_timeSignature = new CCRuleForTag("TimeSignature");
        defaultData_track.addChild(defaultData_track_timeSignature);

        defaultData_track_timeSignature.setupAttributeMust("TimeSignature", CCValueRule.valueRuleTimeSignature); // 4/4
        defaultData_track_timeSignature.setupAttribute("Tick", "", CCValueRule.valueRulePlusMinus);
        defaultData_track_timeSignature.setupAttribute("Step", "", CCValueRule.valueRulePlusMinus);

        /* ModuleData/DefaultData/Track/KeySignature */
        defaultData_track_keySignature = new CCRuleForTag("KeySignature");
        defaultData_track.addChild(defaultData_track_keySignature);
        defaultData_track_keySignature.setupAttributeMust("KeySignature", CCValueRule.valueKeySignature);
        defaultData_track_keySignature.setupAttribute("Tick", "", CCValueRule.valueRulePlusMinus);
        defaultData_track_keySignature.setupAttribute("Step", "", CCValueRule.valueRulePlusMinus);

        /* ModuleData/DefaultData/Track/CC */
        defaultData_track_CC = new CCRuleForTag("CC");
        defaultData_track.addChild(defaultData_track_CC);
        defaultData_track_CC.setupAttributeMust("ID", CCValueRule.valueRulePlusMinus);
        defaultData_track_CC.setupAttribute("Value", "", CCValueRule.valueRulePlusMinus);
        defaultData_track_CC.setupAttribute("Gate", "", CCValueRule.valueRulePlusMinus);
        defaultData_track_CC.setupAttribute("Tick", "", CCValueRule.valueRulePlusMinus);
        defaultData_track_CC.setupAttribute("Step", "", CCValueRule.valueRulePlusMinus);

        /* ModuleData/DefaultData/Track/PC */
        defaultData_track_PC = new CCRuleForTag("PC");
        defaultData_track.addChild(defaultData_track_PC);
        defaultData_track_PC.setupAttribute("PC", "1", CCValueRule.valueRule7bit);
        defaultData_track_PC.setupAttribute("MSB", "", CCValueRule.valueRule7bit);
        defaultData_track_PC.setupAttribute("LSB", "", CCValueRule.valueRule7bit);
        defaultData_track_PC.setupAttribute("Mode", "", CCValueRule.valueRuleTypeDrumOR);
        defaultData_track_PC.setupAttribute("Tick", "", CCValueRule.valueRulePlusMinus);
        defaultData_track_PC.setupAttribute("Step", "", CCValueRule.valueRulePlusMinus);

        /* ModuleData/DefaultData/Track/Comment */
        defaultData_track_Comment = new CCRuleForTag("Comment");
        defaultData_track.addChild(defaultData_track_Comment);
        defaultData_track_Comment.setupAttribute("Text", "", CCValueRule.valueRuleText);
        defaultData_track_Comment.setupAttribute("Tick", "", CCValueRule.valueRulePlusMinus);
        defaultData_track_Comment.setupAttribute("Step", "", CCValueRule.valueRulePlusMinus);
        
        /* ModuleData/DefaultData/Track/Template */
        defaultData_track_Template = new CCRuleForTag("Template");
        defaultData_track.addChild(defaultData_track_Template);
        defaultData_track_Template.setupAttribute("ID", "", CCValueRule.valueRulePlusMinus);
        defaultData_track_Template.setupAttribute("Tick", "", CCValueRule.valueRulePlusMinus);
        defaultData_track_Template.setupAttribute("Step", "", CCValueRule.valueRulePlusMinus);

        /* ModuleData/DefaultData/Track/EOT */
        defaultData_track_EOT = new CCRuleForTag("EOT");
        defaultData_track.addChild(defaultData_track_EOT);
        defaultData_track_EOT.setupAttribute("ID", "", CCValueRule.valueRulePlusMinus);
        defaultData_track_EOT.setupAttribute("Tick", "", CCValueRule.valueRulePlusMinus);
    }

    public static void dumpRulesSub(int space, CCRuleForTag current, HashSet<CCRuleForTag> already) {
        StringBuffer indent = new StringBuffer();
        for (int i = 0; i < space; ++ i) {
            indent.append("    ");
        }
        if (already.contains(current)) {
            return;
        }
        already.add(current);

        List<CCRuleForAttribute> listAttributes = current.listAttributes();
        StringBuffer strAttributes = new StringBuffer();
        for (CCRuleForAttribute attribute : listAttributes) {
            if (strAttributes.length() > 0) {
                strAttributes.append(", ");
            }
            strAttributes.append("[");
            strAttributes.append(attribute.getName());
            if (attribute.getDefaultValue() != null) {
                strAttributes.append("=");
                strAttributes.append(attribute.getDefaultValue());
            }
            strAttributes.append("]");
        }
        if (current.hasTextContents()) {
            if (strAttributes.length() > 0) {
                strAttributes.append(", ");
            }
            strAttributes.append("[Text]");
        }
        
        System.out.println(indent.toString() + current.getName() + strAttributes.toString());
        
        List<CCRuleForTag> listChildren = current.listChildTags();
        for (CCRuleForTag child : listChildren) {
            dumpRulesSub(space + 1, child, already);
        }
    }
    
    public static void dumpRules() {
        dumpRulesSub(0, CCRuleManager.getInstance().getModuleDataTag(), new HashSet<CCRuleForTag>());
    }
}
