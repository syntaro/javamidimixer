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
package jp.synthtarou.midimixer;

import jp.synthtarou.midimixer.progress.MXProgressDialog;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.libs.MXSafeThread;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import javax.swing.JFrame;
import jp.synthtarou.midimixer.ccxml.xml.CXXMLManager;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.port.FinalMIDIOut;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIInManager;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOutManager;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.midimixer.libs.swing.themes.ThemeManager;
import jp.synthtarou.midimixer.mx80vst.MX80Process;
import jp.synthtarou.midimixer.libs.vst.VSTStream;
import jp.synthtarou.midimixer.mx00playlist.MX00Process;
import jp.synthtarou.midimixer.mx10input.MX10Process;
import jp.synthtarou.midimixer.mx30surface.MX30Process;
import jp.synthtarou.midimixer.mx40layer.MX40Process;
import jp.synthtarou.midimixer.mx60output.MX60Process;
import jp.synthtarou.midimixer.libs.midi.console.MXMidiConsoleElement;
import jp.synthtarou.libs.smf.SMFSequencer;
import jp.synthtarou.libs.MainThreadTask;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.midimixer.libs.vst.VSTInstance;
import jp.synthtarou.midimixer.mx36ccmapping.MX36Process;
import jp.synthtarou.midimixer.mx12masterpiano.MX12Process;
import jp.synthtarou.midimixer.mx50resolution.MX50Process;
import jp.synthtarou.midimixer.mx70console.MX70Process;
import jp.synthtarou.midimixer.mx90debug.MX90Process;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMain {

    private static MXMain _main = new MXMain();

    public static MXMain getMain() {
        return _main;
    }

    private MXMainWindow _mainWindow;

    public MXMainWindow getMainWindow() {
        return _mainWindow;
    }

    MXProgressDialog _progress;

    public static void progressIf(String line) {
        if (_main != null) {
            if (_main._progress != null) {
                _main._progress.progressTextLines(line);
            }
        }
    }

    public static void addOutsideInput(MXMessage msg) {
        if (getMain()._mx70CosoleProcess != null) {
            getMain()._mx70CosoleProcess.addOutsideInput(new MXMidiConsoleElement(msg));
        }
    }

    public static void addInsideInput(MXMessage msg) {
        if (getMain()._mx70CosoleProcess != null) {
            getMain()._mx70CosoleProcess.addInsideInput(msg);
        }
    }

    public static void addInsideOutput(MXMessage msg) {
        if (getMain()._mx70CosoleProcess != null) {
            getMain()._mx70CosoleProcess.addInsideOutput(msg);
        }
    }

    public static void addOutsideOutput(MXMidiConsoleElement msg) {
        if (getMain()._mx70CosoleProcess != null) {
            getMain()._mx70CosoleProcess.addOutsideOutput(msg);
        }
    }

    private MX10Process _mx10inputProcess;
    private MX00Process _mx00playlistProcess;
    private MX12Process _mx12SoftKeyboardProcess;
    private MX30Process _mx30kontrolProcess;
    private MX36Process _mx36ccmappingProcess;
    private MX40Process _mx40layerProcess;
    private MX50Process _mx50resolutionProcess;
    private MX60Process _mx60outputProcess;
    private MX70Process _mx70CosoleProcess;
    private MX80Process _mx80VstRack;
    private MX90Process _mx90Debugger;
    private CXXMLManager _mxXMLManager;

    public static MXReceiver _capture = null;

    public static void setCapture(MXReceiver capture) {
        _capture = capture;
    }

    /**
     * アプリを起動する
     *
     * @param args 引数
     * @throws Exception エラー通知
     */
    public static void main(String[] args) throws Exception {
        int x = 1 << 2 | 3;
        int y = 1 << 2 + 3;
        System.out.println("jp.synthtarou.midimixer.MXMain.main(x)" + x);
        System.out.println("jp.synthtarou.midimixer.MXMain.main(y)" + y);
        try {
            MXUtil.fixConsoleEncoding();
        } catch (Throwable ex) {
            MXFileLogger.getLogger(MXMain.class).log(Level.SEVERE, ex.getMessage(), ex);
        }
        try {
            //フォント描写でアンチエイリアスを有効にする
            System.setProperty("awt.useSystemAAFontSettings", "on");
            initProcessWithSetting(ThemeManager.getInstance());
        } catch (Throwable ex) {
            MXFileLogger.getLogger(MXMain.class).log(Level.SEVERE, ex.getMessage(), ex);
        }
        try {
            getMain().startUI();
        } catch (Throwable ex) {
            MXFileLogger.getLogger(MXMain.class).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void startUI() {
        _progress = new MXProgressDialog(null, false);
        _progress.setMessageAsStartUP();
        _progress.setVisible(true);

        if (!MXMIDIInManager.getManager().readJSonfile(null)) {
            if (!MXMIDIInManager.getManager().readINIFile(null)) {
                MXMIDIInManager.getManager().resetSetting();
            }
        }
        if (!MXMIDIOutManager.getManager().readJSonfile(null)) {
            if (!MXMIDIOutManager.getManager().readINIFile(null)) {
                MXMIDIOutManager.getManager().resetSetting();
            }
        }

        _mx00playlistProcess = new MX00Process();
        _mx10inputProcess = new MX10Process();
        _mx12SoftKeyboardProcess = new MX12Process();
        _mx30kontrolProcess = new MX30Process();
        _mx36ccmappingProcess = new MX36Process();
        _mx40layerProcess = new MX40Process();
        _mx50resolutionProcess = new MX50Process();

        _mx80VstRack = MX80Process.getInstance();
        _mx90Debugger = new MX90Process();
        _mxXMLManager = CXXMLManager.getInstance();

        _mx60outputProcess = new MX60Process();
        _mx70CosoleProcess = new MX70Process();

        _masterToList.addNameAndValue("*Auto", null);
        _masterToList.addNameAndValue(_mx10inputProcess.getReceiverName(), _mx10inputProcess);
        _masterToList.addNameAndValue(_mx30kontrolProcess.getReceiverName(), _mx30kontrolProcess);
        _masterToList.addNameAndValue(_mx36ccmappingProcess.getReceiverName(), _mx36ccmappingProcess);
        _masterToList.addNameAndValue(_mx40layerProcess.getReceiverName(), _mx40layerProcess);
        _masterToList.addNameAndValue(_mx50resolutionProcess.getReceiverName(), _mx50resolutionProcess);
        _masterToList.addNameAndValue(_mx60outputProcess.getReceiverName(), _mx60outputProcess);
        _masterToList.addNameAndValue("Direct Output", FinalMIDIOut.getInstance());

        _mx10inputProcess.setNextReceiver(_mx30kontrolProcess);

        if (false) {
            _mx30kontrolProcess.setNextReceiver(_mx50resolutionProcess);
            _mx30kontrolProcess.setMappingProcess(_mx36ccmappingProcess);
            _mx36ccmappingProcess.setNextReceiver(_mx50resolutionProcess);
        } else {
            _mx30kontrolProcess.setNextReceiver(_mx40layerProcess);
            _mx30kontrolProcess.setMappingProcess(_mx36ccmappingProcess);
            _mx36ccmappingProcess.setNextReceiver(_mx40layerProcess);
            _mx40layerProcess.setNextReceiver(_mx50resolutionProcess);
        }
        _mx50resolutionProcess.setNextReceiver(_mx60outputProcess);
        _mx60outputProcess.setNextReceiver(FinalMIDIOut.getInstance());

        initProcessWithSetting(_mx30kontrolProcess);
        initProcessWithSetting(_mx00playlistProcess);
        _mx10inputProcess.readJSonfile(null);
        initProcessWithSetting(_mx12SoftKeyboardProcess);
        initProcessWithSetting(_mx36ccmappingProcess);
        _mx60outputProcess.readJSonfile(null);
        initProcessWithSetting(_mx40layerProcess);
        initProcessWithSetting(_mx50resolutionProcess);
        initProcessWithSetting(_mx80VstRack);

        _mainWindow = new MXMainWindow(this);
        _mainWindow.setEnabled(false);
        _mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        _mainWindow.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                AppCloseTask(true);
            }

            public void windowClosed(WindowEvent e) {
            }
        });

        ArrayList<MXReceiver> reList = new ArrayList();
        reList.add(_mx00playlistProcess);
        reList.add(_mx10inputProcess);
        reList.add(_mx30kontrolProcess);
        reList.add(_mx36ccmappingProcess);
        reList.add(_mx40layerProcess);
        reList.add(_mx50resolutionProcess);
        reList.add(_mx60outputProcess);
        reList.add(_mx80VstRack);
        reList.add(_mxXMLManager);
        reList.add(_mx90Debugger);
        reList.add(_mx70CosoleProcess);

        new MainThreadTask(() -> {
            _mainWindow.initLatebind(reList);

            if (_progress != null) {
                _progress.setVisible(false);
                _progress = null;
            }
            _mainWindow.setVisible(true);

            Runnable run;
            while ((run = getNextLaunchSequence()) != null) {
                run.run();
            }

            _mainWindow.setEnabled(true);
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        });
    }

    LinkedList<Runnable> _startQueue = new LinkedList();

    public void addLaunchSequence(Runnable run) {
        _startQueue.add(run);
    }

    public Runnable getNextLaunchSequence() {
        if (_startQueue.isEmpty()) {
            return null;
        }
        return _startQueue.removeFirst();
    }

    private MXNamedObjectList<MXReceiver> _masterToList = new MXNamedObjectList();

    public MXNamedObjectList<MXReceiver> getReceiverList() {
        MXNamedObjectList<MXReceiver> list = new MXNamedObjectList();
        if (_masterToList.size() == 0) {
            _masterToList.addNameAndValue("Direct Output", FinalMIDIOut.getInstance());
        }
        list.addAll(_masterToList);
        return list;
    }

    public void saveEverySettingToFile() {
        MXJsonParser.invokeAutosave();
        MXINIFile.invokeAutoSave();
    }

    public void exitWithoutSave() {
        AppCloseTask(false);
    }

    public void saveAndExit() {
        AppCloseTask(true);
    }

    public void AppCloseTask(boolean save) {
        _progress = new MXProgressDialog(_mainWindow, false);
        _progress.setMessageAsExit();
        _progress.setVisible(true);
        Thread t = new Thread(() -> {
            if (save) {
                try {
                    saveEverySettingToFile();
                } catch (RuntimeException ex) {
                    MXFileLogger.getLogger(MXMain.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }

            try {
                VSTStream.getInstance().postCloseStream(null);
            } catch (Throwable ex) {
                MXFileLogger.getLogger(MXMain.class).log(Level.WARNING, ex.getMessage(), ex);
            }

            try {
                SMFSequencer.stopAll();
                MXMIDIInManager.getManager().closeAll();
                MXMIDIOutManager.getManager().closeAll();
                MXSafeThread.exitAll();
            } catch (RuntimeException ex) {
                MXFileLogger.getLogger(MXMain.class).log(Level.WARNING, ex.getMessage(), ex);
            }

            try {
                MXFileLogger.getLogger(MXMain.class).info("stopping vst");
                VSTInstance.stopEngine(null);
                MXFileLogger.getLogger(MXMain.class).info("stopped vst");
            } catch (Throwable ex) {
                MXFileLogger.getLogger(MXMain.class).log(Level.WARNING, ex.getMessage(), ex);
            }

            /*
            try {
                VSTStream.getInstance().forceTerminate();
            } catch (Throwable ex) {
                MXFileLogger.getLogger(MXMain.class).log(Level.WARNING, ex.getMessage(), ex);
            }*/

            System.out.println("System.exit(0)");
            System.exit(0);

        });
        t.start();
    }

    public MX10Process getInputProcess() {
        return _mx10inputProcess;
    }

    public MX00Process getPlayListProcess() {
        return _mx00playlistProcess;
    }

    public MX30Process getKontrolProcess() {
        return _mx30kontrolProcess;
    }

    public MX36Process getCCMappingProcess() {
        return _mx36ccmappingProcess;
    }

    public MX40Process getLayerProcess() {
        return _mx40layerProcess;
    }

    public MX12Process getMasterkeyProcess() {
        return _mx12SoftKeyboardProcess;
    }

    public CXXMLManager getXMLManager() {
        return _mxXMLManager;
    }

    public MXNamedObjectList<MXReceiver> listSendableReceiver() {
        MXNamedObjectList<MXReceiver> ret = new MXNamedObjectList<>();
        ret.addNameAndValue("Auto", null);

        MXReceiver[] list = {
            _mx10inputProcess,
            _mx30kontrolProcess,
            _mx36ccmappingProcess,
            _mx40layerProcess,
            _mx50resolutionProcess,
            _mx60outputProcess,};

        for (MXReceiver seek : list) {
            ret.addNameAndValue(seek.getReceiverName(), seek);
        }
        return ret;
    }

    public MXReceiver getAutoSendableReceiver() {
        if (_mainWindow == null) {
            return _mx10inputProcess;
        }
        MXReceiver receiver = _mainWindow.getSelectedReceiver();
        if (receiver == _mx80VstRack) {
            return _mx60outputProcess;
        }
        if (receiver == _mx00playlistProcess
                || receiver == _mxXMLManager
                || receiver == null
                || receiver == _mx90Debugger
                || receiver == _mx70CosoleProcess) {
            return _mx10inputProcess;
        }
        return receiver;
    }

    public static void initProcessWithSetting(MXINIFileSupport support) {
        if (support == null) {
            throw new NullPointerException();
        }
        boolean done = false;
        if (support instanceof MXJsonSupport) {
            done = ((MXJsonSupport) support).readJSonfile(null);
            MXFileLogger.getLogger(support.getClass()).info("tried read json = " + done);
        }
        if (!done) {
            done = support.readINIFile(null);
            MXFileLogger.getLogger(support.getClass()).info("tried read ini= " + done);
        }
        if (!done) {
            support.resetSetting();
            MXFileLogger.getLogger(support.getClass()).info("tried reset setting");
        }
    }
}
