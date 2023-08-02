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
package jp.synthtarou.midimixer.libs.midi.smf;

import jp.synthtarou.midimixer.libs.midi.MXMessage;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFRecorder {
    SMFTempoList _listTempo;
    SMFPlayer _player = new SMFPlayer();
    SMFMessageList _listMessage;

    long _startClock = System.currentTimeMillis();
    
    public void startRecording() {
        _startClock = System.currentTimeMillis();
        _player = new SMFPlayer();
        _player._paraPlay = true;
        _listTempo = new SMFTempoList(null, 96);
        _listMessage = _player.listMessage();
    }

    public SMFPlayer getPlayer() {
        return _player;
    }
    
    public long currentTick() {
        long spent = System.currentTimeMillis() - _startClock;
        return _listTempo.MicrosecondsToTicks(spent * 1000);
    }
    
    public void addNote(MXMessage message) {
        byte[] data = message.createBytes();
        int port = message.getPort();
        if (data.length >= 3) {
            int status = data[0] & 0xff;
            int data1 = data[1] & 0xff;
            int data2 = data[2] & 0xff;
    
            if (data.length >= 5 && status == 0xff && data1 == 0x51) {
                SMFMessage smf = new SMFMessage(currentTick(), data);
                smf._port = port;
                _listMessage.add(smf);
                int mpq1 = (data[2] & 0xff) << 16;
                int mpq2 = (data[3] & 0xff) << 8;
                int mpq3 = (data[4] & 0xff);
                _listTempo.addTempo(System.currentTimeMillis() - _startClock, currentTick(), mpq1 + mpq2 + mpq3);
                return;
            }
            else if (status == 0xff) {
                SMFMessage smf = new SMFMessage(currentTick(), data);
                smf._port = port;
                _listMessage.add(smf);
                return;
            }
            else if (status == 0xf0 || status == 0xf7) {
                SMFMessage smf = new SMFMessage(currentTick(), data);
                smf._port = port;
                _listMessage.add(smf);
                return;
            }
            else {
                SMFMessage smf = new SMFMessage(currentTick(), status, data1, data2);
                smf._port = port;
                _listMessage.add(smf);
                return;
            }
        }
    }
}
