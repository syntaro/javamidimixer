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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import jp.synthtarou.midimixer.MXMain;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXSettingNode {
    protected MXSetting _setting;
    
    public String toString() {
        return "[" + _path + "=" + _value + "(" + _children.size() + ")]";
    }

    public void register(String name) {
        StringPath p = _path.clone();
        p.addAll(StringPath.parsePath(name));
        _setting.register(p);
    }

    public boolean isRegistered(String name) {
        StringPath p = _path.clone();
        p.addAll(StringPath.parsePath(name));
        return _setting.isRegistered(p);
    }

    private final StringPath _path;
    public String _value;
 
    protected StringPath getPath() {
        return _path;
    }
    
    public String getName() {
        if (_path.size() > 0) {
            return _path.get(_path.size() - 1);
        }
        return null;
    }
    
    public boolean isInteger() {
        if (_path.size() > 0) {
            return MXSettingUtil.isInteger(_path.get(_path.size() -1));
        }
        return false;
    }
    
    public ArrayList<MXSettingNode> _children;
    
    public MXSettingNode(MXSetting setting, StringPath path, String name) {
        _children = new ArrayList();
        _setting = setting;
        _path = new StringPath();
        if (path != null) {
            _path.addAll(path);
        }
        if (name != null) {
            _path.add(name);
        }
    }
    
    public int size() {
        return _children.size();
    }

    protected  MXSettingNode childByIndex(int index) {
        if (_children.size() >= index - 1) {
            return _children.get(index);
        }
        return null;
    }

    protected  MXSettingNode childByKey(String name) {
        for (MXSettingNode e : _children) {
            if (name.equals(e.getName())) {
                return e;
            }
        }
        return null;
    }
    
    public boolean isEmpty() {
        if (_setting.isRegistered(_path)) {
            if (_value != null) {
                return false;
            }
        }
        for (MXSettingNode node : _children) {
            if (node.isEmpty() == false) {
                return false;
            }
        }
        return true;
    }
        
    public void clearValues() {
        _value = null;
        _children = new ArrayList();
    }
    
    public MXSettingNode findNode(String name) {
        StringPath path = StringPath.parsePath(name);
        MXSettingNode node = this;
        for (String text : path) {
            MXSettingNode e = node.childByKey(text);
            if (e == null) {
                return null;
            }
            node = e;
        }
        return node;
    }
    
    public List<MXSettingNode> findNumbers() {
        ArrayList<MXSettingNode> retList = new ArrayList();
        for (MXSettingNode node : _children) {
            if (node.isInteger()) {
                retList.add(node);
            }
        }
        return retList;
    }

    public String getSetting(String name) {
        MXSettingNode node = findNode(name);
        if (node != null) {
            return node._value;
        }
        return null;
    }

    public int getSettingAsInt(String name, int defval) {
        MXSettingNode node = findNode(name);
        if (node != null) {
            String text = node._value;
            try {
                return Integer.parseInt(text);
            }catch(NumberFormatException e) {
                return defval;
            }
        }
        return defval;
    }

    public Integer getSettingAsInt(String name) {
        MXSettingNode node = findNode(name);
        if (node != null) {
            String text = node._value;
            try {
                return Integer.valueOf(text);
            }catch(NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /*
    public Integer getSettingAsWrappedInteger(String name, MXWrapList<Integer> table) {
        MXSettingNode node = findNode(name);
        if (node != null) {
            String text = node._value;
            try {
                Integer x = table.valueOfName(text);
                if (x == null) {
                    return null;
                }
                return x;
            }catch(NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    */
    
    public boolean getSettingAsBoolean(String name, boolean defval) {
        if (defval) {
            return MXSettingNode.this.getSettingAsInt(name, 1) != 0;
        }else {
            return MXSettingNode.this.getSettingAsInt(name, 0) != 0;
        }
    }

    /*
    public Boolean getSettingAsBoolean(String name) {
        Integer x = getSettingAsInt(name);
        if (x != null) {
            return x == 0 ? Boolean.FALSE : Boolean.TRUE;
        }
        return null;
    }
    */
    
/*
    public boolean setSetting(String name, MXWrapList<Integer> table, int value) {
        String valueText = table.nameOfValue(value);
        if (valueText == null) {
            throw new MXSettingException("setSetting Not Found Name Of Value " + value);
        }
        return setSetting(name, valueText);
    }
*/
    
    public boolean setSetting(String name, String value) {
        if (isRegistered(name) == false) {
            new Throwable("setSetting Not registered " + name + " = " + value).printStackTrace();
            return false;
        }

        StringPath child = StringPath.parsePath(name);
        StringPath current = _path;

        MXSettingNode node = this;
        for (String text : child) {
            MXSettingNode e = node.childByKey(text);
            if (e == null) {
                e = new MXSettingNode(_setting, current, text);
                node._children.add(e);
            }
            node = e;
            current = e._path;
        }
        node._value = value;
        return true;
    }
    
    public boolean setSetting(String name, int value) {
        return setSetting(name, String.valueOf(value));
    }
    
    public boolean setSetting(String name, boolean value) {
        if (value) {
            return setSetting(name, "1");
        }else {            
            return setSetting(name, "0");
        }
    }

    public boolean havingName(String name) {
        return childByKey(name) != null;
    }

    protected void recuesiveDump(Writer writer) throws IOException {
        TreeSet reg = new TreeSet();
        recuesiveDump(writer, 0, reg);
    }

    protected void recuesiveDump(Writer writer, int indent, TreeSet registered) throws IOException {
        StringBuffer text = new StringBuffer();
        boolean wasnum = false;
        boolean first = true;
        for (String e : _path) {
            if (MXSettingUtil.isInteger(e)) {
                if (wasnum) {
                }else {
                }
                text.append("[" + e + "]");
                wasnum = true;
                first = false;
            }else {
                if (!first) {
                    if (wasnum) {
                        text.append(".");
                    }else {
                        text.append(".");
                    }
                }
                text.append(e);
                wasnum = false;
                first = false;
            }
        }
        if (_setting.isRegistered(_path)) {
            if (_value == null) {
                writer.write(text + "=" + "" + "\n");
            }else {
                writer.write(text + "=" + _value + "\n");
            }
        }else if (_value != null) {
            MXMain.printDebug("not registered " + text + "=" + _value);
        }

        for (MXSettingNode e : _children) {
            e.recuesiveDump(writer, indent + 1, registered);
        }
    }
}
