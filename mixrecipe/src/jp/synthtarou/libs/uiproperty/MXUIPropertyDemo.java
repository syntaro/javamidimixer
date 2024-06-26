/*
 * Copyright (C) 2024 Syntarou YOSHIDA
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
package jp.synthtarou.libs.uiproperty;

import java.util.logging.Level;
import javax.swing.SpinnerNumberModel;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXSafeThread;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderSingleClick;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXUIPropertyDemo extends javax.swing.JPanel {

    public static void main(String[] args) {
        MXUIPropertyDemo demo = new MXUIPropertyDemo();
        MXUtil.showAsDialog(null, demo, "demo");
        System.exit(0);
    }

    class ViewModel implements MXUIPropertyListener {

        MXUIProperty bindSpinner;
        MXUIProperty bindTextArea;
        MXUIProperty bindTextField;
        MXUIProperty bindLabel;
        MXUIProperty bindSlider;

        MXUIProperty[] bindTenkey;
        MXUIProperty bindBack;
        MXUIProperty bindClear;
        MXUIProperty bindTo100;

        boolean inConstruction = true;

        public ViewModel() {
            bindSpinner = new MXUIProperty(100);
            bindTextArea = new MXUIProperty("100");
            bindTextField = new MXUIProperty("100");
            bindSlider = new MXUIProperty(100);
            bindLabel = new MXUIProperty("100");

            bindSpinner.install(jSpinner1);
            bindTextArea.install(jTextArea1);
            bindTextField.install(jTextField1);
            bindSlider.install(jSlider1);
            bindLabel.install(jLabel1);

            bindTenkey = new MXUIProperty[10];
            bindTenkey[0] = new MXUIProperty(false);
            bindTenkey[1] = new MXUIProperty(false);
            bindTenkey[2] = new MXUIProperty(false);
            bindTenkey[3] = new MXUIProperty(false);
            bindTenkey[4] = new MXUIProperty(false);
            bindTenkey[5] = new MXUIProperty(false);
            bindTenkey[6] = new MXUIProperty(false);
            bindTenkey[7] = new MXUIProperty(false);
            bindTenkey[8] = new MXUIProperty(false);
            bindTenkey[9] = new MXUIProperty(false);

            bindTenkey[0].install(jButton0);
            bindTenkey[1].install(jButton1);
            bindTenkey[2].install(jButton2);
            bindTenkey[3].install(jButton3);
            bindTenkey[4].install(jButton4);
            bindTenkey[5].install(jButton5);
            bindTenkey[6].install(jButton6);
            bindTenkey[7].install(jButton7);
            bindTenkey[8].install(jButton8);
            bindTenkey[9].install(jButton9);

            bindBack = new MXUIProperty();
            bindClear = new MXUIProperty();
            bindTo100 = new MXUIProperty();

            bindBack.install(jButtonBack);
            bindClear.install(jButtonClear);
            bindTo100.install(jButtonto100);

            inConstruction = false;
        }

        public void uiProperityValueChanged(MXUIPropertyEvent evt) {
            if (inConstruction) {
                return;
            }
            if (evt.getMXContoller() == bindSlider || evt.getMXContoller() == bindSpinner) {
                if (evt.getMXContoller() != bindSlider) {
                    bindSlider.set(evt.getMXContoller().getAsInt());
                    bindLabel.set(evt.getMXContoller().getAsText());
                }
                if (evt.getMXContoller() != bindSpinner) {
                    bindSpinner.set(evt.getMXContoller().getAsInt());
                    bindLabel.set(evt.getMXContoller().getAsText());
                }
            } else {
                bindLabel.set(evt.getMXContoller().getAsText());
            }
        }
    }

    ViewModel _vm;

    /**
     * Creates new form MXControllerDemo
     */
    public MXUIPropertyDemo() {
        initComponents();
        new MXAttachSliderSingleClick(jSlider1);
        new MXAttachSliderLikeEclipse(jSlider1);
        int max = 1000000000;
        jSpinner1.setModel(new SpinnerNumberModel(150, 0, max, 1));
        jSlider1.setMinimum(0);
        jSlider1.setMaximum(max);
        _vm = new ViewModel();

        _vm.bindTenkey[1].doClickAction();
        _vm.bindBack.doClickAction();
        _vm.bindBack.doClickAction();
        _vm.bindBack.doClickAction();
        _vm.bindTenkey[6].doClickAction();
        _vm.bindTenkey[3].doClickAction();
        _vm.bindTenkey[9].doClickAction();
        new MXSafeThread("MXComponentControllerDemo", () -> {
            try {
                Thread.sleep(1000);
                for (int i = 0; i < 4; ++ i) {
                        _vm.bindBack.doClickAction();
                        Thread.sleep(50);
                }
                for (int i = 0; i < 7; ++i) {
                    for (int x = 0; x < 9; ++x) {
                        _vm.bindTenkey[(int) (Math.random() * 9) + 1].doClickAction();
                        Thread.sleep(100);
                    }
                    for (int x = 0; x < 9; ++x) {
                        _vm.bindBack.doClickAction();
                        Thread.sleep(50);
                    }
                }
                for (int i = 0; i < 3; ++i) {
                    Thread.sleep(333);
                    _vm.bindTenkey[7].doClickAction();
                }
            } catch (InterruptedException ex) {
                MXFileLogger.getLogger(MXUIPropertyDemo.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }).start();
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

        jButton0 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jSpinner1 = new javax.swing.JSpinner();
        jButtonto100 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jSlider1 = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jButtonClear = new javax.swing.JButton();
        jButtonBack = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jButton0.setText("0");
        jButton0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton0ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jButton0, gridBagConstraints);

        jButton1.setText("1");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jButton1, gridBagConstraints);

        jButton2.setText("2");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jButton2, gridBagConstraints);

        jButton3.setText("3");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jButton3, gridBagConstraints);

        jButton4.setText("4");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jButton4, gridBagConstraints);

        jButton5.setText("5");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jButton5, gridBagConstraints);

        jButton6.setText("6");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jButton6, gridBagConstraints);

        jButton7.setText("7");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jButton7, gridBagConstraints);

        jButton8.setText("8");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jButton8, gridBagConstraints);

        jButton9.setText("9");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jButton9, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jSpinner1, gridBagConstraints);

        jButtonto100.setText("0~100");
        jButtonto100.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonto100ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jButtonto100, gridBagConstraints);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jTextField1.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jTextField1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jSlider1, gridBagConstraints);

        jLabel1.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jLabel1, gridBagConstraints);

        jButtonClear.setText("C");
        jButtonClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jButtonClear, gridBagConstraints);

        jButtonBack.setText("<");
        jButtonBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBackActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButtonBack, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton0ActionPerformed
        _vm.bindSpinner.set(_vm.bindSpinner.getAsInt() * 10);
        _vm.bindSlider.set(_vm.bindSpinner.getAsInt());
    }//GEN-LAST:event_jButton0ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        _vm.bindSpinner.set(_vm.bindSpinner.getAsInt() * 10 + 1);
        _vm.bindSlider.set(_vm.bindSpinner.getAsInt());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        _vm.bindSpinner.set(_vm.bindSpinner.getAsInt() * 10 + 2);
        _vm.bindSlider.set(_vm.bindSpinner.getAsInt());
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        _vm.bindSpinner.set(_vm.bindSpinner.getAsInt() * 10 + 3);
        _vm.bindSlider.set(_vm.bindSpinner.getAsInt());
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        _vm.bindSpinner.set(_vm.bindSpinner.getAsInt() * 10 + 4);
        _vm.bindSlider.set(_vm.bindSpinner.getAsInt());
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        _vm.bindSpinner.set(_vm.bindSpinner.getAsInt() * 10 + 5);
        _vm.bindSlider.set(_vm.bindSpinner.getAsInt());

    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        _vm.bindSpinner.set(_vm.bindSpinner.getAsInt() * 10 + 6);
        _vm.bindSlider.set(_vm.bindSpinner.getAsInt());
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        _vm.bindSpinner.set(_vm.bindSpinner.getAsInt() * 10 + 7);
        _vm.bindSlider.set(_vm.bindSpinner.getAsInt());

    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        _vm.bindSpinner.set(_vm.bindSpinner.getAsInt() * 10 + 8);
        _vm.bindSlider.set(_vm.bindSpinner.getAsInt());
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        _vm.bindSpinner.set(_vm.bindSpinner.getAsInt() * 10 + 9);
        _vm.bindSlider.set(_vm.bindSpinner.getAsInt());
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButtonto100ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonto100ActionPerformed
        new MXSafeThread("Button", () -> {
            for (int i = 0; i < 100; ++i) {
                _vm.bindSpinner.set(i * 100 + 50);
                try {
                    Thread.sleep(20);
                } catch (Exception e) {

                }
            }
        }).start();;
    }//GEN-LAST:event_jButtonto100ActionPerformed

    private void jButtonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearActionPerformed
        _vm.bindSpinner.set(0);
    }//GEN-LAST:event_jButtonClearActionPerformed

    private void jButtonBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBackActionPerformed
        _vm.bindSpinner.set(_vm.bindSpinner.getAsInt() / 10);
    }//GEN-LAST:event_jButtonBackActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton0;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButtonBack;
    private javax.swing.JButton jButtonClear;
    private javax.swing.JButton jButtonto100;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
