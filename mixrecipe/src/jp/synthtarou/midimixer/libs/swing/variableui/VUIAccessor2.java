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
package jp.synthtarou.midimixer.libs.swing.variableui;

import jp.synthtarou.midimixer.mx30surface.MGCircle;
import jp.synthtarou.midimixer.mx30surface.MGSlider;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class VUIAccessor2 extends  VUIAccessor {
    protected static final int TYPE_MGSLIDER = 100;
    protected static final int TYPE_MGCIRCLE = 101;

    public VUIAccessor2(MGSlider component) {
        super(component, TYPE_MGSLIDER);
    }

    public VUIAccessor2(MGCircle component) {
        super(component, TYPE_MGCIRCLE);
    }

    @Override
    public void internalToUI() {
        new VUITask() {
            @Override
            public Object run() {
                _selfLock ++;
                try {
                    synchronized(VUIAccessor2.this) {
                        switch (_supportedType) {
                            case TYPE_MGSLIDER:
                                MGSlider slider = (MGSlider)_component;
                                slider.getStatus().setMessageValue(_varAsNumeric);
                                slider.publishUI();
                                break;
                            case TYPE_MGCIRCLE:
                                MGCircle circle = (MGCircle)_component;
                                circle.getStatus().setMessageValue(_varAsNumeric);
                                circle.publishUI();
                                break;
                        }
                    }
                }finally {
                    _selfLock --;                 
                }
                return NOTHING;
            }
        };
    }
    
    @Override
    public synchronized void uiToInternal() {
        int var;
        switch (_supportedType) {
            case TYPE_MGSLIDER:
                MGSlider slider = (MGSlider)_component;
                var = slider.getStatus().getValue()._value;
                _varAsObject = var;
                _varAsBoolean = (var > 0) ? true : false;
                _varAsNumeric = var;
                _varAsText = Integer.toString(_varAsNumeric);
                break;
            case TYPE_MGCIRCLE:
                MGCircle circle = (MGCircle)_component;
                var = circle.getStatus().getValue()._value;
                _varAsObject = var;
                _varAsBoolean = (var > 0) ? true : false;
                _varAsNumeric = var;
                _varAsText = Integer.toString(_varAsNumeric);
                break;
        }
    }
}
