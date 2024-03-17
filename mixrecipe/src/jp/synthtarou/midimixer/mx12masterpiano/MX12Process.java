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

import javax.swing.JPanel;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.accordion.MXAccordion;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX12Process extends MXReceiver<MXAccordion> implements MXSettingTarget {

    private MXSetting _setting;
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
        _setting = new MXSetting("VirtualKey");
        _setting.setTarget(this);
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
    public MXSetting getSettings() {
        return _setting;
    }

    @Override
    public void prepareSettingFields() {
        _setting.register("outputReceiver");
        _setting.register("outputPort");
        _setting.register("outputChannel");
        _setting.register("overwriteControllerChannel");
        _setting.register("outputVelocity");
        _setting.register("acceptThisPanelSignal");
        _setting.register("acceptInputPanelSignal");
    }

    @Override
    public void afterReadSettingFile() {
        String receiverName = _setting.getSetting("outputReceiver");
        if (receiverName != null) {
            int x = MXMain.getMain().getReceiverList().indexOfName(receiverName);
            if (x >= 0) {
                setNextReceiver(MXMain.getMain().getReceiverList().get(x)._value);
            }
        }
        setOverwriteInputChannel(_setting.getSettingAsInt("overwriteControllerChannel", 0) != 0);        
        setMousePort(_setting.getSettingAsInt("outputPort", 0));
        setMouseChannel(_setting.getSettingAsInt("outputChannel", 0));
        setMouseVelocity(_setting.getSettingAsInt("outputVelocity", 100));
        _acceptThisPageSignal = _setting.getSettingAsBoolean("acceptThisPanelSignal", true);
        _acceptInputPanelSignal = _setting.getSettingAsBoolean("acceptInputPanelSignal", true);
        _view.updateViewForSettingChange();
    }

    @Override
    public void beforeWriteSettingFile() {
        if (getNextReceiver() == null) {
            _setting.setSetting("outputReceiver", "");
        }else {
            _setting.setSetting("outputReceiver", getNextReceiver().getReceiverName());
        }
        _setting.setSetting("outputPort", getMousePort());
        _setting.setSetting("outputChannel", getMouseChannel());
        _setting.setSetting("overwriteControllerChannel", isOverwriteInputChannel());
        _setting.setSetting("outputVelocity", getMouseVelocity());
        _setting.setSetting("acceptThisPanelSignal", _acceptThisPageSignal);
        _setting.setSetting("acceptInputPanelSignal", _acceptInputPanelSignal);
    }

    @Override
    public String getReceiverName() {
        return "Virtual Key";
    }

    @Override
    public MXAccordion getReceiverView() {
        return _accordion;
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
            _view._piano.noteOff(target.getGate()._value);
        }
    }

    public void processMXMessage(MXMessage message) {
    }

    public void mouseMessage(MXMessage message) {
        MXReceiver receiver = getNextReceiver();
        if (receiver == null) {
            receiver = MXMain.getMain().getActiveSendableReceiver();
        }
        MXMain.getMain().messageDispatch(message, receiver);
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
