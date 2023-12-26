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
package jp.synthtarou.midimixer.libs.swing.folderbrowser;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.swing.MXModalFrame;
import jp.synthtarou.midimixer.libs.navigator.legacy.INavigator;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXSwingFolderBrowser extends javax.swing.JPanel implements INavigator<FileList> {

    static final int DISKDRIVE_DEPTH = 20;
    /*
     * DEBUG = true
     * RELESAE = false
     */
    static boolean _disableThreading = false;

    @Override
    public int getNavigatorType() {
        return INavigator.TYPE_SELECTOR;
    }

    @Override
    public boolean isNavigatorRemovable() {
        return false;
    }

    @Override
    public boolean validateWithNavigator(FileList result) {
        return true;
    }

    FileList _returnValue = null;
    int _returnStatus = INavigator.RETURN_STATUS_NOTSET;

    @Override
    public int getReturnStatus() {
        return _returnStatus;
    }

    @Override
    public FileList getReturnValue() {
        return _returnValue;
    }

    class Alive {

        Throwable e = new Throwable();
        long tick = System.currentTimeMillis();

        public long distance() {
            return System.currentTimeMillis() - tick;
        }

        public void dump() {
            e.printStackTrace();
        }
    }

    HashMap<Thread, Alive> _test = new HashMap();

    abstract class Runnable2 {

        public Runnable2() {
        }

        public final void launchStay(FileSystemCache.Element file) {
            try {
                process(file);
            } catch (Throwable e) {
                e.printStackTrace();;
            } finally {
            }
        }

        public final void launchProcess(FileSystemCache.Element file) {
            if (_disableThreading) {
                launchStay(file);
            } else {
                Runnable run = new Runnable() {
                    public void run() {
                        try {
                            launchStay(file);
                        } finally {
                            synchronized (_test) {
                                Collection<Alive> seek = _test.values();
                                for (Alive a : seek) {
                                    if (a.distance() >= 100000) {
                                        //System.out.println("Distance " + a.distance());
                                        //a.dump();
                                        //_test.remove(a);
                                    }
                                }
                            }
                        }
                    }
                };
                if (_disableThreading) {
                    run.run();
                } else {
                    Thread th = new Thread(run);
                    th.setPriority(Thread.MIN_PRIORITY);
                    synchronized (_test) {
                        _test.put(th, new Alive());
                    }
                    th.start();
                }
            }
        }

        public final void launchUIThread(FileSystemCache.Element file) {
            if (SwingUtilities.isEventDispatchThread()) {
                process(file);
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            process(file);
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        protected abstract void process(FileSystemCache.Element file);
    }

    DefaultMutableTreeNode _networkNode = null;

    DefaultTreeModel _model;
    FileSystemCache.Element[] _selection;

    FileFilter _filterOpenable = null;
    FileFilter _filterVisible = null;

    ChildSeekFilter _deepScan = null;

    public static void main(String[] args) {

        MXSwingFolderBrowser c0 = new MXSwingFolderBrowser(new File("C:/Program Files/Common Files"), null);
        MXModalFrame.showAsDialog(null, c0, "Standard");

        FileFilter filterxml = new FileFilterListExt(new String[]{".xml"});
        MXSwingFolderBrowser c1 = new MXSwingFolderBrowser(new File("C:/Domino144/Module"), filterxml);
        MXModalFrame.showAsDialog(null, c1, "Domino XML");

        FileFilter filtervst = new FileFilterListExt(new String[]{".vst3"});
        MXSwingFolderBrowser c2 = new MXSwingFolderBrowser(null, filtervst);
        MXModalFrame.showAsDialog(null, c2, "VST3");

        FileFilter filtermid = new FileFilterListExt(new String[]{".mid"});
        MXSwingFolderBrowser c3 = new MXSwingFolderBrowser(new File("C:/"), filtermid);
        MXModalFrame.showAsDialog(null, c3, "MID File");

        System.exit(0);
    }

    /**
     *
     * @param initialDir
     * @param filterOpenable
     */
    public MXSwingFolderBrowser(File initialDir, FileFilter filterOpenable) {
        this(initialDir, filterOpenable, null);
    }

    boolean _init = true;
    File _initialDir = null;

    /**
     *
     * @param initialDir
     * @param filterOpenable
     * @param filterVisible
     */
    public MXSwingFolderBrowser(File initialDir, FileFilter filterOpenable, FileFilter filterVisible) {
        initComponents();

        _filterOpenable = filterOpenable;
        _filterVisible = filterVisible;

        FileSystemCache.Element root = _cache.addCache(null);
        _model = new DefaultTreeModel(root._pairNode, true);
        fillSubDirectory(root);

        jTree1.setCellRenderer(new MyRenderer());
        jTree1.setRootVisible(false);
        jTree1.setModel(_model);

        jTree1.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (_init) {
                    return;
                }
                viewSelection();
            }
        });

        jTree1.setRequestFocusEnabled(true);
        jTree1.requestFocusInWindow();

        _initialDir = initialDir;
        InitialRun init = new InitialRun();

        init.launchUIThread(_cache.addCache(null));

        setPreferredSize(new Dimension(600, 400));
    }

    class DigRun extends Runnable2 {

        @Override
        public void process(FileSystemCache.Element initialDir) {
            LinkedList<File> seekPath = new LinkedList();
            File seek = _initialDir;
            if (seek == null) {
                return;
            }
            seekPath.add(seek);
            while (true) {
                File parent = seek.getParentFile();
                if (parent == null) {
                    break;
                }
                seek = parent;
                seekPath.add(seek);

            }

            FileSystemCache.Element lastParent = null;
            //System.out.println("Path To Build " + seekPath);

            while (seekPath.isEmpty() == false) {
                File file = seekPath.removeLast();
                FileSystemCache.Element node = _cache.getCache1(file);

                if (node == null) {
                    if (lastParent != null) {
                        fillSubDirectory(lastParent);
                        lastParent = _cache.getCache1(file);

                        if (lastParent == null) {
                            System.out.println("Wrong Directory Seek");
                            return;
                        }
                    } else {
                        FileSystemCache.Element root = _cache.getRoot1();
                        fillSubDirectory(root);
                        node = _cache.getCache1((File) null);
                    }
                } else {
                    lastParent = node;
                }
            }
            if (lastParent != null) {
                ensureNodeToVisible(lastParent);
            }
        }
    }

    class StepRun extends Runnable2 {

        @Override
        public void process(FileSystemCache.Element file) {
            if (_deepScan != null) {
                _deepScan.noticeResetCancel();
            }
            jProgressBar1.setIndeterminate(true);
            FileSystemCache.Element element = _cache.getCache1(file._fileObject);
            fillSubDirectory(element);
            jProgressBar1.setIndeterminate(false);
        }
    }

    class InitialRun extends Runnable2 {

        int forceCount = 3;

        public boolean isProcessDrivesUnder(FileSystemCache.Element itsPC) {
            boolean hasDrive = false;
            File[] nest = null;
            try {
                if (itsPC._fileObject.isDirectory()) {
                    if (isCancelReasonNetwork(itsPC._fileObject)) {
                        return false;
                    }
                    nest = itsPC._fileObject.listFiles();
                }
            } catch (Throwable ex) {
                new IOException("**************************", ex).printStackTrace();
            }
            if (nest == null) {
                return false;
            }
            for (File disk : nest) {
                if (FileSystemCache.isHomeDrive(disk)) {
                    hasDrive = true;
                }
            }
            if (hasDrive) {
                int z = 0;
                fillSubDirectory(itsPC);
                for (File disk : nest) {
                    if (FileSystemCache.isHomeDrive(disk)) {
                        System.out.println("disk node " + disk);
                        fillSubDirectory(_cache.addCache(disk));
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public void process(FileSystemCache.Element parentFile) {
            fillSubDirectory(parentFile);
            File[] files = null;
            if (parentFile._fileObject == null) {
                files = FileSystemCache.getRoots();
            } else {
                if (isCancelReasonNetwork(parentFile._fileObject)) {
                    return;
                }

                files = parentFile._fileObject.listFiles();
                if (files == null) {
                    return;
                }

            }
            for (File file : files) {
                if (forceCount >= 0) {
                    forceCount--;
                    System.out.println("Force thru " + file);
                } else {
                    if (FileSystemCache.isUniqueNamed(file) == false) {
                        continue;
                    }
                }
                if (isCancelReasonNetwork(file)) {
                    continue;
                }

                FileSystemCache.Element isPC = _cache.addCache(file);
                fillSubDirectory(isPC);

                if (isProcessDrivesUnder(isPC)) {
                    _init = false;
                    jTree1.updateUI();
                    FileSystemCache.Element e = _cache.getCache1(_initialDir);
                    DigRun dig = new DigRun();
                    dig.launchProcess(null);

                    return;
                } else {
                    process(isPC);
                }
            }
        }
    }

    TreeSet<File> _alreadySeek = new TreeSet();
    boolean _alreaydNull = false;

    public void fillSubDirectory(FileSystemCache.Element parent) {
        File[] listFiles = null;

        File f = parent._fileObject;

        if (f == null) {
            if (_alreaydNull) {
                return;
            }
            _alreaydNull = true;
        } else {
            synchronized (_alreadySeek) {
                if (_alreadySeek.contains(f)) {
                    return;
                }
                _alreadySeek.add(parent._fileObject);
            }
        }
        if (f == null) {
            listFiles = FileSystemCache.getRoots();
        } else {
            if (isCancelReasonNetwork(f)) {
                return;
            }
            listFiles = f.listFiles();
        }

        if (listFiles == null) {
            return;
        }
        for (File seek : listFiles) {
            if (jCheckBoxNested.isSelected()) {
                if (_deepScan == null) {
                    _deepScan = new ChildSeekFilter(this);
                    Window w = MXUtil.getOwnerWindow(this);
                    w.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            System.out.println(".windowClosing()");
                            _deepScan.noticeCancelScan();
                        }

                        @Override
                        public void windowClosed(WindowEvent e) {
                            System.out.println(".windowClosed()");
                            _deepScan.noticeCancelScan();
                        }
                    });
                }
                boolean flag = _deepScan.accept(seek);
                if (_deepScan.isCanceled()) {
                    return;
                }
                if (flag == false) {
                    continue;
                }
            } else {
                if (isVisibleFile(seek) == false) {
                    continue;
                }
            }
            synchronized (_model) {
                FileSystemCache.Element element = _cache.addCache(seek);
                if (parent._pairNode == null) {
                    DefaultMutableTreeNode root = (DefaultMutableTreeNode) _model.getRoot();
                    _model.insertNodeInto(element._pairNode, root, root.getChildCount());
                } else {
                    _model.insertNodeInto(element._pairNode, parent._pairNode, parent._pairNode.getChildCount());
                }
            }
        }
    }

    FileSystemCache _cache = new FileSystemCache();

    public void viewSelection() {
        TreePath[] list = jTree1.getSelectionPaths();
        if (list != null) {
            ArrayList<FileSystemCache.Element> result = new ArrayList<>();

            for (TreePath path : list) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                FileSystemCache.Element element = (FileSystemCache.Element) node.getUserObject();
                result.add(element);
            }

            if (result.size() > 0) {
                _selection = new FileSystemCache.Element[result.size()];
                result.toArray(_selection);
            } else {
                _selection = null;
            }
            if (_selection == null) {
                jLabelSelection.setText("-");
            } else {
                jLabelSelection.setText(result.toString());
            }
        }
    }

    public LinkedList<File> separateDirectory(File file) {
        LinkedList<File> path = new LinkedList<>();
        while (file.exists() == false) {
            file = file.getParentFile();
            if (file == null) {
                return path;
            }
        }
        path.add(0, file);
        while (true) {
            File parent = file.getParentFile();
            if (parent == null) {
                break;
            }
            path.addFirst(parent);
            file = parent;
        }
        return path;
    }

    public void ensureNodeToVisible(FileSystemCache.Element node) {
        if (node == null) {
            throw new NullPointerException("NULLPO");
        }
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ensureNodeToVisible(node);
                }
            });
            return;
        }
        TreePath path = new TreePath(node._pairNode.getPath());
        jTree1.expandPath(path);
        jTree1.setSelectionPath(path);

        jTree1.scrollPathToVisible(path);
        jTree1.paintImmediately(jTree1.getVisibleRect());
    }

    public boolean isCancelReasonNetwork(File f) {
        if (f == null) {
            return false;
        }
        if (f.getPath().startsWith("::")) {
            if (f.toString().equals("ネットワーク")
                    || f.toString().equalsIgnoreCase("network")) {
                if (jCheckBoxNetwork.isSelected() == false) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isVisibleFile(File target) {
        if (_filterVisible != null) {
            if (_filterVisible.accept(target) == false) {
                if (isOpenableFile(target)) {
                    return true;
                }
                return false;
            }
        } else {
            return isOpenableFile(target);
        }

        return true;
    }

    public boolean isOpenableFile(File target) {
        if (_filterOpenable != null) {
            return _filterOpenable.accept(target);
        }

        return true;
    }

    @Override
    public JPanel getNavigatorPanel() {
        return this;
    }

    public void progress(String text) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress(text);
                }
            });
            return;
        }
        jLabelScan.setText(text);
        jLabelScan.paintImmediately(jLabelScan.getVisibleRect());
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

        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jLabelSelection = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jCheckBoxNetwork = new javax.swing.JCheckBox();
        jCheckBoxNested = new javax.swing.JCheckBox();
        jLabelScan = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jToggleButton1 = new javax.swing.JToggleButton();

        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });
        setLayout(new java.awt.GridBagLayout());

        jButtonOK.setText("Enter");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButtonOK, gridBagConstraints);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButtonCancel, gridBagConstraints);

        jLabelSelection.setText("Please Select 1 From Tree");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jLabelSelection, gridBagConstraints);

        jTree1.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
            }
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                jTree1TreeWillExpand(evt);
            }
        });
        jTree1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTree1KeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTree1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jCheckBoxNetwork.setText("Unlock Network Access");
        jCheckBoxNetwork.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNetworkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jCheckBoxNetwork, gridBagConstraints);

        jCheckBoxNested.setText("Scan for Skip Folder Which Have No Contents");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jCheckBoxNested, gridBagConstraints);

        jLabelScan.setText("Scanning");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jLabelScan, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jProgressBar1, gridBagConstraints);

        jToggleButton1.setText("Pause");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jToggleButton1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        setResultAndClose(true);
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        setResultAndClose(false);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jTree1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTree1KeyPressed
        switch (evt.getExtendedKeyCode()) {
            case KeyEvent.VK_ENTER:
                setResultAndClose(true);
                return;
            case KeyEvent.VK_ESCAPE:
                setResultAndClose(false);
                return;
        }
    }//GEN-LAST:event_jTree1KeyPressed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        switch (evt.getExtendedKeyCode()) {
            case KeyEvent.VK_ENTER:
                setResultAndClose(true);
                return;
            case KeyEvent.VK_ESCAPE:
                setResultAndClose(false);
                return;
        }
    }//GEN-LAST:event_formKeyPressed

    private void jCheckBoxNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNetworkActionPerformed
        if (_networkNode != null) {
            new StepRun().launchProcess((FileSystemCache.Element) _networkNode.getUserObject());
        }
    }//GEN-LAST:event_jCheckBoxNetworkActionPerformed

    private void jTree1TreeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {//GEN-FIRST:event_jTree1TreeWillExpand
        if (_init) {
            return;
        }
        TreePath path = evt.getPath();
        DefaultMutableTreeNode last = (DefaultMutableTreeNode) path.getLastPathComponent();

        jToggleButton1.setSelected(false);
        if (_deepScan != null) {
            _deepScan.pause(false);
        }
        new StepRun().launchProcess((FileSystemCache.Element) last.getUserObject());

    }//GEN-LAST:event_jTree1TreeWillExpand

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        if (_deepScan != null) {
            _deepScan.pause(jToggleButton1.isSelected());
        }
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    public void setResultAndClose(boolean accept) {
        if (accept == false) {
            _returnStatus = INavigator.RETURN_STATUS_CANCELED;
            MXUtil.getOwnerWindow(this).setVisible(false);
            return;
        }
        ArrayList<File> safe = new ArrayList<File>();
        ArrayList<File> error = new ArrayList<File>();
        if (_selection == null) {
            JOptionPane.showMessageDialog(this, "Please, Select Files", "Notice", JOptionPane.OK_OPTION);
            return;
        }
        for (FileSystemCache.Element temp : _selection) {
            if (temp == null) {
                continue;
            }
            if (isOpenableFile(temp._fileObject)) {
                safe.add(temp._fileObject);
            } else {
                error.add(temp._fileObject);
            }
        }
        if (error.size() > 0) {
            JOptionPane.showMessageDialog(this, "Can't open " + error, "Notice", JOptionPane.OK_OPTION);
            return;
        }
        if (safe.size() == 0) {
            JOptionPane.showMessageDialog(this, "Not selected", "Notice", JOptionPane.OK_OPTION);
            return;
        }

        _returnStatus = INavigator.RETURN_STATUS_APPROVED; 
        _returnValue = new FileList(safe);
        MXUtil.getOwnerWindow(this).setVisible(false);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JCheckBox jCheckBoxNested;
    private javax.swing.JCheckBox jCheckBoxNetwork;
    private javax.swing.JLabel jLabelScan;
    private javax.swing.JLabel jLabelSelection;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

}
