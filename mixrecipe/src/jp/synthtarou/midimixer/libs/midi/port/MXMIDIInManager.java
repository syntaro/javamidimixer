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
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.common.log.MXDebugPrint;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_Java;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_UWP;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_Empty;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMIDIInManager implements MXSettingTarget {
    private static final MXDebugPrint _debug = new MXDebugPrint(MXMIDIInManager.class);
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

            MXWrapList<MXMIDIIn> list = listAllInput();
            
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

    protected MXWrapList<MXMIDIIn> _listAllInput;
    protected MXWrapList<MXMIDIIn> _listUsingInput;

    public synchronized MXWrapList<MXMIDIIn> listAllInput() {
        if (_listAllInput != null) {
            return _listAllInput;
        }

        MXWrapList<MXMIDIIn> temp = new MXWrapList<MXMIDIIn>();

        MXMIDIIn sequencer = MXMIDIIn.INTERNAL_PLAYER;
        temp.addNameAndValue(sequencer.getName(), sequencer);

        MXDriver java = MXDriver_Java._instance;
        
        for (int i = 0; i < java.InputDevicesRoomSize(); i++) {
            MXMIDIIn device = new MXMIDIIn(java, i);
            try {
                if (device.getName().equals("Real Time Sequencer")) {
                    continue;
                }
                temp.addNameAndValue(device.getName(), device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        MXDriver uwp = MXDriver_UWP._instance;
        for (int i = 0; i < uwp.InputDevicesRoomSize(); i++) {
            MXMIDIIn device = new MXMIDIIn(uwp, i);
            try {
                System.out.println("UWP : "+ device.getName());
                if (device.getName().equals("Real Time Sequencer")) {
                    continue;
                }
                temp.addNameAndValue(device.getName(), device);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public synchronized MXWrapList<MXMIDIIn>listSelectedInput() {
        if (_listUsingInput != null) {
            return _listUsingInput;
        }
        MXWrapList<MXMIDIIn> newInput = new MXWrapList();
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
                System.out.println("closing input " + input);
                input.close();
            }
        }
    }
    
    @Override
    public void prepareSettingFields(MXSetting setting) {
        setting.register("device[].name");
        setting.register("device[].open");
        setting.register("device[].port");
        setting.register("device[].toMaster");
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
        MXDriver_Empty dummy = MXDriver_Empty.getInstance();
        
        for (int seek = 0; seek < 1000; ++ seek) {
            String deviceName = setting.getSetting("device[" + seek + "].name");
            String deviceOpen = setting.getSetting("device[" + seek + "].open");
            String deviceMaster = setting.getSetting("device[" + seek + "].toMaster");
            String devicePort = setting.getSetting("device[" + seek + "].port");
            if (deviceName == null || deviceName.length() == 0) {
                break;
            }
            if (devicePort == null || devicePort.length() == 0) {
                //For upgrade setting
                devicePort = String.valueOf(seek);
            }
            
            try {
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

                MXWrapList<MXMIDIIn> detected = listAllInput();
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
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        clearMIDIInCache();
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        MXWrapList<MXMIDIIn> all = listAllInput();
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
                setting.setSetting("device[" + x + "].name", e.getName());
                setting.setSetting("device[" + x + "].open", e.isOpen() ? "1" : "0");
                setting.setSetting("device[" + x + "].port", assigned.toString());
                setting.setSetting("device[" + x + "].toMaster", e.getMasterList());
                x ++;
            }
        }
    }
}
