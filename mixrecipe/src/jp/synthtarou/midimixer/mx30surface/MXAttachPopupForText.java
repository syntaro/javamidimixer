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
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.navigator.INavigator;
import jp.synthtarou.midimixer.libs.navigator.NavigatorForText;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXAttachPopupForText extends MXAttachPopup {

    public MXAttachPopupForText(JTextField target) {
        super(target);
    }

    @Override
    public void showPopup() {
        NavigatorForText text = new NavigatorForText(_target.getText());
        MXUtil.showAsDialog(_target, text, "Your Choice ?");
        if (text.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
            approvedText(text.getReturnValue());
        } else {

        }
    }
    
    public abstract void approvedText(String text);
}
