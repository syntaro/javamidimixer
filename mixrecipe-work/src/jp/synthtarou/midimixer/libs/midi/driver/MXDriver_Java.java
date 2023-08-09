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

import java.util.ArrayList;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import jp.synthtarou.midimixer.MXThreadList;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXDriver_Java implements MXDriver {
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
            }catch(MidiUnavailableException e) {
                e.printStackTrace();
                continue;
            }

            if (device.getMaxTransmitters() != 0) {
                try {
                    String name = device.getDeviceInfo().getName();
                    try {
                        String charset2 = System.getProperty("file.encoding");
                        String name2 = new String(name.getBytes(charset2), charset2);
                        String charset3 = System.getProperty("sun.jnu.encoding");
                        String name3 = new String(name.getBytes(charset3), charset3);
                        if (!name.equals(name2) || !name.equals(name3)) {
                            StringBuffer out = new StringBuffer();
                            for (int x = 0; x < name.length(); ++ x) {
                                int ch = name.charAt(x);
                                out.append(Integer.toHexString(ch));
                                out.append(",");
                            }

                            System.out.println(name + " = " + name2 + " = " + name3);
                            continue;
                        }else {
                            System.out.println(name + " = safe for " + charset2 +" ," + charset3);
                        }
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                    if (name.equals("Real Time Sequencer")) {
                        continue;
                    }
                    newList.add(device);
                } catch (Exception e) {
                }
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
            }catch(MidiUnavailableException e) {
                e.printStackTrace();
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
        Thread _last;
        
        public JavaInputReceiver(MXMIDIIn input) {
            _input = input;
        }
        
        @Override
        public void send(MidiMessage msg, long timestamp) {
            if (_last != Thread.currentThread()) {
                _last = Thread.currentThread();
                MXThreadList.attachIfNeed("MXDriver_java", _last);
            }
            if (msg instanceof ShortMessage) {
                ShortMessage shortMsg = (ShortMessage)msg;
                int status = shortMsg.getStatus() & 0xff;
                int data1 = shortMsg.getData1() & 0xff;
                int data2 = shortMsg.getData2() & 0xff;
                if (status == MXMidi.STATUS_RESET || status == MXMidi.STATUS_ACTIVESENSING) {
                    return;
                }
                
                int dword = (((status << 8) | data1) << 8) | data2;
                _input.receiveShortMessage(dword);
            }else {
                byte[] data = msg.getMessage();
                _input.receiveLongMessage(data);
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
            }catch(MidiUnavailableException e) {
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
            }catch(MidiUnavailableException e) {
               e.printStackTrace();
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
        _listOutput.get(x).close();
    }
    
    public boolean OutputShortMessage(int x, int message) {
        listAllOutput();

        int status = ((message >> 8) >> 8) & 0xff;
        int data1 = (message >> 8) & 0xff;
        int data2 = (message) & 0xff;
        if (_listOutput.get(x).isOpen()) {
            try {
                ShortMessage msg = new ShortMessage(status, data1, data2);
                _listOutput.get(x).getReceiver().send(msg, 0);
                return true;
            }catch(Throwable e) {
                System.out.println("Unknown Message: " + MXUtil.toHexFF(status)  + "  " + MXUtil.toHexFF(data1) + " " + MXUtil.toHexFF(data2) );
                e.printStackTrace();
            }
        }
        return false;
    }

    static MidiDevice gervill = null;
    static boolean _seekedGervill = false;
    static final byte[] gmsystem = { (byte)0xf0, (byte)0x7e, (byte)0x7f, (byte)0x09, (byte)0x01, (byte)0xf7 };

    public boolean OutputLongMessage(int x, byte[] data) {
        listAllOutput();

        if (data == null || data.length == 0) {
            return true;
        }
        
        try {
            int status = data[0] & 0xff;
            switch(status) {
                case 0xf0:
                case 0xf7:
                    try {
                        if (_seekedGervill == false) {
                            _seekedGervill = true;
                            for (MidiDevice device : _listOutput) {
                                if (device.getDeviceInfo().getName().equalsIgnoreCase("Gervill")) {
                                    gervill = device;
                                }
                            }
                        }
                        if (_seekedGervill && _listOutput.get(x) == gervill) {
                            int count = gmsystem.length;
                            if (gmsystem.length == data.length) {
                                for (int i = 0; i < gmsystem.length; ++ i) {
                                    if (data[i] == gmsystem[i]) {
                                        count --;
                                    }
                                }
                            }
                            if (count == 0) {
                                return false;
                            }
                            
                            //JavaSynth には、GM Resetを送らない
                        }
                        SplittableSysexMessage msg = new SplittableSysexMessage(data);
                        System.out.println("SysEx: " + MXUtil.dumpHexFF(msg.getMessage()));
                        _listOutput.get(x).getReceiver().send(msg, 0);

                        return true;
                    }catch(Throwable e) {
                        e.printStackTrace();
                    }
                    break;
                case 0xff:
                    break;
                default:
                    if (data.length <= 3) {
                        int data1 = (data.length >= 2) ? data[1] : 0;
                        int data2 = (data.length >= 3) ? data[2] : 0;
                        if (_listOutput.get(x).isOpen()) {
                            try {
                                ShortMessage msg3 = new ShortMessage(status & 0xff, data1 & 0xff, data2 & 0xff);
                                _listOutput.get(x).getReceiver().send(msg3, 0);
                                return true;
                            }catch(InvalidMidiDataException e) {
                            }catch(Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
            }
        }catch(Throwable e) {
            e.printStackTrace();
        }
        return false;
    }
}
