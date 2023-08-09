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
import javax.swing.JComponent;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXStatic;
import jp.synthtarou.midimixer.MXThreadList;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMessageTemplate;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_Java;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_UWP;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMIDIIn {
    private static final MXDebugPrint _debug = new MXDebugPrint(MXMIDIIn.class);

    public static final MXMIDIInForPlayer INTERNAL_PLAYER = new MXMIDIInForPlayer();
    
    private String _name;
    private MXDriver _driver;
    private int _driverOrder;
    
    public MXDriver getDriver() {
        return _driver;
    }

    public int getDriverOrder() {
        return _driverOrder;
    }

    public void close() {
        MXMIDIInManager manager = MXMIDIInManager.getManager();
        synchronized(manager) {
            if (isOpen()) {
                allNoteOff();
                manager.onClose(this);
                _driver.InputDeviceClose(_driverOrder);
            }
        }
    }

    private MXNoteOffWatcher _myNoteOff = new MXNoteOffWatcher();
    
    private boolean[] _assigned;
    private int _assignCount = 0;
    private boolean[] _toMaster = new boolean[16];
    private MXVisitant16 _visitant16 = new MXVisitant16();
    
    public boolean isOpen() {
        if (this instanceof MXMIDIInForPlayer) {
            return true;
        }
        if (_driver == null) {
            return false;
        }
        return _driver.InputDeviceIsOpen(_driverOrder);
    }
    
    public MXMIDIIn(MXDriver driver, int driverOrder) {
        _name = driver.InputDeviceName(driverOrder);
        _assigned = new boolean[MXStatic.TOTAL_PORT_COUNT];
        _driver = driver;
        _driverOrder = driverOrder;
        if (driver instanceof MXDriver_UWP) {
            MXDriver_UWP._instance.addInputCatalog(this);
        }
        if (driver instanceof MXDriver_Java) {
            MXDriver_UWP._instance.addInputCatalog(this);
        }
    }
    
    public boolean isPortAssigned(int port) {
        return _assigned[port];
    }
    
    public int getPortAssignCount() {
        return _assignCount;
    }
    
    public String getPortAssignedAsText() {
        StringBuffer assigned = new StringBuffer();
        for (int p = 0; p < MXStatic.TOTAL_PORT_COUNT; ++ p) {
            if (isPortAssigned(p)) {
                if (assigned.length() > 0) {
                    assigned.append(",");
                }
                assigned.append((char)('A' + p));
            }
        }
        return assigned.toString();
    }
    
    public void setPortAssigned(int port, boolean flag) {
        synchronized(MXMIDIInManager.getManager()) {
            if (_assigned[port] != flag) {
                if (!flag) {
                    _myNoteOff.allNoteOffToPort(new MXTiming(), port);
                }
                _assigned[port] = flag;
                int x =  0;
                for (int i = 0; i < _assigned.length; ++ i) {
                    if (_assigned[i]) {
                        x ++;
                    }
                }
                _assignCount = x;
                MXMIDIInManager.getManager().clearMIDIInCache();;
            }
        }
    }
    
    public void resetPortAssigned() {
        synchronized(MXMIDIInManager.getManager()) {
            for (int i = 0; i < MXStatic.TOTAL_PORT_COUNT; ++ i) {
                setPortAssigned(i, false);
            }
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
        for (int ch = 0; ch < 16; ++ ch) {
            if (isToMaster(ch)) {
                if (str.length() == 0) {
                    str.append(Integer.toString(ch));
                }else {
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

    public void allNoteOff() {
        synchronized(MXTiming.mutex) {
            MXTiming timing = new MXTiming();
            _myNoteOff.allNoteOff(timing);
        }
        for (int ch = 0; ch < 16; ++ ch) {
            int status  = MXMidi.COMMAND_CONTROLCHANGE | ch;
            int data1 = MXMidi.DATA1_CC_ALLNOTEOFF;
            receiveShortMessage((status << 16) | (data1 << 8));
            data1 = MXMidi.DATA1_CC_ALLSOUNDOFF;
            receiveShortMessage((status << 16) | (data1 << 8));
        }
    }

    public void allNoteOffToPort(int target) {
        synchronized(MXTiming.mutex) {
            _myNoteOff.allNoteOffToPort(new MXTiming(), target);
        }
    }

    public boolean openInput(long timeout) {
        MXMIDIInManager manager = MXMIDIInManager.getManager();
        synchronized(manager) {
            if (_driver == null) {
                return false;
            }
            if (isOpen()) {
                return true;
            }
            return _driver.InputDeviceOpen(_driverOrder, timeout, this);
        }
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
        for (int ch = 0; ch < 16; ++ ch) {
            if (isToMaster(ch)) {
                if (masterMark.length() != 0) {
                    masterMark.append(", ");
                }
                masterMark.append(Integer.valueOf(ch + 1));
            }
        }
        return masterMark.toString();
    }
    
    public int parseMasteredText(String text){ 
        if (text == null) {
            return 0;
        }
        String[] array = text.split("[ ,]+");
        int hit = 0;
        
        for (int i = 0; i < array.length; ++ i) {
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
                hit ++;
            }
        }
        return hit;
    }
    
    public static final MXReceiver returnReceirer = new MXReceiver() {
        @Override
        public String getReceiverName() {
            return "Return";
        }

        @Override
        public JComponent getReceiverView() {
            return null;
        }

        @Override
        protected void processMXMessageImpl(MXMessage message) {
            MXTiming timing = new MXTiming();
            MXWrapList<MXMIDIIn> list = MXMIDIInManager.getManager().listAllInput();
            for (int x = 0;  x < list.size(); ++ x) {
                MXMIDIIn input = list.valueOfIndex(x);
                for (int port = 0; port < MXStatic.TOTAL_PORT_COUNT; ++ port) {
                    if (input.isPortAssigned(port) == false) {
                        continue;
                    }
                    MXMessage ported = MXMessageFactory.fromClone(message);
                    ported.setPort(port);
                    input.dispatchToPort(ported);
                }
            }
        }
    };

    public void receiveShortMessage(int dword) {
        startMainPath(dword, null);
    }
    
    public void receiveLongMessage(byte[] data) {
        startMainPath(-1, data);
    }

    Thread lastSent = null;
    Thread _attached = null;
    
    private void startMainPath(int dword, byte[] data) {
        if (Thread.currentThread() != _attached) {
            _attached = Thread.currentThread();
            MXThreadList.attachIfNeed("startMainPath", _attached);
        }
        if (lastSent != Thread.currentThread()) {
            lastSent = Thread.currentThread();
            if (Thread.currentThread().getPriority() != Thread.MAX_PRIORITY) {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                System.out.println("Thread MAX");
            }
        }

        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = (dword) & 0xff;
        
        int command = status & 0xf0;
        int channel = status & 0x0f;
        
        /*
        if (status == 0xf8 || status == 0xfe) {
            return;
        }*/
        
        synchronized(MXTiming.mutex) {
            MXTiming timing = new MXTiming();

            if (command == MXMidi.COMMAND_NOTEON) {
                if (data2 == 0) {
                    command = MXMidi.COMMAND_NOTEOFF;
                    status = command | channel;
                    dword = (status << 16) | (data1 << 8) | data2;
                }
            }
            if (this == INTERNAL_PLAYER) {
                MXMain.getMain().getPlayListProcess().updatePianoKeys(dword);
            }

            else if (command == MXMidi.COMMAND_NOTEOFF) {
                if (this == INTERNAL_PLAYER) {
                    MXMain.getMain().getPlayListProcess().updatePianoKeys(dword);
                }
                if (_myNoteOff.raiseHandler(0, timing, status & 0xf, data1)) {
                    return;
                }
            }
            else if (command == MXMidi.COMMAND_CONTROLCHANGE && data1 == MXMidi.DATA1_CC_ALLNOTEOFF) {
                _myNoteOff.allNoteOff(timing);
            }
            else if (command == MXMidi.COMMAND_CONTROLCHANGE && data1 == MXMidi.DATA1_CC_ALLSOUNDOFF) {
                _myNoteOff.allNoteOff(timing);
            }

            for (int port = 0; port < MXStatic.TOTAL_PORT_COUNT; ++ port) {
                if (isPortAssigned(port) == false) {
                    continue;
                }
                MXMessage message = null;
                if (data == null) {
                    message = MXMessageFactory.fromShortMessage(port, status, data1, data2);
                    if ((status & 0xf0) == MXMidi.COMMAND_NOTEON) {
                        MXMessage noteon = MXMessageFactory.fromShortMessage(0, status, data1, data2);
                        noteon._timing = timing;
                        MXMessage noteoff = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_NOTEOFF + message.getChannel(), data1, 0);
                        _myNoteOff.setHandler(noteon, noteoff, new MXNoteOffWatcher.Handler() {
                            @Override
                            public void onNoteOffEvent(MXMessage target) {  
                                MXMain.addOutsideInput(target);
                                dispatchToPort(target);
                            }
                        });
                    }
                }else {
                    message = MXMessageFactory.fromBinary(port, data);
                }
                if (message != null) {
                    message._timing = timing;
                    MXMain.addOutsideInput(message);
                    dispatchToPort(message);
                }
            }
        }
    }

    private void dispatchToPort(MXMessage message) {
        int port = message.getPort();
        synchronized(MXTiming.mutex) {
            if (message.isMessageTypeChannel()) {
                boolean worked = false;
                int ch = message.getChannel();
                int command = message.getCommand();
                int gate = message.getGate();

                MXVisitant visit = _visitant16.get(ch);

                visit.updateVisitantChannel(message);
                message.setVisitant(visit.getSnapShot());

                if (command == MXMidi.COMMAND_CONTROLCHANGE) {
                    if (gate == MXMidi.DATA1_CC_BANKSELECT || gate == (MXMidi.DATA1_CC_BANKSELECT + 0x20)) {
                        if (visit.isHavingBank()) {
                            MXMessage message2 = MXMessageFactory.fromShortMessage(port, command + ch, MXMidi.DATA1_CC_BANKSELECT, 0);
                            message2._timing = message._timing;
                            message2.setVisitant(visit.getSnapShot());
                            dispatchMainPath(message2);
                        }
                        return;
                    }
                    if (gate == MXMidi.DATA1_CC_DATAENTRY || gate == MXMidi.DATA1_CC_DATAENTRY + 0x20) {
                        if (visit.isHaveDataentryRPN()) {
                            int[] temp0 = { MXMessageTemplate.DTEXT_RPN, 0, 0, 0 };
                            MXMessageTemplate template = new MXMessageTemplate(temp0);
                            message = template.buildMessage(port, 0, 0);
                            message.setVisitant(visit.getSnapShot());
                            dispatchMainPath(message);
                        }else if (visit.isHaveDataentryNRPN()) {
                            int[] temp0 = { MXMessageTemplate.DTEXT_NRPN, 0, 0, 0};
                            MXMessageTemplate template = new MXMessageTemplate(temp0);
                            message = template.buildMessage(port, 0, 0);
                            message.setVisitant(visit.getSnapShot());
                            dispatchMainPath(message);
                        }else {
                        }
                        return;
                    }
                    if (gate == MXMidi.DATA1_CC_RPN_LSB || gate == MXMidi.DATA1_CC_RPN_MSB 
                    ||gate == MXMidi.DATA1_CC_NRPN_LSB || gate == MXMidi.DATA1_CC_NRPN_MSB) {
                        //already by updateVisitantChannel
                        return;
                    }
                }

                if (visit.isIncompleteBankInfo()) {
                    if (command == MXMidi.COMMAND_CONTROLCHANGE && (gate == MXMidi.DATA1_CC_BANKSELECT || gate == (MXMidi.DATA1_CC_BANKSELECT + 0x20))) {
                        ;
                    }else {
                        visit.forceCompleteBankInfo();
                    }
                }

                if (visit.isIncomplemteDataentry()) {
                    if (command == MXMidi.COMMAND_CONTROLCHANGE && (gate == MXMidi.DATA1_CC_DATAENTRY || (gate == MXMidi.DATA1_CC_DATAENTRY + 0x20))) { 
                        ;
                    }else {
                        visit.forceCompleteBankDataentry();
                    }
                }

                if (visit.isIncomplemteDataroom()) {
                    if (command == MXMidi.COMMAND_CONTROLCHANGE
                      && (gate == MXMidi.DATA1_CC_RPN_LSB || gate == MXMidi.DATA1_CC_RPN_MSB
                        ||gate == MXMidi.DATA1_CC_NRPN_LSB || gate == MXMidi.DATA1_CC_NRPN_MSB)) {
                        ;
                    }else {
                        visit.forceCompleteBankDataroom();
                    }
                }
            }

            dispatchMainPath(message);
        }
    }
    
    private static void dispatchMainPath(MXMessage message) {
        MXMain.getMain().messageDispatch(message, MXMain.getMain().getInputProcess());
    }
}
