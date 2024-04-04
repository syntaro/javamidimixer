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
package jp.synthtarou.midimixer.mx50resolution;

import java.io.File;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.logging.Level;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageBag;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.json.MXJsonValue;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX50Process extends MXReceiver<MX50View> implements MXINIFileSupport, MXJsonSupport {

    MX50View _view;
    ArrayList<MXResolution> _listResolution;
    ArrayList<MXResolutionView> _listResolutionView;

    public MX50Process() {
        _listResolution = new ArrayList();
        _listResolutionView = new ArrayList();
        _view = new MX50View(this);
    }

    @Override
    public String getReceiverName() {
        return "ResolutionDown";
    }

    @Override
    public MX50View getReceiverView() {
        return _view;
    }

    public int indexOfResolution(MXResolution reso) {
        return  _listResolution.indexOf(reso);
    }

    @Override
    public void processMXMessage(MXMessage message) {
        MXMessageBag result = new MXMessageBag();
        boolean proc = false;
        for (MXResolution reso : _listResolution) {
            if (reso.controlByMessage(message, result)) {
                proc = true;
            }
        }
        if (proc) {
            while (true) {
                MXMessage seek = result.popResult();
                if (seek == null) {
                    break;
                }
                sendToNext(seek);
            }
        } else {
            sendToNext(message);
        }
    }

    @Override
    public MXINIFile prepareINIFile(File custom) {
        if (custom == null) {
            custom = MXINIFile.pathOf("ResolutionDown");
        }
        MXINIFile setting = new MXINIFile(custom, this);
        setting.register("Resolution[].Command");
        setting.register("Resolution[].Port");
        setting.register("Resolution[].Channel");
        setting.register("Resolution[].Gate");
        setting.register("Resolution[].Min");
        setting.register("Resolution[].Max");
        setting.register("Resolution[].Resolution");
        return setting;
    }

    @Override
    public boolean readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        if (!setting.readINIFile()) {
            return false;
        }
        int x = 1;
        _listResolution.clear();
        _listResolutionView.clear();

        while (true) {
            String prefix = "Resolution[" + x + "].";
            x++;
            String command = setting.getSetting(prefix + "Command");
            int port = setting.getSettingAsInt(prefix + "Port", -1);
            if (port < 0) {
                break;
            }
            int channel = setting.getSettingAsInt(prefix + "Channel", 0);
            int gate = setting.getSettingAsInt(prefix + "Gate", 0);
            //int min = setting.getSettingAsInt(prefix + "Min", -1);
            //int max = setting.getSettingAsInt(prefix + "Max", -1);
            int resolution = setting.getSettingAsInt(prefix + "Resolution", -1);

            try {
                MXTemplate template = new MXTemplate(command);
                MXRangedValue gateObj = MXRangedValue.new7bit(gate);
                //MXRangedValue value = new MXRangedValue(0, min, max);
                
                MXResolution reso = new MXResolution(this);
                reso._channel = channel;
                reso._gate = gate;
                reso._command = template;
                reso._lastSent = -1;
                reso._port = port;
                reso._resolution = resolution;
                _listResolution.add(reso);
                _listResolutionView.add(new MXResolutionView(reso));
                
            } catch (RuntimeException ex) {
                MXFileLogger.getLogger(MX50Process.class).log(Level.WARNING, ex.getMessage(), ex);
                continue;
            }
        }
        _view.reloadList();
        return true;
    }

    @Override
    public boolean writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        int x = 1;
        for (MXResolution reso : _listResolution) {
            String prefix = "Resolution[" + x + "].";
            x++;
            setting.setSetting(prefix + "Command", reso._command != null ? reso._command.toDText(): "-");
            setting.setSetting(prefix + "Port", reso._port);
            setting.setSetting(prefix + "Channel", reso._channel);
            setting.setSetting(prefix + "Gate", reso._gate);
            setting.setSetting(prefix + "Resolution", reso._resolution);
        }
        return setting.writeINIFile();
    }

    public MXResolution createNewResolution() {
        MXResolution reso = new MXResolution(this);
        MXResolutionView view = new MXResolutionView(reso);
        _listResolution.add(reso);
        _listResolutionView.add(view);
        return reso;
    }

    public void removeResolution(MXResolution reso) {
        int index = _listResolution.indexOf(reso);
        if (index >= 0) {
            _listResolution.remove(index);
            _listResolutionView.remove(index);
        }
    }

    @Override
    public boolean readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("ResolutionDown");
        }
        MXJsonValue value = new MXJsonParser(custom).parseFile();
        if (value == null) {
            return false;
        }
        MXJsonValue.HelperForStructure root = value.new HelperForStructure();
        
        MXJsonValue.HelperForArray listResolution = root.getFollowingArray("Resolution");
        if(listResolution != null) {
            for (int i = 0; i < listResolution.count(); ++ i) {
                MXJsonValue.HelperForStructure resolution = listResolution.getFollowingStructure(i);
                MXResolution reso = new MXResolution(this);
                String textComand = resolution.getFollowingText("Command", "");
                try {
                    if (textComand == null) {
                        reso._command = null;
                    }
                    else {                        
                        reso._command = textComand == null ? null : new MXTemplate(textComand);
                    }
                }catch(IllegalFormatException ex) {
                    MXFileLogger.getLogger(MX50Process.class).log(Level.WARNING, ex.getMessage(), ex);
                    continue;
                }
                reso._port = resolution.getFollowingInt("Port",  -1);
                if (reso._port < 0) {
                    continue;
                }
                reso._channel = resolution.getFollowingInt("Channel", 0);
                reso._gate = resolution.getFollowingInt("Gate", 0);
                reso._resolution = resolution.getFollowingInt("Resolution", 128);
                MXJsonValue.HelperForArray listGateTable = resolution.getFollowingArray("GateTable");
                for (int j = 0; j < listGateTable.count(); ++ j) {
                    MXJsonValue gate = listGateTable.getFollowingValue(j);
                    Number label = gate.getLabelNumber();
                    String text = value.getContentsTypeText();
                    reso._gateTable.addNameAndValue(text, label.intValue());
                }
            }
        }

        return true;
    }

    @Override
    public boolean writeJsonFile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("ResolutionDown");
        }
        MXJsonParser parser = new MXJsonParser(custom);
        MXJsonValue.HelperForStructure root = parser.getRoot().new HelperForStructure();
        
        MXJsonValue.HelperForArray listResolution = root.addFollowingArray("Resolution");

        for (MXResolution reso : _listResolution) {
            MXJsonValue.HelperForStructure resolution = listResolution.addFollowingStructure();
            resolution.setFollowingText("Command", reso._command != null ? reso._command.toDText(): "");
            resolution.setFollowingInt("Port", reso._port);
            resolution.setFollowingInt("Channel", reso._channel);
            resolution.setFollowingInt("Gate", reso._gate);
            resolution.setFollowingInt("Resolution", reso._resolution);
            MXJsonValue.HelperForArray listGateTable = resolution.addFollowingArray("GateTable");
            for (int i = 0; i < reso._gateTable.getSize(); ++ i) {
                MXJsonValue.HelperForStructure gateTable = listGateTable.addFollowingStructure();
                gateTable.setFollowingText(reso._gateTable.get(i)._value, reso._gateTable.get(i)._name);
            }
        }

        return parser.writeFile();
    }

    @Override
    public void resetSetting() {
    }
}
