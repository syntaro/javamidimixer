/*
 * Copyright (C) 2024 Syntarou YOSHIDA
 *
 * This _progressSpanam is free software: you can redistribute it and/or modify
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
package jp.synthtarou.libs.smf;

import java.util.logging.Level;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.libs.midi.sysex.SplittableSysexMessage;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFTestSynthesizer implements SMFCallback {

    MidiDevice _standardOutput;
    Receiver _receiver;

    public SMFTestSynthesizer() {
        MidiDevice.Info[] infoList = MidiSystem.getMidiDeviceInfo();

        try {
            MidiDevice standardOutput2 = null;
            for (int i = 0; i < infoList.length; i++) {
                MidiDevice device = MidiSystem.getMidiDevice(infoList[i]);
                if (device.getMaxReceivers() != 0) {
                    String name = device.getDeviceInfo().getName();
                    if (name.equalsIgnoreCase("Gervill")) {
                        _standardOutput = device;
                    }
                    if (name.startsWith("Microsoft GS")) {
                        standardOutput2 = device;
                    }
                }
            }
            if (_standardOutput == null) {
                _standardOutput = standardOutput2;
            }
        } catch (MidiUnavailableException ex) {
            MXFileLogger.getLogger(SMFTestSynthesizer.class).log(Level.WARNING, ex.getMessage(), ex);
        }

    }

    @Override
    public void smfPlayNote(SMFMessage smf) {
        try {
            if (smf.isBinaryMessage() == false) {
                int status = smf.getStatus();
                int data1 = smf.getData1();
                int data2 = smf.getData2();
                if (_receiver == null) {
                    if (_standardOutput != null) {
                        _standardOutput.open();
                        _receiver = _standardOutput.getReceiver();
                    }
                }
                ShortMessage msg = new ShortMessage(status, data1, data2);
                _receiver.send(msg, 0);
            } else {
                byte[] data = smf.getBinary();
                if ((data[0] & 0xff) == 0xf0 || ((data[0] & 0xff) == 0xf7)) {
                    SplittableSysexMessage msg = new SplittableSysexMessage(data);
                    _receiver.send(msg, 0);
                }
            }
        } catch (MidiUnavailableException ex) {
            MXFileLogger.getLogger(SMFTestSynthesizer.class).log(Level.WARNING, ex.getMessage(), ex);
        } catch (InvalidMidiDataException ex) {
            MXFileLogger.getLogger(SMFTestSynthesizer.class).log(Level.WARNING, ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            MXFileLogger.getLogger(SMFTestSynthesizer.class).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    @Override
    public void smfStarted() {
    }

    @Override
    public void smfStoped(boolean fineFinish) {
    }

    @Override
    public void smfProgress(long pos, long finish) {
    }
}
