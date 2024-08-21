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

import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JPanel;
import jp.synthtarou.libs.MXQueue;
import jp.synthtarou.libs.MXSafeThread;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_UWP;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMIDIIn implements Comparable<MXMIDIIn>{
    public static final MXMIDIInForPlayer INTERNAL_PLAYER = new MXMIDIInForPlayer();
    public static final MXMIDIInForPlayer DEBUGGER = new MXMIDIInForPlayer();
    
    static {
        DEBUGGER.setPortAssigned(0, true);
    }

    String _name;
    MXDriver _driver;
    int _orderInDriver;
    private final ArrayList<MXMidiFilter> _filter;

    public MXPreprocess _preprocess;

    public MXDriver getDriver() {
        return _driver;
    }

    public int getOrderInDriver() {
        return _orderInDriver;
    }
    
    public synchronized  MXMidiFilter getFilter(int port) {
        while(_filter.size() <= port) {
            _filter.add(new MXMidiFilter());
        }
        return _filter.get(port);
    }

    public void close() {
        MXMIDIInManager manager = MXMIDIInManager.getManager();
        if (isOpen()) {
            allNoteOff(null);
            manager.onClose(this);
            _driver.InputDeviceClose(_orderInDriver);
        }
    }

    private MXNoteOffWatcher _myNoteOff = new MXNoteOffWatcher();

    private boolean[] _assigned;
    private int _assignCount = 0;
    private boolean[] _toMaster = new boolean[16];

    public boolean isOpen() {
        if (this instanceof MXMIDIInForPlayer) {
            return true;
        }
        if (_driver == null) {
            return false;
        }
        return _driver.InputDeviceIsOpen(_orderInDriver);
    }

    public MXMIDIIn(MXDriver driver, int driverOrder) {
        _name = driver.InputDeviceName(driverOrder);
        _assigned = new boolean[MXConfiguration.TOTAL_PORT_COUNT];
        _driver = driver;
        _orderInDriver = driverOrder;
        _filter = new ArrayList<>();
        if (driver instanceof MXDriver_UWP) {
            MXDriver_UWP._instance.addInputCatalog(this);
        }
        _preprocess = new MXPreprocess(this);
        _preprocess.setNextReceiver(new MXReceiver() {
            @Override
            public String getReceiverName() {
                return "TempIn";
            }

            @Override
            public JPanel getReceiverView() {
                return null;
            }

            @Override
            public void processMXMessage(MXMessage message) {
                if (!getFilter(message.getPort()).isOK(message)) {
                    return;
                }
                for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++port) {
                    if (isPortAssigned(port)) {
                        MXMessage ported = MXMessageFactory.fromClone(message);
                        message._owner = MXMessage.getRealOwner(message);
                        message.setPort(port);
                        messageToReceiverThreaded(message, MXMain.getMain().getInputProcess());
                    }
                }
            }
        });
    }

    public boolean isPortAssigned(int port) {
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
                _myNoteOff.allNoteOffToPort(null, port);
            }
            _assigned[port] = flag;
            int x = 0;
            for (int i = 0; i < _assigned.length; ++i) {
                if (_assigned[i]) {
                    x++;
                }
            }
            _assignCount = x;
            MXMIDIInManager.getManager().clearMIDIInCache();;
        }
    }

    public void resetPortAssigned() {
        for (int i = 0; i < MXConfiguration.TOTAL_PORT_COUNT; ++i) {
            setPortAssigned(i, false);
        }
    }

    public boolean isToMaster(int channel) {
        return _toMaster[channel];
    }

    public void setToMaster(int channel, boolean toMaster) {
        _toMaster[channel] = toMaster;
    }

    public String getMasterList() {
        StringBuffer str = new StringBuffer();
        for (int ch = 0; ch < 16; ++ch) {
            if (isToMaster(ch)) {
                if (str.length() == 0) {
                    str.append(Integer.toString(ch));
                } else {
                    str.append(",");
                    str.append(Integer.toString(ch));
                }
            }
        }
        return str.toString();
    }

    public void setMasterList(String text) {
        if (text == null) {
            return;
        }
        ArrayList<String> list = new ArrayList();
        MXUtil.split(text, list, ',');
        _toMaster = new boolean[16];
        for (String x : list) {
            int ch = MXUtil.numberFromText(x);
            if (ch >= 0) {
                _toMaster[ch] = true;
            }
        }
    }

    /**
     *
     * @return
     */
    public String getName() {
        return _name;
    }

    /**
     *
     * @return
     */
    public String toString() {
        return _name;
    }

    public void allNoteOff(MXMessage parent) {
        _myNoteOff.allNoteOff(parent);
        for (int ch = 0; ch < 16; ++ch) {
            int status = MXMidi.COMMAND_CH_CONTROLCHANGE | ch;
            int data1 = MXMidi.DATA1_CC_ALLNOTEOFF;
            receiveShortMessage(parent, (status << 16) | (data1 << 8));
            data1 = MXMidi.DATA1_CC_ALLSOUNDOFF;
            receiveShortMessage(parent, (status << 16) | (data1 << 8));
        }
    }

    public void allNoteOffToPort(MXMessage parent, int target) {
        _myNoteOff.allNoteOffToPort(parent, target);
    }

    public boolean openInput(long timeout) {
        MXFileLogger.getLogger(MXMIDIIn.class).info("timeout open input " + timeout);
        MXMIDIInManager manager = MXMIDIInManager.getManager();
        if (_driver == null) {
            return false;
        }
        if (isOpen()) {
            return true;
        }
        MXFileLogger.getLogger(MXMIDIIn.class).info("timeout open input go " + _driver.getDriverUID());
        return _driver.InputDeviceOpen(_orderInDriver, timeout, this);
    }

    public int hashCode() {
        return _name.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        MXMIDIIn in = (MXMIDIIn) o;
        if (in._name == this._name) {
            return true;
        }
        return false;
    }

    public String textForMasterChannel() {
        StringBuffer masterMark = new StringBuffer();
        for (int ch = 0; ch < 16; ++ch) {
            if (isToMaster(ch)) {
                if (masterMark.length() != 0) {
                    masterMark.append(", ");
                }
                masterMark.append(Integer.valueOf(ch + 1));
            }
        }
        return masterMark.toString();
    }

    public int parseMasteredText(String text) {
        if (text == null) {
            return 0;
        }
        String[] array = text.split("[ ,]+");
        int hit = 0;

        for (int i = 0; i < array.length; ++i) {
            String parts = array[i];
            if (parts.length() >= 1) {
                int ch1 = parts.charAt(0) - '0';
                if (ch1 < 0 || ch1 > 9) {
                    continue;
                }
                if (parts.length() >= 2) {
                    int ch2 = parts.charAt(2) - '0';
                    if (ch2 < 0 || ch2 > 9) {
                        continue;
                    }
                    ch1 = ch1 * 10 + ch2;
                }
                if (ch1 >= 1 && ch1 <= 16) {
                    setToMaster(ch1 - 1, true);
                }
                hit++;
            }
        }
        return hit;
    }

    public final void receiveShortMessage(MXMessage owner, int dword) {
        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = (dword) & 0xff;

        int command = status & 0xf0;
        int channel = status & 0x0f;

        if (command == MXMidi.COMMAND_CH_NOTEON) {
            if (data2 == 0) {
                command = MXMidi.COMMAND_CH_NOTEOFF;
                status = command | channel;
                dword = (status << 16) | (data1 << 8) | data2;
            }
        }
        if (command == MXMidi.COMMAND_CH_NOTEOFF) {
            if (_myNoteOff.raiseHandler(null, 0, channel, data1)) {
                return;
            }
        } else if (command == MXMidi.COMMAND_CH_CONTROLCHANGE && data1 == MXMidi.DATA1_CC_ALLNOTEOFF) {
            _myNoteOff.allNoteOff(null);
        } else if (command == MXMidi.COMMAND_CH_CONTROLCHANGE && data1 == MXMidi.DATA1_CC_ALLSOUNDOFF) {
            _myNoteOff.allNoteOff(null);
        }

        MXMessage message = MXMessageFactory.fromShortMessage(0, status, data1, data2);
        message._owner = MXMessage.getRealOwner(owner);
        MXMain.addOutsideInput(message);
        _preprocess.processMXMessage(message);
    }

    public void receiveExMessage(MXMessage owner, MXMessage message) {
        message._owner = MXMessage.getRealOwner(owner);
        MXMain.addOutsideInput(message);
        _preprocess.processMXMessage(message);
    }

    public void receiveLongMessage(MXMessage owner, byte[] data) {
        MXMessage message = MXMessageFactory.fromBinary(0, data);
        message._owner = MXMessage.getRealOwner(owner);
        MXMain.addOutsideInput(message);
        _preprocess.processMXMessage(message);
    }

    @Override
    public int compareTo(MXMIDIIn o) {
        int x = getDriver().getDriverUID() - o.getDriver().getDriverUID();
        if (x != 0) {
            return x;
        }
        x = getName().compareTo(o.getName());
        if (x != 0) {
            return x;
        }
        
        x = getOrderInDriver() - o.getOrderInDriver();
        return x;
    }

    public static class MessageQueueElement {

        MessageQueueElement(MXMessage message, MXReceiver receiver) {
            _message = message;
            _receiver = receiver;
        }

        MXMessage _message;
        MXReceiver _receiver;
    }

    static int _messageQueueCount = 0;
    static MXQueue<MessageQueueElement> _messageQueue = new MXQueue<>();
    static MXSafeThread _messageThread = null;
    static MessageQueueElement _messageProcessing = null;

    static {
        _messageThread = new MXSafeThread("MessageProcess", () -> {
            while (true) {
                _messageProcessing = null;
                _messageProcessing = _messageQueue.pop();
                if (_messageProcessing == null) {
                    _messageQueueCount --;
                    break;
                }
                MXMessage message = _messageProcessing._message;
                MXReceiver receiver = _messageProcessing._receiver;
                try {
                    messageToReceiver(message, receiver);
                } catch (Throwable ex) {
                    ex.printStackTrace();;
                }
                if (--_messageQueueCount  == 0) {
                    synchronized (_messageQueue) {
                        _messageQueue.notifyAll();
                    }
                }
            }
        });
        _messageThread.setPriority(Thread.MAX_PRIORITY);
        _messageThread.setDaemon(true);
        _messageThread.start();
    }

    public static void messageToReceiverThreaded(MXMessage message, MXReceiver receiver) {
        if (MXConfiguration._DEBUG || Thread.currentThread() == _messageThread) {
            messageToReceiver(message, receiver);
        } else {
            _messageQueueCount ++;
            _messageQueue.push(new MessageQueueElement(message, receiver));
        }
    }

    MXMessage[] retBuf = new MXMessage[3];

    public static void queueMustEmpty() {
        try {
            while (_messageQueueCount != 0) {
                synchronized (_messageQueue) {
                    _messageQueue.wait(10);
                }
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return;
        }
    }

    private synchronized static void messageToReceiver(MXMessage message, MXReceiver receiver) {
        try {
            try {
                if (receiver == MXMain.getMain().getInputProcess()) {
                    MXMain.addInsideInput(message);
                    if (MXMain._capture != null) {
                        MXMain._capture.processMXMessage(message);
                    }
                }
                if (receiver != null) {
                    receiver.processMXMessage(message);
                }
            } catch (RuntimeException ex) {
                MXFileLogger.getLogger(MXMain.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        } catch (Throwable ex) {
            MXFileLogger.getLogger(MXMain.class).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
