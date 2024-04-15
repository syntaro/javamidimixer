/*
 * Copyright (C) 2024 Syntarou YOSHIDA
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

import javax.swing.DefaultListModel;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXPreprocess extends MXReceiver<MXPreprocessPanel> {

    public final MXPreprocessDiagnostics _analyzer;
    DefaultListModel<String> _console = new DefaultListModel<>();
    MXMIDIIn _inputPort;

    public MXPreprocess(MXMIDIIn inputPort) {
        _inputPort = inputPort;
        _analyzer = new MXPreprocessDiagnostics(this, _inputPort);
    }

    @Override
    public String getReceiverName() {
        return "Preprocessor";
    }

    @Override
    public synchronized MXPreprocessPanel getReceiverView() {
        return MXPreprocessPanel.getInstance();
    }

    @Override
    public void processMXMessage(MXMessage message) {
        MXPreprocessDiagnostics diag = _analyzer;
        diag.processMain(message);
        while (true) {
            MXMessage seek = diag.popResult();
            if (seek == null) {
                break;
            }
            if (message == seek) {
                //LAUGH
            } else {
                seek._owner = MXMessage.getRealOwner(message);
            }
            sendToNext(seek);
        }
    }
}
