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

import java.util.ArrayList;
import javax.swing.JPanel;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX30Process extends MXReceiver implements MXSettingTarget {

    private MX30View _rootView;
    private MX32MixerProcess[] _pageProcess;
    MXSetting _setting;
    boolean _underConstruction;
    
    public MX30Process() {
        _underConstruction = true;
        _setting = new MXSetting("MixingGlobal");
        _setting.setTarget(this);
        prepareActiveSlider();
        _rootView = new MX30View(this);
        _pageProcess = new MX32MixerProcess[MXAppConfig.TOTAL_PORT_COUNT];
        for (int i = 0; i < MXAppConfig.TOTAL_PORT_COUNT; ++ i) {
            _pageProcess[i] = new MX32MixerProcess(this, i);
        }
        _underConstruction = false;
    }

    @Override    
    public void setNextReceiver(MXReceiver next) {
        super.setNextReceiver(next);
        for (int i = 0; i < _pageProcess.length; ++ i) {
            _pageProcess[i].setNextReceiver(next);
        }
    }
    
    public void readSettings() {
        _setting.readSettingFile();
        for (int port = 0; port < MXAppConfig.TOTAL_PORT_COUNT; ++ port) {
            _pageProcess[port].readSettings();
            MX32MixerProcess mixer = _pageProcess[port];
            _rootView.addPage(port, mixer);
        }
        globalContollerHidden();
    }
    
    @Override
    public void prepareSettingFields(MXSetting setting) {
        setting.register("ActiveCircle");
        setting.register("ActiveLine");
        setting.register("ActivePad");
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
        String circle = setting.getSetting("ActiveCircle");
        if  (circle == null) {
            circle = "1, 2, 3, 4";
        }
        ArrayList<String> circleList = new ArrayList();
        MXUtil.split(circle, circleList, ',');
        _visibleKnob = new boolean[4];
        for (String str : circleList) {
            str = str.trim();
            try {
                _visibleKnob[Integer.parseInt(str)-1] = true;
            }catch(Exception e) {

            }
        }

        String line = setting.getSetting("ActiveLine");
        int lineNum = 17;
        try {
            if (line != null) {
                lineNum = Integer.parseInt(line.trim());
            }
        }catch(Exception e) {
        }
        this._visibleLineCount = lineNum;
        String pad = setting.getSetting("ActivePad");
        if (pad == null) {
            pad = "1, 2, 3";
        }
        
        ArrayList<String> padList = new ArrayList();
        MXUtil.split(pad, padList, ',');
        _visiblePad = new boolean[4];
        for (String str : padList) {
            str = str.trim();
            try {
                _visiblePad[Integer.parseInt(str)-1] = true;
            }catch(Exception e) {

            }
        }
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        setting.clearValue();
        StringBuffer circle = new StringBuffer();
        for (int i = 0; i < _visibleKnob.length; ++ i) {
            if (_visibleKnob[i]) {
                if (circle.length() != 0) circle.append(",");
                circle.append(i+1);
            }
        }
        StringBuffer pad = new StringBuffer();
        for (int i = 0; i < _visiblePad.length; ++ i) {
            if (_visiblePad[i]) {
                if (pad.length() != 0) pad.append(",");
                pad.append(i+1);
            }
        }
        setting.setSetting("ActiveCircle", circle.toString());
        setting.setSetting("ActiveLine", _visibleLineCount);
        setting.setSetting("ActivePad", pad.toString());
    }
       
    public MX32MixerProcess getPage(int i) {
        return _pageProcess[i];
    }

    @Override
    public void processMXMessage(MXMessage message) {
        if (isUsingThisRecipe() == false) { 
            sendToNext(message);
            return; 
        }

        int port = message.getPort();
        _pageProcess[port].processMXMessage(message);
    }
    
    @Override
    public String getReceiverName() {
        return "Surface17";
    }

    @Override
    public JPanel getReceiverView() {
        return _rootView;
    }
    
    protected void prepareActiveSlider() {
        if (_visibleLineCount == 0) {
            _visibleLineCount = 17;
            _visibleKnob = new boolean[4];
            for (int i = 0; i < _visibleKnob.length; ++ i) {
                _visibleKnob[i] = true;
            }
            _visiblePad = new boolean[3];
            for (int i = 0; i < _visiblePad.length; ++ i) {
                _visiblePad[i] = true;
            }
        }
    }
    
    private boolean[] _visibleKnob;
    private boolean[] _visiblePad;
    private int _visibleLineCount;

    public int getVisibleLineCount() {
        return _visibleLineCount;
    }

    public void setVisibleLineCount(int lines) {
        this._visibleLineCount = lines;
    }

    public boolean isKnobVisible(int r) {
        return _visibleKnob[r];
    }

    public void setKnobVisible(int r, boolean visible) {
        _visibleKnob[r] = visible;
    }

    public boolean isPadVisible(int r) {
        return _visiblePad[r];
    }

    public void setPadVisible(int r, boolean visible) {
        _visiblePad[r] = visible;
    }
    
    public void globalContollerHidden() {
        for (int t = 0; t < _pageProcess.length; ++ t) {
            _pageProcess[t]._view.globalControllerHidden();
        }
    }
}
