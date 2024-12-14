/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.synth.audio;

import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class DoubleOutputStream {
    class Segment {
        double[]_data = new double[44100];
        int _writePos = 0;
        int _readPos = 0;
    }

    ArrayList<Segment> _segment;
    final int _frequency = 44100;
    
    double _min = 100000000;
    double _max = -100000000;
    double _range = -1;
    
    public DoubleOutputStream() {
       _segment = new ArrayList<>();
    }
    
    Segment ensureForWrite() {
        if (_segment.size() == 0) {
            Segment seg = new Segment();
            _segment.add(seg);
            return seg;
        }
        Segment seg = _segment.get(_segment.size() - 1);
        if (seg._writePos >= seg._data.length) {
            seg = new Segment();
            _segment.add(seg);
        }
        return seg;
    }
    
    Segment ensureForRead() {
        while(_segment.isEmpty() == false) {
            Segment seek = _segment.get(0);
            if (seek._readPos < seek._writePos) {
                return seek;
            }
            else {
                _segment.remove(0);
            }
        }
        return null;
    }
   

    long _wroteCount = 0; 

    public void writeDouble(double x) {
       if (x > _max) { _max = x; }
       if (x < _min) { _min = x; }
       Segment seg = ensureForWrite();
       seg._data[seg._writePos ++] = x;
       _wroteCount ++;
    }
   
    public double readDouble() {
       if (_range < 0) {
           _range = (_max - _min) *4;
       }
       Segment seg = ensureForRead();
       if (seg == null) {
           return 0;
       }
       return seg._data[seg._readPos ++] / _range / 4;
   }
   
   public boolean isEnd() {
       if (ensureForRead() == null) {
           return true;
       }
       return false;
   }
   
   public long getAsMilliseconds() {
       return (long)(1000.0 * _wroteCount / _frequency);
   }
}
