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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCOrder {
    static final HashMap<String, Integer> _all = new HashMap();
    static final HashMap<Integer, String> _reverse = new HashMap();

    public static final int ORDER_RPN = 0x100;
    public static final int ORDER_NRPN = 0x200;
    public static final int ORDER_PB = 0x300;
    public static final int ORDER_CP = 0x400;
    public static final int ORDER_PKP = 0x500;
    public static final int ORDER_CC = 0x600;
    public static final int ORDER_SYSEX = 0x700;
    
    static {
        _all.put("@RPN", ORDER_RPN);
        _all.put("@NRPN", ORDER_NRPN);
        _all.put("@PB", ORDER_PB);
        _all.put("@CP", ORDER_CP);
        _all.put("@PKP", ORDER_PKP);
        _all.put("@CC", ORDER_CC);
        _all.put("@SYSEX", ORDER_SYSEX);
        
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
    
    public static List<String> split(String text) {
        ArrayList<String> list = new ArrayList<>();
        
        int readX = 0;
        while (readX < text.length()) {
            char ch = text.charAt(readX);

            if (ch == '[') {
                list.add("[");
                readX ++;
                continue;
            }
            
            if (ch == ']') {
                list.add("]");
                readX ++;
                continue;
            }
            
            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                readX ++;
                continue;
            }

            StringBuffer segment = new StringBuffer();
            while (ch != ',' && ch != ' ') {
                segment.append(ch);
                readX ++;
                if (readX >= text.length()) {
                    break;
                }
                ch = text.charAt(readX);
            }
            
            String str = MXUtil.shrinkText(segment.toString());
            if (str.length() > 0) {
                list.add(str);
            }
        }
        return list;
    }
    
    public CCMessage interpretation(String orderText) {
        List<String> list = split(orderText);
        List<CCMessage> result = new ArrayList<>();
 
        String gate, var, varLow;
        String msb, lsb, datamsb, datalsb;

        ArrayList<String> output = new ArrayList();

        for (int sx = 0; sx < list.size(); ++ sx) {
            String order = list.get(sx);
            int orderN = CCOrder.byName(order);
            if (orderN < 0) {
                output.add(order);
                continue;
            }
            
            switch(orderN) {
                case CCOrder.ORDER_PB:
                    var = list.get(++ sx);
                    varLow = list.get(++ sx);
                    output.add("#ECH");
                    output.add(var);
                    output.add(varLow);
                    break;
                    
                case CCOrder.ORDER_CP:
                    var = list.get(++ sx);
                    output.add("#DCH");
                    output.add(var);
                    output.add("#NONE");
                    break;
                
                case CCOrder.ORDER_PKP:
                    gate = list.get(++ sx);
                    var = list.get(++ sx);
                    output.add("#ACH");
                    output.add(gate);
                    output.add(var);
                    break;
                
                case CCOrder.ORDER_CC:
                    gate = list.get(++ sx);
                    var = list.get(++ sx);
                    output.add("#BCH");
                    output.add(gate);
                    output.add(var);
                    break;
                
                case CCOrder.ORDER_SYSEX:
                    while(sx + 1 < list.size()) {
                        String next = list.get(++ sx);
                        output.add(next);
                        if (MXUtil.numberFromText(next) == 0xf7) {
                            break;
                        }
                    }
                    break;

                case CCOrder.ORDER_RPN:
                    msb = list.get(++ sx);
                    output.add("#BCH");
                    output.add("101");
                    output.add(msb);
                    
                    lsb = list.get(++ sx);
                    output.add("#BCH");
                    output.add("100");
                    output.add(lsb);
                    
                    datamsb = list.get(++ sx); 
                    output.add("#BCH");
                    output.add("6");
                    output.add(datamsb);
                    
                    datalsb = list.get(++ sx); 
                    output.add("#BCH");
                    output.add("38");
                    output.add(datalsb);
                    break;
                    
                case CCOrder.ORDER_NRPN:
                    msb = list.get(++ sx);
                    output.add("#BCH");
                    output.add("99");
                    output.add(msb);
      
                    lsb = list.get(++ sx);
                    output.add("#BCH");
                    output.add("98");
                    output.add(lsb);
                    
                    datamsb = list.get(++ sx);
                    output.add("#BCH");
                    output.add("6");
                    output.add(datamsb);
                    
                    datalsb = list.get(++ sx);
                    output.add("#BCH");
                    output.add("38");
                    output.add(datalsb);

                default:
                    break;
            }
        }

        int [] template = new int[output.size()];
        for (int i = 0; i < output.size(); ++ i) {
            String vaName = output.get(i);
            int varNumber = CCVariable.byName(vaName);
            if (varNumber >= 0) {
                template[i] = varNumber;
                continue;
            }
            try {
                int x = MXUtil.numberFromText(vaName, true);
                template[i] = x;
                continue;
            }catch(NumberFormatException e) {
            }
            throw new IllegalArgumentException(orderText);
        }

        CCMessage message = new CCMessage(new CCFormat(template), new CCParameters());
        return message;
    }
}
