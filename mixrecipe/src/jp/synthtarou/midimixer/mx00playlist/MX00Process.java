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
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.libs.inifile.MXINIFileNode;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import jp.synthtarou.libs.json.MXJsonValue;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;

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
    public boolean readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        if (!setting.readINIFile()) {
            return false;
        }
        _viewData._playListModel.clear();
        _viewData._playAsRepeated = setting.getSettingAsBoolean("playAsLooped", false);
        _viewData._playAsChained = setting.getSettingAsBoolean("playAsChained", false);
        _viewData._focusChannel = setting.getSettingAsInt("focusChannel", -1);
        _viewData._highlightTiming = setting.getSettingAsBoolean("showMeasure", true);
        _viewData._soundMargin = setting.getSettingAsInt("soundMargin", 100);
        _viewData._soundSpan = setting.getSettingAsInt("soundSpan", 4000);
        
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
            _viewData._playListModel.addFile("synth01.mid");
            _viewData._playListModel.addFile("synth02.mid");
            _viewData._playListModel.addFile("synth03.mid");
            _viewData._playListModel.addFile("synth04.mid");
            _viewData._playListModel.addFile("synth05.mid");
        }

        _view.showDataFirst();
        return true;
    }

    @Override
    public boolean writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);

        setting.setSetting("playAsLooped", _viewData._playAsRepeated);
        setting.setSetting("playAsChained", _viewData._playAsChained);
        
        setting.setSetting("focusChannel", _viewData._focusChannel);
        setting.setSetting("showMeasure", _viewData._highlightTiming);
        setting.setSetting("soundMargin", _viewData._soundMargin);
        setting.setSetting("soundSpan", _viewData._soundSpan);

        for (int i = 0; i < _viewData._playListModel.size(); ++ i) {
            File f = _viewData._playListModel.getElementAt(i)._file;
            setting.setSetting("song[" + (i + 1) + "]", f.getPath());
        }
        return setting.writeINIFile();
    }

    @Override
    public void processMXMessage(MXMessage message) {
    }
    
    public void updatePianoDX(int dword) {
        _view.updatePianoDX(dword);
    }

    @Override
    public boolean readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("PlayList");
            MXJsonParser.setAutosave(this);
        }
        MXJsonValue value = new MXJsonParser(custom).parseFile();
        if (value == null) {
            return false;
        }
        MXJsonValue.HelperForStructure root = value.new HelperForStructure();

        _viewData._playListModel.clear();
        _viewData._playAsRepeated = root.getFollowingBool("playAsLooped", false);
        _viewData._playAsChained = root.getFollowingBool("playAsChained", false);
        _viewData._focusChannel = root.getFollowingInt("focusChannel", -1);
        _viewData._highlightTiming = root.getFollowingBool("showMeasure", true);
        _viewData._soundMargin = root.getFollowingInt("soundMargin", 100);
        _viewData._soundSpan = root.getFollowingInt("soundSpan", 4000);
        
        MXJsonValue.HelperForArray listSong = root.getFollowingArray("songs");
        if (listSong != null) {
            for (int i = 0; i < listSong.count(); ++ i) {
                String file = listSong.getFollowingText(i, "");
                if (file.isEmpty()) {
                    continue;
                }
                _viewData._playListModel.addFile(file);
            }
        }
        if (_viewData._playListModel.isEmpty()) {
            _viewData._playListModel.addFile("synth01.mid");
            _viewData._playListModel.addFile("synth02.mid");
            _viewData._playListModel.addFile("synth03.mid");
            _viewData._playListModel.addFile("synth04.mid");
        }

        _view.showDataFirst();

        return true;
    }

    @Override
    public boolean writeJsonFile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("PlayList");
        }
        MXJsonValue value = new MXJsonValue(null);
        MXJsonParser parser = new MXJsonParser(custom);
        MXJsonValue root = parser.getRoot();
        MXJsonValue.HelperForStructure setting = root.new HelperForStructure();

        setting.setFollowingBool("playAsLooped", _viewData._playAsRepeated);
        setting.setFollowingBool("playAsChained", _viewData._playAsChained);
        
        setting.setFollowingInt("focusChannel", _viewData._focusChannel);
        setting.setFollowingBool("showMeasure", _viewData._highlightTiming);
        setting.setFollowingInt("soundMargin", _viewData._soundMargin);
        setting.setFollowingInt("soundSpan", _viewData._soundSpan);
        
        MXJsonValue.HelperForArray songList = setting.addFollowingArray("songs");
        
        for (int i = 0; i < _viewData._playListModel.size(); ++ i) {
            File f = _viewData._playListModel.getElementAt(i)._file;
            songList.addFollowingText(f.getPath());
        }

        return parser.writeFile();
    }

    @Override
    public void resetSetting() {
        _viewData._playListModel.clear();
        _viewData._playListModel.addFile("synth01.mid");
        _viewData._playListModel.addFile("synth02.mid");
        _viewData._playListModel.addFile("synth03.mid");
        _viewData._playListModel.addFile("synth04.mid");
        _viewData._playListModel.addFile("synth05.mid");

        _viewData._playAsRepeated = false;
        _viewData._playAsChained = false;
        _viewData._focusChannel = -1;
        _viewData._highlightTiming = true;
        _viewData._soundMargin = 100;
        _viewData._soundSpan = 4000;
        
        _view.showDataFirst();
    }
}
