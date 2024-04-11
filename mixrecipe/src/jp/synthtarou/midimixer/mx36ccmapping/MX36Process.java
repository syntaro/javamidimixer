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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import jp.synthtarou.libs.log.MXFileLogger;
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
import jp.synthtarou.libs.json.MXJsonParser;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36Process extends MXReceiver<MX36View> implements MXINIFileSupport, MXJsonSupport {

    MX36View _view;
    MX36FolderList _folderList;

    public MX36Process() {
        _folderList = new MX36FolderList(this);
        _view = new MX36View(this, _folderList);
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
        System.out.println("36->input->" + message);
        sendToNext(message);
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
    public boolean readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        if (!setting.readINIFile()) {
            return false;
        }
        List<MXINIFileNode> listFolder = setting.findByPath("Folder[]");
        for (MXINIFileNode folderNode : listFolder) {
            String folderName = folderNode.getSetting("Name");
            MX36Folder folder = _folderList.newFolder(folderName);

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
                    status._outGateTypeKey = props.getSettingAsBoolean("GateTypeIsKey", false);

                    int port = props.getSettingAsInt("SurfacePort", 0);
                    int type = props.getSettingAsInt("SurfaceUIType", 0);
                    int row = props.getSettingAsInt("SurfaceRow", 0);
                    int column = props.getSettingAsInt("SurfaceColumn", 0);
                    status.setSurface(port, type, row, column);
                    status._surfaceReplace = props.getSettingAsBoolean("SurfaceReplace", true);

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
        return true;
    }

    @Override
    public boolean writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        int folderN = 0;

        for (MX36Folder folder : _folderList._listFolder) {
            String prefix = "Folder[" + folderN + "].";
            folderN++;

            setting.setSetting(prefix + "Name", folder._folderName);
            setting.setSetting(prefix + "Selected", folder.isSelected());

            if (folder == this._folderList._autodetectFolder) {
                continue;
            }

            for (int statusN = 0; statusN < folder._accordion.getElementCount(); ++statusN) {
                MX36StatusPanel panel = (MX36StatusPanel) folder._accordion.getElementAt(statusN);
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

                setting.setSetting(prefix2 + "SurfacePort", status.getSurfacePort());
                setting.setSetting(prefix2 + "SurfaceUIType", status.getSurfaceUIType());
                setting.setSetting(prefix2 + "SurfaceRow", status.getSurfaceRow());
                setting.setSetting(prefix2 + "SurfaceColumn", status.getSurfaceColumn());
                setting.setSetting(prefix2 + "SurfaceReplace", status._surfaceReplace);

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
        return setting.writeINIFile();
    }

    MXMessage updateSurfaceValue(MX36Status status, int value) {
        return updateSurfaceValue(status, status._surfaceValueRange.changeValue(value));
    }

    MXMessage updateSurfaceValue(MX36Status status, MXRangedValue value) {
        if (status._surfaceValueRange.equals(value)) {
            return null;
        }
        if (status._folder.isSelected()) {
            return updateOutputValue(status, value.changeRange(status._outValueRange._min, status._outValueRange._max));
        }
        return null;
    }

    MXMessage updateOutputValue(MX36Status status, int value) {
        return updateOutputValue(status, status._outValueRange.changeValue(value));
    }

    MXMessage updateOutputValue(MX36Status status, MXRangedValue value) {
        if (status._outValueRange.equals(value)) {
            return null;
        }
        if (status._folder.isSelected() == false) {
            return null;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (_view._detailPanel._status == status) {
                    _view._detailPanel.repaintDetailSliderStatus();
                }
                if (status._folder != null) {
                    status._folder.repaintStatus(status);
                }
                //_view.tabActivated();
            }
        });
        status._outValueRange = value;
        return status.createOutMessage();
    }

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
        if (_folderList._trashedFolder._accordion.getElementCount() != 0) {
            return true;
        }
        return false;
    }

    public void cleanupTrash() {
        _folderList._trashedFolder.removeAll();
    }

    public boolean haveEmptyFolder() {
        for (MX36Folder seek : _folderList._listFolder) {
            if (seek._accordion.getElementCount() == 0 && seek._folderName.startsWith("*") == false) {
                return true;
            }
        }
        return false;
    }

    public void cleanupEmptyFolder() {
        List<MX36Folder> listEmpty = new ArrayList<>();
        for (MX36Folder seek : _folderList._listFolder) {
            if (seek._accordion.getElementCount() == 0 && seek._folderName.startsWith("*") == false) {
                listEmpty.add(seek);
            }
        }
        for (MX36Folder seek : listEmpty) {
            _folderList._listFolder.remove(seek);
        }
    }

    public void ownwerTabChanged() {
        //_view._detailPanel.showupStatus();
    }

    @Override
    public boolean readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("CCMapping");
            MXJsonParser.setAutosave(this);
        }
        MXJsonValue value = new MXJsonParser(custom).parseFile();
        if (value == null) {
            return false;
        }

        MXJsonValue.HelperForStructure root = value.new HelperForStructure();
        //TODO
        MXJsonValue.HelperForArray listFolder = root.getFollowingArray("Folder");
        for (int i = 0; i < listFolder.count(); ++i) {
            MXJsonValue.HelperForStructure folderNode = listFolder.getFollowingStructure(i);
            String folderName = folderNode.getFollowingText("Name", "");
            MX36Folder folder = _folderList.newFolder(folderName);

            folder.setSelected(folderNode.getFollowingBool("Selected", true));

            MXJsonValue.HelperForArray statusArray = folderNode.getFollowingArray("Status");

            if (statusArray != null) {
                for (int j = 0; j < statusArray.count(); ++j) {
                    MXJsonValue.HelperForStructure statusNode = statusArray.getFollowingStructure(j);
                    MX36Status status = new MX36Status();

                    status._outName = statusNode.getFollowingText("Name", "");
                    status._outMemo = statusNode.getFollowingText("Memo", "");
                    status.setOutDataText(statusNode.getFollowingText("DataText", ""));
                    status._outPort = statusNode.getFollowingInt("Port", -1);
                    status._outChannel = statusNode.getFollowingInt("Channel", 0);
                    status._outValueRange = stringToRange(statusNode.getFollowingText("ValueRange", "0, 0, 127"));
                    status._outValueOffset = statusNode.getFollowingInt("ValueOffset", 0);

                    MXJsonValue.HelperForArray valueArray = statusNode.getFollowingArray("ValueTable");
                    MXNamedObjectList<Integer> parsedValueTable = new MXNamedObjectList<>();
                    if (valueArray != null) {
                        for (int k = 0; k < valueArray.count(); ++k) {
                            MXJsonValue seek = valueArray.getFollowingValue(k);
                            int var = seek.getLabelNumber().intValue();
                            String name = seek.getContentsAt(0).getLabelUnscaped();
                            parsedValueTable.addNameAndValue(name, var);
                        }
                    }
                    if (parsedValueTable.size() == 0) {
                        parsedValueTable = null;
                    }
                    status._outValueTable = parsedValueTable;

                    status._outGateRange = stringToRange(statusNode.getFollowingText("GateRange", "0, 0, 127"));
                    status._outGateOffset = statusNode.getFollowingInt("GateOffset", 0);

                    MXJsonValue.HelperForArray gateArray = statusNode.getFollowingArray("GateTable");
                    MXNamedObjectList<Integer> parsedGateTable = new MXNamedObjectList<>();
                    if (valueArray != null) {
                        for (int k = 0; k < gateArray.count(); ++k) {
                            MXJsonValue seek = gateArray.getFollowingValue(k);
                            try {
                                int var = seek.getLabelNumber().intValue();
                                String name = seek.getContentsAt(0).getLabelUnscaped();
                                parsedValueTable.addNameAndValue(name, var);
                            } catch (NumberFormatException ex) {
                                MXFileLogger.getLogger(MX36Process.class).log(Level.SEVERE, ex.getMessage(), ex);
                            }
                        }
                    }

                    if (parsedGateTable.size() == 0) {
                        parsedGateTable = null;
                    }
                    status._outGateTable = parsedGateTable;
                    status._outGateTypeKey = statusNode.getFollowingBool("GateTypeIsKey", false);

                    int port = statusNode.getFollowingInt("SurfacePort", 0);
                    int type = statusNode.getFollowingInt("SurfaceUIType", 0);
                    int row = statusNode.getFollowingInt("SurfaceRow", 0);
                    int column = statusNode.getFollowingInt("SurfaceColumn", 0);
                    status._surfaceReplace = statusNode.getFollowingBool("ReplaceOriginal", true);
                    
                    status._bind1RCH = statusNode.getFollowingInt("Bind1RCH1", 0);
                    status._bind2RCH = statusNode.getFollowingInt("Bind1RCH2", 0);
                    status._bind4RCH = statusNode.getFollowingInt("Bind1RCH4", 0);

                    status._bindRSCTRT1 = statusNode.getFollowingInt("BindRSCTRT1", 0);
                    status._bindRSCTRT2 = statusNode.getFollowingInt("BindRSCTRT2", 0);
                    status._bindRSCTRT3 = statusNode.getFollowingInt("BindRSCTRT3", 0);

                    status._bindRSCTPT1 = statusNode.getFollowingInt("BindRSCTPT1", 0);
                    status._bindRSCTPT2 = statusNode.getFollowingInt("BindRSCTPT2", 0);
                    status._bindRSCTPT3 = statusNode.getFollowingInt("BindRSCTPT3", 0);

                    folder.addCCItem(status);
                }
                folder.sortElements();
            }
        }
        return true;
    }

    @Override
    public boolean writeJsonFile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("CCMapping");
        }
        MXJsonValue value = new MXJsonValue(null);
        MXJsonParser parser = new MXJsonParser(custom);
        MXJsonValue.HelperForStructure  root = parser.getRoot().new HelperForStructure();
        MXJsonValue.HelperForArray folderList = root.addFollowingArray("Folder");

        for (MX36Folder folder : _folderList._listFolder) {
            MXJsonValue.HelperForStructure setting = folderList.addFollowingStructure();

            setting.setFollowingText("Name", folder._folderName);
            setting.setFollowingBool("Selected", folder.isSelected());

            if (folder == this._folderList._autodetectFolder) {
                continue;
            }

            MXJsonValue.HelperForArray statusList = setting.addFollowingArray("Status");
            
            for (int statusN = 0; statusN < folder._accordion.getElementCount(); ++statusN) {
                MX36StatusPanel panel = (MX36StatusPanel) folder._accordion.getElementAt(statusN);
                MX36Status status = panel._status;
                MXJsonValue.HelperForStructure node = statusList.addFollowingStructure();

                node.setFollowingText("Name", status._outName);
                node.setFollowingText("Memo", status._outMemo);
                node.setFollowingText("DataText", status.getOutDataText());
                node.setFollowingInt("Port", status._outPort);
                node.setFollowingInt("Channel", status._outChannel);
                node.setFollowingText("ValueRange", rangeToString(status._outValueRange));
                node.setFollowingInt("ValueOffset", status._outValueOffset);
                if (status._outValueTable != null) {
                    MXJsonValue.HelperForArray valueTable = node.addFollowingArray("ValueTable");
                    if (!isSameRangeAndTable(status._outValueRange, status._outValueTable)) {
                        for (MXNamedObject<Integer> seek : status._outValueTable) {
                            MXJsonValue adding = new MXJsonValue(null);
                            adding.setLabelAndText(seek._value, status._outValueTable.nameOfValue(seek._value));
                            valueTable.toJsonValue().addToContentsArray(adding);
                        }
                    }
                }
                node.setFollowingText("GateRange", rangeToString(status._outGateRange));
                node.setFollowingInt("GateOffset", status._outGateOffset);
                if (status._outGateTable != null) {
                    MXJsonValue.HelperForArray gateTable = node.addFollowingArray("GateTable");
                    if (!isSameRangeAndTable(status._outGateRange, status._outGateTable)) {
                        for (MXNamedObject<Integer> seek : status._outGateTable) {
                            MXJsonValue adding = new MXJsonValue(null);
                            adding.setLabelAndText(seek._value, status._outValueTable.nameOfValue(seek._value));
                            gateTable.toJsonValue().addToContentsArray(adding);
                        }
                    }
                }
                node.setFollowingBool("GateTypeIsKey", status._outGateTypeKey);

                node.setFollowingInt("SurfacePort", status.getSurfacePort());
                node.setFollowingInt("SurfaceUIType", status.getSurfaceUIType());
                node.setFollowingInt("SurfaceRow", status.getSurfaceRow());
                node.setFollowingInt("SurfaceColumn", status.getSurfaceColumn());
                node.setFollowingBool("SurfaceReplace", status._surfaceReplace);

                node.setFollowingInt("Bind1RCH1", status._bind1RCH);
                node.setFollowingInt("Bind1RCH2", status._bind2RCH);
                node.setFollowingInt("Bind1RCH4", status._bind4RCH);

                node.setFollowingInt("BindRSCTRT1", status._bindRSCTRT1);
                node.setFollowingInt("BindRSCTRT2", status._bindRSCTRT2);
                node.setFollowingInt("BindRSCTRT3", status._bindRSCTRT3);

                node.setFollowingInt("BindRSCTPT1", status._bindRSCTPT1);
                node.setFollowingInt("BindRSCTPT2", status._bindRSCTPT2);
                node.setFollowingInt("BindRSCTPT3", status._bindRSCTPT3);
            }

        }
        return parser.writeFile();
    }

    @Override
    public void resetSetting() {
    }
    
    public boolean isNotLinked(MGStatus status) {
        return status.getLinked36() == null;
    }
    
    public void addToAutoDetected(MGStatus status) {
        if (status._uiType != MGStatus.TYPE_DRUMPAD) {           
            for (MX36Folder seek : _folderList._listFolder) {
                if (seek.isAlreadyHave(status)) {
                    return;
                }            
            }
            
            MX36Status status36 = MX36Status.fromMGStatus(status);
            _folderList._autodetectFolder.addCCItem(status36);
        }
    }

    public boolean invokeMapping(MGStatus status) {
        LinkedList<MX36Status> list = status.getLinked36();
        if (status._uiType == MGStatus.TYPE_DRUMPAD) {
            return false;
        }
        boolean did = false;

        for (MX36Status seek : list) {
            MX36Folder folder = seek._folder;
            if (folder == null || folder == _folderList._trashedFolder) {
                continue;
            }
            if (folder == _folderList._autodetectFolder) {
                if (seek.getOutDataText() == null
                  ||seek.getOutDataText().isBlank()) {
                    continue;
                }
            }
            if (folder.isSelected()) {
                MXMessage msg = updateSurfaceValue(seek, status.getValue());
                if (msg != null) {
                    sendToNext(msg);
                    folder.repaintStatus(seek);
                }
                did = true;
            }
        }
        return did;
    }
}
