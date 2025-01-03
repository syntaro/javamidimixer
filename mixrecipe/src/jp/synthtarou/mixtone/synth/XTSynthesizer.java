/*
 * Copyright (C) 2024 Syntarou YOSHIDA
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
package jp.synthtarou.mixtone.synth;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_SoundFont;

import jp.synthtarou.mixtone.synth.audio.XTAudioStream;
import jp.synthtarou.mixtone.synth.soundfont.XTFile;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTSynthesizer {
    XTAudioStream _audioStream;
    public XTFile _sfz;
    XTSynthesizerTrack[] _tracks;

    public XTSynthesizerTrack getTrack(int ch) {
        return  _tracks[ch];
    }

    public boolean isReady() {
        if (_audioStream == null) {
            return false;
        }
        if (_audioStream.isReady() == false) {
            return false;
        }
        return _sfz != null;
    }

    public XTSynthesizer() {
        _tracks = new XTSynthesizerTrack[16];
        for (int i = 0; i < 16; ++ i) {
            _tracks[i] = new XTSynthesizerTrack(this, i);
        }
    }
    
    private String _lastFile = null;
    public void checkSettingAndUpdate() throws IOException {
        XTSynthesizerSetting setting = XTSynthesizerSetting.getSetting();
        if (setting.isUpdated()) {
            setting.clearUpdated();
            stopAudioStream();
            closeSoundFont();
            String sfz = setting.getCustomSFZ();
            if (_lastFile != null && _lastFile.equals(sfz)) {
                
            }
            else {
                openSoundfont(sfz == null ? null : new File(sfz));
                _lastFile = sfz;
            }
            startAudioStream();
        }
    }
    
    public void resetGM() {
        for (int i = 0; i < 16; ++ i) {
            if (i == 9) {
                _tracks[i].setupPHDRObject(0, 127);
                _tracks[i].setupPHDRObject(0, 128);
            }
            else {
               _tracks[i].setupPHDRObject(0, 0);
            }
        }
    }
    
    public void allNoteOff() {
        for (int i = 0; i < 16; ++ i) {
            _tracks[i].allNoteOff();
        }
    }
    
    public XTAudioStream startAudioStream() {
        if (_audioStream == null) {
            _audioStream = new XTAudioStream(this);
        }
        _audioStream.startStream();
        return _audioStream;
    }

    public void stopAudioStream() {
        if (_audioStream != null) {
            _audioStream.stopStream();
            _audioStream = null;
        }
    }

    public XTFile openSoundfont(File file) throws IOException {
        boolean copyInfo = false;
        if (_sfz != null) {
            copyInfo = true;
        }
        if (file == null) {
            InputStream stream = MXDriver_SoundFont.class.getResourceAsStream("GeneralUser-GS.sf2");
            _sfz = null;
            XTFile newSfz = new XTFile(stream);
            _sfz = newSfz;
        }
        else {
            _sfz = null;
            XTFile newSfz = new XTFile(file);
            _sfz = newSfz;
        }
        if (_sfz != null) {
            allNoteOff();
            resetGM();
        }
        return _sfz;
    }

    public void closeSoundFont() {
        allNoteOff();
        _audioStream.stopStream();
        _sfz = null;
    }
    
    int[] masterVolume =  { 0xf0, 0x7F, 0x7F, 0x04, 0x01, 0x11, -1, 0xF7 };
    static final boolean debugMessage = false;
    
    public void processMessage(OneMessage message) {
        int status = message.getStatus();
        
        if (status >= 0x80 && status <= 0xef) {
            int ch = status & 0x0f;
            _tracks[ch].processMesssage(message);
        }
        else {
            if (status == 0xf0 && message.isBinaryMessage()) {
                int detected = -1;
                byte[] data = message.getBinary();
                if (data.length == masterVolume.length) {
                    for (int i = 0; i < masterVolume.length; ++ i) {
                        int c = data[i] & 0xff;
                        if (masterVolume[i] != c) {
                            if (masterVolume[i] < 0) {
                                detected = c;
                            }
                            else {
                                detected = -1;
                                break;
                            }
                        }
                    }
                    if (detected >= 0) {
                        _audioStream._masterVolume = detected * 1.0 / 127;
                    }
                }
            }
        }
    }
}
