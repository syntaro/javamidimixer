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

import java.util.ArrayList;
import java.util.logging.Level;
import jp.synthtarou.libs.log.MXFileLogger;

/**
 * jsonファイル内のラベルとコンテンツを表現するクラス。ネストしている。
 *
 * @author Syntarou YOSHIDA
 */
public class MXJsonValue {

    /**
     * ラベルを指定してオブジェクトを生成する
     *
     * @param text
     */
    public MXJsonValue(String text) {
        _label = text;
    }

    /**
     * ラベルとコンテンツを指定してオブジェクトを生成する
     *
     * @param label ラベル
     * @param value コンテンツ（単一）
     */
    public MXJsonValue(String label, String value) {
        _label = label;
        addToContentsValue(new MXJsonValue(value));
    }

    String _label;
    ArrayList<MXJsonValue> _listContents;

    /**
     * コンテンツタイプ：未設定
     */
    public static final int CONTENTS_TYPE_NOTSET = 0;

    /**
     * コンテンツタイプ：リスト
     */
    public static final int CONTENTS_TYPE_LIST = 1;

    /**
     * コンテンツタイプ：構造体
     */
    public static final int CONTENTS_TYPE_STRUCTURE = 2;

    /**
     * コンテンツタイプ：値
     */
    public static final int CONTENTS_TYPE_VALUE = 3;

    int _conetentsType = CONTENTS_TYPE_NOTSET;
    
    public String getContentsTypeText() {
        switch (_conetentsType) {
            case CONTENTS_TYPE_NOTSET:
                return "not set";
            case CONTENTS_TYPE_LIST:
                return "list";
            case CONTENTS_TYPE_STRUCTURE:
                return "structure";
            case CONTENTS_TYPE_VALUE:
                return "value";
        }
        return "?";
    }

    /**
     * コンテンツタイプを設定する（一度設定したら変更できない）
     * @param type コンテンツタイプ
     */
    public void setContentsType(int type) {
        if (_conetentsType == CONTENTS_TYPE_NOTSET) {
            _conetentsType = type;
            return;
        }
        if (_conetentsType != type) {
            throw new IllegalArgumentException(this._label + " =" + getContentsTypeText() + "(" + _conetentsType + ") != " + type);
        }
    }

    /**
     * コンテンツタイプが、値であるか
     * @return 値であればTrue
     */
    public boolean isContentsValue() {
        return _conetentsType == CONTENTS_TYPE_VALUE;
    }

    /**
     * コンテンツタイプが、配列であるか
     * @return 配列であればTrue
     */
    public boolean isContentsArray() {
        return _conetentsType == CONTENTS_TYPE_LIST;
    }

    /**
     * コンテンツタイプが、構造体であるか
     * @return 構造体であればTrue
     */
    public boolean isContentsSturucture() {
        return _conetentsType == CONTENTS_TYPE_STRUCTURE;
    }

    /**
     * Valueタイプ：オールマイティ
     */
    public static final int TYPE_ANY = 0;

    /**
     * Valueタイプ：NULL
     */
    public static final int TYPE_NULL = 1;

    /**
     * Valueタイプ:ダブルカンマ -> :
     */
    public static final int TYPE_DOUBLECOMMA = 2;

    /**
     * Valueタイプ:リストの開始-> [
     */
    public static final int TYPE_START_LIST = 4;

    /**
     * Valueタイプ:構造体(名前つきリスト)の開始-> {
     */
    public static final int TYPE_START_STRUCTURE = 5;

    /**
     * Valueタイプ:リストの終了-> ]
     */
    public static final int TYPE_END_LIST = 6;

    /**
     * Valueタイプ:構造体(名前つきリスト)の終了-> }
     */
    public static final int TYPE_END_STRUCTURE = 7;

    /**
     * Valueタイプ：カンマ -> ,
     */
    public static final int TYPE_COMMA = 8;

    /**
     * Valueタイプ：テキスト（エスケープのありなしはプログラマーが管理すること
     */
    public static final int TYPE_TEXT = 10;

