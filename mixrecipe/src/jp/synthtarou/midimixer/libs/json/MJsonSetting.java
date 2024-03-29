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
package jp.synthtarou.midimixer.libs.json;

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
import jp.synthtarou.midimixer.libs.common.MXLogger2;
import jp.synthtarou.midimixer.libs.settings.MXSettingUtil;
import static jp.synthtarou.midimixer.libs.settings.MXSettingUtil.getAppBaseDirectory;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MJsonSetting {
    public MJsonSetting(String name) {
        _settingFile = new File(MXSettingUtil.getSettingDirectory(), name + ".json");
    }
    
    public MJsonValue readJson() {
        try {
            _root = new MJsonParser(_settingFile)._treeRoot;
        }catch(Exception ex) {
            _root = null;
        }
        return _root;
    }
    
    public boolean writeJson(MJsonValue value) {
        String text= value.formatForDisplay();

        File target = createTemporaryFile(_settingFile);
        boolean needMove = false;
        if (target != null) {
            needMove = true;
        }
        else {
            needMove = false;
            target = _settingFile;
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
                _settingFile.delete();
                target.renameTo(_settingFile);
            }
            return true;
        }catch(IOException ioe) {
            MXLogger2.getLogger(MJsonSetting.class).log(Level.SEVERE, ioe.toString(), ioe);
        }
        finally{
            if (fout != null) {
                try {
                    fout.close();
                }catch(IOException ioe) {
                    MXLogger2.getLogger(MJsonSetting.class).log(Level.SEVERE, ioe.toString(), ioe);
                }
            }
        }
        return false;
    }

    protected File _settingFile;
    protected MJsonValue _root;
    static ArrayList<MJsonSetting> everySetting = new ArrayList();

    public File getFile() {
        return _settingFile;
    }

    public static File getJsonDirectory() {
        File dir = new File(getAppBaseDirectory(), "json");
        if (dir.isDirectory()) {
            return dir;
        }

        try {
            Path p = Paths.get(dir.toURI());
            Files.createDirectory(p);
        }catch(IOException ex) {
            MXLogger2.getLogger(MXSettingUtil.class).log(Level.WARNING, ex.getMessage(), ex);
        }
        
        if (dir.isDirectory()) {
            return dir;
        }

        return null;
    }
    
    public static File createTemporaryFile(File target) {
        File dir = target.getParentFile();
        String fileName = target.getName();
        for (int i = 1; i < 100; ++ i) {
            String newName = fileName + "_temporary" + String.valueOf(i);
            File newFile = new File(dir, newName);
            if (newFile.exists()) {
                continue;
            }
            try {
                new FileOutputStream(newFile).close();;
            }catch(IOException e) {
                continue;
            }
            return newFile;
        }
        return null;
    }
}
