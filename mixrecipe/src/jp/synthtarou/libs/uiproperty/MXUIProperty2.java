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
package jp.synthtarou.libs.uiproperty;

import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.mx30surface.MGCircle;
import jp.synthtarou.midimixer.mx30surface.MGSlider;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXUIProperty2 extends MXUIProperty {

    public void install(MGSlider slider) {
        super.install(slider);
    }

    public void install(MGCircle circle) {
        super.install(circle);
    }
 
    @Override
    protected void internalToUI() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("not from ui thread");
        }
        if (_component instanceof MGSlider slider || _component instanceof MGCircle circle) {
            MXMain.invokeUI(() ->  {
                _stopFeedback++;
                try {
                    synchronized (MXUIProperty2.this) {
                        if (_component instanceof MGSlider slider) {
                            slider.getStatus().setMessageValue(_varAsNumeric);
                            slider.publishUI(slider.getStatus().getValue());
                        } else if (_component instanceof MGCircle circle) {
                            circle.getStatus().setMessageValue(_varAsNumeric);
                            circle.publishUI(circle.getStatus().getValue());
                        }
                    }
                } finally {
                    _stopFeedback--;
                }
            });
        } else {
            super.internalToUI();
        }
    }

    @Override
    protected void uiToInternal() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("not from ui thread");
        }
        int var;
        if (_component instanceof MGSlider slider) {
            var = slider.getStatus().getValue()._value;
            _varAsObject = var;
            _varAsBoolean = (var > 0) ? true : false;
            _varAsNumeric = var;
            _varAsText = Integer.toString(_varAsNumeric);
        } else if (_component instanceof MGCircle circle) {
            var = circle.getStatus().getValue()._value;
            _varAsObject = var;
            _varAsBoolean = (var > 0) ? true : false;
            _varAsNumeric = var;
            _varAsText = Integer.toString(_varAsNumeric);
        } else {
            super.uiToInternal();
        }
    }
}
