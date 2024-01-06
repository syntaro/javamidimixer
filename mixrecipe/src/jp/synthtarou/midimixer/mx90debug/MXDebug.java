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

import java.util.LinkedList;
import java.util.List;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.port.FinalMIDIOut;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIInForTest;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXDebug {
    static int _interval = 3;

    static FinalMIDIOut _final = FinalMIDIOut.getInstance();

    LinkedList<MXMessage> _input;
    ResultModel _debugMessages;
    MXMIDIInForTest _test = MXMIDIIn.INTERNAL_TESTER;

    public MXDebug(ResultModel debugMessages, MXMessage target) {
        _final.startTestSignal(-1);
        _input = new LinkedList<>();
        _input.add(target);
        _test.startTest(target);
        _debugMessages = debugMessages;
        checkResult();
        if (_interval >= 1) {
            try {
                Thread.sleep(_interval);
            }catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public MXDebug(ResultModel debugMessages, List<MXMessage> target) {
        _final.startTestSignal(-1);
        _input = new LinkedList<>();
        _debugMessages = debugMessages;
        for (MXMessage seek : target) {
            _input.add(seek);
            _test.startTest(seek);
        }
        checkResult();
        if (_interval >= 1) {
            try {
                Thread.sleep(_interval);
            }catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // overrider it
    public abstract void checkResult();
    
    public void addDebugMessage(String text) {
        if (_debugMessages == null) {
            _debugMessages = new ResultModel();
        }
        _debugMessages.println(text);
    }
    
    public ResultModel getDebugMessages() {
        return _debugMessages;
    }
    
    public void checkResultSame() {
        if (_input.size() != 1) {
            addDebugMessage("Test must process as 1 on 1 (for this checkResult, otherwise please override ::checkResult)");
        }
        MXMessage message1 = _input.get(0);
        LinkedList<MXMessage> result = _final.getTestResult();
        if (result.size() != 1) {
            addDebugMessage("Error output size = " + result.size());
        }
        MXMessage message2 =  result.get(0);
        
        if (message1.getPort() != message2.getPort()) {
            addDebugMessage("Error output port = " + message2.getPort()
                  + ", input port = " + message1.getPort());
        }
        
        if (message1.getDwordCount() != message2.getDwordCount())  {
            addDebugMessage("Error output dword length = " + message2.getDwordCount()
                  + ", input dword length = " + message1.getDwordCount());
        }
        else if (message1.getDwordCount() >= 1) {
            for (int i = 0; i < message1.getDwordCount(); ++ i) {
                int d1 = message1.getAsDword(i);
                int d2 = message2.getAsDword(i);
                if (d1 != d2)  {
                    addDebugMessage("Error output dword[" + i + "] = " + d2
                         + ", input dword[" +  i +  "] = " + d1);
                }
            }
        }else {
            byte[] data1 = message1.getBinary();
            byte[] data2 = message1.getBinary();
            for (int i = 0; i <data1.length; ++ i) {
                if (data1[i] != data2[i]) {
                    addDebugMessage("Error output dump[" + MXUtil.dumpHex(data2) + "]"
                          + ", input dump[" + MXUtil.dumpHex(data1) + "]");
                }
            }
        }
    }
}
