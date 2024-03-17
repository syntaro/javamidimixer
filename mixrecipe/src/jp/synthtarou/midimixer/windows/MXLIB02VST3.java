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

import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.async.TransactionBox;
import jp.synthtarou.midimixer.mx80vst.MX80Process;

/**
 * \
 *
 * @author Syntarou YOSHIDA
 */
public class MXLIB02VST3 {
    static MXLIB02VST3 _instance = new MXLIB02VST3();
    
    public static MXLIB02VST3 getInstance() {
        return _instance;
    }
    
    private boolean _loaded = false;

    protected MXLIB02VST3() {
        try {
            System.loadLibrary("MXLIB02");
            _loaded = true;
        } catch (Throwable e) {
           System.err.println("App can't load MXLIB02(VST3 Support). Function disabled.");
        }
    }

    public boolean isUsable() {
        return _loaded;
    }

    public native int countStream();
    public native void postInitializeStream(int task);
    public native void postOpenStream(int device, int sampleRate, int latency, int task);
    public native void postCloseStream(int task);
    public native boolean isStreamOpen();
    public native String nameOfStream(int device);
    public native String typeNameOfStream(int device);

    public native void postLaunchVST(boolean effect, int slot, String path, int task);
    public native boolean isLaunchedVST(boolean effect, int slot);
    public native void postOpenEditor(boolean effect, int slot, int task, int whenclose);
    public native void postCloseEditor(boolean effect, int slot, int task);
    public native boolean isEditorOpen(boolean effect, int slot);
    public native boolean isBlackListed(boolean effect, int slot);
    public native void postRemoveSynth(boolean effect, int slot, int task);
    public native void savePreset(boolean effect, int slot, String path, int task);
    public native void loadPreset(boolean effect, int slot, String path, int task);

    public native void waitQueued(int task);

    public native boolean postShortMessage(boolean effect, int port, int message);

    public native boolean postLongMessage(boolean effect, int port, byte[] data);

    public native float getMasterVolume();
    public native void setMasterVolume(float volume);

    public native int getBusCount(boolean effect, int synth);
    public native float getBusVolume(boolean effect, int synth, int bus);
    public native void setBusVolume(boolean effect, int synth, int bus, float volume);

    public native float getInsertBalance(int synth);
    public native void setInsertBalance(int synth, float volume);

    public native float getAuxSend(int synth);
    public native void setAuxSend(int synth, float volume);

    public native void stopEngine(int task);

    static private void cbCallText(String text) {
        MXMain.printDebug("VST-[" + text.toString() + "]");
    }
    
    static private void cbBlackListed(boolean effect, int port) {
        MX80Process vstProcess = MX80Process.getInstance();
        vstProcess.addBlackList(effect, port);
    }
    
    static private void cbTaskDone(int id, int result) {
        TransactionBox.getInstance().callmeWhenFinished(id, result);
    }

    public int firstStreamTypedIndex(String seekType) {
        int count = countStream();
        System.out.println(count);
        int found = -1;
        System.out.println("Searching in " + count);
        for (int i = 0; i < count; ++i) {
            String name = nameOfStream(i);
            String typeName = typeNameOfStream(i);
            
            if (typeName.startsWith(seekType)) {
                if (found < 0) {
                    found = i;
                }
            }
        }
        return found;
    }

    public int firstAsioIndex() {
        int asio = firstStreamTypedIndex("ASIO");
        if (asio < 0) {
            return firstDirectXIndex();
        }

        System.out.println("First ASIO = " + asio);
        return asio;
    }

    public int firstDirectXIndex() {
        int directx = firstStreamTypedIndex("Windows DirectSound");
        System.out.println("First Windows DirectSound = " + directx);
        return directx;
    }
  
    public void main(String[] args) {
        try {
            if (isUsable()) {
                cbCallText("Dll Okay");
            } else {
                cbCallText("Dll Not Standby");
                return;
            }

            //String uniPath  = "C:\\Program Files\\Common Files\\VST3\\Steinberg\\HALion Sonic SE\\Halion Sonic SE.vst3";
            //String uniPath = "C:\\Program Files\\Common Files\\VST3\\Steinberg\\HALion Sonic.vst3";
            String uniPath = "C:/Program Files/Common Files/VST3/KORG/M1.vst3";
            postInitializeStream(0);
            waitQueued(0);
            postOpenStream(firstAsioIndex(), 44100, 1024, 0);
            waitQueued(0);
            System.out.println("stream open = " + isStreamOpen());
            if (isStreamOpen() == false) {
                return;
            }
            String path = "C:\\github\\MXLIB01\\x64\\Debug\\VSTSTATE.BIN";

            int synth0 = 0;

            postLaunchVST(false, synth0, uniPath, 0);
            postOpenEditor(false, synth0, 1, 0);
            waitQueued(2);

            Thread.sleep(7000);

            savePreset(false, synth0, path, 8);

            int synth1 = 1;
            
            postLaunchVST(false, synth1, uniPath, 9);
            postOpenEditor(false, synth1, 10, 0);
            waitQueued(11);
            Thread.sleep(1000);
            loadPreset(false, synth1, path, 12);

            Thread.sleep(5000);

            postCloseEditor(false, synth1, 13);
            postOpenEditor(false, synth1, 14, 0);
            postCloseEditor(false, synth1, 15);
            postOpenEditor(false, synth1, 16, 0);
            waitQueued(17);
            

            Thread.sleep(1000);

            postRemoveSynth(false, synth0, 0);
            postRemoveSynth(false, synth1, 0);

            postCloseStream(0);
            waitQueued(0);

            System.exit(0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
