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
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_Java;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_UWP;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_NotFound;
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
            MNamedValueList<MXMIDIOut> listOut = listAllOutput();
            _setting.readSettingFile();

            MNamedValueList<MXMIDIOut> list = listAllOutput();

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
        }
    }

    protected MXMIDIOutManager() {
    }

    protected MNamedValueList<MXMIDIOut> _listAllOutput;
    protected MNamedValueList<MXMIDIOut> _selectedOutput = null;
    //protected MXMIDIOut[] _cache;

    public MNamedValueList<MXMIDIOut> listAllOutput() {
        synchronized (MXTiming.mutex) {
            if (_listAllOutput != null) {
                return _listAllOutput;
            }

            MNamedValueList<MXMIDIOut> temp = new MNamedValueList<MXMIDIOut>();

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
        MNamedValueList<MXMIDIOut> model = listAllOutput();
        return model.valueOfName(deviceName);
    }

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

    public MNamedValueList<MXMIDIOut> listSelectedOutput() {
        synchronized (MXTiming.mutex) {
            if (_selectedOutput != null) {
                return _selectedOutput;
            }
            _selectedOutput = new MNamedValueList();
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
                    output.close();
                }
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
    }

    @Override
    public void afterReadSettingFile() {
        MXDriver_NotFound dummy = MXDriver_NotFound.getInstance();

        for (int seek = 0; seek < 1000; ++seek) {
            String deviceName = _setting.getSetting("device[" + seek + "].name");
            String deviceOpen = _setting.getSetting("device[" + seek + "].open");
            String devicePort = _setting.getSetting("device[" + seek + "].port");

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

            MNamedValueList<MXMIDIOut> detected = listAllOutput();
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
    public void beforeWriteSettingFile() {
        MNamedValueList<MXMIDIOut> all = listAllOutput();
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
                _setting.setSetting("device[" + x + "].name", e.getName());
                _setting.setSetting("device[" + x + "].open", e.isOpen() ? "1" : "0");
                _setting.setSetting("device[" + x + "].port", assigned.toString());
                x++;
            }
        }
    }
}
