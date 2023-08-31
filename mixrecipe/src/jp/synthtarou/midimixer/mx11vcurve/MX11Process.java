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
package jp.synthtarou.midimixer.mx11vcurve;

import javax.swing.JPanel;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX11Process extends MXReceiver {
    MX11Data _data;
    MX11View _view;
    int _type;

    public MX11Process() {
        _data = new MX11Data();
        _view = new MX11View(this);
    }
    
    public MX11Data getData() {
        return _data;
    }

    @Override
    protected  void processMXMessageImpl(MXMessage message) {
        if (isUsingThisRecipe() == false) { sendToNext(message); return; }

        if (message.isCommand(MXMidi.COMMAND_NOTEON)) {
            int port = message.getPort();
            int velocity = message.getGate()._var;
            int newVelocity = _data.transform(port, velocity);
            if (velocity == newVelocity) {
                sendToNext(message);
            }else {
                int channel = message.getChannel();
                int note = message.getGate()._var;
                MXMessage newMessage = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_NOTEON + channel, note, newVelocity);
                newMessage._timing = message._timing;
                sendToNext(newMessage);
            }
        }else {
            sendToNext(message);
        }
    }

    @Override
    public String getReceiverName() {
        return "Velocity Curve";
    }

    @Override
    public JPanel getReceiverView() {
        return _view;
    }
}
