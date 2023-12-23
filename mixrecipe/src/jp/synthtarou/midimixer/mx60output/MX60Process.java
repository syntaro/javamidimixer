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
package jp.synthtarou.midimixer.mx60output;

import java.io.File;
import javax.swing.JPanel;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.smf.SMFCallback;
import jp.synthtarou.midimixer.libs.midi.smf.SMFMessage;
import jp.synthtarou.midimixer.libs.midi.smf.SMFSequencer;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.mx10input.MX10Data;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX60Process extends MXReceiver implements MXSettingTarget {
    public MX60Data _data;
    private MX60View _view;
    private SMFSequencer[] _listRecorder;
    private int _recordingTrack = -1;
    private SMFSequencer _playingTrack;

    MXSetting _setting;

    public MX60Process() {
        _data = new MX60Data();
        _view = new MX60View(this);
        _setting = new MXSetting("OutputSkip");
        _setting.setTarget(this);
        _listRecorder = new SMFSequencer[5];
        for (int i = 0; i < _listRecorder.length; ++ i) {
            _listRecorder[i] = new SMFSequencer();
            File seq = getSequenceDirectory(i);
            if (seq != null) {
                _listRecorder[i].readFromDirectory(seq);
                _view.setSongLength(i, _listRecorder[i].getSongLength());
            }
        }
        _view.enableRecordingButton();
        _playingTrack = null;
    }
    
    public void readSettings() {
        _setting.readSettingFile();
    }
    
    @Override
    public void processMXMessage(MXMessage message) {
        if (isUsingThisRecipe() && _data.isMarkedAsSkip(message)) {
            return;
        }

        if (isRecording()) {
            synchronized (this) {
                SMFSequencer recorder = _listRecorder[_recordingTrack];
                recorder.record(message);
                _view.setSongLength(_recordingTrack, recorder.getSongLength());
            }
        }

        sendToNext(message);
    }

    @Override
    public String getReceiverName() {
        return "Output Dispatcher";
    }

    @Override
    public JPanel getReceiverView() {
        return _view;
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
        for (int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            StringBuffer str = new StringBuffer();
            for (int j = 0; j <_data.countOfTypes(); ++ j) {
                String name = _data.typeNames[j];
                //System.out.println(name + " = " + setting.getSetting(prefix + name));
                boolean set = setting.getSettingAsBoolean(prefix + name, false);
                _data.setSkip(port, j, set);
            }
        }
        _view.resetTableModel();
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        saveSequenceData();
        for (int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++ port) {
            String prefix = "Setting[" + port + "].";
            StringBuffer str = new StringBuffer();
            for (int j = 0; j <_data.countOfTypes(); ++ j) {
                boolean set = _data.isSkip(port, j);
                String name = _data.typeNames[j];
                setting.setSetting(prefix + name, set);
            }
        }
    }

    @Override
    public void prepareSettingFields(MXSetting setting) {
        String prefix = "Setting[].";
        for (String text : MX10Data.typeNames) {
            setting.register(prefix + text);
        }
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
        //_playingTrack.setStartMilliSecond(_playingTrack.getFirstNoteMilliSecond());
        _playingTrack.startPlayer(new SMFCallback() {
            MXNoteOffWatcher _noteOff = new MXNoteOffWatcher();

            @Override
            public void smfPlayNote(MXTiming timing, SMFMessage e) {
                MXMessage message = e.fromSMFtoMX();
                if (message == null) {
                    return;
                }
                synchronized (MXTiming.mutex) {
                    if (message.isCommand(MXMidi.COMMAND_CH_NOTEON) && message.getData2() == 0) {
                        message = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_CH_NOTEOFF + message.getChannel(), message.getData1(), 0);
                        message._timing = timing;
                    }
                    if (message.isCommand(MXMidi.COMMAND_CH_NOTEOFF)) {
                        if (_noteOff.raiseHandler(message.getPort(), message._timing, message.getChannel(), message.getData1())) {
                            return;
                        }
                    }
                    if (message.isCommand(MXMidi.COMMAND_CH_NOTEON)) {
                        _noteOff.setHandler(message, message, new MXNoteOffWatcher.Handler() {
                            @Override
                            public void onNoteOffEvent(MXTiming timing, MXMessage target) {
                                MXMessage noteOff = MXMessageFactory.fromShortMessage(
                                        target.getPort(), 
                                        MXMidi.COMMAND_CH_NOTEOFF + target.getChannel(), 
                                        target.getData1(), 
                                        0);
                                noteOff._timing = timing;
                                sendToNext(target);
                            }
                        });
                    }
                    if (isUsingThisRecipe() && _data.isMarkedAsSkip(message)) {
                    }
                    else {
                        if (MXMidi.isReset(message.getBinary())) {
                            System.out.println("Meow *****************");
                        }
                        sendToNext(message);
                    }
                }
            }

            @Override
            public void smfStarted() {
                _view.progressStart();
            }

            @Override
            public void smfStoped(boolean fineFinish) {
                _noteOff.allNoteOff(null);
                _view.progressFinish(fineFinish);
            }

            @Override
            public void smfProgress(long pos, long finish) {
                _view.progress(pos, finish);
            }
        });
    }
    
    public synchronized void stopPlaying() {
        if (_playingTrack == null) {
            return;
        }
        if (_playingTrack.isRunning()) {
            _playingTrack.stopPlayer();
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
        return _listRecorder[x].countMessage() > 0;
    }

    public boolean saveSequenceData() {
        boolean error = false;
        for (int i = 0; i < _listRecorder.length; ++ i) {
            File seq = getSequenceDirectory(i);
            if (seq != null) {
                if (_listRecorder[i]._updateFlag) {
                    if (_listRecorder[i].writeToDirectory(seq) == false) {
                        error = true;
                    }
                    _listRecorder[i]._updateFlag = false;
                }
            }
            else {
                error = true;
            }
        }
        return !error;
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
}
