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
package jp.synthtarou.midimixer.mx35cceditor.prompt;

import java.awt.Dimension;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jp.synthtarou.midimixer.libs.domino.CCXMLNode;
import jp.synthtarou.midimixer.libs.domino.CCRuleForAttribute;
import jp.synthtarou.midimixer.libs.domino.CCRuleForTag;
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.common.MXWrapList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class PromptForAttribute extends javax.swing.JPanel implements IInputPromptPanel<MXWrap<String>> {

    CCXMLNode _node;
    MXWrap<String> _result;
    String _targetName;

    /**
     * Creates new form CCV13TextPrompt
     */
    public PromptForAttribute(CCXMLNode target, String attrName, String attrValue) {
        initComponents();
        _node = target;
        _targetName = attrName;
        _result = new MXWrap<String>(attrName, attrValue);
        buttonGroup1.add(jRadioButtonNameSelect);
        buttonGroup1.add(jRadioButtonNameCustom);
        buttonGroup2.add(jRadioButtonValueSelect);
        buttonGroup2.add(jRadioButtonValueCustom);

        setAttrNameModel();
        setAttrName(attrName);
        setAttrValueModel(attrName);
        setAttrValue(attrValue);
        disableUnused();
    }

    public void disableUnused() {
        jListName.setEnabled(jRadioButtonNameSelect.isSelected());
        jTextFieldName.setEnabled(jRadioButtonNameCustom.isSelected());
        jListValue.setEnabled(jRadioButtonValueSelect.isSelected());
        jTextFieldValue.setEnabled(jRadioButtonValueCustom.isSelected());
    }

    MXWrapList<String> _attrNameModel;

    protected void setAttrNameModel() {
        _attrNameModel = new MXWrapList<>();

        CCRuleForTag tag = _node.getTagRule();
        if (tag != null) {
            List<CCRuleForAttribute> attrRules = tag.listAttributes();
            for (CCRuleForAttribute attr : attrRules) {
                _attrNameModel.addNameAndValue(attr.getName(), attr.getName());
            }
        }

        jListName.setModel(_attrNameModel);
    }

    protected void setAttrName(String name) {
        int x = _attrNameModel.indexOfNameShrink(name);
        if (x >= 0) {
            jRadioButtonNameSelect.setSelected(true);
            jRadioButtonNameCustom.setSelected(false);
            jListName.setSelectedIndex(x);
        } else {
            jRadioButtonNameSelect.setSelected(false);
            jRadioButtonNameCustom.setSelected(true);
            jTextFieldName.setText(name);
        }
    }

    protected String getAttrName() {
        if (jRadioButtonNameSelect.isSelected()) {
            int x = jListName.getSelectedIndex();
            if (x >= 0) {
                return _attrNameModel.nameOfIndex(x);
            }
            return null;
        }
        return jTextFieldName.getText();
    }

    MXWrapList<String> _attrValueModel;

    protected void setAttrValueModel(String attrName) {
        _attrValueModel = new MXWrapList<>();

        CCRuleForTag tag = _node.getTagRule();
        if (tag != null) {
            CCRuleForAttribute attr = tag.getAttribute(attrName);
            if (attr != null) {
                CCValueRule values = attr.getValueRule();
                List<String> list = values.refillForUI();
                if (list != null) {
                    for (String value : list) {
                        _attrValueModel.addNameAndValue(value, value);
                    }
                } else {

                }
            }
        }

        jListValue.setModel(_attrValueModel);
    }

    protected void setAttrValue(String value) {
        int x = _attrValueModel.indexOfNameShrink(value);
        if (x >= 0) {
            jRadioButtonValueSelect.setSelected(true);
            jRadioButtonValueCustom.setSelected(false);
            jListValue.setSelectedIndex(x);
        } else {
            jRadioButtonValueSelect.setSelected(false);
            jRadioButtonValueCustom.setSelected(true);
            jTextFieldValue.setText(value);
        }
    }

    protected String getAttrValue() {
        if (jRadioButtonValueSelect.isSelected()) {
            int x = jListValue.getSelectedIndex();
            if (x >= 0) {
                return _attrValueModel.nameOfIndex(x);
            }
            return null;
        }
        return jTextFieldValue.getText();
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jButtonCancel = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jRadioButtonNameSelect = new javax.swing.JRadioButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListName = new javax.swing.JList<>();
        jRadioButtonNameCustom = new javax.swing.JRadioButton();
        jTextFieldName = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jRadioButtonValueSelect = new javax.swing.JRadioButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListValue = new javax.swing.JList<>();
        jRadioButtonValueCustom = new javax.swing.JRadioButton();
        jTextFieldValue = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jButtonCancel, gridBagConstraints);

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButtonOK, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Name"));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jRadioButtonNameSelect.setText("Select");
        jRadioButtonNameSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonNameSelectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jRadioButtonNameSelect, gridBagConstraints);

        jListName.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListNameValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jListName);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane2, gridBagConstraints);

        jRadioButtonNameCustom.setText("Custom");
        jRadioButtonNameCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonNameCustomActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel1.add(jRadioButtonNameCustom, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jTextFieldName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Value"));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jRadioButtonValueSelect.setText("Select");
        jRadioButtonValueSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonValueSelectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel2.add(jRadioButtonValueSelect, gridBagConstraints);

        jScrollPane3.setViewportView(jListValue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jScrollPane3, gridBagConstraints);

        jRadioButtonValueCustom.setText("Custom");
        jRadioButtonValueCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonValueCustomActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel2.add(jRadioButtonValueCustom, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jTextFieldValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        _result = new MXWrap<String>(getAttrName(), getAttrValue());
        if (validatePromptResult()) {
            CCPromptUtil.closeAnyway(this);
        }
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        _result = null;
        CCPromptUtil.closeAnyway(this);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jRadioButtonNameSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonNameSelectActionPerformed
        disableUnused();;
    }//GEN-LAST:event_jRadioButtonNameSelectActionPerformed

    private void jRadioButtonNameCustomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonNameCustomActionPerformed
        disableUnused();;
    }//GEN-LAST:event_jRadioButtonNameCustomActionPerformed

    private void jRadioButtonValueSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonValueSelectActionPerformed
        disableUnused();;
    }//GEN-LAST:event_jRadioButtonValueSelectActionPerformed

    private void jRadioButtonValueCustomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonValueCustomActionPerformed
        disableUnused();;
    }//GEN-LAST:event_jRadioButtonValueCustomActionPerformed

    private void jListNameValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListNameValueChanged
        String name = getAttrName();
        setAttrValueModel(name);
    }//GEN-LAST:event_jListNameValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JList<String> jListName;
    private javax.swing.JList<String> jListValue;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButtonNameCustom;
    private javax.swing.JRadioButton jRadioButtonNameSelect;
    private javax.swing.JRadioButton jRadioButtonValueCustom;
    private javax.swing.JRadioButton jRadioButtonValueSelect;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextFieldName;
    private javax.swing.JTextField jTextFieldValue;
    // End of variables declaration//GEN-END:variables

    @Override
    public JPanel getAsPanel() {
        return this;
    }

    @Override
    public String getPanelTitle() {
        return "Attribute";
    }

    @Override
    public Dimension getPanelSize() {
        return new Dimension(500, 500);
    }

    @Override
    public MXWrap<String> getPromptResult() {
        return _result;
    }

    @Override
    public boolean validatePromptResult() {
        String name = getAttrName();
        String value = getAttrValue();
        
        if (_targetName != null && name != null && _targetName.equalsIgnoreCase(name) == false) {
            CCRuleForTag tagRule = _node.getTagRule();
            int already = _node._listAttributes.indexOfName(getAttrName());
            if (already >= 0) {
                JOptionPane.showMessageDialog(this, "Already having attriute named [" + name + "]");
                return false;
            }
        }
        else if (_targetName != null && name != null) {
            
        }
        else if (name == null) {
            
        }
        else if (_targetName == null) {
            CCRuleForTag tagRule = _node.getTagRule();
            int already = _node._listAttributes.indexOfName(getAttrName());
            if (already >= 0) {
                JOptionPane.showMessageDialog(this, "Already having attriute named [" + name + "]");
                return false;
            }
        }
        return true;
    }
}
