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
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.midimixer.libs.swing.themes.ThemeManager;

/**
 *
 * @author Syntarou YOSHIDA
 */

public class MXConfiguration {
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
    public static final boolean _DEBUG = false;
    public static final String LOOPMIDI_NAME = "EX MIDIMixer Out";
    public static final String MX_APPLICATION = "MixRecipe 0.75 beta63" + (_DEBUG ? " **DEBUGMODE" : "(PublicBeta)");
    public static final String MX_EDITING = "*** EDIT CONTROl MODE ***";
    public static final int DRUM_CH = 9;

    public static final int TOTAL_PORT_COUNT = 16; // must <= 32 (bit calculation 1 << port should not overflow

    public static final int SLIDER_COLUMN_COUNT = 17;
    public static final int SLIDER_ROW_COUNT = 1;
    public static final int CIRCLE_ROW_COUNT = 4;
    public static final int DRUM_ROW_COUNT = 3;

    private static final Color colorSlider1 = MXUtil.mixtureColor(Color.blue, 25, Color.lightGray, 25, Color.white, 50);
    private static final Color colorSlider2 = MXUtil.mixtureColor(colorSlider1, 50, Color.white, 50);
    
    public static final int MOUSE_VELOCITY = 100;
}