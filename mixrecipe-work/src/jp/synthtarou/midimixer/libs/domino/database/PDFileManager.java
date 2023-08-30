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
package jp.synthtarou.midimixer.libs.domino.database;

import java.util.ArrayList;
import jp.synthtarou.midimixer.libs.midi.programlist.GMFile;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class PDFileManager {
    static final PDFileManager _manager = new PDFileManager();

    ArrayList<PDFile> _listXML;

    protected PDFileManager() {
        _listXML = new ArrayList();
        _listXML.add(new GMFile());
    }
    
    public static PDFileManager getManager() {
        return _manager;
    }
    
    public void register(PDFile file) {
        for (PDFile xml : _listXML) {
            if (xml.getName().equals(file.getName())) {
                return;
            }
        }
        try {
            _listXML.add(file);
        }catch(Throwable e){
            e.printStackTrace();;
        }
    }
    
    public void unregist(String name) {
        for (PDFile xml : _listXML) {
            if (xml.getName().equals(name)) {
                _listXML.remove(xml);
                return;
            }
        }
    }
    
    public PDFile get(int x) {
        return _listXML.get(x);
    }

    public int size() {
        return _listXML.size();
    }
}
