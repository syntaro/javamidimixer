/*
 * Copyright (C) 2024 Syntarou YOSHIDA
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
package jp.synthtarou.midimixer.mx85soundfont;

import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.driver.MXDriver_SoundFont;
import jp.synthtarou.mixtone.synth.XTSynthesizer;
import jp.synthtarou.mixtone.synth.view.XTSynthesizerSettingPanel;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX85Process extends MXReceiver<XTSynthesizerSettingPanel> {
    XTSynthesizerSettingPanel _view;
    XTSynthesizer _synth;

    public MX85Process() {
        _view = new XTSynthesizerSettingPanel();
        _synth = MXDriver_SoundFont.getSharedSynthesizer();
    }
    
    @Override
    public String getReceiverName() {
        return "Sound Font";
    }

    @Override
    public XTSynthesizerSettingPanel getReceiverView() {
        return _view;
    }

    @Override
    public void processMXMessage(MXMessage message) {
    }
}
