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
import java.util.TreeSet;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_Java;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_UWP;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_NotFound;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_VSTi;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.json.MXJsonValue;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_FluidSynth;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMIDIOutManager implements MXINIFileSupport, MXJsonSupport {

    private static final MXMIDIOutManager _instance = new MXMIDIOutManager();

    public static MXMIDIOutManager getManager() {
        return _instance;
    }

    public void reloadDeviceList() {
        //TODO Java not support?
    }

    @Override
    public boolean readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        if (!setting.readINIFile()) {
            afterReadSettings();
            return false;
        }
        MXNamedObjectList<MXMIDIOut> listOut = listAllOutput();
        MXDriver_NotFound dummy = MXDriver_NotFound.getInstance();

        for (int seek = 0; seek < 1000; ++seek) {
            String deviceName = setting.getSetting("device[" + seek + "].name");
            String deviceOpen = setting.getSetting("device[" + seek + "].open");
            String devicePort = setting.getSetting("device[" + seek + "].port");

            if (deviceName == null || deviceName.length() == 0) {
                break;
            }
            if (deviceOpen == null) {
                deviceOpen = "0";
            }
            if (deviceOpen.equals("1")) {
                if (devicePort == null) {
                    devicePort = String.valueOf(seek);
                }
            }

            MXNamedObjectList<MXMIDIOut> detected = listAllOutput();
            MXMIDIOut out = detected.valueOfName(deviceName);
            if (out != null) {
                if (deviceOpen.equals("1")) {
                    out.openOutput(5);
                }
            } else {
                out = new MXMIDIOut(dummy, dummy.OuputAddDevice(deviceName));
                detected.addNameAndValue(deviceName, out);
            }
            ArrayList<String> split = new ArrayList();
            MXUtil.split(devicePort, split, ',');
            for (String t1 : split) {
                try {
                    int x = Integer.parseInt(t1);
                    out.setPortAssigned(x, true);
                } catch (NumberFormatException e) {

                }
            }
        }
        afterReadSettings();
        return true;
    }

    void afterReadSettings() {
        MXNamedObjectList<MXMIDIOut> list = listAllOutput();

        boolean assigned = false;
        for (int i = 0; i < list.getSize(); ++i) {
            MXMIDIOut out = list.valueOfIndex(i);
            if (out.getPortAssignCount() > 0) {
                assigned = true;
            }
        }

        if (!assigned) {
            MXMIDIOut reserve1 = MXMIDIOutManager.getManager().findMIDIOutput("Microsoft GS Wavetable Synth");
            MXMIDIOut reserve2 = MXMIDIOutManager.getManager().findMIDIOutput("Gervill");
            MXMIDIOut reserve3 = MXMIDIOutManager.getManager().findMIDIOutput("VirtualMIDISynth #1");

            if (reserve2 != null) {
                reserve1 = reserve2;
            }
            if (reserve3 != null) {
                reserve1 = reserve3;
            }

            if (reserve1 != null) {
                reserve1.setPortAssigned(0, true);
                reserve1.openOutput(5);
            }
        }
        clearMIDIOutCache();
    }

    protected MXMIDIOutManager() {
    }

    protected MXNamedObjectList<MXMIDIOut> _listAllOutput;
    protected MXNamedObjectList<MXMIDIOut> _selectedOutput = null;
    //protected MXMIDIOut[] _cache;

    public MXNamedObjectList<MXMIDIOut> listAllOutput() {
        if (_listAllOutput != null) {
            return _listAllOutput;
        }

        TreeSet<MXMIDIOut> sorted = new TreeSet<>();

/*        
        MXDriver sfz = MXDriver_SoundFont.getInstance();
        for (int i = 0; i < sfz.OutputDevicesRoomSize(); i++) {
            MXMIDIOut device = new MXMIDIOut(sfz, i);
            sorted.add(device);
        }
*/
        System.err.println("loading Fluid");
        MXDriver fluid = MXDriver_FluidSynth.getInstance();
        System.err.println("Fluid = " + fluid + "/ " + fluid.isUsable());
        for (int i = 0; i < fluid.OutputDevicesRoomSize(); i++) {
            MXMIDIOut device = new MXMIDIOut(fluid, i);
            sorted.add(device);
        }
        
        MXDriver java = MXDriver_Java._instance;
        for (int i = 0; i < java.OutputDevicesRoomSize(); i++) {
            MXMIDIOut device = new MXMIDIOut(java, i);

            String name = device.getName();
            if (MXDriver_UWP._instance.isUsable()) {
                if (name.equals("Real Time Sequencer") || name.equals("Unknown name")) {
                    continue;
                }
                if (name.startsWith("Microsoft GS Wave")) {
                    continue;
                }
            }

            sorted.add(device);
        }
        if (MXDriver_UWP._instance.isUsable()) {
            MXDriver uwp = MXDriver_UWP._instance;
            for (int i = 0; i < uwp.OutputDevicesRoomSize(); i++) {
                MXMIDIOut device = new MXMIDIOut(uwp, i);
                //if (name.equals("MIDI")) {
                //    continue;
                //}
                //if (temp.indexOfName(name) >= 0) {
                //    continue;
                //}
                sorted.add(device);
            }
        }
        if (MXDriver_VSTi._instance.isUsable()) {
            MXDriver vst = MXDriver_VSTi._instance;
            for (int i = 0; i < vst.OutputDevicesRoomSize(); i++) {
                MXMIDIOut device = new MXMIDIOut(MXDriver_VSTi._instance, i);
                sorted.add(device);
            }
        }
        MXNamedObjectList<MXMIDIOut> temp = new MXNamedObjectList<MXMIDIOut>();
        
        for (MXMIDIOut seek :sorted) {
            temp.addNameAndValue(seek.getName(), seek);
        }

        _listAllOutput = temp;
        return _listAllOutput;
    }

    public MXMIDIOut findMIDIOutput(String deviceName) {
        MXNamedObjectList<MXMIDIOut> model = listAllOutput();
        return model.valueOfName(deviceName);
    }

    void onClose(MXMIDIOut output) {
        clearMIDIOutCache();
    }

    protected void clearMIDIOutCache() {
        _selectedOutput = null;
    }

    public MXNamedObjectList<MXMIDIOut> listSelectedOutput() {
        if (_selectedOutput != null) {
            return _selectedOutput;
        }
        _selectedOutput = new MXNamedObjectList();
        for (MXMIDIOut midi : listAllOutput().valueList()) {
            if (midi.getPortAssignCount() == 0) {
                continue;
            }
            _selectedOutput.addNameAndValue(midi.getName(), midi);
        }
        return _selectedOutput;
    }

    public void closeAll() {
        for (MXMIDIOut output : listAllOutput().valueList()) {
            if (output.isOpen()) {
                output.close();
            }
        }
    }

    @Override
    public MXINIFile prepareINIFile(File custom) {
        if (custom == null) {
            custom = MXINIFile.pathOf("MIDIOutput");
        }
        MXINIFile setting = new MXINIFile(custom, this);
        setting.register("device[].name");
        setting.register("device[].open");
        setting.register("device[].port");
        return setting;
    }

    @Override
    public boolean writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        MXNamedObjectList<MXMIDIOut> all = listAllOutput();
        int x = 0;
        for (MXMIDIOut e : all.valueList()) {
            StringBuilder assigned = new StringBuilder();
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
                x++;
            }
        }
        return setting.writeINIFile();
    }

    @Override
    public boolean readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("MIDIOutput");
            MXJsonParser.setAutosave(this);
        }
        MXJsonParser parser = new MXJsonParser(custom);
        MXJsonValue value = parser.parseFile();
        if (value == null) {
            return false;
        }

        MXNamedObjectList<MXMIDIOut> listOut = listAllOutput();
        MXDriver_NotFound dummy = MXDriver_NotFound.getInstance();

        MXJsonValue.HelperForStructure root = value.new HelperForStructure();
        MXJsonValue.HelperForArray list = root.getFollowingArray("deviceList");

        if (list != null) {
            for (int seek = 0; seek < list.count(); ++seek) {
                MXJsonValue.HelperForStructure device = list.getFollowingStructure(seek);

                String deviceName = device.getFollowingText("name", "");
                boolean deviceOpen = device.getFollowingBool("open", false);
                MXJsonValue.HelperForArray listPort = device.getFollowingArray("portlist");

                if (deviceName == null || deviceName.length() == 0) {
                    break;
                }

                MXNamedObjectList<MXMIDIOut> detected = listAllOutput();
                MXMIDIOut out = detected.valueOfName(deviceName);
                if (out != null) {
                    if (deviceOpen) {
                        out.openOutput(5);
                    }
                } else {
                    out = new MXMIDIOut(dummy, dummy.OuputAddDevice(deviceName));
                    detected.addNameAndValue(deviceName, out);
                }
                if (listPort != null) {
                    for (int p = 0; p < listPort.count(); ++ p) {
                        MXJsonValue.HelperForStructure port = listPort.getFollowingStructure(p);
                        int assigned = port.getFollowingInt("port", -1);
                        if (assigned < 0) {
                            continue;
                        }
                        out.setPortAssigned(assigned, true);
                        MXJsonValue.HelperForArray listFilter = port.getFollowingArray("filter");
                        for (int f = 0; f < listFilter.count(); ++ f) {
                            String filterName = listFilter.getFollowingText(f, "");
                            int z = MXMidiFilter.fromName(filterName);
                            if (z >= 0) {
                                out.getFilter(p).setChecked(z, true);
                            }
                        }
                    }
                }
            }
        }
        clearMIDIOutCache();
        afterReadSettings();
        return true;
    }

    @Override
    public boolean writeJsonFile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("MIDIOutput");
        }
        MXJsonParser parser = new MXJsonParser(custom);
        MXJsonValue value = parser.getRoot();
        MXJsonValue.HelperForStructure root = value.new HelperForStructure();

        MXJsonValue.HelperForArray deviceList = root.addFollowingArray("deviceList");

        MXNamedObjectList<MXMIDIOut> listOut = listAllOutput();
        for (MXMIDIOut e : listOut.valueList()) {
            StringBuilder assigned = new StringBuilder();
            for (int p = 0; p < MXConfiguration.TOTAL_PORT_COUNT; ++p) {
                if (e.isPortAssigned(p)) {
                    if (assigned.length() > 0) {
                        assigned.append(",");
                    }
                    assigned.append(Integer.toString(p));
                }
            }
            if (assigned.length() > 0) {
                MXJsonValue.HelperForStructure device = deviceList.addFollowingStructure();
                device.setFollowingText("name", e.getName());
                device.setFollowingBool("open", e.isOpen());

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
        }

        return parser.writeFile();
    }

    @Override
    public void resetSetting() {
    }
}
