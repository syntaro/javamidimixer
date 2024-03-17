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

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.logging.Level;
import jp.synthtarou.midimixer.MXMain;
import jp.synthtarou.midimixer.libs.common.MXLineReader;
import jp.synthtarou.midimixer.libs.common.MXLineWriter;
import jp.synthtarou.midimixer.libs.common.MXLogger2;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXSetting {

    public static void main(String[] args) {
        MXSetting root = new MXSetting("test");

        root.register("base.attribute");
        root.register("base.attribute[].text");
        root.register("base[b]");
        root.register("base[1]");
        root.register("base.[].position");

        root.setSetting("base", "123");
        root.setSetting("base.attribute[1].text", "a1");
        root.setSetting("base.attribute[2].text", "a2");
        root.setSetting("base.attribute[3].text", "a3");

        root.setSetting("base[12].position", "12");
        root.setSetting("base.[13].position", "13");
        root.setSetting("base[14]position", "14");

        try {
            root.dump(new OutputStreamWriter(System.out));
        } catch (IOException ex) {
            MXLogger2.getLogger(MXSetting.class).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    protected File _settingFile;
    static ArrayList<MXSetting> everySetting = new ArrayList();

    public File getFile() {
        return _settingFile;
    }

    public MXSetting(File file) {
        this(file, true);
    }

    public MXSetting(File file, boolean addToEvery) {
        _settingFile = file;
        //System.out.println("MXSetting::MXSetting " + _settingFile + " : addToEvenry " + addToEvery);
        _registered = new TreeSet(comparatorForRegister);
        if (addToEvery) {
            everySetting.add(this);
        }
    }

    public MXSetting(String name) {
        this(name, true);
    }

    public MXSetting(String name, boolean addToEvery) {
        this(new File(MXSettingUtil.getSettingDirectory(), name + ".ini"), addToEvery);
    }

    public static void saveEverySettingToFile() {
        for (MXSetting setting : everySetting) {
            setting.writeSettingFile();
        }
    }

    protected static class Detail {

        String key;
        String value;
    }

    protected static class DetailArray {

        ArrayList<Detail> list = new ArrayList();
    }

    private MXSettingTarget _target = null;
    private String _targetName;

    public void setTarget(MXSettingTarget target) {
        if (target == null) {
            throw new NullPointerException();
        }
        _target = target;
        _target.prepareSettingFields();
        _targetName = getClassName(_target.getClass());
        //System.out.println("MXSetting " + _targetName +" : setTarget");
    }

    public static String getClassName(Class cls) {
        String name = cls.getName();
        if (name != null) {
            int x = name.lastIndexOf('.');
            if (x >= 0) {
                name = name.substring(x + 1);
            }
        }
        return name;
    }

    MXSettingNode _root = new MXSettingNode(this, null, null);

    public boolean readSettingFile() {
        //System.out.println("MXSetting " + _targetName +" : readSettingFile " + _settingFile);
        if (_registered.size() == 0) {
            return false;
        }
        InputStream fin = null;
        try {
            _root.clearValues();
            fin = new FileInputStream(_settingFile);

            String fileName = _settingFile.toString();
            String dir = MXSettingUtil.getSettingDirectory().getParent();
            if (fileName.startsWith(dir)) {
                fileName = "$(APP)" + fileName.substring(dir.length());
            }
            MXMain.progress("reading " + _targetName + " from " + fileName);
            MXLineReader reader = new MXLineReader(fin, "utf-8");
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.length() == 0) {
                    continue;
                }
                char first = line.charAt(0);
                if (first == '#' || first == '/') {
                    continue;
                }
                int index = line.indexOf('=');
                if (index < 0) {
                    continue;
                }
                String key = line.substring(0, index);
                String value = line.substring(index + 1);
                _root.setSetting(key, value);
            }
        } catch (IOException e) {
            //System.out.println("First Time for [" + _settingFile + "]");
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ex) {
                    MXLogger2.getLogger(MXSetting.class).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
        if (_target != null) {
            _target.afterReadSettingFile();
        }
        return true;
    }

    public boolean writeSettingFile() {
        //System.out.println("MXSetting " + _targetName +" : writeSettingFile " + _settingFile);
        if (_target != null) {
            _root.clearValues();
            _target.beforeWriteSettingFile();
        }
        File temporary = MXSettingUtil.createTemporaryFile(_settingFile);
        MXLineWriter writer = null;

        String fileName = _settingFile.toString();
        String dir = MXSettingUtil.getSettingDirectory().getParent();
        if (fileName.startsWith(dir)) {
            fileName = "$(APP)" + fileName.substring(dir.length());
        }
        MXMain.progress("writing " + _targetName + " from " + fileName);

        try {
            writer = new MXLineWriter(temporary, "utf-8");

            dump(writer.getWriter());

            writer.close();

            File backup = MXSettingUtil.autobackupFile(_settingFile);
            temporary.renameTo(_settingFile);

            if (backup != null) {
                if (MXSettingUtil.isFileContentsSame(backup, _settingFile)) {
                    try {
                        backup.delete();
                    } catch (Exception e) {
                    }
                } else {
                    try {
                        Desktop.getDesktop().moveToTrash(backup);
                    } catch (Exception e) {
                    }
                }
            }
            writer = null;
            return true;
        } catch (IOException ex) {
            MXLogger2.getLogger(MXSetting.class).log(Level.WARNING, ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            MXLogger2.getLogger(MXSetting.class).log(Level.WARNING, ex.getMessage(), ex);
        }

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ex2) {
                MXLogger2.getLogger(MXSetting.class).log(Level.WARNING, ex2.getMessage(), ex2);
            }
            temporary.delete();
        }
        return false;
    }

    public void clearValue() {
        _root.clearValues();
    }

    public String getSetting(String name) {
        return _root.getSetting(name);
    }

    public int getSettingAsInt(String name, int defvalue) {
        return _root.getSettingAsInt(name, defvalue);
    }

    public boolean getSettingAsBoolean(String name, boolean defvalue) {
        return _root.getSettingAsBoolean(name, defvalue);
    }

    public boolean setSetting(String name, String value) {
        return _root.setSetting(name, value);
    }

    public boolean isEmpty() {
        return _root.isEmpty();
    }

    public boolean setSetting(String name, int value) {
        return setSetting(name, String.valueOf(value));
    }

    public boolean setSetting(String name, boolean value) {
        if (value) {
            return setSetting(name, "1");
        } else {
            return setSetting(name, "0");
        }
    }

    public boolean havingName(String name) {
        return _root.childByKey(name) != null;
    }

    public void dump(Writer writer) throws IOException {
        _root.recuesiveDump(writer);
        writer.flush();
    }

    public void register(String name) {
        StringPath p = _root.getPath().clone();
        p.addAll(StringPath.parsePath(name));
        register(p);
    }

    public void register(StringPath path) {
        _registered.add(path);
    }

    public boolean isRegistered(String name) {
        StringPath p = _root.getPath().clone();
        p.addAll(StringPath.parsePath(name));
        return isRegistered(p);
    }

    public boolean isRegistered(StringPath path) {
        return _registered.contains(path);
    }

    protected TreeSet<StringPath> _registered;

    protected Comparator comparatorForRegister = new Comparator() {
        public int compare(Object o1, Object o2) {
            StringPath p1 = (StringPath) o1;
            StringPath p2 = (StringPath) o2;
            int length = Math.min(p1.size(), p2.size());
            for (int x = 0; x < length; ++x) {
                String str1 = p1.get(x);
                String str2 = p2.get(x);

                if (MXSettingUtil.isInteger(str1)) {
                    str1 = "0";
                }
                if (MXSettingUtil.isInteger(str2)) {
                    str2 = "0";
                }

                int d = str1.compareTo(str2);
                if (d != 0) {
                    return d;
                }
            }
            if (p1.size() < p2.size()) {
                return -1;
            } else if (p1.size() > p2.size()) {
                return 1;
            }

            return 0;
        }
    };

    public ArrayList<MXSettingNode> findByPath(String name) {
        StringPath path = StringPath.parsePath(name);

        ArrayList<MXSettingNode> seeking = new ArrayList();
        seeking.add(_root);

        for (String text : path) {
            ArrayList<MXSettingNode> hit = new ArrayList();
            boolean isInteger = MXSettingUtil.isInteger(text);

            for (MXSettingNode parent : seeking) {
                int count = parent.size();
                for (int x = 0; x < count; ++x) {
                    MXSettingNode child = parent.childByIndex(x);
                    if (isInteger) {
                        if (child.isInteger()) {
                            hit.add(child);
                        }
                    } else {
                        if (child.getName().equalsIgnoreCase(text)) {
                            hit.add(child);
                        }
                    }
                }
            }

            seeking = hit;
        }

        return seeking;
    }
}
