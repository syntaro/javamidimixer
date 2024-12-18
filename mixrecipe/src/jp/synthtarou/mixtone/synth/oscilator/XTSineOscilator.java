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

import jp.synthtarou.mixtone.synth.audio.XTAudioStream;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTSineOscilator {
    private int times;
    private double _frequency;
    private double _angle_freq;
    private double _angle_freq_discrete;
    private double _sample_freq;

    public XTSineOscilator(int note){
        _frequency = 440 * Math.pow(2, (note-69) / 12.0);
        _sample_freq = XTAudioStream._sampleRate;
        times = 0;
        _angle_freq = _frequency * 2 * Math.PI;
        _angle_freq_discrete = _angle_freq/ _sample_freq;
    }

    public double step(){
        return Math.sin(_angle_freq_discrete * (times ++)) / 100;
    }
    
    public boolean isClose() {
        return times >= _sample_freq * 3;
    }

    public long _frame = 0;
}
