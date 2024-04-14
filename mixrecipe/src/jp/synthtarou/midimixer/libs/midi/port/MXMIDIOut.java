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
package jp.synthtarou.midimixer.libs.midi.port;

import jp.synthtarou.midimixer.libs.midi.visitant.MXVisitant16;
import jp.synthtarou.midimixer.libs.midi.visitant.MXVisitant;
import java.util.logging.Level;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.console.MXMidiConsoleElement;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_Java;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_UWP;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_VSTi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMIDIOut {

    private MXDriver _driver;
    private int _driverOrder;
    private String _name;
    boolean[] _assigned;
    int _assignCount;
    private MXVisitant16 _visitantOut16 = new MXVisitant16();

    private MXNoteOffWatcher _myNoteOff = new MXNoteOffWatcher();

    public MXDriver getDriver() {
        return _driver;
    }

    public int getDriverOrder() {
        return _driverOrder;
    }

    public boolean isPortAssigned(int port) {
        if (port < 0 || port >= MXConfiguration.TOTAL_PORT_COUNT) {
            return false;
        }
        return _assigned[port];
    }

    public int getPortAssignCount() {
        return _assignCount;
    }

    public String getPortAssignedAsText() {
        StringBuffer assigned = new StringBuffer();
        for (int p = 0; p < MXConfiguration.TOTAL_PORT_COUNT; ++p) {
            if (isPortAssigned(p)) {
                if (assigned.length() > 0) {
                    assigned.append(",");
                }
                assigned.append((char) ('A' + p));
            }
        }
        return assigned.toString();
    }

    public void setPortAssigned(int port, boolean flag) {
        if (_assigned[port] != flag) {
            if (!flag) {
                _myNoteOff.allNoteFromPort(null, port);
            }
            _assigned[port] = flag;
            int x = 0;
            for (int i = 0; i < _assigned.length; ++i) {
                if (_assigned[i]) {
                    x++;
                }
            }
            _assignCount = x;
            MXMIDIOutManager.getManager().clearMIDIOutCache();
        }
    }

    public void resetPortAssigned() {
        for (int i = 0; i < MXConfiguration.TOTAL_PORT_COUNT; ++i) {
            setPortAssigned(i, false);
        }
    }

    public boolean isOpen() {
        return _driver.OutputDeviceIsOpen(_driverOrder);
    }

    protected MXMIDIOut(MXDriver driver, int driverOrder) {
        _assigned = new boolean[16];
        _driver = driver;
        _driverOrder = driverOrder;
        if (driver == null) {
            _name = "(NULL)";
        } else {
            _name = driver.OutputDeviceName(driverOrder);
        }
    }

    public String getName() {
        return _name;
    }

    public int hashCode() {
        return _name.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        MXMIDIOut out = (MXMIDIOut) o;
        if (out._name == this._name) {
            return true;
        }
        return false;
    }

    public String toString() {
        return _name;
    }

    long startTime = System.currentTimeMillis();

    public long getTimestamp() {
        try {
            return System.currentTimeMillis() - startTime;
        } catch (Exception e) {
            return -1;
        }
    }

    public void processMidiOut(MXMessage message) {
        try {
            processMidiOutInternal(message);
        } catch (Throwable ex) {
            MXFileLogger.getLogger(MXMIDIOut.class).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    MXMessage[] retBuf = new MXMessage[4];

    private void processMidiOutInternal(MXMessage message) {
        synchronized (MXTiming.mutex) {
            if (!_driver.OutputDeviceIsOpen(_driverOrder)) {
                return;
            }

            MXVisitant portVisitant = _visitantOut16.get(message.getChannel());

            long timeStamp = getTimestamp();
            long recTime = 0;

            if (message.isChannelMessage2() == false) {
                finalOut(message);
            } else {
                MXVisitant msgVisitant = message.getVisitant();
                int status = message.getCompiled(0);
                int channel = message.getChannel();
                int gate = message.getGate()._value;
                int command = status;
                if (status >= 0x80 && status <= 0xef) {
                    command = status & 0xffff0;
                }
                if (msgVisitant == null) {
                } else {
                    if (command != MXMidi.COMMAND_CH_PROGRAMCHANGE) {
                        if (msgVisitant.isHavingProgram()) {
                            int must = msgVisitant.getProgram();
                            int old = portVisitant.getProgram();
                            if (portVisitant.isHavingProgram() == false || portVisitant.getProgram() != must) {
                                portVisitant.setProgram(must);
                                MXMessage newMessage = MXMessageFactory.fromProgramChange(message.getPort(), channel, must);
                                newMessage._owner = message;
                                newMessage.setVisitant(portVisitant.getSnapShot());
                                processMidiOutInternal(newMessage);
                                System.out.println("need Fix ProgramChange"+ " @" + channel + " from " + old + " to " + must);
                            }
                        }
                    }
                    if (command != MXMidi.COMMAND_CH_CONTROLCHANGE || (gate != MXMidi.DATA1_CC_BANKSELECT && gate != MXMidi.DATA1_CC_BANKSELECT + 32)) {
                        //0と32が連続でくる間は、この処理は実行されないので、半端を送ることもない
                        if (msgVisitant.isHavingBank()) {
                            int mustLSB = msgVisitant.getBankLSB();
                            int mustMSB = msgVisitant.getBankMSB();
                            if (portVisitant.isHavingBank() == false || portVisitant.getBankMSB() != mustMSB || portVisitant.getBankLSB() != mustLSB) {
                                portVisitant.setBankLSB(msgVisitant.getBankMSB());
                                portVisitant.setBankLSB(msgVisitant.getBankLSB());

                                MXMessage newMessage1 = MXMessageFactory.fromControlChange(message.getPort(), channel, MXMidi.DATA1_CC_BANKSELECT, mustMSB);
                                MXMessage newMessage2 = MXMessageFactory.fromControlChange(message.getPort(), channel, MXMidi.DATA1_CC_BANKSELECT + 32, mustLSB);
                                newMessage1.setVisitant(portVisitant.getSnapShot());
                                newMessage2.setVisitant(portVisitant.getSnapShot());
                                newMessage1._owner = message;
                                newMessage2._owner = message;

                                processMidiOutInternal(newMessage1);
                                processMidiOutInternal(newMessage2);
                                System.out.println("need Fix BankSelect"+ " @" + channel);
                            }
                        }
                    }

                    if (command != MXMidi.COMMAND_CH_CONTROLCHANGE) {
                        if (portVisitant._currentCCAge != msgVisitant._currentCCAge) {
                            int did = 0;
                            for (int code = 0; code < 128; ++code) {
                                if (msgVisitant.isCCSet(code)) {
                                    int must = msgVisitant.getCCValue(code);
                                    int old = portVisitant.getCCValue(code);

                                    if (portVisitant.isCCSet(gate) == false || must != old) {
                                        did++;
                                        portVisitant.setCCValue(code, must);

                                        MXMessage newMessage = MXMessageFactory.fromControlChange(message.getPort(), channel, code, must);

                                        newMessage._owner = message;
                                        newMessage.setVisitant(portVisitant.getSnapShot());

                                        processMidiOutInternal(newMessage);
                                        System.out.println("need Fix CC " + code+ " @" + channel+ " from " + old + " to " + must);
                                    }
                                }
                            }
                            portVisitant._currentCCAge = msgVisitant._currentCCAge;
                        }
                    }
                }

                MXMessage ret = portVisitant.catchTheVisitant(message);
                if (ret != null) {
                    finalOut(ret);
                }
            }
        }
    }

    public void finalOut(MXMessage message) {
        synchronized (MXTiming.mutex) {
            if (message == null) {
                return;
            }

            if (message.isCommand(MXMidi.COMMAND_CH_NOTEON)) {
                _myNoteOff.setHandler(message, message, new MXNoteOffWatcher.Handler() {
                    @Override
                    public void onNoteOffEvent(MXMessage target) {
                        int dword = target.getAsDword(0);
                        _driver.OutputShortMessage(_driverOrder, dword);
                        MXMain.addOutsideOutput(new MXMidiConsoleElement(target));
                    }
                });
                int dword = message.getAsDword(0);
                _driver.OutputShortMessage(_driverOrder, dword);
                MXMain.addOutsideOutput(new MXMidiConsoleElement(message));
                return;
            } else if (message.isCommand(MXMidi.COMMAND_CH_NOTEOFF)) {
                if (_myNoteOff.raiseHandler(message)) {
                    return;
                }
            } else if (message.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)
                    && message.getCompiled(1) == MXMidi.DATA1_CC_ALLNOTEOFF) {
                allNoteOff(message);
            }

            int col = message.getDwordCount();
            if (col == 0) {
                byte[] data = message.getBinary();
                _driver.OutputLongMessage(_driverOrder, data);
                MXMain.addOutsideOutput(new MXMidiConsoleElement(message));
            } else {
                for (int j = 0; j < col; ++j) {
                    int dword = message.getAsDword(j);
                    if (dword == 0) {

                    }else {
                        _driver.OutputShortMessage(_driverOrder, dword);
                        int status = (dword >> 16) & 0xff;
                        int data1 = (dword >> 8) & 0xff;
                        int data2 = (dword) & 0xff;
                        MXMessage newMessage = MXMessageFactory.fromShortMessage(message.getPort(), status, data1, data2);
                        newMessage._owner = message;
                        MXMain.addOutsideOutput(new MXMidiConsoleElement(newMessage));
                    }
                }
            }
        }
    }

    public void allNoteOff(MXMessage owner) {
        synchronized (MXTiming.mutex) {
            _myNoteOff.allNoteOff(owner);
        }
    }

    public void allNoteOffFromPort(MXMessage owner, int port) {
        synchronized (MXTiming.mutex) {
            _myNoteOff.allNoteFromPort(owner, port);
        }
    }

    public boolean openOutput(long timeout) {
        MXMIDIOutManager manager = MXMIDIOutManager.getManager();
        synchronized (MXTiming.mutex) {
            manager.clearMIDIOutCache();
            _visitantOut16 = new MXVisitant16();
            if (_driver.OutputDeviceIsOpen(_driverOrder) == false) {
                _driver.OutputDeviceOpen(_driverOrder, timeout);
            }
            return _driver.OutputDeviceIsOpen(_driverOrder);
        }
    }

    public void close() {
        MXMIDIOutManager manager = MXMIDIOutManager.getManager();
        synchronized (MXTiming.mutex) {
            if (isOpen()) {
                allNoteOff(null);
                if (_name.equals("Gervill")) {
                    manager.onClose(this);
                    _driver.OutputDeviceClose(_driverOrder);
                } else {
                    manager.onClose(this);
                    _driver.OutputDeviceClose(_driverOrder);
                }
            }
        }
    }

    public boolean isDriverTypeVSTi() {
        if (_driver instanceof MXDriver_VSTi) {
            return true;
        }
        return false;
    }

    public int getVStiDriverNumber() {
        if (_driver instanceof MXDriver_VSTi) {
            return _driverOrder;
        }
        return -1;
    }

    public boolean isDriverTypeJava() {
        if (_driver instanceof MXDriver_Java) {
            return true;
        }
        return false;
    }

    public boolean isDriverTypeUWP() {
        if (_driver instanceof MXDriver_UWP) {
            return true;
        }
        return false;
    }
}
