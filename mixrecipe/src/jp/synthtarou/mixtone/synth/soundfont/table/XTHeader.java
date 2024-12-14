/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.synth.soundfont.table;

import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTHeader extends ArrayList<String> {
    public XTHeader() {
    }

    public XTHeader(String[] list) {
        addAll(list);
    }

    public void addAll(String[] list) {
        for (String seek : list) {
            add(seek);
        }
    }

    public String nameOf(int index) {
        return get(index);
    }
}
