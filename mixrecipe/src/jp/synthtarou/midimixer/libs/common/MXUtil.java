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
package jp.synthtarou.midimixer.libs.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
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
import java.util.LinkedList;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.mx30controller.MGCircle;
import jp.synthtarou.midimixer.mx30controller.MGDrumPad;
import jp.synthtarou.midimixer.mx30controller.MGSlider;

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

        if (text == null) {
            return errorNumber;
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

    public static void swingTreeEditable(Component c, boolean enable) {
        if (c instanceof Container) {
            Container parent = (Container) c;
            int count = parent.getComponentCount();
            for (int x = 0; x < count; ++x) {
                swingTreeEditable(parent.getComponent(x), enable);
            }
        }
        if (c instanceof MGCircle) {
            MGCircle circle = (MGCircle) c;
            circle.setValueChangeable(enable);
        }
        if (c instanceof MGSlider) {
            MGSlider slider = (MGSlider) c;
            slider.setValueChangeable(enable);
        }
        if (c instanceof MGDrumPad) {
            MGDrumPad pad = (MGDrumPad) c;
            pad.setValueChangeable(enable);
        }
    }

    public static void showAsDialog(Container parent, JPanel panel, String title) {
        Container cont = getOwnerWindow(parent);
        if (title == null) {
            title = MXAppConfig.MX_APPNAME;
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

    public static Color mixedColor(Color left, Color right, int percent) {
        int lr = (int) (left.getRed() * (100 - percent) / 100);
        int lg = (int) (left.getGreen() * (100 - percent) / 100);
        int lb = (int) (left.getBlue() * (100 - percent) / 100);
        int rr = (int) (right.getRed() * percent / 100);
        int rg = (int) (right.getGreen() * percent / 100);
        int rb = (int) (right.getBlue() * percent / 100);
        return new Color(lr + rr, lg + rg, lb + rb);
    }

    public static void backgroundRecursive(Container container, Color color) {
        LinkedList<Container> listContainer = new LinkedList();
        listContainer.add(container);

        while (listContainer.isEmpty() == false) {
            Container cont = listContainer.remove();
            if (cont == null) {
                continue;
            }
            cont.setBackground(color);

            Component[] list = cont.getComponents();
            for (Component child : list) {
                if (child instanceof Container) {
                    listContainer.add((Container) child);
                } else {
                    child.setBackground(color);
                }
            }
        }
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
            ProtectionDomain pd = MXUtil.class
                    .getProtectionDomain();
            CodeSource cs = pd.getCodeSource();
            URL location = cs.getLocation();
            URI uri = location.toURI();
            Path path = Paths.get(uri);
            fileName = path.toString();
        } catch (URISyntaxException ex) {
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
            ex.printStackTrace();
            return -1;
        } finally {
            if (i1 != null) {
                try {
                    i1.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (i2 != null) {
                try {
                    i2.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
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
                    ex.printStackTrace();
                }
                ;

            }
            if (in2 != null) {
                try {
                    in2.close();

                } catch (IOException ex) {
                    ex.printStackTrace();
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

                System.setOut(new PrintStream(System.out, true, targetCharset));
                System.setErr(new PrintStream(System.err, true, targetCharset));
                System.out.println("Console Encoding fixed.");
                System.out.println("after overwrite System.out.charset() = " + System.out.charset());
                System.out.println("after overwrite System.err.charset() = " + System.err.charset());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
