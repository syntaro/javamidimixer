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
package jp.synthtarou.mixtone.listmodel;

import java.io.File;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ComboModelMidiFile extends DefaultComboBoxModel<ComboModelMidiFile.Entry> {
    static public class Entry {
        Entry(File file) {
            _file = file;
        }
        
        File _file;
        
        public File getFile() {
            return _file;
        }
        
        public String toString() {
            if (_file == null) {
                return "   -Browse";
            }
            if (_file.exists() == false) {
                return "* " + _file.getName() + "[" + _file.getParent() + "]";
            }
            return _file.getName() + "[" + _file.getParent() + "]";
        }
    }
    
    public ComboModelMidiFile() {
        addFile((File)null);
        addFile("C:/midi/GeneralUser-GS/demo MIDIs/Dance.mid");
    }
    
    public int findFile(File file) {
        for (int i = 0; i < getSize(); ++ i) {
            Entry e = getElementAt(i);
            if (e._file == file) {
                return i;
            }
        }
        if (file == null) {
            return -1;
        }
        return findFile(file.getPath());
    }

    public int findFile(String path) {
        for (int i = 0; i < getSize(); ++ i) {
            Entry e = getElementAt(i);
            if (e._file == null) {
                continue;
            }
            String p1 = e._file.getPath();
            if (p1.equalsIgnoreCase(path)) {
                setSelectedItem(e);
                return i;
            }
        }
        return -1;
    } 
    
    public void selectFile(File file) {
        int x = findFile(file);
        if (x >= 0) {
            this.setSelectedItem(getElementAt(x));
        }
    }

    public void selectFile(String path) {
        int x = findFile(path);
        if (x >= 0) {
            this.setSelectedItem(getElementAt(x));
        }
    }
    
    public void addFile(String file) {
        if (findFile(file) >= 0) {
            return;
        }
        File f = new File(file);
        addFile(f);
    }
    public void addFile(File file) {
        if (findFile(file) >= 0) {
            return;
        }
        Entry e = new Entry(file);
        if (file == null) {
            addElement(e);
        }
        else {
            insertElementAt(e, getSize() - 1);
        }
    }
}
