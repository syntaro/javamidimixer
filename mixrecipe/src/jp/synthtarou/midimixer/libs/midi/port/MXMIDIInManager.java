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
package jp.synthtarou.midimixer.libs.midi.port;

import java.util.ArrayList;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.namedvalue.MNamedValueList;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_Java;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_UWP;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_NotFound;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMIDIInManager implements MXSettingTarget {
    private static final MXMIDIInManager _instance = new MXMIDIInManager();
    
    public static MXMIDIInManager getManager() {
        return _instance;
    }

    public void reloadDeviceList() {
        //TODO Java not support?
    }

    protected MXMIDIInManager() {
    }
    
    private MXSetting _setting;
    
    public int getFreeAssignPort() {
        int found = -1;
        for (int i = 0; i < MXAppConfig.TOTAL_PORT_COUNT; ++ i) {
            boolean entered = false;
            for (MXMIDIIn in : listAllInput().valueList()) {
                if (in.isPortAssigned(i)) {
                    entered = true;
                    break;
                }
            }
            if (!entered) {
                found = i;
                break;
            }
        }
        return found;
    }
    
    public void initWithSetting() {
        if (_setting == null) {
            _setting = new MXSetting("MIDIInput");
            _setting.setTarget(this);

            MNamedValueList<MXMIDIIn> list = listAllInput();
            
            _setting.readSettingFile();

            boolean assigned = false;
            for (int i = 0; i < list.getSize(); ++ i) {
                MXMIDIIn in = list.valueOfIndex(i);
                if (in.getPortAssignCount() > 0) {
                    assigned = true;
                }
            }
            
            if (!assigned) {
                MXMIDIIn.INTERNAL_PLAYER.setPortAssigned(0, true);
            }
        }
    }

    protected MNamedValueList<MXMIDIIn> _listAllInput;
    protected MNamedValueList<MXMIDIIn> _listUsingInput;

    public synchronized MNamedValueList<MXMIDIIn> listAllInput() {
        if (_listAllInput != null) {
            return _listAllInput;
        }

        MNamedValueList<MXMIDIIn> temp = new MNamedValueList<MXMIDIIn>();

        MXMIDIIn tester = MXMIDIIn.INTERNAL_TESTER;
        temp.addNameAndValue(tester.getName(), tester);
        if (tester.getPortAssignCount() == 0) {
            tester.setPortAssigned(0, true);
        }
        
        MXMIDIIn sequencer = MXMIDIIn.INTERNAL_PLAYER;
        temp.addNameAndValue(sequencer.getName(), sequencer);
        if (sequencer.getPortAssignCount() == 0) {
            sequencer.setPortAssigned(0, true);
        }

        MXDriver java = MXDriver_Java._instance;
        
        for (int i = 0; i < java.InputDevicesRoomSize(); i++) {
            MXMIDIIn device = new MXMIDIIn(java, i);
            if (device == null) {
                continue;
            }
            if (device.getName().equals("Real Time Sequencer")) {
                continue;
            }
            temp.addNameAndValue(device.getName(), device);
        }
        
        MXDriver uwp = MXDriver_UWP._instance;
        for (int i = 0; i < uwp.InputDevicesRoomSize(); i++) {
            MXMIDIIn device = new MXMIDIIn(uwp, i);
            if (device == null) {
                continue;
            }
            if (device.getName().equals("Real Time Sequencer")) {
                continue;
            }
            temp.addNameAndValue(device.getName(), device);
        }

        _listAllInput = temp;
        return _listAllInput;
    }

    synchronized void onClose(MXMIDIIn input) {
        clearMIDIInCache();
    }
    
    protected synchronized void clearMIDIInCache() {
        _listUsingInput = null;
        //_cache = null;        
    }

    public synchronized MNamedValueList<MXMIDIIn>listSelectedInput() {
        if (_listUsingInput != null) {
            return _listUsingInput;
        }
        MNamedValueList<MXMIDIIn> newInput = new MNamedValueList();
        for (MXMIDIIn midi : listAllInput().valueList()) {
            if (midi.getPortAssignCount() > 0) {
                newInput.addNameAndValue(midi.toString(), midi);
            }
        }
        _listUsingInput = newInput;
        return newInput;
    }

    public synchronized void closeAll() {
        for(MXMIDIIn input : listAllInput().valueList()) {
            if (input.isOpen()) {
                input.close();
            }
        }
    }

    @Override
    public MXSetting getSettings() {
        return _setting;
    }

    @Override
    public void prepareSettingFields() {
        _setting.register("device[].name");
        _setting.register("device[].open");
        _setting.register("device[].port");
        _setting.register("device[].toMaster");
    }

    @Override
    public void afterReadSettingFile() {
        MXDriver_NotFound dummy = MXDriver_NotFound.getInstance();
        
        for (int seek = 0; seek < 1000; ++ seek) {
            String deviceName = _setting.getSetting("device[" + seek + "].name");
            String deviceOpen = _setting.getSetting("device[" + seek + "].open");
            String deviceMaster = _setting.getSetting("device[" + seek + "].toMaster");
            String devicePort = _setting.getSetting("device[" + seek + "].port");
            if (deviceName == null || deviceName.length() == 0) {
                break;
            }
            if (devicePort == null || devicePort.length() == 0) {
                //For upgrade setting
                devicePort = String.valueOf(seek);
            }
            
            if (deviceName == null) {
                continue;
            }
            if (deviceOpen == null) {
                deviceOpen = "0";
            }

            if (deviceOpen.equals("1")) {
                if (devicePort == null) {
                    devicePort = String.valueOf(seek);
                }
            }

            MNamedValueList<MXMIDIIn> detected = listAllInput();
            MXMIDIIn in = detected.valueOfName(deviceName);
            if (in != null) {
                if (deviceOpen.equals("1")) {
                    in.openInput(5000);
                }
            }else {
                in  = new MXMIDIIn(dummy, dummy.InputAddDevice(deviceName));
                in.setPortAssigned(seek, true);
                detected.addNameAndValue(deviceName, in);
            }

            ArrayList<String> split = new ArrayList();
            MXUtil.split(devicePort, split, ',');
            for (String t1 : split) {
                try {
                    int x = Integer.parseInt(t1);
                    in.setPortAssigned(x, true);
                }catch(NumberFormatException e) {

                }
            }
            in.setMasterList(deviceMaster);
        }

        clearMIDIInCache();
    }

    @Override
    public void beforeWriteSettingFile() {
        MNamedValueList<MXMIDIIn> all = listAllInput();
        int x = 0;
        for (MXMIDIIn e : all.valueList()) {
            if (e.getPortAssignCount() <= 0) {
                continue;
            }
            StringBuffer assigned = new StringBuffer();
            for (int p = 0; p < MXAppConfig.TOTAL_PORT_COUNT; ++ p) {
                if (e.isPortAssigned(p)) {
                    if (assigned.length() > 0) {
                        assigned.append(",");
                    }
                    assigned.append(Integer.toString(p));
                }
            }
            if (assigned.length() > 0) {
                _setting.setSetting("device[" + x + "].name", e.getName());
                _setting.setSetting("device[" + x + "].open", e.isOpen() ? "1" : "0");
                _setting.setSetting("device[" + x + "].port", assigned.toString());
                _setting.setSetting("device[" + x + "].toMaster", e.getMasterList());
                x ++;
            }
        }
    }
}
