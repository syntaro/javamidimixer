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

import java.util.HashMap;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCVariable {
    static final HashMap<String, Integer> _all = new HashMap();
    static final HashMap<Integer, String> _reverse = new HashMap();
        
    public static final int VAR_NONE = 0x100;
    public static final int VAR_VL = 0x200;
    public static final int VAR_VH = 0x300;
    public static final int VAR_GL = 0x400;
    public static final int VAR_GH = 0x500;
    public static final int VAR_CH = 0x600;

    public static final int VAR_1CH = 0x1100;
    public static final int VAR_2CH = 0x1200;
    public static final int VAR_3CH = 0x1300;
    public static final int VAR_PCH = 0x2000;

    public static final int VAR_0CH = 0x1000;
    public static final int VAR_4CH = 0x1400;
    public static final int VAR_5CH = 0x1500;
    public static final int VAR_6CH = 0x1600;
    public static final int VAR_7CH = 0x1700;
    public static final int VAR_8CH = 0x1800;
    public static final int VAR_9CH = 0x1900;
    public static final int VAR_ACH = 0x1a00;
    public static final int VAR_BCH = 0x1b00;
    public static final int VAR_CCH = 0x1c00;
    public static final int VAR_DCH = 0x1d00;
    public static final int VAR_ECH = 0x1e00;
    public static final int VAR_FCH = 0x1f00;

    public static final int VAR_1RCH = 0x2100;
    public static final int VAR_2RCH = 0x2200;
    public static final int VAR_3RCH = 0x2300;
    public static final int VAR_4RCH = 0x2400;
    public static final int VAR_VF1 = 0x3100;
    public static final int VAR_VF2 = 0x32000;
    public static final int VAR_VF3 = 0x3300;
    public static final int VAR_VF4 = 0x3400;
    public static final int VAR_VPGL = 0x3500;
    public static final int VAR_VPGH = 0x3600;
    public static final int VAR_CCNUM = 0x4000;
    public static final int VAR_RSCTRT1 = 0x4100;
    public static final int VAR_RSCTRT2 = 0x4200;
    public static final int VAR_RSCTRT3 = 0x4300;
    public static final int VAR_RSCTRT1P = 0x4400;
    public static final int VAR_RSCTRT2P = 0x4500;
    public static final int VAR_RSCTRT3P = 0x4600;
    public static final int VAR_RSCTPT1 = 0x4700;
    public static final int VAR_RSCTPT2 = 0x4800;
    public static final int VAR_RSCTPT3 = 0x3900;
    public static final int VAR_RSCTPT1P = 0x5000;
    public static final int VAR_RSCTPT2P = 0x5100;
    public static final int VAR_RSCTPT3P = 0x5200;
    public static final int VAR_CHECKSUM_START = 0x6000;
    public static final int VAR_CHECKSUM_SET = 0x6100;    

    static {
        _all.put("#NONE", VAR_NONE);
        _all.put("#VL", VAR_VL);
        _all.put("#VH", VAR_VH);
        _all.put("#GL", VAR_GL);
        _all.put("#GH", VAR_GH);
        _all.put("#CH", VAR_CH);
        _all.put("#1CH", VAR_1CH);
        _all.put("#2CH", VAR_2CH);
        _all.put("#3CH", VAR_3CH);
        _all.put("#PCH", VAR_PCH);

        _all.put("#0CH", VAR_0CH);
        _all.put("#4CH", VAR_4CH);
        _all.put("#5CH", VAR_5CH);
        _all.put("#6CH", VAR_6CH);
        _all.put("#7CH", VAR_7CH);
        _all.put("#8CH", VAR_8CH);
        _all.put("#9CH", VAR_9CH);
        _all.put("#ACH", VAR_ACH);
        _all.put("#BCH", VAR_BCH);
        _all.put("#CCH", VAR_CCH);
        _all.put("#DCH", VAR_DCH);
        _all.put("#ECH", VAR_ECH);
        _all.put("#FCH", VAR_FCH);

        _all.put("#1RCH", VAR_1RCH);
        _all.put("#2RCH", VAR_2RCH);
        _all.put("#3RCH", VAR_3RCH);
        _all.put("#4RCH", VAR_4RCH);
        _all.put("#VF1", VAR_VF1);
        _all.put("#VF2", VAR_VF2);
        _all.put("#VF3", VAR_VF3);
        _all.put("#VF4", VAR_VF4);
        _all.put("#VPGL", VAR_VPGL);
        _all.put("#VPGH", VAR_VPGH);
        _all.put("#RSCTRT1", VAR_RSCTRT1);
        _all.put("#RSCTRT2", VAR_RSCTRT2);
        _all.put("#RSCTRT3", VAR_RSCTRT3);
        _all.put("#RSCTRT1P", VAR_RSCTRT1P);
        _all.put("#RSCTRT2P", VAR_RSCTRT2P);
        _all.put("#RSCTRT3P", VAR_RSCTRT3P);
        _all.put("#RSCTPT1", VAR_RSCTPT1);
        _all.put("#RSCTPT2", VAR_RSCTPT2);
        _all.put("#RSCTPT3", VAR_RSCTPT3);
        _all.put("#RSCTPT1P", VAR_RSCTPT1P);
        _all.put("#RSCTPT2P", VAR_RSCTPT2P);
        _all.put("#RSCTPT3P", VAR_RSCTPT3P);

        for (String text : _all.keySet()) {
            Integer code = _all.get(text);
            _reverse.put(code, text);
        }
    }
    
    public static int byName(String text) {
        Integer code = _all.get(text);
        if (code != null) {
            return code.intValue();
        }
        return -1;
    }
    
    public static String byNumber(int code) {
        return _reverse.get(code);
    }

    public static int getValiable(int code, CCParameters params) {
        int channel = params._channel;
        
        switch(code & 0xff00) {
            case CCVariable.VAR_NONE:
                return 0;
 
            case CCVariable.VAR_VL:
                return params._value & 0x7f;
            
            case CCVariable.VAR_VH:
                return (params._value >> 7) & 0x7f;
            
            case CCVariable.VAR_GL:
                return params._gate & 0x7f;
            
            case CCVariable.VAR_GH:
                return (params._gate >> 7) & 0x7f;
            
            case CCVariable.VAR_CH:
                return channel;
            
            case CCVariable.VAR_1CH:
                return 0x10 + channel;

            case CCVariable.VAR_2CH:
                return 0x20 + channel;

            case CCVariable.VAR_3CH:
                return 0x30 + channel;

            case CCVariable.VAR_PCH:
                if (params._rolandPort != CCParameters.UNSET
                 && params._rolandPort >= 0
                 && params._rolandPort <= 3) {
                    return params._rolandPort * 0x10 + channel;
                }
                return channel;

            case CCVariable.VAR_4CH:
                return 0x40 + channel;

            case CCVariable.VAR_5CH:
                return 0x50 + channel;

            case CCVariable.VAR_6CH:
                return 0x60 + channel;

            case CCVariable.VAR_7CH:
                return 0x70 + channel;

            case CCVariable.VAR_8CH:
                return 0x80 + channel;

            case CCVariable.VAR_9CH:
                return 0x90 + channel;

            case CCVariable.VAR_ACH:
                return 0xA0 + channel;

            case CCVariable.VAR_BCH:
                return 0xB0 + channel;

            case CCVariable.VAR_CCH:
                return 0xC0 + channel;

            case CCVariable.VAR_DCH:
                return 0xD0 + channel;

            case CCVariable.VAR_ECH:
                return 0xE0 + channel;

            case CCVariable.VAR_FCH:
                return 0xF0 + channel;

            case CCVariable.VAR_VF1:
                return params._value & 0x0f;

            case CCVariable.VAR_VF2:
                return (params._value>>4) & 0x0f;

            case CCVariable.VAR_VF3:
                return (params._value>>8) & 0x0f;

            case CCVariable.VAR_VF4:
                return (params._value>>12) & 0x0f;

            case CCVariable.VAR_VPGL:
                return (params._value + params._gate) & 0x0f;

            case CCVariable.VAR_VPGH:
                return ((params._value + params._gate) >> 7) & 0x0f;

            case CCVariable.VAR_RSCTRT1:
            case CCVariable.VAR_RSCTRT2:
            case CCVariable.VAR_RSCTRT3:
                throw new IllegalArgumentException("RSCTRT1, RSCTRT2, RSCTRT3 not supported.");
                //break;
            case CCVariable.VAR_RSCTRT1P:
            case CCVariable.VAR_RSCTRT2P:
            case CCVariable.VAR_RSCTRT3P:
                throw new IllegalArgumentException("RSCTRT1P, RSCTRT2P, RSCTRT3P not supported.");
                //break;
            case CCVariable.VAR_RSCTPT1:
            case CCVariable.VAR_RSCTPT2:
            case CCVariable.VAR_RSCTPT3:
                throw new IllegalArgumentException("RSCTPT1, RSCTPT2, RSCTPT3 not supported.");
                 //break;
            case CCVariable.VAR_RSCTPT1P:
            case CCVariable.VAR_RSCTPT2P:
            case CCVariable.VAR_RSCTPT3P:
                throw new IllegalArgumentException("RSCTPT1P, RSCTPT2P, RSCTPT3P not supported.");
                
            case CCVariable.VAR_CHECKSUM_SET:
            case CCVariable.VAR_CHECKSUM_START:
                throw new IllegalArgumentException("Should be handle Without this function");
                
            case CCVariable.VAR_1RCH:
            case CCVariable.VAR_2RCH:
            case CCVariable.VAR_3RCH:
            case CCVariable.VAR_4RCH:
                throw new IllegalArgumentException("1RCH, 2RCH, 3RCH, 4RCH not supported.");
                //break;
        }
        throw new IllegalArgumentException("something wrong " + Integer.toHexString(code) + " " + byNumber(code));
    }
}
