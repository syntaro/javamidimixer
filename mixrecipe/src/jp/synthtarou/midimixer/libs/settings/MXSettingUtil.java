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
package jp.synthtarou.midimixer.libs.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXSettingUtil {
   public static boolean isInteger(String text) {
        for (int x = 0; x < text.length(); ++ x) {
            char ch = text.charAt(x);
            if (ch >= '0' && ch <= '9') {
                continue;
            }
            return false;
        }
        if (text.length() == 0) {
            return false;
        }
        return true;
    }
   
   public static File getAppBaseDirectory() {
        String fileName = null;
        try {
            ProtectionDomain pd =MXSetting.class.getProtectionDomain();
            CodeSource cs = pd.getCodeSource();
            URL location = cs.getLocation();
            URI uri = location.toURI();
            Path path = Paths.get(uri);
            fileName = path.toString();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();;
        }

        File base = new File(fileName);
        
        if (base.isFile()) {
            base = base.getParentFile();
        }
        return base;
    }

    public static File getSettingDirectory() {
        File dir = new File(getAppBaseDirectory(), "settings");
        if (dir.isDirectory()) {
            return dir;
        }

        try {
            Path p = Paths.get(dir.toURI());
            Files.createDirectory(p);
        }catch(IOException e) {
            e.printStackTrace();
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
    
    public static File autobackupFile(File target) {
        File parent = target.getParentFile();

        String fileName = target.getName();
        
        int lastDot = fileName.indexOf('.');
        String forward, fileExt;
        
        if (lastDot >= 0) {
            forward = fileName.substring(0, lastDot);
            fileExt = fileName.substring(lastDot);
        }else {
            forward = fileName;
            fileExt = "";
        }

        Date lastMod = new Date(target.lastModified());
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastMod);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);

        String newName1 = forward + "_back" + year + "-" + month + "-" + day;
        String newName3 = fileExt;

        for (int count = 1; count <= 99; ++ count) {
            String newName;
            if (count == 0) { 
                newName = newName1 + newName3;
            }else {
                String newName2 = "(" + count + ")";
                newName = newName1 + newName2 + newName3;
            }
            
            File f = new File(parent, newName);
            if (f.isFile()) {
                continue;
            }
            target.renameTo(f);
            return f;
        }
        return null;
    }
    
    public static boolean isFileContentsSame(File f1, File f2) {
        FileInputStream in1 = null, in2 = null;
        try {
            if (f1.isFile() == false || f2.isFile() == false) {
                return false;
            }
            in1 = new FileInputStream(f1);
            in2 = new FileInputStream(f2);
            
            byte[] buffer1 = new byte[4096];
            byte[] buffer2 = new byte[4096];
            
            while(true) {
                int x1 = in1.read(buffer1);
                int x2 = in2.read(buffer2);
                if (x1 != x2) {
                    return false;
                }
                if (x1 <= 0) {
                    break;
                }
                for (int i = 0; i < x1; ++ i) {
                    if (buffer1[i] != buffer2[i]) {
                        return false;
                    }
                }
            }

            return true;
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if (in1 != null) {
                try { in1.close(); } catch(IOException e) {}
            }
            if (in2 != null) {
                try { in2.close(); } catch(IOException e) {}
            }
        }
        return false;
    }
}
