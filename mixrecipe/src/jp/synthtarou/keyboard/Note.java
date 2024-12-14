/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.keyboard;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class Note {
    private int key;
    private double volume = 1;
    private int times = 0;
    private double frq = 1;
    private double angle_frq = 1;
    private double angle_frq_discrete = 1;
    private double sample_frq = 1;
    private boolean on_flag = false;

    public Note(int key,int note, int volume,double sample_frq){
        this.key = key;
        this.frq = 440 * Math.pow(2, (note-69) / 12.0);
        this.volume = volume;
        this.sample_frq = sample_frq;

        this.angle_frq = this.frq * 2 * Math.PI;
        this.angle_frq_discrete = this.angle_frq/this.sample_frq;
    }

    public double value(){
        //現在の位相の値における値の取得
        return this.volume * Math.sin(this.angle_frq_discrete * this.times) / 100;
    }

    public void set_volume(double volume){
        this.volume = volume;
    }
    
    public void clear_phase(){
        this.times = 0;
    }
    public void inc_phase(){
        this.times = this.times + 1;
    }
    public void set_on(){
        this.on_flag = true;
    }
    public void set_off(){
        this.on_flag = false;
    }
    public boolean get_state(){
        return this.on_flag;
    }
    public int get_key(){
        return this.key;
    }
}