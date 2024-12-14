/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.synth.soundfont;

import java.util.ArrayList;
import java.util.List;
import jp.synthtarou.mixtone.listmodel.TextListForDebug;
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

        @Override
        public void getDump(TextListForDebug dump) {
            get(0).setColumn(LIST_TYPE ,_type);
            get(0).setColumn(LIST_COUNT , _childCache.size());
            super.getDump(dump);
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
        byte[] _data;
        
        public SFZElement_smpl(RiffChunk riff) {
            super("smpl");
            _data = riff.readData(-1);
        }
        
        public int size() {
            return _data.length / 2;
        }
        
        public int getSample16(int pos) {
            int b1 = _data[pos*2] & 0xff;
            int b2 = _data[pos*2+1] & 0xff;
            int x = b2 << 8  | b1;
            if ((b2 & 0x80) != 0){
                x |= 0xffff0000;
            }
            return x;
        }
    }

    public static class SFZElement_sm24 extends SFZElement {
        byte[] _data;

        public SFZElement_sm24(RiffChunk riff) {
            super("sm24");
            _data = riff.readData(-1);
        }

        public int size() {
            return _data.length;
        }
        
        public int getSample24Add(int pos) {
            int x = _data[pos] & 0xff;
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
    public static final int PHDR_BAGINDEX_TABLE = 7;

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
            header.add("bagIndex_table");
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
    public static final int PBAG_PGENINDEX_TABLE = 2;
    public static final int PBAG_PMODINDEX_TABLE = 3;

    public static class SFZElement_pbag extends SFZElement {
        public SFZElement_pbag(RiffChunk riff) {
            super("pbag");

            XTHeader header = getHeader();
            header.add("pmodIndex");
            header.add("pgenIndex");
            header.add("pmodIndex_table");
            header.add("pgenIndex_table");

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

    static XTGenOperatorMaster operatorList = new XTGenOperatorMaster();

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

                XTGenOperator operator = operatorList.get(genOper);

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
            XTGenOperator operator = operatorList.get(oper);
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
    public static final int INST_BAGINDEX_TABLE = 2;

    public static class SFZElement_inst extends SFZElement {
        public SFZElement_inst(RiffChunk riff) {
            super("");

            XTHeader header = getHeader();
            header.add("name");
            header.add("bagIndex");
            header.add("bagIndex_table");
            
            while (riff.remainByteLength() > 0) {
                XTRow row = newRow();
                row.setColumn(0, riff.readZSTR(20));
                row.setColumn(1, riff.readWORD());
            }
        }
    }

    public static final int IBAG_IGENINDEX = 0;
    public static final int IBAG_IMODINDEX = 1;
    public static final int IBAG_IGENINDEX_TABLE = 2;
    public static final int IBAG_IMODINDEX_TABLE = 3;
    
    public static class SFZElement_ibag extends SFZElement {
        public SFZElement_ibag(RiffChunk riff) {
            super("ibag");

            XTHeader header = getHeader();
            header.add("igenIndex");
            header.add("imodIndex");
            header.add("igenIndex_table");
            header.add("imodIndex_table");

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

                XTGenOperator operator = operatorList.get(genOper);

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
    
    public static SFZElement parseSingle(RiffChunk riff) {
        if (riff._listDataElement != null) {
            return riff._listDataElement;
        }
        String chunk = riff._chunkId;
        if (chunk == null) {
            return null;
        }

        SFZElement element =  null;
        if (chunk.equals("ifil")) {
            element = (new SFZElement.SFZElement_ifil(riff));
        } else if (chunk.equals("isng")) {
            element = (new SFZElement.SFZElement_isng(riff));
        } else if (chunk.equals("INAM")) {
            element = (new SFZElement.SFZElement_INAM(riff));
        } else if (chunk.equals("irom")) {
            element = (new SFZElement.SFZElement_irom(riff));
        } else if (chunk.equals("iver")) {
            element = (new SFZElement.SFZElement_iver(riff));
        } else if (chunk.equals("ICRD")) {
            element = (new SFZElement.SFZElement_ICRD(riff));
        } else if (chunk.equals("IENG")) {
            element = (new SFZElement.SFZElement_IENG(riff));
        } else if (chunk.equals("IPRD")) {
            element = (new SFZElement.SFZElement_IPRD(riff));
        } else if (chunk.equals("ICOP")) {
            element = (new SFZElement.SFZElement_ICOP(riff));
        } else if (chunk.equals("ICMT")) {
            element = (new SFZElement.SFZElement_ICMT(riff));
        } else if (chunk.equals("ISFT")) {
            element = (new SFZElement.SFZElement_ISFT(riff));
        } else if (chunk.equals("smpl")) {
            element = (new SFZElement.SFZElement_smpl(riff));
        } else if (chunk.equals("sm24")) {
            element = (new SFZElement.SFZElement_sm24(riff));
        } else if (chunk.equals("phdr")) {
            element = (new SFZElement.SFZElement_phdr(riff));
        } else if (chunk.equals("pbag")) {
            element = (new SFZElement.SFZElement_pbag(riff));
        } else if (chunk.equals("pmod")) {
            element = (new SFZElement.SFZElement_pmod(riff));
        } else if (chunk.equals("pgen")) {
            element = (new SFZElement.SFZElement_pgen(riff));
        } else if (chunk.equals("inst")) {
            element = (new SFZElement.SFZElement_inst(riff));
        } else if (chunk.equals("ibag")) {
            element = (new SFZElement.SFZElement_ibag(riff));
        } else if (chunk.equals("imod")) {
            element = (new SFZElement.SFZElement_imod(riff));
        } else if (chunk.equals("igen")) {
            element = (new SFZElement.SFZElement_igen(riff));
        } else if (chunk.equals("shdr")) {
            element = (new SFZElement.SFZElement_shdr(riff));
        } else {
            element = (new SFZElement("unknown") {
                @Override
                public void getDump(TextListForDebug dump) {
                    dump.add("Unknown");
                }
            });
        }

        riff._listDataElement = element;
        return element;
    }
}
