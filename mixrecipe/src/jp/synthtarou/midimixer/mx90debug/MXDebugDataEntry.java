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
package jp.synthtarou.midimixer.mx90debug;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 *
 */
public class MXDebugDataEntry extends MXDebug {

    static List<MXMessage> createRPN(int msb, int lsb, int data) {
        int high = (data >> 7) & 0x7f;
        int low = data & 0x7f;
        MXMessage parent = MXMessageFactory.newEmpty(0);
        MXMessage message100 = MXMessageFactory.fromControlChange(0, 0, MXMidi.DATA1_CC_RPN_LSB, lsb);
        MXMessage message101 = MXMessageFactory.fromControlChange(0, 0, MXMidi.DATA1_CC_RPN_MSB, msb);
        MXMessage messageMSB = MXMessageFactory.fromControlChange(0, 0, MXMidi.DATA1_CC_DATAENTRY, high);
        MXMessage messageLSB = MXMessageFactory.fromControlChange(0, 0, MXMidi.DATA1_CC_DATAENTRY + 32, low);
        message100._owner = MXMessage.getRealOwner(parent);
        message101._owner = MXMessage.getRealOwner(parent);
        messageMSB._owner = MXMessage.getRealOwner(parent);
        messageLSB._owner = MXMessage.getRealOwner(parent);
        ArrayList<MXMessage> list = new ArrayList();
        list.add(message101);
        list.add(message100);
        list.add(messageMSB);
        list.add(messageLSB);
        return list;
    }
 
    static List<MXMessage> createNRPN(int msb, int lsb, int data) {
        int high = (data >> 7) & 0x7f;
        int low = data & 0x7f;
        MXMessage parent = MXMessageFactory.newEmpty(0);
        MXMessage message98 = MXMessageFactory.fromControlChange(0, 0, MXMidi.DATA1_CC_NRPN_LSB, lsb);
        MXMessage message99 = MXMessageFactory.fromControlChange(0, 0, MXMidi.DATA1_CC_NRPN_MSB, msb);
        MXMessage messageMSB = MXMessageFactory.fromControlChange(0, 0, MXMidi.DATA1_CC_DATAENTRY, high);
        MXMessage messageLSB = MXMessageFactory.fromControlChange(0, 0, MXMidi.DATA1_CC_DATAENTRY + 32, low);
        message98._owner = MXMessage.getRealOwner(parent);
        message99._owner = MXMessage.getRealOwner(parent);
        messageMSB._owner = MXMessage.getRealOwner(parent);
        messageLSB._owner = MXMessage.getRealOwner(parent);
        ArrayList<MXMessage> list = new ArrayList();
        list.add(message99);
        list.add(message98);
        list.add(messageMSB);
        list.add(messageLSB);
        return list;
    }
    
    public MXDebugDataEntry(boolean type, int msb, int lsb, int data) {
        super(type ? createRPN(msb, lsb, data) : createNRPN(msb, lsb, data));
    }

    @Override
    public void checkResult() {
        if (_input.size() == 4) {
            OneMessage x1 = _input.get(0).toOneMessage(0);
            OneMessage x2 = _input.get(1).toOneMessage(0);
            OneMessage x3 = _input.get(2).toOneMessage(0);
            OneMessage x4 = _input.get(3).toOneMessage(0);
            
            if (_result.size() == 1) {
                MXMessage message = _result.get(0);
                OneMessage y1 = message.toOneMessage(0);
                OneMessage y2 = message.toOneMessage(1);
                OneMessage y3 = message.toOneMessage(2);
                OneMessage y4 = message.toOneMessage(3);

                if (x1.equals(y1) == false
                 || x2.equals(y2) == false
                 || x3.equals(y3) == false
                 || x4.equals(y4) == false) {
                    String str1 = x1 + ", " + x2 + ", " + x3 + ", " + x4;
                    String str2 = y1 + ", " + y2 + ", " + y3 + ", " + y4;
                    MXFileLogger.getLogger(MXDebugDataEntry.class).severe("fail in (" + str1 + ") out (" + str2+ ")");
                }
            }
        }
        else {
            MXFileLogger.getLogger(MXDebugDataEntry.class).log(Level.SEVERE, "Size error in (" + _input.size() + " must be 4)" + _input, new Throwable());
        }
        if (_result.size() != 1) {
            MXFileLogger.getLogger(MXDebugDataEntry.class).log(Level.SEVERE, "Size error out (" +_result.size() + " must be 1)", new Throwable());
            for (int i = 0; i < _input.size(); ++ i) {
                System.out.println("input dump"  +_input.get(i));
            }
            for (int i = 0; i < _result.size(); ++ i) {
                System.out.println("result dump"  +_result.get(i));
            }
        }
    }
}
