/*
 * Copyright (C) 2024 Syntarou YOSHIDA
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
package jp.synthtarou.libs.json;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.libs.MXFileLogger;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.inifile.MXSettingUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXJsonFile {
    public MXJsonFile(String name) {
        _file = new File(getJsonDirectory(), name + ".json");
    }

    public MXJsonFile(File file) {
        _file = file;
    }
    
    public MXJsonValue readJsonFile() {
        try {
            _root = new MXJsonParser(_file)._treeRoot;
            return _root;
        } catch (Exception ex) {
            MXFileLogger.getLogger(MXJsonFile.class).log(Level.SEVERE, ex.toString(), ex);
            return null;
        }
    }
    
    public boolean writeJsonFile(MXJsonValue value) {
        String text = value.formatForDisplay();

        MXMain.progress("writing " + _file.getName());

        File target = MXUtil.createTemporaryFile(_file);
        boolean needMove = false;
        if (target != null) {
            needMove = true;
        } else {
            needMove = false;
            target = _file;
        }
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(target);
            BufferedOutputStream bout = new BufferedOutputStream(fout);
            OutputStreamWriter writer = new OutputStreamWriter(bout, "utf-8");
            writer.write(text);
            writer.flush();
            fout.close();
            fout = null;
            if (needMove) {
                _file.delete();
                target.renameTo(_file);
            }
            return true;
        } catch (IOException ioe) {
            MXFileLogger.getLogger(MXJsonFile.class).log(Level.SEVERE, ioe.toString(), ioe);
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException ioe) {
                    MXFileLogger.getLogger(MXJsonFile.class).log(Level.SEVERE, ioe.toString(), ioe);
                }
            }
        }
        return false;
    }

    protected File _file;
    protected MXJsonValue _root;
    static ArrayList<MXJsonFile> everySetting = new ArrayList();

    public static File getJsonDirectory() {
        File dir = new File(MXUtil.getAppBaseDirectory(), "json");
        if (dir.isDirectory()) {
            return dir;
        }

        try {
            Path p = Paths.get(dir.toURI());
            Files.createDirectory(p);
        } catch (IOException ex) {
            MXFileLogger.getLogger(MXSettingUtil.class).log(Level.WARNING, ex.getMessage(), ex);
        }

        if (dir.isDirectory()) {
            return dir;
        }

        return null;
    }
}
