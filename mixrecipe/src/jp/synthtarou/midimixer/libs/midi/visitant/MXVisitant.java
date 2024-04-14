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
package jp.synthtarou.midimixer.libs.midi.visitant;

import java.util.logging.Level;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXVisitant implements Cloneable {
    int _channel = -1;
    int _program = -1;
    int _bankMSB = -1;
    int _bankLSB = -1;
    int[] _valueCC = new int[128];

    public MXVisitant(int channel) {
        for (int i = 0; i < _valueCC.length; ++i) {
            _valueCC[i] = -1;
        }
        _channel = channel;
    }

    public boolean isCCSet(int cc) {
        return _valueCC[cc] >= 0;
    }

    public void setCCValue(int cc, int value) {
        // skip mode change type cc
        if (cc == MXMidi.DATA1_CC_ALLNOTEOFF || cc == MXMidi.DATA1_CC_ALLSOUNDOFF
         || cc == MXMidi.DATA1_CC_BANKSELECT || cc == MXMidi.DATA1_CC_DATAENTRY
         || cc == MXMidi.DATA1_CC_DATAINC || cc == MXMidi.DATA1_CC_DATADEC
         || cc == MXMidi.DATA1_CC_RPN_MSB || cc == MXMidi.DATA1_CC_RPN_LSB
         || cc == MXMidi.DATA1_CC_NRPN_MSB || cc == MXMidi.DATA1_CC_NRPN_LSB
         || cc == MXMidi.DATA1_CC_RESET_ALLCTRLS) {
            return;
        }
        if (_valueCC[cc] != value) {
            _valueCC[cc] = value;
            _currentAge++;
            _currentCCAge++;
        }
    }

    public synchronized int getCCValue(int cc) {
        return _valueCC[cc];
    }

    public void setBankMSB(int msb) {
        if (_isLocked) {
            throw new IllegalStateException("Immutable can't change");
        }
        if (_bankMSB >= 0 && _bankLSB >= 0) {
            //is it necessary?
            //_bankLSB = -1;
        }
        if (_bankMSB != msb) {
            _bankMSB = msb;
            _currentAge++;
        }
    }

    public int getBankMSB() {
        return _bankMSB;
    }

    public void setBankLSB(int lsb) {
        if (_isLocked) {
            throw new IllegalStateException("Immutable can't change");
        }
        if (_bankLSB >= 0 && _bankLSB >= 0) {
            //is it necessary?
            //_bankLSB = -1;
        }
        if (_bankLSB != lsb) {
            _bankLSB = lsb;
            _currentAge++;
        }
    }

    public int getBankLSB() {
        return _bankLSB;
    }

    public boolean isHavingBank() {
        return _bankLSB >= 0 && _bankMSB >= 0;
    }

    public boolean isIncompleteBankInfo() {
        if (_bankLSB >= 0 || _bankMSB >= 0) {
            if (_bankLSB < 0 || _bankMSB < 0) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        String str = " ";

        if (_program >= 0) {
            str += "Prog[" + _program + "] ";
        }
        if (_bankMSB >= 0 || _bankLSB >= 0) {
            str += "Bank[" + MXUtil.toHexFF(_bankMSB) + ":" + MXUtil.toHexFF(_bankLSB) + "] ";
        }
        for (int cc = 0; cc < 128; ++cc) {
            if (isCCSet(cc)) {
                str += "[" + MXMidi.nameOfControlChange(cc);
                str += "." + getCCValue(cc);
                str += "]";
            }
        }

        return str.substring(1);
    }

    public MXVisitant clone() {
        MXVisitant obj = new MXVisitant(_channel);
        
        obj._valueCC = _valueCC.clone();
        obj._program = _program;
        obj._bankMSB = _bankMSB;
        obj._bankLSB = _bankLSB;

        return obj;
    }

    public int mergeNew(MXVisitant visitant) {
        int x = _currentAge;
        if (visitant == null) {
            return _currentAge - x;
        }
        if (visitant._program >= 0) {
            setProgram(visitant._program);
        }
        if (visitant._bankMSB >= 0) {
            setBankMSB(visitant._bankMSB);
        }
        if (visitant._bankLSB >= 0) {
            setBankLSB(visitant._bankLSB);
        }

        for (int cc = 0; cc < 128; ++cc) {
            if (visitant.isCCSet(cc)) {
                int contValue = visitant.getCCValue(cc);
                setCCValue(cc, contValue);
            }
        }
        return _currentAge - x;
    }

    int _currentAge = 0;
    public int _currentCCAge = 0;
    MXVisitant _lastSnapShot = null;
    boolean _isLocked = false;

    public synchronized MXVisitant getSnapShot() {
        if (_lastSnapShot == null || _lastSnapShot._currentAge != _currentAge) {
            _lastSnapShot = clone();
            _lastSnapShot._currentAge = _currentAge;
            _lastSnapShot._isLocked = true;
        }
        return _lastSnapShot;
    }

    
    public synchronized MXMessage catchTheVisitant(MXMessage message) {
        if (!message.isChannelMessage2()) {
            return message;
        }
        if (_channel != message.getChannel()) {
            MXFileLogger.getLogger(MXVisitant.class).log(Level.SEVERE, "invalid channel", new Exception());
        }

        int widerStatus = message.getStatus();
        if (widerStatus >= 0x100) {
            int port = message.getPort();
            int channel = message.getChannel();
            switch (widerStatus & 0xfff0) {
                case MXMidi.COMMAND2_CH_PROGRAM_DEC:
                    int progDec = isHavingProgram() ? getProgram() - 1 : 0;
                    if (progDec >= 0) {
                        setProgram(progDec);
                        MXMessage newMessage = MXMessageFactory.fromProgramChange(port, channel, progDec);
                        newMessage._owner = message;
                        return newMessage;
                    }else {
                        MXFileLogger.getLogger(MXVisitant.class).severe("@PROG_DEC got less than 0");
                    }
                    return null;
                case MXMidi.COMMAND2_CH_PROGRAM_INC:
                    int progInc = isHavingProgram() ? getProgram() + 1 : 0;
                    if (progInc <= 127) {
                        setProgram(progInc);
                        MXMessage newMesage = MXMessageFactory.fromProgramChange(port, channel, progInc);
                        newMesage._owner = message;
                        return newMesage;
                    }  else {
                        MXFileLogger.getLogger(MXVisitant.class).severe("@PROG_INC got biggger than 127");
                    }
            }
            return message;
        } else if (message.isChannelMessage1()) {
            int gate = message.getGate()._value;
            int value = message.getValue()._value;
            int status = message.getStatus() & 0xf0;
            int channel = message.getStatus() & 0x0f;

            if (status == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
                if (gate >= 0 && gate <= 127) {
                    setProgram(gate);
                    return message;
                }
                else {
                    MXFileLogger.getLogger(MXVisitant.class).severe("@PROG got not in 0~127");
                }
            }
            if (status == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                setCCValue(message.getCompiled(1), value);
                switch (message.getCompiled(1)) {

                    case MXMidi.DATA1_CC_BANKSELECT:
                        setBankMSB(value);
                        break;

                    case MXMidi.DATA1_CC_BANKSELECT + 32:
                        setBankLSB(value);
                        break;

                    default:
                        break;
                }
                
            }
            return message;
        }
        return message;
    }

    public boolean isHavingProgram() {
        return _program >= 0;
    }

    public int getProgram() {
        return _program;
    }

    public void setProgram(int program) {
        if (_isLocked) {
            throw new IllegalStateException("Immutable can't change");
        }
        if (program < 0) {
            MXFileLogger.getLogger(MXVisitant.class).warning("program <= o");
        } else if (program == _program) {
            //MXFileLogger.getLogger(MXVisitant.class).info("program not change " + program);
        } else {
            _currentAge++;
            _program = program;
        }
    }
}
