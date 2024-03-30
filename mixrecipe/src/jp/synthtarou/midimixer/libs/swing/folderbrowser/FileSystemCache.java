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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import jp.synthtarou.libs.MXFileLogger;
import jp.synthtarou.midimixer.libs.swing.JTableWithFooter;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class FileSystemCache {

    File _fileSystem;
    String _path;
    DefaultMutableTreeNode _treeNode;
    static FileSystemView _view = FileSystemView.getFileSystemView();
    
    public static File[] getRoots() {
        return _view.getRoots();
    }
    
    /**
     *
     */
    public static class Element implements Comparable<Element> {

        public final String _path;
        public final File _fileObject;
        public final DefaultMutableTreeNode _pairNode;
        public String _displayName;
        public Icon _icon;

        /**
         *
         * @param file
         */
        public Element(File file) {
            _path = file == null ? null : file.toString();
            _fileObject = file;
            _pairNode = new DefaultMutableTreeNode(this);
            if (file == null) {
                _pairNode.setAllowsChildren(true);
            }else if (file.isDirectory()) {
                _pairNode.setAllowsChildren(true);
            }
            else {
                _pairNode.setAllowsChildren(false);
            }
        }
        
        public synchronized String getDisplayName() {
            if (_fileObject == null) {
                return "<ROOT>";
            }
            if (_displayName == null) {
                _displayName = _view.getSystemDisplayName(_fileObject);
            }
            return _displayName;
        }

        public synchronized Icon getIcon() {
            if (_fileObject == null) {
                return null;
            }
            if (_icon == null) {
                _icon = _view.getSystemIcon(_fileObject);
             }
            return _icon;
        }

        @Override
        public int compareTo(Element o) {
            if (_path == null) {
                return -1;
            }
            else if (o._path == null) {
                return 1;
            }
            int x = _path.compareTo(o._path);
            if (x == 0) {
                x = _fileObject.compareTo(o._fileObject);
            }
            return x;
        }
        
        public String toString() {
            return _path == null ? "<ROOT>" : _path.toString();
        }
    }

    public static class ElementList {

        public ElementList(String path) {
            _path = path;
            _elements = new LinkedList();
            if (path == null) {
                _elements.add(new Element(null));
            }
        }
        public final String _path;
        public final List<Element> _elements;
    }
    
    public final ElementList _listroot = new ElementList(null);
    
    TreeMap<String, ElementList> _cache = new TreeMap();

    public List<Element> getRoot() {
        return getCache((String)null);
    }

    public Element getRoot1() {
        return getCache((String)null).get(0);
    }
    
    public List<Element> getCache(String path) {
        if (path == null) {
            return _listroot._elements;
        }
        ElementList c = _cache.get(path);
        if (c == null) {
            return null;
        }
        return c._elements;
    }

    public List<Element> getCache(File path) {
        return getCache(path == null ? null : path.toString());
    }

    public List<Element> getCache(DefaultMutableTreeNode node) {
        FileSystemCache.Element f = node == null ? null : ((FileSystemCache.Element)node.getUserObject());
        return getCache(f._fileObject);
    }

    static String _staticDrive;
    static {
        _staticDrive = System.getProperty("user.home");
        if (_staticDrive == null) {
            _staticDrive = System.getProperty("java.home");
        }
        if (_staticDrive.startsWith("/")) {
            _staticDrive = "/";
        }
        else if (_staticDrive.length() >= 3) {
            char c = _staticDrive.charAt(0);
            char dblCln = _staticDrive.charAt(1);
            if (dblCln == ':') {
                _staticDrive = Character.toString(c) + ":\\";
            }
        }
    }

    public static boolean isHomeDrive(File hdd) {
        if (hdd == null) {
            return false;
        }
        String path = hdd.toString();
        //System.out.println("test home " + _staticDrive +" == " + path);
        if (path.startsWith(_staticDrive)) {
            if (_staticDrive.length()== 3) {
                if (path.length() == 3) {
                    return true;
                }
            }
            else {
                int count = 0;
                for (int i = 0; i < _staticDrive.length(); ++ i) {
                    char ch = _staticDrive.charAt(i);
                    if (ch == '/') {
                        count ++;
                    }
                }
                if (count == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isUniqueNamed(File file) {
        if (file == null) {
            return false;
        }
        String path = file.getPath();
        String text = file.toString();
        if (path.equals(text)) {
            return false;
        }
        return true;
    }

    public Element getCache1(String path) {
        List<Element> ret = getCache(path);
        if (ret == null) {
            return null;
        }
        if (ret.size() == 0) {
            return null;
        }
        if (ret.size() == 1) {
            return ret.get(0);
        }
        for (Element e : ret) {
            File f = e._fileObject;
            while (f != null) {
                try {
                    f = f.getParentFile();
                    if (f != null && isHomeDrive(f) && f.getParentFile() == null) {
                        return e;
                    }
                    if (f != null && isUniqueNamed(f)) {
                        break;
                    }
                }catch(Exception ex) {
                    MXFileLogger.getLogger(FileSystemCache.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
        return ret.get(0);
    }

    public Element getCache1(File path) {
        return getCache1(path == null ? (String)null : path.toString());
    }
    
    public Element getCache1(DefaultMutableTreeNode node) {
        return getCache1((File)node.getUserObject());
    }

    Element addCache(File file) {
        String path = file == null ? "" : file.toString();
        ElementList c = path == null ? _listroot : _cache.get(path);
        if (c == null) {
            c = new ElementList(path);
            _cache.put(path, c);
        }
        Element newE = new Element(file);
        for (Element e : c._elements) {
            if (e.compareTo(newE) == 0) {
                return e;
            }
        }
        c._elements.add(newE);
        return newE;
    }
}
