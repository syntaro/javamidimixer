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
package jp.synthtarou.midimixer.windows;

import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_UWP;

/**
 *\
 * @author Syntarou YOSHIDA
 */
public class MXLIB01UWPMidi {
    private boolean _loaded  = false;

    public MXLIB01UWPMidi() {
        try {
            System.loadLibrary("MXLIB01");
            StartLibrary();
            _loaded = true;
        }catch(Throwable e) {
            System.err.println("MXLIB01(UWP Support) Couldn't Load.");
        }
    }
    
    public boolean isDLLAvail() {
        return _loaded;
    }
   
    public native void StartLibrary();

    public native int InputDevicesRoomSize();
    public native String InputDeviceName(int device);
    public native String InputDeviceId(int device);
    
    public native boolean InputOpen(int device, long timeout);
    public native boolean InputIsOpen(int device);
    public native void InputClose(int device);

    public native int OutputDevicesRoomSize();
    public native String OutputDeviceName(int device);
    public native String OutputDeviceId(int device);

    public native boolean OutputOpen(int device, long timeout);
    public native boolean OutputIsOpen(int device);
    public native void OutputClose(int device);
    
    public native boolean OutputShortMessage(int device, int message);
    public native boolean OutputLongMessage(int device, byte[] data);

    static private void cbCallText(String text) {
        System.out.println("UWP-[" + text.toString() + "]");
    }
    
    int _microsoftSynthIndex = -1;

    static private void cbCallShortMessage(int device, int message) {
        MXDriver_UWP uwp = MXDriver_UWP._instance;
        uwp.findInputCatalog(device).receiveShortMessage(new MXTiming(), message);
    }

    static private void cbCallLongMessage(int device, byte[] data) {
        MXDriver_UWP uwp = MXDriver_UWP._instance;
        uwp.findInputCatalog(device).receiveLongMessage(new MXTiming(), data);
    }
    
    static private void cbDeviceListed() {
    }
}