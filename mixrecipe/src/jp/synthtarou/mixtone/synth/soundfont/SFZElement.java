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
package jp.synthtarou.mixtone.synth.soundfont;

import java.util.ArrayList;
import java.util.List;

import jp.synthtarou.mixtone.synth.soundfont.table.XTTable;
import jp.synthtarou.mixtone.synth.soundfont.table.XTHeader;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class SFZElement extends XTTable {
    String _type;
    
    public SFZElement(String type) {
        super();
        _type = type;
        if (_genMaster == null) {
            _genMaster = XTGenOperatorMaster.getMaster();
        }
    }
    
    public XTRow get(int x) {
        try {
            return super.get(x);
        }catch(Throwable ex) {
        }
        return null;
    }
        
    public static final int LIST_TYPE = 0;
    public static final int LIST_COUNT = 1;
        
    public static class SFZElement_LIST extends SFZElement {
        List<SFZElement> _childCache;
        
        public SFZElement_LIST(RiffChunk riff, String type) {
            super(type);
            XTHeader header = getHeader();
            header.add("type");
            header.add("count");
            newRow();
            
            _childCache = new ArrayList<>();
        }

        public void addChild(SFZElement e) {
            _childCache.add(e);
        }

        public List<SFZElement> getChildren() {
            return _childCache;
        }
    }

    public static final int RIFF_TYPE = LIST_TYPE;
    public static final int RIFF_COUNT = LIST_COUNT;

    public static class SFZElement_RIFF extends SFZElement_LIST {
        public SFZElement_RIFF(RiffChunk riff, String type) {
            super(riff, type);
        }
    }

    public static final int IFIL_MAJOR = 0;
    public static final int IFIL_MINOR = 1;
    
    public static class SFZElement_ifil extends SFZElement{
        public SFZElement_ifil(RiffChunk riff) {
            super("ifil");

            XTHeader header = getHeader();
            header.add("major");
            header.add("minor");
            XTRow row = newRow();
            row.setColumn(0, riff.readDWORD());
            row.setColumn(1, riff.readDWORD());
        }
    }

    public static final int ISNG_NAME = 0;

    public static class SFZElement_isng extends SFZElement {
        public SFZElement_isng(RiffChunk riff) {
            super("isng");
    
            XTHeader header = getHeader();
            header.add("name");
            XTRow row = newRow();
            row.setColumn(0, riff.readText());
        }
    }
    
    public static final int INAME_NAME = 0;

    public static class SFZElement_INAM extends SFZElement {
        public SFZElement_INAM(RiffChunk riff) {
            super("INAM");

            XTHeader header = getHeader();
            header.add("name");
            XTRow row = newRow();
            row.setColumn(0, riff.readText());
        }
    }
    
    public static final int IROM_NAME = 0;

    public static class SFZElement_irom extends SFZElement {
        public SFZElement_irom(RiffChunk riff) {
            super("irom");
            XTHeader header = getHeader();
            header.add("name");
            XTRow row = newRow();
            row.setColumn(0, riff.readText());
        }
    }
    
    public static final int IVER_MAJOR = 0;
    public static final int IVER_MINOR = 1;

    public static class SFZElement_iver extends SFZElement{
        public SFZElement_iver(RiffChunk riff) {
            super("iver");
            XTHeader header = getHeader();
            header.add("major");
            header.add("minor");
            XTRow row = newRow();
            row.setColumn(0, riff.readDWORD());
            row.setColumn(1, riff.readDWORD());
        }
    }
    
    public static final int ICRD_DATE = 0;

    public static class SFZElement_ICRD extends SFZElement {
        public SFZElement_ICRD(RiffChunk riff) {
            super("ICRD");
            XTHeader header = getHeader();
            header.add("date");
            XTRow row = newRow();
            row.setColumn(0, riff.readText());
        }
    }

    public static final int IENG_NAME = 0;

    public static class SFZElement_IENG extends SFZElement {
        public SFZElement_IENG(RiffChunk riff) {
            super("IENG");
            XTHeader header = getHeader();
            header.add("name");
            XTRow row = newRow();
            row.setColumn(0, riff.readText());
        }
    }

    public static final int IPRD_NAME = 0;

    public static class SFZElement_IPRD extends SFZElement {
        public SFZElement_IPRD(RiffChunk riff) {
            super("IPRD");
            XTHeader header = getHeader();
            header.add("name");
            XTRow row = newRow();
            row.setColumn(0, riff.readText());
        }
    }

    public static final int ICOP_NAME = 0;

    public static class SFZElement_ICOP extends SFZElement {
        public SFZElement_ICOP(RiffChunk riff) {
            super("ICOP");
            XTHeader header = getHeader();
            header.add("name");
            XTRow row = newRow();
            row.setColumn(0, riff.readText());
        }
    }

    public static final int ICMT_TEXT = 0;

    public static class SFZElement_ICMT extends SFZElement {
        public SFZElement_ICMT(RiffChunk riff) {
            super("ICMT");
            XTHeader header = getHeader();
            header.add("text");
            XTRow row = newRow();
            row.setColumn(0, riff.readText());
        }
    }

    public static final int ISFT_NAME = 0;

    public static class SFZElement_ISFT extends SFZElement {
        public SFZElement_ISFT(RiffChunk riff) {
            super("ISFT");
            XTHeader header = getHeader();
            header.add("name");
            XTRow row = newRow();
            row.setColumn(0, riff.readText());
        }
    }
    
    public static class SFZElement_smpl extends SFZElement {
        int _offset;
        int _length;
        byte[] _data;
        
        public SFZElement_smpl(RiffChunk riff) {
            super("smpl");
            _offset = riff._chunkDataOffset;
            _length = riff.remainByteLength();
            _data = riff._chunkData;
        }
        
        public int size() {
            return _length / 2;
        }
        
        public int getSample16(int pos) {
            int b1 = _data[_offset + pos*2] & 0xff;
            int b2 = _data[_offset + pos*2+1] & 0xff;
            int x = b2 << 8  | b1;
            if ((b2 & 0x80) != 0){
                x |= 0xffff0000;
            }
            return x;
        }
    }

    public static class SFZElement_sm24 extends SFZElement {
        int _offset;
        int _length;
        byte[] _data;
        
        public SFZElement_sm24(RiffChunk riff) {
            super("smpl");
            _offset = riff._readX;
            _length = riff.remainByteLength();
            _data = riff._chunkData;
        }
        
        public int size() {
            return _length;
        }
        
        public int getSample24Add(int pos) {
            int x = _data[_offset + pos] & 0xff;
            return x;
        }
    }

    public static final int PHDR_NAME = 0;
    public static final int PHDR_PRESETNO = 1;
    public static final int PHDR_BANK = 2;
    public static final int PHDR_BAGINDEX = 3;
    public static final int PHDR_LIBRARY = 4;
    public static final int PHDR_GENRE = 5;
    public static final int PHDR_MORTH = 6;

    public static class SFZElement_phdr extends SFZElement {
        public SFZElement_phdr(RiffChunk riff) {
            super("phdr");

            XTHeader header = getHeader();
            header.add("name");
            header.add("presetno");
            header.add("bank");
            header.add("bagIndex");
            header.add("library");
            header.add("genre");
            header.add("morth");
            while (riff.remainByteLength() > 0) {
                XTRow row = newRow();
                row.setColumn(0, riff.readZSTR(20));
                row.setColumn(1, riff.readWORD());
                row.setColumn(2, riff.readWORD());
                row.setColumn(3, riff.readWORD());
                row.setColumn(4, riff.readDWORD());
                row.setColumn(5, riff.readDWORD());
                row.setColumn(6, riff.readDWORD());
            }
        }
    }

    public static final int PBAG_PGENINDEX = 0;
    public static final int PBAG_PMODINDEX = 1;

    public static class SFZElement_pbag extends SFZElement {
        public SFZElement_pbag(RiffChunk riff) {
            super("pbag");

            XTHeader header = getHeader();
            header.add("pmodIndex");
            header.add("pgenIndex");

            while (riff.remainByteLength() > 0) {
                XTRow row = newRow();
                row.setColumn(0, riff.readWORD());
                row.setColumn(1, riff.readWORD());
            }
        }
    }

    public static final int PMOD_SRCOPER = 0;
    public static final int PMOD_DESTOPER = 1;
    public static final int PMOD_MODAMOUNT = 2;
    public static final int PMOD_AMTSRCOPER = 3;
    public static final int PMOD_MODTRANSOPER = 4;

    public static class SFZElement_pmod extends SFZElement {
        public SFZElement_pmod(RiffChunk riff) {
            super("pmod");

            XTHeader header = getHeader();
            header.add("srcOper");
            header.add("destOper");
            header.add("modAmount");
            header.add("amtSrcOper");
            header.add("modTransOper");

            while (riff.remainByteLength() > 0) {
                XTRow row = newRow();
                row.setColumn(0, riff.readWORD());
                row.setColumn(1, riff.readWORD());
                int modAmount = riff.readWORD();
                if ((modAmount & 0x8000) != 0) {
                    modAmount = 0xffff0000 | modAmount;
                }
                row.setColumn(2, modAmount);
                row.setColumn(3, riff.readWORD());
                row.setColumn(4, riff.readWORD());
            }
        }
    }

    static XTGenOperatorMaster _genMaster = null;
    
    public static final int PGEN_GENOPER = 0;
    public static final int PGEN_GENAMOUNT = 1;
    public static final int PGEN_GENOPER_MEAN = 2;
    public static final int PGEN_GENAMOUNT_MEAN = 3;

    public static class SFZElement_pgen extends SFZElement {

        public SFZElement_pgen(RiffChunk riff) {
            super("pgen");
            XTHeader header = getHeader();
            header.add("genOper");
            header.add("genAmount");
            header.add("genOper_Mean");
            header.add("genAmount_Mean");

            while (riff.remainByteLength() > 0) {
                XTRow row = newRow();
                int genOper = riff.readWORD();
                int genAmount = riff.readWORD();
                row.setColumn(0, genOper);
                row.setColumn(1, genAmount);

                XTGenOperatorMasterEntry operator = _genMaster.getEntry(genOper);

                if (operator != null) {
                    String name = operator._name;
                    String unit = operator._unit;
                    if (unit == null) {
                        unit = "";
                    }
                    else {
                        unit = "(" + unit +")";
                    }
                    row.setColumn(2, name);
                    Double d = operator.asParameter(genAmount);
                    if (genOper == 43 || genOper == 44) {
                        int hi = (d.intValue() >> 8) & 0xff;
                        int lo = d.intValue() & 0xff;
                        row.setColumn(3, "" + lo + " -> " + hi + unit);
                    }
                    else {
                        row.setColumn(3, d + unit);
                    }
                }
            }
        }
        
        public double getAmount(int oper) {
            XTGenOperatorMasterEntry operator = _genMaster.getEntry(oper);
            if (operator == null) {
                return Double.NaN;
            }

            for (int i = 0; i < size(); ++ i) {
                XTRow row = get(i);
                int genOper = row.intColumn(PGEN_GENOPER);
                int genAmount = row.intColumn(PGEN_GENAMOUNT);
                
                if (genOper != oper) {
                    continue;
                }

                String name = operator._name;
                String unit = operator._unit;
                if (unit == null) {
                    unit = "";
                }
                else {
                    unit = "(" + unit +")";
                }
                row.setColumn(2, name);
                Double d = operator.asParameter(genAmount);
                return d;
            }
            
            return operator._initial;
        }
    }

    public static final int INST_NAME = 0;
    public static final int INST_BAGINDEX = 1;

    public static class SFZElement_inst extends SFZElement {
        public SFZElement_inst(RiffChunk riff) {
            super("");

            XTHeader header = getHeader();
            header.add("name");
            header.add("bagIndex");
            
            while (riff.remainByteLength() > 0) {
                XTRow row = newRow();
                row.setColumn(0, riff.readZSTR(20));
                row.setColumn(1, riff.readWORD());
            }
        }
    }

    public static final int IBAG_IGENINDEX = 0;
    public static final int IBAG_IMODINDEX = 1;
    
    public static class SFZElement_ibag extends SFZElement {
        public SFZElement_ibag(RiffChunk riff) {
            super("ibag");

            XTHeader header = getHeader();
            header.add("igenIndex");
            header.add("imodIndex");

            while (riff.remainByteLength() > 0) {
                XTRow row = newRow();
                row.setColumn(0, riff.readWORD());
                row.setColumn(1, riff.readWORD());
            }
        }
    }

    public static final int IMOD_SRCOPER = 0;
    public static final int IMOD_DESTOPER = 1;
    public static final int IMOD_MODAMOUNT = 2;
    public static final int IMOD_AMTSRCOPER = 3;
    public static final int IMOD_MODTRANSOPER  = 4;

    public static class SFZElement_imod extends SFZElement {
        public SFZElement_imod(RiffChunk riff) {
            super("imod");

            XTHeader header = getHeader();
            header.add("srcOper");
            header.add("destOper");
            header.add("modAmount");
            header.add("amtSrcOper");
            header.add("modTransOper");

            while (riff.remainByteLength() > 0) {
                XTRow row = newRow();
                row.setColumn(0, riff.readWORD());
                row.setColumn(1, riff.readWORD());
                row.setColumn(2, riff.readWORD());
                row.setColumn(3, riff.readWORD());
                row.setColumn(4, riff.readWORD());
            }
        }
    }

    public static final int IGEN_GENOPER = 0;
    public static final int IGEN_GENAMOUNT = 1;
    public static final int IGEN_GENOPER_MEAN = 1;
    public static final int IGEN_GENMAMOUNT_MEAN = 2;

    public static class SFZElement_igen extends SFZElement {
        public SFZElement_igen(RiffChunk riff) {
            super("igen");

            XTHeader header = getHeader();
            header.add("genOper");
            header.add("genAmount");
            header.add("genOper_Mean");
            header.add("genAmount_Mean");

            while (riff.remainByteLength() > 0) {
                XTRow row = newRow();
                int genOper =  riff.readWORD();
                int genAmount =  riff.readWORD();
                
                row.setColumn(0, genOper);
                row.setColumn(1, genAmount);

                XTGenOperatorMasterEntry operator = _genMaster.getEntry(genOper);

                if (operator != null) {
                    String name = operator._name;
                    String unit = operator._unit;
                    if (unit == null) {
                        unit = "";
                    }
                    else {
                        unit = "(" + unit + ")";
                    }
                    row.setColumn(2, name);
                    Double d = operator.asParameter(genAmount);
                    if (genOper == 43 || genOper == 44) {
                        int hi = (d.intValue() >> 8) & 0xff;
                        int lo = d.intValue() & 0xff;
                        row.setColumn(3, "" + lo + " -> " + hi + unit);
                    }
                    else {
                        row.setColumn(3, d + unit);
                    }
                }
            }
        }
    }

    public static final int SHDR_NAME = 0;
    public static final int SHDR_START = 1;
    public static final int SHDR_END = 2;
    public static final int SHDR_LOOPSTART = 3;
    public static final int SHDR_LOOPEND = 4;
    public static final int SHDR_SAMPLERATE = 5;
    public static final int SHDR_ORIGINALPITCH = 6;
    public static final int SHDR_PITCHCORRECTION = 7;
    public static final int SHDR_SAMPLELINK = 8;
    public static final int SHDR_TYPE = 9;

    public static class SFZElement_shdr extends SFZElement {
        public SFZElement_shdr(RiffChunk riff) {
            super("shdr");

            XTHeader header = getHeader();
            header.add("name");
            header.add("start");
            header.add("end");
            header.add("loopstart");
            header.add("loopend");
            header.add("sampleRate");
            header.add("originalPitch");
            header.add("pitchCorrection");
            header.add("sampleLink");
            header.add("type");

            while (riff.remainByteLength() > 0) {
                XTRow row = newRow();
                row.setColumn(0, riff.readZSTR(20));
                row.setColumn(1, riff.readDWORD());
                row.setColumn(2, riff.readDWORD());
                row.setColumn(3, riff.readDWORD());
                row.setColumn(4, riff.readDWORD());
                row.setColumn(5, riff.readDWORD());
                row.setColumn(6, riff.read());
                int correction = riff.read();
                if ((correction & 0x80) != 0) {
                    correction |= 0xffffff00;
                }
                row.setColumn(7, correction);
                row.setColumn(8, riff.readWORD());
                row.setColumn(9, riff.readWORD());
            }
       }
    }
    
}
