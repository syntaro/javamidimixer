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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import jp.synthtarou.libs.accordionui.MXAccordion;
import jp.synthtarou.libs.accordionui.MXAccordionElement;
import jp.synthtarou.libs.accordionui.MXAccordionFocus;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36FolderList {

    final TreeSet<MX36Folder> _listFolder = new TreeSet<>();

    MXAccordionFocus _focus;
    MX36Folder _autodetectFolder;
    MX36Folder _primalFolder;
    MX36Folder _trashedFolder;
    final MX36Process _process;
    
    public MX36FolderList(MX36Process process) {
        _focus = new MXAccordionFocus();
        _process = process;

        _autodetectFolder = newFolder(Integer.MAX_VALUE, "*NoSave(AutoDetected)");
        _primalFolder = newFolder("*Primal");
        _trashedFolder = newFolder(Integer.MAX_VALUE - 1, "*Trash");
    }
    
    int _orderNext = 1;

    public MX36Folder getFolderForNosave() {
        return _autodetectFolder;
    }

    public MX36Folder getPrimalFolder() {
        return _primalFolder;
    }

    public synchronized MX36Folder getFolder(String name) {
        for (MX36Folder seek : _listFolder) {
            if (seek._folderName.equals(name)) {
                return seek;
            }
        }
        return null;
    }

    public synchronized MX36Folder newFolder(String name) {
        for (MX36Folder seek : _listFolder) {
            if (seek._folderName.equals(name)) {
                return seek;
            }
        }
        MX36Folder folder = new MX36Folder(_process, _focus, _orderNext++, name);
        _listFolder.add(folder);
        return folder;
    }

    public synchronized MX36Folder newFolder(int index, String name) {
        for (MX36Folder seek : _listFolder) {
            if (seek._folderName.equals(name)) {
                return seek;
            }
        }
        MX36Folder folder = new MX36Folder(_process, _focus, index, name);
        _listFolder.add(folder);
        return folder;
    }

    public synchronized void deleteFolder(MX36Folder target) {
        _listFolder.remove(target);
        if (target._accordion != null) {
            _process._view.tabActivated();
        }
    }

    public void selectFirstAtm() {
        MXAccordion accordion = null;
        MXAccordionElement panel = null;

        //last folder
        for (MX36Folder folder : _listFolder) {
            accordion = folder._accordion;
        }

        //first child
        if (accordion != null) {

            try {
                panel = accordion.getElementAt(0);
            } catch (Exception e) {
            }
        }

        if (accordion != null && panel != null) {
            _focus.setSelected(0, accordion, panel);
        }

        for (MX36Folder folder : _listFolder) {
            if (folder._accordion != accordion) {
                folder._accordion.setColorFull(false);
            }
        }
    }

    public synchronized List<MX36Folder> findConflict(MX36Folder target) {
        List<MX36Folder> result = null;
        for (MX36Folder folder : _listFolder) {
            if (folder == target) {
                continue;
            }
            if (folder == _trashedFolder) {
                continue;
            }
            boolean hit = false;
            for (int i = 0; i < folder._accordion.getElementCount(); ++ i) {
                MX36StatusPanel panel1 = (MX36StatusPanel)folder._accordion.getElementAt(i);
                MX36Status status1 = panel1._status;
                for (int j = 0; j < target._accordion.getElementCount(); ++ j) {
                    MX36StatusPanel panel2 = (MX36StatusPanel)target._accordion.getElementAt(j);
                    MX36Status status2 = panel2._status;
                    if (status1.getSurfacePort() == status2.getSurfacePort()
                            && status1.getSurfaceRow() == status2.getSurfaceRow()
                            && status1.getSurfaceColumn() == status2.getSurfaceColumn()) {
                        hit = true;
                        break;
                    }
                }
                if (hit) {
                    break;
                }
            }
            if (hit) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(folder);
            }
        }
        return result;
    }
}
