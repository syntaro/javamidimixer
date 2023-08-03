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

/**
 *
 * @author Syntarou YOSHIDA
 */
public class DTextFolder {
    String _name;
    String _id;
    DTextMessageList _list = new DTextMessageList();
    
    public DTextFolder(String name) {
        _name = name;
    }
    
    public String toString() {
        return _name;
    }
    
    public void addFolder(DTextFolder folder) {
        _list.addFolder(folder);
    }
    
    public void addMessage(DTextMessage message) {
        _list.addMessage(message);
    }
}
