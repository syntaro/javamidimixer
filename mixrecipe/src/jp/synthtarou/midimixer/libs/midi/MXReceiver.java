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
package jp.synthtarou.midimixer.libs.midi;

import javax.swing.JPanel;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXReceiver<T extends JPanel> {

    public abstract String getReceiverName();

    public abstract T getReceiverView();

    private MXReceiver _nextReceiver;

    public final MXReceiver getNextReceiver() {
        return _nextReceiver;
    }

    public void setNextReceiver(MXReceiver next) {
        _nextReceiver = next;
    }

    public abstract void processMXMessage(MXMessage message);

    public void sendToNext(MXMessage message) {
        if (_nextReceiver != null) {
            MXMIDIIn.messageToReceiverThreaded(message, _nextReceiver);
        } else {
            MXFileLogger.getLogger(MXReceiver.class).warning("receiver not set " + message);
        }
    }

    private boolean _usingThis = true;

    public boolean isUsingThisRecipe() {
        return _usingThis;
    }

    public void setUsingThisRecipe(boolean usingThis) {
        _usingThis = usingThis;
    }
}
