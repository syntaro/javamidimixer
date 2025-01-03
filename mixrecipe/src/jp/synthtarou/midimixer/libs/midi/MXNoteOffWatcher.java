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

    public synchronized boolean setHandler(MXMessage noteOn, MXMessage noteOff, Handler listener) {
        if (noteOn.isCommand(MXMidiStatic.COMMAND_CH_NOTEON) == false) {
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

    public synchronized void allNoteOff(MXMessage parent) {
        for (Element e : _list) {
            MXMessage base = e.sendSide;
            MXMessage msg = MXMessageFactory.fromNoteoff(base.getPort(), base.getChannel(), base.getCompiled(1));
            msg._owner = MXMessage.getRealOwner(parent);
            e.listener.onNoteOffEvent(msg);
        }
        _list.clear();
    }

    public synchronized void allNoteOffToPort(MXMessage parent, int to) {
        Iterator<Element> it = _list.iterator();
        while (it.hasNext()) {
            Element e = it.next();
            if (e.sendSide.getPort() != to) {
                continue;
            }
            it.remove();
            MXMessage base = e.sendSide;
            MXMessage msg = MXMessageFactory.fromNoteoff(base.getPort(), base.getChannel(), base.getCompiled(1));
            msg._owner = MXMessage.getRealOwner(parent);
            e.listener.onNoteOffEvent(msg);
        }
    }

    public synchronized void allNoteFromPort(MXMessage parent, int from) {
        Iterator<Element> it = _list.iterator();
        while (it.hasNext()) {
            Element e = it.next();
            if (e.catchSide.getPort() != from) {
                continue;
            }
            it.remove();
            MXMessage base = e.sendSide;
            MXMessage msg = MXMessageFactory.fromNoteoff(base.getPort(), base.getChannel(), base.getCompiled(1));
            msg._owner = MXMessage.getRealOwner(parent);
            e.listener.onNoteOffEvent(msg);
        }
        //_list.clear();
    }

    public boolean raiseHandler(MXMessage target) {
        return raiseHandler(target, target.getPort(), target.getChannel(), target.getCompiled(1));
    }

    public synchronized boolean raiseHandler(MXMessage parent, int port, int ch, int note) {
        int proc = 0;
        Iterator<Element> it = _list.iterator();
        while (it.hasNext()) {
            Element e = it.next();
            if (e.catchSide.getPort() == port
                    && e.catchSide.getChannel() == ch
                    && e.catchSide.getCompiled(1) == note) {
                MXMessage noteOff = MXMessageFactory.fromNoteoff(
                        e.sendSide.getPort(),
                        e.sendSide.getChannel(),
                        e.sendSide.getCompiled(1));
                noteOff._owner = MXMessage.getRealOwner(parent);
                e.listener.onNoteOffEvent(noteOff);
                it.remove();
                proc++;
            }
        }
        return proc > 0 ? true : false;
    }
}
