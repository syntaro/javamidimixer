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

import jp.synthtarou.midimixer.mx00playlist.MXPianoRoll;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.libs.MXSafeThread;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.libs.SortedArray;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;

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
    Thread _playerThread;
    int asyncLock = 0;

    public void startPlayerThread(long position, SMFCallback callback) {
        if (asyncLock > 0) {
            System.out.println("locked");
            return;
        }
        asyncLock++;
        Thread t = new MXSafeThread("SMFPlayer", () -> {
            stopPlayerAsyncAndWait();
            _callback = callback;
            _playerThread = Thread.currentThread();
            _isRunning = true;
            _callback.smfStarted();
            _playerThread = Thread.currentThread();
            try {
                asyncLock--;
                playWithMilliSeconds(position);
            } catch (RuntimeException ex) {
                MXFileLogger.getLogger(SMFSequencer.class).log(Level.WARNING, ex.getMessage(), ex);
            } finally {
                _isRunning = false;
            }
            _callback.smfStoped(_stopPlayer ? false : true);
            try {
                synchronized (this) {
                    wait(500);
                }
            } catch (InterruptedException e) {
                _stopPlayer = true;
                return;
            }
        });
        t.setDaemon(true);
        t.start();;
    }

    public void stopPlayerAsync() {
        _stopPlayer = true;
        if (_isRunning) {
            synchronized (this) {
                if (_playerThread != null) {
                    notifyAll();
                }
            }
        }
    }

    public void stopPlayerAsyncAndWait() {
        stopPlayerAsync();
        while (isRunning()) {
            synchronized (this) {
                try {
                    wait(500);
                } catch (InterruptedException ex) {
                    break;
                }
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

        for (int ch = 0; ch < 16; ++ch) {
            if (firstBank0[ch] != null) {
                smfPlayNote(firstBank0[ch]);
                didProgramChange = true;
            }
            if (firstBank32[ch] != null) {
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
                _pianoRoll.setPosition(elapsed, false);
                _nextDraw += _divDraw;
            }
        }
    }

    protected void playWithMilliSeconds(long position) {
        _stopPlayer = false;
        if (_pianoRoll != null) {
            _pianoRoll.setPosition(position, true);
        }
        _startMilliSeconds = position;
        paintPiano(position);

        boolean[] reset = new boolean[MXConfiguration.TOTAL_PORT_COUNT];

        SortedArray<SMFMessage> list = _parser._listMessage;
        int pos = 0;

        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i)._millisecond >= _startMilliSeconds) {
                pos = i;
                break;
            }
        }

        for (int i = pos; i < list.size(); ++i) {
            SMFMessage message = list.get(i);
            if (reset[message._port] == false) {
                reset[message._port] = true;
            }
        }

        if (pos == 0) {
            for (int i = 0; i < MXConfiguration.TOTAL_PORT_COUNT; ++i) {
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

        _nextDraw = 0;

        while (!_stopAll && !_stopPlayer && pos < list.size()) {
            long elapsed = (System.currentTimeMillis() - launched);
            long nextNote = list.get(pos)._millisecond;
            paintPiano(elapsed);
            _currentMilliSeconds = elapsed;
            while (nextNote - elapsed >= 5) {
                try {
                    long waiting = nextNote - elapsed - 5;
                    long waiting2 = _nextDraw - elapsed;
                    if (waiting >= waiting2) {
                        waiting = waiting2;
                    }
                    if (waiting >= 1) {
                        synchronized (this) {
                            wait(waiting);
                            if (_stopPlayer) {
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
                SMFMessage currentEvent = list.get(pos++);

                smfPlayNote(currentEvent);
                if (pos >= list.size()) {
                    break;
                }
            }
            if (_stopAll) {
                _stopPlayer = true;
            }
            if (_stopPlayer) {
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

    boolean _isRunning;

    public void resetControllers(int port) {
        if (getStartMilliSecond() == 0 && !_paraPlay) {
            int start = 0, end = 15;
            if (_forceSingleChannel >= 0) {
                start = _forceSingleChannel;
                end = _forceSingleChannel;
            }
            synchronized (SMFSequencer.this) {
                for (int i = start; i <= end; ++i) {
                    SMFMessage message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_DAMPERPEDAL, 0);
                    message._port = port;
                    smfPlayNote(message);
                    message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_ALLNOTEOFF, 127);
                    message._port = port;
                    smfPlayNote(message);
                    message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_EXPRESSION, 127);
                    message._port = port;
                    smfPlayNote(message);
                    message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_CHANNEL_VOLUME, 127);
                    message._port = port;
                    smfPlayNote(message);
                }
                MXTemplate temp = new MXTemplate(MXMidi.MASTER_VOLUME);
                MXMessage msg1 = MXMessageFactory.fromTemplate(0, temp, 0, MXRangedValue.ZERO7, MXRangedValue.new7bit(127));
                byte[]reset = msg1.getBinary();

                SMFMessage mesasge = new SMFMessage(0, reset);
                smfPlayNote(mesasge);
            }
        }
    }

    public int countMessage() {
        return _parser._listMessage.size();
    }

    public void allNoteOff(int port) {
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
            message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_DAMPERPEDAL, 0);
            message._port = port;
            smfPlayNote(message);
            message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_ALLNOTEOFF, 127);
            message._port = port;
            smfPlayNote(message);
            message = new SMFMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + i, MXMidi.DATA1_CC_ALLSOUNDOFF, 127);
            message._port = port;
            smfPlayNote(message);
        }
    }

    public boolean isRunning() {
        return _isRunning;
    }

    public long getStartMilliSecond() {
        return _startMilliSeconds;
    }

    public long getCurrentMilliSeconds() {
        return _currentMilliSeconds;
    }

    public long getMaxMilliSecond() {
        long t = 0;
        ArrayList<SMFMessage> list = _parser._listMessage;
        if (list.isEmpty() == false) {
            t = list.get(list.size() - 1)._millisecond;
        }
        return t + 500;
    }

    boolean _noteOnly = false;
    int _forceSingleChannel = -1;
    SMFCallback _callback;

    void smfPlayNote(SMFMessage smf) {
        try {
            if (smf.isBinaryMessage()) {
                _callback.smfPlayNote(smf);
            } else {
                if (!_noteOnly) {
                    _callback.smfPlayNote(smf);
                } else {
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
        long timing = System.currentTimeMillis();
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
        } else {
            for (int i = 0; i < message.getDwordCount(); ++i) {
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
            } else {
                return false;
            }
        }
        directory.mkdirs();
        if (directory.isDirectory() == false) {
            return false;
        }
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++port) {
            File f = new File(directory, directory.getName() + "-" + MXMidi.nameOfPortInput(port) + ".mid");
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
        SortedArray<SMFMessage> merge = new SortedArray<>();
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++port) {
            File f = new File(directory, directory.getName() + "-" + MXMidi.nameOfPortInput(port) + ".mid");
            if (f.isFile()) {
                try {
                    SMFParser parser = new SMFParser(f);
                    for (SMFMessage seek : parser._listMessage) {
                        seek._port = port;
                        merge.add(seek);
                    }
                } catch (IOException ex) {
                    MXFileLogger.getLogger(SMFSequencer.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
        _parser._listMessage.clear();
        for (SMFMessage seek : merge) {
            _parser._listMessage.insertSorted(seek);
        }
        return true;
    }

    public long getSongLength() {
        try {
            SMFMessage message = _parser._listMessage.get(_parser._listMessage.size() - 1);
            return message._millisecond;
        } catch (Exception e) {
            return 0;
        }
    }
}
