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
package jp.synthtarou.midimixer.libs.midi;

import java.util.ArrayList;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMessageTemplate implements Cloneable {
    int _metaType;
    Throwable _trace;
    
    private final int[] _template;
    private int _bytePosHiValue;
    private int _bytePosValue;
    private int _bytePosGate;
    private int _bytePosHiGate;
    protected int _checksumLength = 0;
    protected int _checksumTo;
    private boolean _init = false;

    @Override
    public Object clone() {
        MXMessageTemplate temp = new MXMessageTemplate(_template);
        temp._metaType = _metaType;
        temp._bytePosGate = getBytePosGate();
        temp._bytePosHiGate = getBytePosHiGate();
        temp._bytePosValue = getBytePosValue();
        temp._bytePosHiValue = getBytePosHiValue();
        temp._checksumLength = _checksumLength;
        temp._checksumTo = _checksumTo;
        return temp;
    }

    public static final String EXCOMMAND_PROGRAM_INC = "@PROG_INC";
    public static final String EXCOMMAND_PROGRAM_DEC = "@PROG_DEC";

    public static final int DTEXT_NONE = 0x100;
    public static final int DTEXT_VL = 0x200;
    public static final int DTEXT_VH = 0x300;
    public static final int DTEXT_GL = 0x400;
    public static final int DTEXT_GH = 0x500;
    public static final int DTEXT_CH = 0x600;
    public static final int DTEXT_1CH = 0x700;
    public static final int DTEXT_2CH = 0x800;
    public static final int DTEXT_3CH = 0x900;
    public static final int DTEXT_PCH = 0xA00;
    public static final int DTEXT_1RCH = 0xB00;
    public static final int DTEXT_2RCH = 0xC00;
    public static final int DTEXT_3RCH = 0xD00;
    public static final int DTEXT_4RCH = 0xE00;
    public static final int DTEXT_VF1 = 0xF00;
    public static final int DTEXT_VF2 = 0x1000;
    public static final int DTEXT_VF3 = 0x1100;
    public static final int DTEXT_VF4 = 0x1200;
    public static final int DTEXT_VPGL = 0x1300;
    public static final int DTEXT_VPGH = 0x1400;
    public static final int DTEXT_CCNUM = 0x1500;
    public static final int DTEXT_RSCTRT1 = 0x1A00;
    public static final int DTEXT_RSCTRT2 = 0x1B00;
    public static final int DTEXT_RSCTRT3 = 0x1C00;
    public static final int DTEXT_RSCTRT1P = 0x1D00;
    public static final int DTEXT_RSCTRT2P = 0x1E00;
    public static final int DTEXT_RSCTRT3P = 0x1F00;
    public static final int DTEXT_RSCTPT1 = 0x2000;
    public static final int DTEXT_RSCTPT2 = 0x2100;
    public static final int DTEXT_RSCTPT3 = 0x2200;
    public static final int DTEXT_RSCTPT1P = 0x2300;
    public static final int DTEXT_RSCTPT2P = 0x2400;
    public static final int DTEXT_RSCTPT3P = 0x2500;
    public static final int DTEXT_CHECKSUM = 0x2600;

    public static final int DTEXT_4CH = 0x2700;
    public static final int DTEXT_5CH = 0x2800;
    public static final int DTEXT_6CH = 0x2900;
    public static final int DTEXT_7CH = 0x2a00;
    public static final int DTEXT_8CH = 0x2b00;
    public static final int DTEXT_9CH = 0x2c00;
    public static final int DTEXT_ACH = 0x2d00;
    public static final int DTEXT_BCH = 0x2e00;
    public static final int DTEXT_CCH = 0x2f00;
    public static final int DTEXT_DCH = 0x3000;
    public static final int DTEXT_ECH = 0x3100;
    public static final int DTEXT_FCH = 0x3200;

    public static final int DTEXT_RPN = 0x4000;
    public static final int DTEXT_NRPN = 0x4100;

    public static final int DTEXT_PROGINC = 0x5000;
    public static final int DTEXT_PROGDEC = 0x5100;
    
    static MXWrapList<Integer> textAlias = new MXWrapList();

    public MXMessageTemplate(int[] template) {
        _template = template;
        _trace = new Throwable(toString());
        initFields();
    }

    public MXMessageTemplate(int[] template, int checksumLength) {
        if (template == null) {
            throw new IllegalArgumentException("MXMessage is NULL");
        }
        if (template.length == 0) {
             throw new IllegalArgumentException("MXMessage.length == 0");
        }
        _template = template;
        _trace = new Throwable();
        _checksumLength = checksumLength;
        initFields();
    }
    
    public int get(int pos) {
        if (_template == null) {
            return 0;
        }
        if (pos < 0 || pos >= _template.length) {
            return 0;
        }
        return _template[pos];
    }
    
    public int size() {
        if (_template == null)  {
            return 0;
        }
        return _template.length;
    }

    public void initFields() {
        //if (_init) {
        //    return;
        //}
        _bytePosHiValue = -1;
        _bytePosValue = -1;
        _bytePosGate = -1;
        _bytePosHiGate = -1;
        if (_template == null) {
            return;
        }

        if (_template[0] == 0xff) {
            _metaType = _template[1];
            //nothing fof now
        }
        for (int i = 0; i < _template.length; ++ i) {
            int x = _template[i];
            if (_template[i] == DTEXT_VL) {
                _bytePosValue = i;
            }
            else if (_template[i] == DTEXT_VH) {
                _bytePosHiValue = i;
            }
            else if (_template[i] == DTEXT_GL) {
                _bytePosGate = i;
            }
            else if (_template[i] == DTEXT_GH) {
                _bytePosHiGate = i;
            }
            else if (x == DTEXT_CHECKSUM) {
                _checksumTo = i;
            }
        }
        _init = true;
    }
    
    public byte[] makeBytes(byte[] data, MXMessage message) {
        synchronized(this) {
            if (data == null || _template.length != data.length) {
                data = new byte[_template.length];
            }
            int _checksumTo = -1;

            _bytePosHiValue = -1;
            _bytePosValue = -1;
            _bytePosGate = -1;
            _bytePosHiGate = -1;
            if (isDataentry()) {//TODO VISITANT
                data[0] = (byte)(MXMidi.COMMAND_CONTROLCHANGE + message.getChannel());
                data[1] = MXMidi.DATA1_CC_DATAENTRY;
                data[2] = 0;
            }else {
                for (int i = 0; i < _template.length; ++ i) {
                    int x = _template[i];
                    if ((x & 0xff00) != 0) {
                        if (x == DTEXT_CHECKSUM) {
                            _checksumTo = i;
                        }else {
                            try {
                                x = getDValue(x, message);
                            }catch(IllegalArgumentException e) {
                                throw e;
                            }
                        }
                    }
                    data[i] = (byte)(x & 0xff);
                }
                int command = data[0] & 0xf0;
                if (command >= 0x80 && command <= 0xe0) {
                    data[0] = (byte)(command + message.getChannel());
                }
                if (_checksumLength >= 0 && _checksumTo >= 0) {
                    int x128 = 0;
                    for (int x = _checksumTo - _checksumLength; x < _checksumTo; ++ x) {
                        x128 += data[x];
                    }
                    x128 = x128 & 0x7f;
                    int r = 128 - x128;
                    data[_checksumTo] = (byte)(r & 0x7f);
                }
            }
        }
        return data;
    }
    
    public void sealChecksum(byte[] data) {
        if (_checksumLength >= 0 && _checksumTo >= 0) {
            int x128 = 0;
            for (int x = _checksumTo - _checksumLength; x < _checksumTo; ++ x) {
                x128 += data[x];
            }
            x128 = x128 & 0x7f;
            int r = 128 - x128;
            data[_checksumTo] = (byte)(r & 0x7f);
        }
    }

    public static int getDValue(int alias, MXMessage message) {
        int gate = message.getGate();
        int value = message.getValue();
        int channel = message.getChannel();
    
        String str = fromD(alias);

        switch(alias & 0xff00) {
            case DTEXT_RPN:
            case DTEXT_NRPN:
            case DTEXT_PROGINC:
            case DTEXT_PROGDEC:
            case DTEXT_NONE:
                return 0;
            case DTEXT_VL:
                alias = value & 0x7f;
                break;
            case DTEXT_VH:
                alias = (value >> 7) & 0x7f;
                break;
            case DTEXT_GL:
                alias = gate & 0x7f;
                break;
            case DTEXT_GH:
                alias = (gate >> 7) & 0x7f;
                break;
            case DTEXT_CH:
                alias = channel;
                break;
            case DTEXT_1CH:
                alias = 0x10 + channel;
                break;
            case DTEXT_2CH:
                alias = 0x20 + channel;
                break;
            case DTEXT_3CH:
                alias = 0x30 + channel;
                break;
            case DTEXT_4CH:
                alias = 0x40 + channel;
                break;
            case DTEXT_5CH:
                alias = 0x50 + channel;
                break;
            case DTEXT_6CH:
                alias = 0x60 + channel;
                break;
            case DTEXT_7CH:
                alias = 0x70 + channel;
                break;
            case DTEXT_8CH:
                alias = 0x80 + channel;
                break;
            case DTEXT_9CH:
                alias = 0x90 + channel;
                break;
            case DTEXT_ACH:
                alias = 0xA0 + channel;
                break;
            case DTEXT_BCH:
                alias = 0xB0 + channel;
                break;
            case DTEXT_CCH:
                alias = 0xC0 + channel;
                break;
            case DTEXT_DCH:
                alias = 0xD0 + channel;
                break;
            case DTEXT_ECH:
                alias = 0xE0 + channel;
                break;
            case DTEXT_FCH:
                alias = 0xF0 + channel;
                break;
            case DTEXT_PCH:
                if (message.getPort() >= 0 && message.getPort() <= 3) {
                    alias = message.getPort() * 0x10 + channel;
                }else {
                    alias = 0x30 + channel;
                }
                break;
            case DTEXT_1RCH:
            case DTEXT_2RCH:
            case DTEXT_3RCH:
            case DTEXT_4RCH:
                throw new IllegalArgumentException("1RCH, 2RCH, 3RCH, 4RCH not supported.");
                //break;
            case DTEXT_VF1:
                alias = (value) & 0x0f;
                break;
            case DTEXT_VF2:
                alias = (value>> 4) & 0x0f;
                break;
            case DTEXT_VF3:
                alias = (value>> 8) & 0x0f;
                break;
            case DTEXT_VF4:
                alias = (value >> 12) & 0x0f;
                break;
            case DTEXT_VPGL:
                alias = (value + gate) & 0x7f;
                break;
            case DTEXT_VPGH:
                alias = ((value + gate) >> 7) & 0x7f;
                break;
            case DTEXT_RSCTRT1:
            case DTEXT_RSCTRT2:
            case DTEXT_RSCTRT3:
                throw new IllegalArgumentException("RSCTRT1, RSCTRT2, RSCTRT3 not supported.");
                //break;
            case DTEXT_RSCTRT1P:
            case DTEXT_RSCTRT2P:
            case DTEXT_RSCTRT3P:
                throw new IllegalArgumentException("RSCTRT1P, RSCTRT2P, RSCTRT3P not supported.");
                //break;
            case DTEXT_RSCTPT1:
            case DTEXT_RSCTPT2:
            case DTEXT_RSCTPT3:
                throw new IllegalArgumentException("RSCTPT1, RSCTPT2, RSCTPT3 not supported.");
                 //break;
            case DTEXT_RSCTPT1P:
            case DTEXT_RSCTPT2P:
            case DTEXT_RSCTPT3P:
                throw new IllegalArgumentException("RSCTPT1P, RSCTPT2P, RSCTPT3P not supported.");
                //break;
/*
static final int DTEXT_CCNUM = 0x1500;
*/
            case DTEXT_CHECKSUM:
                //_checksumTo = i;
                break;

            case 0:
                return (byte)alias;
                
            default:
                boolean haveEx = false;
                throw new IllegalArgumentException("something wrong " + Integer.toHexString(alias) + " , " + fromD(alias));
        }
        return (byte) alias;
    }

    public static String fromD(int dtext) {
        int index = textAlias.indexOfValue(dtext);
        if (index >= 0) {
            return textAlias.get(index).name;
        }
        //return MXUtil.toHexFF(dtext) + "h";
        return Integer.toHexString(dtext) + "h";
    }

    public String toDText(MXMessage message) {
        ArrayList<String> array = toDArray(message);
        
        StringBuffer text = new StringBuffer();
        String last = "]";
        for (String seg : array) {
            if (seg.length() == 0) {
                continue;
            }
            if (text.length() >= 0) {
                if (seg.equals("[") || seg.equals("]") || last.equals("[") || last.equals("]")) {
                    // nothing
                }else {
                    text.append(" ");
                }
            }
            last = seg;
            text.append(seg);
        }
        return text.toString();
    }

    public ArrayList<String> toDArray(MXMessage message) {
        ArrayList<String> texts = new ArrayList();

        if (_template == null) {
            return texts;
        }
        if (_template[0] == MXMessageTemplate.DTEXT_PROGINC) {
            texts.add(EXCOMMAND_PROGRAM_INC);
            return texts;
        }
        if (_template[0] == MXMessageTemplate.DTEXT_PROGDEC) {
            texts.add(EXCOMMAND_PROGRAM_DEC);
            return texts;
        }
        if (isDataentry()) {
            if (_template[0] == DTEXT_RPN) {
                texts.add("@RPN");
            }else {
                texts.add("@NRPN");
            }
            MXVisitant visitant = message.getVisitant();
            if (visitant != null) {
                texts.add(fromD(visitant.getDataentryMSB()));
                texts.add(fromD(visitant.getDataentryLSB()));
                texts.add(fromD(message.getValue()));
                return texts;
            }else {
                new Exception("RPN have no DATA").printStackTrace();
                return null;
            }
        }
        
        if (message.isMessageTypeChannel()) {
            int command = message.getCommand();
            int channel = message.getChannel();
            int data1 = message.getData1();
            int data2 = message.getData2();
            if (command == MXMidi.COMMAND_PITCHWHEEL) {
                texts.add("@PB");
                texts.add(fromD(_template[1]));
                texts.add(fromD(_template[2]));
                return texts;
            }
            if (command == MXMidi.COMMAND_CHANNELPRESSURE) {
                texts.add("@CP");
                texts.add(fromD(_template[1]));
                return texts;
            }
            if (command == MXMidi.COMMAND_POLYPRESSURE) {
                texts.add("@PKP");
                texts.add(fromD(_template[1]));
                texts.add(fromD(_template[2]));
                return texts;
            }
            if (command == MXMidi.COMMAND_CONTROLCHANGE) {
                texts.add("@CC");
                texts.add(fromD(_template[1]));
                texts.add(fromD(_template[2]));
                return texts;
            }
            /*
                @RPN [RPN MSB] [RPN LSB] [Data MSB] [Data LSB] 	RPNを送信します。
                @NRPN [NRPN MSB] [NRPN LSB] [Data MSB] [Data LSB] 	NRPNを送信します。 
            */
        }
        
        int csumFrom = -1;
        if (_checksumLength >= 0) {
            for (int i = 0; i < _template.length; ++ i) {
                if (_template[i] == DTEXT_CHECKSUM) {
                    csumFrom = i -_checksumLength;
                    break;
                 }
            }
        }
        
        for (int i = 0; i < _template.length; ++ i) {
            if (i == csumFrom) {
                texts.add("[");
            }
            int code = _template[i];
            if (i == 0 && message.isMessageTypeChannel()) {
                code &= 0xf0;
            }
            if (code == DTEXT_CHECKSUM) {
                texts.add("]");
                continue;
            }
            texts.add(fromD(code));
        }
        return texts;
    }
   
    static {
        textAlias.addNameAndValue("#NONE", DTEXT_NONE);
        textAlias.addNameAndValue("#VL", DTEXT_VL);
        textAlias.addNameAndValue("#VH", DTEXT_VH);
        textAlias.addNameAndValue("#GL", DTEXT_GL);
        textAlias.addNameAndValue("#GH", DTEXT_GH);
        textAlias.addNameAndValue("#CH", DTEXT_CH);
        textAlias.addNameAndValue("#1CH", DTEXT_1CH);
        textAlias.addNameAndValue("#2CH", DTEXT_2CH);
        textAlias.addNameAndValue("#3CH", DTEXT_3CH);
        textAlias.addNameAndValue("#PCH", DTEXT_PCH);
        textAlias.addNameAndValue("#1RCH", DTEXT_1RCH);
        textAlias.addNameAndValue("#2RCH", DTEXT_2RCH);
        textAlias.addNameAndValue("#3RCH", DTEXT_3RCH);
        textAlias.addNameAndValue("#4RCH", DTEXT_4RCH);
        textAlias.addNameAndValue("#VF1", DTEXT_VF1);
        textAlias.addNameAndValue("#VF2", DTEXT_VF2);
        textAlias.addNameAndValue("#VF3", DTEXT_VF3);
        textAlias.addNameAndValue("#VF4", DTEXT_VF4);
        textAlias.addNameAndValue("#VPGL", DTEXT_VPGL);
        textAlias.addNameAndValue("#VPGH", DTEXT_VPGH);
        textAlias.addNameAndValue("#RSCTRT1", DTEXT_RSCTRT1);
        textAlias.addNameAndValue("#RSCTRT2", DTEXT_RSCTRT2);
        textAlias.addNameAndValue("#RSCTRT3", DTEXT_RSCTRT3);
        textAlias.addNameAndValue("#RSCTRT1P", DTEXT_RSCTRT1P);
        textAlias.addNameAndValue("#RSCTRT2P", DTEXT_RSCTRT2P);
        textAlias.addNameAndValue("#RSCTRT3P", DTEXT_RSCTRT3P);
        textAlias.addNameAndValue("#RSCTPT1", DTEXT_RSCTPT1);
        textAlias.addNameAndValue("#RSCTPT2", DTEXT_RSCTPT2);
        textAlias.addNameAndValue("#RSCTPT3", DTEXT_RSCTPT3);
        textAlias.addNameAndValue("#RSCTPT1P", DTEXT_RSCTPT1P);
        textAlias.addNameAndValue("#RSCTPT2P", DTEXT_RSCTPT2P);
        textAlias.addNameAndValue("#RSCTPT3P", DTEXT_RSCTPT3P);
        textAlias.addNameAndValue("#CHECKSUM", DTEXT_CHECKSUM);

        textAlias.addNameAndValue("#4CH", DTEXT_4CH);
        textAlias.addNameAndValue("#5CH", DTEXT_5CH);
        textAlias.addNameAndValue("#6CH", DTEXT_6CH);
        textAlias.addNameAndValue("#7CH", DTEXT_7CH);
        textAlias.addNameAndValue("#8CH", DTEXT_8CH);
        textAlias.addNameAndValue("#9CH", DTEXT_9CH);
        textAlias.addNameAndValue("#ACH", DTEXT_ACH);
        textAlias.addNameAndValue("#BCH", DTEXT_BCH);
        textAlias.addNameAndValue("#CCH", DTEXT_CCH);
        textAlias.addNameAndValue("#DCH", DTEXT_DCH);
        textAlias.addNameAndValue("#ECH", DTEXT_ECH);
        textAlias.addNameAndValue("#FCH", DTEXT_FCH);

        textAlias.addNameAndValue("#RPN", DTEXT_RPN); //@RPN [RPN MSB] [RPN LSB] [Data MSB] [Data LSB]
        textAlias.addNameAndValue("#NRPN", DTEXT_NRPN); //@NRPN [NRPN MSB] [NRPN LSB] [Data MSB] [Data LSB]
    }

    public static int readAliasText(String str) {
        int code = -1;
        if (str.startsWith("#")) {
            int find = textAlias.indexOfName(str);
            if (find >= 0) {
                code = textAlias.get(find).value.intValue();
                return code & 0xff00;
            }
        }
        int x = MXUtil.numberFromText(str);
        if (x >= 0) {
            return x & 0xff;
        }
        return 0;
    }

    public MXMessage buildMessage(int port, int gate, int value) {
        MXMessage message = new MXMessage(port, this);
        message.setPort(port);
        message.setGate(gate);
        message.setValue(value);
        return message;
    }


    /**
     * @return the _bytePosHiValue
     */
    public int getBytePosHiValue() {
        initFields();
        return _bytePosHiValue;
    }

    /**
     * @return the _bytePosValue
     */
    public int getBytePosValue() {
        initFields();
        return _bytePosValue;
    }

    /**
     * @return the _bytePosGate
     */
    public int getBytePosGate() {
        initFields();
        return _bytePosGate;
    }

    /**
     * @return the _bytePosHiGate
     */
    public int getBytePosHiGate() {
        initFields();
        return _bytePosHiGate;
    }
    
    public String toString() {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < _template.length; ++ i){
            int x = _template[i];
            String seg = fromD(x);
            if (seg == null) {
                seg = Integer.toHexString(x);
            }
            if (str.length() != 0) {
                str.append(", ");
            }
            str.append(seg);
        }
        return str.toString();
    }
    
    public boolean isDataentry() {
        if (_template.length > 0) {
            if (_template[0] == DTEXT_RPN || _template[0] == DTEXT_NRPN) {
                return true;
            }
        }
        return false;
    }
}
