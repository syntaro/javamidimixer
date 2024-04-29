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
import jp.synthtarou.libs.json.MXJsonValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX10Process extends MXReceiver<MX10View> implements MXJsonSupport  {
    MX10View _view;
    MXINIFile _setting;

    public MX10Process() {
        _view = new MX10View(this);
    }

    @Override
    public void processMXMessage(MXMessage message) {
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
    public boolean readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("InputPatch");
            MXJsonParser.setAutosave(this);
        }
        MXJsonValue value = new MXJsonParser(custom).parseFile();
        if (value == null) {
            return false;
        }
        
        return true;
    }

    @Override
    public boolean writeJsonFile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("InputPatch");
        }
        
        MXJsonParser parser = new MXJsonParser(custom);

        return parser.writeFile();
    }
    
    @Override
    public void resetSetting() {
    }
    
    public void showMIDIInDetail(MXMIDIIn in) {
        _view.showMIDIInDetail(in);
    }
}
