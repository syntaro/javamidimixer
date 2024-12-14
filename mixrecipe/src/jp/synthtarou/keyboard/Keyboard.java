/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.keyboard;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import static java.awt.event.KeyEvent.*;

public class Keyboard extends JFrame implements LineListener ,KeyListener {

    private final int height = 600;
    private final int width = 600;
    private final int buffer_size = 4000;
    private final int frame_size = 1000;

    private SourceDataLine source;

    List<Note> list_note = new LinkedList<Note>();

    private double[] wave_frame = new double[frame_size];
    private byte[] wave_frame_16 = new byte[frame_size * 2];

    public static void main(String[] args){
        System.out.println("Start main");
        Keyboard keyboard = new Keyboard();
        java.util.Timer timer = new Timer();
        Timer_task timer_task = new Timer_task();
        timer_task.setKeyboard(keyboard);
        timer_task.setTimer(timer);
        timer_task.clearEndTimer();
        timer.scheduleAtFixedRate(timer_task ,0,1);
    }

    public Keyboard(){
        //フレーム作成
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(this.width, this.height);
        setVisible(true);
        addKeyListener(this);

        //音符の作成
        Note C4 = new Note(VK_A, 64,20,44100);
        Note D4 = new Note(VK_S, 65,20,44100);
        Note E4 = new Note(VK_D, 66,20,44100);
        Note F4 = new Note(VK_F, 67,20,44100);
        Note G4 = new Note(VK_J, 68,20,44100);
        Note A4 = new Note(VK_K, 69,20,44100);
        Note B4 = new Note(VK_L,70,20,44100);
        Note C5 = new Note(59,71,20,44100);

        list_note.add(C4);
        list_note.add(D4);
        list_note.add(E4);
        list_note.add(F4);
        list_note.add(G4);
        list_note.add(A4);
        list_note.add(B4);
        list_note.add(C5);

        //C4.set_on();
        //D4.set_on();
        //E4.set_on();


        //音声出力設定
        AudioFormat frmt= new AudioFormat(44100,16,1,true,false);
        DataLine.Info info= new DataLine.Info(SourceDataLine.class,frmt);
        try {
            this.source = (SourceDataLine) AudioSystem.getLine(info);
        } catch (LineUnavailableException e) {
            System.out.println("cant get line///");
            throw new RuntimeException(e);
        }
        this.source.addLineListener(this);
        this.source.flush();
        try {
            this.source.open(frmt,buffer_size);
        } catch (LineUnavailableException e) {
            System.out.println("cant open line....");
            throw new RuntimeException(e);
        }
        this.source.start();
    }

    public void send_state(){
        /*
        System.out.print("active ");
        System.out.print(this.source.isActive());
        System.out.print(" , running ");
        System.out.println(this.source.isRunning());
        */
    }
    public void send_available(){
        /*
        System.out.print("available ");
        System.out.println(this.source.available());
       */
     }
    public void send_remain(){
        /*
        System.out.print("remain size ");
        System.out.println(this.buffer_size - this.source.available());
        */
    }
    public void send_buffer_size(){
        /*
        System.out.print("buffer size ");
        System.out.println(this.source.getBufferSize());
       */
     }

    public void calculate_buffer(double[] wave,int length){
        //バッファーの計算
        for(int i = 0;i < length;i++) {
            double value = 0;
            for (Note j : this.list_note) {
                if (j.get_state()) {
                    value += j.value();
                    j.inc_phase();
                }
            }
            wave[i] = value;
        }
    }

    @Override
    public void paint(Graphics g){
        Image imgBuf = createImage(this.width,this.height);
        Graphics gBuf = imgBuf.getGraphics();               //gBufがバッファの画像
        gBuf.setColor(Color.white);
        gBuf.fillRect(0,0,this.width,this.height);

        //各種　描画開始
        //System.out.println(this.source.getBufferSize());
        //System.out.println(this.source.available());
        //send_state();


        //描画完了
        Graphics graphics = getContentPane().getGraphics();
        graphics.drawImage(imgBuf,0,0,this);

    }
    public boolean buffer_update(){
        int now_available = this.source.available();
        if( this.buffer_size - now_available < this.frame_size){
            //bufferに残った量　＜　フレームサイズ
            this.calculate_buffer(this.wave_frame,this.wave_frame.length);
            
            int pos = 0;
            
            for (int x = 0; x < wave_frame.length; x ++) {
                int sampleInt = (int)(wave_frame[x] * (1<<7));
                if (wave_frame[x] > 1) {
                    sampleInt = 1 << 8;
                }else if (wave_frame[x] < -1) {
                    sampleInt = -(1 << 8);
                }

                wave_frame_16[pos + 0] = (byte)((sampleInt >> 8)  & 0xff);
                wave_frame_16[pos + 1] = (byte)(sampleInt  & 0xff);
                pos += 2;
            }
            //this.source.write(this.wave_frame,this.buffer_size - now_available,this.wave_frame.length);
            this.source.write(this.wave_frame_16,0,this.wave_frame_16.length);

            return true;
        }
        return false;

        //System.out.println("update buffer");
        //this.send_state();
    }

    @Override
    public void update(LineEvent event){
        //source_data_lineの open,close,start,stopイベント処理
        LineEvent.Type type = event.getType();
        if(type == LineEvent.Type.STOP){
            System.out.println("STOP source");
            this.buffer_update();
            //this.calculate_buffer(this.wave,this.wave.length);
            //this.source.write(this.wave,0,this.wave.length);
            System.out.println(this.source.isActive());

        }else if(type == LineEvent.Type.OPEN){
            System.out.println("OPEN source");
            //this.buffer_update();
            //this.calculate_buffer(this.wave,this.wave.length);
            //this.source.write(this.wave,0,this.wave.length);
        }else if(type == LineEvent.Type.START){
            System.out.println("START source");
        }

    }
    @Override
    public void keyTyped(KeyEvent e) {
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int key_code = e.getKeyCode();
        //System.out.println(key_code);

        for(Note i:list_note){
            if(i.get_key() == key_code){
                i.set_on();
            }
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        int key_code = e.getKeyCode();

        for(Note i:list_note){
            if(i.get_key() == key_code){
                i.set_off();
            }
        }
    }
}