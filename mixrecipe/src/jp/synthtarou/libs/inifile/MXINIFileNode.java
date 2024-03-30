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
package jp.synthtarou.libs.inifile;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.libs.MXFileLogger;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXINIFileNode {
    protected MXINIFile _setting;
    
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
    
    public ArrayList<MXINIFileNode> _children;
    
    public MXINIFileNode(MXINIFile setting, StringPath path, String name) {
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

    protected  MXINIFileNode childByIndex(int index) {
        if (_children.size() >= index - 1) {
            return _children.get(index);
        }
        return null;
    }

    protected  MXINIFileNode childByKey(String name) {
        for (MXINIFileNode e : _children) {
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
        for (MXINIFileNode node : _children) {
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
    
    public MXINIFileNode findNode(String name) {
        StringPath path = StringPath.parsePath(name);
        MXINIFileNode node = this;
        for (String text : path) {
            MXINIFileNode e = node.childByKey(text);
            if (e == null) {
                return null;
            }
            node = e;
        }
        return node;
    }
    
    public List<MXINIFileNode> findNumbers() {
        ArrayList<MXINIFileNode> retList = new ArrayList();
        for (MXINIFileNode node : _children) {
            if (node.isInteger()) {
                retList.add(node);
            }
        }
        return retList;
    }

    public String getSetting(String name) {
        MXINIFileNode node = findNode(name);
        if (node != null) {
            return node._value;
        }
        return null;
    }

    public int getSettingAsInt(String name, int defval) {
        MXINIFileNode node = findNode(name);
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
        MXINIFileNode node = findNode(name);
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

    public boolean getSettingAsBoolean(String name, boolean defval) {
        if (defval) {
            return MXINIFileNode.this.getSettingAsInt(name, 1) != 0;
        }else {
            return MXINIFileNode.this.getSettingAsInt(name, 0) != 0;
        }
    }

    public boolean setSetting(String name, String value) {
        if (isRegistered(name) == false) {
            Exception ex = new Exception("Not registered " + name + " = " + value);
            MXFileLogger.getLogger(MXINIFileNode.class).log(Level.WARNING, ex.getMessage(), ex);
            return false;
        }

        StringPath child = StringPath.parsePath(name);
        StringPath current = _path;

        MXINIFileNode node = this;
        for (String text : child) {
            MXINIFileNode e = node.childByKey(text);
            if (e == null) {
                e = new MXINIFileNode(_setting, current, text);
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

        for (MXINIFileNode e : _children) {
            e.recuesiveDump(writer, indent + 1, registered);
        }
    }
}
