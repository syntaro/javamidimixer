package jp.synthtarou.midimixer.libs.swing;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSliderUI;
import jp.synthtarou.midimixer.MXStatic;

/**
 *
 * @author https://stackoverflow.com/questions/518471/jslider-question-position-after-leftclick
 */
public class MXSliderUIForTablet extends BasicSliderUI {
    BasicSliderUI lastUI;
    
    public MXSliderUIForTablet(final JSlider slider) {
        super(slider);
       
        for (MouseListener l : slider.getMouseListeners()) {
            slider.removeMouseListener(l);
        }
        for (MouseMotionListener l2 : slider.getMouseMotionListeners()) {
            slider.removeMouseMotionListener(l2);
        }
        lastUI = (BasicSliderUI) slider.getUI();

        BasicSliderUI.TrackListener tl = lastUI.new TrackListener() {

            // this is where we jump to absolute value of click
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                if (MXStatic._trapMouseForOnlySelect) {
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
