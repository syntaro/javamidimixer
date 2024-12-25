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

import java.util.ArrayList;
import jp.synthtarou.libs.navigator.legacy.INavigator;
import jp.synthtarou.libs.navigator.legacy.NavigatorForText;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 *
 * @author Syntarou YOSHIDA
 */
public abstract class MXPopupForText extends MXPopup {

    public MXPopupForText(JTextField target) {
        super(target);
    }

    @Override
    public void simpleAskAsync(JComponent mouseBase) {
        String previous = "";
        if (_target != null) {
            previous = _target.getText();
        }
        NavigatorForText text = new NavigatorForText(previous);
        if (_listCandidate != null) {
            for (CandidateEntry e : _listCandidate) {
                text.addCandidate(e._text, e._memo);
            }
        }
        if (text.simpleAsk(mouseBase)) {
            approvedText(text.getReturnValue());
        }
        hideMenuAndResponse();
    }
    
    public void setDialogTitle(String title) {
        _dialogTitle = title;
    }
    
    class CandidateEntry {
        String _text;
        String _memo;

        public CandidateEntry(String text, String memo) {
           _text = text;
           _memo = memo;
        }
    }
    
    public void addCandidate(String text, String meaning) {
        if (_listCandidate == null) {
            _listCandidate = new ArrayList<>();
        }
        _listCandidate.add(new CandidateEntry(text, meaning));
    }

    ArrayList<CandidateEntry> _listCandidate = null;
    String _dialogTitle = INavigator.DEFAULT_TITLE;
    
    public abstract void approvedText(String text);
}
