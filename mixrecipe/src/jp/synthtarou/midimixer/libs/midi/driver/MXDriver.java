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

/**
 *　MIDIドライバーの（基底）ンターフェース
 *
 * @author Syntarou YOSHIDA
 */
public interface MXDriver{
    public boolean isUsable();
    public void StartLibrary();
    
    public int InputDevicesRoomSize();
    public String InputDeviceName(int device);
    public String InputDeviceId(int device);
    public boolean InputDeviceIsOpen(int device);

    public boolean InputDeviceOpen(int device, long timeout, MXMIDIIn input);
    public void InputDeviceClose(int device);
    
    public int OutputDevicesRoomSize();
    public String OutputDeviceName(int device);
    public String OutputDeviceId(int device);
    public boolean OutputDeviceIsOpen(int device);

    public boolean OutputDeviceOpen(int device, long timeout);
    public void OutputDeviceClose(int device);
    
    public boolean OutputShortMessage(int port, int message);
    public boolean OutputLongMessage(int port, byte[] data);
}
