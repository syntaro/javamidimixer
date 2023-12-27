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
package jp.synthtarou.midimixer.libs.common;

import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXRangedValue implements Comparable<MXRangedValue>{

    public static MXRangedValue[] _cache128;

    static {
        // 大した、メモリ負荷はないが、いちおうできそうなので、キャッシュ
        _cache128 = new MXRangedValue[128];
        for (int i = 0; i < 128; ++i) {
            _cache128[i] = new MXRangedValue(i, 0, 127);
        }
    }

    public static final MXRangedValue ZERO7 = _cache128[0];

    public static final MXRangedValue new7bit(int x) {
        return _cache128[x];
    }

    public static final MXRangedValue new14bit(int x) {
        return new MXRangedValue(x, 0, 128 * 128 - 1);
    }

    public final int _min;
    public final int _max;
    public final int _count;

    public final int _value;
    public final double _position;

    public MXRangedValue(int value, int min, int max) {
        this(value, min, max, calcPosition(value, min, max));
    }

    static double calcPosition(int value, int min, int max) {
        if (min <= max) {
            int count = max - min;
            return (value - min) * 1.0 / count;
        } else {
            int count = min - max;
            return (min - value) * 1.0 / count;
        }
    }

    protected MXRangedValue(int value, int min, int max, double position) {
        _min = min;
        _max = max;
        if (min <= max) {
            _count = max - min + 1;
        } else {
            _count = min - max + 1;
        }

        _value = value;
        _position = position;
    }

    public MXRangedValue changeRange(int newMin, int newMax) {
        if (newMin == _min && newMax == _max) {
            return this;
        }

        int newValue;

        if (newMin < newMax) {
            int roomCountTo = newMax - newMin + 1;
            double toD = _position * roomCountTo;
            
            double multi = (double) roomCountTo / _count;

            newValue = newMin + (int) Math.floor(toD);
            
            if (newValue > newMax) {
                newValue = newMax;
            }
        } else {
            int roomCountTo = newMin - newMax + 1;
            double toD = _position * roomCountTo;

            double multi = (double) roomCountTo / _count;

            newValue = newMin - (int) Math.floor(toD);
            if (newValue < newMin) {
                newValue = newMin;
            }
        }

        if (newMin == 0 && newMax == 127) {
            if (newValue >= 0 && newValue <= 127) {
                MXRangedValue t = new7bit(newValue);
                if (t._position == _position) {
                    return t;
                }
            }
        }
        return new MXRangedValue(newValue, newMin, newMax, _position);
    }

    public static void test0(int oldMin, int oldMax, int newMin, int newMax) {
        TreeMap<Integer, Integer> count = new TreeMap();
        MXRangedValue sample = new MXRangedValue(0, oldMin, oldMax);
        // １．分布を集計する

        if (oldMin <= oldMax) {
            for (int i = oldMin; i <= oldMax; ++i) {
                MXRangedValue calc = sample.changeValue(i).changeRange(newMin, newMax);
                Integer x = count.get(calc._value);
                if (x == null) {
                    x = 0;
                }
                count.put(calc._value, x + 1);
            }
        } else {
            for (int i = oldMin; i >= oldMax; --i) {
                MXRangedValue calc = sample.changeValue(i).changeRange(newMin, newMax);
                Integer x = count.get(calc._value);
                if (x == null) {
                    x = 0;
                }
                count.put(calc._value, x + 1);
            }
        }

        Set<Integer> keys = count.keySet();

        int minKey = Integer.MAX_VALUE; // 値の最小
        int maxKey = Integer.MIN_VALUE; // 値の最大

        int minCount = Integer.MAX_VALUE; // 値箱の中身の数（アベレージ最小）
        int maxCount = Integer.MIN_VALUE; // 値箱の中身の数（アベレージ最大）

        int boxCount = 0;

        for (Integer seek : keys) {
            boxCount++;
            if (seek < minKey) {
                minKey = seek;
            }
            if (seek > maxKey) {
                maxKey = seek;
            }
            Integer value = count.get(seek);
            if (value < minCount) {
                minCount = value;
            }
            if (value > maxCount) {
                maxCount = value;
            }
        }

        MXRangedValue dump = new MXRangedValue(0, newMin, newMax);

        System.out.println("");
        System.out.println("元の幅：" + oldMin + " ~ " + oldMax + " = " + sample._count + " 通り");
        System.out.println("新しい幅：" + newMin + " ~ " + newMax + " = " + dump._count + " 通り");
        System.out.println("箱の数:" + boxCount + " 新最小値：" + minKey + " 新最大値：" + maxKey);
        if (minCount == maxCount) {
            System.out.println("わけた箱の中身は、すべて " + minCount + " 個で統一されています。");
        } else {
            System.out.println("わけた箱の中身は、" + minCount + " 個から " + maxCount + " 個の幅でゆれています。");

            int sepa = 0;
            for (Integer key : count.keySet()) {
                Integer value = count.get(key);
                if (sepa == 0) {
                    System.out.print("   ");
                }
                System.out.print(key + "=" + value + "個, ");
                if (++sepa >= 16) {
                    sepa = 0;
                    System.out.println("");
                }
            }
            if (sepa != 0) {
                System.out.println("");
            }
        }
    }

    public static void test(int oldMin, int oldMax, int newMin, int newMax) {
        test0(oldMin, oldMax, newMin, newMax);
        test0(oldMin, oldMax, newMax, newMin);
        test0(oldMax, oldMin, newMin, newMax);
        test0(oldMax, oldMin, newMax, newMin);
    }
    
    public boolean contains(int x) {
        if (_min < _max) {
            if (_min <= x && x <= _max) {
                return true;
            }
        }else {
            if (_max <= x && x <= _min) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        MXUtil.fixConsoleEncoding();

        //test(0, 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3, 0, 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2);
        //test(0, 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2, 0, 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3);

        int offset = 0;
        int newOffset = 0;

        test(offset + 0, offset + 127, newOffset + 0, newOffset + 32767);
        test(offset + 0, offset + 32767, newOffset + 0, newOffset + 127);
        test(offset + 0, offset + 99, newOffset + 0, newOffset + 9999);
        test(offset + 0, offset + 9999, newOffset + 0, newOffset + 99);
        test(offset + 0, offset + 2, newOffset + 0, newOffset + 3);
        test(offset + 0, offset + 3, newOffset + 0, newOffset + 2);
        test(offset + 0, offset + 20, newOffset + 0, newOffset + 300);
        test(offset + 0, offset + 300, newOffset + 0, newOffset + 20);

    }

    public MXRangedValue increment() {
        return changeValue(_value + (_min < _max ? 1 : -1));
    }

    public MXRangedValue decrement() {
        return changeValue(_value + (_min < _max ? -1 : 1));
    }

    public MXRangedValue changeValue(int value) {
        if (_value == value) {
            return this;
        }
        if (_min < _max) {
            if (value < _min) {
                value = _min;
            }
            if (value > _max) {
                value = _max;
            }
        } else {
            if (value < _max) {
                value = _max;
            }
            if (value > _min) {
                value = _min;
            }
        }
        if (_min == 0 && _max == 127) {
            return _cache128[value];
        }
        return new MXRangedValue(value, _min, _max);
    }

    @Override
    public String toString() {
        return "" + _value + " (" + _min + "-"  + _max + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MXRangedValue) { 
            MXRangedValue x = (MXRangedValue)obj;
            if (x._value == _value) {
                if (x._min == _min && x._max == _max) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int compareTo(MXRangedValue o) {
        int x = this._value - o._value;
        if (x == 0) {
            x = this._min - o._min;
            if (x == 0) {
                x = this._max - o._max;
            }
        }
        if (x < 0) return -1;
        if (x > 0) return 1;
        return 0;
    }
}
