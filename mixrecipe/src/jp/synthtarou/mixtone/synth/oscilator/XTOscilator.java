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
import static jp.synthtarou.mixtone.synth.oscilator.XTEnvelope.sampleRate;
import jp.synthtarou.mixtone.synth.soundfont.XTFile;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTOscilator {
    XTFile _sfz;
    SFZElement.SFZElement_shdr _shdr;
    SFZElement.SFZElement_smpl _smpl;
    SFZElement.SFZElement_sm24 _sm24;

    long _oscStartTime;
    
    long _totalFrame;
    
    double _limitVolume;

    boolean _oscInLoop;
    
    public final String _name;
    public boolean _loop;
    
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
    
    public XTOscilator(int playKey, double velocity, XTFile sfz, int sampleId, boolean loop, int overridingRootKey) {
        _sfz = sfz;
        _sampleId = sampleId;
        _playKey = playKey;
        _loop = loop;
        _velocity = velocity;
        _overridingRootKey = overridingRootKey;

        _shdr = (SFZElement.SFZElement_shdr)sfz.getElement("shdr");
        _row = _shdr.get(sampleId);
        if (_row == null) {
            throw new IllegalArgumentException("sampleID " + sampleId + " overflow " + _shdr.size());
        }
        _name = _row.textColumn(SFZElement.SHDR_NAME);
        _start = _row.intColumn(SFZElement.SHDR_START);
        _end = _row.intColumn(SFZElement.SHDR_END);
        _loopStart = _row.intColumn(SFZElement.SHDR_LOOPSTART);
        _loopEnd = _row.intColumn(SFZElement.SHDR_LOOPEND);
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

        _sampleRate = _row.intColumn(SFZElement.SHDR_SAMPLERATE);

        _correction = _row.intColumn(SFZElement.SHDR_PITCHCORRECTION);
        _sampleLink = _row.intColumn(SFZElement.SHDR_SAMPLELINK);
        _type= _row.intColumn(SFZElement.SHDR_TYPE);
        _originalKey = _row.intColumn(SFZElement.SHDR_ORIGINALPITCH);

        _sfz = sfz;
        _shdr = (SFZElement.SFZElement_shdr)_sfz.getElement("shdr");
        _smpl = (SFZElement.SFZElement_smpl)_sfz.getElement("smpl");
        _sm24 = (SFZElement.SFZElement_sm24)_sfz.getElement("sm24");
        int sampleKey = _originalKey;
        if (_overridingRootKey >= 0) {
            sampleKey = _overridingRootKey;
        }
        if (playKey< 0) {
            playKey = sampleKey;
        }
       
        _oscStartTime = System.currentTimeMillis();
        _totalFrame = 0;

        int min = 100000;
        int max = -100000;
        for (int i = _start; i <= _end; ++ i) {
            int x = _smpl.getSample16(i);
            if (max < x) max = x;
            if (min > x) min = x;
        }
        _limitVolume = 0.2 / (max - min);
        _pos = new OscilatorPosition(_sampleRate, sampleKey, _correction, playKey);

        initEnvelope();
    }

    public double nextValueOfOscilator(long frame){
        long x = _pos.frameToSampleoffset(frame);
        if (_loopEnd >= 0 && x + _start > _loopEnd) {
            if (_loop && _loopStart <= _loopEnd) {
                long distance = _loopEnd - _loopStart + 1;
                while (x > _loopEnd - _start) {
                    x -= distance;
                }
                if (_ampEnv.isNoteOff()) {
                    x += distance;
                    if (x > _end - _start) {
                        x = _end - _start;
                    }
                }
                int smpl = _smpl.getSample16((int)(_start + x));
                return smpl * _limitVolume;
            }
        }
        if (x + _start > _end) {
            return 0;
        }
        int smpl = _smpl.getSample16((int)(_start + x));
        return smpl * _limitVolume;
    }

    XTEnvelope _ampEnv;
    double _releaseOscNeeded = 0;
    double _releaseAmpNeeded = 0;

    public void initEnvelope() {
        _ampEnv = new XTEnvelope();
 
        _ampEnv.setAttachSamples((long)(0));
        _ampEnv.setDecaySamples((long)(0));
        _ampEnv.setSustainLevel(1);
        _ampEnv.setReleaseSamples((long)(sampleRate * 0.5));
        _releaseAmpNeeded = _ampEnv._releaseSamples;
        _releaseOscNeeded = _pos.frameToSampleoffset(_end - _loopEnd);
    }
    
    public void noteOff() {
        if (!_ampEnv.isNoteOff()) {
            _ampEnv.noteOff();
        }
    }
    
    public double nextValueWithAmp() {
        long frame = _totalFrame ++;
        double amp = _ampEnv.getAmountAt(frame);
        return nextValueOfOscilator(frame) * amp;
    }
    
    public boolean isClose() {
        return _ampEnv.isNoteFaded();
    }

    public void setPan(double x) { //-1 ~ +1
        _pan = x;
    }
    
    public void setVolume(double volume) { //0~+1
        _volume = volume;
    }
    
    public double _pan;
    public double _volume;
    public double _velocity;
    public long _frame = 0;
}
