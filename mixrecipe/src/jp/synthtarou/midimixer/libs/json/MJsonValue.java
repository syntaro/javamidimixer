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

import java.util.ArrayList;

/**
 * jsonファイル内のラベルとコンテンツを表現するクラス。ネストしている。
 * @author Syntarou YOSHIDA
 */
public class MJsonValue {

    /**
     * ラベルを指定してオブジェクトを生成する
     * @param text
     */
    public MJsonValue(String text) {
        _label = text;
    }

    /**
     * ラベルとコンテンツを指定してオブジェクトを生成する
     * @param label ラベル
     * @param value コンテンツ（単一）
     * @param doEscapeValue コンテンツをエスケープする必要がある場合
     */
    public MJsonValue(String label, String value, boolean doEscapeValue) {
        _label = label;
        if (doEscapeValue) {
            value = MJsonReader.encape(value);
        }
        addToContentsValue(new MJsonValue(value));
    }

    String _label;
    ArrayList<MJsonValue> _listContents;
    
    boolean _contentsTypeArray;
    boolean _contentsTypeStructure;
    boolean _contentsTypeList;

    /**
     * タイプ：オールマイティ
     */
    public static final int TYPE_ANY = 0;

    /**
     * タイプ：NULL
     */
    public static final int TYPE_NULL = 1;

    /**
     * タイプ:ダブルカンマ -> :
     */
    public static final int TYPE_DOUBLECOMMA = 2;

    /**
     * タイプ:リストの開始-> [
     */
    public static final int TYPE_START_LIST = 4;

    /**
     * タイプ:構造体(名前つきリスト)の開始-> {
     */
    public static final int TYPE_START_STRUCTURE = 5;

    /**
     * タイプ:リストの終了-> ]
     */
    public static final int TYPE_END_LIST = 6;

    /**
     * タイプ:構造体(名前つきリスト)の終了-> }
     */
    public static final int TYPE_END_STRUCTURE = 7;

    /**
     * タイプ：カンマ -> ,
     */
    public static final int TYPE_COMMA = 8;

    /**
     *　タイプ：テキスト（エスケープのありなしはプログラマーが管理すること
     */
    public static final int TYPE_TEXT = 10;

    /**
     * ラベルの文字列からタイプを推測する
     * @return タイプ
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
     *　推測されたタイプから、タイプを表すタイプ名を取得する
     * @return タイプ名
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
     *　contentsとして配列を選択し、varを追加する
     * @param var MJsonValueタイプ
     */
    public void addToContentsArray(MJsonValue var) {
        if (_contentsTypeStructure || _contentsTypeList) {
            throw new IllegalStateException();
        }
        if (_listContents == null) {
            _listContents = new ArrayList<>();
        }
        _contentsTypeArray = true;
        _listContents.add(var);
    }

    /**
     *　contentsとして名前つきリスト(構造体)を選択し、varを追加する
     * @param var MJsonValueタイプ
     */
    public void addToContentsStructure(MJsonValue var) {
        if (_contentsTypeArray || _contentsTypeList) {
            throw new IllegalStateException();
        }
        if (_listContents == null) {
            _listContents = new ArrayList<>();
        }
        _contentsTypeStructure = true;
        _listContents.add(var);
    }

    /**
     *　contentsとして単一のvarを設定する
     * @param var MJsonValueタイプ
     */
    public void addToContentsValue(MJsonValue var) {
        if (_contentsTypeArray || _contentsTypeStructure) {
            throw new IllegalStateException();
        }
        if (_listContents == null) {
            _listContents = new ArrayList<>();
        }
        else {
            _listContents.clear();
        }
        _contentsTypeList = true;
        _listContents.clear();
        _listContents.add(var);
    }

    /**
     *　contentsとして名前つきリスト(構造体)を選択し、Stringを追加する
     * @param label ラベル
     * @param value 値（文字列）文字列以外の場合、もうひとつの同名メソッドを用いること
     * @param doEscapeValue 値をエスケープする必要があるか
     */
    public void addToContentsStructure(String label, String value, boolean doEscapeValue) {
        MJsonValue temp = new MJsonValue(label, value, doEscapeValue);
        addToContentsStructure(temp);
    }

    /**
     *　contentsとして配列を選択し、Stringを追加する
     * @param value 配列についかされる値（子のラベル)
     * @param doEscape 値をエスケープする必要があるか
     */
    public void addToContentsArray(String value, boolean doEscape) {
        MJsonValue temp = new MJsonValue(value);
        if (doEscape) {
            temp.setLabel(value, doEscape);
        }
        addToContentsArray(temp);
    }

