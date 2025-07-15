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
package jp.synthtarou.midimixer.libs.midi.driver;

import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.windows.MXLIB03FluidSynth;

/**
 * Java標準MIDI用のドライバ
 *
 * @author Syntarou YOSHIDA
 */
public class MXDriver_FluidSynth implements MXDriver {
    static MXDriver_FluidSynth _instance;
    
    public static MXDriver_FluidSynth getInstance() {
        if (_instance == null) {
            _instance = new MXDriver_FluidSynth();
        }
        return _instance;
    }
    
    private MXDriver_FluidSynth() {
        try {
            _fluid = MXLIB03FluidSynth.getInstance();
        }catch(Throwable ex) {
            ex.printStackTrace();
        }
    }

    public int getDriverUID() {
        return 4;
    }
    
    MXLIB03FluidSynth _fluid;
    int _handle = -1;

    public boolean isUsable() {
        return _fluid.isUsable();
    }

    @Override
    public int InputDevicesRoomSize() {
        return 0;
    }

    @Override
    public String InputDeviceName(int x) {
        return null;
    }

    @Override
    public String InputDeviceId(int x) {
        return null;
    }

    @Override
    public boolean InputDeviceIsOpen(int x) {
        return false;
    }

    @Override
    public boolean InputDeviceOpen(int device, long timeout, MXMIDIIn input) {
        return false;
    }

    @Override
    public void InputDeviceClose(int x) {
    }

    @Override
    public int OutputDevicesRoomSize() {
        return isUsable() ? 1 : 0;
    }

    public String OutputDeviceName(int x) {
        return "fluidsynth";
    }

    public String OutputDeviceId(int x) {
        return "fluidsynth1";
    }

    public boolean OutputDeviceIsOpen(int x) {
        return isUsable() && _handle >= 1;
    }

    public boolean OutputDeviceOpen(int device, long timeout) {
        int x = _fluid.allocate();
        _fluid.open(x);
        boolean loaded = _fluid.loadFont(x, "./GeneralUser GS v1.471.sf2");
        if (loaded) {
            _handle = x;
            _fluid.systemReset(_handle);
        }
        else {
            _fluid.close(x);
            _handle = -1;
        }
        return true;
    }

    public void OutputDeviceClose(int x) {
        if (_handle > 0) {
            _fluid.unloadFont(_handle);
            _fluid.close(_handle);
            _handle = -1;
        }
    }

    public boolean OutputOneMessage(int x, OneMessage one) {
        if (one.isBinaryMessage()) {
            byte[] data = one.getBinary();
            _fluid.sendLongMessage(_handle, data);
        }
        else {
            int status = one.getStatus();
            int data1 = one.getData1();
            int data2 = one.getData2();
            _fluid.sendShortMessage(_handle, status, data1, data2);
        }
        return true;
}

    @Override
    public void StartLibrary() {
    }
    
    Thread t;
}
