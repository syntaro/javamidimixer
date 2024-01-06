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

import javax.swing.JPanel;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX90Process extends MXReceiver {

    MX90View _view;

    public MX90Process() {
        _view = new MX90View(this);
    }

    @Override
    public String getReceiverName() {
        return "(Debug)";
    }

    @Override
    public JPanel getReceiverView() {
        return _view;
    }

    @Override
    public void processMXMessage(MXMessage message) {
    }

    public void doAllTest(ResultModel result) {
        result.println("Starting All Test");
        //do sometihng
        result.println("Testing Program Change");
        for (int ch = 0; ch < 16; ++ch) {
            int pg = random(128);
            MXMessage program = MXMessageFactory.fromShortMessage(0, MXMidi.COMMAND_CH_PROGRAMCHANGE + ch, pg, 0);
            new MXDebugSame(result, program);
        }
        result.println("Testing Note Grissand");
        for (int i = 0; i < 127; ++i) {
            int port = 0;
            int channel = random(16);

            MXMessage noteOn = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_NOTEON + channel, i, 100);
            new MXDebugSame(result, noteOn);
            MXMessage noteOff = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_NOTEOFF + channel, i, 0);
            new MXDebugSame(result, noteOff);
        }
        result.println("Testing Random Note");
        for (int x = 0; x < 50; ++x) {
            for (int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++port) {
                int channel = random(16);
                int note = random(50) + 50;

                MXMessage noteOn = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_NOTEON + channel, note, 100);
                new MXDebugSame(result, noteOn);
                MXMessage noteOff = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_NOTEOFF + channel, note, 0);
                new MXDebugSame(result, noteOff);
            }
        }
        result.println("Testing Data Entry1");
        for (int msb =0; msb < 128; msb += 12) {
            for (int lsb =0; lsb < 128; lsb += 12) {
                int varMSB = random(127);
                int varLSB = random(127);
                new MXDebugDataEntry(result, true, msb, lsb, (varMSB << 7) | varLSB);
                new MXDebugDataEntry(result, false, msb, lsb, (varMSB << 7) | varLSB);
            }
        }
        result.println("Testing Data Entry2");
        for (int msb =0; msb < 128; msb += 3) {
            for (int lsb =0; lsb < 128; lsb += 3) {
                int varMSB = random(127);
                int varLSB = random(127);
                String textRPN = "@RPN " + msb + " "  +lsb + " " + varMSB + " " + varLSB;
                MXTemplate temp1 = new MXTemplate(textRPN);
                MXMessage message1 = MXMessageFactory.fromTemplate(0, temp1, 0, null, null);
                new MXDebugSame(result, message1);
                
                String textNRPN = "@RPN " + msb + " "  +lsb + " " + varMSB + " " + varLSB;
                MXTemplate temp2 = new MXTemplate(textNRPN);
                MXMessage message2 = MXMessageFactory.fromTemplate(0, temp2, 0, null, null);
                new MXDebugSame(result, message2);
            }
        }
        result.println("Finished All Test");
    }

    public int random(int capacity) {
        return (int) (Math.random() * capacity);
    }
}
