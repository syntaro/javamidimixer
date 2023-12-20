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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;
import jp.synthtarou.midimixer.mx36ccmapping.SortedArray;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFParser {

    public static void main(String[] args) {
        ArrayList<String> skip = new ArrayList();
        skip.add("C:/Windows");
        String[] paths = { "C:/MIDI" , "C:/Program Files", "C:/Users" };

        for (String loop : paths) {
            int countSmpte = 0, countTempo = 0;
            ArrayList<File> fileList = SMFParser.scanSMF(loop, skip);
            skip.add(loop);
            for (File seek : fileList) {
                try {
                    SMFParser parse = new SMFParser(seek);
                    //System.out.println(parse._file + " = " + parse._messageList.size());
                    if (parse._smpteFormat >= 0) {
                        System.out.println(seek);
                        countSmpte ++;
                    }
                    else {
                        countTempo ++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("SMPTE " + countSmpte +" / TEMPO " + countTempo);
        }
    }

    static ArrayList<File> scanSMF(String directory, Collection<String> skip) {
        File f = new File(directory);
        if (f.isDirectory()) {
            return scanSMF(f, skip);
        }
        return null;
    }

    static ArrayList<File> scanSMF(File directory, Collection<String> skip) {
        ArrayList<File> ret = new ArrayList();
        LinkedList<File> seek = new LinkedList();
        int midCount = 0, directoryCount = 0;
        TreeSet<String> already = new TreeSet();
        if (skip != null) {
            for (String text : skip) {
                try {
                    String path = new File(text).getCanonicalPath();
                    already.add(path);
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        seek.add(directory);
        System.out.println("Start scan " + directory);
        while (seek.isEmpty() == false) {
            File file = seek.remove();
            try {
                String canon = file.getCanonicalPath();
                if (already.contains(canon)) {
                    System.out.println("Skip " + file + " as " + canon);
                    continue;
                }
                already.add(canon);
            }catch(IOException e) {
                e.printStackTrace();;
            }
            if (file.isDirectory()) {
                File[] childs = file.listFiles();
                directoryCount ++;
                if ((directoryCount % 1000) == 0) {
                    System.out.println("hit " + midCount + " / total " + directoryCount );
                }
                if (childs == null) {
                    continue;
                }
                for (File scan : childs) {                
                    seek.add(scan);
                }
            } else if (file.isFile()) {
                if (file.getName().toLowerCase().endsWith(".mid")) {
                    ret.add(file);
                    midCount ++;
                }
            }
        }
        System.out.println("fileList " + ret.size());
        return ret;
    }

    File _file;
    int _infoType = 0; //0 or 1, 2 will error
    int _infoTrackCount = 1; // 1 or 16 ?
    int _smpteFormat = -99999; // 23.976fps、24.00fps、25.00fps、29.97fps、30.00fps
    int _fileResolution = 96;

    public SMFParser() {
        // as recorder
        _listMessage = new SortedArray<>();
    }
    
    public SMFParser(File file) {
        try {
            if (!readFile(file)) {
                throw new IllegalArgumentException("Invalid MIDI File. " + file);
            }
        } catch (FileNotFoundException ioe) {
            throw new IllegalArgumentException("MIDI File Not Found. " + file, ioe);
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Invalid MIDI File. " + file, ioe);
        }
    }

    void calcMillisecondsUseSMPTE() {
        double frameRate = _smpteFormat;
        if (_smpteFormat == 29) {
            frameRate = 29.97;
        }

        double tickPerMilliSecond = frameRate * _fileResolution / 1000;

        for (SMFMessage message : _listMessage) {
            message._milliSeconds = (long) (message._tick / tickPerMilliSecond);
        }
    }

    void calcMillisecondsUseTempo() {
        SMFTempoList tempo = new SMFTempoList(_listMessage, _fileResolution);
        for (SMFMessage message : _listMessage) {
            message._milliSeconds = tempo.TicksToMicroseconds(message._tick) / 1000;
        }
    }

    void calcSMPTEUseMilliseconds() {
        double frameRate = _smpteFormat;
        if (_smpteFormat == 29) {
            frameRate = 29.97;
        }

        double tickPerMilliSecond = frameRate * _fileResolution / 1000;

        for (SMFMessage message : _listMessage) {
            message._tick = (long)(message._milliSeconds * tickPerMilliSecond);
        }
    }
    

    protected boolean seekMagicNumber(SMFInputStream stream, String magic) {
        int x = 0;
        while (!stream._eof) {
            int c = stream.read8();
            if (c == magic.charAt(x)) {
                x++;
                if (x >= magic.length()) {
                    return true;
                }
            } else {
                x = 0;
            }
        }
        return false;
    }

    protected boolean readFile(File file) throws IOException {
        _file = file;
        InputStream input = new BufferedInputStream(new FileInputStream(file));
        try {
            SMFInputStream reader = new SMFInputStream(input);

            if (seekMagicNumber(reader, "MThd") == false) {
                return false;
            }

            if (reader._eof) {
                return false;
            }

            int headerLength = reader.read32();
            if (headerLength < 6) {
                return false;
            }

            int type = reader.read16();
            int trackCount = reader.read16();
            _infoType = type;
            _infoTrackCount = trackCount;
            int res = reader.read16();

            if (res > 0) {
                // delta = 四分音符あたりの解像度
                _smpteFormat = -99999;
                _fileResolution = res;
            } else {
                // delta = 秒の分数
                _smpteFormat = (res >> 8) * -1;
                _fileResolution = res & 0xff; // フレーム内の分解能
            }

            if (headerLength >= 7) {
                reader.skip(headerLength - 6);
            }

            if (type != 0 && type != 1) {
                throw new IllegalArgumentException("Unsupported SMF Type " + type + " is not 0 nor 1");
            }

            SortedArray<SMFMessage> list = new SortedArray();

            for (int tr = 0; tr < trackCount; tr++) {
                if (seekMagicNumber(reader, "MTrk") == false) {
                    break;
                }
                if (reader._eof) {
                    break;
                }

                int trackLength = reader.read32();
                if (trackLength <= 0) {
                    continue;
                }

                SMFStreamForTrack child = new SMFStreamForTrack(reader, trackLength);
                long tick = 0;
                int status = 0;
                int fileOrder = 0;

                while (!child._eof) {
                    long step = child.readVariable();

                    SMFMessage message = fromStream(child);
                    tick += step;
                    if (step != 0) {
                        fileOrder = 0;
                    } else {
                        fileOrder++;
                    }
                    if (message != null) {
                        message._tick = tick;
                        message._seqTrack = tr;
                        message._fileOrder = fileOrder;
                        list.insertSorted(message);
                    }
                }
            }
            _listMessage = list;

            if (_smpteFormat > 0) {
                calcMillisecondsUseSMPTE();
            } else {
                calcMillisecondsUseTempo();
            }
            return !_listMessage.isEmpty();
        } finally {
            input.close();
        }
    }

    public SortedArray<SMFMessage> _listMessage;

    public SMFMessage fromStream(SMFStreamForTrack child) {
        SMFMessage message = null;

        int status = child.read8();
        int data1, data2;

        switch (status & 0xF0) {
            case 0x80:
            case 0x90:
            case 0xA0:
            case 0xB0:
            case 0xE0:
                data1 = child.read8();
                data2 = child.read8();

                message = new SMFMessage(0, status, data1, data2);
                break;
            case 0xC0:
            case 0xD0:
                data1 = child.read8();
                message = new SMFMessage(0, status, data1, 0);
                break;
            case 0xF0:
                // sys-ex or meta
                switch (status) {
                    case 0xF0:
                    case 0xF7:
                        // sys ex
                        int sysexLength = (int) child.readVariable();
                        if (sysexLength == 0) {
                            break;
                        }
                        byte[] sysexData = new byte[sysexLength + 1];
                        sysexData[0] = (byte) status;
                        child.readBuffer(sysexData, 1, sysexLength);

                        message = new SMFMessage(0, sysexData);
                        break;

                    case 0xFF:
                        // meta
                        int metaType = child.read8();
                        int metaLength = (int) child.readVariable();
                        if (metaType < 0 || metaLength < 0) {
                            break;
                        }
                        if (metaLength == 0) {
                            break;
                        }
                        byte[] metaData = new byte[metaLength + 2];
                        metaData[0] = (byte) status;
                        metaData[1] = (byte) metaType;
                        child.readBuffer(metaData, 2, metaLength);

                        message = new SMFMessage(0, metaData);
                        break;
                    default:
                        message = null;
                        break;
                }
                break;
            default:
                message = null;
                break;
        }
        return message;
    }

    public void toStream(SMFOutputStream out, SMFMessage message) throws IOException {

        out.write8(message.getStatus());

        switch (message.getStatus() & 0xF0) {
            case 0x80:
            case 0x90:
            case 0xA0:
            case 0xB0:
            case 0xE0:
                out.write8(message.getData1());
                out.write8(message.getData2());
                break;
            case 0xC0:
            case 0xD0:
                out.write8(message.getData1());
                break;
            case 0xF0:
                // sys-ex or meta
                switch (message.getStatus()) {
                    case 0xF0:
                    case 0xF7:
                        byte[] binary = message.getBinary();
                        int length = binary.length;
                        out.writeVariable(length - 1);
                        for (int i = 1; i < binary.length; ++ i) {
                            out.write8(binary[i]);
                        }
                        break;

                    case 0xFF:
                        byte[] data = message.getBinary();
                        out.write8(message.getData1());
                        out.writeVariable(data.length - 2);
                        for (int i = 2; i < data.length; ++ i) {
                            out.write8(data[i]);
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    protected void writeMagicNumber(OutputStream stream, String magic) throws IOException {
        for (int i = 0; i < magic.length(); ++ i){
            char ch = magic.charAt(i);
            stream.write(ch & 0xff);
        }
    }

    protected void writeFile(File file) throws IOException {
        _file = file;
        SMFOutputStream output = new SMFOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        try {

            writeMagicNumber(output, "MThd");
            output.write32(6); //header length
            _infoType = 0;
            output.write16(_infoType);
            _infoTrackCount = 1;
            output.write16(_infoTrackCount);
            
            if (_smpteFormat < 0) {
                output.write16(_fileResolution);
            }
            else {
                output.write16((_fileResolution & 0xff) - (_smpteFormat << 8));
            }

            SortedArray<SMFMessage> list = _listMessage;

            writeMagicNumber(output, "MTrk");
            long lastTick = 0;

            for(SMFMessage message : _listMessage) {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                SMFOutputStream track = new SMFOutputStream(byteStream);

                long diffTick = message._tick - lastTick;
                if (diffTick <= 0) {
                    diffTick  = 0;
                }
                track.writeVariable(diffTick);
                toStream(track, message);

                //finish track
                int trackLength = byteStream.size();
                output.write32(trackLength);
                output.write(byteStream.toByteArray());
            }
        } finally {
            output.close();
        }
    }
}
