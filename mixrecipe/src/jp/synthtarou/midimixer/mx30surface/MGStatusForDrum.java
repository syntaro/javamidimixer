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
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageBag;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.smf.SMFCallback;
import jp.synthtarou.midimixer.libs.midi.smf.SMFMessage;
import jp.synthtarou.midimixer.libs.midi.smf.SMFSequencer;

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
    SMFSequencer _songFilePlayer = null;

    int _outPort = 0;
    int _outChannel = 0;

    boolean _currentSwitch = false;
    boolean _modeToggle = false;
    boolean _onlySwitched = false; //TODO
    MXRangedValue _strikeZone = new MXRangedValue(0, 1, 127);

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
                _songFilePlayer = new SMFSequencer(new File(_sequencerFile));
            } catch (IOException ioe) {
                return;
            }
        }
        if (_sequencerSingleTrack) {
            _songFilePlayer.setForceSingleChannel(_outChannel);
        } else {
            _songFilePlayer.setForceSingleChannel(-1);
        }
        _songFilePlayer.setFilterNoteOnly(_sequencerFilterNote);
        long seek = _sequencerSeekStart ? _songFilePlayer.getFirstNoteMilliSecond() : 0;
        final int _port = port;
        final int _channel = channel;
        _songFilePlayer.startPlayer(seek, new SMFCallback() {
            @Override
            public void smfPlayNote(MXTiming timing, SMFMessage e) {
                e._port = _port;
                MXMessage message = e.fromSMFtoMX();
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
            public void smfProgress(long pos, long finish) {
            }
        });
    }

    public void stopSongPlayer() {
        if (_songFilePlayer != null) {
            _songFilePlayer.stopPlayer();
        }
    }

    void mouseDetected(boolean push, MXMessageBag result) {
        int velocity;
        if (push) {
            _status.setMessageValue(_mouseOnValue);
        } else {
            _status.setMessageValue(_mouseOffValue);
        }

        messageDetected(result);
    }

    boolean messageDetected(MXMessageBag result) {
        boolean flag = _strikeZone.contains(_status.getValue()._value);

        if (flag == _lastDetected) {
            return false;
        }
        _lastDetected = flag;
        if (flag) {
            if (_modeToggle) {
                _lastToggled = !_lastToggled;
                flag = _lastToggled;
            }
        } else {
            if (_modeToggle) {
                return false;
            }
        }
        doAction(flag, result);
        return true;
    }

    class SliderOperation implements Runnable {
        MGStatus _slider;
        int _newValue;
        MXMessageBag _bag;
        
        public SliderOperation(MGStatus slider, int newValue, MXMessageBag bag) {
            _slider = slider;
            _newValue = newValue;
            _bag = bag;
        }
        
        @Override
        public void run() {
            switch(_slider._uiType) {
                case MGStatus.TYPE_SLIDER:
                    MGSlider slider = (MGSlider)_slider.getComponent();
                    _slider.setMessageValue(_newValue);
                    slider.publishUI();
                    _slider._mixer.updateStatusAndSend(_slider, _newValue, null, _bag);
                    break;
                case MGStatus.TYPE_CIRCLE:
                    MGCircle circle = (MGCircle)_slider.getComponent();
                    _slider.setMessageValue(_newValue);
                    circle.publishUI();
                    _slider._mixer.updateStatusAndSend(_slider, _newValue, null, _bag);
                    break;
                case MGStatus.TYPE_DRUMPAD:
                    return;
            }
        }
        
    }
    void doAction(boolean flag, MXMessageBag result) {
        MXMessage message;
        if (_lastSent == flag) {
            return;
        }
        _lastSent = flag;
        int velocity = _status.getValue()._value;

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
        if (flag) {
            pad.setDrumActive(true);
        } else {
            pad.setDrumActive(false);
        }
        if (flag) {
            switch (_outValueTypeOn) {
                case VALUETYPE_AS_INPUT:
                    velocity = _status.getValue()._value;
                    break;
                case VALUETYPE_AS_MOUSE:
                    velocity = _mouseOnValue;
                    break;
                case VALUETYPE_NOTHING:
                    return;
            }
            switch (_outStyle) {
                case STYLE_SAME_CC:
                    message = (MXMessage) _status._base.clone();
                    message.setValue(velocity);
                    result.addTranslated(message);
                    return;

                case STYLE_CUSTOM_CC:
                    if (_customTemplate != null) {
                        message = MXMessageFactory.fromTemplate(port, _customTemplate, channel, _customGate, MXRangedValue.new7bit(velocity));
                        result.addTranslated(message);
                        return;
                    }
                    break;
                case STYLE_NOTES:
                    int[] noteList = MXMidi.textToNoteList(_harmonyNotes);
                    for (int note : noteList) {
                        message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_NOTEON + channel, note, velocity);
                        result.addTranslated(message);
                    }
                    break;
                case STYLE_SEQUENCE:
                    result.addTranslatedTask(new Runnable() {
                        @Override
                        public void run() {
                            startSongPlayer();
                        }
                    });
                    break;
                case STYLE_LINK_SLIDER:
                    int type = _status._drum._linkKontrolType;
                    int column = _status._drum._linkColumn;
                    int row = _status._drum._linkRow;

                    if (column < 0) {
                        column = pad._column;
                    }

                    MGStatus status = mixer._parent.getPage(_status._drum._outPort).getStatus(type, row, column);
                    int value = 0;
                    switch (_status._drum._linkMode) {
                        case MGStatusForDrum.LINKMODE_VALUE:
                            if (_outValueTypeOn != VALUETYPE_AS_MOUSE) {
                                value = velocity;
                                return;
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
                            return;
                    }
                    result.addTranslatedTask(new SliderOperation(status, value, result));
                    return;

                case STYLE_PROGRAM_CHANGE:
                    switch (_programType) {
                        case PROGRAM_SET:
                            message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_PROGRAMCHANGE + channel, _programNumber, 0);
                            message.setProgramBank(_programMSB, _programLSB);
                            result.addTranslated(message);
                            break;
                        case PROGRAM_INC:
                            message = MXMessageFactory.fromTemplate(port,
                                    new MXTemplate(new int[]{MXMidi.COMMAND2_CH_PROGRAM_INC}),
                                    channel, null, null);
                            result.addTranslated(message);
                            break;
                        case PROGRAM_DEC:
                            message = MXMessageFactory.fromTemplate(port,
                                    new MXTemplate(new int[]{MXMidi.COMMAND2_CH_PROGRAM_DEC}),
                                    channel, null, null);
                            result.addTranslated(message);
                            break;
                    }
                    return;
            }
        } else {
            switch (_outValueTypeOff) {
                case VALUETYPE_AS_INPUT:
                    // velocity = velocity
                    break;
                case VALUETYPE_AS_MOUSE:
                    velocity = _mouseOnValue;
                    break;
                case VALUETYPE_NOTHING:
                    return;
            }
            switch (_outStyle) {
                case STYLE_SAME_CC:
                    message = (MXMessage) _status._base.clone();
                    message.setValue(velocity);
                    result.addTranslated(message);
                    break;

                case STYLE_CUSTOM_CC:
                    if (_customTemplate != null) {
                        message = MXMessageFactory.fromTemplate(port, _customTemplate, channel, _customGate, MXRangedValue.new7bit(velocity));
                        result.addTranslated(message);
                    }
                    break;
                case STYLE_NOTES:
                    int[] noteList = MXMidi.textToNoteList(_harmonyNotes);
                    for (int note : noteList) {
                        message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_NOTEOFF + channel, note, 0);
                        result.addTranslated(message);
                    }
                    break;
                case STYLE_SEQUENCE:
                    result.addTranslatedTask(new Runnable() {
                        @Override
                        public void run() {
                            stopSongPlayer();
                        }
                    });
                    break;
                case STYLE_LINK_SLIDER:
                    break;
                case STYLE_PROGRAM_CHANGE:
                    break;
            }
        }
    }

    public int[] getHarmonyNotesAsArray() {
        return MXMidi.textToNoteList(_harmonyNotes);
    }

    public void setHarmoyNotesAsArray(int[] array) {
        _harmonyNotes = MXMidi.noteListToText(array);
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
        drumStatus._linkKontrolType = _linkKontrolType;
        drumStatus._linkMode = _linkMode;

        return drumStatus;
    }
}
