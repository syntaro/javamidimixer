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
package jp.synthtarou.mixtone.main;

import jp.synthtarou.mixtone.main.view.XTSynthesizerFrame;
import java.io.File;
import java.io.IOException;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.mixtone.listmodel.TheConsole;
import jp.synthtarou.mixtone.synth.audio.XTAudioStream;
import jp.synthtarou.mixtone.synth.soundfont.XTFile;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTSynthesizer {
    XTSynthesizerFrame _mainFrame;
    XTAudioStream _audioStream;
    public XTFile _sfz;
    XTSynthesizerTrack[] _tracks;
    
    public XTSynthesizer() {
        _tracks = new XTSynthesizerTrack[16];
        for (int i = 0; i < 16; ++ i) {
            _tracks[i] = new XTSynthesizerTrack(this, i);
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
    
    public XTAudioStream prepareAudioStream() {
        if (_audioStream == null) {
            _audioStream = XTAudioStream.getInstance();
        }
        _audioStream.startStream();
        return _audioStream;
    }
    
    public XTFile openSoundfont(File file) {
        try {
            XTFile newSfz = new XTFile(file);
            _sfz = newSfz;
        }catch(IOException ex) {
            ex.printStackTrace();
            _sfz = null;
        }
        prepareAudioStream();
        allNoteOff();
        return _sfz;
    }
    
    public void dump(TheConsole console) {
        _sfz.dumpPreset(console);
        _sfz.dumpHeader(console);
        _sfz.dumpSamples(console);
        _sfz.dumpInstrument(console);
    }
    
    public void closeSoundFont() {
        allNoteOff();
        _audioStream.stopStream();
        _sfz = null;
    }
    
    public void processMessage(OneMessage message) {
        int status = message.getStatus();
        if (status >= 0x80 && status <= 0xef) {
            int ch = status & 0x0f;
            _tracks[ch].processMesssage(message);
        }
        else {
            //master volume etc TODO:
            //?
        }
    }
}
