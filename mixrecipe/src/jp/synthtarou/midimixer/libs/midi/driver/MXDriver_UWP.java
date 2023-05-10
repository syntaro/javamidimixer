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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package jp.synthtarou.midimixer.libs.midi.driver;

import java.util.ArrayList;
import jp.synthtarou.midimixer.MXThreadList;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOut;
import jp.synthtarou.midimixer.windows.MXLIB01UWPMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXDriver_UWP implements MXDriver {
    public static final MXDriver_UWP _instance = new MXDriver_UWP();
    
    MXLIB01UWPMidi windows10;

    public MXDriver_UWP() {
        windows10 = new MXLIB01UWPMidi();
    }

    public String driverSuffix() {
        return "(UWP)";
    }

    @Override
    public boolean isUsable() {
        return  windows10.isDLLAvail();
    }

    @Override
    public void StartLibrary() {
        if (isUsable()) {
            windows10.StartLibrary();
        }
    }

    @Override
    public int InputDevicesRoomSize() {
        if (!isUsable()) {
            return 0;
        }
        return windows10.InputDevicesRoomSize();
    }

    @Override
    public String InputDeviceName(int device) {
        if (!isUsable()) {
            return "";
        }
        return windows10.InputDeviceName(device) + "(UWP)";
    }

    @Override
    public String InputDeviceId(int device) {
        if (!isUsable()) {
            return "";
        }
        return windows10.InputDeviceId(device);
    }

    @Override
    public boolean InputDeviceIsOpen(int device) {
        if (!isUsable()) {
            return false;
        }
        return windows10.InputIsOpen(device);
    }

    @Override
    public boolean InputDeviceOpen(int device, long timeout, MXMIDIIn input) {
        if (!isUsable()) {
            return false;
        }
        return  windows10.InputOpen(device, timeout);
    }
    
    @Override
    public void InputDeviceClose(int device) {
        if (!isUsable()) {
            return;
        }
        windows10.InputClose(device);
    }

    @Override
    public int OutputDevicesRoomSize() {
        if (!isUsable()) {
            return 0;
        }
        return windows10.OutputDevicesRoomSize();
    }

    @Override
    public String OutputDeviceName(int device) {
        if (!isUsable()) {
            return "";
        }
        return windows10.OutputDeviceName(device) + "(UWP)";
    }

    @Override
    public String OutputDeviceId(int device) {
        if (!isUsable()) {
            return "";
        }
        return windows10.OutputDeviceId(device);
    }

    @Override
    public boolean OutputDeviceIsOpen(int device) {
        if (!isUsable()) {
            return false;
        }
        return windows10.OutputIsOpen(device);
    }

    @Override
    public boolean OutputDeviceOpen(int device, long timeout) {
        if (!isUsable()) {
            return false;
        }
        return windows10.OutputOpen(device, timeout);
    }

    @Override
    public void OutputDeviceClose(int device) {
        if (!isUsable()) {
            return;
        }
        windows10.OutputClose(device);
    }

    @Override
    public boolean OutputShortMessage(int device, int message) {
        if (!isUsable()) {
            return false;
        }
        return windows10.OutputShortMessage(device, message);
    }

    @Override
    public boolean OutputLongMessage(int device, byte[] message) {
        if (!isUsable()) {
            return false;
        }
        return windows10.OutputLongMessage(device, message);
    }
    
    ArrayList<MXMIDIIn> _listInputCatalog = new ArrayList();
    ArrayList<MXMIDIOut> _listOutputCatalog = new ArrayList();
    
    public void addInputCatalog(MXMIDIIn input) {
        int order = input.getDriverOrder();
        if (input.getDriver() == this) {
            while (_listInputCatalog.size() <= order) {
                _listInputCatalog.add(null);
            }
            _listInputCatalog.set(order, input);
        }
    }
    
    Thread _last;
    
    public MXMIDIIn findInputCatalog(int order) {
        Thread t = Thread.currentThread();
        if (_last != t) {
            _last = t;
            MXThreadList.attachIfNeed("MXDriver_UWP", t);
        }
        while (_listInputCatalog.size() <= order) {
            _listInputCatalog.add(null);
        }
        MXMIDIIn in =_listInputCatalog.get(order);
        if (in.getDriverOrder() == order) {
            return in;
        }
        throw new IllegalArgumentException();
    }
    
    public void closeAllInput() {
        for (MXMIDIIn in : _listInputCatalog) {
            if (in != null) {
                in.close();
            }
        }
    }

    public void addOuputCatalog(MXMIDIOut output) {
        int order = output.getDriverOrder();
        if (output.getDriver() == this) {
            while (_listOutputCatalog.size() <= order) {
                _listOutputCatalog.add(null);
            }
            _listOutputCatalog.set(order, output);
        }
    }
    
    public MXMIDIOut findOutputCatalog(int order) {
        while (_listOutputCatalog.size() <= order) {
            _listOutputCatalog.add(null);
        }
        MXMIDIOut out =_listOutputCatalog.get(order);
        if (out.getDriverOrder() == order) {
            return out;
        }
        throw new IllegalArgumentException();
    }
    
    public void closeAllOutput() {
        for (MXMIDIOut out  : _listOutputCatalog) {
            if (out != null) {
                out.close();
            }
        }
    }
}
