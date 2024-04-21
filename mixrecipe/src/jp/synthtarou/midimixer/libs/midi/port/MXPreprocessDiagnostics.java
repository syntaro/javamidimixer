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
package jp.synthtarou.midimixer.libs.midi.port;

import java.util.LinkedList;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXPreprocessDiagnostics {

    MXPreprocess _parent;
    RecordEntry _caret = null;

    int _pastWasRPN = -1;   //1 = RPN 2 = NRPN
    int _pastDataMSB = -1;
    int _pastDataLSB = -1;

    public MXPreprocessDiagnostics(MXPreprocess parent, MXMIDIIn owner) {
        _parent = parent;
        _owner = owner;
    }

    public void recordCC(int cc, int value) {
        MXMessage message = MXMessageFactory.fromControlChange(0, 0, cc, value);
        processMain(message);
    }

    MXMessage _lastMessage = null;

    public void processMain(MXMessage message) {
        if (message == null) {
            return;
        }
        int thisCC = -1;
        if (message.getStatus() >= 0x100) {
            switch(message.getStatus() & 0xfff0) {
                case MXMidi.COMMAND2_CH_RPN:
                case MXMidi.COMMAND2_CH_NRPN:
                    MXTemplate temp = message.getTemplate();
                    RecordEntry e = getEntry(MXMidi.DATA1_CC_DATAENTRY);
                    _caret = e;
                    _pastDataMSB = MXTemplate.parseDAlias(temp.get(1), message);
                    _pastDataLSB  = MXTemplate.parseDAlias(temp.get(2), message);
                    e._pooling0 = MXTemplate.parseDAlias(temp.get(3), message);
                    e._pooling32 = MXTemplate.parseDAlias(temp.get(4), message);
                    if ((message.getStatus() & 0xfff0) == MXMidi.COMMAND2_CH_RPN)  {
                        _pastWasRPN = 1;
                    }else {
                        _pastWasRPN = 2;
                    }
                    flushPool();
                    return;
            }
        }
        if (message.isCommand(MXMidi.COMMAND_CH_PITCHWHEEL)) {
            MXRangedValue value = message.getValue();
            message = MXMessageFactory.fromTemplate(message.getPort()
                    , MXMidi.TEMPLATE_CCXMLPB, message.getChannel()
                    , MXRangedValue.ZERO7, value);
        }

        if (message.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
            thisCC = message.getCompiled(1);
        }

        MXMessage last = _lastMessage;
        _lastMessage = message;
        int lastCC = -1;

        if (last != null && last.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
            lastCC = last.getCompiled(1);
        }

        switch (thisCC) {
            case MXMidi.DATA1_CC_RPN_MSB:
                _pastDataMSB = message.getCompiled(2);
                _pastWasRPN = 1;
                return;
            case MXMidi.DATA1_CC_RPN_LSB:
                _pastDataLSB = message.getCompiled(2);
                _pastWasRPN = 1;
                return;
            case MXMidi.DATA1_CC_NRPN_MSB:
                _pastDataMSB = message.getCompiled(2);
                _pastWasRPN = 2;
                return;
            case MXMidi.DATA1_CC_NRPN_LSB:
                _pastDataLSB = message.getCompiled(2);
                _pastWasRPN = 2;
                return;
        }

        if (thisCC >= 0 && thisCC <= 0x3f) {
            RecordEntry e = getEntry(thisCC);
            if (thisCC >= 0x20 && thisCC <= 0x3f) {
                e._count20h++;
                if (lastCC + 0x20 == thisCC) {
                    e._countPair++;
                    e._count0h--;
                    e._count20h--;
                }
            } else {
                e._count0h++;
                if (lastCC - 0x20 == thisCC) {
                    e._countPair++;
                    e._count0h--;
                    e._count20h--;
                }
            }
            e.recalc();
            if (_caret == null || _caret._cc != e._cc) {
                if (_caret != null) {
                    flushPool();
                }
                _caret = e;
                e._pooling0 = -1;
                e._pooling32 = -1;
            }
            if (e.is14bitChoiced()) {
                boolean is32 = (thisCC >= 0x20 && thisCC <= 0x3f);
                //mean 0 <= e.cc <= 0x1f
                if (is32) {
                    e._pooling32 = message.getCompiled(2);
                    if (e._pooling0 >= 0) {
                        flushPool();
                    }
                } else {
                    e._pooling0 = message.getCompiled(2);
                    if (e._pooling32 >= 0) {
                        flushPool();
                    }
                }
            } else {
                flushPool();
                addResult(message);
            }
        } else {
            if (_caret != null) 
            {
                flushPool();
            }
            addResult(message);
        }
    }

    MXTemplate cc7bit = new MXTemplate(new int[]{MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.CCXML_GL, MXMidi.CCXML_VL});
    MXTemplate cc14bit = new MXTemplate(new int[]{MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.CCXML_GL, MXMidi.CCXML_VH, MXMidi.CCXML_VL});

    MXTemplate datar7bit = new MXTemplate(new int[]{MXMidi.COMMAND2_CH_RPN, MXMidi.CCXML_GH, MXMidi.CCXML_GL, MXMidi.CCXML_VL});
    MXTemplate datar14bit = new MXTemplate(new int[]{MXMidi.COMMAND2_CH_RPN, MXMidi.CCXML_GH, MXMidi.CCXML_GL, MXMidi.CCXML_VH, MXMidi.CCXML_VL});

    MXTemplate datan7bit = new MXTemplate(new int[]{MXMidi.COMMAND2_CH_NRPN, MXMidi.CCXML_GH, MXMidi.CCXML_GL, MXMidi.CCXML_VL});
    MXTemplate datan14bit = new MXTemplate(new int[]{MXMidi.COMMAND2_CH_NRPN, MXMidi.CCXML_GH, MXMidi.CCXML_GL, MXMidi.CCXML_VH, MXMidi.CCXML_VL});

    MXRangedValue dataRoom = new MXRangedValue(128 * 30 + 20, 0, 128 * 128 - 1);

    public synchronized void flushPool() {
        if (_caret == null) {
            return;
        }
        int value0 = _caret._pooling0;
        int value32 = _caret._pooling32;
        //System.out.println("flush value = " +value0 +" , " + + value32 + " room = "+  _pastDataMSB + " ," + _pastDataLSB);
        if (value0 < 0) {
            return;
        }
        _caret._pooling0 = -1;
        _caret._pooling32 = -1;
        if (_pastDataLSB >= 0 && _pastDataMSB >= 0 && (_pastWasRPN == 1 || _pastWasRPN == 2)) {
            MXRangedValue dataroom = MXRangedValue.new14bit((_pastDataMSB << 7) | _pastDataLSB);
            MXTemplate template7 = null;
            MXTemplate template14 = null;

            if (_pastWasRPN == 1) {
                template7 = datar7bit;
                template14 = datar14bit;
            } else {
                template7 = datan7bit;
                template14 = datan14bit;
            }

            MXRangedValue zero7 = MXRangedValue.ZERO7;
            MXRangedValue zero14 = MXRangedValue.ZERO14;
            if (value32 < 0) {
                MXMessage message = MXMessageFactory.fromTemplate(0, template7, 0, zero14, zero7);
                message.setGate(dataroom);
                message.setValue(MXRangedValue.new7bit(value0));
                addResult(message);
            } else {
                MXMessage message = MXMessageFactory.fromTemplate(0, template14, 0, zero14, zero14);
                message.setGate(dataroom);
                message.setValue(MXRangedValue.new14bit(value32 | (value0 << 7)));
                addResult(message);
            }
        }
        _caret = null;
    }

    synchronized void addResult(MXMessage message) {
        if (message == null) {
            //new IllegalCallerException("NULL").printStackTrace();
            return;
        }
        if (message.isEmpty()) {
            //new IllegalCallerException("EMPTY").printStackTrace();
            return;
        }
        if (message.getStatus() == MXMidi.COMMAND_CH_CONTROLCHANGE) {
            //DATAENTRY設定中
        }
        _result.add(message);
    }

    public synchronized RecordEntry getEntry(int cc) {
        MXMIDIIn in = _owner;
        int seek = (cc >= 32 && cc < 64) ? (cc - 32) : cc;
        if (listCC[seek] == null) {
            listCC[seek] = new RecordEntry(in, seek);
        }
        return listCC[seek];
    }

    public synchronized MXMessage popResult() {
        if (_result.isEmpty()) {
            return null;
        }
        return _result.removeFirst();
    }

    RecordEntry[] listCC = new RecordEntry[128];
    MXMIDIIn _owner;
    LinkedList<MXMessage> _result = new LinkedList<>();

    public static void flush(MXPreprocessDiagnostics diag) {
        System.out.println("before Flush");
        while (true) {
            MXMessage e = diag.popResult();
            if (e == null) {
                break;
            }
            System.out.println("result " + e + "[" + e.toStringDumped());//e.toStringMessageInfo(1));
        }
        System.out.println("after Flush");
        diag.flushPool();
        while (true) {
            MXMessage e = diag.popResult();
            if (e == null) {
                break;
            }
            System.out.println("result " + e + "[" + e.toStringDumped());//e.toStringMessageInfo(1));
        }
    }

    public static void test0(MXPreprocessDiagnostics diag) {
        System.out.println("test0");
        diag.processMain(MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_RPN_MSB, 10));
        diag.processMain(MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_RPN_LSB, 50));
        diag.recordCC(0 + 6, 10);
        diag.recordCC(32 + 6, 1);
        diag.recordCC(0 + 6, 10);
        diag.recordCC(32 + 6, 2);
        diag.recordCC(0 + 6, 10);
        diag.recordCC(32 + 6, 3);
        diag.recordCC(0 + 6, 10);
        diag.recordCC(32 + 6,4);
        diag.processMain(MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_NRPN_MSB, 1));
        diag.processMain(MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_NRPN_LSB, 2));
        diag.recordCC(0 + 6, 10);
        diag.recordCC(32 + 6, 5);
        diag.recordCC(0 + 6, 10);
        diag.recordCC(32 + 6,6);
        diag.recordCC(0 + 6, 10);
        diag.recordCC(32 + 6, 7);
        diag.recordCC(0 + 6, 10);
        diag.recordCC(32 + 6, 8);
        flush(diag);
    }

    public static void test1(MXPreprocessDiagnostics diag) {
        System.out.println("test1");
        MXMessage message;
        for (int i = 0; i < 128; ++i) {
            for (int j = 0; j < 128; ++j) {
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_NOTEON, j, j);
                diag.processMain(message);
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_NOTEOFF, j, j);
                diag.processMain(message);
            }
        }
        flush(diag);
    }

    public static void test2(MXPreprocessDiagnostics diag) {
        System.out.println("test2");
        MXMessage message;
        for (int i = 0; i < 128; ++i) {
            for (int j = 0; j < 128; ++j) {
                diag.recordCC(i, j);
            }
        }
        flush(diag);
    }

    public static void test3(MXPreprocessDiagnostics diag) {
        System.out.println("test3");
        MXMessage message;
        for (int i = 0; i < 128; ++i) {
            for (int j = 0; j < 128; ++j) {
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_NOTEON, j, j);
                diag.processMain(message);
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_NOTEOFF, j, j);
                diag.processMain(message);
            }
        }
        flush(diag);
    }

    public static void test4(MXPreprocessDiagnostics diag) {
        System.out.println("test4");
        MXMessage message;
        for (int i = 0; i < 128; ++i) {
            for (int j = 0; j < 128; ++j) {
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_DATAENTRY, i);
                diag.processMain(message);
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_DATAENTRY2, j);
                diag.processMain(message);
            }
        }
        flush(diag);
    }

    public static void test5(MXPreprocessDiagnostics diag) {
        System.out.println("test5");
        MXMessage message;
        for (int i = 0; i < 128; ++i) {
            message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_DATAENTRY, i);
            diag.processMain(message);
        }
        flush(diag);
    }

    public static void main(String[] args) {
        MXPreprocess parent = new MXPreprocess(null);
        MXPreprocessDiagnostics diag = new MXPreprocessDiagnostics(parent, MXMIDIIn.INTERNAL_PLAYER);
        int x = 0;
        switch (x) {
            case 0:
                test0(diag);
                break;
            case 1:
                test1(diag);
                break;
            case 2:
                test2(diag);
                break;
            case 3:
                test3(diag);
                break;
            case 4:
                test4(diag);
                break;
            case 5:
                test5(diag);
                break;
            default:
                test0(diag);
                test1(diag);
                test2(diag);
                test3(diag);
                test4(diag);
                test5(diag);
                break;
        }
    }
}
