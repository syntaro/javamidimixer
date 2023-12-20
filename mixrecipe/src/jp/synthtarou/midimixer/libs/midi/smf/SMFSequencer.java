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
import javax.sound.midi.InvalidMidiDataException;
import jp.synthtarou.midimixer.MXThreadList;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.mx36ccmapping.SortedArray;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFSequencer {
    private long _startMilliSeconds;
    private File _lastFile;
    boolean _paraPlay = false;
    private SMFParser _parser;

    public SMFSequencer() {
        _parser = new SMFParser(); // as recorder
        _lastFile = null;
        _firstNote = new SMFMessage[16];
    }

    public SMFSequencer(File file) throws IOException {
        _parser = new SMFParser(file);
        _firstNote = new SMFMessage[16];
        _lastFile = file;
    }

    public File getLastFile() {
        return _lastFile;
    }

    public SortedArray<SMFMessage> listMessage() {
        return _parser._listMessage;
    }

    boolean _stopPlayer;

    public void startPlayer(SMFCallback callback) {
        if (isRunning()) {
            stopPlayer();
        }
        _callback = callback;
        Thread t = MXThreadList.newThread("SMFPlayer", new Runnable() {
            @Override
            public void run() {
                _callback.smfStarted();
                try {
                    resetControllers();
                    playWithMilliSeconds();
                    _isRunning = false;
                } catch (Throwable e) {
                    _isRunning = false;
                    e.printStackTrace();
                } finally {
                    synchronized (this) {
                        notifyAll();
                    }
                }
                allNoteOff(null);
                _callback.smfStoped(_stopPlayer ? false : true);
                try {
                    Thread.sleep(500);
                }catch(InterruptedException e) {

                }
            }
        });
        t.setDaemon(true);
        t.start();
        _isRunning = true;
    }

    public void stopPlayer() {
        _stopPlayer = true;
        synchronized (this) {
            notifyAll();
        }
        if (_isRunning) {
            while (_isRunning) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
    }

    SMFMessage[] _firstNote = null;

    protected boolean sendProgramChangeBeforeNote() {
        SMFMessage[] firstProgram = new SMFMessage[16];
        SMFMessage[] firstBank0 = new SMFMessage[16];
        SMFMessage[] firstBank32 = new SMFMessage[16];
        _firstNote = new SMFMessage[16];

        int doneCh = 0;
        int pos = 0;
        
        for (SMFMessage smf : _parser._listMessage) {
            int status = smf.getStatus();
            int command = status & 0xf0;
            int channel = status & 0x0f;
            int data1 = smf.getData1();

            if (_firstNote[channel] == null) {
                if (command == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
                    firstProgram[channel] = smf;
                } else if (command == MXMidi.COMMAND_CH_CONTROLCHANGE && data1 == 0) {
                    firstBank0[channel] = smf;
                } else if (command == MXMidi.COMMAND_CH_CONTROLCHANGE && data1 == 32) {
                    firstBank32[channel] = smf;
                } else if (command == MXMidi.COMMAND_CH_NOTEON) {
                    _firstNote[channel] = smf;
                    doneCh++;
                    if (doneCh >= 16) {
                        break;
                    }
                }
            }
            pos++;
        }

        boolean didProgramChange = false;

        synchronized (MXTiming.mutex) {
            MXTiming timing = new MXTiming();
            for (int ch = 0; ch < 16; ++ch) {
                if (firstProgram[ch] != null) {
                    smfPlayNote(timing, firstProgram[ch]);
                    didProgramChange = true;
                }
                if (firstBank0[ch] != null && firstBank32[ch] != null) {
                    smfPlayNote(timing, firstBank0[ch]);
                    smfPlayNote(timing, firstBank32[ch]);
                    didProgramChange = true;
                }
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

    protected void playWithMilliSeconds() throws InvalidMidiDataException {
        _stopPlayer = false;

        int pos = 0;
        
        SortedArray<SMFMessage> list = _parser._listMessage;
        for (int i = 0; i < list.size(); ++ i) {
            if (list.get(i)._milliSeconds >= _startMilliSeconds) {
                pos = i;
                break;
            }
        }

        if (_paraPlay) {

        } else if (_startMilliSeconds == 0) {
            sendProgramChangeBeforeNote();
        }

        long launched = System.currentTimeMillis() - _startMilliSeconds;
        long lastSent = 0;
        
        while (!_stopPlayer && pos < list.size()) {
            long elapsed = (System.currentTimeMillis() - launched);
            
            if (lastSent + 500 < elapsed) {
                _callback.smfProgress(list.get(pos)._milliSeconds, getMaxMilliSecond());
                lastSent = elapsed;
            }
            
            while (!_stopPlayer && list.get(pos)._milliSeconds <= elapsed) {
                SMFMessage currentEvent = list.get(pos++);

                if (currentEvent.isBinaryMessage() || currentEvent.isMetaMessage()) {
                    smfPlayNote(null, currentEvent);
                    if (pos >= list.size()) {
                        break;
                    }
                } else if (currentEvent.getStatus() >= 0x80) {
                    int status = currentEvent.getStatus();
                    int data1 = currentEvent.getData1();
                    int data2 = currentEvent.getData2();
                    int command = status & 0xf0;
                    int channel = status & 0x0f;

                    if (_firstNote != null) {
                        if (_firstNote[channel] != null) {
                            if (command == MXMidi.COMMAND_CH_PROGRAMCHANGE
                                    || (command == MXMidi.COMMAND_CH_CONTROLCHANGE && (data1 == 0 || data1 == 32))) {
                                continue;
                            } else if (command == MXMidi.COMMAND_CH_NOTEON) {
                                if (_firstNote[channel] == currentEvent) {
                                    _firstNote[channel] = null;
                                }
                            }
                        }
                    }

                    smfPlayNote(null, currentEvent);
                    if (pos >= list.size()) {
                        break;
                    }
                }
            }
            try {
                if (_stopPlayer) {
                    break;
                }
                if (pos >= list.size()) {
                    break;
                }
                long nextTick = list.get(pos)._milliSeconds;
                long differ = nextTick - System.currentTimeMillis();
                synchronized (this) {
                    if (differ >= 500) {
                        wait(500);
                    }else if (differ >= 10) {
                        wait(differ - 5);
                    } else {
                        wait(1);
                    }
                }
            } catch (InterruptedException e) {
                _stopPlayer = true;
            }
        }
    }

    boolean _isRunning;

    public void resetControllers() {
        if (getStartMilliSecond() == 0 && !_paraPlay) {
            int start = 0, end = 15;
            if (_forceSingleChannel >= 0) {
                start = _forceSingleChannel;
                end = _forceSingleChannel;
            }            
            synchronized (MXTiming.mutex) {
                MXTiming timing = new MXTiming();
                for (int i = start; i <= end; ++i) {
                    SMFMessage message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_DAMPERPEDAL, 0);
                    smfPlayNote(timing, message);
                    message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_ALLNOTEOFF, 127);
                    smfPlayNote(timing, message);
                    message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_EXPRESSION, 127);
                    smfPlayNote(timing, message);
                    message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_CHANNEL_VOLUME, 127);
                    smfPlayNote(timing, message);
                }
            }
        }
    }

    public int countMessage() {
        return _parser._listMessage.size();
    }
    
    public void allNoteOff(MXTiming timing) {
        if (_paraPlay) {
            return;
        }
        int chFrom = 0, chTo = 15;
        if (_forceSingleChannel >= 0) {
            chFrom = _forceSingleChannel;
            chTo = _forceSingleChannel;
        }
        synchronized (MXTiming.mutex) {
            if (timing == null) {
                timing = new MXTiming();
            }
            for (int i = chFrom; i <= chTo; ++i) {
                SMFMessage message;
                message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_DAMPERPEDAL, 0);
                smfPlayNote(timing, message);
                message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_ALLNOTEOFF, 127);
                smfPlayNote(timing, message);
                message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_ALLSOUNDOFF, 127);
                smfPlayNote(timing, message);
            }
        }
    }

    public synchronized boolean isRunning() {
        return _isRunning;
    }

    public long getStartMilliSecond() {
        return _startMilliSeconds;
    }

    public void setStartMilliSecond(long pos) {
        _startMilliSeconds = pos;
    }

    public long getMaxMilliSecond() {
        long t = 0;
        ArrayList<SMFMessage> list = _parser._listMessage;
        if (list.isEmpty() == false) {
            t = list.get(list.size()  -1)._milliSeconds;
        }
        return t + 500;
    }

    boolean _noteOnly = false;
    int _forceSingleChannel = -1;
    SMFCallback _callback;

    void smfPlayNote(MXTiming timing, SMFMessage smf) {
        try {
            synchronized (MXTiming.mutex) {
                if (timing == null) {
                    timing = new MXTiming();
                }
                if (smf.isBinaryMessage()) {
                    _callback.smfPlayNote(timing, smf);
                } else {
                    int dword = smf.toDwordMessage();
                    if (!_noteOnly) {
                        _callback.smfPlayNote(timing, smf);
                    }else {
                        boolean skip = true;

                        int status = (dword >> 16) & 0xff;
                        int data1 = (dword >> 8) & 0xff;
                        int data2 = (dword) & 0xff;

                        if (_forceSingleChannel >= 0) {
                            if (status >= 0x80 && status <= 0xef) {
                                status = status & 0xf0 | _forceSingleChannel;
                            }
                        }

                        switch (status & 0xf0) {
                            case MXMidi.COMMAND_CH_NOTEON:
                            case MXMidi.COMMAND_CH_NOTEOFF:
                            case MXMidi.COMMAND_CH_PITCHWHEEL:
                                skip = false;
                                break;
                            case MXMidi.COMMAND_CH_CONTROLCHANGE:
                                if (data1 == MXMidi.DATA1_CC_DAMPERPEDAL
                                    || data1 == MXMidi.DATA1_CC_MODULATION
                                    || data1 == MXMidi.DATA1_CC_ALLNOTEOFF
                                    || data1 == MXMidi.DATA1_CC_ALLSOUNDOFF) {
                                    skip = false;
                                }
                                break;
                        }
                        if (!skip) {
                            _callback.smfPlayNote(timing, smf);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void setForceSingleChannel(int ch) {
        _forceSingleChannel = ch;
    }

    public void setFilterNoteOnly(boolean only) {
        _noteOnly = only;
    }

    public long getFirstNoteMilliSecond() {
        ArrayList<SMFMessage> list = _parser._listMessage;
        for (SMFMessage smf : list) {
            int command = smf.getStatus() & 0xf0;
            if (command == MXMidi.COMMAND_CH_NOTEON) {
                return smf._milliSeconds;
            }
        }
        return 0;
    }

    boolean _recording = false;
    long _startRecord;
    
    public synchronized void startRecording() {
        _parser._listMessage.clear();
        _recording = true;
        _startRecord = -1;
    }
    
    public synchronized void record(MXMessage message) {
        if (_recording == false) {
            return;
        }
        long timing = message._timing._clock;
        if (_startRecord < 0) {
            _startRecord = timing;
        }
        timing -= _startRecord;
        if (message.isBinaryMessage()) {
            byte[] data = message.getBinary();
            SMFMessage smf = new SMFMessage(0, data);
            smf._milliSeconds = timing;
            smf._port = message.getPort();
            _parser._listMessage.insertSorted(smf);
        }
        else {
            for (int i = 0; i < message.getDwordCount(); ++ i) {
                int dword = message.getAsDword(i);
                int status = (dword >> 16) & 0xff;
                int data1 = (dword >> 8) & 0xff;
                int data2 = dword & 0xff;
                SMFMessage smf = new SMFMessage(0, status, data1, data2);
                smf._milliSeconds = timing;
                smf._port = message.getPort();
                _parser._listMessage.insertSorted(smf);
            }
        }
               
    }
    
    public synchronized void stopRecording() {
        if (_recording) {
            _recording = false;
            _parser.calcSMPTEUseMilliseconds();
        }
    }
}
