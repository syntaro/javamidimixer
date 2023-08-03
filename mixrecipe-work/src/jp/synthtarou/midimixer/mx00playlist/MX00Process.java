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

import jp.synthtarou.midimixer.libs.common.FileWithId;
import java.io.File;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.settings.MXSettingNode;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX00Process extends MXReceiver implements MXSettingTarget {
    private static final MXDebugPrint _debug = new MXDebugPrint(MX00Process.class);

    public MX00Process() {
        _playListModel = new DefaultListModel();
        _setting = new MXSetting("PlayList");
        _setting.setTarget(this);

        _view = new MX00View(this);
    }

    public void readSettings() {
        _setting.readSettingFile();

        if (_playListModel.size() == 0) {
            _playListModel.addElement(new FileWithId(new File("SynthTAROU000.mid")));
            _playListModel.addElement(new FileWithId(new File("SynthTAROU001.mid")));
            _playListModel.addElement(new FileWithId(new File("SynthTAROU002.mid")));
        }

        _view.settingUpdated();
    }
    
    MX00View _view;
    FileWithId _file;
    DefaultListModel _playListModel;
    MXSetting _setting;
    boolean _playAsChained;
    boolean _playAsRepeated;
    
    @Override
    public String getReceiverName() {
        return "SMF Player";
    }

    @Override
    public JComponent getReceiverView() {
        return _view;
    }
    @Override
    public void prepareSettingFields(jp.synthtarou.midimixer.libs.settings.MXSetting config) {
        config.register("playAsLooped");
        config.register("playAsChained");
        config.register("song[]");
    }

    @Override
    public void afterReadSettingFile(jp.synthtarou.midimixer.libs.settings.MXSetting config) {
        _playListModel.clear();

        _playAsRepeated = config.getSettingAsBoolean("playAsLooped", false);
        _playAsChained = config.getSettingAsBoolean("playAsChained", false);
        
        List<MXSettingNode> nodeList  = _setting.findByPath("song[]");
        int min = 100000;
        int max = -1;
        for (MXSettingNode node : nodeList) {
            String name = node.getName();
            try {
                int x = Integer.parseInt(name);
                if (x < min) min = x;
                if (x > max) max = x;
            }catch(NumberFormatException e) {
                _debug.printStackTrace(e);
            }
        }
        for (int x = min; x <= max; ++ x) {
            String value = config.getSetting("song[" + x + "]");
            if (value != null && value.length() > 0) {
                FileWithId file = new FileWithId(new File(value));
                _playListModel.addElement(file);
            }
        }
    }

    @Override
    public void beforeWriteSettingFile(MXSetting config) {
        config.clearValue();
        config.setSetting("playAsLooped", _playAsRepeated);
        config.setSetting("playAsChained", _playAsChained);

        for (int i = 0; i < _playListModel.getSize(); ++ i) {
            FileWithId f = (FileWithId)_playListModel.get(i);
            config.setSetting("song[" + (i + 1) + "]", f._file.getPath());
        }
    }

    @Override
    protected  void processMXMessageImpl(MXMessage message) {
    }
    
    public void updatePianoKeys(int dword) {
        _view.updatePianoKeys(dword);
    }

    public void createPianoControls(int lowNote, int octaveRange, boolean[] activeChannels, int[] listPrograms, int[] drumProgs)  {
        _view.createPianoControls(lowNote, octaveRange, activeChannels, listPrograms, drumProgs);
    }
}
