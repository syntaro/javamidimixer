/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.listmodel;

import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class TextListForDebug extends ArrayList<String> {
    public TextListForDebug() {
        super();
    }

    public static String quote(String text) {
        boolean hit = false;
        for (int i = 0; i < text.length(); ++ i) {
            int ch = text.charAt(i);
            if (ch == '\'' || ch <= 0x1f) {
                hit = true;
                break;
            }
        }
        if (!hit) {
            return "\"" + text + "\"";
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append('"');
        for (int i = 0; i < text.length(); ++ i) {
            int ch = text.charAt(i);
            if (ch == '\'' || ch <= 0x1f) {
                buffer.append("\\" + Integer.toHexString(ch)+"h");
            }
            else {
                buffer.append(ch);
            }
        }
        buffer.append('"');
        return buffer.toString();
    }
}
