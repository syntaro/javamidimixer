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
package jp.synthtarou.midimixer.mx10input;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX10Preprocessor extends MXReceiver<MX10PreprocessorView>{
    int _port;
    MX10Process _parent;
    MX10PreprocessorView _view;
    MX10Diagnostics[] listAnalyzer = null;
    DefaultListModel<String> _console = new DefaultListModel<>();

    public MX10Preprocessor(int port, MX10Process parent) {
        _parent = parent;
        _port = port;
        _view = null;
    }

    @Override
    public String getReceiverName() {
        return "Preprocessor";
    }

    public void addText(String text) {
        if (_view != null) {            
            if (SwingUtilities.isEventDispatchThread() == false) {
                SwingUtilities.invokeLater(() -> {
                    addText(text);
                });
                return;
            }
            _console.addElement(text);
            _view.reloadList();
        }
    }
    
    @Override
    public synchronized  MX10PreprocessorView getReceiverView() {
        if (_view == null) {
            _view = new MX10PreprocessorView(this);
        }
        return _view;
    }
    
    public synchronized MX10Diagnostics getAnalyzer(int ch) {
        if (listAnalyzer == null) {
            listAnalyzer = new MX10Diagnostics[16];
        }
        if (listAnalyzer[ch] == null) {
            listAnalyzer[ch] = new MX10Diagnostics(this, ch);
        }
        return listAnalyzer[ch];
    }

    @Override
    public void processMXMessage(MXMessage message) {
        if (message.isChannelMessage2()) {
            MX10Diagnostics diag = getAnalyzer(message.getChannel());
            diag.record(message);
            while (true) {
                MXMessage seek = diag.popResult();
                if (seek == null) {
                    break;
                }
                seek._owner = message;
                _parent.sendToNext(seek);
            }
            return;
        }
        else {
            for (int i = 0; i < 16; ++ i) {
                MX10Diagnostics diag = getAnalyzer(i);
                diag.flushPool();
                while (true) {
                    MXMessage seek = diag.popResult();
                    if (seek == null) {
                        break;
                    }
                    seek._owner = message;
                    _parent.sendToNext(seek);
                }
            }
        }
        
        _parent.sendToNext(message);
    }
}
