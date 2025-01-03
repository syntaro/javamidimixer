/*
 * Copyright (C) 2024 Syntarou YOSHIDA
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
package jp.synthtarou.midimixer.mx60output;

import java.io.File;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.libs.smf.SMFCallback;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.libs.smf.SMFSequencer;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX60ViewData  {

    SMFSequencer[] _listRecorder;
    int _recordingTrack = -1;
    SMFSequencer _playingTrack;
    final MX60Process _process;

    public MX60ViewData(MX60Process process) {
        super();

        _process = process;
        _listRecorder = new SMFSequencer[5];
        _playingTrack = null;
    }

    public synchronized void record(MXMessage message) {
        if (_recordingTrack >= 0) {
            SMFSequencer recorder = _listRecorder[_recordingTrack];
            recorder.record(message);
        }
    }

    public File getSequenceDirectory(int song) {
        File base = MXUtil.getAppBaseDirectory();
        File seq = new File(base, "songs");
        File folder = new File(seq, "Song" + Character.toString('A' + song));
        if (folder.exists() == false) {
            folder.mkdirs();
            if (folder.exists() == false) {
                return null;
            }
        }
        return folder;
    }

    public long getSongLength(int track) {
        return _listRecorder[_recordingTrack].getSongLength();
    }

    public synchronized void startRecording(int x) {
        _recordingTrack = x;
        _listRecorder[x] = new SMFSequencer();
        _listRecorder[x].startRecording();
    }

    public synchronized boolean isRecording() {
        return _recordingTrack >= 0;
    }

    public synchronized void stopRecording() {
        if (_recordingTrack >= 0) {
            _listRecorder[_recordingTrack].stopRecording();
            _recordingTrack = -1;
        }
    }

    public synchronized void startPlaying(int x) {
        _playingTrack = _listRecorder[x];
        if (_playingTrack == null) {
            return;
        }
        _playingTrack.startPlayerThread(0, new SMFCallback() {
            MXNoteOffWatcher _noteOff = new MXNoteOffWatcher();

            @Override
            public void smfPlayNote(OneMessage e) {
                MXMessage message = e.toMXMessage();
                if (message == null) {
                    return;
                }
                if (message.isCommand(MXMidiStatic.COMMAND_CH_NOTEON) && message.getCompiled(2) == 0) {
                    message = MXMessageFactory.fromNoteoff(message.getPort(), message.getChannel(), message.getCompiled(1));
                }
                if (message.isCommand(MXMidiStatic.COMMAND_CH_NOTEOFF)) {
                    if (_noteOff.raiseHandler(message, message.getPort(), message.getChannel(), message.getCompiled(1))) {
                        return;
                    }
                }
                if (message.isCommand(MXMidiStatic.COMMAND_CH_NOTEON)) {
                    _noteOff.setHandler(message, message, new MXNoteOffWatcher.Handler() {
                        @Override
                        public void onNoteOffEvent(MXMessage target) {
                            MXMessage newMessage = MXMessageFactory.fromNoteoff(
                                    target.getPort(),
                                    target.getChannel(),
                                    target.getCompiled(1));
                            newMessage._owner = MXMessage.getRealOwner(target);
                            _process.sendToNext(target);
                        }
                    });
                }
                _process.sendToNext(message);
            }

            @Override
            public void smfStarted() {
                _process.getReceiverView().progressStart();
            }

            @Override
            public void smfStoped(boolean fineFinish) {
                _noteOff.allNoteOff(null);
                _process.getReceiverView().progressFinish(fineFinish);
            }

            @Override
            public void smfProgress(long pos, long finish) {
                _process.getReceiverView().progress(pos, finish);
            }
        });
    }

    public synchronized void stopPlaying() {
        if (_playingTrack == null) {
            return;
        }
        if (_playingTrack.isRunning()) {
            _playingTrack.stopPlayerAwait();
        }
        _playingTrack = null;
    }

    public synchronized boolean isPlaying() {
        return _playingTrack != null && _playingTrack.isRunning();
    }

    public boolean hasRecorning(int x) {
        if (_listRecorder == null) {
            //startup
            return false;
        }
        if (_listRecorder[x] != null) {
            return _listRecorder[x].countMessage() > 0;
        }
        return false;
    }

    public void loadSequenceData() {
        for (int i = 0; i < _listRecorder.length; ++i) {
            _listRecorder[i] = new SMFSequencer();
            File seq = getSequenceDirectory(i);
            if (seq != null) {
                System.out.println("readFromDirectory ******** " + seq);
                _listRecorder[i].readFromDirectory(seq);
                _process.getReceiverView().setSongLengthDX(i, _listRecorder[i].getSongLength());
            }
        }
    }

    public boolean saveSequenceData() {
        boolean error = false;
        for (int i = 0; i < _listRecorder.length; ++i) {
            if (_listRecorder[i] == null) {
                continue;
            }
            File seq = getSequenceDirectory(i);
            if (seq != null) {
                if (_listRecorder[i]._updateFlag) {
                    if (_listRecorder[i].writeToDirectory(seq) == false) {
                        error = true;
                    }
                    _listRecorder[i]._updateFlag = false;
                }
            } else {
                error = true;
            }
        }
        return !error;
    }

    boolean _isUsingThieRecipe = true;
}
