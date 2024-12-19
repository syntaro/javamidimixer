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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.libs.swing.MXFileChooser;

import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;
import jp.synthtarou.mixtone.synth.soundfont.table.XTTable;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTFile {
    public static String TAG = "XTFile";
    public XTFile(File f) throws IOException {
        RiffChunk riff = new RiffChunk();
        riff.setData(f);
        _storage = new HashMap<>();
        prepareChunkTree(riff);
        prepareRooms(riff);
        dumpPreset();
        dumpInstrument();
    }

    public XTFile(InputStream stream) throws IOException {
        try {
            RiffChunk riff = new RiffChunk();
            riff.setData(stream);
            _storage = new HashMap<>();
            prepareChunkTree(riff);
            prepareRooms(riff);
            dumpPreset();
            dumpInstrument();
        }catch(Throwable ex) {
            MXFileLogger.getLogger(XTFile.class).log(Level.SEVERE, ex.getMessage(), ex);
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

    public void dumpInstrument() {
        SFZElement _inst = getElement("inst");
        SFZElement _ibag = getElement("ibag");
        SFZElement _imod = getElement("imod");
        SFZElement _igen = getElement("igen");

        if (_inst == null || _ibag == null || _imod == null || _igen == null ){
            return;
        }

        for (int i = 0; i < _inst.size() - 1; ++ i) {
            XTRow inst = _inst.get(i);
            XTRow instNext = _inst.get(i + 1);

            int instBagIndex = inst.intColumn(SFZElement.INST_BAGINDEX);
            int nextBagIndex = instNext.intColumn(SFZElement.INST_BAGINDEX);

            XTTable bagIndexExtra = new XTTable(_ibag, instBagIndex, nextBagIndex - 1);
            inst.setColumn(SFZElement.INST_BAGINDEX_TABLE, bagIndexExtra);

            for (int bagIndex = instBagIndex; bagIndex < nextBagIndex; ++ bagIndex) {
                XTRow bag = _ibag.get(bagIndex);
                XTRow bagNext = _ibag.get(bagIndex+1);

                int modFrom = bag.intColumn(SFZElement.IBAG_IMODINDEX);
                int modTo = bagNext.intColumn(SFZElement.IBAG_IMODINDEX) - 1;

                XTTable modIndexExtra = new XTTable(_imod, modFrom, modTo);
                bag.setColumn(SFZElement.IBAG_IMODINDEX_TABLE, modIndexExtra);

                int genFrom = bag.intColumn(SFZElement.IBAG_IGENINDEX);
                int genTo = bagNext.intColumn(SFZElement.IBAG_IGENINDEX) - 1;

                XTTable genIndexExtra = new XTTable(_igen, genFrom, genTo);
                bag.setColumn(SFZElement.IBAG_IGENINDEX_TABLE, genIndexExtra);
            }
        }
    }

    public void dumpPreset() {
        SFZElement _phdr = getElement("phdr");
        SFZElement _pbag = getElement("pbag");
        SFZElement _pmod = getElement("pmod");
        SFZElement _pgen = getElement("pgen");

        for (int i = 0; i < _phdr.size() - 1; ++ i) {
            XTRow preset = _phdr.get(i);
            XTRow next = _phdr.get(i + 1);

            int presetBagIndex = preset.intColumn(SFZElement.PHDR_BAGINDEX);
            int nextBagIndex = next.intColumn(SFZElement.PHDR_BAGINDEX);

            XTTable bagIndexExtra = new XTTable(_pbag, presetBagIndex, nextBagIndex - 1);
            preset.setColumn(SFZElement.PHDR_BAGINDEX_TABLE, bagIndexExtra);

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

    SFZElement.SFZElement_inst _inst = null;
    public SFZElement.SFZElement_inst getElement_inst() {
        if (_inst == null) {
            _inst = (SFZElement.SFZElement_inst)getElement("inst");
        }
        return _inst;
    }
    
    SFZElement.SFZElement_phdr _phdr = null;
    public SFZElement.SFZElement_phdr getElement_phdr() {
        if (_phdr == null) {
            _phdr = (SFZElement.SFZElement_phdr)getElement("phdr");
        }
        return _phdr;
    }
    
    SFZElement.SFZElement_shdr _shdr = null;
    public SFZElement.SFZElement_shdr getElement_shdr() {
        if (_shdr == null) {
            _shdr = (SFZElement.SFZElement_shdr)getElement("shdr");
        }
        return _shdr;
    }

    SFZElement.SFZElement_smpl _smpl = null;
    public SFZElement.SFZElement_smpl getElement_smpl() {
        if (_smpl == null) {
            _smpl = (SFZElement.SFZElement_smpl)getElement("smpl");
        }
        return _smpl;
    }
    
    SFZElement.SFZElement_sm24 _sm24 = null;
    public SFZElement.SFZElement_sm24 getElement_sm24() {
        if (_sm24 == null) {
            _sm24 = (SFZElement.SFZElement_sm24)getElement("sm24");
        }
        return _sm24;
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
}
