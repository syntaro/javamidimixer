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
package jp.synthtarou.midimixer.mx30surface;

import jp.synthtarou.midimixer.libs.midi.MXMessageBag;
import java.util.ArrayList;
import java.util.logging.Level;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.MXLogger2;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
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
public class MX32MixerProcess extends MXReceiver<MX32MixerView> implements MXSettingTarget {

    final int _port;
    final MX30Process _parent;
    final MX32MixerView _view;
    MXNoteOffWatcher _noteOff;
    MXSetting _setting;

    MXVisitant16 _visitant16 = new MXVisitant16();
    String _mixerName;

    int _patchToMixer = -1;
    boolean _patchTogether = false;

    public MX32MixerProcess(MX30Process parent, int port) {
        _parent = parent;
        _port = port;
        _view = new MX32MixerView(this);
        _setting = new MXSetting("Mixing" + (port + 1));
        _setting.setTarget(this);
        new MX32MixerInitializer(this).initVolumeMixer();
    }

    public void readSettingFile() {
        _setting.readSettingFile();
    }

    @Override
    public String getReceiverName() {
        return "#" + MXMidi.nameOfPortShort(_port);
    }

    @Override
    public MX32MixerView getReceiverView() {
        return _view;
    }

    @Override
    public void processMXMessage(MXMessage message) {
        if (isUsingThisRecipeDX() == false) {
            sendToNext(message);
            return;
        }

        startProcess(message, null);
    }

    @Override
    public MXSetting getSettings() {
        return _setting;
    }

    @Override
    public void prepareSettingFields() {
        _setting.register("PatchToMixer");

        /* general for circle */
        _setting.register("Circle[].name");
        _setting.register("Circle[].note");
        _setting.register("Circle[].type");
        _setting.register("Circle[].row");
        _setting.register("Circle[].column");
        _setting.register("Circle[].message");
        _setting.register("Circle[].channel");
        _setting.register("Circle[].gate");
        _setting.register("Circle[].value");
        _setting.register("Circle[].valuemin");
        _setting.register("Circle[].valuemax");
        _setting.register("Circle[].isCCPair");
        _setting.register("Circle[].valueinvert");
        _setting.register("Circle[].attributes");

        /* general for slider */
        _setting.register("Slider[].name");
        _setting.register("Slider[].note");
        _setting.register("Slider[].type");
        _setting.register("Slider[].row");
        _setting.register("Slider[].column");
        _setting.register("Slider[].message");
        _setting.register("Slider[].channel");
        _setting.register("Slider[].gate");
        _setting.register("Slider[].value");
        _setting.register("Slider[].valuemin");
        _setting.register("Slider[].valuemax");
        _setting.register("Slider[].isCCPair");
        _setting.register("Slider[].valueinvert");
        _setting.register("Slider[].attributes");

        /* general for pad */
        _setting.register("Pad[].name");
        _setting.register("Pad[].note");
        _setting.register("Pad[].type");
        _setting.register("Pad[].row");
        _setting.register("Pad[].column");
        _setting.register("Pad[].message");
        _setting.register("Pad[].channel");
        _setting.register("Pad[].gate");
        _setting.register("Pad[].value");
        _setting.register("Pad[].valuemin");
        _setting.register("Pad[].valuemax");
        _setting.register("Pad[].isCCPair");
        _setting.register("Pad[].valueinvert");
        _setting.register("Pad[].attributes");

        /* drum */
        _setting.register("Pad[].switchInputOnMin");
        _setting.register("Pad[].switchInputOnMax");
        _setting.register("Pad[].switchMouseOnValue");
        _setting.register("Pad[].switchMouseOffValue");
        _setting.register("Pad[].switchWithToggle");
        _setting.register("Pad[].switchOnlySwitched");

        /* drum out */
        _setting.register("Pad[].switchOutPort");
        _setting.register("Pad[].switchOutChannel");
        _setting.register("Pad[].switchOutStyle");
        _setting.register("Pad[].switchOutValueTypeOn");
        _setting.register("Pad[].switchOutValueTypeOff");

        /* template */
        _setting.register("Pad[].switchTemplateText");
        _setting.register("Pad[].switchTemplateTextGate");

        /* program */
        _setting.register("Pad[].switchProgramType");
        _setting.register("Pad[].switchProgramNumber");
        _setting.register("Pad[].switchProgramMSB");
        _setting.register("Pad[].switchProgramLSB");

        /* note */
        _setting.register("Pad[].switchHarmonyNotes");

        /* sequencer */
        _setting.register("Pad[].switchSequencerFile");
        _setting.register("Pad[].switchSequencerSingleTrack");
        _setting.register("Pad[].switchSequencerSeekStart");
        _setting.register("Pad[].switchSequencerFilterNote");

        /* linkslider TOOD */
        _setting.register("Pad[].switchLinkRow");
        _setting.register("Pad[].switchLinkColumn");
        _setting.register("Pad[].switchLinkMode");
        _setting.register("Pad[].switchLinkKontrolType");
    }

