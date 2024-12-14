/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.synth.oscilator;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTEnvelope {
    long _attachSamples = 0;
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
        env.setAttachSamples((long)sampleRate);
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
        _attachSamples = samples;
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
        
        if (samples < _attachSamples) {
            _currentAmount = samples * 1.0 / _attachSamples;
            return _currentAmount;
        }
        if (samples < (_attachSamples + _decaySamples)) {
            double from = 1.0;
            double to = _sustainLevel;
            
            long spentSample = samples - _attachSamples;
            double spentPercent = ((double)spentSample) / _decaySamples;
            double step = to - from;

            _currentAmount = from + step * spentPercent;
            return _currentAmount;
        }
        _currentAmount = _sustainLevel;
        return _sustainLevel;
    }
    
    
}
