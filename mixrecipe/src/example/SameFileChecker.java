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
package example;

import example.SameFileChecker.Scanner.UserObject;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import jp.synthtarou.midimixer.MXThread;
import jp.synthtarou.midimixer.libs.common.MXLogger2;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.navigator.legacy.INavigator;
import jp.synthtarou.midimixer.libs.navigator.legacy.NavigatorForText;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileList;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.MXFolderBrowser;

/**
 * 重複するファイルをリスト表示する
 *
 * @author Syntarou YOSHIDA
 */
public class SameFileChecker extends javax.swing.JPanel {

    /**
     * Mainメソッド
     *
     * @param args 使用しない
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Same FileChecker");
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        SameFileChecker checker = new SameFileChecker();
        frame.getContentPane().add(checker);
        frame.setPreferredSize(new Dimension(600, 400));
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                checker._cancel = true;
                System.exit(0);
            }
        });
        frame.setSize(new Dimension(600, 400));
        frame.setVisible(true);
    }

    static String CUSTOM_FIELD = "[Custom]";
    
    String[] _defaultSkip = {
        "C:\\ProgramData",
        "C:\\Windows\\servicing",
        "C:\\Windows\\WinSxS",
        "C:\\Windows\\Installer"
    };
    /**
     * Creates new form SameFileChecker
     */
    public SameFileChecker() {
        initComponents();
        String[] suffix = {
            "jpg, jpeg, bmp, png, gif"
            ,"mov, avi, mp4, heic"
            ,"aac, mp3, wav"
            ,"zip, cab, gz, exe, jar"
            ,"txt, doc, pdf, ppt, pptx"
            ,"xls, xlsx, csv"
            ,CUSTOM_FIELD
        };
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (String seek  : suffix) {
            model.addElement(seek);
        }
        jComboBoxFileType.setModel(model);
        jComboBoxFileType.setSelectedIndex(0);
            
        jTextAreaSkip.setText(String.join("\n", _defaultSkip));
        autoAdjust();
    }
    
