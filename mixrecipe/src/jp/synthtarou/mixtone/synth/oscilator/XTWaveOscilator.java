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

import jp.synthtarou.mixtone.synth.audio.DoubleOutputStream;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTWaveOscilator {
    DoubleOutputStream _buffer;
    
    public XTWaveOscilator(DoubleOutputStream buffer){
        _buffer = buffer;
    }

    public double step(){
        return _buffer.readDouble();
    }
    
    public boolean isClose() {
        return _buffer.isEnd();
    }

    public long _frame = 0;
}
