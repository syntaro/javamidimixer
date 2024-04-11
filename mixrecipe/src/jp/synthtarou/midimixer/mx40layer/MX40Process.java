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

import jp.synthtarou.midimixer.libs.midi.visitant.MXVisitant16TableModel;
import jp.synthtarou.midimixer.libs.midi.visitant.MXVisitant;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.libs.inifile.MXINIFileNode;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.json.MXJsonValue;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX40Process extends MXReceiver<MX40View> implements MXINIFileSupport, MXJsonSupport {

    MX40View _view;
    MXVisitant16TableModel _inputInfo;
    MXVisitant16TableModel _outputInfo;
    MXNoteOffWatcher _noteOff;
    ArrayList<MX40Group> _groupList;

    public MX40Process() {
        _groupList = new ArrayList();
        _inputInfo = new MXVisitant16TableModel();
        _outputInfo = new MXVisitant16TableModel();
        _noteOff = new MXNoteOffWatcher();
        _view = new MX40View(this);
    }

    MXMessage[] _buf = new MXMessage[2];

    public void sendToNext(MXMessage message) {
        MXMessage[] buf = _outputInfo.preprocess16ForVisitant(message, _buf);
        if (buf != null) {
            _buf = buf;
        } else {
            buf = _buf;
            buf[0] = message;
            for (int i = 1; i < buf.length; ++i) {
                buf[i] = null;
            }
        }
        for (int i = 0; i < buf.length; ++i) {
            if (buf[i] == null) {
                continue;
            }
            if (_outputInfo.mergeVisitant16WithVisitant(buf[i])) {

            }
            super.sendToNext(buf[i]);
        }
    }

    @Override
    public void processMXMessage(MXMessage message) {
        if (isUsingThisRecipe() == false) {
            sendToNext(message);
            return;
        }

        try {

            MXMessage[] buf = _inputInfo.preprocess16ForVisitant(message, _buf);
            if (buf != null) {
                _buf = buf;
            } else {
                buf = _buf;
                buf[0] = message;
                for (int i = 1; i < buf.length; ++i) {
                    buf[i] = null;
                }
            }
            for (int mi = 0; mi < _buf.length; ++mi) {
                message = _buf[mi];
                if (message !=  null) {
                    sendToNext(message);
                    message = null;
                    continue;
                }
                if (message == null) {
                    continue;
                }
                _inputInfo.mergeVisitant16WithVisitant(message);
                
                if (message.isBinaryMessage()) {
                    sendToNext(message);
                    continue;
                }

                int port = message.getPort();
                int status = message.getStatus();
                int channel = message.getChannel();
                int command = status;
                if (command >= 0x80 && command <= 0xef) {
                    command &= 0xf0;
                }

                int first = message.getTemplate().get(0);

                if (first == MXMidi.COMMAND2_CH_PROGRAM_INC) {
                    int x = message.getVisitant().getProgram() + 1;
                    if (x >= 128) {
                        x = 127;
                    }
                    message = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_CH_PROGRAMCHANGE + message.getChannel(), x, 0);
                    command = MXMidi.COMMAND_CH_PROGRAMCHANGE;
                }
                if (first == MXMidi.COMMAND2_CH_PROGRAM_DEC) {
                    int x = message.getVisitant().getProgram() - 1;
                    if (x < 0) {
                        x = 0;
                    }
                    message = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_CH_PROGRAMCHANGE + message.getChannel(), x, 0);
                    command = MXMidi.COMMAND_CH_PROGRAMCHANGE;
                }

                if (command == MXMidi.COMMAND_CH_NOTEOFF) {
                    if (_noteOff.raiseHandler(port, channel, message.getGate()._value)) {
                        continue;
                    }
                }

                boolean dispatched = false;

                if (message.isMessageTypeChannel()) {
                    for (int i = 0; i < _groupList.size(); ++i) {
                        final MX40Group col = _groupList.get(i);
                        if (col.processByGroup(message)) {
                            if (command == MXMidi.COMMAND_CH_NOTEON) {
                                _noteOff.setHandler(message, message, new MXNoteOffWatcher.Handler() {
                                    public void onNoteOffEvent(MXMessage target) {
                                        MXMessage msg = MXMessageFactory.fromShortMessage(target.getPort(),
                                                MXMidi.COMMAND_CH_NOTEOFF + target.getChannel(),
                                                target.getGate()._value, 0);
                                        col.processByGroup(msg);
                                    }
                                });
                            }
                            dispatched = true;
                            break;
                        }
                    }
                }
                if (!dispatched) {
                    if (command == MXMidi.COMMAND_CH_NOTEON) {
                        _noteOff.setHandler(message, message, new MXNoteOffWatcher.Handler() {
                            public void onNoteOffEvent(MXMessage target) {
                                MXMessage msg = MXMessageFactory.fromShortMessage(target.getPort(),
                                        MXMidi.COMMAND_CH_NOTEOFF + target.getChannel(),
                                        target.getGate()._value, 0);
                                //msg._timing = timing;
                                sendToNext(msg);
                            }
                        });
                    }
                    if (message.isCommand(MXMidi.COMMAND_CH_PROGRAMCHANGE) && message.getGate()._value < 0) {
                        continue;
                    }
                    sendToNext(message);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();;
        }

    }

    @Override
    public String getReceiverName() {
        return "Sound Layer";
    }

    @Override
    public MX40View getReceiverView() {
        return _view;
    }

    public synchronized void resendProgramChange() {
        for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++port) {
            for (int channel = 0; channel < 16; ++channel) {
                MXVisitant info = _inputInfo.getVisitant(port, channel);
                if (info.isHavingProgram()) {
                    MXMessage programChange = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_PROGRAMCHANGE + channel, info.getProgram(), 0);
                    processMXMessage(programChange);
                } else {
                    //Tricky way to set Gate -1
                    //TODO Magic Number 
                    /*
                    MXMessage programChange = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_PROGRAMCHANGE + channel, 0, 0);
                    programChange.setGate(null);
                    processMXMessage(programChange);
                     */
                }
            }
        }
    }

    @Override
    public MXINIFile prepareINIFile(File custom) {
        if (custom == null) {
            custom = MXINIFile.pathOf("SoundLayer");
        }
        MXINIFile setting = new MXINIFile(custom, this);

        setting.register("Group[].title");
        setting.register("Group[].isWatchPort");
        setting.register("Group[].watchingPort");
        setting.register("Group[].isWatchChannel");
        setting.register("Group[].watchingCahnnel");
        setting.register("Group[].isWatchBank");
        setting.register("Group[].watchingBankMSB");
        setting.register("Group[].watchingBankLSB");
        setting.register("Group[].isWatchProgram");
        setting.register("Group[].watchingProgram");
        setting.register("Group[].rotateLayer");
        setting.register("Group[].rotatePoly");

        setting.register("Group[].Layer");
        setting.register("Group[].Layer[]");
        setting.register("Group[].Layer[].title");
        setting.register("Group[].Layer[].disabled");
        setting.register("Group[].Layer[].modPort");
        setting.register("Group[].Layer[].fixedPort");
        setting.register("Group[].Layer[].modChannel");
        setting.register("Group[].Layer[].fixedChannel");
        setting.register("Group[].Layer[].modBank");
        setting.register("Group[].Layer[].fixedBankMSB");
        setting.register("Group[].Layer[].fixedBankLSB");
        setting.register("Group[].Layer[].modProgram");
        setting.register("Group[].Layer[].fixedProgram");
        setting.register("Group[].Layer[].modPan");
        setting.register("Group[].Layer[].fixedPan");
        setting.register("Group[].Layer[].adjustExpression");
        setting.register("Group[].Layer[].adjustTranspose");
        setting.register("Group[].Layer[].adjustVelocity");
        setting.register("Group[].Layer[].sendKeyLowest");
        setting.register("Group[].Layer[].sendKeyHighest");
        setting.register("Group[].Layer[].sendVelocityLowest");
        setting.register("Group[].Layer[].sendVelocityHighest");
        return setting;
    }

    @Override
    public boolean readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        if (!setting.readINIFile()) {
            return false;
        }
        ArrayList<MX40Group> newGroupList = new ArrayList();
        this._groupList = newGroupList;
        List<MXINIFileNode> readingGroups = setting.findByPath("Group");
        if (readingGroups.size() > 0) {
            readingGroups = readingGroups.get(0).findNumbers();
        }
        for (MXINIFileNode node : readingGroups) {
            MX40Group group = new MX40Group(this);
            group._title = node.findNode("title")._value;

            group._isWatchPort = node.getSettingAsBoolean("isWatchPort", false);
            group._watchingPort = node.getSettingAsInt("watchingPort", 0);
            group._isWatchChannel = node.getSettingAsBoolean("isWatchChannel", false);
            group._watchingChannel = node.getSettingAsInt("watchingCahnnel", 0);
            group._isRotate = node.getSettingAsBoolean("rotateLayer", false);
            group._rotatePoly = node.getSettingAsInt("rotatePoly", 16);

            group._isWatchBank = node.getSettingAsBoolean("isWatchBank", false);
            group._watchingBankMSB = node.getSettingAsInt("watchingBankMSB", 0);
            group._watchingBankLSB = node.getSettingAsInt("watchingBankLSB", 0);

            group._isWatchProgram = node.getSettingAsBoolean("isWatchProgram", false);
            group._watchingProgram = node.getSettingAsInt("watchingProgram", 0);
            newGroupList.add(group);

            MXINIFileNode layerNode = node.findNode("Layer");
            if (layerNode != null) {
                List<MXINIFileNode> numbers = layerNode.findNumbers();
                for (MXINIFileNode node2 : numbers) {
                    MX40Layer layer = new MX40Layer(this, group);
                    layer._title = node2.getSetting("title");
                    layer._disabled = node2.getSettingAsBoolean("disabled", false);
                    layer._modPort = node2.getSettingAsInt("modPort", MX40Layer.MOD_ASFROM);
                    layer._fixedPort = node2.getSettingAsInt("fixedPort", 0);

                    layer._modChannel = node2.getSettingAsInt("modChannel", MX40Layer.MOD_ASFROM);
                    layer._fixedChannel = node2.getSettingAsInt("fixedChannel", 0);

                    layer._modBank = node2.getSettingAsInt("modBank", MX40Layer.MOD_ASFROM);
                    layer._fixedBankMSB = node2.getSettingAsInt("fixedBankMSB", 0);
                    layer._fixedBankLSB = node2.getSettingAsInt("fixedBankLSB", 0);

                    layer._modProgram = node2.getSettingAsInt("modProgram", MX40Layer.MOD_ASFROM);
                    layer._fixedProgram = node2.getSettingAsInt("fixedProgram", 0);

                    layer._modPan = node2.getSettingAsInt("modPan", MX40Layer.MOD_ASFROM);
                    layer._fixedPan = node2.getSettingAsInt("fixedPan", 64);

                    layer._adjustExpression = node2.getSettingAsInt("adjustExpression", 100);
                    layer._adjustTranspose = node2.getSettingAsInt("adjustTranspose", 0);
                    layer._adjustVelocity = node2.getSettingAsInt("adjustVelocity", 0);
                    layer.setAcceptKeyLowest(node2.getSettingAsInt("sendKeyLowest", 0));
                    layer.setAcceptKeyHighest(node2.getSettingAsInt("sendKeyHighest", 127));
                    layer.setAcceptVelocityLowest(node2.getSettingAsInt("sendVelocityLowest", 0));
                    layer.setAcceptVelocityHighest(node2.getSettingAsInt("sendVelocityHighest", 127));

                    group._listLayer.add(layer);
                }
            }
        }
        resendProgramChange();
        _view.justRefreshViewListAndPanel();
        return true;
    }

    @Override
    public boolean writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        for (int i = 0; i < _groupList.size(); i++) {
            String prefixG = "Group[" + i + "]";
            MX40Group group = _groupList.get(i);
            setting.setSetting(prefixG + ".title", group._title);
            setting.setSetting(prefixG + ".isWatchPort", group._isWatchPort);
            setting.setSetting(prefixG + ".watchingPort", group._watchingPort);
            setting.setSetting(prefixG + ".isWatchChannel", group._isWatchChannel);
            setting.setSetting(prefixG + ".watchingCahnnel", group._watchingChannel);
            setting.setSetting(prefixG + ".isWatchBank", group._isWatchBank);
            setting.setSetting(prefixG + ".watchingBankMSB", group._watchingBankMSB);
            setting.setSetting(prefixG + ".watchingBankLSB", group._watchingBankLSB);
            setting.setSetting(prefixG + ".isWatchProgram", group._isWatchProgram);
            setting.setSetting(prefixG + ".watchingProgram", group._watchingProgram);
            setting.setSetting(prefixG + ".rotateLayer", group._isRotate);
            setting.setSetting(prefixG + ".rotatePoly", group._rotatePoly);

            for (int j = 0; j < group._listLayer.size(); ++j) {
                String prefixL = "Group[" + i + "].Layer[" + j + "]";
                MX40Layer layer = group._listLayer.get(j);
                setting.setSetting(prefixL + ".title", layer._title);
                setting.setSetting(prefixL + ".disabled", layer._disabled);
                setting.setSetting(prefixL + ".modPort", layer._modPort);
                setting.setSetting(prefixL + ".fixedPort", layer._fixedPort);
                setting.setSetting(prefixL + ".modChannel", layer._modChannel);
                setting.setSetting(prefixL + ".fixedChannel", layer._fixedChannel);
                setting.setSetting(prefixL + ".modBank", layer._modBank);
                setting.setSetting(prefixL + ".fixedBankMSB", layer._fixedBankMSB);
                setting.setSetting(prefixL + ".fixedBankLSB", layer._fixedBankLSB);
                setting.setSetting(prefixL + ".modProgram", layer._modProgram);
                setting.setSetting(prefixL + ".fixedProgram", layer._fixedProgram);
                setting.setSetting(prefixL + ".modPan", layer._modPan);
                setting.setSetting(prefixL + ".fixedPan", layer._fixedPan);
                setting.setSetting(prefixL + ".adjustExpression", layer._adjustExpression);
                setting.setSetting(prefixL + ".adjustTranspose", layer._adjustTranspose);
                setting.setSetting(prefixL + ".adjustVelocity", layer._adjustVelocity);
                setting.setSetting(prefixL + ".sendKeyLowest", layer.getAcceptKeyLowest());
                setting.setSetting(prefixL + ".sendKeyHighest", layer.getAcceptKeyHighest());
                setting.setSetting(prefixL + ".sendVelocityLowest", layer.getAcceptVelocityLowest());
                setting.setSetting(prefixL + ".sendVelocityHighest", layer.getAcceptVelocityHighest());
            }
        }
        return setting.writeINIFile();
    }

    @Override
    public boolean readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("SoundLayer");
            MXJsonParser.setAutosave(this);
        }
        MXJsonValue value = new MXJsonParser(custom).parseFile();
        if (value == null) {
            return false;
        }
        MXJsonValue.HelperForStructure root = value.new HelperForStructure();

        ArrayList<MX40Group> newGroupList = new ArrayList();
        this._groupList = newGroupList;
        MXJsonValue.HelperForArray readingGroups = root.getFollowingArray("Group");
        if (readingGroups != null) {
            for (int i = 0; i < readingGroups.count(); ++i) {
                MXJsonValue.HelperForStructure node = readingGroups.getFollowingStructure(i);
                MX40Group group = new MX40Group(this);
                group._title = node.getFollowingText("title", "");

                group._isWatchPort = node.getFollowingBool("isWatchPort", false);
                group._watchingPort = node.getFollowingInt("watchingPort", 0);
                group._isWatchChannel = node.getFollowingBool("isWatchChannel", false);
                group._watchingChannel = node.getFollowingInt("watchingCahnnel", 0);
                group._isRotate = node.getFollowingBool("rotateLayer", false);
                group._rotatePoly = node.getFollowingInt("rotatePoly", 16);

                group._isWatchBank = node.getFollowingBool("isWatchBank", false);
                group._watchingBankMSB = node.getFollowingInt("watchingBankMSB", 0);
                group._watchingBankLSB = node.getFollowingInt("watchingBankLSB", 0);

                group._isWatchProgram = node.getFollowingBool("isWatchProgram", false);
                group._watchingProgram = node.getFollowingInt("watchingProgram", 0);
                newGroupList.add(group);

                MXJsonValue.HelperForArray layerNode = node.getFollowingArray("Layer");
                if (layerNode != null) {
                    for (int j = 0; j < layerNode.count(); ++j) {
                        MXJsonValue.HelperForStructure node2 = layerNode.getFollowingStructure(j);
                        MX40Layer layer = new MX40Layer(this, group);
                        layer._title = node2.getFollowingText("title", "");
                        layer._disabled = node2.getFollowingBool("disabled", false);
                        layer._modPort = node2.getFollowingInt("modPort", MX40Layer.MOD_ASFROM);
                        layer._fixedPort = node2.getFollowingInt("fixedPort", 0);

                        layer._modChannel = node2.getFollowingInt("modChannel", MX40Layer.MOD_ASFROM);
                        layer._fixedChannel = node2.getFollowingInt("fixedChannel", 0);

                        layer._modBank = node2.getFollowingInt("modBank", MX40Layer.MOD_ASFROM);
                        layer._fixedBankMSB = node2.getFollowingInt("fixedBankMSB", 0);
                        layer._fixedBankLSB = node2.getFollowingInt("fixedBankLSB", 0);

                        layer._modProgram = node2.getFollowingInt("modProgram", MX40Layer.MOD_ASFROM);
                        layer._fixedProgram = node2.getFollowingInt("fixedProgram", 0);

                        layer._modPan = node2.getFollowingInt("modPan", MX40Layer.MOD_ASFROM);
                        layer._fixedPan = node2.getFollowingInt("fixedPan", 64);

                        layer._adjustExpression = node2.getFollowingInt("adjustExpression", 100);
                        layer._adjustTranspose = node2.getFollowingInt("adjustTranspose", 0);
                        layer._adjustVelocity = node2.getFollowingInt("adjustVelocity", 0);
                        layer.setAcceptKeyLowest(node2.getFollowingInt("sendKeyLowest", 0));
                        layer.setAcceptKeyHighest(node2.getFollowingInt("sendKeyHighest", 127));
                        layer.setAcceptVelocityLowest(node2.getFollowingInt("sendVelocityLowest", 0));
                        layer.setAcceptVelocityHighest(node2.getFollowingInt("sendVelocityHighest", 127));

                        group._listLayer.add(layer);
                    }
                }
            }
        }
        resendProgramChange();
        _view.justRefreshViewListAndPanel();

        if (value == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean writeJsonFile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("SoundLayer");
        }
        MXJsonParser parser = new MXJsonParser(custom);
        MXJsonValue.HelperForStructure root = parser.getRoot().new HelperForStructure();
        MXJsonValue.HelperForArray baseGroup = root.addFollowingArray("Group");
        for (int i = 0; i < _groupList.size(); i++) {
            MX40Group seekGroup = _groupList.get(i);
            MXJsonValue.HelperForStructure settingGroup = baseGroup.addFollowingStructure();
            settingGroup.setFollowingText("title", seekGroup._title);
            settingGroup.setFollowingBool("isWatchPort", seekGroup._isWatchPort);
            settingGroup.setFollowingInt("watchingPort", seekGroup._watchingPort);
            settingGroup.setFollowingBool("isWatchChannel", seekGroup._isWatchChannel);
            settingGroup.setFollowingInt("watchingCahnnel", seekGroup._watchingChannel);
            settingGroup.setFollowingBool("isWatchBank", seekGroup._isWatchBank);
            settingGroup.setFollowingInt("watchingBankMSB", seekGroup._watchingBankMSB);
            settingGroup.setFollowingInt("watchingBankLSB", seekGroup._watchingBankLSB);
            settingGroup.setFollowingBool("isWatchProgram", seekGroup._isWatchProgram);
            settingGroup.setFollowingInt("watchingProgram", seekGroup._watchingProgram);
            settingGroup.setFollowingBool("rotateLayer", seekGroup._isRotate);
            settingGroup.setFollowingInt("rotatePoly", seekGroup._rotatePoly);

            MXJsonValue.HelperForArray baseLayer = settingGroup.addFollowingArray("Layer");

            for (int j = 0; j < seekGroup._listLayer.size(); ++j) {
                String prefixL = "Group[" + i + "].Layer[" + j + "]";
                MX40Layer seekLayer = seekGroup._listLayer.get(j);
                MXJsonValue.HelperForStructure settingLayer = baseLayer.addFollowingStructure();
                settingLayer.setFollowingText(prefixL + ".title", seekLayer._title);
                settingLayer.setFollowingBool(prefixL + ".disabled", seekLayer._disabled);
                settingLayer.setFollowingInt(prefixL + ".modPort", seekLayer._modPort);
                settingLayer.setFollowingInt(prefixL + ".fixedPort", seekLayer._fixedPort);
                settingLayer.setFollowingInt(prefixL + ".modChannel", seekLayer._modChannel);
                settingLayer.setFollowingInt(prefixL + ".fixedChannel", seekLayer._fixedChannel);
                settingLayer.setFollowingInt(prefixL + ".modBank", seekLayer._modBank);
                settingLayer.setFollowingInt(prefixL + ".fixedBankMSB", seekLayer._fixedBankMSB);
                settingLayer.setFollowingInt(prefixL + ".fixedBankLSB", seekLayer._fixedBankLSB);
                settingLayer.setFollowingInt(prefixL + ".modProgram", seekLayer._modProgram);
                settingLayer.setFollowingInt(prefixL + ".fixedProgram", seekLayer._fixedProgram);
                settingLayer.setFollowingInt(prefixL + ".modPan", seekLayer._modPan);
                settingLayer.setFollowingInt(prefixL + ".fixedPan", seekLayer._fixedPan);
                settingLayer.setFollowingInt(prefixL + ".adjustExpression", seekLayer._adjustExpression);
                settingLayer.setFollowingInt(prefixL + ".adjustTranspose", seekLayer._adjustTranspose);
                settingLayer.setFollowingInt(prefixL + ".adjustVelocity", seekLayer._adjustVelocity);
                settingLayer.setFollowingInt(prefixL + ".sendKeyLowest", seekLayer.getAcceptKeyLowest());
                settingLayer.setFollowingInt(prefixL + ".sendKeyHighest", seekLayer.getAcceptKeyHighest());
                settingLayer.setFollowingInt(prefixL + ".sendVelocityLowest", seekLayer.getAcceptVelocityLowest());
                settingLayer.setFollowingInt(prefixL + ".sendVelocityHighest", seekLayer.getAcceptVelocityHighest());
            }
        }

        return parser.writeFile();
    }

    @Override
    public void resetSetting() {
    }
}
