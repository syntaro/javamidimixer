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
package jp.synthtarou.midimixer.mx00playlist;

import java.io.File;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.common.MXQueue1;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class DXPlayList extends DefaultListModel<PlayListElement> {

    MXQueue1<PlayListElement> _queueForAdd = new MXQueue1<>();

    private void orderCommit() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    orderCommit();
                }
            });
            return;
        }
        while (true) {
            if (_queueForAdd.isEmpty()) {
                return;
            }
            PlayListElement e = _queueForAdd.pop();
            if (e != null) {
                DXPlayList.this.addElement(e);
            }
        }
    }

    public void addFile(PlayListElement file) {
        _queueForAdd.push(file);
        orderCommit();
    }

    public void addFile(String file) {
        _queueForAdd.push(new PlayListElement(file));
        orderCommit();
    }

    public void addAsFile(File file) {
        _queueForAdd.push(new PlayListElement(file));
        orderCommit();
    }
    
    public boolean isEmpty() {
        if (super.isEmpty()) {
            return _queueForAdd.isEmpty();
        }
        return false;
    }
}
