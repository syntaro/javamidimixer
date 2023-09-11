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
package jp.synthtarou.midimixer.mx35cceditor.ccxml.navigator;

import javax.swing.JPanel;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ParamsOfNavigator<T> {
    public ParamsOfNavigator() {
        _mode = MODE_VIEWER;
        _returnCode = 0;
        _value = null;

        _valueRemovable = false;
    }
    
    public static int MODE_VIEWER = 1;
    public static int MODE_CHOOSER = 2;
    public static int MODE_EDITOR = 3;
    
    public int _mode;
    
    public boolean _valueRemovable;
    
    private static int RETURN_APPROVED = 4;
    private static int RETURN_CANCEL = 5;
    private static int RETURN_REMOVED = 6;
    
    private int _returnCode;
    
    private T _value;

    public boolean isCanceled() {
        return _returnCode == RETURN_CANCEL;
    }
    
    public boolean isRemoved() {
        return _returnCode == RETURN_REMOVED;
    }
    
    public boolean isApproved() {
         return _returnCode == RETURN_APPROVED;
    }
    
    public T getApprovedValue() {
        if (isApproved()) {
            return _value;
        }
        return null;
    }
    
    public void closeWithApprove(INavigator panel, T value) {
        _returnCode = RETURN_APPROVED;
        _value = value;
        NavigatorUtil.closeOwnerWindow(panel);
    }

    public void closeWithCancel(INavigator panel) {
        _returnCode = RETURN_CANCEL;
        _value = null;
        NavigatorUtil.closeOwnerWindow(panel);
    }

    public void closeWithRemoved(INavigator panel) {
        if (_valueRemovable == false) {
            throw new IllegalStateException("can't remove by navigator (Program problem)");
        }
        _returnCode = RETURN_REMOVED;
        _value = null;
        NavigatorUtil.closeOwnerWindow(panel);
    }
}
