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
package jp.synthtarou.mixtone.listmodel;

import java.util.ArrayList;
import javax.swing.AbstractListModel;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;
import jp.synthtarou.mixtone.synth.soundfont.SFZElement;
import jp.synthtarou.mixtone.synth.soundfont.XTFile;
import jp.synthtarou.mixtone.synth.oscilator.XTOscilator;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ListModelINSTSHDRProperty extends AbstractListModel<String>{
    public final XTFile _sfz;
    public final int _sampleId;
    public final SFZElement.SFZElement_shdr _shdr;
    ArrayList<String> _text;
    public final XTRow _row;
    
    public final String _name;
    public final boolean _doLoop;
    
    public final int _start;
    public final int _end;
    public final int _loopStart;
    public final int _loopEnd;
    public final int _sampleRate;
    public final int _originalKey;
    public final int _correction;
    public final int _overridingRootKey;
    public final int _sampleLink;
    public final int _type;
    
    public ListModelINSTSHDRProperty(XTFile sfz, int sampleId, boolean loop, int overridingRootKey) {
        _sfz = sfz;
        _sampleId = sampleId;
        _doLoop = loop;
        _overridingRootKey = overridingRootKey;
        _shdr = sfz.getElement_shdr();
        _text = new ArrayList<>();
        
        _row = _shdr.get(sampleId);
        if (_row != null) {
            _name = _row.textColumn(SFZElement.SHDR_NAME);
            _start = _row.intColumn(SFZElement.SHDR_START);
            _end = _row.intColumn(SFZElement.SHDR_END);
            _loopStart = _row.intColumn(SFZElement.SHDR_LOOPSTART);
            _loopEnd = _row.intColumn(SFZElement.SHDR_LOOPEND);
            _sampleRate = _row.intColumn(SFZElement.SHDR_SAMPLERATE);

            _correction = _row.intColumn(SFZElement.SHDR_PITCHCORRECTION);
            _sampleLink = _row.intColumn(SFZElement.SHDR_SAMPLELINK);
            _type= _row.intColumn(SFZElement.SHDR_TYPE);
            _originalKey = _row.intColumn(SFZElement.SHDR_ORIGINALPITCH);
        }
        else {
            _name = null;
            _start = 0;
            _end = 0;
            _loopStart = 0;
            _loopEnd = 0;
            _sampleRate = 0;
            
            _correction = 0;
            _sampleLink = -1;
            _type= 0;
            _originalKey = 0;
        }
 
        _text.add("name=" + _name);

        _text.add("start=" + _start);
        _text.add("end=" + _end);
        
        _text.add("loopstart=" + _loopStart);
        _text.add("loopend=" + _loopEnd);
        
        _text.add("samplerate=" + _sampleRate);
        _text.add("originalkey=" + _originalKey + " / over " + _overridingRootKey);

        _text.add("correction=" + _correction);
        _text.add("samplelink=" + _sampleLink);
        _text.add("type=" + _type);
    }
    
    @Override
    public int getSize() {
        return _text.size();
    }

    @Override
    public String getElementAt(int index) {
        return _text.get(index);
    }
    
    public XTOscilator newOscilator(int note) {
        return new XTOscilator(0, note, 1.0, _sfz, _sampleId, _doLoop, _overridingRootKey, 0);
    }
}
