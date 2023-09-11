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
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_Java;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_UWP;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_Empty;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_VSTi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXMIDIOutManager implements MXSettingTarget {

    private static final MXMIDIOutManager _instance = new MXMIDIOutManager();

    public static MXMIDIOutManager getManager() {
        return _instance;
    }

    public void reloadDeviceList() {
        //TODO Java not support?
    }

    private MXSetting _setting;

    public void initWithSetting() {
        if (_setting == null) {
            _setting = new MXSetting("MIDIOutput");
            _setting.setTarget(this);
            MXWrapList<MXMIDIOut> listOut = listAllOutput();
            _setting.readSettingFile();

            MXWrapList<MXMIDIOut> list = listAllOutput();

            boolean assigned = false;
            for (int i = 0; i < list.getSize(); ++i) {
                MXMIDIOut out = list.valueOfIndex(i);
                if (out.getPortAssignCount() > 0) {
                    assigned = true;
                }
            }

            if (!assigned) {
                MXMIDIOut reserve = MXMIDIOutManager.getManager().findMIDIOutput("Gervill");
                MXMIDIOut reserve2 = MXMIDIOutManager.getManager().findMIDIOutput("Microsoft GS Wavetable Synth");
                MXMIDIOut reserve3 = MXMIDIOutManager.getManager().findMIDIOutput("VirtualMIDISynth #1");

                if (reserve2 != null) {
                    reserve = reserve2;
                }
                if (reserve3 != null) {
                    reserve = reserve3;
                }

                if (reserve != null) {
                    reserve.setPortAssigned(0, true);
                    reserve.openOutput(5);
                }
            }
        }
    }

    protected MXMIDIOutManager() {
    }

    protected MXWrapList<MXMIDIOut> _listAllOutput;
    protected MXWrapList<MXMIDIOut> _selectedOutput = null;
    //protected MXMIDIOut[] _cache;

    public MXWrapList<MXMIDIOut> listAllOutput() {
        synchronized (MXTiming.mutex) {
            if (_listAllOutput != null) {
                return _listAllOutput;
            }

            MXWrapList<MXMIDIOut> temp = new MXWrapList<MXMIDIOut>();

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

                temp.addNameAndValue(device.getName(), device);
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
                    temp.addNameAndValue(device.getName(), device);
                }
            }
            if (MXDriver_VSTi._instance.isUsable()) {
                MXDriver vst = MXDriver_VSTi._instance;
                for (int i = 0; i < vst.OutputDevicesRoomSize(); i++) {
                    MXMIDIOut out = new MXMIDIOut(MXDriver_VSTi._instance, i);
                    temp.addNameAndValue(out.getName(), out);
                }
            }

            _listAllOutput = temp;
            return _listAllOutput;
        }
    }

    public MXMIDIOut findMIDIOutput(String deviceName) {
        MXWrapList<MXMIDIOut> model = listAllOutput();
        return model.valueOfName(deviceName);
    }

    /*
    public List<MXMIDIOut> listMIDIOutput(int port) {
        MXWrapList<MXMIDIOut> model = listAllOutput();
        ArrayList<MXMIDIOut> ret = new ArrayList();
        for (int x = 0; x < model.getSize(); ++ x) {
            MXMIDIOut out = model.valueOfIndex(x);
            if (out.isPortAssigned(port)) {
                ret.add(out);
            }
        }
        return ret;
    }
     */
    void onClose(MXMIDIOut output) {
        synchronized (MXTiming.mutex) {
            clearMIDIOutCache();
        }
    }

    protected void clearMIDIOutCache() {
        synchronized (MXTiming.mutex) {
            _selectedOutput = null;
            //_cache = null;
        }
    }

    public MXWrapList<MXMIDIOut> listSelectedOutput() {
        synchronized (MXTiming.mutex) {
            if (_selectedOutput != null) {
                return _selectedOutput;
            }
            _selectedOutput = new MXWrapList();
            for (MXMIDIOut midi : listAllOutput().valueList()) {
                if (midi.getPortAssignCount() == 0) {
                    continue;
                }
                _selectedOutput.addNameAndValue(midi.getName(), midi);
            }
            return _selectedOutput;
        }
    }

    public void closeAll() {
        synchronized (MXTiming.mutex) {
            for (MXMIDIOut output : listAllOutput().valueList()) {
                if (output.isOpen()) {
                    System.out.println("closing output " + output);
                    output.close();
                    System.out.println("closing done " + output);
                }
            }
        }
    }

    @Override
    public void prepareSettingFields(MXSetting setting) {
        setting.register("device[].name");
        setting.register("device[].open");
        setting.register("device[].port");
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
        MXDriver_Empty dummy = MXDriver_Empty.getInstance();

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

            MXWrapList<MXMIDIOut> detected = listAllOutput();
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
        clearMIDIOutCache();
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        MXWrapList<MXMIDIOut> all = listAllOutput();
        int x = 0;
        for (MXMIDIOut e : all.valueList()) {
            StringBuffer assigned = new StringBuffer();
            for (int p = 0; p < MXAppConfig.TOTAL_PORT_COUNT; ++p) {
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
    }
}
