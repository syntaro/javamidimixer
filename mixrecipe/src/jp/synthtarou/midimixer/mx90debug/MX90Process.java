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

import javax.swing.JOptionPane;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.libs.midi.port.RecordEntry;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX90Process extends MXReceiver<MX90View> {

    MX90View _view;

    public MX90Process() {
        _view = new MX90View(this);
    }

    @Override
    public String getReceiverName() {
        return "(Debug)";
    }

    @Override
    public MX90View getReceiverView() {
        return _view;
    }

    @Override
    public void processMXMessage(MXMessage message) {
    }

    public void doAllTest() {
        MXFileLogger.getLogger(MX90Process.class).info("Starting All Test");
        
        MXFileLogger.getLogger(MX90Process.class).info("Testing Data Entry2");
        for (int msb = 0; msb < 128; msb += random(20)) {
            for (int lsb = 0; lsb < 128; lsb += random(20)) {
                int varMSB = random(127);
                int varLSB = random(127);
                String textRPN = "@RPN " + msb + " " + lsb + " " + varMSB + " " + varLSB;
                MXTemplate temp1 = new MXTemplate(textRPN);
                MXMessage message1 = MXMessageFactory.fromTemplate(0, temp1, 0, null, null);

                new MXDebugSame(message1);

                String textNRPN = "@NRPN " + msb + " " + lsb + " " + varMSB + " " + varLSB;
                MXTemplate temp2 = new MXTemplate(textNRPN);
                MXMessage newMessage = MXMessageFactory.fromTemplate(0, temp2, 0, null, null);
                new MXDebugSame(newMessage);
                if (Thread.interrupted()) {
                    return;
                }

            }
        }
        MXFileLogger.getLogger(MX90Process.class).info("Testing Data Entry1");
        for (int msb = 0; msb < 128; msb += random(20)) {
            for (int lsb = 0; lsb < 128; lsb += random(20)) {
                int varMSB = random(127);
                int varLSB = random(127);
                new MXDebugDataEntry(true, msb, lsb, (varMSB << 7) | varLSB);
                new MXDebugDataEntry(false, msb, lsb, (varMSB << 7) | varLSB);
            }
        }

        MXFileLogger.getLogger(MX90Process.class).info("Testing Program Change");
        for (int ch = 0; ch < 16; ++ch) {
            int pg = random(128);
            MXMessage program = MXMessageFactory.fromProgramChange(0, ch, pg);
            new MXDebugSame(program);
            if (Thread.interrupted()) {
                return;
            }
        }

        MXFileLogger.getLogger(MX90Process.class).info("Testing Control Change");
        for (int ch = 0; ch < 16; ++ch) {
            int cc = random(128);
            int value = random(128);
            RecordEntry e = MXMIDIIn.DEBUGGER._preprocess._analyzer.getEntry(cc);
            MXMessage program = MXMessageFactory.fromControlChange(0, ch, cc, value);
            if (e != null && e.is14bitChoiced()) {
                continue;
                //program = MXMessageFactory.fromControlChange14(0, ch, cc, value, 20);
            }
            new MXDebugSame(program);
            if (Thread.interrupted()) {
                return;
            }
        }

        MXFileLogger.getLogger(MX90Process.class).info("Testing Volume");
        for (int ch = 0; ch < 16; ++ch) {
            int vol = random(128);
            MXMessage volume = MXMessageFactory.fromControlChange(0, ch, MXMidi.DATA1_CC_CHANNEL_VOLUME, vol);
            new MXDebugSame(volume);
            if (Thread.interrupted()) {
                return;
            }
        }
        MXFileLogger.getLogger(MX90Process.class).info("Testing Note 2");
        for (int i = 0; i < 127; ++i) {
            int port = 0;
            int channel = random(16);

            MXMessage noteOn = MXMessageFactory.fromNoteon(port, channel, i, 100);
            new MXDebugSame(noteOn);
            MXMessage noteOff = MXMessageFactory.fromNoteoff(port, channel, i);
            new MXDebugSame(noteOff);
            if (Thread.interrupted()) {
                return;
            }
        }
        MXFileLogger.getLogger(MX90Process.class).info("Testing Random Note");
        for (int x = 0; x < 50; ++x) {
            for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++port) {
                int channel = random(16);
                int note = random(50) + 50;

                MXMessage noteOn = MXMessageFactory.fromNoteon(port, channel, note, 100);
                new MXDebugSame(noteOn);
                MXMessage noteOff = MXMessageFactory.fromNoteoff(port, channel, note);
                new MXDebugSame(noteOff);
                if (Thread.interrupted()) {
                    return;
                }
            }
        }
        MXFileLogger.getLogger(MX90Process.class).info("Testing Sysex");
        for (int value = 0; value < 128; value += random(20)) {
            for (int gate = 0; gate < 128; gate += random(20)) {
                MXTemplate template3 = new MXTemplate("@SYSEX F0H #GL #VL F7H");
                MXTemplate template4 = new MXTemplate("@SYSEX F0H [ #GL #VL ] F7H");
                MXMessage message3 = MXMessageFactory.fromTemplate(0, template3, 10, MXRangedValue.new7bit(gate), MXRangedValue.new7bit(value));
                MXMessage message4 = MXMessageFactory.fromTemplate(0, template4, 10, MXRangedValue.new7bit(gate), MXRangedValue.new7bit(value));

                byte[] question3 = message3.getBinary();
                byte[] answer = {(byte) 0xf0, (byte) gate, (byte) value, (byte) 0xf7};
                byte[] question4 = message4.getBinary();

                if (checkSame(question3, answer) == false) {
                    MXFileLogger.getLogger(MX90Process.class).info("Sysex Error: " + MXUtil.dumpHex(question3) + " -> " + MXUtil.dumpHex(answer));
                }
                if (question4.length == 5 && question3.length == 4) {
                    for (int i = 0; i < 3; ++i) {
                        if (question3[i] != question4[i]) {
                            MXFileLogger.getLogger(MX90Process.class).info("checksum broken " + MXUtil.dumpHex(question3) + " != " + MXUtil.dumpHex(question4));
                        }
                    }
                    if (question3[3] != question4[4]) {
                        MXFileLogger.getLogger(MX90Process.class).info("checksum broken " + MXUtil.dumpHex(question3) + " != " + MXUtil.dumpHex(question4));
                    }
                } else {
                    MXFileLogger.getLogger(MX90Process.class).info("checksum not work");
                }
                new MXDebugSame(message3);
                new MXDebugSame(message4);
                if (Thread.interrupted()) {
                    return;
                }
            }
        }

        MXMain.getMain().getMasterkeyProcess().startDebug(null);
        MXFileLogger.getLogger(MX90Process.class).info("Finished All Tests");
        JOptionPane.showMessageDialog(_view, "Finished All Tests", "Done.", JOptionPane.OK_OPTION);
    }

    public boolean checkSame(byte[] data1, byte[] data2) {
        if (data1.length != data2.length) {
            return false;
        }
        for (int i = 0; i < data1.length; ++i) {
            if (data1[i] != data2[i]) {
                return false;
            }
        }
        return true;
    }

    public int random(int capacity) {
        return (int) (Math.random() * capacity);
    }
}
