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

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.plaf.SliderUI;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.libs.json.MXJsonValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.libs.inifile.MXINIFileNode;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import jp.synthtarou.midimixer.mx36ccmapping.MX36Process;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX32MixerProcess extends MXReceiver<MX32MixerView> implements MXINIFileSupport, MXJsonSupport  {

    final int _port;
    final MX30Process _parent;
    final MX32MixerView _view;
    MXNoteOffWatcher _noteOff;

    String _mixerName;

    int _patchToMixer = -1;
    boolean _patchTogether = false;

    public MX32MixerProcess(MX30Process parent, int port) {
        _parent = parent;
        _port = port;
        _view = new MX32MixerView(this);
        new MX32MixerInitializer(this).initVolumeMixer();
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
        if (isUsingThisRecipe() == false) {
            sendToNext(message);
            return;
        }

        startProcess(message);
    }

    @Override
    public MXINIFile prepareINIFile(File custom) {
        if (custom == null) {
            custom = MXINIFile.pathOf("Mixing" + (_port + 1));
        }
        MXINIFile setting = new MXINIFile(custom, this);
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

        /* program */
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
        setting.register("Pad[].switchLinkMode");
        setting.register("Pad[].switchLinkKontrolType");
        
        return setting;
    }

    @Override
    public boolean readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        if (!setting.readINIFile()) {
            return false;
        }
        ArrayList<MXINIFileNode> children;
        children = setting.findByPath("Circle[]");
        _patchToMixer = setting.getSettingAsInt("PatchToMixer", -1);
        int x = 0;
        for (MXINIFileNode node : children) {
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
                MXFileLogger.getLogger(MX32MixerProcess.class).log(Level.WARNING, ex.getMessage(), ex);
            }
            setStatus(MGStatus.TYPE_CIRCLE, row, column, status);
        }

        children = setting.findByPath("Slider[]");
        for (MXINIFileNode node : children) {
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
                MXFileLogger.getLogger(MX32MixerProcess.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        children = setting.findByPath("Pad[]");
        int count = 0;
        for (MXINIFileNode node : children) {
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
                int switchLinkMode = node.getSettingAsInt("switchLinkMode", MGStatusForDrum.LINKMODE_VALUE);
                int switchLinkKontrolType = node.getSettingAsInt("switchLinkKontrolType", MGStatus.TYPE_SLIDER);

                drum._linkRow = switchLinkRow;
                drum._linkColumn = switchLinkColumn;
                drum._linkKontrolType = switchLinkKontrolType;
                drum._linkMode = switchLinkMode;

                setStatus(MGStatus.TYPE_DRUMPAD, row, column, status);
            } catch (RuntimeException ex) {
                MXFileLogger.getLogger(MX32MixerProcess.class).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        _view.updateUI();
        return true;
    }

    @Override
    public boolean writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        int counter;
        counter = 1;
        setting.setSetting("PatchToMixer", _patchToMixer);
        for (int column = 0; column < MXConfiguration.SLIDER_COLUMN_COUNT; ++column) {
            for (int row = 0; row < MXConfiguration.CIRCLE_ROW_COUNT; ++row) {
                String prefix = "Circle[" + counter + "].";
                MGStatus status = getStatus(MGStatus.TYPE_CIRCLE, row, column);
                MXMessage base = status._base;
                setting.setSetting(prefix + "name", status._name);
                setting.setSetting(prefix + "note", status._memo);
                setting.setSetting(prefix + "type", status._uiType);
                setting.setSetting(prefix + "row", row);
                setting.setSetting(prefix + "column", column);
                setting.setSetting(prefix + "message", base.getTemplateAsText());
                setting.setSetting(prefix + "channel", base.getChannel());
                setting.setSetting(prefix + "gate", base.getGate()._value);
                setting.setSetting(prefix + "value", base.getValue()._value);
                setting.setSetting(prefix + "valuemin", base.getValue()._min);
                setting.setSetting(prefix + "valuemax", base.getValue()._max);
                setting.setSetting(prefix + "isCCPair", status._ccPair14);
                counter++;
            }
        }
        counter = 1;
        for (int column = 0; column < MXConfiguration.SLIDER_COLUMN_COUNT; ++column) {
            for (int row = 0; row < MXConfiguration.SLIDER_ROW_COUNT; ++row) {
                String prefix = "Slider[" + counter + "].";
                MGStatus status = getStatus(MGStatus.TYPE_SLIDER, row, column);
                MXMessage base = status._base;
                setting.setSetting(prefix + "name", status._name);
                setting.setSetting(prefix + "note", status._memo);
                setting.setSetting(prefix + "type", status._uiType);
                setting.setSetting(prefix + "row", row);
                setting.setSetting(prefix + "column", column);
                setting.setSetting(prefix + "message", base.getTemplateAsText());
                setting.setSetting(prefix + "channel", base.getChannel());
                setting.setSetting(prefix + "gate", base.getGate()._value);
                setting.setSetting(prefix + "value", base.getValue()._value);
                setting.setSetting(prefix + "valuemin", base.getValue()._min);
                setting.setSetting(prefix + "valuemax", base.getValue()._max);
                setting.setSetting(prefix + "isCCPair", status._ccPair14);

                counter++;
            }
        }
        counter = 1;
        for (int column = 0; column < MXConfiguration.SLIDER_COLUMN_COUNT; ++column) {
            for (int row = 0; row < MXConfiguration.DRUM_ROW_COUNT; ++row) {
                String prefix = "Pad[" + counter + "].";
                MGStatus status = getStatus(MGStatus.TYPE_DRUMPAD, row, column);
                MXMessage base = status._base;
                setting.setSetting(prefix + "name", status._name);
                setting.setSetting(prefix + "note", status._memo);
                setting.setSetting(prefix + "type", status._uiType);
                setting.setSetting(prefix + "row", row);
                setting.setSetting(prefix + "column", column);
                setting.setSetting(prefix + "message", base.getTemplateAsText());
                setting.setSetting(prefix + "channel", base.getChannel());
                setting.setSetting(prefix + "gate", base.getGate()._value);
                setting.setSetting(prefix + "value", base.getValue()._value);
                setting.setSetting(prefix + "valuemin", base.getValue()._min);
                setting.setSetting(prefix + "valuemax", base.getValue()._max);
                setting.setSetting(prefix + "isCCPair", status._ccPair14);

                /* Drum */
                MGStatusForDrum drum = status._drum;
                setting.setSetting(prefix + "switchInputOnMin", drum._strikeZone._min);
                setting.setSetting(prefix + "switchInputOnMax", drum._strikeZone._max);

                setting.setSetting(prefix + "switchMouseOnValue", drum._mouseOnValue);
                setting.setSetting(prefix + "switchMouseOffValue", drum._mouseOffValue);

                setting.setSetting(prefix + "switchWithToggle", drum._modeToggle);

                setting.setSetting(prefix + "switchOnlySwitched", drum._onlySwitched);

                /* drum out */
                setting.setSetting(prefix + "switchOutPort", drum._outPort);
                setting.setSetting(prefix + "switchOutChannel", drum._outChannel);

                setting.setSetting(prefix + "switchOutStyle", drum._outStyle);
                setting.setSetting(prefix + "switchOutValueTypeOn", drum._outValueTypeOn);
                setting.setSetting(prefix + "switchOutValueTypeOff", drum._outValueTypeOff);

                /* template */
                setting.setSetting(prefix + "switchTemplateText", drum._templateText);
                setting.setSetting(prefix + "switchTemplateTextGate", drum._teplateTextGate);

                /* program */
                setting.setSetting(prefix + "switchProgramType", drum._programType);
                setting.setSetting(prefix + "switchProgramNumber", drum._programNumber);
                setting.setSetting(prefix + "switchProgramMSB", drum._programMSB);
                setting.setSetting(prefix + "switchProgramLSB", drum._programLSB);

                /* note */
                setting.setSetting(prefix + "switchHarmonyNotes", drum._harmonyNotes);

                /* sequencer */
                setting.setSetting(prefix + "switchSequencerFile", drum._sequencerFile);
                setting.setSetting(prefix + "switchSequencerSingleTrack", drum._sequencerSeekStart);
                setting.setSetting(prefix + "switchSequencerSeekStart", drum._sequencerFilterNote);
                setting.setSetting(prefix + "switchSequencerFilterNote", drum._sequencerSingleTrack);

                /* linkslider TOOD */
                setting.setSetting(prefix + "switchLinkRow", drum._linkRow);
                setting.setSetting(prefix + "switchLinkColumn", drum._linkColumn);
                setting.setSetting(prefix + "switchLinkMode", drum._linkMode);
                setting.setSetting(prefix + "switchLinkKontrolType", drum._linkKontrolType);

                setStatus(MGStatus.TYPE_DRUMPAD, row, column, status);

                counter++;
            }
        }
        return setting.writeINIFile();
    }
    
    void updateUIStatusAndGetResult(MGStatus status, int newValue, MXTiming timing) {
        if (_parent._underConstruction) {
            return;
        }
        synchronized (MXTiming.mutex) {
            if (timing == null) {
                timing = new MXTiming();
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
                status.setMessageValue(newValue);
                status._drum.messageDetected();
                message = null;
            }

            if (_patchToMixer >= 0) {
                MX32MixerProcess nextMixer = _parent.getPage(_patchToMixer);
                MGStatus nextStatus = nextMixer.getStatus(status._uiType, row, column);

                int nextMin = nextStatus._base.getValue()._min;
                int nextMax = nextStatus._base.getValue()._max;
                newValue = status._base.getValue().changeRange(nextMin, nextMax)._value;

                MGSliderMove move = new MGSliderMove(nextStatus, newValue, timing);
                _parent.addSliderMove(move);
                if (!_patchTogether) {
                    message = null;
                }
            }
            
            MX36Process mapping = _parent._mappingProcess;
            if (mapping != null) {
                if (mapping.isNotLinked(status)) {
                    mapping.addToAutoDetected(status);
                }
                else if (mapping.invokeMapping(status)) {
                    return;
                }
            }

            if (message != null) {
                message._timing = timing;
                startProcess(message);
                
                MXMessageBag bag = _parent.startBagging();
                try {
                    bag.addResult(message);
                }finally {
                    _parent.endBagging();
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
        if (row >= MXConfiguration.SLIDER_ROW_COUNT || column >= MXConfiguration.SLIDER_COLUMN_COUNT) {
            throw new IllegalArgumentException("row " + row + " , column = " + column);
        }
        return _matrixSliderComponent[row].get(column);
    }

    public MGCircle getCircle(int row, int column) {
        if (_matrixCircleComponent == null) {
            return null;
        }
        if (row >= MXConfiguration.CIRCLE_ROW_COUNT || column >= MXConfiguration.SLIDER_COLUMN_COUNT) {
            throw new IllegalArgumentException("row " + row + " , column = " + column);
        }
        return _matrixCircleComponent[row].get(column);
    }

    public MGDrumPad getDrumPad(int row, int column) {
        if (_matrixDrumComponent == null) {
            return null;
        }
        if (row >= MXConfiguration.DRUM_ROW_COUNT || column >= MXConfiguration.SLIDER_COLUMN_COUNT) {
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

    public void startProcess(MXMessage message) {
        if (message == null) {
            return;
        }
        if (message.isEmpty()) {
            sendToNext(message);
            return;
        }

        synchronized (this) {
            if (_finder == null) {
                _finder = new MGStatusFinder(this);
            }
        }

        MXMessageBag bag = _parent.startBagging();
        try {
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
                        int x = seek.controlByMessage(message);
                        if (x >= 0) {
                            _parent.addSliderMove(seek, x);
                            proc = true;
                        }
                    }
                }
                if (!proc) {
                    bag.addResult(message);
                }
            }
        }finally{
            _parent.endBagging();
        }
    }


    @Override
    public boolean readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("Mixing" + (_port + 1));
        }
        MXJsonValue value = new MXJsonParser(custom).parseFile();
        if (value == null) {
            return false;
        }
        //TODO
        return true;
    }

    @Override
    public boolean writeJsonFile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("Mixing" + (_port + 1));
        }
        MXJsonValue value = new MXJsonValue(null);

        MXJsonParser parser = new MXJsonParser(custom);
        parser.setRoot(value);
        return parser.writeFile();
    }

    @Override
    public void resetSetting() {
        new MX32MixerInitializer(this).initVolumeMixer();
        _view.updateUI();
    }
}
