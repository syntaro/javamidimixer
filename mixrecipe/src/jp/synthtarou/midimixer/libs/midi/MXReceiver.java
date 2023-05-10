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

import javax.swing.JComponent;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXReceiver {
    private static final MXDebugPrint _debug = new MXDebugPrint(MXReceiver.class);
    private static MXMain _lock = MXMain.getMain();
    
    public abstract String getReceiverName();

    public abstract JComponent getReceiverView();
    
    private MXReceiver _nextReceiver;

    public final MXReceiver getNextReceiver() {
        return _nextReceiver;
    }

    public void setNextReceiver(MXReceiver next) {
        _nextReceiver = next;
    }
    
    protected abstract void processMXMessageImpl(MXMessage message);
    
    public synchronized void processMXMessage(MXMessage message) {
        //System.out.println("processMXMessage " + this.getClass().getName() + " / " + message);
        /*
        if (message._timing == null) {
            message._timing = new MXTiming();
        }
        */
        processMXMessageImpl(message);
    }

    protected void sendToNext(MXMessage message) {
        //System.out.println("sendToNext " + this.getClass().getName() + " / " + message);
        if (message._timing == null) {
            message._timing = new MXTiming();
        }
        if (_nextReceiver != null) {
            MXMain.getMain().messageDispatch(message, _nextReceiver);
        }else {
            _debug.println("receiver not set " + message);
            _debug.printStackTrace();
        }
    }

    boolean _usingThis = true;
    
    public final boolean isUsingThisRecipe() {
        return _usingThis;
    }

    public final void setUsingThisRecipe(boolean usingThis) {
        _usingThis = usingThis;
    }
}
