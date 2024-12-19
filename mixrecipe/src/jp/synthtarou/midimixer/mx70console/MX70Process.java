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

import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.midi.console.MXMidiConsole;
import jp.synthtarou.midimixer.libs.midi.console.MXMidiConsoleElement;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX70Process extends MXReceiver<MX70View>{
    MX70View _view;
    MXMidiConsole _outsideInput = new MXMidiConsole();
    MXMidiConsole _insideInput = new MXMidiConsole();
    MXMidiConsole _insideOutput = new MXMidiConsole();
    MXMidiConsole _outsideOutput = new MXMidiConsole();
    MXMidiConsole _listBinary = new MXMidiConsole();

    public MX70Process() {
    }

    public void addOutsideInput(MXMidiConsoleElement e) {
        _outsideInput.addElement2(e);
    }

    public void addInsideInput(MXMessage msg) {
        MXMidiConsoleElement e = new MXMidiConsoleElement(msg);
        _insideInput.addElement2(e);
        if (msg.isSysexOrMeta()) {
            _listBinary.addElement2(e);
        }
    }

    public void addInsideOutput(MXMessage msg) {
        MXMidiConsoleElement e = new MXMidiConsoleElement(msg);
        _insideOutput.addElement2(e);
    }

    public void addOutsideOutput(MXMidiConsoleElement e) {
        _outsideOutput.addElement2(e);
    }
    
    MX70SysexPanel _sysex;
    
    public  synchronized MX70SysexPanel createSysexPanel() {
        if (_sysex == null) {
            _sysex = new MX70SysexPanel(_listBinary);
        }
        return _sysex;
    }

    @Override
    public String getReceiverName() {
        return "SysEX";
    }

    @Override
    public synchronized MX70View getReceiverView() {
        if (_view == null) {
            _view = new MX70View(this);
        }
        return _view;
    }

    @Override
    public void processMXMessage(MXMessage message) {
        return;
    }
}
