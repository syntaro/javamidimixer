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
package jp.synthtarou.midimixer.libs.midi.driver;

import jp.synthtarou.midimixer.libs.midi.sysex.SplittableSysexMessage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;

/**
 * Java標準MIDI用のドライバ
 *
 * @author Syntarou YOSHIDA
 */
public class MXDriver_Java implements MXDriver {
    public int getDriverUID() {
        return 10;
    }

    public static final MXDriver_Java _instance = new MXDriver_Java();

    public boolean isUsable() {
        return true;
    }

    public void StartLibrary() {
        listAllInput();
        listAllOutput();
    }

    ArrayList<MidiDevice> _listInput;
    ArrayList<MidiDevice> _listOutput;

    public synchronized void listAllInput() {
        if (_listInput != null) {
            return;
        }

        ArrayList<MidiDevice> newList = new ArrayList<MidiDevice>();
        MidiDevice.Info[] infoList = MidiSystem.getMidiDeviceInfo();

        for (int i = 0; i < infoList.length; i++) {
            MidiDevice device = null;
            try {
                device = MidiSystem.getMidiDevice(infoList[i]);
            } catch (MidiUnavailableException ex) {
                MXFileLogger.getLogger(MXDriver_Java.class).log(Level.WARNING, ex.getMessage(), ex);
                continue;
            }

            if (device.getMaxTransmitters() != 0) {
                if (device.getDeviceInfo() == null) {
                    //maybe everything hannpens
                    continue;
                }
                String name = device.getDeviceInfo().getName();
                try {
                    String charset2 = System.getProperty("file.encoding");
                    String name2 = new String(name.getBytes(charset2), charset2);
                    String charset3 = System.getProperty("sun.jnu.encoding");
                    String name3 = new String(name.getBytes(charset3), charset3);
                    if (!name.equals(name2) || !name.equals(name3)) {
                        StringBuilder out = new StringBuilder();
                        for (int x = 0; x < name.length(); ++x) {
                            int ch = name.charAt(x);
                            out.append(Integer.toHexString(ch));
                            out.append(",");
                        }

                        continue;
                    }
                } catch (UnsupportedEncodingException ex) {
                    MXFileLogger.getLogger(MXDriver_Java.class).log(Level.WARNING, ex.getMessage(), ex);
                }
                if (name.equals("Real Time Sequencer")) {
                    continue;
                }
                newList.add(device);
            }
        }

        _listInput = newList;
    }

    public synchronized void listAllOutput() {
        if (_listOutput != null) {
            return;
        }

        ArrayList<MidiDevice> newList = new ArrayList<MidiDevice>();
        MidiDevice.Info[] infoList = MidiSystem.getMidiDeviceInfo();

        for (int i = 0; i < infoList.length; i++) {
            MidiDevice device = null;
            try {
                device = MidiSystem.getMidiDevice(infoList[i]);
            } catch (MidiUnavailableException ex) {
                MXFileLogger.getLogger(MXDriver_Java.class).log(Level.WARNING, ex.getMessage(), ex);
                continue;
            }

            if (device.getMaxReceivers() != 0) {
                String name = device.getDeviceInfo().getName();
                if (name.equals("Real Time Sequencer")) {
                    continue;
                }
                newList.add(device);
            }
        }

        _listOutput = newList;
    }

    public int InputDevicesRoomSize() {
        listAllInput();
        return _listInput.size();
    }

    public String InputDeviceName(int x) {
        listAllInput();
        MidiDevice device = _listInput.get(x);
        return device.getDeviceInfo().getName();
    }

    public String InputDeviceId(int x) {
        listAllInput();
        return InputDeviceName(x);
    }

    public boolean InputDeviceIsOpen(int x) {
        listAllInput();
        return _listInput.get(x).isOpen();
    }

    class JavaInputReceiver implements Receiver {

        MXMIDIIn _input;

        public JavaInputReceiver(MXMIDIIn input) {
            _input = input;
        }

        @Override
        public void send(MidiMessage msg, long timestamp) {
            if (msg instanceof ShortMessage) {
                ShortMessage shortMsg = (ShortMessage) msg;
                int status = shortMsg.getStatus() & 0xff;
                int data1 = shortMsg.getData1() & 0xff;
                int data2 = shortMsg.getData2() & 0xff;
                if (status == MXMidiStatic.COMMAND_META_OR_RESET || status == MXMidiStatic.COMMAND_ACTIVESENSING) {
                    return;
                }

                int dword = (((status << 8) | data1) << 8) | data2;
                _input.receiveShortMessage(null, dword);
            } else {
                byte[] data = msg.getMessage();
                _input.receiveLongMessage(null, data);
            }
        }

        @Override
        public void close() {
            _input.close();
        }
    }

    public boolean InputDeviceOpen(int device, long timeout, MXMIDIIn input) {
        listAllInput();
        if (!_listInput.get(device).isOpen()) {
            try {
                _listInput.get(device).open();
                if (_listInput.get(device).isOpen()) {
                    _listInput.get(device).getTransmitter().setReceiver(new JavaInputReceiver(input));
                    return true;
                }
            } catch (MidiUnavailableException e) {
            }
        }
        return false;
    }

