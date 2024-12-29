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
package jp.synthtarou.mixtone.synth.soundfont.wrapper;

import jp.synthtarou.mixtone.synth.soundfont.SFZElement;
import jp.synthtarou.mixtone.synth.soundfont.XTFile;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SHDREntry {
    XTFile _sfz;
    int _sampleId;
    XTRow _smplEntry;
    
    public SHDREntry(XTFile sfz, int sampleId) {
        _sfz = sfz;
        _sampleId = sampleId;
        _smplEntry = _sfz._shdr.get(_sampleId);
    }
    
    public String getName() {
        return _smplEntry.get(SFZElement.SHDR_NAME).textValue();
    }
    
    public int getAsInt(int type) {
        Number x =_smplEntry.get(type).numberValue();
        if (x == null) {
            return -1;
        }
        return x.intValue();
    }
    
    public int getStart() {
        return getAsInt(SFZElement.SHDR_START);
    }

    public int getEnd() {
        return getAsInt(SFZElement.SHDR_END);
    }

    public int getLoopStart() {
        return getAsInt(SFZElement.SHDR_LOOPSTART);
    }

    public int getLoopEnd() {
        return getAsInt(SFZElement.SHDR_LOOPEND);
    }

    public int getSampleRate() {
        return getAsInt(SFZElement.SHDR_SAMPLERATE);
    }

    public int getOriginalPitch() {
        return getAsInt(SFZElement.SHDR_ORIGINALPITCH);
    }

    public int getPitchCorrection() {
        return getAsInt(SFZElement.SHDR_PITCHCORRECTION);
    }

    public int getSampleLink() {
        return getAsInt(SFZElement.SHDR_SAMPLELINK);
    }

    public int getType() {
        return getAsInt(SFZElement.SHDR_TYPE);
    }
}
