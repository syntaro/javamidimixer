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
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class FileList extends ArrayList<File>{
    public FileList() {
        
    }
    
    public FileList(File[] list) {
        for (File file : list) {
            add(file);
        }
    }

    public FileList(Collection<File> list) {
        for (File file : list) {
            add(file);
        }
    }
}
