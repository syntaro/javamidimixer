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

import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.mx80vst.MX80Process;
import jp.synthtarou.midimixer.libs.vst.VSTStream;
import jp.synthtarou.midimixer.windows.MXLIB02VST3;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXDriver_VSTi implements MXDriver {
    public static final MXDriver_VSTi _instance = new MXDriver_VSTi();
    
    MXLIB02VST3 _vstLib = MXLIB02VST3.getInstance();
    MX80Process _ownList = MX80Process.getInstance();
    VSTStream _wrapStream = VSTStream.getInstance();
    
    public String driverSuffix() {
        return "";
    }
    
    public MXDriver_VSTi() {
        StartLibrary();
    }

    @Override
    public boolean isUsable() {
        return _vstLib.isUsable();
    }

    @Override
    public void StartLibrary() {
    }

    @Override
    public int InputDevicesRoomSize() {
        //nothing
        return 0;
    }

    @Override
    public String InputDeviceName(int device) {
        return "VSTi:" + (char)('A'+device);
    }

    @Override
    public String InputDeviceId(int device) {
        //nothing
        return null;
    }

    @Override
    public boolean InputDeviceIsOpen(int device) {
        //nothing
        return false;
    }

    @Override
    public boolean InputDeviceOpen(int device, long timeout, MXMIDIIn input) {
        return true;
        //nothing
    }

    @Override
    public void InputDeviceClose(int device) {
        //nothing
    }

    @Override
    public int OutputDevicesRoomSize() {
        return  16;
    }

    @Override
    public String OutputDeviceName(int device) {
        return "VSTi:" + (char)('A'+device);
    }

    @Override
    public String OutputDeviceId(int device) {
        return _ownList.getInstrument(device).getPath();
    }

    //boolean[] flagOpen = new boolean[20];
    
    @Override
    public boolean OutputDeviceIsOpen(int port) {
        //return flagOpen[device];
        return _ownList.getInstrument(port).isOpen();
    }

    @Override
    public boolean OutputDeviceOpen(int device, long timeout) {
        return true;
        //flagOpen[device] = true;
    }

    @Override
    public void OutputDeviceClose(int device) {
        //flagOpen[device] = false;
    }

    @Override
    public boolean OutputShortMessage(int port, int message) {
        return _ownList.getInstrument(port).postShortMessage(message);
    }

    @Override
    public boolean OutputLongMessage(int port, byte[] data) {
        return _ownList.getInstrument(port).postLongMessage(data);
    }
}
