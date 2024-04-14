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
package jp.synthtarou.midimixer.mx10input;

import jp.synthtarou.midimixer.libs.midi.port.MXPreprocess;
import java.io.File;
import java.util.TreeMap;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.libs.json.MXJsonValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import static jp.synthtarou.midimixer.mx10input.MX10ViewData.TYPE_ACTIVE_SENSING;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX10Process extends MXReceiver<MX10View> implements MXINIFileSupport, MXJsonSupport  {
    MX10View _view;
    MXINIFile _setting;
    MX10ViewData _viewData;

    public MX10Process() {
        _view = new MX10View();
        _viewData = new MX10ViewData();
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
        if (isUsingThisRecipe()) {
            if (_viewData.isMessageForSkip(message)) {
                return;
            }
        }
        sendToNext(message);
    }

    @Override
    public String getReceiverName() {
        return "Input Receiver";
    }

    @Override
    public MX10View getReceiverView() {
        return _view;
    }
    
    @Override
    public MXINIFile prepareINIFile(File custom) {
        if (custom == null) {
            custom = MXINIFile.pathOf("InputSkip");
        }
        MXINIFile setting = new MXINIFile(custom, this);
        String prefix = "Setting[].";
        for (String text : _viewData._typeNames) {
            setting.register(prefix + text);
        }
        return setting;
    }

    @Override
    public boolean readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        if (!setting.readINIFile()) {
            return false;
        }
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            for (int j = 0; j < _viewData.countOfTypes(); ++ j) {
                String name = _viewData._typeNames[j];
                boolean set = setting.getSettingAsBoolean(prefix + name, false);
                _viewData.setSkip(port, j, set);
            }
        }
        _viewData._isUsingThieRecipe = true;
        _view.setViewData(_viewData);
        return true;
    }

    @Override
    public boolean writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            StringBuffer str = new StringBuffer();
            for (int j = 0; j < _viewData.countOfTypes(); ++ j) {
                boolean set = _viewData.isSkip(port, j);
                if (set) {                   
                    String name = _viewData._typeNames[j];
                    setting.setSetting(prefix + name, set);
                }
            }
        }
        return setting.writeINIFile();
    }

    @Override
    public boolean readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("InputSkip");
            MXJsonParser.setAutosave(this);
        }
        MXJsonValue value = new MXJsonParser(custom).parseFile();
        if (value == null) {
            return false;
        }
        
        MXJsonValue.HelperForStructure root = value.new HelperForStructure();
        MXJsonValue.HelperForArray arraySetting = root.getFollowingArray("ListPort");
        _viewData._isUsingThieRecipe = root.getFollowingBool("useThis", true);
        _viewData.resetSkip();
        
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
        _view.setViewData(_viewData);
        return true;
    }

    @Override
    public boolean writeJsonFile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("InputSkip");
        }
        MXJsonValue value = new MXJsonValue(null);
        MXJsonValue.HelperForStructure root = value.new HelperForStructure();

        root.setFollowingBool("useThis", _viewData._isUsingThieRecipe);
        
        MXJsonValue.HelperForArray arraySetting = root.addFollowingArray("ListPort");

        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            MXJsonValue.HelperForStructure setting = arraySetting.addFollowingStructure();
            setting.setFollowingNumber("Port", port);
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
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            _viewData.setSkip(port, TYPE_ACTIVE_SENSING, true);
        }
        _view.setViewData(_viewData);
    }
}
