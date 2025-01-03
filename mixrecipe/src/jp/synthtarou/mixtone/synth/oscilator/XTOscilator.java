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
package jp.synthtarou.mixtone.synth.oscilator;

import jp.synthtarou.mixtone.synth.soundfont.SFZElement;

import jp.synthtarou.mixtone.synth.soundfont.XTFile;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTOscilator {
    static final String TAG =  "XTOscilator";
    XTFile _sfz;
    SFZElement.SFZElement_shdr _shdr;
    SFZElement.SFZElement_smpl _smpl;
    SFZElement.SFZElement_sm24 _sm24;

    long _totalFrame;
    
    boolean _oscInLoop;
    
    public final String _name;
    public boolean _loop;
    
    public final int _track;
    public final int _playKey;
    public final int _sampleId;
    public final int _start;
    public final int _end;
    public int _loopStart;
    public int _loopEnd;
    public final int _sampleRate;
    public final int _originalKey;
    public final int _correction;
    public final int _overridingRootKey;
    public final int _sampleLink;
    public final int _type;
    public final XTRow _row;
    public OscilatorPosition _pos;
    public XTFilter _filter;
    public double _tonePan;
    
    public XTOscilator(int track, int playKey, double velocity, XTFile sfz, int sampleId, boolean loop, int overridingRootKey, double pan) {
        _track = track;
        _sfz = sfz;
        _sampleId = sampleId;
        _playKey = playKey;
        _loop = loop;
        _velocity = velocity;
        _overridingRootKey = overridingRootKey;
        _tonePan = pan;

        _sfz = sfz;
        _shdr = _sfz._shdr;
        _smpl = _sfz._smpl;
        _sm24 = _sfz._sm24;
        
        _row = _shdr.get(sampleId);
        if (_row == null) {
            throw new IllegalArgumentException("sampleID " + sampleId + " overflow " + _shdr.size());
        }
        _name = "";//_row.textColumn(SFZElement.SHDR_NAME);
        _start = _row.intColumn(SFZElement.SHDR_START);
        _end = _row.intColumn(SFZElement.SHDR_END);
        _loopStart = _row.intColumn(SFZElement.SHDR_LOOPSTART);
        _loopEnd = _row.intColumn(SFZElement.SHDR_LOOPEND);
        
        /*
        if (_loopStart >= 0 && _loopEnd >= 0 && _start <= _loopStart && _loopStart <= _loopEnd && _loopEnd <= _end) {
            _loop = true;
        }
        if (_loop && _loopStart < 0) {
            _loopStart = _start;
            System.err.println("fixed loopStart " + _start);
        }
        if (_loop && _loopEnd < 0) {
            _loopEnd = _end;
            System.err.println("fixed loopEnd " + _end);
        }
        */
        _sampleRate = _row.intColumn(SFZElement.SHDR_SAMPLERATE);
        _correction = _row.intColumn(SFZElement.SHDR_PITCHCORRECTION);
        _sampleLink = _row.intColumn(SFZElement.SHDR_SAMPLELINK);
        _type= _row.intColumn(SFZElement.SHDR_TYPE);
        _originalKey = _row.intColumn(SFZElement.SHDR_ORIGINALPITCH);

        int sampleKey = _originalKey;
        if (_overridingRootKey >= 0) {
            sampleKey = _overridingRootKey;
        }
        /*
        if (playKey< 0) {
            playKey = sampleKey;
        }*/
       
        _totalFrame = 0;
        _pos = new OscilatorPosition(_sampleRate, sampleKey, _correction, playKey);
        //_filter = new XTFilter();

        initEnvelope();
    }

    private double getFromOscilator(long frame){
        if (isFaded()) {
            return 0;
        }
        long x = _pos.frameToSampleoffset(frame);
        if (_loopEnd >= 0 && x + _start > _loopEnd) {
            if (_loop) {
                long distance = _loopEnd - _loopStart + 1;
                while (x + _start > _loopEnd) {
                    x -= distance;
                }
                if (_ampEnv.isNoteOff()) {
                    x += distance;
                    if (x + _start > _end) {
                        _faded = true;
                        return 0;
                    }
                }
                int smpl = _smpl.getSample16((int)(_start + x));
                return smpl;
            }
        }
        if (x + _start > _end) {
            _faded = true;
            return 0;
        }
        return _smpl.getSample16((int)(_start + x));
    }

    XTEnvelope _ampEnv;

    public void initEnvelope() {
        _ampEnv = new XTEnvelope();
    }
    
    public void noteOff() {
        _ampEnv.noteOff();
    }

    public double nextValueWithAmp() {
        long frame = _totalFrame ++;
        double osc = getFromOscilator(frame);
        //double filt = _filter.update(osc);
        double amp = _ampEnv.getAmpAmount(frame);
        return osc * amp;
    }
    
    public boolean isNoteOff() {
        return _ampEnv.isNoteOff();
    }

    public void noteMute() {
        _ampEnv.noteMute();
    }
    
    public boolean isFaded() {
        if (_faded) {
            return _faded;
        }
        if (_ampEnv.isNoteFaded()) {
            _faded = true;
        }
        return _faded;
    }

    public void setPan(double x) { //-1 ~ +1
        _pan = _tonePan;
        if (x > 0) {
            _pan += x;
            if (_pan > 1) {
                _pan = 1;
            }
        }
        else if (x < 0) {
            _pan += x;
            if (_pan < -1) {
                _pan = -1;
            }
        }
    }
    
    public void setVolume(double volume) { //0~+1
        _volume = volume;
    }
    
    public double _pan;
    public double _volume;
    public double _velocity;
    public boolean _faded = false;
    public long _timing;
}
