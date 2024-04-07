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
package jp.synthtarou.midimixer.mx36ccmapping;

import java.util.ArrayList;
import java.util.List;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.mx30surface.MGStatus;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX36RowId /* implements Comparable<MX36StatusRow> */{
    public MX36RowId(int uiType, int row) {
        _uiType = uiType;
        _row = row;
    }
    /*
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof MX36RowId) {
            MX36RowId row = (MX36RowId)o;
            if (_uiType == row._uiType && _row == row._row) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int compareTo(MX36RowId row) {
        if (row == null) {
            return -1;
        }
        int x = _uiType - row._uiType;
        int y = _row - row._row;
        if (x < 0) return -1;
        if (x > 0) return 1;
        if (y < 0) return -1;
        if (y > 0) return 1;
        return 0;
    }*/
    

    int _uiType;
    int _row;

    public String toString() {
        String num = "";
        if (_row >= 1) {
            num = Integer.toString(_row + 1);
        }
        switch(_uiType) {
            case MGStatus.TYPE_CIRCLE:
                return "Knob" + num;
            case MGStatus.TYPE_SLIDER:
                return "Slider" + num;
            case MGStatus.TYPE_DRUMPAD:
                return "Drum" + num;
        }
        return "-";
    }
    
    static final MXNamedObjectList<MX36RowId> ListModel = new MXNamedObjectList<>();

    public static MX36RowId find(int uiType, int row) {
        for (int i = 0; i < ListModel.size() ; ++ i) {
            MX36RowId seek = ListModel.get(i)._value;
            if (seek._uiType == uiType && seek._row == row) {
                return seek;
            }
        }
        return null;
    }
    

    static {
        List<MX36RowId> build = new ArrayList();

        build.add(new MX36RowId(0, 0));
        
        for (int i = 0; i < MXConfiguration.CIRCLE_ROW_COUNT; ++ i) {
            build.add(new MX36RowId(MGStatus.TYPE_CIRCLE, i));
        }
        for (int i = 0; i < MXConfiguration.SLIDER_ROW_COUNT; ++ i) {
            build.add(new MX36RowId(MGStatus.TYPE_SLIDER, i));
        }
        for (int i = 0; i < MXConfiguration.DRUM_ROW_COUNT; ++ i) {
            build.add(new MX36RowId(MGStatus.TYPE_DRUMPAD, i));
        }
        
        for (MX36RowId seek : build) {
            ListModel.addNameAndValue(seek.toString(), seek);
        }
    }
}
