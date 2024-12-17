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
public class XTFilter {
    double _cutoffFreq;
    double _resonance;
    double _samplerate;
    double[] _smpl = new double[2];

    double _cut_lp;
    double _fb_lp;
            
    public XTFilter() {
        this(20000, 0.0);
    }
    
    public XTFilter(double cutoffFreq, double resonance) {
        cutoffFreq = Math.clamp(cutoffFreq, 10, 22200);
        resonance = Math.clamp(resonance, 0.1, 1);
        _cutoffFreq = cutoffFreq;
        _resonance = resonance;
        _samplerate = 44100;
        _cut_lp = _cutoffFreq * 2 / _samplerate;
        _fb_lp = _resonance + _resonance / ( 1.0 - _cut_lp );
    }
    
    public double update_old(double value) {
	_smpl[0] = _smpl[0] + _cut_lp * (value- _smpl[0] + _fb_lp * (_smpl[0] - _smpl[1]));
	_smpl[1] = _smpl[1] + _cut_lp * (_smpl[0] - _smpl[1]);
        return _smpl[1];
    }
    
    double[] _inputHistory = new double[2];
    double[] _outputHistory = new double[3];
    
    public double update(double newInput) {
        double c = 1.0 / (float)Math.tan(Math.PI * _cutoffFreq / _samplerate);
        double a1 = 1.0 / (1.0f + _resonance * 10 * c + c * c);
        double a2 = 2.0 * a1;
        double a3 = a1;
        double b1 = 2.0f * (1.0f - c * c) * a1;
        double b2 = (1.0f - _resonance * 10 * c + c * c) * a1;

        double newOutput = a1 * newInput + a2 * _inputHistory[0] + a3 * _inputHistory[1] - b1 * _outputHistory[0] - b2 * _outputHistory[1];

        _inputHistory[1] = _inputHistory[0];
        _inputHistory[0] = newInput;

        _outputHistory[2] = _outputHistory[1];
        _outputHistory[1] = _outputHistory[0];
        _outputHistory[0] = newOutput;
        return newOutput;
    }
}