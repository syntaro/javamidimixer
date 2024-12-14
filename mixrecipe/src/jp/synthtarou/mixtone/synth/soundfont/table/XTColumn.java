/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.synth.soundfont.table;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTColumn {
    public XTColumn(String value) {
        _textValue = value;
    }

    public XTColumn(Number value) {
        _numberValue = value;
    }

    public XTColumn(XTTable value) {
        _extraValue = value;
    }
    
    public String textValue() {
        return _textValue;
    }
    
    public Number numberValue() {
        return _numberValue;
    }

    public XTTable extraValue() {
        return _extraValue;
    }

    String _textValue;
    Number _numberValue;
    XTTable _extraValue;
    
    public String toString() {
        if (_numberValue != null) {
            return _numberValue.toString();
        }
        if (_textValue != null) {
            return '"' + _textValue + '"';
        }
        if (_extraValue != null) {
            return "(" + _extraValue.size() + " rows)";
        }
        return "none";
    }
}
