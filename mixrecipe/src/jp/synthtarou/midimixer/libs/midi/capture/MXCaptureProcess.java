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
package jp.synthtarou.midimixer.libs.midi.capture;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeCellRenderer;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.swing.UITask;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXCaptureProcess extends MXReceiver{
    public MXCaptureProcess() {
    }

    @Override
    public String getReceiverName() {
        return "Capture";
    }

    @Override
    public JPanel getReceiverView() {
        return null;
    }

    CounterTable _table = new CounterTable();
    JTree _jTree = null;
    
    @Override
    public void processMXMessage(MXMessage message) {
        int channel = message.getChannel();
        int gate = message.getGate()._value;
        int value = message.getValue()._value;
        
        Counter count = _table.captureIt(message);
    }
    
    public void setRederer(JTree tree) {
        _jTree = tree;
        new UITask() {
            @Override
            public Object run() {
                TreeCellRenderer render = new CounterTreeModel.CounterRender(tree);
                _jTree.setCellRenderer(render);
                _jTree.setModel(_table._model);
                return NOTHING;
            }
        };
    }
    
    public void clearTree() {
        new UITask() {
            @Override
            public Object run() {
                _table = new CounterTable();
               _jTree.setModel(_table._model);
               return NOTHING;
            }
        };
    }
}
