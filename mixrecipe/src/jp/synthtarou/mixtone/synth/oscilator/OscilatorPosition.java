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

import jp.synthtarou.mixtone.synth.XTSynthesizerSetting;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class OscilatorPosition {
    float _streamSampleRate = XTSynthesizerSetting.getSetting().getSampleRate();
    double _waveSampleRate;
    int _waveKey;
    double _waveFrequency;
    int _targetKey;
    double _targetFrequency;
    int _waveCorrection;
    int _pitchRange = 2;

    XTEnvelope _debugEnv = null;//new XTEnvelope().setAttachSamples(44100);

    public OscilatorPosition(int waveSampleRate, int waveKey, int waveCorrection, int targetKey) {
        _waveKey = waveKey;
        _targetKey = targetKey;
        _waveSampleRate = waveSampleRate;
        _waveCorrection = waveCorrection;
        
        _waveFrequency = 440 * Math.pow(2, (_waveKey-69) / 12.0 );
        _targetFrequency = 440 * Math.pow(2, (_targetKey -69) / 12.0 +(_waveCorrection / 100.0 / 12.0));

        /*
        System.out.println("wave " + _waveKey + " (" + _waveFrequency + "hz)");
        System.out.println("target " + _targetKey + " (" + _targetFrequency + "hz)");
        */
    }

    double _lastEnv = 0;

    public int frameToSampleoffset(long timeFrame) {
        double envelope = 0;//_debugMEnv.getAmpAmount(timeFrame) * 2 - 1;
        timeFrame -= _timeBase;
        
        _targetFrequency = 440 * Math.pow(2, ((_targetKey+(envelope*_pitchRange))-69) / 12.0 +(_waveCorrection / 100.0 / 12.0));

        double time = timeFrame / _streamSampleRate * _targetFrequency / _waveFrequency;
        double sampleFrame = time * _waveSampleRate;
        if (sampleFrame >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (true) {
            _timeBase += timeFrame;
            _sampleBase += sampleFrame;
        }
        sampleFrame += _sampleBase;
        return (int)sampleFrame;
    }
    
    long _timeBase = 0;
    double _sampleBase = 0;
    
    public double sampleOffsetToTime(long frame) {
        double time = frame / _waveSampleRate;
        return time;
    }
}
