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
package jp.synthtarou.midimixer.mx63patch;

import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOut;
import jp.synthtarou.midimixer.libs.midi.port.MXMidiFilter;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX63Process extends MXReceiver<MX63View> {

    MX63View _view;

    public MX63Process(boolean isInput) {
        _view = new MX63View(this);
    }

    @Override
    public String getReceiverName() {
        return "Patch";
    }

    @Override
    public MX63View getReceiverView() {
        return _view;
    }

    @Override
    public void processMXMessage(MXMessage message) {
    }

    public static String getFilterInfo(MXMIDIOut out) {
        StringBuffer result = new StringBuffer();
        boolean reset = true;
        if (out.getPortAssignCount() == 0) {
            return "";
        }
        boolean addedTo = false;
        int countTo = out.getPortAssignCount();
        for (int p = 0; p < MXConfiguration.TOTAL_PORT_COUNT; ++ p) {
            if (out.isPortAssigned(p) == false) {
                continue;
            }
            ArrayList<String> listTypes = new ArrayList<>();
            int countType = 0;
            
            for (int t = 0; t < MXMidiFilter.COUNT_TYPE; ++ t) {
                if (out.getFilter(p).isChecked(t)) {
                    listTypes.add(MXMidiFilter.getName(t));
                    countType ++;
                }
            }
            
            if (listTypes.size() == 1 && out.getFilter(p).isChecked(MXMidiFilter.TYPE_ISSKIPPER)) {
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

    public void showMIDIOutDetail(MXMIDIOut out) {
        _view.showMIDIOutDetail(out);
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
