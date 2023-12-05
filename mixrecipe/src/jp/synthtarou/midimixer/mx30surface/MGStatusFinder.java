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
package jp.synthtarou.midimixer.mx30surface;

import java.util.ArrayList;
import java.util.TreeSet;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MGStatusFinder {

    ArrayList<MGStatus>[][] _cachedControlChange;
    ArrayList<MGStatus>[][] _cachedChannelMessage;
    ArrayList<MGStatus>[][] _cachedNoteMessage;
    ArrayList<MGStatus> _cachedAnotherMessage;
    ArrayList<MGStatus> _cachedDataentry;

    MX32Mixer _mixer;

    public MGStatusFinder(MX32Mixer data) {
        _mixer = data;
        _cachedControlChange = new ArrayList[16][256];
        _cachedChannelMessage = new ArrayList[16][256];
        _cachedNoteMessage = new ArrayList[16][256];
        _cachedAnotherMessage = new ArrayList();
        _cachedDataentry = new ArrayList();
        for (int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++row) {
            for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
                makeCacheInternal1(_mixer.getStatus(MGStatus.TYPE_SLIDER,  row, column));
            }
        }

        for (int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++row) {
            for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
                makeCacheInternal1(_mixer.getStatus(MGStatus.TYPE_CIRCLE, row, column));
            }
        }

        for (int row = 0; row < MXAppConfig.DRUM_ROW_COUNT; ++row) {
            for (int column = 0; column < MXAppConfig.SLIDER_COLUMN_COUNT; ++column) {
                MGStatus status = _mixer.getStatus(MGStatus.TYPE_DRUMPAD, row, column);
                makeCacheInternal1(status);
            }
        }
    }

    public void makeCacheInternal1(MGStatus status) {
        if (status == null) {
            return;
        }

        MXMessage message = status._base;
        
        if (message == null) {
            return;
        }
        if (message.isCommand(MXMidi.COMMAND_CH_CONTROLCHANGE)) {
            int data1 = message.getData1();
            if (data1 == MXMidi.DATA1_CC_DATAENTRY || data1 == MXMidi.DATA1_CC_DATAINC || data1 == MXMidi.DATA1_CC_DATADEC) {
                _cachedDataentry.add(status);
                return;
            }
            if (_cachedControlChange[message.getChannel()][data1] == null) {
                _cachedControlChange[message.getChannel()][data1] = new ArrayList<>();
            }
            _cachedControlChange[message.getChannel()][data1].add(status);
            int data2 = -1;
            if (data1 >= 0 && data1 <= 31 && status._ccPair14) {
                data2 = data1 + 32;
                if (_cachedControlChange[message.getChannel()][data2] == null) {
                    _cachedControlChange[message.getChannel()][data2] = new ArrayList();
                }
                _cachedControlChange[message.getChannel()][data2].add(status);
            }
        } else if (message.isCommand(MXMidi.COMMAND_CH_NOTEON) || message.isCommand(MXMidi.COMMAND_CH_NOTEOFF)
                || message.isCommand(MXMidi.COMMAND_CH_POLYPRESSURE)) {
            int note = message.getData1();
            if (_cachedNoteMessage[message.getChannel()][note] == null) {
                _cachedNoteMessage[message.getChannel()][note] = new ArrayList();
            }
            _cachedNoteMessage[message.getChannel()][note].add(status);
        } else if (message.isMessageTypeChannel()) {
            int command = message.getStatus() & 0xf0;
            if (_cachedChannelMessage[message.getChannel()][command] == null) {
                _cachedChannelMessage[message.getChannel()][command] = new ArrayList();
            }
            _cachedChannelMessage[message.getChannel()][command].add(status);
        } else {
            _cachedAnotherMessage.add(status);
        }
    }

    public synchronized ArrayList<MGStatus> findCandidate(MXMessage request) {
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
