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

import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX10Process extends MXReceiver<MX10View> implements MXSettingTarget {
    MX10View _view;
    MXSetting _setting;
    MX10Structure _structure;

    public MX10Process() {
        _view = new MX10View();
        _structure = new MX10Structure();
        _setting = new MXSetting("InputSkip");
        _setting.setTarget(this);
    }
    
    @Override
    public boolean isUsingThisRecipe() {
        return _view.isDXUsingThisRecipe();
    }

    @Override
    public void setUsingThisRecipe(boolean flag) {
        _view.setDXUsingThisRecipe(flag);
    }
    
    @Override
    public void processMXMessage(MXMessage message) {
        if (isUsingThisRecipe()) {
            if (_structure.isMessageForSkip(message)) {
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
    public MXSetting getSettings() {
        return _setting;
    }
    
    @Override
    public void prepareSettingFields() {
        String prefix = "Setting[].";
        for (String text : _structure.typeNames) {
            _setting.register(prefix + text);
        }
    }

    @Override
    public void afterReadSettingFile() {
        for (int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            for (int j = 0; j < _structure.countOfTypes(); ++ j) {
                String name = _structure.typeNames[j];
                boolean set = _setting.getSettingAsBoolean(prefix + name, false);
                _structure.setSkip(port, j, set);
            }
        }
        _view.setDXUsingThisRecipe(isUsingThisRecipe());
        _view.setDXStructure(_structure);
    }

    @Override
    public void beforeWriteSettingFile() {
        for (int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            StringBuffer str = new StringBuffer();
            for (int j = 0; j < _structure.countOfTypes(); ++ j) {
                boolean set = _structure.isSkip(port, j);
                if (set) {                   
                    String name = _structure.typeNames[j];
                    _setting.setSetting(prefix + name, set);
                }
            }
        }
    }
}
