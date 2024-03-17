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
package jp.synthtarou.midimixer.ccxml.xml;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.navigator.legacy.INavigator;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingNode;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.libs.settings.MXSettingUtil;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileFilterListExt;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.FileList;
import jp.synthtarou.midimixer.libs.swing.folderbrowser.MXSwingFolderBrowser;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CXXMLManager extends MXReceiver<CXXMLManagerPanel> implements MXSettingTarget {

    private static final CXXMLManager _instance = new CXXMLManager();
    private MXSetting _setting;
    
    public static CXXMLManager getInstance() {
        return _instance;
    }

    private CXXMLManager() {
        _setting = new MXSetting("CCXMLManager");
        _setting.setTarget(this);
        _setting.readSettingFile();
        CXGeneralMidiFile gmfile = CXGeneralMidiFile.getInstance();
        for (CXFile file : _listLoaded) {
            if (file._file.getName().equals(gmfile._file.getName())) {
                return;
            }
        }
        _listLoaded.add(gmfile);
    }
    
    public static File getSaveDirectory() {
        File dir = new File(MXSettingUtil.getAppBaseDirectory(), "Editing");
        if (dir.isDirectory()) {
            return dir;
        }

        dir.mkdir();
        if (dir.isDirectory()) {
            return dir;
        }

        return null;
    }

    public List<File> listXMLFiles() {
        File dir = getSaveDirectory();

        LinkedList<File> listDirectory = new LinkedList<>();
        ArrayList<File> listResult = new ArrayList<>();

        listDirectory.add(dir);

        while (listDirectory.isEmpty() == false) {
            File f = listDirectory.removeFirst();

            File[] listFiles = f.listFiles();

            for (File e : listFiles) {
                if (e.isDirectory()) {
                    listDirectory.add(e);
                    continue;
                }
                if (e.getName().toLowerCase().endsWith(".xml")) {
                    listResult.add(e);
                    continue;
                }
            }
        }

        return listResult;
    }

    ArrayList<CXFile> _listLoaded = new ArrayList<>();
    
    public ArrayList<CXFile> listLoaded() {
        return _listLoaded;
    }

    public boolean browseAndImport(JComponent parent) {
        FileFilter filter = new FileFilterListExt(new String[]{".xml"});
        MXSwingFolderBrowser chooser = new MXSwingFolderBrowser(getSaveDirectory(), filter, null);
        MXUtil.showAsDialog(parent, chooser, "Choise XML");
        if (chooser.getReturnStatus() != INavigator.RETURN_STATUS_APPROVED) {
            return false;
        }
        FileList selected  = chooser.getReturnValue();
        if (selected == null) {
            return false;
        }
        for (File file : selected) {
            if (file.isFile() == false) {
                JOptionPane.showMessageDialog(parent, "Choose File");
                return false;
            }
        }
        boolean ret = false;
        for (File file : selected) {
            if (importXMLFile(parent, file)) {
                ret = true;
            }
        }
        return ret;
    }

    public boolean copyFileNative(File from, File to) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);
            byte[] data = new byte[32768];
            int len;

            while ((len = in.read(data)) >= 0) {
                if (len == 0) {
                    // can't happen, but did it
                    // read = wait for 1+ byte or return -1
                    break;
                }
                out.write(data, 0, len);
            }

            out.close();
            out = null;
            return true;
        } catch (IOException ex) {
            Logger.getLogger(CXXMLManager.class.getName()).log(Level.WARNING, null, ex);
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                    out = null;
                } catch (IOException ex) {
                    Logger.getLogger(CXXMLManager.class.getName()).log(Level.INFO, null, ex);
                }
            }
            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (IOException ex) {
                    Logger.getLogger(CXXMLManager.class.getName()).log(Level.INFO, null, ex);
                }
            }
        }
    }

    public boolean tryMoveFile(File from, File to) {
        if (to.exists()) {
            to.delete();
        }

        if (from.renameTo(to)) {
            return true;
        }

        if (copyFileNative(from, to)) {
            from.delete();
            return true;
        }

        return false;
    }

    public boolean backupFileToOld(File target) {
        File backupFolder = getSaveDirectory();
        if (backupFolder == null) {
            //can't user this function
            return false;
        }
        backupFolder = new File(backupFolder, "Old");
        if (backupFolder.isDirectory() == false) {
            backupFolder.mkdirs();
        }
        long tick = target.lastModified();
        DateFormat inst = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        String addText = inst.format(new Date(tick));
        
        addText = addText.replace('/', '-');
        addText = addText.replace(':', '-');
        addText = addText.replace('*', '-');
        addText = addText.replace('?', '-');

        String name = target.getName();
        String extension = "";
        int last = name.lastIndexOf('.');
        if (last >= 0) {
            extension = name.substring(last);
            name = name.substring(0, last);
        }

        for (int i = 0; i < 10000; ++i) {
            String newName = name + "-" + addText + extension;
            File f = new File(backupFolder, newName);
            if (f.exists() == false) {
                if (tryMoveFile(target, f) == false) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public boolean importXMLFile(JComponent parent, File file) {
        File saveDirectory = getSaveDirectory();

        //same file name
        for (CXFile already : _listLoaded) {
            String path1 = already._file.getName();
            String path2 = file.getName();
            if (path1.equalsIgnoreCase(path2)) {
                JOptionPane.showMessageDialog(parent, "Already exists [" + path1 + "]\nClose previous file before open.", "Import Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        // validate
        CXFile validate = new CXFile(file);
        if (validate.isLoaded() == false) {
            JOptionPane.showMessageDialog(parent, "File format error when parse [" + file  + "].\n" + validate.getAdviceForXML(), "Open Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // already in managed directory
        if (file.getParent().equalsIgnoreCase(saveDirectory.getPath())) {
            CXFile xmlFile = new CXFile(file);
            _listLoaded.add(xmlFile);
            return true;
        }
        
        File localFile = new File(saveDirectory, file.getName());
        if (localFile.exists()) {
            String path1 = file.getName();
            int user = JOptionPane.showConfirmDialog(parent, "[" + path1 + "] is in manager directory (" + saveDirectory+ ").\nAre you sure to overwrite it?", "Question", JOptionPane.ERROR_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            if (user != JOptionPane.OK_OPTION) {
                user = JOptionPane.showConfirmDialog(parent, "Ok, open already managed [" + path1 +"]?", "Question", JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION);
                if (user != JOptionPane.NO_OPTION) {
                    CXFile xmlFile = new CXFile(localFile);
                    _listLoaded.add(xmlFile);
                    return true;
                }
                return false;
            }else {
                backupFileToOld(localFile);
            }
        }
        if (copyFileNative(file, localFile) == false) {
            JOptionPane.showMessageDialog(parent, "Some error when copy to [" + localFile + "]", "Import Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        CXFile xmlFile = new CXFile(localFile);
        _listLoaded.add(xmlFile);
        return true;
    }
    
    @Override
    public MXSetting getSettings() {
        return _setting;
    }

    @Override
    public void prepareSettingFields() {
        _setting.register("file[]");
    }

    @Override
    public void afterReadSettingFile() {
        ArrayList<MXSettingNode> list = _setting.findByPath("file[]");

        _listLoaded.clear();

        for (MXSettingNode node : list) {
            String path = node._value;
            CXFile xmlFile = new CXFile(new File(path));
            if (xmlFile.isLoaded()) {
                _listLoaded.add(xmlFile);
            }
            else {
                Logger.getLogger(CXXMLManager.class.getName()).log(Level.SEVERE, xmlFile.getAdviceForXML());
            }
        }
    }

    @Override
    public void beforeWriteSettingFile() {
        _setting.clearValue();
        for (int i = 0; i < _listLoaded.size(); ++ i) {
            _setting.setSetting("file[" + i + "]", _listLoaded.get(i)._file.getPath());
        }
    }

    @Override
    public String getReceiverName() {
        return "(XML Manager)";
    }

    CXXMLManagerPanel _view = null;

    @Override
    public synchronized  CXXMLManagerPanel getReceiverView() {
        if (_view == null) {
            _view = new CXXMLManagerPanel();
        }
        return _view;
    }

    @Override
    public void processMXMessage(MXMessage message) {
        //nothing
    }
}
