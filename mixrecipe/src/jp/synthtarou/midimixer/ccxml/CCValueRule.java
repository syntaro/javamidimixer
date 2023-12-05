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
package jp.synthtarou.midimixer.ccxml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import jp.synthtarou.midimixer.ccxml.CXNode;
import jp.synthtarou.midimixer.ccxml.CCRuleForAttribute;
import jp.synthtarou.midimixer.ccxml.CCRuleForTag;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCValueRule {

    int _type = TYPE_TEXT;

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_MENU = 1;
    public static final int TYPE_NUMBER = 2;
    public static final int TYPE_CUSTOM = 3;

    ArrayList<String> _listMenuItem;
    int _valueFrom, _valueTo, _valueStep = 0;


    public static CCValueRule valueRuleText = new CCValueRule(TYPE_TEXT);
    public static CCValueRule valueRulePlusMinus = new CCValueRule(TYPE_NUMBER);
    public static CCValueRule valueRuleChannel = new CCValueRule(TYPE_NUMBER, 1, 16, 1);

    public static CCValueRule valueRule1bit = new CCValueRule(TYPE_NUMBER, 0, 1, 1);
    public static CCValueRule valueRule4bit = new CCValueRule(TYPE_NUMBER, 0, 15, 1);
    public static CCValueRule valueRule7bit = new CCValueRule(TYPE_NUMBER, 0, 127, 1);
    public static CCValueRule valueRule14bit = new CCValueRule(TYPE_NUMBER, 0, 16383, 1);

    public static CCValueRule valueRuleColorFormat = new CCValueRule(TYPE_CUSTOM) {
        public  boolean validateText(String text) {
            try {
                if (text.charAt(0) != '#') {
                    return false;
                }
                String red = text.substring(1, 2);
                String green = text.substring(3, 4);
                String blue = text.substring(5, 6);
                
                if (MXUtil.numberFromText("0x" + red, -1) < 0
                 || MXUtil.numberFromText("0x" + green, -1) < 0
                 || MXUtil.numberFromText("0x" + blue, -1) < 0) {
                    return false;
                }

                return true;
            }catch(ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            return false;
        }
    };

    public static CCValueRule valueKeySignature = new CCValueRule(TYPE_TEXT);
    
    public static CCValueRule valueRuleTimeSignature = new CCValueRule(TYPE_CUSTOM) {
        public  boolean validateText(String text) {
            try {
                //4/4 etc
                return true;
            }catch(ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            return false;
        }
    };
    public static CCValueRule valueRuleTypeDrumOR = new CCValueRule(TYPE_MENU, new String[] { "" , "Auto", "Drumset"} );

    public static CCValueRule valueRuleSyncOR = new CCValueRule(TYPE_MENU, new String[] { "" , "Sync", "MuteSync"} );

    public static CCValueRule valueRuleKeyOR = new CCValueRule(TYPE_MENU, new String[] { "" , "Key"} );
    
    
    public CCValueRule() {
        this(TYPE_TEXT);
    }

    public CCValueRule(int type) {
        _type = type;
    }

    public CCValueRule(int type, int from, int to, int step) {
        _type = type;
        if (type != TYPE_NUMBER) {
            throw new IllegalArgumentException("type was not number");
        }
        _valueFrom = from;
        _valueTo = to;
        _valueStep = step;
    }
    
    public CCValueRule(int type, String[] menu) {
        _type = type;
        if (type != TYPE_MENU) {
            throw new IllegalArgumentException("type was not number");
        }
        _listMenuItem = new ArrayList<>();
        for (String x : menu) {
            _listMenuItem.add(x);
        }
    }
    
    public boolean validateText(String text) {
        switch (_type) {
            case TYPE_TEXT:
                return true;

            case TYPE_MENU:
                if (_listMenuItem.indexOf(text) >= 0) {
                    return true;
                }
                break;

            case TYPE_NUMBER:
                try {
                int x = Integer.parseInt(text);
                if (_valueStep == 0) {
                    return true;
                } else if (_valueStep > 0) {
                    if (_valueFrom <= x && x <= _valueTo) {
                        return true;
                    }
                } else {
                    if (_valueTo <= x && x <= _valueFrom) {
                        return true;
                    }
                }
            } catch (NumberFormatException e) {
            }
            break;
        }
        return false;
    }

    public ArrayList<String> getMenu() {
        return _listMenuItem;
    }

    public void reverseMenu() {
        if (_listMenuItem != null) {
            ArrayList<String> temp = new ArrayList<>(_listMenuItem);
            _listMenuItem.clear();
            for (int i = temp.size() - 1; i >= 0; --i) {
                _listMenuItem.add(temp.get(i));
            }
        }
    }

    public CCValueRule valueRuleForAttributeNames(CXNode node) {
        _listMenuItem = new ArrayList<>();
        _type = TYPE_MENU;

        CCRuleForTag def = node.getTagRule();
        List<CCRuleForAttribute> listDefAttr = def.listAttributes();

        HashSet<String> already = new HashSet();
        for (int i = 0; i < listDefAttr.size(); ++i) {
            String text = listDefAttr.get(i).getName();
            if (already.contains(text)) {
                continue;
            }
            already.add(text);
            _listMenuItem.add(text);
        }
        return this;
    }

    public CCValueRule valueRuleForTagNames(CXNode node) {
        _listMenuItem = new ArrayList<>();
        _type = TYPE_MENU;

        CCRuleForTag def = node.getTagRule();
        List<CCRuleForTag> listDefTag = def.listChildTags();

        HashSet<String> already = new HashSet();
        for (int i = 0; i < listDefTag.size(); ++i) {
            String text = listDefTag.get(i).getName();
            if (already.contains(text)) {
                continue;
            }
            already.add(text);
            _listMenuItem.add(text);
        }
        return this;
    }

    public CCValueRule valueRuleForAttributeValues(CXNode node, String attributeName) {
        int x = node._listAttributes.indexOfName(attributeName);
        CCRuleForTag def = node.getTagRule();
        CCRuleForAttribute defAttr = def.getAttribute(attributeName);
        CCValueRule rule = defAttr.getValueRule();

        _type = rule._type;

        _valueFrom = rule._valueFrom;
        _valueTo = rule._valueTo;
        _valueStep = rule._valueStep;

        if (rule.getMenu() == null) {
            _listMenuItem = null;
        } else {
            _listMenuItem = new ArrayList<>();
            _listMenuItem.addAll(rule.getMenu());
        }
        return this;
    }

    public CCValueRule valueRuleForNumber(int from, int to, int step) {
        _listMenuItem = null;

        _type = TYPE_NUMBER;
        _valueFrom = from;
        _valueTo = to;
        _valueStep = step;
        _listMenuItem = null;
        return this;
    }

    public ArrayList<String> refillForUI() {
        switch (_type) {
            case TYPE_TEXT:
                return null;
            case TYPE_MENU:
                return _listMenuItem;
            case TYPE_NUMBER:
                if (_listMenuItem != null) {
                    return _listMenuItem;
                }
                _listMenuItem = new ArrayList<>();
                if (_valueFrom < _valueTo) {
                    if (_valueStep < 0) {
                        throw new IllegalArgumentException(" from < to & step < 0");
                    }
                    for (int x = _valueFrom; x < _valueTo; x += _valueStep) {
                        _listMenuItem.add(String.valueOf(x));
                    }
                    _listMenuItem.add(String.valueOf(_valueTo));
                } else {
                    if (_valueStep > 0) {
                        throw new IllegalArgumentException(" from < to & step > 0");
                    }
                    for (int x = _valueFrom; x > _valueTo; x += _valueStep) {
                        _listMenuItem.add(String.valueOf(x));
                    }
                    _listMenuItem.add(String.valueOf(_valueTo));
                }
                return _listMenuItem;
        }
        return null;
    }
}
