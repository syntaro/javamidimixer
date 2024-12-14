/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.synth.soundfont;

import java.io.IOException;
import java.util.ArrayList;
import java.io.InputStream;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class BytesAppender {
    ArrayList<byte[]> _listAll;
    
    public BytesAppender(InputStream input) throws IOException {
        _listAll = new ArrayList<>();
        while(true) {
            byte[] data = new byte[32768];
            int x = input.read(data, 0, data.length);
            if (x < 0) { 
                return;
            }
            appendReused(data, x);
        }
    }
    
    public void append(byte[] data, int length) {
        byte[] copy = new byte[length];
        for (int i = 0; i < length; ++ i) {
            copy[i] = data[i];
        }
        appendReused(copy, length);
    }

    protected void appendReused(byte[] data, int length) {
        if (length > 0) {
            if (length != data.length) {
                byte[] copy = new byte[length];
                for (int i = 0; i < length; ++ i) {
                    copy[i] = data[i];
                }
                data = copy;
            }
            _listAll.add(data);
        }
    }
    
    public int size() {
        int x = 0;
        for (byte[] data : _listAll) {
            x += data.length;
        }
        return x;
    }

    public byte[] toByteArray() {
        byte[] result = new byte[size()];
        int x = 0;
        for (byte[] data : _listAll) {
            for (int i = 0; i < data.length; ++ i) {
                result[x++] = data[i];
            }
        }
        return result;
    }
}
