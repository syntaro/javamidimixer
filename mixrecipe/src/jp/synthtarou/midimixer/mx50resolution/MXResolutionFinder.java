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
package jp.synthtarou.midimixer.mx50resolution;

import jp.synthtarou.midimixer.mx30surface.*;
import java.util.ArrayList;
import java.util.List;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXResolutionFinder {

    ArrayList<MXResolution>[][] _cachedControlChange;
    ArrayList<MXResolution>[][] _cachedChannelMessage;
    ArrayList<MXResolution>[][] _cachedNoteMessage;
    ArrayList<MXResolution> _cachedAnotherMessage;
    ArrayList<MXResolution> _cachedDataentry;

    MX32MixerProcess _mixer;

    public MXResolutionFinder(List<MXResolution> list) {
        _cachedControlChange = new ArrayList[16][256];
        _cachedChannelMessage = new ArrayList[16][256];
        _cachedNoteMessage = new ArrayList[16][256];
        _cachedAnotherMessage = new ArrayList();
        _cachedDataentry = new ArrayList();
        for (MXResolution reso : list) {
            makeCacheInternal1(reso);
        }
    }

    public void makeCacheInternal1(MXResolution reso) {
        if (reso == null) {
            return;
        }

        MXMessage message = reso._base;
        
        if (message == null) {
            return;
        }
        if (message.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
            int data1 = message.getData1();
            if (data1 == MXMidi.DATA1_CC_DATAENTRY || data1 == MXMidi.DATA1_CC_DATAINC || data1 == MXMidi.DATA1_CC_DATADEC) {
                _cachedDataentry.add(reso);
                return;
            }
            if (_cachedControlChange[message.getChannel()][data1] == null) {
                _cachedControlChange[message.getChannel()][data1] = new ArrayList<>();
            }
            _cachedControlChange[message.getChannel()][data1].add(reso);
        } else if (message.isCommand(MXMidi.COMMAND_CH_NOTEON) || message.isCommand(MXMidi.COMMAND_CH_NOTEOFF)
                || message.isCommand(MXMidi.COMMAND_CH_POLYPRESSURE)) {
            int note = message.getData1();
            if (_cachedNoteMessage[message.getChannel()][note] == null) {
                _cachedNoteMessage[message.getChannel()][note] = new ArrayList();
            }
            _cachedNoteMessage[message.getChannel()][note].add(reso);
        } else if (message.isMessageTypeChannel()) {
            int command = message.getStatus() & 0xf0;
            if (_cachedChannelMessage[message.getChannel()][command] == null) {
                _cachedChannelMessage[message.getChannel()][command] = new ArrayList();
            }
            _cachedChannelMessage[message.getChannel()][command].add(reso);
        } else {
            _cachedAnotherMessage.add(reso);
        }
    }

    public synchronized ArrayList<MXResolution> findCandidate(MXMessage request) {
        if (request.isMessageTypeChannel()) {
            int command = request.getStatus() & 0xf0;
            if (command == MXMidi.COMMAND_CH_CONTROLCHANGE) {
                int data1 = request.getData1();
                if (data1 == MXMidi.DATA1_CC_DATAENTRY || data1 == MXMidi.DATA1_CC_DATAINC || data1 == MXMidi.DATA1_CC_DATADEC) {
                    return _cachedDataentry;
                }
                return _cachedControlChange[request.getChannel()][request.getGate()._var];
            } else if (command == MXMidi.COMMAND_CH_NOTEON || command == MXMidi.COMMAND_CH_NOTEOFF
                    || command == MXMidi.COMMAND_CH_POLYPRESSURE) {
                return _cachedNoteMessage[request.getChannel()][request.getGate()._var];
            }

            return _cachedChannelMessage[request.getChannel()][command];
        } else {
            return _cachedAnotherMessage;
        }
    }
}
