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
 */package jp.synthtarou.midimixer.libs.midi.smf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiFileFormat;
import static jp.synthtarou.midimixer.libs.midi.smf.SMFMessage.dumpHexFF;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFParser {
    File _file;
    SMFFileInfo _info;

    public SMFParser(File file) throws IOException {
        _file = file;
        parseFile(file);
    }
    
    public SMFFileInfo getFileInfo() {
        return _info;
    }

    protected boolean parseFile(File file) throws IOException {
        InputStream stream = new FileInputStream(file);
        _info = new SMFFileInfo();
        try {
            int fileLength = MidiFileFormat.UNKNOWN_LENGTH;
            ByteReader reader = new ByteReader(stream);

            try {
                int maxReadLength = 16;
                int duration = MidiFileFormat.UNKNOWN_LENGTH;

                int x1, x2, x3, x4;
                boolean okMagic;

                while(!reader._eof) {
                    x1 = reader.read8();
                    if (x1 != 0x4d) continue;
                    x2 = reader.read8();
                    if (x2 != 0x54) continue;
                    x3 = reader.read8();
                    if (x3 != 0x68) continue;
                    x4 = reader.read8();
                    if (x4 != 0x64) continue;
                    break;
                }
                if (reader._eof) {
                    return  false;
                }

                int bytesRemaining = reader.read32() - 6;

                int type = reader.read16();
                int numtracks = reader.read16();
                _info.setResolution(reader.read16());

                if (_info.getResolution() > 0) {
                    // delta = 四分音符あたりの解像度
                    _info.setSmpteFormat(-99999);
                }else {
                    // delta = 秒の分数
                    int frameCode = (_info.getResolution() >> 8) * -1;
                    _info.setSmpteFormat(frameCode);
                    _info.setResolution(_info.getResolution() & 0xff); // フレーム内の分解能
                }


                reader.skip(bytesRemaining);
                int tracks = numtracks;

                if (type != 0 && type != 1) {
                    return false;
                }

                SMFMessageList list = new SMFMessageList();

                for (int tr = 0; tr < tracks; tr++) {
                    int trackLength = 0;
                    while(!reader._eof) {
                        x1 = reader.read8();
                        if (x1 != 0x4d) continue;
                        x2 = reader.read8();
                        if (x2 != 0x54) continue;
                        x3 = reader.read8();
                        if (x3 != 0x72) continue;
                        x4 = reader.read8();
                        if (x4 != 0x6B) continue;
                        break;
                    }
                    if (reader._eof) {
                        break;
                    }

                    trackLength = reader.read32();
                    if (trackLength < 0) {
                        return false;
                    }
                    if (trackLength == 0) {
                        System.out.println("SMF parts length = 0");
                        continue;
                    }

                    try {
                        ByteReaderChildren child = new ByteReaderChildren(reader, trackLength);
                        // reset current tick to 0
                        long tick = 0;

                        // reset current status byte to 0 (invalid value).
                        // this should cause us to throw an InvalidMidiDataException if we don't
                        // get a valid status byte from the beginning of the track.
                        int status = 0;
                        boolean endOfTrackFound = false;

                        SMFMessage message = null;
                        int order = 0;

                        while (!child._eof && !endOfTrackFound) {

                            int data1 = -1;         // initialize to invalid value
                            int data2 = 0;

                            // each event has a tick delay and then the event data.

                            // first read the delay (a variable-length int) and update our tick value
                            long step = child.readVariable();
                            if (step != 0) {                                
                                tick += step;
                                order = 0;
                            }else {
                                order ++;
                            }

                            // check for new status
                            int byteValue = child.read8();

                            if (byteValue >= 0x80) {
                                status = byteValue;
                            } else {
                                data1 = byteValue;
                            }

                            switch (status & 0xF0) {
                            case 0x80:
                            case 0x90:
                            case 0xA0:
                            case 0xB0:
                            case 0xE0:
                                // two data bytes
                                if (data1 == -1) {
                                    data1 =  child.read8();
                                }
                                data2 =  child.read8();

                                message = new SMFMessage(tick, status, data1, data2);
                                message._seqTrack = tr;
                                message._order = order;
                                break;
                            case 0xC0:
                            case 0xD0:
                                // one data byte
                                if (data1 == -1) {
                                    data1 =  child.read8();
                                }
                                message = new SMFMessage(tick, status, data1, 0);
                                message._seqTrack = tr;
                                message._order = order;
                                break;
                            case 0xF0:
                                // sys-ex or meta
                                switch(status) {
                                case 0xF0:
                                case 0xF7:
                                    // sys ex
                                    int sysexLength = (int) child.readVariable();
                                    if (sysexLength == 0) {
                                        continue;
                                    }
                                    byte[] sysexData = new byte[sysexLength];
                                    child.readBuffer(sysexData, sysexLength);

                                    message = new SMFMessage(tick, status, status, sysexData);
                                    message._seqTrack = tr;
                                    message._order = order;
                                    break;

                                case 0xFF:
                                    // meta
                                    int metaType = child.read8();
                                    int metaLength = (int) child.readVariable();
                                    if (metaType < 0 || metaLength < 0) {
                                        break;
                                    }
                                    if (metaLength == 0) {
                                        continue;
                                    }
                                    byte[] metaData = new byte[metaLength];
                                    child.readBuffer(metaData, metaLength);

                                    message = new SMFMessage(tick, status, metaType, metaData);
                                    message._seqTrack = tr;
                                    message._order = order;
                                    break;
                                default:
                                    throw new InvalidMidiDataException("Invalid status byte: " + status);
                                } // switch sys-ex or meta
                                break;
                            default:
                                throw new InvalidMidiDataException("Invalid status byte: " + status);
                            }
                            if (message == null) {
                                continue;
                            }
                            list.add(message);
                        } 
                    } catch (ArrayIndexOutOfBoundsException e) {
                        return false;
                    }
                }
                _messageList = list;
                return true;
            }catch(InvalidMidiDataException midi)  {
                return false;
            }
        }finally {
            stream.close();
        }

    }

    private static final int MThd_MAGIC = 0x4d546864;  // 'MThd'
    
    public SMFMessageList getMessageList() {
        return _messageList;
    }
    
    public String[] readMetaInfo() {
        ArrayList<String> ret  = new ArrayList();
        
        for (SMFMessage message : _messageList._set) {
            if (message._status == 0xff && message._dataType != 0x51) {
                int metaType = message._dataType;
                byte[] data = message.getBinary();
                String meta = "";
                try {
                    meta = dumpHexFF(data);
                    meta = new String(data, 3, data.length - 3, "ISO-8859-1");
                    meta = new String(data, 3, data.length - 3, "Shift_JIS");
                }catch(Exception e) {
                    
                }
                ret.add(meta);
            }
        }
        String[] ar = new String[ret.size()];
        ret.toArray(ar);
        return ar;
    }
    
    private SMFMessageList _messageList;
    private BufferedInputStream _stream;
}