    /**
     * ラベルの文字列からValueタイプを推測する
     *
     * @return Valueタイプ
     */
    public int getType() {
        if (_label == null) {
            return TYPE_NULL;
        }
        if (_label.length() == 0) {
            return TYPE_TEXT;
        }
        if (_label.length() == 1) {
            int ch = _label.charAt(0);
            switch (ch) {
                case ':':
                    return TYPE_DOUBLECOMMA;
                case ',':
                    return TYPE_COMMA;
                case '[':
                    return TYPE_START_LIST;
                case ']':
                    return TYPE_END_LIST;
                case '{':
                    return TYPE_START_STRUCTURE;
                case '}':
                    return TYPE_END_STRUCTURE;
            }
        }
        if (_label.equals("null")) {
            return TYPE_NULL;
        }
        return TYPE_TEXT;
    }

    /**
     * 推測されたValueタイプから、Valueタイプの名称を取得する
     *
     * @return Valueタイプ名
     */
    public String getTypeText() {
        switch (getType()) {
            case TYPE_ANY:
                return "any";
            case TYPE_NULL:
                return "any";
            case TYPE_DOUBLECOMMA:
                return "doubleComma";
            case TYPE_START_LIST:
                return "list";
            case TYPE_START_STRUCTURE:
                return "structure";
            case TYPE_END_LIST:
                return "endList";
            case TYPE_END_STRUCTURE:
                return "endStructure";
            case TYPE_COMMA:
                return "comma";
            case TYPE_TEXT:
                return "text";

        }
        return "Unknown";
    }

    /**
     * contentsとして配列を選択し、varを追加する
     *
     * @param var MXJsonValueタイプ
     */
    public void addToContentsArray(MXJsonValue var) {
        if (_listContents == null) {
            _listContents = new ArrayList<>();
        }
        setContentsType(CONTENTS_TYPE_LIST);
        _listContents.add(var);
    }

    /**
     * contentsとして名前つきリスト(構造体)を選択し、varを追加する
     *
     * @param var MXJsonValueタイプ
     */
    public void addToContentsStructure(MXJsonValue var) {
        if (_listContents == null) {
            _listContents = new ArrayList<>();
        }
        setContentsType(CONTENTS_TYPE_STRUCTURE);
        _listContents.add(var);
    }

    /**
     * contentsとして単一のvarを設定する
     *
     * @param var MXJsonValueタイプ
     */
    public void addToContentsValue(MXJsonValue var) {
        if (_listContents == null) {
            _listContents = new ArrayList<>();
        } else {
            _listContents.clear();
        }
        setContentsType(CONTENTS_TYPE_VALUE);
        _listContents.clear();
        _listContents.add(var);
    }

    /**
     * contentsとして名前つきリスト(構造体)を選択し、Stringを追加する
     *
     * @param label ラベル
     * @param value 値（文字列）文字列以外の場合、もうひとつの同名メソッドを用いること
     */
    public void addToContentsStructure(String label, String value) {
        MXJsonValue temp = new MXJsonValue(label, value);
        addToContentsStructure(temp);
    }

    /**
     * contentsとして配列を選択し、Stringを追加する
     *
     * @param value 配列についかされる値（子のラベル)
     */
    public void addToContentsArray(String value) {
        MXJsonValue temp = new MXJsonValue(value);
        addToContentsArray(temp);
    }
    /**
     * String型にする（表示用）
     *
     * @return ラベルのテキスト
     */
    public String toString() {
        return (_label == null) ? "" : _label;
    }

    /**
     * 表示用に、自身とコンテンツを簡易整形する
     * @return 整形された文字列
     */
    public String formatForDisplay(boolean showDetail) {
        return formatForDisplay(showDetail, 0);
    }

    /**
     * 表示用に、自身とコンテンツをファイル用に成形する
     * @return 整形された文字列
     */
    public String formatForFile() {
        return formatForDisplay(false, 0);
    }
    
    /**
     * 文字バッファにインデントを追加する
     * @param indent インデント数
     * @param str 文字バッファ
     */
    public void doIndent(int indent, StringBuffer str) {
        for (int i = 0; i < indent; ++ i) {
            str.append(" ");
        }
    }

