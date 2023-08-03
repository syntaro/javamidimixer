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
package jp.synthtarou.midimixer.libs.vst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.TreeMap;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class IndexedFile {
    Comparator<Integer> comparator = new Comparator<Integer>() {
        public int compare(Integer int1, Integer int2) {
            return (int1.compareTo(int2));
        }
    };
    
    public File getBaseDir() {
        return _baseDir;
    }

    TreeMap<Integer, String> _listEntry = new TreeMap(comparator);
    
    File _baseDir;
    File _indexFile;
    File _indexLockFile;
    File _indexTempFile;
    FileOutputStream _lock;
    
    public IndexedFile(File baseDir)  {
        _baseDir = baseDir;
        _indexTempFile = new File(_baseDir, "MXIndex.tmp");
        _indexLockFile = new File(_baseDir, "MXIndex.lockfile");
        _indexFile = new File(_baseDir, "MXIndex");
    }
    
    public synchronized boolean getLock() {
        if (_lock == null) {
            try {
                _lock = new FileOutputStream(_indexLockFile);
                readIndexFile();
                return true;
            }catch(IOException e) {
            }
        }
        else {
            return true;
        }
        return false;
    }
    
    public synchronized void releaseLock() {
        if (_lock != null) {
            try {
                _lock.close();
            }catch(IOException e) {
            }
        }
    }
    
    public synchronized String getTitle(int index) {
        if (_lock == null) {
            throw new IllegalStateException("getLock before Operation");
        }
        String title = _listEntry.get(index);
        return title;
    }
    
    public synchronized void setTitle(int index, String title) {
        if (_lock == null) {
            throw new IllegalStateException("getLock before Operation");
        }
        _listEntry.put(index, title);
        writeIndexFile();
    }
    
    public synchronized File getPath(int file) {
        String fileName = "0000000" + String.valueOf(file);
        if (fileName.length() > 8) {
            fileName = fileName.substring(fileName.length() - 8);
        }
        return new File(_baseDir, fileName);
    }

    public synchronized boolean readIndexFile() {
        if (_lock == null) {
            throw new IllegalStateException("getLock before Operation");
        }
        _listEntry.clear();

        File[] children = _baseDir.listFiles();
        if (children != null) {
            for (File f : children) {
                String title = f.getName();
                int fileX;

                try {
                    fileX = Integer.parseInt(title);
                }
                catch(NumberFormatException e) {
                    continue;
                }

                //System.out.println("fileIndex " + fileX + " = " + title);
                _listEntry.put(fileX, title);
            }
        }

        BufferedReader reader = null;
        try {
            Charset utf8 = Charset.forName("utf-8");
            reader = new BufferedReader(new FileReader(_indexFile, utf8));
            while(true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                int splitPos = line.indexOf("=");
                if (splitPos < 0) {
                    continue;
                }
                
                String file = line.substring(0, splitPos);
                String title = line.substring(splitPos + 1);
                int fileX = -1;
                
                try {
                    fileX = Integer.parseInt(file);
                }catch(NumberFormatException e) {
                    continue;
                }

                _listEntry.put(fileX, title);
                //System.out.println("putIndex " + fileX + " = " + title);
            }
            return true;
        }catch(IOException e) {
        }finally{
            try { 
                if (reader != null) {
                    reader.close();
                }
            }
            catch(IOException e) {
            }
        }
        return false;
    }

    public synchronized boolean writeIndexFile() {
        if (_lock == null) {
            throw new IllegalStateException("getLock before Operation");
        }
        Charset utf8 = Charset.forName("utf-8");
        FileWriter fwrite = null;
        BufferedWriter bwrite = null;
        try {
            fwrite = new FileWriter(_indexTempFile, utf8);
            bwrite = new BufferedWriter(fwrite);
            for (Integer file : _listEntry.keySet()) {
                String title = _listEntry.get(file);
                File f = getPath(file);
                bwrite.write(f.getName() + "=" + title + "\n");
            }
            bwrite.flush();
            bwrite.close();
            _indexFile.delete();
            _indexTempFile.renameTo(_indexFile);
            return true;
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if (bwrite != null) {
                try {
                    bwrite.close();
                }catch(IOException e) {
                }
                bwrite = null;
                if (fwrite != null) {
                    try {
                        fwrite.close();
                    }catch(IOException e) {
                    }
                    fwrite = null;
                }
            }
        }
        _indexTempFile.delete();
        return false;
    }

    public synchronized int findTitle(String title) {
        if (_lock == null) {
            throw new IllegalStateException("getLock before Operation");
        }
        for (Integer n : _listEntry.keySet()) {
            String name = _listEntry.get(n);
            if (name.equalsIgnoreCase(title)) {
                return n;
            }
        }
        return -1;
    }
    
    public synchronized int newFileEntry(String title) {
        if (_lock == null) {
            new IllegalStateException("getLock before Operation");
        }
        int next = _listEntry.size() + 1;
        while (true) {
            File f = getPath(next);
            if (!f.exists()) {
                _listEntry.put(next, title);
                writeIndexFile();
                return next;
            }
            next ++;
            if (next >= 100000000) {
                return -1;
            }
        }
    }
}
