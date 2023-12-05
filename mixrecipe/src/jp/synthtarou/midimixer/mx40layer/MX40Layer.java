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

import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.midimixer.libs.midi.MXTiming;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX40Layer {

    /**
     * @return the _acceptVelocityLowest
     */
    public int getAcceptVelocityLowest() {
        return _acceptVelocityLowest;
    }

    /**
     * @param _acceptVelocityLowest the _acceptVelocityLowest to set
     */
    public void setAcceptVelocityLowest(int _acceptVelocityLowest) {
        this._acceptVelocityLowest = _acceptVelocityLowest;
    }

    /**
     * @return the _acceptVelocityHighest
     */
    public int getAcceptVelocityHighest() {
        return _acceptVelocityHighest;
    }

    /**
     * @param _acceptVelocityHighest the _acceptVelocityHighest to set
     */
    public void setAcceptVelocityHighest(int _acceptVelocityHighest) {
        this._acceptVelocityHighest = _acceptVelocityHighest;
    }

    /**
     * @return the _acceptKeyLowest
     */
    public int getAcceptKeyLowest() {
        return _acceptKeyLowest;
    }

    /**
     * @param _acceptKeyLowest the _acceptKeyLowest to set
     */
    public void setAcceptKeyLowest(int _acceptKeyLowest) {
        this._acceptKeyLowest = _acceptKeyLowest;
    }

    /**
     * @return the _acceptKeyHighest
     */
    public int getAcceptKeyHighest() {
        return _acceptKeyHighest;
    }

    /**
     * @param _acceptKeyHighest the _acceptKeyHighest to set
     */
    public void setAcceptKeyHighest(int acceptKeyHighest) {
        this._acceptKeyHighest = acceptKeyHighest;
        /*
        if (acceptKeyHighest == 0) {
            new Throwable("setAcceptKeyHighest 0").printStackTrace();
        }*/
    }
    
    public static final int MOD_NONE = 0;
    public static final int MOD_ASFROM = 1;
    public static final int MOD_FIXED = 2;
    
    public static String modName(int mod) {
        switch(mod) {
            case MOD_NONE: return "mod-none";
            case MOD_ASFROM: return "mod-asfrom";
            case MOD_FIXED: return "mod-fixed";
        }
        return "mod-unknown";
    }
    
    public static MXWrapList createSendOption(boolean haveNoneOption) {
        MXWrapList<Integer> list = new MXWrapList();
        list.addNameAndValue("Fixed", MOD_FIXED);
        list.addNameAndValue("AsFrom", MOD_ASFROM);
        if (haveNoneOption) {
            list.addNameAndValue("none", MOD_NONE);
        }
        return list;
    }
    
    public String _title = "New Layer";

    public int _modPort = MOD_ASFROM;
    public int _fixedPort;

    public int _modChannel = MOD_ASFROM;
    public int _fixedChannel;

    public int _modBank = MOD_NONE;
    public int _fixedBankMSB;
    public int _fixedBankLSB;

    public int _modProgram = MOD_NONE;
    public int _fixedProgram;

    public int _modPan = MOD_ASFROM;
    public int _fixedPan = 64;
    
    public int _adjustTranspose;
    public int _adjustVelocity;
    public int _adjustExpression = 100;

    private int _acceptKeyLowest = 0;
    private int _acceptKeyHighest = 127;

    private int _acceptVelocityLowest = 0;
    private int _acceptVelocityHighest = 127;
    
    public boolean _disabled = false;
    
    public MX40Group _parentGroup;
    public MX40Process _process;
   
    private MXNoteOffWatcher _noteOff = new MXNoteOffWatcher();
    
    public MX40Layer(MX40Process process, MX40Group parentGroup) {
        _process = process;
        _parentGroup = parentGroup;
    }
    
    public String toString() {
        StringBuffer str = new StringBuffer();
        if (_modChannel == MOD_FIXED) {
            str.append("[Channel=").append(MXMidi.nameOfPortOutput(_fixedPort)).append("/").append(_fixedChannel+1).append("]");
        }
        if (_modBank == MOD_FIXED) {
            str.append("[Bank=").append(MXUtil.toHexFF(_fixedBankMSB));
            str.append(":").append(MXUtil.toHexFF(_fixedBankLSB)).append("]");
        }else if (_modBank == MOD_ASFROM) {
            str.append("[Bank=AsFrom]");
        }
        if (_modProgram == MOD_FIXED) {
            str.append("[Program=").append(_fixedProgram).append("]");
        }else if (_modProgram == MOD_ASFROM) {
            str.append("[Program=AsFrom]");
        }
        if (getAcceptKeyLowest() != 0 || getAcceptKeyHighest() != 127) {
            str.append("[Note=");
            str.append(MXMidi.nameOfNote(getAcceptKeyLowest()));
            str.append("-");
            str.append(MXMidi.nameOfNote(getAcceptKeyHighest()));
            str.append("]");
        }
        if (getAcceptVelocityLowest()!= 0 || getAcceptVelocityHighest() != 127) {
            str.append("[Velocity=");
            str.append(getAcceptVelocityLowest());
            str.append("-");
            str.append(getAcceptVelocityHighest());
            str.append("]");
        }
        if (_adjustTranspose != 0) {
            str.append("[Transpose=");
            str.append(_adjustTranspose);
            str.append("]");
        }
        
        return str.toString();
    }

    public boolean processByLayer(MXMessage message) {
        if (message.isMessageTypeChannel() == false) {
            _process.sendToNext(message);
            return true;
        }

        boolean changed = false;
        
        int port = message.getPort();
        int status = message.getStatus();
        int command = status;
        if (message.isMessageTypeChannel()) {
            command &= 0xf0;
        }
        int channel = message.getChannel();
        int data1 = message.getData1();
        int data2 = message.getData2();
        
        if (command == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
            processProgramChange(message);
            return true;
        }
        
        int port_trans = port;
        int command_trans = command;
        int channel_trans = channel;
        int data1_trans = data1;
        int data2_trans = data2;

        if (command == MXMidi.COMMAND_CH_NOTEOFF) {
            if (_noteOff.raiseHandler(port, message._timing, channel, data1)) {
                return true;
            }
        }

        if (_modPort == MOD_FIXED) {
            changed = true;
            port_trans = _fixedPort;
        }
        
        if (_modChannel == MOD_FIXED) {
            changed = true;
            channel_trans = _fixedChannel;
        }

        if (command == MXMidi.COMMAND_CH_NOTEON || command == MXMidi.COMMAND_CH_NOTEOFF || command == MXMidi.COMMAND_CH_POLYPRESSURE) {
            if (_adjustTranspose != 0) {
                data1_trans = data1 + _adjustTranspose;
                if(data1_trans < 1) data1_trans  = 0;
                if(data1_trans > 127) data1_trans = 127;
                changed = true;
            }
            if (command == MXMidi.COMMAND_CH_NOTEON && _adjustVelocity != 0) {
                data2_trans = data2 + _adjustVelocity;
                if(data2_trans < 1) data2_trans  = 1;
                if(data2_trans > 127) data2_trans = 127;
                changed = true;
            }
        }

        if (command == MXMidi.COMMAND_CH_NOTEON) {
            MXMessage target = MXMessageFactory.fromShortMessage(port_trans, MXMidi.COMMAND_CH_NOTEOFF + channel_trans, data1_trans, 0);
            target._timing = message._timing;
            _noteOff.setHandler(message, target, new MXNoteOffWatcher.Handler() {
                public void onNoteOffEvent(MXTiming timing, MXMessage target) {
                    target._timing = timing;
                    _process.sendToNext(target);
                }
            });
        }

        if(data1_trans < 0) return true;
        if(data1_trans > 128) return true;

        if (command == MXMidi.COMMAND_CH_NOTEON || command == MXMidi.COMMAND_CH_NOTEOFF || command == MXMidi.COMMAND_CH_POLYPRESSURE) {
            if(data1_trans < getAcceptKeyLowest()) return true;
            if(data1_trans > getAcceptKeyHighest()) return true;
        }

        if (command == MXMidi.COMMAND_CH_NOTEON) {
           if (data2_trans < getAcceptVelocityLowest()) return true;
           if (data2_trans > getAcceptVelocityHighest()) return true;
        }

        if (command == MXMidi.COMMAND_CH_CONTROLCHANGE && message.getGate()._var == MXMidi.DATA1_CC_EXPRESSION) {
            if (_adjustExpression != 100) {
                double exp = message.getValue()._var;
                exp = exp * _adjustExpression;
                exp = exp / 100;
                int iexp = (int)exp;
                if (iexp < 0) iexp = 0;
                if (iexp > 127) iexp = 127;
                data2_trans = iexp;
                changed = true;
            }
        }
        if (command == MXMidi.COMMAND_CH_CONTROLCHANGE && message.getGate()._var == MXMidi.DATA1_CC_PANPOT) {
            if (_modPan == MOD_FIXED) {
                //int x = message.getValue();
                int y = _fixedPan;
                data2_trans = y;
                changed = true;
            }
        }

        if (changed) {
            MXMessage message_trans = MXMessageFactory.fromShortMessage(port_trans, channel_trans + command, data1_trans, data2_trans);
            message_trans._timing = message._timing;
            message = message_trans;
        }

        _process.sendToNext(message);

        return true;
    }
    
    public void processProgramChange(MXMessage message) {
        int port = message.getPort();
        int channel = message.getChannel();
        
        MXVisitant info = _process._inputInfo.getVisitant(port, channel);
        info.mergeNew(message.getVisitant());

        int bankMSB = info.isHavingBank() ? info.getBankMSB() : -1;
        int bankLSB = info.isHavingBank() ? info.getBankLSB() : -1;
        int program =  info.isHavingProgram() ? info.getProgram() : -1;
        
        if (_modPort == MOD_FIXED) {
            port = _fixedPort;
        }

        if (_modChannel == MOD_FIXED) {
            channel = _fixedChannel;
        }
        
        boolean proc = false;

        if (_modBank == MOD_ASFROM) {
            if (bankMSB >= 0 && bankLSB >= 0) {
                MXMessage msb = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_CONTROLCHANGE + channel, MXMidi.DATA1_CC_BANKSELECT, bankMSB);
                MXMessage lsb = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_CONTROLCHANGE + channel, MXMidi.DATA1_CC_BANKSELECT + 32 , bankLSB);

                msb._timing = message._timing;
                lsb._timing = message._timing;

                _process.sendToNext(msb);
                _process.sendToNext(lsb);
                proc = true;
            }
        }else if (_modBank == MOD_FIXED) {
            if (_fixedBankMSB >= 0 && _fixedBankLSB >= 0) {
                MXMessage msb  = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_CONTROLCHANGE + channel, MXMidi.DATA1_CC_BANKSELECT, _fixedBankMSB);
                MXMessage lsb  = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_CONTROLCHANGE + channel, MXMidi.DATA1_CC_BANKSELECT + 32, _fixedBankLSB);

                msb._timing = message._timing;
                lsb._timing = message._timing;

                _process.sendToNext(msb);
                _process.sendToNext(lsb);
                proc = true;
            }
        }

        if (_modProgram == MOD_ASFROM) {
            //program = message.getGate();
            if (program >= 0) {
                MXMessage message2 = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_PROGRAMCHANGE + channel, program, 0);
                message2._timing = message._timing;
                _process.sendToNext(message2);
                proc = true;
                //System.out.println("Program Change +" + message);
            }else {
                //System.out.println("Program Change -");
            }
        }else if (_modProgram == MOD_FIXED) {
            if (_fixedProgram >= 0) {
                MXMessage message2 = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_PROGRAMCHANGE + channel, _fixedProgram, 0);
                message2._timing = message._timing;
                _process.sendToNext(message2);
                proc = true;
            }
        }

        if (proc) {
            double exp = info.getInfoExpression();
            exp = exp * _adjustExpression * 0.01;
            int data2_exp = (int)exp;
            int command = MXMidi.COMMAND_CH_CONTROLCHANGE;
            int data1_cc = MXMidi.DATA1_CC_EXPRESSION;
            if (data2_exp < 0) data2_exp = 0;
            if (data2_exp > 127) data2_exp = 127;
            MXMessage message2 = MXMessageFactory.fromShortMessage(port, command + channel, data1_cc, data2_exp);
            message2._timing = message._timing;
            _process.sendToNext(message2);

            command = MXMidi.COMMAND_CH_CONTROLCHANGE;
            int data2_value = info.getInfoPan();
            if (_modPan == MX40Layer.MOD_FIXED) {
                data2_value = _fixedPan;
            }
            if (data2_value < 0) data2_value = 0;
            if (data2_value > 127) data2_value = 127;
            message2 = MXMessageFactory.fromShortMessage(port, command + channel, MXMidi.DATA1_CC_PANPOT, data2_value);
            message2._timing = message._timing;
            _process.sendToNext(message2);
        }
    }
    
    public boolean equals(Object o) {
        MX40Layer target = (MX40Layer)o;
        if (!_title.equals(target._title)) {
            return false;
        }
        if (_modPort != target._modPort) {
            return false;
        }
        if (_modPort == MX40Layer.MOD_FIXED && _fixedPort != target._fixedPort) {
            return false;
        }
        if (_modChannel != target._modChannel) {
            return false;
        }
        if (_modChannel == MX40Layer.MOD_FIXED && _fixedChannel != target._fixedChannel) {
            return false;
        }
        if (_modBank != target._modBank) {
            return false;
        }
        if (_modBank == MX40Layer.MOD_FIXED) {
            if (_fixedBankMSB != target._fixedBankMSB || _fixedBankLSB != target._fixedBankLSB) {
                return false;
            }
        }
        
        if (_modProgram != target._modProgram) {
            return false;
        }
        if (_modProgram == MX40Layer.MOD_FIXED && _fixedProgram != target._fixedProgram) {
            return false;
        }
        if (_modPan != target._modPan) {
            return false;
        }
        if (_modPan == MX40Layer.MOD_FIXED && _fixedPan != target._fixedPan) {
            return false;
        }
        if (_fixedPan == target._fixedPan
         && _adjustTranspose == target._adjustTranspose
         && _adjustVelocity == target._adjustVelocity
         && _adjustExpression == target._adjustExpression
         && getAcceptKeyLowest() == target.getAcceptKeyLowest()
         && getAcceptKeyHighest() == target.getAcceptKeyHighest()) {
            return true;
        }
        return false;
    }
}
