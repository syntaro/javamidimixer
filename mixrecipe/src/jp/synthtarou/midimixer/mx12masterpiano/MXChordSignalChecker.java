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
package jp.synthtarou.midimixer.mx12masterpiano;

import java.util.ArrayList;
import jp.synthtarou.midimixer.mx00playlist.MXPianoHarmony;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXChordSignalChecker {
    boolean[] _currentNote = new boolean[128];
    ArrayList<Integer> _detected;

    public MXChordSignalChecker() {
        _detected = new ArrayList<>();
    }

    public void noteOn(int note) {
        _currentNote[note] = true;
        refresh();
    }
    
    public void noteOff(int note) {
        _currentNote[note] = false;
    }
    
    public void refresh() {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < _currentNote.length; ++ i) {
            if (_currentNote[i]) {
                result.add(i);
            }
        }
        _detected = result;
    }
    
    public ArrayList<Integer> getNotes() {
        return _detected;
    }
    
    MXPianoHarmony _harmony = new MXPianoHarmony();
    
    public ArrayList<String> getChordName() {
        ArrayList<Integer> c = _detected;
        int[] keys  = new int[c.size()];
        for (int i = 0; i < keys.length; ++ i) {
            keys[i] = c.get(i);
        }
        
        return _harmony.getChordName(keys);
    }
}
