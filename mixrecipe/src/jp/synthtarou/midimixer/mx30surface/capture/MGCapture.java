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

import java.util.TreeMap;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGCapture {
    public static void main(String[] args) {
        MXMessage msg1 = MXMessageFactory.fromControlChange(0, 1, 10, 10);
        MXMessage msg2 = MXMessageFactory.fromNoteon(0, 2, 20, 20);
        MXMessage msg3 = MXMessageFactory.fromProgramChange(0, 3, 30);
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
        try {
            //modify
            MXTemplate temp = message.getTemplate();
            if (temp.safeGet(0) == MXMidiStatic.COMMAND2_CH_RPN
              || temp.safeGet(0) == MXMidiStatic.COMMAND2_CH_NRPN) {
                int room1 = temp.safeGet(1);
                int room2 = temp.safeGet(2);
                int value1 = temp.safeGet(3);
                int value2 = temp.size() > 4 ? temp.safeGet(4) : 0xffff;
                boolean changed = false;
                
                if ((value2 & 0xff00) == 0xff00) {
                    value2 = 0;
                    if ((value1 & 0xff00) == 0) {
                        message.setValue(value1);
                        value1 = MXMidiStatic.CCXML_VL;
                        changed = true;
                    }
                }
                else {
                    if ((value1 & 0xff00) == 0 && (value2 & 0xff00) == 0) {
                        message.setValue(MXRangedValue.new14bit(value1 << 7 | value2));
                        value1 = MXMidiStatic.CCXML_VH;
                        value2 = MXMidiStatic.CCXML_VL;
                        changed = true;
                    }
                }
                
                if (changed) {
                    temp = new MXTemplate(new int[] { 
                       temp.safeGet(0), room1, room2, value1, value2
                   });
                }
                MXMessage newMessage = MXMessageFactory.fromTemplate(
                        message.getPort(), temp, 
                        message.getChannel(), message.getGate(), message.getValue());
                newMessage._owner = MXMessage.getRealOwner(message);
                message = newMessage;
            }
        }catch(Throwable e) {
            e.printStackTrace();
        }
        String strCommand = message.getTemplateAsText();
        int command = message.getStatus() & 0xf0;
        int gate = message.getGate()._value;
        int value = message.getValue()._value;
        CaptureCommand capCommand = _listCommand.get(command);
        
        if (capCommand == null) {
            capCommand = new CaptureCommand(message.getChannel(), strCommand);
            _listCommand.put(command, capCommand);
        }
        
        CaptureGate capGate = capCommand._listGate.get(gate);
        if (capGate == null) {
            capGate = new CaptureGate(capCommand, gate, Integer.toString(gate) + "");
            capCommand._listGate.put(gate, capGate);
        }
        
        CaptureValue capValue = capGate._value;
        capValue.record(value);
    }
    
    public MXNamedObjectList<CaptureCommand> listCommand() {
        MXNamedObjectList<CaptureCommand> ret = new MXNamedObjectList<>();
        for (Integer key : _listCommand.keySet()) {
            CaptureCommand value = _listCommand.get(key);
            ret.addNameAndValue(value.toString(), value);
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

    TreeMap<Integer, CaptureCommand> _listCommand = new TreeMap<>();

    public synchronized MXNamedObjectList<CaptureCommand> createCommandListModel() {
        MXNamedObjectList<CaptureCommand> list = new MXNamedObjectList<>();

        for (CaptureCommand seek : _listCommand.values()) {
            list.addNameAndValue(seek.toString(), seek);
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
            list.addNameAndValue(seek.toString(), seek);
        }
        return list;
    }

        
    public synchronized MXNamedObjectList<CaptureValue> createValueListModel(CaptureGate gate) {
        MXNamedObjectList<CaptureValue> list = new MXNamedObjectList<>();
        if (gate == null) {
            return list;
        }
        if (gate._command._template != null && gate._command._template.indexOfValueHi() >= 0) {
            CaptureValue temp = new CaptureValue();
            temp.record(0);
            temp.record(128 * 128 -1);
            list.addNameAndValue(temp.toString(), temp);
        }
        if (true) {
            CaptureValue temp = new CaptureValue();
            temp.record(0);
            temp.record(128 -1);
            list.addNameAndValue(temp.toString(), temp);
        }
        if (true) {
            CaptureValue temp = new CaptureValue();
            temp.record(gate._value._minValue);
            temp.record(gate._value._maxValue);
            list.addNameAndValue("detected " + temp.toString(), temp);
        }

        return list;
    }
}
