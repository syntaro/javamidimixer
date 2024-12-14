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

import java.util.ArrayList;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXPianoHarmony {
    
    boolean isOrdinary(int[] keys) {
        for (int i = 1; i < keys.length; ++ i) {
            if (keys[i-1] < keys[i]) {
                continue;
            }
            else {
                //not ordinally
                return false;
            }
        }
        return true;
    }

    boolean[] makeMap(int[] keys) {
        int offset = keys[0];
        boolean[] map = new boolean[256];
        for (int x :  keys) {
            map[x - offset] = true;
        }
        return map;
    }
    
    boolean isSameMap(boolean[] map1, boolean[] map2) {
        for (int i = 0; i < map1.length; ++ i) {
            if (map1[i] != map2[i]) {
                return false;
            }
        }
        return true;
    }
    
    class Element {
        String _name;
        boolean[] _touch;

        public Element(String name, int[] keys) {
            if (!isOrdinary(keys)) {
                throw new IllegalArgumentException("keys is not sorted");
            }
            _name = name;
            _touch = makeMap(keys);
        }
        
        public String toString() {
            return toString(0, 4);
        }
        
        public ArrayList<Integer> toNoteList(int note, int octave) {
            //note 0~11
            //octave -1~8?
            ArrayList<Integer> ret = new ArrayList<>();
            for (int i = 0; i < _touch.length; ++ i) {
                if (_touch[i]) {
                    ret.add(note + i + ((octave+1) * 12));
                }
            }
            return ret;
        }
        
        public String toString(int note, int octave) {
            StringBuilder str = new StringBuilder();
            str.append(_noteNameList[note]);
            str.append(_name);
            ArrayList<Integer> list = toNoteList(note, octave);
            for (int i = 0; i < list.size(); ++ i) {
                if (i == 0) {
                    str.append("[");
                }else {
                    str.append(", ");
                }
                str.append(MXMidi.nameOfNote(list.get(i)));
            }
            str.append("]");
            return str.toString();
        }
    }
    
    ArrayList<Element> _listFormat;

    void addToListFormat(String name, int key1, int key2, int key3) {
        _listFormat.add(new Element(name, new int[] { key1, key2, key3 }));
    }
    
    void addToListFormat(String name, int key1, int key2, int key3, int key4) {
        _listFormat.add(new Element(name, new int[] { key1, key2, key3, key4 }));
    }
    
    void addToListFormat(String name, int key1, int key2, int key3, int key4, int key5) {
        _listFormat.add(new Element(name, new int[] { key1, key2, key3, key4, key5 }));
    }
    
    final String[] _noteNameList = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    final int _rootOctave = -1;

    public MXPianoHarmony() {
        _listFormat = new ArrayList<>();
        addToListFormat("", 0, 4, 7);
        addToListFormat("m", 0, 3,7);
        addToListFormat("7", 0,4 ,7, 10);
        addToListFormat("M7", 0,4 ,7, 11);
        addToListFormat("m7", 0,3,7, 10);
        addToListFormat("mM7", 0,3,7, 11);
        addToListFormat("6", 0,4,7, 9);
        addToListFormat("m6", 0,3,7, 9);
        addToListFormat("9", 0,4,7, 10, 14);
        addToListFormat("M9", 0,4,7, 11, 14);
        addToListFormat("m9", 0,3,7, 10, 14);
        addToListFormat("69", 0,4,7, 9, 14);
        addToListFormat("m69", 0,3,7, 9, 14);
        addToListFormat("sus4", 0,5,7);
        addToListFormat("7sus4", 0,5,7, 10);
        addToListFormat("dim", 0,3, 6);
        addToListFormat("aug", 0,4, 8);
        addToListFormat("aug7", 0,4, 8, 10);
        addToListFormat("add9", 0,4, 7, 14);
        addToListFormat("7+5", 0,4,8,10);
        addToListFormat("7-5", 0, 3,6,10);
        addToListFormat("7(#9)", 0, 4, 7, 10, 15);
        addToListFormat("7-9", 0, 4, 7, 10, 13);
    }
    
    public static int[] rotateNote(int[] original, int step) {
        int len = original.length;
        int[] ret = new int[len];
        for (int i = 0; i < len - step; ++ i)  {
            ret[i] = original[i + step];
        }
        for (int i = len - step; i < len; ++ i) {
            ret[i] = original[i - (len - step)] + 12;
        }
        return ret;
    }
    
    public ArrayList<String> getChordName(int[] noteList) {
        //validate
        ArrayList<String> ret = new ArrayList<>();
        for (int shift = 0; shift < noteList.length; ++ shift) {
            int[] shifted = rotateNote(noteList, shift);
            int root = shifted[0];
            if (isOrdinary(shifted) == false) {
                break;
            }
            boolean[] map = makeMap(shifted);
            for (Element seek : _listFormat) {
                if (isSameMap(map, seek._touch)) {
                    String base = _noteNameList[root % 12];
                    String chord = seek._name;
                    String text = base + chord;
                    if (ret.indexOf(text) < 0) {
                        ret.add(text);
                    }
                }
            }
        }
        return ret;
    }
    
    public static void main(String[] args) {
        MXPianoHarmony examine = new MXPianoHarmony();
        for (int note = 0; note < 12; ++ note) {
            for (Element e : examine._listFormat) {
                String base = e.toString(note, 4);
                System.out.println("base  = " + base);

                int[] noteList = toArray(e.toNoteList(note, 4));
                for (int shift = 0; shift < noteList.length; ++ shift) {
                    int[] noteShifted = examine.rotateNote(noteList, shift);
                    if (examine.isOrdinary(noteShifted)) {
                        ArrayList<String> chord = examine.getChordName(noteShifted);
                        System.out.println("shift = " + chord);
                    }
                }
            }
        }
    }
    
    static int[] toArray(ArrayList<Integer> list) {
        int [] ret = new int[list.size()];
        for (int i = 0; i < list.size(); ++ i) {
            ret[i] = list.get(i);
        }
        return ret;
    }
    
    static ArrayList<Integer> toList(int[] array) {
        ArrayList<Integer> ret = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; ++ i) {
            ret.add(array[i]);
        }
        return ret;
    }
}
