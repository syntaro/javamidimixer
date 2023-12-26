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
package jp.synthtarou.midimixer.libs.midi.capture;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jp.synthtarou.midimixer.ccxml.InformationForCCM;
import jp.synthtarou.midimixer.ccxml.xml.CXGeneralMidiFile;
import jp.synthtarou.midimixer.ccxml.xml.CXNode;
import jp.synthtarou.midimixer.ccxml.InformationForModule;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CounterTable {
    
    public static void main(String[] args) {
        CounterTable table = new CounterTable();
        for (Counter seek : table._listAll) {
            System.out.println("[" + seek._folderName +"] " + seek._name + " = " +  seek._message);
        }
    }

    public static CXGeneralMidiFile _generalMidi = CXGeneralMidiFile.getInstance();

    public CounterTable() {
        _listAll = new ArrayList<>();
        _listDetected = new ArrayList<>();
        for (InformationForModule module : _generalMidi.listModules()) {
            seekEveryCCM(module);
        }
    }
    
    List<Counter> _listAll;
    List<Counter> _listDetected;
    
    public void seekEveryCCM(InformationForModule module) {
        LinkedList<CXNode> listTarget = new LinkedList();
        listTarget.add(module._node);
        
        while (listTarget.isEmpty() == false) {
            CXNode parent = listTarget.removeFirst();
            int count = parent.countChildTags();
            for (int i = 0; i < count; ++ i) {
                CXNode child = parent.getChild(i);

                if (child.getName().equals("CCM")) {
                    Counter cap = new Counter();
                    cap._ccNode = child;

                    InformationForCCM ccm = new InformationForCCM(module, child);
                    MXTemplate template = null;
                    try {
                        template = new MXTemplate(ccm._data);
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                    MXMessage message = MXMessageFactory.fromTemplate(0, template, 0, MXRangedValue.ZERO7, MXRangedValue.ZERO7);
                    
                    cap._message = message;
                    ArrayList<String> folderList = new ArrayList<>();
                    cap._name = child._listAttributes.valueOfName("Name");
                    
                    CXNode seekFolder = child.getParent();
                    while (seekFolder != null) {
                        if (seekFolder.getName().equalsIgnoreCase("Folder")) {
                            folderList.add(0, seekFolder._listAttributes.valueOfName("Name"));
                            break;
                        }
                        seekFolder = seekFolder.getParent();
                    }
                    if (folderList.isEmpty() == false) {
                        StringBuffer path = new StringBuffer();
                        for (String part : folderList) {
                            if (path.length() == 0) {
                                path.append(part);
                            }else {
                                path.append(", ");
                                path.append(part);
                            }
                            cap._folderName = path.toString();
                        }
                    }
                    _listAll.add(cap);
                }
                else {
                    listTarget.add(child);
                }
            }
        }
    }
    
    public Counter captureIt(MXMessage message) {
        for (Counter seek : _listDetected) {
            if (seek.captureIt(message, true)) {
                _model.reloadCounter(seek);
                return seek;
            }
        }
        for (Counter seek : _listAll) {
            if (seek.captureIt(message, false)) {
                seek = seek.createFrends(message);
                _listDetected.add(seek);
                seek._name = message.getTemplateAsText();
                seek._bindedFolder = ensureFolder(seek._folderName);
                _listDetected.add(seek);
                _model.addCounter(seek._bindedFolder, seek);
                _model.reloadFolder(seek._bindedFolder);
                return seek;
            }
        }
        Counter newCounter = new Counter();
        newCounter._name = message.getTemplateAsText();
        newCounter._ccNode = null;
        newCounter._folderName = "Uncategorized";
        newCounter._bindedFolder = ensureFolder(newCounter._folderName);
        newCounter._message = message;
        _listDetected.add(newCounter);
        _model.addCounter(newCounter._bindedFolder, newCounter);
        _model.reloadFolder(newCounter._bindedFolder);
        return newCounter;
    }
    
    CounterTreeModel _model = new CounterTreeModel();
    Map<String, CounterFolder> _folderCache = new TreeMap();
    
    public synchronized  CounterFolder ensureFolder(String path) {
        CounterFolder ret = _folderCache.get(path);
        if (ret == null) {
            ret = new CounterFolder(path);
            _model.addFolder(ret);
            _folderCache.put(path, ret);
        }
        return ret;
    }
}
