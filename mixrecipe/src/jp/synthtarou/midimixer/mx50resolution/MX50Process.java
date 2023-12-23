/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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
package jp.synthtarou.midimixer.mx50resolution;

import java.util.ArrayList;
import javax.swing.JPanel;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX50Process extends MXReceiver implements MXSettingTarget {
    MXSetting _setting;
    MX50View _view;
    ArrayList<MXResolution> _listResolution;

    public MX50Process() {
        _listResolution = new ArrayList();
        
        _view = new MX50View(this);
        _setting = new MXSetting("ResolutionDown");
        _setting.setTarget(this);
    }
    
    @Override
    public String getReceiverName() {
        return "ResolutionDown";
    }

    @Override
    public JPanel getReceiverView() {
        return _view;
    }

    public void readSettings() {
        _setting.readSettingFile();
    }

    @Override
    public void processMXMessage(MXMessage message) {
        for (MXResolution reso : _listResolution) {
            if (reso.hasSameTemplateChGate(message)) {
                message = reso.updateWithNewResolution(message);
                sendToNext(message);
                return;
            }
        }
        sendToNext(message);
    }

    @Override
    public void prepareSettingFields(MXSetting setting) {
        setting.register("Resolution[].Command");
        setting.register("Resolution[].Port");
        setting.register("Resolution[].Channel");
        setting.register("Resolution[].Gate");
        setting.register("Resolution[].Min");
        setting.register("Resolution[].Max");
        setting.register("Resolution[].ResolutionMin");
        setting.register("Resolution[].ResolutionMax");
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
        int x = 1;
        _listResolution.clear();

        while(true) {
            String prefix = "Resolution[" + x + "].";
            String command = setting.getSetting(prefix + "Command");
            if (command == null || command.isBlank()) {
                break;
            }
            int port = setting.getSettingAsInt(prefix + "Port", 0);
            int channel = setting.getSettingAsInt(prefix + "Channel", 0);
            int gate = setting.getSettingAsInt(prefix + "Gate", 0);
            int min = setting.getSettingAsInt(prefix + "Min", -1);
            int max = setting.getSettingAsInt(prefix + "Max", -1);
            int resolutionMin = setting.getSettingAsInt(prefix + "ResolutionMin", -1);
            int resolutionMax = setting.getSettingAsInt(prefix + "ResolutionMax", -1);
            
            MXResolution reso = new MXResolution();
            try {
                MXTemplate template = new MXTemplate(command);
                MXRangedValue gateObj = MXRangedValue.new7bit(gate);
                MXRangedValue value = new MXRangedValue(0, min, max);

                MXMessage message = MXMessageFactory.fromTemplate(port, template, channel, gateObj, value);
                reso.setBaseMessage(message);
                reso._lastSent = -1;
                reso._newResolution = new MXRangedValue(0, resolutionMin, resolutionMax);
                _listResolution.add(reso);

            }catch(Throwable e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        setting.clearValue();
        int x = 1;
        for (MXResolution reso : _listResolution) {
            String prefix = "Resolution[" + x + "].";
            x ++;
            setting.setSetting(prefix + "Command", reso._base.getTemplateAsText());
            setting.setSetting(prefix + "Port", reso._base.getPort());
            setting.setSetting(prefix + "Channel", reso._base.getChannel());
            setting.setSetting(prefix + "Gate", reso._base.getGate()._var);
            setting.setSetting(prefix + "Min",  reso._base.getValue()._min);
            setting.setSetting(prefix + "Max",  reso._base.getValue()._max);
            setting.setSetting(prefix + "ResolutionMin", reso._newResolution._min);
            setting.setSetting(prefix + "ResolutionMax", reso._newResolution._max);
        }
    }
}
