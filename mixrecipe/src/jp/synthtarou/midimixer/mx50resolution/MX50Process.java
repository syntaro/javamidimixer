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
import jp.synthtarou.midimixer.libs.midi.MXMessageBag;
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
    ArrayList<MXResolutionView> _listResolutionView;

    public MX50Process() {
        _listResolution = new ArrayList();
        _listResolutionView = new ArrayList();
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
        _view.reloadList();
    }

    public int indexOfResolution(MXResolution reso) {
        return  _listResolution.indexOf(reso);
    }

    @Override
    public void processMXMessage(MXMessage message) {
        MXMessageBag result = new MXMessageBag();
        boolean proc = false;
        for (MXResolution reso : _listResolution) {
            if (reso.controlByMessage(message, result)) {
                proc = true;
            }
        }
        if (proc) {
            while (true) {
                MXMessage seek = result.popTranslated();
                if (seek == null) {
                    break;
                }
                sendToNext(seek);
            }
        } else {
            sendToNext(message);
        }
    }

    @Override
    public void prepareSettingFields(MXSetting setting) {
        setting.register("Resolution[].Command");
        setting.register("Resolution[].Port");
        setting.register("Resolution[].Channel");
        setting.register("Resolution[].Gate");
        setting.register("Resolution[].Min");
        setting.register("Resolution[].Max");
        setting.register("Resolution[].Resolution");
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
        int x = 1;
        _listResolution.clear();
        _listResolutionView.clear();

        while (true) {
            String prefix = "Resolution[" + x + "].";
            x++;
            String command = setting.getSetting(prefix + "Command");
            int port = setting.getSettingAsInt(prefix + "Port", -1);
            if (port < 0) {
                break;
            }
            int channel = setting.getSettingAsInt(prefix + "Channel", 0);
            int gate = setting.getSettingAsInt(prefix + "Gate", 0);
            int min = setting.getSettingAsInt(prefix + "Min", -1);
            int max = setting.getSettingAsInt(prefix + "Max", -1);
            int resolution = setting.getSettingAsInt(prefix + "Resolution", -1);

            try {
                MXTemplate template = new MXTemplate(command);
                MXRangedValue gateObj = MXRangedValue.new7bit(gate);
                MXRangedValue value = new MXRangedValue(0, min, max);
                
                MXResolution reso = new MXResolution(this);
                reso._channel = channel;
                reso._gate = gate;
                reso._command = template;
                reso._lastSent = -1;
                reso._port = port;
                reso._resolution = resolution;
                _listResolution.add(reso);
                _listResolutionView.add(new MXResolutionView(reso));
                
            } catch (Throwable e) {
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
            x++;
            setting.setSetting(prefix + "Command", reso._command != null ? reso._command.toDText(): "-");
            setting.setSetting(prefix + "Port", reso._port);
            setting.setSetting(prefix + "Channel", reso._channel);
            setting.setSetting(prefix + "Gate", reso._gate);
            setting.setSetting(prefix + "Resolution", reso._resolution);
        }
    }

    public MXResolution createNewResolution() {
        MXResolution reso = new MXResolution(this);
        MXResolutionView view = new MXResolutionView(reso);
        _listResolution.add(reso);
        _listResolutionView.add(view);
        return reso;
    }

    public void removeResolution(MXResolution reso) {
        int index = _listResolution.indexOf(reso);
        if (index >= 0) {
            _listResolution.remove(index);
            _listResolutionView.remove(index);
        }
    }
}
