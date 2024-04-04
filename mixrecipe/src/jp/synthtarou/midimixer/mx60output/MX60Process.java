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
package jp.synthtarou.midimixer.mx60output;

import java.io.File;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.midimixer.mx10input.MX10ViewData;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.json.MXJsonValue;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX60Process extends MXReceiver<MX60View> implements MXINIFileSupport, MXJsonSupport {
    MX60View _view;
    MX60ViewData _viewData;

    public MX60Process() {
        _viewData = new MX60ViewData(this);
        _view = new MX60View();
    }
    
    @Override
    public boolean isUsingThisRecipe() {
        return _viewData._isUsingThieRecipe;
    }

    @Override
    public void setUsingThisRecipe(boolean flag) {
        _viewData._isUsingThieRecipe = flag;
    }

    @Override
    public void processMXMessage(MXMessage message) {
        if (isUsingThisRecipe() && _viewData.isMessageForSkip(message)) {
            return;
        }

        if (_viewData.isRecording()) {
            _viewData.record(message);
            _view.setSongLengthDX(_viewData._recordingTrack, _viewData.getSongLength(_viewData._recordingTrack));
        }

        sendToNext(message);
    }

    @Override
    public String getReceiverName() {
        return "Output Dispatcher";
    }

    @Override
    public MX60View getReceiverView() {
        return _view;
    }

    @Override
    public boolean readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        if (setting.readINIFile() == false) {
            return false;
        }
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            StringBuffer str = new StringBuffer();
            for (int j = 0; j <_viewData.countOfTypes(); ++ j) {
                String name = _viewData._typeNames[j];
                boolean set = setting.getSettingAsBoolean(prefix + name, false);
                _viewData.setSkip(port, j, set);
            }
        }
        _viewData.loadSequenceData();
        _viewData._isUsingThieRecipe = true;
        _view.setViewData(_viewData);
        return true;
    }

    @Override
    public boolean writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        _viewData.saveSequenceData();
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            StringBuffer str = new StringBuffer();
            for (int j = 0; j <_viewData.countOfTypes(); ++ j) {
                boolean set = _viewData.isSkip(port, j);
                String name = _viewData._typeNames[j];
                setting.setSetting(prefix + name, set);
            }
        }
        return setting.writeINIFile();
    }

    @Override
    public MXINIFile prepareINIFile(File custom) {
        if (custom == null) {
            custom = MXINIFile.pathOf("OutputSkip");
        }
        MXINIFile setting = new MXINIFile(custom, this);
        String prefix = "Setting[].";
        for (String text : MX10ViewData._typeNames) {
            setting.register(prefix + text);
        }
        return setting;
    }

    @Override
    public boolean readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("OutputSkip");
            MXJsonParser.setAutosave(this);
        }
        MXJsonValue value = new MXJsonParser(custom).parseFile();
        if (value == null) {
            return false;
        }
        
        MXJsonValue.HelperForStructure root = value.new HelperForStructure();
        MXJsonValue.HelperForArray arraySetting = root.getFollowingArray("ListPort");
        
        if (arraySetting != null) {
            for (int i = 0; i < arraySetting.count(); ++ i) {
                MXJsonValue.HelperForStructure setting = arraySetting.getFollowingStructure(i);

                int port = setting.getFollowingInt("Port", -1);
                if (port < 0) {
                    continue;
                }
                MXJsonValue.HelperForArray types = setting.getFollowingArray("Skip");
                if (types != null) {
                    for (int j = 0; j < types.count(); ++ j) {
                        String type = types.getFollowingValue(j).getLabelUnscaped();
                        int typeN = MX10ViewData.typeOfName(type);
                        if (typeN >= 0) {
                            _viewData.setSkip(port, typeN, true);
                        }
                    }
                }
            }
        }
        _viewData.loadSequenceData();
        _view.setViewData(_viewData);
        return true;
    }

    @Override
    public boolean writeJsonFile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("OutputSkip");
        }
        MXJsonValue value = new MXJsonValue(null);
        MXJsonValue.HelperForStructure root = value.new HelperForStructure();
        MXJsonValue.HelperForArray arraySetting = root.addFollowingArray("ListPort");
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            MXJsonValue.HelperForStructure setting = arraySetting.addFollowingStructure();
            setting.addFollowingNumber("Port", port);
            MXJsonValue.HelperForArray arrayTypes = setting.addFollowingArray("Skip");
            
            for (int j = 0; j <_viewData.countOfTypes(); ++ j) {
                boolean set = _viewData.isSkip(port, j);
                if (set) {
                    String name = _viewData._typeNames[j];
                    arrayTypes.addFollowingText(name);
                } 
            }
        }

        MXJsonParser parser = new MXJsonParser(custom);
        parser.setRoot(value);
        return parser.writeFile();
    }

    @Override
    public void resetSetting() {
        _viewData.resetSkip();
        _view.setViewData(_viewData);
    }
}
