/*
 * Copyright 2023 Syntarou YOSHIDA.
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
package jp.synthtarou.libs.smf;

// https://qiita.com/takayoshi1968/items/8e3f901539c92a6aac16

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFTempo implements Comparable<SMFTempo>{
    // Set Tempo イベントが持つ 
    public long _mpq;
    // Set Tempo イベントが発生するまでの時間 (Ticks)
    public long _tick;
    // Set Tempo イベントが発生するまでの時間 (us)
    public long _microsecond;

    @Override
    public int compareTo(SMFTempo o) {
        long x = _tick - o._tick;
        if (x == 0) {
            x = _microsecond - o._microsecond;
        }
        if (x < 0) return -1;
        if (x > 0) return 1;
        return 0;
    }
    
    public String toString() {
        return "MPQ = " + _mpq +" when tick = " + _tick +", ms = " + _microsecond;
    }
}
