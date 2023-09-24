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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import jp.synthtarou.midimixer.libs.common.MXWrap;
import jp.synthtarou.midimixer.libs.common.MXWrapList;
import jp.synthtarou.midimixer.libs.settings.MXSetting;
import jp.synthtarou.midimixer.libs.settings.MXSettingTarget;
import jp.synthtarou.midimixer.libs.swing.CurvedSlider;
import jp.synthtarou.midimixer.libs.swing.attachment.MXAttachSliderLikeEclipse;

public class ThemeManager implements MXSettingTarget {
    MXSetting _setting;
    static ThemeManager _manager = new ThemeManager();
    public static ThemeManager getInstance() {
        return _manager;
    }
    
    public boolean colorfulMetalTheme;
    public String themeName;
    public String fontName;
    public int fontSize;
    public int fontStyle;

    public static final int THEME_SEA = 0;
    public static final int THEME_FOREST = 1;
    public static final int THEME_WINE = 2;
    public static final int THEME_STONE = 3;

    public static String[] additionalTheme;
    
   
    public ThemeManager() {
        additionalTheme = new String[] {
            "*Sea", "*Forest", "*WineRed", "*Stone"
        };
        _setting = new MXSetting("ThemeManager");
        _setting.setTarget(this);
        _setting.readSettingFile();
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
        } catch (Exception e) {
            e.printStackTrace();
            // If Nimbus is not available, you can set the GUI to another look and feel.
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
            if (win[i] instanceof JDialog) {
                //win[i].pack();
            }
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
    public void prepareSettingFields(MXSetting setting) {
        setting.register("themeLabelColorful");
        setting.register("themeName");
        setting.register("fontName");
        setting.register("fontSize");
        setting.register("fontStyle");
        setting.register("circleIsCircle");
    }

    @Override
    public void afterReadSettingFile(MXSetting setting) {
        colorfulMetalTheme = setting.getSettingAsBoolean("themeLabelColorful", false);
        themeName = setting.getSetting("themeName");
        if (themeName == null || themeName.isEmpty()) {
            themeName = "Metal";
        }
        fontName = setting.getSetting("fontName");
        if (fontName == null || fontName.isEmpty()) {
            fontName = "メイリオ";
        }
        fontSize = setting.getSettingAsInt("fontSize", 12);
        fontStyle = setting.getSettingAsInt("fontStyle", Font.PLAIN);
        setFont(fontName, fontStyle, fontSize);
        setUITheme(themeName);
        CurvedSlider.setMouseCircleIsCircle(setting.getSettingAsBoolean("circleIsCircle", true));
        updateUITree();
    }

    @Override
    public void beforeWriteSettingFile(MXSetting setting) {
        setting.setSetting("themeLabelColorful", colorfulMetalTheme);
        setting.setSetting("themeName", themeName);
        setting.setSetting("fontName", fontName);
        setting.setSetting("fontSize", fontSize);
        setting.setSetting("fontStyle", fontStyle);
        setting.setSetting("circleIsCircle", CurvedSlider.isMouseCircleIsCircle());
    }
    

    public MXWrapList<String> getLookAndFeelModel() {
        MXWrapList<String> model = new MXWrapList();
        for (UIManager.LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
            model.addNameAndValue(info.getName(), info.getName());
        }
        /*
        for (int i = 0; i < additionalTheme.length; ++ i) {
            model.addNameAndValue(additionalTheme[i], additionalTheme[i]);
        }*/

        return model;
    }
    
    public MXWrapList<String> getFontNameModel() {
        MXWrapList<String> model = new MXWrapList();
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

    public MXWrapList<Integer> getFontStyleModel() {
        MXWrapList<Integer> model = new MXWrapList();
        model.addNameAndValue("Plain", Font.PLAIN);
        model.addNameAndValue("Italic", Font.ITALIC);
        model.addNameAndValue("Bold", Font.BOLD);
        model.addNameAndValue("BoldItalic", Font.BOLD + Font.ITALIC);

        for (int i = 0; i < model.size(); i++) {
            MXWrap<Integer> wrap = model.get(i);
            if (wrap._value == fontStyle) {
                model.setSelectedItem(wrap);
            }
        }
        return model;
    }

    public MXWrapList<Integer> getFontSizeModel() {
        MXWrapList<Integer> model = new MXWrapList();
        for (int x = 6; x < 16; x += 1) {
            model.addNameAndValue(String.valueOf(x), x);
        }
        for (int i = 0; i < model.size(); i++) {
            MXWrap<Integer> wrap = model.get(i);
            if (wrap._value == fontSize) {
                model.setSelectedItem(wrap);
            }
        }
        return model;
    }
    
    public boolean isColorfulMetalTheme() {
        return colorfulMetalTheme;
    }
}
