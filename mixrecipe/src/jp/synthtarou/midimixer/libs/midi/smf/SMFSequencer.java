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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import jp.synthtarou.midimixer.MXAppConfig;
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
    public SMFParser _parser;
    public MXPianoRoll _pianoRoll;
    public boolean _updateFlag = false;

    public SMFSequencer() {
        _parser = new SMFParser(); // as recorder
        _lastFile = null;
        _firstNote = new SMFMessage[16];
    }
    
    public int getResolution() {
        return _parser._fileResolution;
    }

    public int getSMPTEFormat() {
        return _parser._smpteFormat;
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
                _callback.smfStoped(_stopPlayer ? false : true);
                try {
                    synchronized (this) {
                        wait(500);
                    }
                } catch (InterruptedException e) {
                    _stopPlayer = true;
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
                if (firstBank0[ch] != null) {
                    smfPlayNote(timing, firstBank0[ch]);
                    didProgramChange = true;
                }
                if (firstBank32[ch] != null) {
                    smfPlayNote(timing, firstBank32[ch]);
                    didProgramChange = true;
                }
                if (firstProgram[ch] != null) {
                    smfPlayNote(timing, firstProgram[ch]);
                    didProgramChange = true;
                }
            }
        }

        if (didProgramChange) {
            try {
                System.out.println("*** Sleep 150 x Program Change Count*");
                Thread.sleep(150 * doneCh);
            } catch (Exception e) {

            }
        }

        return didProgramChange;
    }
    
    public int _progressSpan = 500;

    protected void playWithMilliSeconds() throws InvalidMidiDataException {
        _stopPlayer = false;
        
        boolean []reset = new boolean[MXAppConfig.TOTAL_PORT_COUNT];

        SortedArray<SMFMessage> list = _parser._listMessage;
        int pos = 0;

        for (int i = 0; i < list.size(); ++ i) {
            if (list.get(i)._millisecond >= _startMilliSeconds) {
                pos = i;
                break;
            }
        }

        for (int i = pos; i < list.size(); ++ i) {
            SMFMessage message = list.get(i);
            if (reset[message._port] == false) {
                reset[message._port] = true;
            }
        }

        if (pos == 0) {
            for (int i = 0; i < MXAppConfig.TOTAL_PORT_COUNT; ++ i) {
                if (reset[i]) {
                    resetControllers(i);
                }
            }
        }

        _firstNote = null;
        if (_paraPlay) {

        } else if (_startMilliSeconds == 0) {
            sendProgramChangeBeforeNote();
        }

        long launched = System.currentTimeMillis() - _startMilliSeconds;
        long lastSent = 0;
        
        if (_pianoRoll != null) {
            _pianoRoll.resetTiming(_pianoRoll.getSoundSpan(), _pianoRoll.getSoundMargin());
        }
        while (!_stopPlayer && pos < list.size()) {
            long elapsed = (System.currentTimeMillis() - launched);
            
            if (lastSent + _progressSpan < elapsed) {
                _callback.smfProgress(list.get(pos)._millisecond, getMaxMilliSecond());
                lastSent = elapsed;
            }
            _pianoRoll.setTiming(elapsed);
            
            while (!_stopPlayer && list.get(pos)._millisecond <= elapsed) {
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

                    smfPlayNote(null, currentEvent);
                    if (pos >= list.size()) {
                        break;
                    }
                }
            }
            if (_stopPlayer) {
                break;
            }
            if (pos >= list.size()) {
                _callback.smfProgress(getMaxMilliSecond(), getMaxMilliSecond());
                break;
            }
        }

        for (int i = 0; i < MXAppConfig.TOTAL_PORT_COUNT; ++ i) {
            if (reset[i]) {
                allNoteOff(null, i);
            }
        }
    }

    boolean _isRunning;

    public void resetControllers(int port) {
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
                    message._port = port;
                    smfPlayNote(timing, message);
                    message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_ALLNOTEOFF, 127);
                    message._port = port;
                    smfPlayNote(timing, message);
                    message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_EXPRESSION, 127);
                    message._port = port;
                    smfPlayNote(timing, message);
                    message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_CHANNEL_VOLUME, 127);
                    message._port = port;
                    smfPlayNote(timing, message);
                }
            }
        }
    }

    public int countMessage() {
        return _parser._listMessage.size();
    }
    
    public void allNoteOff(MXTiming timing, int port) {
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
                message._port = port;
                smfPlayNote(timing, message);
                message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_ALLNOTEOFF, 127);
                message._port = port;
                smfPlayNote(timing, message);
                message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_ALLSOUNDOFF, 127);
                message._port = port;
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
            t = list.get(list.size()  -1)._millisecond;
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
                    if (!_noteOnly) {
                        _callback.smfPlayNote(timing, smf);
                    }else {
                        boolean skip = true;
                        int dword = smf.toDwordMessage();

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
                return smf._millisecond;
            }
        }
        return 0;
    }

    boolean _recording = false;
    long _startRecord;
    
    public synchronized void startRecording() {
        _parser = new SMFParser();
        _updateFlag = true;
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
            smf._millisecond = timing;
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
                smf._millisecond = timing;
                smf._port = message.getPort();
                _parser._listMessage.insertSorted(smf);
            }
        }
               
    }
    
    public synchronized void stopRecording() {
        if (_recording) {
            _recording = false;
        }
    }
    
    public boolean writeToDirectory(File directory) {
        if (directory.exists()) {
            if (directory.isDirectory()) {
                File[] children = directory.listFiles();
                for (File f : children) {
                    Desktop.getDesktop().moveToTrash(f);
                }
            }else {
                return false;
            }
        }
        directory.mkdirs();
        if (directory.isDirectory() == false) {
            return false;
        }
        for (int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++ port) {
            File f = new File(directory, directory.getName() + "-" + MXMidi.nameOfPortInput(port) + ".mid");
            try {
                _parser.writeFile(f, port);
            }catch(IOException e) {
                e.printStackTrace();;
                return false;
            }
        }
        return true;
    }
    
    public boolean readFromDirectory(File directory) {
        if (directory.isDirectory() == false) {
            return false;
        }
        SortedArray<SMFMessage> marge  = new SortedArray<>();
        for (int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++ port) {
            File f = new File(directory, directory.getName() + "-" + MXMidi.nameOfPortInput(port) + ".mid");
            if (f.isFile()) {
                try {
                    SMFParser parser = new SMFParser(f);
                    for (SMFMessage seek : parser._listMessage) {
                        seek._port = port;
                        marge.add(seek);
                    }
                }catch(IOException e) {
                    e.printStackTrace();;
                }
            }
        }
        _parser._listMessage.clear();
        for (SMFMessage seek : marge) {            
            _parser._listMessage.insertSorted(seek);
        }
        return true;
    }

    public long getSongLength() {
        try {
            SMFMessage message = _parser._listMessage.getLast();
            return message._millisecond;
        }catch(Exception e) {
            return 0;
        }
    }
}
