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
package jp.synthtarou.midimixer.libs.vst;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOut;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class VSTFolder {
    public TreeMap<File, ArrayList<File>> getListResult() {
        return _listResult;
    }

    public static interface Callback {
        public void seekingCallback(String text, long hit, long total);
    }
    
    public VSTFolder() {
        
    }
    
    private Callback _callback = (String text, long hit, long total) -> {
        System.out.println(hit + " / " + total + " = " + text);
    };
    
    public static void main(String args[]) {
        VSTFolder filter = new VSTFolder(".mid");
        File root = new File("C:/midi");
        filter.setRootDirectory(root);
        filter.scan(null);
        
        for (File dir : filter.getListResult().keySet()) {
            System.out.println("@"  + getAsAbsolute(root, dir));
            ArrayList<File> childs = filter.getListResult().get(dir);
            for (File file : childs) {
                System.out.println("    "  + getAsAbsolute(dir, file));
            }
        }
    }
    
    public static String getAsAbsolute(File root, File target) {
        String rootPath = root.getPath();
        String targetPath = target.getPath();
        if (targetPath.equals(rootPath)) {
            return  "";
        }
        if (targetPath.startsWith(rootPath)) {
            return targetPath.substring(rootPath.length() + 1);
        }
        return targetPath;
    }

    public VSTFolder(String extension) {
        setExtension(extension);
    }
    
    public void setRootDirectory(File directory) {
        _rootDirectory = directory;
    }
    
    public void scan(ArrayList<String> listSkip) {
        _cancelOperation = false;
        if (_rootDirectory == null) {
            MXFileLogger.getLogger(VSTFolder.class).warning("rootDirectory is null");
            return;
        }
        if (_rootDirectory.isDirectory() == false) {
            MXFileLogger.getLogger(VSTFolder.class).warning("rootDirectory[" + _rootDirectory + "] is not directory");
            return;
        }

        LinkedList<File> listDirctory = new LinkedList();
        listDirctory.add(_rootDirectory);

        TreeMap<File, ArrayList<File>> newResult = new TreeMap();
        _progressTotal = 0;
        _progrssHit = 0;

        while(listDirctory.isEmpty() == false) {
            File dir = listDirctory.pop();
            File[] childs = dir.listFiles();
            
            if (childs == null) {
                continue;
            }
            
            ArrayList<File> list = new ArrayList();
            for (File f : childs) {
                _progressTotal ++;
                if (_cancelOperation) {
                    return;
                }
                if ((_progressTotal % 1000) == 0) {
                    _callback.seekingCallback(getAsAbsolute(_rootDirectory, f), _progrssHit, _progressTotal);
                }
                String path = f.getPath();
                if (isSkipTarget(listSkip, path)) {
                    continue;
                }
                if (matchFilter(path)) {
                    list.add(f);
                    _progrssHit ++;
                }
                else if (f.isDirectory()) {
                    listDirctory.add(f);
                }
            }
            if (list.size() > 0) {
                newResult.put(dir, list);
            }
        }
        _callback.seekingCallback("Done", _progrssHit, _progressTotal);
        _listResult = newResult;
        _scanDone = true;
    }
    
    public void setExtension(String extension) {
        if (extension.startsWith("*.")) {
            extension = extension.substring(2);
        }
        else if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        for (char ch : extension.toCharArray()) {
            switch(ch) {
                case '*':
                case '<':
                case '>':
                    throw new IllegalArgumentException("Bad Filter (contains *><)");
            }
        }
        
        _targetExtension = extension.toLowerCase();
    }

    public boolean matchFilter(String path) {
        int last = path.lastIndexOf('.');
        String extension   = "";
        if (last > 0) {
            extension = path.substring(last + 1).toLowerCase();
        }
        if (_targetExtension.equals(extension)) {
            return true;
        }
        return false;
    }
    
    public void cancelOperation() {
        _cancelOperation = true;
    }

    boolean isSkipTarget(ArrayList<String> listSkip, String path) {
        if (listSkip != null) {
            for (String skip : listSkip) {
                if (path.equalsIgnoreCase(skip)) {
                    return true;
                }
            }
        }
        return false;
    }

    private long _progressTotal;
    private long _progrssHit;
    private boolean _scanDone;
    public boolean _cancelOperation;
    private String _targetExtension;
    public File _rootDirectory = null;
    private TreeMap<File, ArrayList<File>> _listResult = new TreeMap();

    public void setResult(TreeMap<File, ArrayList<File>> newReult) {
        _listResult = newReult;
    }

    public void setCallback(Callback _callback) {
        this._callback = _callback;
    }

    public void setScanDone(boolean done) {
        _scanDone = done;
    }

    public boolean isScanDone() {
        return _scanDone;
    }

}
