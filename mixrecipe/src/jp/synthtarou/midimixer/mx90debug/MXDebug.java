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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JPanel;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIInForTest;
import jp.synthtarou.midimixer.mx12masterpiano.MX12Process;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXDebug {

    static int _interval = 3;

    LinkedList<MXMessage> _input = new LinkedList();
    ArrayList<MXMessage> _result = new ArrayList<>();
    MXMIDIInForTest _test = new MXMIDIInForTest();

    static MXReceiver<JPanel> _debugProcess = new MXReceiver<JPanel>() {
        @Override
        public String getReceiverName() {
            return "Temp";
        }

        @Override
        public JPanel getReceiverView() {
            return null;
        }

        @Override
        public void processMXMessage(MXMessage message) {
            if (message.getTemplate().get(0) >= 0x100) {
                MXMIDIIn.DEBUGGER.receiveExMessage(message, message);
            }
            else{
                if (message.getDwordCount() == 0) {
                    byte[] data = message.getBinary();
                    MXMIDIIn.DEBUGGER.receiveLongMessage(message, data);
                } else {
                    for (int i = 0; i < message.getDwordCount(); ++i) {
                        int dword = message.getAsDword(i);
                        MXMIDIIn.DEBUGGER.receiveShortMessage(message, dword);
                    }
                }
            }
        }
    };

    public MXDebug(MXMessage message) {
        MX12Process process = MXMain.getMain().getMasterkeyProcess();
        process.startDebug(_result);
        _input.add(message);
        MXMain.getMain().getMasterkeyProcess().sendCCAndGetResult(message, _debugProcess);
        MXMIDIIn.queueMustEmpty();
        if (_result.size() == 0) {
            try {
                Thread.sleep(100); 
            } catch (Exception ex) {
            }
            checkResult();
        }
    }

    public MXDebug(List<MXMessage> target) {
        MX12Process process = MXMain.getMain().getMasterkeyProcess();
        process.startDebug(_result);
        for (MXMessage seek : target) {
            _input.add(seek);
            MXMain.getMain().getMasterkeyProcess().sendCCAndGetResult(seek, _debugProcess);
        }
        MXMIDIIn.queueMustEmpty();
        checkResult();
        if (_interval >= 1) {
            try {
                synchronized (this) {
                    wait(_interval);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();;
            }
        }
    }

    // overrider it
    public abstract void checkResult();

    public void checkResultSame() {
        if (_input.size() != 1) {
            MXFileLogger.getLogger(MXDebug.class).severe(
                    "Test must process as 1 on 1 (for this checkResult, otherwise please override ::checkResult)");
        }
        MXMessage message1 = _input.get(0);
        List<MXMessage> result = _result;
        if (result.size() != 1) {
            System.err.println(_input);
            System.err.println(_result);
            for (int i = 0; i < _result.size(); ++i) {
                for (int j = 0; j < _result.size(); ++j) {
                    if (i != j) {
                        if (_result.get(i) == _result.get(j)) {
                            System.out.println("*******DOBLED " + i + " , " + j);
                        } else if (_result.get(i).equals(_result.get(j))) {
                            if (MXConfiguration._DEBUG) {
                                System.err.println("Need trace");
                                _result.get(i)._trace.printStackTrace();
                                _result.get(j)._trace.printStackTrace();
                            }
                        }
                    }
                }
            }
            MXFileLogger.getLogger(MXDebug.class).log(Level.SEVERE, "Error output size = " + result.size() + "input was " + _input, new Exception());
            if (result.size() >= 2 && result.get(0) != result.get(1)) {
                System.err.println(result.get(0).equals(result.get(1)));
                System.err.println(result.get(0));
                System.err.println(result.get(1));
            }
        } else {
            MXMessage message2 = result.get(0);

            if (message1 == message2) {
                return;
            }

            if (message1.getPort() != message2.getPort()) {
                MXFileLogger.getLogger(MXDebug.class).log(Level.SEVERE, "Error output port = " + message2.getPort()
                        + ", input port = " + message1.getPort(), new Exception());
            }

            if (message1.getDwordCount() != message2.getDwordCount()) {
                MXFileLogger.getLogger(MXDebug.class).log(Level.SEVERE, "Error output dword length = " + message2.getDwordCount()
                        + ", input dword length = " + message1.getDwordCount(), new Exception());
            } else if (message1.getDwordCount() >= 1) {
                for (int i = 0; i < message1.getDwordCount(); ++i) {
                    int d1 = message1.getAsDword(i);
                    int d2 = message2.getAsDword(i);
                    if (d1 != d2) {
                        MXFileLogger.getLogger(MXDebug.class).log(Level.SEVERE, "Error output dword[" + i + "] = " + MXUtil.dumpDword(d2)
                                + ", input dword[" + i + "] = " + MXUtil.dumpDword(d1), new Exception());
                    }
                }
            } else {
                byte[] data1 = message1.getBinary();
                byte[] data2 = message1.getBinary();
                for (int i = 0; i < data1.length; ++i) {
                    if (data1[i] != data2[i]) {
                        MXFileLogger.getLogger(MXDebug.class).log(Level.SEVERE, "Error output bin[" + i + "] = "
                                + MXUtil.dumpDword(data1[i]) + "<>" + MXUtil.dumpDword(data2[i]), new Exception());
                    }
                }
            }
        }
    }
    /*
    public static void printDebug(String text) {
        MXFileLogger.getLogger(MXDebug.class).info(text);
    }*/
}
