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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSliderUI;
import jp.synthtarou.midimixer.MXAppConfig;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXAttachSliderSingleClick {
    public MXAttachSliderSingleClick(JSlider slider) {
        for (MouseListener l : slider.getMouseListeners()) {
            slider.removeMouseListener(l);
        }
        for (MouseMotionListener l2 : slider.getMouseMotionListeners()) {
            slider.removeMouseMotionListener(l2);
        }
        BasicSliderUI lastUI = (BasicSliderUI) slider.getUI();
        
        BasicSliderUI.TrackListener tl = lastUI.new TrackListener() {

            // this is where we jump to absolute value of click
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                if (MXAppConfig._trapMouseForOnlySelect) {
                    return;
                }
                if (slider.isEnabled()) {
                    BasicSliderUI ui = (BasicSliderUI) slider.getUI();
                    Point p = e.getPoint();
                    if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
                        int value = ui.valueForXPosition(p.x);
                        slider.setValue(value);
                    }else {
                        int value = ui.valueForYPosition(p.y);
                        slider.setValue(value);
                    }
                    slider.repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                 mouseClicked(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                mouseClicked(e);
            }
            // disable check that will invoke scrollDueToClickInTrack
            @Override
            public boolean shouldScroll(int dir) {
                return false;
            }        
        };

        slider.addMouseListener(tl);
        slider.addMouseMotionListener(tl);
    }
}
