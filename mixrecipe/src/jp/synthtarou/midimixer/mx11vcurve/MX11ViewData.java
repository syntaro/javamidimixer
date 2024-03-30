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
package jp.synthtarou.midimixer.mx11vcurve;

import jp.synthtarou.midimixer.MXConfiguration;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX11ViewData {
    int _portCount;
    static int[] DEFAULT_CURVE =  new int[] {
            1, 25, 50, 75, 100, 127
    };
    int[][] _vc;

    public MX11ViewData() {
        _portCount = MXConfiguration.TOTAL_PORT_COUNT;
        _vc = new int[_portCount][];
        for (int i = 0; i < _portCount; ++ i) {
            _vc[i] = new int[DEFAULT_CURVE.length];
            for (int j = 0; j < DEFAULT_CURVE.length; ++ j) {
                _vc[i][j] = DEFAULT_CURVE[j];
            }
        }
    }

    public int portCount() {
        return _portCount;
    }
    
    public int curveCount() {
        return DEFAULT_CURVE.length;
    }
    
    public int curveDefault(int pos) {
        return DEFAULT_CURVE[pos];
    }
    
    public int curveValue(int port, int pos) {
        return _vc[port][pos];
    }

    public void setCurveValue(int port, int pos, int value) {
        _vc[port][pos] = value;
    }
    
    public int transform(int port, int velocity) {
        if (velocity == 0) return 0;
        if (velocity >= 128) return velocity;

        int[] curve = _vc[port];
        
        for (int j = 0; j < curve.length - 1; j ++) {
            int prev = DEFAULT_CURVE[j];
            int next = DEFAULT_CURVE[j+1];

            int prevTo = curve[j];
            int nextTo = curve[j+1];

            if (velocity == prev) {
                return prevTo;
            }
            if (velocity == next) {
                return nextTo;
            }

            if (velocity >= prev + 1 && velocity <= next) {
                if (prev == prevTo && next == nextTo) {
                    return velocity;
                }
                double width = next - prev;
                double widthTo = nextTo - prevTo;
                
                double v = velocity;
                double step = v - prev;

                double percent = step / width;
                double stepTo = widthTo * percent;
              
                return (int)stepTo + prevTo;
            }
        }        
        return velocity;
    }
}
