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
package jp.synthtarou.libs.json;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import jp.synthtarou.libs.log.MXFileLogger;

/**
 * jsonファイルを読み込むため
 * @author Syntarou YOSHIDA
 */
public class MXJsonParsersReader {

    String _data;
    int _pos;
    File _file;
    
    /**
     * 文字列化
     * @return ファイル名または、"String"または、"null"
     */
    public String toString() {
        if (_file != null) {
            return _file.toString();
        }
        if (_data != null) {
            return "String";
        }
        return "null";
    }

    /**
     * ファイルを指定してインスタンスを生成する
     * 読み込みまで行う（パースは readPartialでおこなっている)
     * 主に,Utf8が用いられているが、まれにUnicode形式なこともある
     * 自動検出(utf or unicodeの2種類のみ)した文字コードでデコードする
     * @param file File ファイルを指定
     * @throws java.io.FileNotFoundException　ファイルが開けない場合
     * @throws IOException 読込エラー ファイルが開けない場合
     */
    public MXJsonParsersReader(File file) throws FileNotFoundException, IOException {
        _file = file;
        _data = readFileImpl(file, "utf-8");
        int zero0 = 0;
        int zero1 = 0;
        int nonZero = 0;
            for (int i = 0; i < _data.length(); ++ i) {
                if (_data.charAt(i) == 0) {
                    if ((i & 1) == 0) {
                        zero0 ++ ;
                    }
                    else {
                        zero1 ++;
                    }
                }
                else {
                    nonZero ++;
                }
            }
            if (nonZero >= 1 && (nonZero - zero0 - zero1) >= -5 && (nonZero - zero0 - zero1) <= 5) {
                MXFileLogger.getLogger(MXJsonParsersReader.class).info("RELOADED");
                if (zero0 > zero1) {
                    _data = readFileImpl(file, "UTF-16BE");
                }else {
                    _data = readFileImpl(file, "UTF-16LE");
                }
            }
        _pos = 0;
    }

    /**
     * 文字列からインスタンスを生成する
     * @param text readPartial用の文字列を設定する
     */
    public MXJsonParsersReader(String text) {
        _file = null;
        _data = text;
        _pos = 0;
    }

    /**
     * 先頭１文字目を読み取る（読み取り位置は更新しない）
     * @return 文字コード、EOFの場合-1
     */
    public int peek() {
        if (_pos < _data.length()) {
            return _data.charAt(_pos);
        }
        return -1;
    }

    /**
     * 先頭ｎ文字目を読み取る（読み取り位置は更新しない）
     * @param offset 何文字目を読み取るか（日本語にするとoffset=0で最初の文字)
     * @return 文字コード、EOFの場合-1
     */
    public int peek(int offset) {
        if (_pos + offset < _data.length()) {
            return _data.charAt(_pos + offset);
        }
        return -1;
    }

    /**
     * offset分スキップする
     * @param offset offset
     */
    public void skip(int offset) {
        _pos += offset;
    }

    /**
     * 戦闘文字を取得し、1文字すすめる
     * @return 文字コード、EOFの場合-1
     */
    public int read() {
        if (_pos < _data.length()) {
            return _data.charAt(_pos++);
        }
        return -1;
    }

    /**
     *　ファイルを読み込む
     * @param file ファイル
     * @param encoding 文字コード
     * @return String 読み込まれた文字列
     * @throws java.io.FileNotFoundException 読み取りに失敗した
     * @throws IOException 読み取りに失敗した
     */
    public String readFileImpl(File file, String encoding) throws FileNotFoundException, IOException {
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
                result.append(text);
                result.append("\n");
            }
            return result.toString();
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (EOFException ex) {
            return result.toString();
        } catch (IOException ex) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex2) {
                    MXFileLogger.getLogger(MXJsonParsersReader.class).log(Level.SEVERE, ex2.getMessage(), ex2);
                }
            }
            throw  ex;
        }
    }

    /**
     * 空白をスキップする
     */
    public void skipBlank() {
        int ch = peek();
        while (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
            read();
            ch = peek();
        }
    }

    /**
     * マジックナンバーをスキップする
     * @param text マジックナンバー
     * @return 検出してスキップしたばあいtrue
     */
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

    /**
     * ファイルを文字列パーツに分解して1件づつ取得する
     * @return タイプのつく文字列パーツ
     */
    public String readPartial() {
        boolean inDQuote = false;
        boolean inComment = false;
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
            } else if (inComment) {
                if (ch == '*' && peek(1) == '/') {
                    skip(2);
                    inComment = false;
                    continue;
                }
                skip(1);
            } else {
                ch = peek();
                if (ch == '/') {
                    if (peek(1) == '*') {
                        inComment = true;
                        skip(2);
                        continue;
                    }
                    if (peek(1) == '/') {
                        skip(2);
                        while (true) {
                            ch = read();
                            if (ch < 0) {
                                break;
                            }
                            if (ch == '\n') {
                                break;
                            }
                        }
                        if (ch < 0) {
                            break;
                        }
                        continue;
                    }
                }
                if (ch == '"') {
                    inDQuote = true;
                    text.append((char) ch);
                    skip(1);
                    continue;
                }
                if (",[]{}:".indexOf(ch) >= 0) {
                    if (text.length() == 0) {
                        skip(1);
                        text.append((char) ch);
                        return text.toString();
                    }else {
                        break;
                    }
                }
                if (" \t\r\n".indexOf(ch) >= 0) {
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

    /**
     * 文字コードが数値であるか
     * @param ch 文字コード
     * @return 数値とみなせる場合true
     */
    static boolean isNumericCharSub(int ch) {
        if (ch >= '0' && ch <= '9') {
            return true;
        }
        if (ch == '+' || ch == '-' || ch == '.') {
            return true;
        }
        return false;
    }

    /**
     * 文字コードが数値であるか、先頭につかえないコードは除外される
     * @param ch 文字コード
     * @return 数値とみなせる場合true
     */
    static boolean isCapitalNumericCharSub(int ch) {
        if (ch >= '0' && ch <= '9') {
            return true;
        }
        if (ch == '+' || ch == '-') {
            return true;
        }
        return false;
    }
}
