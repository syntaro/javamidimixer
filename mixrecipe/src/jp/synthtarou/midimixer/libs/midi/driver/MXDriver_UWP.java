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
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOut;
import jp.synthtarou.midimixer.windows.MXLIB01UWPMidi;

/**
 * UWPｰMIDIを入出力するドライバー
 *
 * @author Syntarou YOSHIDA
 */
public class MXDriver_UWP implements MXDriver {
    public int getDriverUID() {
        return 20;
    }

    public static final MXDriver_UWP _instance = new MXDriver_UWP();

    MXLIB01UWPMidi windows10;

    public MXDriver_UWP() {
        windows10 = new MXLIB01UWPMidi();
    }

    public String driverSuffix() {
        return "(UWP)";
    }

    @Override
    public synchronized boolean isUsable() {
        return windows10.isDLLAvail();
    }

    @Override
    public synchronized void StartLibrary() {
        if (isUsable()) {
            windows10.StartLibrary();
        }
    }

    @Override
    public synchronized int InputDevicesRoomSize() {
        if (!isUsable()) {
            return 0;
        }
        return windows10.InputDevicesRoomSize();
    }

    @Override
    public synchronized String InputDeviceName(int device) {
        if (!isUsable()) {
            return "";
        }
        return windows10.InputDeviceName(device) + "(UWP)";
    }

    @Override
    public synchronized String InputDeviceId(int device) {
        if (!isUsable()) {
            return "";
        }
        return windows10.InputDeviceId(device);
    }

    @Override
    public synchronized boolean InputDeviceIsOpen(int device) {
        if (!isUsable()) {
            return false;
        }
        return windows10.InputIsOpen(device);
    }

    @Override
    public synchronized boolean InputDeviceOpen(int device, long timeout, MXMIDIIn input) {
        MXFileLogger.getLogger(MXMIDIIn.class).info("InputDeviceOpen 1" + timeout);
        if (!isUsable()) {
            return false;
        }
        MXFileLogger.getLogger(MXMIDIIn.class).info("InputDeviceOpen 2" + timeout);
        return windows10.InputOpen(device, timeout);
    }

    @Override
    public synchronized void InputDeviceClose(int device) {
        if (!isUsable()) {
            return;
        }
        windows10.InputClose(device);
    }

    @Override
    public synchronized int OutputDevicesRoomSize() {
        if (!isUsable()) {
            return 0;
        }
        return windows10.OutputDevicesRoomSize();
    }

    @Override
    public synchronized String OutputDeviceName(int device) {
        if (!isUsable()) {
            return "";
        }
        return windows10.OutputDeviceName(device) + "(UWP)";
    }

    @Override
    public synchronized String OutputDeviceId(int device) {
        if (!isUsable()) {
            return "";
        }
        return windows10.OutputDeviceId(device);
    }

    @Override
    public synchronized boolean OutputDeviceIsOpen(int device) {
        if (!isUsable()) {
            return false;
        }
        return windows10.OutputIsOpen(device);
    }

    @Override
    public synchronized boolean OutputDeviceOpen(int device, long timeout) {
        if (!isUsable()) {
            return false;
        }
        return windows10.OutputOpen(device, timeout);
    }

    @Override
    public synchronized void OutputDeviceClose(int device) {
        if (!isUsable()) {
            return;
        }
        windows10.OutputClose(device);
    }

    @Override
    public synchronized boolean OutputOneMessage(int device, OneMessage message) {
        if (!isUsable()) {
            return false;
        }
        
        if (message.isBinaryMessage()) {
            return windows10.OutputLongMessage(device, message.getBinary());
        }
        else {
            return windows10.OutputShortMessage(device, message.getDWORD());
        }
    }


    ArrayList<MXMIDIIn> _listInputCatalog = new ArrayList();
    ArrayList<MXMIDIOut> _listOutputCatalog = new ArrayList();

    public synchronized void addInputCatalog(MXMIDIIn input) {
        int order = input.getOrderInDriver();
        if (input.getDriver() == this) {
            while (_listInputCatalog.size() <= order) {
                _listInputCatalog.add(null);
            }
            _listInputCatalog.set(order, input);
        }
    }

    public synchronized MXMIDIIn findInputCatalog(int order) {
        Thread t = Thread.currentThread();
        while (_listInputCatalog.size() <= order) {
            _listInputCatalog.add(null);
        }
        MXMIDIIn in = _listInputCatalog.get(order);
        if (in.getOrderInDriver() == order) {
            return in;
        }
        throw new IllegalArgumentException();
    }

    public synchronized void closeAllInput() {
        for (MXMIDIIn in : _listInputCatalog) {
            if (in != null) {
                in.close();
            }
        }
    }

    public synchronized void addOuputCatalog(MXMIDIOut output) {
        int order = output.getOrderInDriver();
        if (output.getDriver() == this) {
            while (_listOutputCatalog.size() <= order) {
                _listOutputCatalog.add(null);
            }
            _listOutputCatalog.set(order, output);
        }
    }

    public synchronized MXMIDIOut findOutputCatalog(int order) {
        while (_listOutputCatalog.size() <= order) {
            _listOutputCatalog.add(null);
        }
        MXMIDIOut out = _listOutputCatalog.get(order);
        if (out.getOrderInDriver() == order) {
            return out;
        }
        throw new IllegalArgumentException();
    }

    public synchronized void closeAllOutput() {
        for (MXMIDIOut out : _listOutputCatalog) {
            if (out != null) {
                out.close();
            }
        }
    }
}
