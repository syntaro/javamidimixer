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
import jp.synthtarou.midimixer.libs.common.RangedValue;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMessageFactory {

    static MXTemplateCache _cache = MXTemplateCache.getInstance();

    public static MXMessage createDummy() {
        MXTemplate template = _cache.fromDword(0);
        return template.buildMessage(0, 0, RangedValue.ZERO7, RangedValue.ZERO7);
    }

    public static MXMessage fromClone(MXMessage old) {
        MXMessage msg = (MXMessage) old.clone();
        return msg;
    }

    public static MXMessage fromDWordMessage(int port, int dword) {
        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = dword & 0xff;
        return fromShortMessage(port, status, data1, data2);
    }

    public static MXMessage fromMeta(int port, byte[] data) {
        MXTemplate template = _cache.fromBinary(data);

        MXMessage message = new MXMessage(port, template, 0, RangedValue.ZERO7, RangedValue.ZERO7);
        message.setMetaType(data[1] & 0xff);

        String text = null;
        try {
            text = new String(data, 2, data.length - 2, "ASCII");
            text = new String(data, 2, data.length - 2);
            text = new String(data, 2, data.length - 2, "SJIS");
        } catch (Exception e) {
            e.printStackTrace();
        }

        message._metaText = text;
        message._dataBytes = data;
        return message;
    }

    public static MXMessage fromBinary(int port, byte[] data) {
        try {
            MXTemplate template = _cache.fromBinary(data);
            return template.buildMessage(port, 0, RangedValue.ZERO7, RangedValue.ZERO7);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MXMessage fromShortMessage(int port, int status, int data1, int data2) {
        int dword = (status << 16) | (data1 << 8) | data2;
        MXTemplate template = _cache.fromDword(dword);

        int valueLow = template.getBytePosValue();
        int valueHi = template.getBytePosHiValue();

        int value = 0;

        switch (valueLow) {
            case 0:
                value += status;
                break;
            case 1:
                value += data1;
                break;
            case 2:
                value += data2;
                break;
        }

        switch (valueHi) {
            case 0:
                value += status << 7;
                break;
            case 1:
                value += data1 << 7;
                break;
            case 2:
                value += data2 << 7;
                break;
        }

        int gateLow = template.getBytePosGate();
        int gateHi = template.getBytePosHiGate();
        int gate = 0;

        switch (gateLow) {
            case 0:
                gate += status;
                break;
            case 1:
                gate += data1;
                break;
            case 2:
                gate += data2;
                break;
        }

        switch (gateHi) {
            case 0:
                gate += status << 7;
                break;
            case 1:
                gate += data1 << 7;
                break;
            case 2:
                gate += data2 << 7;
                break;
        }

        int command = status;
        int channel = 0;
        if (status >= 0x80 && status <= 0xef) {
            command = status & 0xf0;
            channel = status & 0x0f;
        }

        if (template.getBytePosHiValue() >= 0) {
            return template.buildMessage(port, channel, RangedValue.new7bit(gate), RangedValue.new14bit(value));
        }

        return template.buildMessage(port, channel, RangedValue.new7bit(gate), RangedValue.new7bit(value));
    }

    public static MXTemplate fromDtext(String text, int channel) {
        if (text == null || text.length() == 0) {
            return null;
        }

        while (text.startsWith(" ")) {
            text = text.substring(1);
        }
        while (text.endsWith(" ")) {
            text = text.substring(0, text.length() - 1);
        }

        if (text.equals(MXTemplate.EXCOMMAND_PROGRAM_INC)) {
            int[] template = {MXTemplate.DTEXT_PROGINC, channel};
            return _cache.fromTemplate(template);
        }
        if (text.equals(MXTemplate.EXCOMMAND_PROGRAM_DEC)) {
            int[] template = {MXTemplate.DTEXT_PROGDEC, channel};
            return _cache.fromTemplate(template);
        }

        try {
            int rpn_msb;
            int rpn_lsb;
            int nrpn_msb;
            int nrpn_lsb;

            char[] line = text.toCharArray();

            char[] word = new char[line.length];
            int wx = 0;

            int readX = 0;
            ArrayList<String> separated = new ArrayList();
            boolean inChecksum = false;

            while (readX < line.length) {
                char ch = line[readX++];
                if (ch == '[') {
                    separated.add("#CHECKYSUM_START");
                    inChecksum = true;
                    continue;
                }
                if (ch == ']') {
                    if (inChecksum) {
                        inChecksum = false;
                        if (wx != 0) {
                            separated.add(new String(word, 0, wx));
                        }
                        separated.add("#CHECKSUM_SET");
                        wx = 0;
                    } else {
                        new Exception("Checksum have not opened").printStackTrace();
                    }
                    continue;
                }
                if (ch == ' ' || ch == '\t' || ch == ',') {
                    if (wx != 0) {
                        separated.add(new String(word, 0, wx));
                    }
                    wx = 0;
                    continue;
                }
                word[wx++] = ch;
            }

            if (wx != 0) {
                separated.add(new String(word, 0, wx));
                wx = 0;
            }

            if (text.contains("@")) {
                ArrayList<String> sepa2 = new ArrayList();
                for (int sx = 0; sx < separated.size(); ++sx) {
                    String str = separated.get(sx);
                    if (str.startsWith("@")) {
                        if (str.equalsIgnoreCase("@PB")) {
                            sepa2.add("#ECH");
                            sepa2.add(separated.get(++sx));
                            sepa2.add(separated.get(++sx));
                        } else if (str.equalsIgnoreCase("@CP")) {
                            sepa2.add("#DCH");
                            sepa2.add(separated.get(++sx));
                            sepa2.add("#NONE");
                        } else if (str.equalsIgnoreCase("@PKP")) {
                            sepa2.add("#ACH");
                            String t = separated.get(++sx);
                            if (t.startsWith("#")) {
                                sepa2.add(t);
                            } else {
                                sepa2.add(t);
                            }
                            sepa2.add(separated.get(++sx));
                        } else if (str.equalsIgnoreCase("@CC")) {
                            sepa2.add("#BCH");
                            String t = separated.get(++sx);
                            sepa2.add(t);
                            sx++;
                            if (separated.size() <= sx) {
                                return null;
                            }
                            sepa2.add(separated.get(sx));
                        } else if (str.equalsIgnoreCase("@SYSEX")) {
                            //THRU (no need recompile)
                        } else if (str.equalsIgnoreCase("@RPN")) {
                            int msb = MXTemplate.readAliasText(separated.get(++sx));
                            int lsb = MXTemplate.readAliasText(separated.get(++sx));
                            int data = MXTemplate.readAliasText(separated.get(++sx));
                            if (separated.size() >= sx + 2) {
                                data = data << 7;
                                data |= MXTemplate.readAliasText(separated.get(++sx));
                            }

                            int[] template = {MXTemplate.DTEXT_RPN, msb, lsb, data};
                            return _cache.fromTemplate(template);
                        } else if (str.equalsIgnoreCase("@NRPN")) {
                            int msb = MXTemplate.readAliasText(separated.get(++sx));
                            int lsb = MXTemplate.readAliasText(separated.get(++sx));
                            int data = MXTemplate.readAliasText(separated.get(++sx));
                            if (separated.size() >= sx + 2) {
                                data = data << 7;
                                data |= MXTemplate.readAliasText(separated.get(++sx));
                            }

                            int[] template = {MXTemplate.DTEXT_NRPN, msb, lsb, data};
                            return _cache.fromTemplate(template);
                        } else {
                            System.out.println("Not Support [" + text + "]");
                            return null;
                        }
                    } else {
                        sepa2.add(str);
                    }
                }
                separated = sepa2;
            }

            // cleanup
            int[] compiled = new int[separated.size()];
            int cx = 0;
            int px = 0;

            for (int sx = 0; sx < separated.size(); ++sx) {
                String str = separated.get(sx);
                int code = MXTemplate.readAliasText(str);
                if (code < 0) {
                    return null;
                }
                compiled[px++] = code;
            }

            MXTemplate temp = _cache.fromTemplate(compiled);
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
