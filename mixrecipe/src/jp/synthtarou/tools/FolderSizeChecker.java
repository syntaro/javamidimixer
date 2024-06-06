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
package jp.synthtarou.tools;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import jp.synthtarou.libs.MXSafeThread;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.navigator.legacy.INavigator;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileList;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.MXFolderBrowser;

/**
 * フォルダの容量をサブフォルダ含めツリー表示する
 * @author Syntarou YOSHIDA
 */
public class FolderSizeChecker extends javax.swing.JPanel {

    /**
     * Mainメソッド
     * @param args 使用しない
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Folder Size Checker");
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        FolderSizeChecker checker = new FolderSizeChecker();
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

    /**
     * Creates new form FolderSizeChecker
     */
    public FolderSizeChecker() {
        initComponents();
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

        jTextFieldRootFolder = new javax.swing.JTextField();
        jButtonBrowse = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTreeFiles = new javax.swing.JTree();
        jButtonScan = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jTextFieldRootFolder.setText("C:\\");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            add(jTextFieldRootFolder, gridBagConstraints);

            jButtonBrowse.setText("Browse");
            jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonBrowseActionPerformed(evt);
                }
            });
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            add(jButtonBrowse, gridBagConstraints);

            jTreeFiles.setModel(createTreeModel());
            jScrollPane1.setViewportView(jTreeFiles);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            add(jScrollPane1, gridBagConstraints);

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
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
            add(jButtonScan, gridBagConstraints);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            add(jProgressBar1, gridBagConstraints);

            jLabel1.setText("100/100");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            add(jLabel1, gridBagConstraints);
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
     * @param evt 
     */
    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        File file = null;
        try {
            file = new File(jTextFieldRootFolder.getText());
            file = file.getCanonicalFile();
        }catch(IOException ex) {
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
                    MXFileLogger.getLogger(FolderSizeChecker.class).log(Level.SEVERE, ioe.getMessage(), ioe);
                }
            }
        }
    }//GEN-LAST:event_jButtonBrowseActionPerformed

    DefaultTreeModel _model = null;

    /**
     * DefaultMutableTreeNodeのカスタムしたUser Object
     */
    static class UserObjectForFolder {

        String _name;
        long _size;

        public UserObjectForFolder(String name, long folderSize) {
            _name = name;
            _size = folderSize;
        }

        public String byteFormat(long b) {
            if (b >= 1000) {
                long k = b / 1000;
                b = b % 1000;
                if (k >= 1000) {
                    long m = k / 1000;
                    k = k % 1000;
                    if (m >= 1000) {
                        long g = m / 1000;
                        m = m % 1000;
                        return g + "G " + m + "M " + k + "k " + b;
                    }
                    return m + "M " + k + "k " + b;
                }
                return k + "k " + b;
            }
            return Long.toString(b);
        }

        public String toString() {
            String name = _name;
            if (name.equals(".")) {
                name = "[files]";
            }
            if (_size < 0) {
                return name + "(?)";
            } else {
                return name + "(" + byteFormat(_size) + ")";
            }
        }
    }

    /**
     * ツリーのモデルを生成する
     * @return
     */
    public TreeModel createTreeModel() {
        return new DefaultTreeModel(new DefaultMutableTreeNode(null));
    }

    long dispCounter;

    /**
     * Treeにファイルを追加する、UIスレッドから呼ぶ必要がある
     * @param file
     * @param size
     */
    public void addToTree(File file, long size) {
        Path path = file.toPath();
        int z = path.getNameCount();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) _model.getRoot();
        DefaultMutableTreeNode seek = root;
        DefaultMutableTreeNode foundLast = null;
        
        long tick = System.currentTimeMillis();
        if (tick - dispCounter >= 500) {
            dispCounter = tick;
            jLabel1.setText(file.toString());
        }
        for (int i = 0; i < z; ++i) {
            String name = path.getName(i).toString();
            DefaultMutableTreeNode found = null;

            for (int x = 0; x < seek.getChildCount(); ++x) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) seek.getChildAt(x);
                UserObjectForFolder user = (UserObjectForFolder) node.getUserObject();
                if (user._name.equalsIgnoreCase(name)) {
                    found = node;
                    break;
                }
            }
            if (found == null) {
                UserObjectForFolder folder = new UserObjectForFolder(name, -1);
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(folder);
                seek.add(newNode);
                found = newNode;
            }
            if (i == z - 1) {
                foundLast = found;
                break;
            }
            seek = found;
        }
        if (foundLast != null) {
            UserObjectForFolder folder = (UserObjectForFolder) foundLast.getUserObject();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) foundLast.getParent();
            if (folder._size != size) {
                folder._size = size;
                DefaultMutableTreeNode newNode = cloneTreeNode(foundLast);
                parent.remove(foundLast);
                parent.insert(newNode, findInsertIndex(parent, newNode));
                _model.reload(parent);
                if (_autoExpanded != null && _autoExpanded != seek) {
                    jTreeFiles.collapsePath(new TreePath(_autoExpanded.getPath()));
                }
                _autoExpanded = seek;
                jTreeFiles.expandPath(new TreePath(seek.getPath()));
            }
        }
    }

    /**
     * TreeNodeをクローンする（そのまま付け足すと、崩れるので）
     * @param node 対象とするノード
     * @return クローンされたノード
     */
    DefaultMutableTreeNode cloneTreeNode(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode c = new DefaultMutableTreeNode(node.getUserObject());
        for (int i = 0; i < node.getChildCount(); ++i) {
            c.add(cloneTreeNode((DefaultMutableTreeNode) node.getChildAt(i)));
        }
        return c;
    }

    /**
     *　TreeNodeをソートして挿入する
     * @param parent
     * @param newChild 
     * @return
     */
    public int findInsertIndex(DefaultMutableTreeNode parent, DefaultMutableTreeNode newChild) {
        UserObjectForFolder childFolder = (UserObjectForFolder) newChild.getUserObject();
        for (int i = 0; i < parent.getChildCount(); ++i) {
            DefaultMutableTreeNode seek = (DefaultMutableTreeNode) parent.getChildAt(i);
            UserObjectForFolder seekFolder = (UserObjectForFolder) seek.getUserObject();
            if (seekFolder._size >= childFolder._size) {
                continue;
            } else {
                return i;
            }
        }
        return parent.getChildCount();
    }

    DefaultMutableTreeNode _autoExpanded = null;

    MXSafeThread _thread = null;
    boolean _cancel = false;

    private void jButtonScanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonScanActionPerformed
        if (_thread != null) {
            _cancel = true;
            _thread = null;
            return;
        }
        File file = new File(jTextFieldRootFolder.getText());
        if (file.isDirectory()) {
            _thread = new MXSafeThread("FolderScanner", () -> {
                _cancel = false;
                _model = new DefaultTreeModel(new DefaultMutableTreeNode(null));
                jTreeFiles.setModel(_model);
                jButtonScan.setText("Stop");
                MXMain.invokeUI(() -> {
                    jProgressBar1.setIndeterminate(true);
                });
                try {
                    Scanner scanner = new Scanner(new Scanner.Callback() {
                        @Override
                        public boolean addEntry(File file, long size) {
                            MXMain.invokeUI(() ->  {
                                addToTree(file, size);
                            });
                            return _cancel == false;
                        }
                    });
                    scanner.scan(file);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                } finally {
                    MXMain.invokeUI(() ->  {
                        jProgressBar1.setIndeterminate(false);
                        _thread = null;
                        jButtonScan.setText("Scan");
                    });
                }
            });
            _thread.start();
        } else {
            JOptionPane.showMessageDialog(this, "file not found " + jTextFieldRootFolder.getText(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonScanActionPerformed

    /**
     * フォルダースキャナ
     */
    public static class Scanner {

        /**
         * コールバック
         */
        public interface Callback {

            /**
             *
             * @param file
             * @param size
             * @return
             */
            public boolean addEntry(File file, long size);
        }

        /**
         * コンストラクタ
         * @param callback Callbackインターフェース
         */
        public Scanner(Callback callback) {
            _callback = callback;
        }

        Callback _callback;

        /**
         * フォルダサイズを再帰的に算出する
         * 
         * callbackは、フォルダ見つかったときに、size=-1で呼ばれて、
         * サイズが確定したときに、sizeを入れて呼びなおされる
         * 
         * @param file 検索したいディレクトリ
         * @return ディレクトリのサイズ
         * @throws InterruptedException callbackがfalseを返して場合、Interruptとしているが・・？
         */
        long folderSize(File file) throws InterruptedException {
            String path = file.getPath();
            if (path.equalsIgnoreCase("C:\\Windows\\servicing")) {
                return 0;
            }
            if (path.equalsIgnoreCase("C:\\Windows\\WinSxS")) {
                return 0;
            }
            _callback.addEntry(file, -1);
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                if (children == null) {
                    return 0;
                }
                for (File seek : children) {
                    if (seek.isDirectory()) {
                        _callback.addEntry(seek, -1);
                    }
                }
                long sumDir = 0;
                long sumFile = 0;
                for (File seek : children) {
                    try {
                        String str1 = seek.getPath();
                        String str2 = seek.getCanonicalPath();
                        if (str1.equals(str2) == false) {
                            System.out.println(str1 + " <> " + str2);
                            continue;
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    if (seek.isDirectory()) {
                        sumDir += folderSize(seek);
                    }
                    else {
                        //Fileだけど階層がある場合がある
                        sumFile += seek.length();
                    }
                }
                //if (sumFile + sumDir > 0) {
                    _callback.addEntry(new File(file, "."), sumFile);
                //}
                if (_callback.addEntry(file, sumFile + sumDir) == false) {
                    throw new InterruptedException();
                }
                return sumFile + sumDir;
            } else {
                long sum = file.length();
                return sum;
            }
        }

        /**
         *　スキャン開始メソッド
         * @param file 対象フォルダ
         * @throws InterruptedException 中断された場合の例外
         */
        public void scan(File file) throws InterruptedException {
            folderSize(file);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JButton jButtonScan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldRootFolder;
    private javax.swing.JTree jTreeFiles;
    // End of variables declaration//GEN-END:variables
}
