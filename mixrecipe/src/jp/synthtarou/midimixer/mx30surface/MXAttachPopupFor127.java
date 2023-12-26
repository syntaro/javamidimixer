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
package jp.synthtarou.midimixer.mx30surface;

import javax.swing.JTextField;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.navigator.INavigator;
import jp.synthtarou.midimixer.libs.navigator.NavigatorForNumber;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXAttachPopupFor127 extends MXAttachPopup {
    public MXAttachPopupFor127(JTextField target) {
        super(target);
    }

    @Override
    public void showPopup() {
        int x = 0;
        try {
            x = Integer.parseInt(_target.getText());
        }catch(Exception e) {
        }
        NavigatorForNumber navi = new NavigatorForNumber(MXRangedValue.new7bit(x));
        MXUtil.showAsDialog(_target, navi, INavigator.DEFAULT_TITLE);
        if (navi.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
            int result = navi.getReturnValue()._var;
            approvedValue(result);
        }
    }

    public abstract void approvedValue(int selectedValue);
}
