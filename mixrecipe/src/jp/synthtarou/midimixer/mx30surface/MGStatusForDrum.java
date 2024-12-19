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
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.libs.smf.SMFCallback;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.libs.smf.SMFSequencer;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;

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

    static MXNamedObjectList<Integer> _typeMap = new MXNamedObjectList();

    int _outStyle = STYLE_SAME_CC;
    int _outValueTypeOn = VALUETYPE_AS_INPUT;
    int _outValueTypeOff = VALUETYPE_AS_INPUT;

    String _harmonyNotes = "";

    String _sequencerFile = "";
    boolean _sequencerSeekStart = true;
    boolean _sequencerSingleTrack = true;
    boolean _sequencerFilterNote = true;
    SMFSequencer _sequencerPlayer = null;

    int _outPort = -1;
    int _outChannel = -1;

    boolean _currentSwitch = false;
    boolean _modeToggle = false;
    boolean _onlySwitched = true;
    MXRangedValue _strikeZone = new MXRangedValue(0, 1, 127);

    public int _mouseOnValue = 127;
    public int _mouseOffValue = 0;

    int _linkMode = MGStatusForDrum.LINKMODE_VALUE;
    int _linkKontrolType = MGStatus.TYPE_SLIDER;
    int _linkRow = 0;
    int _linkColumn = -1; // -1=same, 0~16;

    MGStatus _status;

    boolean _lastDetected = false;
    boolean _lastToggled = false;
    MXTemplate _customTemplate = null;
    MXRangedValue _customGate = MXRangedValue.ZERO7; // TODO
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
        if (_sequencerPlayer != null) {
            _sequencerPlayer.stopPlayerAwait();
            _sequencerPlayer = null;
        }
        _sequencerFile = switchSongFile;
        _sequencerPlayer = null;
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

        if (_sequencerPlayer == null) {
            try {
                if (_sequencerFile.isEmpty()) {
                    return;
                }
                File f = new File(_sequencerFile);
                if (f == null || f.exists() == false) {
                    return;
                }
                _sequencerPlayer = new SMFSequencer(new File(_sequencerFile));
            } catch (IOException ioe) {
                return;
            }
        }
        if (_sequencerSingleTrack && _outChannel >= 0) {
            _sequencerPlayer.setForceSingleChannel(_outChannel);
        } else {
            _sequencerPlayer.setForceSingleChannel(-1);
        }
        _sequencerPlayer.setFilterNoteOnly(_sequencerFilterNote);
        long seek = _sequencerSeekStart ? _sequencerPlayer.getFirstNoteMilliSecond() : 0;
        final int _port = port;
        final int _channel = channel;
        _sequencerPlayer.startPlayerThread(seek, new SMFCallback() {
            @Override
            public void smfPlayNote(OneMessage e) {
                e._port = _port;
                MXMessage message = e.toMXMessage();
                if (_sequencerSingleTrack) {
                    if (_channel >= 0) {
                        message.setChannel(_channel);
                    }
                }
                MXMIDIIn.messageToReceiverThreaded(message, _status._mixer._parent.getNextReceiver());
            }

            @Override
            public void smfStarted() {
            }

            @Override
            public void smfStoped(boolean fineFinish) {
            }

            @Override
            public void smfProgress(long pos, long finish) {
            }
        });
    }

    public void stopSongPlayer() {
        if (_sequencerPlayer != null) {
            _sequencerPlayer.stopPlayerAwait();
        }
    }

    boolean isStrike(int value) {
        return _strikeZone.contains(value);
    }

    MXMessage updatingValue(MXMessage parent, boolean strike, int value) {
        if (strike) {
            if (_modeToggle) {
                _lastToggled = !_lastToggled;
                strike = _lastToggled;
            }
        } else {
            if (_modeToggle) {
                return null;
            }
        }
        if (strike == _lastDetected) {
            if (_onlySwitched) {
                return null;
            }
        }
        _lastDetected = strike;
        return doAction(parent, strike, value);
    }

    MXMessage doAction(MXMessage owner, boolean flag, int invalue) {
        MXMessage message = null;

        MX30Packet packet = _status._mixer._parent.startTransaction(owner);
        try {
            int velocity = invalue;

            MX30Process parentProcess = MXMain.getMain().getKontrolProcess();
            MX32MixerProcess mixer = _status._mixer;
            MGDrumPad pad = mixer.getDrumPad(_status._row, _status._column);
            int port = _outPort;
            if (port < 0) {
                port = _status._port;
            }
            int channel = _outChannel;
            if (channel < 0) {
                channel = _status._base.getChannel();
            }
            pad.setDrumLook(flag);
            if (_lastSent == flag) {
                if (_onlySwitched) {
                    return null;
                }
            }
            _lastSent = flag;
            if (flag) {
                switch (_outValueTypeOn) {
                    case VALUETYPE_AS_INPUT:
                        velocity = invalue;
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
                        message._owner = MXMessage.getRealOwner(owner);
                        message.setValue(velocity);
                        break;

                    case STYLE_CUSTOM_CC:
                        if (_customTemplate != null) {
                            message = MXMessageFactory.fromTemplate(port, _customTemplate, channel, _customGate, MXRangedValue.new7bit(velocity));
                            message._owner = MXMessage.getRealOwner(owner);
                            packet.addResult(message);
                        }
                        message = null;
                        break;
                    case STYLE_NOTES:
                        int[] noteList = MXMidiStatic.textToNoteList(_harmonyNotes);
                        for (int note : noteList) {
                            message = MXMessageFactory.fromNoteon(port, channel, note, velocity);
                            message._owner = MXMessage.getRealOwner(owner);
                            packet.addResult(message);
                        }
                        message = null;
                        break;
                    case STYLE_SEQUENCE:
                        packet.addResultTask(() -> {
                            stopSongPlayer();
                            startSongPlayer();
                        });
                        break;
                    case STYLE_LINK_SLIDER:
                        int type = _status._drum._linkKontrolType;
                        int column = _status._drum._linkColumn;
                        int row = _status._drum._linkRow;

                        if (column < 0) {
                            column = pad._column;
                        }

                        MGStatus status = mixer._parent.getPage(port).getStatus(type, row, column);
                        int value = 0;
                        switch (_status._drum._linkMode) {
                            case MGStatusForDrum.LINKMODE_VALUE:
                                if (_outValueTypeOn != VALUETYPE_AS_MOUSE) {
                                    value = velocity;
                                }
                                break;
                            case MGStatusForDrum.LINKMODE_INC:
                                value = status.getValue().increment()._value;
                                break;
                            case MGStatusForDrum.LINKMODE_DEC:
                                value = status.getValue().decrement()._value;
                                break;
                            case MGStatusForDrum.LINKMODE_MAX:
                                value = status.getValue()._max;
                                break;
                            case MGStatusForDrum.LINKMODE_MIN:
                                value = status.getValue()._min;
                                break;
                            case MGStatusForDrum.LINKMODE_MIDDLE:
                                value = (int) Math.round((status.getValue()._max + status.getValue()._min) / 2.0);
                                break;
                            default:
                                return null;
                        }
                        if (status.getValue()._value == value && _onlySwitched) {
                        } else {
                            packet.addSliderMove(new MGSliderMove(owner, status, value));
                        }
                        return null;
                    case STYLE_PROGRAM_CHANGE:
                        switch (_programType) {
                            case PROGRAM_SET:
                                message = MXMessageFactory.fromProgramChange(port, channel, _programNumber);
                                message.setProgramBank(_programMSB, _programLSB);
                                break;
                            case PROGRAM_INC:
                                message = MXMessageFactory.fromTemplate(port,
                                        new MXTemplate(new int[]{MXMidiStatic.COMMAND2_CH_PROGRAM_INC}),
                                        channel, null, null);
                                break;
                            case PROGRAM_DEC:
                                message = MXMessageFactory.fromTemplate(port,
                                        new MXTemplate(new int[]{MXMidiStatic.COMMAND2_CH_PROGRAM_DEC}),
                                        channel, null, null);
                                break;
                            default:
                                break;
                        }
                        message._owner = MXMessage.getRealOwner(owner);
                        packet.addResult(message);
                        message = null;
                }
            } else {
                switch (_outStyle) {
                    case VALUETYPE_AS_INPUT:
                        // velocity = velocity
                        break;
                    case VALUETYPE_AS_MOUSE:
                        velocity = _mouseOffValue;
                        break;
                    case VALUETYPE_NOTHING:
                        return null;
                }
                switch (_outStyle) {
                    case STYLE_SAME_CC:
                        message = MXMessageFactory.fromClone(_status._base);
                        message.setValue(velocity);
                        message._owner = MXMessage.getRealOwner(owner);
                        break;

                    case STYLE_CUSTOM_CC:
                        if (_customTemplate != null) {
                            message = MXMessageFactory.fromTemplate(port, _customTemplate, channel, _customGate, MXRangedValue.new7bit(velocity));
                            message._owner = MXMessage.getRealOwner(owner);
                            packet.addResult(message);
                        }
                        message = null;
                        break;
                    case STYLE_NOTES:
                        int[] noteList = MXMidiStatic.textToNoteList(_harmonyNotes);
                        for (int note : noteList) {
                            message = MXMessageFactory.fromNoteoff(port, channel, note);
                            message._owner = MXMessage.getRealOwner(owner);
                            packet.addResult(message);
                        }
                        message = null;
                        break;
                    case STYLE_SEQUENCE:
                        packet.addResultTask(this::stopSongPlayer);
                        break;
                    case STYLE_LINK_SLIDER:
                        break;
                    case STYLE_PROGRAM_CHANGE:
                        break;
                }
            }
            if (message != null) {
                packet.addResult(message);
                message = null;
            }
            return message;
        } finally {
            _status._mixer._parent.endTransaction();
        }

    }

    public int[] getHarmonyNotesAsArray() {
        return MXMidiStatic.textToNoteList(_harmonyNotes);
    }

    public void setHarmoyNotesAsArray(int[] array) {
        _harmonyNotes = MXMidiStatic.noteListToText(array);
    }

    public Object clone() {
        MGStatusForDrum drumStatus = new MGStatusForDrum(_status);

        drumStatus._outChannel = _outChannel;
        drumStatus._outStyle = _outStyle;
        drumStatus._outValueTypeOn = _outValueTypeOn;
        drumStatus._outValueTypeOff = _outValueTypeOff;

        drumStatus._harmonyNotes = _harmonyNotes;

        drumStatus._customGate = _customGate;
        drumStatus._customTemplate = _customTemplate;
        drumStatus._customOutOnValue = _customOutOnValue;
        drumStatus._customOutOffValue = _customOutOffValue;

        drumStatus._sequencerFile = _sequencerFile;
        drumStatus._sequencerSeekStart = _sequencerSeekStart;
        drumStatus._sequencerSingleTrack = _sequencerSingleTrack;
        drumStatus._sequencerFilterNote = _sequencerFilterNote;
        ///drumStatus._sequencerPlayer = _sequencerPlayer;

        drumStatus._programLSB = _programLSB;
        drumStatus._programMSB = _programMSB;
        drumStatus._programNumber = _programNumber;
        drumStatus._programType = _programType;

        drumStatus._outPort = _outPort;
        drumStatus._outChannel = _outChannel;

        drumStatus._currentSwitch = _currentSwitch;
        drumStatus._modeToggle = _modeToggle;
        drumStatus._onlySwitched = _onlySwitched;
        drumStatus._strikeZone = _strikeZone;

        drumStatus._mouseOnValue = _mouseOnValue;
        drumStatus._mouseOffValue = _mouseOffValue;

        drumStatus._linkRow = _linkRow;
        drumStatus._linkColumn = _linkColumn;
        drumStatus._linkKontrolType = _linkKontrolType;
        drumStatus._linkMode = _linkMode;

        return drumStatus;
    }
}
