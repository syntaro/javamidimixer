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

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.LinkedList;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.inifile.MXSettingUtil;
import jp.synthtarou.midimixer.MXMain;

/**
 * jsonテキストを,MJsonValueオブジェクトのツリーに格納する、パーサー
 * @author Syntarou YOSHIDA
 */
public class MXJsonParser {
    static ArrayList<MXJsonSupport> _listAutosave = new ArrayList<>();

    private File _file;
    private MXJsonValue _root;

    public MXJsonParser() {
        this((File)null);
    }
   
    public MXJsonValue getRoot() {
        return _root;
    }

    public void setRoot(MXJsonValue root) {
        _root = root;
    }
    
    public MXJsonParser(String file) {
        this(pathOf(file));
    }
    
    /**
     * ファイルからインスタンスを生成する。パースまでやる
     * @throws IOException　Fileエラー
     */
    public MXJsonParser(File file) {
        _file = file;
        _root = new MXJsonValue(null);
    }

    public static File pathOf(String name) {
        return new File(getJsonDirectory(), name + ".json");
    }
    
    public static void invokeAutosave() {
        for (MXJsonSupport seek : _listAutosave) {
            try {
                boolean done = seek.writeJsonFile(null);
                System.out.println("tried write json = " + done + "@ " + seek.getClass());
            }catch(Throwable ex) {
                MXFileLogger.getLogger(MXJsonParser.class).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
    public static void setAutosave(MXJsonSupport target) {
        if (_listAutosave.indexOf(target) < 0) {
            _listAutosave.add(target);
        }
    }

    public boolean writeFile() {
        MXJsonValue value = _root;
        String text = value.formatForFile();

        String fileName = _file.toString();
        String dir = getJsonDirectory().getParent();
        if (fileName.startsWith(dir)) {
            fileName = "$(APP)" + fileName.substring(dir.length());
        }
        MXMain.progress("writing " + fileName);

        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(_file);
            BufferedOutputStream bout = new BufferedOutputStream(fout);
            OutputStreamWriter writer = new OutputStreamWriter(bout, "utf-8");
            writer.write(text);
            writer.flush();
            writer.close();
            bout.close();
            fout.close();
            fout = null;
            return true;
        } catch (IOException ioe) {
            MXFileLogger.getLogger(MXJsonParser.class).log(Level.SEVERE, ioe.toString(), ioe);
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException ioe) {
                    MXFileLogger.getLogger(MXJsonParser.class).log(Level.SEVERE, ioe.toString(), ioe);
                }
            }
        }
        return false;
    }

    public static File getJsonDirectory() {
        File dir = new File(MXUtil.getAppBaseDirectory(), "json");
        if (dir.isDirectory()) {
            return dir;
        }

        try {
            Path p = Paths.get(dir.toURI());
            Files.createDirectory(p);
        } catch (IOException ex) {
            MXFileLogger.getLogger(MXSettingUtil.class).log(Level.WARNING, ex.getMessage(), ex);
        }

        if (dir.isDirectory()) {
            return dir;
        }

        return null;
    }

