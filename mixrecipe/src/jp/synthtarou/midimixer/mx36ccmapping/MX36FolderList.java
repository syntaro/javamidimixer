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
import java.util.TreeSet;
import jp.synthtarou.midimixer.mx36ccmapping.accordion.MXAccordion;
import jp.synthtarou.midimixer.mx36ccmapping.accordion.MXAccordionElement;
import jp.synthtarou.midimixer.mx36ccmapping.accordion.MXAccordionFocus;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36FolderList {

    final TreeSet<MX36Folder> _listFolder = new TreeSet<>();

    MXAccordionFocus _focus;
    MX36Folder _autodetectedFolder;
    MX36Folder _primalFolder;
    MX36Process _process;
 
    public MX36FolderList(MX36Process process) {
        _focus = new MXAccordionFocus();
        _process = process;
        _autodetectedFolder = newFolder(Integer.MAX_VALUE, "AutoDectected");
        _primalFolder = newFolder("Primal");
    }

    int _orderNext = 1;

    public synchronized void renumberFolder() {
        int newOrder = 1;
        for (MX36Folder f : _listFolder) {
            if (f._order == Integer.MAX_VALUE) {

            } else {
                f._order = newOrder++;
            }
        }
        _orderNext = newOrder;
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

    public synchronized MX36Folder getFolder(String name) {
        for (MX36Folder seek : _listFolder) {
            if (seek._folderName.equals(name)) {
                return seek;
            }
        }
        return null;
    }

    public synchronized MX36Folder getAutoDetectedFolder() {
        return _autodetectedFolder;
    }

    public synchronized MX36Folder getPrimalFolder() {
        return _primalFolder;
    }

    public synchronized MX36Folder newFolder(int index, String name) {
        for (MX36Folder seek : _listFolder) {
            if (seek._folderName.equals(name)) {
                return seek;
            }
        }
        MX36Folder folder = new MX36Folder(_process,_focus, index, name);
        _listFolder.add(folder);
        return folder;
    }

    public void sortWith(Comparator<MX36Status> comp) {
        for (MX36Folder folder : _listFolder) {
            folder.sort(comp);
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
                panel = accordion.elementAt(0);
            }catch(Exception e) {
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
}
