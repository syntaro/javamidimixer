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
package jp.synthtarou.midimixer.libs.midi.smf;

// https://qiita.com/takayoshi1968/items/8e3f901539c92a6aac16

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFTempo {
    // ファイル内にテンポ指定がない場合は 120 bpm = 500 000 mpq とする.
    /*
        BPM = 60,000,000 / MPQ
        MPQ = 60,000,000 / BPM
        BPM * MPQ = 60,000,000
    */
    public static int DEFAULT_MPQ = 500000;
    // mpqStack = Set Tempo イベントが持つ MPQ
    public long mpqStack;
    // cumulativeTicks = Set Tempo イベントが発生するまでの時間 (Ticks)
    public long cumulativeTicks;
    // cumulativeMicroseconds = Set Tempo イベントが発生するまでの時間 (us)
    public long cumulativeMicroseconds;
}
