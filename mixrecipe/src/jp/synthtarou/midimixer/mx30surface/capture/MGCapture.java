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
package jp.synthtarou.midimixer.mx30surface.capture;

import java.util.ArrayList;
import java.util.TreeMap;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGCapture {
    public static void main(String[] args) {
        MXMessage msg1 = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE + 1, 10, 10);
        MXMessage msg2 = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_NOTEON + 2, 20, 20);
        MXMessage msg3 = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_PROGRAMCHANGE + 3, 30, 30);
        MGCapture cap = new MGCapture();
        System.out.println("1");
        cap.record(msg1);
        System.out.println("2");
        cap.record(msg2);
        System.out.println("3");
        cap.record(msg3);
        
        System.out.println(cap.createCommandListModel());
        
        CapturePool pool = new CapturePool();
        pool.record(msg1);
        pool.record(msg2);
        pool.record(msg3);
    }

    public MGCapture() {
    }
    
    public void record(MXMessage message) {
        String command = message.getChannel() + ":" + message.getTemplateAsText();
        int gate = message.getGate()._value;
        int value = message.getValue()._value;
        CaptureCommand capCommand = _listCommand.get(command);
        
        if (capCommand == null) {
            capCommand = new CaptureCommand(command);
            _listCommand.put(command, capCommand);
        }
        
        CaptureGate capGate = capCommand._listGate.get(gate);
        if (capGate == null) {
            capGate = new CaptureGate(gate, Integer.toString(gate) + "");
            capCommand._listGate.put(gate, capGate);
        }
        
        CaptureValue capValue = capGate._listValue.get(value);
        if (capValue == null) {
            capValue = new CaptureValue(value);
            capGate._listValue.put(value, capValue);
        }

        capValue._count ++;
    }
    
    public MXNamedObjectList<CaptureCommand> listCommand() {
        MXNamedObjectList<CaptureCommand> ret = new MXNamedObjectList<>();
        for (String key : _listCommand.keySet()) {
            CaptureCommand value = _listCommand.get(key);
            ret.addNameAndValue(key, value);
        }
        return ret;
    }

    public MXNamedObjectList<CaptureGate> listGate(CaptureCommand command) {
        MXNamedObjectList<CaptureGate> ret = new MXNamedObjectList<>();
        for (Integer key : command._listGate.keySet()) {
            CaptureGate value = command._listGate.get(key);
            ret.addNameAndValue(String.valueOf(key), value);
        }
        return ret;
    }

    public MXNamedObjectList<CaptureValue> listValue(CaptureGate gate) {
        MXNamedObjectList<CaptureValue> ret = new MXNamedObjectList<>();
        for (Integer key : gate._listValue.keySet()) {
            CaptureValue value = gate._listValue.get(key);
            ret.addNameAndValue(String.valueOf(key) +" x" + String.valueOf(value._count), value);
        }
        return ret;
    }
    
    TreeMap<String, CaptureCommand> _listCommand = new TreeMap<>();

    public synchronized MXNamedObjectList<CaptureCommand> createCommandListModel() {
        MXNamedObjectList<CaptureCommand> list = new MXNamedObjectList<>();

        for (CaptureCommand seek : _listCommand.values()) {
            list.addNameAndValue(seek._command, seek);
        }
        if (list.isEmpty()) {
            list.addNameAndValue("nothing" + System.currentTimeMillis(), null);
        }
        return list;
    }

    public synchronized MXNamedObjectList<CaptureGate> createGateListModel(CaptureCommand command) {
        MXNamedObjectList<CaptureGate> list = new MXNamedObjectList<>();
        if (command == null) {
            return list;
        }
        for (CaptureGate seek : command._listGate.values()) {
            list.addNameAndValue(seek._text, seek);
        }
        return list;
    }

    public synchronized MXNamedObjectList<String> createValueListModel(CaptureGate gate) {
        MXNamedObjectList<String> list = new MXNamedObjectList<>();
        if (gate == null) {
            return list;
        }
        
        list.addNameAndValue("0-127", "0-127");
        list.addNameAndValue("0-16383", "0-16383");
        
        TreeMap<Integer,CaptureValue> values = gate._listValue;
        if (!values.isEmpty()) {
            int min = values.firstKey();
            int max = values.lastKey();
            String str = Integer.toString(min) + "-" + Integer.toString(max);
        }
        
        return list;
    }
}
