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
import java.util.logging.Level;
import jp.synthtarou.libs.log.MXFileLogger;


/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTFile {
    public static String TAG = "XTFile";
    public XTFile(File f) throws IOException {
        RiffChunk riff = new RiffChunk();
        riff.setData(f);
        prepareChunkTree(riff);
        prepareRooms(riff);
        //dumpPreset();
        //dumpInstrument();
    }

    public XTFile(InputStream stream) throws IOException {
        try {
            RiffChunk riff = new RiffChunk();
            riff.setData(stream);
            prepareChunkTree(riff);
            prepareRooms(riff);
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

    public void prepareRooms(RiffChunk data) {
        ArrayList<RiffChunk> children = data._children;
        
        if (children == null) {
            return;
        }
        for (RiffChunk seek : children) {
            String id = seek._chunkId;
            String type = seek._chunkType; 
            int size = seek._chunkSize;
            SFZElement struct = parseSingle(seek);
            prepareRooms(seek);
        }
    }

    public SFZElement parseSingle(RiffChunk riff) {
        if (riff._listDataElement != null) {
            return riff._listDataElement;
        }
        String chunk = riff._chunkId;
        if (chunk == null) {
            return null;
        }

        SFZElement element =  null;
        if (chunk.equals("ifil")) {
            _ifil = new SFZElement.SFZElement_ifil(riff);
        } else if (chunk.equals("isng")) {
            _isng = new SFZElement.SFZElement_isng(riff);
        } else if (chunk.equals("INAM")) {
            _INAM = new SFZElement.SFZElement_INAM(riff);
        } else if (chunk.equals("irom")) {
            _irom = new SFZElement.SFZElement_irom(riff);
        } else if (chunk.equals("iver")) {
            _iver = new SFZElement.SFZElement_iver(riff);
        } else if (chunk.equals("ICRD")) {
            _ICRD = new SFZElement.SFZElement_ICRD(riff);
        } else if (chunk.equals("IENG")) {
            _IENG = new SFZElement.SFZElement_IENG(riff);
        } else if (chunk.equals("IPRD")) {
            _IPDR = new SFZElement.SFZElement_IPRD(riff);
        } else if (chunk.equals("ICOP")) {
            _ICOP = new SFZElement.SFZElement_ICOP(riff);
        } else if (chunk.equals("ICMT")) {
            _ICMT = new SFZElement.SFZElement_ICMT(riff);
        } else if (chunk.equals("ISFT")) {
            _ISFT = new SFZElement.SFZElement_ISFT(riff);
        } else if (chunk.equals("smpl")) {
            _smpl = new SFZElement.SFZElement_smpl(riff);
        } else if (chunk.equals("sm24")) {
            _sm24 = new SFZElement.SFZElement_sm24(riff);
        } else if (chunk.equals("phdr")) {
            _phdr = new SFZElement.SFZElement_phdr(riff);
        } else if (chunk.equals("pbag")) {
            _pbag = new SFZElement.SFZElement_pbag(riff);
        } else if (chunk.equals("pmod")) {
            _pmod = new SFZElement.SFZElement_pmod(riff);
        } else if (chunk.equals("pgen")) {
            _pgen = new SFZElement.SFZElement_pgen(riff);
        } else if (chunk.equals("inst")) {
            _inst  = new SFZElement.SFZElement_inst(riff);
        } else if (chunk.equals("ibag")) {
            _ibag = new SFZElement.SFZElement_ibag(riff);
        } else if (chunk.equals("imod")) {
            _imod = new SFZElement.SFZElement_imod(riff);
        } else if (chunk.equals("igen")) {
            _igen = new SFZElement.SFZElement_igen(riff);
        } else if (chunk.equals("shdr")) {
            _shdr = new SFZElement.SFZElement_shdr(riff);
        } else {
            element = (new SFZElement("unknown") {
            });
        }

        riff._listDataElement = element;
        return element;
    }

    public SFZElement.SFZElement_ifil _ifil = null;
    public SFZElement.SFZElement_isng _isng = null;
    public SFZElement.SFZElement_INAM _INAM = null;
    public SFZElement.SFZElement_irom _irom = null;
    public SFZElement.SFZElement_inst _inst = null;
    public SFZElement.SFZElement_ibag _ibag = null;
    public SFZElement.SFZElement_igen _igen = null;
    public SFZElement.SFZElement_imod _imod = null;
    public SFZElement.SFZElement_phdr _phdr = null;
    public SFZElement.SFZElement_pbag _pbag = null;
    public SFZElement.SFZElement_pgen _pgen = null;
    public SFZElement.SFZElement_pmod _pmod = null;
    public SFZElement.SFZElement_shdr _shdr = null;
    public SFZElement.SFZElement_smpl _smpl = null;
    public SFZElement.SFZElement_sm24 _sm24 = null;

    public SFZElement.SFZElement_iver _iver = null;
    public SFZElement.SFZElement_ICRD _ICRD = null;;
    public SFZElement.SFZElement_IENG _IENG = null;;
    public SFZElement.SFZElement_IPRD _IPDR = null;;
    public SFZElement.SFZElement_ICOP _ICOP = null;;
    public SFZElement.SFZElement_ICMT _ICMT = null;;
    public SFZElement.SFZElement_ISFT _ISFT = null;;
}
