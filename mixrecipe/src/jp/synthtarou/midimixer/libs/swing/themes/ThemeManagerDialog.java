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
package jp.synthtarou.midimixer.libs.swing.themes;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.wraplist.MXWrap;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.swing.CurvedSlider;
import jp.synthtarou.midimixer.libs.swing.variableui.VUITask;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ThemeManagerDialog extends javax.swing.JDialog {
    JFrame parentFrame;
    ThemeManager config;
    MXWrapList<String> _listModelFontName;
    MXWrapList<Integer> _listModelFontSyle;
    MXWrapList<Integer> _listModelFontSize;

    /**
     *
     * @param parent
     * @param modal
     */
    public ThemeManagerDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        parentFrame = parent;
        initComponents();
        this.setLocationRelativeTo(null);

        config = ThemeManager.getInstance();
        _listModelFontName = config.getFontNameModel();
        _listModelFontSyle = config.getFontStyleModel();
        _listModelFontSize = config.getFontSizeModel();

        jComboBoxFontName.setModel(_listModelFontName);
        jComboBoxFontSize.setModel(_listModelFontSize);
        jComboBoxFontStyle.setModel(_listModelFontSyle);
        
        MXWrapList<String> listLaf = config.getLookAndFeelModel();
        for (MXWrap<String> elem : listLaf) {
            String name = elem._name;
            JButton button = new JButton(name);
            button.addActionListener(new LookAndFeelThemeAction(name));
            jPanelSystemTheme.add(button);
        }
        
        pack();
        
        setPreferredSize(new Dimension(400, 400));
        CurvedSlider curve = new CurvedSlider(35);
        curve.setValue(MXRangedValue.new7bit(0));

        jLabelFill.setText("");
        jCheckBox1.setSelected(CurvedSlider.isMouseCircleIsCircle());
        jPanelCircle.add(curve);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButton7 = new javax.swing.JButton();
        jPanel1Colorful = new javax.swing.JPanel();
        jButtonAqua = new javax.swing.JButton();
        jButtonWyne = new javax.swing.JButton();
        jButtonForest = new javax.swing.JButton();
        jButtonStone = new javax.swing.JButton();
        jPanelSystemTheme = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jComboBoxFontName = new javax.swing.JComboBox();
        jComboBoxFontStyle = new javax.swing.JComboBox();
        jComboBoxFontSize = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jPanelCircle = new javax.swing.JPanel();
        jLabelFill = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jButton7.setText("OK");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jButton7, gridBagConstraints);

        jPanel1Colorful.setBorder(javax.swing.BorderFactory.createTitledBorder("カラフルテーマ"));
        jPanel1Colorful.setLayout(new java.awt.GridBagLayout());

        jButtonAqua.setText("Aqua");
        jButtonAqua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAquaActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1Colorful.add(jButtonAqua, gridBagConstraints);

        jButtonWyne.setText("Wyne");
        jButtonWyne.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWyneActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1Colorful.add(jButtonWyne, gridBagConstraints);

        jButtonForest.setText("Forest");
        jButtonForest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonForestActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1Colorful.add(jButtonForest, gridBagConstraints);

        jButtonStone.setText("Midnight");
        jButtonStone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStoneActionPerformed(evt);
            }
        });
        jPanel1Colorful.add(jButtonStone, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel1Colorful, gridBagConstraints);

        jPanelSystemTheme.setBorder(javax.swing.BorderFactory.createTitledBorder("システムテーマ"));
        jPanelSystemTheme.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanelSystemTheme, gridBagConstraints);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("フォント"));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jComboBoxFontName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFontNameActionPerformed(evt);
            }
        });
        jPanel3.add(jComboBoxFontName, new java.awt.GridBagConstraints());

        jComboBoxFontStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFontStyleActionPerformed(evt);
            }
        });
        jPanel3.add(jComboBoxFontStyle, new java.awt.GridBagConstraints());

        jComboBoxFontSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFontSizeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jComboBoxFontSize, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel3, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Another"));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jCheckBox1.setText("Citrcle is Circle (Mouse Drag Mode)");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jCheckBox1, gridBagConstraints);

        jLabel1.setText("Test");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jPanelCircle, gridBagConstraints);

        jLabelFill.setText("Fill");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabelFill, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAquaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAquaActionPerformed
        config.themeName = ThemeManager.additionalTheme[ThemeManager.THEME_SEA];
        updateLookAndFeel();
    }//GEN-LAST:event_jButtonAquaActionPerformed

    private void jButtonWyneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWyneActionPerformed
        config.themeName = ThemeManager.additionalTheme[ThemeManager.THEME_WINE];
        updateLookAndFeel();
    }//GEN-LAST:event_jButtonWyneActionPerformed

    private void jButtonForestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonForestActionPerformed
        config.themeName = ThemeManager.additionalTheme[ThemeManager.THEME_FOREST];
        updateLookAndFeel();
    }//GEN-LAST:event_jButtonForestActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButtonStoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStoneActionPerformed
        config.themeName = ThemeManager.additionalTheme[ThemeManager.THEME_STONE];
        updateLookAndFeel();
    }//GEN-LAST:event_jButtonStoneActionPerformed

    private void jComboBoxFontSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFontSizeActionPerformed
        config.fontSize = _listModelFontSize.readComboBox(jComboBoxFontSize);
        updateLookAndFeel();
    }//GEN-LAST:event_jComboBoxFontSizeActionPerformed

    private void jComboBoxFontNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFontNameActionPerformed
        config.fontName = _listModelFontName.readComboBox(jComboBoxFontName);
        updateLookAndFeel();
    }//GEN-LAST:event_jComboBoxFontNameActionPerformed

    private void jComboBoxFontStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFontStyleActionPerformed
        config.fontStyle = _listModelFontSyle.readComboBox(jComboBoxFontStyle);
        updateLookAndFeel();
    }//GEN-LAST:event_jComboBoxFontStyleActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        CurvedSlider.setMouseCircleIsCircle(jCheckBox1.isSelected());
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    public void updateLookAndFeel() {
        new VUITask() {
            public Object run() {
                config.setUITheme(config.themeName);
                config.setFont(config.fontName, config.fontStyle, config.fontSize);
                config.updateUITree();
                return NOTHING;
            }
        };
    }
    
    class LookAndFeelThemeAction implements ActionListener {
        String _theme;
        
        LookAndFeelThemeAction(String theme) {
            _theme = theme;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            config.themeName = _theme;
            updateLookAndFeel();
        }
        
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButtonAqua;
    private javax.swing.JButton jButtonForest;
    private javax.swing.JButton jButtonStone;
    private javax.swing.JButton jButtonWyne;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBoxFontName;
    private javax.swing.JComboBox jComboBoxFontSize;
    private javax.swing.JComboBox jComboBoxFontStyle;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelFill;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel1Colorful;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelCircle;
    private javax.swing.JPanel jPanelSystemTheme;
    // End of variables declaration//GEN-END:variables
}
