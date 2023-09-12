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
package jp.synthtarou.midimixer.mx30controller;

import java.awt.Color;
import java.io.File;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.smf.SMFCallback;
import jp.synthtarou.midimixer.libs.midi.smf.SMFMessage;
import jp.synthtarou.midimixer.libs.midi.smf.SMFPlayer;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGStatusForDrum {
    public static final int TYPE_SAME_CC = 10;
    public static final int TYPE_CUSTOM_CC = 11;
    public static final int TYPE_NOTES = 12;
    public static final int TYPE_SONG = 13;
    public static final int TYPE_JUMP = 14;
    public static final int TYPE_PROGRAM = 15;

    MGStatus _status;
    
    public MGStatusForDrum(MGStatus status) {
        _status = status;
        
    }
    
    int _type = TYPE_SAME_CC;

    String _notesNoteList = "";

    String _songFile = "";
    boolean _songSeekFirstNote = true;
    boolean _songSingleTrack = true;
    boolean _songFilterCC = true;
    SMFPlayer _switchSequencer = null;

    int _port = 0;
    int _channel = 0;
    
    boolean _currentSwitch = false;
    boolean _modeToggle = false;
    RangedValue _strikeZone = new RangedValue(1, 1, 127);

    protected void setSwitchSequencerFile(String switchSequencerFile) {
        if (switchSequencerFile != null) {
            if (_songFile != null) {
                if (switchSequencerFile.equals(_songFile)) {
                    return;
                }
            }
        }
        _songFile = switchSequencerFile;
        if (_switchSequencer != null) {
            _switchSequencer.stopPlayer();
        }
        _switchSequencer = null;
        if (switchSequencerFile != null && switchSequencerFile.isEmpty() == false) {
            File f = new File(switchSequencerFile);
            if (f.exists()) {
                try {
                    SMFPlayer player = new SMFPlayer(f);
                    _switchSequencer = player;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startSequence() {
        if (_switchSequencer != null) {
            if (_songSingleTrack) {
                _switchSequencer.setForceSingleChannel(_channel);
            } else {
                _switchSequencer.setForceSingleChannel(-1);
            }
            _switchSequencer.setFilterNoteOnly(_songFilterCC);
            if (_songSeekFirstNote) {
                _switchSequencer.setStartPosition(_switchSequencer.getPositionOfFirstNote());
            } else {
                _switchSequencer.setStartPosition(0);
            }
            _switchSequencer.startPlayer(new SMFCallback() {
                @Override
                public void smfPlayNote(SMFMessage e) {
                    dispatchNextLayer(e.fromSMFtoMX(_port));
                }

                @Override
                public void smfStarted() {
                }

                @Override
                public void smfStoped(boolean fineFinish) {
                }

                @Override
                public void smfProgress(int pos, int finish) {
                }
            });
        }
    }

    public void stopSequence() {
        if (_switchSequencer != null) {
            _switchSequencer.stopPlayer();
            _switchSequencer = null;
        }
    }

    public int _mouseOnValue = 127;
    public int _mouseOffValue = 0;
    boolean _dontSendOff = false;

    void mouseDetected(MXTiming timing, boolean push) {
        int velocity;
        if (push) {
            velocity = _mouseOnValue;
        }else {
            velocity = _mouseOffValue;
            if (_dontSendOff) {
                return;
            }
        }
        
        MXMessage message = _status.toMXMessage(timing);
        message.setValue(message.getValue().updateValue(velocity));
        messageDetected(message);
    }

    void messageDetected(MXMessage message) {
        // ここでも、二重エンターを、Statusでも絞り込めば可能だが、オーバーヘッドがかかるので、やらない
        if (_strikeZone.contains(message.getValue()._var)) {
            doSwitching(message._timing, true, message.getValue()._var);
        }
        else {
            doSwitching(message._timing,false, message.getValue()._var);
        }
    }

    boolean _lastDetected = false;
    boolean _lastToggled = false;
    
    void doSwitching(MXTiming timing, boolean flag, int velocity) {
        if (flag == _lastDetected) {
            return;
        }
        _lastDetected = flag;
        if (flag) {
            if (_modeToggle) {
                _lastToggled = !_lastToggled;
                flag = _lastToggled;
            }
        }
        createMessage(timing, flag, velocity);
    }
    
    MXMessage _customTemplate = null;
    int _customOutOnValue = -1;
    int _customOutOffValue = -1;
    int _jumpTo = 64;
    
    int _programNumber = 0;
    int _bankMSB = 0;
    int _bankLSB = 0;
    
    boolean _lastSent = false;
    
    void createMessage(MXTiming timing, boolean flag, int velocity) {
        MXMessage message, message2, message3;

        if (_lastSent == flag) {
            return;
        }
        _lastSent = flag;
        
        if (flag) {
            if (_customOutOnValue >= 0) {
                velocity = _customOutOnValue;
            }
        }else {
            if (_customOutOffValue >= 0) {
                velocity = _customOutOffValue;
            }
        }

        MX30Process parentProcess = MXMain.getMain().getKontrolProcess();
        MX32MixerProcess process = parentProcess.getPage(_status._port);
        MGDrumPad pad = process._data.getDrumPad(_status._row, _status._column);
        if (flag) {
            pad.updateButtonUI(true);
        }else {
            pad.updateButtonUI(false);
        }
        if (flag) {
            switch(_type) {
                case TYPE_SAME_CC:
                    message = _status.toMXMessage(timing);
                    message.setValue(message.getValue().updateValue(velocity));
                    dispatchCurrentLayer(message);
                    break;
                case TYPE_CUSTOM_CC:
                    if (_customTemplate != null) {
                        message = (MXMessage)_customTemplate.clone();
                        message._timing = timing;
                        message.setValue(RangedValue.new7bit(velocity));
                        message.setGate(_customTemplate.getGate());
                        dispatchCurrentLayer(message);
                    }
                    break;
                case TYPE_NOTES:
                    int[] noteList = MXMidi.textToNoteList(_notesNoteList);
                    for (int note : noteList) {
                        message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_NOTEON + _channel, note, velocity);
                        message._timing = timing; 
                        dispatchNextLayer(message);
                    }
                case TYPE_SONG:
                    startSequence();
                    break;
                case TYPE_JUMP:
                    int column = _status._column;
                    MGStatus slider = process._data.getSliderStatus(0, column);
                    slider.setValue(slider._base.getValue().updateValue(_jumpTo));
                    dispatchCurrentLayer(slider.toMXMessage(null));
                    break;
                case TYPE_PROGRAM:
                    message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_PROGRAMCHANGE + _channel, _programNumber, 0);
                    message2 = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_CONTROLCHANGE + _channel, MXMidi.DATA1_CC_BANKSELECT, _bankMSB);
                    message3 = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_CONTROLCHANGE + _channel, MXMidi.DATA1_CC_BANKSELECT+ 0x20, _bankLSB);
                    synchronized (MXTiming.mutex) {
                        message._timing = timing;
                        message2._timing = timing;
                        message3._timing = timing;
                        dispatchNextLayer(message2);
                        dispatchNextLayer(message3);
                        dispatchNextLayer(message);
                    }
                    break;
            }
        }
        else {
            switch(_type) {
                case TYPE_SAME_CC:
                    message = _status.toMXMessage(timing);
                    message.setValue(message.getValue().updateValue(velocity));
                    dispatchCurrentLayer(message);
                    break;
                case TYPE_CUSTOM_CC:
                    if (_customTemplate != null) {
                        message = (MXMessage)_customTemplate.clone();
                        _customTemplate._timing = timing;
                        dispatchCurrentLayer(message);
                    }
                    break;
                case TYPE_NOTES:
                    int[] noteList = MXMidi.textToNoteList(_notesNoteList);
                    for (int note : noteList) {
                        message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_NOTEOFF + _channel, note, 0);
                        message._timing = timing; 
                        dispatchNextLayer(message);
                    }
                case TYPE_SONG:
                    stopSequence();
                    break;
                case TYPE_JUMP:
                    break;
                case TYPE_PROGRAM:
                    break;
            }
        }
    }
    
    void dispatchCurrentLayer(MXMessage message) {
        MX30Process parentProcess = MXMain.getMain().getKontrolProcess();
        parentProcess.processMXMessage(message);
    }

    void dispatchNextLayer(MXMessage message) {
        if (message.takeTicket(0) == false) {
            return;
        }
        MX30Process parentProcess = MXMain.getMain().getKontrolProcess();
        parentProcess.getNextReceiver().processMXMessage(message);
    }
}
