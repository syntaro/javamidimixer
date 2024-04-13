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
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.log.MXFileLogger;
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
        MXMessage message101 = MXMessageFactory.fromShortMessage(0, 0, MXMidi.DATA1_CC_RPN_MSB, msb);
        MXMessage messageMSB = MXMessageFactory.fromShortMessage(0, 0, MXMidi.DATA1_CC_DATAENTRY, high);
        MXMessage messageLSB = MXMessageFactory.fromShortMessage(0, 0, MXMidi.DATA1_CC_DATAENTRY + 32, low);
        message100._owner = parent;
        message101._owner = parent;
        messageMSB._owner = parent;
        messageLSB._owner = parent;
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
        message98._owner = parent;
        message99._owner = parent;
        messageMSB._owner = parent;
        messageLSB._owner = parent;
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
            int x1 = _input.get(0).getAsDword(0);
            int x2 = _input.get(1).getAsDword(0);
            int x3 = _input.get(2).getAsDword(0);
            int x4 = _input.get(3).getAsDword(0);
            
            if (_result.size() == 1) {
                MXMessage message = _result.get(0);
                int y1 = message.getAsDword(0);
                int y2 = message.getAsDword(1);
                int y3 = message.getAsDword(2);
                int y4 = message.getAsDword(3);

                if (x1 != y1 || x2 != y2 || x3 != y3 || x4 != y4) {
                    String str1 = MXUtil.toHexFF(x1) + ", " + MXUtil.toHexFF(x2) + ", " + MXUtil.toHexFF(x3) + ", " + MXUtil.toHexFF(x4) ;
                    String str2 = MXUtil.toHexFF(y1) + ", " + MXUtil.toHexFF(y2) + ", " + MXUtil.toHexFF(y3) + ", " + MXUtil.toHexFF(y4) ;
                    MXFileLogger.getLogger(MXDebugDataEntry.class).severe("fail in (" + str1 + ") out (" + str2+ ")");
                }
            }
        }
        else {
            MXFileLogger.getLogger(MXDebugDataEntry.class).severe("Size error in (" + _input.size() + " is not 4)" + _input);
        }
        if (_result.size() != 1) {
            MXFileLogger.getLogger(MXDebugDataEntry.class).severe("Size error out (" +_result.size() + " is not 1)");
            for (int i = 0; i < _result.size(); ++ i) {
                System.out.println(_result.get(i));
            }
        }
    }
}