    /**
     *　contentsとして単一のvarを設定する
     * @param var 子のラベル=thisのcontents
     * @param doEscape 値をエスケープする必要があるか
     */
    public void addToContentsValue(String var, boolean doEscape) {
        MJsonValue temp = new MJsonValue(var);
        if (doEscape) {
            temp.setLabel(var, doEscape);
        }
        addToContentsValue(temp);
    }

    /**
     * String型にする（表示用）
     * @return ラベルのテキスト
     */
    public String toString() {
        return (_label == null) ? "" : _label;
    }

    private void enterIfNeed(StringBuffer str) {
        if (str.length() > 0) {
            if (str.charAt(str.length() - 1) == '\n') {
                return;
            }
            str.append("\n");
        }
    }

    /**
     * 表示用に、自身とコンテンツを簡易整形する
     * @return 整形された文字列
     */
    public String formatForDisplay() {
        StringBuffer str = new StringBuffer();
        if (_label != null) {
            str.append(_label);
        }
        if (_contentsTypeArray) {
            if (_label != null) {
                str.append(":");
            }
            str.append("[");
            if (_listContents != null) {
                boolean first = true;
                for (MJsonValue seek : _listContents) {
                    if (first == false) {
                        str.append(",");
                    }
                    first = false;
                    str.append(seek.formatForDisplay());
                }
            }
            str.append("]");
            enterIfNeed(str);
        } else if (_contentsTypeStructure) {
            if (_label != null) {
                str.append(":");
            }
            str.append("{");
            if (_listContents != null) {                
                boolean first = true;
                for (MJsonValue seek : _listContents) {
                    if (first == false) {
                        str.append(",");
                        enterIfNeed(str);
                    }
                    first = false;
                    str.append(seek.formatForDisplay());
                }
            }
            str.append("}");
            enterIfNeed(str);
        } else if (_contentsTypeList) {
            if (_label != null) {
                str.append(":");
            }
            if (_listContents.size() > 0) {
                str.append(_listContents.get(0).formatForDisplay());
            } else {
                str.append("null");
            }
        }
        return str.toString();
    }
    
    /**
     * ラベルをあとから変更する
     * @param label ラベル
     * @param doEscape エスケープする必要がある場合
     */
    public void setLabel(String label, boolean doEscape) {
        if (doEscape) {
            label = MJsonReader.encape(label);
        }
        _label = label;
    }
    
    /**
     * ラベルを取得する
     * @param doUnescape アンエスケープ（解除）する必要がある場合
     * @return ラベル
     */
    public String getLabel(boolean doUnescape) {
        String label = _label;
        if (doUnescape) {
            label = MJsonReader.unescape(label);
        }
        return label;
    }
    
    /**
     * コンテンツのカウント
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
     * @param index インデックス
     * @return MJsonValue型
     */
    public MJsonValue getContentsAt(int index) {
        if (_listContents == null) {
            return null;
        }
        return _listContents.get(index);
    }

    /**
     * コンテンツをラベルから取得する（名前つき配列の場合用いることができる）
     * @param label ラベル
     * @param doEscapeAndSearch エスケープしてから探索する場合
     * @return 見つかったインデックス
     */
    public int searchContentsByLabel(String label, boolean doEscapeAndSearch) {
        if (doEscapeAndSearch) {
            label = MJsonReader.encape(label);
        }
        for (int i = 0; i < _listContents.size(); ++ i) {
            MJsonValue seek = _listContents.get(i);
            if (seek._label == null){
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
     * @param args 不使用
     */
    public static void main(String[] args) {
        MJsonValue root = new MJsonValue(null);
        
        root.addToContentsStructure(new MJsonValue("abc", "ABC[]", true));
        root.addToContentsStructure(new MJsonValue("def", "DEF\\", true));
        root.addToContentsStructure(new MJsonValue("ghi", "GHI\"", true));
        
        MJsonValue attributes = new MJsonValue("attributes");
        attributes.addToContentsStructure("name", "synthtarou", true);
        attributes.addToContentsStructure("age", "47", false);
        attributes.addToContentsStructure("gender", "man", true);
        attributes.addToContentsStructure("fine", "thank you", true);
        
        root.addToContentsStructure(attributes);
        
        MJsonValue routine = new MJsonValue("routine");
        routine.addToContentsArray("eat", true);
        routine.addToContentsArray("sleep", true);
        routine.addToContentsArray("work", true);
        routine.addToContentsArray("walk", true);
        routine.addToContentsArray("study", true);
        
        root.addToContentsStructure(routine);
        
        System.out.println(root.formatForDisplay());
        
        MJsonParser parser = new MJsonParser(root.formatForDisplay());
        System.out.println("***** COMPARE ***********");
        System.out.println(parser._treeRoot.formatForDisplay());
    }
}
