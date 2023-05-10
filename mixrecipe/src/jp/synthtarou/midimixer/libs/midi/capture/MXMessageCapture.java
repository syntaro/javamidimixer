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
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMessageTemplate;

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
        int gate = message.getGate();
        int value = message.getValue();
        String dtext = message.toDText();

        synchronized(this) {
            TextInformation textNode = _captureData.get(dtext + channel);
            if (textNode == null) {
                textNode = new TextInformation();
                textNode.dtext = dtext;
                textNode.command = message.getCommand();
                textNode.channel = message.getChannel();
                age ++;
                _captureData.put(dtext + message.getChannel(), textNode);
            }

            GateInfomation gateNode = textNode.listGateValues.get(gate);
            if (gateNode == null) {
                gateNode = new GateInfomation();
                gateNode._gate = gate;
                gateNode._maxValue = value;
                gateNode._minValue = value;
                gateNode._parent = textNode;
                textNode.listGateValues.put(gate, gateNode);
                age ++;
                return;
            }
            if (gateNode._minValue > value) {
                gateNode._minValue = value;
                age ++;
            }
            if (gateNode._maxValue < value) {
                gateNode._maxValue = value;
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

        int port = 0;
        MXMessageTemplate template = MXMessageFactory.fromDtext(text.dtext, gate._parent.channel);
        MXMessage message = template.buildMessage(0, 0, 0);

        return  message.toStringHeader(gate._minValue, gate._maxValue);
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