    /**
     * 文字列をパースする
     * @param value 格納先
     * @param contents 文字列
     */
    public MXJsonValue parseText(String contents) {
        MXJsonValue value = new MXJsonValue(null);
        return parseImpl(value, new MXJsonFileReader(contents));
    }
    /**
     * ファイルをパースする
     * @param value 格納先
     * @param file jsonファイル
     * @throws java.io.IOException
     * @throws IOException　Fileエラー
     */
    public MXJsonValue parseFile() {
        File file = _file;
        MXJsonValue value = new MXJsonValue(null);
        try {
            return parseImpl(value, new MXJsonFileReader(file));
        }catch(FileNotFoundException ex) {
            return null;
        }catch(IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * ファイルをパースする
     * @param value 格納先
     * @param file jsonファイル
     * @throws java.io.IOException
     * @throws IOException　Fileエラー
     */
    public MXJsonValue parseFile(String resource){
        MXJsonValue value = new MXJsonValue(null);
        File file = pathOf(resource);
        try {
            return parseImpl(value, new MXJsonFileReader(file));
        }catch(FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }catch(IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    ArrayList<String> _parse1 = null;
    static boolean g_dodebug = true;

    /**
     * MJsonReaderを指定してパースする、ほかのparseメソッドから呼ばれる
     * @param reader MJsonReader
     * @return パース結果
     */
    protected MXJsonValue parseImpl(MXJsonValue root, MXJsonFileReader reader) {
        int point = 0;
        int total = 0;
        if (_file != null) {
            String fileName = _file.toString();
            String dir = getJsonDirectory().getParent();
            if (fileName.startsWith(dir)) {
                fileName = "$(APP)" + fileName.substring(dir.length());
            }
            MXMain.progress("reading " + fileName);
        }
        _parse1 = new ArrayList<>();
        _root = null;
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
                    escaped1 = MXJsonValue.escape(original);
                }
                String unescaped1 = MXJsonValue.unescape(escaped1);
                String escaped2 = MXJsonValue.escape(unescaped1);
                String unescaped2 = MXJsonValue.unescape(escaped2);

                if (escaped1.equals(escaped2) == false || unescaped2.equals(unescaped1) == false) {
                    System.out.println("orginal[" + original + "]");
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
        LinkedList<MXJsonValue> queue = new LinkedList<>();
        for (String seek : _parse1) {
            MXJsonValue value = new MXJsonValue(seek);
            queue.add(value);
        }
        try {
            MXJsonValue value = readSomething(queue);
            root._conetentsType = value._conetentsType;
            root._label = value._label;
            root._listContents = value._listContents;
            _root = root;
            return root;
        } catch (Throwable ex) {
            MXFileLogger.getLogger(MXJsonParser.class).log(Level.WARNING, ex.getMessage(), ex);
            return null;
        }
    }

    int currentReading = MXJsonValue.TYPE_START_STRUCTURE;

    /**
     * キューに、type1~type4が並んでいるか調べる
     * @param queue キュー、MJsonValueのリスト
     * @param type1 タイプ
     * @param type2 タイプ
     * @param type3 タイプ
     * @param type4 タイプ
     * @return その通りに並んでいる場合
     */
    public boolean checkQueue(LinkedList<MXJsonValue> queue, int type1, int type2, int type3, int type4) {
        if (queue.size() >= 4) {
            if (queue.get(0).getType() != type1 && type1 != MXJsonValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(1).getType() != type2 && type2 != MXJsonValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(2).getType() != type3 && type3 != MXJsonValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(3).getType() != type4 && type4 != MXJsonValue.TYPE_ANY) {
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
    public boolean checkQueue(LinkedList<MXJsonValue> queue, int type1, int type2, int type3) {
        if (queue.size() >= 3) {
            if (queue.get(0).getType() != type1 && type1 != MXJsonValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(1).getType() != type2 && type2 != MXJsonValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(2).getType() != type3 && type3 != MXJsonValue.TYPE_ANY) {
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
    public boolean checkQueue(LinkedList<MXJsonValue> queue, int type1, int type2) {
        if (queue.size() >= 2) {
            if (queue.get(0).getType() != type1 && type1 != MXJsonValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(1).getType() != type2 && type2 != MXJsonValue.TYPE_ANY) {
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
    public boolean checkQueue(LinkedList<MXJsonValue> queue, int type1) {
        if (queue.size() >= 1) {
            if (queue.get(0).getType() != type1 && type1 != MXJsonValue.TYPE_ANY) {
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
    protected String first5ToText(LinkedList<MXJsonValue> queue) {
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
        return queue.size() + " : " + str.toString();
    }

    /**
     * リスト構造をパースする
     * @param queue キュー
     * @return リストを意味するMJsonValue
     */
    public MXJsonValue enterList(LinkedList<MXJsonValue> queue) {
        MXJsonValue result = new MXJsonValue(null);
        boolean commaFin = false;

        while (true) {
            if (checkQueue(queue, MXJsonValue.TYPE_END_LIST)) {
                if (result._listContents == null) {
                    result.setContentsType(MXJsonValue.CONTENTS_TYPE_LIST);
                }
                queue.removeFirst();
                if (commaFin) {
                    result.addToContentsArray(new MXJsonValue(""));
                }
                break;
            } else if (checkQueue(queue, MXJsonValue.TYPE_ANY)) {
                MXJsonValue value = readSomething(queue);
                result.addToContentsArray(value);
                commaFin = false;
                if (checkQueue(queue, MXJsonValue.TYPE_COMMA)) {
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
    public MXJsonValue enterStructure(LinkedList<MXJsonValue> queue) {
        MXJsonValue result = new MXJsonValue(null);
        boolean commaFin = false;

        while (true) {
            if (checkQueue(queue, MXJsonValue.TYPE_END_STRUCTURE)) {
                if (result._listContents == null) {
                    result.setContentsType(MXJsonValue.CONTENTS_TYPE_STRUCTURE);
                }
                queue.removeFirst();
                if (commaFin) {
                    result.addToContentsStructure(new MXJsonValue(null));
                }
                break;
            } else if (checkQueue(queue, MXJsonValue.TYPE_TEXT, MXJsonValue.TYPE_DOUBLECOMMA, MXJsonValue.TYPE_START_LIST)) {
                MXJsonValue name2 = queue.removeFirst();
                MXJsonValue dq = queue.removeFirst();
                MXJsonValue value = readSomething(queue);
                value._label = name2._label;
                result.addToContentsStructure(value);
                commaFin = false;

                if (checkQueue(queue, MXJsonValue.TYPE_COMMA)) {
                    commaFin = true;
                    queue.removeFirst();
                    continue;
                }
            } else if (checkQueue(queue, MXJsonValue.TYPE_TEXT, MXJsonValue.TYPE_DOUBLECOMMA, MXJsonValue.TYPE_START_STRUCTURE)) {
                MXJsonValue name2 = queue.removeFirst();
                MXJsonValue dq = queue.removeFirst();
                MXJsonValue value = readSomething(queue);
                value._label = name2._label;
                result.addToContentsStructure(value);
                commaFin = false;

                if (checkQueue(queue, MXJsonValue.TYPE_COMMA)) {
                    commaFin = true;
                    queue.removeFirst();
                    continue;
                }
            } else if (checkQueue(queue, MXJsonValue.TYPE_TEXT, MXJsonValue.TYPE_DOUBLECOMMA, MXJsonValue.TYPE_ANY)) {
                MXJsonValue name2 = queue.removeFirst();
                MXJsonValue dq = queue.removeFirst();
                MXJsonValue value = readSomething(queue);
                
                MXJsonValue add = new MXJsonValue(name2._label, value._label);
                result.addToContentsStructure(add);
                commaFin = false;

                if (checkQueue(queue, MXJsonValue.TYPE_COMMA)) {
                    commaFin = true;
                    queue.removeFirst();
                    continue;
                }
            } else {
                throw new IllegalArgumentException("File format " + first5ToText(queue));
            }
        }
        return result;
    }

    /**
     * 現在のキューから、意味をふくめてパースする
     * @param queue キュー
     * @return 取り出したMJsonValue
     */
    public MXJsonValue readSomething(LinkedList<MXJsonValue> queue) {
        if (queue.isEmpty()) {
            return null;
        }
        
        MXJsonValue operand = queue.removeFirst();
        MXJsonValue result = null;
        switch (operand.getType()) {
            case MXJsonValue.TYPE_NULL:
                //System.out.println(operand._name +" -> reading null ... "+ first5ToText(queue));
                return operand;

            case MXJsonValue.TYPE_END_LIST:
            case MXJsonValue.TYPE_END_STRUCTURE:
                //System.out.println(operand._name +" -> reading bad ... "+ first5ToText(queue));
                break;

            case MXJsonValue.TYPE_TEXT:
                //System.out.println(operand._name +" -> reading text... "+ first5ToText(queue));
                return operand;

            case MXJsonValue.TYPE_COMMA:
            case MXJsonValue.TYPE_DOUBLECOMMA:
                //System.out.println(operand._name +" -> reading bad2 ... "+ first5ToText(queue));
                return operand;

            case MXJsonValue.TYPE_START_STRUCTURE:
                //System.out.println(operand._name +" -> reading structure ... "+ first5ToText(queue));
                MXJsonValue structure = enterStructure(queue);
                return structure;

            case MXJsonValue.TYPE_START_LIST:
                //System.out.println(operand._name +" -> reading list ... "+ first5ToText(queue));
                MXJsonValue list = enterList(queue);
                return list;

            default:
                break;
        }
        queue.removeFirst();
        return readSomething(queue);
    }

    static int count = 20000;
    static Throwable _fatal = null;

    /**
     * テスト用メソッド1
     * @param file ファイルをつかってロジックをテストする
     */
    public static void doTest(File file) {
        System.out.println("*PARSE*" + file);
        MXJsonParser parser1 = new MXJsonParser(file);
        MXJsonParser parser2 = new MXJsonParser();
        String form, form2;
        MXJsonValue value = parser1.parseFile();
        form = value.formatForFile();
        //System.out.println("*DONE*");

        MXJsonValue value2 = parser2.parseText(form);
        form2 = value2.formatForFile();
        if (form.equals(form2)) {
            //System.out.println("Test O");
        } else {
            System.out.println("Test X");
            _fatal = new Throwable("Test X");
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
                System.out.println(file + "  -> index " + i + " / " + parser1._parse1.size());
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
        recursiveCheck(new File("C:\\Users\\All Users\\Microsoft\\VisualStudio\\ChromeAdapter\\262f7dc2\\i18n\\chs\\out\\src\\chromeDebugAdapter.i18n.json"));
        recursiveCheck(new File("C:\\VST_SDK"));
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
                MXFileLogger.getLogger(MXJsonParser.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
}
