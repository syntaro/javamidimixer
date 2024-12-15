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

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTEnvelope {
    long _attackSamples = 0;
    long _decaySamples = 0;
    double _sustainLevel = 1.0;
    long _releaseSamples = 0;
    
    long _currentSample;
    double _currentAmount;

    long _noteOffSample = -1;
    double _noteOffAmount;
    
    static final double sampleRate = 44100;
   
    public static void main(String[] args) {
        XTEnvelope env = new XTEnvelope();
        env.setAttachSamples((long)sampleRate / 4);
        env.setDecaySamples((long)sampleRate);
        env.setSustainLevel(0.5);
        env.setReleaseSamples((long)sampleRate * 2);
        
        for(long x = 0; x <= (sampleRate * 10); x += sampleRate / 10) {
            if (x >= sampleRate * 5) {
                env.noteOff();
            }
            System.out.println("step " + x + " -> " + env.getAmountAt(x));
        }
    }
    
    public void setAttachSamples(long samples) {
        _attackSamples = samples;
        if (samples == 0) {
            _currentAmount = 1;
        }
        else {
            _currentAmount = 1;
        }
    }

    public void setDecaySamples(long samples) {
        _decaySamples = samples;
    }

    public void setSustainLevel(double level) {
        _sustainLevel = level;
    }

    public void setReleaseSamples(long samples) {
        _releaseSamples = samples;
    }
    
    public static long millisecToSample(long millisec) {
        double temp = 1000.0 * millisec;
        return (long)Math.round(temp / sampleRate);
    }

    public boolean isNoteOff() {
        return _noteOffSample >= 0;
    }
    
    public boolean isNoteFaded() {
        if (_noteOffSample >= 0) {
            if (_currentSample >= _noteOffSample + _releaseSamples) {
                return true;
            }
        }
        return false;
    }
    
    public void noteOff() {
        _noteOffSample = _currentSample;
        _noteOffAmount = _currentAmount;
    }
    
    long _tillMute = Long.MIN_VALUE;

    public long calcFrameTillMute() {
        return (long)_tillMute;
    }
    
    public double getAmountAt(long samples) {
        _currentSample = samples;

        if (_noteOffSample >= 0) {
            if (_releaseSamples == 0) {
                return 0;
            }

            if (_noteOffAmount == 0) {
                return 0;
            }

            double from = _noteOffAmount;
            double to = 0;

            long spentSample = samples - _noteOffSample;
            double spentPercent = ((double)spentSample) / _releaseSamples;
            double step = to - from;

            double leftPercent = 1 - spentPercent;
            _tillMute = (long)(leftPercent * _releaseSamples);

            _currentAmount = from + step * spentPercent;
            return _currentAmount;
        }

        if (samples < _attackSamples) {
            _currentAmount = samples * 1.0 / _attackSamples;
            return _currentAmount;
        }
        if (samples < (_attackSamples + _decaySamples)) {
            double from = 1.0;
            double to = _sustainLevel;

            long spentSample = samples - _attackSamples;
            double spentPercent = ((double)spentSample) / _decaySamples;
            double step = to - from;

            _currentAmount = from + step * spentPercent;
            return _currentAmount;
        }
        else {
            _currentAmount = _sustainLevel;
            return _currentAmount;
        }
    }
}
