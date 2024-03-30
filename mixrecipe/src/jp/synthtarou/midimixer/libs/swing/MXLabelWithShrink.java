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
package jp.synthtarou.midimixer.libs.swing;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jp.synthtarou.libs.MXUtil;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXLabelWithShrink extends JLabel {
    String _text;
    int _width;
    
    /**
     *
     * @param text
     */
    public MXLabelWithShrink(String text) {
        super(text);
        _text = text;
        _width = 300;
    }
    
    /**
     *
     * @param text
     * @param width
     */
    public MXLabelWithShrink(String text, int width) {
        super(text);
        _text = text;
        _width = width;
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension calc = super.getPreferredSize();
        return new Dimension(_width, calc.height);
    }
    
    private static final int LENGTH = 20;
    private static final float DIFF = .05f;

    @Override
    public void paintComponent(Graphics g) {
        // https://ateraimemo.com/Swing/TextOverflowFadeLabel.html
        Insets i = getInsets();
        int basew = _width < getWidth() ? _width : getWidth();
        int w = basew - i.left - i.right;
        int h = getHeight() - i.top - i.bottom;
        Rectangle rect = new Rectangle(i.left, i.top, w - LENGTH, h);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setFont(g.getFont());
        g2.setPaint(getForeground());

        FontRenderContext frc = g2.getFontRenderContext();
        String text = _text;
        if (text == null || text.length() == 0) {
            text = " ";
        }
        TextLayout tl = new TextLayout(text, getFont(), frc);
        int baseline = getBaseline(w, h);

        g2.setClip(rect);
        tl.draw(g2, getInsets().left, baseline);

        rect.width = 1;
        float alpha = 1f;
        for (int x = w - LENGTH; x < w; x++) {
            rect.x = x;
            alpha = Math.max(0f, alpha - DIFF);
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2.setClip(rect);
            tl.draw(g2, getInsets().left, baseline);
        }
        g2.dispose();
    }
    
    public static void main(String[] args) {
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; 
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (int i = 100; i < 800; i += 50) {
            JLabel label = new MXLabelWithShrink(alpha + alpha + alpha, i);
            panel.add(label);
        }
        MXUtil.showAsDialog(null, panel, "Shrink?");
        
    }
}
