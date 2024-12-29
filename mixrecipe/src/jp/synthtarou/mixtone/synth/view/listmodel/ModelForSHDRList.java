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
package jp.synthtarou.mixtone.synth.view.listmodel;

import java.util.ArrayList;
import javax.swing.AbstractListModel;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;
import jp.synthtarou.mixtone.synth.soundfont.SFZElement;
import jp.synthtarou.mixtone.synth.soundfont.XTFile;
import jp.synthtarou.mixtone.synth.oscilator.XTOscilator;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.IBAGEntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.SHDREntry;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ModelForSHDRList extends AbstractListModel<String>{
    public final int _sampleId;
    ArrayList<String> _text;
    
    public final Integer _doLoop;
    public final Integer _overridingRootKey;
    public final XTFile _sfz;
    
    public ModelForSHDRList(IBAGEntry ibag) {
        _sfz = ibag.getXTFile();
        _sampleId = ibag.getIGENEntry().getSampleId();
        _doLoop = ibag.getIGENEntry().getSampleModes();
        _overridingRootKey = ibag.getIGENEntry().getOverridingRootKey();
        _text = new ArrayList<>();
        SHDREntry shdr = new SHDREntry(_sfz, _sampleId);

        _text.add("name=" + shdr.getName());
        _text.add("start=" + shdr.getStart());
        _text.add("end=" + shdr.getEnd());
        _text.add("loopstart=" + shdr.getLoopStart());
        _text.add("loopend=" + shdr.getLoopEnd());
        _text.add("samplerate=" + shdr.getSampleRate());
        _text.add("originalkey=" + shdr.getOriginalPitch() + " / over " + _overridingRootKey);
        _text.add("correction=" + shdr.getPitchCorrection());
        _text.add("samplelink=" + shdr.getSampleLink());
        _text.add("type=" + shdr.getType());
    }
    
    @Override
    public int getSize() {
        return _text.size();
    }

    @Override
    public String getElementAt(int index) {
        return _text.get(index);
    }
}
