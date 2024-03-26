/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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

import java.util.LinkedList;
import jp.synthtarou.midimixer.ccxml.xml.CXNode;
import jp.synthtarou.midimixer.ccxml.xml.CXFile;
import jp.synthtarou.midimixer.ccxml.rules.CCRuleManager;
import java.util.List;
import java.util.TreeMap;
import jp.synthtarou.midimixer.libs.namedvalue.MNamedValueList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class InformationForModule {
    public final CXNode _node;
    public final CXFile _file;
    
    public InformationForModule(CXFile file, CXNode moduleNode) {
        _node = moduleNode;
        _file = file;
        if (_node._nodeName.equals("ModuleData") == false) {
            throw new IllegalArgumentException("This is not module node."); 
        }
    }        
    
    MNamedValueList<MNamedValueList<Integer>> _listTable = null;
    TreeMap<Integer, CXNode> _cacheCCM = null;
    
    public CXNode getCCMById(int id) {
        if (_cacheCCM != null) {
            return _cacheCCM.get(id);
        }
        
        LinkedList<CXNode> prepare = new LinkedList<>();
        prepare.add(_node);
        TreeMap<Integer, CXNode> cacheCCM = new TreeMap();
        
        while (prepare.isEmpty() == false) {
            CXNode seek = prepare.removeFirst();
            if (seek._nodeName.equals("CCM")) {
                String textId = seek._listAttributes.valueOfName("ID");
                try {
                    int seekID = Integer.parseInt(textId);
                    cacheCCM.put(seekID, seek);
                }catch(Exception ex) {
                }
            }
            prepare.addAll(seek.listChildren());
        }

        _cacheCCM = cacheCCM;
        return _cacheCCM.get(id);
    }

    public void fillCCMLink() {
        LinkedList<CXNode> prepare = new LinkedList<>();
        prepare.add(_node);
        TreeMap<Integer, CXNode> cacheCCM = new TreeMap();
        
        while (prepare.isEmpty() == false) {
            CXNode seek = prepare.removeFirst();
            if (seek._nodeName.equalsIgnoreCase("CCMLink")) {
                String textId = seek._listAttributes.valueOfName("ID");
                try {
                    int linkId = Integer.parseInt(textId);
                    seek._ccmLinkTo = getCCMById(linkId);
                }catch(Exception ex) {
                }
            }
            List<CXNode> children = seek.listChildren();
            if (children != null) {        
                prepare.addAll(seek.listChildren());
            }
        }
    }

    public MNamedValueList<Integer> getTable(int id) {
        prepareTable();
        if (_listTable == null) {
            return null;
        }
        return _listTable.valueOfName(String.valueOf(id));
    }
    
    public void prepareTable() {
        if (_listTable != null) {
            return;
        }
        CCRuleManager rules = CCRuleManager.getInstance();
        CXNode ccm = _node.firstChild(rules.controlChangeMacroList);
        if (ccm == null) {
            return;
        }
        _listTable = new MNamedValueList<>();

        List<CXNode> listTable = ccm.listChildren(rules.controlChangeMacroList_table);
        if (listTable == null) {
            return;
        }
        for (CXNode seekTable : listTable) {
            String id = seekTable._listAttributes.valueOfName("ID");
            int id2 = CXFile.parseNumber(id);
            if (id2 < 0) {
                continue;
            }
            List<CXNode> listEntry = seekTable.listChildren("Entry");
            if (listEntry == null) {
                continue;
            }
            MNamedValueList<Integer> contents = new MNamedValueList<>();

            for (CXNode seekEntry : listEntry) {
                String label = seekEntry._listAttributes.valueOfName("Label");
                String value = seekEntry._listAttributes.valueOfName("Value");
                if (value == null || value.isBlank()) {
                    continue;
                }
                if (label == null || label.isBlank()) {
                    //is it right?
                    label = value;
                }
                int x = CXFile.parseNumber(value);

                contents.addNameAndValue(label, x);
            }

            if (!contents.isEmpty()) {
                _listTable.addNameAndValue(String.valueOf(id2), contents);
            }
        }
    }

}
