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
package jp.synthtarou.midimixer.libs.swing.attachment;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicSliderUI;
import jp.synthtarou.midimixer.libs.common.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXAttachSliderLikeEclipse extends BasicSliderUI {

    BasicSliderUI lastUI;
    int _lineWeight = 7;
    int _thumbWeight = 15;
    int arc = 30;

    boolean _itsSwitch = false;

    /**
     *
     * @param slider
     */
    public MXAttachSliderLikeEclipse(final JSlider slider) {
        super(slider);

        lastUI = (BasicSliderUI) slider.getUI();

        slider.setUI(this);
    }

    @Override
    public void paintTrack(Graphics g) {
        int bold = _lineWeight;

        calculateTrackRect();
        int trackHeight = slider.getHeight();
        int trackWidth = slider.getWidth();
        int fillTop = 0;
        int fillBottom = trackHeight;
        int fillLeft = 0;
        int fillRight = trackWidth;
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(slider.getBackground());
        g2.fillRect(fillLeft, fillTop, trackWidth, trackHeight);

        int arcTop = fillTop;
        int arcBottom = fillBottom;
        int arcLeft = fillLeft;
        int arcRight = fillRight;

        if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
            if (bold > trackWidth) {
                bold = trackWidth;
            }
            fillTop = (trackHeight / 2) - (bold / 2);
            fillBottom = fillTop + bold;
            arcTop = fillTop;
            arcBottom = fillBottom;
            arcLeft = fillLeft;
            arcRight = xPositionForValue(slider.getValue());
        } else {
            if (bold > trackHeight) {
                bold = trackHeight;
            }
            arcTop = yPositionForValue(slider.getValue());
            arcBottom = fillBottom;

            fillLeft = (trackWidth / 2) - (bold / 2);
            fillRight = fillLeft + bold;
            arcLeft = fillLeft;
            arcRight = fillRight;
        }

        if (fillLeft < fillRight) {
            g2.setColor(Color.gray);
            g2.fillRoundRect(fillLeft, fillTop, fillRight - fillLeft, fillBottom - fillTop, arc, arc);
        }
        if (arcLeft < arcRight - 2) {
            if (_itsSwitch) {
                g2.setColor(MXUtil.mixtureColor(Color.green, 10, Color.white, 40, Color.orange, 80));
            } else {
                g2.setColor(MXUtil.mixtureColor(Color.pink, 40, Color.yellow, 60));
            }
            g2.fillRoundRect(arcLeft, arcTop, arcRight - arcLeft, arcBottom - arcTop, arc, arc);
        }
        if (fillLeft < fillRight) {
            g2.setColor(Color.black);
            g2.drawRoundRect(fillLeft, fillTop, fillRight - fillLeft, fillBottom - fillTop, arc, arc);
        }

        g2.dispose();
    }

    @Override
    public void paintThumb(Graphics g) {
        int bold = _thumbWeight - 2;
        
        //TODO enabled or disabled
        calculateTrackRect();
        int trackHeight = slider.getHeight();
        int trackWidth = slider.getWidth();
        int fillTop = 0;
        int fillBottom = trackHeight;
        int fillLeft = 0;
        int fillRight = trackWidth;
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2f));

        int arcTop = fillTop;
        int arcBottom = fillBottom;
        int arcLeft = fillLeft;
        int arcRight = fillRight;

        if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
            if (bold > trackWidth) {
                bold = trackWidth;
            }
            fillTop = (trackHeight / 2) - (bold / 2);
            fillBottom = fillTop + bold;

            arcTop = fillTop;
            arcBottom = fillBottom;
            arcLeft = xPositionForValue(slider.getValue()) - (bold / 2);
            arcRight = arcLeft + bold;
        } else {
            if (bold > trackHeight) {
                bold = trackHeight;
            }
            arcTop = yPositionForValue(slider.getValue()) - (bold / 2);
            arcBottom = arcTop + bold;

            fillLeft = (trackWidth / 2) - (bold / 2);
            fillRight = fillLeft + bold;
            arcLeft = fillLeft;
            arcRight = fillRight;
        }

        if (arcLeft < arcRight) {
            if (_itsSwitch) {
                g2.setColor(MXUtil.mixtureColor(Color.green, 60, Color.white, 40, Color.yellow, 140));
            } else {
                g2.setColor(Color.blue);
            }
            g2.fillRoundRect(arcLeft, arcTop, arcRight - arcLeft, arcBottom - arcTop, arc, arc);
            if (_itsSwitch) {
                g2.setColor(MXUtil.mixtureColor(Color.blue, 80, Color.white, 20, Color.yellow, 40));
            } else {
                g2.setColor(Color.white);
            }
            g2.drawRoundRect(arcLeft, arcTop, arcRight - arcLeft, arcBottom - arcTop, arc, arc);
        }

        g2.dispose();
    }

    @Override
    protected Dimension getThumbSize() {
        return new Dimension(_thumbWeight, _thumbWeight);
    }

    public void showAsSwitch(boolean b) {
        _itsSwitch = b;
    }
}
