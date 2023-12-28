/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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
package jp.synthtarou.midimixer.mx36ccmapping;

import java.util.Comparator;
import java.util.List;
import jp.synthtarou.midimixer.libs.accordion.MXAccordion;
import jp.synthtarou.midimixer.libs.accordion.MXAccordionElement;
import jp.synthtarou.midimixer.libs.accordion.MXAccordionFocus;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36Folder implements Comparable<MX36Folder> {

    MX36StatusList _list;
    MXAccordionFocus _focus;
    MX36Process _process;

    final String _folderName;
    int _order;
    final MXAccordion _accordion;

    public MX36Folder(MX36Process process, MXAccordionFocus focus, int order, String name) {
        _list = new MX36StatusList();
        _order = order;
        _process = process;
        _focus = focus;
        _accordion = new MXAccordion(focus, name) {
            @Override
            public void openAccordion(boolean sel) {
                if (sel) {
                    disableAnotherFolder();
                }
                super.openAccordion(sel);
            }
        };
        _folderName = name;
    }
    
    public boolean isSelected() {
        return _accordion.isAccordionOpened();
    }

    public void setSelected(boolean selected) {
        _accordion.openAccordion(selected);
    }
    
    @Override
    public int compareTo(MX36Folder o) {
        if (_order < o._order) {
            return -1;
        }
        if (_order > o._order) {
            return 1;
        }
        return 0;
    }

    public void sort(Comparator<MX36Status> comp) {
        _list._insertComparator = comp;
        _list.sort(comp);
    }

    public void insertSorted(MX36Status status) {
        status._folder = this;
        int pos = _list.insertSorted(status);
        if (pos < 0) {
            return;
        }
        MX36StatusPanel element = new MX36StatusPanel(_process, _accordion, status);
        _accordion.insertElement(pos, element);
        refill(status);
        setupMouse();
    }

    public void refill(MX36Status status) {
        int count = 0;
        for (MX36Status seek : _list) {
            if (seek == status) {
                _accordion.refill(count);
                return;
            }
            count++;
        }
        if (count == 0) {
            _accordion.refill(-1);
        }
    }

    public void setupMouse() {
        _focus.setupMouse(0, _accordion);
    }

    public void remove(MXAccordionElement elem) {
        if (_list.size() != _accordion.elementCount()) {
            new Throwable("before").printStackTrace();
        }
        
        MX36StatusPanel p = (MX36StatusPanel)elem;
        _accordion.removeElement(elem);
        _list.remove(p.getStatus());
        if (_list.size() != _accordion.elementCount()) {
            new Throwable("after").printStackTrace();
        }
    }
    
    public String toString() {
        return _folderName;
    }

    public void disableAnotherFolder() {
        List<MX36Folder> conflicts = _process._folders.findConflict(this);
        if (conflicts == null) {
            return;
        }
        for (MX36Folder seek : conflicts) {
            seek.setSelected(false);
        }
    }
}
