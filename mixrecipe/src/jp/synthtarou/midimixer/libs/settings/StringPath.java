/*
 * Copyright 2023 Syntarou YOSHIDA.
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
package jp.synthtarou.midimixer.libs.settings;

import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class StringPath extends ArrayList<String> implements Cloneable {

    public StringPath() {
        super();
    }

    public StringPath clone() {
        StringPath p = new StringPath();
        p.addAll(this);
        return p;
    }
    
    public static StringPath parsePath(String name) {
        StringPath path = new StringPath();

        char[]buffer = name.toCharArray();
        StringBuffer writingChar = new StringBuffer();

        for (int x = 0; x < buffer.length; ++ x) {
            char ch = buffer[x];
            if (ch == '.') {
                String part = writingChar.toString();
                writingChar = new StringBuffer();
                if (part.length() >= 1) {
                    if (part.charAt(0) >= '0' && part.charAt(0) <= '9') {
                        try {
                            Integer.parseInt(part);
                        }catch(NumberFormatException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                    path.add(part);
                }
                continue;
            }
            if (ch == '[') {
                if (writingChar.length() >= 1) {
                    String part = writingChar.toString();
                    writingChar = new StringBuffer();
                    if (part.length() >= 1) {
                        if (part.charAt(0) >= '0' && part.charAt(0) <= '9') {
                            try {
                                Integer.parseInt(part);
                            }catch(NumberFormatException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                        path.add(part);
                    }
                }
                int nextClose = x + 1;
                while(nextClose < buffer.length) {
                    if (buffer[nextClose] == ']') {
                        break;
                    }
                    nextClose ++;
                }
                if (nextClose - x - 1 >= 0) {
                    String number = new String(buffer, x + 1, nextClose - x - 1);
                    if (number.length() == 0) {
                        number = "0";
                    }
                    try {
                        Integer.parseInt(number);
                        path.add(number);
                    }catch(NumberFormatException e) {
                        e.printStackTrace();
                        return null;
                    }
                }else {
                    throw new NumberFormatException("length < 0");
                }
                if (nextClose + 1 < buffer.length) {
                    if (buffer[nextClose + 1] == '.') {
                        nextClose ++;
                    }
                }
                x = nextClose;
                continue;
            }
            writingChar.append(ch);
        }
        if (writingChar.length() >= 1) {
            String part = writingChar.toString();
            writingChar = new StringBuffer();
            if (part.length() >= 1) {
                if (part.charAt(0) >= '0' && part.charAt(0) <= '9') {
                    try {
                        Integer.parseInt(part);
                    }catch(NumberFormatException e) {
                        throw e;
                    }
                }
                path.add(part);
            }
        }
        return path;
    }
 }
