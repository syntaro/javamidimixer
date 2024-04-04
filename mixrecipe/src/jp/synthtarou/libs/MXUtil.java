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
package jp.synthtarou.libs;

import jp.synthtarou.libs.log.MXFileLogger;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.color.ColorSpace;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.libs.inifile.MXSettingUtil;
import jp.synthtarou.midimixer.MXConfiguration;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXUtil {

    public static String toHexFF(int i) {
        String str = Integer.toHexString(i).toUpperCase();
        if (str.length() == 1) {
            return "0" + str;
        }
        /*
        if (str.length() >= 3) {
            return str.substring(str.length() - 2, str.length());
        }*/
        return str;
    }

    public static String dumpHex(byte[] data) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < data.length; ++i) {
            if (i != 0) {
                str.append(" ");
            }
            str.append(toHexFF(data[i] & 0xff));
        }
        return str.toString();
    }

    public static String dumpDword(int dword) {
        byte[] data = new byte[4];
        data[0] = (byte)((dword >> 24) & 0xff);
        data[1] = (byte)((dword >> 16) & 0xff);
        data[2] = (byte)((dword >> 8) & 0xff);
        data[3] = (byte)((dword) & 0xff);
        return dumpHex(data);
    }

    public static String dumpHex(int[] data) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < data.length; ++i) {
            if (i != 0) {
                str.append(" ");
            }
            str.append(toHexFF(data[i]));
        }
        return str.toString();
    }

    public static boolean isNumberFormat(String text) {
        try {
            if (MXUtil.numberFromText(text, Integer.MIN_VALUE) == Integer.MIN_VALUE) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public static final int numberFromText(String text) {
        return MXUtil.numberFromText(text, 0);
    }

    public static final int numberFromText(String text, int errorNumber) {
        int mum = 10;
        boolean negative = false;

        if (text == null) {
            return errorNumber;
        }
        if (text.startsWith("-")) {
            negative = true;
            text = text.substring(1);
        }
        if (text.startsWith("0x")) {
            text = text.substring(2);
            mum = 16;
        }
        if (text.endsWith("h") || text.endsWith("H")) {
            text = text.substring(0, text.length() - 1);
            mum = 16;
        }

        int start = 0;
        int end = text.length();

        if (start >= end) {
            return errorNumber;
        }

        int x = 0;
        for (int pos = start; pos < end; ++pos) {
            int ch = text.charAt(pos);
            if (ch >= '0' && ch <= '9') {
                x *= mum;
                x += ch - (char) '0';
            } else if (ch >= 'A' && ch <= 'F' && mum == 16) {
                x *= mum;
                x += ch - (char) 'A' + 10;
            } else if (ch >= 'a' && ch <= 'f' && mum == 16) {
                x *= mum;
                x += ch - (char) 'a' + 10;
            } else {
                return errorNumber;
            }
        }
        if (negative) {
            return -x;
        }
        return x;
    }

    public static boolean searchTextIgnoreCase(String text, String words) {
        text = text.toLowerCase();
        words = words.toLowerCase();
        if (words.indexOf(' ') < 0) {
            return text.indexOf(words) >= 0;
        }
        ArrayList<String> cells = new ArrayList();
        split(words, cells, ' ');
        for (String parts : cells) {
            if (text.indexOf(parts) < 0) {
                return false;
            }
        }
        return true;
    }

    public static void swingTreeEnable(Component c, boolean enable) {
        if (c instanceof Container) {
            Container parent = (Container) c;
            int count = parent.getComponentCount();
            for (int x = 0; x < count; ++x) {
                swingTreeEnable(parent.getComponent(x), enable);
            }
        }
        if (c instanceof JComponent) {
            ((JComponent) c).setEnabled(enable);
        }
    }

    public static void showAsDialog(Container parent, JPanel panel, String title) {
        Container cont = getOwnerWindow(parent);
        if (title == null) {
            title = MXConfiguration.MX_APPLICATION;
        }
        JDialog dialog = null;
        if (cont instanceof Window) {
            Window W = (Window) cont;
            dialog = new JDialog(W, title);
        } else if (cont instanceof Dialog) {
            Dialog D = (Dialog) cont;
            dialog = new JDialog(D, title);
        } else {
            dialog = new JDialog((Window) parent, title);
        }
        dialog.setModal(true);
        dialog.getContentPane().add(panel, "Center");
        dialog.pack();
        centerWindow(dialog);
        panel.requestFocusInWindow();

        dialog.setVisible(true);
    }

    public static void centerWindow(Component c) {
        Component owner = (c instanceof Window) ? ((Window) c).getOwner() : null;
        if (owner != null && !owner.isVisible()) {
            owner = null;
        }
        Component parent = (owner != null) ? owner : c.getParent();
        if (parent != null && !parent.isVisible()) {
            parent = null;
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension mySize = c.getSize();
        Point loc = new Point(0, 0);
        if (parent != null) {
            screenSize = parent.getSize();
            if (parent == owner) {
                loc = owner.getLocation();
            }
        }
        loc.x += (screenSize.width - mySize.width) / 2;
        loc.y += (screenSize.height - mySize.height) / 2;
        c.setLocation(loc);
    }

    public static Window getOwnerWindow(Component panel) {
        while (panel != null) {
            if (panel instanceof Window) {
                return (Window) panel;
            }
            if (panel instanceof Dialog) {
                return (Dialog) panel;
            }
            panel = panel.getParent();
        }
        return null;
    }

    public static void split(String str, ArrayList<String> list, char splitter) {
        list.clear();
        int len = str.length();
        int from = 0;
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (ch == splitter) {
                list.add(str.substring(from, i));
                from = i + 1;
                continue;
            }
        }
        if (from < len) {
            list.add(str.substring(from, len));
        }
    }

    public static Color mixtureColor(Color left, int leftPercent, Color right, int rightPercent) {
        int totalPercent = leftPercent + rightPercent;
        int lr = (int) (left.getRed() * leftPercent / totalPercent);
        int lg = (int) (left.getGreen() * leftPercent / totalPercent);
        int lb = (int) (left.getBlue() * leftPercent / totalPercent);
        int rr = (int) (right.getRed() * rightPercent / totalPercent);
        int rg = (int) (right.getGreen() * rightPercent / totalPercent);
        int rb = (int) (right.getBlue() * rightPercent / totalPercent);
        return new Color(lr + rr, lg + rg, lb + rb, 255);
    }
    
    public static Color mixtureColor(Color left, int leftPercent, Color center, int centerPercent, Color right, int rightPercent) {
        int totalPercent = leftPercent + centerPercent + rightPercent;
        int lr = (int) (left.getRed() * leftPercent / totalPercent);
        int lg = (int) (left.getGreen() * leftPercent / totalPercent);
        int lb = (int) (left.getBlue() * leftPercent / totalPercent);
        int cr = (int) (center.getRed() * centerPercent / totalPercent);
        int cg = (int) (center.getGreen() * centerPercent / totalPercent);
        int cb = (int) (center.getBlue() * centerPercent / totalPercent);
        int rr = (int) (right.getRed() * rightPercent / totalPercent);
        int rg = (int) (right.getGreen() * rightPercent / totalPercent);
        int rb = (int) (right.getBlue() * rightPercent / totalPercent);
        return new Color(lr + cr + rr, lg + cg + rg, lb + cb + rb, 255);
    }

    public static boolean isShrinkTarget(char c) {
        if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
            return true;
        }
        return false;
    }

    public static String shrinkText(String text) {
        if (text == null) {
            return null;
        }
        if (text.length() == 0) {
            return text;
        }
        int start = 0;
        int end = text.length() - 1;
        while (start <= end && isShrinkTarget(text.charAt(start))) {
            start++;
        }
        while (start <= end && isShrinkTarget(text.charAt(end))) {
            end--;
        }
        if (start > end) {
            return "";
        }
        return text.substring(start, end + 1);
    }


   public static File getAppBaseDirectory() {
        String fileName = null;
        try {
            ProtectionDomain pd =MXINIFile.class.getProtectionDomain();
            CodeSource cs = pd.getCodeSource();
            URL location = cs.getLocation();
            URI uri = location.toURI();
            Path path = Paths.get(uri);
            fileName = path.toString();
        } catch (URISyntaxException ex) {
            MXFileLogger.getLogger(MXSettingUtil.class).log(Level.WARNING, ex.getMessage(), ex);
            return new File(".");
        }

        File base = new File(fileName);
        if (base.isFile()) {
            base = base.getParentFile();
        }
        return base;
    }

    public static File createTemporaryFile(File target) {
        File dir = target.getParentFile();
        dir.mkdirs();
        String fileName = target.getName();
        for (int i = 1; i < 100; ++i) {
            String newName = fileName + "_temporary" + String.valueOf(i);
            File newFile = new File(dir, newName);
            if (newFile.exists()) {
                continue;
            }
            try {
                new FileOutputStream(newFile).close();
            } catch (IOException e) {
                continue;
            }
            return newFile;
        }
        return null;
    }

    public static File safeRenameToBackup(File target) {
        File parent = target.getParentFile();

        parent = new File(parent, "Old");
        parent.mkdir();

        String fileName = target.getName();

        int lastDot = fileName.indexOf('.');
        String forward, fileExt;

        if (lastDot >= 0) {
            forward = fileName.substring(0, lastDot);
            fileExt = fileName.substring(lastDot);
        } else {
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

        for (int count = 1; count <= 99; ++count) {
            String newName;
            if (count == 0) {
                newName = newName1 + newName3;
            } else {
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

    public static int compareFileText(File f1, File f2) {
        MXLineReader r1 = null, r2 = null;
        InputStream i1 = null, i2 = null;

        try {
            if (!f1.exists() || !f2.exists()) {
                return -1;
            }
            i1 = new FileInputStream(f1);
            i2 = new FileInputStream(f2);

            r1 = new MXLineReader(i1, "utf-8");
            r2 = new MXLineReader(i2, "utf-8");

            while (true) {
                String line1 = "", line2 = "";

                while (line1 != null && line1.isEmpty()) {
                    line1 = r1.readLine();
                    line1 = shrinkText(line1);
                }

                while (line2 != null && line2.isEmpty()) {
                    line2 = r2.readLine();
                    line2 = shrinkText(line2);
                }

                if (line1 == null) {
                    if (line2 == null) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
                if (line2 == null) {
                    return 1;
                }

                int x = line1.compareTo(line2);
                if (x != 0) {
                    return x;
                }
            }
        } catch (IOException ex) {
            MXFileLogger.getLogger(MXUtil.class).log(Level.WARNING, ex.getMessage(), ex);
            return -1;
        } finally {
            if (i1 != null) {
                try {
                    i1.close();
                } catch (IOException ex) {
                    MXFileLogger.getLogger(MXUtil.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
            if (i2 != null) {
                try {
                    i2.close();
                } catch (IOException ex) {
                    MXFileLogger.getLogger(MXUtil.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
    }

    public static int compareFileBinary(File f1, File f2) throws IOException {
        long l1 = f1.length();
        long l2 = f2.length();
        if (l1 < l2) {
            return -1;
        }
        if (l1 > l2) {
            return -1;
        }

        byte[] data1 = new byte[4096];
        byte[] data2 = new byte[4096];
        FileInputStream in1 = null;
        FileInputStream in2 = null;

        try {
            in1 = new FileInputStream(f1);
            in2 = new FileInputStream(f2);

            while (true) {
                int len1 = in1.read(data1);
                int len2 = in1.read(data2);
                if (len1 < len2) {
                    return -1;
                }
                if (len1 > len2) {
                    return 1;
                }
                if (len1 <= 0) {
                    break;
                }
                for (int x = 0; x < len1; ++x) {
                    if (data1[x] < data2[x]) {
                        return -1;
                    }
                    if (data1[x] > data2[x]) {
                        return 1;
                    }
                }
            }
            return 0;
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (in1 != null) {
                try {
                    in1.close();

                } catch (IOException ex) {
                    MXFileLogger.getLogger(MXUtil.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
            if (in2 != null) {
                try {
                    in2.close();
                } catch (IOException ex) {
                    MXFileLogger.getLogger(MXUtil.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
    }

    public static void fixConsoleEncoding() {
        try {
            // for NetBeans + Ant With Fork
            String targetCharset = System.getProperty("stdout.encoding");
            if (targetCharset == null) {
                targetCharset = System.getProperty("native.encoding");
            }
            if (targetCharset.equals(System.out.charset().toString())) {
                System.out.println("Console Have Valid Encoding.");
            } else {
                System.out.println("*ENV*JAVA_TOOL_OPTIONS=" + System.getenv("JAVA_TOOL_OPTIONS"));
                System.out.println("*ENV*_JAVA_OPTIONS=" + System.getenv("_JAVA_OPTIONS"));
                System.out.println("*ENV*LANG=" + System.getenv("LANG"));

                System.out.println("*PROP*file.encoding = " + System.getProperty("file.encoding"));
                System.out.println("*PROP*native.encoding = " + System.getProperty("native.encoding"));
                System.out.println("*PROP*stdout.encoding = " + System.getProperty("stdout.encoding"));
                System.out.println("*PROP*stderr.encoding = " + System.getProperty("stderr.encoding"));

                System.out.println("*METHOD*Charset.defaultCharset() = " + Charset.defaultCharset());
                System.out.println("*METHOD*System.out.charset() = " + System.out.charset());

                try {
                    System.setOut(new PrintStream(System.out, true, targetCharset));
                    System.setErr(new PrintStream(System.err, true, targetCharset));
                    System.out.println("Console Encoding fixed.");
                    System.out.println("after overwrite System.out.charset() = " + System.out.charset());
                    System.out.println("after overwrite System.err.charset() = " + System.err.charset());
                } catch (IOException ex) {
                    MXFileLogger.getLogger(MXUtil.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        } catch (RuntimeException ex) {
            MXFileLogger.getLogger(MXUtil.class).log(Level.WARNING, ex.getMessage(), ex);
        }
    }


    public static String digitalClock(long time) {
        String hour = Long.toString(time / 60 / 60 / 1000);
        String min = Long.toString((time / 60 / 1000) % 60);
        String sec = Long.toString((time / 1000) % 60);
        if (hour.equals("0")) {
            hour = "";
            if (min.equals("0")) {
                min = "";
            }
        }
        
        if (min.length() >= 1) {
            if (sec.length() == 1) {
                sec = "0" + sec;
            }
        }
        if (hour.length() >= 1) {
            if (min.length() == 1) {
                min = "0" + sec;
            }
        }
        
        if (hour.length() >= 1) {
            return "" + hour +":" + min + ":" + sec;
        }
        if (min.length() >= 1) {
            return "" + min + ":" + sec;
        }
        return "" + sec;
     }
    
    static ColorSpace _colorSpaceXYZ = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);

    public static Color mixedColorXYZ(Color[] list) {
        float[] mixed = null;
        int count = 0;
        for (Color seek : list) {
            if (seek == null) {
                continue;
            }
            float red = (float)(seek.getRed() * 1.0 / 0xff);
            float green = (float)(seek.getGreen() * 1.0 / 0xff);
            float blue = (float)(seek.getBlue() * 1.0 / 0xff);
            float[] rgb = new float[] { red, green, blue };
            float[] conv = _colorSpaceXYZ.fromRGB(rgb);
            if (mixed == null) {
                mixed = new float[conv.length];
                for (int i = 0; i < conv.length; ++ i) {
                    mixed[i] = conv[i];
                }
            }
            else if (mixed.length < conv.length) {
                float[] newMixed = new float[conv.length];
                for (int i = 0; i < conv.length; ++ i) {
                    if (i < mixed.length) {
                        newMixed[i] = mixed[i];
                    }else {
                        break;
                    }
                }
                mixed = newMixed;
            }
            for (int i = 0; i < conv.length; ++ i) {
                mixed[i] += conv[i];
            }
            count ++;
        }
        if (mixed != null) {
            for (int i = 0; i < mixed.length; ++ i) {
                mixed[i] = mixed[i] / count;
            }
            float[] result = _colorSpaceXYZ.toRGB(mixed);
            
            int red = (int)(result[0] * 0xff);
            int green  = (int)(result[1] * 0xff);
            int blue  = (int)(result[2] * 0xff);
            return new Color(red, green, blue, 255);
        }
        return null;
    }

}
