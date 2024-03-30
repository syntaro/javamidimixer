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
package jp.synthtarou.midimixer.mx00playlist;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import jp.synthtarou.libs.MXFileLogger;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.libs.inifile.MXINIFileNode;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonFile;
import jp.synthtarou.libs.json.MXJsonValue;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX00Process extends MXReceiver<MX00View> implements MXINIFileSupport, MXJsonSupport {

    public MX00Process() {
        _viewData = new MX00ViewData();
        _view = new MX00View(this);
        readINIFile(null);
    }

    MX00View _view;
    MX00ViewData _viewData;
    
    @Override
    public String getReceiverName() {
        return "SMF Player";
    }

    @Override
    public MX00View getReceiverView() {
        return _view;
    }

    @Override
    public MXINIFile prepareINIFile(File custom) {
        if (custom == null) {
            custom = MXINIFile.pathOf("PlayList");
        }
        MXINIFile setting = new MXINIFile(custom, this);
        setting.register("playAsLooped");
        setting.register("playAsChained");
        setting.register("song[]");
        setting.register("focusChannel");
        setting.register("showMeasure");
        setting.register("soundMargin");
        setting.register("soundSpan");
        return setting;
    }

    @Override
    public void readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        setting.readINIFile();
        _viewData._playListModel.clear();
        _viewData._playAsRepeated = setting.getSettingAsBoolean("playAsLooped", false);
        _viewData._playAsChained = setting.getSettingAsBoolean("playAsChained", false);
        _viewData._focusChannel = setting.getSettingAsInt("focusChannel", -1);
        _viewData._showMeasure = setting.getSettingAsBoolean("showMeasure", true);
        _viewData._soundMargin = setting.getSettingAsInt("soundMargin", 100);
        _viewData._soundSpan = setting.getSettingAsInt("soundSpan", 6000);
        
        List<MXINIFileNode> nodeList  = setting.findByPath("song[]");
        int min = 100000;
        int max = -1;
        for (MXINIFileNode node : nodeList) {
            String name = node.getName();
            try {
                int x = Integer.parseInt(name);
                if (x < min) min = x;
                if (x > max) max = x;
            }catch(NumberFormatException ex) {
                MXFileLogger.getLogger(MX00Process.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        for (int x = min; x <= max; ++ x) {
            String value = setting.getSetting("song[" + x + "]");
            if (value != null && value.length() > 0) {
                _viewData._playListModel.addFile(value);
            }
        }
        if (_viewData._playListModel.isEmpty()) {
            _viewData._playListModel.addFile("SynthTAROU000.mid");
            _viewData._playListModel.addFile("SynthTAROU001.mid");
            _viewData._playListModel.addFile("SynthTAROU002.mid");
        }

        _view.showDataFirst();
    }

    @Override
    public void writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);

        setting.setSetting("playAsLooped", _viewData._playAsRepeated);
        setting.setSetting("playAsChained", _viewData._playAsChained);
        
        setting.setSetting("focusChannel", _viewData._focusChannel);
        setting.setSetting("showMeasure", _viewData._showMeasure);
        setting.setSetting("soundMargin", _viewData._soundMargin);
        setting.setSetting("soundSpan", _viewData._soundSpan);

        for (int i = 0; i < _viewData._playListModel.size(); ++ i) {
            File f = _viewData._playListModel.getElementAt(i)._file;
            setting.setSetting("song[" + (i + 1) + "]", f.getPath());
        }
        setting.writeINIFile();
    }

    @Override
    public void processMXMessage(MXMessage message) {
    }
    
    public void updatePianoDX(int dword) {
        _view.updatePianoDX(dword);
    }

    @Override
    public void readJSonfile(File custom) {
        MXJsonFile file = new MXJsonFile(custom);
        MXJsonValue value = file.readJsonFile();
        if (value == null) {
            value = new MXJsonValue(null);
        }
        //TODO
    }

    @Override
    public void writeJsonFile(File custom) {
        MXJsonValue value = new MXJsonValue(null);
        
        MXJsonFile file = new MXJsonFile(custom);
        file.writeJsonFile(value);
    }
}
