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
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;

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

    MX32MixerProcess _mixer;

    public MGStatusFinder(MX32MixerProcess data) {
        _mixer = data;
        _cachedControlChange = new ArrayList[16][256];
        _cachedChannelMessage = new ArrayList[16][256];
        _cachedNoteMessage = new ArrayList[16][256];
        _cachedAnotherMessage = new ArrayList();
        _cachedDataentry = new ArrayList();
        for (int row = 0; row < MXConfiguration.SLIDER_ROW_COUNT; ++row) {
            for (int column = 0; column < MXConfiguration.SLIDER_COLUMN_COUNT; ++column) {
                makeCacheImpl(_mixer.getStatus(MGStatus.TYPE_SLIDER, row, column));
            }
        }

        for (int row = 0; row < MXConfiguration.CIRCLE_ROW_COUNT; ++row) {
            for (int column = 0; column < MXConfiguration.SLIDER_COLUMN_COUNT; ++column) {
                makeCacheImpl(_mixer.getStatus(MGStatus.TYPE_CIRCLE, row, column));
            }
        }

        for (int row = 0; row < MXConfiguration.DRUM_ROW_COUNT; ++row) {
            for (int column = 0; column < MXConfiguration.SLIDER_COLUMN_COUNT; ++column) {
                makeCacheImpl(_mixer.getStatus(MGStatus.TYPE_DRUMPAD, row, column));
            }
        }
    }

    public void makeCacheImpl(MGStatus status) {
        if (status == null) {
            return;
        }

        MXMessage message = status._base;

        if (message.isCommand(MXMidiStatic.COMMAND_CH_CONTROLCHANGE)) {
            int data1 = message.getCompiled(1);
            if (data1 == MXMidiStatic.DATA1_CC_DATAENTRY || data1 == MXMidiStatic.DATA1_CC_DATAINC || data1 == MXMidiStatic.DATA1_CC_DATADEC) {
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
        } else if (message.isCommand(MXMidiStatic.COMMAND_CH_NOTEON) || message.isCommand(MXMidiStatic.COMMAND_CH_NOTEOFF)
                || message.isCommand(MXMidiStatic.COMMAND_CH_POLYPRESSURE)) {
            int note = message.getCompiled(1);
            if (_cachedNoteMessage[message.getChannel()][note] == null) {
                _cachedNoteMessage[message.getChannel()][note] = new ArrayList();
            }
            _cachedNoteMessage[message.getChannel()][note].add(status);
        } else if (message.isChannelMessage1()) {
            int command = message.getStatus() & 0xf0;
            if (_cachedChannelMessage[message.getChannel()][command] == null) {
                _cachedChannelMessage[message.getChannel()][command] = new ArrayList();
            }
            _cachedChannelMessage[message.getChannel()][command].add(status);
        } else {
            _cachedAnotherMessage.add(status);
        }
    }

    public synchronized ArrayList<MGStatus> findCandidate(MXMessage message) {
        if (message.isChannelMessage1()) {
            int command = message.getStatus() & 0xf0;
            if (command == MXMidiStatic.COMMAND_CH_CONTROLCHANGE) {
                int data1 = message.getCompiled(1);
                if (data1 == MXMidiStatic.DATA1_CC_DATAENTRY || data1 == MXMidiStatic.DATA1_CC_DATAINC || data1 == MXMidiStatic.DATA1_CC_DATADEC) {
                    return _cachedDataentry;
                }
                return _cachedControlChange[message.getChannel()][message.getCompiled(1)];
            } else if (command == MXMidiStatic.COMMAND_CH_NOTEON || command == MXMidiStatic.COMMAND_CH_NOTEOFF
                    || command == MXMidiStatic.COMMAND_CH_POLYPRESSURE) {
                return _cachedNoteMessage[message.getChannel()][message.getCompiled(1)];
            }

            return _cachedChannelMessage[message.getChannel()][command];
        } else {
            return _cachedAnotherMessage;
        }
    }

}
