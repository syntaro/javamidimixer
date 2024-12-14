/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.synth.oscilator;

import jp.synthtarou.mixtone.synth.audio.DoubleOutputStream;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTWaveOscilator {
    DoubleOutputStream _buffer;
    
    public XTWaveOscilator(DoubleOutputStream buffer){
        _buffer = buffer;
    }

    public double step(){
        return _buffer.readDouble();
    }
    
    public boolean isClose() {
        return _buffer.isEnd();
    }

    public long _frame = 0;
}
