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
package jp.synthtarou.midimixer.mx36ccmapping;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageWrapListFactory;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingNode;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.mx30surface.MGStatus;


/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36Process extends MXReceiver implements MXSettingTarget {

    MX36View _view;
    MXSetting _setting;
    MX36FolderList _list;

    public MX36Process() {
        _setting = new MXSetting("CCMapping");
        _setting.setTarget(this);
        _list = new MX36FolderList(this);
        _view = new MX36View(this, _list);
        _setting.readSettingFile();
    }

    public void processStatus(MGStatus status) {
    }

    @Override
    public String getReceiverName() {
        return "CCMapping (Not Worked yet)";
    }

    @Override
    public JPanel getReceiverView() {
        return _view;
    }

    @Override
    public void processMXMessage(MXMessage message) {
        MGStatus[] result = message._mx30result;
        boolean done = false;
        
        if (result != null && result.length > 0) {
            for (MGStatus status : result) {
                if (status._uiType == MGStatus.TYPE_DRUMPAD) {
                    continue;
                }
                MX36Status status2 = null;
                for (MX36Folder folder : _list._listFolder) 
                {
                    for (MX36Status seek : folder._list) {
                        if (seek._surfacePort == status._port
                         && seek._surfaceRow == status._row
                         && seek._surfaceColumn == status._column
                         && seek._surfaceUIType == status._uiType) {
                            status2 = seek;
                        }
                    }
                }
                if(status2 == null) {                    
                    MX36Folder folder2 = _list._autodetectedFolder;
                    status2 = MX36Status.fromMGStatus(folder2, status);
                    folder2.insertSorted(status2);
                    folder2.refill(status2);
                    //メッセージがアサインされてないので不要
                    //updateSurfaceValue(status2, status.getValue());
                }
                else {
                    MX36Folder folder2 = status2._folder;
                    updateSurfaceValue(status2, status.getValue());
                    if (status2._outDataText == null || status2._outDataText.isBlank()) {
                        
                    }else {
                        done = true;
                    }
                    folder2.refill(status2);
                }
            }
        }
        if (!done) {
            sendToNext(message);
        }
    }

    @Override
    public void prepareSettingFields(MXSetting setting) {
        setting.register("Folder[].Name");
        setting.register("Folder[].Status[].Name");

        setting.register("Folder[].Status[].Memo");
        setting.register("Folder[].Status[].DataText");
        setting.register("Folder[].Status[].Port");
        setting.register("Folder[].Status[].Channel");
        setting.register("Folder[].Status[].ValueRange");
        setting.register("Folder[].Status[].ValueOffset");
        setting.register("Folder[].Status[].ValueTable[]");
        setting.register("Folder[].Status[].GateRange");
        setting.register("Folder[].Status[].GateOffset");
        setting.register("Folder[].Status[].GateTable[]");
        setting.register("Folder[].Status[].GateTypeIsKey");

        setting.register("Folder[].Status[].SurfacePort");
        setting.register("Folder[].Status[].SurfaceUIType");
        setting.register("Folder[].Status[].SurfaceRow");
        setting.register("Folder[].Status[].SurfaceColumn");

        setting.register("Folder[].Status[].Bind1RCH1");
        setting.register("Folder[].Status[].Bind1RCH2");
        setting.register("Folder[].Status[].Bind1RCH4");

        setting.register("Folder[].Status[].BindRSCTRT1");
        setting.register("Folder[].Status[].BindRSCTRT2");
        setting.register("Folder[].Status[].BindRSCTRT3");

        setting.register("Folder[].Status[].BindRSCTPT1");
        setting.register("Folder[].Status[].BindRSCTPT2");
        setting.register("Folder[].Status[].BindRSCTPT3");
    }
    
    static String rangeToString(MXRangedValue value) {
        return value._var  +"," + value._min + "," + value._max;
    }
    
    static MXRangedValue stringToRange(String text) {
        ArrayList<String> split = new ArrayList<>();
        MXUtil.split(text, split, ',');
        if(split.size() >= 3) {
            try {
                int var = Integer.parseInt(MXUtil.shrinkText(split.get(0)));
                int min = Integer.parseInt(MXUtil.shrinkText(split.get(1)));
                int max = Integer.parseInt(MXUtil.shrinkText(split.get(2)));
                if (var >= min && var <= max) {
                    if (min == 0 && max == 127) {
                        return MXRangedValue.new7bit(var);
                    }
                    return new MXRangedValue(var, min, max);
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
        
        
    @Override
    public void afterReadSettingFile(MXSetting setting) {
        List<MXSettingNode> listFolder = setting.findByPath("Folder[]");
        for (MXSettingNode folderNode : listFolder) {
            String folderName = folderNode.getSetting("Name");
            MX36Folder folder = _list.newFolder(folderName);
            
            MXSettingNode statusNode = folderNode.findNode("Status");
            if (statusNode != null) {
                List<MXSettingNode> numbers = statusNode.findNumbers();
                for(MXSettingNode props : numbers) {
                    MX36Status status = new MX36Status();
                    
                    status._outName = props.getSetting("Name");
                    status._outMemo = props.getSetting("Memo");
                    status._outDataText = props.getSetting("DataText");
                    status._outPort = props.getSettingAsInt("Port", -1);
                    status._outChannel = props.getSettingAsInt("Channel", 0);
                    status._outValueRange = stringToRange(props.getSetting("ValueRange"));
                    status._outValueOffset = props.getSettingAsInt("ValueOffset", 0);

                    MXSettingNode valueTable = props.findNode("ValueTable");
                    MXWrapList<Integer> parsedValueTable = new MXWrapList<>();
                    if (valueTable != null) {
                        List<MXSettingNode> listValue = valueTable.findNumbers();
                        if (listValue != null) {
                            for (MXSettingNode valueNode : listValue) {
                                try {
                                    int name = Integer.valueOf(valueNode.getName());
                                    String var = valueNode._value;
                                    parsedValueTable.addNameAndValue(var, name);
                                }catch(Exception e) {
                                    continue;
                                }
                            }
                        }
                    }
                    if (parsedValueTable.size() == 0) {
                        parsedValueTable  = MXMessageWrapListFactory.listupRange(status._outValueRange._min, status._outValueRange._max);
                    }
                    status._outValueTable = parsedValueTable;
                    
                    status._outGateRange = stringToRange(props.getSetting("GateRange"));
                    status._outGateOffset = props.getSettingAsInt("GateOffset", 0);
                    MXSettingNode gateTable = props.findNode("GateTable");
                    MXWrapList<Integer> parsedGateTable = new MXWrapList<>();
                    if (gateTable != null) {
                        List<MXSettingNode> listGate = gateTable.findNumbers();
                        if (listGate != null) {
                            for (MXSettingNode gateNode : listGate) {
                                try {
                                    int name = Integer.valueOf(gateNode.getName());
                                    String var = gateNode._value;
                                    parsedGateTable.addNameAndValue(var, name);
                                }catch(Exception e) {
                                    continue;
                                }
                            }
                        }
                    }
                    if (parsedGateTable.size() == 0) {
                        parsedGateTable  = MXMessageWrapListFactory.listupRange(status._outGateRange._min, status._outGateRange._max);
                    }
                    status._outGateTable = parsedGateTable;
                    status._outGateTypeKey = props.getSettingAsBoolean(props.getSetting("GateTypeIsKey"), false);

                    status._surfacePort = props.getSettingAsInt("SurfacePort", 0);
                    status._surfaceUIType = props.getSettingAsInt("SurfaceUIType", 0);
                    status._surfaceRow = props.getSettingAsInt("SurfaceRow", 0);
                    status._surfaceColumn = props.getSettingAsInt("SurfaceColumn", 0);

                    status._bind1RCH = props.getSettingAsInt("Bind1RCH1" ,0);
                    status._bind2RCH = props.getSettingAsInt("Bind1RCH2", 0);
                    status._bind4RCH = props.getSettingAsInt("Bind1RCH4", 0);

                    status._bindRSCTRT1 =  props.getSettingAsInt("BindRSCTRT1", 0);
                    status._bindRSCTRT2 =  props.getSettingAsInt("BindRSCTRT2", 0);
                    status._bindRSCTRT3 =  props.getSettingAsInt("BindRSCTRT3", 0);

                    status._bindRSCTPT1 =  props.getSettingAsInt("BindRSCTPT1", 0);
                    status._bindRSCTPT2 =  props.getSettingAsInt("BindRSCTPT2", 0);
                    status._bindRSCTPT3 =  props.getSettingAsInt("BindRSCTPT3", 0);

                    folder.insertSorted(status);
                }
            }
        }
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        int folderN = 0;

        setting.clearValue();
        for (MX36Folder folder : _list._listFolder) {
            String prefix = "Folder[" + folderN + "].";
            folderN ++;
            
            setting.setSetting(prefix + "Name", folder._folderName);

            int statusN = 0;
            for (MX36Status status : folder._list) {
                String prefix2 = prefix + "Status[" + statusN + "].";
                statusN ++;

                setting.setSetting(prefix2 + "Name", status._outName);
                setting.setSetting(prefix2 + "Memo", status._outMemo);
                setting.setSetting(prefix2 + "DataText", status._outDataText);
                setting.setSetting(prefix2 + "Port", status._outPort);
                setting.setSetting(prefix2 + "Channel", status._outChannel);
                setting.setSetting(prefix2 + "ValueRange", rangeToString(status._outValueRange));
                setting.setSetting(prefix2 + "ValueOffset", status._outValueOffset);
                if (!isSameAsTable(status._outValueRange, status._outValueTable)) {
                    for (MXWrap<Integer> value : status._outValueTable) {
                        setting.setSetting(prefix2 + "ValueTable[" + value._value + "]", value._value);
                    }
                }
                setting.setSetting(prefix2 + "GateRange", rangeToString(status._outGateRange));
                setting.setSetting(prefix2 + "GateOffset", status._outGateOffset);
                if (!isSameAsTable(status._outGateRange, status._outGateTable)) {
                    for (MXWrap<Integer> gate : status._outGateTable) {
                        setting.setSetting(prefix2 + "GateTable[" + gate._value + "]", gate._value);
                    }
                }
                setting.setSetting(prefix2 + "GateTypeIsKey", status._outGateTypeKey);

                setting.setSetting(prefix2 + "SurfacePort",  status._surfacePort);
                setting.setSetting(prefix2 + "SurfaceUIType", status._surfaceUIType);
                setting.setSetting(prefix2 + "SurfaceRow", status._surfaceRow);
                setting.setSetting(prefix2 + "SurfaceColumn", status._surfaceColumn);

                setting.setSetting(prefix2 + "Bind1RCH1", status._bind1RCH);
                setting.setSetting(prefix2 + "Bind1RCH2", status._bind2RCH);
                setting.setSetting(prefix2 + "Bind1RCH4", status._bind4RCH);

                setting.setSetting(prefix2 + "BindRSCTRT1", status._bindRSCTRT1);
                setting.setSetting(prefix2 + "BindRSCTRT2", status._bindRSCTRT2);
                setting.setSetting(prefix2 + "BindRSCTRT3", status._bindRSCTRT3);

                setting.setSetting(prefix2 + "BindRSCTPT1", status._bindRSCTPT1);
                setting.setSetting(prefix2 + "BindRSCTPT2", status._bindRSCTPT2);
                setting.setSetting(prefix2 + "BindRSCTPT3", status._bindRSCTPT3);
            }
            
        }
    }

    public void updateSurfaceValue(MX36Status status, int value) {
        updateSurfaceValue(status, status._surfaceValueRange.changeValue(value));
    }

    public void updateSurfaceValue(MX36Status status, MXRangedValue value) {
        if (status._surfaceValueRange.equals(value)) {
            return;
        }
        status._surfaceValueRange = value;
        updateOutputValue(status, value.changeRange(status._outValueRange._min, status._outValueRange._max));
    }

    public void updateOutputValue(MX36Status status, int value) {
        updateOutputValue(status, status._outValueRange.changeValue(value));
    }

    public void updateOutputValue(MX36Status status, MXRangedValue value) {
        if (status._outValueRange.equals(value)) {
            return;
        }
        status._outValueRange = value;
        _view._detailPanel.updateSliderByStatus();
        if (status._folder != null) {
            status._folder.refill(status);
        }
        raiseSignal(status);
        _view.refreshList();
    }

    public void raiseSignal(MX36Status status) {
        MXMessage message = status.createOutMessage();
        if (message == null) {
            return;
        }
        sendToNext(message);
    }

    public void moveFolder(MX36Folder folder, MX36Status status) {
        if (status._folder != null) {
            status._folder.remove(status._view);
        }
        folder.insertSorted(status);
    }
    
    public boolean isSameAsTable(MXRangedValue range, MXWrapList<Integer> table) {
        int rangeCount = range._max - range._min + 1;
        int tableCount = table.size();
        if (rangeCount == tableCount) {
            for (int i = range._min; i <= range._max; ++ i) {
                String name = String.valueOf(i);
                Integer value = table.valueOfName(name);
                
                if (String.valueOf(value).equals(name) == false) {
                    return false;
                }
            }
            return true;
        }
        else {        
            return false;
        }
    }
}
