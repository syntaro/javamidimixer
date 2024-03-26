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
import java.util.logging.Level;
import javax.swing.JPanel;
import jp.synthtarou.midimixer.libs.common.MXLogger2;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.namedvalue.MNamedValue;
import jp.synthtarou.midimixer.libs.namedvalue.MNamedValueList;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingNode;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.mx30surface.MGStatus;
import jp.synthtarou.midimixer.mx30surface.MX32MixerProcess;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36Process extends MXReceiver<MX36View> implements MXSettingTarget {

    MX36View _view;
    MXSetting _setting;
    MX36FolderList _folders;

    public MX36Process() {
        _setting = new MXSetting("CCMapping");
        _setting.setTarget(this);
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
    public MXSetting getSettings() {
        return _setting;
    }

    @Override
    public void prepareSettingFields() {
        _setting.register("Folder[].Name");
        _setting.register("Folder[].Selected");
        _setting.register("Folder[].Status[].Name");

        _setting.register("Folder[].Status[].Memo");
        _setting.register("Folder[].Status[].DataText");
        _setting.register("Folder[].Status[].Port");
        _setting.register("Folder[].Status[].Channel");
        _setting.register("Folder[].Status[].ValueRange");
        _setting.register("Folder[].Status[].ValueOffset");
        _setting.register("Folder[].Status[].ValueTable[]");
        _setting.register("Folder[].Status[].GateRange");
        _setting.register("Folder[].Status[].GateOffset");
        _setting.register("Folder[].Status[].GateTable[]");
        _setting.register("Folder[].Status[].GateTypeIsKey");

        _setting.register("Folder[].Status[].SurfacePort");
        _setting.register("Folder[].Status[].SurfaceUIType");
        _setting.register("Folder[].Status[].SurfaceRow");
        _setting.register("Folder[].Status[].SurfaceColumn");

        _setting.register("Folder[].Status[].Bind1RCH1");
        _setting.register("Folder[].Status[].Bind1RCH2");
        _setting.register("Folder[].Status[].Bind1RCH4");

        _setting.register("Folder[].Status[].BindRSCTRT1");
        _setting.register("Folder[].Status[].BindRSCTRT2");
        _setting.register("Folder[].Status[].BindRSCTRT3");

        _setting.register("Folder[].Status[].BindRSCTPT1");
        _setting.register("Folder[].Status[].BindRSCTPT2");
        _setting.register("Folder[].Status[].BindRSCTPT3");
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
                MXLogger2.getLogger(MX36Process.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return null;
    }

    @Override
    public void afterReadSettingFile() {
        List<MXSettingNode> listFolder = _setting.findByPath("Folder[]");
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
                    MNamedValueList<Integer> parsedValueTable = new MNamedValueList<>();
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
                    MNamedValueList<Integer> parsedGateTable = new MNamedValueList<>();
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
    public void beforeWriteSettingFile() {
        int folderN = 0;

        _setting.clearValue();
        for (MX36Folder folder : _folders._listFolder) {
            String prefix = "Folder[" + folderN + "].";
            folderN++;

            _setting.setSetting(prefix + "Name", folder._folderName);
            _setting.setSetting(prefix + "Selected", folder.isSelected());

            if (folder == this._folders._nosaveFolder) {
                continue;
            }

            for (int statusN = 0; statusN < folder._accordion.getElementCount(); ++ statusN) {
                MX36StatusPanel  panel  = (MX36StatusPanel) folder._accordion.getElementAt(statusN);
                MX36Status status = panel._status;
                String prefix2 = prefix + "Status[" + statusN + "].";
                
                _setting.setSetting(prefix2 + "Name", status._outName);
                _setting.setSetting(prefix2 + "Memo", status._outMemo);
                _setting.setSetting(prefix2 + "DataText", status.getOutDataText());
                _setting.setSetting(prefix2 + "Port", status._outPort);
                _setting.setSetting(prefix2 + "Channel", status._outChannel);
                _setting.setSetting(prefix2 + "ValueRange", rangeToString(status._outValueRange));
                _setting.setSetting(prefix2 + "ValueOffset", status._outValueOffset);
                if (status._outValueTable != null) {
                    if (!isSameRangeAndTable(status._outValueRange, status._outValueTable)) {
                        for (MNamedValue<Integer> value : status._outValueTable) {
                            _setting.setSetting(prefix2 + "ValueTable[" + value._value + "]", value._value);
                        }
                    }
                }
                _setting.setSetting(prefix2 + "GateRange", rangeToString(status._outGateRange));
                _setting.setSetting(prefix2 + "GateOffset", status._outGateOffset);
                if (status._outGateTable != null) {
                    if (!isSameRangeAndTable(status._outGateRange, status._outGateTable)) {
                        for (MNamedValue<Integer> gate : status._outGateTable) {
                            _setting.setSetting(prefix2 + "GateTable[" + gate._value + "]", gate._value);
                        }
                    }
                }
                _setting.setSetting(prefix2 + "GateTypeIsKey", status._outGateTypeKey);

                _setting.setSetting(prefix2 + "SurfacePort", status._surfacePort);
                _setting.setSetting(prefix2 + "SurfaceUIType", status._surfaceUIType);
                _setting.setSetting(prefix2 + "SurfaceRow", status._surfaceRow);
                _setting.setSetting(prefix2 + "SurfaceColumn", status._surfaceColumn);

                _setting.setSetting(prefix2 + "Bind1RCH1", status._bind1RCH);
                _setting.setSetting(prefix2 + "Bind1RCH2", status._bind2RCH);
                _setting.setSetting(prefix2 + "Bind1RCH4", status._bind4RCH);

                _setting.setSetting(prefix2 + "BindRSCTRT1", status._bindRSCTRT1);
                _setting.setSetting(prefix2 + "BindRSCTRT2", status._bindRSCTRT2);
                _setting.setSetting(prefix2 + "BindRSCTRT3", status._bindRSCTRT3);

                _setting.setSetting(prefix2 + "BindRSCTPT1", status._bindRSCTPT1);
                _setting.setSetting(prefix2 + "BindRSCTPT2", status._bindRSCTPT2);
                _setting.setSetting(prefix2 + "BindRSCTPT3", status._bindRSCTPT3);
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

    public boolean isSameRangeAndTable(MXRangedValue range, MNamedValueList<Integer> table) {
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
}
