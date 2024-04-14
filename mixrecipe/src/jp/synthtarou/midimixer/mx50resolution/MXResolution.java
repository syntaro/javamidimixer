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

import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.mx30surface.MX30Packet;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXResolution implements Cloneable {
    MXTemplate _command;
    int _port;
    int _channel;
    int _gate;
    int _resolution;
    
    final MX50Process _process;
    MXResolutionView _bindedView;   
    
    public MXResolution(MX50Process process) {
        _process = process;
    }
    
    int _lastSent = -1;
    
    int shrinkAndExpand(int oldValue, int min, int max) {
        MXRangedValue in = new MXRangedValue(oldValue, min, max);
        if ((in._max - in._min) < _resolution || _resolution <= 0) {
            return oldValue;
        }
        MXRangedValue temp = in.changeRange(0, _resolution);
        MXRangedValue mathFloor = new MXRangedValue(temp._value, temp._min, temp._max);
        MXRangedValue ret = mathFloor.changeRange(in._min, in._max);
 
        return ret._value;
    }
    
    MXMessage updateWithNewResolution(MXMessage message) {
        int detect = message.getValue()._value;
        int newValue = shrinkAndExpand(detect, message.getValue()._min, message.getValue()._max);

        if (_lastSent != newValue) {
            _lastSent = newValue;
            MXMessage newMessage = (MXMessage)message.clone();
            newMessage.setValue(newValue);
            newMessage._owner = MXMessage.getRealOwner(message);
            return newMessage;
        }
        return null;
    }

    public boolean controlByMessage(MXMessage message, MX30Packet packet) {
        if (_command == null || _command.isEmpty()) {
            return false;
        }
        MXMessage base = MXMessageFactory.fromTemplate(_port, _command, _channel, MXRangedValue.new7bit(_gate), MXRangedValue.ZERO7);
        boolean proc = false;
        MXRangedValue value = base.catchValue(message);
        if (value != null) {
            base.setValue(value);
            proc = true;
            MXMessage translated = updateWithNewResolution(message);
            if (translated != null) {
               _bindedView.updateMonitor(message.getValue()._value, translated.getValue()._value);
                packet.addResult(translated);
            }
        }

        return proc;
    }
}
