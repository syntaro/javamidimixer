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
import jp.synthtarou.midimixer.libs.common.MXLogger2;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.settings.MXSettingNode;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.libs.vst.IndexedFile;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX00Process extends MXReceiver<MX00View> implements MXSettingTarget {

    public MX00Process() {
        _setting = new MXSetting("PlayList");
        _setting.setTarget(this);

        _structure = new MX00Structure();
        _view = new MX00View();
    }

    MX00View _view;
    MX00Structure _structure;
    MXSetting _setting;
    
    @Override
    public String getReceiverName() {
        return "SMF Player";
    }

    @Override
    public MX00View getReceiverView() {
        return _view;
    }

    @Override
    public MXSetting getSettings() {
        return _setting;
    }

    @Override
    public void prepareSettingFields() {
        _setting.register("playAsLooped");
        _setting.register("playAsChained");
        _setting.register("song[]");
    }

    @Override
    public void afterReadSettingFile() {
        _structure._playListModel.clear();
        _structure._playAsRepeated = _setting.getSettingAsBoolean("playAsLooped", false);
        _structure._playAsChained = _setting.getSettingAsBoolean("playAsChained", false);
        
        List<MXSettingNode> nodeList  = _setting.findByPath("song[]");
        int min = 100000;
        int max = -1;
        for (MXSettingNode node : nodeList) {
            String name = node.getName();
            try {
                int x = Integer.parseInt(name);
                if (x < min) min = x;
                if (x > max) max = x;
            }catch(NumberFormatException ex) {
                MXLogger2.getLogger(MX00Process.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        for (int x = min; x <= max; ++ x) {
            String value = _setting.getSetting("song[" + x + "]");
            if (value != null && value.length() > 0) {
                _structure._playListModel.addFile(value);
            }
        }
        if (_structure._playListModel.isEmpty()) {
            _structure._playListModel.addFile("SynthTAROU000.mid");
            _structure._playListModel.addFile("SynthTAROU001.mid");
            _structure._playListModel.addFile("SynthTAROU002.mid");
        }

        _view.setStructureDX(_structure);
    }

    @Override
    public void beforeWriteSettingFile() {
        _setting.clearValue();
        _structure = _view.getStructureDX();

        _setting.setSetting("playAsLooped", _structure._playAsRepeated);
        _setting.setSetting("playAsChained", _structure._playAsChained);

        for (int i = 0; i < _structure._playListModel.size(); ++ i) {
            File f = _structure._playListModel.getElementAt(i)._file;
            _setting.setSetting("song[" + (i + 1) + "]", f.getPath());
        }
    }

    @Override
    public void processMXMessage(MXMessage message) {
    }
    
    public void updatePianoDX(int dword) {
        _view.updatePianoDX(dword);
    }
}
