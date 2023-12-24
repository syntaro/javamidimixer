/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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

import java.util.TreeSet;
import jp.synthtarou.midimixer.ccxml.CXGeneralMidiFile;
import jp.synthtarou.midimixer.ccxml.CXNode;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class Counter {
    public Counter() {
        
    }
    
    public static class Detected {
        TreeSet<Integer> _detected = new TreeSet();

        public void record(int var) {
            _detected.add(var);
        }
        
        public boolean isEmpty() {
            return  _detected.isEmpty();
        }
        
        public int getMin() throws ArrayIndexOutOfBoundsException {
            try {
                Integer i = _detected.first();
                return i;
            }catch(Exception e) {
                throw new ArrayIndexOutOfBoundsException("can't get min from empty. check if empty before it");
            }
        }

        public int getMax() throws ArrayIndexOutOfBoundsException {
            try {
                Integer i = _detected.last();
                return i;
            }catch(Exception e) {
                throw new ArrayIndexOutOfBoundsException("can't get max from empty. check if empty before it");
            }
        }
        
        public String toString() {
            if (isEmpty()) {
                return "[]";
            }
            return  "[" + getMin() + "-" + getMax() + "]";
        }
    }

    public String formatCounter() {
        String value;
        MXMessage message = _message; 
        int gate = message.getGate()._var;
        switch (message.getStatus() & 0xf0) {
            case MXMidi.COMMAND_CH_NOTEON:
                value = "@ON " + MXMidi.nameOfNote(gate) + " (" + gate + ")";
                break;
            case MXMidi.COMMAND_CH_CHANNELPRESSURE:
                value = "@CP";
                break;
            case MXMidi.COMMAND_CH_PITCHWHEEL:
                value = "@PITCH";
                break;
            case MXMidi.COMMAND_CH_POLYPRESSURE:
                value = "@PKP " + MXMidi.nameOfNote(gate) + " (" + gate + ")";
                break;
            case MXMidi.COMMAND_CH_NOTEOFF:
                value = "@OFF " + MXMidi.nameOfNote(gate) + " (" + gate + ")";
                break;
            case MXMidi.COMMAND_CH_CONTROLCHANGE:
                value = "@CC " + MXMidi.nameOfControlChange(gate) + "(" + gate + ")";
                break;
            case MXMidi.COMMAND_CH_PROGRAMCHANGE:
                value = "@PROGRAM " + CXGeneralMidiFile.getInstance().simpleFindProgram(gate).getName()+ "(" + gate + ")";
                break;
            default:
                value = _name + " : " + message.getTemplateAsText();
                break;
        }
        return value;
    } 
    
    public String _name;
    public MXMessage _message;
    public CXNode _ccNode;
    public CounterFolder _bindedFolder;
    public String _folderName;

    public Detected _channel = new Detected();
    public Detected _gate = new Detected();
    public Detected _value = new Detected();
    
    public Counter createFrends(MXMessage message) {
        Counter renew = new Counter();
        renew._name = this._name;
        renew._message = (MXMessage)message.clone();
        renew._ccNode = _ccNode;
        renew._bindedFolder = this._bindedFolder;
        renew._folderName = this._folderName;
        renew._channel = _channel;
        renew._gate = _gate;
        renew._value = _value;
        return renew;
    }
    
    public boolean captureIt(MXMessage message, boolean checkGate) {
        if (_message == null) {
            return false;
        }

        MXMessage msg1 = this._message;
        MXMessage msg2 = message;
        
        MXTemplate temp1 = msg1.getTemplate();
        MXTemplate temp2 = msg2.getTemplate();

        if (temp1.isEmpty() || temp2.isEmpty()) {
            return false;
        }

        if (temp1.size() != temp2.size()) {
            return false;
        }
        for (int i = 0; i < temp1.size(); ++i) {
            int t1 = temp1.get(i);
            int t2 = temp2.get(i);
            
            if (t1 == MXMidi.CCXML_VH || t1 == MXMidi.CCXML_VL) {
                continue;
            }

            if (t2 == MXMidi.CCXML_VH || t2 == MXMidi.CCXML_VL) {
                continue;
            }

            if (t1 == MXMidi.CCXML_CHECKSUM_END || t2 == MXMidi.CCXML_CHECKSUM_END) {
                continue;
            }

            if (checkGate) {
                if (t1 == MXMidi.CCXML_GL || t2 == MXMidi.CCXML_GL) {
                    if (t1 == MXMidi.CCXML_GL) {
                        t1 = msg1.getGate()._var & 0x7f;
                    }
                    if (t2 == MXMidi.CCXML_GL) {
                        t2 = msg2.getGate()._var & 0x7f;
                    }
                    if (t1 != t2) {
                        return false;
                    }
                }
                if (t1 == MXMidi.CCXML_GH || t2 == MXMidi.CCXML_GH) {
                    if (t1 == MXMidi.CCXML_GH) {
                        t1 = (msg1.getGate()._var >> 7) & 0x7f;
                    }
                    if (t2 == MXMidi.CCXML_GH) {
                        t2 = (msg2.getGate()._var >> 7) & 0x7f;
                    }
                    if (t1 != t2) {
                        return false;
                    }
                }
            }
            else {
                if (t1 == MXMidi.CCXML_GL) {
                    continue;
                }
                if (t2 == MXMidi.CCXML_GL) {
                    continue;
                }
                if (t1 == MXMidi.CCXML_GH) {
                    continue;
                }
                if (t2 == MXMidi.CCXML_GH) {
                    continue;
                }
            }
            if (t1 == t2) {
                continue;
            }
            return false;
        }

        _channel.record(message.getChannel());
        if (message.hasGateLowField()) {
            _gate.record(message.getGate()._var);
        }
        if (message.hasValueLowField()) {
            _value.record(message.getValue()._var);
        }
        return true;
    }
}