    @Override
    public void afterReadSettingFile() {
        ArrayList<MXSettingNode> children;
        children = _setting.findByPath("Circle[]");
        _patchToMixer = _setting.getSettingAsInt("PatchToMixer", -1);
        int x = 0;
        for (MXSettingNode node : children) {
            ++x;
            int type = node.getSettingAsInt("type", -1);
            int row = node.getSettingAsInt("row", -1);
            int column = node.getSettingAsInt("column", -1);
            if (type < 0 || row < 0 || column < 0) {
                break;
            }
            MGStatus status = new MGStatus(this, type, row, column);
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
                MXRangedValue gate = template.indexOfGateHi() >= 0 ? MXRangedValue.new14bit(gateN) : MXRangedValue.new7bit(gateN);
                MXRangedValue value = new MXRangedValue(valueN, valueMin, valueMax);
                status.setBaseMessage(MXMessageFactory.fromTemplate(_port, template, channel, gate, value));

            } catch (RuntimeException ex) {
                MXLogger2.getLogger(MX32MixerProcess.class).log(Level.WARNING, ex.getMessage(), ex);
            }
            setStatus(MGStatus.TYPE_CIRCLE, row, column, status);
        }

        children = _setting.findByPath("Slider[]");
        for (MXSettingNode node : children) {
            int type = node.getSettingAsInt("type", -1);
            int row = node.getSettingAsInt("row", -1);
            int column = node.getSettingAsInt("column", -1);
            if (type < 0 || row < 0 || column < 0) {
                break;
            }
            MGStatus status = new MGStatus(this, type, row, column);
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
                MXRangedValue gate = template.indexOfGateHi() >= 0 ? MXRangedValue.new14bit(gateN) : MXRangedValue.new7bit(gateN);
                MXRangedValue value = new MXRangedValue(valueN, valueMin, valueMax);
                status.setBaseMessage(MXMessageFactory.fromTemplate(_port, template, channel, gate, value));

                setStatus(MGStatus.TYPE_SLIDER, row, column, status);
            } catch (RuntimeException ex) {
                MXLogger2.getLogger(MX32MixerProcess.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        children = _setting.findByPath("Pad[]");
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
            MGStatus status = new MGStatus(this, type, row, column);
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
                MXRangedValue gate = template.indexOfGateHi() >= 0 ? MXRangedValue.new14bit(gateN) : MXRangedValue.new7bit(gateN);
                MXRangedValue value = new MXRangedValue(valueN, valueMin, valueMax);
                status.setBaseMessage(MXMessageFactory.fromTemplate(_port, template, channel, gate, value));

                /* Drum */
                MGStatusForDrum drum = status._drum;

                int switchInputOnMin = node.getSettingAsInt("switchInputOnMin", 1);
                int switchInputOnMax = node.getSettingAsInt("switchInputOnMax", 127);
                drum._strikeZone = new MXRangedValue(0, switchInputOnMin, switchInputOnMax);

                int switchMouseOnValue = node.getSettingAsInt("switchMouseOnValue", 100);
                int switchMouseOffValue = node.getSettingAsInt("switchMouseOffValue", 0);
                drum._mouseOnValue = switchMouseOnValue;
                drum._mouseOffValue = switchMouseOffValue;

                boolean switchModeToggle = node.getSettingAsBoolean("switchWithToggle", false);
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

                /* program */
                int switchProgramType = node.getSettingAsInt("switchProgramType", MGStatusForDrum.STYLE_PROGRAM_CHANGE);
                int switchProgramNumber = node.getSettingAsInt("switchProgramNumber", 0);
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
                boolean switchSequencerSingleTrack = _setting.getSettingAsBoolean("switchSequencerSingleTrack", false);
                boolean switchSequencerSeekStart = _setting.getSettingAsBoolean("switchSequencerSeekStart", false);
                boolean switchSequencerFilterNote = _setting.getSettingAsBoolean("switchSequencerFilterNote", false);

                drum._sequencerFile = switchSequencerFile;
                drum._sequencerSeekStart = switchSequencerSeekStart;
                drum._sequencerFilterNote = switchSequencerFilterNote;
                drum._sequencerSingleTrack = switchSequencerSingleTrack;

                /* linkslider TOOD */
                int switchLinkRow = node.getSettingAsInt("switchLinkRow", 0);
                int switchLinkColumn = node.getSettingAsInt("switchLinkColumn", -1);
                int switchLinkMode = node.getSettingAsInt("switchLinkMode", MGStatusForDrum.LINKMODE_VALUE);
                int switchLinkKontrolType = node.getSettingAsInt("switchLinkKontrolType", MGStatus.TYPE_SLIDER);

                drum._linkRow = switchLinkRow;
                drum._linkColumn = switchLinkColumn;
                drum._linkKontrolType = switchLinkKontrolType;
                drum._linkMode = switchLinkMode;

                setStatus(MGStatus.TYPE_DRUMPAD, row, column, status);
            } catch (RuntimeException ex) {
                MXLogger2.getLogger(MX32MixerProcess.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        _view.updateUI();
    }

    @Override
    public void beforeWriteSettingFile() {
        int counter;
        counter = 1;
        _setting.setSetting("PatchToMixer", _patchToMixer);
        for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
            for (int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++row) {
                String prefix = "Circle[" + counter + "].";
                MGStatus status = getStatus(MGStatus.TYPE_CIRCLE, row, column);
                MXMessage base = status._base;
                _setting.setSetting(prefix + "name", status._name);
                _setting.setSetting(prefix + "note", status._memo);
                _setting.setSetting(prefix + "type", status._uiType);
                _setting.setSetting(prefix + "row", row);
                _setting.setSetting(prefix + "column", column);
                _setting.setSetting(prefix + "message", base.getTemplateAsText());
                _setting.setSetting(prefix + "channel", base.getChannel());
                _setting.setSetting(prefix + "gate", base.getGate()._value);
                _setting.setSetting(prefix + "value", base.getValue()._value);
                _setting.setSetting(prefix + "valuemin", base.getValue()._min);
                _setting.setSetting(prefix + "valuemax", base.getValue()._max);
                _setting.setSetting(prefix + "isCCPair", status._ccPair14);
                counter++;
            }
        }
        counter = 1;
        for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
            for (int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++row) {
                String prefix = "Slider[" + counter + "].";
                MGStatus status = getStatus(MGStatus.TYPE_SLIDER, row, column);
                MXMessage base = status._base;
                _setting.setSetting(prefix + "name", status._name);
                _setting.setSetting(prefix + "note", status._memo);
                _setting.setSetting(prefix + "type", status._uiType);
                _setting.setSetting(prefix + "row", row);
                _setting.setSetting(prefix + "column", column);
                _setting.setSetting(prefix + "message", base.getTemplateAsText());
                _setting.setSetting(prefix + "channel", base.getChannel());
                _setting.setSetting(prefix + "gate", base.getGate()._value);
                _setting.setSetting(prefix + "value", base.getValue()._value);
                _setting.setSetting(prefix + "valuemin", base.getValue()._min);
                _setting.setSetting(prefix + "valuemax", base.getValue()._max);
                _setting.setSetting(prefix + "isCCPair", status._ccPair14);

                counter++;
            }
        }
        counter = 1;
        for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
            for (int row = 0; row < MXAppConfig.DRUM_ROW_COUNT; ++row) {
                String prefix = "Pad[" + counter + "].";
                MGStatus status = getStatus(MGStatus.TYPE_DRUMPAD, row, column);
                MXMessage base = status._base;
                _setting.setSetting(prefix + "name", status._name);
                _setting.setSetting(prefix + "note", status._memo);
                _setting.setSetting(prefix + "type", status._uiType);
                _setting.setSetting(prefix + "row", row);
                _setting.setSetting(prefix + "column", column);
                _setting.setSetting(prefix + "message", base.getTemplateAsText());
                _setting.setSetting(prefix + "channel", base.getChannel());
                _setting.setSetting(prefix + "gate", base.getGate()._value);
                _setting.setSetting(prefix + "value", base.getValue()._value);
                _setting.setSetting(prefix + "valuemin", base.getValue()._min);
                _setting.setSetting(prefix + "valuemax", base.getValue()._max);
                _setting.setSetting(prefix + "isCCPair", status._ccPair14);

                /* Drum */
                MGStatusForDrum drum = status._drum;
                _setting.setSetting(prefix + "switchInputOnMin", drum._strikeZone._min);
                _setting.setSetting(prefix + "switchInputOnMax", drum._strikeZone._max);

                _setting.setSetting(prefix + "switchMouseOnValue", drum._mouseOnValue);
                _setting.setSetting(prefix + "switchMouseOffValue", drum._mouseOffValue);

                _setting.setSetting(prefix + "switchWithToggle", drum._modeToggle);

                _setting.setSetting(prefix + "switchOnlySwitched", drum._onlySwitched);

                /* drum out */
                _setting.setSetting(prefix + "switchOutPort", drum._outPort);
                _setting.setSetting(prefix + "switchOutChannel", drum._outChannel);

                _setting.setSetting(prefix + "switchOutStyle", drum._outStyle);
                _setting.setSetting(prefix + "switchOutValueTypeOn", drum._outValueTypeOn);
                _setting.setSetting(prefix + "switchOutValueTypeOff", drum._outValueTypeOff);

                /* template */
                _setting.setSetting(prefix + "switchTemplateText", drum._templateText);
                _setting.setSetting(prefix + "switchTemplateTextGate", drum._teplateTextGate);

                /* program */
                _setting.setSetting(prefix + "switchProgramType", drum._programType);
                _setting.setSetting(prefix + "switchProgramNumber", drum._programNumber);
                _setting.setSetting(prefix + "switchProgramMSB", drum._programMSB);
                _setting.setSetting(prefix + "switchProgramLSB", drum._programLSB);

                /* note */
                _setting.setSetting(prefix + "switchHarmonyNotes", drum._harmonyNotes);

                /* sequencer */
                _setting.setSetting(prefix + "switchSequencerFile", drum._sequencerFile);
                _setting.setSetting(prefix + "switchSequencerSingleTrack", drum._sequencerSeekStart);
                _setting.setSetting(prefix + "switchSequencerSeekStart", drum._sequencerFilterNote);
                _setting.setSetting(prefix + "switchSequencerFilterNote", drum._sequencerSingleTrack);

                /* linkslider TOOD */
                _setting.setSetting(prefix + "switchLinkRow", drum._linkRow);
                _setting.setSetting(prefix + "switchLinkColumn", drum._linkColumn);
                _setting.setSetting(prefix + "switchLinkMode", drum._linkMode);
                _setting.setSetting(prefix + "switchLinkKontrolType", drum._linkKontrolType);

                setStatus(MGStatus.TYPE_DRUMPAD, row, column, status);

                counter++;
            }
        }
    }

    void updateStatusAndSend(MGStatus status, int newValue) {
        MXMessageBag bag = new MXMessageBag();
        updateStatusAndGetResult(status, newValue, null, bag);
        while (true) {
            MXMessage message = bag.popResult();
            if (message == null) {
                break;
            }
            sendToNext(message);
            _parent.processMXMessage(message);
        }
    }

    void updateStatusAndGetResult(MGStatus status, int newValue, MXTiming timing, MXMessageBag bag) {
        if (_parent._underConstruction) {
            return;
        }
        synchronized (MXTiming.mutex) {
            if (timing == null) {
                timing = new MXTiming();
            }
            if (bag.isTouchedStatus(status)) {
                return;
            }
            MXMessage message = null;
            int row = status._row, column = status._column;
            int uiType = status._uiType;
            if (uiType == MGStatus.TYPE_SLIDER) {
                MGSlider slider = getSlider(row, column);
                status.setMessageValue(status.getValue().changeValue(newValue));
                message = (MXMessage) status._base.clone();
                slider.publishUI();
            } else if (uiType == MGStatus.TYPE_CIRCLE) {
                MGCircle circle = getCircle(row, column);
                status.setMessageValue(status.getValue().changeValue(newValue));
                message = (MXMessage) status._base.clone();
                circle.publishUI();
            } else {
                return;//nothing
            }

            if (_patchToMixer >= 0) {
                MX32MixerProcess nextMixer = _parent.getPage(_patchToMixer);
                MGStatus nextStatus = nextMixer.getStatus(status._uiType, row, column);

                int nextMin = nextStatus._base.getValue()._min;
                int nextMax = nextStatus._base.getValue()._max;
                newValue = status._base.getValue().changeRange(nextMin, nextMax)._value;
                nextMixer.updateStatusAndGetResult(nextStatus, newValue, timing, bag);
            }

            if (_patchToMixer < 0 || _patchTogether) {
                if (message != null) {
                    message._timing = timing;
                    message._mx30record = new MGStatus[]{status};
                    startProcess(message, bag);
                    bag.addResult(message);
                }
            }
        }
    }

    MXMessage _poolFor14bit = null;
    int _gotValue14bit = 0;

    public boolean isPairToPooled14bit(MXMessage message) {
        if (_poolFor14bit != null) {
            if (message.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
                int gate = message.getGate()._value;
                if (gate >= 0 && gate < 32) {
                    if (gate + 32 == _poolFor14bit.getGate()._value) {
                        return true;
                    }
                } else if (gate >= 32 && gate < 64) {
                    if (gate == _poolFor14bit.getGate()._value + 32) {
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
                int gate = message.getGate()._value;
                if (gate >= 0 && gate < 32) {
                    if (gate + 32 == _poolFor14bit.getGate()._value) {
                        return message.getValue()._value * 128 + _poolFor14bit.getValue()._value;
                    }
                } else if (gate >= 32 && message.getChannel() <= 64) {
                    if (gate == _poolFor14bit.getGate()._value + 32) {
                        return message.getValue()._value + _poolFor14bit.getValue()._value * 128;
                    }
                }
            }
        }
        throw new IllegalStateException("valueForPair not work at the moment");
    }

    public boolean isSameToPooled14bit(MXMessage message) {
        if (_poolFor14bit != null) {
            if (message.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
                if (message.getGate()._value == _poolFor14bit.getGate()._value) {
                    return true;
                }
            }
        }
        return false;
    }

    long lastTick = 0;

    public void clearPoolImpl() {
        if (lastTick + 400 < System.currentTimeMillis()) {
            if (_poolFor14bit != null) {
                MXMessage prev = _poolFor14bit;
                _poolFor14bit = null;
                prev.setPairedWith14(true);
                sendToNext(prev);
            }
        }
    }

    public void notifyCacheBroken() {
        synchronized (this) {
            _finder = null;
        }
    }

    MGStatusFinder _finder = null;

    ArrayList<MGSlider>[] _matrixSliderComponent;
    ArrayList<MGCircle>[] _matrixCircleComponent;
    ArrayList<MGDrumPad>[] _matrixDrumComponent;

    ArrayList<MGStatus>[] _matrixSliderStatus;
    ArrayList<MGStatus>[] _matrixCircleStatus;
    ArrayList<MGStatus>[] _matrixDrumStatus;

    public MGSlider getSlider(int row, int column) {
        if (_matrixSliderComponent == null) {
            return null;
        }
        if (row >= MXAppConfig.SLIDER_ROW_COUNT || column >= MXAppConfig.SLIDER_COLUMN_COUNT) {
            throw new IllegalArgumentException("row " + row + " , column = " + column);
        }
        return _matrixSliderComponent[row].get(column);
    }

    public MGCircle getCircle(int row, int column) {
        if (_matrixCircleComponent == null) {
            return null;
        }
        if (row >= MXAppConfig.CIRCLE_ROW_COUNT || column >= MXAppConfig.SLIDER_COLUMN_COUNT) {
            throw new IllegalArgumentException("row " + row + " , column = " + column);
        }
        return _matrixCircleComponent[row].get(column);
    }

    public MGDrumPad getDrumPad(int row, int column) {
        if (_matrixDrumComponent == null) {
            return null;
        }
        if (row >= MXAppConfig.DRUM_ROW_COUNT || column >= MXAppConfig.SLIDER_COLUMN_COUNT) {
            throw new IllegalArgumentException("row " + row + " , column = " + column);
        }
        return _matrixDrumComponent[row].get(column);
    }

    public MGStatus getStatus(int type, int row, int column) {
        switch (type) {
            case MGStatus.TYPE_CIRCLE:
                return _matrixCircleStatus[row].get(column);
            case MGStatus.TYPE_SLIDER:
                return _matrixSliderStatus[row].get(column);
            case MGStatus.TYPE_DRUMPAD:
                return _matrixDrumStatus[row].get(column);
        }
        return null;
    }

    public void setStatus(int type, int row, int column, MGStatus status) {
        switch (type) {
            case MGStatus.TYPE_CIRCLE:
                _matrixCircleStatus[row].set(column, status);
                break;
            case MGStatus.TYPE_SLIDER:
                _matrixSliderStatus[row].set(column, status);
                break;
            case MGStatus.TYPE_DRUMPAD:
                _matrixDrumStatus[row].set(column, status);
                break;
        }
    }

    public void setEveryComponents(ArrayList<MGSlider>[] slider, ArrayList<MGCircle>[] circle, ArrayList<MGDrumPad>[] drum) {
        _matrixSliderComponent = slider;
        _matrixCircleComponent = circle;
        _matrixDrumComponent = drum;

        notifyCacheBroken();
    }

    public void startProcess(MXMessage message, MXMessageBag bag) {
        if (message == null) {
            return;
        }
        boolean createdNow = false;
        if (bag == null) {
            createdNow = true;
            bag = new MXMessageBag();
        }
        if (message.isEmpty()) {
            sendToNext(message);
            return;
        }
        if (MXVisitant.isMesssageHaveVisitant(message)) {
            _visitant16.get(message.getChannel()).updateVisitantChannel(message);
        }
        if (message.isMessageTypeChannel()) {
            _visitant16.get(message.getChannel()).attachChannelVisitantToMessage(message);
        }

        synchronized (this) {
            if (_finder == null) {
                _finder = new MGStatusFinder(this);
            }
        }

        bag.addQueue(message);

        while (true) {
            message = bag.popQueue();
            if (message == null) {
                break;
            }
            ArrayList<MGStatus> listStatus = _finder.findCandidate(message);
            boolean proc = false;
            if (listStatus != null && listStatus.isEmpty() == false) {
                for (MGStatus seek : listStatus) {
                    if (bag.isTouchedStatus(seek)) {
                        continue;
                    }
                    if (seek.controlByMessage(message, bag)) {
                        updateStatusAndGetResult(seek, seek.getValue()._value, message._timing, bag);
                        proc = true;
                    }
                }
                message._mx30record = bag.listTouchedStatus();
            }
            if (!proc) {
                bag.addResult(message);
            }
        }
        if (createdNow) {
            flushSendQueue(bag);
        }
    }

    void flushSendQueue(MXMessageBag bag) {
        while (true) {
            MXMessage message = bag.popResult();
            if (message == null) {
                break;
            }

            int port = message.getPort();
            sendToNext(message);
            _parent._pageProcess[port].startProcess(message, bag);
        }
        while (true) {
            Runnable run = bag.popResultTask();
            if (run == null) {
                break;
            }
            try {
                run.run();
            } catch (RuntimeException ex) {
                MXLogger2.getLogger(MX32MixerProcess.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
}
