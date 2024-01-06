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
package jp.synthtarou.midimixer.libs.midi.port;

import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXVisitant implements Cloneable {

    public static final int HAVE_VAL_NOT = 0;
    public static final int HAVE_VAL_MSB = 1;
    public static final int HAVE_VAL_LSB = 2;
    public static final int HAVE_VAL_BOTH = 3;

    public static final int ROOMTYPE_NODATA = 0;
    public static final int ROOMTYPE_RPN = 1;
    public static final int ROOMTYPE_NRPN = 2;

    boolean _havingProgram;
    int _program;
    boolean _havingVolume = false;
    int _infoVolume = 127;
    boolean _havingExpression = false;
    int _infoExpression = 127;
    boolean _havingPan = false;
    int _infoPan = 64;
    int _havingBank;
    int _bankMSB;
    int _bankLSB;

    int _dataroomType = ROOMTYPE_NODATA;
    int _gotDataroom = HAVE_VAL_NOT;
    int _dataroomLSB = 0; //98 NL 99 NM 100 RL 101 RN
    int _dataroomMSB = 0;
    int _gotDatavalue = HAVE_VAL_NOT;
    int _dataentryMSB = 0; //CC6
    int _dataentryLSB = 0; //CC38

    public void setBankMSB(int msb) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _bankMSB = msb;
        switch (_havingBank) {
            case HAVE_VAL_NOT:
            case HAVE_VAL_BOTH:
                _havingBank = HAVE_VAL_MSB;
                break;
            case HAVE_VAL_MSB:
                break;
            case HAVE_VAL_LSB:
                _havingBank = HAVE_VAL_BOTH;
                break;

        }
    }

    public int getBankMSB() {
        return _bankMSB;
    }

    public void setBankLSB(int lsb) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _bankLSB = lsb;
        switch (_havingBank) {
            case HAVE_VAL_NOT:
            case HAVE_VAL_BOTH:
                _havingBank = HAVE_VAL_LSB;
                break;
            case HAVE_VAL_MSB:
                _havingBank = HAVE_VAL_BOTH;
                break;
            case HAVE_VAL_LSB:
                break;

        }
    }

    public int getBankLSB() {
        return _bankLSB;
    }

    public boolean isHavingBank() {
        return _havingBank == HAVE_VAL_BOTH;
    }

    public boolean isIncompleteBankInfo() {
        return _havingBank != HAVE_VAL_NOT && _havingBank != HAVE_VAL_BOTH;
    }

    public void forceCompleteBankInfo() {
        if (_havingBank != HAVE_VAL_NOT) {
            _havingBank = HAVE_VAL_BOTH;
        }
    }

    public boolean isIncomplemteDataroom() {
        if (_gotDataroom == HAVE_VAL_NOT || _gotDataroom == HAVE_VAL_BOTH) {
            return false;
        }
        return true;
    }

    public void forceCompleteBankDataroom() {
        if (_gotDataroom != HAVE_VAL_NOT) {
            _gotDataroom = HAVE_VAL_BOTH;
        }
    }

    public boolean isIncomplemteDataentry() {
        if (_gotDatavalue == HAVE_VAL_NOT || _gotDatavalue == HAVE_VAL_BOTH) {
            return false;
        }
        return true;
    }

    public void forceCompleteBankDataentry() {
        if (_gotDatavalue != HAVE_VAL_NOT) {
            _gotDatavalue = HAVE_VAL_BOTH;
        }
    }

    public boolean isHaveDataroom() {
        if (_gotDataroom == HAVE_VAL_BOTH) {
            return true;
        }
        return false;
    }

    public boolean isHaveDataentryRPN() {
        if (_gotDatavalue == HAVE_VAL_BOTH) {
            if (_dataroomType == MXVisitant.ROOMTYPE_RPN) {
                return isHaveDataroom();
            }
        }
        return false;
    }

    public boolean isHaveDataentryNRPN() {
        if (_gotDatavalue == HAVE_VAL_BOTH) {
            if (_dataroomType == MXVisitant.ROOMTYPE_NRPN) {
                return isHaveDataroom();
            }
        }
        return false;
    }

    public int getDataroomLSB() {
        return _dataroomLSB;
    }

    public int getDataroomMSB() {
        return _dataroomMSB;
    }

    public int getDataentryLSB() {
        return _dataentryLSB;
    }

    public int getDataentryMSB() {
        return _dataentryMSB;
    }

    public int getDataentryValue14() {
        return ((_dataentryMSB & 0x7f) << 7) | (_dataentryLSB & 0x7f);
    }

    public void setDataroom14(int longValue) {
        int msb = longValue >> 7;
        int lsb = longValue & 0x7f;
        setDataroomMSB(msb);
        setDataroomLSB(lsb);
        _gotDataroom = MXVisitant.HAVE_VAL_BOTH;
    }

    public void setDataentry14(int longValue) {
        int msb = longValue >> 7;
        int lsb = longValue & 0x7f;
        setDataentryMSB(msb);
        setDataentryLSB(lsb);
        _gotDatavalue = MXVisitant.HAVE_VAL_BOTH;
    }

    public void setDataroomType(int roomType) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _dataroomType = roomType;
        _gotDatavalue = 0;
    }

    public int getDataroomType() {
        return _dataroomType;
    }

    public void setDataroomMSB(int msb) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _dataroomMSB = msb;
        switch (_gotDataroom) {
            case HAVE_VAL_NOT:
                _gotDataroom = HAVE_VAL_MSB;
                break;
            case HAVE_VAL_MSB:
                break;
            case HAVE_VAL_BOTH:
                break;
            case HAVE_VAL_LSB:
                _gotDataroom = HAVE_VAL_BOTH;
                break;

        }
    }

    public void setDataroomLSB(int lsb) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _dataroomLSB = lsb;
        switch (_gotDataroom) {
            case HAVE_VAL_NOT:
                _gotDataroom = HAVE_VAL_LSB;
                break;
            case HAVE_VAL_BOTH:
                break;
            case HAVE_VAL_MSB:
                _gotDataroom = HAVE_VAL_BOTH;
                break;
            case HAVE_VAL_LSB:
                break;

        }
    }

    public void setDataentryMSB(int msb) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _dataentryMSB = msb;
        switch (_gotDatavalue) {
            case HAVE_VAL_NOT:
                _gotDatavalue = HAVE_VAL_MSB;
                break;
            case HAVE_VAL_MSB:
                break;
            case HAVE_VAL_BOTH:
                break;
            case HAVE_VAL_LSB:
                _gotDatavalue = HAVE_VAL_BOTH;
                break;

        }
    }

    public void setDataentryLSB(int lsb) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _dataentryLSB = lsb;
        switch (_gotDatavalue) {
            case HAVE_VAL_NOT:
                _gotDatavalue = HAVE_VAL_LSB;
                break;
            case HAVE_VAL_BOTH:
                break;
            case HAVE_VAL_MSB:
                _gotDatavalue = HAVE_VAL_BOTH;
                break;
            case HAVE_VAL_LSB:
                break;

        }
    }

    public String toString() {
        String str = "";

        if (_havingBank != HAVE_VAL_NOT) {
            str += " Bank[" + MXUtil.toHexFF(_bankMSB) + ":" + MXUtil.toHexFF(_bankLSB) + "]";
        }
        if (_havingProgram) {
            str += " Prog." + _program; // + ":" + GMProgramList.getInstance().nameOfValue(_program)
        }
        if (_havingPan) {
            str += " Pan." + _infoPan; // + ":" + GMProgramList.getInstance().nameOfValue(_program)
        }
        if (_havingVolume) {
            str += " Voume." + _infoVolume; // + ":" + GMProgramList.getInstance().nameOfValue(_program)
        }
        if (_havingExpression) {
            str += " Expression." + _infoExpression; // + ":" + GMProgramList.getInstance().nameOfValue(_program)
        }
        if (_dataroomType == ROOMTYPE_RPN) {
            str += " RPN." + _dataroomMSB + "," + _dataroomLSB + " = " + getDataentryValue14();
        }
        if (_dataroomType == ROOMTYPE_NRPN) {
            str += " NRPN." + _dataroomMSB + "," + _dataroomLSB + " = " + getDataentryValue14();
        }

        return str.substring(1);
    }

    public MXVisitant clone() {
        MXVisitant obj = new MXVisitant();

        obj._havingProgram = _havingProgram;
        obj._program = _program;
        obj._havingVolume = _havingVolume;
        obj._infoVolume = _infoVolume;
        obj._havingExpression = _havingExpression;
        obj._infoExpression = _infoExpression;
        obj._havingPan = _havingPan;
        obj._infoPan = _infoPan;
        obj._havingBank = _havingBank;
        obj._bankMSB = _bankMSB;
        obj._bankLSB = _bankLSB;

        obj._dataroomType = _dataroomType;
        obj._gotDataroom = _gotDataroom;
        obj._dataroomLSB = _dataroomLSB;
        obj._dataroomMSB = _dataroomMSB;
        obj._gotDatavalue = _gotDatavalue;
        obj._dataentryMSB = _dataentryMSB;
        obj._dataentryLSB = _dataentryLSB;

        return obj;
    }

    public boolean mergeNew(MXVisitant visitant) {
        boolean mod = false;
        if (visitant == null) {
            return mod;
        }
        if (visitant.isHavingProgram()) {
            if (_havingProgram != true || _program != visitant._program) {
                _havingProgram = true;
                _program = visitant._program;
                mod = true;
            }
        }
        if (visitant._havingBank != HAVE_VAL_NOT) {
            if (_havingBank == HAVE_VAL_NOT || _bankMSB != visitant._bankMSB || _bankLSB != visitant._bankLSB) {
                _havingBank = visitant._havingBank;
                _bankMSB = visitant._bankMSB;
                _bankLSB = visitant._bankLSB;
                mod = true;
            }
        }
        if (visitant.isHavingVolume()) {
            if (_havingVolume == false || _infoVolume != visitant._infoVolume) {
                _infoVolume = visitant._infoVolume;
                mod = true;
            }
        }
        if (visitant.isHavingExpression()) {
            if (_havingExpression == false || _infoExpression != visitant._infoExpression) {
                _havingExpression = true;
                _infoExpression = visitant._infoExpression;
                mod = true;
            }
        }
        if (visitant.isHavingPan()) {
            if (_havingPan == false || _infoPan != visitant._infoPan) {
                _havingPan = true;
                _infoPan = visitant._infoPan;
                mod = true;
            }
        }
        if (visitant.getDataroomType() != MXVisitant.ROOMTYPE_NODATA) {
            if (_dataroomType != visitant._dataroomType
                    || _dataentryMSB != visitant._dataentryMSB || _dataroomLSB != visitant._dataentryLSB
                    || _dataentryMSB != visitant._dataentryMSB || _dataentryLSB != visitant._dataentryLSB) {
                _gotDataroom = visitant._gotDataroom;
                _dataroomType = visitant._dataroomType;
                _dataentryMSB = visitant._dataentryMSB;
                _gotDatavalue = visitant._gotDatavalue;
                _dataroomLSB = visitant._dataentryLSB;
                _dataentryMSB = visitant._dataentryMSB;
                _dataentryLSB = visitant._dataentryLSB;
                mod = true;
            }
        }
        return mod;
    }

    public static boolean isMesssageHaveVisitant(MXMessage message) {
        if (message.isMessageTypeChannel()) {
            switch (message.getStatus() & 0xf0) {
                case MXMidi.COMMAND_CH_PROGRAMCHANGE:
                    return true;
                case MXMidi.COMMAND2_CH_RPN:
                case MXMidi.COMMAND2_CH_NRPN:
                case MXMidi.COMMAND2_CH_PROGRAM_DEC:
                case MXMidi.COMMAND2_CH_PROGRAM_INC:
                    return true;
                case MXMidi.COMMAND_CH_CONTROLCHANGE:
                    switch (message.getData1()) {
                        case MXMidi.DATA1_CC_DATAENTRY:
                        case MXMidi.DATA1_CC_DATAENTRY + 32:
                        case MXMidi.DATA1_CC_DATAINC:
                        case MXMidi.DATA1_CC_DATADEC:
                        case MXMidi.DATA1_CC_BANKSELECT:
                        case MXMidi.DATA1_CC_BANKSELECT + 32:
                        case MXMidi.DATA1_CC_RPN_LSB:
                        case MXMidi.DATA1_CC_RPN_MSB:
                        case MXMidi.DATA1_CC_NRPN_LSB:
                        case MXMidi.DATA1_CC_NRPN_MSB:
                        case MXMidi.DATA1_CC_PANPOT:
                        case MXMidi.DATA1_CC_EXPRESSION:
                        case MXMidi.DATA1_CC_CHANNEL_VOLUME:
                            return true;
                    }
            }
        }
        return false;
    }

    int _currentAge = 0;
    MXVisitant _lastSnapShort = null;
    boolean _isImmutable = false;

    public synchronized MXVisitant getSnapShot() {
        if (_lastSnapShort == null || _lastSnapShort._currentAge != _currentAge) {
            _lastSnapShort = clone();
            _lastSnapShort._currentAge = _currentAge;
            _lastSnapShort._isImmutable = true;
        }
        return _lastSnapShort;
    }

    public synchronized boolean updateVisitantChannel(MXMessage message) {
        int widerStatus = message.getTemplate().get(0);
        if (widerStatus >= 0x100) {
            switch(widerStatus & 0xfff0) {
                case MXMidi.COMMAND2_CH_RPN:
                case MXMidi.COMMAND2_CH_NRPN:
                    byte[] data = message.createBytes();
                    if (data.length >= 5) {
                        int type = 0, msb = 0, lsb = 0, datamsb = 0, datalsb = 0;
                        //type = data[0] & 0xff;
                        msb = data[1] & 0xff;
                        lsb = data[2] & 0xff;
                        datamsb = data[3] & 0xff;
                        datalsb = data[4] & 0xff;
                        if ((message.getTemplate().get(0) & 0xfff0) == MXMidi.COMMAND2_CH_RPN) {
                            type = ROOMTYPE_RPN;
                        }
                        else {
                            type = ROOMTYPE_NRPN;
                        }
                        setDataroomType(type);
                        setDataroomMSB(msb);
                        setDataroomLSB(lsb);
                        setDataentryMSB(datamsb);
                        setDataentryLSB(datalsb);
                    }
                    return true;
                case MXMidi.COMMAND2_CH_PROGRAM_DEC:
                    int programD = isHavingProgram() ? getProgram() -  1: 0;
                    setHavingProgram(true);
                    setProgram(programD);
                    return true;
                case MXMidi.COMMAND2_CH_PROGRAM_INC:
                    int programI = isHavingProgram() ? getProgram() +  1: 0;
                    setHavingProgram(true);
                    setProgram(programI);
                    return true;
            }
        }else if (message.isMessageTypeChannel()) {
            int gate = message.getGate()._value;
            int value = message.getValue()._value;

            switch (message.getStatus() & 0xf0) {
                case MXMidi.COMMAND_CH_PROGRAMCHANGE:
                    if (gate >= 0 && gate <= 127) { // for Tricky Ghost Number
                        setHavingProgram(true);
                        setProgram(gate);
                        _currentAge++;
                    }
                    return true;
                case MXMidi.COMMAND_CH_CONTROLCHANGE:
                    switch (message.getData1()) {
                        case MXMidi.DATA1_CC_DATAENTRY:
                            setDataentryMSB(value);
                            _currentAge++;
                            return true;
                        case MXMidi.DATA1_CC_DATAENTRY + 32:
                            setDataentryLSB(value);
                            _currentAge++;
                            return true;
                        case MXMidi.DATA1_CC_DATAINC:
                            value = getDataentryValue14() + 1;
                            if (value >= 16384) {
                                value = 16383;
                            }
                            setDataentry14(value);
                            _currentAge++;
                            return true;
                        case MXMidi.DATA1_CC_DATADEC:
                            value = getDataentryValue14() - 1;
                            if (value < 0) {
                                value = 0;
                            }
                            setDataentry14(value);
                            _currentAge++;
                            return true;
                        case MXMidi.DATA1_CC_BANKSELECT:
                            setBankMSB(value);
                            _currentAge++;
                            return true;
                        case MXMidi.DATA1_CC_BANKSELECT + 32:
                            setBankLSB(value);
                            forceCompleteBankInfo();
                            _currentAge++;
                            return true;
                        case MXMidi.DATA1_CC_RPN_MSB:
                            setDataroomType(ROOMTYPE_RPN);
                            setDataroomMSB(value);
                            _currentAge++;
                            return true;
                        case MXMidi.DATA1_CC_RPN_LSB:
                            setDataroomType(ROOMTYPE_RPN);
                            setDataroomLSB(value);
                            _currentAge++;
                            return true;
                        case MXMidi.DATA1_CC_NRPN_MSB:
                            setDataroomType(ROOMTYPE_NRPN);
                            setDataroomMSB(value);
                            return true;
                        case MXMidi.DATA1_CC_NRPN_LSB:
                            setDataroomType(ROOMTYPE_NRPN);
                            setDataroomLSB(value);
                            _currentAge++;
                            return true;
                        case MXMidi.DATA1_CC_PANPOT:
                            setHavingPan(true);
                            setInfoPan(value);
                            _currentAge++;
                            return true;
                        case MXMidi.DATA1_CC_EXPRESSION:
                            setHavingExpression(true);
                            setInfoExpression(value);
                            _currentAge++;
                            return true;
                        case MXMidi.DATA1_CC_CHANNEL_VOLUME:
                            setHavingVolume(true);
                            setInfoVolume(value);
                            _currentAge++;
                            return true;
                    }
            }
        }
        return false;
    }

    public void attachChannelVisitantToMessage(MXMessage message) {
        message.setVisitant(getSnapShot());
    }

    public boolean isHavingProgram() {
        return _havingProgram;
    }

    public void setHavingProgram(boolean havingProgram) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _havingProgram = havingProgram;
    }

    public int getProgram() {
        return _program;
    }

    public void setProgram(int program) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _program = program;
    }

    public void setHavingBank(int havingBank) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _havingBank = havingBank;
    }

    public boolean isHavingVolume() {
        return _havingVolume;
    }

    public void setHavingVolume(boolean havingVolume) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _havingVolume = havingVolume;
    }

    public int getInfoVolume() {
        return _infoVolume;
    }

    public void setInfoVolume(int infoVolume) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _infoVolume = infoVolume;
    }

    public boolean isHavingExpression() {
        return _havingExpression;
    }

    public void setHavingExpression(boolean havingExpression) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _havingExpression = havingExpression;
    }

    public int getInfoExpression() {
        return _infoExpression;
    }

    public void setInfoExpression(int infoExpression) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _infoExpression = infoExpression;
    }

    public boolean isHavingPan() {
        return _havingPan;
    }

    public void setHavingPan(boolean havingPan) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _havingPan = havingPan;
    }

    public int getInfoPan() {
        return _infoPan;
    }

    public void setInfoPan(int infoPan) {
        if (_isImmutable) {
            throw new IllegalStateException("Immutable can't change");
        }
        _infoPan = infoPan;
    }
}
