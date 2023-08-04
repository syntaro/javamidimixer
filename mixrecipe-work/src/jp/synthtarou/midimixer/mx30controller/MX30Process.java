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

import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.MXStatic;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX30Process extends MXReceiver implements MXSettingTarget {

    private MX30View _rootView;
    private MX32MixerProcess[] _pageProcess;
    boolean _editingControl;
    MXSetting _setting;

    public MX30Process() {
        _setting = new MXSetting("MixingGlobal");
        _setting.setTarget(this);
        prepareActiveSlider();
        _rootView = new MX30View(this);
        _pageProcess = new MX32MixerProcess[MXStatic.TOTAL_PORT_COUNT];
        for (int i = 0; i < MXStatic.TOTAL_PORT_COUNT; ++ i) {
            _pageProcess[i] = new MX32MixerProcess(this, i);
        }
    }

    public void readSettings() {
        _setting.readSettingFile();
        for (int port = 0; port < MXStatic.TOTAL_PORT_COUNT; ++ port) {
            _pageProcess[port].readSettings();
            MX32MixerProcess proc = _pageProcess[port];
            _rootView.addPage(port, proc);
            proc.setNextReceiver(new MXReceiver() {
                public String getReceiverName() {
                    return null;
                }
                public JComponent getReceiverView() {
                    return null;
                }
                protected void processMXMessageImpl(MXMessage message) {
                    MX30Process.this.sendToNext(message);
                }
           });
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
        _activeKnob = new boolean[4];
        for (String str : circleList) {
            str = str.trim();
            try {
                _activeKnob[Integer.parseInt(str)-1] = true;
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
        this._activeLines = lineNum;
        String pad = setting.getSetting("ActivePad");
        if (pad == null) {
            pad = "1, 2, 3";
        }
        
        ArrayList<String> padList = new ArrayList();
        MXUtil.split(pad, padList, ',');
        _activePad = new boolean[4];
        for (String str : padList) {
            str = str.trim();
            try {
                _activePad[Integer.parseInt(str)-1] = true;
            }catch(Exception e) {

            }
        }
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        setting.clearValue();
        StringBuffer circle = new StringBuffer();
        for (int i = 0; i < _activeKnob.length; ++ i) {
            if (_activeKnob[i]) {
                if (circle.length() != 0) circle.append(",");
                circle.append(i+1);
            }
        }
        StringBuffer pad = new StringBuffer();
        for (int i = 0; i < _activePad.length; ++ i) {
            if (_activePad[i]) {
                if (pad.length() != 0) pad.append(",");
                pad.append(i+1);
            }
        }
        setting.setSetting("ActiveCircle", circle.toString());
        setting.setSetting("ActiveLine", _activeLines);
        setting.setSetting("ActivePad", pad.toString());
    }
       
    public MX32MixerProcess getPage(int i) {
        return _pageProcess[i];
    }

    @Override
    protected void processMXMessageImpl(MXMessage message) {
        if (isUsingThisRecipe() == false) { 
            sendToNext(message); return; 
        }

        int port = message.getPort();
        _pageProcess[port].processMXMessage(message);
        //if (message.isDataentry() && message.getVisitant().getDataentryValue14() == 0 && message._trace == null) { message._trace = new Throwable(); }
    }

    @Override
    public String getReceiverName() {
        return "Mixer (MX)";
    }

    @Override
    public JComponent getReceiverView() {
        return _rootView;
    }
    
    int focus_port, focus_type = -1, focus_row, focus_column;
    
    Color colorBack = null;
    Color colorEdit =  null;
    Color colorFocus = null;
    
    public boolean isFocusControl(MGStatus status) {
        if (status.getPort() == focus_port) {
            if (status.getUiType() == focus_type) {
                if (status.getRow() == focus_row && status.getColumn() == focus_column) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean isFocusEditing(MGStatus status) {
        if (isFocusControl(status) == false) {
            return false;
        }
        return _editingControl;
    }
    
    public void showTextForFocus(int type, int port, int row,int column)
    {
        String value = "(NONE)";
        
        try {
            if (colorBack == null) {
                colorBack = new JLabel().getBackground();
                colorEdit = Color.orange;
                colorFocus = Color.cyan;
            }

            if (focus_type > 0) {
                Color c = MXStatic.sliderColor(focus_column);

                MX32MixerData focus_data = getPage(focus_port)._data;
                switch(focus_type) {
                    case MGStatus.TYPE_SLIDER:
                        MGSlider slider = focus_data.getSlider(focus_row, focus_column);
                        MXUtil.backgroundRecursive(slider, c);
                        break;
                    case MGStatus.TYPE_CIRCLE:
                        MGCircle circle  = focus_data.getCircle(focus_row, focus_column);
                        MXUtil.backgroundRecursive(circle, c);
                        break;
                    case MGStatus.TYPE_DRUMPAD:
                        MGPad drum = focus_data.getDrumPad(focus_row, focus_column);
                        MXUtil.backgroundRecursive(drum, c);
                        break;
                }
            }
            
            MX32MixerData focus_data = getPage(port)._data;

            switch(type) {
                case MGStatus.TYPE_SLIDER:
                    MGSlider slider = focus_data.getSlider(row, column);
                    value = focus_data.getSliderStatus(row, column).toString();
                    if (_editingControl) {
                        MXUtil.backgroundRecursive(slider, colorEdit);
                    }else {
                        MXUtil.backgroundRecursive(slider, colorFocus);
                    }
                    break;
                case MGStatus.TYPE_CIRCLE:
                    MGCircle circle  = focus_data.getCircle(row, column);
                    value = focus_data.getCircleStatus(row, column).toString();
                    if (_editingControl) {
                        MXUtil.backgroundRecursive(circle, colorEdit);
                    }else {
                        MXUtil.backgroundRecursive(circle, colorFocus);
                    }
                    break;
                case MGStatus.TYPE_DRUMPAD:
                    MGPad drum = focus_data.getDrumPad(row, column);
                    value = focus_data.getDrumPadStatus(row, column).toString();
                    if (_editingControl) {
                        MXUtil.backgroundRecursive(drum, colorEdit);
                    }else {
                        MXUtil.backgroundRecursive(drum, colorFocus);
                    }
                    break;
                default:
                    value = "Unknown";
                    break;
            }
            focus_type = type;
            focus_port = port;
            focus_row = row;
            focus_column = column;
        }catch(Throwable e) {
            value = "ERR";
            e.printStackTrace();
        }
        
        _pageProcess[port]._view.setFocusString(value);
    }

    public void goNextFocus(final int port, final int keyCode) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(new Runnable() { public void run() { goNextFocus(port, keyCode); } });
            return;
        }

        int type = focus_type;
        int row = focus_row;
        int column = focus_column;
        
        if (type < 0) {
            type = 0;
            row = 0;
            column = 0;
        }else {
            switch(keyCode) {
                case 38: //UP
                    if (row > 0) {
                        row --;
                        break;
                    }
                    switch(-- type) {
                        case 0:
                            type = 1;
                            break;
                        case MGStatus.TYPE_CIRCLE:
                            row = 3;
                            break;
                        case MGStatus.TYPE_SLIDER:
                            row = 0;
                            break;
                        case MGStatus.TYPE_DRUMPAD:
                            row = 1;
                            break;
                    }
                    break;

                case 40: //DOWN
                    row ++;
                    switch(type) {
                        case MGStatus.TYPE_CIRCLE:
                            if (row >= 4) {
                                row = 0;
                                type = MGStatus.TYPE_SLIDER;
                            }
                            break;
                        case MGStatus.TYPE_SLIDER:
                            if (row >= 1) {
                                row = 0;
                                type = MGStatus.TYPE_DRUMPAD;
                            }
                            break;
                        case MGStatus.TYPE_DRUMPAD:
                            if (row >= 2) {
                                row --;
                            }
                            break;
                    }
                    break;

                case 37: //LEFT
                    column --;
                    if (column < 0) {
                        column = 0;
                    }
                    break;

                case 39: //RIGHT
                    column ++;
                    if (column >= MXStatic.SLIDER_COLUMN_COUNT) {
                        column --;
                    }
                    break;

                case ' ':
                case '\b':
                case '\n':
                    MX32MixerData focus_data = getPage(port)._data;

                    switch(type) {
                        case MGStatus.TYPE_SLIDER:
                            MGSlider slider = focus_data.getSlider(focus_row, focus_column);
                            switch(keyCode) {
                                case ' ': slider.increment(); break;
                                case '\b': slider.decriment(); break;
                                case '\n': slider.editContoller(); break;
                            }
                            break;
                        case MGStatus.TYPE_CIRCLE:
                            MGCircle circle = focus_data.getCircle(focus_row, focus_column);
                            switch(keyCode) {
                                case ' ': circle.increment(); break;
                                case '\b': circle.decriment(); break;
                                case '\n': circle.editContoller(); break;
                            }
                            break;
                        case MGStatus.TYPE_DRUMPAD:
                            MGPad drum = focus_data.getDrumPad(focus_row, focus_column);
                            switch(keyCode) {
                                case ' ': drum.increment(null); break;
                                case '\b': drum.decriment(null); break;
                                case '\n': drum.editContoller(); break;
                            }
                            break;
                        default:
                            break;
                    }                
                    break;
            }
        }
        showTextForFocus(type,  port, row,  column);
    }

    public void enterEditMode(boolean flag) {
        if (flag) {
            _editingControl = true;
            _rootView.lockAnothereTabs(true);
            MXMain.getMain().getMainWindow().setTitle(MXStatic.MX_EDITING);
        }else {
            _editingControl = false;
            _rootView.lockAnothereTabs(false);
            MXMain.getMain().getMainWindow().setTitle(MXStatic.MX_APPNAME_WITH_VERSION);
        }
    }

    protected void prepareActiveSlider() {
        if (_activeLines == 0) {
            _activeLines = 17;
            _activeKnob = new boolean[4];
            for (int i = 0; i < _activeKnob.length; ++ i) {
                _activeKnob[i] = true;
            }
            _activePad = new boolean[3];
            for (int i = 0; i < _activePad.length; ++ i) {
                _activePad[i] = true;
            }
        }
    }
    
    private boolean[] _activeKnob;
    private boolean[] _activePad;
    private int _activeLines;

    public int getActiveLines() {
        return _activeLines;
    }

    public void setActiveLines(int _activeLines) {
        this._activeLines = _activeLines;
    }

    public boolean isKnobActive(int r) {
        return _activeKnob[r];
    }

    public void setKnobActive(int r, boolean active) {
        _activeKnob[r] = active;
    }

    public boolean isPadActive(int r) {
        return _activePad[r];
    }

    public void setPadActive(int r, boolean active) {
        _activePad[r] = active;
    }
    
    public void globalContollerHidden() {
        for (int t = 0; t < _pageProcess.length; ++ t) {
            _pageProcess[t]._view.globalControllerHidden();
        }
    }
}
