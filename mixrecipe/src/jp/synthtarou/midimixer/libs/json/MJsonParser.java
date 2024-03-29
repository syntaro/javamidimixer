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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * jsonテキストを,MJsonValueオブジェクトのツリーに格納する、パーサー
 * @author Syntarou YOSHIDA
 */
public class MJsonParser {

    /**
     * 文字列からインスタンスを生成する。パースまでやる
     * @param text json文字列　nullの場合パースしない
     */
    public MJsonParser(String text) {
        if (text != null) {
            parse(text);
        }
    }

    /**
     * ファイルからインスタンスを生成する。パースまでやる
     * @param file jsonファイル
     * @throws java.io.IOException
     * @throws IOException　Fileエラー
     */
    public MJsonParser(File file) throws IOException {
        parse(file);

    }

    /**
     * あとから文字列をパースする
     * @param contents 文字列
     */
    public void parse(String contents) {
        _reader = new MJsonReader(contents);
        parseImpl(_reader);
    }

    /**
     * あとからファイルをパースする
     * @param file jsonファイル
     * @throws java.io.IOException
     * @throws IOException　Fileエラー
     */
    public void parse(File file) throws IOException {
        _reader = new MJsonReader(file);
        parseImpl(_reader);
    }

    ArrayList<String> _parse1 = null;
    static boolean g_dodebug = true;

