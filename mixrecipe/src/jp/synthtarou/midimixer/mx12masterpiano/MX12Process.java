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
package jp.synthtarou.midimixer.mx12masterpiano;

import java.io.File;
import java.util.List;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.libs.accordionui.MXAccordion;
import jp.synthtarou.libs.json.MXJsonValue;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import jp.synthtarou.midimixer.libs.midi.console.MXMidiConsoleElement;
import jp.synthtarou.midimixer.libs.midi.port.FinalMIDIOut;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX12Process extends MXReceiver<MXAccordion> implements MXINIFileSupport, MXJsonSupport  {

    MX12MasterkeysPanel _view;
    MXAccordion _accordion;
    MXNoteOffWatcher _noteOff;
    
    int _mousePort = 0;
    int _mouseChannel = 0;
    boolean _overwriteInputChannel;
    int _mouseVelocity = 100;

    int _construction = 1;
    
    private boolean _acceptThisPageSignal;
    private boolean _acceptInputPanelSignal;
    
    public MX12Process() {
        _noteOff = new MXNoteOffWatcher();
        _overwriteInputChannel = false;
        _view = new MX12MasterkeysPanel(this);
        _accordion = new MXAccordion(null, "Virtual Key", true);
        _accordion.openAccordion(false);
        _accordion.insertElement(0, _view);
        _accordion.setLabelAfterName(_view.getComponentAfterName());
        _construction --;
    }

    @Override
    public MXINIFile prepareINIFile(File custom) {
        if (custom == null) {
            custom = MXINIFile.pathOf("VirtualKey");
        }
        MXINIFile setting = new MXINIFile(custom, this);
        setting.register("outputReceiver");
        setting.register("outputPort");
        setting.register("outputChannel");
        setting.register("overwriteControllerChannel");
        setting.register("outputVelocity");
        setting.register("acceptThisPanelSignal");
        setting.register("acceptInputPanelSignal");
        return setting;
    }

    @Override
    public boolean readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        if (!setting.readINIFile()) {
            return false;
        }
        String receiverName = setting.getSetting("outputReceiver");
        if (receiverName != null) {
            int x = MXMain.getMain().getReceiverList().indexOfName(receiverName);
            if (x >= 0) {
                setNextReceiver(MXMain.getMain().getReceiverList().get(x)._value);
            }
        }
        _overwriteInputChannel = setting.getSettingAsInt("overwriteControllerChannel", 0) != 0;
        _mousePort = setting.getSettingAsInt("outputPort", 0);
        _mouseChannel = setting.getSettingAsInt("outputChannel", 0);
        _mouseVelocity = setting.getSettingAsInt("outputVelocity", 100);
        _acceptThisPageSignal = setting.getSettingAsBoolean("acceptThisPanelSignal", true);
        _acceptInputPanelSignal = setting.getSettingAsBoolean("acceptInputPanelSignal", true);
        _view.updateViewForSettingChange();
        return true;
    }

    @Override
    public boolean writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        if (getNextReceiver() == null) {
            setting.setSetting("outputReceiver", "");
        }else {
            setting.setSetting("outputReceiver", getNextReceiver().getReceiverName());
        }   
        setting.setSetting("outputPort", _mousePort);
        setting.setSetting("outputChannel", _mouseChannel);
        setting.setSetting("overwriteControllerChannel", _overwriteInputChannel);
        setting.setSetting("outputVelocity", _mouseVelocity);
        setting.setSetting("acceptThisPanelSignal", _acceptThisPageSignal);
        setting.setSetting("acceptInputPanelSignal", _acceptInputPanelSignal);
        return setting.writeINIFile();
    }

    @Override
    public String getReceiverName() {
        return "Key";
    }

    @Override
    public MXAccordion getReceiverView() {
        return _accordion;
    }

    @Override
    public boolean readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("VirtualKey");
            MXJsonParser.setAutosave(this);
        }
        MXJsonValue value = new MXJsonParser(custom).parseFile();
        if (value == null) {
            return false;
        }
        MXJsonValue.HelperForStructure root = value.new HelperForStructure();
        
        String receiverName = root.getFollowingText("outputReceiver", "");
        if (receiverName.isBlank() == false) {
            int x = MXMain.getMain().getReceiverList().indexOfName(receiverName);
            if (x >= 0) {
                setNextReceiver(MXMain.getMain().getReceiverList().get(x)._value);
            }
        }
        _overwriteInputChannel = root.getFollowingInt("overwriteControllerChannel", 0) != 0;        
        _mousePort = root.getFollowingInt("outputPort", 0);
        _mouseChannel = root.getFollowingInt("outputChannel", 0);
        _mouseVelocity = root.getFollowingInt("outputVelocity", 100);
        _acceptThisPageSignal = root.getFollowingBool("acceptThisPanelSignal", true);
        _acceptInputPanelSignal = root.getFollowingBool("acceptInputPanelSignal", true);
        _view.updateViewForSettingChange();
        return true;
    }

    @Override
    public boolean writeJsonFile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("VirtualKey");
        }
        MXJsonParser parser = new MXJsonParser(custom);
        MXJsonValue.HelperForStructure root = parser.getRoot().new HelperForStructure();

        if (getNextReceiver() == null) {
            root.setFollowingText("outputReceiver", "");
        }else {
            root.setFollowingText("outputReceiver", getNextReceiver().getReceiverName());
        }   
        root.setFollowingInt("outputPort", _mousePort);
        root.setFollowingInt("outputChannel", _mouseChannel);
        root.setFollowingBool("overwriteControllerChannel", _overwriteInputChannel);
        root.setFollowingInt("outputVelocity", _mouseVelocity);
        root.setFollowingBool("acceptThisPanelSignal", _acceptThisPageSignal);
        root.setFollowingBool("acceptInputPanelSignal", _acceptInputPanelSignal);

        return parser.writeFile();
    }

    @Override
    public void resetSetting() {
        _view.updateViewForSettingChange();
    }

    public class MyNoteOffHandler implements MXNoteOffWatcher.Handler {
        MXReceiver _receiver;
        
        public MyNoteOffHandler(MXReceiver receiver) {
            _receiver = receiver;
        }

        @Override
        public void onNoteOffEvent(MXMessage target) {
            MXMIDIIn.messageToReceiverThreaded(target, _receiver);
            _view._piano.noteOff(target.getCompiled(1));
        }
    }

    public void processMXMessage(MXMessage message) {
    }

    List<MXMessage> _debugResult = null;
    
    public void sendCCAndGetResult(MXMessage message, MXReceiver receiver) {
        if (receiver == null) {
            receiver = getNextReceiver();
            if (receiver == null) {
                receiver = MXMain.getMain().getAutoSendableReceiver();
            }
        }
        FinalMIDIOut.getInstance().startTestSignal(message, -1);
        MXMIDIIn.messageToReceiverThreaded(message, receiver);
        List<MXMessage> result;

        if (_debugResult != null) {
            MXMIDIIn.queueMustEmpty();
            result = FinalMIDIOut.getInstance().getTestResult();
           _debugResult.addAll(result);
            MXMain.invokeUI(() ->  {
                _view._input.addElement2(new MXMidiConsoleElement(message));
                for (MXMessage seek : result) {
                    _view._output.addElement2(new MXMidiConsoleElement(seek));
                }
            });
        }
        else {
            MXMain.invokeUI(() ->  {
                _view._input.addElement2(new MXMidiConsoleElement(message));
            });
        }
    }
    
    public void startDebug(List<MXMessage> result) {
        _debugResult = result;
    }
    
    public void updateViewForSettingChange() {
        _view.updateViewForSettingChange();
    }

    public void openAccordionDebugMode() {
        _view.setDebugMode(true);
        _accordion.openAccordion(true);
    }
}
 