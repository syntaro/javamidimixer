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
import jp.synthtarou.libs.MXFileLogger;

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
     * @param doEscapeValue コンテンツをエスケープする必要がある場合
     */
    public MXJsonValue(String label, String value, boolean doEscapeValue) {
        _label = label;
        if (doEscapeValue) {
            value = escape(value);
        }
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
            throw new IllegalArgumentException();
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
     * @param var MJsonValueタイプ
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
     * @param var MJsonValueタイプ
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
     * @param var MJsonValueタイプ
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
     * @param doEscapeValue 値をエスケープする必要があるか
     */
    public void addToContentsStructure(String label, String value, boolean doEscapeValue) {
        MXJsonValue temp = new MXJsonValue(label, value, doEscapeValue);
        addToContentsStructure(temp);
    }

    /**
     * contentsとして配列を選択し、Stringを追加する
     *
     * @param value 配列についかされる値（子のラベル)
     * @param doEscape 値をエスケープする必要があるか
     */
    public void addToContentsArray(String value, boolean doEscape) {
        MXJsonValue temp = new MXJsonValue(value);
        if (doEscape) {
            temp.setLabel(value, doEscape);
        }
        addToContentsArray(temp);
    }

    /**
     * contentsとして単一のvarを設定する
     *
     * @param var 子のラベル=thisのcontents
     * @param doEscape 値をエスケープする必要があるか
     */
    public void addToContentsValue(String var, boolean doEscape) {
        MXJsonValue temp = new MXJsonValue(var);
        if (doEscape) {
            temp.setLabel(var, doEscape);
        }
        addToContentsValue(temp);
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
    public String formatForDisplay() {
        return formatForDisplay(0);
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
    public String formatForDisplay(int indent) {
        StringBuffer str = new StringBuffer();
        switch (_conetentsType) {
            case CONTENTS_TYPE_LIST:
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
                        str.append(seek.formatForDisplay(indent));
                    }
                }
                str.append("\n");
                indent -= 4;
                doIndent(indent, str);
                str.append("]");
                break;
            case CONTENTS_TYPE_STRUCTURE:
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
                        str.append(seek.formatForDisplay(indent));
                    }
                }
                str.append("\n");
                indent -= 4;
                doIndent(indent, str);
                str.append("}");
                break;
            case CONTENTS_TYPE_VALUE:
                if (_label != null) {
                    str.append(_label + ":");
                }
                if (_listContents.size() > 0) {
                    str.append(_listContents.get(0).formatForDisplay(indent));
                } else {
                    str.append("null");
                }
                break;
            case CONTENTS_TYPE_NOTSET:
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
        return str.toString();
    }

    /**
     * ラベルをあとから変更する
     *
     * @param label ラベル
     * @param doEscape エスケープする必要がある場合
     */
    public void setLabel(String label, boolean doEscape) {
        if (doEscape) {
            label = escape(label);
        }
        _label = label;
    }

    /**
     * ラベルを取得する
     *
     * @param doUnescape アンエスケープ（解除）する必要がある場合
     * @return ラベル
     */
    public String getLabel(boolean doUnescape) {
        String label = _label;
        if (doUnescape) {
            label = unescape(label);
        }
        return label;
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
     * @return MJsonValue型
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
        String text1 = parse1.formatForDisplay();

        MXJsonValue parse2 = testNewType();
        String text2 = parse2.formatForDisplay();
        
        MXJsonValue parse3 = new MXJsonParser(text1)._treeRoot;
        String text3 = parse3.formatForDisplay();

        MXJsonValue parse4 = new MXJsonParser(text2)._treeRoot;
        String text4 = parse4.formatForDisplay();

        System.out.println(text1.equals(text2));
        System.out.println(text1.equals(text3));
        System.out.println(text1.equals(text4));
        System.out.println(text2.equals(text3));
        System.out.println(text2.equals(text4));
        System.out.println(text3.equals(text4));

        System.out.println(text1);
        System.out.println(text4);
    }
    
    /**
     *
     * @return
     */
    public static MXJsonValue testLegacy() {
        MXJsonValue root = new MXJsonValue(null);

        root.addToContentsStructure(new MXJsonValue("abc", "ABC[]", true));
        root.addToContentsStructure(new MXJsonValue("def", "DEF\\", true));
        root.addToContentsStructure(new MXJsonValue("ghi", "GHI\"", true));

        MXJsonValue attributes = new MXJsonValue("attributes");
        attributes.addToContentsStructure("name", "synthtarou", true);
        attributes.addToContentsStructure("age", "47", false);
        attributes.addToContentsStructure("gender", "man", true);
        attributes.addToContentsStructure("fine", "thank you", true);

        root.addToContentsStructure(attributes);

        MXJsonValue routine = new MXJsonValue("routine");
        routine.addToContentsArray("eat", true);
        routine.addToContentsArray("sleep", true);
        routine.addToContentsArray("work", true);
        routine.addToContentsArray("walk", true);
        routine.addToContentsArray("study", true);

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
        rootSttucture.addText("abc", "ABC[]");
        rootSttucture.addText("def", "DEF\\");
        rootSttucture.addText("ghi", "GHI\"");

        MXJsonValue.HelperForStructure structure2 = rootSttucture.addStructure("attributes");
        structure2.addText("name", "synthtarou");
        structure2.addNumber("age", 47);
        structure2.addText("gender", "man");
        structure2.addText("fine", "thank you");

        MXJsonValue.HelperForArray routine = rootSttucture.addArray("routine");
        routine.addText("eat");
        routine.addText("sleep");
        routine.addText("work");
        routine.addText("walk");
        routine.addText("study");

        return root;
    }

    /**
     * 一般的なプログラムで用いるかたちでラベルを取得する
     * @return アンエスケープされたラベル
     */
    public String helpGetLabel() {
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
    public Number helpGetLabelAsNumber() throws NumberFormatException {
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
    public void helpSetLabel(String label) {
        if (label == null) {
            addToContentsValue("null", false);
        } else {
            addToContentsValue(label, true);
        }
    }

    /**
     * 一般的なプログラムで用いる数値でラベルを設定する
     * @param label 設定する数値
     */
    public void helpSetLabelAsNumber(Number label) {
        if (label == null) {
            addToContentsValue("null", false);
        } else {
            addToContentsValue(String.valueOf(label), false);
        }
    }

    /**
     * 配列タイプをあつかうヘルパークラス
     */
    public class HelperForArray {

        /**
         * コンストラクタ
         * MJsonValue value = ...;
         * MJsonValue.HelperForArray helper = value.new HelperForArray();
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
            return unescape(MXJsonValue.this._label);
        }

        /**
         * ラベル文字列を設定する
         * @param label エスケープされていないラベル文字列
         */
        public void setLabel(String label) {
            MXJsonValue.this._label = MXJsonValue.escape(label);
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
        public MXJsonValue getValue(int index) {
            MXJsonValue child = getContentsAt(index);
            return child;
        }

        /**
         * 配列から数を取得する 
         * @param index インデックス
         * @return 数値
         */
        public Number getNumber(int index) {
            MXJsonValue child = getContentsAt(index);
            return child.helpGetLabelAsNumber();
        }

        /**
         * 配列からエスケープされていない文字列を取得する 
         * @param index インデックス
         * @return 文字列
         */
        public String getText(int index) {
            MXJsonValue child = getContentsAt(index);
            return child.helpGetLabel();
        }

        /**
         * 配列にnullを追加する
         */
        public void addNull() {
            MXJsonValue.this.addToContentsArray("null", false);
        }

        /**
         * 配列に数値を追加する
         * @param value 数値
         */
        public void addNumber(Number value) {
            MXJsonValue.this.addToContentsArray(String.valueOf(value), false);
        }

        /**
         * 配列に文字列をエスケープして追加する
         * @param value エスケープされていない文字列
         */
        public void addText(String value) {
            if (value == null) {
                addNull();
            } else {
                MXJsonValue.this.addToContentsArray(value, true);
            }
        }

        /**
         * 配列にMJsonValue型を追加する
         * @param value 追加するMJsonValue
         */
        public void addValue(MXJsonValue value) {
            if (value == null) {
                addNull();
            } else {
                MXJsonValue.this.addToContentsArray(value);
            }
        }

        /**
         * 配列を追加して、HelperForArray型で取得する
         * @param label ラベル
         * @return 追加されたHelperForArray型
         */
        public HelperForArray addArray(String label) {
            MXJsonValue value = new MXJsonValue(label);
            addValue(value);
            return value.new HelperForArray();
        }

        /**
         * 構造体を追加して、HelperForStructure型で取得する
         * @param label ラベル
         * @return 追加されたHelperForStructure型
         */
        public HelperForStructure addStructure(String label) {
            MXJsonValue value = new MXJsonValue(label);
            addValue(value);
            return value.new HelperForStructure();
        }

        /**
         * 構造体を検索して、HelperForStructure型で取得する
         * @param index インデックス
         * @return HelperForStructure型
         */
        public HelperForStructure findStructure(int index) {
            MXJsonValue value = getContentsAt(index);
            return value.new HelperForStructure();
        }

        /**
         * 配列を検索して、HelperForStructure型で取得する
         * @param index インデックス
         * @return HelperForStructure型
         */
        public HelperForArray findArray(int index) {
            MXJsonValue value = getContentsAt(index);
            return value.new HelperForArray();
        }
    }

    /**
     * 構造体タイプをあつかうヘルパークラス
     */
    public class HelperForStructure {

        /**
         * コンストラクタ
         * MJsonValue value = ...;
         * MJsonValue.HelperForSturucture helper = value.new HelperForSturucture();
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
            return unescape(MXJsonValue.this._label);
        }

        /**
         * ラベル文字列を設定する
         * @param label エスケープされていないラベル文字列
         */
        public void setLabel(String label) {
            MXJsonValue.this._label = MXJsonValue.escape(label);
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
        public String getName(int index) {
            MXJsonValue child = getContentsAt(index);
            return child.helpGetLabel();
        }

        /**
         * 配列から要素を取得する
         * @param index　インデックス
         * @return 要素
         */
        public MXJsonValue getValue(int index) {
            MXJsonValue child = getContentsAt(index);
            return child;
        }

        /**
         * 配列から数を取得する 
         * @param index インデックス
         * @return 数値
         */
        public Number getNumber(int index) {
            MXJsonValue child = getContentsAt(index);
            return child.helpGetLabelAsNumber();
        }

        /**
         * 配列をラベル名で探索する
         * @param label ラベル名
         * @return 見つかったインデックス、見つからない場合-1
         */
        public int findByNameLabel(String label) {
            String escaped = label == null ? "null" : escape(label);
            for (int i = 0; i < MXJsonValue.this.contentsCount(); ++i) {
                MXJsonValue value = MXJsonValue.this.getContentsAt(i);
                if (escaped.equals(value._label)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * 配列を巣内で探索する
         * @param number 数値型のラベル
         * @return 見つかったインデックス、見つからない場合-1
         */
        public int findByNumberLabel(Number number) {
            String label = number == null ? "null" : String.valueOf(number);
            for (int i = 0; i < MXJsonValue.this.contentsCount(); ++i) {
                MXJsonValue value = MXJsonValue.this.getContentsAt(i);
                if (label.equals(value._label)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * 構造体から要素を取得する
         * @param name 文字列型のラベル
         * @return 要素
         */
        public MXJsonValue getValue(String name) {
            int index = findByNameLabel(name);
            if (index < 0) {
                return null;
            }
            MXJsonValue child = getContentsAt(index);
            return child;
        }

        /**
         * ラベルから文字列を取得する
         * @param name ラベル文字列
         * @return みつかった文字列
         */
        public String getText(String name) {
            MXJsonValue child = getValue(name);
            return child != null ? child.helpGetLabel() : null;
        }

        /**
         *
         * ラベルから数値を取得する
         * @param name ラベル文字列
         * @return みつかった数値
         */
        public Number getNumber(String name) {
            MXJsonValue child = getValue(name);
            return child != null ? child.helpGetLabelAsNumber() : null;
        }

        /**
         * 配列にnullを追加する
         * @param name ラベル
         */
        public void addNull(String name) {
            MXJsonValue.this.addToContentsStructure(name, "null", false);
        }

        /**
         * 配列に数値を追加する
         * @param name ラベル
         * @param value 数値
         */
        public void addNumber(String name, Number value) {
            MXJsonValue.this.addToContentsStructure(name, String.valueOf(value), false);
        }

        /**
         * 配列に文字列をエスケープして追加する
         * @param name ラベル
         * @param value エスケープされていない文字列
         */
        public void addText(String name, String value) {
            if (value == null) {
                addNull(name);
            } else {
                MXJsonValue.this.addToContentsStructure(name, value, true);
            }
        }

        /**
         * 配列にMJsonValue型を追加する
         * @param name 名前
         * @param value 追加するMJsonValue
         */
        public void addValue(String name, MXJsonValue value) {
            if (value == null) {
                addNull(name);
            } else {
                MXJsonValue temp = new MXJsonValue(name);
                temp.addToContentsValue(value);
                MXJsonValue.this.addToContentsStructure(temp);
            }
        }

        /**
         * 配列を追加して、HelperForArray型で取得する
         * @param label ラベル
         * @return 追加されたHelperForArray型
         */
        public HelperForArray addArray(String label) {
            MXJsonValue value = new MXJsonValue(label);
            addToContentsStructure(value);
            return value.new HelperForArray();
        }

        /**
         * 構造体を追加して、HelperForStructure型で取得する
         * @param label ラベル
         * @return 追加されたHelperForStructure型
         */
        public HelperForStructure addStructure(String label) {
            MXJsonValue value = new MXJsonValue(label);
            addToContentsStructure(value);
            return value.new HelperForStructure();
        }

        /**
         * 構造体を検索して、HelperForStructure型で取得する
         * @param label ラベル
         * @return HelperForStructure型
         */
        public HelperForStructure findStructure(String label) {
            int index = findByNameLabel(label);
            if (index < 0) {
                return null;
            }
            MXJsonValue value = getContentsAt(index);
            return value.new HelperForStructure();
        }

        /**
         * 配列を検索して、HelperForStructure型で取得する
         * @param label ラベル
         * @return HelperForStructure型
         */
        public HelperForArray findArray(String label) {
            int index = findByNameLabel(label);
            if (index < 0) {
                return null;
            }
            MXJsonValue value = getContentsAt(index);
            return value.new HelperForArray();
        }
        
        public String getSettingText(String label, String defValue) {
            try {
                MXJsonValue var = getValue(label);
                if (var != null) {
                    return unescape(var._label);
                }
            }
            catch(Throwable ex) {
                
            }
            return defValue;
        }
        
        public int getSettingInt(String label, int defvalue) {
            try {
                MXJsonValue var = getValue(label);
                if (var != null) {
                    return Integer.parseInt(var._label);
                }
            }catch(Exception ex) {
                
            }
            return defvalue;
        }

        public boolean getSettingBool(String label, boolean defvalue) {
            try {
                MXJsonValue var = getValue(label);
                if (var != null) {
                    if (var._label.equalsIgnoreCase("true")) {
                        return true;
                    }
                    if (var._label.equalsIgnoreCase("false")) {
                        return true;
                    }
                    return Integer.parseInt(var._label) > 0;
                }
            }catch(Exception ex) {
                
            }
            return defvalue;
        }

        public void setSettingText(String label, String value) {
            MXJsonValue var = getValue(label);
            if (var != null) {
                var._label = escape(value);
            }else {
                addText(value, value);
            }
        }
        
        public void setSettingInt(String label, int value) {
            MXJsonValue var = getValue(label);
            if (var != null) {
                var._label = String.valueOf(value);
            }else {
                addNumber(label, value);
            }
        }

        public void setSettingBool(String label, boolean value) {
            MXJsonValue var = getValue(label);
            if (var != null) {
                var._label = value ? "true" : "false";
            }else {
                MXJsonValue.this.addToContentsValue(value ? "true" : "false", false);
            }
        }
    }

    /**
     * json用のエスケープシーケンスで、エスケープする
     *
     * @param unescaped　エスケープされてない文字列を渡す
     * @return エスケープされた文字列
     */
    public static String escape(String unescaped) {
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
