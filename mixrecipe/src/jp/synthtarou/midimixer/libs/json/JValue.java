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
 *
 * @author Syntarou YOSHIDA
 */
public class JValue {

    public JValue(String text) {
        _name = text;
    }

    String _name;
    ArrayList<JValue> _children;
    boolean _hasArray;
    boolean _hasSturucture;
    boolean _hasSingleValue;

    public static final int TYPE_ANY = 0;
    public static final int TYPE_NULL = 1;
    public static final int TYPE_DOUBLECOMMA = 2;
    public static final int TYPE_START_LIST = 4;
    public static final int TYPE_START_STRUCTURE = 5;
    public static final int TYPE_END_LIST = 6;
    public static final int TYPE_END_STRUCTURE = 7;
    public static final int TYPE_COMMA = 8;
    public static final int TYPE_TEXT = 10;

    public int getType() {
        if (_name == null) {
            return TYPE_NULL;
        }
        if (_name.length() == 0) {
            return TYPE_TEXT;
        }
        if (_name.length() == 1) {
            int ch = _name.charAt(0);
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
        if (_name.equals("null")) {
            return TYPE_NULL;
        }
        return TYPE_TEXT;
    }

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

    public String getValueString() {
        switch (getType()) {
            case TYPE_NULL:
                return "null";
            case TYPE_DOUBLECOMMA:
                return ":";
            case TYPE_COMMA:
                return ",";
            case TYPE_TEXT:
                return _name;

        }
        return null;
    }

    public void addToArray(JValue var) {
        if (_children == null) {
            _children = new ArrayList<>();
        }
        if (_hasSturucture || _hasSingleValue) {
            throw new IllegalStateException();
        }
        _hasArray = true;
        _children.add(var);
    }

    public void addToStructure(JValue var) {
        if (_children == null) {
            _children = new ArrayList<>();
        }
        if (_hasArray || _hasSingleValue) {
            throw new IllegalStateException();
        }
        _hasSturucture = true;
        _children.add(var);
    }

    public void addToValue(JValue var) {
        if (_children == null) {
            _children = new ArrayList<>();
        }
        if (_hasArray || _hasSturucture) {
            throw new IllegalStateException();
        }
        _hasSingleValue = true;
        _children.add(var);
    }

    public String toString() {
        String name2 = (_name == null) ? "" : _name;
        return name2;
    }

    public void adjustedEnter(StringBuffer str) {
        if (str.length() > 0) {
            if (str.charAt(str.length() - 1) == '\n') {
                return;
            }
            str.append("\n");
        }
    }

    public String format() {
        StringBuffer str = new StringBuffer();
        if (_name != null) {
            str.append(_name);
        }
        if (_hasArray) {
            if (_name != null) {
                str.append(":");
            }
            str.append("[");
            if (_children != null) {
                for (JValue seek : _children) {
                    if (str.length() != 1) {
                        str.append(",");
                    }
                    str.append(seek.format());
                }
            }
            str.append("]");
            adjustedEnter(str);
        } else if (_hasSturucture) {
            if (_name != null) {
                str.append(":");
            }
            str.append("{");
            if (_children != null) {                
                for (JValue seek : _children) {
                    if (str.length() != 1) {
                        str.append(",");
                        adjustedEnter(str);
                    }
                    str.append(seek.format());
                }
            }
            str.append("}");
            adjustedEnter(str);
        } else if (_hasSingleValue) {
            if (_name != null) {
                str.append(":");
            }
            if (_children.size() > 0) {
                str.append(_children.get(0).format());
            } else {
                str.append("null");
            }
        }
        return str.toString();
    }
}
