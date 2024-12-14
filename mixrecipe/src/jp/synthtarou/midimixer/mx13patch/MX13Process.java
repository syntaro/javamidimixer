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
package jp.synthtarou.midimixer.mx13patch;

import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.libs.midi.port.MXMidiFilter;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX13Process extends MXReceiver<MX13View> {

    MX13View _view;

    public MX13Process(boolean isInput) {
        _view = new MX13View(this);
    }

    @Override
    public String getReceiverName() {
        return "Patch";
    }

    @Override
    public MX13View getReceiverView() {
        return _view;
    }

    @Override
    public void processMXMessage(MXMessage message) {
    }

    public static String getFilterInfo(MXMIDIIn in) {
        StringBuilder result = new StringBuilder();
        boolean reset = true;
        if (in.getPortAssignCount() == 0) {
            return "";
        }
        boolean addedTo = false;
        int countTo = in.getPortAssignCount();
        for (int p = 0; p < MXConfiguration.TOTAL_PORT_COUNT; ++ p) {
            if (in.isPortAssigned(p) == false) {
                continue;
            }
            ArrayList<String> listTypes = new ArrayList<>();
            int countType = 0;
            
            for (int t = 0; t < MXMidiFilter.COUNT_TYPE; ++ t) {
                if (in.getFilter(p).isChecked(t)) {
                    listTypes.add(MXMidiFilter.getName(t));
                    countType ++;
                }
            }
            
            if (listTypes.size() == 1 && in.getFilter(p).isChecked(MXMidiFilter.TYPE_ISSKIPPER)) {
                listTypes = null;
            }

            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(MXMidi.nameOfPortShort(p));
            if (listTypes != null) {
                result.append(listTypes.toString());
            }
        }
        return result.toString();
    }

    public void showMIDIInDetail(MXMIDIIn in) {
        _view.showMIDIInDetail(in);
    }
    
    LinkedList<ChangeListener> _listenerList = new LinkedList();
    
    public void addChangeListener(ChangeListener listen) {
        if (_listenerList.contains(listen)) {
            return;
        }
        _listenerList.add(listen);
    }
    
    public void fireChangeListener(ChangeEvent evt) {
        ArrayList<ChangeListener> list = new ArrayList(_listenerList);
        for (ChangeListener seek : list) {
            seek.stateChanged(evt);
        }
    }
}
