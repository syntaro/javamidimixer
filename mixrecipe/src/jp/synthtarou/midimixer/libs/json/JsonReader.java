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
package jp.synthtarou.midimixer.libs.json;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import jp.synthtarou.midimixer.libs.common.MXLogger2;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class JsonReader {

    String _data;
    int _pos;
    File _file;

    public JsonReader(File file) throws IOException {
        _file = file;
        _data = readFile(file, "utf-8");
        int zero = 0;
        int nonZero = 0;
        for (int i = 0; i < _data.length(); ++ i) {
            if (_data.charAt(i) == 0) {
                zero ++;
            }
            else {
                nonZero ++;
            }
        }
        if (nonZero >= 1 && (nonZero - zero) >= -5 && (nonZero - zero) <= 5) {
            System.out.println("******************RELOADED***************");
            _data = readFile(file, "Unicode");
        }
        _pos = 0;
    }

    public JsonReader(String text) {
        _file = null;
        _data = text;
        _pos = 0;
    }

    public int peek() {
        if (_pos < _data.length()) {
            return _data.charAt(_pos);
        }
        return -1;
    }

    public int peek(int offset) {
        if (_pos + offset < _data.length()) {
            return _data.charAt(_pos + offset);
        }
        return -1;
    }

    public void skip(int offset) {
        _pos += offset;
    }

    public int read() {
        if (_pos < _data.length()) {
            return _data.charAt(_pos++);
        }
        return -1;
    }

    public String readFile(File file, String encoding) throws IOException {
        InputStream in = null;
        StringBuffer result = new StringBuffer();
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            BufferedReader r = new BufferedReader(new InputStreamReader(in, encoding));
            char[] buffer = new char[1024];
            while (true) {
                String text = r.readLine();
                if (text == null) {
                    break;
                }
                if (text.startsWith("//") && result.length() == 0)  {
                    continue;
                }
                result.append(text);
                result.append("\n");
            }
            return result.toString();
        } catch (EOFException ex) {
            ex.printStackTrace();
            return result.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            MXLogger2.getLogger(JsonParser.class).log(Level.SEVERE, ex.getMessage(), ex);
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex2) {
                    MXLogger2.getLogger(JsonParser.class).log(Level.SEVERE, ex2.getMessage(), ex2);
                }
            }
            return null;
        }
    }

    public void skipBlank() {
        int ch = peek();
        while (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
            read();
            ch = peek();
        }
    }

    public boolean skipMagicNumber(String text) {
        for (int i = 0; i < text.length(); ++i) {
            if (_pos + i < _data.length()) {
                if (_data.charAt(_pos + i) == text.charAt(i)) {
                    continue;
                } else {
                    return false;
                }
            }
        }
        _pos += text.length();
        return true;
    }

    public static String encape(String unescaped) {
        int len = unescaped.length();
        int pos = 0;
        StringBuffer buffer = new StringBuffer();
        try {
            while (pos < len) {
                char ch = unescaped.charAt(pos ++);
                switch(ch) {
                    case '"':
                        buffer.append("\\\"");
                        break;
                    case '\\':
                        buffer.append("\\\\");
                        break;
                    case '/':
                        buffer.append("/");
                        break;
                    case '\b':
                        buffer.append("\\b");
                        break;
                    case '\f':
                        buffer.append("\\f");
                        break;
                    case '\n':
                        buffer.append("\\n");
                        break;
                    case '\r':
                        buffer.append("\\r");
                        break;
                    case '\t':
                        buffer.append("\\t");
                        break;
                    default:
                        if (ch >= 0 && ch <= 0x1f) {
                           buffer.append("\\u");
                           String hex = Integer.toHexString(ch);
                           while (hex.length() < 4) {
                                hex = " "  + hex;
                           }
                           buffer.append(hex);
                           break;
                        }
                        buffer.append(ch);
                        break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();;
        }
        return "\"" + buffer.toString() + "\"";
    }
    
    public static String unescape(String escaped) {
        int len = escaped.length();
        int pos = 0;
        StringBuffer buffer = new StringBuffer();
        boolean inDQuote = false;
        try {
            while (pos < len) {
                int ch = escaped.charAt(pos);
                if (!inDQuote) {
                    if (ch == '"') {
                        inDQuote = true;
                        pos++;
                        continue;
                    }
                    buffer.append((char) ch);
                    pos ++;
                    continue;
                } else {
                    if (ch == '\"') {
                        inDQuote = false;
                        pos ++;
                        continue;
                    }else if (ch == '\\') {
                        if (pos + 1 >= len) {
                            throw new IllegalArgumentException("pos +1 >= len when detected \\");
                        }
                        int ch1 = escaped.charAt(pos + 1);
                        switch (ch1) {
                            case '"':
                                buffer.append("\"");
                                pos += 2;
                                break;
                            case '\\':
                                buffer.append("\\");
                                pos += 2;
                                break;
                            case '/':
                                buffer.append("/");
                                pos += 2;
                                break;
                            case 'b': //バックスぺース
                                buffer.append("\n");
                                pos += 2;
                                break;
                            case 'f': //改ページ
                                buffer.append("\f");
                                pos += 2;
                                break;
                            case 'n': //改行
                                buffer.append("\n");
                                pos += 2;
                                break;
                            case 'r': //リターン
                                pos += 2;
                                break;
                            case 't': //タブ
                                buffer.append("\t");
                                pos += 2;
                                break;
                            case 'u': //文字コード (4桁) 0000~001f
                                try {
                                    if (pos + 6 <= len) {
                                        String hex = escaped.substring(pos + 2, pos + 6);
                                        int built = Integer.parseInt(hex, 16);
                                        buffer.append(Character.valueOf((char) built));
                                        pos += 6;
                                        continue;
                                    } else {
                                    }
                                } catch (NumberFormatException ex) {
                                }
                                buffer.append('\\');
                                buffer.append(Character.valueOf((char)ch1));
                                pos += 2;
                                break;
                            default:
                                buffer.append('\\');
                                buffer.append(Character.valueOf((char)ch1));
                                pos += 2;
                                break;
                        }
                        continue;
                    } else {
                        buffer.append((char) ch);
                        pos ++;
                    }
                }

            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();;
        }
        return buffer.toString();
    }

    public String readPartial() {
        boolean inDQuote = false;
        StringBuffer text = new StringBuffer();
        if (peek() == '"') {
            inDQuote = true;
            text.append("\"");
            skip(1);
        }
        while (true) {
            int ch = peek();
            if (ch < 0) {
                break;
            }
            if (inDQuote) {
                if (ch == '\\') {
                    ch = peek(1);
                    if (ch >= 0) {
                        text.append("\\");
                        text.append((char) ch);
                        skip(2);
                    }else {
                        text.append("\\");
                        skip(1);
                    }
                }
                else if (ch == '"') {
                    text.append("\"");
                    skip(1);
                    inDQuote = false;
                    break;
                } else if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                    text.append((char) ch);
                    skip(1);
                } else {
                    text.append((char) ch);
                    skip(1);
                }
            } else {
                ch = peek();
                if (ch == '"') {
                    inDQuote = true;
                    text.append((char) ch);
                    skip(1);
                    continue;
                }
                char[] separatorKind = {',', '[', ']', '{', '}', ':'};
                boolean hit = false;
                for (int i = 0; i < separatorKind.length; ++ i) {
                    if (ch == separatorKind[i]) {
                        hit = true;
                        break;
                    }
                } 
                if (hit) {
                    if (text.length() == 0) {
                        skip(1);
                        text.append((char) ch);
                        return text.toString();
                    }else {
                        break;
                    }
                }
                if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                    skip(1);
                    if (text.length() > 0) {
                        break;
                    }
                } else {
                    text.append((char) ch);
                    skip(1);
                }
            }
            String t = text.toString();
            if (t.equals("[") || t.equals("]") || t.equals('{') || t.equals('}')) {
                return t;
            }
        }
        if (text.length() == 0) {
            if (peek() < 0) {
                return null;
            }
        }
        return text.toString();
    }

    public boolean isNumericCharSub(int ch) {
        if (ch >= '0' && ch <= '9') {
            return true;
        }
        if (ch == '+' || ch == '-' || ch == '.') {
            return true;
        }
        return false;
    }

    public boolean isCapitalNumericCharSub(int ch) {
        if (ch >= '0' && ch <= '9') {
            return true;
        }
        if (ch == '+' || ch == '-') {
            return true;
        }
        return false;
    }
}
