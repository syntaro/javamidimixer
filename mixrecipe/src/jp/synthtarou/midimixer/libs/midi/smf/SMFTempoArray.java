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

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFTempoArray extends ArrayList<SMFTempo> {

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

        long deltaTick = ticks - tempo._tick;
        long deltaMicrosecond = deltaTick * tempo._mpq / _parent._fileResolution;

        long ret = tempo._microsecond + deltaMicrosecond;
        return ret;
    }

    /* TickベースでMPQを追加 （順番通りに追加する必要がある）  */
    public void addMPQwithTick(long mpq, long tick) {
        SMFTempo tempo = backSeekByTick(tick);

        long deltaTick = tick - tempo._tick;
        long deltaMicrosecond = deltaTick * tempo._mpq / _parent._fileResolution;
        

        if (deltaTick == 0) {
            tempo._mpq = mpq;
            return;
        }
        
        SMFTempo newTempo = new SMFTempo();
        newTempo._mpq = mpq;
        newTempo._tick = tick;
        newTempo._microsecond = tempo._microsecond + deltaMicrosecond;

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
    public long calcTicksByMicroseconds(long microSeconds) {
        SMFTempo tempo = backSeekByMicroSecond(microSeconds);
        
        long deltaMicroSeconds = microSeconds - tempo._microsecond;
        long deltaTicks = deltaMicroSeconds * _parent._fileResolution / tempo._mpq;

        return tempo._tick + deltaTicks;
    }

    /* MicrosecondベースでMPQを追加 （順番通りに追加する必要がある） */
    public void addMPQwithMicrosecond(long mpq, long microSeconds) {
        SMFTempo tempo = backSeekByMicroSecond(microSeconds);

        long deltaMicroSeconds = microSeconds - tempo._microsecond;

        if (deltaMicroSeconds == 0) {
            tempo._mpq = mpq;
            return;
        }

        long deltaTicks = deltaMicroSeconds / tempo._mpq * _parent._fileResolution;

        SMFTempo newTempo = new SMFTempo();

        newTempo._mpq = mpq;
        newTempo._tick = tempo._tick + deltaTicks;
        newTempo._microsecond = microSeconds;

        add(newTempo);
    }
}
