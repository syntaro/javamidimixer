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
package jp.synthtarou.midimixer.mx80vst;

import jp.synthtarou.midimixer.libs.vst.VSTInstance;
import jp.synthtarou.midimixer.libs.vst.VSTFolder;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.JComponent;
import jp.synthtarou.midimixer.MXThreadList;
import jp.synthtarou.midimixer.libs.common.async.Transaction;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.libs.vst.VSTStream;
import jp.synthtarou.midimixer.windows.MXLIB02VST3;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX80Process extends MXReceiver implements MXSettingTarget {
    static MX80Process _instance = new MX80Process();
    
    public static synchronized MX80Process getInstance() {
        return _instance;
    }
    
    ArrayList<VSTFolder> _listFolder = new ArrayList();
    MXSetting _setting;
    Callback _callback;
    Thread _thread = null;

    ArrayList<VSTInstance> _listInstrument = new ArrayList();
    ArrayList<VSTInstancePanel> _listInstrumentPanel = new ArrayList();

    ArrayList<VSTInstance> _listEffect = new ArrayList();
    ArrayList<VSTInstancePanel> _listEffectPanel = new ArrayList();

    ArrayList<String> _listSkip;
    long _scanRealTotal;

    MX80Process() {
        _listSkip = new ArrayList();

        for (int i = 0; i < 16;++ i) {
            VSTInstance vst = new VSTInstance(false, i);
            VSTInstancePanel panel = new VSTInstancePanel(vst);
            
            _listInstrument.add(vst);
            _listInstrumentPanel.add(panel);
        }
        
        for (int i = 0; i < 2;++ i) {
            VSTInstance vst = new VSTInstance(true, i);
            VSTInstancePanel panel = new VSTInstancePanel(vst);

            _listEffect.add(vst);
            _listEffectPanel.add(panel);
        }
        
        if (MXLIB02VST3.getInstance().isUsable()) {
            _setting = new MXSetting("VSTFolderList");
            _setting.setTarget(this);
            _setting.readSettingFile();
        }
    }
    
    public boolean readSettingFile() {
        return _setting.readSettingFile();
    }
    
    public boolean writeToSettingFile() {
        if (MXLIB02VST3.getInstance().isUsable() == false) {
            return false;
        }
        return _setting.writeSettingFile();
    }
    
    public int countFolder() {
        return _listFolder.size();
    }
    
    public VSTFolder getFolder(int x) {
        return _listFolder.get(x);
    }
    
    public void addFolder(File folder) {
        if (indexOfFolder(folder) >= 0) {
            return;
        }
        VSTFolder f = new VSTFolder();
        f.setRootDirectory(folder);
        f.setExtension(".vst3");
        _listFolder.add(f);
    }
    
    public void removeFolder(File folder) {
        int x = indexOfFolder(folder);
        if (x >= 0) {
            _listFolder.remove(x);
        }
    }
    
    public int indexOfFolder(File folder) {
        String path1 = folder.getPath().toLowerCase();

        for (int x = 0; x < _listFolder.size(); ++ x) {
            VSTFolder f = _listFolder.get(x);
            String path2 = f._rootDirectory.getPath().toLowerCase();
            
            if (path1.equals(path2)) {
                return x;
            }
        }
        return -1;
    }
    
    boolean _isThreadCancelling = false;

    public boolean isScanThreadAlive() {
        if (_thread != null) {
            return _thread.isAlive();
        }
        return false;
    }
    
    public void cancelScan() {
        _isThreadCancelling = true;
        for (int x = 0; x < _listFolder.size(); ++ x) {
            VSTFolder f = _listFolder.get(x);
            f._cancelOperation = true;
        }
        if (_thread != null) {
            if (_thread.isAlive()) {
                try {
                    _thread.join();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void cleanCancelFlag() {
        _isThreadCancelling = false;
        for (int x = 0; x < _listFolder.size(); ++ x) {
            VSTFolder f = _listFolder.get(x);
            f._cancelOperation = false;
        }
    }

    @Override
    public void prepareSettingFields(MXSetting setting) {
        System.out.println("setting file = " + setting.getFile().toString());
        setting.register("stream.name");
        setting.register("stream.open");
        setting.register("stream.latency");
        setting.register("stream.samplingrate");
        setting.register("stream.masterVolume");
        setting.register("load[].path");
        setting.register("load[].open");
        setting.register("load[].volume.count");
        setting.register("load[].volume[]");
        setting.register("load[].insertBalance");
        setting.register("load[].auxSend");
        setting.register("effect[].path");
        setting.register("effect[].open");
        setting.register("effect[].volume.count");
        setting.register("effect[].volume[]");
        setting.register("base[].path");
        setting.register("base[].scanDone");
        setting.register("base[].directory[].path");
        setting.register("base[].directory[].file[].path");
        setting.register("skip[].path");
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
        _listFolder.clear();
        _listSkip.clear();

        String streamName  =  setting.getSetting("stream.name");
        boolean open = setting.getSettingAsBoolean("stream.open", false);
        int samplingRate = setting.getSettingAsInt("stream.samplingrate", 48000);
        int latency =  setting.getSettingAsInt("stream.latency", 128);
        int volume =  setting.getSettingAsInt("stream.masterVolume", 20);
        int streamIndex = -1;

        MXLIB02VST3.getInstance().setMasterVolume(0.01f * volume);

        VSTStream stream = VSTStream.getInstance();
        if (streamName != null && streamName.length() > 0) {
            for (int i = 0; i < stream.count(); ++ i) {
                if (stream.getName(i).equals(streamName)) {
                    streamIndex = i;
                    break;
                }
            }
        }

        if (streamIndex < 0) {
            streamIndex = stream.getOffer();
        }

        stream.setSampleRate(samplingRate);
        stream.setBlockSize(latency);
        stream.setStream(streamIndex);

        if (streamIndex >= 0 && open) {
            stream.postOpenStream(null);
        }

        int l = 1;
        while(true) {
            
            String loadPath = setting.getSetting("load[" +  l + "].path");
            if (loadPath == null) {
                break;
            }
            if (loadPath.length() == 0) {
                l ++;
                continue;
            }

            open = setting.getSettingAsBoolean("load[" +  l + "].open", false);
            
            VSTInstance vst = new VSTInstance(false, l - 1);

            vst.setPath(loadPath);
            _listInstrument.set(l - 1, vst);

            VSTInstancePanel panel = new VSTInstancePanel(vst);
            _listInstrumentPanel.set(l - 1, panel);
            
            if (open) {
                panel.enterOpenVST();
                File file = VSTInstance.getTotalRecallSetting(false, l - 1);
                int result = vst.postLoadPreset(file.getPath(), null).awaitResult();
            }

            int count = setting.getSettingAsInt("load[" + l + "].volume.count", 0);
            for (int bus = 0; bus < count; ++ bus) {
                int busVolume = setting.getSettingAsInt("load[" + l + "].volume[" + bus + "]", 127);
                vst.setBusVolume(bus, busVolume);
            }

            int balance = setting.getSettingAsInt("load[" + l + "].insertBalance", 0);
            vst.setInsertBalance(balance);
            int send = setting.getSettingAsInt("load[" + l + "].auxSend", 0);
            vst.setAuxSend(send);
            
            panel.createVolumePanel();

            l ++;
        }
        
        l = 1;
        while(true) {
            
            String loadPath = setting.getSetting("effect[" +  l + "].path");
            if (loadPath == null) {
                break;
            }
            if (loadPath.length() == 0) {
                l ++;
                continue;
            }
            
            open = setting.getSettingAsBoolean("effect[" +  l + "].open", false);

            VSTInstance vst = new VSTInstance(true, l - 1);

            vst.setPath(loadPath);
            _listEffect.set(l - 1, vst);

            VSTInstancePanel panel = new VSTInstancePanel(vst);
            _listEffectPanel.set(l - 1, panel);
            
            if (open) {
                panel.enterOpenVST();
                File file = VSTInstance.getTotalRecallSetting(true, l - 1);
                int result = vst.postLoadPreset(file.getPath(), null).awaitResult();
            }

            int count = setting.getSettingAsInt("effect[" + l + "].volume.count", 0);
            for (int bus = 0; bus < count; ++ bus) {
                int busVolume = setting.getSettingAsInt("effect[" + l + "].volume[" + bus + "]", 127);
                vst.setBusVolume(bus, busVolume);
            }

            panel.createVolumePanel();

            l ++;
        }

        int b = 1;
        while(true) {
            int scanDone = setting.getSettingAsInt("base[" + b + "].scanDone", 0);

            String basePath = setting.getSetting("base[" + b + "].path");
            if (basePath == null) {
                break;
            }
            if (basePath.length() == 0) {
                b ++;
                continue;
            }

            VSTFolder baseVSTFolder = new VSTFolder();
            baseVSTFolder.setExtension(".vst3");
            baseVSTFolder.setRootDirectory(new File(basePath));
            baseVSTFolder.setScanDone(scanDone != 0);

            int d = 1;
            
            TreeMap<File, ArrayList<File>> mapFolders = new TreeMap();
            while (true) {
                String directory = setting.getSetting("base[" + b + "].directory[" + d + "].path");
                if (directory == null) {
                    break;
                }
                if (directory.length() == 0) {
                    d ++;
                    continue;
                }
                ArrayList<File> listFiles = new ArrayList();
                int f = 1;
                while(true) {
                    String file = setting.getSetting("base[" + b + "].directory[" + d + "].file[" + f + "].path");
                    f ++;
                    if (file == null) {
                        break;
                    }
                    if (file.length() == 0) {
                        continue;
                    }
                    listFiles.add(new File(file));
                }

                mapFolders.put(new File(directory), listFiles);
                d ++;
            }
            
             baseVSTFolder.setResult(mapFolders);
             _listFolder.add(baseVSTFolder);
            b ++;
        }

        int s = 1;
        while(true) {
            String skipPath = setting.getSetting("skip[" +  s + "].path");
            if (skipPath == null) {
                break;
            }
            if (skipPath.length() == 0) {
                s ++;
                continue;
            }
            _listSkip.add(skipPath);
            s ++;
        }
        
        if (_listFolder.isEmpty()) {
           VSTFolder folder = new VSTFolder();
           folder.setExtension(".VST3");
           folder.setRootDirectory(new File("C:/Program Files/Common Files/VST3"));
           _listFolder.add(folder);
        }
    }
    
    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        setting.clearValue();
        
        VSTStream stream = VSTStream.getInstance();

        setting.setSetting("stream.name", stream.getName(stream.getStream()));
        setting.setSetting("stream.open", stream.isOpen());
        setting.setSetting("stream.latency", stream.getBlockSize());
        setting.setSetting("stream.samplingrate", stream.getSampleRate());

        int volume = (int)(MXLIB02VST3.getInstance().getMasterVolume() * 100);
        setting.setSetting("stream.masterVolume", volume);
        
        int streamIndex = -1;

        MXLIB02VST3.getInstance().setMasterVolume(0.01f * volume);

        Iterator<VSTInstance> itLoad = _listInstrument.iterator();
        int l = 1;
        Transaction t = null;
        while(itLoad.hasNext()) {
            VSTInstance vst = itLoad.next();
            setting.setSetting("load[" + l +  "].path", vst.getPath());
            if (vst.isOpen()) {
                t = vst.postSavePreset(VSTInstance.getTotalRecallSetting(false, l - 1).getPath(), null);
            }
            setting.setSetting("load[" +  l + "].open", vst.isOpen());
            setting.setSetting("load[" +  l + "].volume.count", vst.getBusCount());
            for (int bus = 0; bus < vst.getBusCount(); ++ bus) {
                setting.setSetting("load[" +  l + "].volume[" + bus + "]", vst.getBusVolume(bus));
            }
            setting.setSetting("load[" + l + "].insertBalance", vst.getInsertBalanace());
            setting.setSetting("load[" + l + "].auxSend", vst.getAuxSend());
            
            l ++;
        }

        if (t != null) {
            t.awaitResult();
        }

        itLoad = _listEffect.iterator();
        
        l = 1;
        t = null;
        while(itLoad.hasNext()) {
            VSTInstance vst = itLoad.next();
            setting.setSetting("effect[" + l +  "].path", vst.getPath());
            if (vst.isOpen()) {
                t = vst.postSavePreset(VSTInstance.getTotalRecallSetting(true, l - 1).getPath(), null);
            }
            setting.setSetting("effect[" +  l + "].open", vst.isOpen());
            setting.setSetting("effect[" +  l + "].volume.count", vst.getBusCount());
            for (int bus = 0; bus < vst.getBusCount(); ++ bus) {
                setting.setSetting("effect[" +  l + "].volume[" + bus + "]", vst.getBusVolume(bus));
            }
            l ++;
        }

        if (t != null) {
            t.awaitResult();
        }
        
        for (int i = 0; i < _listFolder.size(); ++ i) {
            int b = i + 1;
            
            VSTFolder vstFolder = _listFolder.get(i);

            setting.setSetting("base[" + b + "].scanDone", vstFolder.isScanDone() ? 1 : 0);

            setting.setSetting("base[" + b + "].path", vstFolder._rootDirectory.toString());
            
            TreeMap<File, ArrayList<File>> result = vstFolder.getListResult();
            
            int d = 1;
            for (File directory : result.keySet()) {
                setting.setSetting("base[" + b + "].directory[" + d + "].path", directory.toString());
                ArrayList<File> fileList = result.get(directory);
    
                int f = 1;
                for (File file : fileList) {
                    setting.setSetting("base[" + b + "].directory[" + d + "].file[" + f + "].path", file.toString());
                    f ++;
                }
                d ++;
            }
        }

        Iterator<String> itSkip = _listSkip.iterator();
        int s = 1;
        while(itSkip.hasNext()) {
            String path = itSkip.next();
            setting.setSetting("skip[" + s +  "].path", path);
            s ++;
        }
    }

    @Override
    public String getReceiverName() {
        return "VSTRack";
    }

    @Override
    public JComponent getReceiverView() {
        return MX80Panel.getInstance();
    }

    @Override
    protected void processMXMessageImpl(MXMessage message) {
        //nothing
    }
    
    public interface Callback {
        public void vstScanProgress(String text, long hit, long total);
        public void vstScanCanceled();
        public void vstScanFinished();
    }
    
    public void startScan(boolean quick) {
        cleanCancelFlag();
        _thread = MXThreadList.newThread("MX80Process", new Runnable() {
            public void run() {
                _scanRealTotal = 0;
                for(VSTFolder filter : _listFolder) {
                    filter._cancelOperation = false;
                }
                for(VSTFolder filter : _listFolder) {
                    if (filter.isScanDone()) {
                        if (quick) {
                            continue;
                        }
                    }
                    if (filter._cancelOperation || _isThreadCancelling) {
                        _isThreadCancelling = true;
                        continue;
                    }
                    final String base = filter._rootDirectory.getPath();
                    filter.setCallback(new VSTFolder.Callback() {
                        @Override
                        public void seekingCallback(String text, long hit, long total) {
                            _callback.vstScanProgress(base, hit, total);
                        }
                    });
                    filter.scan(_listSkip); 
                }
                if (_isThreadCancelling) {
                    _callback.vstScanCanceled();
                }else {
                    _callback.vstScanFinished();;
                }
            }
        });
        _thread.start();
    }
    
    public void addBlackList(boolean effect, int synth) {
        if (effect) {
            _listEffectPanel.get(synth).noticeBlackListed();
        }
        else {
            _listInstrumentPanel.get(synth).noticeBlackListed();
        }
    }

    public VSTInstance getInstrument(int synth) {
        return _listInstrument.get(synth);
    }

    public VSTInstancePanel getInstrumentPanel(int synth) {
        return _listInstrumentPanel.get(synth);
    }

    public VSTInstance getEffect(int synth) {
        return _listEffect.get(synth);
    }

    public VSTInstancePanel getEffectPanel(int synth) {
        return _listEffectPanel.get(synth);
    }
}
