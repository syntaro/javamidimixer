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

import java.io.File;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
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
    public static final int STYLE_SAME_CC = 10;
    public static final int STYLE_CUSTOM_CC = 11;
    public static final int STYLE_NOTES = 12;
    public static final int STYLE_SEQUENCE = 13;
    public static final int STYLE_LINK_SLIDER = 14;
    public static final int STYLE_PROGRAM_CHANGE = 15;
    public static final int STYLE_DONT_SEND = 16;

    public static final int VALUETYPE_AS_INPUT = 20;
    public static final int VALUETYPE_AS_MOUSE = 21;
    public static final int VALUETYPE_NOTHING = 22;

    public static final int PROGRAM_SET = 30;
    public static final int PROGRAM_INC = 31;
    public static final int PROGRAM_DEC = 32;

    static MXWrapList<Integer> _typeMap = new MXWrapList();
    
    int _outStyle = STYLE_SAME_CC;
    int _outValueTypeOn = VALUETYPE_AS_INPUT;
    int _outValueTypeOff = VALUETYPE_AS_INPUT;

    String _harmonyNotes = "";

    String _sequencerFile = "";
    boolean _sequencerSeekStart = true;
    boolean _sequencerSingleTrack = true;
    boolean _sequencerFilterNote = true;
    SMFPlayer _songFilePlayer = null;

    int _outPort = 0;
    int _outChannel = 0;
    
    boolean _currentSwitch = false;
    boolean _modeToggle = false;
    boolean _onlySwitched = false; //TODO
    RangedValue _strikeZone = new RangedValue(0, 1, 127);

    public int _mouseOnValue = 127;
    public int _mouseOffValue = 0;
    boolean _dontSendOff = false;

    String _templateText = "";
    int _teplateTextGate = 0;

    int _LinkRow = 0; //slider = 0; knob = 1~4
    int _LinkColumn = -1; // -1=same, 0~16;
    
    static {
        _typeMap.addNameAndValue("CC", STYLE_CUSTOM_CC);
        _typeMap.addNameAndValue("Note", STYLE_NOTES);
        _typeMap.addNameAndValue("Song", STYLE_SEQUENCE);
        _typeMap.addNameAndValue("SliderJump", STYLE_LINK_SLIDER);
        _typeMap.addNameAndValue("ProgramChange", STYLE_PROGRAM_CHANGE);
    }

    public String getTypeAsText() {
        int x = _typeMap.indexOfValue(_outStyle);
        if (x >= 0) {
            return _typeMap.nameOfIndex(x);
        }
        return "?";
    }
    
    public void setTypeByText(String name) {
        int x = _typeMap.indexOfName(name);
        if (x >= 0) {
            _outStyle = _typeMap.valueOfIndex(x);
            return;
        }
        _outStyle = STYLE_PROGRAM_CHANGE;
    }
    
    MGStatus _status;
    
    public MGStatusForDrum(MGStatus status) {
        _status = status;
        _customTemplate = new MXTemplate("");
    }
    
    protected void setSwitchSongFile(String switchSongFile) {
        if (switchSongFile != null) {
            if (_sequencerFile != null) {
                if (switchSongFile.equals(_sequencerFile)) {
                    return;
                }
            }
        }
        _sequencerFile = switchSongFile;
        if (_songFilePlayer != null) {
            _songFilePlayer.stopPlayer();
        }
        _songFilePlayer = null;
        if (switchSongFile != null && switchSongFile.isEmpty() == false) {
            File f = new File(switchSongFile);
            if (f.exists()) {
                try {
                    SMFPlayer player = new SMFPlayer(f);
                    _songFilePlayer = player;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startSongPlayer() {
        if (_songFilePlayer != null) {
            if (_sequencerSingleTrack) {
                _songFilePlayer.setForceSingleChannel(_outChannel);
            } else {
                _songFilePlayer.setForceSingleChannel(-1);
            }
            _songFilePlayer.setFilterNoteOnly(_sequencerFilterNote);
            if (_sequencerSeekStart) {
                _songFilePlayer.setStartPosition(_songFilePlayer.getPositionOfFirstNote());
            } else {
                _songFilePlayer.setStartPosition(0);
            }
            _songFilePlayer.startPlayer(new SMFCallback() {
                @Override
                public void smfPlayNote(SMFMessage e) {
                    dispatchNextLayer(e.fromSMFtoMX(_outPort));
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

    public void stopSongPlayer() {
        if (_songFilePlayer != null) {
            _songFilePlayer.stopPlayer();
            _songFilePlayer = null;
        }
    }

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
        
        MXMessage message = (MXMessage) _status._base.clone();
        message._timing = timing;
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
    
    MXTemplate _customTemplate = null;
    RangedValue _customGate = RangedValue.ZERO7; // TODO
    int _customOutOnValue = -1;
    int _customOutOffValue = -1;
    int _jumpTo = 64;
    
    int _programType = 0;
    int _programNumber = 0;
    int _programMSB = 0;
    int _programLSB = 0;
    
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
            switch(_outStyle) {
                case STYLE_SAME_CC:
                    message = (MXMessage)_status._base.clone();
                    message._timing = timing;
                    message.setValue(message.getValue().updateValue(velocity));
                    dispatchCurrentLayer(message);
                    break;
                case STYLE_CUSTOM_CC:
                    if (_customTemplate != null) {
                        message = MXMessageFactory.fromTemplate(_outPort, _customTemplate, _outChannel, _customGate, RangedValue.new7bit(velocity));
                        message._timing = timing; 
                        dispatchCurrentLayer(message);
                    }
                    break;
                case STYLE_NOTES:
                    int[] noteList = MXMidi.textToNoteList(_harmonyNotes);
                    for (int note : noteList) {
                        message = MXMessageFactory.fromShortMessage(_outPort, MXMidi.COMMAND_CH_NOTEON + _outChannel, note, velocity);
                        message._timing = timing; 
                        dispatchNextLayer(message);
                    }
                case STYLE_SEQUENCE:
                    startSongPlayer();
                    break;
                case STYLE_LINK_SLIDER:
                    int column = _status._column;
                    MGStatus slider = process._data.getSliderStatus(0, column);
                    slider.setValue(slider._base.getValue().updateValue(_jumpTo));
                    message = (MXMessage)slider._base.clone();
                    message._timing = timing;
                    dispatchCurrentLayer(message);
                    break;
                case STYLE_PROGRAM_CHANGE:
                    message = MXMessageFactory.fromShortMessage(_outPort, MXMidi.COMMAND_CH_PROGRAMCHANGE + _outChannel, _programNumber, 0);
                    message2 = MXMessageFactory.fromShortMessage(_outPort, MXMidi.COMMAND_CH_CONTROLCHANGE + _outChannel, MXMidi.DATA1_CC_BANKSELECT, _programMSB);
                    message3 = MXMessageFactory.fromShortMessage(_outPort, MXMidi.COMMAND_CH_CONTROLCHANGE + _outChannel, MXMidi.DATA1_CC_BANKSELECT+ 0x20, _programLSB);
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
            switch(_outStyle) {
                case STYLE_SAME_CC:
                    message = (MXMessage)_status._base.clone();
                    message._timing = timing;
                    message.setValue(message.getValue().updateValue(velocity));
                    dispatchCurrentLayer(message);
                    break;
                case STYLE_CUSTOM_CC:
                    if (_customTemplate != null) {
                        message = MXMessageFactory.fromTemplate(_outPort, _customTemplate, _outChannel, _customGate, RangedValue.new7bit(velocity));
                        message._timing = timing;
                        dispatchCurrentLayer(message);
                    }
                    break;
                case STYLE_NOTES:
                    int[] noteList = MXMidi.textToNoteList(_harmonyNotes);
                    for (int note : noteList) {
                        message = MXMessageFactory.fromShortMessage(_outPort, MXMidi.COMMAND_CH_NOTEOFF + _outChannel, note, 0);
                        message._timing = timing; 
                        dispatchNextLayer(message);
                    }
                case STYLE_SEQUENCE:
                    stopSongPlayer();
                    break;
                case STYLE_LINK_SLIDER:
                    break;
                case STYLE_PROGRAM_CHANGE:
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
    
    public int[] getHarmonyNotesAsArray() {
        return MXMidi.textToNoteList(_harmonyNotes);
    }
    
    public void setHarmoyNotesAsArray(int[] array) {
        _harmonyNotes = MXMidi.noteListToText(array);
    }
}
