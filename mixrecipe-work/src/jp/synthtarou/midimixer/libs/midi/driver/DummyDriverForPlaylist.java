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

import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class DummyDriverForPlaylist implements MXDriver {

    @Override
    public boolean isUsable() {
        return true;
    }

    @Override
    public void StartLibrary() {
    }

    @Override
    public int InputDevicesRoomSize() {
        return 1;
    }

    @Override
    public String InputDeviceName(int device) {
        return "<PlayList>";
    }

    @Override
    public String InputDeviceId(int device) {
        return "<SEQ>";
    }

    boolean _open;
    
    @Override
    public boolean InputDeviceIsOpen(int device) {
        return _open;
    }

    @Override
    public boolean InputDeviceOpen(int device, long timeout, MXMIDIIn input) {
        _open = true;
        return true;
    }

    @Override
    public void InputDeviceClose(int device) {
        _open = false;
    }

    @Override
    public int OutputDevicesRoomSize() {
        return 0;
    }

    @Override
    public String OutputDeviceName(int device) {
        return null;
    }

    @Override
    public String OutputDeviceId(int device) {
        return null;
    }

    @Override
    public boolean OutputDeviceIsOpen(int device) {
        return false;
    }

    @Override
    public boolean OutputDeviceOpen(int device, long timeout) {
        return true;
    }

    @Override
    public void OutputDeviceClose(int device) {
    }

    @Override
    public boolean OutputShortMessage(int port, int message) {
        return false;
    }

    @Override
    public boolean OutputLongMessage(int port, byte[] data) {
        return false;
    }
}
