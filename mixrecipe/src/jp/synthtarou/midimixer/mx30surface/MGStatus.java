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
import java.util.LinkedList;
import java.util.logging.Level;
import javax.swing.JComponent;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.visitant.MXVisitant;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.mx36ccmapping.MX36Status;

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

    public MXNamedObjectList<Integer> _outValueTable;
    public MXNamedObjectList<Integer> _outGateTable;

    String _name = "";
    String _memo = "";
    
    public String getAsName() {
        if (_name == null || _name.isBlank()) {
            return _base.toStringMessageInfo(1);
        }
        return _name;
    }

    MXMessage _base = null;
    
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

    @Override
    public Object clone() {
        MGStatus status = new MGStatus(_mixer, _uiType, _row, _column);

        status._base = _base == null ? null : (MXMessage)_base.clone();

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
        if (base == null) {
            _base = null;
        }else {
            _base = (MXMessage)base.clone();
            _base._owner = base;
        }
    }

    public String toString() {
        MXMessage message = _base;
        if (message == null) {
           return "NULL";
        }
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
            name = message.toStringMessageInfo(2);
        } else {
            name = _name;
        }
        return name + " (" + _memo + ")" + ", "
                + text + "[row " + (_row + 1) + ", col " + (_column + 1) + "], "
                + (message.indexOfValueHi() >= 0 ? " (=14bit)" : "");
    }

    public int controlByMessage(MXMessage message) {
        if (_base == null || message == null) {
            return -1;
        }
        if (message.isEmpty() || _base.isEmpty()) {
            return -1;
        }

        if ((_base.getTemplate().get(0) & 0xfff0) == MXMidi.COMMAND_CH_NOTEON) {
            if ((message.getTemplate().get(0) & 0xfff0) == MXMidi.COMMAND_CH_NOTEOFF) {
                MXMessage newMessage = MXMessageFactory.fromNoteon(message.getPort(), message.getChannel(), message.getCompiled(1), 0);
                newMessage._owner = message;
                message = newMessage;
            }
        }
        MXRangedValue value = _base.catchValue(message);

        if (value != null) {
            MXVisitant visit = message.getVisitant();

            int newValue = value._value;

            if (message.isCommand(MXMidi.COMMAND_CH_NOTEOFF)) {
                newValue = 0;
            }
            if (newValue >= value._min && newValue <= value._max) {
                return newValue;
            }
        }
        return -1;
    }

    public MXRangedValue getValue() {
        if (_base == null) {
            return MXRangedValue.ZERO7;
        }
        return _base.getValue();
    }

    public boolean setMessageValue(MXRangedValue value) {
        if (_base == null) {
            return false;
        }
        return _base.setValue(value);
    }

    public boolean setMessageValue(int value) {
        if (_base == null) {
            return false;
        }
        return _base.setValue(_base.getValue().changeValue(value));
    }

    public boolean hasCustomRange() {
        if (_base == null) {
            return false;
        }
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
        if (_base == null) {
            return;
        }
        if (hasCustomRange()) {
            if (_base.indexOfValueHi() >= 0) {
                setMessageValue(_base.getValue().changeRange(0, 128 * 128 - 1));
            } else if (_ccPair14) {
                setMessageValue(_base.getValue().changeRange(0, 128 * 128 - 1));
            } else {
                setMessageValue(_base.getValue().changeRange(0, 128 - 1));
            }
        }
    }

    public void setCustomRange(int min, int max) {
        if (_base == null) {
            return;
        }
        setMessageValue(_base.getValue().changeRange(min, max));
    }

    public int compareTo(MGStatus another) {
        if (_base == null) {
            if (another._base == null) {
                return 0;
            }
            return 1;
        }
        else if (another._base == null) {
            return 1;
        }
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
    
    LinkedList<MX36Status> _linked36Status = null;
    
    public synchronized  void regist36Link(MX36Status linkTo) {
        if (_linked36Status == null) {
            _linked36Status = new LinkedList<>();
        }
        if (_linked36Status.contains(linkTo)) {
            return;
        }
        _linked36Status.add(linkTo);
    }

    public synchronized  void unregist36Link(MX36Status linkTo) {
        if (_linked36Status != null) {
            _linked36Status.remove(linkTo);
        }
    }
    
    public synchronized LinkedList<MX36Status> getLinked36() {
        return _linked36Status;
    }
}
