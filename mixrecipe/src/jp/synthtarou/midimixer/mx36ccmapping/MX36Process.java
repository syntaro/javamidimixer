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
import jp.synthtarou.midimixer.libs.wraplist.MXWrap;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
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
    MX36FolderList _folders;

    public MX36Process() {
        _setting = new MXSetting("CCMapping");
        _setting.setTarget(this);
        _folders = new MX36FolderList(this);
        _view = new MX36View(this, _folders);
    }

    public void readSettings() {
        _setting.readSettingFile();
    }

    @Override
    public String getReceiverName() {
        return "CCMapping";
    }

    @Override
    public JPanel getReceiverView() {
        return _view;
    }

    @Override
    public void processMXMessage(MXMessage message) {
        MGStatus[] result = message._mx30record;
        boolean done = false;

        if (result != null && result.length > 0) {
            for (MGStatus status : result) {
                if (status._uiType == MGStatus.TYPE_DRUMPAD) {
                    continue;
                }
                List<MX36StatusPanel> indexed = indexedSearch(status);
                if (indexed == null) {
                    MX36Folder folder2 = _folders._nosaveFolder;
                    MX36Status added = MX36Status.fromMGStatus(folder2, status);
                    folder2.addCCItem(added);
                    folder2.refill(added);
                    _index.safeAdd(added._panel);
                    //メッセージがアサインされてないので不要
                    //updateSurfaceValue(status2, status.getValue());
                } else {
                    for (MX36StatusPanel seek : indexed) {
                        MX36Status status2 = seek._status;
                        MX36Folder folder2 = status2._folder;
                        if (folder2 == _folders._trashedFolder) {
                            continue;
                        }
                        if (folder2.isSelected()) {
                            MXMessage msg = updateSurfaceValue(status2, status.getValue());
                            if (msg != null) {
                                sendToNext(msg);
                                folder2.refill(status2);
                            }
                            done = true;
                        }
                    }
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
        setting.register("Folder[].Selected");
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
        return value._value + "," + value._min + "," + value._max;
    }

    static MXRangedValue stringToRange(String text) {
        ArrayList<String> split = new ArrayList<>();
        MXUtil.split(text, split, ',');
        if (split.size() >= 3) {
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
            } catch (Exception e) {
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
            MX36Folder folder = _folders.newFolder(folderName);
            
            folder.setSelected(folderNode.getSettingAsBoolean("Selected", true));

            MXSettingNode statusNode = folderNode.findNode("Status");

            if (statusNode != null) {
                List<MXSettingNode> numbers = statusNode.findNumbers();
                for (MXSettingNode props : numbers) {
                    MX36Status status = new MX36Status();

                    status._outName = props.getSetting("Name");
                    status._outMemo = props.getSetting("Memo");
                    status.setOutDataText(props.getSetting("DataText"));
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
                                } catch (Exception e) {
                                    continue;
                                }
                            }
                        }
                    }
                    if (parsedValueTable.size() == 0) {
                        parsedValueTable = null;
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
                                } catch (Exception e) {
                                    continue;
                                }
                            }
                        }
                    }
                    if (parsedGateTable.size() == 0) {
                        parsedGateTable = null;
                    }
                    status._outGateTable = parsedGateTable;
                    status._outGateTypeKey = props.getSettingAsBoolean(props.getSetting("GateTypeIsKey"), false);

                    status._surfacePort = props.getSettingAsInt("SurfacePort", 0);
                    status._surfaceUIType = props.getSettingAsInt("SurfaceUIType", 0);
                    status._surfaceRow = props.getSettingAsInt("SurfaceRow", 0);
                    status._surfaceColumn = props.getSettingAsInt("SurfaceColumn", 0);

                    status._bind1RCH = props.getSettingAsInt("Bind1RCH1", 0);
                    status._bind2RCH = props.getSettingAsInt("Bind1RCH2", 0);
                    status._bind4RCH = props.getSettingAsInt("Bind1RCH4", 0);

                    status._bindRSCTRT1 = props.getSettingAsInt("BindRSCTRT1", 0);
                    status._bindRSCTRT2 = props.getSettingAsInt("BindRSCTRT2", 0);
                    status._bindRSCTRT3 = props.getSettingAsInt("BindRSCTRT3", 0);

                    status._bindRSCTPT1 = props.getSettingAsInt("BindRSCTPT1", 0);
                    status._bindRSCTPT2 = props.getSettingAsInt("BindRSCTPT2", 0);
                    status._bindRSCTPT3 = props.getSettingAsInt("BindRSCTPT3", 0);

                    folder.addCCItem(status);
                }
                folder.sortElements();
            }
        }
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        int folderN = 0;

        setting.clearValue();
        for (MX36Folder folder : _folders._listFolder) {
            String prefix = "Folder[" + folderN + "].";
            folderN++;

            setting.setSetting(prefix + "Name", folder._folderName);
            setting.setSetting(prefix + "Selected", folder.isSelected());

            if (folder == this._folders._nosaveFolder) {
                continue;
            }

            for (int statusN = 0; statusN < folder._accordion.getElementCount(); ++ statusN) {
                MX36StatusPanel  panel  = (MX36StatusPanel) folder._accordion.getElementAt(statusN);
                MX36Status status = panel._status;
                String prefix2 = prefix + "Status[" + statusN + "].";
                
                setting.setSetting(prefix2 + "Name", status._outName);
                setting.setSetting(prefix2 + "Memo", status._outMemo);
                setting.setSetting(prefix2 + "DataText", status.getOutDataText());
                setting.setSetting(prefix2 + "Port", status._outPort);
                setting.setSetting(prefix2 + "Channel", status._outChannel);
                setting.setSetting(prefix2 + "ValueRange", rangeToString(status._outValueRange));
                setting.setSetting(prefix2 + "ValueOffset", status._outValueOffset);
                if (status._outValueTable != null) {
                    if (!isSameRangeAndTable(status._outValueRange, status._outValueTable)) {
                        for (MXWrap<Integer> value : status._outValueTable) {
                            setting.setSetting(prefix2 + "ValueTable[" + value._value + "]", value._value);
                        }
                    }
                }
                setting.setSetting(prefix2 + "GateRange", rangeToString(status._outGateRange));
                setting.setSetting(prefix2 + "GateOffset", status._outGateOffset);
                if (status._outGateTable != null) {
                    if (!isSameRangeAndTable(status._outGateRange, status._outGateTable)) {
                        for (MXWrap<Integer> gate : status._outGateTable) {
                            setting.setSetting(prefix2 + "GateTable[" + gate._value + "]", gate._value);
                        }
                    }
                }
                setting.setSetting(prefix2 + "GateTypeIsKey", status._outGateTypeKey);

                setting.setSetting(prefix2 + "SurfacePort", status._surfacePort);
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

    MXMessage updateSurfaceValue(MX36Status status, int value) {
        return updateSurfaceValue(status, status._surfaceValueRange.changeValue(value));
    }

    MXMessage updateSurfaceValue(MX36Status status, MXRangedValue value) {
        if (status._surfaceValueRange.equals(value)) {
            return null;
        }
        status._surfaceValueRange = value;
        return updateOutputValue(status, value.changeRange(status._outValueRange._min, status._outValueRange._max));
    }

    MXMessage updateOutputValue(MX36Status status, int value) {
        return updateOutputValue(status, status._outValueRange.changeValue(value));
    }

    MXMessage updateOutputValue(MX36Status status, MXRangedValue value) {
        if (status._outValueRange.equals(value)) {
            return null;
        }
        status._outValueRange = value;
        if (_view._detailPanel._status == status) {
            _view._detailPanel.updateSliderByStatus();
        }
        if (status._folder != null) {
            status._folder.refill(status);
        }
        _view.tabActivated();
        return status.createOutMessage();
    }

    /*
    public void raiseSignal(MX36Status status) {
        MXMessage message = status.createOutMessage();
        if (message == null) {
            return;
        }
        sendToNext(message);
    }
    */

    public void moveFolder(MX36Folder folder, MX36Status status) {
        if (status._folder != null) {
            status._folder.remove(status._panel);
        }
        folder.addCCItem(status);
    }

    public boolean isSameRangeAndTable(MXRangedValue range, MXWrapList<Integer> table) {
        int rangeCount = range._max - range._min + 1;
        int tableCount = table.size();
        if (rangeCount == tableCount) {
            for (int i = range._min; i <= range._max; ++i) {
                String name = String.valueOf(i);
                Integer value = table.valueOfName(name);

                if (String.valueOf(value).equals(name) == false) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    MX36Index _index = null;

    public ArrayList<MX36StatusPanel> indexedSearch(MGStatus status) {
        if (_index == null) {
            _index = new MX36Index();
            for (MX36Folder folder : _folders._listFolder) {
                for (int i = 0; i < folder._accordion.getElementCount(); ++ i) {
                    MX36StatusPanel panel = (MX36StatusPanel)folder._accordion.getElementAt(i);
                    _index.safeAdd(panel);
                }
            }
        }
        return _index.safeGet(status._port, status._uiType, status._row, status._column);
    }
    
    public boolean haveTrashedItem() {
        if (_folders._trashedFolder._accordion.getElementCount() != 0) {
            return true;
        }
        return false;
    }
    
    public void cleanupTrash() {
        _folders._trashedFolder.removeAll();
    }

    public boolean haveEmptyFolder() {
        for (MX36Folder seek : _folders._listFolder) {
            if (seek._accordion.getElementCount() == 0 && seek._folderName.startsWith("*") == false) {
                return true;
            }
        }
        return false;
    }
    
    public void cleanupEmptyFolder() {
        List<MX36Folder> listEmpty = new ArrayList<>();
        for (MX36Folder seek : _folders._listFolder) {
            if (seek._accordion.getElementCount() == 0 && seek._folderName.startsWith("*") == false) {
                listEmpty.add(seek);
            }
        }
        for (MX36Folder seek : listEmpty) {
            _folders._listFolder.remove(seek);
        }
    }

    public void ownwerTabChanged() {
        //_view._detailPanel.updateViewByStatus();
    }
}
