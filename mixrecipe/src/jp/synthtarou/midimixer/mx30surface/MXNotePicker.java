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
package jp.synthtarou.midimixer.mx30surface;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapList;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.wraplist.MXWrapListFactory;
import jp.synthtarou.midimixer.libs.navigator.INavigator;
import jp.synthtarou.midimixer.libs.swing.MXSwingPiano;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXNotePicker extends javax.swing.JPanel implements  INavigator<int[]> {
    public static void main(String[] args) {
        MXUtil.showAsDialog(null, new MXNotePicker(true), "Note Picker");
        System.exit(0);
    }

    MXSwingPiano _piano;
    MXWrapList<Integer> _watchPort = MXWrapListFactory.listupPort("Omni");
    MXWrapList<Integer> _watchChannel = MXWrapListFactory.listupChannel(null);
    private boolean _closeOK = false;
    private int[] _retNote = null;
    
    int _valuePitch = -1;
    int _valueModulation = -1;

    /**
     * Creates new form MXNotePicker
     */
    public MXNotePicker(boolean multiSelect) {
        initComponents();

        _piano = new MXSwingPiano();

        _piano.setAllowMultiSelect(multiSelect);
        _piano.setNoteRange(0, 11);
        _piano.setMinimumSize(new Dimension(9 * 200, 1));
        _piano.setPreferredSize(new Dimension(9 * 200, 150));

        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setViewportView(_piano);

        _piano.setHandler(new MXSwingPiano.Handler() {
            @Override
            public void noteOn(int note) {
            }

            @Override
            public void noteOff(int note) {
            }

            @Override
            public void selectionChanged() {
                noteSelectionChanged();
            }
        });

        setPreferredSize(new Dimension(800, 200));
        scrollToCenter();
    }
    
    public void scrollToCenter() {
        Dimension scrollSize = jScrollPane1.getSize();
        Dimension pianoSize = _piano.getSize();
        Rectangle rect = new Rectangle((int)(pianoSize.getWidth()-scrollSize.getWidth()) / 2, 0, (int)scrollSize.getWidth(), 50);
        jScrollPane1.getViewport().scrollRectToVisible(rect);
    }

    public void noteSelectionChanged() {
        _retNote = _piano.listMultiSelected();
        jLabelNoteList.setText(MXMidi.noteListToText(_retNote));
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabelNoteList = new javax.swing.JLabel();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setViewportView(jPanel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setText("Selected Note:");
        jPanel2.add(jLabel1);

        jLabelNoteList.setText("C1");
        jPanel2.add(jLabelNoteList);

        add(jPanel2, new java.awt.GridBagConstraints());

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jButtonOK, gridBagConstraints);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        add(jButtonCancel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        _closeOK = true;
        MXUtil.getOwnerWindow(this).setVisible(false);
    }//GEN-LAST:event_jButtonOKActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelNoteList;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    
    public void setSelectedNoteList(int[] note) {
        if (note != null) {
            for (int x : note) {
                _piano.selectNote(x,  true);
            }
        }
    }

    @Override
    public int getNavigatorType() {
        return INavigator.TYPE_SELECTOR;
    }

    @Override
    public int getReturnStatus() {
        if (_closeOK) {
            return INavigator.RETURN_STATUS_APPROVED;
        }
        return INavigator.RETURN_STATUS_NOTSET;
    }

    @Override
    public int[] getReturnValue() {
        if (_closeOK) {
            return _retNote;
        }
        return null;
    }

    @Override
    public boolean isNavigatorRemovable() {
        return false;
    }

    @Override
    public boolean validateWithNavigator(int[] result) {
        return false;
    }

    @Override
    public JPanel getNavigatorPanel() {
        return this;
    }
}
