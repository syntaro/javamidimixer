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

import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.mx10input.MX10Structure;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX60Process extends MXReceiver<MX60View> implements MXSettingTarget {
    MX60View _view;
    MXSetting _setting;
    MX60Structure _structure;

    public MX60Process() {
        _structure = new MX60Structure(this);
        _view = new MX60View();
        _setting = new MXSetting("OutputSkip");
        _setting.setTarget(this);
    }
    
    @Override
    public boolean isUsingThisRecipeDX() {
        return _view.isUsingThisRecipeDX();
    }

    @Override
    public void setUsingThisRecipe(boolean flag) {
        _view.setUsingThisRecipeDX(flag);
    }

    @Override
    public void processMXMessage(MXMessage message) {
        if (isUsingThisRecipeDX() && _structure.isMessageForSkip(message)) {
            return;
        }

        if (_structure.isRecording()) {
            _structure.record(message);
            _view.setSongLengthDX(_structure._recordingTrack, _structure.getSongLength(_structure._recordingTrack));
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
    public MXSetting getSettings() {
        return _setting;
    }
    
    @Override
    public void afterReadSettingFile() {
        for (int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            StringBuffer str = new StringBuffer();
            for (int j = 0; j <_structure.countOfTypes(); ++ j) {
                String name = _structure.typeNames[j];
                boolean set = _setting.getSettingAsBoolean(prefix + name, false);
                _structure.setSkip(port, j, set);
            }
        }
        _structure.loadSequenceData();
        _view.setStructureDX(_structure);
    }

    @Override
    public void beforeWriteSettingFile() {
        _structure.saveSequenceData();
        for (int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            StringBuffer str = new StringBuffer();
            for (int j = 0; j <_structure.countOfTypes(); ++ j) {
                boolean set = _structure.isSkipDX(port, j);
                String name = _structure.typeNames[j];
                _setting.setSetting(prefix + name, set);
            }
        }
    }

    @Override
    public void prepareSettingFields() {
        String prefix = "Setting[].";
        for (String text : MX10Structure.typeNames) {
            _setting.register(prefix + text);
        }
    }
}
