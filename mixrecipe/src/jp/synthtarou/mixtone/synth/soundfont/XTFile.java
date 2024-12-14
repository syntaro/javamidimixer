/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.synth.soundfont;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jp.synthtarou.mixtone.listmodel.TextListForDebug;
import jp.synthtarou.mixtone.listmodel.TheConsole;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;
import jp.synthtarou.mixtone.synth.soundfont.table.XTTable;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTFile {
    public XTFile(File f) throws IOException {
        try {
            RiffChunk riff = new RiffChunk();
            riff.setData(f);
            _storage = new HashMap<>();
            prepareChunkTree(riff);
            prepareRooms(riff);
        }catch(IOException ex) {
            throw ex;
        }catch(Throwable ex) {
            ex.printStackTrace();
        }
    }

    protected void prepareChunkTree(RiffChunk parent) {
        while (parent.remainByteLength() > 0) {
            RiffChunk current = new RiffChunk();
            String chunkId = parent.read4char();
            int chunkSize = parent.readDWORD();
            int remain = parent.remainByteLength();

            current._chunkData = parent._chunkData;
            current._chunkDataOffset = parent._chunkDataOffset + parent._readX;
            current._chunkSize = chunkSize;

            current._chunkId = chunkId;
            current._chunkType = null;

            prepareList(chunkId).add(current);

            if (chunkSize < 0) {
                throw new IllegalStateException("chunkSize was minus");
            }
            if (chunkId.equals("RIFF") || chunkId.equals("LIST")) {
                current._chunkType = parent.read4char();
                current._chunkDataOffset += 4;
                current._chunkSize -= 4;
                prepareChunkTree(current);
                parent.skip(current._chunkSize);
            } else {
                parent.skip(chunkSize);
            }
            parent.addChild(current);
        }
    }

    ArrayList<String> _result = new ArrayList<>();

    public ArrayList<String> getDebugResult() {
        return _result;
    }


    public static String countSample(RiffChunk current) {
        return "";
    }

    HashMap<String, List<RiffChunk>> _storage;

    public List<RiffChunk> listChunk(String type) {
        List<RiffChunk> list = _storage.get(type);
        return list;
    }

    protected List<RiffChunk> prepareList(String type) {
        List<RiffChunk> list = _storage.get(type);
        if (list == null) {
            list = new ArrayList<>();
            _storage.put(type, list);
        }
        return list;
    }

    public void prepareRooms(RiffChunk data) {
        ArrayList<RiffChunk> children = data._children;
        
        if (children == null) {
            return;
        }
        for (RiffChunk seek : children) {
            String id = seek._chunkId;
            String type = seek._chunkType; 
            int size = seek._chunkSize;
            SFZElement struct = SFZElement.parseSingle(seek);
            prepareRooms(seek);
        }
    }
    
    public SFZElement getElement(String id) {
        try {
            List<RiffChunk> list = prepareList(id);
            List<SFZElement> result = new ArrayList<>();
            if (list.size() >= 2) {
                throw new IllegalStateException();
            }
            for (RiffChunk chunk : list)  {
                if (chunk._chunkId.equals(id)) {
                    return chunk._listDataElement;
                }
            }
            return null;
        }catch(Throwable ex) {
            return null;
        }
    }
    
    public void dumpHeader(TheConsole console) {
        console.add("Info File: " +getElement("ifil"));
        console.add("Info Sound Gear: " + getElement("isng"));
        console.add("Info Name: " + getElement("INAM"));
        console.add("Info Hard ROM Ver: " + getElement("irom"));
        console.add("Info Soft Ver: " + getElement("iver"));
        console.add("Info CreatedDate: " + getElement("ICRD"));
        console.add("Info Engenear: " + getElement("IENG"));
        console.add("Info Product: " + getElement("IPRD"));
        console.add("Info Copyright: " + getElement("ICOP"));
        console.add("Info Comment: " + getElement("ICMT"));
        console.add("Info Software: " + getElement("ISFT"));
    }

    public void dumpPreset(TheConsole console) {
        SFZElement _phdr = getElement("phdr");
        SFZElement _pbag = getElement("pbag");
        SFZElement _pmod = getElement("pmod");
        SFZElement _pgen = getElement("pgen");

        if (_phdr == null) {
            console.add("Preset phdr was null");
        }
        if (_pbag == null) {
            console.add("Preset pbag was null");
        }
        if (_pmod == null) {
            console.add("Preset pmod was null");
        }
        if (_pgen == null) {
            console.add("Preset pgen was null");
        }
        if (_phdr == null || _pbag == null || _pmod == null || _pgen == null ){
            return;
        }
        console.add("Preset Length: " + _phdr.size());
        for (int i = 0; i < _phdr.size() - 1; ++ i) {
            XTRow preset = _phdr.get(i);
            XTRow next = _phdr.get(i + 1);

            int presetBagIndex = preset.intColumn(SFZElement.PHDR_BAGINDEX);
            int nextBagIndex = next.intColumn(SFZElement.PHDR_BAGINDEX);
            
            XTTable bagIndexExtra = new XTTable(_pbag, presetBagIndex, nextBagIndex - 1);
            preset.setColumn(SFZElement.PHDR_BAGINDEX_TABLE, bagIndexExtra);

            console.add("Preset[" + i + "]=[" + presetBagIndex +"~" +  (nextBagIndex-1)  +"]" + preset.getDump());

            for (int bagIndex = presetBagIndex; bagIndex < nextBagIndex; ++ bagIndex) {
                XTRow bag = _pbag.get(bagIndex);
                XTRow bagNext = _pbag.get(bagIndex+1);
                
                int modFrom = bag.intColumn(SFZElement.PBAG_PMODINDEX);
                int modTo = bagNext.intColumn(SFZElement.PBAG_PMODINDEX) - 1;
                if (modFrom <= modTo) {
                    XTTable modIndexExtra = new XTTable(_pmod, modFrom, modTo);
                    bag.setColumn(SFZElement.PBAG_PMODINDEX_TABLE, modIndexExtra);
                }

                int genFrom = bag.intColumn(SFZElement.PBAG_PGENINDEX);
                int genTo = bagNext.intColumn(SFZElement.PBAG_PGENINDEX) - 1;
                
                if (genFrom <= genTo) {
                    XTTable genIndexExtra = new XTTable(_pgen, genFrom, genTo);
                    bag.setColumn(SFZElement.PBAG_PGENINDEX_TABLE, genIndexExtra);
                }
            }
        }
    }

    public void dumpInstrument(TheConsole console) {
        SFZElement _inst = getElement("inst");
        SFZElement _ibag = getElement("ibag");
        SFZElement _imod = getElement("imod");
        SFZElement _igen = getElement("igen");

        if (_inst == null) {
            console.add("Instrument inst was null");
        }
        if (_ibag == null) {
            console.add("Instrument ibag was null");
        }
        if (_imod == null) {
            console.add("Instrument imod was null");
        }
        if (_igen == null) {
            console.add("Instrument igen was null");
        }
        if (_inst == null || _ibag == null || _imod == null || _igen == null ){
            return;
        }

        console.add("Instrument Length: " + _inst.size());

        for (int i = 0; i < _inst.size() - 1; ++ i) {
            XTRow inst = _inst.get(i);
            XTRow instNext = _inst.get(i + 1);

            int instBagIndex = inst.intColumn(SFZElement.INST_BAGINDEX);
            int nextBagIndex = instNext.intColumn(SFZElement.INST_BAGINDEX);

            XTTable bagIndexExtra = new XTTable(_ibag, instBagIndex, nextBagIndex - 1);
            inst.setColumn(SFZElement.INST_BAGINDEX_TABLE, bagIndexExtra);
            
            console.add("Inst[" + i + "]=[" + instBagIndex +"~" +  (nextBagIndex-1)  +"]" + inst.getDump());

            for (int bagIndex = instBagIndex; bagIndex < nextBagIndex; ++ bagIndex) {
                XTRow bag = _ibag.get(bagIndex);
                XTRow bagNext = _ibag.get(bagIndex+1);

                console.add("Room["+ bagIndex +"] of " + inst.textColumn(SFZElement.INST_NAME));
                
                int modFrom = bag.intColumn(SFZElement.IBAG_IMODINDEX);
                int modTo = bagNext.intColumn(SFZElement.IBAG_IMODINDEX) - 1;
                
                XTTable modIndexExtra = new XTTable(_imod, modFrom, modTo);
                bag.setColumn(SFZElement.IBAG_IMODINDEX_TABLE, modIndexExtra);
                
                for (int mod = modFrom; mod <= modTo; ++ mod) {
                    XTRow imod = _imod.get(mod);
                    console.add("imod " + imod);
                }

                int genFrom = bag.intColumn(SFZElement.IBAG_IGENINDEX);
                int genTo = bagNext.intColumn(SFZElement.IBAG_IGENINDEX) - 1;

                XTTable genIndexExtra = new XTTable(_igen, genFrom, genTo);
                bag.setColumn(SFZElement.IBAG_IGENINDEX_TABLE, genIndexExtra);

                for (int gen = genFrom; gen <= genTo; ++ gen) {
                    XTRow igen = _igen.get(gen);
                    console.add("igen " + igen);
                }
            }
        }
    }
    
    public void dumpSamples(TheConsole console) {
        SFZElement _shdr = getElement("shdr");

        console.add("Sample Length: " + _shdr.size());
        for (int i = 0; i < _shdr.size(); ++ i) {
            XTRow row = _shdr.get(i);
            console.add("Sample[" + i + "]=" + row);
        }
    }
    
    public void writeCSV(String section) throws IOException {
        SFZElement element = getElement(section);
        if (element == null) {
            throw new IllegalArgumentException("section " + section + " unknown");
        }
        
        TextListForDebug dump = new TextListForDebug();
        element.getDump(dump);
        
        File file = new File("c:/users/yaman", section + ".csv");
        FileOutputStream stream = new FileOutputStream(file);
        try {
            BufferedOutputStream buf = new BufferedOutputStream(stream);
            OutputStreamWriter writer = new OutputStreamWriter(buf);

            for (String line : dump) {
                writer.write(line);
                writer.write("\n");
            }
            
            buf.flush();
            stream.close();
            stream = null;
        }finally {
            if (stream != null) {
                try {
                    stream.close();
                }catch(IOException ex) {
                    
                }
                file.delete();
            }
        }
    }
}
