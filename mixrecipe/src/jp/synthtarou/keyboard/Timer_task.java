/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.keyboard;

import java.util.Timer;
import java.util.TimerTask;

public class Timer_task extends TimerTask {
    Keyboard keyboard;
    private int num = 0;
    private boolean endFlag = true;
    Timer timer;
    private long start_time;
    private long before_time;

    public void run() {
        num++;

        if(num>1000 && endFlag == true){
            System.out.println("End");
            timer.cancel();
        }
        keyboard.buffer_update();
        //System.out.print();
        if(num % 1000 == 0){
            keyboard.repaint();
        }
        send_elapsed_time_us();
        //send_elapsed_time();

    }
    public void send_elapsed_time(){
        long now_time = System.currentTimeMillis();
        long elapsed = now_time - this.before_time;
        /*
        System.out.print("elapsed [ms] ");
        System.out.println(elapsed);
        System.out.print(" ");
        System.out.print(now_time);
        System.out.print(" ");
        System.out.println(before_time);

         */
        this.before_time = now_time;
    }

    public void send_elapsed_time_us(){
        long now_time = System.nanoTime();
        long elapsed = now_time - this.before_time;
        //System.out.print("elapsed [us] ");
        //System.out.println((int)(elapsed/1000));
/*
        System.out.print(" ");
        System.out.print(now_time);
        System.out.print(" ");
        System.out.println(before_time);

 */
        this.before_time = now_time;
    }

    public void setKeyboard(Keyboard keyboard){
        this.keyboard = keyboard;
    }
    public void setTimer(Timer time3){
        this.timer = time3;
    }
    public void setEndTimer(){
        this.endFlag = true;
    }
    public void clearEndTimer(){
        this.endFlag = false;
    }
}