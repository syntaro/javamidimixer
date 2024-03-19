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

import java.util.EventObject;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class VUIAccessorEvent extends EventObject {
    private VUIAccessor _accessor;
    
    public VUIAccessorEvent(Object component, VUIAccessor accessor) {
        super(component);
        _accessor = accessor;
    }
    
    public VUIAccessor getUIAccessor() {
        return _accessor;
    }

    public String toString() {
        String name = _accessor._component.getUIClassID();
        Object value = _accessor.get();
        return name + " = " + value;
    }
}
