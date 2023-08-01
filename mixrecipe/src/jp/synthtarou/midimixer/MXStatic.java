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
package jp.synthtarou.midimixer;

import java.awt.Color;
import javax.swing.plaf.metal.MetalLookAndFeel;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.swing.themes.ThemeManager;

/**
 *
 * @author Syntarou YOSHIDA
 */

public class MXStatic {

    public static Color sliderColor(int column) {
        column /= 4;
        boolean sprite = (column % 2 == 0) ? true : false;
        if (sprite) {
            if (ThemeManager.getInstance().colorfulMetalTheme) {
                return MetalLookAndFeel.getCurrentTheme().getControlShadow();
            }
            return colorSlider1;
        }else {
            if (ThemeManager.getInstance().colorfulMetalTheme) {
                return MetalLookAndFeel.getCurrentTheme().getControlHighlight();
            }
            return colorSlider2;
        }
    }
    public static final String LOOPMIDI_NAME = "EX MIDIMixer Out";
    public static final String MX_APPNAME = "MIX Recipe";
    public static final String MX_APPNAME_WITH_VERSION = "MIX Recipe VSTi 0.43";
    public static final String MX_EDITING = "*** EDIT CONTROl MODE ***";
    public static final int DRUM_CH = 9;
    
    static final MXStatic _setting = new MXStatic();
    public static final int TOTAL_PORT_COUNT = 16;

    public static final int SLIDER_COLUMN_COUNT = 17;
    public static final int SLIDER_ROW_COUNT = 1;
    public static final int CIRCLE_ROW_COUNT = 4;
    public static final int DRUM_ROW_COUNT = 3;

    public static boolean _trapMouseForOnlySelect = false;

    private static final Color colorSlider1 = MXUtil.mixedColor(MXUtil.mixedColor(Color.blue, Color.lightGray, 50), Color.white, 50);
    private static final Color colorSlider2 = MXUtil.mixedColor(colorSlider1, Color.white, 50);
    
    public static final int MOUSE_VELOCITY = 100;
}
