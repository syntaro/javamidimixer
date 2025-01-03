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
package jp.synthtarou.libs.smf;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.*;
import java.util.ArrayList;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.MXSafeThread;
import jp.synthtarou.libs.SortedArray;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.mx00playlist.MXPianoRoll;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFSequencer {

    static boolean _stopAll = false;

    public static void stopAll() {
        _stopAll = true;
    }

    private long _startMilliSeconds;
    private long _currentMilliSeconds;
    private File _lastFile;
    public SMFParser _parser;
    public MXPianoRoll _pianoRoll;
    public boolean _updateFlag = false;

    public SMFSequencer() {
        _parser = new SMFParser(); // as recorder
        _lastFile = null;
        _firstNote = new OneMessage[16];
    }

    public int getResolution() {
        return _parser._fileResolution;
    }

    public int getSMPTEFormat() {
        return _parser._smpteFormat;
    }

    public SMFSequencer(File file) throws IOException {
        _parser = new SMFParser(file);
        _firstNote = new OneMessage[16];
        _lastFile = file;
    }

    public File getLastFile() {
        return _lastFile;
    }

    public SortedArray<OneMessage> listMessage() {
        return _parser._listMessage;
    }

    boolean _stopPlayer;
    Thread _playerThread;
    int asyncLock = 0;

    public void startPlayerThread(long position, SMFCallback callback) {
        if (asyncLock > 0) {
            System.out.println("locked");
            return;
        }
        asyncLock++;
        Thread t = new MXSafeThread("SMFPlayer", () -> {
            stopPlayerAwait();
            _callback = callback;
            _playerThread = Thread.currentThread();
            _callback.smfStarted();
            try {
                asyncLock--;
                playWithMilliSeconds(position);
            } catch (RuntimeException ex) {
                MXFileLogger.getLogger(SMFSequencer.class).log(Level.WARNING, ex.getMessage(), ex);
            } finally {
            }
            _callback.smfStoped(_stopPlayer ? false : true);
            _playerThread = null;
            synchronized (SMFSequencer.this) {
                SMFSequencer.this.notifyAll();
            }
        });
        t.setDaemon(true);
        t.start();;
    }

    public void stopPlayerAwait() {
        _stopPlayer = true;
        if (_playerThread != null) {
            synchronized (this) {
                notifyAll();
                while (_playerThread != null) {
                    try {
                        wait(1000);
                    } catch (InterruptedException ex) {
                        MXFileLogger.getLogger(SMFSequencer.class).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    OneMessage[] _firstNote = null;

    protected boolean sendProgramChangeBeforeNote() {
        OneMessage[] firstProgram = new OneMessage[16];
        OneMessage[] firstBank0 = new OneMessage[16];
        OneMessage[] firstBank32 = new OneMessage[16];
        _firstNote = new OneMessage[16];

        int doneCh = 0;
        int pos = 0;

        for (OneMessage smf : _parser._listMessage) {
            int status = smf.getStatus();
            int command = status & 0xf0;
            int channel = status & 0x0f;
            int data1 = smf.getData1();

            if (_firstNote[channel] == null) {
                if (command == MXMidiStatic.COMMAND_CH_PROGRAMCHANGE) {
                    firstProgram[channel] = smf;
                } else if (command == MXMidiStatic.COMMAND_CH_CONTROLCHANGE && data1 == 0) {
                    firstBank0[channel] = smf;
                } else if (command == MXMidiStatic.COMMAND_CH_CONTROLCHANGE && data1 == 32) {
                    firstBank32[channel] = smf;
                } else if (command == MXMidiStatic.COMMAND_CH_NOTEON) {
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

        for (int ch = 0; ch < 16; ++ch) {
            if (firstProgram[ch] != null) {
                smfPlayNote(firstProgram[ch]);
                didProgramChange = true;
            }
            if (firstBank0[ch] != null) {
                smfPlayNote(firstBank0[ch]);
                didProgramChange = true;
            }
            if (firstBank32[ch] != null) {
                smfPlayNote(firstBank32[ch]);
                didProgramChange = true;
            }
        }

        if (didProgramChange) {
            try {
                MXFileLogger.getLogger(SMFSequencer.class).info("*** Sleep 150 x Program Change Count*");
                Thread.sleep(150 * doneCh);
            } catch (Exception e) {

            }
        }

        return didProgramChange;
    }

    public int _progressSpan = 500;
    long _nextDraw = 0;
    long _divDraw = 33;

    public void paintPiano(long elapsed) {
        if (_pianoRoll != null) {
            double pixel = _pianoRoll.getHeight();
            double disptime = _pianoRoll.getSoundSpan();
            while (elapsed >= _nextDraw) {
                if (pixel >= 1) {
                    double needTime = disptime / pixel;
                    _divDraw = (long) needTime;
                    if (_divDraw <= 0) {
                        _divDraw = 1;
                    }
                }
                _pianoRoll.postPosition(elapsed);
                _nextDraw += _divDraw;
            }
        }
    }

    protected void playWithMilliSeconds(long position) {
        _stopPlayer = false;
        if (_pianoRoll != null) {
            _pianoRoll.postPosition(position);
        }
        _startMilliSeconds = position;
        paintPiano(position);

        boolean[] reset = new boolean[MXConfiguration.TOTAL_PORT_COUNT];

        SortedArray<OneMessage> list = _parser._listMessage;
        int pos = 0;

        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i)._millisecond >= _startMilliSeconds) {
                pos = i;
                break;
            }
        }

        for (int i = pos; i < list.size(); ++i) {
            OneMessage message = list.get(i);
            if (reset[message._port] == false) {
                reset[message._port] = true;
                if (_startMilliSeconds == 0) {
                    allNoteOff(i);
                    resetControllers(i);
                }
            }
        }

        _firstNote = null;
        if (_startMilliSeconds == 0) {
            //sendProgramChangeBeforeNote();
        }

        long launched = System.currentTimeMillis() - _startMilliSeconds;
        long lastSent = 0;

        _nextDraw = -1;

        while (!_stopAll && !_stopPlayer && pos < list.size()) {
            long elapsed = (System.currentTimeMillis() - launched);
            long nextNote = list.get(pos)._millisecond;
            paintPiano(elapsed);
            _currentMilliSeconds = elapsed;
            while (nextNote - elapsed >= 2) {
                try {
                    long waiting = nextNote - elapsed - 2;
                    if (_nextDraw >= 0) {
                        long waiting2 = _nextDraw - elapsed;
                        if (waiting >= waiting2) {
                            waiting = waiting2;
                        }
                    }
                    if (waiting >= 1) {
                        synchronized (this) {
                            wait(waiting);
                            if (_stopPlayer || _stopAll) {
                                break;
                            }
                        }
                    }
                } catch (InterruptedException ex) {
                    break;
                }
                elapsed = (System.currentTimeMillis() - launched);
                nextNote = list.get(pos)._millisecond;
                paintPiano(elapsed);
            }
            if (lastSent + _progressSpan < elapsed) {
                _callback.smfProgress(list.get(pos)._millisecond, getMaxMilliSecond());
                lastSent = elapsed;
            }

            while (!_stopAll && !_stopPlayer && list.get(pos)._millisecond <= elapsed) {
                OneMessage currentEvent = list.get(pos++);

                smfPlayNote(currentEvent);
                if (pos >= list.size()) {
                    break;
                }
            }
            if (_stopAll || _stopPlayer) {
                break;
            }
            if (pos >= list.size()) {
                _callback.smfProgress(getMaxMilliSecond(), getMaxMilliSecond());
                break;
            }
        }

        for (int i = 0; i < MXConfiguration.TOTAL_PORT_COUNT; ++i) {
            if (reset[i]) {
                allNoteOff(i);
            }
        }
    }

    public void resetControllers(int port) {
        if (getStartMilliSecond() == 0) {
            int start = 0, end = 15;
            if (_forceSingleChannel >= 0) {
                start = _forceSingleChannel;
                end = _forceSingleChannel;
            }
            synchronized (SMFSequencer.this) {
                for (int i = start; i <= end; ++i) {
                    OneMessage message = OneMessage.thisCodes(0, MXMidiStatic.COMMAND_CH_CONTROLCHANGE + i, MXMidiStatic.DATA1_CC_DAMPERPEDAL, 0);
                    message._port = port;
                    smfPlayNote(message);
                    message = OneMessage.thisCodes(0, MXMidiStatic.COMMAND_CH_CONTROLCHANGE + i, MXMidiStatic.DATA1_CC_ALLNOTEOFF, 127);
                    message._port = port;
                    smfPlayNote(message);
                    message = OneMessage.thisCodes(0, MXMidiStatic.COMMAND_CH_CONTROLCHANGE + i, MXMidiStatic.DATA1_CC_EXPRESSION, 127);
                    message._port = port;
                    smfPlayNote(message);
                    message = OneMessage.thisCodes(0, MXMidiStatic.COMMAND_CH_CONTROLCHANGE + i, MXMidiStatic.DATA1_CC_CHANNEL_VOLUME, 127);
                    message._port = port;
                    smfPlayNote(message);
                }
                MXTemplate temp = MXMidiStatic.TEMPLATE_MASTERVOLUME;
                MXMessage msg1 = MXMessageFactory.fromTemplate(0, temp, 0, MXRangedValue.ZERO7, MXRangedValue.new7bit(127));
                OneMessage mesasge = msg1.toOneMessage(0);
                smfPlayNote(mesasge);
            }
        }
    }

    public int countMessage() {
        return _parser._listMessage.size();
    }

    public void allNoteOff(int port) {
        int chFrom = 0, chTo = 15;
        if (_forceSingleChannel >= 0) {
            chFrom = _forceSingleChannel;
            chTo = _forceSingleChannel;
        }
        for (int i = chFrom; i <= chTo; ++i) {
            OneMessage message;
            message = OneMessage.thisCodes(0, MXMidiStatic.COMMAND_CH_CONTROLCHANGE + i, MXMidiStatic.DATA1_CC_DAMPERPEDAL, 0);
            message._port = port;
            smfPlayNote(message);
            message = OneMessage.thisCodes(0, MXMidiStatic.COMMAND_CH_CONTROLCHANGE + i, MXMidiStatic.DATA1_CC_ALLNOTEOFF, 0);
            message._port = port;
            smfPlayNote(message);
            message = OneMessage.thisCodes(0, MXMidiStatic.COMMAND_CH_CONTROLCHANGE + i, MXMidiStatic.DATA1_CC_ALLSOUNDOFF, 0);
            message._port = port;
            smfPlayNote(message);
        }
    }

    public boolean isRunning() {
        return _playerThread != null;
    }

    public long getStartMilliSecond() {
        return _startMilliSeconds;
    }

    public long getCurrentMilliSeconds() {
        return _currentMilliSeconds;
    }

    public long getMaxMilliSecond() {
        long t = 0;
        ArrayList<OneMessage> list = _parser._listMessage;
        if (list.isEmpty() == false) {
            t = list.get(list.size() - 1)._millisecond;
        }
        return t + 500;
    }

    boolean _noteOnly = false;
    int _forceSingleChannel = -1;
    SMFCallback _callback;

    void smfPlayNote(OneMessage smf) {
        try {
            if (smf.isBinaryMessage()) {
                _callback.smfPlayNote(smf);
            } else {
                if (!_noteOnly) {
                    _callback.smfPlayNote(smf);
                } else {
                    boolean skip = true;
                    int status = smf.getStatus();
                    int data1 = smf.getData1();
                    int data2 = smf.getData2();

                    if (_forceSingleChannel >= 0) {
                        if (status >= 0x80 && status <= 0xef) {
                            status = status & 0xf0 | _forceSingleChannel;
                        }
                    }

                    switch (status & 0xf0) {
                        case MXMidiStatic.COMMAND_CH_NOTEON:
                        case MXMidiStatic.COMMAND_CH_NOTEOFF:
                        case MXMidiStatic.COMMAND_CH_PITCHWHEEL:
                            skip = false;
                            break;
                        case MXMidiStatic.COMMAND_CH_CONTROLCHANGE:
                            if (data1 == MXMidiStatic.DATA1_CC_DAMPERPEDAL
                                    || data1 == MXMidiStatic.DATA1_CC_MODULATION
                                    || data1 == MXMidiStatic.DATA1_CC_ALLNOTEOFF
                                    || data1 == MXMidiStatic.DATA1_CC_ALLSOUNDOFF) {
                                skip = false;
                            }
                            break;
                    }
                    if (!skip) {
                        _callback.smfPlayNote(smf);
                    }
                }
            }
        } catch (RuntimeException ex) {
            MXFileLogger.getLogger(SMFSequencer.class).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    public void setForceSingleChannel(int ch) {
        _forceSingleChannel = ch;
    }

    public void setFilterNoteOnly(boolean only) {
        _noteOnly = only;
    }

    public long getFirstNoteMilliSecond() {
        ArrayList<OneMessage> list = _parser._listMessage;
        for (OneMessage smf : list) {
            int command = smf.getStatus() & 0xf0;
            if (command == MXMidiStatic.COMMAND_CH_NOTEON) {
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
        long timing = System.currentTimeMillis();
        if (_startRecord < 0) {
            _startRecord = timing;
        }
        timing -= _startRecord;
        for (int i = 0; i < message.countOneMessage(); ++i) {
            OneMessage smf = message.toOneMessage(i);
            smf._millisecond = timing;
            smf._port = message.getPort();
            _parser._listMessage.insertSorted(smf);
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
            } else {
                return false;
            }
        }
        directory.mkdirs();
        if (directory.isDirectory() == false) {
            return false;
        }
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++port) {
            File f = new File(directory, directory.getName() + "-" + MXMidiStatic.nameOfPortInput(port) + ".mid");
            try {
                _parser.writeFile(f, port);
            } catch (IOException ex) {
                MXFileLogger.getLogger(SMFSequencer.class).log(Level.WARNING, ex.getMessage(), ex);
                return false;
            }
        }
        return true;
    }

    public boolean readFromDirectory(File directory) {
        if (directory.isDirectory() == false) {
            return false;
        }
        SortedArray<OneMessage> merge = new SortedArray<>();
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++port) {
            File f = new File(directory, directory.getName() + "-" + MXMidiStatic.nameOfPortInput(port) + ".mid");
            if (f.isFile()) {
                try {
                    SMFParser parser = new SMFParser(f);
                    for (OneMessage seek : parser._listMessage) {
                        seek._port = port;
                        merge.add(seek);
                    }
                } catch (IOException ex) {
                    MXFileLogger.getLogger(SMFSequencer.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
        _parser._listMessage.clear();
        for (OneMessage seek : merge) {
            _parser._listMessage.insertSorted(seek);
        }
        return true;
    }

    public long getSongLength() {
        try {
            OneMessage message = _parser._listMessage.get(_parser._listMessage.size() - 1);
            return message._millisecond;
        } catch (Exception e) {
            return 0;
        }
    }
}
