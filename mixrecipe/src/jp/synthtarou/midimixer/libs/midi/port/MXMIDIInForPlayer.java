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
package jp.synthtarou.midimixer.libs.midi.port;
 
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXThreadList;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_PlayList;
import jp.synthtarou.midimixer.libs.midi.smf.SMFMessage;
import jp.synthtarou.midimixer.libs.midi.smf.SMFSequencer;
import jp.synthtarou.midimixer.libs.midi.smf.SMFCallback;
import jp.synthtarou.midimixer.mx36ccmapping.SortedArray;
 
/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMIDIInForPlayer extends MXMIDIIn {
 
    public MXMIDIInForPlayer() {
        super(MXDriver_PlayList._instance, 0);
    }

    /*
    public void setOutPort(int outPort) {
        open(0);
        _assigned = outPort;
    }*/

    public void openFile(File file) throws IOException, MidiUnavailableException, InvalidMidiDataException {
        String fileName = file.toString();
 
        if (_sequencer != null) {
           _sequencer.allNoteOff(null, 0); //not important number (will be overwrited
           
           if (_sequencer.getLastFile().equals(file)) {
               return;
           }
           _sequencer = null;
        }
 
        _sequencer = new SMFSequencer(file);
        ArrayList<Integer> drums = _sequencer._parser._drums;
        int noteLowest = _sequencer._parser._noteLowest;
        int noteHighest = _sequencer._parser._noteHighest;
        int []program = _sequencer._parser._programList;
        boolean []exist = _sequencer._parser._existNoteChannel;
        
        int lo2 = noteLowest;
        lo2 = lo2 / 12;
        lo2 = lo2 * 12;
 
        int hi2 = lo2;
 
        while(hi2 < noteHighest) {
            hi2 += 12;
        }
 
        int x = lo2 / 12;
        int octaveRange = hi2 / 12;
 
        octaveRange -= x;
        x *= 12;
 
        if (octaveRange <= 2) {
            if (x >= 12) {
                x -= 12;
                octaveRange += 1;
            }
            if (octaveRange <= 2) {
                octaveRange += 1;
            }
        }

        noteLowest = x;
        noteHighest = octaveRange * 12 + x;

        while (octaveRange <= 4) {
            octaveRange += 2;
            noteLowest -= 12;
        }
        while (octaveRange < 5) {
            octaveRange++;
        }
        
        MXMain.getMain().getPlayListProcess().createPianoControls(noteLowest, octaveRange,exist, program, drums);
    }
 
    public static String[] readFileInfo(File file) {
        int _noteLowest;
        int _noteHighest;

        ArrayList<String> ret = new ArrayList();
        String fileName = file.toString();
 
        try {
            Date lastDate = new Date(file.lastModified());
            String textDate = DateFormat.getDateTimeInstance().format(lastDate);

            ret.add("File: " + file.getName() +" (in " + file.getParent() + ")");
            ret.add("Size: " + file.length());
            ret.add("Date: " + textDate);
            
            SMFSequencer player = new SMFSequencer(file);
            SortedArray<SMFMessage> listMessage = player.listMessage();
            
            ret.add("SongLength: " + MXUtil.digitalClock(player.getMaxMilliSecond()));
            ret.add("SMPTE: " + player.getSMPTEFormat());
            ret.add("Resolution : " + player.getResolution());

            for (SMFMessage message : listMessage) {
                if (message.getStatus() != 0xff) {
                    continue;
                }
                
                ret.add(MXUtil.digitalClock(message._millisecond) + " : "+  message.getMetaText());
            }
        } catch (Exception e) {
            if (file.exists()) {                
                e.printStackTrace();
            }
            ret.add("Can't open [" + file.toString() + "]");
            ret.add(e.toString());
        }
        String[] list = new String[ret.size()];
        ret.toArray(list);
        return list;
    }
    
    private SMFSequencer _sequencer = null;
    private boolean _gotBreak = false;
    
    public long getLength() {
        return _sequencer.getMaxMilliSecond();
    }
    
    public int getFirstNotePos() {
        return _sequencer._parser._firstNotePos;
    }
    
    public SMFSequencer getSequencer() {
        return _sequencer;
    }

    public synchronized void startSequencer(SMFCallback parent, long position) throws IOException {
        if (_sequencer != null) {
            _sequencer.stopPlayer();
        }
        
        _gotBreak = false;
        _sequencer.startPlayer(position, new SMFCallback() {
            Thread _last;
            @Override
            public void smfPlayNote(MXTiming timing, SMFMessage smf) {
                 try {
                    if (_last != Thread.currentThread()) {
                        _last = Thread.currentThread();
                        MXThreadList.attachIfNeed("smfPlayNote", _last);
                    }
                    if (smf.isBinaryMessage()) {
                        receiveLongMessage(timing, smf.getBinary());
                    }else {
                        int dword = smf.toDwordMessage();
                        receiveShortMessage(timing, dword);
                    }
                }catch(Throwable e) {
                    e.printStackTrace();
                }
           }

            @Override
            public void smfStarted() {
                parent.smfStarted();
            }

            @Override
            public void smfStoped(boolean fineFinish) {
                parent.smfStoped(fineFinish);
            }

            @Override
            public void smfProgress(long pos, long finish) {
                parent.smfProgress(pos, finish);
            }
        });
    }
 
    public synchronized boolean isSequencerPlaying() {
         if (_sequencer != null) {
           return  _sequencer.isRunning();
        }
        return false;
    }
    
    public synchronized void stopSequencer() {
        if (_sequencer != null) {
            _sequencer.stopPlayer();
            allNoteOff(null);
        }
    }
}