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
package jp.synthtarou.midimixer.libs.midi.recorder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MIDIRecorder {
    private static final MIDIRecorder _instance = new MIDIRecorder();
    public static MIDIRecorder getInstance() {
        return _instance;
    }

    public int getPort() {
        return _port;
    }

    public void setPort(int _port) {
        this._port = _port;
    }

    Sequencer _sequencer = null;
    Sequence _sequence = null;
    Receiver _receiver = null;
    boolean _isrecording = false;
    private int _port = -1;
    
    public MIDIRecorder() {
    }    

    public boolean isRecording() {
        return _isrecording;
    }
    
    public long tickLength() {
        if (_sequence == null) {
            return 0;
        }
        return _sequence.getTickLength();
    }
    
    public boolean startRecording() {
        try {
            if (_isrecording) { 
                return true;
            }
            _sequencer = MidiSystem.getSequencer();
            _sequence = new Sequence(Sequence.PPQ, 24);
            Track currentTrack = _sequence.createTrack();
            _sequencer.setSequence(_sequence);
            _sequencer.setTickPosition(0);
            _sequencer.recordEnable(currentTrack, -1);
            _sequencer.open();
            _sequencer.startRecording();
            _receiver = _sequencer.getReceiver();
            _isrecording = true;
        }catch(MidiUnavailableException e) {
            e.printStackTrace();
            _sequence = null;
            _sequencer = null;
            return false;
        }catch(InvalidMidiDataException e) {
            e.printStackTrace();
            _sequence = null;
            _sequencer = null;
            return false;
        }
        return true;
    }
    
    public void stopRecording() {
        if (_isrecording) {
            _sequencer.stopRecording();
            _isrecording = false;
        }
    }
    
    public void saveRecording(File file) {
        if (_sequence != null && _isrecording == false) {
            FileOutputStream fout;
            BufferedOutputStream bout;
            try {
                fout = new FileOutputStream(file);
                bout = new BufferedOutputStream(fout);
                MidiSystem.write(_sequence, 0, bout);
                bout.flush();
                fout.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public Receiver getReceiver() {
        return _receiver;
    }
}
