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
package jp.synthtarou.midimixer.mx30controller;

import java.awt.font.NumericShaper;
import jp.synthtarou.midimixer.libs.UniqueChecker;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.midi.MXScaledNumber;
import jp.synthtarou.midimixer.libs.MXGlobalTimer;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingNode;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant16;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX32MixerProcess extends MXReceiver implements MXSettingTarget {
    final int _port;
    final MX30Process _parent;
    final MX32MixerView _view;
    MXNoteOffWatcher _noteOff;
    MXSetting _setting;
    
    MXVisitant16 _visitant16 = new MXVisitant16();
    
    String _mixerName;

    MX32MixerData _data;

    int _patchToMixer = -1;
    boolean  _patchTogether = false;    

    public MX32MixerProcess(MX30Process parent, int port) {
        _parent = parent;
        _port = port;
        _data = new MX32MixerData(this);
        _view = new MX32MixerView(this);
        _setting = new MXSetting("Mixing" + (port+1));
        _setting.setTarget(this);
    }

    public void readSettings() {
        _setting.readSettingFile();
        _view.updateUI();
    }

    @Override
    public String getReceiverName() {
        return "#" + MXMidi.nameOfPortShort(_port);
    }

    @Override
    public JPanel getReceiverView() {
        return _view;
    }

    @Override
    protected void processMXMessageImpl(MXMessage message) {
        if (isUsingThisRecipe() == false) { 
            sendToNext(message); return; 
        }
        letsTryMessage(message, new UniqueChecker(_parent.getNextReceiver()));
        //if (message.isDataentry() && message.getVisitant().getDataentryValue14() == 0 && message._trace == null) { message._trace = new Throwable(); }
    }

    @Override
    public void prepareSettingFields(MXSetting setting) {
        setting.register("PatchToMixer");
        
        setting.register("Circle[].name");
        setting.register("Circle[].note");
        setting.register("Circle[].type");
        setting.register("Circle[].row");
        setting.register("Circle[].column");
        setting.register("Circle[].message");
        setting.register("Circle[].channel");
        setting.register("Circle[].gate");
        setting.register("Circle[].value");
        setting.register("Circle[].valuemin");
        setting.register("Circle[].valuemax");
        setting.register("Circle[].isCCPair");
        setting.register("Circle[].valueinvert");
        setting.register("Circle[].attributes");

        setting.register("Circle[].dataentryType");
        setting.register("Circle[].dataentryMSB");
        setting.register("Circle[].dataentryLSB");

        setting.register("Slider[].name");
        setting.register("Slider[].note");
        setting.register("Slider[].type");
        setting.register("Slider[].row");
        setting.register("Slider[].column");
        setting.register("Slider[].message");
        setting.register("Slider[].channel");
        setting.register("Slider[].gate");
        setting.register("Slider[].value");
        setting.register("Slider[].valuemin");
        setting.register("Slider[].valuemax");
        setting.register("Slider[].isCCPair");
        setting.register("Slider[].valueinvert");
        setting.register("Slider[].attributes");

        setting.register("Slider[].dataentryType");
        setting.register("Slider[].dataentryMSB");
        setting.register("Slider[].dataentryLSB");

        setting.register("Pad[].name");
        setting.register("Pad[].note");
        setting.register("Pad[].type");
        setting.register("Pad[].row");
        setting.register("Pad[].column");
        setting.register("Pad[].message");
        setting.register("Pad[].channel");
        setting.register("Pad[].gate");
        setting.register("Pad[].value");
        setting.register("Pad[].valuemin");
        setting.register("Pad[].valuemax");
        setting.register("Pad[].isCCPair");
        setting.register("Pad[].valueinvert");
        setting.register("Pad[].attributes");

        setting.register("Pad[].dataentryType");
        setting.register("Pad[].dataentryMSB");
        setting.register("Pad[].dataentryLSB");

        setting.register("Pad[].switchType");
        setting.register("Pad[].switchWithToggle");
        setting.register("Pad[].switchInputType");

        setting.register("Pad[].switchOutPort");
        setting.register("Pad[].switchOutChannel");

        setting.register("Pad[].switchOutOnType");
        setting.register("Pad[].switchOutOnValue");
        setting.register("Pad[].switchOutOnValueFixed");
        setting.register("Pad[].switchOutOnText");
        setting.register("Pad[].switchOutOnTextGate");
        setting.register("Pad[].switchOutOff");
        setting.register("Pad[].switchOutOffValue");
        setting.register("Pad[].switchOutOffText");
        setting.register("Pad[].switchOutOffTextGate");
        setting.register("Pad[].switchHarmonyVelocityType");
        setting.register("Pad[].switchHarmonyVelocityFixed");
        setting.register("Pad[].switchHarmonyNotes");
        setting.register("Pad[].switchSequencerFile");
        setting.register("Pad[].switchSequencerSingltTrack");
        setting.register("Pad[].switchSequencerOneChannel");
        setting.register("Pad[].switchSequencerSeekStart");
        setting.register("Pad[].switchSequencerFilterNote");
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
        ArrayList<MXSettingNode> children;
        children = setting.findByPath("Circle[]");
        _patchToMixer = setting.getSettingAsInt("PatchToMixer", -1);
        int x = 0;
        for (MXSettingNode node : children) {
            ++ x;
            int type = node.getSettingAsInt("type", -1);
            int row = node.getSettingAsInt("row", -1);
            int column = node.getSettingAsInt("column", -1);
            if (type < 0 || row < 0 || column < 0) {
                break;
            }
            MGStatus status = new MGStatus(_port, type, row, column);
            try {
                status.setName(node.getSetting("name"));
                status.setMemo(node.getSetting("note"));
                String msgText = node.getSetting("message");
                int channel = node.getSettingAsInt("channel",0);
                RangedValue gate = RangedValue.new7bit(node.getSettingAsInt("gate", 0));
                RangedValue value = new RangedValue(node.getSettingAsInt("value", 0), 
                        node.getSettingAsInt("valuemin", 0),
                        node.getSettingAsInt("valuemax", 127));

                status.setUiValueInvert(node.getSettingAsBoolean("valueinvert", false));
                status.setupByDtext(msgText, channel, gate, value);


                status.setDataroomType(node.getSettingAsInt("dataentryType", MXVisitant.HAVE_VAL_NOT));
                status.setDataroomMSB(node.getSettingAsInt("dataentryMSB", 0));
                status.setDataroomLSB(node.getSettingAsInt("dataentryLSB", 0));
                
                status.setValuePairCC14(node.getSettingAsBoolean("isCCPair", false));

                _data.setCircleStatus(row, column, status);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        children = setting.findByPath("Slider[]");
        for (MXSettingNode node : children) {
            int type = node.getSettingAsInt("type", -1);
            int row = node.getSettingAsInt("row", -1);
            int column = node.getSettingAsInt("column", -1);
            if (type < 0 || row < 0 || column < 0) {
                break;
            }
            MGStatus status = new MGStatus(_port, type, row, column);
            try {
                status.setName(node.getSetting("name"));
                status.setMemo(node.getSetting("note"));
                String msgText = node.getSetting("message");

                int channel = node.getSettingAsInt("channel",0);
                RangedValue gate = RangedValue.new7bit(node.getSettingAsInt("gate", 0));
                RangedValue value = new RangedValue(node.getSettingAsInt("value", 0), 
                        node.getSettingAsInt("valuemin", 0),
                        node.getSettingAsInt("valuemax", 127));
                status.setupByDtext(msgText, channel, gate, value);

                status.setUiValueInvert(node.getSettingAsBoolean("valueinvert", false));

                status.setDataroomType(node.getSettingAsInt("dataentryType", MXVisitant.HAVE_VAL_NOT));
                status.setDataroomMSB(node.getSettingAsInt("dataentryMSB", 0));
                status.setDataroomLSB(node.getSettingAsInt("dataentryLSB", 0));

                status.setValuePairCC14(node.getSettingAsBoolean("isCCPair", false));

                _data.setSliderStatus(row, column, status);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        children = setting.findByPath("Pad[]");
        int count = 0;
        for (MXSettingNode node : children) {
            int type = node.getSettingAsInt("type", -1);
            int row = node.getSettingAsInt("row", -1);
            int column = node.getSettingAsInt("column", -1);
            String msgText;
            if (type < 0 || row < 0 || column < 0) {
                break;
            }
            if (row == 0 && column == 0) {
                count ++;
            }
            MGStatus status = new MGStatus(_port, type, row, column);

            try {
                status.setName(node.getSetting("name"));
                status.setMemo(node.getSetting("note"));
                msgText = node.getSetting("message");

                int channel = node.getSettingAsInt("channel",0);
                RangedValue gate = RangedValue.new7bit(node.getSettingAsInt("gate", 0));
                RangedValue value = new RangedValue(node.getSettingAsInt("value", 0), 
                        node.getSettingAsInt("valuemin", 0),
                        node.getSettingAsInt("valuemax", 127));
                status.setupByDtext(msgText, channel, gate, value);
                
                status.setUiValueInvert(node.getSettingAsBoolean("valueinvert", false));

                status.setDataroomType(node.getSettingAsInt("dataentryType", MXVisitant.HAVE_VAL_NOT));
                status.setDataroomMSB(node.getSettingAsInt("dataentryMSB", 0));
                status.setDataroomLSB(node.getSettingAsInt("dataentryLSB", 0));

                status.setValuePairCC14(node.getSettingAsBoolean("isCCPair", false));

                status.setSwitchType(node.getSettingAsInt("switchType", MGStatus.SWITCH_TYPE_ONOFF));
                status.setSwitchWithToggle(node.getSettingAsBoolean("switchWithToggle", false));
                status.setSwitchInputType(node.getSettingAsInt("switchInputType", MGStatus.SWITCH_ON_IF_PLUS1));
                status.setSwitchOutOnType(node.getSettingAsInt("switchOutOnType", MGStatus.SWITCH_OUT_ON_SAME_AS_INPUT));
                status.setSwitchOutOnTypeOfValue(node.getSettingAsInt("switchOutOnValue", MGStatus.SWITCH_OUT_ON_VALUE_AS_INPUT));
                status.setSwitchOutOnText(node.getSetting("switchOutOnText"));
                status.setSwitchOutOnTextGate(node.getSettingAsInt("switchOutOnTextGate", 127));
                status.setSwitchOutOnValueFixed(node.getSettingAsInt("switchOutOnValueFixed", 127));
                status.setSwitchOutOffType(node.getSettingAsInt("switchOutOff", MGStatus.SWITCH_OUT_OFF_SAME_AS_INPUT));
                status.setSwitchOutOffTypeOfValue(node.getSettingAsInt("switchOutOffValue", MGStatus.SWITCH_OUT_OFF_VALUE_0));
                status.setSwitchOutOffText(node.getSetting("switchOutOffText"));
                status.setSwitchOutOffTextGate(node.getSettingAsInt("switchOutOffTextGate", 0));
                status.setSwitchOutChannel(node.getSettingAsInt("switchOutChannel", 0));
                status.setSwitchHarmonyVelocityType(node.getSettingAsInt("switchHarmonyVelocityType", MGStatus.SWITCH_HARMONY_VELOCITY_FIXED));
                status.setSwitchHarmonyVelocityFixed(node.getSettingAsInt("switchHarmonyVelocityFixed", 100));
                status.setSwitchHarmonyNotes(node.getSetting("switchHarmonyNotes"));
                status.setSwitchSequencerFile(node.getSetting("switchSequencerFile"));
                status.setSwitchSequencerToSingltTrack(node.getSettingAsBoolean("switchSequencerSingltTrack", true));
                status.setSwitchSequenceSeekStart(node.getSettingAsBoolean("switchSequencerSeekStart", true));
                status.setSwitchSequencerFilterNote(node.getSettingAsBoolean("switchSequencerFilterNote", true));

                status.setSwitchOutPort(node.getSettingAsInt("switchOutPort", 0));
                status.setSwitchOutChannel(node.getSettingAsInt("switchOutChannel", 0));

                _data.setDrumPadStatus(row, column, status);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        int counter;
        counter = 1;
        setting.setSetting("PatchToMixer", _patchToMixer);
        for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++ column) {
            for (int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++ row) {
                String prefix = "Circle[" + counter + "].";
                MGStatus status = _data.getCircleStatus(row, column);
                MXMessage message = status.toMXMessage(null);
                setting.setSetting(prefix + "name", status.getName());
                setting.setSetting(prefix + "note", status.getMemo());
                setting.setSetting(prefix + "type", status.getUiType());
                setting.setSetting(prefix + "row", row);
                setting.setSetting(prefix + "column", column);
                setting.setSetting(prefix + "message", status.getTextCommand());
                setting.setSetting(prefix + "channel", message.getChannel());
                setting.setSetting(prefix + "gate", message.getGate()._var);
                setting.setSetting(prefix + "value", status.getValue()._var);
                setting.setSetting(prefix + "valuemin", status.getValue()._min);
                setting.setSetting(prefix + "valuemax", status.getValue()._max);
                setting.setSetting(prefix + "isCCPair", status.isValuePairCC14());
                setting.setSetting(prefix + "valueinvert", status.isUiValueInvert());
                setting.setSetting(prefix + "dataentryType", status.getDataroomType());
                setting.setSetting(prefix + "dataentryMSB", status.getDataeroomMSB());
                setting.setSetting(prefix + "dataentryLSB", status.getDataroomLSB());
                setting.setSetting(prefix + "isCCPair", status.isValuePairCC14());
                counter ++;
            }
        }
        counter = 1;
        for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++ column) {
            for (int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++ row) {
                String prefix = "Slider[" + counter + "].";
                MGStatus status = _data.getSliderStatus(row, column);
                MXMessage message = status.toMXMessage(null);
                setting.setSetting(prefix + "name", status.getName());
                setting.setSetting(prefix + "note", status.getMemo());
                setting.setSetting(prefix + "type", status.getUiType());
                setting.setSetting(prefix + "row", row);
                setting.setSetting(prefix + "column", column);
                setting.setSetting(prefix + "message", status.getTextCommand());
                setting.setSetting(prefix + "channel", message.getChannel());
                setting.setSetting(prefix + "gate", message.getGate()._var);
                setting.setSetting(prefix + "value", status.getValue()._var);
                setting.setSetting(prefix + "valuemin", status.getValue()._min);
                setting.setSetting(prefix + "valuemax", status.getValue()._max);
                setting.setSetting(prefix + "isCCPair", status.isValuePairCC14());
                setting.setSetting(prefix + "valueinvert", status.isUiValueInvert());
                setting.setSetting(prefix + "dataentryType", status.getDataroomType());
                setting.setSetting(prefix + "dataentryMSB", status.getDataeroomMSB());
                setting.setSetting(prefix + "dataentryLSB", status.getDataroomLSB());
                setting.setSetting(prefix + "isCCPair", status.isValuePairCC14());
                counter ++;
            }
        }
        counter = 1;
        for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++ column) {
            for (int row = 0; row < MXAppConfig.DRUM_ROW_COUNT; ++ row) {
                String prefix = "Pad[" + counter + "].";
                MGStatus status = _data.getDrumPadStatus(row, column);
                MXMessage message = status.toMXMessage(null);
                setting.setSetting(prefix + "name", status.getName());
                setting.setSetting(prefix + "note", status.getMemo());
                setting.setSetting(prefix + "type", status.getUiType());
                setting.setSetting(prefix + "row", row);
                setting.setSetting(prefix + "column", column);
                setting.setSetting(prefix + "message", status.getTextCommand());
                setting.setSetting(prefix + "channel", message.getChannel());
                setting.setSetting(prefix + "gate", message.getGate()._var);
                setting.setSetting(prefix + "value", status.getValue()._var);
                setting.setSetting(prefix + "valuemin", status.getValue()._min);
                setting.setSetting(prefix + "valuemax", status.getValue()._max);
                setting.setSetting(prefix + "isCCPair", status.isValuePairCC14());
                setting.setSetting(prefix + "valueinvert", status.isUiValueInvert());
                setting.setSetting(prefix + "dataentryType", status.getDataroomType());
                setting.setSetting(prefix + "dataentryMSB", status.getDataeroomMSB());
                setting.setSetting(prefix + "dataentryLSB", status.getDataroomLSB());
                setting.setSetting(prefix + "isCCPair", status.isValuePairCC14());

                setting.setSetting(prefix + "switchType", status.getSwitchType());
                setting.setSetting(prefix + "switchWithToggle", status.isSwitchWithToggle());
                setting.setSetting(prefix + "switchInputType", status.getSwitchInputType());
                setting.setSetting(prefix + "switchOutOnType", status.getSwitchOutOnType());
                setting.setSetting(prefix + "switchOutOnValue", status.getSwitchOutOnTypeOfValue());
                setting.setSetting(prefix + "switchOutOnValueFixed", status.getSwitchOutOnValueFixed());
                setting.setSetting(prefix + "switchOutOnText", status.getSwitchOutOnText());
                setting.setSetting(prefix + "switchOutOnTextGate", status.getSwitchOutOnTextGate());
                setting.setSetting(prefix + "switchOutOff", status.getSwitchOutOffType());
                setting.setSetting(prefix + "switchOutOffValue", status.getSwitchOutOffTypeOfValue());
                setting.setSetting(prefix + "switchOutOffText", status.getSwitchOutOffText());
                setting.setSetting(prefix + "switchOutOffTextGate", status.getSwitchOutOffTextGate());
                setting.setSetting(prefix + "switchOutPort", status.getSwitchOutPort());
                setting.setSetting(prefix + "switchOutChannel", status.getSwitchOutChannel());
                setting.setSetting(prefix + "switchHarmonyVelocityType", status.getSwitchHarmonyVelocityType());
                setting.setSetting(prefix + "switchHarmonyVelocityFixed", status.getSwitchHarmonyVelocityFixed());
                setting.setSetting(prefix + "switchHarmonyNotes", status.getSwitchHarmonyNotes());
                setting.setSetting(prefix + "switchSequencerFile", status.getSwitchSequencerFile());
                setting.setSetting(prefix + "switchSequencerSingltTrack", status.isSwitchSequencerToSingltTrack());
                setting.setSetting(prefix + "switchSequencerSeekStart", status.isSwitchSequenceSeekStart());
                setting.setSetting(prefix + "switchSequencerFilterNote", status.isSwitchSequencerFilterNote());

                counter ++;
            }
        }
    }
    
    public void catchedValue(MGStatus status, MXTiming timing, int newValue, UniqueChecker already) {
        synchronized(MXTiming.mutex) {
            if (_data.ready() == false) {
                //constructor
                return;
            }
            if (already == null) {
                already = new UniqueChecker(getNextReceiver());
            }
            if (already.checkAlready(status)) {
                return;
            }
            if (timing == null) {
                timing = new MXTiming();
            }

            int row = status.getRow(), column = status.getColumn();

            MGSlider slider = null; 
            MGCircle circle  = null;
            MGPad drumpad = null;

            if (status.getUiType() == MGStatus.TYPE_SLIDER) {
                slider = _data.getSlider(row, column);
                already.push(status);
                status.setValue(newValue);
                slider.updateUIOnly(timing, newValue);
            }
            if (status.getUiType() == MGStatus.TYPE_CIRCLE) {
                circle = _data.getCircle(row, column);
                already.push(status);
                status.setValue(newValue);
                circle.changeUIOnly(timing, newValue);
            }
            if (status.getUiType() == MGStatus.TYPE_DRUMPAD) {
                status.setValue(newValue);
                if (status.isDrumOn(newValue)) {
                    catchedValueDrum(status, timing, true, newValue, already);
                }else {
                    catchedValueDrum(status, timing, false, 0, already);
                }
                return;
            }

            if (_patchToMixer >= 0) {
                MX32MixerProcess nextMixer = _parent.getPage(_patchToMixer);

                MGStatus nextStatus = null;
                switch(status.getUiType()) {
                    case MGStatus.TYPE_SLIDER:
                        nextStatus  = nextMixer._data.getSliderStatus(row, column);
                        break;
                    case MGStatus.TYPE_CIRCLE:
                        nextStatus  = nextMixer._data.getCircleStatus(row, column);
                        break;
                    case MGStatus.TYPE_DRUMPAD:
                        nextStatus  = nextMixer._data.getDrumPadStatus(row, column);
                        break;
                }

                if (nextStatus.getValue()._max!= status.getValue()._max
                 || nextStatus.getValue()._min != status.getValue()._min) {
                    newValue = status.getValue().modifyRangeTo(nextStatus.getValue()._min, nextStatus.getValue()._max)._var;
                }

                nextMixer.catchedValue(nextStatus, timing, newValue, already);

                if (_patchTogether == false) {
                    return;
                }
            }

            MXMessage message = status.toMXMessage(timing);
            if (message.getValue()._var == 0) {
                //TODO whats this
                message.setValue(newValue);
            }
            if (message != null) {
                if (message.getValue()._var == 0 && message.isCommand(MXMidi.COMMAND_NOTEON)) {
                    message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_NOTEOFF + message.getChannel(), message.getData1(), 0);
                    message._timing = timing;
                }
            }
            //if (message.isDataentry() && message.getVisitant().getDataentryValue14() == 0 && message._trace == null) { message._trace = new Throwable(); }

            already.sendOnlyNeed(message);
            letsTryMessage(message, already);
        }
    }
    
    public void catchedValueDrum(MGStatus status, MXTiming timing, boolean newValue, int velocity, UniqueChecker already) {
        synchronized(MXTiming.mutex) {
            if (already == null) {
                already = new UniqueChecker(getNextReceiver());
            }
            if (already.checkAlready(status)) {
                return;
            }
            already.push(status);
            if (timing == null) {
                timing = new MXTiming();
            }

            int row = status.getRow(), column = status.getColumn();

            MGSlider slider = null; 
            MGCircle circle  = null;
            MGPad drumpad = null;

            if (status.getUiType() == MGStatus.TYPE_DRUMPAD) {
                drumpad = _data.getDrumPad(row, column);
            }else {
                throw new IllegalStateException();
            }

            if (_patchToMixer >= 0) {
                MX32MixerProcess nextMixer = _parent.getPage(_patchToMixer);

                MGStatus nextStatus = null;
                switch(status.getUiType()) {
                    case MGStatus.TYPE_SLIDER:
                        nextStatus  = nextMixer._data.getSliderStatus(row, column);
                        break;
                    case MGStatus.TYPE_CIRCLE:
                        nextStatus  = nextMixer._data.getCircleStatus(row, column);
                        break;
                    case MGStatus.TYPE_DRUMPAD:
                        nextStatus  = nextMixer._data.getDrumPadStatus(row, column);
                        break;
                }

                nextMixer.catchedValueDrum(nextStatus, timing, newValue, velocity, already);

                if (_patchTogether == false) {
                    return;
                }
            }

            boolean now = newValue;
            boolean prev = status.isValueLastDetect();

            int value = status.getValue()._var;
            if (status.toMXMessage(timing).isCommand(MXMidi.COMMAND_PROGRAMCHANGE)) {
                value = 0;
            }

            status.setSwitchLastDetected(velocity);

            boolean flag = (prev != now);

            if (!flag && status.toMXMessage(timing).hasValueLowField() == false) {
                flag = true;
            }

            if (!flag && status.getSwitchInputType() == MGStatus.SWITCH_ON_WHEN_ANY) {
                flag = true;
            }

            if (flag) { // ワンショットまたは、画面上の数値が切り替わった (nowへ)
                status.setValueLastDetect(now);
                if (now) { // オンにきりかわった
                    if (status.isSwitchWithToggle()) {
                        boolean lastSent = status.isValueLastSent();
                        now = !lastSent;
                    }
                }else { // オフにきりかわたｔ
                    if (status.isSwitchWithToggle()) {
                        //　トグルなら終了
                        return;    
                    }
                }
                MXMessage message = null;
                status.setValueLastSent(now);
                drumpad.updateUIOnly(now);
                if (now) {
                    if (status.getSwitchType() == MGStatus.SWITCH_TYPE_SEQUENCE) {
                        status.startSequence();
                        return;
                    }
                    if (status.getSwitchType() == MGStatus.SWITCH_TYPE_HARMONY) {
                        String notes = status.getSwitchHarmonyNotes();
                        int veltype = status.getSwitchHarmonyVelocityType();
                        if (veltype == MGStatus.SWITCH_HARMONY_VELOCITY_SAME_AS_INPUT) {
                            if (value != 0) {
                                velocity = value;
                            }else {
                                velocity = status.getSwitchHarmonyVelocityFixed();
                            }
                        }else if (veltype == MGStatus.SWITCH_HARMONY_VELOCITY_FIXED) {
                            velocity = status.getSwitchHarmonyVelocityFixed();
                        }else {
                            throw  new IllegalStateException("velocity unknown");
                        }
                        int[] noteList = MXMidi.textToNoteList(notes);
                        for (int note : noteList) {
                            message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_NOTEON + status.getSwitchOutChannel(), note, velocity);
                            message._timing = timing;
                            already.sendOnlyNeed(message);
                        }
                        return;
                    }
                    message = status.toMXMessageCaseDrumOn(timing);
                    if (message == null) {
                        return;
                    }
                }else {
                    if (status.getSwitchType() == MGStatus.SWITCH_TYPE_ON) {
                        return;
                    }
                    if (status.getSwitchType() == MGStatus.SWITCH_TYPE_SEQUENCE) {
                        status.stopSequence();
                        return;
                    }
                    if (status.getSwitchType() == MGStatus.SWITCH_TYPE_HARMONY) {
                        String notes = status.getSwitchHarmonyNotes();
                        int[] noteList = MXMidi.textToNoteList(notes);
                        for (int note : noteList) {
                            message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_NOTEOFF + status.getSwitchOutChannel(), note, 0);
                            message._timing = timing;
                            already.sendOnlyNeed(message);
                        }
                        return;
                    }
                    message = status.toMXMessageCaseDrumOff(timing);
                    if (message == null) {
                        return;
                    }
                }
                message = MXMessageFactory.fromClone(message);
                if (message.isCommand(MXMidi.COMMAND_NOTEON) && message.getValue()._var == 0) {
                    message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_NOTEOFF + message.getChannel(), message.getData1(), 0);
                }
                message._timing = timing;
                letsTryMessage(message, already);
            }
        }
    }
    

    public void makeCacheInternal1(MGStatus status) {
        if (status == null) {
            return;
        }
        
        MXMessage message = status.toMXMessage(null);
        if (message.isCommand(MXMidi.COMMAND_CONTROLCHANGE)) {
            int data1 = message.getData1();
            if (data1 == MXMidi.DATA1_CC_DATAENTRY || data1 == MXMidi.DATA1_CC_DATAINC || data1 == MXMidi.DATA1_CC_DATADEC) {
                _data._cachedDataentry.add(status);
                return;
            }
            if (_data._cachedControlChange[message.getChannel()][data1] == null) {
                _data._cachedControlChange[message.getChannel()][data1] = new ArrayList();
            }
            _data._cachedControlChange[message.getChannel()][data1].add(status);
            int data2 = -1;
            if (data1 >= 0 && data1 <= 31 && status.isValuePairCC14()) {
                data2 = data1 + 32;
                if (_data._cachedControlChange[message.getChannel()][data2] == null) {
                    _data._cachedControlChange[message.getChannel()][data2] = new ArrayList();
                }
                _data._cachedControlChange[message.getChannel()][data2].add(status);
            }
        }else if (message.isCommand(MXMidi.COMMAND_NOTEON) || message.isCommand(MXMidi.COMMAND_NOTEOFF)
                ||message.isCommand(MXMidi.COMMAND_POLYPRESSURE)) {
            int note = message.getData1();
            if (_data._cachedNoteMessage[message.getChannel()][note] == null) {
                _data._cachedNoteMessage[message.getChannel()][note] = new ArrayList();
            }
            _data._cachedNoteMessage[message.getChannel()][note].add(status);    
        }else if (message.isMessageTypeChannel()) {
            int command = message.getStatus() & 0xf0;
            if (_data._cachedChannelMessage[message.getChannel()][command] == null) {
                _data._cachedChannelMessage[message.getChannel()][command] = new ArrayList();
            }
            _data._cachedChannelMessage[message.getChannel()][command].add(status);
        }else {
            _data._cachedSystemMessage.add(status);
        }
    }

    public synchronized List<MGStatus> getCachedList(MXMessage request) {
        if (_data._cachedControlChange == null) {
            _data._cachedControlChange = new ArrayList[16][256];
            _data._cachedChannelMessage = new ArrayList[16][256];
            _data._cachedNoteMessage = new ArrayList[16][256];
            _data._cachedSystemMessage = new ArrayList();
            _data._cachedDataentry = new ArrayList();
            
            for (int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++ row) {
                for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
                    makeCacheInternal1(_data.getSliderStatus(row, column));
                }
            }

            for (int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++ row) {
                for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
                    makeCacheInternal1(_data.getCircleStatus(row, column));
                }
            }

            for (int row = 0; row < MXAppConfig.DRUM_ROW_COUNT; ++ row) {
                for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
                    MGStatus status = _data.getDrumPadStatus(row, column);
                    makeCacheInternal1(status);
                }
            }
        }
        
        if (request.isMessageTypeChannel()) {
            int command = request.getStatus() & 0xf0;
            if (command == MXMidi.COMMAND_CONTROLCHANGE) {
                int data1 = request.getData1();
                if (data1 == MXMidi.DATA1_CC_DATAENTRY || data1 == MXMidi.DATA1_CC_DATAINC || data1 == MXMidi.DATA1_CC_DATADEC) {
                    return _data._cachedDataentry;
                }
                return _data._cachedControlChange[request.getChannel()][request.getGate()._var];
            }else if (command == MXMidi.COMMAND_NOTEON || command == MXMidi.COMMAND_NOTEOFF
                    || command == MXMidi.COMMAND_POLYPRESSURE) {
                return _data._cachedNoteMessage[request.getChannel()][request.getGate()._var];
            }

            return _data._cachedChannelMessage[request.getChannel()][command];
        }else {
            return _data._cachedSystemMessage;
        }
    }
    
    MXMessage _poolFor14bit = null;
    MGStatus _poolFor14bitStatus = null;
    int _gotValue14bit = 0;
    
    public boolean isPairToPooled14bit(MXMessage message) {
        if (_poolFor14bit != null) {
            if (message.isCommand(MXMidi.COMMAND_CONTROLCHANGE)) {
                int gate = message.getGate()._var;
                if (gate >= 0 && gate < 32) {
                    if (gate +32 == _poolFor14bit.getGate()._var) {
                        return true;
                    }
                }else if (gate >= 32 && gate < 64) {
                    if (gate == _poolFor14bit.getGate()._var +32) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public int valueForPair(MXMessage message) {
        if (isPairToPooled14bit(message)) {
            if (message.isCommand(MXMidi.COMMAND_CONTROLCHANGE)) {
                int gate = message.getGate()._var;
                if (gate >= 0 && gate  < 32) {
                    if (gate +32 == _poolFor14bit.getGate()._var) {
                        return message.getValue()._var * 128 + _poolFor14bit.getValue()._var;
                    }
                }else if (gate >= 32 && message.getChannel() <= 64) {
                    if (gate == _poolFor14bit.getGate()._var +32) {
                        return message.getValue()._var + _poolFor14bit.getValue()._var  * 128;
                    }
                }
            }
        }
        throw new IllegalStateException("valueForPair not work at the moment");
    }

    public boolean isSameToPooled14bit(MXMessage message) {
        if (_poolFor14bit != null) {
            if (message.isCommand(MXMidi.COMMAND_CONTROLCHANGE)) {
                if (message.getGate() == _poolFor14bit.getGate()) {
                    return true;
                }
            }
        }
        return false;
    }

    long lastTick = 0;
    
    public synchronized  void clearPoolImpl() {
        if (lastTick + 400 < System.currentTimeMillis()) {
            if (_poolFor14bit != null) {
                MXMessage prev = _poolFor14bit;
                _poolFor14bit = null;
                prev.setValuePairCC14(true);
                sendToNext(prev);
                return;
            }
        }
    }

    public synchronized  void letsTryMessage(MXMessage message, UniqueChecker already) {
        if (already == null) {
            already = new UniqueChecker(getNextReceiver());
        }
        
        int fail = 0;
        int hit = 0;
        if (_poolFor14bit != null) {
            if (isPairToPooled14bit(message)) {
                int value = valueForPair(message);
                _poolFor14bit = null;
                message = MXMessageFactory.fromClone(message);
                message.setValuePairCC14(true);
                message.setValue(value);
                already.sendOnlyNeed(message);
                catchedValue(_poolFor14bitStatus, message._timing, value, already);
                return;
            }else {
                clearPoolImpl();
            }
        }
        int port = message.getPort();
        List<MGStatus> list = getCachedList(message);
        if (list != null) {
            boolean foundSome = false;
            for (int i = 0; i < list.size(); ++ i) {
                MGStatus status = list.get(i);
                if (status.statusTryCatch(message)) {
                    int gate = message.getGate()._var;
                    if (message.isCommand(MXMidi.COMMAND_CONTROLCHANGE) && gate >= 0 && gate < 32 && status.isValuePairCC14()) {
                        //TODO more logical
                        if (gate == message.getGate()._var) {
                            _poolFor14bit = message;
                            _poolFor14bitStatus = status;
                        }else if (gate + 32 == message.getGate()._var) {
                            _poolFor14bit = message;
                            _poolFor14bitStatus = status;
                        }else {
                            continue;
                        }
                        lastTick = System.currentTimeMillis();
                        foundSome = true;
                        hit ++;
                        MXGlobalTimer.letsCountdown(500, new Runnable() {
                            @Override
                            public void run() {
                                clearPoolImpl();
                            }
                        });
                    }
                    catchedValue(status, message._timing, status.getValue()._var, already);
                    foundSome = true;
                    hit ++;
                }else {
                    fail ++;
                }
            }
            if (foundSome) {
                return;
            }
        }
        if (MXVisitant.isMesssageHaveVisitant(message)) {
            _visitant16.get(message.getChannel()).updateVisitantChannel(message);
        }
        if (message.isMessageTypeChannel()) {
            _visitant16.get(message.getChannel()).attachChannelVisitantToMessage(message);
        }
        already.sendOnlyNeed(message);
    }

    public void notifyCacheBroken() {
        _data._cachedControlChange = null;
    }
}
