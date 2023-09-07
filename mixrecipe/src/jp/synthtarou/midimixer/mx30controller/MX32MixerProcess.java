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

import jp.synthtarou.midimixer.libs.UniqueChecker;
import java.util.ArrayList;
import java.util.TreeSet;
import javax.swing.JPanel;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
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
    boolean _patchTogether = false;

    public MX32MixerProcess(MX30Process parent, int port) {
        _parent = parent;
        _port = port;
        _data = new MX32MixerData(this);
        _view = new MX32MixerView(this);
        _setting = new MXSetting("Mixing" + (port + 1));
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
            sendToNext(message);
            return;
        }
        controlProcessByMessage(message);
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
            ++x;
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
                int channel = node.getSettingAsInt("channel", 0);
                RangedValue gate = RangedValue.new7bit(node.getSettingAsInt("gate", 0));
                RangedValue value = new RangedValue(node.getSettingAsInt("value", 0),
                        node.getSettingAsInt("valuemin", 0),
                        node.getSettingAsInt("valuemax", 127));

                status.setTemplateAsText(msgText, channel);
                status.setChannel(channel);
                status.setGate(gate);
                status.setValue(value);

                status.setDataroomType(node.getSettingAsInt("dataentryType", MXVisitant.HAVE_VAL_NOT));
                status.setDataroomMSB(node.getSettingAsInt("dataentryMSB", 0));
                status.setDataroomLSB(node.getSettingAsInt("dataentryLSB", 0));

                status.setValuePairCC14(node.getSettingAsBoolean("isCCPair", false));

                _data.setCircleStatus(row, column, status);
            } catch (Exception e) {
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

                int channel = node.getSettingAsInt("channel", 0);
                RangedValue gate = RangedValue.new7bit(node.getSettingAsInt("gate", 0));
                RangedValue value = new RangedValue(node.getSettingAsInt("value", 0),
                        node.getSettingAsInt("valuemin", 0),
                        node.getSettingAsInt("valuemax", 127));

                status.setTemplateAsText(msgText, channel);
                status.setChannel(channel);
                status.setGate(gate);
                status.setValue(value);

                status.setDataroomType(node.getSettingAsInt("dataentryType", MXVisitant.HAVE_VAL_NOT));
                status.setDataroomMSB(node.getSettingAsInt("dataentryMSB", 0));
                status.setDataroomLSB(node.getSettingAsInt("dataentryLSB", 0));

                status.setValuePairCC14(node.getSettingAsBoolean("isCCPair", false));

                _data.setSliderStatus(row, column, status);
            } catch (Exception e) {
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
                count++;
            }
            MGStatus status = new MGStatus(_port, type, row, column);

            try {
                status.setName(node.getSetting("name"));
                status.setMemo(node.getSetting("note"));
                msgText = node.getSetting("message");

                int channel = node.getSettingAsInt("channel", 0);
                RangedValue gate = RangedValue.new7bit(node.getSettingAsInt("gate", 0));
                RangedValue value = new RangedValue(node.getSettingAsInt("value", 0),
                        node.getSettingAsInt("valuemin", 0),
                        node.getSettingAsInt("valuemax", 127));

                status.setTemplateAsText(msgText, channel);
                status.setChannel(channel);
                status.setGate(gate);
                status.setValue(value);

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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        int counter;
        counter = 1;
        setting.setSetting("PatchToMixer", _patchToMixer);
        for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
            for (int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++row) {
                String prefix = "Circle[" + counter + "].";
                MGStatus status = _data.getCircleStatus(row, column);
                MXMessage message = status.toMXMessage(null);
                setting.setSetting(prefix + "name", status.getName());
                setting.setSetting(prefix + "note", status.getMemo());
                setting.setSetting(prefix + "type", status.getUiType());
                setting.setSetting(prefix + "row", row);
                setting.setSetting(prefix + "column", column);
                setting.setSetting(prefix + "message", status.toTemplateText());
                setting.setSetting(prefix + "channel", message.getChannel());
                setting.setSetting(prefix + "gate", message.getGate()._var);
                setting.setSetting(prefix + "value", status.getValue()._var);
                setting.setSetting(prefix + "valuemin", status.getValue()._min);
                setting.setSetting(prefix + "valuemax", status.getValue()._max);
                setting.setSetting(prefix + "isCCPair", status.isValuePairCC14());
                setting.setSetting(prefix + "dataentryType", status.getDataroomType());
                setting.setSetting(prefix + "dataentryMSB", status.getDataeroomMSB());
                setting.setSetting(prefix + "dataentryLSB", status.getDataroomLSB());
                setting.setSetting(prefix + "isCCPair", status.isValuePairCC14());
                counter++;
            }
        }
        counter = 1;
        for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
            for (int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++row) {
                String prefix = "Slider[" + counter + "].";
                MGStatus status = _data.getSliderStatus(row, column);
                MXMessage message = status.toMXMessage(null);
                setting.setSetting(prefix + "name", status.getName());
                setting.setSetting(prefix + "note", status.getMemo());
                setting.setSetting(prefix + "type", status.getUiType());
                setting.setSetting(prefix + "row", row);
                setting.setSetting(prefix + "column", column);
                setting.setSetting(prefix + "message", status.toTemplateText());
                setting.setSetting(prefix + "channel", message.getChannel());
                setting.setSetting(prefix + "gate", message.getGate()._var);
                setting.setSetting(prefix + "value", status.getValue()._var);
                setting.setSetting(prefix + "valuemin", status.getValue()._min);
                setting.setSetting(prefix + "valuemax", status.getValue()._max);
                setting.setSetting(prefix + "isCCPair", status.isValuePairCC14());
                setting.setSetting(prefix + "dataentryType", status.getDataroomType());
                setting.setSetting(prefix + "dataentryMSB", status.getDataeroomMSB());
                setting.setSetting(prefix + "dataentryLSB", status.getDataroomLSB());
                setting.setSetting(prefix + "isCCPair", status.isValuePairCC14());
                counter++;
            }
        }
        counter = 1;
        for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
            for (int row = 0; row < MXAppConfig.DRUM_ROW_COUNT; ++row) {
                String prefix = "Pad[" + counter + "].";
                MGStatus status = _data.getDrumPadStatus(row, column);
                MXMessage message = status.toMXMessage(null);
                setting.setSetting(prefix + "name", status.getName());
                setting.setSetting(prefix + "note", status.getMemo());
                setting.setSetting(prefix + "type", status.getUiType());
                setting.setSetting(prefix + "row", row);
                setting.setSetting(prefix + "column", column);
                setting.setSetting(prefix + "message", status.toTemplateText());
                setting.setSetting(prefix + "channel", message.getChannel());
                setting.setSetting(prefix + "gate", message.getGate()._var);
                setting.setSetting(prefix + "value", status.getValue()._var);
                setting.setSetting(prefix + "valuemin", status.getValue()._min);
                setting.setSetting(prefix + "valuemax", status.getValue()._max);
                setting.setSetting(prefix + "isCCPair", status.isValuePairCC14());
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

                counter++;
            }
        }
    }

    public void updateUIByStatus(MGStatus status) {
        if (status.getUiType() == MGStatus.TYPE_SLIDER) {
            MGSlider slider = _data.getSlider(status._row, status._column);
            slider.updateUIByStatus();
        }
        if (status.getUiType() == MGStatus.TYPE_CIRCLE) {
            MGCircle circle = _data.getCircle(status._row, status._column);
            circle.updateUIByStatus();
        }
        if (status.getUiType() == MGStatus.TYPE_DRUMPAD) {
            MGDrumPad pad = _data.getDrumPad(status._row, status._column);
            pad.pickupTriggerStatus();
        }
    }

    public void controlByUI(MGStatus status, int newValue) {
        controlByUI(status, newValue, null, null);
    }

    public void controlByUI(MGStatus status, int newValue, MXTiming timing, UniqueChecker uniqueChecker) {
        synchronized (MXTiming.mutex) {
            if (_data.ready() == false) {
                //constructor
                return;
            }
            if (timing == null) {
                timing = new MXTiming();
            }
            if (uniqueChecker == null) {            
                uniqueChecker = new UniqueChecker(getNextReceiver());
            }
            int row = status.getRow(), column = status.getColumn();
            int uiType = status.getUiType();
            if (uiType == MGStatus.TYPE_SLIDER) {
                MGSlider slider = _data.getSlider(row, column);
                status.updateValue(newValue);
                //slider.updateUIByStatus();
            } else if (uiType == MGStatus.TYPE_CIRCLE) {
                MGCircle circle = _data.getCircle(row, column);
                status.updateValue(newValue);
                //circle.updateUIByStatus();
            } else if (uiType == MGStatus.TYPE_DRUMPAD) {
                // check have collect _drumSwitch value
                MGDrumPad drumPad = _data.getDrumPad(row, column);
                status.updateValue(newValue);
                drumPad.pickupTriggerStatus();
            } else {
                new Throwable("Illegal State uiType unknown");
                return;
            }
            if (_patchToMixer >= 0) {
                MX32MixerProcess nextMixer = _parent.getPage(_patchToMixer);
                MGStatus nextStatus = null;
                switch (status.getUiType()) {
                    case MGStatus.TYPE_SLIDER:
                        nextStatus = nextMixer._data.getSliderStatus(row, column);
                        break;
                    case MGStatus.TYPE_CIRCLE:
                        nextStatus = nextMixer._data.getCircleStatus(row, column);
                        break;
                    case MGStatus.TYPE_DRUMPAD:
                        nextStatus = nextMixer._data.getDrumPadStatus(row, column);
                        break;
                }

                int nextMin = nextStatus.getValue()._min;
                int nextMax = nextStatus.getValue()._min;
                newValue = status.getValue().modifyRangeTo(nextMin, nextMax)._var;
                nextMixer.controlByUI(nextStatus, newValue, timing, uniqueChecker);

                if (_patchTogether == false) {
                    return;
                }
            }

            if (uiType == MGStatus.TYPE_DRUMPAD) {
                MGDrumPad drumPad = _data.getDrumPad(row, column);
                drumPad.invokeDrumAction();
            } else {
                MXMessage message = status.toMXMessage(timing);
                if (message != null) {
                    if (message.getValue()._var == 0 && message.isCommand(MXMidi.COMMAND_NOTEON)) {
                        message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_NOTEOFF + message.getChannel(), message.getData1(), 0);
                        message._timing = timing;
                    }
                }
                //if (message.isDataentry() && message.getVisitant().getDataentryValue14() == 0 && message._trace == null) { message._trace = new Throwable(); }

                reenterMXMessageByUI(message);
            }
        }
    }

    /*
    MXMessage _poolFor14bit = null;
    MGStatus _poolFor14bitStatus = null;
    int _gotValue14bit = 0;

    public boolean isPairToPooled14bit(MXMessage message) {
        if (_poolFor14bit != null) {
            if (message.isCommand(MXMidi.COMMAND_CONTROLCHANGE)) {
                int gate = message.getGate()._var;
                if (gate >= 0 && gate < 32) {
                    if (gate + 32 == _poolFor14bit.getGate()._var) {
                        return true;
                    }
                } else if (gate >= 32 && gate < 64) {
                    if (gate == _poolFor14bit.getGate()._var + 32) {
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
                if (gate >= 0 && gate < 32) {
                    if (gate + 32 == _poolFor14bit.getGate()._var) {
                        return message.getValue()._var * 128 + _poolFor14bit.getValue()._var;
                    }
                } else if (gate >= 32 && message.getChannel() <= 64) {
                    if (gate == _poolFor14bit.getGate()._var + 32) {
                        return message.getValue()._var + _poolFor14bit.getValue()._var * 128;
                    }
                }
            }
        }
        throw new IllegalStateException("valueForPair not work at the moment");
    }*/
 /*
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

    public synchronized void clearPoolImpl() {
        if (lastTick + 400 < System.currentTimeMillis()) {
            if (_poolFor14bit != null) {
                MXMessage prev = _poolFor14bit;
                _poolFor14bit = null;
                prev.setValuePairCC14(true);
                sendToNext(prev);
                return;
            }
        }
    }*/

    public synchronized void reenterMXMessageByUI(MXMessage message) {
        if (MXVisitant.isMesssageHaveVisitant(message)) {
            _visitant16.get(message.getChannel()).updateVisitantChannel(message);
        }
        if (message.isMessageTypeChannel()) {
            _visitant16.get(message.getChannel()).attachChannelVisitantToMessage(message);
        }
        //画面のコントロールをすべて更新する
        TreeSet<MGStatus> listStatus = _data.controlStatusByMessage(message);
        if (listStatus != null && listStatus.isEmpty() == false) {
            for (MGStatus status : listStatus) {
                updateUIByStatus(status);
            }
        }
        sendToNext(message);
    }

    public synchronized void controlProcessByMessage(MXMessage message) {
        if (MXVisitant.isMesssageHaveVisitant(message)) {
            _visitant16.get(message.getChannel()).updateVisitantChannel(message);
        }
        if (message.isMessageTypeChannel()) {
            _visitant16.get(message.getChannel()).attachChannelVisitantToMessage(message);
        }
        //画面のコントロールをすべて更新する
        TreeSet<MGStatus> listStatus = _data.controlStatusByMessage(message);
        if (listStatus != null && listStatus.isEmpty() == false) {
            for (MGStatus status : listStatus) {
                updateUIByStatus(status);
            }
        }
        sendToNext(message);
    }

    public void notifyCacheBroken() {
        _data._finder = null;
    }
}
