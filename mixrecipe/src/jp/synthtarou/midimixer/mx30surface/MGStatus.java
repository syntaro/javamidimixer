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

import java.util.IllegalFormatException;
import java.util.logging.Level;
import javax.swing.JComponent;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageBag;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.visitant.MXVisitant;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGStatus implements Cloneable, Comparable<MGStatus> {
    public static final int TYPE_CIRCLE = 1;
    public static final int TYPE_SLIDER = 2;
    public static final int TYPE_DRUMPAD = 3;

    public MX32MixerProcess _mixer;
    public final int _port;
    public int _uiType;
    public final int _row;
    public final int _column;

    public MXNamedObjectList<Integer> _outValueTable; //TODO
    public MXNamedObjectList<Integer> _outGateTable;

    String _name = "";
    String _memo = "";
    
    public String getAsName() {
        if (_name == null || _name.isBlank()) {
            return _base.toStringForUI();
        }
        return _name;
    }

    MXMessage _base = MXMessageFactory.fromTemplate(0, new MXTemplate(""), 0, null, null);
/*
    int _dataroomType = MXVisitant.ROOMTYPE_NODATA;
    int _dataroomMSB = -1;
    int _dataroomLSB = -1;
*/
    boolean _ccPair14 = false;

    MGStatusForDrum _drum = null;

    public MGStatus(MX32MixerProcess mixer, int uiType, int row, int column) {
        clearAll();
        _mixer = mixer;
        _port = mixer._port;
        _uiType = uiType;
        _row = row;
        _column = column;

        if (uiType == TYPE_DRUMPAD) {
            _drum = new MGStatusForDrum(this);
        }
    }
    
    public JComponent getComponent() {
        switch(_uiType) 
        {
            case TYPE_SLIDER:
                MGSlider slider = _mixer.getSlider(_row, _column);
                return slider;
            case TYPE_CIRCLE:
                MGCircle circle = _mixer.getCircle(_row, _column);
                return circle;
            case TYPE_DRUMPAD:
                MGDrumPad pad = _mixer.getDrumPad(_row, _column);
                return pad;
        }
        return null;
    }

    public Object clone() {
        MGStatus status = new MGStatus(_mixer, _uiType, _row, _column);

        status._base = (MXMessage)_base.clone();

        status._name = _name;
        status._memo = _memo;

        status._ccPair14 = _ccPair14;
        
        if (_drum != null) {
            status._drum = (MGStatusForDrum)_drum.clone();
            status._drum._status = status;
        }

        return status;
    }

    public void setBaseMessage(String text) {
        _base = null;
        if (text != null) {
            try {
                if (text == null || text.isBlank()) {
                    return;
                }
                MXTemplate template = new MXTemplate(text);
                MXMessage message = MXMessageFactory.fromTemplate(_port, template, 0, null, null);
                _base = message;
            } catch (IllegalFormatException ex) {
                MXFileLogger.getLogger(MGStatus.class).log(Level.WARNING, ex.getMessage(), ex);
            }  
        }
    }

    public int getChannel() {
        if (_base != null) {
            return _base.getChannel();
        }
        return 0;
    }
    
    public void setBaseMessage(MXMessage base) {
        _base = null;
        _base = (MXMessage)base.clone();
    }

    public String toString() {
        MXMessage message = _base;
        String text;
        switch (_uiType) {
            case TYPE_CIRCLE:
                text = "Circle";
                break;
            case TYPE_SLIDER:
                text = "Slider";
                break;
            case TYPE_DRUMPAD:
                text = "Pad";
                break;
            default:
                message = null;
                text = "Error";
                break;
        }
        String name;
        if (_name == null || _name.length() == 0) {
            if (message == null) {
                name = "null";
            } else {
                name = message.toStringForUI();
            }
        } else {
            name = _name;
        }
        return name + " (" + _memo + ")" + ", "
                + text + "[row " + (_row + 1) + ", col " + (_column + 1) + "], "
                + message.toString() + (message.isPairedWith14() ? " (=14bit)" : "");
    }

    public boolean controlByMessage(MXMessage message, MXMessageBag bag) {
        if (message.isEmpty()) {
            return false;
        }
        if (_base.isEmpty()) {
            return false;
        }

        if ((_base.getStatus() & 0xf0) == MXMidi.COMMAND_CH_NOTEON) {
            if ((message.getStatus() & 0xf0) == MXMidi.COMMAND_CH_NOTEOFF) {
                message = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_CH_NOTEON + message.getChannel(), message.getData1(), 0);
            }
        }
        MXRangedValue value = _base.catchValue(message);

        if (value != null) {
            MXVisitant visit = message.getVisitant();
            
            if (message.isDataentryByCC()) {
                int original = value._value;
                int newVar = visit.getFlushedDataentry().getDataentryValue14();
                switch (message.getGate()._value) {
                    case MXMidi.DATA1_CC_DATAENTRY:
                        if (newVar >= value._min && newVar <= value._max) {
                        } else {
                            return false;
                        }
                        break;
                    case MXMidi.DATA1_CC_DATAINC:
                        newVar = original + 1;
                        if (newVar >= value._min && newVar <= value._max) {
                        } else {
                            return false;
                        }
                        break;
                    case MXMidi.DATA1_CC_DATADEC:
                        newVar = original - 1;
                        if (newVar >= value._min && newVar <= value._max) {
                        } else {
                            return false;
                        }
                        break;
                }
                if (newVar >= value._min && newVar <= value._max) {
                    setMessageValue(value.changeValue(newVar));
                    return true;
                }
                return false;
            }

            int newValue = value._value;

            if (message.isCommand(MXMidi.COMMAND_CH_NOTEOFF)) {
                newValue = 0;
            }
            if (newValue >= value._min && newValue <= value._max) {
                setMessageValue(newValue);
                if (_drum != null) {
                    _drum.messageDetected(bag);
                    return true;
                }
                else {
                    setMessageValue(value.changeValue(newValue));
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    public MXRangedValue getValue() {
        return _base.getValue();
    }

    public void setMessageValue(MXRangedValue value) {
        _base.setValue(value);
    }

    public void setMessageValue(int value) {
        _base.setValue(_base.getValue().changeValue(value));
    }

    public boolean hasCustomRange() {
        int wishMax = _base.indexOfValueHi() >= 0 ? (128 * 128 - 1) : 127;

        if (_ccPair14) {
            wishMax = 128 * 128 - 1;
        }

        if (_base.getValue()._min == 0
        && _base.getValue()._max == wishMax) {
            return false;
        } else {
            return true;
        }
    }

    public void resetCustomRange() {
        if (hasCustomRange()) {
            
        }
        if (_base.indexOfValueHi() >= 0) {
            setMessageValue(_base.getValue().changeRange(0, 128 * 128 - 1));
        } else if (_ccPair14) {
            setMessageValue(_base.getValue().changeRange(0, 128 * 128 - 1));
        } else {
            setMessageValue(_base.getValue().changeRange(0, 128 - 1));
        }
    }

    public void setCustomRange(int min, int max) {
        setMessageValue(_base.getValue().changeRange(min, max));
    }

    public int compareTo(MGStatus another) {
        if (this == another) {
            return 0;
        }
        int x;

        x = _port - another._port;
        if (x == 0) {
            x = _uiType - another._uiType;
        }
        if (x == 0) {
            x = _row - another._row;
        }
        if (x == 0) {
            x = _column - another._column;
        }
        return x;
    }

    public void fillTogglePedal() {
        clearAll();
        MXTemplate template = new MXTemplate("90h #GL #VL");
        setBaseMessage(MXMessageFactory.fromTemplate(_port, template, getChannel(), new MXRangedValue(12*5, 0, 127), MXRangedValue.ZERO7));
        _name = "note";
        _drum._outStyle = MGStatusForDrum.STYLE_CUSTOM_CC;
        _drum._modeToggle = true;
        _drum._currentSwitch = false;
        _drum._strikeZone = new MXRangedValue(127, 1, 127);
        _drum._customTemplate = new MXTemplate("@CC #GL #VL");
        _drum._customOutOnValue = 127;
        _drum._customOutOffValue = 0;

    }

    public void clearAll() {
        setBaseMessage("");

        _name = "";
        _memo = "";

        _ccPair14 = false;

        if (_uiType == TYPE_DRUMPAD) {
            _drum = new MGStatusForDrum(this);
        }
        else {
            _drum = null;
        }
    }
}
