/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
    public final boolean _loop;
    
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
        _loop = loop;
        _overridingRootKey = overridingRootKey;
        _shdr = (SFZElement.SFZElement_shdr)sfz.getElement("shdr");
        _text = new ArrayList<>();
        
        _row = _shdr.get(sampleId);
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
        return new XTOscilator(note, _sfz, _sampleId, _loop, _overridingRootKey);
    }
}
