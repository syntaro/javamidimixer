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

import java.io.File;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXStatic;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.MXQueue1;
import jp.synthtarou.midimixer.libs.domino.DTextXML;
import jp.synthtarou.midimixer.libs.midi.programlist.database.PDFileManager;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.programlist.database.PDFile;
import jp.synthtarou.midimixer.libs.console.ConsoleElement;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_Java;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_UWP;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_VSTi;
import org.xml.sax.SAXException;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMIDIOut {
    private static final MXDebugPrint _debug = new MXDebugPrint(MXMIDIOut.class);
    public static final MXMIDIOut OUTPUT_NONE = new MXMIDIOut(/* TODO*/ null, 0);

    private MXDriver _driver;
    private int _driverOrder;
    private String _name;
    boolean[] _assigned;
    int _assignCount;
    private File _DXMLFile;
    private MXVisitant16 _visitant16 = new MXVisitant16();

    MXQueue1<MXMessage> _queue;
    Thread _thread;
    
    private MXNoteOffWatcher _myNoteOff = new MXNoteOffWatcher();

    public MXDriver getDriver() {
        return _driver;
    }

    public int getDriverOrder() {
        return _driverOrder;
    }

    public boolean isPortAssigned(int port) {
        if (port < 0 || port >= MXStatic.TOTAL_PORT_COUNT) {
            return false;
        }
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
                    _myNoteOff.allNoteFromPort(new MXTiming(), port);
                }
                _assigned[port] = flag;
                int x =  0;
                for (int i = 0; i < _assigned.length; ++ i) {
                    if (_assigned[i]) {
                        x ++;
                    }
                }
                _assignCount = x;
                MXMIDIOutManager.getManager().clearMIDIOutCache();
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

    public boolean isOpen() {
        return _driver.OutputDeviceIsOpen(_driverOrder);
    }
    
    protected MXMIDIOut(MXDriver driver, int driverOrder) {
        _assigned = new boolean[16];
        _driver = driver;
        _driverOrder = driverOrder;
        if (driver == null) {
            _name = "(NULL)";
        }else {
            _name = driver.OutputDeviceName(driverOrder);
        }
    }

    public String getName() {
        return _name;
    }

    public File getDXMLFile() {
        return _DXMLFile;
    }
    
    public void setDXMLFile(File file) throws SAXException {
        try {
            PDFileManager manager = PDFileManager.getManager();
            if (_DXMLFile != null) {
                manager.unregist(_DXMLFile.getName());
            }
            PDFile parser = DTextXML.fromFile(file);
            _DXMLFile = file;
            manager.register(parser);
        }catch(SAXException e) {
            throw e;
        }catch(Exception e) {
             e.printStackTrace();
        }
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
        }catch(Exception e) {
            return -1;
        }
    }
   
    public void processMidiOut(MXMessage message) {
        try {
            processMidiOutInternal(message);
        }catch(Throwable e) {
            e.printStackTrace();
        }
    }

    private synchronized void processMidiOutInternal(MXMessage message) {
        if (!_driver.OutputDeviceIsOpen(_driverOrder)) {
            return;
        }
        
        MXMain.addInsideOutput(message);

        try {
            if (MXVisitant.isMesssageHaveVisitant(message)) {
                _visitant16.get(message.getChannel()).updateVisitantChannel(message);
            }

            long timeStamp = getTimestamp();
            long recTime = 0;

            if (message.isMessageTypeChannel()) {
                MXVisitant msgVisitant = message.getVisitant();
                MXVisitant visitant = _visitant16.get(message.getChannel());
                int command = message.getCommand();
                int channel = message.getChannel();
                int gate = message.getGate();
                if (command != MXMidi.COMMAND_PROGRAMCHANGE) {
                    if (msgVisitant != null && msgVisitant.isHavingProgram()) {
                        if (visitant.isHavingProgram() == false || visitant.getProgram() != message.getVisitant().getProgram()) {
                            visitant.mergeNew(message.getVisitant());
                            MXMessage newMessage = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_PROGRAMCHANGE + channel, visitant.getProgram(),0);
                            newMessage._timing = message._timing;
                            newMessage.setVisitant(visitant.getSnapShot());
                            processMidiOutInternal(newMessage);
                        }
                    }
                }
                if (command != MXMidi.COMMAND_CONTROLCHANGE || (gate != MXMidi.DATA1_CC_BANKSELECT && gate != MXMidi.DATA1_CC_BANKSELECT + 32)) {
                    //0と32が連続でくる間は、この処理は実行されないので、半端を送ることもない
                    if (msgVisitant != null && msgVisitant.isHavingBank()) {
                        if (visitant.isHavingBank() == false || visitant.getBankMSB() != msgVisitant.getBankMSB() || visitant.getBankLSB() != msgVisitant.getBankLSB()) {
                            visitant.mergeNew(message.getVisitant());
                            
                            MXMessage newMessage1 = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_CONTROLCHANGE + channel, MXMidi.DATA1_CC_BANKSELECT ,visitant._bankMSB);
                            MXMessage newMessage2 = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_CONTROLCHANGE + channel, MXMidi.DATA1_CC_BANKSELECT + 32 ,visitant._bankLSB);
                            newMessage1._timing = message._timing;
                            newMessage2._timing = message._timing;

                            processMidiOutInternal(newMessage1);
                            processMidiOutInternal(newMessage2);
                        }
                    }
                }
                
                if (command != MXMidi.COMMAND_CONTROLCHANGE || (gate != MXMidi.DATA1_CC_CHANNEL_VOLUME)) {
                    if (msgVisitant != null && msgVisitant.isHavingVolume()) {
                        if (visitant.isHavingVolume() == false || visitant.getInfoVolume()!= msgVisitant.getInfoVolume()) {
                            visitant.mergeNew(message.getVisitant());
                            
                            MXMessage newMessage = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_CONTROLCHANGE + channel, MXMidi.DATA1_CC_CHANNEL_VOLUME ,visitant.getInfoVolume());
                            newMessage._timing = message._timing;

                            processMidiOutInternal(newMessage);
                        }
                    }
                }

                if (command != MXMidi.COMMAND_CONTROLCHANGE || (gate != MXMidi.DATA1_CC_EXPRESSION)) {
                    if (msgVisitant != null && msgVisitant.isHavingExpression()) {
                        if (visitant.isHavingExpression() == false || visitant.getInfoExpression()!= msgVisitant.getInfoExpression()) {
                            visitant.mergeNew(message.getVisitant());
                            
                            MXMessage newMessage = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_CONTROLCHANGE + channel, MXMidi.DATA1_CC_EXPRESSION,visitant.getInfoExpression());
                            newMessage._timing = message._timing;

                            processMidiOutInternal(newMessage);
                        }
                    }
                }

                if (command != MXMidi.COMMAND_CONTROLCHANGE || (gate != MXMidi.DATA1_CC_PANPOT)) {
                    if (msgVisitant != null && msgVisitant.isHavingExpression()) {
                        if (visitant.isHavingPan()== false || visitant.getInfoPan()!= msgVisitant.getInfoPan()) {
                            visitant.mergeNew(message.getVisitant());
                            
                            MXMessage newMessage = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_CONTROLCHANGE + channel, MXMidi.DATA1_CC_PANPOT,visitant.getInfoPan());
                            newMessage._timing = message._timing;

                            processMidiOutInternal(newMessage);
                        }
                    }
                }
            }
            
            if (message.getCommand() == MXMidi.COMMAND_NOTEON) {
                _myNoteOff.setHandler(message, message, new MXNoteOffWatcher.Handler() {
                    @Override
                    public void onNoteOffEvent(MXMessage target) {
                        int dword = target.getAsDword(0);
                        _driver.OutputShortMessage(_driverOrder, dword);
                        MXMain.addOutsideOutput(new ConsoleElement(target._timing, target.getPort(), dword));
                    }
                });
                int dword = message.getAsDword(0);
                _driver.OutputShortMessage(_driverOrder, dword);
                MXMain.addOutsideOutput(new ConsoleElement(message._timing, message.getPort(), dword));
                return;
            }
            else if (message.getCommand() == MXMidi.COMMAND_NOTEOFF) {
                if (_myNoteOff.raiseHandler(message)) {
                    return;
                }
            }
            else if (message.getCommand() == MXMidi.COMMAND_CONTROLCHANGE
                  && message.getData1() == MXMidi.DATA1_CC_ALLNOTEOFF) {
                allNoteOff();
            }
            
            int col = message.getDwordCount();
            if (col == 0) {
                byte[] data = message.getDataBytes();
                _driver.OutputLongMessage(_driverOrder, data);
                MXMain.addOutsideOutput(new ConsoleElement(message._timing, message.getPort(), data));
            }else if (col > 0) {
                for (int i = 0; i < col; ++ i) {
                    int dword = message.getAsDword(i);
                    _driver.OutputShortMessage(_driverOrder, dword);
                    MXMain.addOutsideOutput(new ConsoleElement(message._timing, message.getPort(), dword));
                }
            }else {
                if (message.isDataentry()) {
                    if (message.getVisitant() == null) {
                        System.out.println("DATAENTRY no set VISITANT");
                        return;
                    }
                    if (message.getVisitant().isHaveDataentryRPN() == false
                     && message.getVisitant().isHaveDataentryNRPN() == false) {
                        System.out.println("DATAENTRY no set RPN / NRPN");
                        return;
                    }
                }
                //Nothing
                System.out.println("nothing to send : " + message);
            }
        }catch(Throwable e) {
            _debug.printStackTrace(e);
        }
        
    }

    public void allNoteOff() {
        synchronized(MXTiming.mutex) {
            _myNoteOff.allNoteOff(new MXTiming());
        }
    }

    public void allNoteOffFromPort(MXTiming timing, int port) {
        _myNoteOff.allNoteFromPort(timing, port);
    }

    public boolean openOutput(long timeout) {
        MXMIDIOutManager manager = MXMIDIOutManager.getManager();
        synchronized(manager) {
            manager.clearMIDIOutCache();
            _visitant16 = new MXVisitant16();
            if (_driver.OutputDeviceIsOpen(_driverOrder) == false) {
                _driver.OutputDeviceOpen(_driverOrder, timeout);
            }
            return _driver.OutputDeviceIsOpen(_driverOrder);
        }
    }
    
    public void close() {
        MXMIDIOutManager manager = MXMIDIOutManager.getManager();
        synchronized(manager) {
            if (isOpen()) {
                allNoteOff();
                if(_name.equals("Gervill")) {
                    
                }else  {
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
