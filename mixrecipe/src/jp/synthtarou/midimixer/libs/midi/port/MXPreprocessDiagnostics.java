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

    public static void flush(MXPreprocessDiagnostics diag) {
        System.out.println("before Flush");
        while (true) {
            MXMessage e = diag.popResult();
            if (e == null) {
                break;
            }
            System.out.println("result " + e.toStringMessageInfo(1));
        }
        System.out.println("after Flush");
        diag.flushPool();
        while (true) {
            MXMessage e = diag.popResult();
            if (e == null) {
                break;
            }
            System.out.println("result " + e.toStringMessageInfo(1));
        }
    }

    public static void test0(MXPreprocessDiagnostics diag) {
        System.out.println("test0");
        diag.record(MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_RPN_MSB, 10));
        diag.record(MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_RPN_LSB, 50));
        diag.record(0 + 6, 10);
        diag.record(32 + 6, 0);
        diag.record(0 + 6, 10);
        diag.record(32 + 6, 0);
        diag.record(0 + 6, 10);
        diag.record(32 + 6, 0);
        diag.record(0 + 6, 10);
        diag.record(32 + 6, 0);
        diag.record(MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_NRPN_MSB, 1));
        diag.record(MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_NRPN_LSB, 2));
        diag.record(0 + 6, 10);
        diag.record(32 + 6, 0);
        diag.record(0 + 6, 10);
        diag.record(32 + 6, 0);
        diag.record(0 + 6, 10);
        diag.record(32 + 6, 0);
        diag.record(0 + 6, 10);
        diag.record(32 + 6, 0);
        flush(diag);
    }

    public static void test1(MXPreprocessDiagnostics diag) {
        System.out.println("test1");
        MXMessage message;
        for (int i = 0; i < 128; ++i) {
            for (int j = 0; j < 128; ++j) {
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_NOTEON, j, j);
                diag.record(message);
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_NOTEOFF, j, j);
                diag.record(message);
            }
        }
        flush(diag);
    }

    public static void test2(MXPreprocessDiagnostics diag) {
        System.out.println("test2");
        MXMessage message;
        for (int i = 0; i < 128; ++i) {
            for (int j = 0; j < 128; ++j) {
                diag.record(i, j);
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
                diag.record(message);
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_NOTEOFF, j, j);
                diag.record(message);
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
                diag.record(message);
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_DATAENTRY2, j);
                diag.record(message);
            }
        }
        flush(diag);
    }

    public static void test5(MXPreprocessDiagnostics diag) {
        System.out.println("test5");
        MXMessage message;
        for (int i = 0; i < 128; ++i) {
            message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_DATAENTRY, i);
            diag.record(message);
        }
        flush(diag);
    }

    public static void main(String[] args) {
        MXPreprocess parent = new MXPreprocess(null);
        MXPreprocessDiagnostics diag = new MXPreprocessDiagnostics(parent, 0);
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

    int _channel;
    MXPreprocess _parent;
    RecordEntry _caret = null;

    class RecordEntry {

        public RecordEntry(int cc) {
            _cc = cc;
            _needCompact = (cc == MXMidi.DATA1_CC_DATAENTRY);
        }

        int _cc;

        int _count0 = 0;
        int _count32 = 0;
        boolean _needCompact = true;

        int _pooling0 = -1;
        int _pooling32 = -1;
    }

    int _pastWasRPN = -1;   //1 = RPN 2 = NRPN
    int _pastDataMSB = -1;
    int _pastDataLSB = -1;

    public MXPreprocessDiagnostics(MXPreprocess parent, int channel) {
        _channel = channel;
        _parent = parent;
    }

    public void record(int cc, int value) {
        MXMessage message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, cc, value);
        record(message);
    }

    public void record(MXMessage message) {
        if (message.getTemplate().size() >= 3) {
            if (message.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
                int cc = message.getCompiled(1);
                switch (cc) {
                    case MXMidi.DATA1_CC_RPN_MSB:
                        _pastDataMSB = message.getCompiled(2);
                        _pastWasRPN = 1;
                        break;
                    case MXMidi.DATA1_CC_RPN_LSB:
                        _pastDataLSB = message.getCompiled(2);
                        _pastWasRPN = 1;
                        break;
                    case MXMidi.DATA1_CC_NRPN_MSB:
                        _pastDataMSB = message.getCompiled(2);
                        _pastWasRPN = 2;
                        break;
                    case MXMidi.DATA1_CC_NRPN_LSB:
                        _pastDataLSB = message.getCompiled(2);
                        _pastWasRPN = 2;
                        break;
                }
                RecordEntry e = getEntry(cc);
                if (cc == e._cc) {
                    e._count0++;
                } else {
                    e._count32++;
                    _parent.addText(e._cc + " = " + e._count0 + "," + e._count32);
                }
                if (_caret == null || _caret._cc != e._cc) {
                    flushPool();
                    _caret = e;
                    e._pooling0 = -1;
                    e._pooling32 = -1;
                }
                if (e._needCompact) {
                    boolean is32 = (cc != e._cc);
                    if (is32) {
                        e._pooling32 = message.getCompiled(2);
                        if (e._pooling0 >= 0) {
                            flushPool();
                            e._pooling0 = -1;
                            e._pooling32 = -1;
                        }
                    } else {
                        e._pooling0 = message.getCompiled(2);
                        if (e._pooling32 >= 0) {
                            flushPool();
                            e._pooling0 = -1;
                            e._pooling32 = -1;
                        }
                    }
                } else {
                    flushPool();
                    addResult(message);
                }
            } else {
                flushPool();
                addResult(message);
            }
        } else {
            flushPool();
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
        if (_caret != null) {
            int value0 = _caret._pooling0;
            int value32 = _caret._pooling32;
            if (value0 < 0) {
                return;
            }
            if (_pastDataLSB >= 0 && _pastDataMSB >= 0 && (_pastWasRPN== 1 || _pastWasRPN == 2)) {
                MXRangedValue dataroom = MXRangedValue.new14bit((_pastDataMSB << 7) | _pastDataLSB);
                MXTemplate template7 = null;
                MXTemplate template14 = null;
                
                if (_pastWasRPN == 1) {
                    template7 = datar7bit;
                    template14 = datar14bit;
                }
                else {
                    template7 = datan7bit;
                    template14 = datan14bit;
                }
                
                if (_caret._cc == MXMidi.DATA1_CC_DATAENTRY) {
                    MXRangedValue zero7 = MXRangedValue.ZERO7;
                    MXRangedValue zero14 = MXRangedValue.ZERO14;
                    if (value32 < 0) {
                        MXMessage message = MXMessageFactory.fromTemplate(0, template7, _channel, zero14, zero7);
                        message.setGate(dataroom);
                        message.setValue(MXRangedValue.new7bit(value0));
                        addResult(message);
                    } else {
                        MXMessage message = MXMessageFactory.fromTemplate(0, template14, _channel, zero14, zero14);
                        message.setGate(dataroom);
                        message.setValue(MXRangedValue.new14bit(value32 | (value0 << 7)));
                        addResult(message);
                    }
                }
            }
            _caret = null;
        }
    }

    synchronized void addResult(MXMessage message) {
        if (message == null) {
            throw new IllegalCallerException("NULL");
        }
        if (message.isEmpty()) {
            throw new IllegalCallerException("EMPTY");
        }
        if (message.getStatus() == MXMidi.COMMAND_CH_CONTROLCHANGE) {
            //DATAENTRY設定中
        }
        _result.add(message);
    }

    synchronized RecordEntry getEntry(int cc) {
        int seek = (cc >= 32 && cc < 64) ? (cc - 32) : cc;
        if (listCC[seek] == null) {
            listCC[seek] = new RecordEntry(seek);
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
    MXMessage _lastMessage;
    LinkedList<MXMessage> _result = new LinkedList<>();
}
