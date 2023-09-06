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
 * You should havereceived a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jp.synthtarou.midimixer.libs;

import java.util.Comparator;
import java.util.TreeSet;
import javax.swing.JPanel;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.MXReceiver;
import jp.synthtarou.midimixer.libs.midi.port.MXVisitant;
import jp.synthtarou.midimixer.mx30controller.MGStatus;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class UniqueChecker extends MXReceiver {
    TreeSet<MGStatus> _alreadyStatus = new TreeSet(new StatusComparator());

    @Override
    public String getReceiverName() {
        return "Unique Filter";
    }

    @Override
    public JPanel getReceiverView() {
        return null;
    }

    @Override
    protected void processMXMessageImpl(MXMessage message) {
        sendOnlyOnce(message);
    }
    
    final MXReceiver _receiver;
    
    public UniqueChecker(MXReceiver receiver) {
        _receiver = receiver;
    }
    
    public boolean isStatusCanPathThru(MGStatus status) {
        if (_alreadyStatus.contains(status)) {
            return false;
        }
        push(status);
        return true;
    }
    
    public void push(MGStatus status) {
        _alreadyStatus.add(status);
    }
    
    public boolean sendOnlyOnce(MXMessage message) {
        if (message.isCommand(MXMidi.COMMAND_NOTEON) && message.getValue()._var == 0) {
            MXMessage message2 = MXMessageFactory.fromShortMessage(message.getPort(), MXMidi.COMMAND_NOTEOFF + message.getChannel(), message.getGate()._var, 0);
            message2._timing = message._timing;
            message = message2;
        }
        if (_receiver == null) {
            //panel is under construction
            return false;
        }
        if (_alreadyMessage.contains(message)) {
            return false;
        }
        _alreadyMessage.add(message);
        MXMain.getMain().messageDispatch(message, _receiver);
        return true;
    }

    static class StatusComparator implements Comparator<MGStatus> {
        @Override
        public int compare(MGStatus o1, MGStatus o2) {
            int p = o1.getPort() - o2.getPort();
            if (p < 0) return -1;
            if (p > 0) return 1;
            
            int z = o1.getUiType() - o2.getUiType();
            if (z < 0) return -1;
            if (z > 0) return 1;

            int y = o1.getRow() - o2.getRow();
            if (y < 0) return -1;
            if (y > 0) return 1;

            int x = o1.getColumn() - o2.getColumn();
            if (x < 0) return -1;
            if (x > 0) return 1;

            return 0;
        }
    }
    
    TreeSet<MXMessage> _alreadyMessage = new TreeSet(new MessageComparator());
    
    static class MessageComparator implements Comparator<MXMessage> {

        @Override
        public int compare(MXMessage o1, MXMessage o2) {
            if (o1.isDataentry()) {
                if (o2.isDataentry() == false) {
                    return -1;
                }
                MXVisitant v1 = o1.getVisitant();
                MXVisitant v2 = o2.getVisitant();
                int x;
                x = v1.getDataroomType() - v2.getDataroomType();
                if (x == 0) { x = v1.getDataroomMSB() - v2.getDataroomMSB(); }
                if (x == 0) { x = v1.getDataroomLSB() - v2.getDataroomLSB(); }
                if (x == 0) { x = v1.getDataentryMSB()- v2.getDataentryMSB(); }
                if (x == 0) { x = v1.getDataentryLSB() - v2.getDataentryLSB(); }
                if (x == 0) { x = v1.getDataentryValue14() - v2.getDataentryValue14(); }
                return x;
            }else if (o2.isDataentry()) {
                return 1;
            }
            
            byte[] t1 = o1.getDataBytes();
            byte[] t2 = o2.getDataBytes();
            
            if (t1 == null) {
                if (t2 == null) {
                    return 0;
                }
                return  -1;
            }
            if (t2 == null) {
                return 1;
            }            

            int x = t1.length - t2.length;
            
            if (x < 0) return -1;
            if (x > 0) return 1;

            for (int i = 0; i < t1.length; ++ i) {
                x = t1[i] - t2[i];
                if (x < 0) return -1;
                if (x > 0) return 1;
            }
            x = o1.getPort() - o2.getPort();
            if (x < 0) return -1;
            if (x > 0) return 1;

            /*
            x = o1.getStatus()- o2.getStatus();
            if (x < 0) return -1;
            if (x > 0) return 1;

            x = o1.getGate()- o2.getGate();
            if (x < 0) return -1;
            if (x > 0) return 1;

            x = o1.getValue()- o2.getValue();
            if (x < 0) return -1;
            if (x > 0) return 1;
            */
            return 0;
        }
    }

    public TreeSet<MGStatus> skipAlreadyOne(TreeSet<MGStatus> target) {
        if (target == null || target.isEmpty()) {
            return null;
        }
        TreeSet<MGStatus> func = new TreeSet<>();
        for (MGStatus status : target) {
            if (_alreadyStatus.contains(status)) {
            }
            else {
                func.add(status);
            }
        }
        return func.isEmpty() ? null : func;
    }
}
