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

import javax.swing.JPanel;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;

/**
 *
 * @author Syntarou YOSHIDA
 */

public class FinalMIDIOut extends MXReceiver {
    private static final MXDebugPrint _debug = new MXDebugPrint(FinalMIDIOut.class);

    protected static final FinalMIDIOut _instance = new FinalMIDIOut();
    
    public static FinalMIDIOut getInstance() {
        return _instance;
    }

    @Override
    protected void processMXMessageImpl(MXMessage message) {
        MXWrapList<MXMIDIOut> listOut = MXMIDIOutManager.getManager().listAllOutput();
        for (int i = 0; i < listOut.getSize(); ++ i) {
            MXMIDIOut out = listOut.valueOfIndex(i);
            if (out.isOpen() && out.isPortAssigned(message.getPort())) {
                out.processMidiOut(message);   
            }
        }
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
