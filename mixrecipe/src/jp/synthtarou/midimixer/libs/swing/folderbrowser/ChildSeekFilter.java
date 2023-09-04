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
import java.util.HashSet;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ChildSeekFilter implements FileFilter {

    HashSet<String> _listSuceed = new HashSet();
    HashSet<String> _listFailed = new HashSet();
    MXSwingFolderBrowser _owner;
    int _step;

    public ChildSeekFilter(MXSwingFolderBrowser ownwer) {
        _owner = ownwer;
    }

    static SingleThreadGroup _singleGroup = new SingleThreadGroup();
    static boolean _pause;

    public boolean accept(File file) {
        boolean isNewOrder = _singleGroup.addToTop(Thread.currentThread());

        try {
            if (_cancelFlag) {
                return false; //any ok called should check cancelflag before result judge
            }
            if (_listFailed.contains(file.toString())) {
                return false;
            }
            if (_listSuceed.contains(file.toString())) {
                return true;
            }
            if ((++_step % 100) == 0) {
                _owner.progress("Scan Go " + _singleGroup.countThread() + " tasks, count " + _step + ")" + file.toString());
            }
            try {
                boolean hit = false;
                File[] children = file.listFiles();
                if (children == null) {
                    return false;
                }
                _singleGroup.waitTillMyTurn();
                if (_pause) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                }
                for (File child : children) {
                    try {
                        if (child.isFile()) {
                            if (_owner.isVisibleFile(child)) {
                                _listSuceed.add(child.toString());
                                hit = true;
                            }
                        } else {
                            if (accept(child)) {
                                _listSuceed.add(child.toString());
                                hit = true;
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                if (hit) {
                    _listSuceed.add(file.toString());
                } else {
                    _listFailed.add(file.toString());
                }
                return hit;
            } catch (Throwable e) {
                e.printStackTrace();
            }
            _listFailed.add(file.toString());
        } finally {
            if (isNewOrder) {
                _singleGroup.removeFromList(Thread.currentThread());
            }
        }

        return false;
    }

    boolean _cancelFlag = false;

    public void noticeResetCancel() {
        _cancelFlag = false;
    }

    public void noticeCancelScan() {
        _cancelFlag = true;
        _owner.progress("Scan Canceled");
    }

    public boolean isCanceled() {
        return _cancelFlag;
    }

    public void pause(boolean pause) {
        _pause = pause;
    }
}
