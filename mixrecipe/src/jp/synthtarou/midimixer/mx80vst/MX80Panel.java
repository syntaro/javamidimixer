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
package jp.synthtarou.midimixer.mx80vst;

import jp.synthtarou.midimixer.libs.vst.VSTFolder;
import jp.synthtarou.midimixer.libs.vst.VSTInstance;
import jp.synthtarou.midimixer.libs.vst.VSTStream;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.async.Transaction;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileFilterListExt;
import jp.synthtarou.midimixer.libs.swing.MXModalFrame;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.MXSwingFolderBrowser;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderSingleClick;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileList;
import jp.synthtarou.midimixer.mx35cceditor.ccxml.navigator.ParamsOfNavigator;
import jp.synthtarou.midimixer.windows.MXLIB02VST3;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX80Panel extends javax.swing.JPanel {

    static MX80Panel _instance = new MX80Panel();

    public static synchronized MX80Panel getInstance() {
        return _instance;
    }

    boolean _initDone = false;

    public static void main(String[] args) {
        MX80Panel panel = MX80Panel.getInstance();

        panel.addFilter(new File("C:/Program Files/Common Files/VST3"));
        panel.addFilter(new File("C:/Program Files/Steinberg"));

        MXModalFrame.showAsDialog(null, panel, "VST Picker");
    }

    MXWrapList<Integer> _streamModel;
    MXWrapList<Integer> _sampleRateModel;
    MXWrapList<Integer> _latencyModel;

    /**
     * Creates new form FileExtensionFilterPanel
     */
    MX80Panel() {
        initComponents();
        updateMainTree();
        jListSkip.setModel(createSkipListModel());
        updateLoadList();
        updateEffectList();

        jLabelSpacer.setText("");

        VSTStream stream = VSTStream.getInstance();

        _streamModel = createStreamModel();
        jComboBoxStream.setModel(_streamModel);
        _streamModel.writeComboBox(jComboBoxStream, stream.getStream());

        _latencyModel = createLatencyModel();
        jComboBoxLatency.setModel(_latencyModel);
        _latencyModel.writeComboBox(jComboBoxLatency, stream.getBlockSize());

        _sampleRateModel = createSampleRateModel();
        jComboBoxSampleRate.setModel(_sampleRateModel);
        _sampleRateModel.writeComboBox(jComboBoxSampleRate, stream.getSampleRate());

        streamHandler.run();

        if (MXLIB02VST3.getInstance().isUsable() == false) {
            return;
        }
        float vol1 = MXLIB02VST3.getInstance().getMasterVolume();
        int vol1000 = (int) (vol1 * 1000);
        jSliderMasterVolume.setMinimum(0);
        jSliderMasterVolume.setMaximum(1000);
        jSliderMasterVolume.setValue(vol1000);
        new MXAttachSliderSingleClick(jSliderMasterVolume);
        new MXAttachSliderLikeEclipse(jSliderMasterVolume);
        _initDone = true;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Container cont = MXUtil.getOwnerWindow(MX80Panel.this);
                if (cont != null && cont instanceof JDialog) {
                    JDialog frame = (JDialog) cont;
                    System.out.println("Install Close Fook");
                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            for (VSTInstance vst : MX80Process.getInstance()._listInstrument) {
                                vst.postCloseVST(null).awaitResult();
                            }
                        }

                        @Override
                        public void windowClosed(WindowEvent e) {
                        }
                    });
                }
            }
        });
    }

    MXWrapList<Integer> createStreamModel() {
        MXWrapList<Integer> model = new MXWrapList();
        VSTStream stream = VSTStream.getInstance();
        for (int i = 0; i < stream.count(); ++i) {
            if (stream.getTypeName(i).equals(("ASIO"))) {
                model.addNameAndValue(stream.getName(i), i);
            }
        }

        return model;
    }

    MXWrapList<Integer> createLatencyModel() {
        MXWrapList<Integer> list = new MXWrapList();
        int[] entry = {128, 256, 512, 1024, 2048, 4096};
        for (int i = 0; i < entry.length; ++i) {
            int x = entry[i];
            list.addNameAndValue(Integer.toString(x) + " samples", x);
        }
        return list;
    }

    MXWrapList<Integer> createSampleRateModel() {
        MXWrapList<Integer> list = new MXWrapList();
        list.addNameAndValue("22.05khz", (Integer) 22050);
        list.addNameAndValue("44.1khz", (Integer) 44100);
        list.addNameAndValue("48khz", (Integer) 48000);
        list.addNameAndValue("88.2khz", (Integer) 88200);
        list.addNameAndValue("96khz", (Integer) 96000);
        return list;
    }

    private int readPanelLatency() {
        Integer x = _latencyModel.readCombobox(jComboBoxLatency);
        if (x != null) {
            return x;
        }
        return -1;
    }

    private int readPanelSampleRate() {
        Integer x = _sampleRateModel.readCombobox(jComboBoxSampleRate);
        if (x != null) {
            return x;
        }
        return -1;
    }

    Transaction streamHandler = new Transaction("streamHandler") {
        @Override
        public void run() {
            VSTStream stream = VSTStream.getInstance();
            if (stream.isOpen()) {
                jComboBoxStream.setEnabled(false);
                jComboBoxSampleRate.setEnabled(false);
                jComboBoxLatency.setEnabled(false);
                jButtonOpenStream.setText("Close Stream");
            } else {
                jComboBoxStream.setEnabled(true);
                jComboBoxSampleRate.setEnabled(true);
                jComboBoxLatency.setEnabled(true);
                jButtonOpenStream.setText("Open Stream");
            }
            jButtonOpenStream.setEnabled(true);
        }
    };

    public void openStream(int x, int sampleRate, int blockSize) {
        jButtonOpenStream.setEnabled(false);
        _streamModel.writeComboBox(jComboBoxStream, x);

        VSTStream stream = VSTStream.getInstance();
        stream.setStream(x);
        stream.setSampleRate(sampleRate);
        stream.setBlockSize(blockSize);
        stream.postOpenStream(streamHandler.copyWithNewTicket("postOpenStream"));
    }

    public void closeStream() {
        jButtonOpenStream.setEnabled(false);
        VSTStream stream = VSTStream.getInstance();
        stream.postCloseStream(streamHandler.copyWithNewTicket("postCloseStream"));
    }

    public TreeModel createInitialModel(String message) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        node.setUserObject(message);
        root.add(node);

        TreeModel model = new DefaultTreeModel(root);
        return model;
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
        jSplitPane3 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTreeMain = new javax.swing.JTree();
        jButtonAddRoot = new javax.swing.JButton();
        jButtonRemoveRoot = new javax.swing.JButton();
        jButtonStartRescan = new javax.swing.JButton();
        jButtonStartQuickScan = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jButtonAddSkip = new javax.swing.JButton();
        jButtonAddSkipBrowse = new javax.swing.JButton();
        jButtonRemoveSkip = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListSkip = new javax.swing.JList<>();
        jPanelSynthsContainer = new javax.swing.JPanel();
        jScrollPaneSynthContainer = new javax.swing.JScrollPane();
        jPanelSynths = new javax.swing.JPanel();
        jComboBoxStream = new javax.swing.JComboBox<>();
        jButtonOpenStream = new javax.swing.JButton();
        jComboBoxLatency = new javax.swing.JComboBox<>();
        jComboBoxSampleRate = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jSliderMasterVolume = new javax.swing.JSlider();
        jPanelEffectsContainer = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabelDirectory = new javax.swing.JLabel();
        jLabelSpacer = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSplitPane1PropertyChange(evt);
            }
        });

        jSplitPane3.setDividerLocation(400);
        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("RootList"));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setViewportView(jTreeMain);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jScrollPane1, gridBagConstraints);

        jButtonAddRoot.setText("Add ...");
        jButtonAddRoot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddRootActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jButtonAddRoot, gridBagConstraints);

        jButtonRemoveRoot.setText("Remove From List");
        jButtonRemoveRoot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveRootActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jButtonRemoveRoot, gridBagConstraints);

        jButtonStartRescan.setText("ReScan(EveryFolder)");
        jButtonStartRescan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartRescanActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        jPanel2.add(jButtonStartRescan, gridBagConstraints);

        jButtonStartQuickScan.setText("Scan(OnlyNewFolder)");
        jButtonStartQuickScan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartQuickScanActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        jPanel2.add(jButtonStartQuickScan, gridBagConstraints);

        jSplitPane3.setLeftComponent(jPanel2);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("SkipList"));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jButtonAddSkip.setText("Add < Selection");
        jButtonAddSkip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddSkipActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jButtonAddSkip, gridBagConstraints);

        jButtonAddSkipBrowse.setText("Add ...");
        jButtonAddSkipBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddSkipBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jButtonAddSkipBrowse, gridBagConstraints);

        jButtonRemoveSkip.setText("Remove From List");
        jButtonRemoveSkip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveSkipActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jButtonRemoveSkip, gridBagConstraints);

        jListSkip.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(jListSkip);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane3, gridBagConstraints);

        jSplitPane3.setBottomComponent(jPanel1);

        jSplitPane1.setLeftComponent(jSplitPane3);

        jPanelSynthsContainer.setBorder(javax.swing.BorderFactory.createTitledBorder("LoadList"));
        jPanelSynthsContainer.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jPanelSynthsContainerComponentResized(evt);
            }
        });
        jPanelSynthsContainer.setLayout(new java.awt.GridBagLayout());

        jPanelSynths.setLayout(new javax.swing.BoxLayout(jPanelSynths, javax.swing.BoxLayout.PAGE_AXIS));
        jScrollPaneSynthContainer.setViewportView(jPanelSynths);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelSynthsContainer.add(jScrollPaneSynthContainer, gridBagConstraints);

        jComboBoxStream.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelSynthsContainer.add(jComboBoxStream, gridBagConstraints);

        jButtonOpenStream.setText("Open Stream");
        jButtonOpenStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenStreamActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelSynthsContainer.add(jButtonOpenStream, gridBagConstraints);

        jComboBoxLatency.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelSynthsContainer.add(jComboBoxLatency, gridBagConstraints);

        jComboBoxSampleRate.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        jPanelSynthsContainer.add(jComboBoxSampleRate, gridBagConstraints);

        jLabel1.setText("MasterVolume");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelSynthsContainer.add(jLabel1, gridBagConstraints);

        jSliderMasterVolume.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderMasterVolumeStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelSynthsContainer.add(jSliderMasterVolume, gridBagConstraints);

        jPanelEffectsContainer.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanelEffectsContainer.setLayout(new javax.swing.BoxLayout(jPanelEffectsContainer, javax.swing.BoxLayout.LINE_AXIS));

        jLabel2.setText("jLabel2");
        jPanelEffectsContainer.add(jLabel2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelSynthsContainer.add(jPanelEffectsContainer, gridBagConstraints);

        jButton1.setText("Repaint");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 4;
        jPanelSynthsContainer.add(jButton1, gridBagConstraints);

        jSplitPane1.setRightComponent(jPanelSynthsContainer);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSplitPane1, gridBagConstraints);

        jLabelDirectory.setText("......");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jLabelDirectory, gridBagConstraints);

        jLabelSpacer.setText("spacer");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        add(jLabelSpacer, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonStartQuickScanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartQuickScanActionPerformed
        startScanDirectory(true);
    }//GEN-LAST:event_jButtonStartQuickScanActionPerformed

    private void jButtonAddRootActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddRootActionPerformed
        FileFilterListExt filter = new FileFilterListExt() {
            public boolean acacept(File file) {
                if (accept(file)) {
                    return true;
                }
                return false;
            }
        };
        filter.addExtension("VST3");
        filter._stopAllFile = true;
        MXSwingFolderBrowser browse = new MXSwingFolderBrowser(new File("C:\\Program Files"), filter, null);
        MXModalFrame.showAsDialog(this, browse, "Select and Enter");
        FileList selected = browse.getParamsOfNavigator().getApprovedValue();
        if (selected != null) {
            for (File f : selected) {
                if (f != null) {
                    addFilter(f);
                }
            }
        }

    }//GEN-LAST:event_jButtonAddRootActionPerformed

    private void jButtonRemoveRootActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveRootActionPerformed
        File file = getSelectedFile();
        if (file == null) {
            return;
        }
        try {
            TreePath path = jTreeMain.getSelectionPath();
            if (path != null) {
                if (path.getPathCount() <= 2) {
                    int opt = JOptionPane.showConfirmDialog(this, "Remove " + file + " from List", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (opt == JOptionPane.YES_OPTION) {
                        removeFilter(file);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, file + " is not Root Folder", "Error", JOptionPane.OK_OPTION);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButtonRemoveRootActionPerformed

    private void jButtonAddSkipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddSkipActionPerformed
        File file = getSelectedFile();
        if (file != null) {
            try {
                String path = file.getPath();
                if (MX80Process.getInstance()._listSkip.contains(path) == false) {
                    addSkip(path);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jButtonAddSkipActionPerformed

    private void jButtonRemoveSkipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveSkipActionPerformed
        int sel = jListSkip.getSelectedIndex();
        if (sel >= 0) {
            String path = MX80Process.getInstance()._listSkip.get(sel);
            int opt = JOptionPane.showConfirmDialog(this, "Remove " + path + " from SkipList", "Confirm", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                removeSkip(path);
            }
        }
    }//GEN-LAST:event_jButtonRemoveSkipActionPerformed

    private void jButtonAddSkipBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddSkipBrowseActionPerformed
        FileFilterListExt filter = new FileFilterListExt();
        filter.addExtension("VST3");
        MXSwingFolderBrowser browse = new MXSwingFolderBrowser(new File("C:\\Program Files"), filter, null);
        MXModalFrame.showAsDialog(this, browse, "Select and Enter");
        FileList selected = browse.getParamsOfNavigator().getApprovedValue();
        if (selected != null) {
            try {
                for (File path : selected) {
                    String textPath = path.toString();
                    if (MX80Process.getInstance()._listSkip.contains(textPath) == false) {
                        addSkip(textPath);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }        }

    }//GEN-LAST:event_jButtonAddSkipBrowseActionPerformed

    private void jButtonStartRescanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartRescanActionPerformed
        startScanDirectory(false);
    }//GEN-LAST:event_jButtonStartRescanActionPerformed

    private void jButtonOpenStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenStreamActionPerformed
        if (VSTStream.getInstance().isOpen()) {
            closeStream();
        } else {
            MX80Process process = MX80Process.getInstance();
            int sel = _streamModel.readCombobox(jComboBoxStream);
            int sampleRate = _sampleRateModel.readCombobox(jComboBoxSampleRate);
            int blockSize = _latencyModel.readCombobox(jComboBoxLatency);
            openStream(sel, sampleRate, blockSize);
        }
    }//GEN-LAST:event_jButtonOpenStreamActionPerformed

    private void jSliderMasterVolumeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderMasterVolumeStateChanged
        if (_initDone) {
            int vol1000 = jSliderMasterVolume.getValue();
            float vol1 = vol1000 * 0.001f;
            MXLIB02VST3.getInstance().setMasterVolume(vol1);
        }
    }//GEN-LAST:event_jSliderMasterVolumeStateChanged

    private void jPanelSynthsContainerComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanelSynthsContainerComponentResized
        // TODO add your handling code here:
        onResizeSynth();
    }//GEN-LAST:event_jPanelSynthsContainerComponentResized

    private void jSplitPane1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPane1PropertyChange
        onResizeSynth();
    }//GEN-LAST:event_jSplitPane1PropertyChange

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        updateLoadList();
    }//GEN-LAST:event_jButton1ActionPerformed

    public File getSelectedFileAsVST() {
        TreePath path = jTreeMain.getSelectionPath();
        if (path == null) {
            return null;
        }
        if (path.getPathCount() != 4) {
            return null;
        }
        return getSelectedFile();
    }

    public File getSelectedFile() {
        TreePath path = jTreeMain.getSelectionPath();
        if (path == null) {
            return null;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node == null) {
            return null;
        }

        Object obj = node.getUserObject();
        if (obj == null) {
            return null;
        }

        if (obj instanceof String) {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) node.getParent();
            Object user = root.getUserObject();
            if (user instanceof File) {
                return new File((File) user, (String) obj);
            }
            new Throwable(user.getClass() + "(" + user + ") is not file").printStackTrace();
            return null;
        }
        if (obj instanceof File) {
            return (File) obj;
        }
        new Throwable(obj.getClass() + " unknown type").printStackTrace();
        return null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonAddRoot;
    private javax.swing.JButton jButtonAddSkip;
    private javax.swing.JButton jButtonAddSkipBrowse;
    private javax.swing.JButton jButtonOpenStream;
    private javax.swing.JButton jButtonRemoveRoot;
    private javax.swing.JButton jButtonRemoveSkip;
    private javax.swing.JButton jButtonStartQuickScan;
    private javax.swing.JButton jButtonStartRescan;
    private javax.swing.JComboBox<String> jComboBoxLatency;
    private javax.swing.JComboBox<String> jComboBoxSampleRate;
    private javax.swing.JComboBox<String> jComboBoxStream;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelDirectory;
    private javax.swing.JLabel jLabelSpacer;
    private javax.swing.JList<String> jListSkip;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelEffectsContainer;
    private javax.swing.JPanel jPanelSynths;
    private javax.swing.JPanel jPanelSynthsContainer;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPaneSynthContainer;
    private javax.swing.JSlider jSliderMasterVolume;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JTree jTreeMain;
    // End of variables declaration//GEN-END:variables

    public void addSkip(String path) {
        MX80Process.getInstance()._listSkip.add(path);
        updateSkipList();
    }

    public void removeSkip(String path) {
        MX80Process.getInstance()._listSkip.remove(path);
        updateSkipList();
    }

    public void addFilter(File file) {
        MX80Process.getInstance().addFolder(file);
        updateMainTree();
    }

    public void removeFilter(File file) {
        MX80Process.getInstance().removeFolder(file);
        updateMainTree();
    }

    MX80Process.Callback _callback = new MX80Process.Callback() {
        @Override
        public void vstScanProgress(String text, long hit, long total) {
            jLabelDirectory.setText("Scan " + text + ", " + hit + " / " + total);
        }

        @Override
        public void vstScanCanceled() {
            jLabelDirectory.setText("Cancelled");
        }

        @Override
        public void vstScanFinished() {
            jLabelDirectory.setText("Done");
            updateMainTree();
        }
    };

    public void startScanDirectory(boolean quick) {
        MX80Process.getInstance()._callback = _callback;
        MX80Process.getInstance().startScan(quick);
    }

    static class SystemFilRenderer extends DefaultTreeCellRenderer {

        private TreeCellRenderer _defRenderer;
        private FileSystemView _view;

        SystemFilRenderer() {
            _defRenderer = new JTree().getCellRenderer();
            _view = FileSystemView.getFileSystemView();
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component component = _defRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            if (false == (value instanceof DefaultMutableTreeNode)) {
                return component;
            }
            DefaultMutableTreeNode node = null, parentNode = null, parentParentNode = null;
            Object user = null, parent = null, parentPanrent = null;

            node = (DefaultMutableTreeNode) value;
            user = node.getUserObject();
            if (node != null) {
                parentNode = (DefaultMutableTreeNode) node.getParent();
                if (parentNode != null) {
                    parent = parentNode.getUserObject();
                }
            }
            if (parentNode != null) {
                parentParentNode = (DefaultMutableTreeNode) parentNode.getParent();
                if (parentParentNode != null) {
                    parentPanrent = parentParentNode.getUserObject();
                }
            }

            JLabel label = (JLabel) component;

            String name = user.toString();
            Icon icon = null;
            String path = user.toString();

            if (user != null && (user instanceof VSTFolder)) {
                label.setIcon(icon);
                label.setText(name);
                label.setToolTipText(path);
                return label;
            }

            if (parent != null && (parent instanceof VSTFolder)) {
                VSTFolder vst = (VSTFolder) parent;
                File folder = vst._rootDirectory;
                File target = (File) user;
                name = VSTFolder.getAsAbsolute(folder, target);
                label.setIcon(icon);
                label.setText(name);
                label.setToolTipText(path);
                return label;
            }

            if (parent != null && (parent instanceof File)) {
                File folder = (File) parent;
                File target = (File) user;

                name = VSTFolder.getAsAbsolute(folder, target);
                icon = _view.getSystemIcon(target);

                label.setIcon(icon);
                label.setText(name);
                label.setToolTipText(path);
                return label;
            } else {
                return label;
            }
        }
    }

    public void updateMainTree() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TreeModel newModel = MX80Panel.this.createFolderTreeModel();
                jTreeMain.setModel(newModel);
                jTreeMain.setRootVisible(false);
                jTreeMain.setCellRenderer(new SystemFilRenderer());

                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) newModel.getRoot();
                for (int i = 0; i < rootNode.getChildCount(); ++i) {
                    Object second = rootNode.getChildAt(i);
                    TreePath path = new TreePath(new Object[]{rootNode, second});
                    jTreeMain.expandPath(path);
                }
            }
        });
    }

    public void updateSkipList() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ListModel list = MX80Panel.this.createSkipListModel();
                jListSkip.setModel(list);
            }
        });
    }

    public ListModel<String> createSkipListModel() {
        DefaultListModel<String> model = new DefaultListModel<String>();
        for (String path : MX80Process.getInstance()._listSkip) {
            model.addElement(path);
        }
        return model;
    }

    public void updateLoadList() {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateLoadList();
                }
            });
            return;
        }
        jPanelSynths.removeAll();
        jPanelSynths.setLayout(new GridBagLayout());

        for (int i = 0; i < MX80Process.getInstance()._listInstrumentPanel.size(); ++i) {
            VSTInstancePanel vstPanel = MX80Process.getInstance()._listInstrumentPanel.get(i);
            GridBagConstraints pos = new GridBagConstraints();
            vstPanel.setParent(this);
            pos.weightx = 1;
            pos.weighty = 1;
            pos.fill = GridBagConstraints.BOTH;
            pos.gridx = 0;
            pos.gridy = i;
            pos.gridwidth = 1;
            pos.gridheight = 1;
            jPanelSynths.add(vstPanel, pos);
        }
        jPanelSynthsContainer.setBorder(BorderFactory.createEmptyBorder());
        jPanelSynthsContainer.setBorder(BorderFactory.createTitledBorder("LoadList"));
    }

    public void updateEffectList() {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateEffectList();
                }
            });
            return;
        }
        jPanelEffectsContainer.removeAll();
        for (int i = 0; i < MX80Process.getInstance()._listEffect.size(); ++i) {
            VSTInstancePanel vstPanel = MX80Process.getInstance()._listEffectPanel.get(i);
            vstPanel.setParent(this);
            jPanelEffectsContainer.add(vstPanel);
        }
        Dimension size = jPanelEffectsContainer.getSize();
        //for repaint scrollbar
        jPanelEffectsContainer.setBorder(BorderFactory.createEmptyBorder());
        jPanelEffectsContainer.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    }

    public TreeModel createFolderTreeModel() {
        DefaultMutableTreeNode realRootNode = new DefaultMutableTreeNode("Root");

        for (int i = 0; i < MX80Process.getInstance().countFolder(); ++i) {
            VSTFolder filter = MX80Process.getInstance().getFolder(i);
            File root = filter._rootDirectory;
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(root);
            rootNode.setAllowsChildren(true);
            realRootNode.add(rootNode);

            if (filter.getListResult() == null) {
                continue;
            }
            for (File dir : filter.getListResult().keySet()) {
                DefaultMutableTreeNode dirNode = new DefaultMutableTreeNode(dir);
                dirNode.setAllowsChildren(true);

                if (filter.getListResult() != null && filter.getListResult().size() > 0) {
                    ArrayList<File> children = filter.getListResult().get(dir);
                    for (File file : children) {
                        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(file);
                        dirNode.add(fileNode);
                        fileNode.setAllowsChildren(false);
                    }
                }
                rootNode.add(dirNode);
            }

        }

        DefaultTreeModel model = new DefaultTreeModel(realRootNode);
        model.setAsksAllowsChildren(true);
        return model;
    }

    public void onResizeSynth() {
        int width;
        width = (int) jScrollPaneSynthContainer.getViewport().getViewRect().getWidth();
        jScrollPaneSynthContainer.getViewport().setMinimumSize(new Dimension(200, 100));
        Dimension parent = getParent().getSize();
        jPanelSynths.setMaximumSize(new Dimension(width, 20000));
        for (int i = 0; i < MX80Process.getInstance()._listInstrumentPanel.size(); ++i) {
            VSTInstancePanel vstPanel = MX80Process.getInstance()._listInstrumentPanel.get(i);
            vstPanel.onResize(width);
        }
        width = (int) ((jPanelEffectsContainer.getSize().getWidth() / 2) - 2);
        for (int i = 0; i < MX80Process.getInstance()._listEffect.size(); ++i) {
            VSTInstancePanel vstPanel = MX80Process.getInstance()._listEffectPanel.get(i);
            vstPanel.onResize(width);
        }
    }
}
