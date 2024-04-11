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
import jp.synthtarou.libs.MXSafeThread;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.sysex.SysexSplitter;
import jp.synthtarou.midimixer.libs.midi.port.FinalMIDIOut;
import jp.synthtarou.libs.smf.SMFInputStream;
import jp.synthtarou.libs.MXLineReader;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.libs.midi.port.MXMIDIIn;

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
        SMFInputStream reader = new SMFInputStream(in);
        ArrayList<byte[]> temp = new ArrayList();

        int error = 0;
        while (true) {
            int sig = reader.peek8();
            if (sig < 0) {
                break;
            }
            if (sig != 0xf0) {
                MXFileLogger.getLogger(SysEXFile.class).severe("Error " + MXUtil.toHexFF(sig));
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
            text.append(MXUtil.dumpHex(data));
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
        new MXSafeThread("SysEXFile", new Runnable() {
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
                        MXMIDIIn.messageToReceiverThreaded(longMessage, out);
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
}
