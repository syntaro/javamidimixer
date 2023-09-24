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
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
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
        
        /* general for circle */
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

        /* general for slider */
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

        /* general for pad */
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

        /* drum */
        setting.register("Pad[].switchInputOnMin");
        setting.register("Pad[].switchInputOnMax");
        setting.register("Pad[].switchMouseOnValue");
        setting.register("Pad[].switchMouseOffValue");
        setting.register("Pad[].switchWithToggle");
        setting.register("Pad[].switchOnlySwitched");

        /* drum out */
        setting.register("Pad[].switchOutPort");
        setting.register("Pad[].switchOutChannel");
        setting.register("Pad[].switchOutStyle"); 
        setting.register("Pad[].switchOutValueTypeOn"); 
        setting.register("Pad[].switchOutValueTypeOff");

        /* template */
        setting.register("Pad[].switchTemplateText");
        setting.register("Pad[].switchTemplateTextGate");

        /* program TODO */
        setting.register("Pad[].switchProgramType");
        setting.register("Pad[].switchProgramNumber");
        setting.register("Pad[].switchProgramMSB");
        setting.register("Pad[].switchProgramLSB");

        /* note */
        setting.register("Pad[].switchHarmonyNotes");
        
        /* sequencer */
        setting.register("Pad[].switchSequencerFile");
        setting.register("Pad[].switchSequencerSingleTrack");
        setting.register("Pad[].switchSequencerSeekStart");
        setting.register("Pad[].switchSequencerFilterNote");
        
        /* linkslider TOOD */
        setting.register("Pad[].switchLinkRow");
        setting.register("Pad[].switchLinkColumn");
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
            status._name = node.getSetting("name");
            status._memo = node.getSetting("note");
            status._ccPair14 = node.getSettingAsBoolean("isCCPair", false);

            try {
                String msgText = node.getSetting("message");
                MXTemplate template = new MXTemplate(msgText);

                int channel = node.getSettingAsInt("channel", 0);

                int gateN = node.getSettingAsInt("gate", 0);
                int valueN = node.getSettingAsInt("value", 0);
                int valueMin = node.getSettingAsInt("valuemin", 0);
                int valueMax = node.getSettingAsInt("valuemax", 127);
                RangedValue gate = template.getBytePosHiGate()>= 0 ? RangedValue.new14bit(gateN) : RangedValue.new7bit(gateN);
                RangedValue value = new RangedValue(valueN, valueMin, valueMax);
                status.setBaseMessage(MXMessageFactory.fromTemplate(_port, template, channel, gate, value));

            } catch (Exception e) {
                e.printStackTrace();
            }
            _data.setCircleStatus(row, column, status);
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
            status._name = node.getSetting("name");
            status._memo = node.getSetting("note");
            status._ccPair14 = node.getSettingAsBoolean("isCCPair", false);

            try {
                String msgText = node.getSetting("message");
                MXTemplate template = new MXTemplate(msgText);

                int channel = node.getSettingAsInt("channel", 0);

                int gateN = node.getSettingAsInt("gate", 0);
                int valueN = node.getSettingAsInt("value", 0);
                int valueMin = node.getSettingAsInt("valuemin", 0);
                int valueMax = node.getSettingAsInt("valuemax", 127);
                RangedValue gate = template.getBytePosHiGate()>= 0 ? RangedValue.new14bit(gateN) : RangedValue.new7bit(gateN);
                RangedValue value = new RangedValue(valueN, valueMin, valueMax);
                status.setBaseMessage(MXMessageFactory.fromTemplate(_port, template, channel, gate, value));

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
            if (type < 0 || row < 0 || column < 0) {
                break;
            }
            if (row == 0 && column == 0) {
                count++;
            }
            MGStatus status = new MGStatus(_port, type, row, column);
            status._name = node.getSetting("name");
            status._memo = node.getSetting("note");
            status._ccPair14 = node.getSettingAsBoolean("isCCPair", false);

            try {
                String msgText = node.getSetting("message");
                MXTemplate template = new MXTemplate(msgText);

                int channel = node.getSettingAsInt("channel", 0);

                int gateN = node.getSettingAsInt("gate", 0);
                int valueN = node.getSettingAsInt("value", 0);
                int valueMin = node.getSettingAsInt("valuemin", 0);
                int valueMax = node.getSettingAsInt("valuemax", 127);
                RangedValue gate = template.getBytePosHiGate()>= 0 ? RangedValue.new14bit(gateN) : RangedValue.new7bit(gateN);
                RangedValue value = new RangedValue(valueN, valueMin, valueMax);
                status.setBaseMessage(MXMessageFactory.fromTemplate(_port, template, channel, gate, value));

                /* Drum */
                MGStatusForDrum drum = status._drum;

                int switchInputOnMin = node.getSettingAsInt("switchInputOnMin", 1);
                int switchInputOnMax = node.getSettingAsInt("switchInputOnMax", 127);
                drum._strikeZone = new RangedValue(0, switchInputOnMin, switchInputOnMax);

                int switchMouseOnValue =  node.getSettingAsInt("switchMouseOnValue", 100);
                int switchMouseOffValue =  node.getSettingAsInt("switchMouseOffValue", 0);
                drum._mouseOnValue = switchMouseOnValue;
                drum._mouseOffValue = switchMouseOffValue;
                
                boolean switchModeToggle =  node.getSettingAsBoolean("switchWithToggle", false);
                drum._modeToggle = switchModeToggle;

                boolean switchOnlySwitched = node.getSettingAsBoolean("switchOnlySwitched", true);
                drum._onlySwitched = switchOnlySwitched;

                /* drum out */
                int switchOutPort = node.getSettingAsInt("switchOutPort", -1); //-1 = same as input
                drum._outPort = switchOutPort;
                int switchOutChannel = node.getSettingAsInt("switchOutChannel", -1); //-1 = same as input
                drum._outChannel = switchOutChannel;

                int switchOutStyle = node.getSettingAsInt("switchOutStyle", MGStatusForDrum.STYLE_SAME_CC);
                drum._outStyle = switchOutStyle;
                int switchOutValueTypeOn = node.getSettingAsInt("switchOutValueTypeOn", MGStatusForDrum.VALUETYPE_AS_INPUT);
                drum._outValueTypeOn = switchOutValueTypeOn;
                int switchOutValueTypeOff = node.getSettingAsInt("switchOutValueTypeOff", MGStatusForDrum.VALUETYPE_AS_INPUT);
                drum._outValueTypeOff = switchOutValueTypeOff;

                /* template */
                String switchTemplateText = node.getSetting("switchTemplateText");
                drum._templateText = switchTemplateText;
                int switchTemplateTextGate = node.getSettingAsInt("switchTemplateTextGate", 0);
                drum._teplateTextGate = switchTemplateTextGate;

                /* program TODO */
                int switchProgramType = node.getSettingAsInt("switchProgramType", MGStatusForDrum.STYLE_PROGRAM_CHANGE);
                int switchProgramNumber  = node.getSettingAsInt("switchProgramNumber", 0);
                int switchProgramMSB = node.getSettingAsInt("switchProgramMSB", 0);
                int switchProgramLSB = node.getSettingAsInt("switchProgramLSB", 0);
                drum._programType = switchProgramType;
                drum._programNumber = switchProgramNumber;
                drum._programMSB = switchProgramMSB;
                drum._programLSB = switchProgramLSB;

                /* note */
                String switchHarmonyNotes = node.getSetting("switchHarmonyNotes");
                drum._harmonyNotes = switchHarmonyNotes;

                /* sequencer */
                String switchSequencerFile = node.getSetting("switchSequencerFile");
                boolean switchSequencerSingleTrack = setting.getSettingAsBoolean("switchSequencerSingleTrack", false);
                boolean switchSequencerSeekStart = setting.getSettingAsBoolean("switchSequencerSeekStart", false);
                boolean switchSequencerFilterNote = setting.getSettingAsBoolean("switchSequencerFilterNote", false);

                drum._sequencerFile = switchSequencerFile;
                drum._sequencerSeekStart = switchSequencerSeekStart;
                drum._sequencerFilterNote = switchSequencerFilterNote;
                drum._sequencerSingleTrack = switchSequencerSingleTrack;

                /* linkslider TOOD */
                int switchLinkRow = node.getSettingAsInt("switchLinkRow", 0);
                int switchLinkColumn = node.getSettingAsInt("switchLinkColumn", -1);
                
                drum._LinkRow = switchLinkRow;
                drum._LinkColumn = switchLinkColumn;

                _data.setDrumPadStatus(row, column, status);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beforeWriteSettingFile(MXSetting node) {
        int counter;
        counter = 1;
        node.setSetting("PatchToMixer", _patchToMixer);
        for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
            for (int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++row) {
                String prefix = "Circle[" + counter + "].";
                MGStatus status = _data.getCircleStatus(row, column);
                MXMessage base = status._base;
                node.setSetting(prefix + "name", status._name);
                node.setSetting(prefix + "note", status._memo);
                node.setSetting(prefix + "type", status._uiType);
                node.setSetting(prefix + "row", row);
                node.setSetting(prefix + "column", column);
                node.setSetting(prefix + "message", base.getTemplateAsText());
                node.setSetting(prefix + "channel", base.getChannel());
                node.setSetting(prefix + "gate", base.getGate()._var);
                node.setSetting(prefix + "value", base.getValue()._var);
                node.setSetting(prefix + "valuemin", base.getValue()._min);
                node.setSetting(prefix + "valuemax", base.getValue()._max);
                node.setSetting(prefix + "isCCPair", status._ccPair14);
                counter++;
            }
        }
        counter = 1;
        for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
            for (int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++row) {
                String prefix = "Slider[" + counter + "].";
                MGStatus status = _data.getSliderStatus(row, column);
                MXMessage base = status._base;
                node.setSetting(prefix + "name", status._name);
                node.setSetting(prefix + "note", status._memo);
                node.setSetting(prefix + "type", status._uiType);
                node.setSetting(prefix + "row", row);
                node.setSetting(prefix + "column", column);
                node.setSetting(prefix + "message", base.getTemplateAsText());
                node.setSetting(prefix + "channel", base.getChannel());
                node.setSetting(prefix + "gate", base.getGate()._var);
                node.setSetting(prefix + "value", base.getValue()._var);
                node.setSetting(prefix + "valuemin", base.getValue()._min);
                node.setSetting(prefix + "valuemax", base.getValue()._max);
                node.setSetting(prefix + "isCCPair", status._ccPair14);

                counter++;
            }
        }
        counter = 1;
        for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
            for (int row = 0; row < MXAppConfig.DRUM_ROW_COUNT; ++row) {
                String prefix = "Pad[" + counter + "].";
                MGStatus status = _data.getDrumPadStatus(row, column);
                MXMessage base = status._base;
                node.setSetting(prefix + "name", status._name);
                node.setSetting(prefix + "note", status._memo);
                node.setSetting(prefix + "type", status._uiType);
                node.setSetting(prefix + "row", row);
                node.setSetting(prefix + "column", column);
                node.setSetting(prefix + "message", base.getTemplateAsText());
                node.setSetting(prefix + "channel", base.getChannel());
                node.setSetting(prefix + "gate", base.getGate()._var);
                node.setSetting(prefix + "value", base.getValue()._var);
                node.setSetting(prefix + "valuemin", base.getValue()._min);
                node.setSetting(prefix + "valuemax", base.getValue()._max);
                node.setSetting(prefix + "isCCPair", status._ccPair14);
                
                /* Drum */
                MGStatusForDrum drum = status._drum;
                node.setSetting(prefix + "switchInputOnMin", drum._strikeZone._min);
                node.setSetting(prefix + "switchInputOnMax", drum._strikeZone._max);

                node.setSetting(prefix + "switchMouseOnValue", drum._mouseOnValue);
                node.setSetting(prefix + "switchMouseOffValue", drum._mouseOffValue);
                
                node.setSetting(prefix + "switchWithToggle", drum._modeToggle);

                node.setSetting(prefix + "switchOnlySwitched", drum._onlySwitched);

                /* drum out */
                node.setSetting(prefix + "switchOutPort", drum._outPort);
                node.setSetting(prefix + "switchOutChannel",  drum._outChannel);

                node.setSetting(prefix + "switchOutStyle", drum._outStyle);
                node.setSetting(prefix + "switchOutValueTypeOn", drum._outValueTypeOn);
                node.setSetting(prefix + "switchOutValueTypeOff", drum._outValueTypeOff);

                /* template */
                node.setSetting(prefix + "switchTemplateText", drum._templateText);
                node.setSetting(prefix + "switchTemplateTextGate", drum._teplateTextGate);

                /* program TODO */
                node.setSetting(prefix + "switchProgramType", drum._programType);
                node.setSetting(prefix + "switchProgramNumber", drum._programNumber);
                node.getSettingAsInt(prefix + "switchProgramMSB",  drum._programMSB);
                node.getSettingAsInt(prefix + "switchProgramLSB", drum._programLSB);

                /* note */
                node.setSetting(prefix + "switchHarmonyNotes", drum._harmonyNotes);

                /* sequencer */
                node.setSetting(prefix + "switchSequencerFile", drum._sequencerFile);
                node.setSetting(prefix + "switchSequencerSingleTrack", drum._sequencerSeekStart);
                node.setSetting(prefix + "switchSequencerSeekStart", drum._sequencerFilterNote);
                node.setSetting(prefix + "switchSequencerFilterNote", drum._sequencerSingleTrack);

                /* linkslider TOOD */
                node.setSetting(prefix + "switchLinkRow", drum._LinkRow);
                node.setSetting(prefix + "switchLinkColumn", drum._LinkColumn);

                _data.setDrumPadStatus(row, column, status);

                counter++;
            }
        }
    }

    public void updateUIByStatus(MXMessage message, MGStatus status) {
        if (status._uiType == MGStatus.TYPE_SLIDER) {
            MGSlider slider = _data.getSlider(status._row, status._column);
            slider.updateUIByStatus();
        }
        if (status._uiType == MGStatus.TYPE_CIRCLE) {
            MGCircle circle = _data.getCircle(status._row, status._column);
            circle.updateUIByStatus();
        }
        if (status._uiType == MGStatus.TYPE_DRUMPAD) {
            MGDrumPad pad = _data.getDrumPad(status._row, status._column);
            status._drum.messageDetected(message);
        }
    }

    public void controlByUI(MGStatus status, int newValue) {
        controlByUI(status, newValue, null);
    }

    public void controlByUI(MGStatus status, int newValue, MXTiming timing) {
        synchronized (MXTiming.mutex) {
            if (_data.ready() == false) {
                //constructor
                return;
            }
            if (timing == null) {
                timing = new MXTiming();
            }
            int row = status._row, column = status._column;
            int uiType = status._uiType;
            if (uiType == MGStatus.TYPE_SLIDER) {
                MGSlider slider = _data.getSlider(row, column);
                status.updateValue(newValue);
            } else if (uiType == MGStatus.TYPE_CIRCLE) {
                MGCircle circle = _data.getCircle(row, column);
                status.updateValue(newValue);
            } else if (uiType == MGStatus.TYPE_DRUMPAD) {
                new IllegalStateException("Dont use this. See MGStatusForDrum.messageDetected");
            } else {
                new Throwable("Illegal State uiType unknown");
                return;
            }
            if (_patchToMixer >= 0) {
                MX32MixerProcess nextMixer = _parent.getPage(_patchToMixer);
                MGStatus nextStatus = null;
                switch (status._uiType) {
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

                int nextMin = nextStatus.getBaseMessage().getValue()._min;
                int nextMax = nextStatus.getBaseMessage().getValue()._max;
                newValue = status.getBaseMessage().getValue().modifyRangeTo(nextMin, nextMax)._var;
                nextMixer.controlByUI(nextStatus, newValue, timing);

                if (_patchTogether == false) {
                    return;
                }
            }

            if (uiType == MGStatus.TYPE_DRUMPAD) {
                MGStatus drumStatus = _data.getDrumPadStatus(row, column);
                //tODO
                //drumStatus.invokeDrumAction();
            } else {
                MXMessage base = status._base;
                if (base != null) {
                    if (base.getValue()._var == 0 && base.isCommand(MXMidi.COMMAND_CH_NOTEON)) {
                        base = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_NOTEOFF + base.getChannel(), base.getData1(), 0);
                        base._timing = timing;
                    }
                }

                reenterMXMessageByUI(base);
            }
        }
    }

    MXMessage _poolFor14bit = null;
    int _gotValue14bit = 0;

    public boolean isPairToPooled14bit(MXMessage message) {
        if (_poolFor14bit != null) {
            if (message.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
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
            if (message.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
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
    }

    public boolean isSameToPooled14bit(MXMessage message) {
        if (_poolFor14bit != null) {
            if (message.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
                if (message.getGate()._var == _poolFor14bit.getGate()._var) {
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
    }

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
                updateUIByStatus(message, status);
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
                updateUIByStatus(message, status);
            }
        }
        sendToNext(message);
    }

    public void notifyCacheBroken() {
        _data._finder = null;
    }
}
