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

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXScaledNumber {
    int _fromsetRoomCount;
    int _tosetRoomCount;
    
    public MXScaledNumber(int fromsetRoomCount, int tosetRoomCount) {
        _fromsetRoomCount = fromsetRoomCount;
        _tosetRoomCount = tosetRoomCount;
    }
    
    public int getNewNumber(int from, boolean invert) {
        if (from == 0) {
            return 0;
        }
        if (from == _fromsetRoomCount - 1) {
            return _tosetRoomCount - 1;
        }

        double fromD = from;
        double toD = fromD * _tosetRoomCount / _fromsetRoomCount;
        
        double multi = (double)_tosetRoomCount  / _fromsetRoomCount;
        multi --;
        toD += multi / 2;
        
        long x = Math.round(toD);
        return (int)x;
    }
    
    public static void test(int rangeFrom, int rangeTo, boolean invert) {
        TreeMap<Integer, Integer> count = new TreeMap();
        MXScaledNumber exp = new MXScaledNumber(rangeFrom, rangeTo);
        // １．分布を集計する
        for (int i = 0; i < rangeFrom; ++ i) {
            int from = i;
            int to = exp.getNewNumber(from, invert);
            Integer x = count.get(to);
            if (x == null) {
                x = 0;
            }
            count.put(to, x + 1);
        }
        
        Set<Integer> keys = count.keySet();

        int minKey = 10000; // キーの最小
        int maxKey = -1; // キーの最大
        
        int minCount = 10000; // 値のカウントの最小
        int maxCount = -1; // 値のカウントの最大

        for (Integer seek : keys) {
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

        System.out.println("幅 " +  rangeFrom + "　から　" + rangeTo + " に伸縮します");
        System.out.println("  伸縮されて、:" + keys.size() + " 通りの分布ができました");
        System.out.println("  分布は、" + minKey + "から" + maxKey + " に分けられました");
        System.out.println("  わけた箱には、" + minCount + " から "+ maxCount +" 個づつ入っています");
    }
    
    public static void main(String[] args) {
        System.out.println("file.encoding = " + System.getProperty("file.encoding"));
        System.out.println("Charset.defaultCharset() = " + Charset.defaultCharset());
        System.out.println("System.out.charset() = " + System.out.charset());
        try {
            String targetCharset = Charset.defaultCharset().name();
            if (targetCharset.equals(System.out.charset()) == false) {                
                System.setOut(new PrintStream(System.out, true, targetCharset));
                System.setErr(new PrintStream(System.err, true, targetCharset));
            }
            System.out.println("System.out.charset() = " + System.out.charset());
            System.out.println("System.err.charset() = " + System.err.charset());
        }catch(Throwable e) {
            e.printStackTrace();
        }
        for (int x = 0; x <= 1; x++) {
            boolean invert = (x == 1);
            test(128, 32768, invert);
            test(32768, 128, invert);
            test(100, 10000, invert);
            test(10000, 100, invert);
            test(2, 3, invert);
            test(3, 2, invert);
            test(20, 300, invert);
            test(300, 20, invert);

            test(3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3, 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2, invert);
            test(2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2, 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3, invert);
        }
    }
}
