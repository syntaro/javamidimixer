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
package jp.synthtarou.mixtone.synth.audio;

import java.util.ArrayList;
import java.util.Collection;
import jp.synthtarou.mixtone.synth.oscilator.XTOscilator;
import java.util.LinkedList;

/**
 *
 * @author Syntarou YOSHIDA
 */

public class XTAudioTrack {
    LinkedList<XTOscilator> _oscilator = new LinkedList<>();
    public final int _track;
    
    public XTAudioTrack(int track) {
        _track = track;
    }
    
    public int size() {
        return _oscilator.size();
    }
    
    public void add(XTOscilator osc) {
        _oscilator.add(osc);
    }
    
    public void remove(XTOscilator osc) {
        _oscilator.remove(osc);
    }
    
    public void removeAll(Collection<XTOscilator> list) {
        _oscilator.removeAll(list);
    }
    
    public int countNoteOffed() {
        int cnt = 0;
        for (XTOscilator seek : _oscilator) {
            if (seek.isNoteOff()) {
                cnt ++;
            }
        }
        return cnt;
    }

    public void removeSmallTone() {
        XTOscilator ret = null;
        for (XTOscilator seek : _oscilator) {
            if (seek.isNoteOff()) {
                _oscilator.remove(seek);
                break;
            }
            if (ret == null || ret._playKey > seek._playKey) {
                ret = seek;
            }
        }
        _oscilator.remove(ret);
    }

    ArrayList<XTOscilator> _muted = new ArrayList<>();

    public void cleanMuted() {
        for (XTOscilator seek : _oscilator) {
            if (seek.isFaded()) {
                _muted.add(seek);
            }
        }
        if (_muted.isEmpty() == false) {
            _oscilator.removeAll(_muted);
            _muted.clear();
        }
    }
}
