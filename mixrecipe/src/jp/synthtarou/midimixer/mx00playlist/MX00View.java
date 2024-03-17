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
package jp.synthtarou.midimixer.mx00playlist;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.swing.MXFileChooser;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIInForPlayer;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.libs.midi.smf.SMFCallback;
import jp.synthtarou.midimixer.libs.midi.smf.SMFMessage;
import jp.synthtarou.midimixer.libs.navigator.legacy.INavigator;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileFilterListExt;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderSingleClick;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileList;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.MXSwingFolderBrowser;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX00View extends javax.swing.JPanel implements SMFCallback {

    static int DRUM_CH = 10 - 1;

    MX00PianoPanel[] _listKeyboard;
    MX00DrumPadPanel _drumPanel = null;
    MXMIDIInForPlayer _player;
    PlayListElement _playingFile;
    PlayListElement _selectedItem = null;

    public MX00View() {
        _listKeyboard = new MX00PianoPanel[16];

        initComponents();

        jSliderSongPosition.setMinimum(0);
        jSliderSongPosition.setMaximum(10);
        jSliderSongPosition.setValue(0);
        new MXAttachSliderLikeEclipse(jSliderSongPosition);
        new MXAttachSliderSingleClick(jSliderSongPosition);
        jLabelSongPosition.setText("0");
        _player = MXMIDIIn.INTERNAL_PLAYER;
        jSplitPane1.setDividerLocation(350);
        jSplitPane2.setDividerLocation(300);
    }

    public void setDXStructure(MX00Structure structure) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setDXStructure(structure);
                }
            });
            return;
        }
        setDXQueueFileList(structure._playListModel);
        setDXChain(structure._playAsChained);
        setDXRepeat(structure._playAsRepeated);
    }

    DXPlayList _dxPlayList;

    public void setDXQueueFileList(DXPlayList listFiles) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setDXQueueFileList(listFiles);
                }
            });
            return;
        }
        _dxPlayList = listFiles;
        jListPlayList.setModel(_dxPlayList);
    }

    public void setDXChain(boolean chain) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setDXChain(chain);
                }
            });
            return;
        }
        jCheckBoxChain.setSelected(chain);
    }

    public void setDXRepeat(boolean repeat) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setDXRepeat(repeat);
                }
            });
            return;
        }
        jCheckBoxRepeat.setSelected(repeat);
    }

    public void setDXCurrentSongName(String songName) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setDXCurrentSongName(songName);
                }
            });
            return;
        }
        jTextFieldCurrentSongName.setText(songName);
    }

    public MX00Structure getDXStructure() {
        MX00Structure structure = new MX00Structure();
        structure._playListModel = _dxPlayList;
        structure._playAsRepeated = jCheckBoxRepeat.isSelected();
        structure._playAsChained = jCheckBoxChain.isSelected();
        return structure;
    }

    public boolean getDXChained() {
        return jCheckBoxChain.isSelected();
    }

    public boolean getDXRepeat() {
        return jCheckBoxRepeat.isSelected();
    }

    public String getDXCurrentSongName() {
        return jTextFieldCurrentSongName.getText();
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
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPanePlayList = new javax.swing.JScrollPane();
        jListPlayList = new javax.swing.JList<>();
        jPanel3 = new javax.swing.JPanel();
        jButtonUp = new javax.swing.JButton();
        jButtonAdd = new javax.swing.JButton();
        jButtonDown = new javax.swing.JButton();
        jButtonDeque = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanelSelInfo1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jListFileInfo = new javax.swing.JList<>();
        jPanel2 = new javax.swing.JPanel();
        jButtonPlay = new javax.swing.JButton();
        jCheckBoxChain = new javax.swing.JCheckBox();
        jCheckBoxRepeat = new javax.swing.JCheckBox();
        jPanelRight = new javax.swing.JPanel();
        jSliderSongPosition = new javax.swing.JSlider();
        jTextFieldCurrentSongName = new javax.swing.JTextField();
        ｊButtonPause = new javax.swing.JButton();
        jLabelSongPosition = new javax.swing.JLabel();
        jTabbedPanePiano = new javax.swing.JTabbedPane();
        jPanelPianoRollParent = new javax.swing.JPanel();
        jPanelPianoRoll = new javax.swing.JPanel();
        jPanelPianoRollKeys = new javax.swing.JPanel();
        jPanelPianoParent = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPanelPiano = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(100, 30));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        setLayout(new java.awt.CardLayout());

        jSplitPane2.setDividerLocation(350);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("1.PlayList"));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jListPlayList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListPlayListValueChanged(evt);
            }
        });
        jScrollPanePlayList.setViewportView(jListPlayList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel4.add(jScrollPanePlayList, gridBagConstraints);

        jPanel1.add(jPanel4);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jButtonUp.setText("Up");
        jButtonUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jButtonUp, gridBagConstraints);

        jButtonAdd.setText("Add");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jButtonAdd, gridBagConstraints);

        jButtonDown.setText("Down");
        jButtonDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jButtonDown, gridBagConstraints);

        jButtonDeque.setText("Deque");
        jButtonDeque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDequeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jButtonDeque, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jPanel5, gridBagConstraints);

        jPanel1.add(jPanel3);

        jSplitPane2.setLeftComponent(jPanel1);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        jPanelSelInfo1.setBorder(javax.swing.BorderFactory.createTitledBorder("2.Information"));
        jPanelSelInfo1.setLayout(new javax.swing.BoxLayout(jPanelSelInfo1, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane4.setViewportView(jListFileInfo);

        jPanelSelInfo1.add(jScrollPane4);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 2.0;
        jPanel6.add(jPanelSelInfo1, gridBagConstraints);

        jButtonPlay.setText("Play");
        jButtonPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPlayActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonPlay);

        jCheckBoxChain.setText("Chained");
        jCheckBoxChain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxChainActionPerformed(evt);
            }
        });
        jPanel2.add(jCheckBoxChain);

        jCheckBoxRepeat.setText("Repeat");
        jCheckBoxRepeat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxRepeatActionPerformed(evt);
            }
        });
        jPanel2.add(jCheckBoxRepeat);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel6.add(jPanel2, gridBagConstraints);

        jSplitPane2.setRightComponent(jPanel6);

        jSplitPane1.setLeftComponent(jSplitPane2);

        jPanelRight.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelRight.add(jSliderSongPosition, gridBagConstraints);

        jTextFieldCurrentSongName.setEditable(false);
        jTextFieldCurrentSongName.setText("3.Playinng");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelRight.add(jTextFieldCurrentSongName, gridBagConstraints);

        ｊButtonPause.setText("PAUSE");
        ｊButtonPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ｊButtonPauseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanelRight.add(ｊButtonPause, gridBagConstraints);

        jLabelSongPosition.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelRight.add(jLabelSongPosition, gridBagConstraints);

        jTabbedPanePiano.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPanePianoStateChanged(evt);
            }
        });
        jTabbedPanePiano.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTabbedPanePianoPropertyChange(evt);
            }
        });

        jPanelPianoRollParent.setLayout(new java.awt.GridBagLayout());

        jPanelPianoRoll.setLayout(new javax.swing.BoxLayout(jPanelPianoRoll, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelPianoRollParent.add(jPanelPianoRoll, gridBagConstraints);

        jPanelPianoRollKeys.setPreferredSize(new java.awt.Dimension(111, 111));
        jPanelPianoRollKeys.setLayout(new javax.swing.BoxLayout(jPanelPianoRollKeys, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        jPanelPianoRollParent.add(jPanelPianoRollKeys, gridBagConstraints);

        jTabbedPanePiano.addTab("Piano Roll", jPanelPianoRollParent);

        jPanelPianoParent.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jPanelPianoParentComponentResized(evt);
            }
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanelPianoParentComponentShown(evt);
            }
        });
        jPanelPianoParent.setLayout(new javax.swing.BoxLayout(jPanelPianoParent, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jPanelPiano.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jPanelPianoComponentResized(evt);
            }
        });
        jPanelPiano.setLayout(new java.awt.GridBagLayout());
        jScrollPane3.setViewportView(jPanelPiano);

        jPanelPianoParent.add(jScrollPane3);

        jTabbedPanePiano.addTab("Piano Keys", jPanelPianoParent);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelRight.add(jTabbedPanePiano, gridBagConstraints);

        jSplitPane1.setRightComponent(jPanelRight);

        add(jSplitPane1, "card2");
        jSplitPane1.getAccessibleContext().setAccessibleDescription("");
    }// </editor-fold>//GEN-END:initComponents

    public void createPianoControls(int noteLowest, int octaveRange, boolean[] activeChannels, int[] listPrograms, List<Integer> drumProgs) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createPianoControls(noteLowest, octaveRange, activeChannels, listPrograms, drumProgs);
                }
            });
            return;
        }

        invalidate();
        _listKeyboard = new MX00PianoPanel[16];
        _drumPanel = null;

        for (int i = jPanelPiano.getComponentCount(); i >= 1; i--) {
            Component comp = jPanelPiano.getComponent(i - 1);
            jPanelPiano.remove(comp);
        }

        int height = 150;

        _drumPanel = new MX00DrumPadPanel();
        for (int note : drumProgs) {
            _drumPanel.addNote(note);
        }
        int rows = 0;
        for (int ch = 0; ch < 16; ++ch) {
            if (activeChannels[ch]) {
                if (ch == DRUM_CH) {
                    if (_drumPanel != null) {
                        GridBagConstraints cont = new GridBagConstraints();
                        cont.gridx = 0;
                        cont.gridy = rows;
                        cont.weightx = 1.0;
                        cont.weighty = 0;
                        cont.fill = GridBagConstraints.HORIZONTAL;

                        _drumPanel.setMinimumSize(new Dimension(300, height));

                        cont.fill = GridBagConstraints.HORIZONTAL;
                        jPanelPiano.add(_drumPanel, cont);
                        rows++;
                    }
                } else {
                    MXPianoKeys keys = new MXPianoKeys();
                    keys.setNoteRange(noteLowest, octaveRange);
                    MX00PianoPanel piano = new MX00PianoPanel(keys);
                    piano.setChannel(ch);
                    piano.updateProgramNumber(listPrograms[ch] < 0 ? 0 : listPrograms[ch]);
                    piano.setMinimumSize(new Dimension(300, height));
                    piano.setPreferredSize(new Dimension(300, height));
                    piano.setMaximumSize(new Dimension(2000, height));

                    GridBagConstraints cont = new GridBagConstraints();
                    cont.gridx = 0;
                    cont.gridy = rows;
                    cont.weightx = 1.0;
                    cont.weighty = 0;
                    cont.fill = GridBagConstraints.HORIZONTAL;
                    jPanelPiano.add(piano, cont);
                    _listKeyboard[ch] = piano;
                    rows++;
                }
            }
        }

        GridBagConstraints cont2 = new GridBagConstraints();
        cont2.gridx = 0;
        cont2.gridy = rows;
        cont2.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cont2.fill = java.awt.GridBagConstraints.BOTH;
        cont2.weightx = 1.0;
        cont2.weighty = 3.0;
        jPanelPiano.add(new JPanel(), cont2);

        GridBagConstraints cont = new GridBagConstraints();
        cont.gridx = 0;
        cont.gridy = rows;
        cont.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        cont.fill = java.awt.GridBagConstraints.BOTH;
        cont.weightx = 1.0;
        cont.weighty = 1.0;

        cont.fill = GridBagConstraints.BOTH;
        jPanelPiano.add(new JPanel(), cont);
        rows++;

        _pianoRollKeys = new MXPianoKeys();
        _pianoRollKeys.setNoteRange(noteLowest, octaveRange);
        _pianoRollRoll = new MXPianoRoll(_player.getSequencer(), _pianoRollKeys);
        _pianoRollRoll.setNoteRange(noteLowest, octaveRange);
        jPanelPianoRollKeys.removeAll();
        jPanelPianoRollKeys.add(_pianoRollKeys);
        jPanelPianoRoll.removeAll();;
        jPanelPianoRoll.add(_pianoRollRoll);
        jPanelPianoRollKeys.setMinimumSize(new Dimension(100, 150));
        autoResizePiano();
    }

    MXPianoRoll _pianoRollRoll;
    MXPianoKeys _pianoRollKeys;

    public void autoResizePiano() {
        try {
            if (_drumPanel != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        _drumPanel.setPreferredSize(new Dimension(jPanelRight.getWidth() - 70, 350));
                        _drumPanel.updateUI();
                        /*SwingUtilities.invokeLater(new Runnable(){
                            public void run() {*/
                        int maxHeight = 0;
                        for (JToggleButton c : _drumPanel.listDrums) {
                            if (c != null) {
                                Rectangle r = c.getBounds();
                                int height = r.y + r.height;
                                if (maxHeight < height) {
                                    maxHeight = height;
                                }
                            }
                        }
                        _drumPanel.setPreferredSize(new Dimension(jPanelRight.getWidth() - 70, maxHeight));
                        _drumPanel.setSize(new Dimension(jPanelRight.getWidth() - 70, maxHeight));
                        /*    }
                        });*/
                    }
                });
            }
            for (int ch = 0; ch < 16; ++ch) {
                if (_listKeyboard[ch] != null) {
                    _listKeyboard[ch].autoAdjustHeight(jPanelRight.getWidth() - 70);
                    _listKeyboard[ch]._keys.invalidate();
                }
            }
            if (_pianoRollKeys != null && _pianoRollRoll != null) {
                //_pianoRollRoll.resetTiming(4000,  20);
                _pianoRollRoll.clearCache(_pianoRollRoll.getSongPos());
                int width = _pianoRollRoll.getWidth();
                _pianoRollKeys.setPreferredSize(new Dimension(width, 100));
            }
            revalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
        if (true) {
            FileFilterListExt filter = new FileFilterListExt();
            filter.addExtension(".MID");

            File root = new File("C:/midi");

            if (root.isDirectory() == false) {
                root = null;
            }

            MXSwingFolderBrowser folders = new MXSwingFolderBrowser(root, filter, null);
            MXUtil.showAsDialog(this, folders, "Select MIDI File");
            if (folders.getReturnStatus() != INavigator.RETURN_STATUS_APPROVED) {
                return;
            }
            FileList selected = folders.getReturnValue();
            if (selected == null) {
                return;
            }
            for (File file : selected) {
                _dxPlayList.addAsFile(file);
            }
        } else {
            MXFileChooser chooser = new MXFileChooser();

            chooser.addExtension(".mid", "Standard MIDI File");
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setDialogTitle("Add Standard MIDI File");

            if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = chooser.getSelectedFile();
            _dxPlayList.addAsFile(file);
        }

    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jListPlayListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListPlayListValueChanged
        int x = jListPlayList.getSelectedIndex();
        if (x >= 0) {
            PlayListElement f = _dxPlayList.elementAt(x);
            DefaultListModel model = new DefaultListModel();

            _selectedItem = f;

            try {
                String[] str = _player.readFileInfo(f._file);
                model.addElement("MetaInfo: ");
                for (int i = 0; i < str.length; ++i) {
                    model.addElement(str[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            jListFileInfo.setModel(model);
        }
    }//GEN-LAST:event_jListPlayListValueChanged

    private void jButtonDequeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDequeActionPerformed
        int x = jListPlayList.getSelectedIndex();
        if (x >= 0) {
            PlayListElement file = _dxPlayList.elementAt(x);
            if (_playingFile != null && _playingFile._id == file._id) {
                if (JOptionPane.showConfirmDialog(
                        this,
                        file + " is current song.\nThis operation will cancel Chain / Repeat.",
                        null,
                        JOptionPane.OK_CANCEL_OPTION)
                        == JOptionPane.OK_OPTION) {
                    jCheckBoxChain.setSelected(false);
                    jCheckBoxRepeat.setSelected(false);
                    _dxPlayList.removeElementAt(x);
                }
            } else {
                _dxPlayList.removeElementAt(x);
            }
        }
    }//GEN-LAST:event_jButtonDequeActionPerformed

    private void jButtonUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpActionPerformed
        int x = jListPlayList.getSelectedIndex();
        if (x >= 1) {
            PlayListElement f = _dxPlayList.elementAt(x);
            _dxPlayList.removeElementAt(x);
            _dxPlayList.insertElementAt(f, x - 1);
            jListPlayList.setSelectedIndex(x - 1);
        }
    }//GEN-LAST:event_jButtonUpActionPerformed

    private void jButtonDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDownActionPerformed
        int x = jListPlayList.getSelectedIndex();
        if (x >= 0 && x < _dxPlayList.size() - 1) {
            PlayListElement f = _dxPlayList.elementAt(x);
            _dxPlayList.removeElement(x);
            _dxPlayList.insertElementAt(f, x + 1);
            jListPlayList.setSelectedIndex(x + 1);
        }
    }//GEN-LAST:event_jButtonDownActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        autoResizePiano();
    }//GEN-LAST:event_formComponentResized

    private void jPanelPianoComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanelPianoComponentResized
    }//GEN-LAST:event_jPanelPianoComponentResized

    private void ｊButtonPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ｊButtonPauseActionPerformed
        if (_player.isSequencerPlaying()) {
            _player.stopSequencer(0);
        } else {
            if (_playingFile != null) {
                _selectedItem = _playingFile;
            }
            turnOnMusic(_selectedItem, jSliderSongPosition.getValue());
        }
    }//GEN-LAST:event_ｊButtonPauseActionPerformed

    private void jButtonPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPlayActionPerformed
        _player.stopSequencer(0);
        turnOnMusic(_selectedItem, 0);
    }//GEN-LAST:event_jButtonPlayActionPerformed

    private void jCheckBoxChainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxChainActionPerformed
    }//GEN-LAST:event_jCheckBoxChainActionPerformed

    private void jCheckBoxRepeatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxRepeatActionPerformed
    }//GEN-LAST:event_jCheckBoxRepeatActionPerformed

    private void jPanelPianoParentComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanelPianoParentComponentResized
    }//GEN-LAST:event_jPanelPianoParentComponentResized

    private void jPanelPianoParentComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanelPianoParentComponentShown
    }//GEN-LAST:event_jPanelPianoParentComponentShown

    private void jTabbedPanePianoStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPanePianoStateChanged
        tabActivated();
    }//GEN-LAST:event_jTabbedPanePianoStateChanged

    private void jTabbedPanePianoPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTabbedPanePianoPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jTabbedPanePianoPropertyChange

    public void updateDXPianoKeys(int dword) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateDXPianoKeys(dword);
                }
            });
            return;
        }

        int status = (dword >> 16) & 0xff;
        int data1 = (dword >> 8) & 0xff;
        int data2 = (dword) & 0xff;

        int command = status & 0xf0;
        int channel = status & 0x0f;

        if (command >= 0x80 && command <= 0xef) {
            if (command == MXMidi.COMMAND_CH_NOTEON && data2 == 0) {
                command = MXMidi.COMMAND_CH_NOTEOFF;
            }

            if (command == MXMidi.COMMAND_CH_NOTEON) {
                if (channel != DRUM_CH) {
                    MX00PianoPanel piano = _listKeyboard[channel];
                    if (piano != null) {
                        piano.getKeys().noteOn(data1);
                    }
                } else {
                    MX00DrumPadPanel kit = _drumPanel;
                    kit.noteOn(data1);
                }
            } else if (command == MXMidi.COMMAND_CH_NOTEOFF) {
                if (channel != DRUM_CH) {
                    MX00PianoPanel piano = _listKeyboard[channel];
                    if (piano != null) {
                        piano.getKeys().noteOff(data1);
                    }
                } else {
                    if (_drumPanel != null) {
                        MX00DrumPadPanel kit = _drumPanel;
                        kit.noteOff(data1);
                    }
                }
            } else if (command == MXMidi.COMMAND_CH_CONTROLCHANGE
                    && data1 == MXMidi.DATA1_CC_DAMPERPEDAL) {
                MX00PianoPanel piano = _listKeyboard[channel];
                if (piano != null) {
                    piano.getKeys().sustain(data2);
                }
            } else if (command == MXMidi.COMMAND_CH_CONTROLCHANGE
                    && data1 == MXMidi.DATA1_CC_ALLNOTEOFF) {
                MX00PianoPanel piano = _listKeyboard[channel];
                if (piano != null) {
                    piano.getKeys().allNoteOff();
                }
            } else if (command == MXMidi.COMMAND_CH_PROGRAMCHANGE) {
                MX00PianoPanel piano = _listKeyboard[channel];
                if (piano != null) {
                    piano.updateProgramNumber(data1);
                }
            }
        }
    }

    private ComboBoxModel _comboModel;
    private ComboBoxModel _channelSelectModel;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonDeque;
    private javax.swing.JButton jButtonDown;
    private javax.swing.JButton jButtonPlay;
    private javax.swing.JButton jButtonUp;
    private javax.swing.JCheckBox jCheckBoxChain;
    private javax.swing.JCheckBox jCheckBoxRepeat;
    private javax.swing.JLabel jLabelSongPosition;
    private javax.swing.JList<String> jListFileInfo;
    private javax.swing.JList<jp.synthtarou.midimixer.mx00playlist.PlayListElement> jListPlayList;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanelPiano;
    private javax.swing.JPanel jPanelPianoParent;
    private javax.swing.JPanel jPanelPianoRoll;
    private javax.swing.JPanel jPanelPianoRollKeys;
    private javax.swing.JPanel jPanelPianoRollParent;
    private javax.swing.JPanel jPanelRight;
    private javax.swing.JPanel jPanelSelInfo1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPanePlayList;
    private javax.swing.JSlider jSliderSongPosition;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPanePiano;
    private javax.swing.JTextField jTextFieldCurrentSongName;
    private javax.swing.JButton ｊButtonPause;
    // End of variables declaration//GEN-END:variables

    PlayListElement _lastPlayed = null;

    @Override
    public void smfPlayNote(MXTiming timing, SMFMessage e) {
    }

    @Override
    public void smfStarted() {
    }

    @Override
    public void smfStoped(boolean fineFinish) {
        if (!fineFinish) {
            return;
        }

        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    smfStoped(fineFinish);
                }
            });
            return;
        }
        
        PlayListElement file = _lastPlayed;
        PlayListElement next = null;

        if (getDXChained()) {
            for (int i = 0; i < _dxPlayList.size(); ++i) {
                PlayListElement seek = _dxPlayList.get(i);
                if (seek == file) {
                    i++;
                    if (i < _dxPlayList.getSize()) {
                        next = _dxPlayList.elementAt(i);
                        break;
                    } else if (getDXRepeat()) {
                        i = 0;
                        if (i < _dxPlayList.getSize()) {
                            next = _dxPlayList.elementAt(0);
                            break;
                        } else {
                            next = null;
                            break;
                        }
                    }
                }
            }
        } else if (getDXRepeat()) {
            next = file;
        } else {
            next = null;
        }

        if (next != null) {
            _selectedItem = next;
            turnOnMusic(_selectedItem, 0);
        }
    }

    @Override
    public void smfProgress(long pos, long finish) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    smfProgress(pos, finish);
                }
            });
            return;
        }
        if (pos == 0) {
            MXMain.getMain().getLayerProcess().resendProgramChange();
        }
        jLabelSongPosition.setText(MXUtil.digitalClock(pos) + "/" + MXUtil.digitalClock(finish));

        jSliderSongPosition.setMaximum((int) finish);
        jSliderSongPosition.setValue((int) pos);
    }

    public void turnOnMusic(PlayListElement file, final int pos) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    turnOnMusic(file, pos);
                }
            });
            return;
        }
        try {
            if (file == null) {
                JOptionPane.showMessageDialog(this, "Choice one from PlayList", "Error", JOptionPane.OK_OPTION);
                return;
            }
            if (_dxPlayList.indexOf(file) < 0) {
                JOptionPane.showMessageDialog(this, file + " is not in PlayList", "Error", JOptionPane.OK_OPTION);
                return;
            }
            setDXCurrentSongName(file.toString());
            _player.openFile(file._file);

            ArrayList<Integer> drums = _player.getSequencer()._parser._drums;
            int noteLowest = _player.getSequencer()._parser._noteLowest;
            int noteHighest = _player.getSequencer()._parser._noteHighest;
            int[] program = _player.getSequencer()._parser._programList;
            boolean[] exist = _player.getSequencer()._parser._existNoteChannel;

            int lo2 = noteLowest;
            lo2 = lo2 / 12;
            lo2 = lo2 * 12;

            int hi2 = lo2;

            while (hi2 < noteHighest) {
                hi2 += 12;
            }

            int x = lo2 / 12;
            int octaveRange = hi2 / 12;

            octaveRange -= x;
            x *= 12;

            if (octaveRange <= 2) {
                if (x >= 12) {
                    x -= 12;
                    octaveRange += 1;
                }
                if (octaveRange <= 2) {
                    octaveRange += 1;
                }
            }

            noteLowest = x;
            noteHighest = octaveRange * 12 + x;

            while (octaveRange <= 4) {
                octaveRange += 2;
                noteLowest -= 12;
            }
            while (octaveRange < 5) {
                octaveRange++;
            }

            MXMain.getMain().getPlayListProcess().createPianoControls(noteLowest, octaveRange, exist, program, drums);
            _lastPlayed = file;
            _playingFile = file;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jListPlayList.setSelectedValue(file, true);
                    _player.startSequencer(MX00View.this, pos);
                }
            });
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Can't open File [" + file._file +" ]\n" + ex.toString(), "Error", JOptionPane.OK_OPTION);
            return;
        } catch (Throwable ex) {
            return;
        }
    }

    public void tabActivated() {
        if (_pianoRollRoll != null) {
            _pianoRollRoll.setDoingPaint(jTabbedPanePiano.getSelectedIndex() == 0);
            _pianoRollKeys.setDoingPaint(jTabbedPanePiano.getSelectedIndex() == 0);
        }
        if (_listKeyboard != null) {
            for (int i = 0; i < 16; ++i) {
                if (_listKeyboard[i] != null) {
                    _listKeyboard[i]._keys.setDoingPaint(jTabbedPanePiano.getSelectedIndex() == 1);
                }
            }
        }
    }

    public void tabDeactivated() {
        if (_pianoRollRoll != null) {
            _pianoRollRoll.setDoingPaint(false);
            _pianoRollKeys.setDoingPaint(false);
        }
        if (_listKeyboard != null) {
            for (int i = 0; i < 16; ++i) {
                MX00PianoPanel panel = _listKeyboard[i];
                if (panel != null) {
                    panel._keys.setDoingPaint(false);
                }
            }
        }
    }
}
