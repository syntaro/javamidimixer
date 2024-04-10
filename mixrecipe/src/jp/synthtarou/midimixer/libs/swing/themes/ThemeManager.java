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
package jp.synthtarou.midimixer.libs.swing.themes;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.namedobject.MXNamedObject;
import jp.synthtarou.libs.namedobject.MXNamedObjectList;
import jp.synthtarou.libs.inifile.MXINIFile;
import jp.synthtarou.midimixer.libs.swing.CurvedSlider;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;
import jp.synthtarou.libs.json.MXJsonSupport;
import jp.synthtarou.libs.inifile.MXINIFileSupport;
import jp.synthtarou.libs.json.MXJsonParser;
import jp.synthtarou.libs.json.MXJsonValue;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class ThemeManager implements MXINIFileSupport, MXJsonSupport {
    static ThemeManager _manager = new ThemeManager();
    public static ThemeManager getInstance() {
        return _manager;
    }
    
    public boolean colorfulMetalTheme = false;
    public String themeName = "Metal";
    public String fontName = "Serif";
    public int fontSize = 12;
    public int fontStyle = Font.PLAIN;

    public static final int THEME_SEA = 0;
    public static final int THEME_FOREST = 1;
    public static final int THEME_WINE = 2;
    public static final int THEME_STONE = 3;

    public static String[] additionalTheme;
    
   
    public ThemeManager() {
        additionalTheme = new String[] {
            "*Sea", "*Forest", "*WineRed", "*Stone"
        };
    }
 
    public void setUITheme(String themeName) {
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        try {
            boolean done = false;
            for (int i = 0; i < additionalTheme.length; ++ i) {
                if (additionalTheme[i].equals(themeName)) {
                    MetalTheme theme = null;
                    colorfulMetalTheme = true;
                    switch (i) {
                        case THEME_FOREST:
                            theme = new MetalForestTheme();
                            break;
                        case THEME_WINE:
                            theme = new MetalWineTheme();
                            break;
                        case THEME_SEA:
                            theme = new MetalSeaTheme();
                            break;
                        case THEME_STONE:
                            theme = new MetalStoneTheme();
                            break;
                    }
                    MetalLookAndFeel.setCurrentTheme(theme);
                    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Metal".equalsIgnoreCase(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                        }
                    }
                    done = true;
                }
            }
            if (!done) {
                colorfulMetalTheme = false;
                MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
                for (UIManager.LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
                    if (themeName.equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                    }
                }
            }
        } catch (UnsupportedLookAndFeelException ex) {
            MXFileLogger.getLogger(ThemeManager.class).log(Level.WARNING, ex.getMessage(), ex);
        } catch (ClassNotFoundException ex) {
            MXFileLogger.getLogger(ThemeManager.class).log(Level.WARNING, ex.getMessage(), ex);
        } catch (IllegalAccessException ex) {
            MXFileLogger.getLogger(ThemeManager.class).log(Level.WARNING, ex.getMessage(), ex);
        } catch (InstantiationException ex) {
            MXFileLogger.getLogger(ThemeManager.class).log(Level.WARNING, ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            MXFileLogger.getLogger(ThemeManager.class).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    public void setFont(String name, int style, int size) {
        Font f2;
        try {
            f2 = new Font(name, style, size);
        }catch(Exception e) {
            name = "Dialog";
            f2 = new Font(name, style, size);
        }
        for (java.util.Map.Entry<?, ?> entry : UIManager.getDefaults().entrySet()) {
            if (entry.getKey().toString().toLowerCase().endsWith("font")) {
                FontUIResource f1 = new FontUIResource(f2);
                UIManager.put(entry.getKey(), f1);
            }
        }
    }

    public void updateUITree() {
        Window[] win = Window.getWindows();
        for (int i = 0; i < win.length; ++i) {
            updateComponentTreeUI(win[i]);
            /*
            if (win[i] instanceof JDialog) {
                win[i].pack();
            }*/
        }
    }

    public static void updateComponentTreeUI(Component c) {
        updateComponentTreeUI0(c);
        c.invalidate();
        c.validate();
        c.repaint();
    }

    private static void updateComponentTreeUI0(Component c) {
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            if (jc instanceof JSlider) {
                SliderUI ui = (SliderUI)jc.getUI();
                if (ui instanceof MXAttachSliderLikeEclipse) {
                    jc.setBackground(new JLabel().getBackground());
                    return;
                }
            }
            jc.updateUI();
            JPopupMenu jpm =jc.getComponentPopupMenu();
            if(jpm != null) {
                updateComponentTreeUI(jpm);
            }
        }
        Component[] children = null;
        if (c instanceof JMenu) {
            children = ((JMenu)c).getMenuComponents();
        }
        else if (c instanceof Container) {
            children = ((Container)c).getComponents();
        }
        if (children != null) {
            for (Component child : children) {
                updateComponentTreeUI0(child);
            }
        }
    }

    @Override
    public MXINIFile prepareINIFile(File custom) {
        if (custom == null) {
            custom = MXINIFile.pathOf("ThemeManager");
        }
        MXINIFile setting = new MXINIFile(custom, this);
        setting.register("themeLabelColorful");
        setting.register("themeName");
        setting.register("fontName");
        setting.register("fontSize");
        setting.register("fontStyle");
        setting.register("circleIsCircle");
        return setting;
    }

    @Override
    public boolean readINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        if (!setting.readINIFile()) {
            return false;
        }
        colorfulMetalTheme = setting.getSettingAsBoolean("themeLabelColorful", false);
        themeName = setting.getSetting("themeName");
        if (themeName == null || themeName.isEmpty()) {
            for (UIManager.LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
                if (info.getName().equals("Windows")) {
                    themeName = "Windows";
                }
            }
        }
        if (themeName == null || themeName.isEmpty()) {
            for (UIManager.LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
                if (info.getName().equals("Nimbus")) {
                    themeName = "Nimbus";
                }
            }
        }
        if (themeName == null || themeName.isEmpty()) {
            themeName = "Metal";
        }
        fontName = setting.getSetting("fontName");
        if (fontName == null || fontName.isEmpty()) {
            fontName = "Monospaced";
        }
        fontSize = setting.getSettingAsInt("fontSize", 12);
        fontStyle = setting.getSettingAsInt("fontStyle", Font.PLAIN);
        setFont(fontName, fontStyle, fontSize);
        setUITheme(themeName);
        CurvedSlider.setMouseCircleIsCircle(setting.getSettingAsBoolean("circleIsCircle", true));
        updateUITree();
        return true;
    }

    @Override
    public boolean writeINIFile(File custom) {
        MXINIFile setting = prepareINIFile(custom);
        setting.setSetting("themeLabelColorful", colorfulMetalTheme);
        setting.setSetting("themeName", themeName);
        setting.setSetting("fontName", fontName);
        setting.setSetting("fontSize", fontSize);
        setting.setSetting("fontStyle", fontStyle);
        setting.setSetting("circleIsCircle", CurvedSlider.isMouseCircleIsCircle());
        return setting.writeINIFile();
    }
    
    public MXNamedObjectList<String> getLookAndFeelModel() {
        MXNamedObjectList<String> model = new MXNamedObjectList();
        for (UIManager.LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
            model.addNameAndValue(info.getName(), info.getName());
        }
        /*
        for (int i = 0; i < additionalTheme.length; ++ i) {
            model.addNameAndValue(additionalTheme[i], additionalTheme[i]);
        }*/

        return model;
    }
    
    public MXNamedObjectList<String> getFontNameModel() {
        MXNamedObjectList<String> model = new MXNamedObjectList();
        String[] names = {
            "Dialog",
            "Monospaced",
            "Meiryo",
            "Serif",
            "SansSerif",
        };
        String set = ThemeManager.getInstance().fontName;
        for (int i = 0; i < names.length; i++) {
            model.addNameAndValue(names[i], names[i]);
            if (names[i].equals(set)) {
                model.setSelectedItem(model.get(model.size() - 1));
            }
        }
        return model;
    }

    public MXNamedObjectList<Integer> getFontStyleModel() {
        MXNamedObjectList<Integer> model = new MXNamedObjectList();
        model.addNameAndValue("Plain", Font.PLAIN);
        model.addNameAndValue("Italic", Font.ITALIC);
        model.addNameAndValue("Bold", Font.BOLD);
        model.addNameAndValue("BoldItalic", Font.BOLD + Font.ITALIC);

        for (MXNamedObject<Integer> seek : model) {
            if (seek._value == fontStyle) {
                model.setSelectedItem(seek);
            }
        }
        return model;
    }

    public MXNamedObjectList<Integer> getFontSizeModel() {
        MXNamedObjectList<Integer> model = new MXNamedObjectList();
        for (int x = 6; x < 16; x += 1) {
            model.addNameAndValue(String.valueOf(x), x);
        }
        for (MXNamedObject<Integer> seek : model) {
            if (seek._value == fontSize) {
                model.setSelectedItem(seek);
            }
        }
        return model;
    }
    
    public boolean isColorfulMetalTheme() {
        return colorfulMetalTheme;
    }

    @Override
    public boolean readJSonfile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("ThemeManager");
            MXJsonParser.setAutosave(this);
        }
        MXJsonValue value = new MXJsonParser(custom).parseFile();
        if (value == null) {
            return false;
        }

        MXJsonValue.HelperForStructure root = value.new HelperForStructure();

        colorfulMetalTheme = root.getFollowingBool("themeLabelColorful", false);
        themeName = root.getFollowingText("themeName", null);
        if (themeName == null || themeName.isEmpty()) {
            for (UIManager.LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
                if (info.getName().equals("Windows")) {
                    themeName = "Windows";
                }
            }
        }
        if (themeName == null || themeName.isEmpty()) {
            for (UIManager.LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
                if (info.getName().equals("Nimbus")) {
                    themeName = "Nimbus";
                }
            }
        }
        if (themeName == null || themeName.isEmpty()) {
            themeName = "Metal";
        }
        fontName = root.getFollowingText("fontName", null);
        if (fontName == null || fontName.isEmpty()) {
            fontName = "Monospaced";
        }
        fontSize = root.getFollowingInt("fontSize", 12);
        fontStyle = root.getFollowingInt("fontStyle", Font.PLAIN);
        setFont(fontName, fontStyle, fontSize);
        setUITheme(themeName);
        CurvedSlider.setMouseCircleIsCircle(root.getFollowingBool("circleIsCircle", true));
        updateUITree();
        return true;
    }

    @Override
    public boolean writeJsonFile(File custom) {
        if (custom == null) {
            custom = MXJsonParser.pathOf("ThemeManager");
        }
        MXJsonValue value = new MXJsonValue(null);

        MXJsonValue.HelperForStructure structure = value.new HelperForStructure();
        structure.setFollowingBool("themeLabelColorful", colorfulMetalTheme);
        structure.setFollowingText("themeName", themeName);
        structure.setFollowingText("fontName", fontName);
        structure.setFollowingInt("fontSize", fontSize);
        structure.setFollowingInt("fontStyle", fontStyle);
        structure.setFollowingBool("circleIsCircle", CurvedSlider.isMouseCircleIsCircle());

        MXJsonParser parser = new MXJsonParser(custom);
        parser.setRoot(value);
        return parser.writeFile();
    }

    @Override
    public void resetSetting() {
    }
}
