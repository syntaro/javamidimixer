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
public class RangedValue {

    public static RangedValue[] _cache128;

    static {
        // 大した、メモリ負荷はないが、いちおうできそうなので、キャッシュ
        _cache128 = new RangedValue[128];
        for (int i = 0; i < 128; ++i) {
            _cache128[i] = new RangedValue(i, 0, 127);
        }
    }

    public static final RangedValue ZERO7 = _cache128[0];

    public static final RangedValue new7bit(int x) {
        return _cache128[x];
    }

    public static final RangedValue new14bit(int x) {
        return new RangedValue(x, 0, 128 * 128 - 1);
    }

    public final int _min;
    public final int _max;
    public final int _count;

    public final int _var;
    public final double _position;

    public RangedValue(int value, int min, int max) {
        this(value, min, max, calcPosition(value, min, max));
    }

    static double calcPosition(int value, int min, int max) {
        if (min <= max) {
            int count = max - min + 1;
            return (value - min) * 1.0 / count;
        } else {
            int count = min - max + 1;
            return (min - value) * 1.0 / count;
        }
    }

    protected RangedValue(int value, int min, int max, double position) {
        _min = min;
        _max = max;
        if (min <= max) {
            _count = max - min + 1;
        } else {
            _count = min - max + 1;
        }

        _var = value;
        _position = position;
    }

    public RangedValue modifyRangeTo(int newMin, int newMax) {
        if (newMin == _min && newMax == _max) {
            return this;
        }

        int count;
        if (newMin < newMax) {
            count = newMax - newMin + 1;
        } else {
            count = newMin - newMax + 1;
        }

        int newValue;

        if (newMin < newMax) {
            int roomCountTo = newMax - newMin + 1;
            double toD = _position * roomCountTo;

            double multi = (double) roomCountTo / this._count;
            multi--;
            toD += multi / 2;

            newValue = newMin + (int) Math.round(toD);
        } else {
            int roomCountTo = newMin - newMax + 1;
            double toD = _position * roomCountTo;

            double multi = (double) roomCountTo / this._count;
            multi--;
            toD += multi / 2;

            newValue = newMin - (int) Math.round(toD);
        }

        if (newMin == 0 && newMax == 127) {
            if (newValue >= 0 && newValue <= 127) {
                RangedValue t = new7bit(newValue);
                if (t._position == _position) {
                    return t;
                }
            }
        }
        return new RangedValue(newValue, newMin, newMax, _position);
    }

    public static void test0(int oldMin, int oldMax, int newMin, int newMax) {
        TreeMap<Integer, Integer> count = new TreeMap();
        RangedValue sample = new RangedValue(0, oldMin, oldMax);
        // １．分布を集計する

        if (oldMin <= oldMax) {
            for (int i = oldMin; i <= oldMax; ++i) {
                sample = sample.updateValue(i);
                RangedValue calc = sample.modifyRangeTo(newMin, newMax);
                Integer x = count.get(calc._var);
                if (x == null) {
                    x = 0;
                }
                count.put(calc._var, x + 1);
            }
        } else {
            for (int i = oldMin; i >= oldMax; --i) {
                sample = sample.updateValue(i);
                RangedValue calc = sample.modifyRangeTo(newMin, newMax);
                Integer x = count.get(calc._var);
                if (x == null) {
                    x = 0;
                }
                count.put(calc._var, x + 1);
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

        RangedValue dump = new RangedValue(0, newMin, newMax);

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

        test(0, 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3, 0, 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2);
        test(0, 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2, 0, 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3);

        int offset = 1000;
        int newOffset = 1000;

        test(offset + 0, offset + 127, newOffset + 0, newOffset + 32767);
        test(offset + 0, offset + 32767, newOffset + 0, newOffset + 127);
        test(offset + 0, offset + 99, newOffset + 0, newOffset + 9999);
        test(offset + 0, offset + 9999, newOffset + 0, newOffset + 99);
        test(offset + 0, offset + 2, newOffset + 0, newOffset + 3);
        test(offset + 0, offset + 3, newOffset + 0, newOffset + 2);
        test(offset + 0, offset + 20, newOffset + 0, newOffset + 300);
        test(offset + 0, offset + 300, newOffset + 0, newOffset + 20);

    }

    public RangedValue increment() {
        return updateValue(_var + (_min < _max ? 1 : -1));
    }

    public RangedValue decrement() {
        return updateValue(_var + (_min < _max ? -1 : 1));
    }

    public RangedValue updateValue(int value) {
        if (_var == value) {
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
        return new RangedValue(value, _min, _max);
    }

    public String toString() {
        return String.valueOf(_var);
    }
}
