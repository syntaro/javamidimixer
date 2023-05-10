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
package jp.synthtarou.midimixer.libs.midi.smf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import jp.synthtarou.midimixer.MXStatic;
import jp.synthtarou.midimixer.MXThreadList;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFPlayer {
    
    private SMFMessageList _listMessage;
    private int _position;
    private int _smpteFormat = -99999;
    private int _resolution = 96;
    private File _lastFile; 
    boolean _paraPlay = false;

    public SMFPlayer() {
        _listMessage = new SMFMessageList();
        _smpteFormat = -99999;
        _resolution = 96;
        firstNote = new SMFMessage[16];
    }

    public SMFPlayer(File file) throws IOException {
        SMFParser parser = new SMFParser(file);
        _lastFile = file;
        _listMessage = parser.getMessageList();
        _resolution = parser.getFileInfo().getResolution();
        _smpteFormat = parser.getFileInfo().getSmpteFormat();
    }
    
    public File getLastFile() {
        return _lastFile;
    }

    public SMFMessageList listMessage() { 
        return _listMessage;
    }

    boolean _breakSequence;
    
    public void startPlayer(SMFCallback callback) {
        if (isRunning()) {
            stopPlayer();            
        }
        _callback = callback;
        Thread t = MXThreadList.newThread("SMFPlayer", new Runnable() {
            @Override
            public void run() {
                _callback.smfStarted();
                allNoteOff();
                try {
                    resetControllers();
                    if (_smpteFormat > 0) {
                        playWithSMPTE(_smpteFormat, _resolution);
                        _isRunning = false;
                    } else {
                        playWithTempo();
                        _isRunning = false;
                    }
                } catch (Throwable e) {
                    _isRunning = false;
                    e.printStackTrace();
                } finally {
                    synchronized (this) {
                        notifyAll();
                    }
                }
                allNoteOff();
                _callback.smfStoped(_breakSequence ? false : true);
            }
        });
        t.setDaemon(true);
        t.start();
        _isRunning = true;
    }

    public void stopPlayer() {
        _breakSequence = true;
        synchronized(this) {        
            notifyAll();
        }
        while(_isRunning) {
            try {
                Thread.sleep(100);
            }catch(InterruptedException e) {
            }
        }
    }

    SMFMessage[] firstNote = null;

    protected boolean sendProgramChangeBeforeNote() {
        List<SMFMessage> list = _listMessage.listAll();

        SMFMessage[] firstProgram = new SMFMessage[16];
        SMFMessage[] firstBank0 = new SMFMessage[16];
        SMFMessage[] firstBank32 = new SMFMessage[16];
        firstNote = new SMFMessage[16];
    
        int doneCh = 0;
        int pos = _position;

        while (pos < list.size()) {
            SMFMessage smf = list.get(pos);
            int command = smf._status & 0xf0;
            int channel = smf._status & 0x0f;
            int data1 = smf._data1;

            if (firstNote[channel] == null) {
                if (command == MXMidi.COMMAND_PROGRAMCHANGE) {
                    firstProgram[channel] = smf;
                } else if (command == MXMidi.COMMAND_CONTROLCHANGE && data1 == 0) {
                    firstBank0[channel] = smf;
                } else if (command == MXMidi.COMMAND_CONTROLCHANGE && data1 == 32) {
                    firstBank32[channel] = smf;
                } else if (command == MXMidi.COMMAND_NOTEON) {
                    firstNote[channel] = smf;
                    doneCh++;
                    if (doneCh >= 16) {
                        break;
                    }
                }
            }
            pos++;
        }
        pos = 0;

        boolean didProgramChange = false;

        for (int ch = 0; ch < 16; ++ch) {
            if (firstBank0[ch] != null && firstBank32[ch] != null) {
                smfPlayNote(firstBank0[ch]);
                smfPlayNote(firstBank32[ch]);
                didProgramChange = true;
            }
            if (firstProgram[ch] != null) {
                smfPlayNote(firstProgram[ch]);
                didProgramChange = true;
            }
        }

        if (didProgramChange) {
            try {
                System.out.println("*** Did Program Change*");
                Thread.sleep(150 * doneCh);
            } catch (Exception e) {

            }
        }

        return didProgramChange;
    }

    protected void playWithSMPTE(int smpteFormat, int fileResolution) throws InvalidMidiDataException {
        _breakSequence = false;

        double frameRate = smpteFormat;
        if (smpteFormat == 29) {
            frameRate = 29.97;
        }

        double tickPerMilliSecond = frameRate * fileResolution / 1000;

        int pos = _position;
        if (_paraPlay) {
            
        }else if (_position == 0) {
            sendProgramChangeBeforeNote();
        }

        List<SMFMessage> list = _listMessage.listAll();

        long stopWatch = System.currentTimeMillis();
        long alreadySpent = (long) (list.get(pos)._tick * tickPerMilliSecond) / 1000;
        stopWatch -= alreadySpent;


        while (!_breakSequence && pos < list.size()) {
            long elapsed = (System.currentTimeMillis() - stopWatch);
            long elapsedTicks = (long) (elapsed * tickPerMilliSecond);

            if (elapsedTicks < 0) {
                break;
            }

            while (!_breakSequence && list.get(pos)._tick <= elapsedTicks) {
                _position = pos;

                _callback.smfProgress(pos, list.size());
                SMFMessage currentEvent = list.get(pos++);

                if (currentEvent.isBinaryMessage() || currentEvent.isMetaMessage()) {
                    smfPlayNote(currentEvent);
                    if (pos >= list.size()) {
                        break;
                    }
                } else if (currentEvent._status >= 0x80) {
                    int status = currentEvent._status;
                    int data1 = currentEvent._data1;
                    int data2 = currentEvent._data2;
                    int command = status & 0xf0;
                    int channel = status & 0x0f;

                    if (firstNote != null) {
                        if (firstNote[channel] != null) {
                            if (command == MXMidi.COMMAND_PROGRAMCHANGE
                                    || (command == MXMidi.COMMAND_CONTROLCHANGE && (data1 == 0 || data1 == 32))) {
                                continue;
                            } else if (command == MXMidi.COMMAND_NOTEON) {
                                if (firstNote[channel] == currentEvent) {
                                    firstNote[channel] = null;
                                }
                            }
                        }
                    }

                    smfPlayNote(currentEvent);
                    if (pos >= list.size()) {
                        break;
                    }
                }
            }
            try {
                if (_breakSequence) {
                    break;
                }
                if (pos >= list.size()) {
                    break;
                }
                long nextTick = list.get(pos)._tick;
                int nextMillis = (int) (nextTick / tickPerMilliSecond - System.currentTimeMillis());
                long differ = nextMillis - (System.currentTimeMillis() - stopWatch);;
                synchronized(this) {
                    if (differ >= 10) {
                        wait(differ - 5);
                    } else {
                        wait(1);
                    }
                }
            } catch (InterruptedException e) {
                _breakSequence = true;
            }
        }
    }

    protected void playWithTempo() throws InvalidMidiDataException {
        _breakSequence = false;

        SMFTempoList tempo = new SMFTempoList(_listMessage.listAll(), _resolution);

        int pos = _position;
        if (_paraPlay) {
            
        }else if (_position == 0) {
            sendProgramChangeBeforeNote();
        }

        List<SMFMessage> list = _listMessage.listAll();

        long stopWatch = System.currentTimeMillis();
        long alreadySpent = tempo.TicksToMicroseconds(list.get(pos)._tick) / 1000;
        stopWatch -= alreadySpent;
        while (!_breakSequence && pos < list.size()) {
            long elapsed = (System.currentTimeMillis() - stopWatch) * 1000L;
            long elapsedTicks = tempo.MicrosecondsToTicks(elapsed);

            if (elapsedTicks < 0) {
                break;
            }

            while (!_breakSequence && list.get(pos)._tick <= elapsedTicks) {
                _position = pos;
                _callback.smfProgress(pos, list.size());
                SMFMessage currentEvent = list.get(pos++);

                if (currentEvent.isBinaryMessage() || currentEvent.isMetaMessage()) {
                    if (currentEvent.getBinary().length == 0) {
                        continue;
                    }
                    if (currentEvent.getBinary()[0] == 0) {
                        continue;
                    }
                    smfPlayNote(currentEvent);
                    if (pos >= list.size()) {
                        break;
                    }
                } else if (currentEvent._status >= 0x80) {
                    int status = currentEvent._status;
                    int data1 = currentEvent._data1;
                    int data2 = currentEvent._data2;
                    int command = status & 0xf0;
                    int channel = status & 0x0f;

                    if (firstNote != null) {
                        if (firstNote[channel] != null) {
                            if (command == MXMidi.COMMAND_PROGRAMCHANGE
                                    || (command == MXMidi.COMMAND_CONTROLCHANGE && (data1 == 0 || data1 == 32))) {
                                continue;
                            } else if (command == MXMidi.COMMAND_NOTEON) {
                                if (firstNote[channel] == currentEvent) {
                                    firstNote[channel] = null;
                                }
                            }
                        }
                    }

                    smfPlayNote(currentEvent);
                    if (pos >= list.size()) {
                        break;
                    }
                }
            }
            try {
                if (_breakSequence) {
                    break;
                }
                if (pos >= list.size()) {
                    break;
                }
                long nextTick = list.get(pos)._tick;
                long nextMillis = tempo.TicksToMicroseconds(nextTick) / 1000;
                long differ = nextMillis - (System.currentTimeMillis() - stopWatch);;
                synchronized(this) {
                    if (differ >= 10) {
                        wait(differ - 5);
                    } else {
                        wait(1);
                    }
                }
            } catch (InterruptedException e) {
                _breakSequence = true;
            }
        }
    }

    boolean _isRunning;

    public void resetControllers() {
        if (getCurrentPosition() == 0 && !_paraPlay) {
            int start = 0, end = 15;
            if (_forceSingleChannel >= 0) {
                start = _forceSingleChannel;
                end = _forceSingleChannel;
            }
            for (int i = start; i <= end; ++i) {
                SMFMessage message = new SMFMessage(0, MXMidi.COMMAND_CONTROLCHANGE + i, MXMidi.DATA1_CC_DAMPERPEDAL, 0);
                smfPlayNote(message);
                message = new SMFMessage(0, MXMidi.COMMAND_CONTROLCHANGE + i, MXMidi.DATA1_CC_ALLNOTEOFF, 127);
                smfPlayNote(message);
                message = new SMFMessage(0, MXMidi.COMMAND_CONTROLCHANGE + i, MXMidi.DATA1_CC_EXPRESSION, 127);
                smfPlayNote(message);
                message = new SMFMessage(0, MXMidi.COMMAND_CONTROLCHANGE + i, MXMidi.DATA1_CC_CHANNEL_VOLUME, 127);
                smfPlayNote(message);
            }
        }
    }

    public void allNoteOff() {
        if (_paraPlay) {
            return;
        }
        int chFrom = 0, chTo = 15;
        if (_forceSingleChannel >= 0) {
            chFrom = _forceSingleChannel;
            chTo = _forceSingleChannel;
        }
        for (int i = chFrom; i <= chTo; ++i) {
            SMFMessage message;
            message = new SMFMessage(0, MXMidi.COMMAND_CONTROLCHANGE + i, MXMidi.DATA1_CC_DAMPERPEDAL, 0);
            smfPlayNote(message);
            message = new SMFMessage(0, MXMidi.COMMAND_CONTROLCHANGE + i, MXMidi.DATA1_CC_ALLNOTEOFF, 127);
            smfPlayNote(message);
            message = new SMFMessage(0, MXMidi.COMMAND_CONTROLCHANGE + i, MXMidi.DATA1_CC_ALLSOUNDOFF, 127);
            smfPlayNote(message);
        }
    }

    public synchronized boolean isRunning() {
        return _isRunning;
    }

    public int getCurrentPosition() {
        return _position;
    }

    public void setCurrentPosition(int pos) {
        if (pos > getLength()) {
            throw new IllegalArgumentException("timing error pos = " + pos + " length " + getLength());
        }
        _position = pos;
    }

    public int getLength() {
        return _listMessage.size();
    }
    
    boolean _noteOnly = false;
    int _forceSingleChannel = -1;
    SMFCallback _callback;
    
    public void smfPlayNote(SMFMessage smf) {
        try {
            if (smf.isBinaryMessage()) {
                _callback.smfPlayNote(smf);
            }else {
                int dword = smf.toDwordMessage();
                if (_noteOnly) {
                    boolean skip = true;

                    int status = (dword >> 16) & 0xff;
                    int data1 = (dword >> 8) & 0xff;
                    int data2 = (dword) & 0xff;

                    if (_forceSingleChannel >= 0) {
                        if (status >= 0x80 && status <= 0xef) { 
                            status = status & 0xf0 | _forceSingleChannel;
                        }
                    }

                    switch(status & 0xf0) {
                        case MXMidi.COMMAND_NOTEON:
                        case MXMidi.COMMAND_NOTEOFF:
                        case MXMidi.COMMAND_PITCHWHEEL:
                            skip = false;
                            break;
                        case MXMidi.COMMAND_CONTROLCHANGE:
                            if (data1 == MXMidi.DATA1_CC_DAMPERPEDAL
                            || data1 == MXMidi.DATA1_CC_MODULATION
                            || data1 == MXMidi.DATA1_CC_ALLNOTEOFF
                            || data1 == MXMidi.DATA1_CC_ALLSOUNDOFF) {
                                skip = false;
                            }
                            break;
                    }
                    if (skip) {
                        return;
                    }
                }
                _callback.smfPlayNote(smf);
            }
        }catch(Throwable e) {
            e.printStackTrace();
        }
    }
    
    public void setForceSingleChannel(int ch) {
        _forceSingleChannel = ch;
    }
    
    public void setFilterNoteOnly(boolean only) {
        _noteOnly = only;
    }
    
    public void setStartPosition(int pos) {
        _position = pos;
    }
    
    public int getPositionOfFirstNote() {
        int firstNoetPos = 0;
        ArrayList<SMFMessage> list = listMessage().listAll();
        int pos = 0;
        for (SMFMessage smf : list) {
            int command = smf._status; 
            if ((command & 0xf0) == MXMidi.COMMAND_NOTEON) {
                return pos;
            }
            pos ++;
        }
        return 0;
    }
}
