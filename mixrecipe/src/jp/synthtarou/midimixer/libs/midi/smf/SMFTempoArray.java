/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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

import java.util.ArrayList;
import jp.synthtarou.midimixer.mx36ccmapping.SortedArray;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFTempoArray extends SortedArray<SMFTempo> {

    SMFParser _parent;

    /* デフォルトのMPQを記録 */
    public SMFTempoArray(SMFParser parser) {
        _parent = parser;
        SMFTempo tempo = new SMFTempo();
        tempo._mpq = 500000; //120BPM
        tempo._microsecond = 0;
        tempo._tick = 0;
        add(tempo);
    }

    /* Tick以前の、最後のSMFTempoを返す */
    SMFTempo backSeekByTick(long tick) {
        SMFTempo ret = get(0);
        for (int x = 1; x < size(); x++) {
            SMFTempo seek = get(x);
            if (tick >= seek._tick) {
                ret = seek;
            }
        }
        return ret;
    }

    /* Tickから、Microsecondを求める */
    public long calcMicrosecondByTick(long ticks) {
        SMFTempo tempo = backSeekByTick(ticks);

        double deltaTick = ticks - tempo._tick;
        double deltaMicrosecond =  deltaTick * tempo._mpq / _parent._fileResolution;

        long ret = tempo._microsecond + (long)deltaMicrosecond;
        return ret;
    }

    /* TickベースでMPQを追加 （順番通りに追加する必要がある）  */
    public void addMPQwithTick(long mpq, long tick) {
        SMFTempo tempo = backSeekByTick(tick);

        double deltaTick = tick - tempo._tick;
        double deltaMicrosecond = deltaTick * tempo._mpq / _parent._fileResolution;

        if (deltaTick == 0) {
            //上書き
            tempo._mpq = mpq;
            return;
        }
        if (tempo._mpq == mpq) {
            //変化なし
            return;
        }
        
        SMFTempo newTempo = new SMFTempo();
        newTempo._mpq = mpq;
        newTempo._tick = tick;
        newTempo._microsecond = tempo._microsecond + (long)deltaMicrosecond;

        add(newTempo);
    }

    /* Microsecond以前の、最後のSMFTempoを返す */
    SMFTempo backSeekByMicroSecond(long us) {
        SMFTempo ret = get(0);
        for (int x = 1; x < size(); x++) {
            SMFTempo seek = get(x);
            if (us >= seek._microsecond) {
                ret = seek;
            }
        }
        return ret;
    }

    /* Microsecondから、Tickを求める */
    public long calcTicksByMicrosecond(long microSecond) {
        SMFTempo tempo = backSeekByMicroSecond(microSecond);
        
        double deltaMicroSecond = microSecond - tempo._microsecond;
        double deltaTick = deltaMicroSecond * _parent._fileResolution / tempo._mpq;

        return tempo._tick + (long)deltaTick;
    }

    /* MicrosecondベースでMPQを追加 （順番通りに追加する必要がある） */
    public void addMPQwithMicrosecond(long mpq, long microSecond) {
        SMFTempo tempo = backSeekByMicroSecond(microSecond);

        double deltaMicroSecond = microSecond - tempo._microsecond;
        double deltaTick = deltaMicroSecond * _parent._fileResolution / tempo._mpq;

        if (deltaMicroSecond == 0) {
            //上書き
            tempo._mpq = mpq;
            return;
        }
        if (tempo._mpq == mpq) {
            //変化なし
            return;
        }

        SMFTempo newTempo = new SMFTempo();

        newTempo._mpq = mpq;
        newTempo._tick = tempo._tick + (long)deltaTick;
        newTempo._microsecond = microSecond;

        add(newTempo);
    }
}
