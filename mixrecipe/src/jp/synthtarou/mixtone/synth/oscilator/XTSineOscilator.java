/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.synth.oscilator;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTSineOscilator {
    private int times;
    private double _frequency;
    private double _angle_freq;
    private double _angle_freq_discrete;
    private double _sample_freq;

    public XTSineOscilator(int note){
        _frequency = 440 * Math.pow(2, (note-69) / 12.0);
        _sample_freq = 44100;
        times = 0;
        _angle_freq = _frequency * 2 * Math.PI;
        _angle_freq_discrete = _angle_freq/ _sample_freq;
    }

    public double step(){
        return Math.sin(_angle_freq_discrete * (times ++)) / 100;
    }
    
    public boolean isClose() {
        return times >= _sample_freq * 3;
    }

    public long _frame = 0;
}
