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

import javax.swing.JPanel;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX10Process extends MXReceiver implements MXSettingTarget {
    MX10Data _data;
    MX10View _view;

    MXSetting _setting;

    public MX10Process() {
        _data = new MX10Data();
        _view = new MX10View(this);
        _setting = new MXSetting("InputSkip");
        _setting.setTarget(this);
    }
    
    public void readSettings() {
        _setting.readSettingFile();
    }
    

    public void setUseMesssageFilter(boolean log) {
        setUsingThisRecipe(log);
    }
    
    public boolean isUseMessageFilter() {
        return isUsingThisRecipe();
    }
    
    @Override
    public void processMXMessage(MXMessage message) {
        if (isUsingThisRecipe()) {
            if (_data.isMarkedAsSkip(message)) {
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
    public JPanel getReceiverView() {
        return _view;
    }

    @Override
    public void prepareSettingFields(MXSetting setting) {
        String prefix = "Setting[].";
        for (String text : MX10Data.typeNames) {
            setting.register(prefix + text);
        }
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
        for (int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            for (int j = 0; j < _data.countOfTypes(); ++ j) {
                String name = _data.typeNames[j];
                boolean set = setting.getSettingAsBoolean(prefix + name, false);
                _data.setSkip(port, j, set);
            }
        }
        _view.resetTableModel();
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        for (int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            StringBuffer str = new StringBuffer();
            for (int j = 0; j < _data.countOfTypes(); ++ j) {
                boolean set = _data.isSkip(port, j);
                if (set) {                   
                    String name = _data.typeNames[j];
                    setting.setSetting(prefix + name, set);
                }
            }
        }
    }
}