    /**
     * 表示用に、自身とコンテンツを簡易整形する
     *
     * @param indent 現在のインデント値
     * @return 整形された文字列
     */
    public String formatForDisplay(boolean showDetail, int indent) {
        StringBuffer str = new StringBuffer();
        switch (_conetentsType) {
            case CONTENTS_TYPE_LIST:
                if (showDetail) {
                    str.append("[" + getContentsTypeText() + "]");
                }
                if (_label != null) {
                    str.append(_label + ":");
                }
                str.append("[\n");
                indent += 4;
                doIndent(indent, str);
                if (_listContents != null) {
                    boolean first = true;
                    for (MXJsonValue seek : _listContents) {
                        if (first == false) {
                            str.append(",\n");
                            doIndent(indent, str);
                        }
                        first = false;
                        str.append(seek.formatForDisplay(showDetail, indent));
                    }
                }
                str.append("\n");
                indent -= 4;
                doIndent(indent, str);
                str.append("]");
                break;
            case CONTENTS_TYPE_STRUCTURE:
                if (showDetail) {
                    str.append("[" + getContentsTypeText() + "]");
                }
                if (_label != null) {
                    str.append(_label + ":");
                }
                str.append("{\n");
                indent += 4;
                doIndent(indent, str);
                if (_listContents != null) {
                    boolean first = true;
                    for (MXJsonValue seek : _listContents) {
                        if (first == false) {
                            str.append(",\n");
                            doIndent(indent, str);
                        }
                        first = false;
                        str.append(seek.formatForDisplay(showDetail, indent));
                    }
                }
                str.append("\n");
                indent -= 4;
                doIndent(indent, str);
                str.append("}");
                break;
            case CONTENTS_TYPE_VALUE:
                if (showDetail) {
                    str.append("[" + getContentsTypeText() + "]");
                }
                if (_label != null) {
                    str.append(_label + ":");
                }
                if (_listContents.size() > 0) {
                    str.append(_listContents.get(0).formatForDisplay(showDetail, indent));
                } else {
                    str.append("null");
                }
                break;
            case CONTENTS_TYPE_NOTSET:
                if (showDetail) {
                    str.append("[" + getContentsTypeText() + "]");
                }
                if (_label != null) {
                    if (_label.equals("]") || _label.equals("}")) {
                        if (indent >= 4) {
                            indent -= 4;
                        }
                    }
                    str.append(_label);
                    if (_label.equals("[") || _label.equals("{")) {
                        indent += 4;
                    }
                }
                break;
        }
        if (showDetail) {
            str.append("[" + getContentsTypeText() + "]");
        }
        return str.toString();
    }

    /**
     * コンテンツのカウント
     *
     * @return 数値
     */
    public int contentsCount() {
        if (_listContents == null) {
            return 0;
        }
        return _listContents.size();
    }

    /**
     * コンテンツを取得する
     *
     * @param index インデックス
     * @return MXJsonValue型
     */
    public MXJsonValue getContentsAt(int index) {
        if (_listContents == null) {
            return null;
        }
        return _listContents.get(index);
    }

