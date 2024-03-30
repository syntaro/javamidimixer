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
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.json.MXJsonValue;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonFile;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX30Process extends MXReceiver<MX30View> implements MXINIFileSupport, MXJsonSupport  {

    private MX30View _rootView;
    MX32MixerProcess[] _pageProcess;
    boolean _underConstruction;
    
    public MX30Process() {
        _underConstruction = true;
        prepareActiveSlider();
        _rootView = new MX30View(this);
        _pageProcess = new MX32MixerProcess[MXConfiguration.TOTAL_PORT_COUNT];
        for (int i = 0; i < MXConfiguration.TOTAL_PORT_COUNT; ++ i) {
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
    
    @Override
    public MXINIFile prepareINIFile(File custom) {
        if (custom == null) {
            custom = MXINIFile.pathOf("MixingGlobal");
        }
        MXINIFile setting = new MXINIFile(custom, this);
        setting.register("ActiveCircle");
        setting.register("ActiveLine");
        setting.register("ActivePad");
        return setting;
    }

    @Override
    public void readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        setting.readINIFile();
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
        if (custom == null) {            
            for (int port = 0; port < MXConfiguration.TOTAL_PORT_COUNT; ++ port) {
                _pageProcess[port].readINIFile(null);
                MX32MixerProcess mixer = _pageProcess[port];
                _rootView.addPage(port, mixer);
            }
        }
        globalContollerHidden();
    }

    @Override
    public void writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
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
        setting.writeINIFile();
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
    public MX30View getReceiverView() {
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

    @Override
    public void readJSonfile(File custom) {
        MXJsonFile file = new MXJsonFile(custom);
        MXJsonValue value = file.readJsonFile();
        if (value == null) {
            value = new MXJsonValue(null);
        }
        //TODO
    }

    @Override
    public void writeJsonFile(File custom) {
        MXJsonValue value = new MXJsonValue(null);
        
        MXJsonFile file = new MXJsonFile(custom);
        file.writeJsonFile(value);
    }
}
