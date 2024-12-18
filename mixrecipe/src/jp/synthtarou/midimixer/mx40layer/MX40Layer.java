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

import jp.synthtarou.midimixer.libs.midi.visitant.MXVisitant;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;

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
    
    public static MXNamedObjectList createSendOption(boolean haveNoneOption) {
        MXNamedObjectList<Integer> list = new MXNamedObjectList();
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
    final MX40Process _process;
   
    private MXNoteOffWatcher _noteOff = new MXNoteOffWatcher();
    
    public MX40Layer(MX40Process process, MX40Group parentGroup) {
        _process = process;
        _parentGroup = parentGroup;
    }
    
    public String toString() {
        StringBuilder str = new StringBuilder();
        if (_modChannel == MOD_FIXED) {
            str.append("[Channel=").append(MXMidiStatic.nameOfPortOutput(_fixedPort)).append("/").append(_fixedChannel+1).append("]");
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
            str.append(MXMidiStatic.nameOfNote(getAcceptKeyLowest()));
            str.append("-");
            str.append(MXMidiStatic.nameOfNote(getAcceptKeyHighest()));
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
       if (message.parseTemplate(0) >= 0x100) {
            _process.sendToNext(message);
            return true;
        }

        boolean changed = false;
        
        int port = message.getPort();
        int status = message.getStatus();
        int command = status &= 0xffff0;
        int channel = message.getChannel();
        int data1 = message.getCompiled(1);
        int data2 = message.getCompiled(2);
        
        if (command == MXMidiStatic.COMMAND_CH_PROGRAMCHANGE) {
            processProgramChange(message);
            return true;
        }
        
        int port_trans = port;
        int command_trans = command;
        int channel_trans = channel;
        int data1_trans = data1;
        int data2_trans = data2;

        if (command == MXMidiStatic.COMMAND_CH_NOTEOFF) {
            if (_noteOff.raiseHandler(message, port, channel, data1)) {
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
        
        if (command == MXMidiStatic.COMMAND_CH_NOTEON || command == MXMidiStatic.COMMAND_CH_NOTEOFF || command == MXMidiStatic.COMMAND_CH_POLYPRESSURE) {
            if (_adjustTranspose != 0) {
                data1_trans = data1 + _adjustTranspose;
                if(data1_trans < 1) data1_trans  = 0;
                if(data1_trans > 127) data1_trans = 127;
                changed = true;
            }
            if (command == MXMidiStatic.COMMAND_CH_NOTEON && _adjustVelocity != 0) {
                data2_trans = data2 + _adjustVelocity;
                if(data2_trans < 1) data2_trans  = 1;
                if(data2_trans > 127) data2_trans = 127;
                changed = true;
            }
        }

        if (command == MXMidiStatic.COMMAND_CH_NOTEON) {
            MXMessage target = MXMessageFactory.fromNoteoff(port_trans, channel_trans, data1_trans);
            _noteOff.setHandler(message, target, new MXNoteOffWatcher.Handler() {
                public void onNoteOffEvent(MXMessage target) {
                    _process.sendToNext(target);
                }
            });
        }

        if(data1_trans < 0) return true;
        if(data1_trans > 128) return true;

        if (command == MXMidiStatic.COMMAND_CH_NOTEON || command == MXMidiStatic.COMMAND_CH_NOTEOFF || command == MXMidiStatic.COMMAND_CH_POLYPRESSURE) {
            if(data1_trans < getAcceptKeyLowest()) return true;
            if(data1_trans > getAcceptKeyHighest()) return true;
        }

        if (command == MXMidiStatic.COMMAND_CH_NOTEON) {
           if (data2_trans < getAcceptVelocityLowest()) return true;
           if (data2_trans > getAcceptVelocityHighest()) return true;
        }

        if (command == MXMidiStatic.COMMAND_CH_CONTROLCHANGE && message.getCompiled(1) == MXMidiStatic.DATA1_CC_EXPRESSION) {
            if (_adjustExpression != 100) {
                double exp = message.getValue()._value;
                exp = exp * _adjustExpression;
                exp = exp / 100;
                int iexp = (int)exp;
                if (iexp < 0) iexp = 0;
                if (iexp > 127) iexp = 127;
                data2_trans = iexp;
                changed = true;
            }
        }
        if (command == MXMidiStatic.COMMAND_CH_CONTROLCHANGE && message.getCompiled(1) == MXMidiStatic.DATA1_CC_PANPOT) {
            if (_modPan == MOD_FIXED) {
                //int x = message.getValue();
                int y = _fixedPan;
                data2_trans = y;
                changed = true;
            }
        }

        if (changed) {
            MXMessage message_trans = MXMessageFactory.fromShortMessage(port_trans, channel_trans + command, data1_trans, data2_trans);
            message_trans._owner = MXMessage.getRealOwner(message);
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
                MXMessage bank = MXMessageFactory.fromControlChange14(port, channel, MXMidiStatic.DATA1_CC_BANKSELECT, bankMSB, bankLSB);
                bank._owner = MXMessage.getRealOwner(message);
                _process.sendToNext(bank);
                proc = true;
            }
        }else if (_modBank == MOD_FIXED) {
            if (_fixedBankMSB >= 0 && _fixedBankLSB >= 0) {
                MXMessage bank = MXMessageFactory.fromControlChange14(port, channel, MXMidiStatic.DATA1_CC_BANKSELECT, _fixedBankMSB, _fixedBankLSB);
                bank._owner = MXMessage.getRealOwner(message);
                _process.sendToNext(bank);
                proc = true;
            }
        }

        if (_modProgram == MOD_ASFROM) {
            //program = message.getGate();
            if (program >= 0) {
                MXMessage newMessage = MXMessageFactory.fromProgramChange(port, channel, program);
                newMessage._owner = MXMessage.getRealOwner(message);
                _process.sendToNext(newMessage);
                proc = true;
            }
        }else if (_modProgram == MOD_FIXED) {
            if (_fixedProgram >= 0) {
                MXMessage newMessage = MXMessageFactory.fromProgramChange(port, channel, _fixedProgram);
                newMessage._owner = MXMessage.getRealOwner(message);
                _process.sendToNext(newMessage);
                proc = true;
            }
        }

        if (proc) { //プログラムチェンジ直後、EXPとPANをいじる
            int org = info.getCCValue(MXMidiStatic.DATA1_CC_EXPRESSION);
            double exp = org;
            exp = exp * _adjustExpression * 0.01;
            int data2_exp = (int)exp;
            int data1_cc = MXMidiStatic.DATA1_CC_EXPRESSION;
            if (data2_exp < 0) data2_exp = 0;
            if (data2_exp > 127) data2_exp = 127;
            MXMessage newMessage = MXMessageFactory.fromControlChange(port, channel, data1_cc, data2_exp);
            newMessage._owner = MXMessage.getRealOwner(message);
            _process.sendToNext(newMessage);

            org = info.getCCValue(MXMidiStatic.DATA1_CC_PANPOT);
            int data2_value = org;
            if (_modPan == MX40Layer.MOD_FIXED) {
                data2_value = _fixedPan;
            }
            if (data2_value < 0) data2_value = 0;
            if (data2_value > 127) data2_value = 127;
            newMessage = MXMessageFactory.fromControlChange(port, channel, MXMidiStatic.DATA1_CC_PANPOT, data2_value);
            newMessage._owner = MXMessage.getRealOwner(message);
            _process.sendToNext(newMessage);
        }
    }
    
    public boolean equals(Object o) {
        MX40Layer target = (MX40Layer)o;
        if (_title == null || target._title == null) {
        }
        else if (!_title.equals(target._title)) {
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
