/*
 * Copyright (C) 2023 Syntarou YOSHIDA.
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
package jp.synthtarou.libs.accordionui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import jp.synthtarou.midimixer.MXMain;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXAnimationPanel extends JPanel {

    /**
     *
     */
    public MXAnimationPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    BufferedImage _bufferedImage = null;
    Graphics _bufferedImageGraphics = null;
    Rectangle _bufferedRect = null;
    int _percent = 100;

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (isVisible()) {
            return d;
        } else {
            Dimension newsize = new Dimension(d.width, 0);
            return newsize;
        }
    }

    public void setScrollPercent(int x) {
        MXMain.invokeUI(() ->  {
            _percent = x;
            repaint();
        });
    }

    @Override
    public void paint(Graphics g) {
        int widthAll = getWidth(), heightAll = getHeight();
        Rectangle rect = g.getClipBounds();
        if (rect == null) {
            rect = new Rectangle(_bufferedImage.getWidth(), _bufferedImage.getHeight());
        }

        if (_bufferedImage != null) {
            if (widthAll != _bufferedImage.getWidth() || heightAll != _bufferedImage.getHeight()) {
                _bufferedImage = null;
                _bufferedImageGraphics.dispose();
                _bufferedImageGraphics = null;
            }
        }

        if (_bufferedImage == null) {
            _bufferedImage = new BufferedImage(widthAll, heightAll, BufferedImage.TYPE_3BYTE_BGR);
            _bufferedImageGraphics = _bufferedImage.getGraphics();
        }

        paintOnBuffer(rect);

        if (_percent == 100) {
            g.drawImage(_bufferedImage,
                    rect.x, rect.y, rect.x + rect.width, rect.y + rect.height,
                    rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, this);
            return;
        }

        g.setColor(Color.gray);
        g.fillRect(0, 0, rect.width, rect.height);
        int dy = heightAll * (100 - _percent) / 100;

        g.drawImage(_bufferedImage, 0, 0, widthAll, heightAll, 0, dy, widthAll, heightAll + dy, this);
    }

    private void paintOnBuffer(Rectangle rect) {
        int widthAll = getWidth();
        int heightAll = getHeight();

        if (rect == null) {
            rect = new Rectangle(widthAll, heightAll);
        }
        paintOnGraphics(_bufferedImageGraphics, rect);
    }

    private void paintOnGraphics(Graphics g, Rectangle rect) {
        g.setClip(rect.x, rect.y, rect.width, rect.height);
        super.paint(g);
    }
}
