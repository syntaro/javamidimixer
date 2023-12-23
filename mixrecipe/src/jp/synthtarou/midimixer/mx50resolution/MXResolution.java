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
package jp.synthtarou.midimixer.mx50resolution;

import java.util.IllegalFormatException;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageBag;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXResolution implements Cloneable {
    MXMessage _base = MXMessageFactory.fromTemplate(0, new MXTemplate(""), 0, null, null);
    MXRangedValue _newResolution;

    public MXResolution() {
        _newResolution = null;
    }
    
    public void setBaseMessage(int port, String text, MXRangedValue gate) {
        _base = null;
        if (text != null) {
            try {
                MXTemplate template = new MXTemplate(text);
                MXMessage message = MXMessageFactory.fromTemplate(port, template, 0, gate, null);
                _base = message;
            }catch(IllegalFormatException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void setBaseMessage(MXMessage base) {
        _base = null;
        if (base != null) {
           _base = (MXMessage)base.clone();
        }
    }
    
    int _recordDetect = -1;
    int _lastSent = -1;
    
    public MXMessage updateWithNewResolution(MXMessage message) {
        int detect = message.getValue()._var;
        _recordDetect = detect;

        if (_newResolution == null) {
            if (_lastSent != detect) {
                _lastSent = detect;
                return message;
            }
            return null;
        }
        if (_newResolution._min == message.getValue()._min 
         && _newResolution._max == message.getValue()._max) {
            if (_lastSent != detect) {
                _lastSent = detect;
                return message;
            }
            return null;
        }

        MXRangedValue shrink = message.getValue().changeRange(_newResolution._min, _newResolution._max);
        shrink = new MXRangedValue(shrink._var, shrink._min, shrink._max);// Math.Floar To Ignore (double Position)
        MXRangedValue reborn = shrink.changeRange(message.getValue()._min, message.getValue()._max);
        
        int newVar = reborn._var;
        if (_lastSent != newVar) {
            _lastSent = newVar;
            message.setValue(reborn);
           return message;
        }
        return null;
    }

    public boolean controlByMessage(MXMessage message, MXMessageBag result) {
        if (message.isEmpty()) {
            return false;
        }
        if (_base.isEmpty()) {
            return false;
        }
        if (_base.hasSameTemplateChGate(message)) {
            MXRangedValue value = getValue();
            MXVisitant visit = message.getVisitant();

            if (message.isDataentryByCC()) {
                int original = value._var;
                int newVar = visit.getDataentryValue14();
                switch (message.getGate()._var) {
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

            int newValue = message.getValue()._var;

            if (message.isCommand(MXMidi.COMMAND_CH_NOTEOFF)) {
                newValue = 0;
            }
            if (newValue >= value._min && newValue <= value._max) {
                setMessageValue(newValue);
                setMessageValue(value.changeValue(newValue));
                return true;
            }
            return false;
        } else if (hasSameTemplateChGate(message)) { //long message
            MXRangedValue value = _base.getValue();
            int newValue = message.getValue()._var;
            if (newValue >= value._min && newValue <= value._max) {
                setMessageValue(value.changeValue(newValue));
                return true;
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

    public boolean hasSameTemplateChGate(MXMessage message) {
        if (_base.getPort() == message.getPort()) {
            if (_base.hasSameTemplateChGate(message)) {
                return true;
            }
        }
        
        return false;
    }
}
