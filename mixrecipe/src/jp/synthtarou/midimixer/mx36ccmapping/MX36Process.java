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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import jp.synthtarou.libs.MXFileLogger;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.namedobject.MXNamedObject;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.json.MXJsonValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.libs.inifile.MXINIFileNode;
import jp.synthtarou.midimixer.mx30surface.MGStatus;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonFile;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36Process extends MXReceiver<MX36View> implements MXINIFileSupport, MXJsonSupport  {

    MX36View _view;
    MX36FolderList _folders;

    public MX36Process() {
        _folders = new MX36FolderList(this);
        _view = new MX36View(this, _folders);
    }

    @Override
    public String getReceiverName() {
        return "CCMapping";
    }

    @Override
    public MX36View getReceiverView() {
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
                boolean hit = false;
                for (MX36Folder folder : _folders._listFolder) {
                    for (int i = 0; i < folder._accordion.getElementCount(); ++ i) {
                        MX36StatusPanel panel = (MX36StatusPanel)folder._accordion.getElementAt(i);
                        if (panel._status._surfacePort == status._port
                         && panel._status._surfaceUIType == status._uiType
                         && panel._status._surfaceRow == status._row
                         && panel._status._surfaceColumn == status._column) {
                            hit = true;
                            MX36Status status2 = panel._status;
                            MX36Folder folder2 = status2._folder;
                            if (folder2 == _folders._trashedFolder) {
                                continue;
                            }
                            if (folder2.isSelected()) {
                                if (status2.getOutDataText() != null) {
                                    MXMessage msg = updateSurfaceValue(status2, status.getValue());
                                    if (msg != null) {
                                        sendToNext(msg);
                                        folder2.repaintStatus(status2);
                                    }
                                    done = true;
                                }
                            }
                        }
                    }
                }
                if (!hit) {
                    _folders._nosaveFolder.addCCItem(MX36Status.fromMGStatus(status));
                }
            }
        }
        if (!done) {
            sendToNext(message);
        }
    }

    @Override
    public MXINIFile prepareINIFile(File custom) {
        if (custom == null) {
            custom = MXINIFile.pathOf("CCMapping");
        }
        MXINIFile setting = new MXINIFile(custom, this);
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
        return setting;
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
            } catch (Exception ex) {
                MXFileLogger.getLogger(MX36Process.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return null;
    }

    @Override
    public void readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        setting.readINIFile();
        List<MXINIFileNode> listFolder = setting.findByPath("Folder[]");
        for (MXINIFileNode folderNode : listFolder) {
            String folderName = folderNode.getSetting("Name");
            MX36Folder folder = _folders.newFolder(folderName);
            
            folder.setSelected(folderNode.getSettingAsBoolean("Selected", true));

            MXINIFileNode statusNode = folderNode.findNode("Status");

            if (statusNode != null) {
                List<MXINIFileNode> numbers = statusNode.findNumbers();
                for (MXINIFileNode props : numbers) {
                    MX36Status status = new MX36Status();

                    status._outName = props.getSetting("Name");
                    status._outMemo = props.getSetting("Memo");
                    status.setOutDataText(props.getSetting("DataText"));
                    status._outPort = props.getSettingAsInt("Port", -1);
                    status._outChannel = props.getSettingAsInt("Channel", 0);
                    status._outValueRange = stringToRange(props.getSetting("ValueRange"));
                    status._outValueOffset = props.getSettingAsInt("ValueOffset", 0);

                    MXINIFileNode valueTable = props.findNode("ValueTable");
                    MXNamedObjectList<Integer> parsedValueTable = new MXNamedObjectList<>();
                    if (valueTable != null) {
                        List<MXINIFileNode> listValue = valueTable.findNumbers();
                        if (listValue != null) {
                            for (MXINIFileNode valueNode : listValue) {
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
                    MXINIFileNode gateTable = props.findNode("GateTable");
                    MXNamedObjectList<Integer> parsedGateTable = new MXNamedObjectList<>();
                    if (gateTable != null) {
                        List<MXINIFileNode> listGate = gateTable.findNumbers();
                        if (listGate != null) {
                            for (MXINIFileNode gateNode : listGate) {
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
    public void writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        int folderN = 0;

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
                        for (MXNamedObject<Integer> value : status._outValueTable) {
                            setting.setSetting(prefix2 + "ValueTable[" + value._value + "]", value._value);
                        }
                    }
                }
                setting.setSetting(prefix2 + "GateRange", rangeToString(status._outGateRange));
                setting.setSetting(prefix2 + "GateOffset", status._outGateOffset);
                if (status._outGateTable != null) {
                    if (!isSameRangeAndTable(status._outGateRange, status._outGateTable)) {
                        for (MXNamedObject<Integer> gate : status._outGateTable) {
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
        setting.writeINIFile();
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
            status._folder.repaintStatus(status);
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

    public boolean isSameRangeAndTable(MXRangedValue range, MXNamedObjectList<Integer> table) {
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

    @Override
    public void readJSonfile(File custom) {
        MXJsonFile file = new MXJsonFile(custom);
        MXJsonValue value = file.readJsonFile();
        if (value == null) {
            value = new MXJsonValue(null);
        }
        //TODO
    }

    @Override
    public void writeJsonFile(File custom) {
        MXJsonValue value = new MXJsonValue(null);
        
        MXJsonFile file = new MXJsonFile(custom);
        file.writeJsonFile(value);
    }
}
