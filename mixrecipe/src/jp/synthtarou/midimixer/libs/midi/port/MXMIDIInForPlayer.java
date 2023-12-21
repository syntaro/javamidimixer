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
import javax.sound.midi.ShortMessage;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.MXThreadList;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.smf.SMFException;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
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
 
    boolean[] _existNoteChannel = new boolean[16];
    
    public MXMIDIInForPlayer() {
        super(new MXDriver_PlayList(), 0);
    }

    /*
    public void setOutPort(int outPort) {
        open(0);
        _assigned = outPort;
    }*/

    public void openFile(File file) throws IOException, MidiUnavailableException, InvalidMidiDataException {
        int _noteLowest;
        int _noteHighest;
 
        String fileName = file.toString();
 
        if (_sequencer != null) {
           _sequencer.allNoteOff(null);
           
           if (_sequencer.getLastFile().equals(file)) {
               return;
           }
           _sequencer = null;
        }
 
        _noteLowest = 200;
        _noteHighest = 0;
 
        _sequencer = new SMFSequencer(file);
 
        int[] programList = new int[16];
        ArrayList<Integer> drums = new ArrayList<Integer>();
        int firstNotePos = -1;
        
        ArrayList<SMFMessage> list = _sequencer.listMessage();
        for (int i = 0; i < list.size(); ++ i) {
            int msg = 0;
            
            if (list.get(i).isBinaryMessage()) {
                continue;
            }
            try {
                msg = list.get(i).toDwordMessage();
            }catch(SMFException e) {
                //Not happens
                continue;
            }

            int status = (msg >> 16) & 0xff;
            int ch = status & 0x0f;
            int data1 = (msg >> 8) & 0xff;
            int data2 = msg & 0xff;

            int command = status & 0xf0;

            if (command == MXMidi.COMMAND_CH_NOTEON) {
                if (firstNotePos < 0) {
                    firstNotePos = i;
                }
                _existNoteChannel[ch] = true;
                if (ch == MXAppConfig.DRUM_CH && data2 >= 1) {
                    drums.add(data1);
                }else {
                    if (data1 < _noteLowest) _noteLowest = data1;
                    if (data1 > _noteHighest) _noteHighest = data1;
                }
            }
            else if (command == ShortMessage.PROGRAM_CHANGE) {
                if (programList[ch]  < 0) {
                    programList[ch] = data1;
                }
            }
        }
 
        int lo2 = _noteLowest;
        lo2 = lo2 / 12;
        lo2 = lo2 * 12;
 
        int hi2 = lo2;
 
        while(hi2 < _noteHighest) {
            hi2 += 12;
        }
 
        int x = lo2 / 12;
        int width = hi2 / 12;
 
        width -= x;
        x *= 12;
 
        if (width <= 2) {
            if (x >= 12) {
                x -= 12;
                width += 1;
            }
            if (width <= 2) {
                width += 1;
            }
        }
 
        int rows = 0;
 
        int[] drumProgs = new int[drums.size()];
        for (int id = 0; id < drums.size(); ++ id) {
            drumProgs[id] = drums.get(id);
        }
        MXMain.getMain().getPlayListProcess().createPianoControls(lo2, width, _existNoteChannel, programList, drumProgs);
        _firstNotePos = firstNotePos;
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
    int _firstNotePos = -1;
    
    public long getLength() {
        return _sequencer.getMaxMilliSecond();
    }
    
    public int getFirstNotePos() {
        if (_firstNotePos < 0) {
            SortedArray<SMFMessage> list = _sequencer.listMessage();
            int pos = 0;
            for (SMFMessage smf : list) {
                int command = smf.getStatus(); 
                if ((command & 0xf0) == MXMidi.COMMAND_CH_NOTEON) {
                    _firstNotePos = pos;
                    break;
                }
                pos ++;
            }
            if (_firstNotePos < 0) {
                _firstNotePos = 0;
            }
        }
        return (_firstNotePos >= 0) ? _firstNotePos : 0;
    }

    public synchronized void startSequencer(SMFCallback parent, long position) throws IOException {
        if (_sequencer != null) {
            _sequencer.stopPlayer();
        }

        _gotBreak = false;
        _sequencer.setStartMilliSecond(position);
        _sequencer.startPlayer(new SMFCallback() {
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