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
package jp.synthtarou.cceditor.message;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class CCParameters {
    public static final int UNSET = -1;

    public int _channel = UNSET;
    public int _value = UNSET;
    public int _gate = UNSET;
    
    public boolean _hasDataRPN = false;
    public boolean _hasDataNRPN = false;
    public int _dataroomMSB = UNSET;
    public int _dataroomLSB = UNSET;
    
    public void setDataentry(boolean isReserved, int msb, int lsb) {
        if (isReserved) {
            _hasDataRPN = true;
            _hasDataNRPN = false;
            _dataroomMSB = msb;
            _dataroomLSB = lsb;
        }else {
            _hasDataRPN = false;
            _hasDataNRPN = true;
            _dataroomMSB = msb;
            _dataroomLSB = lsb;
        }
    }
    
    public boolean hasDataEntryAny() {
        if (_hasDataRPN || _hasDataNRPN) {
            return true;
        }
        return false;
    }

    public boolean hasDataEntryReserved() {
        if (_hasDataRPN) {
            return true;
        }
        return false;
    }

    public boolean hasDataEntryNoneReserver() {
        if (_hasDataNRPN) {
            return true;
        }
        return false;
    }

    public int _rolandPort = UNSET;
}
