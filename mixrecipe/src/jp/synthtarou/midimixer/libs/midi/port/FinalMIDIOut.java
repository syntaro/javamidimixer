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
package jp.synthtarou.midimixer.libs.midi.port;

import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JPanel;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class FinalMIDIOut extends MXReceiver {

    protected static final FinalMIDIOut _instance = new FinalMIDIOut();

    public static FinalMIDIOut getInstance() {
        return _instance;
    }

    LinkedList<MXMessage> _listTestResult = null;
    int _testPort = -1;
    MXMessage _testBase = null;

    public void startTestSignal(MXMessage testBase, int port) {
        _listTestResult = new LinkedList();
        _testPort = port;
        _testBase = MXMessage.getRealOwner(testBase);
    }

    public ArrayList<MXMessage> getTestResult() {
        synchronized (this) {
            ArrayList<MXMessage> result = new ArrayList(_listTestResult);
            return result;
        }
    }

    static MXMessage _last = null;

    @Override
    public void processMXMessage(MXMessage message) {
        synchronized (this) {
            if (_listTestResult != null) {
                if (_testBase == null || MXMessage.getRealOwner(message) == _testBase) {
                    if (_testPort < 0) {
                        _listTestResult.add(message);
                    } else if (_testPort == message.getPort()) {
                        _listTestResult.add(message);
                    }
                }
            }
        }
        MXMain.addInsideOutput(message);
        MXNamedObjectList<MXMIDIOut> listOut = MXMIDIOutManager.getManager().listAllOutput();
        for (int i = 0; i < listOut.getSize(); ++i) {
            MXMIDIOut out = listOut.valueOfIndex(i);
            int port = message.getPort();
            if (out.isOpen() && out.isPortAssigned(port)&& out.getFilter(port).isOK(message)) {
                out.processMidiOut(message);
            }
        }
        _last = message;
    }

    @Override
    public String getReceiverName() {
        return "Dispatch To Device";
    }

    @Override
    public JPanel getReceiverView() {
        return null;
    }
}