    public void InputDeviceClose(int x) {
        listAllInput();
        if (!_listInput.get(x).isOpen()) {
            return;
        }
        _listInput.get(x).close();
    }

    public int OutputDevicesRoomSize() {
        listAllOutput();
        return _listOutput.size();
    }

    public String OutputDeviceName(int x) {
        listAllOutput();
        return _listOutput.get(x).getDeviceInfo().getName();
    }

    public String OutputDeviceId(int x) {
        listAllOutput();
        return OutputDeviceName(x);
    }

    public boolean OutputDeviceIsOpen(int x) {
        listAllOutput();
        return _listOutput.get(x).isOpen();
    }

    public boolean OutputDeviceOpen(int device, long timeout) {
        listAllOutput();
        if (!_listOutput.get(device).isOpen()) {
            try {
                _listOutput.get(device).open();
                if (_listOutput.get(device).isOpen()) {
                    return true;
                }
            } catch (MidiUnavailableException ex) {
                MXFileLogger.getLogger(MXDriver_Java.class).warning(ex.getMessage());
                return false;
            }
            return false;
        }
        return true;
    }

    public void OutputDeviceClose(int x) {
        listAllOutput();
        if (!_listOutput.get(x).isOpen()) {
            return;
        }
        try {
            if (_listOutput.get(x).getDeviceInfo().getName().equals("Gervill")) {
                return;
            }
        } catch (RuntimeException ex) {
            MXFileLogger.getLogger(MXDriver_Java.class).log(Level.WARNING, ex.getMessage(), ex);
        }
        _listOutput.get(x).close();
    }

    public boolean OutputOneMessage(int x, OneMessage one) {
        listAllOutput();
        if (_listOutput.get(x).isOpen() == false) {
            return false;
        }
        
        if (one.isBinaryMessage()) {
            try {
                byte[] data = one.getBinary();
                int status = (data.length > 0) ? (data[0] & 0xff) : 0;
                switch (status) {
                    case 0xf0:
                    case 0xf7:
                        try {
                            SplittableSysexMessage msg = new SplittableSysexMessage(data);
                            _listOutput.get(x).getReceiver().send(msg, 0);

                            return true;
                        } catch (MidiUnavailableException ex) {
                            MXFileLogger.getLogger(MXDriver_Java.class).log(Level.WARNING, ex.getMessage(), ex);
                        } catch (InvalidMidiDataException ex) {
                            MXFileLogger.getLogger(MXDriver_Java.class).log(Level.WARNING, ex.getMessage(), ex);
                        } catch (RuntimeException ex) {
                            MXFileLogger.getLogger(MXDriver_Java.class).log(Level.WARNING, ex.getMessage(), ex);
                        }
                        break;

                    case 0xff:
                        break;
                    default:
                        if (data.length <= 3) {
                            int data1 = 0;
                            int data2 = 0;
                            if (data.length >= 2) {
                                data1 = data[1] & 0xff;
                            }
                            if (data.length >= 3) {
                                data1 = data[2] & 0xff;
                            }
                            if (_listOutput.get(x).isOpen()) {
                                try {
                                    ShortMessage msg3 = new ShortMessage(status, data1, data2);
                                    _listOutput.get(x).getReceiver().send(msg3, 0);
                                    return true;
                                } catch (MidiUnavailableException ex) {
                                    MXFileLogger.getLogger(MXDriver_Java.class).log(Level.WARNING, ex.getMessage(), ex);
                                } catch (InvalidMidiDataException ex) {
                                    MXFileLogger.getLogger(MXDriver_Java.class).log(Level.WARNING, ex.getMessage(), ex);
                                } catch (RuntimeException ex) {
                                    MXFileLogger.getLogger(MXDriver_Java.class).log(Level.WARNING, ex.getMessage(), ex);
                                }
                            }
                        }
                        break;
                }
            } catch (RuntimeException ex) {
                MXFileLogger.getLogger(MXDriver_Java.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        else {
            int status = one.getStatus();
            if (status == 0) {
                return false;
            }
            int data1 = one.getData1();
            int data2 = one.getData2();
            if (_listOutput.get(x).isOpen()) {
                try {
                    ShortMessage msg = new ShortMessage(status, data1, data2);
                    _listOutput.get(x).getReceiver().send(msg, 0);
                    return true;
                } catch (InvalidMidiDataException ex) {
                    String text = "Unknown Message: " + MXUtil.toHexFF(status) + "  " + MXUtil.toHexFF(data1) + " " + MXUtil.toHexFF(data2);
                    MXFileLogger.getLogger(MXDriver_Java.class).log(Level.WARNING, text, ex);
                } catch (MidiUnavailableException ex) {
                    MXFileLogger.getLogger(MXDriver_Java.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
        return false;
    }
}