    /**
     * コンテンツをラベルから取得する（名前つき配列の場合用いることができる）
     *
     * @param label ラベル
     * @param doEscapeAndSearch エスケープしてから探索する場合
     * @return 見つかったインデックス
     */
    public int searchContentsByLabel(String label, boolean doEscapeAndSearch) {
        if (doEscapeAndSearch) {
            label = escape(label);
        }
        for (int i = 0; i < _listContents.size(); ++i) {
            MXJsonValue seek = _listContents.get(i);
            if (seek._label == null) {
                if (label == null) {
                    return i;
                }
                continue;
            }
            if (label != null && seek._label.equals(label)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * コンテンツから項目を削除
     *
     * @param index 項目のインデックス
     */
    public void removeContentsAt(int index) {
        if (_listContents == null) {
            return;
        }
        _listContents.remove(index);
    }

    /**
     * 簡易テスト
     *
     * @param args 不使用
     */
    public static void main(String[] args) {
        MXJsonValue parse1 = testLegacy();
        String text1 = parse1.formatForDisplay(false);
        
        System.out.println(text1);

        MXJsonParser parse2 = new MXJsonParser();
        MXJsonValue value2 = parse2.parseText(text1);
        String text2 = value2.formatForDisplay(false);

        System.out.println(text2);

        MXJsonValue parse3 = testNewType();
        String text3 = parse3.formatForDisplay(false);
        System.out.println(text3);

        MXJsonParser parse4 = new MXJsonParser();
        MXJsonValue value4 =parse4.parseText(text3);
        String text4 = value4.formatForDisplay(false);
        System.out.println(text4);

        System.out.println(text1.equals(text2));
        System.out.println(text3.equals(text4));

        System.out.println(text1.equals(text3));
        System.out.println(text1.equals(text4));
        System.out.println(text2.equals(text3));
        System.out.println(text2.equals(text4));
    }
    
    /**
     *
     * @return
     */
    public static MXJsonValue testLegacy() {
        MXJsonValue root = new MXJsonValue(null);

        root.addToContentsStructure(new MXJsonValue("\"abc\"", "\"ABC[]\""));
        root.addToContentsStructure(new MXJsonValue("\"def\"", "\"DEF\\\\\""));
        root.addToContentsStructure(new MXJsonValue("\"ghi\"", "\"GHI\\\"\""));

        MXJsonValue attributes = new MXJsonValue("\"attributes\"");
        attributes.addToContentsStructure("\"name\"", "\"synthtarou\"");
        attributes.addToContentsStructure("\"age\"", "47");
        attributes.addToContentsStructure("\"gender\"", "\"man\"");
        attributes.addToContentsStructure("\"fine\"", "\"thank you\"");

        root.addToContentsStructure(attributes);

        MXJsonValue routine = new MXJsonValue("\"routine\"");
        routine.addToContentsArray("\"eat\"");
        routine.addToContentsArray("\"sleep\"");
        routine.addToContentsArray("\"work\"");
        routine.addToContentsArray("\"walk\"");
        routine.addToContentsArray("\"study\"");

        root.addToContentsStructure(routine);

        return root;
    }

    /**
     *
     * @return
     */
    public static MXJsonValue testNewType() {
        MXJsonValue root = new MXJsonValue(null);

        MXJsonValue.HelperForStructure rootSttucture = root.new HelperForStructure();
        rootSttucture.addFollowingText("abc", "ABC[]");
        rootSttucture.addFollowingText("def", "DEF\\");
        rootSttucture.addFollowingText("ghi", "GHI\"");

        MXJsonValue.HelperForStructure structure2 = rootSttucture.addFollowingStructure("attributes");
        structure2.addFollowingText("name", "synthtarou");
        structure2.addFollowingNumber("age", 47);
        structure2.addFollowingText("gender", "man");
        structure2.addFollowingText("fine", "thank you");

        MXJsonValue.HelperForArray routine = rootSttucture.addFollowingArray("routine");
        routine.addFollowingText("eat");
        routine.addFollowingText("sleep");
        routine.addFollowingText("work");
        routine.addFollowingText("walk");
        routine.addFollowingText("study");

        return root;
    }

    /**
     * 一般的なプログラムで用いる数値でラベルと取得する
     * @return 文字列
     */
    public String getLabelUnscaped() {
        if (_label == null || _label.equals("null")) {
            return null;
        }
        return unescape(_label);
    }

    /**
     * 一般的なプログラムで用いる数値でラベルを取得する
     * @return 数値型のラベル
     * @throws NumberFormatException 数値型ではなかった
     */
    public Number getLabelNumber() throws NumberFormatException {
        if (_label == null || _label.equals("null")) {
            return null;
        }
        if (_label.startsWith("\"")) {
            throw new NumberFormatException("label was " + _label);
        }
        NumberFormatException out = null;
        try {
            return Integer.parseInt(_label);
        } catch (NumberFormatException ex) {

        }
        try {
            return Double.parseDouble(_label);
        } catch (NumberFormatException ex) {
            out = ex;
        }

        throw out;
    }

    /**
     * 一般的なプログラムで用いる文字列でラベルを取得する
     * @param label エスケープされていない文字列か、null
     */
    public void setLabelEscaped(String label) {
        _label = escape(label);
    }

    /**
     * 一般的なプログラムで用いる数値でラベルを設定する
     * @param label 設定する数値
     */
    public void setLabelByNumber(Number label) {
        if (label == null) {
            _label = "null";
        } else {
            _label = String.valueOf(label);
        }
    }

    /**
     * 配列タイプをあつかうヘルパークラス
     */
    public class HelperForArray {
        /**
         * コンストラクタ
         * MXJsonValue value = ...;
         * MXJsonValue.HelperForArray helper = value.new HelperForArray();
         * としてインスタンスを生成する
         */
        public HelperForArray() {
            setContentsType(CONTENTS_TYPE_LIST);
        }

        /**
         * ラベル文字列を取得する
         * @return エスケープされていないラベル文字列
         */
        public String getLabel() {
            return getLabelUnscaped();
        }

        /**
         * ラベル文字列を設定する
         * @param label エスケープされていないラベル文字列
         */
        public void setLabel(String label) {
            setLabelEscaped(label);
        }

        /**
         * 配列の大きさを取得する
         * @return 配列の大きさ
         */
        public int count() {
            return contentsCount();
        }

        /**
         * 配列から要素を取得する
         * @param index　インデックス
         * @return 要素
         */
        public MXJsonValue getFollowingValue(int index) {
            MXJsonValue child = getContentsAt(index);
            return child;
        }

        /**
         * 配列から数を取得する 
         * @param index インデックス
         * @return 数値
         */
        public int getFollowingInt(int index, int defValue) {
            MXJsonValue child = getContentsAt(index);
            if (child == null) {
                return defValue;
            }
            Number ret = child.getLabelNumber();
            if (ret == null) {
                return defValue;
            }
            return ret.intValue();
        }

        /**
         * 配列からエスケープされていない文字列を取得する 
         * @param index インデックス
         * @return 文字列
         */
        public String getFollowingText(int index, String defValue) {
            MXJsonValue child = getContentsAt(index);
            if (child == null) {
                return defValue;
            }
            String ret = child.getLabelUnscaped();
            if (ret == null) {
                return defValue;
            }
            return ret;
        }

        /**
         * 配列にnullを追加する
         */
        public void addFollowingNull() {
            MXJsonValue var = new MXJsonValue("null");
            MXJsonValue.this.addToContentsArray(var);
        }

        /**
         * 配列に数値を追加する
         * @param value 数値
         */
        public void addFollowingNumber(Number value) {
            MXJsonValue var = new MXJsonValue(String.valueOf(value));
            MXJsonValue.this.addToContentsArray(var);
        }

        /**
         * 配列にBooleanを追加する
         * @param value 数値
         */
        public void addFollowingBool(boolean value) {
            MXJsonValue var = new MXJsonValue(value ? "true" : "false");
            MXJsonValue.this.addToContentsArray(var);
        }
        /**
         * 配列に文字列をエスケープして追加する
         * @param value エスケープされていない文字列
         */
        public void addFollowingText(String value) {
            MXJsonValue var = new MXJsonValue(escape(value));
            MXJsonValue.this.addToContentsArray(var);
        }

        /**
         * 配列を追加して、HelperForArray型で取得する
         * @param label ラベル
         * @return 追加されたHelperForArray型
         */
        public HelperForArray addFollowingArray() {
            MXJsonValue value = new MXJsonValue(null);
            MXJsonValue.this.addToContentsArray(value);
            return value.new HelperForArray();
        }

        /**
         * 構造体を追加して、HelperForStructure型で取得する
         * @param label ラベル
         * @return 追加されたHelperForStructure型
         */
        public HelperForStructure addFollowingStructure() {
            MXJsonValue value = new MXJsonValue(null);
            MXJsonValue.this.addToContentsArray(value);
            return value.new HelperForStructure();
        }

        /**
         * 構造体を検索して、HelperForStructure型で取得する
         * @param index インデックス
         * @return HelperForStructure型
         */
        public HelperForStructure getFollowingStructure(int index) {
            MXJsonValue value = getContentsAt(index);
            return value.new HelperForStructure();
        }

        /**
         * 配列を検索して、HelperForStructure型で取得する
         * @param index インデックス
         * @return HelperForStructure型
         */
        public HelperForArray getFollowingArray(int index) {
            MXJsonValue value = getContentsAt(index);
            return value.new HelperForArray();
        }

        public MXJsonValue toJsonValue() {
            return MXJsonValue.this;
        }
    }

    /**
     * 構造体タイプをあつかうヘルパークラス
     */
    public class HelperForStructure {

        /**
         * コンストラクタ
         * MXJsonValue value = ...;
         * MXJsonValue.HelperForSturucture helper = value.new HelperForSturucture();
         * としてインスタンスを生成する
         */
        public HelperForStructure() {
            setContentsType(CONTENTS_TYPE_STRUCTURE);
        }

        /**
         * ラベル文字列を取得する
         * @return エスケープされていないラベル文字列
         */
        public String getLabel() {
            return getLabelUnscaped();
        }

        /**
         * ラベル文字列を設定する
         * @param label エスケープされていないラベル文字列
         */
        public void setLabel(String label) {
            setLabelEscaped(label);
        }

        /**
         * 構造体の大きさを取得する
         * @return 配列の大きさ
         */
        public int count() {
            return contentsCount();
        }

        /**
         *　インデックスのラベル名を取得する
         * @param index インデックス
         * @return ラベル名
         */
        public String getFollowingText(int index) {
            MXJsonValue child = getContentsAt(index);
            return child.getLabelUnscaped();
        }

        /**
         * 配列から要素を取得する
         * @param index　インデックス
         * @return 要素
         */
        public MXJsonValue getFollowingValue(int index) {
            MXJsonValue child = getContentsAt(index);
            return child;
        }

        /**
         * 配列から数を取得する 
         * @param index インデックス
         * @return 数値
         */
        public Number getFollowingNumber(int index) {
            MXJsonValue child = getContentsAt(index);
            return child.getLabelNumber();
        }

        /**
         * 配列をラベル名で探索する
         * @param label エスケープされているラベル名
         * @return 見つかったインデックス、見つからない場合-1
         */
        public int indexOfWithEscape(String label) {
            String escaped = escape(label);
            for (int i = 0; i < MXJsonValue.this.contentsCount(); ++i) {
                MXJsonValue value = MXJsonValue.this.getContentsAt(i);
                if (escaped.equals(value._label)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * ラベルから文字列を取得する
         * @param name ラベル文字列
         * @return みつかった文字列
         */
        public String getFollowingText(String name, String defValue) {
            int index = indexOfWithEscape(name);
            if (index < 0) {
                return null;
            }
            MXJsonValue pair = getContentsAt(index).getContentsAt(0);
            return unescape(pair._label);
        }

        /**
         * 配列にnullを追加する
         * @param name ラベル
         */
        public void addFollowingNull(String name) {
            MXJsonValue key = new MXJsonValue(escape(name), null);
            addToContentsStructure(key);
        }

        /**
         * 配列に数値を追加する
         * @param name ラベル
         * @param value 数値
         */
        public void addFollowingNumber(String name, Number value) {
            MXJsonValue key = new MXJsonValue(escape(name), String.valueOf(value));
            addToContentsStructure(key);
        }

        /**
         * 配列に文字列をエスケープして追加する
         * @param name ラベル
         * @param value エスケープされていない文字列
         */
        public void addFollowingText(String name, String value) {
            MXJsonValue key = new MXJsonValue(escape(name), escape(value));
            addToContentsStructure(key);
        }

        /**
         * 配列を追加して、HelperForArray型で取得する
         * @param label ラベル
         * @return 追加されたHelperForArray型
         */
        public HelperForArray addFollowingArray(String label) {
            label = escape(label);
            MXJsonValue value = new MXJsonValue(label);
            addToContentsStructure(value);
            return value.new HelperForArray();
        }

        /**
         * 構造体を追加して、HelperForStructure型で取得する
         * @param label ラベル
         * @return 追加されたHelperForStructure型
         */
        public HelperForStructure addFollowingStructure(String label) {
            label = escape(label);
            MXJsonValue value = new MXJsonValue(label);
            addToContentsStructure(value);
            return value.new HelperForStructure();
        }

        /**
         * 構造体を検索して、HelperForStructure型で取得する
         * @param label ラベル
         * @return HelperForStructure型
         */
        public HelperForStructure getFollowingStructure(String label) {
            int index = indexOfWithEscape(label);
            if (index < 0) {
                return null;
            }
            MXJsonValue value = getContentsAt(index);
            return value.new HelperForStructure();
        }

        /**
         * 配列を検索して、HelperForStructure型で取得する
         * @param label エスケープされてないラベル
         * @return HelperForStructure型
         */
        public HelperForArray getFollowingArray(String label) {
            int index = indexOfWithEscape(label);
            if (index < 0) {
                return null;
            }
            MXJsonValue value = getContentsAt(index);
            return value.new HelperForArray();
        }
        
        public int getFollowingInt(String label, int defvalue) {
            int x = indexOfWithEscape(label);
            if (x < 0) {
                return defvalue;
            }
            MXJsonValue var = getContentsAt(x);
            try {
                String pair = var.getContentsAt(0)._label;
                return Integer.parseInt(pair);
            }catch(NumberFormatException ex) {
                return defvalue;
            }
        }

        public boolean getFollowingBool(String label, boolean defvalue) {
            try {
                int x = indexOfWithEscape(label);
                if (x < 0) {
                    System.out.println("label " + label + "-1");
                    return defvalue;
                }
                MXJsonValue var = getContentsAt(x);
                String pair = var.getContentsAt(0)._label;
                if (pair.equals("true")) {
                    return true;
                }
                if (pair.equals("false")) {
                    return false;
                }
                return defvalue;
            }catch(Exception ex) {
                
            }
            return defvalue;
        }

        public void setFollowingText(String label, String text) {
            int x = indexOfWithEscape(label);
            if (x < 0) {
                MXJsonValue var = new MXJsonValue(escape(label), escape(text));
                MXJsonValue.this.addToContentsStructure(var);
            }
            else {
                MXJsonValue var = getContentsAt(x);
                var.getContentsAt(0)._label = escape(text);
            }
        }
        
        public void setFollowingInt(String label, int value) {
            int x = indexOfWithEscape(label);
            if (x < 0) {
                MXJsonValue var = new MXJsonValue(escape(label), String.valueOf(value));
                MXJsonValue.this.addToContentsStructure(var);
            }
            else {
                MXJsonValue var = getContentsAt(x);
                var.getContentsAt(0)._label = String.valueOf(value);
            }
        }

        public void setFollowingBool(String label, boolean value) {
            int x = indexOfWithEscape(label);
            if (x < 0) {
                MXJsonValue var = new MXJsonValue(escape(label), value ? "true" : "false");
                MXJsonValue.this.addToContentsStructure(var);
            }
            else {
                MXJsonValue var = getContentsAt(x);
                var.getContentsAt(0)._label = value ? "true" : "false";
            }
        }

        public MXJsonValue toJsonValue() {
            return MXJsonValue.this;
        }
    }

    /**
     * json用のエスケープシーケンスで、エスケープする
     *
     * @param unescaped　エスケープされてない文字列を渡す
     * @return エスケープされた文字列
     */
    public static String escape(String unescaped) {
        if (unescaped == null) {
            return "null";
        }
        int len = unescaped.length();
        int pos = 0;
        StringBuffer buffer = new StringBuffer();
        try {
            while (pos < len) {
                char ch = unescaped.charAt(pos++);
                switch (ch) {
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
                                hex = " " + hex;
                            }
                            buffer.append(hex);
                            break;
                        }
                        buffer.append(ch);
                        break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            MXFileLogger.getLogger(MXJsonValue.class).log(Level.WARNING, ex.getMessage(), ex);
        }
        return "\"" + buffer.toString() + "\"";
    }

    /**
     * json用のエスケープシーケンスで、アンエスケープする
     *
     * @param escaped　エスケープされている文字列を渡す
     * @return アンエスケープ（解除）された文字列
     */
    public static String unescape(String escaped) {
        int len = escaped.length();
        int pos = 0;
        if (escaped == null || escaped.equals("null")) {
            return null;
        }
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
                    pos++;
                    continue;
                } else {
                    if (ch == '\"') {
                        inDQuote = false;
                        pos++;
                        continue;
                    } else if (ch == '\\') {
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
                            buffer.append(Character.valueOf((char) ch1));
                            pos += 2;
                            break;
                            default:
                                buffer.append('\\');
                                buffer.append(Character.valueOf((char) ch1));
                                pos += 2;
                                break;
                        }
                        continue;
                    } else {
                        buffer.append((char) ch);
                        pos++;
                    }
                }

            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            MXFileLogger.getLogger(MXJsonValue.class).log(Level.WARNING, ex.getMessage(), ex);
        }
        return buffer.toString();
    }
}
