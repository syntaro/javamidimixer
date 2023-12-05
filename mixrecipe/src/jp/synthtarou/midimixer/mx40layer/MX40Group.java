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
package jp.synthtarou.midimixer.mx40layer;

import jp.synthtarou.midimixer.libs.midi.port.MXVisitantRecorder;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;
import java.util.ArrayList;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX40Group {

    public ArrayList<MX40Layer> _listLayer = new ArrayList();
    
    public String _title = "New";
    public boolean _isWatchPort = false;
    public int _watchingPort = 0;
    public boolean _isWatchChannel = false;
    public int _watchingChannel = 0;
    public boolean _isWatchBank = false;
    public int _watchingBankMSB = 0;
    public int _watchingBankLSB = 0;
    public boolean _isWatchProgram = false;
    public int _watchingProgram = 0;
    public boolean _isRotate = false;
    public int _rotatePoly = 16;

    public int[] _rotateCount = new int[16*9];

    private int _lastLayerPos = -1;
    
    private final MX40Process _process;
    
    MXNoteOffWatcher _noteOff = new MXNoteOffWatcher();
    
    public MX40Group(MX40Process process) {
        _process = process;
    }
    
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("By ");
        str.append("Port=").append(_isWatchPort).append("[").append(MXMidi.nameOfPortOutput(_watchingPort)).append("]");
        str.append("Channel=").append(_isWatchChannel).append("[").append(_watchingChannel+1).append("]");
        str.append("Program=").append(_isWatchProgram).append("[").append(_watchingProgram).append("]");
        str.append("Bank=").append(_watchingBankMSB).append(":").append(_watchingBankLSB).append("]");
        return str.toString();
    }
    
    public boolean isAssigned(int port, int channel) {
        MXVisitantRecorder info = _process._inputInfo;
        MXVisitant e = info.getVisitant(port, channel);

        int infoBankMSB = e.isHavingBank() ? e.getBankMSB() : -1;
        int infoBankLSB = e.isHavingBank() ? e.getBankLSB() : -1;
        int infoProgram = e.isHavingProgram() ? e.getProgram() : -1;

        boolean assigned = false;
    
        if (_listLayer.size()== 0) {
            return false;
        }
        if (_isWatchPort || _isWatchChannel || _isWatchProgram || _isWatchBank) {
            assigned = true;
            if (_isWatchPort) {
                if (_watchingPort != port) {
                    assigned = false;
                }
            }
            if (_isWatchChannel) {
                if (_watchingChannel != channel) {
                    assigned = false;
                }
            }
            if (_isWatchProgram) {
                if (_watchingProgram != infoProgram) {
                    assigned = false;
                }
            }
            if (_isWatchBank) {
                if (_watchingBankMSB != infoBankMSB ||  _watchingBankLSB != infoBankLSB) {
                    assigned = false;
                }
            }
        }else {
            assigned = false;
            if(e.isHavingBank() || e.isHavingProgram()) {
                /* プログラム指定された過去がある */
                assigned = true;
            }
        }
        
        return assigned;
    }

    
    public boolean willDoFixProgram(int port, int channel) {
        for (MX40Layer layer: _listLayer) {
            if (layer._modProgram == MX40Layer.MOD_FIXED) {
                return true;
            }            
        }
        return false;
    }
    
    public boolean processByGroup(MXMessage message) {
        if (message.isMessageTypeChannel() == false) {
            return false;
        }

        int port = message.getPort();
        int status = message.getStatus();
        int command = status;
        if (message.isMessageTypeChannel()) {
            command &= 0xf0;
        }
        int channel = message.getChannel();
        
        if (command == MXMidi.COMMAND_CH_NOTEOFF) {
            if (_noteOff.raiseHandler(port, message._timing, channel, message.getGate()._var)) {
                return true;
            }
        }

        if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
            int cc = message.getGate()._var;
            if (cc == MXMidi.DATA1_CC_BANKSELECT) {
                return true;
            }
            if (cc == MXMidi.DATA1_CC_BANKSELECT + 32) {
                return true;
            }
        }

        //記録したプログラム番号が処理対象か判断する
        boolean assigned;
        
        if (command == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
            assigned = isAssigned(port, channel);
            if (assigned) {
                for (MX40Layer layer: _listLayer) {
                    layer.processProgramChange(message);
                }
                return true;
            }
            return false;
        }else {
            assigned = isAssigned(port, channel);
        }

        if (!assigned) { 
            return false;
        }
        boolean proced = false;

        if (_listLayer.size() == 0) {
            return false;
        }
        if (_rotatePoly >= 1 && command == MXMidi.COMMAND_CH_NOTEON) {
            int found = _lastLayerPos + 1;
            if (found < 0 || found >= _listLayer.size()) {
                found = 0;
            }
            _lastLayerPos = found;

            if (_rotateCount[found] >= _rotatePoly) {
                int foundPoly = 100;
                //一番発音の少ないところ
                for (int i = 0; i < _listLayer.size(); ++ i) {
                    MX40Layer layer = _listLayer.get(i);
                    if (_rotateCount[i] < foundPoly) {
                        found = i;
                        foundPoly = _rotateCount[i];
                    }
                }
            }
            _rotateCount[found] ++;

            MX40Layer layer = _listLayer.get(found);
            proced = layer.processByLayer(message);

            MXMessage msg2 = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_NOTEOFF + channel, message.getGate()._var, 0);
            msg2._timing = message._timing;
            _noteOff.setHandler(message, msg2,  new NoteOffWatcher2(layer, found));
        }else {
            for (MX40Layer layer: _listLayer) {
                layer.processByLayer(message);
                if (command == MXMidi.COMMAND_CH_NOTEON) {
                    MXMessage msg2 = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_NOTEOFF + channel, message.getGate()._var, 0);
                    msg2._timing = message._timing;
                    _noteOff.setHandler(message, msg2,  new NoteOffWatcher2(layer, -1));
                }
                proced = true;
            }
        }

        return proced;
    }
    
    class NoteOffWatcher2 implements  MXNoteOffWatcher.Handler {
        int _rotatePos;
        MX40Layer _layer;

        NoteOffWatcher2(MX40Layer layer, int rotatePos) {
            super();
            _layer = layer;
            _rotatePos = rotatePos;
        }

        @Override
        public void onNoteOffEvent(MXTiming timing, MXMessage target) {
            target._timing = timing;
            _layer.processByLayer(target);
            if (_rotatePos >= 0) {
                _rotateCount[_rotatePos] --;
            }
        }
    }

    
    public boolean equals(Object o) {
        MX40Group target = (MX40Group)o;
        if (_title.equals(target._title)
        && _isWatchPort == target._isWatchPort
        && _watchingPort == target._watchingPort
        && _isWatchChannel == target._isWatchChannel
        && _watchingChannel == target._watchingChannel
        && _isWatchBank == target._isWatchBank
        && _watchingBankMSB == target._watchingBankMSB
        && _watchingBankLSB == target._watchingBankLSB
        && _isWatchProgram == target._isWatchProgram
        && _watchingProgram == target._watchingProgram
        && _isRotate == target._isRotate
        && _rotatePoly == target._rotatePoly) {
            return true;
        }
        return false;
    }

    public void checkEquals(String name, Object v1, Object v2) {
        if (v1.equals(v2) == false) {
            System.out.println(name +" is changing " + v1 + " > " + v2);
        }
    }
    
    public void caneChageTo(MX40Group target) {
        checkEquals("titile", _title, target._title);
        checkEquals("isWatchPort", _isWatchPort, target._isWatchPort);
        checkEquals("watchingPort", _watchingPort, target._watchingPort);
        checkEquals("isWatchChannel", _isWatchChannel, target._isWatchChannel);
        checkEquals("watchingChannel", _watchingChannel, target._watchingChannel);
        checkEquals("isWatchBank", _isWatchBank, target._isWatchBank);
        checkEquals("watchingBankMSB", _watchingBankMSB, target._watchingBankMSB);
        checkEquals("watchingBankLSB", _watchingBankLSB, target._watchingBankLSB);
        checkEquals("isWatchProgram", _isWatchProgram, target._isWatchProgram);
        checkEquals("watchingProgram", _watchingProgram, target._watchingProgram);
        checkEquals("isRotate", _isRotate, target._isRotate);
        checkEquals("rotatePoly", _rotatePoly, target._rotatePoly);

    }
}

