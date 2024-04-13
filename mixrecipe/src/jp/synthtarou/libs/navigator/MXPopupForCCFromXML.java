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

import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTextField;
import jp.synthtarou.midimixer.ccxml.InformationForCCM;
import jp.synthtarou.midimixer.ccxml.ui.NavigatorForCCXMLCC;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.navigator.legacy.INavigator;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXPopupForCCFromXML extends  MXPopup {
    NavigatorForCCXMLCC _navi; 
    
    public MXPopupForCCFromXML(JTextField target) {
        super(target);
    }

    @Override
    public void simpleAskAsync(JComponent mouseBase) {
        _navi = new NavigatorForCCXMLCC();
        MXUtil.showAsDialog(mouseBase, _navi, _dialogTitle);
        if (_navi.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
            List<InformationForCCM> parser = _navi.getReturnValue();
            if (parser.isEmpty() == false) {
                approvedCC(parser.get(0));
            }
        }
        hideMenuAndResponse();
    }
    
    public abstract void approvedCC(InformationForCCM parser);

    public void setDialogTitle(String title) {
        _dialogTitle = title;
    }
    
    String _dialogTitle = INavigator.DEFAULT_TITLE;
}
