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

import jp.synthtarou.midimixer.libs.midi.port.MXVisitantRecorder;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXNoteOffWatcher;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingNode;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX40Process extends MXReceiver implements MXSettingTarget {
    MX40View _view;
    MXVisitantRecorder _inputInfo;
    MXVisitantRecorder _outputInfo;
    MXNoteOffWatcher _noteOff;
    MXSetting _setting;
    ArrayList<MX40Group> _groupList;

    public MX40Process() {
        _groupList = new ArrayList();
        _inputInfo = new MXVisitantRecorder();
        _outputInfo = new MXVisitantRecorder();
        _noteOff = new MXNoteOffWatcher();
        _view = new MX40View(this);
        _setting = new MXSetting("SoundLayer");
        _setting.setTarget(this);
    }
    
    public void exportSetting(File file) {
        MXSetting setting2 = new MXSetting(file, false);
        setting2.setTarget(this);
        setting2.writeSettingFile();
    }

    public void importSetting(File file) {
        MXSetting setting2 = new MXSetting(file, false);
        setting2.setTarget(this);
        setting2.readSettingFile();
    }
    
    public void readSettings() {
        _setting.readSettingFile();
        //_view.reload
    }
    
    public void sendToNext(MXMessage message) {
        _outputInfo.updateVisitant16WithMessage(message);
        if (_outputInfo.mergeVisitant16WithVisitant(message)) {
            
        }
        super.sendToNext(message);
    }

    @Override
    public void processMXMessage(MXMessage message) {
        _inputInfo.updateVisitant16WithMessage(message);
        _inputInfo.mergeVisitant16WithVisitant(message);
        MXTiming timing = message._timing;
        
        if (message.isBinaryMessage()) {
            sendToNext(message);
            return;
        }

        if (isUsingThisRecipe() == false) { 
            sendToNext(message); 
            return; 
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
            message._timing = timing;
            command = MXMidi.COMMAND_CH_PROGRAMCHANGE;
        }
        if (first == MXMidi.COMMAND2_CH_PROGRAM_DEC) {
            int x = message.getVisitant().getProgram() - 1;
            if (x < 0) {
                x = 0;
            }
            message = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_CH_PROGRAMCHANGE + message.getChannel(), x, 0);
            message._timing = timing;
            command = MXMidi.COMMAND_CH_PROGRAMCHANGE;
        }

        if (command == MXMidi.COMMAND_CH_NOTEOFF) {
            if (_noteOff.raiseHandler(port, message._timing, channel, message.getGate()._value)) {
                return;
            }
        }
        
        boolean dispatched = false;

        if (message.isMessageTypeChannel()) {
            for (int i = 0; i < _groupList.size(); ++ i) {
                final MX40Group col = _groupList.get(i);
                if (col.processByGroup(message)) {
                    if (command == MXMidi.COMMAND_CH_NOTEON) {
                        _noteOff.setHandler(message, message, new MXNoteOffWatcher.Handler() {
                            public void onNoteOffEvent(MXTiming timing, MXMessage target) {
                                MXMessage msg = MXMessageFactory.fromShortMessage(target.getPort(), 
                                        MXMidi.COMMAND_CH_NOTEOFF + target.getChannel(), 
                                        target.getGate()._value, 0);
                                msg._timing = timing;
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
                    public void onNoteOffEvent(MXTiming timing, MXMessage target) {
                        MXMessage msg = MXMessageFactory.fromShortMessage(target.getPort(), 
                                MXMidi.COMMAND_CH_NOTEOFF + target.getChannel(), 
                                target.getGate()._value, 0);
                        msg._timing = timing;
                        sendToNext(msg);
                    }
                });
            }
            if (message.isCommand(MXMidi.COMMAND_CH_PROGRAMCHANGE) && message.getGate()._value < 0) {
                return;
            }
            sendToNext(message);
        }
    }

    @Override
    public String getReceiverName() {
        return "Sound Layer";
    }

    @Override
    public JPanel getReceiverView() {
        return _view;
    }

    public synchronized void resendProgramChange() {
        for(int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++ port) {
            for(int channel = 0; channel < 16; ++ channel) {
                MXVisitant info = _inputInfo.getVisitant(port, channel);
                if (info.isHavingProgram()) {
                    MXMessage programChange = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_PROGRAMCHANGE + channel, info.getProgram(), 0);
                    processMXMessage(programChange);
                }else {
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
    public void prepareSettingFields(MXSetting setting) {
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
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
        ArrayList<MX40Group> newGroupList = new ArrayList();
        this._groupList = newGroupList;
        List<MXSettingNode> readingGroups = setting.findByPath("Group");   
        if (readingGroups.size() > 0) {
            readingGroups = readingGroups.get(0).findNumbers();
        }
        for (MXSettingNode node : readingGroups) {
            MX40Group group = new MX40Group(this);
            group._title = node.findNode("title")._value;

            group._isWatchPort = node.getSettingAsBoolean("isWatchPort", false);
            group._watchingPort = node.getSettingAsInt("watchingPort", 0);
            group._isWatchChannel = node.getSettingAsBoolean("isWatchChannel", false);
            group._watchingChannel = node.getSettingAsInt("watchingCahnnel", 0);
            group._isRotate = node.getSettingAsBoolean("rotateLayer", false);
            group._rotatePoly = node.getSettingAsInt("rotatePoly", 16);

            group._isWatchBank = node.getSettingAsBoolean("isWatchBank", false);
            group._watchingBankMSB = node.getSettingAsInt("watchingBankMSB",0 );
            group._watchingBankLSB = node.getSettingAsInt("watchingBankLSB",0 );

            group._isWatchProgram = node.getSettingAsBoolean("isWatchProgram", false);
            group._watchingProgram = node.getSettingAsInt("watchingProgram", 0);
            newGroupList.add(group);

            MXSettingNode layerNode = node.findNode("Layer");
            if (layerNode != null) {
                List<MXSettingNode> numbers = layerNode.findNumbers();
                for (MXSettingNode node2 : numbers) {
                    MX40Layer layer = new MX40Layer(this, group);
                    layer._title = node2.getSetting("title");
                    layer._disabled  = node2.getSettingAsBoolean("disabled", false);
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
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        for (int i = 0; i < _groupList.size(); i ++){
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

            for (int j = 0; j < group._listLayer.size(); ++ j) {
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
                setting.setSetting(prefixL + ".fixedProgram",layer._fixedProgram);
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
//        resendProgramChange();
    }
}
