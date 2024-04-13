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
import jp.synthtarou.libs.log.MXFileLogger;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXNoteOffWatcher {

    public static interface Handler {

        public void onNoteOffEvent(MXMessage target);
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
        synchronized (MXTiming.mutex) {
            if (noteOn.isCommand(MXMidi.COMMAND_CH_NOTEON) == false) {
                MXFileLogger.getLogger(MXMessage.class).severe("Its not note on " + noteOn);
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

    public void allNoteOff(MXMessage parent) {
        synchronized (MXTiming.mutex) {
            for (Element e : _list) {
                MXMessage base = e.sendSide;
                MXMessage msg = MXMessageFactory.fromNoteoff(base.getPort(), base.getChannel(), base.getGate()._value);
                e.listener.onNoteOffEvent(msg);
            }
            _list.clear();
        }
    }

    public void allNoteOffToPort(MXMessage parent, int to) {
        synchronized (MXTiming.mutex) {
            Iterator<Element> it = _list.iterator();
            while (it.hasNext()) {
                Element e = it.next();
                if (e.sendSide.getPort() != to) {
                    continue;
                }
                it.remove();
                MXMessage base = e.sendSide;
                MXMessage msg = MXMessageFactory.fromNoteoff(base.getPort(), base.getChannel(), base.getGate()._value);
                e.listener.onNoteOffEvent(msg);
            }
            //_list.clear();
        }
    }

    public void allNoteFromPort(MXMessage parent, int from) {
        synchronized (MXTiming.mutex) {
            if (parent == null) {
                parent = (MXMessage) MXMessageFactory.createDummy().clone();
            }
            Iterator<Element> it = _list.iterator();
            while (it.hasNext()) {
                Element e = it.next();
                if (e.catchSide.getPort() != from) {
                    continue;
                }
                it.remove();
                MXMessage base = e.sendSide;
                MXMessage msg = MXMessageFactory.fromNoteoff(base.getPort(), base.getChannel(), base.getGate()._value);
                e.listener.onNoteOffEvent(msg);
            }
            //_list.clear();
        }
    }

    public boolean raiseHandler(MXMessage target) {
        return raiseHandler(target, target.getPort(), target.getChannel(), target.getData1());
    }

    public boolean raiseHandler(MXMessage parent, int port, int ch, int note) {
        synchronized (MXTiming.mutex) {
            int proc = 0;
            if (parent == null) {
                parent = (MXMessage) MXMessageFactory.createDummy().clone();
            }
            Iterator<Element> it = _list.iterator();
            while (it.hasNext()) {
                Element e = it.next();
                if (e.catchSide.getPort() == port
                        && e.catchSide.getChannel() == ch
                        && e.catchSide.getData1() == note) {
                    MXMessage noteOff = MXMessageFactory.fromNoteoff(
                            e.sendSide.getPort(),
                            e.sendSide.getChannel(),
                            e.sendSide.getData1());
                    e.listener.onNoteOffEvent(noteOff);
                    it.remove();
                    proc++;
                }
            }
            return proc > 0 ? true : false;
        }
    }
}
