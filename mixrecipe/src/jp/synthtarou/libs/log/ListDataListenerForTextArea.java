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
package jp.synthtarou.libs.log;

import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import jp.synthtarou.midimixer.MXMain;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ListDataListenerForTextArea implements ListDataListener {
    JTextArea _target;
    
    public ListDataListenerForTextArea(JTextArea textArea) {
        _target = textArea;
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        updateTextArea((ListModel)e.getSource());
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        updateTextArea((ListModel)e.getSource());
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        updateTextArea((ListModel)e.getSource());
    }
    
    public void updateTextArea(ListModel model) {
        MXMain.invokeUI(() ->  {
            try {
                StringBuilder str = new StringBuilder();

                for (int i = 0; i < model.getSize(); ++ i) {
                    String seg = model.getElementAt(i).toString();
                    str.append(seg);
                    str.append("\n");
                }
                _target.setText(str.toString());
            }catch(Throwable e) {
                e.printStackTrace();
            }
        });
    }
}
