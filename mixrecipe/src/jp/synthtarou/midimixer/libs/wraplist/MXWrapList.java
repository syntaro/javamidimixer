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
package jp.synthtarou.midimixer.libs.wraplist;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXWrapList<T> extends ArrayList<MXWrap<T>> implements ListModel, ComboBoxModel {

    boolean _ignoreCase = false;

    public MXWrapList() {
        super();
    }

    public boolean ignoreCase() {
        return _ignoreCase;
    }
    
    public void setIgnoreCase(boolean ignore) {
        _ignoreCase = ignore;
    }

    public List<String> nameList() {
        ArrayList<String> list = new ArrayList();
        for (MXWrap<T> e : this) {
            list.add(e._name);
        }
        return list;
    }

    public List<T> valueList() {
        ArrayList<T> list = new ArrayList();
        for (MXWrap<T> e : this) {
            list.add(e._value);
        }
        return list;
    }
   
    public boolean existsName(String name) {
        if (_ignoreCase) {
            for (MXWrap<T> e : this) {
                if(e._name.equalsIgnoreCase(name)) {
                    return true;
                }
            }
            return false;
        }else {
            for (MXWrap<T> e : this) {
                if(e._name.equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    public int indexOfName(String name) {
        if (_ignoreCase) {
            int x = 0;
            for (MXWrap<T> e : this) {
                if(e._name.equalsIgnoreCase(name)) {
                    return x;
                }
                x ++;
            }
            return -1;
        }else {
            int x = 0;
            for (MXWrap<T> e : this) {
                if(e._name.equals(name)) {
                    return x;
                }
                x ++;
            }
            return -1;
        }
    }
    
    public int indexOfNameShrink(String name) {
        name = MXUtil.shrinkText(name);
        if (_ignoreCase) {
            int x = 0;
            for (MXWrap<T> e : this) {
                String name2 = MXUtil.shrinkText(e._name);
                if(name2.equalsIgnoreCase(name)) {
                    return x;
                }
                x ++;
            }
            return -1;
        }else {
            int x = 0;
            for (MXWrap<T> e : this) {
                String name2 = MXUtil.shrinkText(e._name);
                if(name2.equalsIgnoreCase(name)) {
                    return x;
                }
                x ++;
            }
            return -1;
        }
    }

    public int indexOfValue(T value) {
        int x = 0;
        for (MXWrap<T> e : this) {
            if(e._value == value) {
                return x;
            }
            x ++;
        }
        x = 0;
        for (MXWrap<T> e : this) {
            if(e._value != null && value != null) {
                if (e._value.equals(value)) {
                    return x;
                }
            }
            x ++;
        }
        return -1;
    }

    public String nameOfIndex(int x) {
        return get(x)._name;
    }

    public T valueOfIndex(int x) {
        return get(x)._value;
    }
    
    public T valueOfName(String name) {
        int x = indexOfName(name);
        if (x < 0) {
            return null;
        }
        return get(x)._value;
    }

    public String nameOfValue(T value) {
        int x = indexOfValue(value);
        if (x < 0) {
            return null;
        }
        return get(x)._name;
    }

    public boolean addNameAndValue(String name, T value) {
        boolean x = add(new MXWrap(name, value));
        return x;
    }
    
    @Override
    public boolean add(MXWrap<T> value) {
        boolean x = super.add(value);
        fireIntervalAdded(this, size() - 1, size() -1);
        return x;
    }

    @Override
    public int getSize() {
        return size();
    }

    @Override
    public Object getElementAt(int index) {
        return get(index);
    }

    List<ListDataListener> _listeners = new ArrayList<>();
    
    Object _selected = null;
    
    @Override
    public void addListDataListener(ListDataListener l) {
        _listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        _listeners.remove(l);
    }
    
    protected void fireContentsChanged(Object source, int index0, int index1) {
        ListDataEvent e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index0, index1);
        for (ListDataListener l : _listeners) {
            l.contentsChanged(e);
        }
    }
    
    protected void fireIntervalAdded(Object source, int index0, int index1) {
        ListDataEvent e = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED, index0, index1);
        for (ListDataListener l : _listeners) {
            l.intervalAdded(e);
        }
    }

    protected void fireIntervalRemoved(Object source, int index0, int index1) {
        ListDataEvent e = new ListDataEvent(source, ListDataEvent.INTERVAL_REMOVED, index0, index1);
        for (ListDataListener l : _listeners) {
            l.intervalRemoved(e);
        }
    }

    @Override
    public void setSelectedItem(Object o) {
        _selected = o;
    }

    @Override
    public Object getSelectedItem() {
        if (_selected == null) {
            //　自動で初期化する
            if (size() > 0) {
                _selected = get(0);
            }
        }
        return _selected;
    }
    
    public T readComboBox(JComboBox box) {
        ComboBoxModel model = box.getModel();
        MXWrap<T> item = (MXWrap)model.getSelectedItem();
        if (item != null) {
            return item._value;
        }
        return null;
    }

    public void writeComboBox(JComboBox<?> box, T value) {
        ComboBoxModel model = this;
        box.setModel(this);
        for (int i = 0; i < model.getSize(); ++ i) {
            MXWrap<T> item = (MXWrap)model.getElementAt(i);
            if (item != null) {
                if (value == null) {
                    if (item._value == null) {
                        box.setSelectedIndex(i);
                        return;
                    }
                }else if (value.getClass().isPrimitive()) {
                    if (item._value == value) {
                        box.setSelectedIndex(i);
                        return;
                    }
                }else {
                    if (item._value == null && value == null) {
                        box.setSelectedIndex(i);
                        return;
                    }
                    if (item._value != null && value != null) {
                        if (item._value.equals(value)) {
                            box.setSelectedIndex(i);
                            return;
                        }
                    }
                }
            }
        }
    }
}
