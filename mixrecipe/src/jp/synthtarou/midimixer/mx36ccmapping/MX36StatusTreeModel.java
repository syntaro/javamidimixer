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
package jp.synthtarou.midimixer.mx36ccmapping;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import jp.synthtarou.midimixer.mx30surface.MGStatus;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36StatusTreeModel {
    public static final MX36StatusTreeModel _instance = new MX36StatusTreeModel();

    public class Folder {
        Folder(int order, String name) {
            _order = order;
            _name = name;
        }

        Folder(String name) {
            if (_counter >= Integer.MAX_VALUE) {
                renumberListFolder();
            }
            _order = _counter ++;
            _name = name;
        }
        
        int _order;
        String _name;
 
        ArrayList <MX36Status> _listStatus = new ArrayList();

        @Override
        public int hashCode() {
            return _name.hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof Folder) {
                Folder target = (Folder)o;
                return target._name.equals(_name);
            }
            return false;
        }
        
        synchronized boolean removeAll(MX36Status status) {
            boolean removedFlag = false;
            while(true) {
                DefaultMutableTreeNode seek = getTreeStatusNode(null, status);
                if (seek != null) {
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode)seek.getParent();
                    parent.remove(seek);
                    _treeModel.reload(parent);
                }
                else {
                    break;
                }
            }

            for (int i = _listStatus.size() - 1; i >= 0; -- i) {
                MX36Status comp = _listStatus.get(i);
                if (comp == status) {
                    _listStatus.remove(i);
                    removedFlag = true;
                }
                else if (status.compareSurfacePositionColumn(comp) == 0) {
                    _listStatus.remove(i);
                    removedFlag = true;
                }
            }
            return removedFlag;
        }
        
        synchronized MX36Status findBySurfacePosition(MGStatus status) {
            return Folder.this.findBySurfacePosition(status._port, status._uiType, status._row, status._column);
        }

        synchronized MX36Status findBySurfacePosition(MX36Status status) {
            return Folder.this.findBySurfacePosition(status._surfacePort, status._surfaceUIType, status._surfaceRow, status._surfaceColumn);
        }
        
        synchronized MX36Status findBySurfacePosition(int port, int type, int row, int column) {
            for (int i = _listStatus.size() - 1; i >= 0; -- i) {
                MX36Status comp = _listStatus.get(i);
                if (comp.compareSurfacePositionColumn(port, type, row, column) == 0) {
                    return comp;
                }
            }
            return null;
        }
        
        synchronized int insertPoint(MX36Status status) {
            int found = 0;
            for (int i = 0; i < _listStatus.size(); ++ i) {
                MX36Status seek = _listStatus.get(i);
                int x = seek.compareSurfacePositionColumn(status);
                if (x < 0) {
                    found = i + 1;
                    continue;
                }
                if (x > 0) {
                    break;
                }
            }
            return found;
        }

        synchronized void insert(int pos, MX36Status status) {
            _listStatus.add(pos, status);
        }
        
        public String toString() {
            return _name;
        }
    }
    
    public final Folder _pirmalFolder;
    public final Folder _autoDetectedFolder;
    public int _counter = 1;
    
    public MX36StatusTreeModel() {
        _pirmalFolder = new Folder(0, "Primal");
        _autoDetectedFolder = new Folder(Integer.MAX_VALUE, "AutoDetected");
        addFolder(_pirmalFolder);
        addFolder(_autoDetectedFolder);
    }
    
    synchronized void renumberListFolder() {
        Iterator<Folder> it = _listFolder.iterator();
        int renumber = 1;
        while (it.hasNext()) {
            Folder f = it.next();
            if (f == _pirmalFolder || f == _autoDetectedFolder) {
                continue;
            }
            f._order = renumber ++;
        }
        _counter = renumber;
    }

    ArrayList<Folder> _listFolder = new ArrayList();
    
    
    public synchronized void addFolder(Folder folder) {
        DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folder, true);
        for (int i = 0; i < _listFolder.size(); ++ i) {
            Folder seek = _listFolder.get(i);
            if (folder._order >= seek._order) {
                _listFolder.add(i, folder);
                _treeRoot.insert(folderNode, _counter);
                _treeModel.reload(folderNode);
                return;
            }
        }
        _listFolder.add(folder);
        _treeRoot.add(folderNode);
        _treeModel.reload(folderNode);
    }
    
    public synchronized void setFolder(Folder folder, MX36Status status) {
        for (Folder already : _listFolder) {
            already.removeAll(status);
        }

        int position = (folder == _autoDetectedFolder) ? folder.insertPoint(status) : folder._listStatus.size();
        folder.insert(position, status);
        DefaultMutableTreeNode folderNode = getTreeFolderNode(folder);
        DefaultMutableTreeNode statusNode = new DefaultMutableTreeNode(status, false);
        folderNode.insert(statusNode, position);
        _treeModel.reload(folderNode);
    }
    
    public synchronized Folder generateFolder(String name) {
        for (Folder folder : _listFolder) {
            if (folder._name.equals(name)) {
                return folder;
            }
        }
        Folder f = new Folder(name);
        addFolder(f);
        return f;
    }
    
    DefaultMutableTreeNode _treeRoot = new DefaultMutableTreeNode("root", true);
    DefaultTreeModel _treeModel = new DefaultTreeModel(_treeRoot, true);
    
    public synchronized DefaultMutableTreeNode getTreeFolderNode(Folder folder) {
        for (int i = 0; i < _treeRoot.getChildCount(); ++ i) {
            DefaultMutableTreeNode folderNode = (DefaultMutableTreeNode)_treeRoot.getChildAt(i);

            if (folderNode.getUserObject() == folder) {
                return folderNode;
            }
        }
        return null;
    } 
    
    public synchronized DefaultMutableTreeNode getTreeStatusNode(DefaultMutableTreeNode folderNode, MX36Status status) {
        if (folderNode == null) {
            for (int i = 0; i < _treeRoot.getChildCount(); ++ i) {
                DefaultMutableTreeNode seekFolder = (DefaultMutableTreeNode)_treeRoot.getChildAt(i);
                DefaultMutableTreeNode statusNode = getTreeStatusNode(seekFolder, status);
                if (statusNode != null) {
                    return statusNode;
                }
            }
            return null;
        }
        else {
            for (int i = 0; i < folderNode.getChildCount(); ++ i) {
                DefaultMutableTreeNode seekStatus = (DefaultMutableTreeNode)folderNode.getChildAt(i);
                if (seekStatus.getUserObject() == status) {
                    return seekStatus;
                }
            }
            return null;
        }
    }
    
    public void reloadStatusOfTree(MX36Status status) {
        DefaultMutableTreeNode node = getTreeStatusNode(null, status);
        if (node != null) {
            _treeModel.reload(node);
        }
    }
    
    public DefaultTreeModel getTreeModel() {
        return _treeModel;
    }
}

