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
package jp.synthtarou.midimixer.libs.domino;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class DTextMessageList {
    ArrayList<DTextFolder> _listFolder = new ArrayList();
    ArrayList<DTextMessage> _listMessage = new ArrayList();
    
    public DTextMessageList() {
        
    }
    
    public DTextFolder addFolder(String name, String id) {
        DTextFolder folder = new DTextFolder(name);
        folder._id = id;
        _listFolder.add(folder);
        return folder;
    }
    
    public DTextMessage addDCC(String name) {
        return null;
    }

    public void dump() {
        dump("", System.out);
    }

    public void dump(String indent, PrintStream out) {
        if (_listFolder.size() > 0) {
            out.println(indent + "*Folder = " + _listFolder.size());
            out.println(indent + "[");
            for (DTextFolder folder : _listFolder) {
                if (folder._id != null) {
                    out.println(indent + "+Folder:" + folder._name + "(" + folder._id + ")");
                }else {
                    out.println(indent + "+Folder:" + folder._name);
                }
                folder._list.dump(indent + "..", out);
            }
            out.println(indent + "]");
        }
        if (_listMessage.size() > 0) {
            out.println(indent + "*ControlChange = " + _listMessage.size());
            out.println(indent + "[");
            for (DTextMessage ccm : _listMessage) {
                out.println(indent + "-" + ccm._name + "[" + ccm._dataText + "]");
            }
            out.println(indent + "]");
        }
    }

    public void addFolder(DTextFolder folder) {
        _listFolder.add(folder);
    }
    
    public void addMessage(DTextMessage message) {
        _listMessage.add(message);
    }
}
