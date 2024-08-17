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

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_Java;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_UWP;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_NotFound;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.json.MXJsonValue;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMIDIInManager implements MXINIFileSupport, MXJsonSupport {

    private static final MXMIDIInManager _instance = new MXMIDIInManager();

    public static MXMIDIInManager getManager() {
        return _instance;
    }

    public void reloadDeviceList() {
        //TODO Java not support?
    }

    protected MXMIDIInManager() {
    }

    public int getFreeAssignPort() {
        int found = -1;
        for (int i = 0; i < MXConfiguration.TOTAL_PORT_COUNT; ++i) {
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

    protected MXNamedObjectList<MXMIDIIn> _listAllInput;
    protected MXNamedObjectList<MXMIDIIn> _listUsingInput;

    public synchronized MXNamedObjectList<MXMIDIIn> listAllInput() {
        if (_listAllInput != null) {
            return _listAllInput;
        }

        MXNamedObjectList<MXMIDIIn> temp = new MXNamedObjectList<MXMIDIIn>();

        MXMIDIIn sequencer = MXMIDIIn.INTERNAL_PLAYER;
        temp.addNameAndValue(sequencer.getName(), sequencer);
        if (sequencer.getPortAssignCount() == 0) {
            sequencer.setPortAssigned(0, true);
        }

        TreeSet<MXMIDIIn> sorted = new TreeSet<>();

        MXDriver java = MXDriver_Java._instance;
        for (int i = 0; i < java.InputDevicesRoomSize(); i++) {
            MXMIDIIn device = new MXMIDIIn(java, i);
            if (device == null) {
                continue;
            }
            if (device.getName().equals("Real Time Sequencer")) {
                continue;
            }
            sorted.add(device);
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
            sorted.add(device);
        }
        
        for (MXMIDIIn device : sorted) {
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

    public synchronized MXNamedObjectList<MXMIDIIn> listSelectedInput() {
        if (_listUsingInput != null) {
            return _listUsingInput;
        }
        MXNamedObjectList<MXMIDIIn> newInput = new MXNamedObjectList();
        for (MXMIDIIn midi : listAllInput().valueList()) {
            if (midi.getPortAssignCount() > 0) {
                newInput.addNameAndValue(midi.toString(), midi);
            }
        }
        _listUsingInput = newInput;
        return newInput;
    }

    public synchronized void closeAll() {
        for (MXMIDIIn input : listAllInput().valueList()) {
            if (input.isOpen()) {
                input.close();
            }
        }
    }

    @Override
    public MXINIFile prepareINIFile(File custom) {
        if (custom == null) {
            custom = MXINIFile.pathOf("MIDIInput");
        }
        MXINIFile setting = new MXINIFile(custom, this);
        setting.register("device[].name");
        setting.register("device[].open");
        setting.register("device[].port");
        setting.register("device[].toMaster");
        return setting;
    }

    @Override
    public boolean readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        if (!setting.readINIFile()) {
            return false;
        }

        MXNamedObjectList<MXMIDIIn> list = listAllInput();
        MXDriver_NotFound dummy = MXDriver_NotFound.getInstance();

        for (int seek = 0; seek < 1000; ++seek) {
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

            MXNamedObjectList<MXMIDIIn> detected = listAllInput();
            MXMIDIIn in = detected.valueOfName(deviceName);
            if (in != null) {
                if (deviceOpen.equals("1")) {
                    in.openInput(10000);
                }
            } else {
                in = new MXMIDIIn(dummy, dummy.InputAddDevice(deviceName));
                in.setPortAssigned(seek, true);
                detected.addNameAndValue(deviceName, in);
            }

            ArrayList<String> split = new ArrayList();
            MXUtil.split(devicePort, split, ',');
            for (String t1 : split) {
                try {
                    int x = Integer.parseInt(t1);
                    in.setPortAssigned(x, true);
                } catch (NumberFormatException e) {

                }
            }
            in.setMasterList(deviceMaster);
        }

        clearMIDIInCache();
        afterReadSetting();
        return true;
    }

    void afterReadSetting() {
        boolean assigned = false;
        MXNamedObjectList<MXMIDIIn> list = listAllInput();

        for (int i = 0; i < list.getSize(); ++i) {
            MXMIDIIn in = list.valueOfIndex(i);
            if (in.getPortAssignCount() > 0) {
                assigned = true;
            }
        }

        if (!assigned) {
            MXMIDIIn.INTERNAL_PLAYER.setPortAssigned(0, true);
        }

        clearMIDIInCache();
    }

    @Override
    public boolean writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        MXNamedObjectList<MXMIDIIn> all = listAllInput();
        int x = 0;
        for (MXMIDIIn e : all.valueList()) {
            if (e.getPortAssignCount() <= 0) {
                continue;
            }
            StringBuffer assigned = new StringBuffer();
            for (int p = 0; p < MXConfiguration.TOTAL_PORT_COUNT; ++p) {
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
                x++;
            }
        }
        return setting.writeINIFile();
    }

    @Override
    public boolean readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("MIDIInput");
            MXJsonParser.setAutosave(this);
        }
        MXJsonValue value = new MXJsonParser(custom).parseFile();
        if (value == null) {
            return false;
        }
        MXJsonValue.HelperForStructure root = value.new HelperForStructure();

        MXNamedObjectList<MXMIDIIn> listIn = listAllInput();
        MXDriver_NotFound dummy = MXDriver_NotFound.getInstance();
        MXJsonValue.HelperForArray deviceList = root.getFollowingArray("deviceList");

        if (deviceList != null) {
            for (int seek = 0; seek < deviceList.count(); ++seek) {
                MXJsonValue.HelperForStructure device = deviceList.getFollowingStructure(seek);

                String deviceName = device.getFollowingText("name", "");
                boolean deviceOpen = device.getFollowingBool("open", false);
                String deviceMaster = device.getFollowingText("toMaster", "");
                MXJsonValue.HelperForArray listPort = device.getFollowingArray("portlist");

                if (listPort == null) {
                    continue;
                }
                System.out.println("OPEN "+ deviceName + " = " + deviceOpen);

                MXNamedObjectList<MXMIDIIn> detected = listAllInput();
                MXMIDIIn in = detected.valueOfName(deviceName);
                if (in != null) {
                    if (deviceOpen) {
                        in.openInput(10000);
                    }
                } else {
                    in = new MXMIDIIn(dummy, dummy.InputAddDevice(deviceName));
                    detected.addNameAndValue(deviceName, in);
                }
                
                for (int p = 0; p < listPort.count(); ++ p) {
                    MXJsonValue.HelperForStructure port = listPort.getFollowingStructure(p);
                    int assigned = port.getFollowingInt("port", -1);
                    if (assigned < 0) {
                        continue;
                    }
                    in.setPortAssigned(assigned, true);
                    MXJsonValue.HelperForArray listFilter = port.getFollowingArray("filter");
                    for (int f = 0; f < listFilter.count(); ++ f) {
                        String filterName = listFilter.getFollowingText(f, "");
                        int z = MXMidiFilter.fromName(filterName);
                        if (z >= 0) {
                            in.getFilter(p).setChecked(z, true);
                        }
                    }
                }

                in.setMasterList(deviceMaster);
            }
        }
        clearMIDIInCache();
        afterReadSetting();
        return true;
    }

    @Override
    public boolean writeJsonFile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("MIDIInput");
        }

        MXJsonParser parser = new MXJsonParser(custom);
        MXJsonValue.HelperForStructure root = parser.getRoot().new HelperForStructure();
        MXJsonValue.HelperForArray listDevice = root.addFollowingArray("deviceList");
        MXNamedObjectList<MXMIDIIn> listIn = listAllInput();
        for (MXMIDIIn e : listIn.valueList()) {
            if (e.getPortAssignCount() <= 0) {
                continue;
            }
            MXJsonValue.HelperForStructure device = listDevice.addFollowingStructure();
            device.setFollowingText("name", e.getName());
            device.setFollowingBool("open", e.isOpen());
            device.setFollowingText("toMaster", e.getMasterList());

            MXJsonValue.HelperForArray listPort = device.addFollowingArray("portlist");
            for (int p = 0; p < MXConfiguration.TOTAL_PORT_COUNT; ++p) {
                if (e.isPortAssigned(p)) {
                    MXJsonValue.HelperForStructure port = listPort.addFollowingStructure();
                    port.setFollowingInt("port", p);
                    MXJsonValue.HelperForArray listFilter = port.addFollowingArray("filter");
                    for (int x = 0; x < MXMidiFilter.COUNT_TYPE; ++ x) {
                        String filterName = MXMidiFilter.getName(x);
                        if (e.getFilter(p).isChecked(x)) {
                            listFilter.addFollowingText(filterName);
                        }
                    }
                }
            }
        }

        return parser.writeFile();
    }

    @Override
    public void resetSetting() {
        return;
    }
}