    protected void autoAdjust() {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    autoAdjust();;
                }
            });
            return;
        }
        try {
            jSplitPane1.setResizeWeight(1);
            jSplitPane1.setDividerLocation(jSplitPane1.getHeight() - 150);
        }catch(Exception ex){
            ex.printStackTrace();;
        }
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
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaSkip = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jTextFieldRootFolder = new javax.swing.JTextField();
        jButtonBrowse = new javax.swing.JButton();
        jButtonScan = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jButtonShow = new javax.swing.JButton();
        jComboBoxFileType = new javax.swing.JComboBox<>();
        jLabelFileType = new javax.swing.JLabel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Folder to Skip"));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        jTextAreaSkip.setColumns(20);
        jTextAreaSkip.setRows(5);
        jScrollPane1.setViewportView(jTextAreaSkip);

        jPanel1.add(jScrollPane1);

        jSplitPane1.setRightComponent(jPanel1);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jTextFieldRootFolder.setText("C:\\");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            jPanel2.add(jTextFieldRootFolder, gridBagConstraints);

            jButtonBrowse.setText("Browse");
            jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonBrowseActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            jPanel2.add(jButtonBrowse, gridBagConstraints);

            jButtonScan.setText("StartScan");
            jButtonScan.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonScanActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            jPanel2.add(jButtonScan, gridBagConstraints);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 9;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            jPanel2.add(jProgressBar1, gridBagConstraints);

            jLabel1.setText("100/100");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 10;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            jPanel2.add(jLabel1, gridBagConstraints);

            jScrollPane2.setViewportView(jList1);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridheight = 4;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            jPanel2.add(jScrollPane2, gridBagConstraints);

            jButtonShow.setText("Show");
            jButtonShow.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonShowActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
            jPanel2.add(jButtonShow, gridBagConstraints);

            jComboBoxFileType.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jComboBoxFileTypeActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
            gridBagConstraints.weighty = 1.0;
            jPanel2.add(jComboBoxFileType, gridBagConstraints);

            jLabelFileType.setText("File Type");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 3;
            jPanel2.add(jLabelFileType, gridBagConstraints);

            jSplitPane1.setLeftComponent(jPanel2);

            add(jSplitPane1);
        }// </editor-fold>//GEN-END:initComponents

    /**
     * ディレクトリのみブラウズするFileFilter
     */
    static class FolderOnlyFilter implements FileFilter {

        @Override
        public boolean accept(File f) {
            return f.isDirectory();
        }
    }

    /**
     * Browseボタンのアクション
     *
     * @param evt
     */
    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        File file = null;
        try {
            file = new File(jTextFieldRootFolder.getText());
            file = file.getCanonicalFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        MXFolderBrowser folder = new MXFolderBrowser(file, new FolderOnlyFilter());
        MXUtil.showAsDialog(this, folder, "FolderBrowser");
        if (folder.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
            FileList list = folder.getReturnValue();
            if (list.isEmpty() == false) {
                try {
                    jTextFieldRootFolder.setText(list.get(0).getCanonicalPath());
                } catch (IOException ioe) {
                    MXLogger2.getLogger(SameFileChecker.class).log(Level.SEVERE, ioe.getMessage(), ioe);
                }
            }
        }
    }//GEN-LAST:event_jButtonBrowseActionPerformed

    DefaultListModel<UserObject> _model;

    long dispCounter;

    MXThread _thread = null;
    boolean _cancel = false;
    String[] _suffix = null;
    String[] _skipFolder = null;

    private void jButtonScanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonScanActionPerformed
        if (_thread != null) {
            _cancel = true;
            _thread = null;
            return;
        }
        _suffix = null;
        String suffix = (String)jComboBoxFileType.getSelectedItem();
        if (suffix != null && suffix.length() > 0) {
            ArrayList<String> temp = new ArrayList<>();
            String[] sp = suffix.split(",");
            for (String seek : sp) {
                seek = seek.trim();
                temp.add("." + seek);
            }
            _suffix = new String[temp.size()];
            temp.toArray(_suffix);
        }
        _skipFolder = null;
        String skip = jTextAreaSkip.getText();
        if (skip.length() > 0) {
            ArrayList<String> temp = new ArrayList<>();
            String[] sp = skip.split("\n");
            for (String seek : sp) {
                seek = seek.trim();
                temp.add(seek);
            }
            _skipFolder = new String[temp.size()];
            temp.toArray(_skipFolder);
        }
        
        File file = new File(jTextFieldRootFolder.getText());
        if (file.isDirectory()) {
            _thread = new MXThread("FolderScanner", new Runnable() {
                @Override
                public void run() {
                    _cancel = false;
                    _model = new DefaultListModel<>();
                    jList1.setModel(_model);
                    jButtonScan.setText("Stop");
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            jProgressBar1.setIndeterminate(true);
                        }
                    });
                    try {
                        Scanner scanner = new Scanner(new Scanner.Callback() {
                            @Override
                            public boolean progress(long seeked, long queue, long hit, String message) {
                                long tick = System.currentTimeMillis();
                                if (tick - dispCounter >= 500) {
                                    dispCounter = tick;
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            jLabel1.setText("seeked: " + seeked + ", hit: " + hit + ", remain: " + queue  + ", seeking: "+ message);
                                        }
                                    });
                                }
                                return _cancel == false;
                            }
                        });
                        scanner.scan(file);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    } finally {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                jProgressBar1.setIndeterminate(false);
                                jLabel1.setText("Done");
                            }
                        });
                        _thread = null;
                        jButtonScan.setText("Scan");
                    }
                }
            });
            _thread.start();
        } else {
            JOptionPane.showMessageDialog(this, "file not found " + jTextFieldRootFolder.getText(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonScanActionPerformed

    private void jButtonShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonShowActionPerformed
        int x = jList1.getSelectedIndex();
        if (x >= 0) {
            UserObject o = _model.getElementAt(x);
            if (o != null) {
                try{ 
                    Desktop.getDesktop().open(new File(o._path).getParentFile());   
                }catch(IOException ex) {
                    ex.printStackTrace();;
                }
            }
        }
    }//GEN-LAST:event_jButtonShowActionPerformed

    private void jComboBoxFileTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFileTypeActionPerformed
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel)jComboBoxFileType.getModel();
        String selected = (String)model.getSelectedItem();
        if (selected.equals(CUSTOM_FIELD)) {
            NavigatorForText text = new NavigatorForText("Input New Type (, seprated)");
            MXUtil.showAsDialog(jComboBoxFileType, text, "Input New Type");
            if (text.getReturnStatus() == INavigator.RETURN_STATUS_APPROVED) {
                model.addElement(text.getReturnValue());
                jComboBoxFileType.setSelectedIndex(model.getSize() - 1);
            }
        }
    }//GEN-LAST:event_jComboBoxFileTypeActionPerformed

    /**
     * フォルダースキャナ
     */
    public class Scanner {

        /**
         * @param file
         * @param size
         */
        public void addIfPicture(File file) {
            boolean ok = false;
            if (_suffix == null) {
                ok = true;
            }else {
                String name = file.getName().toLowerCase();
                
                for (String x : _suffix) {
                    if (name.endsWith(x)) {
                        ok = true;
                        break;
                    }
                }
            }
            if (ok) {
                Entry e = new Entry(file);
                long length = file.length();
                ArrayList<Entry> list = _all.get(length);
                if (list == null) {
                    list = new ArrayList();
                    _all.put(length, list);
                }
                for (int i = 0; i < list.size(); ++i) {
                    Entry e2 = list.get(i);
                    if (e.contentsSame(e2)) {
                        makePair(e, e2);
                        _added++;
                    }
                }
                list.add(e);
            }
        }

        TreeMap<Long, ArrayList<Entry>> _all = new TreeMap();
        long _added = 0;

        static class Entry {

            Entry(File file) {
                _file = file;
                _size = file.length();
                _hash = -1;
            }

            public String toString() {
                return _file.toString();
            }

            public boolean equals(Object e) {
                if (e instanceof Entry) {
                    Entry e2 = (Entry) e;
                    return _file.toString().equals(e2._file.toString());
                }
                return false;
            }

            long calcHash() {
                if (_hash >= 0) {
                    return _hash;
                }
                long x = 0;
                InputStream in = null;
                try {
                    in = new BufferedInputStream(new FileInputStream(_file));
                    try {
                        int z = in.read();
                        x = x + z;
                    } catch (EOFException eof) {
                        _hash = x & 0x7fffffff;
                        return _hash;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                _hash = x & 0x7fffffff;
                return _hash;
            }

            boolean contentsSame(Entry e2) {
                if (_size == e2._size) {
                    if (calcHash() == e2.calcHash()) {
                        FileInputStream f1 = null, f2 = null;
                        try {
                            f1 = new FileInputStream(_file);
                            f2 = new FileInputStream(e2._file);
                            byte[] data1 = new byte[4096];
                            byte[] data2 = new byte[4096];
                            while (true) {
                                int len1 = 0;
                                try {
                                    len1 = f1.read(data1, 0, 4096);
                                    if (len1 < 0) {
                                        len1 = 0;
                                    }
                                } catch (EOFException ex) {
                                    len1 = 0;
                                }
                                int len2 = 0;
                                try {
                                    len2 = f2.read(data2, 0, 4096);
                                    if (len2 < 0) {
                                        len2 = 0;
                                    }
                                } catch (EOFException ex) {
                                    len2 = 0;
                                }
                                if (len1 != len2) {
                                    return false;
                                }
                                if (len1 == 0) {
                                    break;
                                }
                                for (int x = 0; x < len1; ++x) {
                                    if (data1[x] != data2[x]) {
                                        return false;
                                    }
                                }
                            }
                            return true;
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        } finally {
                            try {
                                if (f1 != null) {
                                    f1.close();
                                }
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                            try {
                                if (f2 != null) {
                                    f2.close();
                                }
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                        }
                    }
                }
                return false;
            }

            File _file;
            long _size;
            long _hash;
        }

        /**
         * コールバック
         */
        public interface Callback {

            public boolean progress(long seeked, long queue, long added, String message);
        }

        /**
         * コンストラクタ
         *
         * @param callback Callbackインターフェース
         */
        public Scanner(Callback callback) {
            _callback = callback;
        }

        Callback _callback;
        long _seeked;

        /**
         * スキャン開始メソッド
         *
         * @param file 対象フォルダ
         * @throws InterruptedException 中断された場合の例外
         */
        public void scan(File file) throws InterruptedException {
            ArrayList<File> queue = new ArrayList<>();
            queue.add(file);
            if (_callback.progress(_seeked, queue.size(), _added, file.toString()) == false) {
                throw new InterruptedException();
            }
            while (!queue.isEmpty()) {
                File f = queue.removeLast();
                String path = f.getPath();
                boolean skip = false;
                for (String seek : _skipFolder) {
                    if (seek.equalsIgnoreCase(path)) {
                        skip = true;
                        break;
                    }
                }
                if (skip) {
                    continue;
                }
                _seeked ++;
                if (_callback.progress(_seeked, queue.size(), _added, f.toString()) == false) {
                    throw new InterruptedException();
                }
                File[] children = f.listFiles();
                if (children == null) {
                    continue;
                }
                jList1.setValueIsAdjusting(true);
                for (File seek : children) {
                    if (seek.isDirectory()) {
                        queue.add(seek);
                    } else {
                        addIfPicture(seek);
                    }
                }
                jList1.setValueIsAdjusting(false);
            }
        }

        class UserObject {

            long _length;
            String _name;
            String _path;
            boolean _isFolder;

            UserObject(File f) {
                this(false, f);
            }

            UserObject(boolean isFolder, File f) {
                _isFolder = isFolder;
                _length = f.length();
                _name = f.getName();
                _path = f.getPath();
            }

            public String toString() {
                if (_isFolder) {
                    return _length + " : " + _name;
                }
                return "     " + _path;
            }
        }

        class UserFolder extends UserObject {

            UserFolder(Entry e1, Entry e2) {
                super(true, e1._file);

                int insertTo = -1;
                for (int i = 0; i < _model.size(); ++i) {
                    UserObject o = _model.elementAt(i);
                    if (o instanceof UserFolder) {
                        if (o._length > e1._size) {
                            insertTo = i;
                            break;
                        }
                    }
                }
                if (insertTo < 0) {
                    _model.add(_model.size(), this);
                } else {
                    _model.add(insertTo, this);
                }

                add(e1);
                add(e2);
            }

            void add(Entry e) {
                _list.add(e);

                for (int i = 0; i < _model.size(); ++i) {
                    UserObject o = _model.elementAt(i);
                    if (o instanceof UserFolder) {
                        if (o == this) {
                            _model.add(i + 1, new UserObject(e._file));
                            return;
                        }
                    }
                }
            }

            ArrayList<Entry> _list = new ArrayList<>();
        }

        List<UserFolder> _folders = new ArrayList<>();

        public void makePair(Entry e1, Entry e2) {

            if (SwingUtilities.isEventDispatchThread() == false) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        makePair(e1, e2);
                    }
                });
                return;
            }

            for (UserFolder p : _folders) {
                if (p._list.contains(e1)) {
                    if (!p._list.contains(e2)) {
                        p.add(e2);
                    }
                    return;
                } else if (p._list.contains(e2)) {
                    if (!p._list.contains(e1)) {
                        p.add(e1);
                    }
                    return;
                }
            }
            UserFolder p = new UserFolder(e1, e2);
            _folders.add(p);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JButton jButtonScan;
    private javax.swing.JButton jButtonShow;
    private javax.swing.JComboBox<String> jComboBoxFileType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelFileType;
    private javax.swing.JList<UserObject> jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextAreaSkip;
    private javax.swing.JTextField jTextFieldRootFolder;
    // End of variables declaration//GEN-END:variables
}
