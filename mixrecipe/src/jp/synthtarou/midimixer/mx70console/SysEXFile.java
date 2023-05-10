/*
 * Copyright (C) 2023 Syntarou YOSHIDA
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
package jp.synthtarou.midimixer.mx70console;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXThreadList;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXTiming;
import jp.synthtarou.midimixer.libs.midi.port.FinalMIDIOut;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOut;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIOutManager;
import jp.synthtarou.midimixer.libs.midi.smf.ByteReader;
import jp.synthtarou.midimixer.libs.text.MXLineReader;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SysEXFile {
    ArrayList<byte[]> _contents = new ArrayList();
    
    public SysEXFile() {
    }            
    
    public boolean readText(File file) {
        MXLineReader in = null;
        FileInputStream fin = null;

        try {
            fin = new FileInputStream(file);
            in = new MXLineReader(fin);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                
                int num = 0;
                int readed = 0;
                for (int i = 0; i < line.length(); ++ i) {
                    char ch = line.charAt(i);
                    if (ch >= '0' && ch <= '9') {
                        num = num * 16;
                        num += ch - '0';
                        readed ++;
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = num * 16;
                        num += ch - 'a' + 10;
                        readed ++;
                    }
                    else if (ch >= 'A' && ch <= 'F') {
                        num = num * 16;
                        num += ch - 'A' + 10;
                        readed ++;
                    }
                    else {
                        if (readed >= 1) {
                            out.write(num);
                            num = 0;
                            readed = 0;
                        }
                    }
                }
                if (readed >= 1) {
                    out.write(num);
                    num = 0;
                    readed = 0;
                }
            }
            setContents(out.toByteArray());
            fin.close();
            fin = null;
            return true;
        }catch(IOException ex) {
            if (fin != null) {
                try { fin.close(); } catch(IOException e2) {} 
            }
            return false;
        }
    }

    public boolean readBinary(File file) {
        MXLineReader in = null;
        FileInputStream fin = null;

        try {
            fin = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] chunk = new byte[256];
            
            while (true) {
                int x = fin.read(chunk);
                if (x >= 1) {
                    out.write(chunk, 0, x);
                }else {
                    break;
                }
            }
            setContents(out.toByteArray());
            fin.close();
            fin = null;
            return true;
        }catch(IOException ex) {
            if (fin != null) {
                try { fin.close(); } catch(IOException e2) {} 
            }
            return false;
        }
    }
    
    public void setContents(byte[] data) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteReader reader = new ByteReader(in);
        _contents.clear();
        int error = 0;
        while (true) {
            int sig = reader.read8();
            if (sig < 0) {
                break;
            }
            if (sig == 0xf0 || sig == 0xf7) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(sig);
                do {
                    sig = reader.read8();
                    if (sig < 0) {
                        break;
                    }
                    out.write(sig);
                }while (sig != 0xf0 && sig != 0xf7);

                byte[] segment = out.toByteArray();
                _contents.add(segment);
            }else {
                error ++;
            }
        }
    }
    
    public void add(byte[] data, JTextArea area) {
        _contents.add(data);
        bind(area);
    }

    public void bind(JTextArea area) {
        StringBuffer text = new StringBuffer();
        int  count = 1; 
        for (byte[] data : _contents) {
            text.append(count + " = " + data.length + " bytes)\n");
            count ++;
            text.append(MXUtil.dumpHexFF(data));
            text.append("\n");
        }
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setText(text.toString());
    }
    
    public void clear(JTextArea area) {
        _contents.clear();
        area.setText("");
    }
    
    public boolean writeText(File file) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(file);
            BufferedOutputStream bout = new BufferedOutputStream(fout);
            OutputStreamWriter write = new OutputStreamWriter(bout);
            for (byte[] data : _contents) {
                for (int x = 0; x < data.length; ++ x) {
                    String hex = MXUtil.toHexFF(data[x]);
                    if (x != 0) {
                        write.write(" ");
                    }
                    write.write(hex);
                }
                write.write("\n");
            }
            write.flush();
            fout.close();
            return true;
        }catch(IOException e) {
            if (fout != null) {
                try { fout.close(); } catch(IOException ex) {}
                file.delete();
            }
            return false;
        }
    }
    
    public boolean  writeBinary(File file) throws IOException {
        FileOutputStream fout = new FileOutputStream(file);
        BufferedOutputStream bout = new BufferedOutputStream(fout);
        try {
            for (byte[] data : _contents) {
                bout.write(data);
            }
            bout.flush();
            return true;
        }catch(IOException e) {
            if (fout != null) {
                try { fout.close(); } catch(Exception ex) { }
                file.delete();
            }
            return false;
        }
    }
    
    public void sendTo(int port, SysexProgress progress) {
        MXThreadList.newThread("SysEXFile", new Runnable() {
            @Override
            public void run() {
                int count = _contents.size();
                int x = 0;
                FinalMIDIOut out = FinalMIDIOut.getInstance();
                for (byte[] data : _contents) {
                    progress.progress(x ++ , count);
                    MXMessage longMessage = MXMessageFactory.fromSysexMessage(port, data[0] & 0xff, data);
                    MXMain.getMain().messageDispatch(longMessage, out);
                    try {
                        Thread.sleep(1000);
                    }catch(Exception e) {
                        
                    }
                }
                progress.progress(x, count);
            }
        }).start();;
    }
    
    public static final int ID_NOTHING = 0;
    public static final int ID_AMERICAN_1 = 1;
    public static final int ID_EUROPEAN_1 = 2;
    public static final int ID_JAPANESE_1 = 3;
    public static final int ID_OTHER_1 = 4;

    public static final int ID_SPECIAL_1 = 5;
    public static final int ID_AMERICAN_3 = 6;
    public static final int ID_EUROPEAN_3 = 7;
    public static final int ID_JAPANESE_3 = 8;
    public static final int ID_OTHER_3 = 9;

    public static final int ID_UNKNOWN_3 = 10;
    
    public static final String HANDSHAKE_ACK_TEXT = "F0 7E @Device 7F @Packet F7";
    public static final String HANDSHAKE_NAK_TEXT = "F0 7E @Device 7F @Packet F7";
    public static final String HANDSHAKE_CANCEL_TEXT = "F0 7E @Device 7D @Packet F7";
    public static final String HANDSHAKE_WAIT_TEXT = "F0 7E @Device 7C @Packet F7";
    public static final String HANDSHAKE_EOF_TEXT = "F0 7E @Device 7B @Packet F7";

    public int handShakeDevice;
    public int handShakeMessage;
    public int handShakePacket;

    public static final int HANDSHAKE_ACK = 0x7f;
    public static final int HANDSHAKE_NAK = 0x7F;
    public static final int HANDSHAKE_CANCEL = 0x7D;
    public static final int HANDSHAKE_WAIT = 0x7C;
    public static final int HANDSHAKE_EOF = 0x7B;

    public int isHandShakeMesage(byte[] data) {
        if (data.length == 6) {
            if (data[0] == 0xf0 && data[1] == 0x7e) {
                int device = data[2];
                int message = data[3];
                int packet = data[4];
                if (data[5] == 0xf7) {
                    handShakeDevice = device;
                    handShakeMessage = message;
                    handShakePacket = packet;
                    return handShakeMessage;
                }
            }
        }
        return -1;
    }
    
    public boolean isMaybeFinish(byte[] data) {
        if (data[data.length - 1] == 0xf7) {
            return true;
        }
        return false;
    }

/*    
    public List<byte[]>splitMessage(byte[] completeData, int limit) {
        ArrayList<byte[]> ret = new ArrayList();
        int start = completeData[0] & 0xff;
        int finish = completeData[completeData.length - 1] & 0xff;
        if (true) {
            System.out.println("Split= " + completeData.length + "<<" +  MXUtil.dumpHexFF(completeData));
            if (start == 0xf0 && finish == 0xf7) {
                ByteArrayInputStream bin = new ByteArrayInputStream(completeData);

                boolean itsStart = true;
                int x = 0;
                byte[] data = new byte[limit];
                int makerlen = 1, maker1 = 0, maker2 = 0, maker3 = 0;

                int total = 0;
                while(true) {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();

                    if (itsStart) {
                        int fetch = bin.read(data, 0, limit);
                        if (fetch <= 0) {
                            break;
                        }
                        maker1 = data[0];
                        if (maker1 == 0) {
                            makerlen = 3;
                            maker2 = data[1];
                            maker3 = data[2];
                        }
                        total += fetch;
                        bout.write(data, 0, fetch);
                        itsStart = false;
                    }else if (false) {
                        int fetch = bin.read(data, 0, limit - 1 - makerlen);
                        if (fetch <= 0) {
                            break;
                        }
                        total += fetch;
                        bout.write(0xf7);
                        if (makerlen == 1) {
                            bout.write(maker1);
                        }else {
                            bout.write(maker1);
                            bout.write(maker2);
                            bout.write(maker3);
                        }
                        bout.write(data, 0, fetch);
                    }else {
                        int fetch = bin.read(data, 0, limit - 1);
                        if (fetch <= 0) {
                            break;
                        }
                        total += fetch;
                        bout.write(0xf7);
                        bout.write(data, 0, fetch);
                    }
                    ret.add(bout.toByteArray());
                }
                if (total != completeData.length) {
                    System.err.println("Length Not Match");
                }else {
                    System.err.println("Length Ok");
                }
                return ret;
            }

            
        }else {

            int maker1 = completeData[1];
            int maker2 = completeData[2];
            int maker3 = completeData[3];
            int headerLength = 1;
            if (maker1 == 0) {
                headerLength += 3;
            }else {
                headerLength += 1;
            }
            System.out.println("Split= " + completeData.length + "<<" +  MXUtil.dumpHexFF(completeData));
            if (start == 0xf0 && finish == 0xf7) {
                boolean itsStart = true;
                int x = headerLength;
                while (x < completeData.length) {
                    int segmentSize = completeData.length - x;
                    if (segmentSize +1 >= limit) {
                        segmentSize = limit - 1;
                    }
                    byte[] sub;
                    if (itsStart) {
                        if (headerLength == 4) {
                            sub = new byte[4 + segmentSize];
                            sub[0] = (byte)(itsStart ? 0xf0 : 0xf7);
                            sub[1] = (byte)maker1;
                            sub[2] = (byte)maker2;
                            sub[3] = (byte)maker3;
                            for (int i = 0; i < segmentSize; ++ i) {
                                sub[4 + i] = completeData[x + i];
                            }
                        }else if (headerLength == 2) {
                            sub = new byte[2 + segmentSize];
                            sub[0] = (byte)(itsStart ? 0xf0 : 0xf7);
                            sub[1] = (byte)maker1;
                            for (int i = 0; i < segmentSize; ++ i) {
                                sub[2 + i] = completeData[x + i];
                            }
                        }else {
                            System.out.println("Unknown error");
                            break;
                        }
                    }else {
                        sub = new byte[1 + segmentSize];
                        sub[0] = (byte)(itsStart ? 0xf0 : 0xf7);
                        for (int i = 0; i < segmentSize; ++ i) {
                            sub[1 + i] = completeData[x + i];
                        }
                    }
                    itsStart = false;
                    ret.add(sub);
                    x += segmentSize;
                }
                return ret;
            }
        }
        return null;
    }
*/
    public void parseTest1(byte[] data) {
        /* F0H , ID (1-3), Device, Sub1, Sub2, ... F7H  */
        int x = 0;
        try {
            int start = data[x++];
            if (start == 0xf0 || start == 0xf7) {
                int maker1 = data[x++];
                int maker2 = 0;
                int maker3 = 0;
                int id = ID_NOTHING;
                if (maker1 >= 01 && maker1 <= 0x1f) {
                    id = ID_AMERICAN_1;
                }
                else if (maker1 >= 0x20 && maker1 <= 0x3f) {
                    id = ID_EUROPEAN_1;
                }
                else if (maker1 >= 0x40 && maker1 <= 0x5f) {
                    id = ID_JAPANESE_1;
                }
                else if (maker1 >= 0x60 && maker1 <= 0x7c) {
                    id = ID_OTHER_1;
                }
                else if (maker1 >= 0x7d && maker1 <= 0x7f) {
                    id = ID_SPECIAL_1;
                }
                else if (maker1 == 0x00) {
                    maker2 = data[x++];
                    maker3 = data[x++];
                    if (maker2 >= 01 && maker2 <= 0x1f) {
                        id = ID_AMERICAN_3;
                    }
                    else if (maker2 >= 0x20 && maker2 <= 0x3f) {
                        id = ID_EUROPEAN_3;
                    }
                    else if (maker2 >= 0x40 && maker2 <= 0x5f) {
                        id = ID_JAPANESE_3;
                    }
                    else if (maker2 >= 0x60 && maker2 <= 0x7f) {
                        id = ID_OTHER_3;
                    }
                }
                else {
                    maker2 = data[x++];
                    maker3 = data[x++];
                    id = ID_UNKNOWN_3;
                }
            }
            int device = data[x++];
            if (device == 0x7f) {
                // ALL CALL
            }
        }catch(IndexOutOfBoundsException ex) {
            return ;
        }
    }
}
