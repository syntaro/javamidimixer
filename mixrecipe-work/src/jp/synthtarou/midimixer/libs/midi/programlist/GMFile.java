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
package jp.synthtarou.midimixer.libs.midi.programlist;

import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.domino.DTextFolder;
import jp.synthtarou.midimixer.libs.domino.DTextMessage;
import jp.synthtarou.midimixer.libs.domino.DTextMessageList;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.midimixer.libs.midi.programlist.database.PDBank;
import jp.synthtarou.midimixer.libs.midi.programlist.database.PDDrumSet;
import jp.synthtarou.midimixer.libs.midi.programlist.database.PDFile;
import jp.synthtarou.midimixer.libs.midi.programlist.database.PDModule;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class GMFile implements PDFile {
    PDModule module = new GMLevel1();
    PDDrumSet drumSet = new GMLevel1Drum();
    PDModule drumModule = null;
    DTextMessageList ccList;

    @Override
    public String getName() {
        return "system Default";
    }

    @Override
    public PDModule getModule() {
        return module;
    }

    @Override
    public PDModule getDrumSet() {
        if (drumModule == null) {
            drumModule = new PDModule();
            PDBank bank = drumModule.smartReserve(getName()).smartReserve(0, "Standard Kit").smartReserve(-1, -1, "Standard Kit");
            bank.smartReserve(drumSet);
        }
        return drumModule;
    }

    @Override
    public DTextMessageList getCCList() {
        if (ccList == null) {
            ccList = new DTextMessageList();

            DTextFolder commandFolder = ccList.addFolder("Command", "Command");
            DTextFolder ccFolder = ccList.addFolder("Control Change", "Control Change");
            
            for (MXWrap<Integer> wrap : MXMidi.listupCommand()) {
                int command = wrap.value;
                if (command == MXMidi.COMMAND_CONTROLCHANGE) {
                    continue;
                }
                String name = MXMidi.nameOfChannelMessage(command);
                MXMessage message = MXMessageFactory.fromShortMessage(0, command, 0, 0);
                DTextMessage text = new DTextMessage(name, message.toDText(), message.getGate());
                commandFolder.addMessage(text);
            }
            for (int i = 0; i < 128; ++ i) {
                try {
                    MXMessage message = MXMessageFactory.fromShortMessage(0,0 + MXMidi.COMMAND_CONTROLCHANGE, i, 0);
                    DTextMessage text = new DTextMessage(MXMidi.nameOfControlChange(i), message.toDText(), message.getGate());
                    ccFolder.addMessage(text);
                }catch(Exception e) {
                    e.printStackTrace();;
                }
            }
        }
        return ccList;
    }
}
