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

import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX11Process extends MXReceiver<MX11View> {
    MX11ViewData _viewData;
    MX11View _view;
    int _type;

    public MX11Process() {
        _viewData = new MX11ViewData();
        _view = new MX11View(this);
    }
    
    @Override
    public void processMXMessage(MXMessage message) {
        if (isUsingThisRecipe() == false) { sendToNext(message); return; }

        if (message.isCommand(MXMidiStatic.COMMAND_CH_NOTEON)) {
            int port = message.getPort();
            int velocity = message.getCompiled(1);
            int newVelocity = _viewData.transform(port, velocity);
            if (velocity == newVelocity) {
                sendToNext(message);
            }else {
                int channel = message.getChannel();
                int note = message.getGate()._value;
                MXMessage newMessage = MXMessageFactory.fromNoteon(port, channel, note, newVelocity);
                newMessage._owner = MXMessage.getRealOwner(message);
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
    public MX11View getReceiverView() {
        return _view;
    }
}
