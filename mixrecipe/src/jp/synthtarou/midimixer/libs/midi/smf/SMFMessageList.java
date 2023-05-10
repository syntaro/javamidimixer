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
package jp.synthtarou.midimixer.libs.midi.smf;

import java.util.ArrayList;
import java.util.TreeSet;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFMessageList {
    TreeSet<SMFMessage> _set = new TreeSet<SMFMessage>();
    
    public SMFMessageList() {
    }
    
    public void add(SMFMessage message) {
        _set.add(message);
    }

    public void  dump() {
        for (SMFMessage message : _set) {
            System.out.println(message);
        }
    }
    
    /*
    public SMFMessage firstTempo() {
        List<SMFMessage> list = listTempo();
        if (list != null && list.size() >= 1) {
            SMFMessage message = list.get(0);
            if (message._tick == 0) {
                return message;
            }
        }
        return null;
    }*/

    public ArrayList<SMFMessage> listAll() {
        ArrayList<SMFMessage> list = new ArrayList();
        list.addAll(_set);
        return list;
    }

    public int size() {
        return _set.size();
    }
}
