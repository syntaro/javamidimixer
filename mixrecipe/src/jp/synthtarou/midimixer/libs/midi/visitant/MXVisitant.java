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

import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXVisitant implements Cloneable {

    int _program = -1;
    int _bankMSB = -1;
    int _bankLSB = -1;

    boolean[] _havingCC = new boolean[128];
    int[] _valueCC = new int[128];
  
    public boolean isCCSet(int cc) {
        return _havingCC[cc];
    }

    public void setCCValue(int cc, int value) {
        for (int cc1 : MXVisitant.codeList) {
            if (cc1 == cc) {
                if (_havingCC[cc] == false || _valueCC[cc] != value) {
                    _havingCC[cc] = true;
                    _valueCC[cc] = value;
                    _currentAge++;
                }
                return;
            }
        }
    }

    public synchronized int getCCValue(int cc) {
        return _valueCC[cc];
    }

    protected MXDataentry _flushed = null;
    protected MXDataentry _fetching = null;

    public MXDataentry getFlushedDataentry() {
        return _flushed;
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
        if (_bankLSB >= 0 || _bankLSB >= 0) {
            if (_bankLSB < 0 || _bankLSB < 0) {
                return true;
            }
        }
        return false;
    }

    public void forceCompleteBankInfo() {
        if (isIncompleteBankInfo()) {
            if (_bankLSB < 0) {
                _bankLSB = 0;
            }
            if (_bankMSB < 0) {
                _bankMSB = 0;
            }
            MXFileLogger.getLogger(MXVisitant.class).severe("did forceCompleteBankInfo");
            _currentAge++;
        }
    }

    public String toString() {
        String str = "";

        if (_program >= 0) {
            str += " Prog[" + _program + "]";
        }
        if (_bankMSB >= 0 || _bankLSB >= 0) {
            str += " Bank[" + MXUtil.toHexFF(_bankMSB) + ":" + MXUtil.toHexFF(_bankLSB) + "]";
        }
        for (int cc = 0; cc < 128; ++cc) {
            if (isCCSet(cc)) {
                str += "[" + MXMidi.nameOfControlChange(cc);
                str += "." + getCCValue(cc);
                str += "]";
            }
        }
        if (_flushed != null) {
            str += _flushed.toString();
        }

        return str.substring(1);
    }

    public MXVisitant clone() {
        MXVisitant obj = new MXVisitant();

        obj._program = _program;

        obj._havingCC = _havingCC.clone();
        obj._valueCC = _valueCC.clone();
        obj._bankMSB = _bankMSB;
        obj._bankLSB = _bankLSB;

        if (obj._flushed != null) {
            obj._flushed = (MXDataentry) obj._flushed.clone();
        }
        obj._fetching = null;

        return obj;
    }

    public static final int[] codeList = {MXMidi.DATA1_CC_CHANNEL_VOLUME, MXMidi.DATA1_CC_PANPOT, MXMidi.DATA1_CC_EXPRESSION};

    public int mergeNew(MXVisitant visitant) {
        int x = _currentAge;
        if (visitant == null) {
            return _currentAge - x;
        }
        if (visitant._program >= 0) {
            setProgram(visitant._program);
        }
        /* TODO keep be matchas past Clear Cached Logic */
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
        if (visitant._flushed != null) {
            _fetching = null;
            if (_flushed.equals(visitant._flushed) == false) {
                _flushed = (MXDataentry) visitant._flushed.clone();
                System.out.println("flushed ");
                _currentAge ++;
            }
        }
        return _currentAge - x;
    }

    int _currentAge = 0;
    MXVisitant _lastSnapShort = null;
    boolean _isLocked = false;

    public synchronized MXVisitant getSnapShot() {
        if (_lastSnapShort == null || _lastSnapShort._currentAge != _currentAge) {
            _lastSnapShort = clone();
            _lastSnapShort._currentAge = _currentAge;
            _lastSnapShort._isLocked = true;
        }
        return _lastSnapShort;
    }

    public MXDataentry getFetchingDataentry() {
        if (_fetching == null) {
            _fetching = new MXDataentry(this, MXDataentry.TYPE_RPN, -1, -1);
        }
        return _fetching;
    }
    
    public void resetFetchingData() {
        _fetching = null;
        _currentAge ++;
    }

    public synchronized MXMessage preprocessDataentry(MXMessage message) {
        int widerStatus = message.getTemplate().get(0);
        if (widerStatus >= 0x100) {
            switch (widerStatus & 0xfff0) {
                case MXMidi.COMMAND2_CH_RPN:
                case MXMidi.COMMAND2_CH_NRPN:
                    if (message.sizeOfTemplate() >= 5) {
                        int isRPN = 0;
                        if ((message.getTemplate().get(0) & 0xfff0) == MXMidi.COMMAND2_CH_RPN) {
                            isRPN = MXDataentry.TYPE_RPN;
                        } else {
                            isRPN = MXDataentry.TYPE_NRPN;
                        }
                        int msb = message.parseTemplate(1);
                        int lsb = message.parseTemplate(2);
                        int datamsb = message.parseTemplate(3);
                        int datalsb = message.parseTemplate(4);
                        _flushed = new MXDataentry(null, isRPN, msb, lsb);
                        _flushed.setDataentryMSB(datamsb);
                        _flushed.setDataentryLSB(datalsb);
                        message.setVisitant(this);
                        _fetching = null;
                        return message;
                    }
            }
            MXFileLogger.getLogger(MXVisitant.class).severe("Invalid Process Route");
        } else {
            int gate = message.getGate()._value;
            int value = message.getValue()._value;
            int status = message.getStatus() & 0xf0;
            int channel = message.getStatus() & 0x0f;
            if (status == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                MXDataentry proc = null;
                if (gate == MXMidi.DATA1_CC_DATAENTRY || gate == MXMidi.DATA1_CC_DATAENTRY + 32) {
                    proc = getFetchingDataentry();
                    if (gate == MXMidi.DATA1_CC_DATAENTRY) {
                        proc.setDataentryMSB(value);
                    } else {
                        proc.setDataentryLSB(value);
                    }
                    if (proc.getDataentryMSB() >= 0 && proc.getDataentryLSB() >= 0) {
                        _flushed = (MXDataentry) proc.clone();
                        _fetching = null;
                        _currentAge++;
                        MXTemplate temp = new MXTemplate(new int[]{
                            proc.isRPN() == MXDataentry.TYPE_RPN ? MXMidi.COMMAND2_CH_RPN : MXMidi.COMMAND2_CH_NRPN,
                             _flushed.getDataroomMSB(), _flushed.getDataroomLSB(),
                             _flushed.getDataentryMSB(), _flushed.getDataentryMSB()});
                        MXMessage message2 = MXMessageFactory.fromTemplate(message.getPort(), temp, message.getChannel(), MXRangedValue.ZERO7, MXRangedValue.ZERO7);
                        message2.setVisitant(getSnapShot());
                        return message2;
                    }
                    return null;
                } else if (gate == MXMidi.DATA1_CC_DATAINC || gate == MXMidi.DATA1_CC_DATADEC) {
                    if (_flushed == null) {
                        MXFileLogger.getLogger(MXVisitant.class).severe("DATAENTRY INC/DEC without anyflush before");
                        return null;
                    }
                    value = _flushed.getDataentryValue14();
                    proc = getFetchingDataentry();
                    proc.setDataroomMSB(_flushed.getDataroomMSB());
                    proc.setDataroomLSB(_flushed.getDataroomLSB());
                    if (gate == MXMidi.DATA1_CC_DATAINC) {
                        value++;
                        if (value >= 16384) {
                            MXFileLogger.getLogger(MXVisitant.class).severe("DATAENTRY INC overflow");
                            return null;
                        }
                    } else {
                        value--;
                        if (value < 0) {
                            MXFileLogger.getLogger(MXVisitant.class).severe("DATAENTRY DEC overflow");
                        }
                        return null;
                    }
                    proc.setDataentry14(value);
                    _flushed = (MXDataentry)proc.clone();
                    MXTemplate temp = new MXTemplate(new int[]{
                        proc.isRPN() == MXDataentry.TYPE_RPN ? MXMidi.COMMAND2_CH_RPN : MXMidi.COMMAND2_CH_NRPN,
                         proc.getDataroomMSB(), proc.getDataroomLSB(),
                         proc.getDataentryMSB(), proc.getDataentryMSB()});
                    MXMessage message2 = MXMessageFactory.fromTemplate(message.getPort(), temp, message.getChannel(), MXRangedValue.ZERO7, MXRangedValue.ZERO7);
                    message2.setVisitant(getSnapShot());
                    return message2;
                } else if (gate == MXMidi.DATA1_CC_RPN_MSB) {
                    proc = getFetchingDataentry();
                    proc.setIsRPN(MXDataentry.TYPE_RPN);
                    proc.setDataroomMSB(value);
                    return null;
                } else if (gate == MXMidi.DATA1_CC_RPN_LSB) {
                    proc = getFetchingDataentry();
                    proc.setIsRPN(MXDataentry.TYPE_RPN);
                    proc.setDataroomLSB(value);
                    return null;
                } else if (gate == MXMidi.DATA1_CC_NRPN_MSB) {
                    proc = getFetchingDataentry();
                    proc.setIsRPN(MXDataentry.TYPE_NRPN);
                    proc.setDataroomMSB(value);
                    return null;
                } else if (gate == MXMidi.DATA1_CC_NRPN_LSB) {
                    proc = getFetchingDataentry();
                    proc.setIsRPN(MXDataentry.TYPE_NRPN);
                    proc.setDataroomLSB(value);
                    return null;
                }
            }
            MXFileLogger.getLogger(MXVisitant.class).severe("Invalid Process Route");
        }
        return message;
    }

    public synchronized MXMessage preprocess(MXMessage message) {
        int widerStatus = message.getTemplate().get(0);
        if (widerStatus >= 0x100) {
            int port = message.getPort();
            int channel = message.getChannel();
            switch (widerStatus & 0xfff0) {
                case MXMidi.COMMAND2_CH_RPN:
                case MXMidi.COMMAND2_CH_NRPN:
                    if (message.sizeOfTemplate() >= 5) {
                        return preprocessDataentry(message);
                    }
                    MXFileLogger.getLogger(MXVisitant.class).severe("@RPN got less than 5 length");
                    return null;
                case MXMidi.COMMAND2_CH_PROGRAM_DEC:
                    int progDec = isHavingProgram() ? getProgram() - 1 : 0;
                    if (progDec >= 0) {
                        setProgram(progDec);
                        message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_PROGRAMCHANGE + channel, progDec, 0);
                        return message;
                    }
                    MXFileLogger.getLogger(MXVisitant.class).severe("@PROG_DEC got less than 0");
                    return null;
                case MXMidi.COMMAND2_CH_PROGRAM_INC:
                    int progInc = isHavingProgram() ? getProgram() + 1 : 0;
                    if (progInc <= 127) {                        
                        setProgram(progInc);
                        message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_PROGRAMCHANGE + channel, progInc, 0);
                        return message;
                    }
                    MXFileLogger.getLogger(MXVisitant.class).severe("@PROG_INC got biggger than 127");
                    return null;
            }
            return message;
        } else if (message.isMessageTypeChannel()) {
            int gate = message.getGate()._value;
            int value = message.getValue()._value;
            int status = message.getStatus() & 0xf0;
            int channel = message.getStatus() & 0x0f;

            if (status == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
                if (gate >= 0 && gate <= 127) {
                    setProgram(gate);
                    return message;
               }
                MXFileLogger.getLogger(MXVisitant.class).severe("@PROG got not in 0~127");
                return null;
            }
            if (status == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                MXDataentry proc;
                setCCValue(message.getData1(), value);
                switch (message.getData1()) {
                    case MXMidi.DATA1_CC_DATAENTRY:
                    case MXMidi.DATA1_CC_DATAENTRY + 32:
                    case MXMidi.DATA1_CC_DATAINC:
                    case MXMidi.DATA1_CC_DATADEC:
                    case MXMidi.DATA1_CC_RPN_MSB:
                    case MXMidi.DATA1_CC_RPN_LSB:
                    case MXMidi.DATA1_CC_NRPN_MSB:
                    case MXMidi.DATA1_CC_NRPN_LSB:
                        return preprocessDataentry(message);

                    case MXMidi.DATA1_CC_BANKSELECT:
                        setBankMSB(value);
                        return message;

                    case MXMidi.DATA1_CC_BANKSELECT + 32:
                        setBankLSB(value);
                        return message;

                    default:
                        return message;
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
        if (program < 0 ) {
            MXFileLogger.getLogger(MXVisitant.class).warning("program <= o");
        }
        else if (program == _program) {
            //MXFileLogger.getLogger(MXVisitant.class).info("program not change " + program);
        }
        else {
            _currentAge++;
            _program = program;
        }
    }
    
    public void forceCompleteDataentry() {
        if (_fetching._isRPN < 0) {
            _fetching._isRPN = MXDataentry.TYPE_RPN;
        }
        if (_fetching._dataroomMSB < 0) {
            _fetching._dataroomMSB = 0;
        }
        if (_fetching._dataroomLSB < 0) {
            _fetching._dataroomLSB = 0;
        }
        if (_fetching._dataentryMSB < 0) {
            _fetching._dataentryMSB = 0;
        }
        if (_fetching._dataentryLSB < 0) {
            _fetching._dataentryLSB = 0;
        }
        _flushed = _fetching;
        _fetching = null;
    }
}