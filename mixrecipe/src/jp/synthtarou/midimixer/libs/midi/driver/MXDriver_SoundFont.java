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
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.mixtone.synth.XTSynthesizer;
import jp.synthtarou.mixtone.synth.XTSynthesizerSetting;

/**
 * Java標準MIDI用のドライバ
 *
 * @author Syntarou YOSHIDA
 */
public class MXDriver_SoundFont implements MXDriver {
    static MXDriver_SoundFont _instance;
    
    public static MXDriver_SoundFont getInstance() {
        if (_instance == null) {
            _instance = new MXDriver_SoundFont();
        }
        return _instance;
    }
    
    public static XTSynthesizer getSharedSynthesizer() {
        return getInstance()._synth;
    }
    
    private MXDriver_SoundFont() {
        XTSynthesizer synth = XTSynthesizerSetting.getSetting().getSynthInstance();
        synth.openSoundfont(null);
        _synth = synth;
    }

    public int getDriverUID() {
        return 3;
    }
    
    XTSynthesizer _synth;

    public boolean isUsable() {
        return true;
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
        return _synth == null ? 0 : 1;
    }

    public String OutputDeviceName(int x) {
        return "(Test)GeneralUser-GS.sf2";
    }

    public String OutputDeviceId(int x) {
        return "GeneralUser-GS.sf2";
    }

    public boolean OutputDeviceIsOpen(int x) {
        return _synth != null && _synth.isReady();
    }

    public boolean OutputDeviceOpen(int device, long timeout) {
        if (_synth == null) {
            return false;
        }
        if (_synth.isReady()) {
            return true;
        }
        return _synth.startAudioStream() != null;
    }

    public void OutputDeviceClose(int x) {
        if (_synth == null) {
            return;
        }
        if (_synth.isReady()) {
            _synth.stopAudioStream();
        }
    }

    public boolean OutputOneMessage(int x, OneMessage one) {
        _synth.processMessage(one);
        return true;
    }

    @Override
    public void StartLibrary() {
    }
}
