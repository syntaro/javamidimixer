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
import java.util.ArrayList;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXStatic;
import jp.synthtarou.midimixer.MXThreadList;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.midi.MXException;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_PlayList;
import jp.synthtarou.midimixer.libs.midi.smf.SMFMessage;
import jp.synthtarou.midimixer.libs.midi.smf.SMFMessageList;
import jp.synthtarou.midimixer.libs.midi.smf.SMFPlayer;
import jp.synthtarou.midimixer.libs.midi.smf.SMFCallback;
 
/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMIDIInForPlayer extends MXMIDIIn {
    private static final MXDebugPrint _debug = new MXDebugPrint(MXMIDIInForPlayer.class);
 
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
           _sequencer.allNoteOff();;
           
           if (_sequencer.getLastFile().equals(file)) {
               return;
           }
           _sequencer = null;
        }
 
        _noteLowest = 200;
        _noteHighest = 0;
 
        _sequencer = new SMFPlayer(file);
 
        int[] programList = new int[16];
        ArrayList<Integer> drums = new ArrayList<Integer>();
        int firstNotePos = -1;
        
        ArrayList<SMFMessage> list = _sequencer.listMessage().listAll();
        for (int i = 0; i < list.size(); ++ i) {
            int msg = 0;
            
            if (list.get(i).isBinaryMessage()) {
                continue;
            }
            try {
                msg = list.get(i).toDwordMessage();
            }catch(MXException e) {
                //Not happens
                continue;
            }

            int status = (msg >> 8) >> 8;
            int command = status & 0xf0;
            int ch = status & 0x0f;
            int data1 = (msg >> 8) & 0xff;
            int data2 = msg & 0xff;

            if (command == MXMidi.COMMAND_NOTEON) {
                if (firstNotePos < 0) {
                    firstNotePos = i;
                }
                _existNoteChannel[ch] = true;
                if (ch == MXStatic.DRUM_CH && data2 >= 1) {
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
            SMFPlayer player = new SMFPlayer(file);
            SMFMessageList list = player.listMessage();
            ArrayList<SMFMessage> listMessage = list.listAll();

            for (SMFMessage message : listMessage) {
                if (message._status!= 0xff) {
                    continue;
                }

                int type = message._dataType;
                byte[] data = message.getBinary();
                String text = null;
                try {
                    text = new String(data, "ASCII");
                    text = new String(data);
                    text = new String(data, "SJIS");
                }catch(Exception e) {
                    e.printStackTrace();
                }
                int number = 0;

                switch(type) {
                    case 0:
                        if (data.length >= 2) {                                
                            number = data[0] * 128 + data[1];
                            ret.add("Sequence Number : " + number);
                        }
                        break;
                    case 1:
                       ret.add("Text : " + text);
                       break;
                    case 2:
                        ret.add("Copyright : " + text);
                        break;  
                    case 3:
                        ret.add("Track Name : " + text);
                        break;  
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret.add("Can't open [" + file.toString() + "]");
            ret.add(e.toString());
        }
        String[] list = new String[ret.size()];
        ret.toArray(list);
        return list;
    }
    
    private SMFPlayer _sequencer = null;
    private boolean _gotBreak = false;
    int _firstNotePos = -1;
    
    public long getLength() {
        return _sequencer.getLength();
    }
    
    public int getFirstNotePos() {
        if (_firstNotePos < 0) {
            ArrayList<SMFMessage> list = _sequencer.listMessage().listAll();
            int pos = 0;
            for (SMFMessage smf : list) {
                int command = smf._status; 
                if ((command & 0xf0) == MXMidi.COMMAND_NOTEON) {
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

    public synchronized void startSequencer(SMFCallback parent, int position) throws IOException {
        if (_sequencer != null) {
            _sequencer.stopPlayer();
        }
 
        _gotBreak = false;
        _sequencer.setCurrentPosition(position);
        _sequencer.startPlayer(new SMFCallback() {
            Thread _last;
            @Override
            public void smfPlayNote(SMFMessage smf) {
                 try {
                    if (_last != Thread.currentThread()) {
                        _last = Thread.currentThread();
                        MXThreadList.attachIfNeed("smfPlayNote", _last);
                    }
                    if (smf.isBinaryMessage()) {
                        receiveLongMessage(smf.getBinary());
                    }else {
                        int dword = smf.toDwordMessage();
                        receiveShortMessage(dword);
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
            public void smfProgress(int pos, int finish) {
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
            allNoteOff();
        }
    }
}