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
public class OscilatorPosition {
    double _streamSampleRate = 44100;
    double _waveSampleRate;
    int _waveKey;
    double _waveFrequency;
    int _targetKey;
    double _targetFrequency;
    
    public OscilatorPosition(int waveSampleRate, int waveKey, int targetKey) {
        _waveKey = waveKey;
        _targetKey = targetKey;
        _waveSampleRate = waveSampleRate;
        
        _waveFrequency = 440 * Math.pow(2, (_waveKey-69) / 12.0);
        _targetFrequency = 440 * Math.pow(2, (_targetKey-69) / 12.0);

        /*
        System.out.println("wave " + _waveKey + " (" + _waveFrequency + "hz)");
        System.out.println("target " + _targetKey + " (" + _targetFrequency + "hz)");
        */
    }
    
    public int frameToSampleoffset(long frame) {
        double time = frame / _streamSampleRate * _targetFrequency / _waveFrequency;
        double frame2 = time * _waveSampleRate;
        if (frame2 >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int)frame2;
    }
    
    public double sampleOffsetToTime(long frame) {
        double time = frame / _waveSampleRate;
        return time;
    }
}
