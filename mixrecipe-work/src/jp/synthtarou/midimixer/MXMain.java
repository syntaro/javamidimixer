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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.capture.MXMessageCapture;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.port.FinalMIDIOut;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIInManager;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOutManager;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.swing.themes.ThemeManager;
import jp.synthtarou.midimixer.mx80vst.MX80Process;
import jp.synthtarou.midimixer.libs.vst.VSTStream;
import jp.synthtarou.midimixer.mx00playlist.MX00Process;
import jp.synthtarou.midimixer.mx10input.MX10Process;
import jp.synthtarou.midimixer.mx30controller.MX30Process;
import jp.synthtarou.midimixer.mx40layer.MX40Process;
import jp.synthtarou.midimixer.mx60output.MX60Process;
import jp.synthtarou.midimixer.libs.console.ConsoleElement;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;
import jp.synthtarou.midimixer.libs.vst.VSTInstance;
import jp.synthtarou.midimixer.mx35cceditor.MX35Process;
import jp.synthtarou.midimixer.mx70console.MX70Process;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMain  {
    private static MXMain _main = new MXMain(); 
    
    public static MXMain getMain() {
        return _main;
    }
    
    private MXMainWindow _mainWindow;

    public MXMainWindow getMainWindow() {
        return _mainWindow;
    }
    
    /*
    public MX12Process getMasterKeys() {
        return _mx12masterkeyProcess;
    }*/

    public static void addOutsideInput(MXMessage msg) {
        if (getMain()._mx70CosoleProcess != null) {
            getMain()._mx70CosoleProcess.addOutsideInput(new ConsoleElement(msg));
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

    public static void addOutsideOutput(ConsoleElement msg) {
        if (getMain()._mx70CosoleProcess != null) {
            getMain()._mx70CosoleProcess.addOutsideOutput(msg);
        }
    }

    private MX10Process _mx10inputProcess;
    //private MX12Process _mx12masterkeyProcess;
    private MX00Process _mx00playlistProcess;
    private MX30Process _mx30kontrolProcess;
    private MX35Process _mx35cceditorProcess;
    private MX40Process _mx40layerProcess;
    private MX60Process _mx60outputProcess;
    private MX70Process _mx70CosoleProcess;
    private MX80Process _vstRack;
    
    public static MXMessageCapture _capture = null;
    
    public static void setCapture(MXMessageCapture capture) {
        _capture = capture;
    }
    
    /**
     * アプリを起動する
     * @param args 引数
     * @throws Exception エラー通知
     */
    public static void main(String[] args) throws Exception {
        try {
            MXUtil.fixConsoleEncoding();
            MXDebugPrint.initDebugLine(args);

            //フォント描写でアンチエイリアスを有効にする
            System.setProperty("awt.useSystemAAFontSettings", "on");
            
            try {
                ThemeManager inst = ThemeManager.getInstance();
                inst.setUITheme("Nimbus");
            }catch( Throwable e ) {
            }
        
        } catch (Throwable e) {
        }

        getMain().startUI();
    }
    
    public void startUI()  {
        _mainWindow = new MXMainWindow(this);

        MXOpening winLogo = MXOpening.showAsStartup(_mainWindow);        
        winLogo.setVisible(true);

        MXMIDIInManager.getManager().initWithSetting();
        MXMIDIOutManager.getManager().initWithSetting();

        winLogo.showProgress(0, 10);

        _mx00playlistProcess = new MX00Process();
        _mx10inputProcess = new MX10Process();
        //_mx12masterkeyProcess = new MX12Process();
        winLogo.showProgress(1, 10);
        _mx30kontrolProcess =  new MX30Process();
        _mx35cceditorProcess = new MX35Process();
        _mx40layerProcess = new MX40Process();
        _vstRack = MX80Process.getInstance();

        winLogo.showProgress(2, 10);

        _mx60outputProcess = new MX60Process();
        _mx70CosoleProcess = new MX70Process();
        
        _masterToList = new MXWrapList();

        winLogo.showProgress(3, 10);

        _masterToList.addNameAndValue("PushBack", MXMIDIIn.returnReceirer);
        _masterToList.addNameAndValue(_mx30kontrolProcess.getReceiverName(), _mx30kontrolProcess);
        _masterToList.addNameAndValue(_mx35cceditorProcess.getReceiverName(), _mx35cceditorProcess);
        _masterToList.addNameAndValue(_mx40layerProcess.getReceiverName(), _mx40layerProcess);
        _masterToList.addNameAndValue(_mx60outputProcess.getReceiverName(), _mx60outputProcess);
        _masterToList.addNameAndValue("Direct Output", FinalMIDIOut.getInstance());

        _mx10inputProcess.setNextReceiver(_mx30kontrolProcess);
        winLogo.showProgress(4, 10);

        _mx30kontrolProcess.setNextReceiver(_mx35cceditorProcess);
        _mx35cceditorProcess.setNextReceiver(_mx40layerProcess);
        _mx40layerProcess.setNextReceiver(_mx60outputProcess);
        _mx60outputProcess.setNextReceiver(FinalMIDIOut.getInstance());

        winLogo.showProgress(5, 10);

        _mx30kontrolProcess.readSettings();
        //_mx12masterkeyProcess.readSettings();
        _mx00playlistProcess.readSettings();
        
        winLogo.showProgress(6, 10);

        _mx10inputProcess.readSettings();
        _mx60outputProcess.readSettings();
        _mx40layerProcess.readSettings();

        _mx70CosoleProcess.readSettings();                

        winLogo.showProgress(7, 10);
        _mainWindow.setEnabled(false);
        _mainWindow.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                MXSetting.saveEverySettingToFile();
                try {
                    VSTStream.getInstance().postCloseStream(null);
                }catch(Throwable ex) {
                    ex.printStackTrace();
                }
            }
            public void windowClosed(WindowEvent e){
                try {
                    MXMIDIInManager.getManager().closeAll();
                    MXMIDIOutManager.getManager().closeAll();

                    System.out.println("stopThread");
                    MXThreadList.onExit();
                }catch(Throwable ex) {
                    ex.printStackTrace();
                }
                System.out.println("stopEnding");
                VSTInstance.stopEngine(null);
                System.out.println("exit");
                System.exit(0);
            }
        });

        winLogo.showProgress(8, 10);
        ArrayList<MXReceiver> reList = new ArrayList();
        reList.add(_mx00playlistProcess);
        reList.add(_mx10inputProcess);
        //reList.add(_velocityProcess);
        reList.add(_mx30kontrolProcess);
        reList.add(_mx35cceditorProcess);
        reList.add(_mx40layerProcess);
        reList.add(_mx60outputProcess);
        reList.add(_vstRack);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                _mainWindow.initLatebind(reList);
                _mainWindow.setVisible(true);

                winLogo.showProgress(9, 10);

                Runnable run;
                while((run = getNextLaunchSequence()) != null) {
                    run.run();
                }

                winLogo.showProgress(10, 10);
                winLogo.setVisible(false);
                _mainWindow.setEnabled(true);
            }
        });
    }
    
    LinkedList<Runnable> _startQueue = new LinkedList();
    
    public void addLaunchSequence(Runnable run) {
        synchronized(MXTiming.mutex) {
            _startQueue.add(run);
        }
    }
    
    public Runnable getNextLaunchSequence() {
        synchronized(MXTiming.mutex) {
            if (_startQueue.isEmpty()) {
                return null;
            }
            return _startQueue.removeFirst();
        }
    }
    
    public void openFreeConsole() {
        _mx70CosoleProcess.createWindow();
    }

    private MXWrapList<MXReceiver> _masterToList = null;
    
    public MXWrapList<MXReceiver> getReceiverList() {
        MXWrapList<MXReceiver> list = new MXWrapList();
        list.addAll(_masterToList);
        return list;
    }

    public void saveEverySettingToFile() {
        MXSetting.saveEverySettingToFile();
    }
    
    public MX10Process getInputProcess() {
        return _mx10inputProcess;
    }

/*    
    public void masterKeyDispatch(MXMessage message) {
        _mx12masterkeyProcess.processMasterPath(message);
    }
*/

    public void messageDispatch(MXMessage message, MXReceiver receiver) {
        synchronized(MXTiming.mutex) {
            try {
                if (message._timing == null) {
                    message._timing = new MXTiming();
                }
                if (receiver == _mx10inputProcess) {
                    MXMain.addInsideInput(message);
                    if (_capture != null) {
                        _capture.process(message);
                    }   
                }
                if (receiver != null) {
                    receiver.processMXMessage(message);
                }
            }catch(Throwable e) {
                e.printStackTrace();
            }
        }
    }
    
    public MX00Process getPlayListProcess() {
        return _mx00playlistProcess;
    }
    
    public MX30Process getKontrolProcess() {
        return _mx30kontrolProcess;
    }

    public MX35Process getCCEditorProcess() {
        return _mx35cceditorProcess;
    }

    public MX40Process getLayerProcess() {
        return _mx40layerProcess;
    }
}
