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
package jp.synthtarou.midimixer.mx30surface;

import java.io.File;
import java.io.IOException;
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
public class MGStatusForDrum implements Cloneable {

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
    
    public static final int LINKMODE_VALUE = 40;
    public static final int LINKMODE_INC = 41;
    public static final int LINKMODE_DEC = 42;
    public static final int LINKMODE_MAX = 43;
    public static final int LINKMODE_MIN = 44;
    public static final int LINKMODE_MIDDLE = 45;

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

    String _templateText = "";
    int _teplateTextGate = 0;

    int _linkMode = MGStatusForDrum.LINKMODE_VALUE;
    int _linkKontrolType = MGStatus.TYPE_SLIDER;
    int _linkRow = 0;
    int _linkColumn = -1; // -1=same, 0~16;
    
    MGStatus _status;

    boolean _lastDetected = false;
    boolean _lastToggled = false;
    MXTemplate _customTemplate = null;
    RangedValue _customGate = RangedValue.ZERO7; // TODO
    int _customOutOnValue = -1;
    int _customOutOffValue = -1;
    
    int _programType = 0;
    int _programNumber = 0;
    int _programMSB = 0;
    int _programLSB = 0;

    boolean _lastSent = false;

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
        if (_songFilePlayer != null) {
            _songFilePlayer.stopPlayer();
            _songFilePlayer = null;
        }
        _sequencerFile = switchSongFile;
        _songFilePlayer = null;
    }

    public void startSongPlayer() {
        int port = _outPort;
        if (port < 0) {
            port = _status._port;
        }
        int channel = _outChannel;
        if (channel < 0) {
            channel = _status._base.getChannel();
        }

        if (_songFilePlayer == null) {
            try {
                if (_sequencerFile.isEmpty()) {
                    return;
                }
                File f = new File(_sequencerFile);
                if (f == null || f.exists() == false) {
                    return;
                }
                _songFilePlayer = new SMFPlayer(new File(_sequencerFile));
            }catch(IOException ioe) {
                return;
            }
        }
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
        final int _port = port;
        final int _channel = channel;
        _songFilePlayer.startPlayer(new SMFCallback() {
            @Override
            public void smfPlayNote(MXTiming timing, SMFMessage e) {
                MXMessage message = e.fromSMFtoMX(_port);
                if (_sequencerSingleTrack) {                        
                    if (_channel >= 0) {
                        message.setChannel(_channel);
                    }
                }
                message._timing = timing;
                MXMain.getMain().messageDispatch(message, _status._mixer._parent.getNextReceiver());
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

    public void stopSongPlayer() {
        if (_songFilePlayer != null) {
            _songFilePlayer.stopPlayer();
        }
    }

    MXMessage mouseDetected(MXTiming timing, boolean push) {
        int velocity;
        if (push) {
            if (_outValueTypeOn == VALUETYPE_NOTHING) {
                return null;
            }
            _status.setMessageValue(_mouseOnValue);
        } else {
            if (_outValueTypeOff == VALUETYPE_NOTHING) {
                return null;
            }
            _status.setMessageValue(_mouseOffValue);
        }

        return messageDetected();
    }

    MXMessage messageDetected() {
        boolean flag = _strikeZone.contains(_status.getValue()._var);

        if (flag == _lastDetected) {
            return null;
        }
        _lastDetected = flag;
        if (flag) {
            if (_modeToggle) {
                _lastToggled = !_lastToggled;
                flag = _lastToggled;
            }
        }
        else {
            if (_modeToggle) {
                return null;
            }
        }
        return doAction(flag);
    }


    MXMessage doAction(boolean flag) {
        MXMessage message;

        if (_lastSent == flag) {
            return null;
        }
        _lastSent = flag;
        int velocity = _status.getValue()._var;

        if (flag) {
            if (_customOutOnValue >= 0) {
                velocity = _customOutOnValue;
            }
        } else {
            if (_customOutOffValue >= 0) {
                velocity = _customOutOffValue;
            }
        }

        MX30Process parentProcess = MXMain.getMain().getKontrolProcess();
        MX32Mixer mixer = _status._mixer;
        MGDrumPad pad = mixer.getDrumPad(_status._row, _status._column);
        int port = _outPort;
        if (port < 0) {
            port = _status._port;
        }
        int channel = _outChannel;
        if (channel < 0) {
            channel = _status._base.getChannel();
        }
        if (_strikeZone.contains(_status.getValue()._var)) {
            pad.setDrumActive(true);
        } else {
            pad.setDrumActive(false);
        }
        if (flag) {
            switch(_outValueTypeOn) {
                case VALUETYPE_AS_INPUT:
                    velocity = _status.getValue()._var;
                    break;
                case VALUETYPE_AS_MOUSE:
                    velocity = _mouseOnValue;
                    break;
                case VALUETYPE_NOTHING:
                    return null;
            }
            switch (_outStyle) {
                case STYLE_SAME_CC:
                    message = (MXMessage) _status._base.clone();
                    message.setValue(velocity);
                    return message;

                case STYLE_CUSTOM_CC:
                    if (_customTemplate != null) {
                        message = MXMessageFactory.fromTemplate(port, _customTemplate, channel, _customGate, RangedValue.new7bit(velocity));
                        return message;
                    }
                    break;
                case STYLE_NOTES:
                    int[] noteList = MXMidi.textToNoteList(_harmonyNotes);
                    for (int note : noteList) {
                        message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_NOTEON + channel, note, velocity);
                        return message;
                    }
                    break;
                case STYLE_SEQUENCE:
                    startSongPlayer();
                    break;
                case STYLE_LINK_SLIDER:
                    int type = _status._drum._linkKontrolType;
                    int column = _status._drum._linkColumn;
                    int row = _status._drum._linkRow;
                    
                    if (column < 0) {
                        column = pad._column;
                    }

                    MGStatus status = mixer._parent.getPage(_status._drum._outPort).getStatus(type, row, column);
                    switch(_status._drum._linkMode) {
                        case MGStatusForDrum.LINKMODE_VALUE:
                            if (_outValueTypeOn != VALUETYPE_AS_MOUSE) {
                                status.setMessageValue(status.getValue().changeValue(velocity));
                                return (MXMessage)status._base.clone();
                            }
                            break;
                        case MGStatusForDrum.LINKMODE_INC:
                            status.setMessageValue(status.getValue().increment());
                            return (MXMessage)status._base.clone();
                        case MGStatusForDrum.LINKMODE_DEC:
                            status.setMessageValue(status.getValue().decrement());
                            return (MXMessage)status._base.clone();
                        case MGStatusForDrum.LINKMODE_MAX:
                             status.setMessageValue(status.getValue()._max);
                            return (MXMessage)status._base.clone();
                        case MGStatusForDrum.LINKMODE_MIN:
                             status.setMessageValue(status.getValue()._min);
                            return (MXMessage)status._base.clone();
                        case MGStatusForDrum.LINKMODE_MIDDLE:
                            status.setMessageValue((int)Math.round((status.getValue()._max + status.getValue()._min)/ 2.0));
                            return (MXMessage)status._base.clone();
                    }
                    System.out.println("valueSet To "+ status.getValue());
                    return null;

                case STYLE_PROGRAM_CHANGE:
                    message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_PROGRAMCHANGE + channel, _programNumber, 0);
                    message.setProgramBank(_programMSB, _programLSB);
                    return message;
            }
        } else {
            switch(_outValueTypeOff) {
                case VALUETYPE_AS_INPUT:
                    // velocity = velocity
                    break;
                case VALUETYPE_AS_MOUSE:
                    velocity = _mouseOnValue;
                    break;
                case VALUETYPE_NOTHING:
                    return null;
            }
            switch (_outStyle) {
                case STYLE_SAME_CC:
                    message = (MXMessage) _status._base.clone();
                    message.setValue(velocity);
                    return message;
 
                case STYLE_CUSTOM_CC:
                    if (_customTemplate != null) {
                        message = MXMessageFactory.fromTemplate(port, _customTemplate, channel, _customGate, RangedValue.new7bit(velocity));
                        return message;
                    }
                    break;
                case STYLE_NOTES:
                    int[] noteList = MXMidi.textToNoteList(_harmonyNotes);
                    for (int note : noteList) {
                        message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_NOTEOFF + channel, note, 0);
                        return message;
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
        return null;
    }
    public int[] getHarmonyNotesAsArray() {
        return MXMidi.textToNoteList(_harmonyNotes);
    }

    public void setHarmoyNotesAsArray(int[] array) {
        _harmonyNotes = MXMidi.noteListToText(array);
        System.out.println("harmony = " + _harmonyNotes);
    }

    public Object clone() {
        MGStatusForDrum drumStatus = new MGStatusForDrum(_status);

        drumStatus._outChannel = _outChannel;
        drumStatus._outStyle = _outStyle;
        drumStatus._outValueTypeOn = _outValueTypeOn;
        drumStatus._outValueTypeOff = _outValueTypeOff;

        drumStatus._harmonyNotes = _harmonyNotes;

        drumStatus._sequencerFile = _sequencerFile;
        drumStatus._sequencerSeekStart = _sequencerSeekStart;
        drumStatus._sequencerSingleTrack = _sequencerSingleTrack;
        drumStatus._sequencerFilterNote = _sequencerFilterNote;
        drumStatus._songFilePlayer = _songFilePlayer;

        drumStatus._outPort = _outPort;
        drumStatus._outChannel = _outChannel;

        drumStatus._currentSwitch = _currentSwitch;
        drumStatus._modeToggle = _modeToggle;
        drumStatus._onlySwitched = _onlySwitched;
        drumStatus._strikeZone = _strikeZone;

        drumStatus._mouseOnValue = _mouseOnValue;
        drumStatus._mouseOffValue = _mouseOffValue;

        drumStatus._templateText = _templateText;
        drumStatus._teplateTextGate = _teplateTextGate;

        drumStatus._linkRow = _linkRow;
        drumStatus._linkColumn = _linkColumn;
        drumStatus._linkKontrolType= _linkKontrolType;
        drumStatus._linkMode = _linkMode;
        
        return drumStatus;
    }
}
