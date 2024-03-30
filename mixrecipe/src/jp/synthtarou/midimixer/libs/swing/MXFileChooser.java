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
package jp.synthtarou.midimixer.libs.swing;

import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import java.io.File;
import java.util.ArrayList;
import javax.swing.filechooser.FileFilter;
import jp.synthtarou.libs.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXFileChooser extends JFileChooser {

    static File currentDir = MXUtil.getAppBaseDirectory().getAbsoluteFile();

    public static File getStartDirectory() {
        return currentDir;
    }
    
    /**
     *
     * @param dir
     */
    public MXFileChooser(File dir) {
        super(dir);
    }

    /**
     *
     */
    public MXFileChooser() {
        this(currentDir);
    }

    public void approveSelection() {
        if (getDialogType() == JFileChooser.OPEN_DIALOG) {
            File[] selectedFilesList = this.getSelectedFiles();
            if (selectedFilesList.length == 0) {
                File selectedFile = this.getSelectedFile();
                if (selectedFile == null) {
                    JOptionPane.showMessageDialog(this, "Please select file.");
                    return;
                }
                selectedFilesList = new File[]{selectedFile};
            }
            ArrayList<File> notFound = new ArrayList();
            for (int i = 0; i < selectedFilesList.length; ++i) {
                if (selectedFilesList[i].exists() == false) {
                    notFound.add(selectedFilesList[i]);
                }
            }
            if (notFound.size() > 0) {
                JOptionPane.showMessageDialog(this, "File not found.\n" + notFound);
                return;
            }
            currentDir = getCurrentDirectory();
        }
        super.approveSelection();
    }

    public static File getExistFileRecursive(File file) {
        while (file != null && file.exists() == false) {
            file = file.getParentFile();
        }
        return file;
    }

    public static File getExistDirectoryRecursive(File file) {
        while (file != null && (file.exists() == false || file.isDirectory() == false)) {
            file = file.getParentFile();
        }
        return file;
    }

    public static void main(String[] args) {
        JFileChooser chooser = new MXFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(null, "Selected file " + chooser.getSelectedFile().getPath());
        }
    }
    
    public void addExtension(String ext, String desc) {
        addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile()) {                    
                    String lower = pathname.getName().toLowerCase();
                    if (lower.endsWith(ext)) {
                        return true;
                    }
                    return false;
                }
                return true;
            }
            @Override
            public String getDescription() {
                return desc;
            }
        });
    }
}
