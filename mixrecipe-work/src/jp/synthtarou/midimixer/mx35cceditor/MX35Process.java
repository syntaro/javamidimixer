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
package jp.synthtarou.midimixer.mx35cceditor;

import javax.swing.JPanel;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX35Process extends MXReceiver implements MXSettingTarget {
    private MX35View _view;
    boolean _editingControl;
    MXSetting _setting;
    
    public MX35Process() {
        _setting = new MXSetting("MixingGlobal");
        _setting.setTarget(this);
        _view = new MX35View(this);
    }

    @Override
    public String getReceiverName() {
        return "Module Mixer";
    }

    @Override
    public JPanel getReceiverView() {
        return _view;
    }

    @Override
    protected void processMXMessageImpl(MXMessage message) {
        sendToNext(message);
    }

    @Override
    public void prepareSettingFields(MXSetting setting) {
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
    }
}

