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
package jp.synthtarou.mixtone.synth.soundfont;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTGenOperator {
    int _id;
    String _name;
    String _unit;
    
    Integer _generatorMin;
    Integer _generatorMax;

    Double _initial;
    Double _min;
    Double _max;

    SFZTranslator _trans = null;
    
    public String getName() {
        return _name;
    }
    
    public interface SFZTranslator {
         Double asParameter(int generator);
    }
    
    public XTGenOperator(int id, String name) {
        _id = id;
        _name = name;
    }

    public XTGenOperator unit(String unit)
    {
        _unit = unit;
        return this;
    }

    public XTGenOperator unit(String unit, SFZTranslator trans){
        _unit = unit;
        return this;
    }

    public XTGenOperator trans(SFZTranslator trans){
        _trans = trans;
        _min = null;
        _max = null;
        return this;
    }

    public XTGenOperator generator(Integer min, Integer max) {
        _generatorMin = min;
        _generatorMax = max;
        return this;
    }

    public XTGenOperator range(Double initial, Double min, Double max) {
        _initial = initial;
        _min = min;
        _max = max;
        return this;
    }
    
    public XTGenOperator range(Integer initial, Integer min, Integer max) {
        _initial = (initial == null) ? null : initial.doubleValue();
        _min = (min == null) ? null :  min.doubleValue();
        _max = (max == null) ? null : max.doubleValue();
        return this;
    }
    
    public Double getInitial() {
        return _initial;
    }

    public Double getMin() {
        return _min;
    }

    public Double getMax() {
        return _max;
    }
    
    public Double asParameter(int x) {
        boolean wasOverflow = false;
        if (_min != null) {
            if (x < _min) {
                if (_initial != null) {
                    return _initial;
                }
                wasOverflow = true;
            }
        }
        if (_max != null) {
            if (x > _max) {
                if (_initial != null) {
                    return _initial;
                }
                wasOverflow = true;
            }
        }
        Double param = x * 1.0;
        if (_trans != null) {
            param = _trans.asParameter(x);
        }
        if (_min != null) {
            if (param < _min) {
                /*
                String err = "param " + _name + " trim to min, param:" + param + " min: " + _min + " overflow " +wasOverflow;
                System.err.println(err);
                */
                param = _min;
            }
        }
        if (_max != null) {
            if (param > _max) {
                /*String err = "param " + _name + " trim to max, param:" + param + " max: " + _max + " overflow " +wasOverflow;
                System.err.println(err);
                */
                param = _max;
            }
        }
       
        return param;
    }
}
