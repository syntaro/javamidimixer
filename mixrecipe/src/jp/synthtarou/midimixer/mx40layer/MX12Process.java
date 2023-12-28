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
package jp.synthtarou.midimixer.mx40layer;

import javax.swing.JPanel;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX12Process extends MXReceiver implements MXSettingTarget {

    private MXSetting _setting;
    MX12MasterkeysPanel _view;
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
        _setting = new MXSetting("MasterKey");
        _setting.setTarget(this);
        _noteOff = new MXNoteOffWatcher();
        _overwriteInputChannel = false;
        _construction = false;
    }

    public void readSettings() {
        _setting.readSettingFile();
        if (_view != null) {
            _view.updateViewForSettingChange();
        }
    }
   
    @Override
    public void prepareSettingFields(MXSetting setting) {
        setting.register("outputReceiver");
        setting.register("outputPort");
        setting.register("outputChannel");
        setting.register("overwriteControllerChannel");
        setting.register("outputVelocity");
        setting.register("acceptThisPanelSignal");
        setting.register("acceptInputPanelSignal");
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
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
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
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
    }

    @Override
    public String getReceiverName() {
        return "Master Key";
    }

    @Override
    public JPanel getReceiverView() {
        return _view;
    }
   
    public class MyNoteOffHandler implements MXNoteOffWatcher.Handler {
        MXReceiver _receiver;
        
        public MyNoteOffHandler(MXReceiver receiver) {
            _receiver = receiver;
        }

        @Override
        public void onNoteOffEvent(MXTiming timing, MXMessage target) {
            target._timing = timing;
            MXMain.getMain().messageDispatch(target, _receiver);
            if (_view != null) {
                _view._piano.noteOff(target.getGate()._value);
            }
        }
    }

    public void processMasterPath(MXMessage message) {
        if (message.isMessageTypeChannel()) {
            int port = message.getPort();
            int ch = message.getChannel();
            int status = message.getStatus();
            int data1 = message.getData1();
            int data2 = message.getData2();
            int command = status & 0xf0;

            if (command == MXMidi.COMMAND_CH_NOTEON && data2 == 0) {
                command = MXMidi.COMMAND_CH_NOTEOFF;
            }

            if (command == MXMidi.COMMAND_CH_NOTEOFF) {
               if (_noteOff.raiseHandler(port, message._timing, ch, data1)) {
                    return;
                }
            }

            MXMessage newMessage = null;
            if (isOverwriteInputChannel()) {
                newMessage = MXMessageFactory.fromClone(message);
                newMessage.setPort(getMousePort());
                if (newMessage.isMessageTypeChannel()) {
                    newMessage.setChannel(getMouseChannel());
                }
            }

            if (command == MXMidi.COMMAND_CH_NOTEON) {
                _view._piano.noteOn(data1);
                if (newMessage != null) {
                    _noteOff.setHandler(message, newMessage, new MyNoteOffHandler(getNextReceiver()));
                }else {
                    _noteOff.setHandler(message, message, new MyNoteOffHandler(getNextReceiver()));
                }
            }else if (command == MXMidi.COMMAND_CH_CONTROLCHANGE && data1 == MXMidi.DATA1_CC_DAMPERPEDAL) {
                _view._piano.sustain(data2);
            }else if (command == MXMidi.COMMAND_CH_PITCHWHEEL) {
                _view.setPitchBend(message.getValue()._value);
            }else if (command == MXMidi.COMMAND_CH_CONTROLCHANGE && data1 == MXMidi.DATA1_CC_MODULATION) {
                _view.setModulatoinWheel(message.getValue()._value);
            }
            if (newMessage != null) {
                sendToNext(newMessage);
            }else {
                sendToNext(message);
            }
            return;
        }
        sendToNext(message);
    }
    
    public void processMXMessage(MXMessage message) {
    }

    public void mouseMessage(MXMessage message) {
        if (_construction) {
            return;
        }
        sendToNext(message);
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
    
    public void createWindow() {
        if (_view != null) {
            if (_view.isOwnerwindowVisible()) { 
                return;
            }
        }
        _view = new MX12MasterkeysPanel(this);
        _view.showAsWindow();
    }
    
    public boolean isAvail() {
        if (_view == null) {
            return false;
        }else {
            if (!_view.isOwnerwindowVisible()) { 
                _view = null;
                return false;
            }
        }
        return true;
    }
}
