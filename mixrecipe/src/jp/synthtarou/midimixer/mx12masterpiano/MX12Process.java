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
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.libs.accordionui.MXAccordion;
import jp.synthtarou.libs.json.MXJsonValue;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX12Process extends MXReceiver<MXAccordion> implements MXINIFileSupport, MXJsonSupport  {

    MX12MasterkeysPanel _view;
    MXAccordion _accordion;
    MXNoteOffWatcher _noteOff;
    
    private int _mousePort = 0;
    private int _mouseChannel = 0;
    private boolean _overwriteInputChannel;
    private int _mouseVelocity = 100;

    boolean _construction;
    
    private boolean _acceptThisPageSignal;
    private boolean _acceptInputPanelSignal;
    
    public MX12Process() {
        _construction = true;
        _noteOff = new MXNoteOffWatcher();
        _overwriteInputChannel = false;
        _construction = false;
        _view = new MX12MasterkeysPanel(this);
        _accordion = new MXAccordion(null, "Virtual Key", true);
        _accordion.openAccordion(false);
        _accordion.insertElement(0, _view);
        _accordion.setLabelAfterName(_view.getComponentAfterName());
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
        setOverwriteInputChannel(setting.getSettingAsInt("overwriteControllerChannel", 0) != 0);        
        setMousePort(setting.getSettingAsInt("outputPort", 0));
        setMouseChannel(setting.getSettingAsInt("outputChannel", 0));
        setMouseVelocity(setting.getSettingAsInt("outputVelocity", 100));
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
        setting.setSetting("outputPort", getMousePort());
        setting.setSetting("outputChannel", getMouseChannel());
        setting.setSetting("overwriteControllerChannel", isOverwriteInputChannel());
        setting.setSetting("outputVelocity", getMouseVelocity());
        setting.setSetting("acceptThisPanelSignal", _acceptThisPageSignal);
        setting.setSetting("acceptInputPanelSignal", _acceptInputPanelSignal);
        return setting.writeINIFile();
    }

    @Override
    public String getReceiverName() {
        return "Virtual Key";
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
        setOverwriteInputChannel(root.getFollowingInt("overwriteControllerChannel", 0) != 0);        
        setMousePort(root.getFollowingInt("outputPort", 0));
        setMouseChannel(root.getFollowingInt("outputChannel", 0));
        setMouseVelocity(root.getFollowingInt("outputVelocity", 100));
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
        root.setFollowingInt("outputPort", getMousePort());
        root.setFollowingInt("outputChannel", getMouseChannel());
        root.setFollowingBool("overwriteControllerChannel", isOverwriteInputChannel());
        root.setFollowingInt("outputVelocity", getMouseVelocity());
        root.setFollowingBool("acceptThisPanelSignal", _acceptThisPageSignal);
        root.setFollowingBool("acceptInputPanelSignal", _acceptInputPanelSignal);

        return parser.writeFile();
    }

    @Override
    public void resetSetting() {
    }

    public class MyNoteOffHandler implements MXNoteOffWatcher.Handler {
        MXReceiver _receiver;
        
        public MyNoteOffHandler(MXReceiver receiver) {
            _receiver = receiver;
        }

        @Override
        public void onNoteOffEvent(MXMessage target) {
            MXMIDIIn.messageToReceiverThreaded(target, _receiver);
            _view._piano.noteOff(target.getGate()._value);
        }
    }

    public void processMXMessage(MXMessage message) {
    }

    public void sentMessageByMouse(MXMessage message) {
        MXReceiver receiver = getNextReceiver();
        if (receiver == null) {
            receiver = MXMain.getMain().getAutoSendableReceiver();
        }
        MXMIDIIn.messageToReceiverThreaded(message, receiver);
    }

    /**
     * @return the acceptThisPageSignal
     */
    public boolean isAcceptThisPageSignal() {
        return _acceptThisPageSignal;
    }

    /**
     * @param acceptThisPageSignal the acceptThisPageSignal to set
     */
    public void setAcceptThisPageSignal(boolean acceptThisPageSignal) {
        this._acceptThisPageSignal = acceptThisPageSignal;
    }

    /**
     * @return the acceptInputPanelSignal
     */
    public boolean isAcceptInputPanelSignal() {
        return _acceptInputPanelSignal;
    }

    /**
     * @param acceptInputPanelSignal the acceptInputPanelSignal to set
     */
    public void setAcceptInputPanelSignal(boolean acceptInputPanelSignal) {
        this._acceptInputPanelSignal = acceptInputPanelSignal;
    }

    /**
     * @return the _mousePort
     */
    public int getMousePort() {
        return _mousePort;
    }

    /**
     * @param _mousePort the _mousePort to set
     */
    public void setMousePort(int _mousePort) {
        this._mousePort = _mousePort;
    }

    /**
     * @return the _mouseChannel
     */
    public int getMouseChannel() {
        return _mouseChannel;
    }

    /**
     * @param _mouseChannel the _mouseChannel to set
     */
    public void setMouseChannel(int _mouseChannel) {
        this._mouseChannel = _mouseChannel;
    }

    /**
     * @return the overwriteInputChannel
     */
    public boolean isOverwriteInputChannel() {
        return _overwriteInputChannel;
    }

    /**
     * @param overwriteInputChannel the overwriteInputChannel to set
     */
    public void setOverwriteInputChannel(boolean overwriteInputChannel) {
        this._overwriteInputChannel = overwriteInputChannel;
    }

    /**
     * @return the _mouseVelocity
     */
    public int getMouseVelocity() {
        return _mouseVelocity;
    }

    /**
     * @param _mouseVelocity the _mouseVelocity to set
     */
    public void setMouseVelocity(int _mouseVelocity) {
        this._mouseVelocity = _mouseVelocity;
    }
    

    public void updateViewForSettingChange() {
        _view.updateViewForSettingChange();
    }
}
