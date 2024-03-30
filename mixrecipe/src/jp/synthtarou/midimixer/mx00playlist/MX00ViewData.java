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
package jp.synthtarou.midimixer.mx00playlist;

import jp.synthtarou.libs.navigator.MXPopupForList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX00ViewData {
    public MX00ViewData() {
        _playListModel = new PlayListDX();
        _playAsChained = false;
        _playAsRepeated = false;
        _focusChannel = -1;
    }

    boolean _playAsChained;
    boolean _playAsRepeated;
    PlayListDX _playListModel;
    
    int _focusChannel;
    int _soundMargin;
    int _soundSpan;
    boolean _showMeasure;
}
