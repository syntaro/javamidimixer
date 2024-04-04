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
package jp.synthtarou.midimixer.libs.swing;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.inifile.StringPath;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class JTableWithFooter extends JTableWithColumnHeader {
    boolean _hideHeader = true;
    boolean _showFooter = true;

    JTableWithColumnHeader _footerTable;

    /**
     *
     * @param gridBagPanel
     */
    public JTableWithFooter(JPanel gridBagPanel) {
        super();
        
        java.awt.GridBagConstraints gridBagConstraints;
        
        if (gridBagPanel.getComponentCount() > 0) {
            Exception ex = new IllegalArgumentException("Need plaing JPanel for MJTable (but cleanup and continue)");
            MXFileLogger.getLogger(JTableWithFooter.class).log(Level.WARNING, ex.getMessage(), ex);
            
            for(Component c : gridBagPanel.getComponents()) {
                gridBagPanel.remove(c);
            }
        }
        gridBagPanel.setLayout(new GridBagLayout());
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagPanel.add(this, gridBagConstraints);

        _footerTable = new JTableWithColumnHeader(getModel(), getColumnModel());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagPanel.add(_footerTable.getTableHeader(), gridBagConstraints);
        
        _footerTable.getTableHeader().setDefaultRenderer(new FoterRenderer(_footerTable.getTableHeader().getDefaultRenderer()));
        _footerTable.getTableHeader().setReorderingAllowed(false);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        
        gridBagPanel.add(new JPanel(), gridBagConstraints);  
    }
 
    public TableCellRenderer getCellRenderer(int row, int column){
        TableCellRenderer root = super.getCellRenderer(row, column);
        Component render = (Component)root;
        render.setEnabled(isEnabled());
        return (TableCellRenderer)render;
    }
    
    public void setEnabled(boolean e) {
        super.setEnabled(e);
        _footerTable.setEnabled(e);
        _footerTable.getTableHeader().setEnabled(e);
    }

    public class FoterRenderer implements  TableCellRenderer{
        TableCellRenderer _base;
        
        public FoterRenderer(TableCellRenderer base) {
            _base = base;
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel)_base.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setText(String.valueOf(value));
            label.setEnabled(_footerTable.getTableHeader().isEnabled());
            return label;
        }
    }
}
