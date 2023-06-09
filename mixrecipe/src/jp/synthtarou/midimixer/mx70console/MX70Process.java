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
package jp.synthtarou.midimixer.mx70console;

import jp.synthtarou.midimixer.libs.console.ConsoleModel;
import jp.synthtarou.midimixer.libs.console.ConsoleElement;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX70Process implements MXSettingTarget {
    MXSetting _setting;
    MX70Panel _view;
    ConsoleModel _outsideInput = new ConsoleModel();
    ConsoleModel _insideInput = new ConsoleModel();
    ConsoleModel _insideOutput = new ConsoleModel();
    ConsoleModel _outsideOutput = new ConsoleModel();
    ConsoleModel _listBinary = new ConsoleModel();

    public MX70Process() {
        _setting = new MXSetting("FreeConsole");
        _setting.setTarget(this);
    }

    public void readSettings() {
        _setting.readSettingFile();
    }

    public void createWindow() {
        if (_view != null) {
            if (_view.isOwnerWindowVisible()) { 
                return;
            }
        }
        _view = new MX70Panel(this);
        _view.showAsWindow();
    }

    public void addOutsideInput(ConsoleElement e) {
        _outsideInput.add(e);
        e.getTiming().recordWrap(0);
    }

    public void addInsideInput(MXMessage msg) {
        ConsoleElement e = new ConsoleElement(msg);
        _insideInput.add(e);
        e.getTiming().recordWrap(1);
        if (msg.isBinMessage()) {
            _listBinary.add(e);
        }
    }

    public void addInsideOutput(MXMessage msg) {
        ConsoleElement e = new ConsoleElement(msg);
        _insideOutput.add(e);
        try {
            e.getTiming().recordWrap(2);
        }catch(NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void addOutsideOutput(ConsoleElement e) {
        _outsideOutput.add(e);
        e.getTiming().recordWrap(3);
    }

    @Override
    public void prepareSettingFields(MXSetting setting) {
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
    }
    
    MX70SysexPanel _sysex;
    
    public  synchronized MX70SysexPanel createSysexPanel() {
        if (_sysex == null) {
            _sysex = new MX70SysexPanel(_listBinary);
        }
        return _sysex;
    }
}
