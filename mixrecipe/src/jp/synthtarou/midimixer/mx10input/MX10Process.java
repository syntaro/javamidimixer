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
import javax.swing.JPanel;
import jp.synthtarou.libs.json.MXJsonValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import jp.synthtarou.midimixer.mx13patch.MX13Process;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX10Process extends MXReceiver<MX10View> implements MXJsonSupport  {
    MX10View _view;
    MXINIFile _setting;
    MX13Process _patch;

    public MX10Process() {
        _patch = new MX13Process(true);
        _view = new MX10View(this);
        _patch.setNextReceiver(new MXReceiver() {
            @Override
            public String getReceiverName() {
                return "@?";
            }

            @Override
            public JPanel getReceiverView() {
                return null;
            }

            @Override
            public void processMXMessage(MXMessage message) {
                MX10Process.this.sendToNext(message);
            }
        });
    }

    @Override
    public void processMXMessage(MXMessage message) {
        _patch.processMXMessage(message);
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
    public boolean readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("InputPatch");
            MXJsonParser.setAutosave(this);
        }
        MXJsonValue value = new MXJsonParser(custom).parseFile();
        if (value == null) {
            _patch.resetSetting();
            _view.showViewData();
            return false;
        }
        
        _patch.clearSetting();
        _patch.readJsonTree(value);
        _view.showViewData();
        return true;
    }

    @Override
    public boolean writeJsonFile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("InputPatch");
        }
        
        MXJsonParser parser = new MXJsonParser(custom);
        _patch.writeJsonTree(parser.getRoot());

        return parser.writeFile();
    }
    
    @Override
    public void resetSetting() {
        _patch.resetSetting();
        _view.showViewData();
    }
}
