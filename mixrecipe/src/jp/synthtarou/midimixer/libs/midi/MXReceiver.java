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
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.MXMain;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXReceiver<T extends JPanel> {
    private static MXMain _lock = MXMain.getMain();
    
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
            MXMain.getMain().messageDispatch(message, _nextReceiver);
        }else {
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
    
    public static void initProcessWithSetting(MXINIFileSupport support) {
        boolean done = false;
        if (support instanceof MXJsonSupport){
            done = ((MXJsonSupport) support).readJSonfile(null);
            MXFileLogger.getLogger(support.getClass()).info("tried read json = " + done);
        }
        if (!done) {
            done = support.readINIFile(null);
            MXFileLogger.getLogger(support.getClass()).info("tried read ini= " + done);
        }
        if (!done) {
            support.resetSetting();
            MXFileLogger.getLogger(support.getClass()).info("tried reset setting");
        }
    }
    
    public void initProcessWithSetting() {
        if (this instanceof MXINIFileSupport) {
            initProcessWithSetting((MXINIFileSupport)this);            
        }
    }
}
