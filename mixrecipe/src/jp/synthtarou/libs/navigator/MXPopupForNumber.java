/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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
package jp.synthtarou.libs.navigator;

import jp.synthtarou.libs.navigator.legacy.INavigator;
import jp.synthtarou.libs.navigator.legacy.NavigatorForNumber;
import javax.swing.JComponent;
import javax.swing.JTextField;
import jp.synthtarou.libs.MXRangedValue;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXPopupForNumber extends MXPopup {
    public MXPopupForNumber(JTextField target, MXRangedValue base) {
        super(target);
        _base = base;
    }

    public MXPopupForNumber(JTextField target, int range) {
        super(target);
        _base = new MXRangedValue(0, 0, range - 1);
    }

    public MXPopupForNumber(JTextField target, int min, int max) {
        super(target);
        _base = new MXRangedValue(0, min, max);
    }
    
    MXRangedValue _base;

    @Override
    public void simpleAskAsync(JComponent mouseBase) {
        int x = 0;
        try {
            x = Integer.parseInt(_target.getText());
        }catch(Exception e) {
        }
        NavigatorForNumber navi = new NavigatorForNumber(_base);
        if (navi.simpleAsk(_target)) {
            int result = navi.getReturnValue()._value;
            approvedValue(result);
        }
        hideMenuAndResponse();
    }

    public abstract void approvedValue(int selectedValue);


    public void setDialogTitle(String title) {
        _dialogTitle = title;
    }
    
    String _dialogTitle = INavigator.DEFAULT_TITLE;
    
}
