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
package jp.synthtarou.midimixer.libs.midi.capture;

import java.util.TreeMap;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMessageCapture {
    TreeMap<String, TextInformation> _captureData = new TreeMap();

    public MXMessageCapture() {
    }
    
    public void clear() {
        _captureData = new TreeMap();
        age = 0;
    }
    
    public int age = 0;
    
    public void process(MXMessage message) {
        int channel = message.getChannel();
        int gate = message.getGate()._var;
        int value = message.getValue()._var;
        String dtext = message.toTemplateText();

        synchronized(this) {
            TextInformation textNode = _captureData.get(dtext + channel);
            if (textNode == null) {
                textNode = new TextInformation();
                textNode.dtext = dtext;
                if (message.getStatus() >= 0x80 && message.getStatus() <= 0xef) {
                    textNode.command = message.getStatus() & 0xf0;
                }
                else {
                    textNode.command = message.getStatus();
                }
                textNode.channel = message.getChannel();
                age ++;
                _captureData.put(dtext + message.getChannel(), textNode);
            }

            GateInfomation gateNode = textNode.listGateValues.get(gate);
            if (gateNode == null) {
                gateNode = new GateInfomation();
                gateNode._gate = gate;
                gateNode._hitHiValue = value;
                gateNode._hitLoValue = value;
                gateNode._parent = textNode;
                textNode.listGateValues.put(gate, gateNode);
                age ++;
                return;
            }
            if (gateNode._hitLoValue > value) {
                gateNode._hitLoValue = value;
                age ++;
            }
            if (gateNode._hitHiValue < value) {
                gateNode._hitHiValue = value;
                age ++;
            }
            notifyAll();
        }
    }
    
    public int getAge() {
        return age;
    }

    public String nameOf(GateInfomation gate) {
        TextInformation text = gate._parent;
        //TODO

        int port = 0;
        MXMessage message = MXTemplate.fromDtext(0, text.dtext, gate._parent.channel, RangedValue.ZERO7, RangedValue.ZERO7);
        return  message.toStringHeader(gate._hitLoValue, gate._hitHiValue);
    }
    
    public MXWrapList<GateInfomation> createListModel() {
        synchronized(this) {
            MXWrapList<GateInfomation> list = new MXWrapList<>();

            TreeMap<String, TextInformation> capture = _captureData;
            for (TextInformation text : capture.values()) {
                for (GateInfomation gate : text.listGateValues.values()) {
                    list.addNameAndValue(nameOf(gate), gate);
                }
            }
            return list;
        }
    }
}
