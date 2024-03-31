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

import java.io.File;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.libs.json.MXJsonValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonParser;

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
        return _view.isUsingThisRecipeDX();
    }

    @Override
    public void setUsingThisRecipe(boolean flag) {
        _view.setUsingThisRecipeDX(flag);
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
        for (String text : _viewData.typeNames) {
            setting.register(prefix + text);
        }
        return setting;
    }

    @Override
    public void readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        setting.readINIFile();
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            for (int j = 0; j < _viewData.countOfTypes(); ++ j) {
                String name = _viewData.typeNames[j];
                boolean set = setting.getSettingAsBoolean(prefix + name, false);
                _viewData.setSkip(port, j, set);
            }
        }
        _view.setUsingThisRecipeDX(isUsingThisRecipe());
        _view.setStructureDX(_viewData);
    }

    @Override
    public void writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            StringBuffer str = new StringBuffer();
            for (int j = 0; j < _viewData.countOfTypes(); ++ j) {
                boolean set = _viewData.isSkip(port, j);
                if (set) {                   
                    String name = _viewData.typeNames[j];
                    setting.setSetting(prefix + name, set);
                }
            }
        }
        setting.writeINIFile();
    }

    @Override
    public void readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("InputSkip");
        }
        MXJsonValue value = MXJsonParser.parseFile(custom);
        if (value == null) {
            value = new MXJsonValue(null);
        }
        MXJsonValue.HelperForStructure root = value.new HelperForStructure();
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            MXJsonValue.HelperForStructure prefix = root.findStructure("Port" + port);
            if (prefix == null) {
                continue;
            }
            for (int j = 0; j < _viewData.countOfTypes(); ++ j) {
                String name = _viewData.typeNames[j];
                boolean set = prefix.getSettingBool(name, false);
                _viewData.setSkip(port, j, set);
            }
        }
        _view.setUsingThisRecipeDX(isUsingThisRecipe());
        _view.setStructureDX(_viewData);
    }

    @Override
    public void writeJsonFile(File custom) {
        MXJsonValue value = new MXJsonValue(null);
        MXJsonValue.HelperForStructure root = value.new HelperForStructure();
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            MXJsonValue.HelperForStructure prefix = root.addStructure("Port" + port);
            StringBuffer str = new StringBuffer();
            for (int j = 0; j < _viewData.countOfTypes(); ++ j) {
                boolean set = _viewData.isSkip(port, j);
                if (set) {                   
                    String name = _viewData.typeNames[j];
                    prefix.setSettingBool(name, set);
                }
            }
        }
        if (custom == null) {
            custom = MXJsonParser.pathOf("InputSkip");
        }
        MXJsonParser.writeFile(value, custom);
    }
}
