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

import java.util.EventObject;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXUIPropertyEvent extends EventObject {
    private MXUIProperty _uiProperty;
    
    public MXUIPropertyEvent(Object component, MXUIProperty uiProperty) {
        super(component);
        _uiProperty = uiProperty;
    }
    
    public MXUIProperty getMXContoller() {
        return _uiProperty;
    }

    public String toString() {
        String name = _uiProperty._component.getUIClassID();
        Object value = _uiProperty.get();
        return name + " = " + value;
    }
}
