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
import javax.swing.JTextArea;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.MXThreadList;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.driver.SysexSplitter;
import jp.synthtarou.midimixer.libs.midi.port.FinalMIDIOut;
import jp.synthtarou.midimixer.libs.midi.smf.MidiByteReader;
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
            setContentsFromSingleFile(out.toByteArray());
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
            setContentsFromSingleFile(out.toByteArray());
            System.out.println("Read bininary size=" + out.size());
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
    
    public void setContentsFromSingleFile(byte[] data) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        MidiByteReader reader = new MidiByteReader(in);
        ArrayList<byte[]> temp = new ArrayList();

        int error = 0;
        while (true) {
            int sig = reader.peek8();
            if (sig < 0) {
                break;
            }
            if (sig != 0xf0) {
                System.err.println("Error " + MXUtil.toHexFF(sig));
                reader.read8();
                continue;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(sig);
            while(true) {
                int b = reader.read8();
                if (b < 0) {
                    break;
                }
                if (b == 0xf0 || b == 0xf7) {
                    byte[] segment = out.toByteArray();
                    temp.add(segment);
                    out = new ByteArrayOutputStream();
                }
                out.write(b);
            }
            byte[] segment = out.toByteArray();
            temp.add(segment);
        }

        for (int i = 0; i < temp.size(); ++ i) {
            byte[] line = temp.get(i);
            if (line.length == 0) {
                temp.remove(i);
                -- i;
                continue;
            }
            if ((line[0] & 0xff) == 0xf0) {
                if (line.length == 1) {
                    if (i + 1 >= temp.size()) {
                        continue;
                    }
                    byte[] next = temp.get(i + 1);
                    byte[] newNext = new byte[next.length + 1];
                    newNext[0] = line[0];
                    System.arraycopy(next, 0, newNext, 1, next.length);
                    temp.set(i, newNext);
                    temp.remove(i + 1);
                    i --;
                    continue;
                }
            }
            if ((line[0] & 0xff) == 0xf7) {
                if (line.length == 1) {
                    byte[] prev = temp.get(i - 1);
                    byte[] newPrev = new byte[prev.length + 1];
                    System.arraycopy(prev, 0, newPrev, 0, prev.length);
                    newPrev[newPrev.length - 1] = line[0];
                    temp.set(i - 1, newPrev);
                    temp.remove(i);
                    -- i;
                    continue;
                }
            }
        }
        
        _contents = temp;
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
                    int pos = x % 64;
                    if (pos != 0) {
                        write.write(" ");
                    }
                    else if (x != 0) {
                        write.write("\n");
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
    
    public void sendSysexTo(int port, SysexProgress progress, int splitSize) {
        MXThreadList.newThread("SysEXFile", new Runnable() {
            @Override
            public void run() {
                int count = _contents.size();
                int x = 0;
                FinalMIDIOut out = FinalMIDIOut.getInstance();

                for (byte[] data : _contents) {
                    progress.progress(x ++ , count);

                    SysexSplitter split = new SysexSplitter();
                    split.append(data);
                    ArrayList<byte[]> arrayData = split.splitOrJoin(0 /* splitSize*/);
                    for (byte[] data2 : arrayData) {
                        MXMessage longMessage = MXMessageFactory.fromBinary(port, data2);
                        MXMain.getMain().messageDispatch(longMessage, out);
                    }
                    try {
                        Thread.sleep(100);
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
