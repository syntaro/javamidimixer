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
import java.util.logging.Level;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_PlayList;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.libs.smf.SMFSequencer;
import jp.synthtarou.libs.smf.SMFCallback;
import jp.synthtarou.libs.SortedArray;
 
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

    public void openFile(File file) throws IOException {
        String fileName = file.toString();
        
        if (_sequencer != null) {
            stopSequencer(0);
           
           if (_sequencer.getLastFile().equals(file)) {
               return;
           }
           _sequencer = null;
        }
 
        _sequencer = new SMFSequencer(file);
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
            
            if (file.isFile() == false || file.exists() == false) {
                ret.add("* not found");
            }
            else {
                SMFSequencer player = new SMFSequencer(file);
                SortedArray<OneMessage> listMessage = player.listMessage();

                ret.add("SongLength: " + MXUtil.digitalClock(player.getMaxMilliSecond()));
                ret.add("SMPTE: " + player.getSMPTEFormat());
                ret.add("Resolution : " + player.getResolution());

                for (OneMessage message : listMessage) {
                    if (message.getStatus() != 0xff) {
                        continue;
                    }

                    ret.add(MXUtil.digitalClock(message._millisecond) + " : "+  message.getMetaText());
                }
            }
        } catch (IOException ex) {
            MXFileLogger.getLogger(MXMIDIInForPlayer.class).log(Level.WARNING, ex.getMessage(), ex);
        }catch(RuntimeException ex) {
            MXFileLogger.getLogger(MXMIDIInForPlayer.class).log(Level.WARNING, ex.getMessage(), ex);
        }
        String[] list = new String[ret.size()];
        ret.toArray(list);
        return list;
    }
    
    private SMFSequencer _sequencer = null;
    private boolean _gotBreak = false;
    
    public long getLength() {
        if (_sequencer == null) {
            return 0;
        }
        return _sequencer.getMaxMilliSecond();
    }
    
    public int getFirstNotePos() {
        if (_sequencer == null) {
            return 0;
        }
        return _sequencer._parser._firstNotePos;
    }
    
    public SMFSequencer getSequencer() {
        return _sequencer;
    }

    public synchronized void startSequencer(SMFCallback parent, long position) {
        _gotBreak = false;
        _sequencer.startPlayerThread(position, new SMFCallback() {
            @Override
            public void smfPlayNote(OneMessage smf) {
                 try {
                    if (smf.isBinaryMessage()) {
                        receiveLongMessage(null, smf.getBinary());
                    }else {
                        int dword = smf.getDWORD();
                        receiveShortMessage(null, dword);
                    }
                     parent.smfPlayNote(smf);
                }catch(RuntimeException ex) {
                    MXFileLogger.getLogger(MXMIDIInForPlayer.class).log(Level.WARNING, ex.getMessage(), ex);
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
    
    public synchronized void stopSequencer(int port) {
        if (_sequencer != null) {
            _sequencer.stopPlayerAwait();
            allNoteOff(null);
        }
    }
}