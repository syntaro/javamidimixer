/*
 * Copyright (C) 2024 Syntarou YOSHIDA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jp.synthtarou.mixtone.synth.view.listmodel;

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
