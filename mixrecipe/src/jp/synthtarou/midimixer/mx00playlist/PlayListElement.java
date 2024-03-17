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
package jp.synthtarou.midimixer.mx00playlist;

import java.io.File;
import jp.synthtarou.midimixer.libs.settings.MXSettingUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class PlayListElement  {
    static int _seq = 1;
     
    public final File _file;
    public final int _id;

    public PlayListElement(String file) {
        this(new File(file));
    }
     
    public PlayListElement(File file) {
        String fileName = file.getPath();
        if (fileName.indexOf('/') < 0 && fileName.indexOf('\\') < 0) {
            file = new File(MXSettingUtil.getAppBaseDirectory(), fileName);
        }
        _file = file;
        _id = _seq ++;
    }

     public String toString() {
         return _file.getName();
     }
 }

 