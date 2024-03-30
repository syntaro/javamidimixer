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

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX60Process extends MXReceiver<MX60View> implements MXINIFileSupport {
    MX60View _view;
    MX60ViewData _viewData;

    public MX60Process() {
        _viewData = new MX60ViewData(this);
        _view = new MX60View();
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
    public void readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        setting.readINIFile();
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            StringBuffer str = new StringBuffer();
            for (int j = 0; j <_viewData.countOfTypes(); ++ j) {
                String name = _viewData.typeNames[j];
                boolean set = setting.getSettingAsBoolean(prefix + name, false);
                _viewData.setSkip(port, j, set);
            }
        }
        _viewData.loadSequenceData();
        _view.setDataDX(_viewData);
    }

    @Override
    public void writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        _viewData.saveSequenceData();
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            StringBuffer str = new StringBuffer();
            for (int j = 0; j <_viewData.countOfTypes(); ++ j) {
                boolean set = _viewData.isSkip(port, j);
                String name = _viewData.typeNames[j];
                setting.setSetting(prefix + name, set);
            }
        }
        setting.writeINIFile();
    }

    @Override
    public MXINIFile prepareINIFile(File custom) {
        if (custom == null) {
            custom = MXINIFile.pathOf("OutputSkip");
        }
        MXINIFile setting = new MXINIFile(custom, this);
        String prefix = "Setting[].";
        for (String text : MX10ViewData.typeNames) {
            setting.register(prefix + text);
        }
        return setting;
    }
}
