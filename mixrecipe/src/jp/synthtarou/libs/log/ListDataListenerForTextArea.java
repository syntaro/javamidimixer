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
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

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
        System.out.println("******"  + model);

        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateTextArea(model);
                }
            });
            System.out.println("***1");
            return;              
        }
        try {
            System.out.println("***2");

            StringBuffer str = new StringBuffer();

            for (int i = 0; i < model.getSize(); ++ i) {
                String seg = model.getElementAt(i).toString();
                System.out.println(seg);
                str.append(seg);
                str.append("\n");
            }
            System.out.println("settext target = "  + _target);
            _target.setText(str.toString());
            System.out.println("gettext = "  + _target.getText());
        }catch(Throwable e) {
            e.printStackTrace();
        }
    }
}
