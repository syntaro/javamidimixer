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
package jp.synthtarou.midimixer.mx10input;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.midi.port.MXPreprocessPanel;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX10View extends javax.swing.JPanel {
    MX10MidiInListPanel _inPanel;
    MX10Process _process;
    
    /**
     * Creates new form MX10View
     */
    public MX10View(MX10Process process) {
        initComponents();
        _process = process;

        _inPanel = new MX10MidiInListPanel();
        jPanelInputSelect.add(_inPanel);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                splitAuto();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                splitAuto();
            }

        });
        
        MXPreprocessPanel manager = MXPreprocessPanel.getInstance();
        jPanelPreprocessor.removeAll();
        jPanelPreprocessor.setLayout(new BoxLayout(jPanelPreprocessor, BoxLayout.Y_AXIS));
        jPanelPreprocessor.add(manager);
    }
    
    public void splitAuto() {
        jSplitPane1.setDividerLocation(Math.max(jSplitPane1.getWidth() * 70 / 100, jSplitPane1.getWidth() - 400));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanelSkip = new javax.swing.JPanel();
        jCheckBoxUseSkip = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jPanelInputSelect = new javax.swing.JPanel();
        jPanelPreprocessor = new javax.swing.JPanel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jSplitPane1.setDividerLocation(400);

        jSplitPane2.setDividerLocation(240);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanelSkip.setBorder(javax.swing.BorderFactory.createTitledBorder("2.Input Patch & Filter"));
        jPanelSkip.setLayout(new java.awt.GridBagLayout());

        jCheckBoxUseSkip.setText("Use This Recipe");
        jCheckBoxUseSkip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseSkipActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelSkip.add(jCheckBoxUseSkip, gridBagConstraints);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelSkip.add(jPanel1, gridBagConstraints);

        jSplitPane2.setBottomComponent(jPanelSkip);

        jPanelInputSelect.setBorder(javax.swing.BorderFactory.createTitledBorder("1.Input Assign"));
        jPanelInputSelect.setLayout(new javax.swing.BoxLayout(jPanelInputSelect, javax.swing.BoxLayout.LINE_AXIS));
        jSplitPane2.setTopComponent(jPanelInputSelect);

        jSplitPane1.setLeftComponent(jSplitPane2);

        jPanelPreprocessor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanelPreprocessor.setLayout(new javax.swing.BoxLayout(jPanelPreprocessor, javax.swing.BoxLayout.LINE_AXIS));
        jSplitPane1.setRightComponent(jPanelPreprocessor);

        add(jSplitPane1);
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxUseSkipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseSkipActionPerformed
        _process.setUsingThisRecipe(jCheckBoxUseSkip.isSelected());
    }//GEN-LAST:event_jCheckBoxUseSkipActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxUseSkip;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelInputSelect;
    private javax.swing.JPanel jPanelPreprocessor;
    private javax.swing.JPanel jPanelSkip;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    // End of variables declaration//GEN-END:variables

    public void showViewData() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                showViewData();
            });
            return;
        }
        jPanel1.removeAll();;
        jPanel1.add(_process._patch.getReceiverView());
        jCheckBoxUseSkip.setSelected(_process._patch.isUsingThisRecipe());
    }
    
    public void tabActivated() {
        _inPanel.refreshList();
    }
}
