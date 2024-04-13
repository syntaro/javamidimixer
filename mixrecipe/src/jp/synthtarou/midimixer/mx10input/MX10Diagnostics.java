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
package jp.synthtarou.midimixer.mx10input;

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
public class MX10Diagnostics {

    public static void main(String[] args) {
        MX10Preprocessor parent = new MX10Preprocessor(0, null);
        MX10Diagnostics diag = new MX10Diagnostics(parent, 0);

        diag.record(0 + 6, 10);
        diag.record(32 + 6, 12);
        diag.record(0 + 6, 10);
        diag.record(32 + 6, 12);
        diag.record(0 + 6, 10);
        diag.record(32 + 6, 12);
        diag.record(0 + 6, 10);
        diag.record(32 + 6, 12);

        for (int i = 0; i < 128; ++i) {
            for (int j = 0; j < 128; ++j) {
                diag.record(i, j);
            }
        }

        MXMessage message;
        for (int i = 0; i < 128; ++i) {
            for (int j = 0; j < 128; ++j) {
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_NOTEON, j, j);
                diag.record(message);
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_NOTEOFF, j, j);
                diag.record(message);
            }
        }
        while (true) {
            MXMessage e = diag.popResult();
            if (e == null) {
                break;
            }
            System.out.println("result " + e.toStringMessageInfo(1));
        }
        System.out.println("before Flush");

        diag.flushPool();

        while (true) {
            MXMessage e = diag.popResult();
            System.out.println("result " + e);
            if (e == null) {
                break;
            }
        }

        for (int i = 0; i < 128; ++i) {
            for (int j = 0; j < 128; ++j) {
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_DATAENTRY, i);
                diag.record(message);
                message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_DATAENTRY2, j);
                diag.record(message);
            }
        }

        while (true) {
            MXMessage e = diag.popResult();
            System.out.println("result " + e);
            if (e == null) {
                break;
            }
        }
        System.out.println("7bit start");
        for (int i = 0; i < 128; ++i) {
            message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.DATA1_CC_DATAENTRY, i);
            diag.record(message);
        }

        while (true) {
            MXMessage e = diag.popResult();
            System.out.println("result " + e);
            if (e == null) {
                break;
            }
        }

    }

    int _channel;
    MX10Preprocessor _parent;
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

    public MX10Diagnostics(MX10Preprocessor parent, int channel) {
        _channel = channel;
        _parent = parent;
    }

    public void record(int cc, int value) {
        MXMessage message = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_CONTROLCHANGE, cc, value);
        record(message);
    }

    public void record(MXMessage message) {
        if (message.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
            int cc = message.getCompiled(1);
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
    }

    MXTemplate cc7bit = new MXTemplate(new int[]{MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.CCXML_GL, MXMidi.CCXML_VL});
    MXTemplate cc14bit = new MXTemplate(new int[]{MXMidi.COMMAND_CH_CONTROLCHANGE, MXMidi.CCXML_GL, MXMidi.CCXML_VH, MXMidi.CCXML_VL});

    MXTemplate data7bit = new MXTemplate(new int[]{MXMidi.COMMAND2_CH_NRPN, MXMidi.CCXML_GH, MXMidi.CCXML_GL, MXMidi.CCXML_VL});
    MXTemplate data14bit = new MXTemplate(new int[]{MXMidi.COMMAND2_CH_NRPN, MXMidi.CCXML_GH, MXMidi.CCXML_GL, MXMidi.CCXML_VH, MXMidi.CCXML_VL});

    MXRangedValue dataRoom = new MXRangedValue(128 * 30 + 20, 0, 128 * 128 - 1);

    public synchronized void flushPool() {
        if (_caret != null) {
            int value0 = _caret._pooling0;
            int value32 = _caret._pooling32;
            if (value0 < 0) {
                return;
            }
            if (_caret._cc == MXMidi.DATA1_CC_DATAENTRY) {
                if (value32 < 0) {
                    //VL only
                    MXMessage message = MXMessageFactory.fromTemplate(_parent._port, data7bit, _channel, dataRoom, MXRangedValue.ZERO7);
                    message.setGate(_caret._cc);
                    message.setValue(MXRangedValue.new7bit(_caret._pooling0));
                    addResult(message);
                } else {
                    MXMessage message = MXMessageFactory.fromTemplate(_parent._port, data14bit, _channel, dataRoom, new MXRangedValue(4000, 0, 128 * 128 - 1));
                    message.setGate(_caret._cc);
                    message.setValue(MXRangedValue.new14bit((_caret._pooling32 << 7) | _caret._pooling0));
                    addResult(message);
                }
            } else {
                if (value32 < 0) {
                    //VL only
                    MXMessage message = MXMessageFactory.fromTemplate(_parent._port, cc7bit, _channel, MXRangedValue.ZERO7, MXRangedValue.ZERO7);
                    message.setGate(_caret._cc);
                    message.setValue(MXRangedValue.new7bit(_caret._pooling0));
                    addResult(message);
                } else {
                    MXMessage message = MXMessageFactory.fromTemplate(_parent._port, cc14bit, _channel, MXRangedValue.ZERO7, MXRangedValue.ZERO7);
                    message.setGate(_caret._cc);
                    message.setValue(MXRangedValue.new14bit((_caret._pooling32 << 7) | _caret._pooling0));
                    addResult(message);
                }
            }
            _caret = null;
        }
    }

    synchronized void addResult(MXMessage message) {
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
