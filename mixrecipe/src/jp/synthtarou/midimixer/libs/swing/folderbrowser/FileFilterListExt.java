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
package jp.synthtarou.midimixer.libs.swing.folderbrowser;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class FileFilterListExt implements FileFilter {

    /**
     *
     */
    public FileFilterListExt() {
    }

    /**
     *
     * @param ext
     */
    public FileFilterListExt(String ext) {
        addExtension(ext);
    }

    /**
     *
     * @param list
     */
    public FileFilterListExt(String[] list) {
        for (String ext : list) {
            addExtension(ext);
        }
    }
    
    public void addExtension(String ext) {
        ext = ext.toLowerCase();
        if (ext.startsWith(".") == false) {
            ext = "." + ext;
        }
        _listExtension.add(ext);
    }
    
    ArrayList<String> _listExtension = new ArrayList<>();
    public boolean _useAllDirectory = true;
    public boolean _stopAllFile = false;
    
    @Override
    public boolean accept(File pathname) {
        String path = pathname.getName();

        if (pathname.isDirectory()) {
            if (_useAllDirectory) {
                return true;
            }
        }
        else {
            if (_stopAllFile) {
                return false;
            }
        }
        for (String ext : _listExtension) {
            if (path.toLowerCase().endsWith(ext)) {
                if (pathname.isDirectory()) {
                    System.out.println("Dirctory return true " + ext);
                }
                return true;
            }
        }
        return false;
    }
}
