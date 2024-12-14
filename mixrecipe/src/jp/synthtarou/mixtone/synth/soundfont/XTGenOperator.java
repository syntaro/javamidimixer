/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
                String err = "param " + _name + " trim to min, param:" + param + " min: " + _min + " overflow " +wasOverflow;
                System.err.println(err);
                param = _min;
            }
        }
        if (_max != null) {
            if (param > _max) {
                String err = "param " + _name + " trim to max, param:" + param + " max: " + _max + " overflow " +wasOverflow;
                System.err.println(err);
                param = _max;
            }
        }
       
        return param;
    }
}
