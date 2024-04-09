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
package jp.synthtarou.libs.smf;

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
import java.util.logging.Level;
import javax.sound.midi.ShortMessage;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.libs.SortedArray;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SMFParser {

    public static void main(String[] args) {
        File file = new File("C:\\midi\\SynthTAROU000.mid");
        try {
            SMFParser parse = new SMFParser(file);
            System.out.println(file);
        } catch (Exception ex) {
            MXFileLogger.getLogger(SMFParser.class).log(Level.WARNING, ex.getMessage(), ex);
        }
        ArrayList<String> skip = new ArrayList();
        skip.add("C:/Windows");
        String[] paths = {"C:/MIDI", "C:/Program Files", "C:/Users"};

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
                        countSmpte++;
                    } else {
                        countTempo++;
                    }
                } catch (Exception ex) {
                    MXFileLogger.getLogger(SMFParser.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
            System.out.println("SMPTE " + countSmpte + " / TEMPO " + countTempo);
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
                } catch (IOException ex) {
                    MXFileLogger.getLogger(SMFParser.class).log(Level.WARNING, ex.getMessage(), ex);
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
            } catch (IOException ex) {
                MXFileLogger.getLogger(SMFParser.class).log(Level.WARNING, ex.getMessage(), ex);
            }
            if (file.isDirectory()) {
                File[] childs = file.listFiles();
                directoryCount++;
                if ((directoryCount % 1000) == 0) {
                    System.out.println("hit " + midCount + " / total " + directoryCount);
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
                    midCount++;
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
    int _fileResolution = 480;
    SMFTempoArray _tempoArray;

    public SMFParser() {
        // as recorder
        _listMessage = new SortedArray<>();
    }

    public SMFParser(File file) throws IOException {
        try {
            if (!readFile(file)) {
                throw new IOException("Invalid MIDI File. " + file);
            }
        } catch (FileNotFoundException ioe) {
            throw ioe;
        } catch (IOException ioe) {
            throw ioe;
        }
    }

    protected boolean seekMagicNumber(SMFInputStream stream, String magic) {
        int x = 0;
        while (!stream._eof) {
            int c = stream.read8();
            if (c < 0) {
                break;
            }
            if (c == magic.charAt(x)) {
                x++;
                if (x >= magic.length()) {
                    return true;
                }
            } else {
                MXFileLogger.getLogger(SMFParser.class).warning("Seeking " + Integer.toHexString(c) + " before " + magic);
                x = 0;
            }
        }
        return false;
    }

    protected boolean readFile(File file) throws IOException {
        _file = file;
        InputStream input = new BufferedInputStream(new FileInputStream(file));
        try {
            SMFInputStream smfStream = new SMFInputStream(input);

            if (seekMagicNumber(smfStream, "MThd") == false) {
                MXFileLogger.getLogger(SMFParser.class).warning("Magic Number MThd not found. @" + file);
                return false;
            }

            if (smfStream._eof) {
                MXFileLogger.getLogger(SMFParser.class).warning("Illegual EOF. @" + file);
                return false;
            }

            int headerLength = smfStream.read32();
            if (headerLength < 6) {
                MXFileLogger.getLogger(SMFParser.class).warning("Header Length != 6.@" + file);
                return false;
            }

            int type = smfStream.read16();
            int trackCount = smfStream.read16();
            _infoType = type;
            _infoTrackCount = trackCount;
            int res = smfStream.read16();

            if (res >= 0x8000) {
                res = 0xffff0000 + res;
            }

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
                smfStream.skip(headerLength - 6);
            }

            if (type != 0 && type != 1) {
                throw new IllegalArgumentException("Unsupported SMF Type " + type + " is not 0 nor 1 @" + file);
            }

            SortedArray<SMFMessage> list = new SortedArray();

            for (int tr = 0; tr < trackCount; tr++) {
                if (seekMagicNumber(smfStream, "MTrk") == false) {
                    MXFileLogger.getLogger(SMFParser.class).warning("Magic Number MTrk not Found. @" + file);
                    break;
                }
                if (smfStream._eof) {
                    MXFileLogger.getLogger(SMFParser.class).warning("EOF Before count " + trackCount + "@" + file);
                    break;
                }

                int trackLength = smfStream.read32();

                if (trackLength <= 0) {
                    continue;
                }

                SMFStreamForTrack trackStream = new SMFStreamForTrack(smfStream, trackLength);
                long tick = 0;
                int fileOrder = 0;

                while (!trackStream._eof) {
                    long step = trackStream.readVariable();
                    tick += step;

                    SMFMessage message = fromStream(trackStream);
                    if (step == 0) {
                        fileOrder++;
                    } else {
                        fileOrder = 0;
                    }
                    if (message != null) {
                        message._tick = tick;
                        message._seqTrack = tr;
                        message._fileOrder = fileOrder;
                        list.insertSorted(message);
                    }
                }
            }

            _listMessage = new SortedArray<>();

            if (_smpteFormat > 0) {
                double frameRate = _smpteFormat;
                if (_smpteFormat == 29) {
                    frameRate = 29.97;
                }

                double tickPerMillisecond = frameRate * _fileResolution / 1000;

                for (SMFMessage seek : list) {
                    seek._millisecond = (long) (seek._tick / tickPerMillisecond);
                    _listMessage.add(seek);
                }
            } else {
                _tempoArray = new SMFTempoArray(this);
                long lastMillisecond = 0;
                for (SMFMessage seek : list) {
                    seek._millisecond = _tempoArray.calcMicrosecondByTick(seek._tick) / 1000;
                    lastMillisecond = seek._millisecond;
                    if (seek.getStatus() == 0xff && seek.getData1() == 0x51) {
                        _tempoArray.addMPQwithTick(seek.getMetaTempo(), seek._tick);
                    }
                    _listMessage.add(seek);
                }
            }
            parseAdditionalInfo();
            return !_listMessage.isEmpty();
        } finally {
            input.close();
        }
    }

    public SortedArray<SMFMessage> _listMessage;
    static int _runningStatus = 0;

    public SMFMessage fromStream(SMFStreamForTrack child) {
        SMFMessage message = null;

        int status = child.peek();
        if (status < 0x80) {
            status = _runningStatus;
        } else {
            status = child.read8();
            _runningStatus = status;
        }
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
                    case MXMidi.COMMAND_SYSEX:
                    case MXMidi.COMMAND_SYSEX_END:
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

                    case MXMidi.COMMAND_META_OR_RESET:
                        // meta
                        int metaType = child.read8();
                        int metaLength = (int) child.readVariable();
                        if (metaType < 0 || metaLength < 0) {
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
                    case MXMidi.COMMAND_SYSEX:
                    case MXMidi.COMMAND_SYSEX_END:
                        byte[] binary = message.getBinary();
                        int length = binary.length;
                        out.writeVariable(length - 1);
                        for (int i = 1; i < binary.length; ++i) {
                            out.write8(binary[i]);
                        }
                        break;

                    case MXMidi.COMMAND_META_OR_RESET:
                        byte[] data = message.getBinary();
                        out.write8(message.getData1());
                        out.writeVariable(data.length - 2);
                        for (int i = 2; i < data.length; ++i) {
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
        for (int i = 0; i < magic.length(); ++i) {
            char ch = magic.charAt(i);
            stream.write(ch & 0xff);
        }
    }

    void writeFile(File file, int port) throws IOException {
        _file = null;

        SortedArray<SMFMessage> list = new SortedArray<>();
        canonicalizeWithMillisecond();

        int count = 0;
        for (SMFMessage seek : _listMessage) {
            if (seek.isTempoMessage()) {
                list.insertSorted(seek);
            } else if (seek._port == port) {
                list.insertSorted(seek);
                count++;
            }
        }
        if (count == 0) {
            return;
        }

        SMFOutputStream output = new SMFOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        try {
            writeMagicNumber(output, "MThd");
            output.write32(6); //header length
            _infoType = 0;
            output.write16(_infoType);
            _infoTrackCount = 1;
            output.write16(_infoTrackCount);
            _smpteFormat = -1;
            output.write16(_fileResolution);

            writeMagicNumber(output, "MTrk");
            long lastTick = 0;

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            SMFOutputStream track = new SMFOutputStream(byteStream);
            for (SMFMessage seek : list) {

                long step = seek._tick - lastTick;
                if (step <= 0) {
                    step = 0;
                }
                track.writeVariable(step);
                toStream(track, seek);
                lastTick += step;
            }
            track.writeVariable(0);
            toStream(track, new SMFMessage(lastTick, new byte[]{(byte) 0xff, (byte) 0x2f, 0}));

            int trackLength = byteStream.size();
            output.write32(trackLength);
            output.write(byteStream.toByteArray());
        } finally {
            output.close();
        }
    }

    public void canonicalizeWithMillisecond() {
        _tempoArray = new SMFTempoArray(this);
        for (SMFMessage seek : _listMessage) {
            long millisecond = seek._millisecond;
            seek._tick = _tempoArray.calcTicksByMicrosecond(millisecond * 1000);
            if (seek.getStatus() == 0xff && seek.getData1() == 0x51) {
                _tempoArray.addMPQwithMicrosecond(seek.getMetaTempo(), millisecond * 1000);
            }
        }
    }

    public boolean[] _existNoteChannel;
    public int _firstNotePos;
    public int _noteLowest;
    public int _noteHighest;
    public ArrayList<Integer> _drums;
    public int[] _programList;

    public void parseAdditionalInfo() {
        _existNoteChannel = new boolean[16];
        _firstNotePos = -1;
        _noteLowest = 128;
        _noteHighest = -1;
        _drums = new ArrayList<Integer>();
        _programList = new int[16];
        for (int i = 0; i < _programList.length; ++i) {
            _programList[i] = -1;
        }

        for (int i = 0; i < _listMessage.size(); ++i) {
            SMFMessage smf = _listMessage.get(i);
            if (smf.isBinaryMessage()) {
                continue;
            }
            int msg = smf.toDwordMessage();

            int status = (msg >> 16) & 0xff;
            int ch = status & 0x0f;
            int data1 = (msg >> 8) & 0xff;
            int data2 = msg & 0xff;

            int command = status & 0xf0;

            if (command == MXMidi.COMMAND_CH_NOTEON) {
                if (_firstNotePos < 0) {
                    _firstNotePos = i;
                }
                _existNoteChannel[ch] = true;
                if (ch == MXConfiguration.DRUM_CH && data2 >= 1) {
                    _drums.add(data1);
                } else {
                    if (data1 < _noteLowest) {
                        _noteLowest = data1;
                    }
                    if (data1 > _noteHighest) {
                        _noteHighest = data1;
                    }
                }
            } else if (command == ShortMessage.PROGRAM_CHANGE) {
                if (_programList[ch] < 0) {
                    _programList[ch] = data1;
                }
            }
        }
    }
    
    public int calcMeasureByMilliseconds(long milli) {
        if (milli < 0) {
            return -1;
        }
        long micro = milli * 1000;
        long tick = _tempoArray.calcTicksByMicrosecond(micro);
        long measure = tick / _tempoArray._parent._fileResolution;
        return (int)measure;
    }
}
