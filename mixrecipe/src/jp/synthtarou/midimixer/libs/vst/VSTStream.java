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

import jp.synthtarou.libs.async.Transaction;
import jp.synthtarou.midimixer.mx80vst.MX80Process;
import jp.synthtarou.midimixer.mx80vst.VSTInstancePanel;
import jp.synthtarou.midimixer.windows.MXLIB02VST3;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class VSTStream {
    private static VSTStream _instance;
    
    public static synchronized VSTStream getInstance() {
        if (_instance == null) {
            _instance = new VSTStream();
        }
        return _instance;
    }
    
    private int _offer = -1;
    private int _stream = -1;
    private int _blockSize = 512;
    private int _sampleRate = 44100;
    private boolean _open = false;
    
    private int _lastSamplingRate;
    private int _lastBlockSize;

    private VSTStream() {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        
        if (vst.isUsable() == false) {
            return;
        }

        Transaction tr = new Transaction("transaction");
        vst.postInitializeStream(tr.getTransactionTicket());

        if (tr.awaitResult() == 0) {
            int spare = -1;
            for (int i = 0; i < vst.countStream(); ++ i) {
                String name = vst.nameOfStream(i);
                String type = vst.typeNameOfStream(i);
    //          System.out.println("name  = " + name + " / type = " + type);
                if (type.equalsIgnoreCase("ASIO")) {
                    if (spare < 0) {
                        spare = i;
                    }
                    if (name.contains("FL Studio")) {
                        _offer = i;
                    }
                    if (_offer < 0 && name.contains("ASIO 4 ALL")) {
                        _offer = i;
                    }
                }
            }
            if (_offer < 0) {
                _offer = spare;
            }
        }
    }
    
    boolean activeReload = false;
    
    public Transaction postOpenStream(Transaction task) {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (task == null) {
            task = new Transaction("postOpenStream " + getName(_stream));
        }
        if (vst.isUsable()) {
            boolean[] loaded = null;
            MX80Process process = MX80Process.getInstance();

            if (activeReload) {
                if (_lastBlockSize != _blockSize || _lastSamplingRate != _sampleRate) {
                    if (_lastBlockSize != 0 && _lastBlockSize != 0) {
                        System.out.println("Need Reload 1 "  + _blockSize + " , " + _sampleRate);
                        loaded = new boolean[16];
                        for (int x = 0; x < 16; ++ x) {
                            VSTInstancePanel panel  = process.getInstrumentPanel(x);
                            if (panel.isOpen()) {
                                loaded[x] = true;
                                panel.enterCloseVST();
                                VSTInstance.waitQueued(null);
                            }
                        }
                    }
                }
            }
            vst.postOpenStream(_stream, _sampleRate,_blockSize, task.getTransactionTicket());
            _lastSamplingRate = _sampleRate;
            _lastBlockSize = _blockSize;
            if (activeReload && loaded != null) {
                System.out.println("Need Reload 2 " + _blockSize + " , " + _sampleRate);
                for (int x = 0; x < 16; ++ x) {
                    if (loaded[x]) {
                        VSTInstancePanel panel  = process.getInstrumentPanel(x);
                        panel.enterOpenVST();
                        VSTInstance.waitQueued(null);
                    }
                }
            }
        }else {
            task.letsCancel();
        }
        return task;
    }
    
    public boolean isOpen() {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (vst.isUsable()) {
            //re-ask
            _open = vst.isStreamOpen();
            return _open;
        }
        return false;
    }
    
    public Transaction postCloseStream(Transaction task) {
        if (task == null) {
            task = new Transaction("postCloseStream " + getName(_stream));
        }
        if (_open) {
            MXLIB02VST3 vst = MXLIB02VST3.getInstance();
            System.out.println("closing stream " + getName(_stream));
            vst.postCloseStream(task.getTransactionTicket());
            task.awaitResult();
            _open = false;
        }else {
            task.letsCancel();
        }
        return task;
    }

    public int count() {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (vst.isUsable()) {
            return vst.countStream();
        }
        return -1;
    }
    
    public String getName(int x) {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (vst.isUsable()) {
            return vst.nameOfStream(x);
        }
        return null;
    }

    public String getTypeName(int x) {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (vst.isUsable()) {
            return vst.typeNameOfStream(x);
        }
        return null;
    }
    
    public int getStream() {
        return _stream;
    }
   
    public void setStream(int x) {
        _stream = x;
    }
    
    public int getOffer() {
        return _offer;
    }

    public int getBlockSize() {
        return _blockSize;
    }

    public void setBlockSize(int blockSize) {
        this._blockSize = blockSize;
    }

    public int getSampleRate() {
        return _sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this._sampleRate= sampleRate;
    }

    public void forceTerminate() {
        MXLIB02VST3 vst = MXLIB02VST3.getInstance();
        if (vst.isUsable()) {
            vst.forceTerminate();
        }
    }
}
