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
package jp.synthtarou.cceditor.message;

import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver;


/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCMessage {
    public CCMessage(int dword) {
        _hasDword = true;
        _dword = dword;
    }

    public CCMessage(int status, int data1, int data2) {
        _hasDword = true;
        _dword = (status << 16) | (data1 << 8) | data2;
    }

    public CCMessage(byte[] data) {
        _hasData = true;
        _data = data;
    }
    
    public CCMessage(CCFormat format, CCParameters param) {
        _hasFormatted = true;
        _format = format;
        _param = param;
    }
    
    boolean _hasDword;
    int _dword;
    
    public int getAsDword() {
        if (_hasDword) {
            return _dword;
        }
        
        byte[] data = getAsData();
        if (data != null) {
            int dword = bytesToDword(data);
            if (dword >= 0) {
                _dword = dword;
                _hasDword = true;
                return dword;
            }
            
        }
        
        return 0;
    }
    
    public void setAsDword(int dword) {
        if (_hasFormatted) {
            throw new IllegalStateException("can't set as dword when already formatted");
        }
        _hasDword = true;
        _dword = dword;

        _hasData = false;
        _data = null;
    }
    
    boolean _hasData;
    byte[] _data;
    
    public byte[] getAsData() {
        if (_hasData) {
            return _data;
        }
        if (_hasDword) {
            int status = (_dword >> 16) & 0xff;
            int data1 = (_dword >> 8) & 0xff;
            int data2 = (_dword) & 0xff;
            
            _data = new byte[3];
            return _data;
        }
        if (_hasFormatted) {
            _data = _format.bindParameters(_param);
            if (_data != null) {
                _hasData = true;
                return _data;
            }
            _hasData = false;
        }
        return null;
    }
    
    public void setAsData(byte[] data) {
        if (_hasFormatted) {
            throw new IllegalStateException("can't set as data when already formatted");
        }
        _hasDword = false;

        _hasData = true;
        _data = data;
    }
    
    boolean _hasFormatted;
    CCFormat _format;
    CCParameters _param;
    
    public void setAsParameter(CCFormat format, CCParameters param) {
        _hasFormatted = true;
        _hasData = false;
        _hasDword = false;
        _format = format;
        _param = param;
    }
    
    public CCParameters getParameters() {
        if (_hasFormatted) {
            return _param;
        }
        if (CCMessage.this.makeFormat()) {
            return _param;
        }

        return null;
    }

    public boolean makeFormat() {
        if (getAsDword() != 0) {
            int status = (_dword >> 16) & 0xff;
            int data1 = (_dword >> 8) & 0xff;
            int data2 = (_dword) & 0xff;

            int command = status;
            int channel = 0;
            if (status >= 0x80 && status <= 0xe0) {
                command = status & 0xf0;
                channel = status & 0x0f;
            }

            if (command < 0 || command > 255) {
                return false;
            }
            if (channel < 0 || channel > 15) {
                return false;
            }
            if (data1 < 0 || data1 > 127) {
                return false;
            }
            if (data2 < 0 || data2 > 127) {
                return false;
            }

            int gate = 0;
            int value = 0;

            switch (command) {
                case CCMIDICode.COMMAND_PROGRAMCHANGE:
                    gate = data1;
                    value = 0;

                    data1 = CCVariable.VAR_GL;
                    data2 = 0;
                    break;
                case CCMIDICode.COMMAND_CONTROLCHANGE:
                    gate = data1;
                    value = data2;

                    data1 = CCVariable.VAR_GL;
                    data2 = CCVariable.VAR_VL;
                    break;
                case CCMIDICode.COMMAND_NOTEON:
                case CCMIDICode.COMMAND_NOTEOFF:
                case CCMIDICode.COMMAND_POLYPRESSURE:
                    gate = data1;
                    value = data2;

                    data1 = CCVariable.VAR_GL;
                    data2 = CCVariable.VAR_VL;
                    break;
                case CCMIDICode.COMMAND_PITCHWHEEL:
                    value = (data1 & 127) | (data2 << 7);
                    data1 = CCVariable.VAR_VL;
                    data2 = CCVariable.VAR_VH;
                    break;
                case CCMIDICode.COMMAND_CHANNELPRESSURE:
                    value = data1;
                    data1 = CCVariable.VAR_VL;
                    break;
                default:
                    if (command >= 240 && command <= 247) {
                        if (command == CCMIDICode.STATUS_SONGPOSITION) {
                            value = (data1 & 127) | (data2 << 7);
                            data1= CCVariable.VAR_VL;
                            data2 = CCVariable.VAR_VH;
                        }
                        if (command == CCMIDICode.STATUS_SONGSELECT) {
                            value = data1;
                            data1 = CCVariable.VAR_VL;
                        }
                    }
                    break;
            }

            int[] template = new int[3];
            template[0] = status;
            template[1] = data1;
            template[2] = data2;
            
            CCFormat format = new CCFormat(template);
            CCParameters params = new CCParameters();
            params._gate = gate;
            params._value = value;

            _format = format;
            _param = params;

            return true;
        }

        byte[] data = getAsData();

        if (data == null) {
            return false;
        }

        int[] template = new int[data.length];
        for (int i = 0; i < data.length; ++ i) {
            template[i] = data[i] & 0xff;
        }
         
        CCFormat format = new CCFormat(template);
        _format = format;
        _param = new CCParameters();
        
        return true;
    }
    
    byte[] trimBytes(byte[] original, int from, int to) {
        if (from == 0 && to == original.length -1) {
            return original;
        }
        int length = to - from + 1;
        byte[] ret = new byte[length];
        for (int i = 0; i < length; ++ i) {
            ret[i] = original[from + i];
        }
        return ret;
    }
    
    int bytesToDword(byte[] data) {
        if (data.length == 3) {
            if (data[0] >= 0x80 & data[0] != 0xff) {
                int status = data[0];
                int data1 = data[1];
                int data2 = data[2];
                
                return (((status << 7) | data1) << 7) | data2;
            }
        }
        if (data.length == 2) {
            if (data[0] >= 0x80 & data[0] != 0xff) {
                int status = data[0];
                int data1 = data[1];

                return (((status << 7) | data1) << 7);
            }
        }
        
        return -1;
    }
    
    int indexOf(byte[] data, int from, int ch) {
        for (int i = from; i < data.length; ++ i) {
            int seek = data[i] & 0xff;
            if (seek == ch) {
                return i;
            }
        }
        return  -1; 
    }
    
    private void sendMessage(MXDriver driver, int device, byte[] buffer, int beginIndex, int endIndex) {
        int length = endIndex - beginIndex - 1;
        if (length <= 0) {
            return;
        }
        int status, data1, data2;
        status = buffer[beginIndex];
        
        if (length >= 2) {
            data1 = buffer[beginIndex + 1];
        }
        else {
            data1 = 0;
        }
        if (length >= 3) {
            data2 = buffer[beginIndex + 2];
        }
        else {
            data2 = 0;
        }
        int dword = (((status << 8) | data1) << 8) | data2;
        switch(status & 0xf0) {
            case 0x80:
            case 0x90:
            case 0xa0:
            case 0xb0:
            case 0xc0:
            case 0xd0:
            case 0xe0:
                driver.OutputShortMessage(device, dword);
                return;
            case 0xf0:
                switch(status & 0xff) {
                    case 0xf0://sysex
                    case 0xf7://sysex spcial
                        driver.OutputLongMessage(device, trimBytes(buffer, beginIndex, endIndex));
                        return;
                }
                driver.OutputShortMessage(device, dword);
                return;
        }
        System.out.println("Skip " + MXUtil.dumpHexFF(trimBytes(_data, beginIndex, endIndex)));
    }
    
/*
    public void sendBuffer(MXDriver driver, int device) {
        int dword = getAsDword();
        if (dword > 0) {
            driver.OutputShortMessage(device, dword);
            return;
        }
        
        byte[] data = getAsData();
        byte[] seg;
        int beginIndex = 0, endIndex = 0;
        int scanIndex = 0;
        while(beginIndex < data.length) {
            int status = data[beginIndex] & 0xf0;
            switch(status) {
                case 0x80:
                case 0x90:
                case 0xa0:
                case 0xb0:
                case 0xc0:
                case 0xd0:
                case 0xe0:
                    endIndex = data.length - 1;
                    scanIndex = beginIndex + 1;
                    while(scanIndex <= endIndex) {
                        if ((scanIndex & 0x80) != 0) {
                            endIndex = scanIndex - 1;
                            break;
                        }
                    }
                    sendMessage(driver, device, data, beginIndex, endIndex);
                    beginIndex = endIndex + 1;
                    continue;

                case 0xf0:
                    int lastTrans = -1;
                    
                    switch(data[beginIndex] & 0xff) {
                        case 0xf0://sysex
                        case 0xf7://sysex spcial
                            endIndex = indexOf(data, beginIndex+1, 0xf7);
                            if (endIndex > 0) {
                                lastTrans = endIndex;
                            }
                            else {
                                lastTrans = data.length - 1;
                            }
                            break;
                        
                        case 0xf1://timecode
                            lastTrans = beginIndex + 1;
                            break;
                            
                        case 0xf2://song position                   
                            lastTrans = beginIndex + 2;
                            break;
                        
                        case 0xf3://song select
                            lastTrans = beginIndex + 1;
                            break;
                        
                        case 0xf6://tune request
                        case 0xf8://clock
                        case 0xfa://start
                        case 0xfb://continue
                        case 0xfc://stop
                        case 0xfe://active
                        case 0xff://reset
                            lastTrans = beginIndex + 0;
                            break;
                            
                        case 0xf4://not specific
                        case 0xf5://not specific
                        case 0xf9://not specific
                        case 0xfd://not specific
                            lastTrans = beginIndex + 0;
                            break;
                    }
                    sendMessage(driver, device, data, beginIndex, lastTrans);
                    break;
                    
                default:
                    System.out.println("SkipByte " + MXUtil.toHexFF(data[beginIndex]));
                    beginIndex ++;
                    break;
            }
        }
    }
*/
}

