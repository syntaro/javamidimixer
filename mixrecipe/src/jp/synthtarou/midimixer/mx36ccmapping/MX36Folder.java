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

import java.util.List;
import jp.synthtarou.libs.accordionui.MXAccordion;
import jp.synthtarou.libs.accordionui.MXAccordionElement;
import jp.synthtarou.libs.accordionui.MXAccordionFocus;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.mx30surface.MGStatus;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36Folder implements Comparable<MX36Folder> {

    MXAccordionFocus _focus;
    MX36Process _process;

    final String _folderName;
    int _order;
    final MXAccordion _accordion;

    public MX36Folder(MX36Process process, MXAccordionFocus focus, int order, String name) {
        _order = order;
        _process = process;
        _focus = focus;
        _accordion = new MXAccordion(focus, name, false) {
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

    public boolean isAlreadyHave(MGStatus mgstatus) {
        for (int i = 0; i < _accordion.getElementCount(); ++i) {
            MXAccordionElement e = _accordion.getElementAt(i);
            if (e instanceof MX36StatusPanel) {
                MX36StatusPanel panel = (MX36StatusPanel) e;
                MX36Status seek = panel._status;
                if (seek.getSurfacePort() == mgstatus._port
                        && seek.getSurfaceUIType() == mgstatus._uiType
                        && seek.getSurfaceRow() == mgstatus._row
                        && seek.getSurfaceColumn() == mgstatus._column) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addCCItem(MX36Status status) {
        MXMain.invokeUI(() -> {
            status._folder = this;
            MX36StatusPanel element = new MX36StatusPanel(_process, _accordion, status);
            _accordion.insertElement(_accordion.getElementCount(), element);
            repaintStatus(status);
            setupMouse();
        });
    }

    public void repaintStatus(MX36Status status) {
        int count = 0;
        for (int i = 0; i < _accordion.getElementCount(); ++i) {
            MX36StatusPanel panel = (MX36StatusPanel) _accordion.getElementAt(i);
            if (panel._status == status) {
                _accordion.repaintAccordionElement(count);
            }
            count++;
        }
        if (count == 0) {
            _accordion.repaintAccordionElement(-1);
        }
    }

    public void setupMouse() {
        _focus.setupMouse(0, _accordion);
    }

    public void remove(MXAccordionElement elem) {
        MX36StatusPanel p = (MX36StatusPanel) elem;
        _accordion.removeElement(elem);
    }

    public void removeAll() {
        int count = 0;
        while (_accordion.getElementCount() != 0) {
            _accordion.removeElementAt(_accordion.getElementCount() - 1);
        }
    }

    public String toString() {
        return _folderName;
    }

    public void disableAnotherFolder() {
        List<MX36Folder> conflicts = _process._folderList.findConflict(this);
        if (conflicts == null) {
            return;
        }
        for (MX36Folder seek : conflicts) {
            seek.setSelected(false);
        }
    }

    public boolean sortElements() {
        return _accordion.sortElements(MX36StatusPanel._sortOrder);
    }
}