    /**
     * MJsonReaderを指定してパースする、ほかのparseメソッドから呼ばれる
     * @param reader MJsonReader
     */
    protected void parseImpl(MJsonReader reader) {
        int point = 0;
        int total = 0;
        _treeRoot = null;
        _parse1 = new ArrayList<>();
        while (true) {
            String original = reader.readPartial();
            if (original == null) {
                break;
            }
            _parse1.add(original);
            if (g_dodebug) {
                String escaped1 = original;
                if (original.startsWith("\"")) {
                    //ok
                } else {
                    escaped1 = MJsonValue.escape(original);
                }
                String unescaped1 = MJsonValue.unescape(escaped1);
                String escaped2 = MJsonValue.escape(unescaped1);
                String unescaped2 = MJsonValue.unescape(escaped2);

                if (escaped1.equals(escaped2) == false || unescaped2.equals(unescaped1) == false) {
                    System.out.println("escaped1[" + escaped1 + "]");
                    System.out.println("escaped2[" + escaped2 + "]");
                    System.out.println("unescaped1[" + unescaped1 + "]");
                    System.out.println("unescaped2[" + unescaped2 + "]");
                    total++;
                } else {
                    point++;
                    total++;
                }
            }
        }
        //System.out.println("point " + point + " total  " + total);
        LinkedList<MJsonValue> queue = new LinkedList<>();
        for (String seek : _parse1) {
            queue.add(new MJsonValue(seek));
        }
        try {
            _treeRoot = readSomething(queue);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    MJsonValue _treeRoot;
    int currentReading = MJsonValue.TYPE_START_STRUCTURE;

    /**
     * キューに、type1~type4が並んでいるか調べる
     * @param queue キュー、MJsonValueのリスト
     * @param type1 タイプ
     * @param type2 タイプ
     * @param type3 タイプ
     * @param type4 タイプ
     * @return その通りに並んでいる場合
     */
    public boolean checkQueue(LinkedList<MJsonValue> queue, int type1, int type2, int type3, int type4) {
        if (queue.size() >= 4) {
            if (queue.get(0).getType() != type1 && type1 != MJsonValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(1).getType() != type2 && type2 != MJsonValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(2).getType() != type3 && type3 != MJsonValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(3).getType() != type4 && type4 != MJsonValue.TYPE_ANY) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * キューに、type1~typeが並んでいるか調べる
     * @param queue キュー、MJsonValueのリスト
     * @param type1 タイプ
     * @param type2 タイプ
     * @param type3 タイプ
     * @return その通りに並んでいる場合
     */
    public boolean checkQueue(LinkedList<MJsonValue> queue, int type1, int type2, int type3) {
        if (queue.size() >= 3) {
            if (queue.get(0).getType() != type1 && type1 != MJsonValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(1).getType() != type2 && type2 != MJsonValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(2).getType() != type3 && type3 != MJsonValue.TYPE_ANY) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * キューに、type1~type2が並んでいるか調べる
     * @param queue キュー、MJsonValueのリスト
     * @param type1 タイプ
     * @param type2 タイプ
     * @return その通りに並んでいる場合
     */
    public boolean checkQueue(LinkedList<MJsonValue> queue, int type1, int type2) {
        if (queue.size() >= 2) {
            if (queue.get(0).getType() != type1 && type1 != MJsonValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(1).getType() != type2 && type2 != MJsonValue.TYPE_ANY) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * キューに、type1が並んでいるか調べる
     * @param queue キュー、MJsonValueのリスト
     * @param type1 タイプ
     * @return その通りに並んでいる場合
     */
    public boolean checkQueue(LinkedList<MJsonValue> queue, int type1) {
        if (queue.size() >= 1) {
            if (queue.get(0).getType() != type1 && type1 != MJsonValue.TYPE_ANY) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * キューの先頭の5つの要素を、デバッグ目的を文字列化する
     * @param queue キュー
     * @return String
     */
    protected String first5ToText(LinkedList<MJsonValue> queue) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < 5; ++i) {
            if (i >= queue.size()) {
                break;
            }
            if (i != 0) {
                str.append(", ");
            }
            if (queue.get(i)._label == null) {
                throw new IllegalStateException();
            }
            str.append(queue.get(i).getTypeText() + "(" + queue.get(i)._label + ")");
        }
        return str.toString();
    }

    /**
     * リスト構造をパースする
     * @param queue キュー
     * @return リストを意味するMJsonValue
     */
    public MJsonValue enterList(LinkedList<MJsonValue> queue) {
        MJsonValue result = new MJsonValue(null);
        boolean commaFin = false;

        while (true) {
            if (checkQueue(queue, MJsonValue.TYPE_END_LIST)) {
                if (result._listContents == null) {
                    result.setContentsType(MJsonValue.CONTENTS_TYPE_LIST);
                }
                queue.removeFirst();
                if (commaFin) {
                    result.addToContentsArray(new MJsonValue(""));
                }
                break;
            } else if (checkQueue(queue, MJsonValue.TYPE_ANY)) {
                MJsonValue value = readSomething(queue);
                result.addToContentsArray(value);
                commaFin = false;
                if (checkQueue(queue, MJsonValue.TYPE_COMMA)) {
                    queue.removeFirst();
                    commaFin = true;
                    continue;
                }
                continue;
            } else if (queue.isEmpty()) {
                break;
            } else {
                System.out.println("can't read list " + first5ToText(queue));
            }
        }
        return result;
    }

    /**
     * 名前つきリスト構造、構造体をパースする
     * @param queue キュー
     * @return 構造体を意味するMJsonValue
     */
    public MJsonValue enterStructure(LinkedList<MJsonValue> queue) {
        MJsonValue result = new MJsonValue(null);
        boolean commaFin = false;

        while (true) {
            if (checkQueue(queue, MJsonValue.TYPE_END_STRUCTURE)) {
                if (result._listContents == null) {
                    result.setContentsType(MJsonValue.CONTENTS_TYPE_STRUCTURE);
                }
                queue.removeFirst();
                if (commaFin) {
                    result.addToContentsStructure(new MJsonValue(null));
                }
                break;
            } else if (checkQueue(queue, MJsonValue.TYPE_TEXT, MJsonValue.TYPE_DOUBLECOMMA, MJsonValue.TYPE_ANY)) {
                MJsonValue name2 = queue.removeFirst();
                MJsonValue dq = queue.removeFirst();
                MJsonValue value = readSomething(queue);
                result.addToContentsStructure(name2);
                name2.addToContentsValue(value);
                commaFin = false;

                if (checkQueue(queue, MJsonValue.TYPE_COMMA)) {
                    commaFin = true;
                    queue.removeFirst();
                    continue;
                }
            } else {
                throw new IllegalArgumentException("File format " + first5ToText(queue) + " file = " + _reader._file);
            }
        }
        return result;
    }

    /**
     * 現在のキューから、意味をふくめてパースする
     * @param queue キュー
     * @return 取り出したMJsonValue
     */
    public MJsonValue readSomething(LinkedList<MJsonValue> queue) {
        if (queue.isEmpty()) {
            return null;
        }

        MJsonValue operand = queue.removeFirst();
        MJsonValue result = null;
        switch (operand.getType()) {
            case MJsonValue.TYPE_NULL:
                //System.out.println(operand._name +" -> reading null ... "+ first5ToText(queue));
                return operand;

            case MJsonValue.TYPE_END_LIST:
            case MJsonValue.TYPE_END_STRUCTURE:
                //System.out.println(operand._name +" -> reading bad ... "+ first5ToText(queue));
                break;

            case MJsonValue.TYPE_TEXT:
                //System.out.println(operand._name +" -> reading text... "+ first5ToText(queue));
                return operand;

            case MJsonValue.TYPE_COMMA:
            case MJsonValue.TYPE_DOUBLECOMMA:
                //System.out.println(operand._name +" -> reading bad2 ... "+ first5ToText(queue));
                return operand;

            case MJsonValue.TYPE_START_STRUCTURE:
                //System.out.println(operand._name +" -> reading structure ... "+ first5ToText(queue));
                MJsonValue structure = enterStructure(queue);
                return structure;

            case MJsonValue.TYPE_START_LIST:
                //System.out.println(operand._name +" -> reading list ... "+ first5ToText(queue));
                MJsonValue list = enterList(queue);
                return list;

            default:
                break;
        }
        System.out.println("readed unknown " + first5ToText(queue));
        queue.removeFirst();
        return readSomething(queue);
    }

    MJsonReader _reader;

    static int count = 20000;
    static Throwable _fatal = null;

    /**
     * テスト用メソッド1
     * @param file ファイルをつかってロジックをテストする
     */
    public static void doTest(File file) {
        System.out.println("*PARSE*" + file);
        MJsonParser parser1;
        try {
            parser1 = new MJsonParser(file);
        } catch (IOException ex) {
            _fatal = ex;
            return;
        }
        String form = parser1._treeRoot.formatForDisplay();
        if (false) {
            System.out.println("*FORMAT*");        
            System.out.println(form);
        }
        //System.out.println("*DONE*");

        MJsonParser parser2 = null;
        try {
            parser2 = new MJsonParser(form);

            String form2 = parser2._treeRoot.formatForDisplay();
            if (form.equals(form2)) {
                //System.out.println("Test O");
            } else {
                System.out.println("Test X");
                _fatal = new Throwable("Test X");
            }
        } catch (Throwable ex) {
            _fatal = ex;
            ex.printStackTrace();
        }
        if (_fatal == null) {
            for (int i = 0; i < parser1._parse1.size(); ++i) {
                String t1 = i >= parser1._parse1.size() ? null : parser1._parse1.get(i);
                String t2 = i >= parser2._parse1.size() ? null : parser2._parse1.get(i);
                if (t1 == t2) {
                    continue;
                } else if (t1 == null || t2 == null) {

                } else if (t1.equals(t2)) {
                    continue;
                }
                System.out.println(parser1._reader._file + "  -> index " + i + " / " + parser1._parse1.size());
                System.out.println("t1 = " + t1);
                System.out.println("t2 = " + t2);
            }
            if (parser1._parse1.size() != parser2._parse1.size()) {
                System.out.println("t1 = " + parser1._parse1);
                System.out.println("t1 = " + parser2._parse1);
            }
            else {
                //System.out.println("t1 = t2");
            }
        }
    }

    /**
     * 簡易テスト用メインメソッド
     * @param args 不使用
     * @throws IOException ファイルエラー
     */
    public static void main(String[] args) throws IOException {
        g_dodebug = true;
        recursiveCheck(new File("C:\\Users\\yaman\\.nuget\\packages\\microsoft.netcore.platforms\\2.0.0"));
        recursiveCheck(new File("C:\\Users\\All Users\\Microsoft"));
        if (false) {
            recursiveCheck(new File("C:\\Users"));
        }
    }

    /**
     * ディレクトリからjsonファイルを再帰的に見つけ出しテストメソッドを呼び出す
     * @param file ディレクトリ
     */
    public static void recursiveCheck(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File seek : children) {
                    recursiveCheck(seek);
                }
            }
            return;
        }
        if (file.getName().toLowerCase().endsWith(".json")) {
            try {
                if (count == 0) {
                    return;
                }
                if (_fatal != null) {
                    return;
                }
                doTest(file);
                if (--count == 0) {
                    return;
                }
            } catch (Throwable ex) {
                ex.printStackTrace();;
            }
        }
    }
}
