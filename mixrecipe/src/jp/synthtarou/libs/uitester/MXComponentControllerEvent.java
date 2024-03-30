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
package jp.synthtarou.libs.uitester;

import java.util.EventObject;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXComponentControllerEvent extends EventObject {
    private MXComponentController _mxcontrol;
    
    public MXComponentControllerEvent(Object component, MXComponentController mxcontrol) {
        super(component);
        _mxcontrol = mxcontrol;
    }
    
    public MXComponentController getMXContoller() {
        return _mxcontrol;
    }

    public String toString() {
        String name = _mxcontrol._component.getUIClassID();
        Object value = _mxcontrol.get();
        return name + " = " + value;
    }
}
