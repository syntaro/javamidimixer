/*
7 * Copyright (C) 2025 yaman
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

/**
 *
 * @author yaman
 */
public class MXLIB03FluidSynth {

    static MXLIB03FluidSynth _instance = new MXLIB03FluidSynth();
    
    public static MXLIB03FluidSynth getInstance() {
        return _instance;
    }
    
    private boolean _loaded = false;

    protected MXLIB03FluidSynth() {
        try {
            System.loadLibrary("MXLIB03");
            _loaded = true;
        } catch (Throwable e) {
           System.err.println("App can't load MXLIB03(FluidSynth).");
           e.printStackTrace();
        }
    }

    public boolean isUsable() {
        return _loaded;
    }

    public native int allocate();
    public native void open(int handle);
    public native void free(int handle);
    public native void close(int handle);
    public native boolean loadFont(int handle, String font);
    public native void unloadFont(int handle);

    public native String listProgram(int handle);
    public native void retune(int handle, float hzamust, boolean equalTemp, int baseKey);
    public native void sendShortMessage(int handle, int status, int data1, int data2);
    public native void sendLongMessage(int handle, byte[] data);
    public native void systemReset(int handle);

    static private void cbCallText(String text) {
        System.err.println(text);
    }
}

