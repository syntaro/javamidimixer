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
package jp.synthtarou.cceditor.view.common;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import javax.swing.plaf.basic.BasicComboBoxRenderer;


/**
 *
 * @author Syntarou YOSHIDA
 */
public class ShrinkableComboBoxRenderer extends BasicComboBoxRenderer {
        String _text;

    public ShrinkableComboBoxRenderer() {
    }

    public void setText(String text) {
        _text = text;
        super.setText(text.length() < 12 ? text : text.substring(0, 12)); //for calculate minimum size
    }

    private static final int LENGTH = 20;
    private static final float DIFF = .05f;

    @Override
    public void paintComponent(Graphics g) {
        // https://ateraimemo.com/Swing/TextOverflowFadeLabel.html
        Insets i = getInsets();
        int w = getWidth() - i.left - i.right;
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
}
