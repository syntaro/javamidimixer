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
package jp.synthtarou.midimixer.libs.accessor;

import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.mx30surface.MGCircle;
import jp.synthtarou.midimixer.mx30surface.MGSlider;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MAccessor2 extends  MAccessor {
    protected static final int TYPE_MGSLIDER = 100;
    protected static final int TYPE_MGCIRCLE = 101;

    public MAccessor2(MGSlider component) {
        super(component, TYPE_MGSLIDER);
    }

    public MAccessor2(MGCircle component) {
        super(component, TYPE_MGCIRCLE);
    }

    @Override
    public void internalToUI() {
        new MainThreadTask() {
            @Override
            public Object runTask() {
                _selfLock ++;
                try {
                    synchronized(MAccessor2.this) {
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
    public void uiToInternal() {
        if (SwingUtilities.isEventDispatchThread() == false) {
            throw new IllegalStateException("not from ui thread");
        }
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
