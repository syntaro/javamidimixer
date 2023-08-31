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
package jp.synthtarou.midimixer.libs.midi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXTiming implements Comparable<MXTiming>{
    public static final Object mutex = MXTiming.class;
    
    public final long _clock;
    public final int _order;
    
    long[] _thisWrap = new long[10];

    static long _lastClock = 0;
    static int _lastOrder = 0;
    
    static long[] _totalWrap = new long[10];
    static long[] _totalBottom = new long[10];
    static long[] _totalCount = new long[10];
    
    public String toString() {
        return _clock + ":" + _order;
    }
    
    public MXTiming() {
        synchronized(MXTiming.class) {
            long t = System.currentTimeMillis();
            if (t < _lastClock) {
                System.err.print(" --- something bad --- (never happens before)");
                t = _lastClock;
            }
            _clock = t;
            if (_clock != _lastClock) {
                _lastClock = _clock;
                _lastOrder = 0;
            }
            _order = _lastOrder;
            _lastOrder ++;
            //new Throwable("" + _clock + "(" + _order + ")").printStackTrace();
        }
    }

    @Override
    public int compareTo(MXTiming o) {
        if (o == null) {
            return -1;
        }
        if (_clock < o._clock) return -1;
        if (_clock > o._clock) return  1;
        if (_order < o._order) return -1;
        if (_order > o._order) return  1;
        return 0;
    }
    
    public long recordWrap(int selector) {
        long wrap = System.currentTimeMillis() - this._clock;
        _thisWrap[selector] = wrap;
        _totalWrap[selector] += wrap;
        _totalCount[selector] ++;
        if (_totalBottom[selector] < wrap) {
            _totalBottom[selector] = wrap;
        }
        return wrap;
    }
    
    public long thisWrap(int selector) {
        return _thisWrap[selector];
    }

    public static long totalWrap(int selector) {
        return _totalWrap[selector];
    }
    
    public static long totalCount(int selector) {
        return _totalCount[selector];
    }

    public static long totalBottom(int selector) {
        return _totalBottom[selector];
    }
}
