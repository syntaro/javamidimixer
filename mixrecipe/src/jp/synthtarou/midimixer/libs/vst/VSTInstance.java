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

import jp.synthtarou.midimixer.libs.common.async.Transaction;
import java.io.File;
import javax.swing.filechooser.FileSystemView;
import jp.synthtarou.midimixer.libs.settings.MXSettingUtil;
import jp.synthtarou.midimixer.windows.MXLIB02VST3;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class VSTInstance {
    static MXLIB02VST3 vst = MXLIB02VST3.getInstance();

    public VSTInstance(boolean effectType, int slot) {
        _slot = slot;
        _isEffect = effectType;
        if (_vstRoot == null) {
            File appdir = MXSettingUtil.getAppBaseDirectory();
            File vst = new File(appdir, "vst");
            if (vst.exists() == false) {
                vst.mkdirs();
            }
            _vstRoot = new IndexedFile(vst);

            
            File document = FileSystemView.getFileSystemView().getDefaultDirectory();
            File docMixrecipe = new File(document, "MIXRecipe");
            if (docMixrecipe.exists() == false) {
                docMixrecipe.mkdirs();
            }
            File global = new File(docMixrecipe, "vst_global");
            if (global.exists() == false) {
                global.mkdirs();
            }
            
            _globalRoot = new IndexedFile(global);
        }
        try {
            _vstRoot.getLock();
            if (effectType) {            
                _vstRoot.setTitle(slot + 16, "EFFECT" + slot);
            }else {
                _vstRoot.setTitle(slot, "SLOT" + slot);
            }
        }finally {
            _vstRoot.releaseLock();
        }
    }
    
    public static File getTotalRecallSetting(boolean effectMode, int rack) {
        try {
            _vstRoot.getLock();
            if (effectMode) {
                return _vstRoot.getPath(16 + rack);
            }
            return _vstRoot.getPath(rack);
        }finally {
            _vstRoot.releaseLock();
        }
    }
    
    public static IndexedFile getGlobalSetting(String vstPath) {
        try {
            _globalRoot.getLock();
            int x = _globalRoot.findTitle(vstPath);
            if (x < 0) {
                x = _globalRoot.newFileEntry(vstPath);
                if (x < 0) {
                    return null;
                }
            }
            File vstRoot = _globalRoot.getPath(x);
            if (!vstRoot.exists()) {
                vstRoot.mkdirs();
            }
            return new IndexedFile(vstRoot);
        }finally {
            _globalRoot.releaseLock();
        }
    }

    private static IndexedFile _vstRoot;
    private static IndexedFile _globalRoot;

    private final int _slot;
    private final boolean _isEffect;
    private String _path;
    private String _name;
    private String _detail;
    
    public int _insertValue = 127;
    public int _sendAuxValue = 0;
    
    public boolean isEffect() {
        return _isEffect;
    }
    
    public int getSlot() {
        return _slot;
    }
    
    public String getName() {
        return _name;
    }

    public String getPath() {
        return _path;
    }

    public String getDetail() {
        return _detail;
    }

    public int getBusCount() {
        if (vst.isUsable()) {
            int x = vst.getBusCount(_isEffect, _slot);
            return x;
        }
        return 0;
    }
    
    public int getBusVolume(int bus) {
        if (vst.isUsable()) {
            float f = vst.getBusVolume(_isEffect, _slot, bus) * 127f;
            return (int)f;
        }
        return 127;
    }
    
    public void setBusVolume(int bus, int volume) {
        if (vst.isUsable()) {
            float f = volume / 127f;
            vst.setBusVolume(_isEffect, _slot, bus, f);
        }
    }
    
    public int getInsertBalanace() {
        if (vst.isUsable()) {
            float f = vst.getInsertBalance(_slot) * 127f;
            return (int)f;
        }
        return 0;
    }
    
    public void setInsertBalance(int volume) {
        if (vst.isUsable()) {
            float f = volume / 127f;
            vst.setInsertBalance(_slot, f);
        }
    }

    public int getAuxSend() {
        if (vst.isUsable()) {
            float f = vst.getAuxSend(_slot) * 127f;
            return (int)f;
        }
        return 0;
    }
    
    public void setAuxSend(int volume) {
        if (vst.isUsable()) {
            float f = volume / 127f;
            vst.setAuxSend(_slot, f);
        }
    }
    
    public void setPath(String path) {
        if (path == null) {
            _path = "";
            _name = "";
            _detail = "";
            return;
        }
        
        _path = path;
        int i1 = path.lastIndexOf('/');
        int i2 = path.lastIndexOf('\\');
        if (i1 < i2) i1 = i2;
        _name = _path.substring(i1+ 1);
        _detail = "-";
    }

    public boolean isOpen() {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (vst.isUsable()) {
            return vst.isLaunchedVST(_isEffect, _slot);
        }
        return false;
    }
    
    public Transaction postLaunchVST(Transaction task) {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (task == null) {
            task = new Transaction("postLaunchVST " + _path);
        }
        if (vst.isUsable() && _path != null && new File(_path).exists()) {
            if (isOpen()) {
                vst.postRemoveSynth(_isEffect, _slot, 0);
            }
            vst.postLaunchVST(_isEffect, _slot, _path, task.getTransactionTicket());
            return task;
        }
        task.letsCancel();
        return task;
    }
    
    public Transaction postCloseVST( Transaction task) {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (task == null) {
            task = new Transaction("postCloseVST " + _path);
        }
        if (isOpen()) {
            vst.postRemoveSynth(_isEffect, _slot, task.getTransactionTicket());
            return task;
        }
        task.letsCancel();
        return task;
    }
    
    public boolean isEditorOpen() {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (isOpen()) {
            return vst.isEditorOpen(_isEffect, _slot);
        }
        return false;
    }
    
    public Transaction postOpenEditor(Transaction task, Transaction whenClose) {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (task == null) {
            task = new Transaction("postOpenEditor " + _path);
        }
        if (whenClose == null) {
            whenClose = new Transaction("postOpenEditor#closeHandler " + _path);
        }
        if (vst.isUsable()) {
            if (_slot >= 0) {
                vst.postOpenEditor(_isEffect, _slot, task.getTransactionTicket(), whenClose.getTransactionTicket());
                return task;
            }
        }
        task.letsCancel();
        return task;
    }

    public Transaction postCloseEditor(Transaction task) {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (task == null) {
            task = new Transaction("postCloseEditor " + _path);
        }
        if (vst.isUsable()) {
            if (_slot >= 0) {
                vst.postCloseEditor(_isEffect, _slot, task.getTransactionTicket());
                return task;
            }
        }
        task.letsCancel();
        return task;
    }

    public Transaction postSavePreset(String path, Transaction task) {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (task == null) {
            task = new Transaction("savePreset " + path);
        }
        if (vst.isUsable()) {
            if (_slot >= 0 && vst.isLaunchedVST(_isEffect, _slot)) {
                vst.savePreset(_isEffect, _slot, path, task.getTransactionTicket());
                return task;
            }
        }
        task.letsCancel();
        return task;
    }

    public Transaction postLoadPreset(String path, Transaction task) {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (task == null) {
            task = new Transaction("loadPreset " + path);
        }
        if (vst.isUsable()) {
            if (_slot >= 0) {
                if (new File(path).exists()) {
                    vst.loadPreset(_isEffect, _slot, path, task.getTransactionTicket());
                    return task;
                }
            }
        }
        task.letsCancel();
        return task;
    }

    public static Transaction waitQueued(Transaction task) {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (task == null) {
            task = new Transaction("waitQueued");
        }
        if (vst.isUsable()) {
            vst.waitQueued(task.getTransactionTicket());
            return task;
        }
        task.letsCancel();
        return task;
    }
    
    public boolean postShortMessage(int dword) {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (vst.isUsable()) {
            if (_slot >= 0) {
                vst.postShortMessage(_isEffect, _slot, dword);
                return true;
            }
        }
        return false;
    }

    public boolean postLongMessage(byte[] data) {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (vst.isUsable()) {
            if (_slot >= 0) {
                vst.postLongMessage(_isEffect, _slot, data);
                return true;
            }
        }
        return false;
    }
    
    public static Transaction stopEngine(Transaction task) {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (task == null) {
            task = new Transaction("stopEngine");
        }
        if (vst.isUsable()) {
            vst.stopEngine(task.getTransactionTicket());
            return task;
        }
        task.letsCancel();
        return task;
    }
}
