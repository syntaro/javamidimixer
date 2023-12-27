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
package jp.synthtarou.midimixer.libs.midi;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Syntarou YOSHIDA
 */

public class MXNoteOffWatcher {

    public static interface Handler {
        public void onNoteOffEvent(MXTiming timing, MXMessage target);
    }
    
    private static class Element {
        MXMessage catchSide;
        MXMessage sendSide;
        Handler listener;
    }

    private LinkedList<Element> _list = new LinkedList<Element>();

    public MXNoteOffWatcher() {
    }
    
    public String toString() {
        return "(MXNoteOff + " + _list.size();
    }
    
    Handler _lastHandler;

    public boolean setHandler(MXMessage noteOn, MXMessage noteOff, Handler listener) {
        synchronized(MXTiming.mutex) {
            if (noteOn.isCommand(MXMidi.COMMAND_CH_NOTEON) == false) {
                System.out.println("Its not note on " + noteOn);
                return false;
            }
            _lastHandler = listener;
            Element e = new Element();
            e.catchSide = noteOn;
            e.sendSide = noteOff;
            e.listener = listener;
            _list.add(e);
            return true;
        }
    }
    
    public void allNoteOff(MXTiming timing) {
        synchronized(MXTiming.mutex) {
            if (timing == null) {
                timing = new MXTiming();
            }
            for (Element e : _list) {
                MXMessage base = e.sendSide;
                MXMessage msg = MXMessageFactory.fromShortMessage(base.getPort(), MXMidi.COMMAND_CH_NOTEOFF + base.getChannel(), base.getGate()._value, 0);
                e.listener.onNoteOffEvent(timing, msg);
            }
            _list.clear();
        }
    }

    public void allNoteOffToPort(MXTiming timing, int to) {
        synchronized(MXTiming.mutex) {
            if (timing == null) {
                timing = new MXTiming();
            }
            Iterator<Element> it = _list.iterator();
            while (it.hasNext()) {
                Element e = it.next();
                if (e.sendSide.getPort() != to) {
                    continue;
                }
                //it.remove();
                MXMessage base = e.sendSide;
                MXMessage msg = MXMessageFactory.fromShortMessage(base.getPort(), MXMidi.COMMAND_CH_NOTEOFF + base.getChannel(), base.getGate()._value, 0);
                e.listener.onNoteOffEvent(timing, msg);
            }
            //_list.clear();
        }
    }

    public void allNoteFromPort(MXTiming timing, int from) {
        synchronized(MXTiming.mutex) {
            Iterator<Element> it = _list.iterator();
            while (it.hasNext()) {
                Element e = it.next();
                if (e.catchSide.getPort() != from) {
                    continue;
                }
                //it.remove();
                MXMessage base = e.sendSide;
                MXMessage msg = MXMessageFactory.fromShortMessage(base.getPort(), MXMidi.COMMAND_CH_NOTEOFF + base.getChannel(), base.getGate()._value, 0);
                e.listener.onNoteOffEvent(timing, msg);
            }
            //_list.clear();
        }
    }
    
    public boolean raiseHandler(MXMessage target) {
        return raiseHandler(target.getPort(), target._timing, target.getChannel(), target.getData1());
    }

    public boolean raiseHandler(int port, MXTiming timing, int ch, int note) {
        synchronized(MXTiming.mutex) {
            int proc = 0;
            Iterator<Element> it = _list.iterator();
            while (it.hasNext()) {
                Element e = it.next();
                if (e.catchSide.getPort() == port
                 && e.catchSide.getChannel() == ch
                 && e.catchSide.getData1()== note) {
                    MXMessage noteOff = MXMessageFactory.fromShortMessage(
                            e.sendSide.getPort(), 
                            MXMidi.COMMAND_CH_NOTEOFF + e.sendSide.getChannel(), 
                            e.sendSide.getData1(), 0);
                    e.listener.onNoteOffEvent(timing, noteOff);
                    it.remove();
                    proc ++;
                }
            }
            return proc > 0 ? true : false;
        }
    }
}
