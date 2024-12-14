/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.synth.oscilator;

import java.util.ArrayList;
import jp.synthtarou.mixtone.listmodel.ListModelINSTSHDRProperty;
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
    long _oscReleaseTime;
    
    double _samplePlayPos;
    double _samplePlaySpeed;
    
    double _limitVolume;

    boolean _oscReadyRelease;
    boolean _oscInLoop;
    boolean _oscInRelease;
    
    public long recalcPlayPos() {
        int startOffset = 0;
        int endOffset = _end - _start;
        int loopStartOffset = _loopStart - _start;
        int loopEndOffset = _loopEnd - _start;
        int fontFrames = (int)_samplePlayPos;

        //in Attack
        if (fontFrames < loopStartOffset) {
            return fontFrames;
        }

        // check releasing 
        if (_samplePlayPos > loopEndOffset) {
            if (_loop == false) {
                _oscReadyRelease = true;
                _oscInRelease = true;
            }
        }

        // in Relase
        if (_oscInRelease) {
            // check end
            if (fontFrames > endOffset) {
                return -1;
            }
            return fontFrames;
        }

        if (_oscReadyRelease) {
            //exiting loop
            if (_samplePlayPos > loopEndOffset) {
                _oscInRelease = true;
            }
        }else {
            //looping 
            while (_samplePlayPos > loopEndOffset) {
                _samplePlayPos -= loopEndOffset - loopStartOffset + 1;
            }
        }
        fontFrames = (int)_samplePlayPos;
        return fontFrames;
    }
    
    public final String _name;
    public final boolean _loop;
    
    public final int _playKey;
    public final int _sampleId;
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
    public final XTRow _row;
    
    public XTOscilator(int playKey, XTFile sfz, int sampleId, boolean loop, int overridingRootKey) {
        _sfz = sfz;
        _sampleId = sampleId;
        _playKey = playKey;
        _loop = loop;
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
       
        if (sampleKey >= 0x80) {
            _samplePlaySpeed = 1.0;
        }
        else {
            double sampleFrequency = 440 * Math.pow(2, (sampleKey-69) / 12.0);
            double playFrequency = 440 * Math.pow(2, (playKey-69) / 12.0);
            _samplePlaySpeed = (_sampleRate / 44100.0) * playFrequency / sampleFrequency;
        }
        
        _oscStartTime = System.currentTimeMillis();
        _oscReleaseTime = -1;
        _beforeX = -1;
        _samplePlayPos = 0;

        int min = 100000;
        int max = -100000;
        for (int i = _start; i <= _end; ++ i) {
            int x = _smpl.getSample16(i);
            if (max < x) max = x;
            if (min > x) min = x;
        }
        _limitVolume = 0.2 / (max - min);
        initEnvelope();
    }

    long _beforeX;

    public double nextValueOfOscilator(){
        long x = recalcPlayPos();
        if (x >= 0 && _beforeX > x) {
            if (_loop == false) {
                _oscReadyRelease = true;
                _oscInRelease = true;
            }
            if (_oscReadyRelease) {
                _oscInRelease = true;
                x = recalcPlayPos();
            }
        }
        if (x < 0) {
            long tick = System.currentTimeMillis();
            _oscReleaseTime = tick;
            return 0;
        }
        _samplePlayPos += _samplePlaySpeed;
        _beforeX = x;
        int smpl = _smpl.getSample16((int)(_start + x));
        return smpl * _limitVolume;
    }

    XTEnvelope _ampEnv;
    long _ampFrame;
    double _releaseOscNeeded = 0;
    double _releaseAmpNeeded = 0;

    public void initEnvelope() {
        _ampEnv = new XTEnvelope();
 
        _ampEnv.setAttachSamples((long)(sampleRate * 0));
        _ampEnv.setDecaySamples((long)(sampleRate / 4));
        _ampEnv.setSustainLevel(0.3);
        _ampEnv.setReleaseSamples((long)(sampleRate * 0.2));
        _releaseAmpNeeded = _ampEnv._releaseSamples;
        _releaseOscNeeded = (_end - _loopEnd) / _samplePlaySpeed;
    }
    
    boolean _closed = false;
    
    public void noteOff() {
        if (!_ampEnv.isNoteOff()) {
            _closed = true;
            _ampEnv.noteOff();
        }
    }
    
    public double nextValueOfAmp() {
        if (_ampEnv.isNoteOff()) {
            long x = _ampEnv.calcFrameTillMute();
            if (x != Long.MIN_VALUE && x <= _releaseOscNeeded) {
                _oscReadyRelease = true;
            }
        }
        double amp = _ampEnv.getAmountAt(_ampFrame ++);
        return nextValueOfOscilator() * amp;
    }
    
    public boolean isClose() {
        return _closed || _ampEnv.isNoteFaded();
    }

    public long _frame = 0;
}
